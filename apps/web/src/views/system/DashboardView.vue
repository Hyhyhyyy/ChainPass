<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  User, Key, Wallet, Lock, Tickets, Position,
  CircleCheck, CircleClose, Loading, Document,
  ArrowUp, ArrowDown, Plus, Refresh, Right,
  QuestionFilled, Star, Timer
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores'
import { didApi } from '@/api/did'
import { vcApi } from '@/api/vc'
import { paymentApi, type TransactionHistory } from '@/api/payment'
import { kycApi } from '@/api/kyc'
import { mockDID, mockWallet, mockKYCStatus, mockVCList, mockTransactions } from '@/mock/previewData'

// 预览模式
const PREVIEW_MODE = true

const router = useRouter()
const userStore = useUserStore()

// 状态
const loading = ref(false)
const didStatus = ref<any>(null)
const vcList = ref<any[]>([])
const walletInfo = ref<any>(null)
const kycStatus = ref<any>(null)
const recentTransactions = ref<TransactionHistory[]>([])
const showGuide = ref(false)

// 计算属性
const hasDID = computed(() => didStatus.value?.did)
const didActive = computed(() => didStatus.value?.status === 'ACTIVE')
const kycVerified = computed(() => kycStatus.value?.verified)
const totalVCCount = computed(() => vcList.value.length)
const validVCCount = computed(() => vcList.value.filter(v => v.status === 'VALID').length)

// 用户引导步骤
const guideSteps = [
  {
    title: '创建DID身份',
    description: 'DID是您在区块链上的数字身份，是使用所有功能的基础',
    icon: Key,
    action: '/identity/did',
    completed: false
  },
  {
    title: '完成KYC认证',
    description: '通过身份认证后可获得支付凭证，解锁跨境支付功能',
    icon: Lock,
    action: '/compliance/kyc',
    completed: false
  },
  {
    title: '开始跨境支付',
    description: '使用您的DID身份进行安全、合规的跨境转账',
    icon: Position,
    action: '/payment/transfer',
    completed: false
  }
]

const currentStep = computed(() => {
  if (!hasDID.value) return 0
  if (!kycVerified.value) return 1
  return 2
})

const progressPercent = computed(() => {
  return Math.round((currentStep.value / 3) * 100)
})

// 获取用户数据
async function fetchDIDStatus() {
  try {
    const res = await didApi.getMy()
    if (res.code === 200 && res.data) {
      didStatus.value = res.data
      guideSteps[0].completed = true
    }
  } catch {
    didStatus.value = null
  }
}

async function fetchVCList() {
  if (!hasDID.value) return
  try {
    const res = await vcApi.getList(hasDID.value)
    if (res.code === 200) {
      vcList.value = res.data || []
    }
  } catch {
    vcList.value = []
  }
}

async function fetchWallet() {
  try {
    const res = await paymentApi.getWallet()
    if (res.code === 200) {
      walletInfo.value = res.data
    }
  } catch {
    walletInfo.value = null
  }
}

async function fetchKYCStatus() {
  try {
    const res = await kycApi.getStatus()
    if (res.code === 200) {
      kycStatus.value = res.data
      if (kycStatus.value?.verified) {
        guideSteps[1].completed = true
      }
    }
  } catch {
    kycStatus.value = null
  }
}

async function fetchRecentTransactions() {
  try {
    const res = await paymentApi.getHistory()
    if (res.code === 200) {
      recentTransactions.value = (res.data || []).slice(0, 5)
      if (recentTransactions.value.length > 0) {
        guideSteps[2].completed = true
      }
    }
  } catch {
    recentTransactions.value = []
  }
}

async function refreshAll() {
  loading.value = true
  try {
    // 预览模式使用假数据
    if (PREVIEW_MODE) {
      didStatus.value = mockDID as any
      guideSteps[0].completed = true
      walletInfo.value = mockWallet as any
      kycStatus.value = mockKYCStatus as any
      guideSteps[1].completed = true
      vcList.value = mockVCList as any
      recentTransactions.value = mockTransactions.slice(0, 5) as any
      guideSteps[2].completed = true
    } else {
      await Promise.all([fetchDIDStatus(), fetchWallet(), fetchKYCStatus()])
      await Promise.all([fetchVCList(), fetchRecentTransactions()])
    }
  } finally {
    loading.value = false
  }
}

