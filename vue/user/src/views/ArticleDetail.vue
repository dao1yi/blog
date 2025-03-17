<template>
  <div class="page-container">
    <!-- 左侧文章内容区 -->
    <div class="content-area">
      <div v-if="loading" class="loading-state">加载中...</div>
      <div v-else class="article-content">
        <h1 class="article-title">{{ article?.title }}</h1>
        <div class="article-meta">
          <div class="author-info">
            <el-avatar :size="40" :src="article?.authorAvatar">道</el-avatar>
            <span class="author-name">道一</span>
          </div>
          <div class="article-stats">
            <span class="stat-item">👀 浏览: {{ article?.viewCount }}</span>
            <span class="stat-item cursor-pointer" @click="handleLike">
              <span :class="{ 'text-primary': isLiked }">
                {{ isLiked ? '❤️' : '🤍' }} 点赞: {{ article?.likeCount }}
              </span>
            </span>
            <span class="stat-item cursor-pointer" @click="handleCollection">
              <span :class="{ 'text-primary': isCollected }">
                {{ isCollected ? '⭐' : '☆' }} 收藏: {{ article?.collectionCount }}
              </span>
            </span>
          </div>
        </div>
        <div class="article-text markdown-body" v-html="htmlContent"></div>
      </div>
    </div>
    
    <!-- 右侧评论区 -->
    <div class="comment-area">
      <h3 class="comment-title">评论 ({{ article?.commentCount || 0 }})</h3>
      
      <!-- 主评论输入框 -->
      <div v-if="!replyToComment" class="comment-input">
        <el-input
          type="textarea"
          :rows="3"
          placeholder="写下你的评论..."
          v-model="commentText"
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
      
      <!-- 评论列表 -->
      <div 
        class="comment-list" 
        ref="commentListRef"
        @scroll="handleCommentScroll"
      >
        <div v-if="loadingComments && !comments.length" class="loading-state">加载评论中...</div>
        <template v-else>
          <div v-if="comments.length === 0" class="empty-state">
            暂无评论，来说两句吧~
          </div>
          <template v-else>
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <div class="comment-user">
                <el-avatar :size="32" :src="comment.userAvatar">{{ comment.userName?.charAt(0) }}</el-avatar>
                <span class="username">{{ comment.userName }}</span>
              </div>
              <div class="comment-body">
                <div class="comment-text">{{ comment.content }}</div>
                <div class="comment-footer">
                  <span class="time">{{ formatDate(comment.createdTime) }}</span>
                  <span class="reply-btn" @click="replyTo(comment)">回复</span>
                  <span 
                    v-if="comment.childCount" 
                    class="expand-btn" 
                    @click="toggleReplies(comment)"
                  >
                    {{ expandedComments.has(comment.id) ? '收起回复' : `加载${comment.childCount}条回复` }}
                  </span>
                  <span 
                    v-if="comment.userId === userInfo?.id" 
                    class="delete-btn" 
                    @click="handleDeleteComment(comment)"
                  >
                    删除
                  </span>
                </div>
                
                <!-- 回复评论的输入框 -->
                <div v-if="replyToComment?.id === comment.id" class="reply-input">
                  <el-input
                    type="textarea"
                    :rows="2"
                    placeholder="写下你的回复..."
                    v-model="commentText"
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
                
                <!-- 回复列表 -->
                <div v-if="comment.childCount" class="reply-list">
                  <div v-if="loadingReplies.has(comment.id)" class="loading-state">
                    加载回复中...
                  </div>
                  <template v-else-if="expandedComments.has(comment.id)">
                    <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                      <div class="comment-user">
                        <el-avatar :size="28" :src="reply.userAvatar">{{ reply.userName?.charAt(0) }}</el-avatar>
                        <span class="username">{{ reply.userName }}</span>
                      </div>
                      <div class="comment-text">{{ reply.content }}</div>
                      <div class="comment-footer">
                        <span class="time">{{ formatDate(reply.createdTime) }}</span>
                        <span 
                          v-if="reply.userId === userInfo?.id" 
                          class="delete-btn" 
                          @click="handleDeleteComment(reply)"
                        >
                          删除
                        </span>
                      </div>
                    </div>
                  </template>
                </div>
              </div>
            </div>
            
            <!-- 加载更多提示 -->
            <div v-if="hasMore" class="load-more" :class="{ loading: loadingMore }">
              {{ loadingMore ? '加载中...' : '下滑加载更多' }}
            </div>
            <div v-else class="no-more">没有更多评论了</div>
          </template>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { marked } from 'marked'
