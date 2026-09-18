<template>
  <header
    class="glass-panel"
    style="
      margin-bottom: 2rem;
      border-radius: 0 0 16px 16px;
      padding: 1rem 2rem;
    "
  >
    <div
      style="
        display: flex;
        justify-content: space-between;
        align-items: center;
        max-width: 1200px;
        margin: 0 auto;
      "
    >
      <NuxtLink
        to="/"
        style="
          display: flex;
          align-items: center;
          gap: 0.75rem;
          text-decoration: none;
          color: inherit;
        "
      >
        <div
          style="
            background: linear-gradient(135deg, #6366f1, #8b5cf6);
            padding: 0.5rem;
            border-radius: 10px;
            display: flex;
          "
        >
          <svg
            width="24"
            height="24"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
          </svg>
        </div>
        <div>
          <h1
            style="
              font-size: 1.25rem;
              font-weight: 800;
              background: linear-gradient(to right, #fff, #9ca3af);
              -webkit-background-clip: text;
              -webkit-text-fill-color: transparent;
            "
          >
            Sales Document Portal
          </h1>
          <p style="font-size: 0.75rem; color: var(--text-muted)">
            Secure Resource Management
          </p>
        </div>
      </NuxtLink>

      <div v-if="user" style="display: flex; align-items: center; gap: 1.5rem">
        <div style="text-align: right">
          <!-- Security Escaping Notice: Vue templates escape text automatically to prevent XSS -->
          <div style="font-weight: 600; font-size: 0.95rem">
            {{ user.fullName }}
          </div>
          <div
            style="
              display: flex;
              gap: 0.5rem;
              justify-content: flex-end;
              margin-top: 0.25rem;
            "
          >
            <span :class="['badge', getRoleBadgeClass(user.role)]">{{
              user.role.replace("ROLE_", "")
            }}</span>
            <span class="badge badge-internal">{{ user.department }}</span>
          </div>
        </div>

        <button
          @click="logout"
          class="glass-button-outline"
          style="padding: 0.5rem 1rem; font-size: 0.85rem"
        >
          Đăng xuất
        </button>
      </div>

      <div v-else style="display: flex; gap: 1rem">
        <NuxtLink
          to="/login"
          class="glass-button-outline"
          style="text-decoration: none; padding: 0.5rem 1.25rem"
          >Đăng nhập</NuxtLink
        >
        <NuxtLink
          to="/register"
          class="glass-button"
          style="text-decoration: none; padding: 0.5rem 1.25rem"
          >Đăng ký</NuxtLink
        >
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
const { user, logout } = useAuth();

const getRoleBadgeClass = (role: string) => {
  if (role === "ROLE_ADMIN") return "badge-admin";
  if (role === "ROLE_MANAGER") return "badge-manager";
  return "badge-staff";
};
</script>
