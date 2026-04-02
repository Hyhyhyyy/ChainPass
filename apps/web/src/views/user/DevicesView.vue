<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Monitor, CircleCheck, CircleClose, Location, Timer } from '@element-plus/icons-vue'
import { mockDevices } from '@/mock/previewData'

// 预览模式
const PREVIEW_MODE = true

// 设备列表
const devices = ref(mockDevices)

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

// 移除所有其他设备
async function removeAllOtherDevices() {
  try {
    await ElMessageBox.confirm('确认移除所有其他设备？移除后这些设备需要重新登录。', '移除所有设备', {
      type: 'warning'
    })
    devices.value = devices.value.filter(d => d.isCurrent)
    ElMessage.success('已移除所有其他设备')
  } catch {
    // 取消
  }
}

// 获取设备图标
function getDeviceIcon(type: string) {
  return Monitor
}
</script>

<template>
  <div class="devices-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h1>
          <el-icon class="title-icon"><Monitor /></el-icon>
          登录设备管理
        </h1>
        <p>管理您的登录设备，保护账户安全</p>
      </div>
      <el-button type="danger" plain @click="removeAllOtherDevices">
        移除所有其他设备
      </el-button>
    </div>

    <!-- 设备列表 -->
    <div class="devices-list">
      <div v-for="device in devices" :key="device.id" class="device-card">
        <div class="device-icon">
          <el-icon :size="32"><Monitor /></el-icon>
        </div>
        <div class="device-info">
          <div class="device-header">
            <h3>{{ device.deviceName }}</h3>
            <el-tag v-if="device.isCurrent" type="success" effect="dark" size="small">
              <el-icon><CircleCheck /></el-icon>
              当前设备
            </el-tag>
          </div>
          <div class="device-meta">
            <div class="meta-item">
              <el-icon><Location /></el-icon>
              <span>{{ device.location }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Timer /></el-icon>
              <span>最后活跃: {{ device.lastLoginAt }}</span>
            </div>
          </div>
          <div class="device-details">
            <span class="detail-tag">{{ device.deviceType }}</span>
            <span class="detail-tag">{{ device.browser }}</span>
            <span class="detail-tag">{{ device.os }}</span>
          </div>
        </div>
        <div class="device-actions" v-if="!device.isCurrent">
          <el-button type="danger" link @click="removeDevice(device.id)">
            <el-icon><CircleClose /></el-icon>
            移除
          </el-button>
        </div>
      </div>
    </div>

    <!-- 安全提示 -->
    <el-card class="security-tips">
      <template #header>
        <div class="tips-header">
          <el-icon><Monitor /></el-icon>
          <span>安全提示</span>
        </div>
      </template>
      <ul class="tips-list">
        <li>定期检查登录设备，移除不认识的设备</li>
        <li>如果发现可疑设备，请立即修改密码</li>
        <li>建议开启DID认证以提高账户安全性</li>
        <li>移除设备后，该设备需要重新登录</li>
      </ul>
    </el-card>
  </div>
</template>

<style scoped>
.devices-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
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

/* 设备列表 */
.devices-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.device-card {
  display: flex;
  align-items: center;
  gap: 20px;
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.device-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
}

.device-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border-radius: 16px;
  color: white;
  flex-shrink: 0;
}

.device-info {
  flex: 1;
}

.device-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.device-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
}

.device-meta {
  display: flex;
  gap: 24px;
  margin-bottom: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #64748b;
}

.meta-item .el-icon {
  color: #94a3b8;
}

.device-details {
  display: flex;
  gap: 8px;
}

.detail-tag {
  font-size: 12px;
  padding: 4px 10px;
  background: #f1f5f9;
  border-radius: 6px;
  color: #64748b;
}

.device-actions {
  flex-shrink: 0;
}

/* 安全提示 */
.security-tips {
  background: #f8fafc;
  border-radius: 16px;
}

.tips-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.tips-list {
  margin: 0;
  padding-left: 20px;
}

.tips-list li {
  margin-bottom: 8px;
  color: #64748b;
  font-size: 14px;
}
</style>