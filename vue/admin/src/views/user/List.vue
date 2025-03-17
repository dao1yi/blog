<template>
  <div class="user-list">
    <div class="header">
      <div class="header-left">
        <h3 class="page-title">用户管理</h3>
        <el-tag type="info" class="user-count">共 {{ users.total }} 个用户</el-tag>
      </div>
    </div>

    <div class="search-area">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item>
          <el-input
            v-model="queryParams.username"
            placeholder="搜索用户名"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
            class="search-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="queryParams.email"
            placeholder="搜索邮箱"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
            class="search-input"
          >
            <template #prefix>
              <el-icon><Message /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-select 
            v-model="queryParams.status" 
            placeholder="用户状态"
            clearable
            @change="handleSearch"
            class="status-select"
          >
            <el-option label="全部" :value="undefined" />
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :icon="Search">搜索</el-button>
          <el-button @click="handleReset" :icon="Refresh">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-card class="table-card">
      <el-table 
        :data="users.records" 
        v-loading="loading"
        :header-cell-style="{
          background: '#f5f7fa',
          color: '#606266',
          fontWeight: 500
        }"
        :cell-style="{
          padding: '16px'
        }"
        border
      >
        <el-table-column label="头像" width="150px">
          <template #default="{ row }">
            <el-avatar 
              :src="row.avatar" 
              :size="40"
              :alt="row.username"
            />
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="300px" />
        <el-table-column prop="email" label="邮箱" width="300px" />
        <el-table-column prop="role" label="角色" width="150px">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : ''">
              {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="150px">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="320px">
          <template #default="{ row }">
            {{ formatDateTime(row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="315px" fixed="right">
          <template #default="{ row }">
            <el-space :size="20">
              <el-button 
                type="primary"
                plain 
                size="default"
                @click="handleEdit(row)"
              >
                编辑
              </el-button>
              <el-button 
                :type="row.status === 1 ? 'danger' : 'success'"
                plain
                size="default"
                @click="handleToggleStatus(row)"
              >
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button 
                type="danger"
                plain
                size="default"
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="users.total"
          @current-change="getUsers"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50, 100]"
        />
      </div>
    </el-card>

    <!-- 编辑用户对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle"
      width="500px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form 
        :model="userForm" 
        :rules="rules"
        ref="formRef"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="普通用户" value="USER" />
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

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Message, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { userApi } from '../../apis'  // 导入 userApi

interface User {
  id: number
  username: string
  email: string
  avatar: string | null
  role: string
  status: number
  createdTime: string
}

interface UserList {
  records: User[]
  total: number
  size: number
  current: number
  pages: number
}

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()

const users = ref<UserList>({
  records: [],
  total: 0,
  size: 10,
  current: 1,
  pages: 0
})

const queryParams = reactive({
  page: 1,
  size: 10,
  username: '',
  email: '',
  status: undefined as number | undefined
})

const userForm = reactive({
  id: 0,
  username: '',
  email: '',
  role: 'USER'
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 获取用户列表
const getUsers = async () => {
  loading.value = true
  try {
    const { data } = await userApi.getUsers(queryParams)
    users.value = data
  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  queryParams.page = 1
  getUsers()
}

// 重置搜索
const handleReset = () => {
  queryParams.username = ''
  queryParams.email = ''
  queryParams.status = undefined
  queryParams.page = 1
  getUsers()
}

// 编辑用户
const handleEdit = (row: User) => {
  dialogTitle.value = '编辑用户'
  userForm.id = row.id
  userForm.username = row.username
  userForm.email = row.email
  userForm.role = row.role
  dialogVisible.value = true
}

// 切换用户状态
const handleToggleStatus = async (row: User) => {
  try {
    await ElMessageBox.confirm(
      `确定要${row.status === 1 ? '禁用' : '启用'}该用户吗？`, 
      '提示', 
      { type: 'warning' }
    )
    await userApi.updateUserStatus(row.id, row.status === 1 ? 0 : 1)
    ElMessage.success('操作成功')
    getUsers()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

// 删除用户
const handleDelete = async (row: User) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      type: 'warning'
    })
    // 暂时注释掉删除功能，因为 API 中没有提供
    // await userApi.deleteUser(row.id)
    ElMessage.success('删除成功')
    getUsers()
  } catch (error) {
    console.error('删除用户失败:', error)
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (userForm.id) {
          await userApi.updateUser(userForm.id, {
            username: userForm.username,
            email: userForm.email,
            role: userForm.role
          })
          ElMessage.success('更新成功')
        }
        dialogVisible.value = false
        getUsers()
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error('操作失败')
      }
    }
  })
}

// 格式化日期时间
const formatDateTime = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}

