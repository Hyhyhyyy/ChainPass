<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { paymentApi, type ComplianceReviewOrder } from '@/api/payment'

const loading = ref(false)
const records = ref<ComplianceReviewOrder[]>([])

const purposeLabels: Record<string, string> = {
  GOODS_SERVICES: '商品或服务贸易',
  EDUCATION: '教育支出',
  FAMILY_SUPPORT: '家庭赡养',
  TRAVEL: '旅行支出',
  BUSINESS: '商业往来',
}

async function loadReviews() {
  loading.value = true
  try {
    const response = await paymentApi.getComplianceReviews()
    records.value = response.data ?? []
  } catch {
    ElMessage.error('加载支付复核队列失败')
  } finally {
    loading.value = false
  }
}

async function review(row: ComplianceReviewOrder, decision: 'approve' | 'reject') {
  const action = decision === 'approve' ? '放行' : '拒绝'
  const result = await ElMessageBox.prompt(
    `请输入${action}订单 ${row.orderNo} 的复核依据。该意见会写入审计记录。`,
    `${action}跨境订单`,
    { inputValidator: value => !!value?.trim() || '复核意见不能为空', type: 'warning' }
  )
  if (decision === 'approve') {
    await paymentApi.approveComplianceReview(row.orderNo, result.value.trim())
  } else {
    await paymentApi.rejectComplianceReview(row.orderNo, result.value.trim())
  }
  ElMessage.success(`订单已${action}`)
  await loadReviews()
}

onMounted(loadReviews)
</script>

<template>
  <el-card>
    <template #header>
      <div class="header">
        <div>
          <h2>跨境支付人工复核</h2>
          <p>规则评分只用于演示分流；审核人仍需核验身份、受益人、用途和交易背景。</p>
        </div>
        <el-button @click="loadReviews">刷新</el-button>
      </div>
    </template>

    <el-table v-loading="loading" :data="records" stripe>
      <el-table-column prop="orderNo" label="订单号" min-width="190" show-overflow-tooltip />
      <el-table-column label="走廊" width="105">
        <template #default="{ row }">{{ row.sourceCountry }} → {{ row.targetCountry }}</template>
      </el-table-column>
      <el-table-column prop="beneficiaryName" label="受益人" width="130" />
      <el-table-column label="用途" width="145">
        <template #default="{ row }">{{ purposeLabels[row.paymentPurpose] ?? row.paymentPurpose }}</template>
      </el-table-column>
      <el-table-column label="汇出/到账" min-width="170">
        <template #default="{ row }">
          {{ row.originalAmount }} {{ row.originalCurrency }} → {{ row.amount }} {{ row.currency }}
        </template>
      </el-table-column>
      <el-table-column label="风险" width="105">
        <template #default="{ row }">
          <el-tag :type="row.riskLevel === 'HIGH' ? 'danger' : 'warning'">{{ row.riskScore }} / {{ row.riskLevel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="complianceReasons" label="触发原因" min-width="260" show-overflow-tooltip />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="success" link @click="review(row, 'approve')">放行</el-button>
          <el-button type="danger" link @click="review(row, 'reject')">拒绝</el-button>
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
