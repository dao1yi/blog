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
              <div class="avatar-wrapper">
                <img v-if="resumeData?.avatar" :src="resumeData.avatar" alt="头像" class="avatar" />
                <div v-else class="avatar default-avatar">
                  {{ resumeData?.name?.charAt(0)?.toUpperCase() || '?' }}
                </div>
                
              </div>
              <div class="basic-info">
                <span style="color: blue;">{{resumeData?.status}}</span>
                <div class="name">姓名：{{ resumeData?.name }}</div>
                <div class="email">电子邮箱：{{ resumeData?.email }}</div>
              </div>
            </div>
            <div class="right-info">
              <div class="introduction">{{ resumeData?.introduction }}</div>
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
            <div class="main-buttons">
              <a :href="resumeData?.resumeUrl" target="_blank" class="resume-btn" :class="{ disabled: !resumeData?.resumeUrl }">
                查看简历
              </a>
              <a :href="resumeData?.projectUrl" target="_blank" class="project-btn" :class="{ disabled: !resumeData?.projectUrl }">
                查看项目
              </a>
            </div>
            <div class="message-btn" @click="handleMessage">
              <div class="message-icon">💬</div>
              <span class="message-text">给我留言</span>
            </div>
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
                  <span>⭐ {{ article.collectionCount }} 收藏</span>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getResume, type Resume } from '@/api/resume'
import { getHotArticles, type ArticleHotVO } from '@/api/article'
import { useUserStore } from '@/store'
import { sendMessage } from '@/api/message'

const router = useRouter()
const userStore = useUserStore()
const defaultResumeData = {
        "name": "许梦旗",
        "avatar": "",
        "email": "3324456464@qq.com",
        "introduction": "具备良好的学习能力和团队协作精神\n熟悉软件开发流程和敏捷开发方法\n对新技术和工具保持敏感，能够快速学习并应用\n参与开源项目，关注技术社区动态\n探索新的编程语言和框架\n阅读与技术和编程相关的书籍和文章",
        "jobIntention": "Java后端开发",
        "status":"求职中",
        "skills": [
            "Spring Boot",
            "Spring Cloud",
            "MySQL",
            "Redis",
            "Minio",
            "ElasticSearch",
            "MongoDB",
            "Git",
            "Linux",
            "Docker",
            "Vue"
        ],
        "honors": [
            "国家励志奖学金",
            "蓝桥杯省三",
            "CCPC省级铜奖",
            "天梯赛国三"
        ],
        "resumeUrl": "http://113.47.13.180:9000/blog/aae790ef029848a6b6543099ee902621.pdf",
        "projectUrl": "https://github.com/dao1yi/blog",
        "schoolName": "安阳师范学院",
        "major": "软件工程",
        "degree": "本科",
        "startDate": "2022-09-01T00:00:00.000+00:00",
        "graduateDate": "2026-06-01T00:00:00.000+00:00",
        "createTime": null,
        "updateTime": null
}

