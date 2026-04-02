<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  DocumentChecked, Loading, CircleCheck, CircleClose,
  Upload, Right, Shield, User, Location, Stamp,
  WarningFilled, SuccessFilled
} from '@element-plus/icons-vue'
import { kycApi, type KYCResponse, type KYCStatusResponse, ID_TYPE_OPTIONS, NATIONALITY_OPTIONS } from '@/api/kyc'
import { didApi } from '@/api/did'

// 状态
const loading = ref(false)
const submitting = ref(false)
const kycStatus = ref<KYCStatusResponse | null>(null)
const kycDetail = ref<KYCResponse | null>(null)
const hasDID = ref(true)
const currentStep = ref(0)

// 表单数据
const form = ref({
  fullName: '',
  nationality: '',
  idType: 'id_card',
  idNumber: '',
  idDocumentFront: '',
  idDocumentBack: ''
})

// 计算属性
const isVerified = computed(() => kycStatus.value?.verified)
const isPending = computed(() => kycStatus.value?.status === 'PENDING')
const isRejected = computed(() => kycStatus.value?.status === 'REJECTED')
const canSubmit = computed(() => hasDID.value && !isVerified.value && !isPending.value)

// 认证等级数据
const kycLevels = [
  {
    level: 1,
    name: '基础认证',
    desc: '姓名、国籍、证件信息验证',
    features: ['跨境支付基础权限', '单笔限额 ¥5,000', '每日限额 ¥20,000'],
    icon: Shield,
    color: '#3b82f6'
  },
  {
    level: 2,
    name: '中级认证',
    desc: '增加人脸识别、地址验证',
    features: ['提升支付限额', '单笔限额 ¥50,000', '每日限额 ¥200,000'],
    icon: Stamp,
    color: '#f59e0b'
  },
  {
    level: 3,
    name: '高级认证',
    desc: '完整企业级认证',
    features: ['无限制支付权限', '专属客服支持', '优先审核通道'],
    icon: CircleCheck,
    color: '#10b981'
  }
]

// 检查DID状态
async function checkDID() {
  try {
    const res = await didApi.getMy()
    hasDID.value = res.code === 200 && !!res.data
  } catch {
    hasDID.value = false
  }
}

// 获取KYC状态
async function fetchKYCStatus() {
  loading.value = true
  try {
    const [statusRes, detailRes] = await Promise.all([
      kycApi.getStatus(),
      kycApi.getDetail()
    ])

    if (statusRes.code === 200) {
      kycStatus.value = statusRes.data
    }
    if (detailRes.code === 200) {
      kycDetail.value = detailRes.data
    }
  } catch (error) {
    console.error('获取KYC状态失败:', error)
  } finally {
    loading.value = false
  }
}