function formatBalance(balance: number, currency: string) {
  if (currency === 'ETH') return balance.toFixed(8)
  return balance.toFixed(2)
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
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

function goToStep(step: any) {
  router.push(step.action)
}

onMounted(() => {
  refreshAll()
  // 首次访问显示引导
  if (!localStorage.getItem('guide_shown')) {
    showGuide.value = true
    localStorage.setItem('guide_shown', 'true')
  }
})
</script>

<template>
  <div class="dashboard-container">
    <!-- 顶部欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <div class="welcome-text">
          <h1>欢迎使用 ChainPass</h1>
          <p>基于区块链的跨境数字身份与合规支付解决方案</p>
        </div>
        <div class="welcome-actions">
          <el-button type="primary" size="large" @click="showGuide = true">
            <el-icon><QuestionFilled /></el-icon>
            新手引导
          </el-button>
        </div>
      </div>

      <!-- 进度条 -->
      <div class="progress-section">
        <div class="progress-header">
          <span class="progress-label">账户完善度</span>
          <span class="progress-value">{{ progressPercent }}%</span>
        </div>
        <el-progress
          :percentage="progressPercent"
          :stroke-width="8"
          :show-text="false"
          stroke-linecap="round"
        />
      </div>
    </div>

    <!-- 新手引导卡片 -->
    <div class="guide-section" v-if="currentStep < 3">
      <div class="guide-header">
        <h2>
          <el-icon class="star-icon"><Star /></el-icon>
          快速开始
        </h2>
        <p>完成以下步骤，开启您的区块链身份之旅</p>
      </div>

      <div class="guide-steps">
        <div
          v-for="(step, index) in guideSteps"
          :key="index"
          class="guide-step"
          :class="{
            'completed': step.completed,
            'current': index === currentStep,
            'locked': index > currentStep
          }"
          @click="index <= currentStep && goToStep(step)"
        >
          <div class="step-number">
            <el-icon v-if="step.completed"><CircleCheck /></el-icon>
            <span v-else>{{ index + 1 }}</span>
          </div>
          <div class="step-content">
            <div class="step-icon">
              <el-icon><component :is="step.icon" /></el-icon>
            </div>
            <div class="step-info">
              <h3>{{ step.title }}</h3>
              <p>{{ step.description }}</p>
            </div>
            <el-icon class="step-arrow" v-if="index <= currentStep"><Right /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- 核心功能卡片 -->
    <div class="core-features">
      <!-- DID身份卡片 -->
      <div class="feature-card did-feature" @click="router.push('/identity/did')">
        <div class="feature-header">
          <div class="feature-icon did">
            <el-icon><Key /></el-icon>
          </div>
          <el-tag :type="hasDID ? 'success' : 'info'" effect="dark" size="small">
            {{ hasDID ? '已创建' : '待创建' }}
          </el-tag>
        </div>
        <h3>DID 数字身份</h3>
        <p>{{ hasDID ? '您的去中心化身份已激活' : '创建您的区块链数字身份' }}</p>
        <div class="feature-status" v-if="hasDID">
          <code class="did-code">{{ didStatus?.did?.substring(0, 25) }}...</code>
        </div>
        <div class="feature-action">
          <el-button :type="hasDID ? 'default' : 'primary'" size="small">
            {{ hasDID ? '管理身份' : '立即创建' }}
            <el-icon><Right /></el-icon>
          </el-button>
        </div>
      </div>

      <!-- KYC认证卡片 -->
      <div class="feature-card kyc-feature" @click="router.push('/compliance/kyc')">
        <div class="feature-header">
          <div class="feature-icon kyc">
            <el-icon><Lock /></el-icon>
          </div>
          <el-tag :type="kycVerified ? 'success' : (kycStatus?.status === 'PENDING' ? 'warning' : 'info')" effect="dark" size="small">
            {{ kycVerified ? '已认证' : (kycStatus?.status === 'PENDING' ? '审核中' : '未认证') }}
          </el-tag>
        </div>
        <h3>KYC 身份认证</h3>
        <p>{{ kycVerified ? '您已完成身份认证' : '完成认证解锁支付功能' }}</p>
        <div class="feature-status" v-if="kycVerified">
          <el-tag type="success" effect="plain">{{ kycStatus?.kycLevelName || '基础认证' }}</el-tag>
        </div>
        <div class="feature-action">
          <el-button :type="kycVerified ? 'default' : 'primary'" size="small">
            {{ kycVerified ? '查看详情' : '开始认证' }}
            <el-icon><Right /></el-icon>
          </el-button>
        </div>
      </div>

      <!-- 钱包卡片 -->
      <div class="feature-card wallet-feature" @click="router.push('/payment/wallet')">
        <div class="feature-header">
          <div class="feature-icon wallet">
            <el-icon><Wallet /></el-icon>
          </div>
          <el-button link type="primary" @click.stop="refreshAll">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>
        <h3>数字钱包</h3>
        <p>管理您的多币种数字资产</p>
        <div class="wallet-balances" v-if="walletInfo">
          <div class="balance-row">
            <span class="currency">CNY</span>
            <span class="amount">¥ {{ formatBalance(walletInfo.balanceCny || 0, 'CNY') }}</span>
          </div>
          <div class="balance-row">
            <span class="currency">USD</span>
            <span class="amount">$ {{ formatBalance(walletInfo.balanceUsd || 0, 'USD') }}</span>
          </div>
        </div>
        <div class="feature-action">
          <el-button type="primary" size="small">
            查看钱包
            <el-icon><Right /></el-icon>
          </el-button>
        </div>
      </div>

      <!-- 凭证卡片 -->
      <div class="feature-card vc-feature" @click="router.push('/identity/vc')">
        <div class="feature-header">
          <div class="feature-icon vc">
            <el-icon><Tickets /></el-icon>
          </div>
          <el-tag type="info" effect="dark" size="small">{{ validVCCount }} 个有效</el-tag>
        </div>
        <h3>可验证凭证</h3>
        <p>您的数字身份证明文件</p>
        <div class="feature-status">
          <div class="vc-count">
            <span class="count">{{ totalVCCount }}</span>
            <span class="label">个凭证</span>
          </div>
        </div>
        <div class="feature-action">
          <el-button size="small">
            查看凭证
            <el-icon><Right /></el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions-section">
      <h2>快捷操作</h2>
      <div class="action-cards">
        <div class="action-card transfer" @click="router.push('/payment/transfer')">
          <div class="action-icon">
            <el-icon><Position /></el-icon>
          </div>
          <div class="action-info">
            <h4>跨境支付</h4>
            <p>安全便捷的跨境转账</p>
          </div>
        </div>
        <div class="action-card history" @click="router.push('/payment/history')">
          <div class="action-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="action-info">
            <h4>交易记录</h4>
            <p>查看历史交易详情</p>
          </div>
        </div>
        <div class="action-card profile" @click="router.push('/user/profile')">
          <div class="action-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="action-info">
            <h4>个人中心</h4>
            <p>管理账户信息</p>
          </div>
        </div>
        <div class="action-card security" @click="router.push('/user/security')">
          <div class="action-icon">
            <el-icon><Lock /></el-icon>
          </div>
          <div class="action-info">
            <h4>安全设置</h4>
            <p>保护您的账户安全</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 最近交易 -->
    <div class="transactions-section" v-if="recentTransactions.length > 0">
      <div class="section-header">
        <h2>最近交易</h2>
        <el-button link type="primary" @click="router.push('/payment/history')">
          查看全部
          <el-icon><Right /></el-icon>
        </el-button>
      </div>
      <div class="transactions-list">
        <div v-for="tx in recentTransactions" :key="tx.orderNo" class="transaction-item">
          <div class="tx-icon" :class="tx.type.toLowerCase()">
            <el-icon><component :is="tx.type === 'IN' ? ArrowDown : ArrowUp" /></el-icon>
          </div>
          <div class="tx-info">
            <p class="tx-desc">{{ tx.description || (tx.type === 'IN' ? '收款' : '付款') }}</p>
            <p class="tx-date">{{ formatDate(tx.createdAt) }}</p>
          </div>
          <div class="tx-amount" :class="tx.type.toLowerCase()">
            {{ tx.type === 'IN' ? '+' : '-' }}
            {{ getSymbol(tx.currency) }}
            {{ formatBalance(tx.amount, tx.currency) }}
          </div>
        </div>
      </div>
    </div>

    <!-- 产品介绍 -->
    <div class="product-intro">
      <h2>ChainPass 能为您做什么？</h2>
      <div class="features-intro">
        <div class="intro-item">
          <div class="intro-icon">
            <el-icon><Key /></el-icon>
          </div>
          <h4>自主身份</h4>
          <p>完全掌控您的数字身份，无需依赖第三方</p>
        </div>
        <div class="intro-item">
          <div class="intro-icon">
            <el-icon><Lock /></el-icon>
          </div>
          <h4>隐私保护</h4>
          <p>零知识证明技术，验证身份不暴露隐私</p>
        </div>
        <div class="intro-item">
          <div class="intro-icon">
            <el-icon><Position /></el-icon>
          </div>
          <h4>跨境支付</h4>
          <p>多币种支持，合规便捷的国际转账</p>
        </div>
        <div class="intro-item">
          <div class="intro-icon">
            <el-icon><Tickets /></el-icon>
          </div>
          <h4>可信凭证</h4>
          <p>W3C标准凭证，全球范围可验证</p>
        </div>
      </div>
    </div>

    <!-- 新手引导对话框 -->
    <el-dialog
      v-model="showGuide"
      title=""
      width="600px"
      :show-close="true"
      class="guide-dialog"
    >
      <div class="guide-dialog-content">
        <div class="guide-welcome">
          <div class="welcome-icon">
            <el-icon :size="48"><Key /></el-icon>
          </div>
          <h2>欢迎使用 ChainPass</h2>
          <p>让我们一起开启区块链身份之旅</p>
        </div>

        <div class="guide-timeline">
          <div class="timeline-item" :class="{ active: currentStep === 0 }">
            <div class="timeline-dot">1</div>
            <div class="timeline-content">
              <h4>创建 DID 身份</h4>
              <p>DID 是您在区块链上的唯一身份标识，是使用所有功能的基础</p>
            </div>
          </div>
          <div class="timeline-item" :class="{ active: currentStep === 1 }">
            <div class="timeline-dot">2</div>
            <div class="timeline-content">
              <h4>完成 KYC 认证</h4>
              <p>通过身份验证后，系统将自动为您签发可验证凭证</p>
            </div>
          </div>
          <div class="timeline-item" :class="{ active: currentStep === 2 }">
            <div class="timeline-dot">3</div>
            <div class="timeline-content">
              <h4>开始跨境支付</h4>
              <p>使用您的 DID 身份进行安全、合规的跨境转账</p>
            </div>
          </div>
        </div>

        <div class="guide-cta">
          <el-button type="primary" size="large" @click="showGuide = false; router.push('/identity/did')">
            开始使用
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