onMounted(() => {
  getUsers()
})
</script>

<style scoped>
.user-list {
  padding: 24px;
  background-color: #f6f8fa;
  min-height: calc(100vh - 64px);
}

.header {
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2f3d;
  background: linear-gradient(120deg, var(--primary-color), #1d39c4);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.user-count {
  font-weight: normal;
  font-size: 14px;
  border-radius: 6px;
  padding: 0 12px;
  height: 28px;
  line-height: 28px;
  background: #f4f4f5;
  border: none;
}

.search-area {
  margin-bottom: 24px;
  padding: 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.search-input {
  width: 300px;
}

.status-select {
  width: 200px;
}

.table-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

:deep(.el-table) {
  --el-table-border-color: #ebeef5;
  --el-table-header-bg-color: #f8f9fa;
}

:deep(.el-table th) {
  background-color: var(--el-table-header-bg-color);
  font-weight: 600;
  color: #1f2f3d;
  padding: 16px;
}

:deep(.el-table td) {
  padding: 16px;
}

:deep(.el-table--border) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-avatar) {
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

:deep(.el-avatar:hover) {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

:deep(.el-tag) {
  border-radius: 4px;
  padding: 0 12px;
  height: 24px;
  line-height: 24px;
  font-weight: 500;
  border: none;
}

:deep(.el-button.el-button--primary.is-plain),
:deep(.el-button.el-button--danger.is-plain),
:deep(.el-button.el-button--success.is-plain) {
  padding: 8px 16px;
  font-size: 14px;
  min-width: 80px;
  background: rgba(64, 158, 255, 0.1);
  border: none;
  transition: all 0.3s ease;
}

:deep(.el-button.el-button--primary.is-plain:hover) {
  background: var(--primary-color);
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
}

:deep(.el-button.el-button--danger.is-plain:hover) {
  background: #f56c6c;
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.15);
}

:deep(.el-button.el-button--success.is-plain:hover) {
  background: #67c23a;
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.15);
}

.pagination {
  padding: 16px;
  display: flex;
  justify-content: flex-end;
  background: #fff;
  border-top: 1px solid var(--el-table-border-color);
}

/* 暗色主题适配 */
@media (prefers-color-scheme: dark) {
  .user-list {
    background-color: #141414;
  }

  .page-title {
    color: #e5e5e5;
  }

  .user-count {
    background: #262626;
    color: #a6a6a6;
  }

  .search-area,
  .table-card {
    background-color: #1f1f1f;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  }

  :deep(.el-table) {
    --el-table-border-color: #303030;
    --el-table-header-bg-color: #262626;
    background-color: #1f1f1f;
  }

  :deep(.el-table th) {
    color: #e5e5e5;
  }

  :deep(.el-avatar) {
    border-color: #303030;
    background-color: #262626;
  }

  :deep(.el-button.el-button--primary.is-plain) {
    background: rgba(64, 158, 255, 0.1);
    color: #a6d1ff;
  }

  :deep(.el-button.el-button--danger.is-plain) {
    background: rgba(245, 108, 108, 0.1);
    color: #ff9999;
  }

  :deep(.el-button.el-button--success.is-plain) {
    background: rgba(103, 194, 58, 0.1);
    color: #95d475;
  }

  .pagination {
    background-color: #1f1f1f;
    border-top-color: #303030;
  }
}

.file-name,
.action-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

:deep(.el-table .cell) {
  padding: 0 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style> 