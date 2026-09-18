package com.salesportal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class RedisTokenService {

    private static final Logger log = LoggerFactory.getLogger(RedisTokenService.class);
    private static final String REVOKED_PREFIX = "revoked_token:";

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    // In-memory fallback if Redis container is not running locally
    private final Map<String, Long> fallbackBlacklist = new ConcurrentHashMap<>();

    public void revokeToken(String token, long expirationMs) {
        if (token == null || token.isBlank()) return;
        String key = REVOKED_PREFIX + token;
        try {
            if (redisTemplate != null) {
                redisTemplate.opsForValue().set(key, "revoked", expirationMs, TimeUnit.MILLISECONDS);
            } else {
                fallbackBlacklist.put(key, System.currentTimeMillis() + expirationMs);
            }
        } catch (Exception e) {
            log.warn("Redis connection failed, using in-memory token revocation list", e);
            fallbackBlacklist.put(key, System.currentTimeMillis() + expirationMs);
        }
    }

    public boolean isRevoked(String token) {
        if (token == null || token.isBlank()) return false;
        String key = REVOKED_PREFIX + token;
        try {
            if (redisTemplate != null) {
                Boolean hasKey = redisTemplate.hasKey(key);
                if (Boolean.TRUE.equals(hasKey)) return true;
            }
        } catch (Exception e) {
            log.warn("Redis check failed, falling back to local memory map");
        }

        Long exp = fallbackBlacklist.get(key);
        if (exp != null) {
            if (System.currentTimeMillis() > exp) {
                fallbackBlacklist.remove(key);
                return false;
            }
            return true;
        }
        return false;
    }
}
