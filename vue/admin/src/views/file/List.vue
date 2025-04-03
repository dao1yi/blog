<template>
  <div class="file-list">
    <div class="header">
      <div class="header-left">
        <h3 class="page-title">文件管理</h3>
        <el-tag type="info" class="file-count">共 {{ files.total }} 个文件</el-tag>
      </div>
      <el-upload
        class="upload-button"
        :http-request="handleUpload"
        :show-file-list="false"
        :before-upload="beforeUpload"
      >
        <el-button type="primary" class="upload-btn">
          <el-icon><Upload /></el-icon>
          上传文件
        </el-button>
      </el-upload>
    </div>

    <div class="search-area">
      <el-form :inline="true" :model="queryParams">
        <el-form-item>
          <el-input
            v-model="queryParams.fileName"
            placeholder="搜索文件名"
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
          <el-select 
            v-model="queryParams.fileType" 
            placeholder="文件类型"
            clearable
            @change="handleSearch"
            class="type-select"
          >
            <el-option 
              v-for="option in fileTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-card class="table-card">
      <el-table 
        :data="files.records" 
        v-loading="loading"
        :header-cell-style="{
          background: '#f5f7fa',
          color: '#606266',
          fontWeight: 500
        }"
        border
      >
        <el-table-column label="预览" width="180">
          <template #default="{ row }">
            <div class="preview-container">
              <el-image 
                v-if="isImage(row.fileType)"
                :src="row.fileUrl"
                :preview-src-list="[row.fileUrl]"
                :initial-index="0"
                fit="cover"
                class="file-preview"
                preview-teleported
              />
              <el-icon v-else :size="40" class="file-icon">
                <Document v-if="isDocument(row.fileType)" />
                <Files v-else />
              </el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="fileName" label="文件名" min-width="140">
          <template #default="{ row }">
            <div class="file-name">
              <a :href="row.fileUrl" target="_blank" class="file-link">
                {{ row.fileName }}
              </a>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="fileSize" label="大小" width="120">
          <template #default="{ row }">
            <span class="file-size">{{ formatFileSize(row.fileSize) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="fileType" label="类型" width="150">
          <template #default="{ row }">
            <el-tag 
              :type="getFileTypeTag(row.fileType)"
              size="small"
              class="file-type-tag"
            >
              {{ getFileTypeLabel(row.fileType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="上传时间" width="180">
          <template #default="{ row }">
            <span class="upload-time">{{ formatDateTime(row.createdTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="340" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button 
                link 
                type="primary" 
                @click="handleDownload(row)"
                class="action-button"
              >
                <el-icon><Download /></el-icon>
                下载
              </el-button>
              <el-divider direction="vertical" />
              <el-button 
                link 
                type="primary" 
                @click="copyFileUrl(row.fileUrl)"
                class="action-button"
              >
                <el-icon><CopyDocument /></el-icon>
                复制链接
              </el-button>
              <el-divider direction="vertical" />
              <el-button 
                link 
                type="danger" 
                @click="handleDelete(row.id)"
                class="action-button"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="files.total"
          @current-change="getFiles"
          background
          layout="total, prev, pager, next, jumper"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { 
  Search, 
  Upload, 
  Document, 
  Files, 
  Download, 
  CopyDocument, 
  Delete 
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fileApi } from '../../apis'

interface FileRecord {
  id: number
  fileName: string
  fileUrl: string
  fileSize: number
  fileType: string
  createdTime: string
}

interface FileList {
  records: FileRecord[]
  total: number
  size: number
  current: number
}

const loading = ref(false)
const files = ref<FileList>({
  records: [],
  total: 0,
  size: 10,
  current: 1
})

const queryParams = reactive({
  page: 1,
  size: 10,
  fileName: '',
  fileType: ''
})

// 获取文件列表
const getFiles = async () => {
  loading.value = true
  try {
    const { data } = await fileApi.getFiles(queryParams)
    files.value = data
  } catch (error) {
    console.error('获取文件列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 上传前校验
const beforeUpload = (file: File) => {
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }
  return true
}

// 处理上传
const handleUpload = async (options: any) => {
  try {
    await fileApi.upload(options.file)
    ElMessage.success('上传成功')
    getFiles()
  } catch (error) {
    console.error('上传失败:', error)
  }
}

// 删除文件
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个文件吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      beforeClose: async (action, instance, done) => {
        if (action === 'confirm') {
          instance.confirmButtonLoading = true
          try {
            await fileApi.deleteFile(id)
            ElMessage.success('删除成功')
            getFiles() // 刷新列表
            done()
          } catch (error) {
            ElMessage.error('删除失败')
          } finally {
            instance.confirmButtonLoading = false
          }
        } else {
          done()
        }
      }
    })
  } catch (error) {
    // 用户取消删除
    console.log('取消删除')
  }
}

// 复制文件链接
const copyFileUrl = (url: string) => {
  // 创建临时输入框
  const textarea = document.createElement('textarea')
  textarea.value = url
  textarea.style.position = 'fixed'
  textarea.style.left = '0'
  textarea.style.top = '0'
  textarea.style.opacity = '0'
  document.body.appendChild(textarea)
  
  try {
    textarea.select()
    document.execCommand('copy')
    ElMessage.success('链接已复制')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败')
  } finally {
    document.body.removeChild(textarea)
  }
}

// 格式化文件大小
const formatFileSize = (size: number) => {
  if (size < 1024) {
    return size + ' B'
  } else if (size < 1024 * 1024) {
    return (size / 1024).toFixed(2) + ' KB'
  } else {
    return (size / 1024 / 1024).toFixed(2) + ' MB'
  }
}

// 格式化时间
const formatDateTime = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString()
}

// 判断文件类型
const isImage = (type: string) => {
  return type.startsWith('image/')
}

const isDocument = (type: string) => {
  return type.startsWith('application/') && type !== 'application/octet-stream'
}

// 处理搜索
const handleSearch = () => {
  // 转换文件类型参数
  const fileType = queryParams.fileType
  if (fileType === 'image') {
    queryParams.fileType = 'image/jpeg'
  } else if (fileType === 'application') {
    queryParams.fileType = 'application/octet-stream'
  } else {
    queryParams.fileType = ''
  }
  
  queryParams.page = 1
  getFiles()
}

// 重置搜索
const handleReset = () => {
  queryParams.fileName = ''
  queryParams.fileType = ''
  queryParams.page = 1
  getFiles()
}

// 修改文件类型选择器的选项
const fileTypeOptions = [
  { label: '全部', value: '' },
  { label: '图片文件', value: 'image' },
  { label: '其他文件', value: 'application' }
]

// 处理下载
const handleDownload = async (file: FileRecord) => {
  try {
    // 如果是图片，使用 fetch 下载
    if (isImage(file.fileType)) {
      const response = await fetch(file.fileUrl)
      const blob = await response.blob()
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.style.display = 'none'
      link.href = url
      link.download = file.fileName
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url) // 释放 URL 对象
    } else {
      // 非图片文件使用原来的方式下载
      const link = document.createElement('a')
      link.style.display = 'none'
      link.href = file.fileUrl
      link.download = file.fileName
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    }
    
    ElMessage.success('开始下载')
  } catch (error) {
    console.error('下载失败:', error)
  }
}

// 获取文件类型标签样式
const getFileTypeTag = (type: string) => {
  if (isImage(type)) return 'success'
  if (isDocument(type)) return 'primary'
  return 'info'
}

// 获取文件类型显示文本
const getFileTypeLabel = (type: string) => {
  if (isImage(type)) {
    // 从 MIME 类型中提取具体格式，如 'image/jpeg' -> 'JPEG'
    const format = type.split('/')[1].toUpperCase()
    return `图片(${format})`
  }
  if (isDocument(type)) {
    // 处理文档类型
    const format = type.split('/')[1]
    switch (format) {
      case 'pdf':
        return 'PDF文档'
      case 'msword':
        return 'Word文档'
      case 'vnd.openxmlformats-officedocument.wordprocessingml.document':
        return 'Word文档'
      default:
        return '文档'
    }
  }
  // 处理其他类型
  if (type === 'application/octet-stream') {
    return '二进制文件'
  }
  return '其他'
}

onMounted(() => {
  getFiles()
})
</script>

<style scoped>
.file-list {
  padding: 24px;
  background-color: #f6f8fa;
  min-height: calc(100vh - 64px);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.file-count {
  font-weight: normal;
  font-size: 14px;
  border-radius: 6px;
  padding: 0 12px;
  height: 28px;
  line-height: 28px;
  background: #f4f4f5;
  border: none;
}

.upload-btn {
  padding: 10px 20px;
  border-radius: 8px;
  transition: all 0.3s ease;
  background: linear-gradient(120deg, var(--primary-color), #1d39c4);
  border: none;
}

.upload-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
}

.upload-btn :deep(.el-icon) {
  margin-right: 8px;
}

.search-area {
  margin-bottom: 24px;
  padding: 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.search-input {
  width: 240px;
}

.type-select {
  width: 160px;
}

.table-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 90px;
}

.file-preview {
  width: 160px;
  height: 90px;
  border-radius: 8px;
  object-fit: cover;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.file-preview:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.file-icon {
  color: #909399;
  transition: all 0.3s ease;
  font-size: 40px;
}

.file-icon:hover {
  color: var(--primary-color);
  transform: scale(1.1);
}

.file-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-link {
  color: var(--primary-color);
  text-decoration: none;
  transition: all 0.3s ease;
  font-weight: 500;
}

.file-link:hover {
  color: #1d39c4;
  transform: translateX(4px);
}

.file-size {
  color: #606266;
  font-size: 14px;
}

.file-type-tag {
  border-radius: 4px;
  padding: 0 12px;
  height: 24px;
  line-height: 24px;
  font-weight: 500;
  border: none;
}

.upload-time {
  color: #606266;
  font-size: 13px;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-button {
  padding: 4px 8px;
  transition: all 0.3s ease;
}

.action-button:hover {
  transform: translateX(2px);
}

.action-button .el-icon {
  margin-right: 4px;
  font-size: 16px;
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

.pagination {
  margin-top: 0;
  padding: 16px;
  display: flex;
  justify-content: flex-end;
  background: #fff;
  border-top: 1px solid var(--el-table-border-color);
}

/* 暗色主题适配 */
@media (prefers-color-scheme: dark) {
  .file-list {
    background-color: #141414;
  }

  .page-title {
    color: #e5e5e5;
  }

  .file-count {
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

  .file-link {
    color: #79bbff;
  }

  .file-link:hover {
    color: #a6d1ff;
  }

  .file-size,
  .upload-time {
    color: #a6a6a6;
  }

  .file-icon {
    color: #a6a6a6;
  }

  .file-icon:hover {
    color: #79bbff;
  }

  .pagination {
    background-color: #1f1f1f;
    border-top-color: #303030;
  }
}
</style> 