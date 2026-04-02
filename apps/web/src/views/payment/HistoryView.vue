<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Download, Search, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import { paymentApi, type TransactionHistory } from '@/api/payment'

// 状态
const loading = ref(false)
const transactions = ref<TransactionHistory[]>([])
const searchQuery = ref('')
const filterType = ref('')
const dateRange = ref<[Date, Date] | null>(null)

// 获取交易历史
async function fetchHistory() {
  loading.value = true
  try {
    const res = await paymentApi.getHistory()
    if (res.code === 200) {
      transactions.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('获取交易历史失败')
  } finally {
    loading.value = false
  }
}

// 过滤后的交易
const filteredTransactions = ref<TransactionHistory[]>([])

function applyFilter() {
  let result = [...transactions.value]

  // 按类型过滤
  if (filterType.value) {
    result = result.filter(tx => tx.type === filterType.value)
  }

  // 按关键词搜索
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(tx =>
      tx.orderNo.toLowerCase().includes(query) ||
      tx.counterpartyDid.toLowerCase().includes(query)
    )
  }

  filteredTransactions.value = result
}

// 格式化
function formatAmount(amount: number, currency: string) {
  return currency === 'ETH' ? amount.toFixed(8) : amount.toFixed(2)
}

function getSymbol(currency: string) {
  switch (currency) {
    case 'CNY': return '¥'
    case 'USD': return '$'
    case 'ETH': return 'Ξ'
    default: return currency
  }
}

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleString('zh-CN')
}

function getTypeIcon(type: string) {
  return type === 'IN' ? ArrowDown : ArrowUp
}

function getTypeColor(type: string) {
  return type === 'IN' ? 'success' : 'warning'
}

// 导出记录
function exportRecords() {
  ElMessage.info('导出功能开发中...')
}

onMounted(() => {
  fetchHistory()
})
</script>

<template>
  <div class="history-view">
    <!-- 搜索过滤 -->
    <el-card class="filter-card">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input
            v-model="searchQuery"
            placeholder="搜索订单号或DID"
            :prefix-icon="Search"
            clearable
            @input="applyFilter"
          />
        </el-col>
        <el-col :span="6">
          <el-select
            v-model="filterType"
            placeholder="交易类型"
            clearable
            @change="applyFilter"
          >
            <el-option label="收款" value="IN" />
            <el-option label="付款" value="OUT" />
          </el-select>
        </el-col>
        <el-col :span="4" :offset="6">
          <el-button type="primary" :icon="Download" @click="exportRecords">
            导出
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 交易列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>交易记录</span>
          <el-button link :icon="Document">
            共 {{ transactions.length }} 条记录
          </el-button>
        </div>
      </template>

      <el-table :data="filteredTransactions" v-loading="loading" stripe>
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <div class="tx-type-cell">
              <el-icon :class="row.type.toLowerCase()">
                <component :is="getTypeIcon(row.type)" />
              </el-icon>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="订单号" width="200">
          <template #default="{ row }">
            <code class="order-no">{{ row.orderNo }}</code>
          </template>
        </el-table-column>

        <el-table-column label="交易对象" min-width="200">
          <template #default="{ row }">
            <div class="counterparty">
              <span class="label">{{ row.type === 'IN' ? '付款人' : '收款人' }}</span>
              <code>{{ row.counterpartyDid?.substring(0, 25) }}...</code>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="金额" width="150" align="right">
          <template #default="{ row }">
            <span :class="['amount', row.type.toLowerCase()]">
              {{ row.type === 'IN' ? '+' : '-' }}
              {{ getSymbol(row.currency) }}
              {{ formatAmount(row.amount, row.currency) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'warning'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="描述" min-width="150">
          <template #default="{ row }">
            {{ row.description || '-' }}
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && filteredTransactions.length === 0" description="暂无交易记录" />
    </el-card>
  </div>
</template>

<style scoped>
.history-view {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.filter-card {
  margin-bottom: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tx-type-cell {
  display: flex;
  justify-content: center;
}

.tx-type-cell .in {
  color: var(--el-color-success);
}

.tx-type-cell .out {
  color: var(--el-color-warning);
}

.order-no {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  background: var(--el-fill-color-light);
  padding: 4px 8px;
  border-radius: 4px;
}

.counterparty {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.counterparty .label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.counterparty code {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 11px;
}

.amount {
  font-weight: 600;
  font-family: 'Monaco', 'Menlo', monospace;
}

.amount.in {
  color: var(--el-color-success);
}

.amount.out {
  color: var(--el-color-warning);
}
</style>