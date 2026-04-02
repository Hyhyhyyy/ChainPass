<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Promotion, Key, InfoFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores'
import type { FormInstance, FormRules } from 'element-plus'
import QRLogin from '@/components/business/QRLogin.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const activeTab = ref<'password' | 'qr' | 'oauth'>('password')

const loginForm = ref({
  username: '',
  password: '',
  remember: false,
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3-20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为 6-32 个字符', trigger: 'blur' },
  ],
}

async function handleLogin() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const success = await userStore.login({
        username: loginForm.value.username,
        password: loginForm.value.password,
      })

      if (success) {
        ElMessage.success('登录成功')
        const redirect = route.query.redirect as string
        router.push(redirect || '/dashboard')
      } else {
        ElMessage.error('用户名或密码错误')
      }
    } catch (error) {
      ElMessage.error('登录失败，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}

async function handleGiteeLogin() {
  try {
    await userStore.giteeLogin()
  } catch (error) {
    ElMessage.error('获取 OAuth 配置失败')
  }
}

function handleZKPLogin() {
  router.push('/auth/zkp-verify')
}

function handleQRLoginSuccess(data: any) {
  const redirect = route.query.redirect as string
  router.push(redirect || '/dashboard')
}

onMounted(() => {
  // 已登录则跳转到首页
  if (userStore.isTokenValid()) {
    router.push('/dashboard')
  }
})
</script>

<template>
  <div class="login-container">
    <!-- 背景动画 -->
    <div class="background-layer"></div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <!-- Logo 和标题 -->
      <div class="header">
        <div class="logo-badge">
          <img src="@/assets/logo.jpg" alt="ChainPass" />
        </div>
        <h1 class="title">ChainPass</h1>
        <p class="subtitle">区块链身份验证系统</p>
      </div>

      <!-- 登录方式切换 -->
      <el-tabs v-model="activeTab" class="login-tabs">
        <!-- 账密登录 -->
        <el-tab-pane label="账号登录" name="password">
          <el-form
            ref="formRef"
            :model="loginForm"
            :rules="rules"
            class="login-form"
            @submit.prevent="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                :prefix-icon="User"
                size="large"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>

            <el-form-item>
              <div class="form-options">
                <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
                <router-link to="/auth/forgot-password" class="forgot-link">忘记密码?</router-link>
              </div>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                class="login-btn"
                @click="handleLogin"
              >
                {{ loading ? '登录中...' : '登 录' }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 二维码登录 -->
        <el-tab-pane label="扫码登录" name="qr">
          <div class="qr-login-wrapper">
            <QRLogin
              type="LOGIN"
              title="扫码登录"
              @success="handleQRLoginSuccess"
              @switch-to-password="activeTab = 'password'"
            />
          </div>
        </el-tab-pane>

        <!-- OAuth 登录 -->
        <el-tab-pane label="快捷登录" name="oauth">
          <div class="oauth-buttons">
            <el-button
              size="large"
              class="oauth-btn gitee"
              @click="handleGiteeLogin"
            >
              <el-icon size="20"><Promotion /></el-icon>
              <span>Gitee 账号登录</span>
            </el-button>

            <el-button
              size="large"
              class="oauth-btn zkp"
              @click="handleZKPLogin"
            >
              <el-icon size="20"><Key /></el-icon>
              <span>零知识证明认证</span>
            </el-button>
          </div>

          <div class="oauth-tips">
            <el-icon><InfoFilled /></el-icon>
            <span>OAuth 登录将跳转到第三方平台进行授权</span>
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- 注册链接 -->
      <div class="register-link">
        还没有账号？<router-link to="/auth/register">立即注册</router-link>
      </div>
    </div>

    <!-- 页脚 -->
    <div class="footer">
      <p>© {{ new Date().getFullYear() }} 大连理工大学区块链实验室 版权所有</p>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: var(--spacing-lg);
  background: linear-gradient(135deg, #0a192f 0%, #172b4d 100%);
  overflow: hidden;
}

.background-layer {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: conic-gradient(transparent, rgba(29, 78, 216, 0.15), transparent 60%);
  animation: rotate 15s linear infinite;
  z-index: 0;
}

@keyframes rotate {
  100% {
    transform: rotate(1turn);
  }
}

.login-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  padding: var(--spacing-xl);
  background-color: rgba(19, 25, 42, 0.95);
  border-radius: var(--radius-xl);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  border: 1px solid rgba(56, 139, 253, 0.2);
  backdrop-filter: blur(10px);
}

.header {
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.logo-badge {
  width: 72px;
  height: 72px;
  margin: 0 auto var(--spacing-md);
  padding: var(--spacing-md);
  background: linear-gradient(135deg, #1d4ed8 0%, #0c4a6e 100%);
  border-radius: var(--radius-full);
  box-shadow: 0 10px 25px rgba(29, 78, 216, 0.4);
}

.logo-badge img {
  width: 100%;
  height: 100%;
}

.title {
  font-size: 28px;
  font-weight: 700;
  color: #f8fafc;
  margin-bottom: var(--spacing-xs);
}

.subtitle {
  font-size: 14px;
  color: #94a3b8;
}

.login-tabs {
  margin-bottom: var(--spacing-lg);
}

:deep(.el-tabs__nav-wrap::after) {
  display: none;
}

:deep(.el-tabs__item) {
  color: #94a3b8;
  font-size: 15px;
}

:deep(.el-tabs__item.is-active) {
  color: #60a5fa;
}

:deep(.el-tabs__active-bar) {
  background-color: #3b82f6;
}

.login-form {
  margin-top: var(--spacing-lg);
}

:deep(.el-input__wrapper) {
  background-color: rgba(30, 41, 59, 0.8);
  box-shadow: none;
  border: 1px solid #334155;
}

:deep(.el-input__wrapper:hover),
:deep(.el-input__wrapper.is-focus) {
  border-color: #3b82f6;
}

:deep(.el-input__inner) {
  color: #f8fafc;
}

:deep(.el-input__inner::placeholder) {
  color: #64748b;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

:deep(.el-checkbox__label) {
  color: #94a3b8;
}

.forgot-link {
  color: #60a5fa;
  font-size: 13px;
}

.forgot-link:hover {
  color: #3b82f6;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border: none;
  border-radius: var(--radius-md);
}

.login-btn:hover {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
}

.qr-login-wrapper {
  display: flex;
  justify-content: center;
  padding: var(--spacing-lg) 0;
}

.oauth-buttons {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  margin-top: var(--spacing-lg);
}

.oauth-btn {
  width: 100%;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  font-size: 15px;
  border-radius: var(--radius-md);
}

.oauth-btn.gitee {
  background-color: #c71d23;
  border-color: #c71d23;
  color: white;
}

.oauth-btn.gitee:hover {
  background-color: #a51920;
  border-color: #a51920;
}

.oauth-btn.zkp {
  background-color: rgba(30, 41, 59, 0.8);
  border-color: #334155;
  color: #f8fafc;
}

.oauth-btn.zkp:hover {
  background-color: #334155;
  border-color: #475569;
}

.oauth-tips {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  margin-top: var(--spacing-lg);
  padding: var(--spacing-md);
  background-color: rgba(30, 41, 59, 0.5);
  border-radius: var(--radius-md);
  color: #94a3b8;
  font-size: 12px;
}

.register-link {
  text-align: center;
  margin-top: var(--spacing-lg);
  color: #94a3b8;
  font-size: 14px;
}

.register-link a {
  color: #60a5fa;
  font-weight: 500;
}

.register-link a:hover {
  color: #3b82f6;
}

.footer {
  position: relative;
  z-index: 1;
  margin-top: var(--spacing-xl);
  text-align: center;
  color: #64748b;
  font-size: 12px;
}
</style>