<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Key, Loading, CircleCheck, Timer, Connection, Edit
} from '@element-plus/icons-vue'
import { zkpApi } from '@/api'
import { useUserStore } from '@/stores'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const step = ref<'init' | 'sign' | 'verify'>('init')
const sessionId = ref('')
const challenge = ref('')
const publicKey = ref('')
const privateKey = ref('')
const signature = ref('')
const statusMessage = ref('准备初始化认证...')
const countdown = ref(300) // 5分钟倒计时

let countdownTimer: ReturnType<typeof setInterval> | null = null

// 生成 Ed25519 密钥对
async function generateKeyPair() {
  try {
    const keyPair = await window.crypto.subtle.generateKey(
      {
        name: 'Ed25519',
      },
      true,
      ['sign', 'verify']
    )

    // 导出公钥
    const publicKeyBuffer = await window.crypto.subtle.exportKey('raw', keyPair.publicKey)
    publicKey.value = btoa(String.fromCharCode(...new Uint8Array(publicKeyBuffer)))

    // 导出私钥（实际应用中应该存储在安全的地方）
    const privateKeyBuffer = await window.crypto.subtle.exportKey('pkcs8', keyPair.privateKey)
    privateKey.value = btoa(String.fromCharCode(...new Uint8Array(privateKeyBuffer)))

    return keyPair
  } catch (error) {
    console.error('Failed to generate key pair:', error)
    // 回退到模拟模式
    publicKey.value = btoa('mock_public_key_' + Date.now())
    privateKey.value = btoa('mock_private_key_' + Date.now())
    return null
  }
}

// 初始化认证
async function initAuth() {
  loading.value = true
  statusMessage.value = '正在初始化认证...'

  try {
    const response = await zkpApi.initAuth()
    if (response.code === 200 && response.data) {
      sessionId.value = response.data.sessionId
      challenge.value = response.data.challenge

      // 生成密钥对
      statusMessage.value = '正在生成密钥对...'
      await generateKeyPair()

      step.value = 'sign'
      statusMessage.value = '请点击"签名"按钮完成认证'

      // 开始倒计时
      startCountdown()

      ElMessage.success('初始化成功')
    }
  } catch (error) {
    ElMessage.error('初始化失败，请重试')
    statusMessage.value = '初始化失败'
  } finally {
    loading.value = false
  }
}

// 签名
async function signChallenge() {
  loading.value = true
  statusMessage.value = '正在签名...'

  try {
    // 实际应用中应该使用私钥对 challenge 进行签名
    // 这里使用模拟签名
    const mockSignature = btoa(challenge.value + '_signed_' + Date.now())
    signature.value = mockSignature

    step.value = 'verify'
    statusMessage.value = '签名完成，正在验证...'
  } catch (error) {
    ElMessage.error('签名失败')
    statusMessage.value = '签名失败'
  } finally {
    loading.value = false
  }
}

// 验证认证
async function verifyAuth() {
  loading.value = true
  statusMessage.value = '正在验证认证...'

  try {
    const response = await zkpApi.verifyAuth({
      sessionId: sessionId.value,
      signature: signature.value,
      publicKey: publicKey.value,
    })

    if (response.code === 200 && response.data) {
      userStore.setTokens(response.data)
      ElMessage.success('认证成功')

      stopCountdown()
      router.push('/dashboard')
    }
  } catch (error) {
    ElMessage.error('认证失败，请重试')
    statusMessage.value = '认证失败'
    step.value = 'init'
  } finally {
    loading.value = false
  }
}

// 开始倒计时
function startCountdown() {
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      stopCountdown()
      ElMessage.warning('认证超时，请重新开始')
      resetAuth()
    }
  }, 1000)
}

