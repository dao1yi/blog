<template>
  <div class="article-list">
    <div class="header">
      <h3 class="page-title">文章列表</h3>
      <el-tag type="info" class="article-count">共 {{ articles.total }} 个文章</el-tag>
      <el-button type="primary" class="create-button" @click="$router.push('/articles/create')">
        <el-icon><Plus /></el-icon>新建文章
      </el-button>
    </div>

    <div class="search-area">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item>
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索文章标题"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-select 
            v-model="queryParams.status" 
            placeholder="文章状态"
            clearable
            @change="handleSearch"
          >
            <el-option label="全部" :value="undefined" />
            <el-option label="已发布" :value="1" />
            <el-option label="草稿" :value="0" />
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
        :data="articles.records" 
        style="width: 100%"
        :header-cell-style="{
          background: '#f5f7fa',
          color: '#606266'
        }"
        border
        :cell-style="{
          padding: '16px'
        }"
      >
        <el-table-column label="封面" width="250">
          <template #default="{ row }">
            <el-image 
              v-if="row.coverImage"
              :src="row.coverImage"
              :preview-src-list="[row.coverImage]"
              fit="contain"
              class="cover-preview"
              @click="$router.push(`/articles/${row.id}`)"
              style="cursor: pointer"
            />
            <span v-else>无封面</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="300">
          <template #default="{ row }">
            <div class="article-info">
              <div class="title-wrapper">
                <span 
                  class="article-title"
                  @click="$router.push(`/articles/${row.id}`)"
                >
                  {{ row.title }}
                </span>
                
              </div>
              <div class="article-stats">
                <span class="stat-item">
                  <el-icon><View /></el-icon>
                  {{ row.viewCount || 0 }}
                </span>
                <el-divider direction="vertical" />
                <span class="stat-item">
                  <el-icon><Star /></el-icon>
                  {{ row.likeCount || 0 }}
                </span>
                <el-divider direction="vertical" />
                <span class="stat-item">
                  <el-icon><Collection /></el-icon>
                  {{ row.collectionCount || 0 }}
                </span>
                <el-divider direction="vertical" />
                <span class="stat-item">
                  <el-icon><ChatLineRound /></el-icon>
                  {{ row.commentCount || 0 }}
                </span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="summary" label="摘要" width="400">
          <template #default="{ row }">
            <el-tooltip
              v-if="row.summary"
              :content="row.summary"
              placement="top-start"
              :show-after="500"
            >
              <p class="article-summary">{{ row.summary }}</p>
            </el-tooltip>
            <span v-else class="no-summary">暂无摘要</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="300">
          <template #default="{ row }">
            
            <div class="status-tags">
              <div class="article-tags">
                  <el-tag v-if="row.isTop" type="danger" effect="dark" size="small">置顶</el-tag>
                  <el-tag v-if="row.isBroadcast" type="warning" effect="plain" size="small">广播</el-tag>
              </div>
              <el-tag :type="row.status === 1 ? 'success' : ''">
                {{ row.status === 1 ? '已发布' : '草稿' }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="255">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/articles/${row.id}/edit`)">
              编辑
            </el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="articles.total"
          @current-change="getArticles"
          background
          layout="total, prev, pager, next"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import { Search, Plus, View, Star, Collection, ChatLineRound } from '@element-plus/icons-vue'
import axios from '../../axios'

interface Article {
  id: number
  title: string
  status: number
  createdTime: string
  coverImage?: string
  summary?: string
  isTop: number
  isBroadcast: number
  viewCount: number
  likeCount: number
  collectionCount: number
  commentCount: number
}

interface ArticleList {
  records: Article[]
  total: number
  size: number
  current: number
}

const articles = ref<ArticleList>({
  records: [],
  total: 0,
  size: 10,
  current: 1
})

const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  status: undefined as number | undefined
})

const getArticles = async () => {
  try {
    const { data } = await axios.get('/articles', { params: queryParams })
    articles.value = data
  } catch (error) {
    console.error('获取文章列表失败:', error)
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇文章吗？', '提示', {
      type: 'warning'
    })
    await axios.delete(`/articles/${id}`)
    getArticles()
  } catch (error) {
    console.error('删除文章失败:', error)
  }
}

// 格式化日期时间
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

// 搜索处理
const handleSearch = () => {
  queryParams.page = 1 // 重置页码
  getArticles()
}

// 重置搜索
const handleReset = () => {
  queryParams.keyword = ''
  queryParams.status = undefined
  queryParams.page = 1
  getArticles()
}

onMounted(() => {
  getArticles()
})
</script>

<style scoped>
.article-list {
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

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2f3d;
  background: linear-gradient(120deg, var(--primary-color), #1d39c4);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.article-count {
  font-weight: normal;
  font-size: 14px;
  border-radius: 6px;
  padding: 0 12px;
  height: 28px;
  line-height: 28px;
  background: #f4f4f5;
  border: none;
}

.create-button {
  padding: 10px 20px;
  border-radius: 8px;
  transition: all 0.3s ease;
  background: linear-gradient(120deg, var(--primary-color), #1d39c4);
  border: none;
}

.create-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
}

.create-button :deep(.el-icon) {
  margin-right: 8px;
}

.search-area {
  margin-bottom: 24px;
  padding: 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 16px;
}

.table-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.cover-preview {
  width: 160px;
  height: 90px;
  border-radius: 8px;
  object-fit: cover;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.cover-preview:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.status-tags {
  display: flex;
  align-items: center;
  gap: 6px;
  justify-content: center;
  flex-wrap: wrap;
}

:deep(.el-tag--small) {
  height: 20px;
  padding: 0 6px;
  font-size: 12px;
  margin: 2px 0;
}

.article-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.title-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.article-title {
  color: var(--el-color-primary);
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  
  &:hover {
    opacity: 0.8;
  }
}

.article-tags {
  display: flex;
  gap: 6px;
}

.article-stats {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #666;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

:deep(.el-divider--vertical) {
  margin: 0 8px;
  height: 12px;
}

.article-summary {
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  text-align: left;
  padding: 0 8px;
}

.no-summary {
  color: #909399;
  font-style: italic;
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
  text-align: center;
}

:deep(.el-table td) {
  padding: 16px;
  text-align: center;
}

:deep(.el-table--border) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-button.is-link) {
  font-size: 14px;
  padding: 4px 8px;
  transition: all 0.3s ease;
}

:deep(.el-button.is-link:hover) {
  transform: translateX(2px);
}

:deep(.el-tag) {
  border-radius: 4px;
  padding: 0 12px;
  height: 24px;
  line-height: 24px;
  font-weight: 500;
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
  .article-list {
    background-color: #141414;
  }

  .page-title {
    color: #e5e5e5;
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

  .article-summary {
    color: #a6a6a6;
  }

  .no-summary {
    color: #808080;
  }

  .pagination {
    background-color: #1f1f1f;
    border-top-color: #303030;
  }

  .article-stats {
    color: #999;
  }
  
  :deep(.el-divider--vertical) {
    border-color: #666;
  }
}
</style> 