package com.salesportal.service;

import com.salesportal.dto.AuthRequest;
import com.salesportal.dto.AuthResponse;
import com.salesportal.dto.RegisterRequest;
import com.salesportal.dto.UserDto;
import com.salesportal.entity.User;
import com.salesportal.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final RedisTokenService redisTokenService;

    @Value("${app.jwt.cookie-name:refresh_token}")
    private String cookieName;

    @Value("${app.jwt.secure-cookie:false}")
    private boolean secureCookie;

    public AuthService(UserRepository userRepository,
                       PasswordService passwordService,
                       JwtService jwtService,
                       RedisTokenService redisTokenService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
        this.redisTokenService = redisTokenService;
    }

    public AuthResponse register(RegisterRequest request, HttpServletResponse response) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        String hashedPassword = passwordService.hashPassword(request.getPassword());
        User user = new User(
                request.getUsername(),
                hashedPassword,
                request.getFullName(),
                request.getRole(),
                request.getDepartment()
        );

        User savedUser = userRepository.save(user);
        return generateTokensAndSetCookie(savedUser, response);
    }

    public AuthResponse login(AuthRequest request, HttpServletResponse response) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordService.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        return generateTokensAndSetCookie(user, response);
    }

    public AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken == null || !jwtService.validateToken(refreshToken)) {
            throw new BadCredentialsException("Invalid or missing Refresh Token");
        }

        if (redisTokenService.isRevoked(refreshToken)) {
            throw new BadCredentialsException("Refresh Token has been revoked (Token Reuse Detected)");
        }

        // Token Rotation: Revoke old Refresh Token immediately
        redisTokenService.revokeToken(refreshToken, jwtService.getRefreshTokenExpirationMs());

        String username = jwtService.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        return generateTokensAndSetCookie(user, response);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            redisTokenService.revokeToken(accessToken, jwtService.getAccessTokenExpirationMs());
        }

        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken != null) {
            redisTokenService.revokeToken(refreshToken, jwtService.getRefreshTokenExpirationMs());
        }

        // Clear Cookie
        ResponseCookie cleanCookie = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cleanCookie.toString());
    }

    private AuthResponse generateTokensAndSetCookie(User user, HttpServletResponse response) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Set HttpOnly, SameSite=Strict, Secure Refresh Token Cookie
        ResponseCookie cookie = ResponseCookie.from(cookieName, refreshToken)
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .maxAge(jwtService.getRefreshTokenExpirationMs() / 1000)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return new AuthResponse(accessToken, new UserDto(user));
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
