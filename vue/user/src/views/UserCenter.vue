<template>
  <div class="container">

    <!-- 个人信息区域 -->
    <div v-if="loading.user" class="loading">加载中...</div>
    <template v-else>
      <div class="user-info">
        <div class="avatar-section">
          <template v-if="userInfo?.avatar">
            <img :src="userInfo.avatar" alt="头像" class="user-avatar" />
          </template>
          <div v-else class="user-avatar default-avatar">
            {{ userInfo?.username?.charAt(0)?.toUpperCase() || '?' }}
          </div>
          <div class="user-data">
            <div class="user-item">{{ userInfo?.username || '未设置用户名' }}</div>
            <div class="user-item">{{ userInfo?.email || '未设置邮箱' }}</div>
          </div>
          <el-button class="edit-btn" type="primary" @click="openEditDialog">
            编辑资料
          </el-button>
        </div>
      </div>
    </template>

    <!-- 操作标签区域 -->
    <div class="action-tabs">
      <button 
        class="tab-btn" 
        :class="{ active: activeTab === 'likes' }"
        @click="switchTab('likes')"
      >
        我的点赞
      </button>
      <button 
        class="tab-btn" 
        :class="{ active: activeTab === 'collections' }"
        @click="switchTab('collections')"
      >
        我的收藏
      </button>
    </div>

    <!-- 内容列表区域 -->
    <div v-if="loading.content" class="loading">加载中...</div>
    <template v-else>
      <div v-if="articles?.length > 0" class="content-list" ref="contentListRef" @scroll="handleScroll">
        <div 
          v-for="article in articles" 
          :key="article.id" 
          class="content-item"
          @click="$router.push(`/article/${article.id}`)"
        >
          <img 
            v-if="article.coverImage"
            :src="article.coverImage" 
            alt="封面" 
            class="item-cover" 
          />
          <div class="item-info">
            <div class="item-title">{{ article.title }}</div>
            <div class="item-meta">
              <span>👀 {{ article.viewCount }} 浏览</span>
              <span>❤️ {{ article.likeCount }} 点赞</span>
              <span>📌 {{ article.collectionCount }} 收藏</span>
              <span class="item-time">{{ formatDate(article.createdTime) }}</span>
            </div>
          </div>
        </div>

        <!-- 加载更多提示 -->
        <div v-if="hasMore" class="load-more" :class="{ loading: loading.more }">
          {{ loading.more ? '加载中...' : '下滑加载更多' }}
        </div>
        <div v-else class="no-more">没有更多文章了</div>
      </div>
      <div v-else class="empty-text">暂无内容</div>
    </template>

    <!-- 添加编辑弹窗 -->
    <el-dialog
      v-model="showEditDialog"
      title="编辑个人资料"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" label-width="100px">
        <!-- 头像上传 -->
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            action="/api/files"
            :headers="uploadHeaders"
            name="file"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <img v-if="editForm.avatar" :src="editForm.avatar" class="avatar-preview" />
            <div v-else class="avatar-uploader-placeholder">
              <el-icon><Plus /></el-icon>
            </div>
          </el-upload>
        </el-form-item>

        <!-- 消息推送开关 -->
        <el-form-item label="接收推送">
          <el-switch v-model="editForm.isReceivePush" />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getUserInfo, type User, updateUserInfo } from '@/api/user'
import { getLikedArticles, getCollectedArticles, type Article } from '@/api/article'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { UploadProps } from 'element-plus'

const router = useRouter()
const loading = ref({
  user: true,
  content: true,
  more: false
})
const userInfo = ref<User | null>(null)
const articles = ref<Article[]>([])
const activeTab = ref<'likes' | 'collections'>('likes')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const contentListRef = ref<HTMLElement | null>(null)
const hasMore = computed(() => articles.value.length < total.value)
const showEditDialog = ref(false)
const submitting = ref(false)
const editForm = ref({
  avatar: userInfo.value?.avatar || '',
  isReceivePush: userInfo.value?.isReceivePush === 1
})

// 添加上传请求头
const uploadHeaders = {
  Authorization: localStorage.getItem('token') || ''
}

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    loading.value.user = true
    const response = await getUserInfo()
    userInfo.value = response
  } catch (error) {
    console.error('获取用户信息失败:', error)
  } finally {
    loading.value.user = false
  }
}

// 获取文章列表
const fetchArticles = async (loadMore = false) => {
  if (loadMore && !hasMore.value) return
  
  try {
    if (loadMore) {
      loading.value.more = true
    } else {
      loading.value.content = true
      articles.value = []
      currentPage.value = 1
    }
    
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    
    const response = activeTab.value === 'likes' 
      ? await getLikedArticles(params)
      : await getCollectedArticles(params)
      
    if (loadMore) {
      articles.value.push(...response.records)
    } else {
      articles.value = response.records
    }
    total.value = response.total
  } catch (error) {
    console.error('获取文章列表失败:', error)
  } finally {
    loading.value.content = false
    loading.value.more = false
  }
}

// 切换标签
const switchTab = (tab: 'likes' | 'collections') => {
  activeTab.value = tab
  currentPage.value = 1
  fetchArticles()
}

// 处理滚动加载
const handleScroll = async (e: Event) => {
  const target = e.target as HTMLElement
  const scrollBottom = target.scrollHeight - target.scrollTop - target.clientHeight
  
  // 距离底部50px时加载更多
  if (scrollBottom < 50 && !loading.value.more && hasMore.value) {
    currentPage.value++
    await fetchArticles(true)
  }
}

