<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Position, Refresh, InfoFilled, Right, Wallet,
  ArrowRight, CircleCheck, Lock, Timer
} from '@element-plus/icons-vue'
import { paymentApi, type PaymentOrder } from '@/api/payment'
import { didApi } from '@/api/did'

// 表单数据
const form = ref({
  payeeDid: '',
  amount: null as number | null,
  currency: 'CNY',
  targetCurrency: '',
  sourceCountry: 'CN',
  targetCountry: 'SG',
  beneficiaryName: '',
  paymentPurpose: 'GOODS_SERVICES',
  description: ''
})

// 状态
const loading = ref(false)
const rateLoading = ref(false)
const currentRate = ref<number | null>(null)
const estimatedAmount = ref<number | null>(null)
const feeAmount = ref<number | null>(null)
const createdOrder = ref<PaymentOrder | null>(null)
const showConfirm = ref(false)
const showSuccess = ref(false)
const complianceMessage = ref('')

// 货币选项
const currencyOptions = [
  { value: 'CNY', label: '人民币', symbol: '¥', flag: '🇨🇳' },
  { value: 'USD', label: '美元', symbol: '$', flag: '🇺🇸' },
  { value: 'ETH', label: '以太坊', symbol: 'Ξ', flag: '💎' }
]

const countryOptions = [
  ['CN', '中国'], ['HK', '中国香港'], ['SG', '新加坡'], ['US', '美国'], ['GB', '英国'],
  ['JP', '日本'], ['KR', '韩国'], ['DE', '德国'], ['FR', '法国'], ['AU', '澳大利亚'], ['CA', '加拿大']
].map(([value, label]) => ({ value, label }))

const purposeOptions = [
  { value: 'GOODS_SERVICES', label: '商品或服务贸易' },
  { value: 'EDUCATION', label: '教育支出' },
  { value: 'FAMILY_SUPPORT', label: '家庭赡养' },
  { value: 'TRAVEL', label: '旅行支出' },
  { value: 'BUSINESS', label: '商业往来' }
]

// 是否进行沙盒记账单位换算
const isCrossBorder = computed(() => {
  return form.value.targetCurrency && form.value.targetCurrency !== form.value.currency
})

// 获取汇率
async function fetchRate() {
  if (!form.value.targetCurrency || form.value.targetCurrency === form.value.currency) {
    currentRate.value = null
    estimatedAmount.value = null
    feeAmount.value = form.value.amount ? form.value.amount * 0.001 : null
    return
  }

  rateLoading.value = true
  try {
    const res = await paymentApi.getRate(form.value.currency, form.value.targetCurrency)
    if (res.code === 200 && res.data) {
      currentRate.value = res.data
      calculateEstimate()
    }
  } catch {
    ElMessage.error('获取汇率失败')
  } finally {
    rateLoading.value = false
  }
}

// 计算预估金额
function calculateEstimate() {
  if (!form.value.amount || !currentRate.value) {
    estimatedAmount.value = null
    feeAmount.value = null
    return
  }
  estimatedAmount.value = form.value.amount * currentRate.value
  feeAmount.value = form.value.amount * 0.001
}

// 监听货币变化
watch([() => form.value.currency, () => form.value.targetCurrency], () => {
  fetchRate()
})

watch(() => form.value.amount, () => {
  if (currentRate.value) {
    calculateEstimate()
  } else {
    feeAmount.value = form.value.amount ? form.value.amount * 0.001 : null
  }
})

