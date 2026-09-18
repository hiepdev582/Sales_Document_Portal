<template>
  <div style="padding: 3rem 0; text-align: center;">
    <div class="glass-panel" style="padding: 4rem 2rem; max-width: 800px; margin: 0 auto; position: relative; overflow: hidden;">
      <div style="font-size: 3.5rem; margin-bottom: 1rem;">🛡️</div>
      <h1 style="font-size: 2.5rem; font-weight: 800; margin-bottom: 1rem; background: linear-gradient(to right, #fff, #9ca3af); -webkit-background-clip: text; -webkit-text-fill-color: transparent;">
        Sales Document Portal
      </h1>
      <p style="font-size: 1.1rem; color: var(--text-muted); max-width: 600px; margin: 0 auto 2.5rem auto;">
        Ứng dụng quản lý tài liệu hợp đồng kinh doanh bảo mật cao cấp dành cho phòng Bán hàng với cơ chế mã hóa AES-256-GCM, Token Security (RTR) & Phân quyền ABAC/BFLA chống OWASP Top 10.
      </p>

      <div style="display: flex; gap: 1rem; justify-content: center;">
        <NuxtLink v-if="user" to="/documents" class="glass-button" style="text-decoration: none; padding: 0.85rem 2rem; font-size: 1rem;">
          Vào kho tài liệu hợp đồng
        </NuxtLink>
        <template v-else>
          <NuxtLink to="/login" class="glass-button" style="text-decoration: none; padding: 0.85rem 2rem; font-size: 1rem;">
            Đăng nhập ngay
          </NuxtLink>
          <NuxtLink to="/register" class="glass-button-outline" style="text-decoration: none; padding: 0.85rem 2rem; font-size: 1rem;">
            Đăng ký tài khoản
          </NuxtLink>
        </template>
      </div>

      <!-- Architecture Features Grid -->
      <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1.5rem; margin-top: 4rem; text-align: left;">
        <div style="background: rgba(255,255,255,0.02); border: 1px solid var(--border-card); border-radius: 12px; padding: 1.25rem;">
          <div style="font-size: 1.25rem; margin-bottom: 0.5rem;">🔑 Token Security</div>
          <div style="font-size: 0.825rem; color: var(--text-muted);">Access Token In-Memory + Refresh Token HttpOnly Cookie & Token Rotation (RTR).</div>
        </div>

        <div style="background: rgba(255,255,255,0.02); border: 1px solid var(--border-card); border-radius: 12px; padding: 1.25rem;">
          <div style="font-size: 1.25rem; margin-bottom: 0.5rem;">🛡️ ABAC & BFLA</div>
          <div style="font-size: 0.825rem; color: var(--text-muted);">Chống khai thác BOLA (IDOR) bằng điều kiện ABAC động và RBAC cho API xóa tài liệu.</div>
        </div>

        <div style="background: rgba(255,255,255,0.02); border: 1px solid var(--border-card); border-radius: 12px; padding: 1.25rem;">
          <div style="font-size: 1.25rem; margin-bottom: 0.5rem;">🔐 AES-256-GCM</div>
          <div style="font-size: 0.825rem; color: var(--text-muted);">Mã hóa nội dung file trước khi ghi ổ đĩa kèm Vector khởi tạo (IV) ngẫu nhiên.</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const { user } = useAuth()
</script>
