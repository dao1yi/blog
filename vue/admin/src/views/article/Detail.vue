<template>
  <div class="page-container">
    <!-- 左侧目录区域 -->
    <div class="toc-area">
      <h3 class="toc-title">目录</h3>
      <div class="toc-list">
        <template v-if="tocItems.length">
          <div 
            v-for="(item, index) in tocItems" 
            :key="index"
            class="toc-item"
            :class="{ 
              'active': currentHeading === item.id,
              [`level-${item.level}`]: true 
            }"
            @click="scrollToHeading(item.id)"
            :title="item.text"
          >
            {{ item.text }}
          </div>
        </template>
        <div v-else class="no-toc">
          暂无目录
        </div>
      </div>
    </div>

    <!-- 中间文章内容区 -->
    <div class="content-area">
      <!-- 文章头部 -->
      <div class="article-header">
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-meta">
          <span class="meta-item">
            <el-icon><Calendar /></el-icon>
            {{ formatDateTime(article.createdTime) }}
          </span>
          <span class="meta-item">
            <el-icon><View /></el-icon>
            {{ article.viewCount }} 阅读
          </span>
          <span class="meta-item" @click="handleLike">
            <el-icon>
              <Star :style="{ color: article.isLiked ? '#409EFF' : '#666' }" />
            </el-icon>
            {{ article.likeCount }} 点赞
          </span>
          <span class="meta-item" @click="handleCollection">
            <el-icon>
              <Collection :style="{ color: article.isCollected ? '#409EFF' : '#666' }" />
            </el-icon>
            {{ article.collectionCount }} 收藏
          </span>
        </div>
      </div>

      <!-- 文章内容 -->
      <div class="article-scroll-container" ref="articleContainer">
        <div 
          class="article-content markdown-body" 
          v-html="htmlContent" 
          ref="articleContent"
        />
      </div>
    </div>

    <!-- 右侧评论区 -->
    <div class="comment-area">
      <div class="comment-header">
        <h3 class="comment-title">评论 ({{ article.commentCount || 0 }})</h3>
        
        <!-- 主评论输入框 -->
        <div v-if="!replyTo" class="comment-input">
          <el-input
            type="textarea"
            :rows="3"
            placeholder="写下你的评论..."
            v-model="commentContent"
            :maxlength="500"
            show-word-limit
          />
          <div class="input-footer">
            <el-button 
              type="primary" 
              size="small" 
              :loading="submitting"
              @click="submitComment"
            >
              发表评论
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 评论列表 -->
      <div class="comment-scroll-container">
        <div v-if="loading" class="loading-state">加载评论中...</div>
        <template v-else>
          <div v-if="comments.length === 0" class="empty-state">
            暂无评论，来说两句吧~
          </div>
          <div v-else v-for="comment in sortedComments" :key="comment.id" class="comment-item">
            <div class="comment-user">
              <div class="user-info">
                <el-avatar :size="32" :src="comment.userAvatar">{{ comment.userName?.charAt(0) }}</el-avatar>
                <span class="username">{{ comment.userName }}</span>
                <el-tag v-if="comment.isTop" type="danger" size="small" effect="dark">置顶</el-tag>
              </div>
              <div class="action-buttons">
                
                <el-button 
                  link 
                  type="danger" 
                  size="small"
                  @click="handleDeleteComment(comment)"
                >
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
                <el-button 
                  link 
                  :type="comment.isTop ? 'success' : 'info'" 
                  size="small"
                  @click="toggleCommentTop(comment)"
                >
                  <el-icon><Top /></el-icon>
                  {{ comment.isTop ? '取消置顶' : '置顶' }}
                </el-button>
              </div>
            </div>
            <div class="comment-body">
              <div class="comment-text">{{ comment.content }}</div>
              <div class="comment-footer">
                <div class="comment-info">
                  <span class="time">{{ formatDateTime(comment.createdTime) }}</span>
                  <span 
                    v-if="comment.childCount" 
                    class="reply-count"
                    @click="toggleReplies(comment)"
                  >
                    {{ expandedComments.has(comment.id) ? '收起回复' : `${comment.childCount}条回复` }}
                  </span>
                  <el-button 
                    link 
                    type="primary" 
                    size="small"
                    @click="handleReply(comment)"
                  >
                    <el-icon><ChatLineRound /></el-icon>
                    回复
                  </el-button>
                </div>
              </div>
              
              <!-- 回复列表 -->
              <div v-if="comment.childCount && expandedComments.has(comment.id)" class="reply-list">
                <div v-if="loadingReplies.has(comment.id)" class="loading-state">
                  加载回复中...
                </div>
                <template v-else>
                  <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                    <div class="comment-user">
                      <div class="user-info">
                        <el-avatar :size="28" :src="reply.userAvatar">{{ reply.userName?.charAt(0) }}</el-avatar>
                        <span class="username">{{ reply.userName }}</span>
                      </div>
                      <div class="action-buttons">
                        <el-button 
                          v-if="userInfo?.id === reply.userId"
                          link 
                          type="danger" 
                          size="small"
                          @click="handleDeleteComment(reply)"
                        >
                          <el-icon><Delete /></el-icon>
                          删除
                        </el-button>
                      </div>
                    </div>
                    <div class="comment-text">{{ reply.content }}</div>
                    <div class="comment-footer">
                      <span class="time">{{ formatDateTime(reply.createdTime) }}</span>
                    </div>
                  </div>
                </template>
              </div>
              
              <!-- 回复输入框 -->
              <div v-if="replyTo?.id === comment.id" class="reply-input">
                <el-input
                  type="textarea"
                  :rows="2"
                  placeholder="写下你的回复..."
                  v-model="commentContent"
                  :maxlength="500"
                  show-word-limit
                />
                <div class="input-footer">
                  <el-button 
                    type="primary" 
                    size="small" 
                    :loading="submitting"
                    @click="submitComment"
                  >
                    回复
                  </el-button>
                  <el-button 
                    size="small" 
                    @click="cancelReply"
                  >
                    取消
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- 评论弹框 -->
    
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { View, Star, Collection, Calendar, Edit, Delete, Top, ChatLineRound } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { marked } from 'marked'
import { articleApi, commentApi, interactionApi } from '../../apis'
import 'github-markdown-css'
import { ElInfiniteScroll } from 'element-plus'
import axios from 'axios'