/* 欢迎区域 */
.welcome-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  padding: 32px;
  color: white;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.welcome-text h1 {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
}

.welcome-text p {
  font-size: 16px;
  opacity: 0.9;
}

.welcome-actions .el-button {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  color: white;
}

.progress-section {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 16px 20px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 14px;
}

.progress-value {
  font-weight: 600;
}

/* 新手引导 */
.guide-section {
  background: white;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.guide-header {
  margin-bottom: 24px;
}

.guide-header h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.star-icon {
  color: #f59e0b;
}

.guide-header p {
  color: #64748b;
  font-size: 14px;
}

.guide-steps {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.guide-step {
  display: flex;
  gap: 16px;
  padding: 20px;
  border-radius: 16px;
  border: 2px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.3s;
}

.guide-step.current {
  border-color: #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
}

.guide-step.completed {
  border-color: #10b981;
  background: rgba(16, 185, 129, 0.05);
}

.guide-step.locked {
  opacity: 0.5;
  cursor: not-allowed;
}

.step-number {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e2e8f0;
  border-radius: 50%;
  font-weight: 600;
  color: #64748b;
  flex-shrink: 0;
}

.guide-step.completed .step-number {
  background: #10b981;
  color: white;
}

.guide-step.current .step-number {
  background: #667eea;
  color: white;
}

.step-content {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16px;
}

.step-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
  border-radius: 12px;
  font-size: 24px;
  color: #667eea;
}

.step-info h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 4px;
}

