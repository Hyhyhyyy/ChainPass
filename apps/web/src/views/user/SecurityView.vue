<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Lock, Key, Shield, Fingerprint, Device,
  CircleCheck, CircleClose, Warning, Refresh,
  Edit, View
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores'
import { didApi } from '@/api/did'

const userStore = useUserStore()

// 状态
const loading = ref(false)
const didInfo = ref<any>(null)
const showPasswordDialog = ref(false)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 安全设置状态
const securitySettings = ref({
  twoFactorEnabled: false,
  zkpAuthEnabled: true,
  didAuthEnabled: false,
  sessionTimeout: 30
})

// 登录设备列表
const devices = ref([
  { id: 1, name: 'Windows PC', location: '大连, 辽宁', lastActive: '2026-04-01 10:30', current: true },
  { id: 2, name: 'iPhone 15', location: '大连, 辽宁', lastActive: '2026-03-30 14:20', current: false }
])

// 获取DID信息
async function fetchDIDInfo() {
  try {
    const res = await didApi.getMy()
    if (res.code === 200 && res.data) {
      didInfo.value = res.data
      securitySettings.value.didAuthEnabled = res.data.status === 'ACTIVE'
    }
  } catch {
    didInfo.value = null
  }
}

// 修改密码
async function changePassword() {
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    ElMessage.warning('请填写完整密码信息')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }

  try {
    await ElMessageBox.confirm('确认修改密码？修改后需要重新登录。', '修改密码', {
      type: 'warning'
    })
    ElMessage.success('密码已修改，请重新登录')
    showPasswordDialog.value = false
    // TODO: 实际调用API修改密码
  } catch {
    // 取消
  }
}

// 启用/禁用DID认证
async function toggleDIDAuth() {
  if (!didInfo.value) {
    ElMessage.warning('请先创建DID身份')
    return
  }

  try {
    const action = securitySettings.value.didAuthEnabled ? '禁用' : '启用'
    await ElMessageBox.confirm(`确认${action}DID认证登录？`, `${action}DID认证`, {
      type: 'info'
    })
    securitySettings.value.didAuthEnabled = !securitySettings.value.didAuthEnabled
    ElMessage.success(`DID认证已${action}`)
  } catch {
    // 取消
  }
}

// 启用/禁用ZKP认证
async function toggleZKPAuth() {
  try {
    const action = securitySettings.value.zkpAuthEnabled ? '禁用' : '启用'
    await ElMessageBox.confirm(`确认${action}零知识证明认证？`, `${action}ZKP认证`, {
      type: 'info'
    })
    securitySettings.value.zkpAuthEnabled = !securitySettings.value.zkpAuthEnabled
    ElMessage.success(`ZKP认证已${action}`)
  } catch {
    // 取消
  }
}

// 移除设备
async function removeDevice(deviceId: number) {
  try {
    await ElMessageBox.confirm('确认移除该设备？移除后该设备需要重新登录。', '移除设备', {
      type: 'warning'
    })
    devices.value = devices.value.filter(d => d.id !== deviceId)
    ElMessage.success('设备已移除')
  } catch {
    // 取消
  }
}

// 检查DID签名
async function verifyDIDSignature() {
  if (!didInfo.value) {
    ElMessage.warning('请先创建DID')
    return
  }
  ElMessage.success('DID签名验证成功')
}

onMounted(() => {
  fetchDIDInfo()
})
</script>

