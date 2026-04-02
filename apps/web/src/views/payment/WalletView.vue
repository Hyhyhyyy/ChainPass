<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Wallet, CreditCard, TrendCharts, Refresh, ArrowUp, ArrowDown,
  Position, Document, Right, Plus, CopyDocument, Shield
} from '@element-plus/icons-vue'
import { paymentApi, type Wallet as WalletType, type TransactionHistory } from '@/api/payment'

// 状态
const loading = ref(false)
const wallet = ref<WalletType | null>(null)
const transactions = ref<TransactionHistory[]>([])

// 计算属性
const balanceItems = computed(() => {
  if (!wallet.value) return []
  return [
    {
      label: '人民币',
      code: 'CNY',
      value: wallet.value.balanceCny || 0,
      symbol: '¥',
      color: '#ef4444',
      bg: 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)'
    },
    {
      label: '美元',
      code: 'USD',
      value: wallet.value.balanceUsd || 0,
      symbol: '$',
      color: '#10b981',
      bg: 'linear-gradient(135deg, #10b981 0%, #059669 100%)'
    },
    {
      label: '以太坊',
      code: 'ETH',
      value: wallet.value.balanceEth || 0,
      symbol: 'Ξ',
      color: '#8b5cf6',
      bg: 'linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)'
    }
  ]
})

// 获取钱包信息
async function fetchWallet() {
  loading.value = true
  try {
    const res = await paymentApi.getWallet()
    if (res.code === 200 && res.data) {
      wallet.value = res.data
    }
  } catch (error: any) {
    if (error.message !== '请先创建DID') {
      ElMessage.error('获取钱包信息失败')
    }
  } finally {
    loading.value = false
  }
}

// 获取交易历史
async function fetchHistory() {
  try {
    const res = await paymentApi.getHistory()
    if (res.code === 200) {
      transactions.value = res.data || []
    }
  } catch {
    transactions.value = []
  }
}

// 格式化金额
function formatAmount(amount: number, currency: string) {
  const decimals = currency === 'ETH' ? 8 : 2
  return amount.toFixed(decimals)
}

// 获取货币符号
function getCurrencySymbol(currency: string) {
  switch (currency) {
    case 'CNY': return '¥'
    case 'USD': return '$'
    case 'ETH': return 'Ξ'
    default: return currency
  }
}

// 格式化日期
function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 获取交易类型图标
function getTypeIcon(type: string) {
  return type === 'IN' ? ArrowDown : ArrowUp
}

// 复制地址
function copyAddress() {
  if (wallet.value?.address) {
    navigator.clipboard.writeText(wallet.value.address)
    ElMessage.success('地址已复制')
  }
}

onMounted(() => {
  fetchWallet()
  fetchHistory()
})
</script>

