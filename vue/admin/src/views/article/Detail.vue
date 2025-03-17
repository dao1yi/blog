<template>
  <div class="article-detail">
    <!-- 文章头部区域 -->
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

    <!-- 文章封面 -->
    <!-- <div class="article-cover" v-if="article.coverImage">
      <el-image 
        :src="article.coverImage" 
        fit="cover"
        :preview-src-list="[article.coverImage]"
        :initial-index="0"
        class="cover-image"
      />
    </div> -->

    <!-- 文章内容 -->
    <div class="article-content markdown-body" v-html="markdownToHtml(article.content)" />

    <!-- 评论区域 -->
    <div class="article-comments" v-loading="loading">
      <div class="comments-header">
        <h3>评论 ({{ article.commentCount }})</h3>
        <el-button type="primary" @click="openCommentDialog">
          <el-icon><Edit /></el-icon>
          发表评论
        </el-button>
      </div>
      
      <!-- 评论列表 -->
      <div 
        class="comment-list"
        v-infinite-scroll="loadMoreComments"
        :infinite-scroll-disabled="!hasMore || loading"
        :infinite-scroll-distance="10"
      >
        <template v-if="comments.length">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <!-- 父评论 -->
            <div class="comment-main">
              <div class="comment-header">
                <div class="user-info">
                  <el-avatar :src="comment.userAvatar" :size="32" />
                  <span class="username">{{ comment.userName }}</span>
                  <el-button 
                    link 
                    type="primary" 
                    @click="handleReply(comment)"
                  >
                    回复
                  </el-button>
                </div>
                <span class="time">{{ formatDateTime(comment.createdTime) }}</span>
              </div>
              <div class="comment-content">{{ comment.content }}</div>
              <div class="comment-actions">
                <el-button 
                  link 
                  type="danger" 
                  size="small"
                  @click="handleDeleteComment(comment.id)"
                >
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </div>

            <!-- 子评论列表 -->
            <div v-if="comment.childCount > 0" class="comment-replies">
              <div v-if="comment.children?.length" class="reply-list">
                <div v-for="reply in comment.children" :key="reply.id" class="reply-item">
                  <div class="comment-header">
                    <div class="user-info">
                      <el-avatar :src="reply.userAvatar" :size="28" />
                      <span class="username">{{ reply.userName }}</span>
                      <span class="reply-to">回复</span>
                      <span class="username">{{ reply.replyToUsername || comment.userName }}</span>
                      <el-button 
                        link 
                        type="primary" 
                        @click="handleReply(reply, comment)"
                      >
                        回复
                      </el-button>
                    </div>
                    <span class="time">{{ formatDateTime(reply.createdTime) }}</span>
                  </div>
                  <div class="comment-content">{{ reply.content }}</div>
                  <div class="comment-actions">
                    <el-button 
                     
                      link 
                      type="danger" 
                      size="small"
                      @click="handleDeleteComment(reply.id)"
                    >
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-button>
                  </div>
                </div>
              </div>

              <!-- 加载更多按钮 -->
              <div v-if="!comment.children?.length" class="load-more">
                <el-button link type="primary" @click="loadReplies(comment)">
                  查看 {{ comment.childCount }} 条回复
                </el-button>
              </div>
            </div>
          </div>
        </template>
        <el-empty v-else description="暂无评论" />
        
        <!-- 加载更多提示 -->
        <div v-if="comments.length" class="load-more-status">
          <p v-if="loading">加载中...</p>
          <p v-else-if="!hasMore">没有更多评论了</p>
        </div>
      </div>

      <!-- 评论分页 -->
      <div class="comment-pagination" v-if="commentTotal > 0">
        <el-pagination
          v-model:current-page="commentQuery.page"
          v-model:page-size="commentQuery.size"
          :total="commentTotal"
          @current-change="getComments"
        />
      </div>
    </div>

    <!-- 评论弹框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="replyTo ? `回复 ${replyTo.userName}` : '发表评论'"
      width="500px"
    >
      <el-input
        v-model="commentContent"
        type="textarea"
        :rows="3"
        :placeholder="replyTo ? `回复 ${replyTo.userName}` : '写下你的评论...'"
      />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelComment">取 消</el-button>
          <el-button type="primary" @click="submitComment">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { View, Star, Collection, Calendar, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { marked } from 'marked'
import { articleApi, commentApi, interactionApi } from '../../apis'
import 'github-markdown-css'
import { ElInfiniteScroll } from 'element-plus'

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
  parentId: number | null
  childCount: number
  createdTime: string
  replyToUsername?: string  // 添加这个可选字段
  children?: Comment[]
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

// 获取文章详情
const getArticleDetail = async () => {
  try {
    // 获取文章详情
    const { data } = await articleApi.getArticle(articleId)
    
    // 获取点赞状态
    const likeRes = await interactionApi.getLikeStatus(articleId)
    
    // 获取收藏状态
    const collectionRes = await interactionApi.getCollectionStatus(articleId)
    
    article.value = {
      ...data,
      isLiked: likeRes.data,
      isCollected: collectionRes.data
    }
  } catch (error) {
    console.error('获取文章详情失败:', error)
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
  }
}

// 删除评论
const handleDeleteComment = async (commentId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该评论吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    
    await commentApi.deleteComment(articleId, commentId)
    
    ElMessage.success('删除成功')
    
    // 更新评论列表
    const parentComment = comments.value.find(comment => 
      comment.children?.some(reply => reply.id === commentId)
    )
    
    if (parentComment) {
      // 如果是子评论，只更新父评论的子评论列表
      parentComment.children = parentComment.children?.filter(reply => reply.id !== commentId)
      parentComment.childCount--
    } else {
      // 如果是父评论，从列表中移除
      comments.value = comments.value.filter(comment => comment.id !== commentId)
    }
    
    // 更新文章的评论总数
    article.value.commentCount--
  } catch (error: any) {
    if (error?.message !== 'cancel') {
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

// Markdown 转 HTML
const markdownToHtml = (content: string) => {
  return marked(content || '')
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

// 加载子评论
const loadReplies = async (comment: Comment) => {
  if (comment.childCount === 0) return

  try {
    // 获取子评论
    const { data } = await commentApi.getComments(articleId, {
      page: 1,
      size: 999, // 获取全部子评论
      parentId: comment.id
    })

    // 更新评论的子评论列表
    const index = comments.value.findIndex(c => c.id === comment.id)
    if (index !== -1) {
      comments.value[index].children = data.records
    }
  } catch (error) {
    console.error('获取子评论失败:', error)
  }
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

onMounted(() => {
  getArticleDetail()
  getComments()
})
</script>

<style scoped>
.article-detail {
  padding: 32px;
  max-width: 900px;
  margin: 0 auto;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
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

.article-cover {
  margin: 24px 0 32px;
  text-align: center;
  width: 100%;
  aspect-ratio: 16/9;
  overflow: hidden;
  border-radius: 12px;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.article-content {
  font-size: 16px;
  line-height: 1.8;
  color: #2c3e50;
  margin-bottom: 48px;
}

.article-comments {
  position: relative; /* 为 loading 添加定位上下文 */
  margin-top: 48px;
  padding-top: 32px;
  border-top: 1px solid #eee;
}

.comments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.comments-header h3 {
  font-size: 1.5em;
  color: #2c3e50;
  margin: 0;
}

.comment-list {
  margin-top: 24px;
  min-height: 200px; /* 确保有足够空间显示空状态 */
}

.comment-item {
  margin-bottom: 24px;
}

.comment-main {
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.comment-main:hover {
  background-color: #f5f7fa;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.username {
  font-weight: 600;
  color: #2c3e50;
}

.time {
  color: #909399;
  font-size: 12px;
}

.comment-content {
  color: #2c3e50;
  line-height: 1.6;
  margin: 12px 0;
}

.comment-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.comment-replies {
  margin-left: 48px;
  margin-top: 16px;
}

.reply-item {
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 12px;
}

.load-more {
  text-align: center;
  margin-top: 16px;
}

.load-more-status {
  text-align: center;
  padding: 16px 0;
  color: #909399;
  font-size: 14px;
}

/* 适配暗色主题 */
@media (prefers-color-scheme: dark) {
  .article-detail {
    background: #1a1a1a;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
  }

  .article-title {
    color: #e5e5e5;
  }

  .comment-main,
  .reply-item {
    background-color: #2a2a2a;
  }

  .comment-main:hover {
    background-color: #333;
  }

  .meta-item:hover {
    background-color: #2a2a2a;
  }

  .load-more-status {
    color: #909399;
  }
}
</style> 