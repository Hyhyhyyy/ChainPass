<template>
  <div class="qr-login-container">
    <div class="qr-header">
      <h3>{{ title }}</h3>
      <p class="qr-tip">使用 ChainPass App 扫描二维码{{ actionText }}</p>
    </div>

    <div class="qr-content">
      <div v-if="loading" class="qr-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>生成二维码中...</span>
      </div>

      <div v-else-if="expired" class="qr-expired">
        <el-icon><WarningFilled /></el-icon>
        <span>二维码已过期</span>
        <el-button type="primary" size="small" @click="createQRCode">
          刷新二维码
        </el-button>
      </div>

      <div v-else class="qr-code-wrapper">
        <div class="qr-mask" :class="{ 'qr-scanned': status === 'SCANNED' }">
          <div v-if="status === 'SCANNED'" class="scanned-tip">
            <el-icon><SuccessFilled /></el-icon>
            <span>扫描成功</span>
            <p>请在手机上确认</p>
          </div>
        </div>
        <QRCodeVue3
          :value="qrContent"
          :size="200"
          :margin="2"
          :dotsOptions="{ type: 'square' }"
          :cornersSquareOptions="{ type: 'square' }"
        />
      </div>
    </div>

    <div class="qr-footer">
      <el-button text type="primary" @click="$emit('switchToPassword')">
        使用账号密码登录
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Loading, WarningFilled, SuccessFilled } from '@element-plus/icons-vue'
import QRCodeVue3 from 'qrcode-vue3'
import { qrApi, type QRSession } from '@/api/qr'
import { useUserStore } from '@/stores/modules/user'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  type?: string
  title?: string
  actionText?: string
}>()

const emit = defineEmits<{
  success: [data: any]
  switchToPassword: []
}>()

const userStore = useUserStore()

const loading = ref(true)
const expired = ref(false)
const status = ref<'PENDING' | 'SCANNED' | 'CONFIRMED' | 'EXPIRED'>('PENDING')
const qrContent = ref('')
const sessionId = ref('')

let pollTimer: ReturnType<typeof setInterval> | null = null

const actionText = computed(() => {
  switch (props.type) {
    case 'LOGIN':
      return '登录'
    case 'PAYMENT_CONFIRM':
      return '确认支付'
    case 'DID_REVOKE':
      return '确认吊销'
    default:
      return '确认操作'
  }
})

const createQRCode = async () => {
  loading.value = true
  expired.value = false
  status.value = 'PENDING'

  try {
    const response = await qrApi.create(props.type || 'LOGIN')
    if (response.code === 200 && response.data) {
      const session: QRSession = response.data
      sessionId.value = session.sessionId
      qrContent.value = session.qrContent
      startPolling()
    }
  } catch (error) {
    ElMessage.error('生成二维码失败')
  } finally {
    loading.value = false
  }
}

const startPolling = () => {
  stopPolling()
  pollTimer = setInterval(async () => {
    try {
      const response = await qrApi.getStatus(sessionId.value)
      if (response.code === 200 && response.data) {
        const session: QRSession = response.data
        status.value = session.status

        if (session.status === 'CONFIRMED') {
          stopPolling()
          // 登录成功，设置 token
          if (session.data?.accessToken) {
            userStore.setTokens({
              accessToken: session.data.accessToken,
              refreshToken: session.data.refreshToken,
              userId: session.data.userId,
              username: session.data.username,
              nickname: session.data.nickname,
              avatar: session.data.avatar,
            })
            ElMessage.success('登录成功')
            emit('success', session.data)
          } else {
            emit('success', session.data)
          }
        } else if (session.status === 'EXPIRED') {
          stopPolling()
          expired.value = true
        }
      }
    } catch (error) {
      console.error('轮询状态失败', error)
    }
  }, 1500) // 每 1.5 秒轮询一次
}

const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

onMounted(() => {
  createQRCode()
})

onUnmounted(() => {
  stopPolling()
  // 组件销毁时取消会话
  if (sessionId.value && status.value === 'PENDING') {
    qrApi.cancel(sessionId.value).catch(() => {})
  }
})
</script>

<style scoped lang="scss">
.qr-login-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.qr-header {
  text-align: center;
  margin-bottom: 24px;

  h3 {
    margin: 0 0 8px;
    font-size: 18px;
    font-weight: 600;
  }

  .qr-tip {
    margin: 0;
    color: var(--el-text-color-secondary);
    font-size: 14px;
  }
}

.qr-content {
  width: 240px;
  height: 240px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
}

.qr-loading,
.qr-expired {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: var(--el-text-color-secondary);

  .el-icon {
    font-size: 32px;
  }

  .el-icon.is-loading {
    color: var(--el-color-primary);
  }
}

.qr-code-wrapper {
  position: relative;
  padding: 20px;

  .qr-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0);
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s;

    &.qr-scanned {
      background: rgba(255, 255, 255, 0.95);

      .scanned-tip {
        opacity: 1;
      }
    }

    .scanned-tip {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
      color: var(--el-color-success);
      opacity: 0;
      transition: opacity 0.3s;

      .el-icon {
        font-size: 48px;
      }

      p {
        margin: 0;
        color: var(--el-text-color-secondary);
        font-size: 12px;
      }
    }
  }
}

.qr-footer {
  margin-top: 24px;
}
</style>