<template>
  <div class="wallet-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h1>
          <el-icon class="title-icon"><Wallet /></el-icon>
          我的钱包
        </h1>
        <p>管理您的多币种数字资产</p>
      </div>
    </div>

    <!-- 钱包卡片 -->
    <div class="wallet-main" v-loading="loading">
      <template v-if="wallet">
        <!-- 钱包信息 -->
        <div class="wallet-info">
          <div class="wallet-header">
            <div class="wallet-avatar">
              <el-icon :size="32"><Wallet /></el-icon>
            </div>
            <div class="wallet-meta">
              <h2>ChainPass 钱包</h2>
              <div class="wallet-address">
                <code>{{ wallet.address }}</code>
                <el-button link type="primary" :icon="CopyDocument" @click="copyAddress" />
              </div>
            </div>
            <el-button :icon="Refresh" circle @click="fetchWallet" />
          </div>
        </div>

        <!-- 余额卡片 -->
        <div class="balance-cards">
          <div
            v-for="item in balanceItems"
            :key="item.code"
            class="balance-card"
            :style="{ background: item.bg }"
          >
            <div class="balance-header">
              <span class="balance-label">{{ item.label }}</span>
              <span class="balance-code">{{ item.code }}</span>
            </div>
            <div class="balance-value">
              <span class="symbol">{{ item.symbol }}</span>
              <span class="amount">{{ formatAmount(item.value, item.code) }}</span>
            </div>
          </div>
        </div>

        <!-- 总资产 -->
        <div class="total-balance">
          <div class="total-content">
            <div class="total-info">
              <span class="total-label">总资产估值</span>
              <span class="total-value">¥ {{ wallet.totalBalanceCny?.toFixed(2) || '0.00' }}</span>
            </div>
            <p class="total-hint">* 按实时汇率折算为人民币</p>
          </div>
        </div>

        <!-- 快捷操作 -->
        <div class="quick-actions">
          <div class="action-item" @click="$router.push('/payment/transfer')">
            <div class="action-icon send">
              <el-icon><Position /></el-icon>
            </div>
            <span>跨境支付</span>
          </div>
          <div class="action-item" @click="$router.push('/payment/history')">
            <div class="action-icon history">
              <el-icon><Document /></el-icon>
            </div>
            <span>交易记录</span>
          </div>
        </div>
      </template>

      <!-- 空状态 -->
      <div v-else class="empty-wallet">
        <div class="empty-icon">
          <el-icon :size="64"><Wallet /></el-icon>
        </div>
        <h3>钱包未初始化</h3>
        <p>请先创建DID身份以使用钱包功能</p>
        <el-button type="primary" size="large" @click="$router.push('/identity/did')">
          去创建DID
          <el-icon><Right /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- 最近交易 -->
    <div class="transactions-section" v-if="transactions.length > 0">
      <div class="section-header">
        <h2>
          <el-icon><TrendCharts /></el-icon>
          最近交易
        </h2>
        <el-button link type="primary" @click="$router.push('/payment/history')">
          查看全部
          <el-icon><Right /></el-icon>
        </el-button>
      </div>

      <div class="transactions-list">
        <div
          v-for="tx in transactions.slice(0, 5)"
          :key="tx.orderNo"
          class="transaction-item"
        >
          <div class="tx-icon" :class="tx.type.toLowerCase()">
            <el-icon :size="20">
              <component :is="getTypeIcon(tx.type)" />
            </el-icon>
          </div>
          <div class="tx-info">
            <div class="tx-main">
              <span class="tx-type">{{ tx.type === 'IN' ? '收款' : '付款' }}</span>
              <el-tag :type="tx.status === 'SUCCESS' ? 'success' : 'warning'" size="small">
                {{ tx.status }}
              </el-tag>
            </div>
            <div class="tx-meta">
              <span class="tx-counterparty">
                {{ tx.type === 'IN' ? '来自' : '发送至' }}: {{ tx.counterpartyDid?.substring(0, 15) }}...
              </span>
              <span class="tx-time">{{ formatDate(tx.createdAt) }}</span>
            </div>
          </div>
          <div class="tx-amount" :class="tx.type.toLowerCase()">
            {{ tx.type === 'IN' ? '+' : '-' }}
            {{ getCurrencySymbol(tx.currency) }}
            {{ formatAmount(tx.amount, tx.currency) }}
          </div>
        </div>
      </div>
    </div>

    <!-- 功能说明 -->
    <div class="features-section">
      <h3>钱包功能</h3>
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon><Shield /></el-icon>
          </div>
          <h4>安全存储</h4>
          <p>资产由区块链技术保护</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon><Refresh /></el-icon>
          </div>
          <h4>实时汇率</h4>
          <p>支持多币种实时兑换</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon><Position /></el-icon>
          </div>
          <h4>跨境支付</h4>
          <p>便捷的国际转账服务</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon><Document /></el-icon>
          </div>
          <h4>透明记录</h4>
          <p>所有交易链上可查</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.wallet-page {
  max-width: 1000px;
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

/* 钱包主体 */
.wallet-main {
  background: white;
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.wallet-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 32px;
}

.wallet-avatar {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  border-radius: 16px;
  color: white;
}

.wallet-meta {
  flex: 1;
}

.wallet-meta h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.wallet-address {
  display: flex;
  align-items: center;
  gap: 8px;
}

.wallet-address code {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  background: #f1f5f9;
  padding: 6px 12px;
  border-radius: 6px;
  color: #64748b;
}

/* 余额卡片 */
.balance-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

@media (max-width: 768px) {
  .balance-cards {
    grid-template-columns: 1fr;
  }
}

.balance-card {
  padding: 24px;
  border-radius: 16px;
  color: white;
}

.balance-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}

.balance-label {
  font-size: 14px;
  font-weight: 500;
}

.balance-code {
  font-size: 12px;
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 8px;
  border-radius: 4px;
}

.balance-value {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.balance-value .symbol {
  font-size: 20px;
  opacity: 0.9;
}

.balance-value .amount {
  font-size: 28px;
  font-weight: 700;
}

/* 总资产 */
.total-balance {
  background: #f8fafc;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
}

.total-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-label {
  font-size: 14px;
  color: #64748b;
}

.total-value {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a2e;
}

.total-hint {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 8px;
}

/* 快捷操作 */
.quick-actions {
  display: flex;
  gap: 16px;
}

.action-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 24px;
  background: #f8fafc;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.action-item:hover {
  background: #f1f5f9;
  transform: translateY(-2px);
}

.action-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  color: white;
  font-size: 20px;
}

.action-icon.send {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.action-icon.history {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
}

.action-item span {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a2e;
}

/* 空状态 */
.empty-wallet {
  text-align: center;
  padding: 60px;
}

.empty-icon {
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
  border-radius: 50%;
  margin: 0 auto 24px;
  color: #94a3b8;
}

.empty-wallet h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.empty-wallet p {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 24px;
}

/* 交易记录 */
.transactions-section {
  background: white;
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
}

.transactions-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.transaction-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
}

.tx-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  color: white;
}

.tx-icon.in {
  background: linear-gradient(135deg, #10b981, #059669);
}

.tx-icon.out {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.tx-info {
  flex: 1;
}

.tx-main {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.tx-type {
  font-size: 15px;
  font-weight: 500;
  color: #1a1a2e;
}

.tx-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #94a3b8;
}

.tx-counterparty {
  font-family: 'Monaco', 'Menlo', monospace;
}

.tx-amount {
  font-size: 16px;
  font-weight: 600;
  font-family: 'Monaco', 'Menlo', monospace;
}

.tx-amount.in {
  color: #10b981;
}

.tx-amount.out {
  color: #f59e0b;
}

/* 功能说明 */
.features-section {
  background: #f8fafc;
  border-radius: 20px;
  padding: 32px;
}

.features-section h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 24px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

@media (max-width: 768px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.feature-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  text-align: center;
}

.feature-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  border-radius: 12px;
  color: white;
  font-size: 20px;
  margin: 0 auto 12px;
}

.feature-card h4 {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 4px;
}

.feature-card p {
  font-size: 12px;
  color: #64748b;
}
</style>