// 提交支付
async function submitPayment() {
  if (!form.value.payeeDid) {
    ElMessage.warning('请输入收款人DID')
    return
  }
  if (!form.value.amount || form.value.amount <= 0) {
    ElMessage.warning('请输入有效金额')
    return
  }
  if (!form.value.beneficiaryName.trim()) {
    ElMessage.warning('请输入受益人姓名')
    return
  }
  if (form.value.sourceCountry === form.value.targetCountry) {
    ElMessage.warning('汇出地和收款地不能相同')
    return
  }

  loading.value = true
  try {
    const checkRes = await didApi.check(form.value.payeeDid)
    if (checkRes.code !== 200 || !checkRes.data) {
      ElMessage.error('收款人DID无效')
      return
    }

    const res = await paymentApi.createOrder({
      payeeDid: form.value.payeeDid,
      amount: form.value.amount,
      currency: form.value.currency,
      targetCurrency: form.value.targetCurrency || undefined,
      sourceCountry: form.value.sourceCountry,
      targetCountry: form.value.targetCountry,
      beneficiaryName: form.value.beneficiaryName,
      paymentPurpose: form.value.paymentPurpose,
      description: form.value.description || undefined,
    })

    if (res.code === 200 && res.data) {
      createdOrder.value = res.data
      complianceMessage.value = res.data.complianceReasons
      if (res.data.status === 'PENDING') {
        showConfirm.value = true
      } else if (res.data.status === 'COMPLIANCE_REVIEW') {
        ElMessage.warning('订单已进入人工合规复核队列')
      } else {
        ElMessage.error(`合规规则拒绝：${res.data.complianceReasons}`)
      }
    }
  } catch (error: any) {
    ElMessage.error(error.message || '创建订单失败')
  } finally {
    loading.value = false
  }
}

// 执行支付
async function executePayment() {
  if (!createdOrder.value) return

  loading.value = true
  try {
    const res = await paymentApi.execute(createdOrder.value.orderNo)
    if (res.code === 200) {
      showConfirm.value = false
      showSuccess.value = true
    }
  } catch (error: any) {
    ElMessage.error(error.message || '支付失败')
  } finally {
    loading.value = false
  }
}

async function refreshReviewStatus() {
  if (!createdOrder.value) return
  loading.value = true
  try {
    const res = await paymentApi.getOrder(createdOrder.value.orderNo)
    if (res.code === 200 && res.data) {
      createdOrder.value = res.data
      complianceMessage.value = res.data.complianceReasons
      if (res.data.status === 'PENDING') {
        ElMessage.success('订单已通过人工复核，请确认支付')
        showConfirm.value = true
      } else if (res.data.status === 'COMPLIANCE_REJECTED') {
        ElMessage.error(`订单已被拒绝：${res.data.complianceReasons}`)
      } else {
        ElMessage.info('订单仍在人工复核中')
      }
    }
  } catch (error: any) {
    ElMessage.error(error.message || '查询复核状态失败')
  } finally {
    loading.value = false
  }
}

// 重置表单
function resetForm() {
  form.value = {
    payeeDid: '',
    amount: null,
    currency: 'CNY',
    targetCurrency: '',
    sourceCountry: 'CN',
    targetCountry: 'SG',
    beneficiaryName: '',
    paymentPurpose: 'GOODS_SERVICES',
    description: ''
  }
  currentRate.value = null
  estimatedAmount.value = null
  feeAmount.value = null
  createdOrder.value = null
  showSuccess.value = false
  complianceMessage.value = ''
}

// 获取货币符号
function getSymbol(currency: string) {
  const option = currencyOptions.find(o => o.value === currency)
  return option?.symbol || currency
}

function getFlag(currency: string) {
  const option = currencyOptions.find(o => o.value === currency)
  return option?.flag || ''
}
</script>

