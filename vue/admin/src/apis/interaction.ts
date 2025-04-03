import axios from '../axios'

export const interactionApi = {
  // 点赞/取消点赞
  toggleLike(articleId: number) {
    return axios.post(`/articles/${articleId}/like`)
  },

  // 收藏/取消收藏
  toggleCollection(articleId: number) {
    return axios.post(`/articles/${articleId}/collection`)
  },

  // 获取点赞状态
  getLikeStatus(articleId: number) {
    return axios.get(`/articles/${articleId}/like/status`)
  },

  // 获取收藏状态
  getCollectionStatus(articleId: number) {
    return axios.get(`/articles/${articleId}/collection/status`)
  }
} 