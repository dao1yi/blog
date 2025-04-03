import axios from '../axios'

export const commentApi = {
  // 获取评论列表
  getComments(articleId: number, params: {
    page: number
    size: number
    parentId?: number
  }) {
    return axios.get(`/articles/${articleId}/comments`, { 
      params: {
        ...params,
        articleId
      }
    })
  },

  // 发表评论
  createComment(articleId: number, data: {
    content: string
    parentId?: number | null
  }) {
    return axios.post(`/articles/${articleId}/comments`, {
      content: data.content,
      articleId,
      parentId: data.parentId || null
    })
  },

  // 删除评论
  deleteComment(articleId: number, commentId: number) {
    return axios.delete(`/articles/${articleId}/comments/${commentId}`)
  }
} 