const resumeData = ref<Resume | null>(defaultResumeData)
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
    resumeData.value = data || defaultResumeData
  } catch (error) {
    console.error('获取简历信息失败:', error)
    resumeData.value = defaultResumeData
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

// 处理留言点击
const handleMessage = async () => {
  if (!userStore.isLoggedIn) {
    try {
      await ElMessageBox.confirm(
        '留言需要先登录，是否前往登录？',
        '提示',
        {
          confirmButtonText: '去登录',
          cancelButtonText: '取消',
          type: 'info'
        }
      )
      router.push({
        path: '/login',
        query: { redirect: '/' }
      })
    } catch {
      // 用户取消登录
    }
    return
  }

  // 已登录，显示留言弹框
  try {
    const { value: message } = await ElMessageBox.prompt(
      '请输入您的留言内容',
      '留言',
      {
        confirmButtonText: '发送',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '写下你想说的话...',
        inputValidator: (value) => {
          if (!value) {
            return '留言内容不能为空'
          }
          if (value.length > 500) {
            return '留言内容不能超过500字'
          }
          return true
        }
      }
    )

    // 发送留言到后端
    await sendMessage(message)
    ElMessage.success('留言发送成功')
  } catch (error) {
    if (error) {
      console.error('留言发送失败:', error)
      ElMessage.error('留言发送失败')
    }
    // 用户取消留言，不做处理
  }
}

onMounted(() => {
  fetchResume()
  fetchHotArticles()
})
</script>

<style scoped>
/* 修改页面基础样式 */
.main-container {
  max-width: 1100px;
  margin: 10px auto;
  padding: 10px;
  height: calc(100vh - var(--header-height) - 40px); /* 减去导航栏高度和margin */
  position: fixed;
  top: var(--header-height);
  left: 50%;
  transform: translateX(-50%);
  overflow: hidden; /* 防止整体滚动 */
}

/* 修改内容容器样式 */
.content-container {
  display: flex;
  gap: 30px;
  height: calc(100% - 80px); /* 减去header-section的高度和margin */
  overflow: hidden; /* 防止整体滚动 */
}

/* 修改左右两侧区域样式 */
.left-section,
.right-section {
  height: 100%;
  overflow-y: auto; /* 允许各自区域内部滚动 */
}

/* 修改右侧文章列表样式 */
.article-list {
  max-height: calc(100% - 60px); /* 减去stats的高度 */
  overflow-y: auto;
}

/* 美化滚动条 */
.left-section::-webkit-scrollbar,
.right-section::-webkit-scrollbar,
.article-list::-webkit-scrollbar {
  width: 6px;
}

.left-section::-webkit-scrollbar-thumb,
.right-section::-webkit-scrollbar-thumb,
.article-list::-webkit-scrollbar-thumb {
  background-color: #ddd;
  border-radius: 3px;
}

.left-section::-webkit-scrollbar-track,
.right-section::-webkit-scrollbar-track,
.article-list::-webkit-scrollbar-track {
  background-color: #f5f5f5;
}

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
  background: linear-gradient(135deg, #409EFF, #1677ff);
  color: white;
  border-radius: 30px;
  font-size: 20px;
  font-weight: bold;
  box-shadow: 0 4px 10px rgba(64, 158, 255, 0.1);
  cursor: pointer;
  transition: transform 0.2s;
}

.article-home:hover {
  transform: scale(1.05);
  background: linear-gradient(135deg, #1677ff, #0958d9);
}

.arrow-icon {
  font-size: 28px;
  margin: 0 15px;
  color: #6c757d;
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

.avatar-wrapper {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border: 3px solid #409EFF;
}

.default-avatar {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #409EFF, #1677ff);
  color: white;
  font-size: 36px;
  font-weight: bold;
  border: none;
}

.name, .email {
  font-size: 14px;
  color: #333;
  text-align: center;
}

.introduction {
  margin-top: 0;
  padding: 15px;
  background: rgba(64, 158, 255, 0.05);
  border-radius: 8px;
  color: #666;
  font-size: 14px;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
  max-height: 200px;
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
  background: rgba(64, 158, 255, 0.1);
  border-radius: 6px;
  color: #409EFF;
  font-size: 16px;
}

/* 按钮组 */
.button-group {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 15px;
}

.main-buttons {
  display: flex;
  gap: 15px;
}

.resume-btn,
.project-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #409EFF, #1677ff);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  transition: transform 0.2s, background 0.3s;
}

.resume-btn:hover,
.project-btn:hover {
  background: linear-gradient(135deg, #1677ff, #0958d9);
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
  background: rgba(64, 158, 255, 0.1);
  color: #409EFF;
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
  color: #409EFF;
  margin-bottom: 15px;
}

.content-item:hover {
  background: rgba(64, 158, 255, 0.05);
  transform: translateX(5px);
}

.introduction::-webkit-scrollbar-thumb {
  background-color: #409EFF;
  border-radius: 3px;
}

.introduction::-webkit-scrollbar-track {
  background: rgba(64, 158, 255, 0.05);
}

/* 修改留言按钮样式 */
.message-btn {
  padding: 12px 24px;
  background: rgba(64, 158, 255, 0.1);  /* 更浅的背景色 */
  color: #409EFF;  /* 主题色文字 */
  border: 1px solid rgba(64, 158, 255, 0.2);  /* 浅色边框 */
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.message-btn:hover {
  background: rgba(64, 158, 255, 0.2);
  transform: scale(1.05);
}

.message-icon {
  font-size: 18px;
  color: #409EFF;
}

.message-text {
  font-size: 16px;
  font-weight: 500;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .button-group {
    flex-direction: column;
    gap: 10px;
  }
  
  .main-buttons {
    width: 100%;
    justify-content: center;
  }
  
  .message-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