.step-info p {
  font-size: 13px;
  color: #64748b;
}

.step-arrow {
  color: #667eea;
}

/* 核心功能卡片 */
.core-features {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

@media (max-width: 1024px) {
  .core-features {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .core-features {
    grid-template-columns: 1fr;
  }
}

.feature-card {
  background: white;
  border-radius: 20px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  border: 1px solid transparent;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1);
  border-color: #667eea;
}

.feature-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.feature-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  font-size: 24px;
  color: white;
}

.feature-icon.did {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.feature-icon.kyc {
  background: linear-gradient(135deg, #10b981, #059669);
}

.feature-icon.wallet {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.feature-icon.vc {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
}

.feature-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.feature-card p {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 16px;
}

.feature-status {
  margin-bottom: 16px;
}

.did-code {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  background: #f1f5f9;
  padding: 8px 12px;
  border-radius: 8px;
  display: block;
  color: #475569;
}

.wallet-balances {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.balance-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f8fafc;
  border-radius: 8px;
}

.balance-row .currency {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

.balance-row .amount {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.vc-count {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.vc-count .count {
  font-size: 32px;
  font-weight: 700;
  color: #8b5cf6;
}

.vc-count .label {
  font-size: 14px;
  color: #64748b;
}

.feature-action .el-button {
  width: 100%;
}

/* 快捷操作 */
.quick-actions-section h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 16px;
}

.action-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

@media (max-width: 800px) {
  .action-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

.action-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: white;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
}

.action-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  font-size: 20px;
}

.action-card.transfer .action-icon {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.action-card.history .action-icon {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}

.action-card.profile .action-icon {
  background: rgba(139, 92, 246, 0.1);
  color: #8b5cf6;
}

.action-card.security .action-icon {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.action-info h4 {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 2px;
}

.action-info p {
  font-size: 12px;
  color: #64748b;
}

/* 最近交易 */
.transactions-section {
  background: white;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
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
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
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

.tx-desc {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a2e;
  margin-bottom: 4px;
}

.tx-date {
  font-size: 12px;
  color: #94a3b8;
}

.tx-amount {
  font-size: 16px;
  font-weight: 600;
}

.tx-amount.in {
  color: #10b981;
}

.tx-amount.out {
  color: #f59e0b;
}

/* 产品介绍 */
.product-intro {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border-radius: 20px;
  padding: 40px;
  color: white;
}

.product-intro h2 {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 32px;
  text-align: center;
}

.features-intro {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

@media (max-width: 800px) {
  .features-intro {
    grid-template-columns: repeat(2, 1fr);
  }
}

.intro-item {
  text-align: center;
}

.intro-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  font-size: 28px;
  margin: 0 auto 16px;
}

.intro-item h4 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}

.intro-item p {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
}

/* 引导对话框 */
.guide-dialog-content {
  text-align: center;
}

.guide-welcome {
  margin-bottom: 32px;
}

.welcome-icon {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 24px;
  color: white;
  margin: 0 auto 20px;
}

.guide-welcome h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.guide-welcome p {
  font-size: 14px;
  color: #64748b;
}

.guide-timeline {
  text-align: left;
  margin-bottom: 32px;
}

.timeline-item {
  display: flex;
  gap: 16px;
  padding: 16px 0;
  border-left: 2px solid #e2e8f0;
  margin-left: 15px;
  padding-left: 24px;
  position: relative;
}

.timeline-item.active {
  border-left-color: #667eea;
}

.timeline-dot {
  position: absolute;
  left: -15px;
  top: 16px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e2e8f0;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
}

.timeline-item.active .timeline-dot {
  background: #667eea;
  color: white;
}

.timeline-content h4 {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 4px;
}

.timeline-content p {
  font-size: 13px;
  color: #64748b;
}

.guide-cta .el-button {
  width: 200px;
}
</style>