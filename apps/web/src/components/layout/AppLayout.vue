<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore, useAppStore } from '@/stores'
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

const sidebarWidth = computed(() => (appStore.sidebarCollapsed ? '64px' : '240px'))
</script>

<template>
  <div class="app-layout">
    <!-- 侧边栏 -->
    <Sidebar :collapsed="appStore.sidebarCollapsed" :width="sidebarWidth" />

    <!-- 主内容区 -->
    <div class="main-container" :style="{ marginLeft: sidebarWidth }">
      <!-- 顶部导航 -->
      <Header @toggle-sidebar="appStore.toggleSidebar" />

      <!-- 内容区 -->
      <main class="content-wrapper">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <keep-alive :include="['Dashboard']">
              <component :is="Component" :key="$route.fullPath" />
            </keep-alive>
          </transition>
        </router-view>
      </main>

      <!-- 页脚 -->
      <footer class="footer">
        <p>© {{ new Date().getFullYear() }} ChainPass 区块链身份验证系统 | 大连理工大学</p>
      </footer>
    </div>
  </div>
</template>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
  background-color: #f3f4f6;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  transition: margin-left 0.3s ease;
  min-height: 100vh;
}

.content-wrapper {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background-color: #f3f4f6;
}

.footer {
  padding: 16px;
  text-align: center;
  color: #6b7280;
  font-size: 14px;
  border-top: 1px solid #e5e7eb;
  background-color: #ffffff;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all var(--transition-normal);
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}
</style>