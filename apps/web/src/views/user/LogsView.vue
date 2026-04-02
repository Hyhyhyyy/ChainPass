<script setup lang="ts">
import { ref, computed } from 'vue'
import { Document, Search, Timer, Location, Monitor, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { mockOperationLogs } from '@/mock/previewData'

// 预览模式
const PREVIEW_MODE = true

// 状态
const loading = ref(false)
const logs = ref(mockOperationLogs)
const searchQuery = ref('')
const filterType = ref('')

// 操作类型选项
const typeOptions = [
  { label: '全部', value: '' },
  { label: '登录', value: '登录' },
  { label: '支付', value: '支付' },
  { label: 'KYC', value: 'KYC' },
  { label: 'DID', value: 'DID' }
]

// 过滤后的日志
const filteredLogs = computed(() => {
  let result = [...logs.value]
  if (filterType.value) {
    result = result.filter(log => log.operationType === filterType.value)
  }
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(log =>
      log.operationDesc.toLowerCase().includes(query) ||
      log.requestUrl.toLowerCase().includes(query)
    )
  }
  return result
})

// 获取状态图标
function getStatusIcon(status: number) {
  return status === 1 ? CircleCheck : CircleClose
}

// 获取状态类型
function getStatusType(status: number) {
  return status === 1 ? 'success' : 'danger'
}

// 获取操作类型颜色
function getTypeColor(type: string) {
  const colors: Record<string, string> = {
    '登录': '#3b82f6',
    '支付': '#f59e0b',
    'KYC': '#10b981',
    'DID': '#8b5cf6'
  }
  return colors[type] || '#64748b'
}
</script>

<template>
  <div class="logs-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h1>
          <el-icon class="title-icon"><Document /></el-icon>
          操作日志
        </h1>
        <p>查看您的账户操作历史记录</p>
      </div>
    </div>

    <!-- 搜索过滤 -->
    <div class="filter-section">
      <el-input
        v-model="searchQuery"
        placeholder="搜索操作描述或URL"
        :prefix-icon="Search"
        clearable
        style="width: 300px"
      />
      <el-select v-model="filterType" placeholder="操作类型" clearable style="width: 150px">
        <el-option
          v-for="opt in typeOptions"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
    </div>

    <!-- 日志列表 -->
    <div class="logs-list">
      <div v-for="log in filteredLogs" :key="log.id" class="log-item">
        <div class="log-icon" :style="{ background: getTypeColor(log.operationType) }">
          <el-icon><Document /></el-icon>
        </div>
        <div class="log-content">
          <div class="log-header">
            <span class="log-type" :style="{ color: getTypeColor(log.operationType) }">
              {{ log.operationType }}
            </span>
            <span class="log-desc">{{ log.operationDesc }}</span>
            <el-tag :type="getStatusType(log.status)" size="small">
              <el-icon><component :is="getStatusIcon(log.status)" /></el-icon>
              {{ log.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </div>
          <div class="log-meta">
            <div class="meta-item">
              <el-icon><Monitor /></el-icon>
              <span>{{ log.requestMethod }} {{ log.requestUrl }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Location /></el-icon>
              <span>{{ log.location }} ({{ log.ip }})</span>
            </div>
            <div class="meta-item">
              <el-icon><Timer /></el-icon>
              <span>{{ log.createdAt }}</span>
            </div>
          </div>
        </div>
        <div class="log-duration">
          <span class="duration-value">{{ log.executionTime }}</span>
          <span class="duration-label">ms</span>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="filteredLogs.length === 0" description="暂无操作日志" />
    </div>

    <!-- 统计信息 -->
    <div class="stats-section">
      <div class="stat-card">
        <span class="stat-value">{{ logs.length }}</span>
        <span class="stat-label">总操作数</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ logs.filter(l => l.status === 1).length }}</span>
        <span class="stat-label">成功</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ logs.filter(l => l.status !== 1).length }}</span>
        <span class="stat-label">失败</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.logs-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 页面标题 */
.page-header {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  border-radius: 20px;
  padding: 32px;
  color: white;
}

.header-content h1 {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
}

.title-icon {
  font-size: 32px;
}

.header-content p {
  font-size: 16px;
  opacity: 0.9;
}

/* 过滤区域 */
.filter-section {
  display: flex;
  gap: 16px;
}

/* 日志列表 */
.logs-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.log-item {
  display: flex;
  align-items: center;
  gap: 16px;
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
  transition: all 0.3s;
}

.log-item:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.log-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  color: white;
  flex-shrink: 0;
}

.log-content {
  flex: 1;
}

.log-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.log-type {
  font-size: 14px;
  font-weight: 600;
}

.log-desc {
  flex: 1;
  font-size: 15px;
  color: #1a1a2e;
}

.log-meta {
  display: flex;
  gap: 24px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #64748b;
}

.meta-item .el-icon {
  color: #94a3b8;
}

.log-duration {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 16px;
  background: #f8fafc;
  border-radius: 8px;
}

.duration-value {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
}

.duration-label {
  font-size: 12px;
  color: #94a3b8;
}

/* 统计信息 */
.stats-section {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a2e;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
}
</style>