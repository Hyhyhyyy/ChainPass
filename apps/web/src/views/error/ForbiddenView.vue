<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="forbidden">
    <div class="content">
      <h1>403</h1>
      <h2>无权访问</h2>
      <p>抱歉，您没有权限访问此页面</p>
      <el-button type="primary" @click="router.push('/dashboard')">返回首页</el-button>
    </div>
  </div>
</template>

<style scoped>
.forbidden {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background-color: var(--color-bg-secondary);
}

.content {
  text-align: center;
}

h1 {
  font-size: 120px;
  font-weight: 700;
  color: var(--color-danger);
  line-height: 1;
  margin-bottom: 16px;
}

h2 {
  font-size: 32px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 8px;
}

p {
  color: var(--color-text-secondary);
  margin-bottom: 24px;
}
</style>