// 提交KYC
async function submitKYC() {
  if (!form.value.fullName) {
    ElMessage.warning('请输入真实姓名')
    return
  }
  if (!form.value.nationality) {
    ElMessage.warning('请选择国籍')
    return
  }
  if (!form.value.idNumber) {
    ElMessage.warning('请输入证件号码')
    return
  }

  submitting.value = true
  try {
    const res = await kycApi.submit(form.value)
    if (res.code === 200) {
      ElMessage.success('🎉 KYC认证申请已提交！')
      currentStep.value = 2
      await fetchKYCStatus()
    }
  } catch (error: any) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

// 获取状态配置
function getStatusConfig() {
  if (!kycStatus.value) return { icon: DocumentChecked, color: '#64748b', text: '未认证' }
  switch (kycStatus.value.status) {
    case 'APPROVED':
      return { icon: CircleCheck, color: '#10b981', text: '认证通过' }
    case 'REJECTED':
      return { icon: CircleClose, color: '#ef4444', text: '认证失败' }
    case 'PENDING':
      return { icon: Loading, color: '#f59e0b', text: '审核中' }
    default:
      return { icon: DocumentChecked, color: '#64748b', text: '未认证' }
  }
}

onMounted(() => {
  checkDID()
  fetchKYCStatus()
})
</script>

<template>
  <div class="kyc-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h1>
          <el-icon class="title-icon"><Shield /></el-icon>
          KYC 身份认证
        </h1>
        <p>完成身份认证，解锁跨境支付功能</p>
      </div>
    </div>

    <!-- 未创建DID提示 -->
    <div v-if="!hasDID" class="did-required">
      <div class="required-content">
        <el-icon class="warning-icon"><WarningFilled /></el-icon>
        <h3>请先创建DID身份</h3>
        <p>KYC认证需要先创建DID身份作为认证基础</p>
        <el-button type="primary" size="large" @click="$router.push('/identity/did')">
          去创建DID
          <el-icon><Right /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- 认证状态展示 -->
    <div v-if="hasDID" class="kyc-content">
      <!-- 状态概览 -->
      <div class="status-overview">
        <div class="status-visual" :style="{ borderColor: getStatusConfig().color }">
          <div class="status-icon" :style="{ background: getStatusConfig().color }">
            <el-icon :size="40">
              <component :is="getStatusConfig().icon" />
            </el-icon>
          </div>
          <div class="status-text">
            <h2>{{ getStatusConfig().text }}</h2>
            <p v-if="kycStatus?.kycLevelName">{{ kycStatus.kycLevelName }}</p>
          </div>
        </div>

        <!-- 已认证信息 -->
        <div v-if="isVerified && kycDetail" class="verified-info">
          <div class="info-row">
            <el-icon><User /></el-icon>
            <span class="label">姓名</span>
            <span class="value">{{ kycDetail.fullName }}</span>
          </div>
          <div class="info-row">
            <el-icon><Location /></el-icon>
            <span class="label">国籍</span>
            <span class="value">{{ kycDetail.nationality }}</span>
          </div>
          <div class="info-row">
            <el-icon><DocumentChecked /></el-icon>
            <span class="label">认证时间</span>
            <span class="value">{{ kycDetail.verifiedAt }}</span>
          </div>
        </div>

        <!-- 审核中提示 -->
        <div v-if="isPending" class="pending-notice">
          <el-icon class="pending-icon"><Loading /></el-icon>
          <p>您的认证申请正在审核中，通常需要1-3个工作日</p>
        </div>

        <!-- 拒绝原因 -->
        <div v-if="isRejected" class="rejected-notice">
          <el-icon><CircleClose /></el-icon>
          <p>认证未通过，请检查信息后重新提交</p>
          <el-button type="primary" @click="kycStatus = null">重新申请</el-button>
        </div>
      </div>

      <!-- 认证等级说明 -->
      <div class="levels-section">
        <h3>认证等级说明</h3>
        <div class="levels-grid">
          <div v-for="level in kycLevels" :key="level.level" class="level-card">
            <div class="level-icon" :style="{ background: level.color }">
              <el-icon><component :is="level.icon" /></el-icon>
            </div>
            <div class="level-info">
              <h4>{{ level.name }}</h4>
              <p>{{ level.desc }}</p>
              <ul class="level-features">
                <li v-for="(feature, idx) in level.features" :key="idx">
                  <el-icon><CircleCheck /></el-icon>
                  {{ feature }}
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <!-- 认证表单 -->
      <div v-if="canSubmit" class="form-section">
        <div class="form-header">
          <h3>开始认证</h3>
          <p>请填写真实信息，确保与证件一致</p>
        </div>

        <el-form :model="form" label-position="top" class="kyc-form">
          <div class="form-grid">
            <el-form-item label="真实姓名" required>
              <el-input
                v-model="form.fullName"
                placeholder="请输入与证件一致的姓名"
                size="large"
              />
            </el-form-item>

            <el-form-item label="国籍" required>
              <el-select
                v-model="form.nationality"
                placeholder="请选择国籍"
                size="large"
                style="width: 100%"
              >
                <el-option
                  v-for="opt in NATIONALITY_OPTIONS"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="证件类型" required>
              <el-radio-group v-model="form.idType" size="large">
                <el-radio-button
                  v-for="opt in ID_TYPE_OPTIONS"
                  :key="opt.value"
                  :value="opt.value"
                >
                  {{ opt.label }}
                </el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="证件号码" required>
              <el-input
                v-model="form.idNumber"
                placeholder="请输入证件号码"
                size="large"
              />
            </el-form-item>
          </div>

          <el-form-item label="证件照片">
            <div class="upload-grid">
              <div class="upload-box">
                <el-upload
                  class="uploader"
                  action="#"
                  :show-file-list="false"
                  :auto-upload="false"
                >
                  <div class="upload-content">
                    <el-icon class="upload-icon"><Upload /></el-icon>
                    <span class="upload-text">上传证件正面</span>
                  </div>
                </el-upload>
              </div>
              <div class="upload-box">
                <el-upload
                  class="uploader"
                  action="#"
                  :show-file-list="false"
                  :auto-upload="false"
                >
                  <div class="upload-content">
                    <el-icon class="upload-icon"><Upload /></el-icon>
                    <span class="upload-text">上传证件背面</span>
                  </div>
                </el-upload>
              </div>
            </div>
            <p class="upload-hint">* 证件照片将加密存储，仅用于身份验证</p>
          </el-form-item>

          <div class="form-actions">
            <el-button type="primary" size="large" :loading="submitting" @click="submitKYC">
              <el-icon><DocumentChecked /></el-icon>
              提交认证
            </el-button>
          </div>
        </el-form>
      </div>

      <!-- 已认证提示 -->
      <div v-if="isVerified" class="success-section">
        <div class="success-content">
          <div class="success-icon">
            <el-icon :size="48"><CircleCheck /></el-icon>
          </div>
          <h3>恭喜！您已完成KYC认证</h3>
          <p>认证通过后已自动签发KYC凭证，可用于跨境支付权限验证</p>
          <el-button type="primary" size="large" @click="$router.push('/payment/transfer')">
            开始跨境支付
            <el-icon><Right /></el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 安全保障说明 -->
    <div class="security-section">
      <h3>🔒 安全保障</h3>
      <div class="security-grid">
        <div class="security-item">
          <el-icon><Shield /></el-icon>
          <span>所有身份数据加密存储</span>
        </div>
        <div class="security-item">
          <el-icon><DocumentChecked /></el-icon>
          <span>符合《数据安全法》要求</span>
        </div>
        <div class="security-item">
          <el-icon><Stamp /></el-icon>
          <span>认证通过自动签发VC凭证</span>
        </div>
        <div class="security-item">
          <el-icon><Shield /></el-icon>
          <span>支持零知识证明验证</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.kyc-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 页面标题 */
.page-header {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
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

/* DID要求提示 */
.did-required {
  background: white;
  border-radius: 20px;
  padding: 60px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.required-content .warning-icon {
  font-size: 64px;
  color: #f59e0b;
  margin-bottom: 20px;
}

.required-content h3 {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 12px;
}

.required-content p {
  font-size: 16px;
  color: #64748b;
  margin-bottom: 24px;
}

/* 状态概览 */
.status-overview {
  background: white;
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.status-visual {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 24px;
  border-radius: 16px;
  border: 2px solid;
  background: #f8fafc;
}

.status-icon {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 20px;
  color: white;
}

.status-text h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 4px;
}

.status-text p {
  font-size: 14px;
  color: #64748b;
}

.verified-info {
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 10px;
}

.info-row .el-icon {
  color: #10b981;
  font-size: 18px;
}

.info-row .label {
  color: #64748b;
  font-size: 14px;
}

.info-row .value {
  margin-left: auto;
  font-weight: 500;
  color: #1a1a2e;
}

.pending-notice {
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px;
  background: rgba(245, 158, 11, 0.1);
  border-radius: 12px;
  text-align: center;
}

.pending-icon {
  font-size: 32px;
  color: #f59e0b;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.rejected-notice {
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px;
  background: rgba(239, 68, 68, 0.1);
  border-radius: 12px;
  text-align: center;
  color: #ef4444;
}

/* 认证等级 */
.levels-section {
  background: white;
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.levels-section h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 20px;
}

.levels-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.level-card {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 16px;
}

.level-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  color: white;
  font-size: 20px;
  flex-shrink: 0;
}

.level-info h4 {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 4px;
}

.level-info p {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 12px;
}

.level-features {
  list-style: none;
  padding: 0;
  margin: 0;
}

.level-features li {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #475569;
  margin-bottom: 4px;
}

.level-features .el-icon {
  font-size: 12px;
  color: #10b981;
}

/* 表单区域 */
.form-section {
  background: white;
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.form-header {
  margin-bottom: 24px;
}

.form-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 4px;
}

.form-header p {
  font-size: 14px;
  color: #64748b;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

@media (max-width: 640px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}

.upload-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.upload-box {
  aspect-ratio: 16/10;
}

.uploader {
  width: 100%;
  height: 100%;
}

.uploader :deep(.el-upload) {
  width: 100%;
  height: 100%;
}

.upload-content {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 2px dashed #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-content:hover {
  border-color: #10b981;
  background: rgba(16, 185, 129, 0.05);
}

.upload-icon {
  font-size: 32px;
  color: #94a3b8;
  margin-bottom: 8px;
}

.upload-text {
  font-size: 14px;
  color: #64748b;
}

.upload-hint {
  margin-top: 12px;
  font-size: 12px;
  color: #94a3b8;
}

.form-actions {
  margin-top: 32px;
}

.form-actions .el-button {
  width: 100%;
}

/* 成功区域 */
.success-section {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border-radius: 20px;
  padding: 48px;
  color: white;
  text-align: center;
}

.success-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.success-icon {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  margin-bottom: 24px;
}

.success-content h3 {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 12px;
}

.success-content p {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 24px;
}

.success-section .el-button {
  background: white;
  border-color: white;
  color: #10b981;
}

/* 安全保障 */
.security-section {
  background: #f8fafc;
  border-radius: 20px;
  padding: 32px;
}

.security-section h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 20px;
}

.security-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.security-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
}

.security-item .el-icon {
  font-size: 20px;
  color: #10b981;
}

.security-item span {
  font-size: 14px;
  color: #475569;
}

@media (max-width: 640px) {
  .levels-grid, .security-grid {
    grid-template-columns: 1fr;
  }

  .level-card {
    flex-direction: column;
    text-align: center;
  }
}
</style>