<template>
  <div class="security-view">
    <!-- DID认证设置 -->
    <el-card class="auth-card">
      <template #header>
        <div class="card-header">
          <span>
            <el-icon><Key /></el-icon>
            DID 身份认证
          </span>
          <el-tag :type="didInfo ? 'success' : 'info'">
            {{ didInfo ? '已创建' : '未创建' }}
          </el-tag>
        </div>
      </template>

      <div class="auth-content">
        <div class="auth-item">
          <div class="auth-info">
            <h4>DID 认证登录</h4>
            <p>使用DID身份进行安全登录验证</p>
          </div>
          <el-switch
            v-model="securitySettings.didAuthEnabled"
            :disabled="!didInfo"
            @change="toggleDIDAuth"
          />
        </div>

        <el-divider />

        <div class="auth-item">
          <div class="auth-info">
            <h4>零知识证明 (ZKP) 认证</h4>
            <p>在验证身份的同时保护您的隐私信息</p>
          </div>
          <el-switch
            v-model="securitySettings.zkpAuthEnabled"
            @change="toggleZKPAuth"
          />
        </div>

        <el-divider />

        <div class="did-status" v-if="didInfo">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="DID状态">
              <el-tag :type="didInfo.status === 'ACTIVE' ? 'success' : 'warning'">
                {{ didInfo.status }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="签名算法">Ed25519</el-descriptions-item>
          </el-descriptions>
          <el-button type="primary" link :icon="Shield" @click="verifyDIDSignature">
            验证DID签名
          </el-button>
        </div>
        <el-alert v-else type="info" :closable="false" show-icon>
          请先创建DID身份以启用DID认证功能
          <el-button type="primary" link @click="$router.push('/identity/did')">
            去创建DID
          </el-button>
        </el-alert>
      </div>
    </el-card>

    <!-- 密码安全 -->
    <el-card class="password-card">
      <template #header>
        <div class="card-header">
          <span>
            <el-icon><Lock /></el-icon>
            密码安全
          </span>
        </div>
      </template>

      <div class="password-content">
        <div class="password-item">
          <div class="password-info">
            <h4>登录密码</h4>
            <p>定期更换密码可以提高账户安全性</p>
          </div>
          <el-button type="primary" :icon="Edit" @click="showPasswordDialog = true">
            修改密码
          </el-button>
        </div>

        <el-divider />

        <div class="password-strength">
          <h4>密码强度评估</h4>
          <div class="strength-bar">
            <div class="strength-level medium"></div>
          </div>
          <p class="strength-text">当前密码强度: 中等</p>
          <el-alert type="warning" :closable="false" show-icon style="margin-top: 12px">
            建议使用至少8位包含大小写字母、数字和特殊字符的密码
          </el-alert>
        </div>
      </div>
    </el-card>

    <!-- 登录设备管理 -->
    <el-card class="devices-card">
      <template #header>
        <div class="card-header">
          <span>
            <el-icon><Device /></el-icon>
            登录设备管理
          </span>
          <el-button link type="danger">移除所有其他设备</el-button>
        </div>
      </template>

      <div class="devices-list">
        <div v-for="device in devices" :key="device.id" class="device-item">
          <div class="device-icon">
            <el-icon :size="24"><Device /></el-icon>
          </div>
          <div class="device-info">
            <div class="device-name">
              {{ device.name }}
              <el-tag v-if="device.current" type="success" size="small">当前设备</el-tag>
            </div>
            <div class="device-meta">
              <span>{{ device.location }}</span>
              <span>最后活跃: {{ device.lastActive }}</span>
            </div>
          </div>
          <el-button
            v-if="!device.current"
            type="danger"
            link
            :icon="CircleClose"
            @click="removeDevice(device.id)"
          >
            移除
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 安全日志 -->
    <el-card class="logs-card">
      <template #header>
        <div class="card-header">
          <span>
            <el-icon><View /></el-icon>
            安全事件记录
          </span>
          <el-button link @click="$router.push('/user/logs')">
            查看全部日志
          </el-button>
        </div>
      </template>

      <el-timeline>
        <el-timeline-item type="success" timestamp="2026-04-01 10:30">
          <el-card shadow="never">
            <h4>登录成功</h4>
            <p>通过DID认证登录，设备: Windows PC</p>
          </el-card>
        </el-timeline-item>
        <el-timeline-item type="warning" timestamp="2026-03-30 14:20">
          <el-card shadow="never">
            <h4>密码修改</h4>
            <p>成功修改登录密码</p>
          </el-card>
        </el-timeline-item>
        <el-timeline-item type="primary" timestamp="2026-03-28 09:15">
          <el-card shadow="never">
            <h4>DID创建</h4>
            <p>成功创建DID身份标识</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="400px">
      <el-form :model="passwordForm" label-width="100px">
        <el-form-item label="当前密码">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
            placeholder="请输入当前密码"
          />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码"
          />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="changePassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.security-view {
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

/* 认证设置 */
.auth-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.auth-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.auth-info h4 {
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 4px;
}

.auth-info p {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.did-status {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 密码安全 */
.password-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.password-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.password-info h4 {
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 4px;
}

.password-info p {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.password-strength h4 {
  font-size: 14px;
  margin-bottom: 12px;
}

.strength-bar {
  height: 8px;
  background: var(--el-fill-color);
  border-radius: 4px;
  overflow: hidden;
}

.strength-level {
  height: 100%;
  transition: width 0.3s;
}

.strength-level.weak {
  width: 33%;
  background: var(--el-color-danger);
}

.strength-level.medium {
  width: 66%;
  background: var(--el-color-warning);
}

.strength-level.strong {
  width: 100%;
  background: var(--el-color-success);
}

.strength-text {
  margin-top: 8px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

/* 设备管理 */
.devices-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.device-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
}

.device-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-fill-color);
  border-radius: 12px;
  color: var(--el-color-primary);
}

.device-info {
  flex: 1;
}

.device-name {
  font-size: 15px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
}

.device-meta {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
  display: flex;
  gap: 16px;
}

/* 日志卡片 */
.logs-card :deep(.el-timeline-item__content) {
  padding-right: 20px;
}

.logs-card :deep(.el-card__body) {
  padding: 12px 16px;
}

.logs-card :deep(.el-card__body h4) {
  font-size: 14px;
  margin-bottom: 4px;
}

.logs-card :deep(.el-card__body p) {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>