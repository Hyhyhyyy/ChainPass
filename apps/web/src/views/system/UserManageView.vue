<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@/api'
import type { UserInfo } from '@chainpass/shared/types'
import { mockUserList } from '@/mock/previewData'

// 预览模式
const PREVIEW_MODE = true

// 搜索表单
const searchForm = reactive({
  username: '',
  email: '',
  status: undefined as number | undefined,
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

// 用户列表
const userList = ref<UserInfo[]>([])
const loading = ref(false)

// 对话框
const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const formRef = ref()

// 表单数据
const form = reactive({
  id: 0,
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  status: 0,
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为6-32个字符', trigger: 'blur' },
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
  ],
}

// 状态选项
const statusOptions = [
  { label: '正常', value: 0 },
  { label: '停用', value: 1 },
]

// 获取用户列表
async function getUserList() {
  loading.value = true
  try {
    if (PREVIEW_MODE) {
      // 使用预览数据
      let filteredList = [...mockUserList]
      if (searchForm.username) {
        filteredList = filteredList.filter(u => u.username.includes(searchForm.username))
      }
      if (searchForm.email) {
        filteredList = filteredList.filter(u => u.email.includes(searchForm.email))
      }
      if (searchForm.status !== undefined) {
        filteredList = filteredList.filter(u => u.status === searchForm.status)
      }
      userList.value = filteredList
      pagination.total = filteredList.length
    } else {
      const response = await userApi.getUserList({
        ...searchForm,
        page: pagination.page,
        pageSize: pagination.pageSize,
      })

      if (response.code === 200 && response.data) {
        userList.value = response.data.list
        pagination.total = response.data.total
      }
    }
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  pagination.page = 1
  getUserList()
}

// 重置
function handleReset() {
  searchForm.username = ''
  searchForm.email = ''
  searchForm.status = undefined
  handleSearch()
}

// 新增用户
function handleAdd() {
  dialogType.value = 'add'
  resetForm()
  dialogVisible.value = true
}

// 编辑用户
function handleEdit(row: UserInfo) {
  dialogType.value = 'edit'
  form.id = row.id
  form.username = row.username
  form.password = ''
  form.nickname = row.nickname || ''
  form.email = row.email || ''
  form.phone = row.phone || ''
  form.status = row.status
  dialogVisible.value = true
}

// 删除用户
async function handleDelete(row: UserInfo) {
  try {
    await ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const response = await userApi.deleteUser(row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      getUserList()
    }
  } catch {
    // 用户取消
  }
}

// 修改状态
async function handleStatusChange(row: UserInfo) {
  const newStatus = row.status === 0 ? 1 : 0
  try {
    const response = await userApi.updateUserStatus(row.id, newStatus)
    if (response.code === 200) {
      ElMessage.success('状态修改成功')
      getUserList()
    }
  } catch {
    ElMessage.error('状态修改失败')
  }
}

// 重置密码
async function handleResetPassword(row: UserInfo) {
  try {
    await ElMessageBox.confirm(`确定要重置用户 "${row.username}" 的密码吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const response = await userApi.resetPassword(row.id)
    if (response.code === 200) {
      ElMessage.success(`密码已重置为: ${response.data}`)
    }
  } catch {
    // 用户取消
  }
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      if (dialogType.value === 'add') {
        const response = await userApi.createUser({
          username: form.username,
          password: form.password,
          nickname: form.nickname,
          email: form.email,
          phone: form.phone,
        })
        if (response.code === 200) {
          ElMessage.success('创建成功')
          dialogVisible.value = false
          getUserList()
        }
      } else {
        const response = await userApi.updateUser(form.id, {
          nickname: form.nickname,
          email: form.email,
          phone: form.phone,
          status: form.status,
        })
        if (response.code === 200) {
          ElMessage.success('更新成功')
          dialogVisible.value = false
          getUserList()
        }
      }
    } catch {
      ElMessage.error('操作失败')
    }
  })
}

// 重置表单
function resetForm() {
  form.id = 0
  form.username = ''
  form.password = ''
  form.nickname = ''
  form.email = ''
  form.phone = ''
  form.status = 0
}

// 分页改变
function handlePageChange(page: number) {
  pagination.page = page
  getUserList()
}

function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
  getUserList()
}

onMounted(() => {
  getUserList()
})
</script>

<template>
  <div class="user-manage">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="searchForm.email" placeholder="请输入邮箱" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增用户
          </el-button>
        </div>
      </template>

      <el-table :data="userList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'">
              {{ row.status === 0 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="250">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="warning" link @click="handleResetPassword(row)">
              <el-icon><Key /></el-icon>
              重置密码
            </el-button>
            <el-button
              :type="row.status === 0 ? 'warning' : 'success'"
              link
              @click="handleStatusChange(row)"
            >
              {{ row.status === 0 ? '停用' : '启用' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增用户' : '编辑用户'"
      width="500px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :disabled="dialogType === 'edit'"
          />
        </el-form-item>
        <el-form-item v-if="dialogType === 'add'" label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item v-if="dialogType === 'edit'" label="状态">
          <el-select v-model="form.status">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
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
.user-manage {
  padding: 0;
}

.search-card {
  margin-bottom: var(--spacing-md);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: var(--spacing-md);
  display: flex;
  justify-content: flex-end;
}
</style>