<template>
  <div class="article-create">
    <div class="header">
      <h3 class="page-title">创建文章</h3>
    </div>

    <el-card class="form-card">
      <el-form 
        :model="articleForm" 
        :rules="rules" 
        ref="formRef"
        label-position="top"
        class="article-form"
      >
        <el-form-item prop="title" label="文章标题">
          <el-input 
            v-model="articleForm.title" 
            placeholder="请输入文章标题" 
            :maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item prop="coverImage" label="封面图片">
          <div class="cover-actions">
            <el-upload
              class="cover-uploader"
              :show-file-list="false"
              accept="image/*"
              :before-upload="beforeUpload"
              :http-request="uploadImage"
            >
              <el-image 
                v-if="articleForm.coverImage" 
                :src="articleForm.coverImage" 
                class="cover-image"
                fit="cover"
              />
              <div v-else class="upload-placeholder">
                <el-icon class="cover-uploader-icon"><Plus /></el-icon>
                <div class="upload-text">点击上传封面</div>
              </div>
            </el-upload>
            <el-button type="primary" @click="openFileDialog" class="select-file-btn">
              <el-icon><Files /></el-icon>
              从文件库选择
            </el-button>
          </div>
          <div class="upload-tip">建议尺寸 900x500px，支持 jpg、png 格式，大小不超过 2MB</div>
        </el-form-item>

        <el-form-item prop="summary" label="文章摘要">
          <el-input 
            v-model="articleForm.summary" 
            type="textarea" 
            :rows="3"
            placeholder="请输入文章摘要"
            :maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="文章选项">
          <div class="article-options">
            <el-switch
              v-model="articleForm.isTop"
              :active-value="1"
              :inactive-value="0"
              active-text="置顶文章"
            />
            <el-switch
              v-model="articleForm.isBroadcast"
              :active-value="1"
              :inactive-value="0"
              active-text="广播文章"
            />
          </div>
        </el-form-item>

        <el-form-item prop="content" label="文章内容">
          <md-editor 
            v-model="articleForm.content"
            @upload-img="handleUploadImage"
            class="md-editor"
          />
        </el-form-item>

        <el-form-item class="form-actions">
          <el-button type="primary" size="large" @click="handleSubmit(1)">
            <el-icon><DocumentAdd /></el-icon>发布文章
          </el-button>
          <el-button size="large" @click="handleSubmit(0)">
            <el-icon><DocumentCopy /></el-icon>保存草稿
          </el-button>
          <el-button size="large" @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 添加文件选择对话框 -->
    <el-dialog
      v-model="fileDialogVisible"
      title="选择图片"
      width="70%"
      :close-on-click-modal="false"
    >
      <div class="file-grid" v-loading="loading">
        <div 
          v-for="file in fileList" 
          :key="file.id"
          class="file-item"
          :class="{ 'is-selected': selectedImage === file.fileUrl }"
          @click="handleSelectImage(file.fileUrl)"
        >
          <el-image 
            :src="file.fileUrl" 
            fit="cover"
            class="file-preview"
          />
          <div class="file-name">{{ file.fileName }}</div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="fileDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmImageSelection">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { MdEditor } from 'md-editor-v3'
import { Plus, DocumentAdd, DocumentCopy, Files } from '@element-plus/icons-vue'
import 'md-editor-v3/lib/style.css'
import type { FormInstance } from 'element-plus'
import { useRouter } from 'vue-router'
import { articleApi, fileApi } from '../../apis'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref<FormInstance>()

const articleForm = reactive({
  title: '',
  content: '',
  summary: '',
  coverImage: '',
  status: 0,
  isTop: 0,
  isBroadcast: 0
})

const rules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入文章内容', trigger: 'blur' }]
}

// 上传前校验
const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 上传封面图片
const uploadImage = async (options: any) => {
  try {
    const { data } = await fileApi.upload(options.file)
    articleForm.coverImage = data.url
    ElMessage.success('上传成功')
  } catch (error) {
    console.error('上传失败:', error)
  }
}

// 处理 Markdown 编辑器的图片上传
const handleUploadImage = async (files: File[], callback: (urls: string[]) => void) => {
  fileDialogVisible.value = true
  const originalCallback = callback
  
  // 重写回调函数
  callback = (urls: string[]) => {
    fileDialogVisible.value = false
    if (urls.length > 0) {
      originalCallback(urls)
    }
  }
}

const handleSubmit = async (status: number) => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await articleApi.createArticle({
          ...articleForm,
          status
        })
        ElMessage.success('创建成功')
        router.push('/articles')
      } catch (error) {
        console.error('创建文章失败:', error)
      }
    }
  })
}

// 添加文件选择相关的状态和方法
const fileDialogVisible = ref(false)
const fileList = ref([])
const selectedImage = ref('')
const loading = ref(false)

// 获取文件列表
const getFiles = async () => {
  loading.value = true
  try {
    const { data } = await fileApi.getFiles({
      page: 1,
      size: 100,
      fileType: ''
    })
    fileList.value = data.records
  } catch (error) {
    console.error('获取文件列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 选择图片
const handleSelectImage = (url: string) => {
  if (selectedImage.value === url) {
    selectedImage.value = ''
  } else {
    selectedImage.value = url
  }
}

// 确认选择
const confirmImageSelection = () => {
  if (selectedImage.value) {
    articleForm.coverImage = selectedImage.value
  }
  fileDialogVisible.value = false
}

// 打开文件选择对话框
const openFileDialog = () => {
  selectedImage.value = articleForm.coverImage
  getFiles()
  fileDialogVisible.value = true
}
</script>

<style scoped>
.article-create {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 60px);
}

.header {
  margin-bottom: 24px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 500;
  color: #1f2f3d;
}

.form-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.article-form {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.cover-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 320px;
  height: 180px;
  transition: border-color 0.3s;
}

.cover-uploader:hover {
  border-color: #409eff;
}

.upload-placeholder {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-text {
  color: #8c939d;
  font-size: 14px;
  margin-top: 8px;
}

.cover-image {
  width: 100%;
  height: 100%;
  display: block;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

:deep(.md-editor) {
  height: 600px;
  margin-bottom: 20px;
  border-radius: 8px;
}

.form-actions {
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}

.form-actions :deep(.el-button) {
  padding: 12px 24px;
  margin-right: 16px;
}

.form-actions :deep(.el-button .el-icon) {
  margin-right: 6px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  padding-bottom: 8px;
  font-size: 15px;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  box-shadow: none;
  border: 1px solid #dcdfe6;
}

:deep(.el-input__wrapper:hover),
:deep(.el-textarea__inner:hover) {
  border-color: #c0c4cc;
}

:deep(.el-input__wrapper:focus-within),
:deep(.el-textarea__inner:focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 1px #409eff;
}

.cover-actions {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.select-file-btn {
  height: 40px;
}

.file-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  padding: 16px;
}

.file-item {
  border: 2px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.file-item:hover {
  border-color: var(--el-color-primary);
  transform: translateY(-2px);
}

.file-item.is-selected {
  border-color: var(--el-color-primary);
  box-shadow: 0 0 0 2px var(--el-color-primary-light-8);
}

.file-preview {
  width: 100%;
  height: 150px;
  object-fit: cover;
}

.file-name {
  padding: 8px;
  font-size: 14px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.article-options {
  display: flex;
  gap: 24px;
  align-items: center;
}

:deep(.el-switch__label) {
  color: #606266;
}

:deep(.el-switch.is-checked .el-switch__label) {
  color: var(--el-color-primary);
}
</style> 