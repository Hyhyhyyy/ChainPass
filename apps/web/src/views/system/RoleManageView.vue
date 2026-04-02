<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { usePermission } from '@/composables'

// 角色列表
const roleList = ref<any[]>([])
const loading = ref(false)

// 对话框
const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const formRef = ref()

// 表单数据
const form = ref({
  id: 0,
  roleName: '',
  roleCode: '',
  description: '',
  status: 0,
})

// 表单验证规则
const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z_]+$/, message: '角色编码只能包含字母和下划线', trigger: 'blur' },
  ],
}

// 获取角色列表
async function getRoleList() {
  loading.value = true
  try {
    // 模拟数据
    roleList.value = [
      { id: 1, roleName: '超级管理员', roleCode: 'admin', description: '拥有所有权限', status: 0 },
      { id: 2, roleName: '普通用户', roleCode: 'user', description: '普通用户权限', status: 0 },
      { id: 3, roleName: '访客', roleCode: 'guest', description: '只读权限', status: 0 },
    ]
  } finally {
    loading.value = false
  }
}

// 新增角色
function handleAdd() {
  dialogType.value = 'add'
  resetForm()
  dialogVisible.value = true
}

// 编辑角色
function handleEdit(row: any) {
  dialogType.value = 'edit'
  form.value = { ...row }
  dialogVisible.value = true
}

// 删除角色
async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定要删除角色 "${row.roleName}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    ElMessage.success('删除成功')
    getRoleList()
  } catch {
    // 用户取消
  }
}

// 分配权限
function handleAssignPermissions(row: any) {
  ElMessage.info('权限分配功能开发中...')
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate((valid) => {
    if (!valid) return
    ElMessage.success(dialogType.value === 'add' ? '创建成功' : '更新成功')
    dialogVisible.value = false
    getRoleList()
  })
}

// 重置表单
function resetForm() {
  form.value = {
    id: 0,
    roleName: '',
    roleCode: '',
    description: '',
    status: 0,
  }
}

onMounted(() => {
  getRoleList()
})
</script>

<template>
  <div class="role-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增角色
          </el-button>
        </div>
      </template>

      <el-table :data="roleList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="150">
          <template #default="{ row }">
            <el-tag>{{ row.roleCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'">
              {{ row.status === 0 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="warning" link @click="handleAssignPermissions(row)">
              <el-icon><Lock /></el-icon>
              权限
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增角色' : '编辑角色'"
      width="500px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input
            v-model="form.roleCode"
            placeholder="请输入角色编码"
            :disabled="dialogType === 'edit'"
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.role-manage {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>