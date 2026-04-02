<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Tickets, Document, CircleCheck, CircleClose, Clock } from '@element-plus/icons-vue'
import { vcApi, type VCResponse, type VCType, type VerifyResult } from '@/api/vc'
import { didApi } from '@/api/did'

// 状态
const loading = ref(false)
const vcList = ref<VCResponse[]>([])
const vcTypes = ref<VCType[]>([])
const verifyDialogVisible = ref(false)
const verifyLoading = ref(false)
const verifyResult = ref<VerifyResult | null>(null)
const verifyVcId = ref('')

// 计算属性
const activeVcs = computed(() => vcList.value.filter(vc => vc.status === 'VALID'))
const expiredVcs = computed(() => vcList.value.filter(vc => vc.status === 'EXPIRED'))
const revokedVcs = computed(() => vcList.value.filter(vc => vc.status === 'REVOKED'))

// 获取凭证列表
async function fetchVCList() {
  loading.value = true
  try {
    const [listRes, typesRes] = await Promise.all([
      vcApi.getMy(),
      vcApi.getTypes()
    ])

    if (listRes.code === 200) {
      vcList.value = listRes.data || []
    }
    if (typesRes.code === 200) {
      vcTypes.value = typesRes.data || []
    }
  } catch (error) {
    console.error('获取凭证列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取状态图标
function getStatusIcon(status: string) {
  switch (status) {
    case 'VALID': return CircleCheck
    case 'EXPIRED': return Clock
    case 'REVOKED': return CircleClose
    default: return Document
  }
}

// 获取状态类型
function getStatusType(status: string) {
  switch (status) {
    case 'VALID': return 'success'
    case 'EXPIRED': return 'warning'
    case 'REVOKED': return 'danger'
    default: return 'info'
  }
}

// 获取状态文本
function getStatusText(status: string) {
  switch (status) {
    case 'VALID': return '有效'
    case 'EXPIRED': return '已过期'
    case 'REVOKED': return '已吊销'
    default: return '未知'
  }
}

// 验证凭证
async function verifyVC() {
  if (!verifyVcId.value) {
    ElMessage.warning('请输入凭证ID')
    return
  }

  verifyLoading.value = true
  try {
    const res = await vcApi.verify(verifyVcId.value)
    if (res.code === 200) {
      verifyResult.value = res.data
    }
  } catch (error) {
    verifyResult.value = {
      valid: false,
      vcId: verifyVcId.value,
      vcType: '',
      holderDid: '',
      message: '验证失败'
    }
  } finally {
    verifyLoading.value = false
  }
}

// 打开验证对话框
function openVerifyDialog() {
  verifyVcId.value = ''
  verifyResult.value = null
  verifyDialogVisible.value = true
}

// 格式化日期
function formatDate(dateStr: string) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 检查是否过期
function isExpired(expiresAt: string) {
  return new Date(expiresAt) < new Date()
}

onMounted(() => {
  fetchVCList()
})
</script>

<template>
  <div class="vc-list">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon total"><Tickets /></el-icon>
            <div class="stat-info">
              <span class="stat-value">{{ vcList.length }}</span>
              <span class="stat-label">总凭证数</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card success">
          <div class="stat-content">
            <el-icon class="stat-icon"><CircleCheck /></el-icon>
            <div class="stat-info">
              <span class="stat-value">{{ activeVcs.length }}</span>
              <span class="stat-label">有效凭证</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card warning">
          <div class="stat-content">
            <el-icon class="stat-icon"><Clock /></el-icon>
            <div class="stat-info">
              <span class="stat-value">{{ expiredVcs.length }}</span>
              <span class="stat-label">已过期</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card danger">
          <div class="stat-content">
            <el-icon class="stat-icon"><CircleClose /></el-icon>
            <div class="stat-info">
              <span class="stat-value">{{ revokedVcs.length }}</span>
              <span class="stat-label">已吊销</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 凭证列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的可验证凭证</span>
          <el-button type="primary" @click="openVerifyDialog">
            验证凭证
          </el-button>
        </div>
      </template>

      <el-empty v-if="!loading && vcList.length === 0" description="暂无凭证" />

      <div class="vc-grid" v-else>
        <el-card
          v-for="vc in vcList"
          :key="vc.vcId"
          class="vc-card"
          :class="{ 'is-expired': isExpired(vc.expiresAt), 'is-revoked': vc.status === 'REVOKED' }"
          shadow="hover"
        >
          <div class="vc-header">
            <div class="vc-type">
              <el-icon :size="24"><Tickets /></el-icon>
              <span>{{ vc.typeName }}</span>
            </div>
            <el-tag :type="getStatusType(vc.status)" size="small">
              {{ getStatusText(vc.status) }}
            </el-tag>
          </div>

          <el-divider />

          <div class="vc-body">
            <div class="vc-info">
              <span class="label">凭证ID:</span>
              <code class="value">{{ vc.vcId.substring(0, 20) }}...</code>
            </div>
            <div class="vc-info">
              <span class="label">签发时间:</span>
              <span class="value">{{ formatDate(vc.issuedAt) }}</span>
            </div>
            <div class="vc-info">
              <span class="label">过期时间:</span>
              <span class="value" :class="{ 'text-danger': isExpired(vc.expiresAt) }">
                {{ formatDate(vc.expiresAt) }}
              </span>
            </div>
          </div>

          <div class="vc-actions">
            <el-button type="primary" link size="small">查看详情</el-button>
            <el-button
              v-if="vc.status === 'VALID'"
              type="success"
              link
              size="small"
              @click="verifyVcId = vc.vcId; verifyVC()"
            >
              验证
            </el-button>
          </div>
        </el-card>
      </div>
    </el-card>

    <!-- 验证对话框 -->
    <el-dialog
      v-model="verifyDialogVisible"
      title="验证可验证凭证"
      width="500px"
    >
      <el-form @submit.prevent="verifyVC">
        <el-form-item label="凭证ID">
          <el-input
            v-model="verifyVcId"
            placeholder="请输入凭证ID (urn:uuid:xxx)"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="verifyLoading" @click="verifyVC">
            验证
          </el-button>
        </el-form-item>
      </el-form>

      <el-divider v-if="verifyResult" />

      <el-result
        v-if="verifyResult"
        :icon="verifyResult.valid ? 'success' : 'error'"
        :title="verifyResult.valid ? '验证通过' : '验证失败'"
        :sub-title="verifyResult.message"
      >
        <template #extra>
          <el-descriptions v-if="verifyResult.valid" :column="1" border size="small">
            <el-descriptions-item label="凭证ID">{{ verifyResult.vcId }}</el-descriptions-item>
            <el-descriptions-item label="凭证类型">{{ verifyResult.vcType }}</el-descriptions-item>
            <el-descriptions-item label="持有者DID">{{ verifyResult.holderDid?.substring(0, 30) }}...</el-descriptions-item>
          </el-descriptions>
        </template>
      </el-result>
    </el-dialog>
  </div>
</template>

<style scoped>
.vc-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stats-row {
  margin-bottom: 0;
}

.stat-card {
  border-radius: 12px;
  overflow: hidden;
}

.stat-card.success :deep(.el-card__body) {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: white;
}

.stat-card.warning :deep(.el-card__body) {
  background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);
  color: white;
}

.stat-card.danger :deep(.el-card__body) {
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
  color: white;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  font-size: 40px;
  opacity: 0.8;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.vc-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.vc-card {
  border-radius: 12px;
  transition: all 0.3s;
}

.vc-card.is-expired {
  opacity: 0.7;
}

.vc-card.is-revoked {
  opacity: 0.5;
}

.vc-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.vc-type {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
}

.vc-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.vc-info {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.vc-info .label {
  color: var(--el-text-color-secondary);
}

.vc-info .value {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
}

.vc-info .text-danger {
  color: var(--el-color-danger);
}

.vc-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--el-border-color-lighter);
}
</style>