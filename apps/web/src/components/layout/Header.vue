<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore, useAppStore } from '@/stores'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  Fold, Expand, Sunny, Moon, User, Lock,
  SwitchButton, ArrowDown, Bell, Document, Position, HomeFilled
} from '@element-plus/icons-vue'

const emit = defineEmits<{
  'toggle-sidebar': []
}>()

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    await userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/auth/login')
  } catch {
    // 用户取消
  }
}

function handleCommand(command: string) {
  switch (command) {
    case 'profile':
      router.push('/user/profile')
      break
    case 'security':
      router.push('/user/security')
      break
    case 'logout':
      handleLogout()
      break
  }
}
</script>

<template>
  <header class="header">
    <div class="left">
      <el-button text @click="emit('toggle-sidebar')" class="toggle-btn">
        <el-icon size="20">
          <Fold v-if="!appStore.sidebarCollapsed" />
          <Expand v-else />
        </el-icon>
      </el-button>

      <!-- 面包屑 -->
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/dashboard' }">
          <el-icon><HomeFilled /></el-icon>
          首页
        </el-breadcrumb-item>
        <el-breadcrumb-item v-for="item in $route.matched.slice(1)" :key="item.path">
          {{ item.meta.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="right">
      <!-- 快捷操作 -->
      <el-tooltip content="跨境支付" placement="bottom">
        <el-button text circle class="action-btn" @click="router.push('/payment/transfer')">
          <el-icon><Position /></el-icon>
        </el-button>
      </el-tooltip>

      <!-- 主题切换 -->
      <el-tooltip :content="appStore.theme === 'dark' ? '切换到浅色模式' : '切换到深色模式'" placement="bottom">
        <el-button text circle class="action-btn" @click="appStore.setTheme(appStore.theme === 'dark' ? 'light' : 'dark')">
          <el-icon size="20">
            <Sunny v-if="appStore.theme === 'dark'" />
            <Moon v-else />
          </el-icon>
        </el-button>
      </el-tooltip>

      <!-- 分割线 -->
      <div class="divider"></div>

      <!-- 用户下拉菜单 -->
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="36" class="avatar">
            {{ userStore.userInitials }}
          </el-avatar>
          <div class="user-text">
            <span class="username">{{ userStore.nickname || userStore.username || '用户' }}</span>
            <span class="user-role">普通用户</span>
          </div>
          <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu class="user-dropdown">
            <div class="dropdown-header">
              <el-avatar :size="48" class="avatar-large">
                {{ userStore.userInitials }}
              </el-avatar>
              <div class="dropdown-user-info">
                <span class="dropdown-username">{{ userStore.nickname || userStore.username }}</span>
                <span class="dropdown-email">{{ userStore.email || 'user@example.com' }}</span>
              </div>
            </div>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>
              个人资料
            </el-dropdown-item>
            <el-dropdown-item command="security">
              <el-icon><Lock /></el-icon>
              安全设置
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 24px;
  background-color: #ffffff;
  border-bottom: 1px solid #f1f5f9;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
}

.left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.toggle-btn {
  color: #64748b;
}

.toggle-btn:hover {
  color: #667eea;
  background: #f8fafc;
}

.breadcrumb {
  display: flex;
  align-items: center;
}

.breadcrumb :deep(.el-breadcrumb__item) {
  display: flex;
  align-items: center;
}

.breadcrumb :deep(.el-breadcrumb__inner) {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #64748b;
  font-weight: 400;
}

.breadcrumb :deep(.el-breadcrumb__inner:hover) {
  color: #667eea;
}

.right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-btn {
  color: #64748b;
}

.action-btn:hover {
  color: #667eea;
  background: #f8fafc;
}

.divider {
  width: 1px;
  height: 24px;
  background: #e2e8f0;
  margin: 0 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 12px;
  transition: all 0.3s;
}

.user-info:hover {
  background: #f8fafc;
}

.avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  font-weight: 600;
}

.user-text {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a2e;
}

.user-role {
  font-size: 12px;
  color: #94a3b8;
}

.dropdown-icon {
  color: #94a3b8;
  transition: transform 0.3s;
}

.user-info:hover .dropdown-icon {
  transform: rotate(180deg);
}

/* 下拉菜单样式 */
:deep(.user-dropdown) {
  padding: 8px;
  border-radius: 16px;
  min-width: 220px;
}

.dropdown-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  margin: -8px -8px 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px 12px 0 0;
}

.avatar-large {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  font-weight: 600;
}

.dropdown-user-info {
  display: flex;
  flex-direction: column;
}

.dropdown-username {
  font-size: 15px;
  font-weight: 600;
  color: white;
}

.dropdown-email {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 8px;
  margin: 2px 0;
}

:deep(.el-dropdown-menu__item:hover) {
  background: #f8fafc;
  color: #667eea;
}
</style>