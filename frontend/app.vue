<template>
  <div style="min-height: 100vh; display: flex; flex-direction: column;">
    <Navbar />
    <main style="flex: 1; max-width: 1200px; width: 100%; margin: 0 auto; padding: 0 1.5rem 3rem 1.5rem;">
      <NuxtPage />
    </main>
  </div>
</template>

<script setup lang="ts">
const { refreshSession } = useAuth()

onMounted(async () => {
  await refreshSession()
})
</script>