import { 
  getArticleDetail, 
  recordArticleView,
  getArticleLikeStatus,
  toggleArticleLike,
  getCollectionStatus,
  toggleCollection
} from '@/api/article'
import { getComments, submitComment as submitCommentApi, deleteComment } from '@/api/comment'
import { getUserInfo } from '@/api/user'
import type { Article, Comment } from '@/types/article'
import type { User } from '@/types/user'

const route = useRoute()
const router = useRouter()
const articleId = computed(() => Number(route.params.id))

// 文章相关
const loading = ref(true)
const article = ref<Article | null>(null)
const isLiked = ref(false)
const isCollected = ref(false)

// 评论相关
const loadingComments = ref(false)
const loadingMore = ref(false)
const submitting = ref(false)
const commentText = ref('')
const comments = ref<Comment[]>([])
const commentPage = ref(1)
const commentPageSize = ref(10)
const commentTotal = ref(0)
const hasMore = computed(() => comments.value.length < commentTotal.value)
const commentListRef = ref<HTMLElement | null>(null)
const replyToComment = ref<Comment | null>(null)
const expandedComments = ref<Set<number>>(new Set())
const loadingReplies = ref<Set<number>>(new Set())

// 用户信息
const userInfo = ref<User | null>(null)

// 将Markdown转换为HTML
const htmlContent = computed(() => {
  if (!article.value?.content) return ''
  return marked(article.value.content)
})

