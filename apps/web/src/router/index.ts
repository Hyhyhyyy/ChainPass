import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/modules/user'

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/auth',
    name: 'Auth',
    component: () => import('@/views/auth/AuthLayout.vue'),
    redirect: '/auth/login',
    children: [
      {
        path: 'login',
        name: 'Login',
        component: () => import('@/views/auth/LoginView.vue'),
        meta: { title: '登录', requiresAuth: false },
      },
      {
        path: 'register',
        name: 'Register',
        component: () => import('@/views/auth/RegisterView.vue'),
        meta: { title: '注册', requiresAuth: false },
      },
      {
        path: 'forgot-password',
        name: 'ForgotPassword',
        component: () => import('@/views/auth/ForgotPasswordView.vue'),
        meta: { title: '忘记密码', requiresAuth: false },
      },
      {
        path: 'zkp-verify',
        name: 'ZKPVerify',
        component: () => import('@/views/auth/ZKPVerifyView.vue'),
        meta: { title: '身份认证', requiresAuth: false },
      },
      {
        path: 'oauth/callback',
        name: 'OAuthCallback',
        component: () => import('@/views/auth/OAuthCallbackView.vue'),
        meta: { title: 'OAuth回调', requiresAuth: false },
      },
    ],
  },
  {
    path: '/',
    component: () => import('@/components/layout/AppLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/system/DashboardView.vue'),
        meta: { title: '仪表盘', requiresAuth: true },
      },
      // 身份管理
      {
        path: 'identity/did',
        name: 'DIDManage',
        component: () => import('@/views/identity/DIDManageView.vue'),
        meta: { title: 'DID身份管理', requiresAuth: true },
      },
      {
        path: 'identity/vc',
        name: 'VCList',
        component: () => import('@/views/identity/VCListView.vue'),
        meta: { title: '可验证凭证', requiresAuth: true },
      },
      // 支付中心
      {
        path: 'payment/wallet',
        name: 'Wallet',
        component: () => import('@/views/payment/WalletView.vue'),
        meta: { title: '我的钱包', requiresAuth: true },
      },
      {
        path: 'payment/transfer',
        name: 'Transfer',
        component: () => import('@/views/payment/TransferView.vue'),
        meta: { title: '跨境支付', requiresAuth: true },
      },
      {
        path: 'payment/history',
        name: 'PaymentHistory',
        component: () => import('@/views/payment/HistoryView.vue'),
        meta: { title: '交易记录', requiresAuth: true },
      },
      // 合规中心
      {
        path: 'compliance/kyc',
        name: 'KYCApply',
        component: () => import('@/views/compliance/KYCApplyView.vue'),
        meta: { title: 'KYC认证', requiresAuth: true },
      },
      // 用户中心
      {
        path: 'user/profile',
        name: 'Profile',
        component: () => import('@/views/user/ProfileView.vue'),
        meta: { title: '个人资料', requiresAuth: true },
      },
      {
        path: 'user/security',
        name: 'Security',
        component: () => import('@/views/user/SecurityView.vue'),
        meta: { title: '安全设置', requiresAuth: true },
      },
      {
        path: 'user/devices',
        name: 'Devices',
        component: () => import('@/views/user/DevicesView.vue'),
        meta: { title: '登录设备', requiresAuth: true },
      },
      {
        path: 'user/logs',
        name: 'UserLogs',
        component: () => import('@/views/user/LogsView.vue'),
        meta: { title: '操作日志', requiresAuth: true },
      },
      // 系统管理
      {
        path: 'system/users',
        name: 'UserManage',
        component: () => import('@/views/system/UserManageView.vue'),
        meta: { title: '用户管理', requiresAuth: true, permission: 'system:user:list' },
      },
      {
        path: 'system/roles',
        name: 'RoleManage',
        component: () => import('@/views/system/RoleManageView.vue'),
        meta: { title: '角色管理', requiresAuth: true, permission: 'system:role:list' },
      },
      {
        path: 'system/permissions',
        name: 'PermissionManage',
        component: () => import('@/views/system/PermissionView.vue'),
        meta: { title: '权限管理', requiresAuth: true, permission: 'system:permission:list' },
      },
      {
        path: 'system/oauth',
        name: 'OAuthConfig',
        component: () => import('@/views/system/OAuthConfigView.vue'),
        meta: { title: 'OAuth配置', requiresAuth: true, permission: 'system:oauth:config' },
      },
      {
        path: 'system/logs',
        name: 'SystemLogs',
        component: () => import('@/views/system/LogQueryView.vue'),
        meta: { title: '日志查询', requiresAuth: true, permission: 'system:log:list' },
      },
    ],
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/ForbiddenView.vue'),
    meta: { title: '无权访问', requiresAuth: false },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFoundView.vue'),
    meta: { title: '页面不存在', requiresAuth: false },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0 }
  },
})

// 白名单路由
const whiteList = ['/auth/login', '/auth/register', '/auth/forgot-password', '/auth/zkp-verify', '/auth/oauth/callback', '/403']

// 预览模式：设为 true 可直接访问所有页面（仅供开发预览）
const PREVIEW_MODE = true

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title || 'ChainPass'} - 区块链身份验证系统`

  // 预览模式：直接放行所有页面
  if (PREVIEW_MODE) {
    next()
    return
  }

  const userStore = useUserStore()

  // 白名单路由直接放行
  if (whiteList.includes(to.path)) {
    // 已登录用户访问登录页，重定向到首页
    if (to.path === '/auth/login' && userStore.isTokenValid()) {
      next('/dashboard')
      return
    }
    next()
    return
  }

  // 检查登录状态
  if (!userStore.isTokenValid()) {
    next(`/auth/login?redirect=${encodeURIComponent(to.fullPath)}`)
    return
  }

  next()
})

export default router