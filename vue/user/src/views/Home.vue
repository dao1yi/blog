<template>
  <div class="main-container">
    <!-- 顶部导航部分 -->
    <div class="header-section">
      <div class="article-home" @click="$router.push('/articles')">进入文章主页</div>
      <div class="arrow-icon">➔</div>
    </div>

    <!-- 主体内容部分 -->
    <div class="content-container">
      <!-- 左侧个人信息部分 -->
      <div class="left-section card">
        <div v-if="loading.resume" class="loading">加载中...</div>
        <template v-else>
          <div class="personal-info">
            <div class="left-info">
              <img :src="resumeData?.avatar || 'https://via.placeholder.com/100x100'" alt="头像" class="avatar" />
              <div class="basic-info">
                <div class="name">姓名：{{ resumeData?.name || '未设置' }}</div>
                <div class="email">电子邮箱：{{ resumeData?.email || '未设置' }}</div>
              </div>
            </div>
            <div class="right-info">
              <div class="introduction">{{ resumeData?.introduction || '这个人很懒，还没有写简介' }}</div>
            </div>
          </div>

          <div class="honor-certificates" v-if="resumeData?.honors?.length">
            <div class="section-title">🏆 荣誉证书</div>
            <div class="tag-container">
              <span v-for="(honor, index) in resumeData.honors" :key="index">{{ honor }}</span>
            </div>
          </div>
          <div v-else class="empty-text">暂无荣誉证书</div>

          <div class="divider"></div>

          <div class="skills" v-if="resumeData?.skills?.length">
            <div class="section-title">🚀 技能标签</div>
            <div class="tag-container">
              <span v-for="(skill, index) in resumeData.skills" :key="index">{{ skill }}</span>
            </div>
          </div>
          <div v-else class="empty-text">暂无技能标签</div>

          <div class="button-group">
            <a :href="resumeData?.resumeUrl" target="_blank" class="resume-btn" :class="{ disabled: !resumeData?.resumeUrl }">查看简历</a>
            <a :href="resumeData?.projectUrl" target="_blank" class="project-btn" :class="{ disabled: !resumeData?.projectUrl }">查看项目</a>
          </div>
        </template>
      </div>

      <!-- 右侧文章列表部分 -->
      <div class="right-section card">
        <div v-if="loading.articles" class="loading">加载中...</div>
        <template v-else>
          <div class="stats">
            <span class="stat-item" :class="{ active: sortType === 'view' }" @click="changeSort('view')">
              👁️ 按浏览量排序
            </span>
            <span class="stat-item" :class="{ active: sortType === 'like' }" @click="changeSort('like')">
              👍 按点赞量排序
            </span>
          </div>

          <div v-if="hotArticles.length" class="article-list">
            <div 
              v-for="article in hotArticles" 
              :key="article.id" 
              class="article-list-item"
              :class="{ 'no-image': !article.coverImage }"
              @click="$router.push(`/article/${article.id}`)"
            >
              <img 
                v-if="article.coverImage"
                :src="article.coverImage" 
                alt="文章图标" 
                class="article-icon" 
              />
              <div class="article-info">
                <div class="article-title">{{ article.title }}</div>
                <div class="article-stats">
                  <span>👀 {{ article.viewCount }} 浏览</span>
                  <span>❤️ {{ article.likeCount }} 点赞</span>
                  <span>📌 {{ article.collectionCount }} 收藏</span>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="empty-text">暂无热门文章</div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getResume, type Resume } from '@/api/resume'
import { getHotArticles, type ArticleHotVO } from '@/api/article'

const router = useRouter()
const resumeData = ref<Resume | null>(null)
const hotArticles = ref<ArticleHotVO[]>([])
const sortType = ref<'view' | 'like'>('view')
const loading = ref({
  resume: true,
  articles: true
})

// 获取简历信息
const fetchResume = async () => {
  try {
    loading.value.resume = true
    const data = await getResume()
    resumeData.value = data
  } catch (error) {
    console.error('获取简历信息失败:', error)
  } finally {
    loading.value.resume = false
  }
}

// 获取热门文章
const fetchHotArticles = async () => {
  try {
    loading.value.articles = true
    const data = await getHotArticles(
      sortType.value,  // 'view' 或 'like'
      'week',         // 时间范围：'day', 'week', 'month'
      5              // 获取5条热门文章
    )
    hotArticles.value = data
  } catch (error) {
    console.error('获取热门文章失败:', error)
  } finally {
    loading.value.articles = false
  }
}

