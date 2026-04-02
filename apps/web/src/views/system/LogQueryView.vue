<script setup lang="ts">
import { ref, computed } from 'vue'
import { Document, Search, Timer, Location, User, CircleCheck, CircleClose, Download } from '@element-plus/icons-vue'

// 日志数据
const logs = ref([
  { id: 1, username: 'admin', operation: '用户登录', method: 'POST', url: '/api/auth/login', ip: '192.168.1.100', location: '辽宁大连', duration: 156, status: 1, time: '2026-04-02 10:30:00' },
  { id: 2, username: 'admin', operation: '创建DID', method: 'POST', url: '/api/did/create', ip: '192.168.1.100', location: '辽宁大连', duration: 2450, status: 1, time: '2026-04-02 10:35:00' },
  { id: 3, username: 'demo_user', operation: 'KYC认证', method: 'POST', url: '/api/kyc/submit', ip: '192.168.1.101', location: '北京', duration: 1890, status: 1, time: '2026-04-02 09:20:00' },
  { id: 4, username: 'demo_user', operation: '跨境支付', method: 'POST', url: '/api/payment/create', ip: '192.168.1.101', location: '北京', duration: 320, status: 1, time: '2026-04-02 09:45:00' },
  { id: 5, username: 'test_user', operation: '用户登录', method: 'POST', url: '/api/auth/login', ip: '192.168.1.102', location: '上海', duration: 89, status: 0, time: '2026-04-02 08:15:00' },
])

// 搜索过滤
const searchQuery = ref('')
const dateRange = ref<[Date, Date] | null>(null)
const filterStatus = ref('')

// 过滤后的日志
const filteredLogs = computed(() => {
  let result = [...logs.value]
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(log =>
      log.username.includes(query) ||
      log.operation.includes(query) ||
      log.url.includes(query)
    )
  }
  if (filterStatus.value) {
    result = result.filter(log => log.status === parseInt(filterStatus.value))
  }
  return result
})

function getStatusType(status: number) {
  return status === 1 ? 'success' : 'danger'
}

function getStatusText(status: number) {
  return status === 1 ? '成功' : '失败'
}

function exportLogs() {
  // 模拟导出
  alert('日志导出功能 - 预览模式')
}
</script>

<template>
  <div class="log-query-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>
        <el-icon><Document /></el-icon>
        日志查询
      </h1>
      <p>查询系统操作日志和审计记录</p>
    </div>

    <!-- 搜索过滤 -->
    <el-card class="filter-card">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input
            v-model="searchQuery"
            placeholder="搜索用户名、操作或URL"
            :prefix-icon="Search"
            clearable
          />
        </el-col>
        <el-col :span="6">
          <el-select v-model="filterStatus" placeholder="状态筛选" clearable style="width: 100%">
            <el-option label="成功" value="1" />
            <el-option label="失败" value="0" />
          </el-select>
        </el-col>
        <el-col :span="4" :offset="6">
          <el-button type="primary" :icon="Download" @click="exportLogs">
            导出日志
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 日志表格 -->
    <el-card>
      <el-table :data="filteredLogs" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户" width="120">
          <template #default="{ row }">
            <div class="user-cell">
              <el-icon><User /></el-icon>
              <span>{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="operation" label="操作" width="100" />
        <el-table-column prop="method" label="方法" width="80">
          <template #default="{ row }">
            <el-tag :type="row.method === 'GET' ? 'info' : 'primary'" size="small">
              {{ row.method }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="url" label="URL" min-width="180">
          <template #default="{ row }">
            <code class="url-code">{{ row.url }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP" width="130" />
        <el-table-column prop="location" label="位置" width="100" />
        <el-table-column prop="duration" label="耗时" width="80">
          <template #default="{ row }">
            <span class="duration">{{ row.duration }}ms</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="时间" width="180" />
      </el-table>
    </el-card>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon total">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ logs.length }}</span>
          <span class="stat-label">总记录数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon success">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ logs.filter(l => l.status === 1).length }}</span>
          <span class="stat-label">成功</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon danger">
          <el-icon><CircleClose /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ logs.filter(l => l.status === 0).length }}</span>
          <span class="stat-label">失败</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.log-query-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.page-header h1 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 24px;
  font-weight: 600;
  color: #1a1a2e;
}

.page-header p {
  color: #64748b;
  font-size: 14px;
}

.filter-card {
  margin-bottom: 0;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.url-code {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  background: #f1f5f9;
  padding: 4px 8px;
  border-radius: 4px;
  color: #475569;
}

.duration {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  color: white;
  font-size: 20px;
}

.stat-icon.total {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
}

.stat-icon.success {
  background: linear-gradient(135deg, #10b981, #059669);
}

.stat-icon.danger {
  background: linear-gradient(135deg, #ef4444, #dc2626);
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
}
</style>