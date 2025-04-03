<template>
  <div class="page-container">
    <!-- 左侧目录区域 -->
    <div class="toc-area" :class="{ 'show': showToc }">
      <h3 class="toc-title">目录</h3>
      <div class="toc-list">
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
      </div>
    </div>

    <!-- 中间文章内容区 -->
    <div class="content-area">
      <div v-if="loading" class="loading-state">加载中...</div>
      <div v-else class="article-content">
        <!-- 目录按钮 (移动端) -->
        <div class="toc-toggle" @click="showToc = !showToc">
          <i class="el-icon-list"></i>
          {{ showToc ? '隐藏目录' : '显示目录' }}
        </div>
        
        <!-- 文章头部 -->
        <div class="article-header">
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
        </div>
        
        <!-- 文章内容 -->
        <div class="article-scroll-container" ref="articleContainer">
          <div class="article-text markdown-body" v-html="htmlContent" ref="articleContent"></div>
        </div>
      </div>
    </div>
    
    <!-- 右侧评论区 -->
    <div class="comment-area">
      <div class="comment-header">
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
      </div>
      
      <!-- 评论列表 -->
      <div class="comment-scroll-container">
        <div v-if="loadingComments && !comments.length" class="loading-state">加载评论中...</div>
        <template v-else>
          <div v-if="comments.length === 0" class="empty-state">
            暂无评论，来说两句吧~
          </div>
          <template v-else>
            <div v-for="comment in sortedComments" :key="comment.id" class="comment-item">
              <div class="comment-user">
                <el-avatar :size="32" :src="comment.userAvatar">{{ comment.userName?.charAt(0) }}</el-avatar>
                <span class="username">{{ comment.userName }}</span>
                <span v-if="comment.isTop" class="top-tag">置顶</span>
                <span class="reply-btn" @click="replyTo(comment)">回复</span>
              </div>
              <div class="comment-body">
                <div class="comment-text">{{ comment.content }}</div>
                <div class="comment-footer">
                  <span class="time">{{ formatDate(comment.createdTime) }}</span>
                 
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
import { ref, onMounted, computed, nextTick } from 'vue'
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

// 目录相关
const articleContainer = ref<HTMLElement | null>(null)
const articleContent = ref<HTMLElement | null>(null)
const tocItems = ref<TocItem[]>([])
const currentHeading = ref('')
const showToc = ref(false)

// 添加 TocItem 接口定义
interface TocItem {
  id: string
  text: string
  level: number
}

// 修改复制功能
const copyCode = (code: string) => {
  // 创建临时文本区域
  const textArea = document.createElement('textarea')
  textArea.value = code
  document.body.appendChild(textArea)
  
  try {
    // 尝试使用 navigator.clipboard API
    if (navigator.clipboard && window.isSecureContext) {
      navigator.clipboard.writeText(code).then(() => {
        ElMessage.success('复制成功')
      }).catch(() => {
        // 如果 clipboard API 失败，使用传统方法
        fallbackCopyToClipboard(textArea)
      })
    } else {
      // 如果不支持 clipboard API，使用传统方法
      fallbackCopyToClipboard(textArea)
    }
  } finally {
    // 清理临时文本区域
    document.body.removeChild(textArea)
  }
}

// 传统的复制方法
const fallbackCopyToClipboard = (textArea: HTMLTextAreaElement) => {
  try {
    textArea.select()
    document.execCommand('copy')
    ElMessage.success('复制成功')
  } catch (err) {
    ElMessage.error('复制失败，请手动复制')
  }
}

// 修改 htmlContent 计算属性
const htmlContent = computed(() => {
  if (!article.value?.content) return ''
  
  // 使用 marked 转换 Markdown
  const html = marked(article.value.content)
  
  // 等待 DOM 更新后处理代码块
  nextTick(() => {
    generateToc()
    addCopyButtons()
  })
  
  return html
})

// 解析文章内容生成目录
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

  // 检查文本是否被截断并添加类名
  await nextTick()
  const tocItemElements = document.querySelectorAll('.toc-item')
  tocItemElements.forEach(element => {
    if (element.scrollWidth > element.clientWidth) {
      element.classList.add('truncated')
    }
  })
}