// 切换排序方式
const changeSort = async (type: 'view' | 'like') => {
  sortType.value = type
  await fetchHotArticles()
}

onMounted(() => {
  fetchResume()
  fetchHotArticles()
})
</script>

<style scoped>
/* 页面基础样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

body {
  background: linear-gradient(135deg, #f0f4ff, #e6f7ff);
}

.main-container {
  max-width: 1100px;
  margin: 40px auto;
  padding: 20px;
}

/* 顶部导航 */
.header-section {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30px;
  color: #333;
}

.article-home {
  padding: 15px 40px;
  background: linear-gradient(135deg, #42b983, #2e8b57);
  color: white;
  border-radius: 30px;
  font-size: 20px;
  font-weight: bold;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.2s;
}

.article-home:hover {
  transform: scale(1.05);
}

.arrow-icon {
  font-size: 28px;
  margin: 0 15px;
  color: #6c757d;
}

/* 内容容器 */
.content-container {
  display: flex;
  gap: 30px;
}

/* 卡片样式 */
.card {
  background-color: white;
  padding: 30px;
  border-radius: 16px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
}

/* 左侧个人信息 */
.left-section {
  flex: 1.5;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.personal-info {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.left-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.right-info {
  flex: 1;
  min-width: 0;
}

.basic-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 3px solid #42b983;
}

.name, .email {
  font-size: 14px;
  color: #333;
  text-align: center;
}

.introduction {
  margin-top: 0;
  padding: 15px;
  background: rgba(66, 185, 131, 0.05);
  border-radius: 8px;
  color: #666;
  font-size: 14px;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
  max-height: 150px;
  overflow-y: auto;
}

/* 荣誉和技能 */
.honor-certificates,
.skills {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.tag-container {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.tag-container span {
  padding: 8px 16px;
  background: rgba(66, 185, 131, 0.1);
  border-radius: 6px;
  color: #2e8b57;
  font-size: 16px;
}

/* 按钮组 */
.button-group {
  display: flex;
  gap: 15px;
}

.resume-btn,
.project-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #42b983, #2e8b57);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  transition: transform 0.2s, background 0.3s;
}

.resume-btn:hover,
.project-btn:hover {
  background: linear-gradient(135deg, #2e8b57, #1f6f43);
  transform: scale(1.05);
}

/* 右侧文章列表 */
.right-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stats {
  display: flex;
  gap: 15px;
}

.stat-item {
  padding: 10px 18px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 6px;
  font-size: 16px;
  color: #495057;
  cursor: pointer;
  transition: background-color 0.3s;
}

.stat-item.active {
  background: rgba(66, 185, 131, 0.1);
  color: #42b983;
}

.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  pointer-events: none;
}

/* 文章列表 */
.article-list {
  padding: 15px;
  background: white;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  transition: transform 0.2s;
}

.article-list-item {
  padding: 12px;
  background: white;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.2s;
  cursor: pointer;
  border: 1px solid #f0f0f0;
}

.article-list-item.no-image {
  padding: 12px 15px;
}

.article-list-item.no-image .article-info {
  margin-left: 0;
}

.article-list-item:hover {
  transform: scale(1.03);
}

.article-icon {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 4px;
  flex-shrink: 0;
}

.article-info {
  flex: 1;
  min-width: 0;
}

.article-title {
  font-size: 15px;
  color: #2c3e50;
  font-weight: 500;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.article-stats span {
  font-size: 13px;
  color: #6c757d;
}

/* 新增样式 */
.loading {
  text-align: center;
  padding: 20px;
  color: #666;
}

.empty-text {
  text-align: center;
  color: #999;
  padding: 10px;
}

/* 响应式 */
@media (max-width: 768px) {
  .content-container {
    flex-direction: column;
  }
  
  .personal-info {
    flex-direction: column;
  }
  
  .right-info {
    width: 100%;
  }
}

.divider {
  margin: 20px 0;
  height: 1px;
  background: linear-gradient(to right, transparent, #e0e0e0, transparent);
}

.section-title {
  font-size: 18px;
  font-weight: 500;
  color: #42b983;
  margin-bottom: 15px;
}
</style>