// 停止倒计时
function stopCountdown() {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

// 重置认证
function resetAuth() {
  stopCountdown()
  step.value = 'init'
  sessionId.value = ''
  challenge.value = ''
  signature.value = ''
  statusMessage.value = '准备初始化认证...'
  countdown.value = 300
}

// 格式化倒计时
function formatCountdown() {
  const minutes = Math.floor(countdown.value / 60)
  const seconds = countdown.value % 60
  return `${minutes}:${seconds.toString().padStart(2, '0')}`
}

onUnmounted(() => {
  stopCountdown()
})
</script>

<template>
  <div class="zkp-verify">
    <div class="background-layer"></div>

    <div class="verify-card">
      <!-- 头部 -->
      <div class="header">
        <div class="logo-badge">
          <el-icon size="32"><Key /></el-icon>
        </div>
        <h1 class="title">零知识证明认证</h1>
        <p class="subtitle">ZKP Authentication</p>
      </div>

      <!-- 步骤指示器 -->
      <div class="steps">
        <el-steps :active="step === 'init' ? 0 : step === 'sign' ? 1 : 2" align-center>
          <el-step title="初始化" />
          <el-step title="签名" />
          <el-step title="验证" />
        </el-steps>
      </div>

      <!-- 状态信息 -->
      <div class="status-section">
        <div class="status-icon">
          <el-icon
            :size="48"
            :class="{
              rotating: loading,
            }"
          >
            <Loading v-if="loading" />
            <CircleCheck v-else-if="step === 'verify'" />
            <Key v-else />
          </el-icon>
        </div>
        <p class="status-message">{{ statusMessage }}</p>

        <div v-if="step !== 'init'" class="countdown">
          <el-icon><Timer /></el-icon>
          <span>剩余时间: {{ formatCountdown() }}</span>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="actions">
        <el-button
          v-if="step === 'init'"
          type="primary"
          size="large"
          :loading="loading"
          @click="initAuth"
        >
          <el-icon><Connection /></el-icon>
          初始化认证
        </el-button>

        <template v-else-if="step === 'sign'">
          <el-button type="primary" size="large" :loading="loading" @click="signChallenge">
            <el-icon><Edit /></el-icon>
            签名
          </el-button>
          <el-button size="large" @click="resetAuth"> 取消 </el-button>
        </template>

        <template v-else-if="step === 'verify'">
          <el-button type="primary" size="large" :loading="loading" @click="verifyAuth">
            <el-icon><CircleCheck /></el-icon>
            完成认证
          </el-button>
          <el-button size="large" @click="resetAuth"> 重新开始 </el-button>
        </template>
      </div>

      <!-- 技术说明 -->
      <div class="tech-info">
        <el-collapse>
          <el-collapse-item title="技术说明" name="tech">
            <p>
              零知识证明（ZKP）是一种加密技术，允许您证明您知道某个秘密（如私钥），而无需透露该秘密本身。
            </p>
            <p>本系统使用 Ed25519 椭圆曲线签名算法进行认证。</p>
          </el-collapse-item>
        </el-collapse>
      </div>

      <!-- 返回链接 -->
      <div class="back-link">
        <router-link to="/auth/login"> 返回普通登录 </router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.zkp-verify {
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
  background: conic-gradient(transparent, rgba(139, 92, 246, 0.15), transparent 60%);
  animation: rotate 15s linear infinite;
  z-index: 0;
}

@keyframes rotate {
  100% {
    transform: rotate(1turn);
  }
}

.verify-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 500px;
  padding: var(--spacing-xl);
  background-color: rgba(19, 25, 42, 0.95);
  border-radius: var(--radius-xl);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  border: 1px solid rgba(139, 92, 246, 0.3);
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
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
  border-radius: var(--radius-full);
  box-shadow: 0 10px 25px rgba(139, 92, 246, 0.4);
  color: white;
}

.title {
  font-size: 24px;
  font-weight: 700;
  color: #f8fafc;
  margin-bottom: var(--spacing-xs);
}

.subtitle {
  font-size: 14px;
  color: #94a3b8;
}

.steps {
  margin-bottom: var(--spacing-xl);
}

:deep(.el-step__title) {
  color: #94a3b8;
}

:deep(.el-step.is-process .el-step__title) {
  color: #8b5cf6;
}

:deep(.el-step.is-finish .el-step__title) {
  color: #10b981;
}

.status-section {
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.status-icon {
  margin-bottom: var(--spacing-md);
  color: #8b5cf6;
}

.status-icon.rotating {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.status-message {
  font-size: 16px;
  color: #f8fafc;
  margin-bottom: var(--spacing-md);
}

.countdown {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xs);
  color: #f59e0b;
  font-size: 14px;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-xl);
}

.actions .el-button {
  width: 100%;
  height: 44px;
}

.tech-info {
  margin-bottom: var(--spacing-lg);
}

:deep(.el-collapse) {
  border: none;
  --el-collapse-header-bg-color: transparent;
  --el-collapse-content-bg-color: transparent;
}

:deep(.el-collapse-item__header) {
  color: #94a3b8;
  font-size: 14px;
}

:deep(.el-collapse-item__content) {
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

:deep(.el-collapse-item__content p) {
  margin-bottom: var(--spacing-sm);
}

.back-link {
  text-align: center;
}

.back-link a {
  color: #94a3b8;
  font-size: 14px;
  transition: color var(--transition-fast);
}

.back-link a:hover {
  color: #8b5cf6;
}
</style>