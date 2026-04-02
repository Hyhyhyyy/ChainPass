<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Phone } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = ref({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: ''
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
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (_rule, value, callback) => {
      if (value !== form.value.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }, trigger: 'blur' },
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
  ],
}

async function handleRegister() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      // TODO: 调用注册API
      await new Promise(resolve => setTimeout(resolve, 1000))
      ElMessage.success('注册成功，请登录')
      router.push('/auth/login')
    } catch (error) {
      ElMessage.error('注册失败，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}
</script>

<template>
  <div class="register-container">
    <!-- 背景动画 -->
    <div class="background-layer"></div>

    <!-- 注册卡片 -->
    <div class="register-card">
      <!-- Logo 和标题 -->
      <div class="header">
        <div class="logo-badge">
          <img src="@/assets/logo.svg" alt="ChainPass" />
        </div>
        <h1 class="title">创建账户</h1>
        <p class="subtitle">加入 ChainPass，开启区块链身份之旅</p>
      </div>

      <!-- 注册表单 -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="register-form"
        @submit.prevent="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input
            v-model="form.email"
            placeholder="请输入邮箱（可选）"
            :prefix-icon="Message"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请确认密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="register-btn"
            @click="handleRegister"
          >
            {{ loading ? '注册中...' : '注 册' }}
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 登录链接 -->
      <div class="login-link">
        已有账号？<router-link to="/auth/login">立即登录</router-link>
      </div>
    </div>

    <!-- 页脚 -->
    <div class="footer">
      <p>© {{ new Date().getFullYear() }} 大连理工大学区块链实验室 版权所有</p>
    </div>
  </div>
</template>

<style scoped>
.register-container {
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

.register-card {
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

.register-form {
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

.register-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border: none;
  border-radius: var(--radius-md);
}

.register-btn:hover {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
}

.login-link {
  text-align: center;
  margin-top: var(--spacing-lg);
  color: #94a3b8;
  font-size: 14px;
}

.login-link a {
  color: #60a5fa;
  font-weight: 500;
}

.login-link a:hover {
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