// 修改滚动到指定标题的函数
const scrollToHeading = (id: string) => {
  const element = document.getElementById(id)
  if (element && articleContainer.value) {
    // 计算顶部偏移量，包括文章头部的高度
    const headerHeight = 180 // 文章头部高度（标题+作者信息等）
    const scrollOffset = 24 // 额外的间距
    
    articleContainer.value.scrollTo({
      top: element.offsetTop - headerHeight - scrollOffset,
      behavior: 'smooth'
    })
    currentHeading.value = id
  }
}

// 修改监听滚动更新当前标题的函数
const handleScroll = () => {
  if (!articleContainer.value || !tocItems.value.length) return
  
  const scrollTop = articleContainer.value.scrollTop
  const headerHeight = 180 // 与上面相同的头部高度
  const scrollOffset = 24 // 与上面相同的间距
  
  for (let i = headings.length - 1; i >= 0; i--) {
    const heading = headings[i]
    if (heading && heading.offsetTop - headerHeight - scrollOffset <= scrollTop) {
      currentHeading.value = heading.id
      break
    }
  }
}

// 获取文章详情
const fetchArticleDetail = async () => {
  try {
    loading.value = true
    const articleData = await getArticleDetail(articleId.value)
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
    
    // 等待 DOM 更新后生成目录
    await nextTick()
    await generateToc()
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

// 修改添加复制按钮的函数
const addCopyButtons = () => {
  if (!articleContent.value) return
  
  const codeBlocks = articleContent.value.querySelectorAll('pre code')
  codeBlocks.forEach(codeBlock => {
    // 检查是否已经添加过按钮
    if (codeBlock.parentElement?.querySelector('.copy-btn')) return
    
    const copyButton = document.createElement('button')
    copyButton.className = 'copy-btn'
    copyButton.innerHTML = '复制'
    copyButton.onclick = (e) => {
      e.stopPropagation()
      copyCode(codeBlock.textContent || '')
    }
    
    // 将按钮添加到 pre 标签中
    codeBlock.parentElement?.classList.add('code-block-wrapper')
    codeBlock.parentElement?.appendChild(copyButton)
  })
}

// 对评论进行排序，置顶的在前面
const sortedComments = computed(() => {
  return [...comments.value].sort((a, b) => {
    // 先按置顶排序
    if (a.isTop !== b.isTop) {
      return b.isTop - a.isTop
    }
    // 再按时间倒序
    return new Date(b.createdTime).getTime() - new Date(a.createdTime).getTime()
  })
})

onMounted(() => {
  if (!articleId.value) {
    router.push('/articles')
    return
  }
  
  fetchUserInfo()
  fetchArticleDetail()
  fetchComments()
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
  height: calc(100vh - var(--header-height) - 40px);
  overflow: hidden;
  position: fixed;
  top: var(--header-height);
  left: 50%;
  transform: translateX(-50%);
}

.content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--card-background);
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow);
  overflow: hidden;
}

.article-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.article-header {
  padding: 24px 24px 0;
  flex-shrink: 0;
}

.article-scroll-container {
  flex: 1;
  overflow-y: auto;
  padding: 0 24px 24px;
}

.article-text {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  padding-bottom: 24px;
}

.comment-area {
  width: 320px;
  display: flex;
  flex-direction: column;
  background: var(--card-background);
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow);
  overflow: hidden;
}

.comment-header {
  padding: 24px 24px 0;
  flex-shrink: 0;
}

.comment-scroll-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
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

.comment-title {
  font-size: 18px;
  color: #333;
  margin-bottom: 20px;
  font-weight: 500;
}

.comment-item {
  margin-bottom: 20px;
}

.comment-user {
  display: flex;
  align-items: center;
  gap: 8px;
}

