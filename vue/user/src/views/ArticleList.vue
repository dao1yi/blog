<template>
  <div class="container">
    <!-- 顶部搜索栏 -->
    <div class="top-bar">
      <div class="search-wrapper">
        <input 
          v-model="searchKeyword" 
          type="text" 
          placeholder="🔍 请输入文章标题" 
          class="search-input"
          @keyup.enter="handleSearch"
        />
        <button class="search-btn" @click="handleSearch">
          搜索
        </button>
      </div>
    </div>

    <!-- 文章列表 -->
    <div v-if="loading && !articles.length" class="loading">加载中...</div>
    <template v-else>
      <div v-if="articles.length" class="article-list" ref="articleListRef" @scroll="handleScroll">
        <div 
          v-for="article in articles" 
          :key="article.id" 
          class="article-card"
          :class="{ 'no-image': !article.coverImage }"
          @click="$router.push(`/article/${article.id}`)"
        >
          <img 
            v-if="article.coverImage"
            :src="article.coverImage" 
            class="article-image" 
            alt="文章封面"
          />
          <div class="article-info">
            <h3 class="article-title">{{ article.title }}</h3>
            <p class="article-summary">{{ article.summary || '暂无简介' }}</p>
            <div class="article-stats">
              <span>👀 {{ article.viewCount }}</span>
              <span>❤️ {{ article.likeCount }}</span>
              <span>💬 {{ article.commentCount }}</span>
              <span>📌 {{ article.collectionCount }}</span>
              <span class="article-time">{{ formatDate(article.createdTime) }}</span>
            </div>
          </div>
        </div>
        
        <!-- 加载更多提示 -->
        <div v-if="hasMore" class="load-more" :class="{ loading: loadingMore }">
          {{ loadingMore ? '加载中...' : '下滑加载更多' }}
        </div>
        <div v-else class="no-more">没有更多文章了</div>
      </div>
      <div v-else class="empty-text">暂无文章</div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getArticleList, type Article } from '@/api/article'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const loadingMore = ref(false)
const articles = ref<Article[]>([])
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const articleListRef = ref<HTMLElement | null>(null)
const hasMore = computed(() => articles.value.length < total.value)

// 获取文章列表
const fetchArticles = async (loadMore = false) => {
  if (loadMore && !hasMore.value) return
  
  try {
    if (loadMore) {
      loadingMore.value = true
    } else {
      loading.value = true
      articles.value = []
      currentPage.value = 1
    }
    
    const data = await getArticleList({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value,
      status: 1 // 只获取已发布的文章
    })
    
    if (loadMore) {
      articles.value.push(...data.records)
    } else {
      articles.value = data.records
    }
    total.value = data.total
  } catch (error) {
    console.error('获取文章列表失败:', error)
    // ElMessage.error('获取文章列表失败')
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  fetchArticles()
}

// 处理滚动加载
const handleScroll = async (e: Event) => {
  const target = e.target as HTMLElement
  const scrollBottom = target.scrollHeight - target.scrollTop - target.clientHeight
  
  // 距离底部50px时加载更多
  if (scrollBottom < 50 && !loadingMore.value && hasMore.value) {
    currentPage.value++
    await fetchArticles(true)
  }
}

// 格式化日期
const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  fetchArticles()
})
</script>

<style scoped>
.container {
  max-width: 900px;
  margin: 40px auto;
  padding: 20px;
  font-family: "Arial", sans-serif;
}

.top-bar {
  display: flex;
  align-items: center;
  background: white;
  padding: 10px 20px;
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow);
  margin-bottom: 20px;
  transition: var(--transition-normal);
}

.top-bar:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.search-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.search-input {
  flex: 1;
  padding: 12px 20px;
  border: 1px solid var(--border-color);
  border-radius: 30px;
  background: #f8f9fa;
  outline: none;
  font-size: 14px;
  transition: all 0.3s;
}

.search-input:focus {
  border-color: #42b983;
  background: white;
  box-shadow: 0 0 0 3px rgba(66, 185, 131, 0.1);
}

.search-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #42b983, #2e8b57);
  color: white;
  border: none;
  border-radius: 30px;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  transition: transform 0.2s;
}

.search-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  max-height: calc(100vh - 120px);
  overflow-y: auto;
  padding-right: 10px;
}

.article-card {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #f0f0f0;
}

.article-card.no-image {
  padding: 15px 20px;
}

.article-card.no-image .article-info {
  margin-left: 0;
}

.article-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #e0e0e0;
}

.article-image {
  width: 200px;
  height: 120px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.article-info {
  flex: 1;
  min-width: 0;
}

.article-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-summary {
  font-size: 14px;
  color: #666;
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}

.article-stats {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
}

.article-stats span {
  font-size: 13px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-time {
  color: #999;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #666;
  font-size: 14px;
}

.empty-text {
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 14px;
}

.load-more {
  text-align: center;
  padding: 15px 0;
  color: #666;
  font-size: 14px;
}

.load-more.loading {
  color: #999;
}

.no-more {
  text-align: center;
  padding: 15px 0;
  color: #999;
  font-size: 14px;
}

/* 自定义滚动条样式 */
.article-list::-webkit-scrollbar {
  width: 6px;
}

.article-list::-webkit-scrollbar-thumb {
  background-color: #ddd;
  border-radius: 3px;
}

.article-list::-webkit-scrollbar-track {
  background-color: #f5f5f5;
}

@media (max-width: 768px) {
  .container {
    padding: 15px;
    margin: 20px auto;
  }

  .top-bar {
    padding: 10px 15px;
  }

  .search-wrapper {
    gap: 8px;
  }

  .search-input {
    padding: 10px 15px;
    font-size: 14px;
  }

  .search-btn {
    padding: 10px 20px;
    font-size: 14px;
  }

  .article-card {
    flex-direction: column;
    padding: 15px;
  }

  .article-image {
    width: 100%;
    height: 160px;
  }

  .article-stats {
    gap: 10px;
  }

  .article-card.no-image {
    padding: 15px;
  }
}
</style>