const route = useRoute()
const articleId = Number(route.params.id)

// 文章详情
const article = ref({
  id: 0,
  title: '',
  content: '',
  coverImage: '',
  viewCount: 0,
  likeCount: 0,
  collectionCount: 0,
  commentCount: 0,
  createdTime: '',
  isLiked: false,
  isCollected: false
})

// 定义评论类型
interface Comment {
  id: number
  content: string
  userId: number
  userName: string
  userAvatar: string | null
  isTop: number        // 添加置顶字段
  parentId: number | null
  childCount: number | null
  createdTime: string
  replyToUsername?: string  // 添加这个可选字段
  children?: Comment[]
  replies?: Comment[]
}

// 评论相关
const dialogVisible = ref(false)
const commentContent = ref('')
const comments = ref<Comment[]>([])
const commentTotal = ref(0)
const commentQuery = reactive({
  page: 1,
  size: 10
})

// 回复相关
const replyTo = ref<{
  id: number
  userName: string
  parentId?: number
} | null>(null)

// 用户信息
const userInfo = ref({
  id: 0,
  username: ''
})

// 加载状态
const loading = ref(false)
// 是否还有更多数据
const hasMore = computed(() => {
  return comments.value.length < commentTotal.value
})

// 在 script 部分添加计算属性
const sortedComments = computed(() => {
  return [...comments.value].sort((a, b) => {
    // 先按置顶状态排序
    if (a.isTop !== b.isTop) {
      return b.isTop - a.isTop
    }
    // 再按时间倒序
    return new Date(b.createdTime).getTime() - new Date(a.createdTime).getTime()
  })
})

