<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Key, CopyDocument, Refresh, Right,
  Shield, CircleCheck, Timer, Document,
  InfoFilled, SuccessFilled, WarningFilled
} from '@element-plus/icons-vue'
import { didApi, type DIDResponse } from '@/api/did'

// 状态
const loading = ref(false)
const creating = ref(false)
const didInfo = ref<DIDResponse | null>(null)
const showDocument = ref(false)

// 计算属性
const hasDID = computed(() => !!didInfo.value)
const statusConfig = computed(() => {
  if (!didInfo.value) return { type: 'info', text: '未创建', icon: InfoFilled }
  switch (didInfo.value.status) {
    case 'ACTIVE':
      return { type: 'success', text: '已激活', icon: SuccessFilled }
    case 'SUSPENDED':
      return { type: 'warning', text: '已停用', icon: WarningFilled }
    case 'REVOKED':
      return { type: 'danger', text: '已吊销', icon: WarningFilled }
    default:
      return { type: 'info', text: '未知', icon: InfoFilled }
  }
})

// 获取DID信息
async function fetchDID() {
  loading.value = true
  try {
    const res = await didApi.getMy()
    if (res.code === 200 && res.data) {
      didInfo.value = res.data
    } else {
      didInfo.value = null
    }
  } catch {
    didInfo.value = null
  } finally {
    loading.value = false
  }
}

// 创建DID
async function createDID() {
  try {
    await ElMessageBox.confirm(
      'DID是您在区块链上的唯一身份标识，创建后将永久关联您的账户。确定要创建吗？',
      '创建DID身份',
      {
        confirmButtonText: '确认创建',
        cancelButtonText: '取消',
        type: 'info',
        customClass: 'create-did-dialog'
      }
    )

    creating.value = true
    const res = await didApi.create()
    if (res.code === 200) {
      ElMessage.success('🎉 DID身份创建成功！')
      await fetchDID()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '创建失败')
    }
  } finally {
    creating.value = false
  }
}

// 复制DID
async function copyDID(did: string) {
  try {
    await navigator.clipboard.writeText(did)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}

// 格式化
function formatDate(dateStr: string) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

function formatShort(text: string, len = 20) {
  if (!text || text.length <= len * 2) return text
  return text.substring(0, len) + '...' + text.substring(text.length - len)
}

onMounted(() => {
  fetchDID()
})
</script>

<template>
  <div class="did-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h1>
          <el-icon class="title-icon"><Key /></el-icon>
          DID 数字身份
        </h1>
        <p>去中心化身份标识符，让您完全掌控自己的数字身份</p>
      </div>
    </div>

    <!-- 未创建DID状态 -->
    <div v-if="!loading && !hasDID" class="empty-state">
      <div class="empty-illustration">
        <div class="illustration-circle">
          <el-icon :size="64"><Key /></el-icon>
        </div>
      </div>
      <div class="empty-content">
        <h2>创建您的DID身份</h2>
        <p>DID是使用ChainPass所有功能的基础，让我们一起开始吧！</p>

        <div class="features-brief">
          <div class="feature-item">
            <el-icon><Shield /></el-icon>
            <span>自主控制</span>
          </div>
          <div class="feature-item">
            <el-icon><CircleCheck /></el-icon>
            <span>全球唯一</span>
          </div>
          <div class="feature-item">
            <el-icon><Document /></el-icon>
            <span>W3C标准</span>
          </div>
        </div>

        <el-button type="primary" size="large" :loading="creating" @click="createDID">
          <el-icon><Plus /></el-icon>
          立即创建DID
        </el-button>
      </div>
    </div>

    <!-- DID信息展示 -->
    <div v-if="hasDID" class="did-content">
      <!-- 状态卡片 -->
      <div class="status-card">
        <div class="status-visual">
          <div class="status-icon" :class="statusConfig.type">
            <el-icon :size="32"><component :is="statusConfig.icon" /></el-icon>
          </div>
          <div class="status-text">
            <h2>DID身份已就绪</h2>
            <el-tag :type="statusConfig.type" effect="dark" size="large">
              {{ statusConfig.text }}
            </el-tag>
          </div>
        </div>
      </div>

      <!-- DID标识卡片 -->
      <div class="did-card">
        <div class="card-header">
          <h3>
            <el-icon><Key /></el-icon>
            DID标识符
          </h3>
          <el-button link type="primary" @click="copyDID(didInfo?.did || '')">
            <el-icon><CopyDocument /></el-icon>
            复制
          </el-button>
        </div>
        <div class="did-display">
          <code>{{ didInfo?.did }}</code>
        </div>
      </div>

      <!-- 详细信息 -->
      <div class="info-grid">
        <div class="info-card">
          <div class="info-icon">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="info-content">
            <span class="info-label">创建时间</span>
            <span class="info-value">{{ formatDate(didInfo?.createdAt || '') }}</span>
          </div>
        </div>
        <div class="info-card">
          <div class="info-icon warning">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="info-content">
            <span class="info-label">过期时间</span>
            <span class="info-value">{{ formatDate(didInfo?.expiresAt || '') }}</span>
          </div>
        </div>
        <div class="info-card">
          <div class="info-icon success">
            <el-icon><Shield /></el-icon>
          </div>
          <div class="info-content">
            <span class="info-label">签名算法</span>
            <span class="info-value">Ed25519</span>
          </div>
        </div>
        <div class="info-card">
          <div class="info-icon purple">
            <el-icon><Document /></el-icon>
          </div>
          <div class="info-content">
            <span class="info-label">凭证数量</span>
            <span class="info-value">-</span>
          </div>
        </div>
      </div>

      <!-- 公钥信息 -->
      <div class="key-card">
        <div class="card-header">
          <h3>
            <el-icon><Shield /></el-icon>
            公钥信息
          </h3>
        </div>
        <div class="key-display">
          <code>{{ didInfo?.publicKey }}</code>
        </div>
      </div>

      <!-- DID文档 -->
      <div class="document-section">
        <div class="section-header" @click="showDocument = !showDocument">
          <h3>
            <el-icon><Document /></el-icon>
            DID文档 (W3C标准)
          </h3>
          <el-icon :class="{ rotated: showDocument }"><Right /></el-icon>
        </div>
        <el-collapse-transition>
          <div v-show="showDocument" class="document-content">
            <pre>{{ JSON.stringify(didInfo?.document, null, 2) }}</pre>
          </div>
        </el-collapse-transition>
      </div>

      <!-- 操作按钮 -->
      <div class="actions-section">
        <el-button size="large" @click="fetchDID">
          <el-icon><Refresh /></el-icon>
          刷新状态
        </el-button>
        <el-button type="primary" size="large" @click="$router.push('/identity/vc')">
          查看我的凭证
          <el-icon><Right /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- 说明卡片 -->
    <div class="info-section">
      <h3>💡 什么是DID？</h3>
      <div class="info-cards">
        <div class="info-item">
          <div class="info-item-icon">
            <el-icon><Shield /></el-icon>
          </div>
          <div class="info-item-content">
            <h4>自主可控</h4>
            <p>您拥有身份的完全控制权，无需依赖任何第三方</p>
          </div>
        </div>
        <div class="info-item">
          <div class="info-item-icon">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="info-item-content">
            <h4>全球唯一</h4>
            <p>基于密码学生成的唯一标识，不可伪造</p>
          </div>
        </div>
        <div class="info-item">
          <div class="info-item-icon">
            <el-icon><Key /></el-icon>
          </div>
          <div class="info-item-content">
            <h4>隐私保护</h4>
            <p>支持零知识证明，验证身份无需暴露隐私</p>
          </div>
        </div>
        <div class="info-item">
          <div class="info-item-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="info-item-content">
            <h4>国际标准</h4>
            <p>遵循W3C DID规范，具有全球互操作性</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.did-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 页面标题 */
.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 40px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.empty-illustration {
  margin-bottom: 32px;
}

.illustration-circle {
  width: 140px;
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  color: white;
  box-shadow: 0 20px 60px rgba(102, 126, 234, 0.3);
}

.empty-content {
  text-align: center;
}

.empty-content h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 12px;
}

