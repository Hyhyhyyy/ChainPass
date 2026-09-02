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
        meta: { title: '跨境合规支付', requiresAuth: true },
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
      {
        path: 'compliance/reviews',
        name: 'KYCReviews',
        component: () => import('@/views/compliance/KYCReviewView.vue'),
        meta: { title: 'KYC人工审核', requiresAuth: true, permission: 'compliance:kyc:audit' },
      },
      {
        path: 'compliance/payment-reviews',
        name: 'PaymentReviews',
        component: () => import('@/views/compliance/PaymentReviewView.vue'),
        meta: { title: '跨境支付复核', requiresAuth: true, permission: 'compliance:payment:audit' },
      },
      {
        path: 'competition/readiness',
        name: 'CompetitionReadiness',
        component: () => import('@/views/competition/ReadinessView.vue'),
        meta: { title: '参赛合规自检', requiresAuth: true },
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
      // 系统管理
      {
        path: 'system/users',
        name: 'UserManage',
        component: () => import('@/views/system/UserManageView.vue'),
        meta: { title: '用户管理', requiresAuth: true, permission: 'system:user:list' },
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
const whiteList = ['/auth/login', '/auth/register', '/403']

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title || 'ChainPass'} - 跨境数字身份与合规支付原型`

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

  const requiredPermission = to.meta.permission as string | undefined
  if (requiredPermission && !userStore.hasPermission(requiredPermission)) {
    next('/403')
    return
  }

  next()
})

export default router
