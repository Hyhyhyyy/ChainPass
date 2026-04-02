<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  User, Key, Shield, Tickets, Wallet,
  Edit, CopyDocument, CircleCheck, CircleClose,
  Calendar, Location, Phone, Message
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores'
import { didApi } from '@/api/did'
import { vcApi } from '@/api/vc'
import { kycApi } from '@/api/kyc'
import { paymentApi } from '@/api/payment'

const router = useRouter()
const userStore = useUserStore()

// 状态
const loading = ref(false)
const didInfo = ref<any>(null)
const kycInfo = ref<any>(null)
const vcList = ref<any[]>([])
const walletInfo = ref<any>(null)
const editingProfile = ref(false)

// 个人信息表单
const profileForm = ref({
  nickname: '',
  email: '',
  phone: '',
  avatar: ''
})

// 计算属性
const hasDID = computed(() => didInfo.value?.did)
const kycVerified = computed(() => kycInfo.value?.verified)
const validVCCount = computed(() => vcList.value.filter(v => v.status === 'VALID').length)

// 获取用户数据
async function fetchUserData() {
  loading.value = true
  try {
    // 初始化个人信息
    profileForm.value.nickname = userStore.nickname || ''
    profileForm.value.email = userStore.email || ''

    // 并行获取数据
    const [didRes, kycRes, walletRes] = await Promise.all([
      didApi.getMy(),
      kycApi.getStatus(),
      paymentApi.getWallet()
    ])

    if (didRes.code === 200 && didRes.data) {
      didInfo.value = didRes.data
      // 获取VC列表（需要DID）
      const vcRes = await vcApi.getList(didInfo.value.did)
      if (vcRes.code === 200) {
        vcList.value = vcRes.data || []
      }
    }

    if (kycRes.code === 200) {
      kycInfo.value = kycRes.data
    }

    if (walletRes.code === 200) {
      walletInfo.value = walletRes.data
    }
  } catch (error) {
    console.error('获取用户数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 保存个人信息
async function saveProfile() {
  ElMessage.success('个人信息已更新')
  editingProfile.value = false
}

// 复制DID
function copyDID() {
  if (didInfo.value?.did) {
    navigator.clipboard.writeText(didInfo.value.did)
    ElMessage.success('DID已复制到剪贴板')
  }
}

// 跳转页面
function goToDID() {
  router.push('/identity/did')
}

function goToKYC() {
  router.push('/compliance/kyc')
}

function goToVC() {
  router.push('/identity/vc')
}

function goToWallet() {
  router.push('/payment/wallet')
}

// 格式化日期
function formatDate(dateStr: string) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

// VC类型映射
const vcTypeNames: Record<string, string> = {
  'IdentityCredential': '身份凭证',
  'KYCCredential': 'KYC认证凭证',
  'PaymentCredential': '支付权限凭证'
}

function getVCTypeName(type: string) {
  return vcTypeNames[type] || type
}

onMounted(() => {
  fetchUserData()
})
</script>

<template>
  <div class="profile-view" v-loading="loading">
    <!-- 个人信息卡片 -->
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
          <el-button type="primary" link :icon="Edit" @click="editingProfile = true">
            编辑
          </el-button>
        </div>
      </template>

      <div class="profile-content">
        <!-- 头像 -->
        <div class="avatar-section">
          <el-avatar :size="100" :icon="User" />
          <div class="user-info">
            <h2>{{ userStore.nickname || userStore.username || '用户' }}</h2>
            <p class="username">@{{ userStore.username }}</p>
          </div>
        </div>

        <!-- 信息详情 -->
        <el-descriptions :column="2" border class="info-descriptions">
          <el-descriptions-item label="用户ID">
            {{ userStore.userId }}
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">
            <el-icon><Calendar /></el-icon>
            {{ formatDate(userStore.createdAt || '') }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            <el-icon><Message /></el-icon>
            {{ userStore.email || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="手机">
            <el-icon><Phone /></el-icon>
            未绑定
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <!-- 编辑个人信息对话框 -->
    <el-dialog v-model="editingProfile" title="编辑个人信息" width="500px">
      <el-form :model="profileForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editingProfile = false">取消</el-button>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>

    <!-- DID身份信息 -->
    <el-card class="did-card">
      <template #header>
        <div class="card-header">
          <span>
            <el-icon><Key /></el-icon>
            DID 去中心化身份
          </span>
          <el-button type="primary" link @click="goToDID">
            管理
          </el-button>
        </div>
      </template>

      <template v-if="hasDID">
        <div class="did-content">
          <div class="did-display">
            <code class="did-code">{{ didInfo.did }}</code>
            <el-button :icon="CopyDocument" circle size="small" @click="copyDID" />
          </div>
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="状态">
              <el-tag :type="didInfo.status === 'ACTIVE' ? 'success' : 'warning'">
                {{ didInfo.status === 'ACTIVE' ? '已激活' : didInfo.status }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDate(didInfo.createdAt) }}
            </el-descriptions-item>
            <el-descriptions-item label="过期时间">
              {{ formatDate(didInfo.expiresAt) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </template>
      <template v-else>
        <el-empty description="尚未创建DID身份" :image-size="80">
          <el-button type="primary" @click="goToDID">创建DID</el-button>
        </el-empty>
      </template>
    </el-card>

    <!-- KYC认证状态 -->
    <el-card class="kyc-card">
      <template #header>
        <div class="card-header">
          <span>
            <el-icon><Shield /></el-icon>
            KYC 身份认证
          </span>
          <el-button type="primary" link @click="goToKYC">
            查看
          </el-button>
        </div>
      </template>

      <template v-if="kycInfo">
        <div class="kyc-content">
          <div class="kyc-status-badge" :class="kycVerified ? 'verified' : 'pending'">
            <el-icon :size="24">
              <component :is="kycVerified ? CircleCheck : CircleClose" />
            </el-icon>
            <span>{{ kycVerified ? '已认证' : kycInfo.statusName || '未认证' }}</span>
          </div>
          <el-descriptions v-if="kycInfo.verified" :column="2" border size="small">
            <el-descriptions-item label="认证等级">
              <el-tag type="success">{{ kycInfo.kycLevelName }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="认证时间">
              {{ formatDate(kycInfo.verifiedAt) }}
            </el-descriptions-item>
            <el-descriptions-item label="有效期至">
              {{ formatDate(kycInfo.expiresAt) }}
            </el-descriptions-item>
          </el-descriptions>
          <el-alert
            v-else-if="kycInfo.status === 'PENDING'"
            type="warning"
            title="认证审核中"
            :closable="false"
            show-icon
          >
            您的KYC认证申请正在审核中，请耐心等待
          </el-alert>
        </div>
      </template>
      <template v-else>
        <el-empty description="未进行KYC认证" :image-size="80">
          <el-button type="primary" @click="goToKYC">开始认证</el-button>
        </el-empty>
      </template>
    </el-card>

    <!-- 可验证凭证 -->
    <el-card class="vc-card">
      <template #header>
        <div class="card-header">
          <span>
            <el-icon><Tickets /></el-icon>
            可验证凭证 (VC)
          </span>
          <el-button type="primary" link @click="goToVC">
            查看全部
          </el-button>
        </div>
      </template>

      <template v-if="vcList.length > 0">
        <div class="vc-list">
          <div v-for="vc in vcList.slice(0, 3)" :key="vc.id" class="vc-item">
            <div class="vc-icon">
              <el-icon><Tickets /></el-icon>
            </div>
            <div class="vc-info">
              <h4>{{ getVCTypeName(vc.type) }}</h4>
              <p>有效期至: {{ formatDate(vc.expiresAt) }}</p>
            </div>
            <el-tag :type="vc.status === 'VALID' ? 'success' : 'warning'" size="small">
              {{ vc.status === 'VALID' ? '有效' : vc.status }}
            </el-tag>
          </div>
        </div>
        <div class="vc-summary">
          共 {{ vcList.length }} 个凭证，{{ validVCCount }} 个有效
        </div>
      </template>
      <template v-else>
        <el-empty description="暂无可验证凭证" :image-size="80">
          <template v-if="hasDID">
            <el-button type="primary" @click="goToKYC">获取KYC凭证</el-button>
          </template>
          <template v-else>
            <el-button type="primary" @click="goToDID">先创建DID</el-button>
          </template>
        </el-empty>
      </template>
    </el-card>

    <!-- 钱包概览 -->
    <el-card class="wallet-card">
      <template #header>
        <div class="card-header">
          <span>
            <el-icon><Wallet /></el-icon>
            钱包概览
          </span>
          <el-button type="primary" link @click="goToWallet">
            管理
          </el-button>
        </div>
      </template>

      <template v-if="walletInfo">
        <div class="wallet-balance">
          <div class="balance-item">
            <span class="currency">CNY</span>
            <span class="amount">¥{{ (walletInfo.balanceCny || 0).toFixed(2) }}</span>
          </div>
          <div class="balance-item">
            <span class="currency">USD</span>
            <span class="amount">${{ (walletInfo.balanceUsd || 0).toFixed(2) }}</span>
          </div>
          <div class="balance-item">
            <span class="currency">ETH</span>
            <span class="amount">Ξ{{ (walletInfo.balanceEth || 0).toFixed(8) }}</span>
          </div>
        </div>
      </template>
      <template v-else>
        <el-empty description="钱包未初始化" :image-size="80">
          <el-button type="primary" @click="goToWallet">初始化钱包</el-button>
        </el-empty>
      </template>
    </el-card>

    <!-- 快捷操作 -->
    <div class="quick-links">
      <el-button @click="router.push('/user/security')">
        <el-icon><Key /></el-icon>
        安全设置
      </el-button>
      <el-button @click="router.push('/user/devices')">
        <el-icon><Location /></el-icon>
        登录设备
      </el-button>
      <el-button @click="router.push('/user/logs')">
        <el-icon><Calendar /></el-icon>
        操作日志
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.profile-view {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

/* 个人信息 */
.profile-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.user-info h2 {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 8px;
}

.username {
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

/* DID卡片 */
.did-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.did-display {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
}

.did-code {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  color: var(--el-text-color-primary);
  word-break: break-all;
}

/* KYC卡片 */
.kyc-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.kyc-status-badge {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  border-radius: 12px;
  font-size: 18px;
  font-weight: 600;
}

.kyc-status-badge.verified {
  background: rgba(103, 194, 58, 0.1);
  color: var(--el-color-success);
}

.kyc-status-badge.pending {
  background: rgba(230, 162, 60, 0.1);
  color: var(--el-color-warning);
}

/* VC卡片 */
.vc-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.vc-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
}

.vc-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
  border-radius: 10px;
  color: white;
}

.vc-info {
  flex: 1;
}

.vc-info h4 {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.vc-info p {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.vc-summary {
  padding-top: 12px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  text-align: center;
}

/* 钱包卡片 */
.wallet-balance {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

@media (max-width: 600px) {
  .wallet-balance {
    grid-template-columns: 1fr;
  }
}

.balance-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  background: var(--el-fill-color-light);
  border-radius: 12px;
}

.balance-item .currency {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
}

.balance-item .amount {
  font-size: 24px;
  font-weight: 600;
  font-family: 'Monaco', 'Menlo', monospace;
}

/* 快捷链接 */
.quick-links {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
</style>