// 目录相关
interface TocItem {
  id: string
  text: string
  level: number
}

const articleContainer = ref<HTMLElement | null>(null)
const articleContent = ref<HTMLElement | null>(null)
const tocItems = ref<TocItem[]>([])
const currentHeading = ref('')

// 修改 htmlContent 计算属性
const htmlContent = computed(() => {
  if (!article.value?.content) return ''
  
  // 使用 marked 转换 Markdown
  const html = marked(article.value.content)
  
  // 等待 DOM 更新后处理目录
  nextTick(() => {
    generateToc()
  })
  
  return html
})

// 生成目录
const generateToc = async () => {
  await nextTick()
  if (!articleContent.value) return
  
  const headings = articleContent.value.querySelectorAll('h1, h2, h3, h4, h5, h6')
  console.log('找到的标题数量:', headings.length) // 调试用
  
  tocItems.value = Array.from(headings).map((heading, index) => {
    const id = `heading-${index}`
    heading.setAttribute('id', id)
    const text = heading.textContent || ''
    const level = parseInt(heading.tagName.charAt(1))
    console.log('处理标题:', { id, text, level }) // 调试用
    return { id, text, level }
  })
}

// 滚动到指定标题
const scrollToHeading = (id: string) => {
  const element = document.getElementById(id)
  if (element && articleContainer.value) {
    articleContainer.value.scrollTo({
      top: element.offsetTop - 100,
      behavior: 'smooth'
    })
    currentHeading.value = id
  }
}

// 监听滚动更新当前标题
const handleScroll = () => {
  if (!articleContainer.value) return
  const scrollTop = articleContainer.value.scrollTop
  
  for (const item of tocItems.value) {
    const element = document.getElementById(item.id)
    if (element && element.offsetTop - 120 <= scrollTop) {
      currentHeading.value = item.id
    }
  }
}