<template>
  <div class="transfer-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h1>
          <el-icon class="title-icon"><Position /></el-icon>
          跨境合规支付原型
        </h1>
        <p>DID 身份门控、跨境走廊、支付用途、风险决策与人工复核的可运行原型</p>
      </div>
      <div class="header-features">
        <div class="feature">
          <el-icon><Lock /></el-icon>
          <span>DID验证</span>
        </div>

        <div class="feature">
          <el-icon><Timer /></el-icon>
          <span>配置汇率</span>
        </div>
        <div class="feature">
          <el-icon><CircleCheck /></el-icon>
          <span>数据库原子记账</span>
        </div>
      </div>
    </div>

    <!-- 支付表单 -->
    <div class="transfer-container">
      <div class="form-section">
        <!-- 收款人 -->
        <div class="form-group">
          <label>
            <span class="label-text">收款人DID</span>
            <span class="label-required">*</span>
          </label>
          <div class="input-wrapper">
            <el-input
              v-model="form.payeeDid"
              placeholder="输入收款人的DID标识符 (did:chainpass:xxx)"
              size="large"
              clearable
            >
              <template #prefix>
                <el-icon><InfoFilled /></el-icon>
              </template>
            </el-input>
          </div>
          <p class="input-hint">DID 是 ChainPass 本地注册表中的收款人标识</p>
        </div>

        <div class="form-grid">
          <div class="form-group">
            <label><span class="label-text">汇出国家或地区</span><span class="label-required">*</span></label>
            <el-select v-model="form.sourceCountry" size="large" style="width: 100%">
              <el-option v-for="opt in countryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </div>
          <div class="form-group">
            <label><span class="label-text">收款国家或地区</span><span class="label-required">*</span></label>
            <el-select v-model="form.targetCountry" size="large" style="width: 100%">
              <el-option v-for="opt in countryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </div>
        </div>

        <div class="form-grid">
          <div class="form-group">
            <label><span class="label-text">受益人姓名</span><span class="label-required">*</span></label>
            <el-input v-model="form.beneficiaryName" size="large" maxlength="100" placeholder="与收款身份资料一致" />
          </div>
          <div class="form-group">
            <label><span class="label-text">支付用途</span><span class="label-required">*</span></label>
            <el-select v-model="form.paymentPurpose" size="large" style="width: 100%">
              <el-option v-for="opt in purposeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </div>
        </div>

        <!-- 金额输入 -->
        <div class="form-group">
          <label>
            <span class="label-text">支付金额</span>
            <span class="label-required">*</span>
          </label>
          <div class="amount-input">
            <div class="amount-field">
              <el-input-number
                v-model="form.amount"
                :precision="2"
                :min="0.01"
                :max="1000000"
                :step="100"
                size="large"
                placeholder="输入金额"
                controls-position="right"
              />
            </div>
            <div class="currency-select">
              <el-select v-model="form.currency" size="large">
                <el-option
                  v-for="opt in currencyOptions"
                  :key="opt.value"
                  :label="`${opt.flag} ${opt.label}`"
                  :value="opt.value"
                >
                  <div class="currency-option">
                    <span class="flag">{{ opt.flag }}</span>
                    <span class="name">{{ opt.label }}</span>
                    <span class="symbol">{{ opt.symbol }}</span>
                  </div>
                </el-option>
              </el-select>
            </div>
          </div>
        </div>

        <!-- 目标货币 -->
        <div class="form-group">
          <label>
            <span class="label-text">目标货币</span>
            <span class="label-hint">（可选）</span>
          </label>
          <el-select
            v-model="form.targetCurrency"
            placeholder="选择目标货币，不选则使用原货币"
            size="large"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="opt in currencyOptions.filter(o => o.value !== form.currency)"
              :key="opt.value"
              :label="`${opt.flag} ${opt.label}`"
              :value="opt.value"
            />
          </el-select>
        </div>

        <!-- 汇率信息 -->
        <div v-if="isCrossBorder" class="rate-info">
          <div class="rate-header">
            <el-icon class="rate-icon"><Refresh /></el-icon>
            <span>汇率信息</span>
          </div>
          <div class="rate-content" v-loading="rateLoading">
            <div class="rate-row">
              <span class="rate-label">当前汇率</span>
              <span class="rate-value">
                1 {{ form.currency }} = {{ currentRate?.toFixed(4) }} {{ form.targetCurrency }}
              </span>
            </div>
            <div class="rate-row">
              <span class="rate-label">预估到账</span>
              <span class="rate-value highlight">
                {{ getSymbol(form.targetCurrency || '') }} {{ estimatedAmount?.toFixed(2) }}
              </span>
            </div>
            <div class="rate-row">
              <span class="rate-label">手续费</span>
              <span class="rate-value small">
                {{ getSymbol(form.currency) }} {{ feeAmount?.toFixed(form.currency === 'ETH' ? 8 : 2) }} (0.1%)
              </span>
            </div>
          </div>
        </div>

        <!-- 描述 -->
        <div class="form-group">
          <label>
            <span class="label-text">转账说明</span>
            <span class="label-hint">（可选）</span>
          </label>
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="2"
            placeholder="请输入转账说明"
          />
        </div>

        <!-- 提交按钮 -->
        <div class="form-actions">
          <el-button type="primary" size="large" :loading="loading" @click="submitPayment">
            <el-icon><Position /></el-icon>
            提交合规预检
          </el-button>
        </div>

        <el-alert
          v-if="createdOrder?.status === 'COMPLIANCE_REVIEW'"
          type="warning"
          :closable="false"
          show-icon
          class="review-alert"
        >
          <template #title>订单 {{ createdOrder.orderNo }} 正在人工复核</template>
          <p>{{ complianceMessage }}</p>
          <el-button size="small" :loading="loading" @click="refreshReviewStatus">刷新复核状态</el-button>
        </el-alert>
      </div>

      <!-- 侧边信息 -->
      <div class="info-section">
        <div class="info-card">
          <h4>
            <el-icon><Lock /></el-icon>
            安全保障
          </h4>
          <ul>
            <li>付款方必须持有有效人工审核结论</li>
            <li>规则引擎输出可解释风险分数和命中原因</li>
            <li>中风险订单进入授权审核员人工复核</li>
          </ul>
        </div>

        <div class="info-card">
          <h4>
            <el-icon><Timer /></el-icon>
            交易时间
          </h4>
          <ul>
            <li>按配置汇率换算</li>
            <li>低风险订单可立即进入沙盒记账</li>
            <li>人工复核订单需等待审核员处理</li>
          </ul>
        </div>

        <div class="info-card">
          <h4>
            <el-icon><Wallet /></el-icon>
            手续费说明
          </h4>
          <ul>
            <li>沙盒账本手续费: 0.1%</li>
            <li>无最低手续费</li>
            <li>汇率来源: 管理员配置的沙盒汇率</li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 确认对话框 -->
    <el-dialog
      v-model="showConfirm"
      title="确认支付"
      width="500px"
      :close-on-click-modal="false"
      class="confirm-dialog"
    >
      <div class="confirm-content" v-if="createdOrder">
        <div class="confirm-amount">
          <span class="amount-value">
            {{ getSymbol(createdOrder.currency) }} {{ createdOrder.amount }}
          </span>
          <span class="amount-label">支付金额</span>
        </div>

        <div class="confirm-details">
          <div class="detail-row">
            <span class="detail-label">订单号</span>
            <span class="detail-value">{{ createdOrder.orderNo }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">收款人</span>
            <span class="detail-value code">{{ createdOrder.payeeDid?.substring(0, 25) }}...</span>
          </div>
          <div class="detail-row" v-if="createdOrder.exchangeRate">
            <span class="detail-label">汇率</span>
            <span class="detail-value">{{ createdOrder.exchangeRate }}</span>
          </div>
          <div class="detail-row" v-if="createdOrder.feeAmount">
            <span class="detail-label">手续费</span>
            <span class="detail-value">{{ getSymbol(createdOrder.originalCurrency) }} {{ createdOrder.feeAmount }}</span>
          </div>
        </div>

        <el-alert type="warning" :closable="false" show-icon>
          <template #title>
            此操作只转移测试额度；确认后由数据库事务原子记账
          </template>
        </el-alert>
      </div>

      <template #footer>
        <el-button size="large" @click="showConfirm = false">取消</el-button>
        <el-button type="primary" size="large" :loading="loading" @click="executePayment">
          <el-icon><CircleCheck /></el-icon>
          确认支付
        </el-button>
      </template>
    </el-dialog>

    <!-- 成功提示 -->
    <el-dialog
      v-model="showSuccess"
      title=""
      width="500px"
      :show-close="false"
      class="success-dialog"
    >
      <div class="success-content">
        <div class="success-icon">
          <el-icon :size="48"><CircleCheck /></el-icon>
        </div>
        <h2>支付成功！</h2>
        <p>您的跨境支付原型订单已完成内部记账</p>
        <div class="success-actions">
          <el-button size="large" @click="resetForm">继续转账</el-button>
          <el-button type="primary" size="large" @click="$router.push('/payment/history')">
            查看记录
            <el-icon><Right /></el-icon>
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.transfer-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 页面标题 */
.page-header {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  border-radius: 16px;
  padding: 32px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
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

.header-features {
  display: flex;
  gap: 24px;
}

.feature {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.2);
  padding: 8px 16px;
  border-radius: 8px;
}

/* 表单容器 */
.transfer-container {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
}

@media (max-width: 900px) {
  .transfer-container {
    grid-template-columns: 1fr;
  }
}

.form-section {
  background: white;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

@media (max-width: 640px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 8px;
}

.label-text {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a2e;
}

.label-required {
  color: #ef4444;
}

.label-hint {
  font-size: 12px;
  color: #94a3b8;
}

.input-hint {
  margin-top: 8px;
  font-size: 12px;
  color: #94a3b8;
}

/* 金额输入 */
.amount-input {
  display: flex;
  gap: 12px;
}

.amount-field {
  flex: 1;
}

.amount-field :deep(.el-input-number) {
  width: 100%;
}

.currency-select {
  width: 160px;
}

.currency-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.currency-option .flag {
  font-size: 16px;
}

.currency-option .name {
  flex: 1;
}

.currency-option .symbol {
  color: #94a3b8;
}

/* 汇率信息 */
.rate-info {
  background: #f8fafc;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
  border: 1px solid #e2e8f0;
}

.rate-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #1a1a2e;
  margin-bottom: 16px;
}

.rate-icon {
  color: #f59e0b;
}

.rate-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rate-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.rate-label {
  font-size: 13px;
  color: #64748b;
}

.rate-value {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 14px;
  color: #1a1a2e;
}

.rate-value.highlight {
  font-size: 18px;
  font-weight: 600;
  color: #10b981;
}

.rate-value.small {
  font-size: 12px;
  color: #94a3b8;
}

/* 操作按钮 */
.form-actions {
  margin-top: 32px;
}

.form-actions .el-button {
  width: 100%;
}

/* 侧边信息 */
.info-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.info-card h4 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 12px;
}

.info-card h4 .el-icon {
  color: #f59e0b;
}

.info-card ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.info-card li {
  font-size: 13px;
  color: #64748b;
  padding: 6px 0;
  padding-left: 16px;
  position: relative;
}

.info-card li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #f59e0b;
}

/* 确认对话框 */
.confirm-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.confirm-amount {
  text-align: center;
  padding: 24px;
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  border-radius: 16px;
  color: white;
}

.amount-value {
  display: block;
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 8px;
}

.amount-label {
  font-size: 14px;
  opacity: 0.9;
}

.confirm-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 8px;
}

.detail-label {
  font-size: 13px;
  color: #64748b;
}

.detail-value {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a2e;
}

.detail-value.code {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
}

/* 成功对话框 */
.success-content {
  text-align: center;
  padding: 32px;
}

.success-icon {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border-radius: 50%;
  color: white;
  margin: 0 auto 24px;
}

.success-content h2 {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.success-content p {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 32px;
}

.success-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}
</style>
