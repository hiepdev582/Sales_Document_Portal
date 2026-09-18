<template>
  <div style="max-width: 480px; margin: 2rem auto;">
    <div class="glass-panel" style="padding: 2.5rem;">
      <h2 style="font-size: 1.75rem; font-weight: 800; margin-bottom: 0.5rem; text-align: center;">Đăng ký tài khoản</h2>
      <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 2rem; text-align: center;">Hệ thống Quản lý Tài liệu Phòng Bán hàng</p>

      <div v-if="errorMessage" style="background: rgba(239, 68, 68, 0.15); border: 1px solid rgba(239, 68, 68, 0.3); color: #f87171; padding: 0.75rem 1rem; border-radius: 10px; font-size: 0.875rem; margin-bottom: 1.5rem;">
        {{ errorMessage }}
      </div>

      <form @submit.prevent="handleRegister" style="display: flex; flex-direction: column; gap: 1.25rem;">
        <div>
          <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 0.5rem;">Họ và tên</label>
          <input v-model="form.fullName" type="text" class="glass-input" placeholder="Ví dụ: Nguyễn Văn A" required />
        </div>

        <div>
          <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 0.5rem;">Tên đăng nhập</label>
          <input v-model="form.username" type="text" class="glass-input" placeholder="Tên đăng nhập" required />
        </div>

        <div>
          <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 0.5rem;">Mật khẩu (Argon2id/BCrypt Hash)</label>
          <input v-model="form.password" type="password" class="glass-input" placeholder="Tối thiểu 6 ký tự" required />
        </div>

        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
          <div>
            <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 0.5rem;">Vai trò (Role)</label>
            <select v-model="form.role" class="glass-input" required>
              <option value="ROLE_STAFF">STAFF</option>
              <option value="ROLE_MANAGER">MANAGER</option>
              <option value="ROLE_ADMIN">ADMIN</option>
            </select>
          </div>

          <div>
            <label style="display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 0.5rem;">Phòng ban</label>
            <select v-model="form.department" class="glass-input" required>
              <option value="SALES">SALES</option>
              <option value="MARKETING">MARKETING</option>
              <option value="FINANCE">FINANCE</option>
              <option value="IT">IT</option>
              <option value="EXECUTIVE">EXECUTIVE</option>
            </select>
          </div>
        </div>

        <button type="submit" class="glass-button" style="margin-top: 0.5rem; width: 100%;" :disabled="loading">
          {{ loading ? 'Đang khởi tạo tài khoản...' : 'Tạo tài khoản mới' }}
        </button>
      </form>

      <div style="margin-top: 2rem; border-top: 1px solid var(--border-card); padding-top: 1.5rem; text-align: center; font-size: 0.85rem; color: var(--text-muted);">
        Đã có tài khoản?
        <NuxtLink to="/login" style="color: var(--accent-primary); font-weight: 600; text-decoration: none;">Đăng nhập tại đây</NuxtLink>
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
  password: '',
  fullName: '',
  role: 'ROLE_STAFF',
  department: 'SALES'
})

const handleRegister = async () => {
  loading.value = true
  errorMessage.value = ''
  try {
    const res = await $fetch<{ accessToken: string; user: any }>('http://localhost:8080/api/auth/register', {
      method: 'POST',
      body: form,
      credentials: 'include'
    })
    setAuthData(res.accessToken, res.user)
    navigateTo('/documents')
  } catch (e: any) {
    errorMessage.value = e.data?.message || 'Đăng ký không thành công. Vui lòng kiểm tra lại.'
  } finally {
    loading.value = false
  }
}
</script>
