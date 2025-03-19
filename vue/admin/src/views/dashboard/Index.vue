<template>
  <div class="dashboard">
    <div class="header">
      <h3 class="page-title">数据统计</h3>
    </div>

    <div class="dashboard-container">
      <!-- 左侧内容 -->
      <div class="left-section">
        <!-- 统计卡片 -->
        <el-row :gutter="20" class="stat-cards">
          <el-col :span="12">
            <el-card shadow="hover" class="stat-card">
              <template #header>
                <div class="card-header">
                  <span>总访问量</span>
                  <el-tag size="small">今日</el-tag>
                </div>
              </template>
              <div class="card-value">
                <span class="number">{{ statistics.totalViews }}</span>
                <el-icon class="icon"><View /></el-icon>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover" class="stat-card">
              <template #header>
                <div class="card-header">
                  <span>独立访客</span>
                  <el-tag size="small" type="success">今日</el-tag>
                </div>
              </template>
              <div class="card-value">
                <span class="number">{{ statistics.uniqueVisitors }}</span>
                <el-icon class="icon"><User /></el-icon>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 图表区域 -->
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <div class="left">
                <span class="title">访问趋势</span>
                <div class="total-stats">
                  <span class="stat-item">
                    <el-icon><View /></el-icon>
                    总访问量：{{ statistics.valueList.reduce((a, b) => a + b, 0) }}
                  </span>
                  <el-divider direction="vertical" />
                  <span class="stat-item">
                    <el-icon><User /></el-icon>
                    独立访客：{{ statistics.uniqueVisitors }}
                  </span>
                </div>
              </div>
              <el-radio-group v-model="timeRange" size="small" @change="handleTimeRangeChange">
                <el-radio-button label="week">最近7天</el-radio-button>
                <el-radio-button label="month">最近30天</el-radio-button>
                <el-radio-button label="year">最近1年</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container" v-loading="loading">
            <div ref="chartRef" class="chart"></div>
          </div>
        </el-card>
      </div>

      <!-- 右侧热门文章 -->
      <div class="right-section">
        <el-card class="hot-articles-card">
          <template #header>
            <div class="card-header">
              <span class="title">热门文章</span>
              <el-radio-group v-model="hotArticleParams.type" size="small" @change="getHotArticles">
                <el-radio-button label="view">浏览榜</el-radio-button>
                <el-radio-button label="like">点赞榜</el-radio-button>
                <!-- <el-radio-button label="collect">收藏榜</el-radio-button> -->
              </el-radio-group>
            </div>
          </template>
          <div class="hot-articles-list" v-loading="hotArticlesLoading">
            <div 
              v-for="(article, index) in hotArticles" 
              :key="article.id"
              class="hot-article-item"
              @click="router.push(`/articles/${article.id}`)"
            >
              <div class="rank" :class="{ 'top3': index < 3 }">{{ index + 1 }}</div>
              <div class="content">
                <div class="title">{{ article.title }}</div>
                <div class="info">
                  <span>
                    <el-icon><View /></el-icon>
                    {{ article.viewCount }}
                  </span>
                  <el-divider direction="vertical" />
                  <span>
                    <el-icon><Star /></el-icon>
                    {{ article.likeCount }}
                  </span>
                  <el-divider direction="vertical" />
                  <span>
                    <el-icon><Collection /></el-icon>
                    {{ article.collectionCount }}
                  </span>
                  <el-divider direction="vertical" />
                  <span>{{ article.authorName }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, reactive } from 'vue'
import { View, User, Star, Collection } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import { useRouter } from 'vue-router'
import { articleApi } from '../../apis'
import axios from '../../axios'

interface Statistics {
  dateList: string[]
  valueList: number[]
  totalViews: number
  uniqueVisitors: number
}

interface HotArticle {
  id: number
  title: string
  authorName: string
  viewCount: number    // 浏览量
  likeCount: number    // 点赞量
  collectionCount: number // 收藏量
}

const loading = ref(false)
const timeRange = ref('week')
const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null
const statistics = ref<Statistics>({
  dateList: [],
  valueList: [],
  totalViews: 0,
  uniqueVisitors: 0
})

const router = useRouter()
const hotArticlesLoading = ref(false)
const hotArticles = ref<HotArticle[]>([])
const hotArticleParams = reactive({
  type: 'view',
  period: 'day',
  limit: 10
})