.top-tag {
  padding: 2px 6px;
  background: linear-gradient(135deg, #409EFF, #1677ff);
  color: white;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  margin-left: 8px;
}

.username {
  font-weight: 500;
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
    padding: 10px;
    gap: 10px;
  }

  .content-area {
    height: 50vh;
  }

  .article-content {
    height: 100%;
  }

  .comment-area {
    width: 100%;
    height: calc(50vh - 20px);
  }

  .article-scroll-container,
  .comment-scroll-container {
    max-height: none;
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
.article-scroll-container::-webkit-scrollbar,
.comment-scroll-container::-webkit-scrollbar {
  width: 6px;
}

.article-scroll-container::-webkit-scrollbar-thumb,
.comment-scroll-container::-webkit-scrollbar-thumb {
  background-color: #ddd;
  border-radius: 3px;
}

.article-scroll-container::-webkit-scrollbar-track,
.comment-scroll-container::-webkit-scrollbar-track {
  background-color: #f5f5f5;
}

:deep(.markdown-body) {
  font-size: 16px;
  line-height: 1.8;
  
  h1, h2, h3, h4, h5, h6 {
    margin-top: 48px; /* 增加标题的上边距 */
    margin-bottom: 16px;
    font-weight: 600;
    line-height: 1.25;
    scroll-margin-top: 144px; /* 添加滚动边距，等于 headerHeight + scrollOffset */
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
    position: relative; /* 为复制按钮定位做准备 */
    padding: 16px;
    overflow: auto;
    font-size: 85%;
    line-height: 1.45;
    background-color: #f6f8fa;
    border-radius: 6px;
    
    &.code-block-wrapper {
      padding-right: 60px; /* 为复制按钮留出空间 */
    }
    
    code {
      padding: 0;
      margin: 0;
      background-color: transparent;
      border: 0;
    }
    
    .copy-btn {
      position: absolute;
      right: 10px;
      top: 10px;
      padding: 4px 8px;
      font-size: 12px;
      color: #666;
      background: #fff;
      border: 1px solid #ddd;
      border-radius: 4px;
      cursor: pointer;
      opacity: 0;
      transition: all 0.2s;
      
      &:hover {
        color: var(--primary-color);
        border-color: var(--primary-color);
        background: rgba(64, 158, 255, 0.1);
      }
    }
    
    &:hover .copy-btn {
      opacity: 1;
    }
  }
  
  /* 第一个标题不需要这么大的上边距 */
  *:first-child {
    margin-top: 0;
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

.toc-area {
  width: 220px;
  display: flex;
  flex-direction: column;
  background: var(--card-background);
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow);
  overflow: hidden;
  height: 100%; /* 确保高度与内容区一致 */
  margin-left: 0; /* 移除之前的右边距 */
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
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  position: relative; /* 添加相对定位 */
}

.toc-item:hover {
  color: var(--primary-color);
  background: rgba(64, 158, 255, 0.1);
}

.toc-item.active {
  color: var(--primary-color);
  background: rgba(64, 158, 255, 0.1);
  border-right: 2px solid var(--primary-color);
}

.level-1 { padding-left: 20px; }
.level-2 { padding-left: 35px; }
.level-3 { padding-left: 50px; }
.level-4 { padding-left: 65px; }
.level-5 { padding-left: 80px; }
.level-6 { padding-left: 95px; }

/* 移动端样式 */
.toc-toggle {
  display: none;
  position: fixed;
  right: 20px;
  bottom: 20px;
  padding: 10px 20px;
  background: var(--primary-color);
  color: white;
  border-radius: 20px;
  cursor: pointer;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

@media (max-width: 1400px) {
  .page-container {
    max-width: 1200px;
  }
  
  .content-area {
    min-width: 600px;
  }
}

@media (min-width: 1201px) {
  .page-container {
    gap: 24px;
  }

  .toc-area {
    width: 220px;
  }

  .content-area {
    flex: 1;
    min-width: 800px;
  }

  .comment-area {
    width: 320px;
  }
}

/* 可选：添加自定义提示样式 */
.toc-item:hover::after {
  content: attr(title);
  position: absolute;
  left: 100%;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
  z-index: 1000;
  margin-left: 10px;
  display: none; /* 默认隐藏 */
}

/* 仅在文本被截断时显示提示 */
.toc-item {
  position: relative;
}

.toc-item.truncated:hover::after {
  display: block;
}

/* 添加代码块相关样式 */
:deep(.code-block-wrapper) {
  position: relative;
}

:deep(.copy-btn) {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 4px 8px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  color: #fff;
  font-size: 12px;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s, background-color 0.2s;
}

:deep(.code-block-wrapper:hover .copy-btn) {
  opacity: 1;
}

:deep(.copy-btn:hover) {
  background: rgba(255, 255, 255, 0.2);
}
</style>