// 获取文章详情
const getArticleDetail = async () => {
  try {
    loading.value = true
    const { data } = await articleApi.getArticle(articleId)
    article.value = data
    
    // 等待 DOM 更新后生成目录
    await nextTick()
    await generateToc()
  } catch (error) {
    console.error('获取文章详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取评论列表
const getComments = async () => {
  loading.value = true
  try {
    // 只获取根评论
    const { data } = await commentApi.getComments(articleId, {
      ...commentQuery,
      parentId: undefined
    })

    if (commentQuery.page === 1) {
      // 第一页时直接替换
      comments.value = data.records.map((comment: Comment) => ({
        ...comment,
        children: []
      }))
    } else {
      // 加载更多时追加
      comments.value.push(...data.records.map((comment: Comment) => ({
        ...comment,
        children: []
      })))
    }
    
    commentTotal.value = data.total
  } catch (error) {
    console.error('获取评论失败:', error)
  } finally {
    loading.value = false
  }
}

// 打开评论弹框
const openCommentDialog = () => {
  dialogVisible.value = true
}

// 提交评论
const submitComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  try {
    await commentApi.createComment(articleId, {
      content: commentContent.value,
      parentId: replyTo.value?.parentId || replyTo.value?.id || null
    })
    ElMessage.success('评论成功')
    commentContent.value = ''
    replyTo.value = null
    resetComments() // 重新加载评论列表
    getArticleDetail() // 更新评论数
  } catch (error) {
    console.error('评论失败:', error)
  } finally {
    getComments()
  }
}

// 在 script setup 中添加
const expandedComments = ref(new Set<number>())
const loadingReplies = ref(new Set<number>())

// 切换评论展开状态
const toggleReplies = async (comment: Comment) => {
  if (expandedComments.value.has(comment.id)) {
    expandedComments.value.delete(comment.id)
  } else {
    if (!comment.replies?.length) {
      await loadReplies(comment)
    }
    expandedComments.value.add(comment.id)
  }
}

// 加载回复列表
const loadReplies = async (comment: Comment) => {
  if (loadingReplies.value.has(comment.id)) return
  
  try {
    loadingReplies.value.add(comment.id)
    const { data } = await commentApi.getComments(
      articleId,
      {
      parentId: comment.id,
      page: 1,
      size: 10
    })
    comment.replies = data.records || []
  } catch (error) {
    console.error('加载回复失败:', error)
  } finally {
    loadingReplies.value.delete(comment.id)
    
  }
}

// 删除评论
const handleDeleteComment = async (comment: Comment) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条评论吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await commentApi.deleteComment(articleId, comment.id)
    ElMessage.success('删除成功')
    
    // 如果是回复评论，重新加载父评论的回复列表
    if (comment.parentId) {
      const parentComment = comments.value.find(c => c.id === comment.parentId)
      if (parentComment) {
        parentComment.childCount--
        await loadReplies(parentComment)
      }
    } else {
      // 如果是主评论，重新加载评论列表
      await getComments()
    }
    
    // 更新文章评论数
    if (article.value) {
      article.value.commentCount = Math.max((article.value.commentCount || 0) - 1, 0)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 点赞
const handleLike = async () => {
  try {
    await interactionApi.toggleLike(articleId)
    // 直接更新本地状态
    article.value.isLiked = !article.value.isLiked
    article.value.likeCount += article.value.isLiked ? 1 : -1
  } catch (error) {
    console.error('操作失败:', error)
  }
}

// 收藏
const handleCollection = async () => {
  try {
    await interactionApi.toggleCollection(articleId)
    // 直接更新本地状态
    article.value.isCollected = !article.value.isCollected
    article.value.collectionCount += article.value.isCollected ? 1 : -1
  } catch (error) {
    console.error('操作失败:', error)
  }
}

// 格式化时间
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

// 取消评论
const cancelComment = () => {
  dialogVisible.value = false
  commentContent.value = ''
  replyTo.value = null
}

// 回复评论
const handleReply = (comment: Comment, parent?: Comment) => {
  replyTo.value = {
    id: comment.id,
    userName: comment.userName,
    parentId: parent?.id
  }
  dialogVisible.value = true  // 打开弹框
}

// 加载更多评论
const loadMoreComments = async () => {
  if (!hasMore.value || loading.value) return
  
  commentQuery.page++
  await getComments()
}

// 重置评论列表
const resetComments = () => {
  comments.value = []
  commentQuery.page = 1
  commentTotal.value = 0
  getComments()
}

// 添加置顶/取消置顶方法
const toggleCommentTop = async (comment: Comment) => {
  try {
    await articleApi.toggleCommentTop(articleId, comment.id)
    ElMessage.success(comment.isTop ? '取消置顶成功' : '置顶成功')
    // 重新获取评论列表
    getComments()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  getArticleDetail()
  getComments()
  generateToc()
  if (articleContainer.value) {
    articleContainer.value.addEventListener('scroll', handleScroll)
  }
})
</script>

<style scoped>
.page-container {
  display: flex;
  gap: 24px;
  padding: 20px;
  max-width: 1600px;
  margin: 0 auto;
  height: calc(100vh - 60px);
  overflow: hidden;
}

.toc-area {
  width: 220px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.toc-title {
  padding: 20px;
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  border-bottom: 1px solid #eee;
}

.toc-list {
  flex: 1;
  overflow-y: auto;
  padding: 15px 0;
}

.toc-item {
  padding: 8px 20px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.2s;
}

.toc-item:hover {
  color: var(--el-color-primary);
  background: rgba(64, 158, 255, 0.1);
}

.toc-item.active {
  color: var(--el-color-primary);
  background: rgba(64, 158, 255, 0.1);
  border-right: 2px solid var(--el-color-primary);
}

.level-1 { padding-left: 20px; }
.level-2 { padding-left: 35px; }
.level-3 { padding-left: 50px; }
.level-4 { padding-left: 65px; }
.level-5 { padding-left: 80px; }
.level-6 { padding-left: 95px; }

.content-area {
  flex: 1;
  min-width: 0;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.article-header {
  margin-bottom: 32px;
  text-align: center;
}

.article-title {
  font-size: 2.4em;
  color: #2c3e50;
  margin-bottom: 16px;
  line-height: 1.4;
}

.article-meta {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 24px;
  color: #666;
  font-size: 14px;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.meta-item:hover {
  background-color: #f5f7fa;
  cursor: pointer;
}

.meta-item .el-icon {
  font-size: 16px;
  transition: transform 0.3s ease;
}

.meta-item:hover .el-icon {
  transform: scale(1.1);
}

.article-scroll-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.article-content {
  font-size: 16px;
  line-height: 1.8;
  color: #2c3e50;
  margin-bottom: 48px;
}

.comment-area {
  width: 320px;
  background: var(--card-background);
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.comment-header {
  padding: 20px 20px 0;
  flex-shrink: 0;
}

.comment-title {
  font-size: 18px;
  color: var(--text-primary);
  margin-bottom: 16px;
  font-weight: 500;
}

.comment-input {
  margin-bottom: 20px;
}

.input-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
  gap: 8px;
}

.comment-scroll-container {
  flex: 1;
  overflow-y: auto;
  padding: 0 20px 20px;
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 40px 0;
  color: var(--text-secondary);
  font-size: 14px;
}

.comment-item {
  padding: 16px;
  background: var(--background-secondary);
  border-radius: 8px;
  margin-bottom: 16px;
  transition: all 0.3s ease;
}

.comment-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.comment-user {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.comment-text {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-primary);
  margin-bottom: 12px;
  word-break: break-word;
}

.comment-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.time {
  font-size: 12px;
  color: var(--text-secondary);
}

.reply-input {
  margin-top: 12px;
  padding: 12px;
  background: var(--background-tertiary);
  border-radius: 6px;
}

:deep(.el-button--small) {
  padding: 4px 8px;
  font-size: 12px;
}

:deep(.el-button.is-link) {
  height: auto;
  padding: 4px 8px;
}

:deep(.el-button.is-link .el-icon) {
  margin-right: 4px;
  font-size: 14px;
}

/* 暗色主题适配 */
:root {
  --card-background: #fff;
  --background-secondary: #f8f9fa;
  --background-tertiary: #f1f3f5;
  --text-primary: #333;
  --text-secondary: #666;
  --border-radius: 8px;
  --box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

@media (prefers-color-scheme: dark) {
  :root {
    --card-background: #1a1a1a;
    --background-secondary: #2a2a2a;
    --background-tertiary: #333;
    --text-primary: #e5e5e5;
    --text-secondary: #999;
    --box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
  }
}

.comment-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.reply-count {
  color: var(--el-color-primary);
  cursor: pointer;
  font-size: 13px;
}

.reply-count:hover {
  opacity: 0.8;
}

.reply-list {
  margin-top: 12px;
  padding: 12px;
  background: var(--background-tertiary);
  border-radius: 6px;
}

.reply-item {
  padding: 12px;
  background: var(--background-secondary);
  border-radius: 6px;
  margin-bottom: 12px;
}

.reply-item:last-child {
  margin-bottom: 0;
}

.reply-item .comment-user {
  justify-content: space-between;
}

.reply-item .user-info {
  gap: 6px; /* 回复评论的间距稍微小一点 */
}

.reply-item .comment-text {
  margin-bottom: 8px;
}

.reply-item .comment-footer {
  margin-top: 8px;
}

.user-info .el-tag {
  margin-left: 4px;
  font-size: 12px;
  height: 20px;
  padding: 0 6px;
}
</style> 