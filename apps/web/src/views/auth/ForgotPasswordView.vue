<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, Key, ArrowLeft } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()

const formRef = ref<FormInstance>()
const loading = ref(false)
const step = ref(1) // 1: 输入邮箱, 2: 已发送

const form = ref({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const rules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
  ],
}

async function handleSendCode() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      // TODO: 调用发送验证码API
      await new Promise(resolve => setTimeout(resolve, 1000))
      step.value = 2
      ElMessage.success('验证码已发送到您的邮箱')
    } catch (error) {
      ElMessage.error('发送失败，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}

async function handleReset() {
  loading.value = true
  try {
    // TODO: 调用重置密码API
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('密码重置成功，请登录')
    router.push('/auth/login')
  } catch (error) {
    ElMessage.error('重置失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="forgot-container">
    <!-- 背景动画 -->
    <div class="background-layer"></div>

    <!-- 卡片 -->
    <div class="forgot-card">
      <!-- Logo 和标题 -->
      <div class="header">
        <div class="logo-badge">
          <img src="@/assets/logo.svg" alt="ChainPass" />
        </div>
        <h1 class="title">找回密码</h1>
        <p class="subtitle">输入您的邮箱地址，我们将发送重置链接</p>
      </div>

      <!-- 步骤1：输入邮箱 -->
      <template v-if="step === 1">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="forgot-form"
          @submit.prevent="handleSendCode"
        >
          <el-form-item prop="email">
            <el-input
              v-model="form.email"
              placeholder="请输入注册时的邮箱"
              :prefix-icon="Message"
              size="large"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="submit-btn"
              @click="handleSendCode"
            >
              {{ loading ? '发送中...' : '发送验证码' }}
            </el-button>
          </el-form-item>
        </el-form>
      </template>

      <!-- 步骤2：重置密码 -->
      <template v-else>
        <div class="success-info">
          <el-icon class="success-icon"><Key /></el-icon>
          <p>验证码已发送至 <strong>{{ form.email }}</strong></p>
        </div>

        <el-form class="forgot-form" @submit.prevent="handleReset">
          <el-form-item>
            <el-input
              v-model="form.code"
              placeholder="请输入验证码"
              size="large"
            />
          </el-form-item>

          <el-form-item>
            <el-input
              v-model="form.newPassword"
              type="password"
              placeholder="请输入新密码"
              :prefix-icon="Key"
              size="large"
              show-password
            />
          </el-form-item>

          <el-form-item>
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请确认新密码"
              :prefix-icon="Key"
              size="large"
              show-password
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="submit-btn"
              @click="handleReset"
            >
              {{ loading ? '重置中...' : '重置密码' }}
            </el-button>
          </el-form-item>
        </el-form>
      </template>

      <!-- 返回链接 -->
      <div class="back-link">
        <router-link to="/auth/login">
          <el-icon><ArrowLeft /></el-icon>
          返回登录
        </router-link>
      </div>
    </div>

    <!-- 页脚 -->
    <div class="footer">
      <p>© {{ new Date().getFullYear() }} 大连理工大学区块链实验室 版权所有</p>
    </div>
  </div>
</template>

<style scoped>
.forgot-container {
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

.forgot-card {
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

.forgot-form {
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

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border: none;
  border-radius: var(--radius-md);
}

.submit-btn:hover {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
}

.success-info {
  text-align: center;
  padding: var(--spacing-lg);
  background: rgba(16, 185, 129, 0.1);
  border-radius: var(--radius-md);
  margin-bottom: var(--spacing-lg);
  color: #10b981;
}

.success-icon {
  font-size: 32px;
  margin-bottom: var(--spacing-sm);
}

.success-info p {
  color: #94a3b8;
  font-size: 14px;
}

.success-info strong {
  color: #f8fafc;
}

.back-link {
  text-align: center;
  margin-top: var(--spacing-lg);
}

.back-link a {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #94a3b8;
  font-size: 14px;
}

.back-link a:hover {
  color: #60a5fa;
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