// 获取统计数据
const getStatistics = async () => {
  loading.value = true
  try {
    // 获取趋势数据
    const startDate = new Date()
    if (timeRange.value === 'week') {
      startDate.setDate(startDate.getDate() - 7)
    } else if (timeRange.value === 'month') {
      startDate.setDate(startDate.getDate() - 30)
    } else if (timeRange.value === 'year') {
      startDate.setFullYear(startDate.getFullYear() - 1)
    }

    const trendRes = await axios.get('/view-records/statistics', {
      params: {
        type: timeRange.value,
        startDate: startDate.toISOString()
      }
    })

    // 获取今日数据
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    const todayRes = await axios.get('/view-records/statistics', {
      params: {
        type: 'day',
        startDate: today.toISOString()
      }
    })

    console.log(trendRes)
    // 更新统计数据
    statistics.value = {
      ...trendRes.data,
      totalViews: todayRes.data.totalViews,
      uniqueVisitors: todayRes.data.uniqueVisitors
    }

    // 确保 DOM 已更新
    await nextTick()
    updateChart()
  } catch (error) {
    console.error('获取统计数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取热门文章
const getHotArticles = async () => {
  hotArticlesLoading.value = true
  try {
    const { data } = await articleApi.getHotArticles(hotArticleParams)
    hotArticles.value = data
  } catch (error) {
    console.error('获取热门文章失败:', error)
  } finally {
    hotArticlesLoading.value = false
  }
}

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return
  
  // 如果已经存在图表实例，先销毁
  if (chart) {
    chart.dispose()
  }
  
  // 创建新的图表实例
  chart = echarts.init(chartRef.value)
  
  // 设置初始配置
  const option: EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: [],
      axisTick: {
        alignWithLabel: true
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '访问量',
        type: 'line',
        smooth: true,
        data: [],
        itemStyle: {
          color: '#409EFF'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            {
              offset: 0,
              color: 'rgba(64,158,255,0.3)'
            },
            {
              offset: 1,
              color: 'rgba(64,158,255,0.1)'
            }
          ])
        }
      }
    ]
  }
  
  chart.setOption(option)
}

// 更新图表数据
const updateChart = () => {
  if (!chart) return
  
  const option = {
    xAxis: {
      data: statistics.value.dateList,
      axisLabel: {
        rotate: timeRange.value === 'year' ? 45 : 0,
        formatter: (value) => {
          if (timeRange.value === 'year') {
            return value.substring(5) // 只显示月-日
          }
          return value
        }
      }
    },
    series: [{
      data: statistics.value.valueList
    }]
  }
  
  chart.setOption(option)
}

// 处理窗口大小变化
const handleResize = () => {
  chart?.resize()
}

// 处理时间范围变化
const handleTimeRangeChange = () => {
  getStatistics()
}

onMounted(async () => {
  // 先初始化图表
  initChart()
  // 再获取数据
  await Promise.all([
    getStatistics(),
    getHotArticles()
  ])
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>

<style scoped>
.dashboard {
  padding: 24px;
  background-color: #f6f8fa;
  min-height: calc(100vh - 64px);
}

.dashboard-container {
  display: flex;
  gap: 24px;
  margin-top: 24px;
}

.left-section {
  flex: 1;
  min-width: 0;
}

.right-section {
  width: 360px;
  flex-shrink: 0;
}

.header {
  margin-bottom: 0;
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

.stat-cards {
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
  height: 100%;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

.card-header span {
  font-size: 16px;
  font-weight: 500;
  color: #1f2f3d;
}

.card-value {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 20px;
}

.number {
  font-size: 32px;
  font-weight: 600;
  background: linear-gradient(120deg, var(--primary-color), #1d39c4);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.icon {
  font-size: 36px;
  color: var(--primary-color);
  opacity: 0.8;
  transition: all 0.3s ease;
}

.stat-card:hover .icon {
  transform: scale(1.1);
  opacity: 1;
}

.chart-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  margin-bottom: 24px;
  overflow: hidden;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

.chart-header .left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.chart-header .title {
  font-size: 16px;
  font-weight: 500;
  color: #1f2f3d;
}

.total-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-item .el-icon {
  font-size: 16px;
  color: var(--primary-color);
}

.chart-container {
  height: 400px;
  padding: 24px;
}

.chart {
  width: 100%;
  height: 100%;
  border-radius: 8px;
}

.hot-articles-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  height: 100%;
  overflow: hidden;
}

.hot-articles-list {
  padding: 16px;
}

.hot-article-item {
  display: flex;
  align-items: center;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
  margin-bottom: 8px;
}

.hot-article-item:hover {
  background: #f8f9fa;
  transform: translateX(4px);
}

.rank {
  width: 28px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  border-radius: 6px;
  background: #f4f4f5;
  color: #909399;
  font-weight: 600;
  margin-right: 16px;
  transition: all 0.3s ease;
}

.rank.top3 {
  background: linear-gradient(120deg, var(--primary-color), #1d39c4);
  color: white;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
}

.content {
  flex: 1;
  overflow: hidden;
}

.title {
  font-size: 14px;
  color: #1f2f3d;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.3s ease;
}

.hot-article-item:hover .title {
  color: var(--primary-color);
}

.info {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 8px;
}

.info span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.info .el-icon {
  font-size: 14px;
}

.info .el-icon.view {
  color: #409EFF;
}

.info .el-icon.star {
  color: #E6A23C;
}

.info .el-icon.collection {
  color: #67C23A;
}

/* 暗色主题适配 */
@media (prefers-color-scheme: dark) {
  .dashboard {
    background-color: #141414;
  }

  .page-title {
    color: #e5e5e5;
  }

  .stat-card,
  .chart-card,
  .hot-articles-card {
    background-color: #1f1f1f;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  }

  .card-header,
  .chart-header {
    border-bottom-color: #303030;
  }

  .card-header span,
  .chart-header .title {
    color: #e5e5e5;
  }

  .hot-article-item:hover {
    background-color: #262626;
  }

  .title {
    color: #e5e5e5;
  }

  .rank {
    background-color: #262626;
    color: #a6a6a6;
  }

  .info {
    color: #808080;
  }

  .chart-header {
    border-bottom-color: #303030;
  }

  .chart-header .title {
    color: #e5e5e5;
  }

  .total-stats {
    color: #a6a6a6;
  }
}
</style> 