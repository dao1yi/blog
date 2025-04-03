import request from '@/utils/request'
import type { Article, ArticleHotVO } from '@/types/article'

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

interface ArticleListResponse {
  total: number
  list: Article[]
}

interface ArticleListParams {
  page: number
  pageSize: number
  keyword?: string
}

export interface ArticleQueryDTO {
  page?: number
  size?: number
  keyword?: string
  status?: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 获取文章列表
export function getArticles(params: ArticleListParams) {
  return request<ApiResponse<ArticleListResponse>>({
    url: '/articles',
    method: 'GET',
    params
  })
}

// 获取热门文章
export function getHotArticles(type: 'view' | 'like', period: 'day' | 'week' | 'month', limit: number = 5) {
  return request<ArticleHotVO[]>({
    url: '/articles/hot',
    method: 'GET',
    params: {
      type,
      period,
      limit
    }
  })
}

// 获取文章详情
export function getArticleDetail(id: number) {
  return request<ApiResponse<Article>>({
    url: `/articles/${id}`,
    method: 'GET'
  })
}

// 记录文章浏览
export function recordArticleView(id: number) {
  return request<ApiResponse<void>>({
    url: `/articles/${id}/view`,
    method: 'POST'
  })
}

// 获取文章点赞状态
export function getArticleLikeStatus(id: number) {
  return request<ApiResponse<boolean>>({
    url: `/articles/${id}/like/status`,
    method: 'GET'
  })
}

// 点赞/取消点赞文章
export function toggleArticleLike(id: number) {
  return request<ApiResponse<void>>({
    url: `/articles/${id}/like`,
    method: 'POST'
  })
}

// 获取文章列表
export function getArticleList(params: ArticleQueryDTO) {
  return request<PageResult<Article>>({
    url: '/articles',
    method: 'GET',
    params
  })
}

// 获取用户点赞的文章列表
export function getLikedArticles(params: { page: number; size: number }) {
  return request<PageResult<Article>>({
    url: '/articles/users/likes',
    method: 'GET',
    params
  })
}

// 获取用户收藏的文章列表
export function getCollectedArticles(params: { page: number; size: number }) {
  return request<PageResult<Article>>({
    url: '/articles/users/collections',
    method: 'GET',
    params
  })
}

// 获取收藏状态
export function getCollectionStatus(id: number) {
  return request<boolean>({
    url: `/articles/${id}/collection/status`,
    method: 'GET'
  })
}

// 切换收藏状态
export function toggleCollection(id: number) {
  return request<void>({
    url: `/articles/${id}/collection`,
    method: 'POST'
  })
} 