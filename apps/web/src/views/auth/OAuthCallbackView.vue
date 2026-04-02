<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

onMounted(async () => {
  const code = route.query.code as string

  if (!code) {
    ElMessage.error('授权码无效')
    router.push('/auth/login')
    return
  }

  try {
    const success = await userStore.handleOAuthCallback(code)
    if (success) {
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } else {
      ElMessage.error('OAuth 登录失败')
      router.push('/auth/login')
    }
  } catch (error) {
    ElMessage.error('OAuth 登录异常')
    router.push('/auth/login')
  }
})
</script>

<template>
  <div class="oauth-callback">
    <div class="loading">
      <el-icon class="is-loading" size="48"><Loading /></el-icon>
      <p>正在处理 OAuth 回调...</p>
    </div>
  </div>
</template>

<style scoped>
.oauth-callback {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #0a192f 0%, #172b4d 100%);
}

.loading {
  text-align: center;
  color: white;
}

.loading p {
  margin-top: 16px;
  font-size: 16px;
  color: #94a3b8;
}
</style>