// 格式化日期
const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 修改上传成功的回调
const handleAvatarSuccess: UploadProps['onSuccess'] = (response) => {

  if (response.code === 200 && response.data) {
    editForm.value.avatar = response.data.url // 假设返回的数据中包含 url 字段
  } else {
    ElMessage.error('上传失败：' + response.message)
  }
}

// 修改上传前的验证
const beforeAvatarUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB！')
    return false
  }
  return true
}

// 打开编辑弹窗时初始化表单数据
const openEditDialog = () => {
  editForm.value = {
    avatar: userInfo.value?.avatar || '',
    isReceivePush: userInfo.value?.isReceivePush === 1
  }
  showEditDialog.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!userInfo.value) return

  try {
    submitting.value = true
    await updateUserInfo(userInfo.value.id, {
      avatar: editForm.value.avatar,
      isReceivePush: editForm.value.isReceivePush ? 1 : 0
    })
    
    ElMessage.success('更新成功')
    showEditDialog.value = false
    await fetchUserInfo() // 刷新用户信息
  } catch (error) {
    console.error('更新用户信息失败:', error)
    ElMessage.error('更新失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchUserInfo()
  fetchArticles()
})
</script>

<style scoped>
.container {
  max-width: 1100px;
  margin: 40px auto;
  padding: 20px;
}

.loading {
  text-align: center;
  padding: 40px;
  color: var(--text-secondary);
  font-size: 16px;
}

.empty-text {
  text-align: center;
  padding: 30px;
  color: var(--text-secondary);
  font-size: 16px;
}

.user-info {
  background: var(--card-background);
  padding: 30px;
  border-radius: var(--border-radius);
  margin-bottom: 30px;
  box-shadow: var(--box-shadow);
  transition: var(--transition-normal);
}

.user-info:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #409EFF;
}

.default-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #409EFF, #1677ff);
  color: white;
  font-size: 32px;
  font-weight: bold;
}

.user-data {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-item {
  font-size: 16px;
  color: var(--text-primary);
}

.action-tabs {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
}

.tab-btn {
  padding: 12px 24px;
  border: none;
  background: rgba(64, 158, 255, 0.1);
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  color: #409EFF;
  transition: all 0.3s;
}

.tab-btn:hover {
  background: rgba(66, 185, 131, 0.2);
  transform: translateY(-2px);
}

.tab-btn.active {
  background: linear-gradient(135deg, #409EFF, #1677ff);
  color: white;
  box-shadow: 0 4px 10px rgba(64, 158, 255, 0.1);
}

.content-list {
  background: var(--card-background);
  padding: 30px;
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow);
  transition: var(--transition-normal);
  max-height: calc(100vh - 300px);
  overflow-y: auto;
}

.content-list:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.content-item {
  display: flex;
  gap: 20px;
  padding: 15px;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: all 0.3s;
}

.content-item:last-child {
  border-bottom: none;
}

.content-item:hover {
  background: rgba(66, 185, 131, 0.05);
  transform: translateX(5px);
}

.item-cover {
  width: 120px;
  height: 80px;
  border-radius: var(--border-radius);
  object-fit: cover;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 16px;
  color: var(--text-primary);
  font-weight: 500;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
}

.item-meta span {
  font-size: 13px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.item-time {
  color: var(--text-secondary);
}

.load-more {
  text-align: center;
  padding: 15px 0;
  color: var(--text-secondary);
  font-size: 14px;
}

.load-more.loading {
  color: var(--text-secondary);
}

.no-more {
  text-align: center;
  padding: 15px 0;
  color: var(--text-secondary);
  font-size: 14px;
}

/* 自定义滚动条样式 */
.content-list::-webkit-scrollbar {
  width: 6px;
}

.content-list::-webkit-scrollbar-thumb {
  background-color: var(--primary-color);
  border-radius: 3px;
  opacity: 0.6;
}

.content-list::-webkit-scrollbar-thumb:hover {
  opacity: 1;
}

.content-list::-webkit-scrollbar-track {
  background-color: var(--page-background);
}

.edit-btn {
  position: absolute;
  right: 20px;
  top: 20px;
}

.avatar-uploader {
  text-align: center;
}

.avatar-uploader .avatar-preview {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-uploader .avatar-uploader-placeholder {
  width: 100px;
  height: 100px;
  border: 1px dashed var(--el-border-color);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: var(--el-text-color-secondary);
  cursor: pointer;
}

.avatar-uploader .avatar-uploader-placeholder:hover {
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
}

.dialog-footer {
  margin-top: 20px;
  text-align: right;
}

@media (max-width: 768px) {
  .container {
    padding: 15px;
    margin: 20px auto;
  }

  .user-info {
    padding: 20px;
    margin-bottom: 20px;
  }

  .avatar-section {
    gap: 15px;
  }

  .user-avatar {
    width: 80px;
    height: 80px;
  }

  .default-avatar {
    font-size: 28px;
  }

  .content-item {
    flex-direction: column;
    gap: 10px;
    padding: 12px;
  }

  .item-cover {
    width: 100%;
    height: 160px;
  }

  .item-meta {
    gap: 10px;
  }

  .tab-btn {
    padding: 10px 20px;
    font-size: 14px;
  }

  .edit-btn {
    position: static;
    margin-top: 10px;
  }
}
</style>