<template>
  <div style="max-width: 440px; margin: 3rem auto;">
    <div class="glass-panel" style="padding: 2.5rem;">
      <h2 style="font-size: 1.75rem; font-weight: 800; margin-bottom: 0.5rem; text-align: center;">Đăng nhập</h2>
      <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 2rem; text-align: center;">Tài liệu hợp đồng kinh doanh bảo mật</p>

      <div v-if="errorMessage" style="background: rgba(239, 68, 68, 0.15); border: 1px solid rgba(239, 68, 68, 0.3); color: #f87171; padding: 0.75rem 1rem; border-radius: 10px; font-size: 0.875rem; margin-bottom: 1.5rem;">
        {{ errorMessage }}
      </div>

      <form @submit.prevent="handleLogin" style="display: flex; flex-direction: column; gap: 1.25rem;">
        <div>
          <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 0.5rem;">Tên đăng nhập</label>
          <input v-model="form.username" type="text" class="glass-input" placeholder="Nhập tên đăng nhập" required />
        </div>

        <div>
          <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 0.5rem;">Mật khẩu</label>
          <input v-model="form.password" type="password" class="glass-input" placeholder="••••••••" required />
        </div>

        <button type="submit" class="glass-button" style="margin-top: 0.5rem; width: 100%;" :disabled="loading">
          {{ loading ? 'Đang xác thực...' : 'Đăng nhập' }}
        </button>
      </form>

      <div style="margin-top: 2rem; border-top: 1px solid var(--border-card); padding-top: 1.5rem; text-align: center; font-size: 0.85rem; color: var(--text-muted);">
        Chưa có tài khoản?
        <NuxtLink to="/register" style="color: var(--accent-primary); font-weight: 600; text-decoration: none;">Đăng ký ngay</NuxtLink>
      </div>

      <!-- Quick Demo Users Test Helper -->
      <div style="margin-top: 1.5rem; background: rgba(255,255,255,0.03); border: 1px dashed var(--border-card); border-radius: 10px; padding: 1rem; font-size: 0.8rem;">
        <div style="font-weight: 700; margin-bottom: 0.5rem; color: var(--text-main);">🔑 Demo Accounts (Pre-seeded):</div>
        <div style="display: flex; flex-direction: column; gap: 0.35rem; color: var(--text-muted);">
          <div><code>sales_staff_a</code> / <code>password123</code> (Staff - Sales)</div>
          <div><code>sales_staff_b</code> / <code>password123</code> (Staff - Sales)</div>
          <div><code>mkt_staff</code> / <code>password123</code> (Staff - Mkt)</div>
          <div><code>admin</code> / <code>admin123</code> (Admin - Executive)</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const { setAuthData } = useAuth()
const loading = ref(false)
const errorMessage = ref('')

const form = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  loading.value = true
  errorMessage.value = ''
  try {
    const res = await $fetch<{ accessToken: string; user: any }>('http://localhost:8080/api/auth/login', {
      method: 'POST',
      body: form,
      credentials: 'include'
    })
    setAuthData(res.accessToken, res.user)
    navigateTo('/documents')
  } catch (e: any) {
    errorMessage.value = e.data?.message || 'Tên đăng nhập hoặc mật khẩu không chính xác'
  } finally {
    loading.value = false
  }
}
</script>
