<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores'
import {
  Odometer, Key, Postcard, Tickets, Wallet, Position,
  Document, User, Setting, UserFilled, Avatar,
  Lock, Connection, Fold, Expand
} from '@element-plus/icons-vue'

interface Props {
  collapsed: boolean
  width: string
}

defineProps<Props>()

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

// 菜单项
const menuItems = [
  {
    path: '/dashboard',
    icon: 'Odometer',
    title: '仪表盘',
    color: '#667eea'
  },
  {
    path: '/identity',
    icon: 'Key',
    title: '身份管理',
    color: '#8b5cf6',
    children: [
      { path: '/identity/did', icon: 'Postcard', title: 'DID身份' },
      { path: '/identity/vc', icon: 'Tickets', title: '可验证凭证' },
    ],
  },
  {
    path: '/payment',
    icon: 'Wallet',
    title: '支付中心',
    color: '#f59e0b',
    children: [
      { path: '/payment/wallet', icon: 'Wallet', title: '我的钱包' },
      { path: '/payment/transfer', icon: 'Position', title: '跨境支付' },
      { path: '/payment/history', icon: 'Document', title: '交易记录' },
    ],
  },
  {
    path: '/compliance',
    icon: 'Lock',
    title: '合规中心',
    color: '#10b981',
    children: [
      { path: '/compliance/kyc', icon: 'CircleCheck', title: 'KYC认证' },
    ],
  },
  {
    path: '/user/profile',
    icon: 'User',
    title: '个人中心',
    color: '#3b82f6'
  },
  {
    path: '/system',
    icon: 'Setting',
    title: '系统管理',
    color: '#64748b',
    children: [
      { path: '/system/users', icon: 'UserFilled', title: '用户管理' },
      { path: '/system/roles', icon: 'Avatar', title: '角色管理' },
      { path: '/system/permissions', icon: 'Lock', title: '权限管理' },
      { path: '/system/oauth', icon: 'Connection', title: 'OAuth配置' },
      { path: '/system/logs', icon: 'Document', title: '日志查询' },
    ],
  },
]

const activeMenu = computed(() => route.path)

function navigateTo(path: string) {
  router.push(path)
}
</script>

<template>
  <aside class="sidebar" :style="{ width }">
    <!-- Logo -->
    <div class="logo-container" @click="navigateTo('/dashboard')">
      <div class="logo-icon">
        <img src="@/assets/logo.svg" alt="ChainPass" class="logo" />
      </div>
      <transition name="fade">
        <span v-if="!collapsed" class="logo-text">ChainPass</span>
      </transition>
    </div>

    <!-- 菜单 -->
    <el-scrollbar class="menu-scrollbar">
      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        :collapse-transition="false"
        background-color="transparent"
        text-color="#64748b"
        active-text-color="#667eea"
        router
        class="sidebar-menu"
      >
        <template v-for="item in menuItems" :key="item.path">
          <!-- 有子菜单 -->
          <el-sub-menu v-if="item.children" :index="item.path" :popper-class="'sidebar-popper'">
            <template #title>
              <div class="menu-item-title">
                <el-icon class="menu-icon" :style="{ color: item.color }">
                  <component :is="item.icon" />
                </el-icon>
                <span>{{ item.title }}</span>
              </div>
            </template>
            <el-menu-item v-for="child in item.children" :key="child.path" :index="child.path">
              <el-icon><component :is="child.icon" /></el-icon>
              <span>{{ child.title }}</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 无子菜单 -->
          <el-menu-item v-else :index="item.path">
            <el-icon class="menu-icon" :style="{ color: item.color }">
              <component :is="item.icon" />
            </el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-scrollbar>

    <!-- 折叠按钮 -->
    <div class="collapse-trigger" @click="appStore.toggleSidebar">
      <el-icon :size="20">
        <component :is="collapsed ? Expand : Fold" />
      </el-icon>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  border-right: none;
  transition: width 0.3s ease;
  overflow: hidden;
  z-index: 100;
  display: flex;
  flex-direction: column;
}

.logo-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  height: 72px;
  padding: 16px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  flex-shrink: 0;
}

.logo {
  width: 24px;
  height: 24px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  white-space: nowrap;
}

.menu-scrollbar {
  flex: 1;
  overflow: hidden;
}

.sidebar-menu {
  border-right: none;
  padding: 8px;
}

.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-sub-menu__title) {
  height: 48px;
  line-height: 48px;
  border-radius: 12px;
  margin-bottom: 4px;
  transition: all 0.3s;
}

.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.08);
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: rgba(102, 126, 234, 0.15);
  color: #667eea;
}

.sidebar-menu :deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 24px;
  background: linear-gradient(180deg, #667eea, #764ba2);
  border-radius: 0 4px 4px 0;
}

.menu-item-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.menu-icon {
  font-size: 20px;
}

.sidebar-menu :deep(.el-sub-menu .el-menu-item) {
  padding-left: 52px !important;
}

.collapse-trigger {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 48px;
  color: #64748b;
  cursor: pointer;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s;
}

.collapse-trigger:hover {
  color: #667eea;
  background: rgba(255, 255, 255, 0.05);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>