.empty-content p {
  font-size: 16px;
  color: #64748b;
  margin-bottom: 32px;
}

.features-brief {
  display: flex;
  gap: 32px;
  margin-bottom: 32px;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #64748b;
}

.feature-item el-icon {
  font-size: 24px;
  color: #667eea;
}

/* 状态卡片 */
.status-card {
  background: white;
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.status-visual {
  display: flex;
  align-items: center;
  gap: 24px;
}

.status-icon {
  width: 72px;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 20px;
  color: white;
}

.status-icon.success {
  background: linear-gradient(135deg, #10b981, #059669);
}

.status-icon.warning {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.status-icon.danger {
  background: linear-gradient(135deg, #ef4444, #dc2626);
}

.status-icon.info {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
}

.status-text h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 8px;
}

/* DID卡片 */
.did-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-header h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.did-display {
  background: #f8fafc;
  border-radius: 12px;
  padding: 16px;
}

.did-display code {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  color: #475569;
  word-break: break-all;
}

/* 信息网格 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.info-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #667eea;
  border-radius: 12px;
  color: white;
  font-size: 20px;
}

.info-icon.warning {
  background: #f59e0b;
}

.info-icon.success {
  background: #10b981;
}

.info-icon.purple {
  background: #8b5cf6;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  color: #94a3b8;
}

.info-value {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
}

/* 密钥卡片 */
.key-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.key-display {
  background: #f8fafc;
  border-radius: 12px;
  padding: 16px;
  overflow-x: auto;
}

.key-display code {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  color: #475569;
  word-break: break-all;
}

/* 文档区域 */
.document-section {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  cursor: pointer;
  transition: background 0.3s;
}

.section-header:hover {
  background: #f8fafc;
}

.section-header h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.section-header .rotated {
  transform: rotate(90deg);
  transition: transform 0.3s;
}

.document-content {
  padding: 0 24px 24px;
}

.document-content pre {
  margin: 0;
  padding: 20px;
  background: #1a1a2e;
  border-radius: 12px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  color: #e2e8f0;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-all;
}

/* 操作区 */
.actions-section {
  display: flex;
  gap: 16px;
}

.actions-section .el-button {
  flex: 1;
}

/* 说明区域 */
.info-section {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 20px;
  padding: 32px;
}

.info-section h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 24px;
}

.info-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
}

.info-item-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
  font-size: 20px;
  flex-shrink: 0;
}

.info-item-content h4 {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 4px;
}

.info-item-content p {
  font-size: 13px;
  color: #64748b;
}

@media (max-width: 640px) {
  .info-grid, .info-cards {
    grid-template-columns: 1fr;
  }

  .features-brief {
    flex-direction: column;
    gap: 16px;
  }
}
</style>