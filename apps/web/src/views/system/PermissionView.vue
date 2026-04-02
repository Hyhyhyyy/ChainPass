<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Lock, Edit, Check, Close } from '@element-plus/icons-vue'

// 权限列表
const permissions = ref([
  { id: 1, name: '用户管理', code: 'system:user:list', module: '系统管理', status: 1 },
  { id: 2, name: '用户创建', code: 'system:user:create', module: '系统管理', status: 1 },
  { id: 3, name: '用户编辑', code: 'system:user:edit', module: '系统管理', status: 1 },
  { id: 4, name: '用户删除', code: 'system:user:delete', module: '系统管理', status: 1 },
  { id: 5, name: '角色管理', code: 'system:role:list', module: '系统管理', status: 1 },
  { id: 6, name: '角色创建', code: 'system:role:create', module: '系统管理', status: 1 },
  { id: 7, name: 'DID管理', code: 'identity:did:manage', module: '身份管理', status: 1 },
  { id: 8, name: 'VC管理', code: 'identity:vc:manage', module: '身份管理', status: 1 },
  { id: 9, name: '支付操作', code: 'payment:execute', module: '支付中心', status: 1 },
  { id: 10, name: 'KYC审核', code: 'compliance:kyc:audit', module: '合规中心', status: 1 },
])

// 模块过滤
const filterModule = ref('')
const modules = ['全部', '系统管理', '身份管理', '支付中心', '合规中心']

// 过滤后的权限
const filteredPermissions = ref(permissions.value)

function filterByModule(module: string) {
  filterModule.value = module
  if (module === '全部' || !module) {
    filteredPermissions.value = permissions.value
  } else {
    filteredPermissions.value = permissions.value.filter(p => p.module === module)
  }
}

function toggleStatus(id: number) {
  const perm = permissions.value.find(p => p.id === id)
  if (perm) {
    perm.status = perm.status === 1 ? 0 : 1
    ElMessage.success(`权限 "${perm.name}" 已${perm.status === 1 ? '启用' : '禁用'}`)
  }
}
</script>

<template>
  <div class="permission-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>
        <el-icon><Lock /></el-icon>
        权限管理
      </h1>
      <p>管理系统权限资源和访问控制</p>
    </div>

    <!-- 模块过滤 -->
    <div class="filter-bar">
      <el-radio-group v-model="filterModule" @change="filterByModule">
        <el-radio-button v-for="m in modules" :key="m" :value="m">
          {{ m }}
        </el-radio-button>
      </el-radio-group>
    </div>

    <!-- 权限列表 -->
    <el-card>
      <el-table :data="filteredPermissions" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="权限名称" width="150" />
        <el-table-column prop="code" label="权限编码" width="200">
          <template #default="{ row }">
            <code class="permission-code">{{ row.code }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="所属模块" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.module }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="toggleStatus(row.id)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" link size="small">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.permission-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
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

.filter-bar {
  margin-bottom: 0;
}

.permission-code {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  background: #f1f5f9;
  padding: 4px 8px;
  border-radius: 4px;
  color: #475569;
}
</style>