// 获取文章详情
const fetchArticleDetail = async () => {
  try {
    loading.value = true
    const articleData = await getArticleDetail(articleId.value)
    console.log('文章详情数据:', articleData)
    article.value = articleData
    
    // 记录浏览
    await recordArticleView(articleId.value)
    
    // 判断用户是否登录
    const token = localStorage.getItem('token')
    if (token) {
      // 获取点赞状态
      const likeStatus = await getArticleLikeStatus(articleId.value)
      isLiked.value = likeStatus

      // 获取收藏状态
      const collectionStatus = await getCollectionStatus(articleId.value)
      isCollected.value = collectionStatus
    } else {
      // 未登录时重置状态
      isLiked.value = false
      isCollected.value = false
    }
    
  } catch (error) {
    console.error('获取文章详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理点赞
const handleLike = async () => {
  if (!articleId.value) return
  
  // 判断用户是否登录
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    await toggleArticleLike(articleId.value)
    isLiked.value = !isLiked.value
    if (article.value) {
      article.value.likeCount += isLiked.value ? 1 : -1
    }
    ElMessage.success(isLiked.value ? '点赞成功' : '已取消点赞')
  } catch (error) {
    console.error('操作失败:', error)
  }
}

// 获取评论列表
const fetchComments = async (loadMore = false) => {
  if (!articleId.value || (loadMore && !hasMore.value)) return
  
  try {
    if (loadMore) {
      loadingMore.value = true
    } else {
      loadingComments.value = true
      // 保存已展开的评论ID
      const expandedIds = Array.from(expandedComments.value)
      comments.value = []
      commentPage.value = 1
      
      const result = await getComments({
        articleId: articleId.value,
        page: commentPage.value,
        size: commentPageSize.value
      })
      
      comments.value = result.records || []
      commentTotal.value = result.total || 0
      
      // 重新加载已展开评论的回复
      for (const comment of comments.value) {
        if (expandedIds.includes(comment.id)) {
          await loadReplies(comment)
        }
      }
      return
    }

    const result = await getComments({
      articleId: articleId.value,
      page: commentPage.value,
      size: commentPageSize.value
    })
    
    comments.value.push(...(result.records || []))
    commentTotal.value = result.total || 0
  } catch (error) {
    console.error('获取评论失败:', error)
  } finally {
    loadingComments.value = false
    loadingMore.value = false
  }
}

// 处理评论列表滚动
const handleCommentScroll = async (e: Event) => {
  const target = e.target as HTMLElement
  const scrollBottom = target.scrollHeight - target.scrollTop - target.clientHeight
  
  // 距离底部50px时加载更多
  if (scrollBottom < 50 && !loadingMore.value && hasMore.value) {
    commentPage.value++
    await fetchComments(true)
  }
}

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const token = localStorage.getItem('token')
    if (token) {
      const data = await getUserInfo()
      userInfo.value = data
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 提交评论
const submitComment = async () => {
  if (!commentText.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  // 判断用户是否登录
  if (!userInfo.value) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    submitting.value = true
    const params = {
      articleId: articleId.value,
      content: commentText.value,
      parentId: replyToComment.value?.id
    }
    await submitCommentApi(params)
    
    ElMessage.success('评论成功')
    commentText.value = ''
    
    // 如果是回复评论，重新加载该评论的回复
    if (replyToComment.value) {
      await loadReplies(replyToComment.value)
      replyToComment.value = null
    } else {
      // 如果是主评论，重新加载评论列表
      await fetchComments()
    }
    
    // 更新文章评论数
    if (article.value) {
      article.value.commentCount = (article.value.commentCount || 0) + 1
    }
  } catch (error) {
    console.error('评论失败:', error)
  } finally {
    submitting.value = false
  }
}

// 回复评论
const replyTo = (comment: Comment) => {
  // 判断用户是否登录
  if (!userInfo.value) {
    ElMessage.warning('请先登录')
    return
  }
  
  replyToComment.value = comment
  commentText.value = ''
}

// 取消回复
const cancelReply = () => {
  replyToComment.value = null
  commentText.value = ''
}

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 处理收藏
const handleCollection = async () => {
  if (!articleId.value) return
  
  // 判断用户是否登录
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    await toggleCollection(articleId.value)
    isCollected.value = !isCollected.value
    if (article.value) {
      article.value.collectionCount += isCollected.value ? 1 : -1
    }
    ElMessage.success(isCollected.value ? '收藏成功' : '已取消收藏')
  } catch (error) {
    console.error('操作失败:', error)
  }
}

// 新增：加载子评论的方法
const loadReplies = async (comment: Comment) => {
  if (loadingReplies.value.has(comment.id)) return
  
  try {
    loadingReplies.value.add(comment.id)
    const result = await getComments({
      articleId: articleId.value,
      parentId: comment.id,
      page: 1,
      size: 10
    })
    
    // 更新评论的回复列表
    comment.replies = result.records || []
    
    // 标记评论为展开状态
    expandedComments.value.add(comment.id)
  } catch (error) {
    console.error('加载回复失败:', error)
  } finally {
    loadingReplies.value.delete(comment.id)
  }
}

// 新增：切换评论展开状态
const toggleReplies = async (comment: Comment) => {
  if (expandedComments.value.has(comment.id)) {
    expandedComments.value.delete(comment.id)
  } else {
    if (!comment.replies?.length) {
      await loadReplies(comment)
    } else {
      expandedComments.value.add(comment.id)
    }
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
    console.log(comment)
    
    await deleteComment(articleId.value,comment.id)
    ElMessage.success('删除成功')
    
    // 重新加载评论列表
    await fetchComments()
    
    // 更新文章评论数
    if (article.value) {
      article.value.commentCount = Math.max((article.value.commentCount || 0) - 1, 0)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
    }
  }
}

onMounted(() => {
  if (!articleId.value) {
    router.push('/articles')
    return
  }
  
  fetchUserInfo()
  fetchArticleDetail()
  fetchComments()
})
</script>

<style scoped>
.page-container {
  display: flex;
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.content-area {
  flex: 1;
  min-width: 0;
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.article-title {
  font-size: 28px;
  color: #333;
  margin-bottom: 20px;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-name {
  font-size: 16px;
  color: #333;
}

.article-stats {
  display: flex;
  gap: 15px;
}

.stat-item {
  color: #666;
  font-size: 14px;
}

.article-text {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
}

.comment-area {
  width: 300px;
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.comment-title {
  font-size: 18px;
  color: #333;
  margin-bottom: 20px;
  font-weight: 500;
}

.comment-list {
  margin-bottom: 20px;
  max-height: 600px;
  overflow-y: auto;
  padding-right: 10px;
}

.comment-item {
  margin-bottom: 20px;
}

.comment-user {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.username {
  font-size: 14px;
  color: #333;
}

.comment-body {
  margin-left: 40px;
}

.comment-text {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  margin-bottom: 8px;
}

.comment-footer {
  display: flex;
  gap: 15px;
}

.expand-btn,
.reply-btn {
  font-size: 13px;
  color: #666;
  cursor: pointer;
}

.expand-btn:hover,
.reply-btn:hover {
  color: #409EFF;
}

.comment-input {
  margin-bottom: 20px;
}

.input-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

@media (max-width: 768px) {
  .page-container {
    flex-direction: column;
  }
  
  .comment-area {
    width: 100%;
  }
  
  .comment-body {
    margin-left: 20px;
  }
}

.loading-state {
  text-align: center;
  padding: 40px;
  color: #666;
  font-size: 14px;
}

.empty-state {
  text-align: center;
  padding: 30px;
  color: #999;
  font-size: 14px;
}

.cursor-pointer {
  cursor: pointer;
}

.text-primary {
  color: #409EFF;
}

.time {
  font-size: 12px;
  color: #999;
}

.load-more {
  text-align: center;
  padding: 15px 0;
  color: #666;
  font-size: 14px;
  cursor: pointer;
}

.load-more:hover {
  color: #409EFF;
}

.load-more.loading {
  cursor: default;
  color: #999;
}

.no-more {
  text-align: center;
  padding: 15px 0;
  color: #999;
  font-size: 14px;
}

/* 自定义滚动条样式 */
.comment-list::-webkit-scrollbar {
  width: 6px;
}

.comment-list::-webkit-scrollbar-thumb {
  background-color: #ddd;
  border-radius: 3px;
}

.comment-list::-webkit-scrollbar-track {
  background-color: #f5f5f5;
}

:deep(.markdown-body) {
  font-size: 16px;
  line-height: 1.8;
  
  h1, h2, h3, h4, h5, h6 {
    margin-top: 24px;
    margin-bottom: 16px;
    font-weight: 600;
    line-height: 1.25;
  }
  
  p {
    margin-bottom: 16px;
  }
  
  img {
    max-width: 100%;
    border-radius: 8px;
  }
  
  code {
    padding: 0.2em 0.4em;
    margin: 0;
    font-size: 85%;
    background-color: rgba(27,31,35,0.05);
    border-radius: 6px;
  }
  
  pre {
    padding: 16px;
    overflow: auto;
    font-size: 85%;
    line-height: 1.45;
    background-color: #f6f8fa;
    border-radius: 6px;
    
    code {
      padding: 0;
      margin: 0;
      background-color: transparent;
      border: 0;
    }
  }
}

.reply-list {
  margin-top: 10px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 4px;
}

.reply-item {
  margin-bottom: 15px;
}

.reply-item:last-child {
  margin-bottom: 0;
}

.reply-input {
  margin: 10px 0;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 4px;
}

.delete-btn {
  font-size: 13px;
  color: #ff4d4f;
  cursor: pointer;
}

.delete-btn:hover {
  color: #ff7875;
}
</style>