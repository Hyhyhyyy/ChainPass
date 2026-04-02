<script setup lang="ts">
// Auth 布局 - 用于登录、注册等页面
</script>

<template>
  <div class="auth-layout">
    <router-view v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </div>
</template>

<style scoped>
.auth-layout {
  min-height: 100vh;
  background-color: var(--color-bg-dark);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>