<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Connection, Edit, Check, CopyDocument, Refresh } from '@element-plus/icons-vue'

// OAuth配置列表
const oauthConfigs = ref([
  {
    id: 1,
    name: 'Gitee',
    type: 'gitee',
    clientId: 'gitee_client_id_xxxx',
    clientSecret: '****hidden****',
    redirectUri: 'https://chainpass.io/auth/oauth/callback',
    scope: 'user_info',
    status: 1,
    createdAt: '2024-01-01 00:00:00'
  },
  {
    id: 2,
    name: 'GitHub',
    type: 'github',
    clientId: 'github_client_id_xxxx',
    clientSecret: '****hidden****',
    redirectUri: 'https://chainpass.io/auth/oauth/callback',
    scope: 'read:user user:email',
    status: 0,
    createdAt: '2024-02-15 10:30:00'
  }
])

// 编辑对话框
const dialogVisible = ref(false)
const editingConfig = ref<any>(null)

function copyClientId(clientId: string) {
  navigator.clipboard.writeText(clientId)
  ElMessage.success('Client ID 已复制')
}

function toggleStatus(id: number) {
  const config = oauthConfigs.value.find(c => c.id === id)
  if (config) {
    config.status = config.status === 1 ? 0 : 1
    ElMessage.success(`${config.name} OAuth 已${config.status === 1 ? '启用' : '禁用'}`)
  }
}

function handleEdit(config: any) {
  editingConfig.value = { ...config }
  dialogVisible.value = true
}

function handleRefresh(config: any) {
  ElMessage.info('正在刷新配置...')
}
</script>

<template>
  <div class="oauth-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>
        <el-icon><Connection /></el-icon>
        OAuth 配置
      </h1>
      <p>管理第三方OAuth登录配置</p>
    </div>

    <!-- 配置列表 -->
    <div class="config-list">
      <el-card v-for="config in oauthConfigs" :key="config.id" class="config-card">
        <div class="config-header">
          <div class="config-info">
            <h3>{{ config.name }}</h3>
            <el-tag :type="config.status === 1 ? 'success' : 'info'" size="small">
              {{ config.status === 1 ? '已启用' : '已禁用' }}
            </el-tag>
          </div>
          <el-switch
            :model-value="config.status === 1"
            @change="toggleStatus(config.id)"
          />
        </div>

        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="Client ID">
            <code class="config-code">{{ config.clientId }}</code>
            <el-button link type="primary" :icon="CopyDocument" @click="copyClientId(config.clientId)" />
          </el-descriptions-item>
          <el-descriptions-item label="Client Secret">
            <code class="config-code">{{ config.clientSecret }}</code>
          </el-descriptions-item>
          <el-descriptions-item label="回调地址" :span="2">
            <code class="config-code">{{ config.redirectUri }}</code>
          </el-descriptions-item>
          <el-descriptions-item label="权限范围">
            <el-tag size="small">{{ config.scope }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ config.createdAt }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="config-actions">
          <el-button type="primary" link @click="handleEdit(config)">
            <el-icon><Edit /></el-icon>
            编辑配置
          </el-button>
          <el-button link @click="handleRefresh(config)">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 说明卡片 -->
    <el-card class="info-card">
      <template #header>
        <span>OAuth 配置说明</span>
      </template>
      <ul class="info-list">
        <li>在 Gitee/GitHub 开发者设置中创建 OAuth 应用</li>
        <li>将 Client ID 和 Client Secret 填入配置</li>
        <li>回调地址需要与第三方平台配置一致</li>
        <li>启用后用户可使用第三方账号登录</li>
      </ul>
    </el-card>
  </div>
</template>

<style scoped>
.oauth-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
  max-width: 800px;
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

.config-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.config-card {
  border-radius: 12px;
}

.config-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.config-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.config-info h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
}

.config-code {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  background: #f1f5f9;
  padding: 4px 8px;
  border-radius: 4px;
  color: #475569;
  margin-right: 8px;
}

.config-actions {
  display: flex;
  gap: 16px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
}

.info-list {
  margin: 0;
  padding-left: 20px;
}

.info-list li {
  margin-bottom: 8px;
  color: #64748b;
  font-size: 14px;
}
</style>