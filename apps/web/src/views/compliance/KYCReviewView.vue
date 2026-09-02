<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { kycApi, type KYCReviewResponse } from '@/api/kyc'

const loading = ref(false)
const records = ref<KYCReviewResponse[]>([])

async function loadReviews() {
  loading.value = true
  try {
    const response = await kycApi.getReviews(1)
    records.value = response.data ?? []
  } catch {
    ElMessage.error('加载审核队列失败')
  } finally {
    loading.value = false
  }
}

async function approve(record: KYCReviewResponse) {
  await ElMessageBox.confirm(
    `确认已人工核验 ${record.fullName} 的材料并批准？此操作会签发审核结论凭证。`,
    '批准申请',
    { type: 'warning' }
  )
  await kycApi.approve(record.id)
  ElMessage.success('已批准并签发凭证')
  await loadReviews()
}

async function reject(record: KYCReviewResponse) {
  const result = await ElMessageBox.prompt('请输入可供申请人理解的拒绝原因', '拒绝申请', {
    inputValidator: value => !!value?.trim() || '拒绝原因不能为空',
    type: 'warning'
  })
  await kycApi.reject(record.id, result.value.trim())
  ElMessage.success('已拒绝申请')
  await loadReviews()
}

onMounted(loadReviews)
</script>

<template>
  <el-card>
    <template #header>
      <div class="header">
        <div>
          <h2>KYC 人工审核</h2>
          <p>这里显示敏感身份字段。仅在已授权环境中使用，审核结论不等同于外部机构 KYC。</p>
        </div>
        <el-button @click="loadReviews">刷新</el-button>
      </div>
    </template>

    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="fullName" label="姓名" width="120" />
      <el-table-column prop="nationality" label="国籍" width="100" />
      <el-table-column prop="idType" label="证件类型" width="120" />
      <el-table-column prop="idNumber" label="证件号码" min-width="180" />
      <el-table-column prop="did" label="DID" min-width="240" show-overflow-tooltip />
      <el-table-column prop="submittedAt" label="提交时间" width="190" />
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <el-button type="success" link @click="approve(row)">批准</el-button>
          <el-button type="danger" link @click="reject(row)">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<style scoped>
.header { display: flex; justify-content: space-between; gap: 24px; align-items: center; }
h2 { margin: 0 0 8px; }
p { margin: 0; color: var(--el-text-color-secondary); }
</style>
