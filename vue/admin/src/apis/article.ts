import axios from '../axios'

export const articleApi = {
  // 获取文章列表
  getArticles(params: {
    page: number
    size: number
    keyword?: string
    status?: number
  }) {
    return axios.get('/articles', { params })
  },

  // 获取文章详情
  getArticle(id: number) {
    return axios.get(`/articles/${id}`)
  },

  // 创建文章
  createArticle(data: {
    title: string
    content: string
    summary?: string
    coverImage?: string
    status: number
  }) {
    return axios.post('/articles', data)
  },

  // 更新文章
  updateArticle(id: number, data: {
    title: string
    content: string
    summary?: string
    coverImage?: string
    status: number
  }) {
    return axios.put(`/articles/${id}`, data)
  },

  // 删除文章
  deleteArticle(id: number) {
    return axios.delete(`/articles/${id}`)
  },

  // 获取热门文章
  getHotArticles(params: {
    type: string      // 类型：view-浏览量, like-点赞数, comment-评论数, collection-收藏数
    period: string    // 时间段：day-今日, week-本周, month-本月
    limit?: number    // 获取数量，默认10
  }) {
    return axios.get('/articles/hot', { params })
  },

  // 修改评论置顶状态
  toggleCommentTop: (articleId: number, commentId: number) => {
    return axios.put(`/articles/${articleId}/comments/${commentId}`)
  }
} 