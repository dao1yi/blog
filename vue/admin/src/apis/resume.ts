import axios from '../axios'

export const resumeApi = {
  // 获取简介
  getResume() {
    return axios.get('/resume')
  },

  // 创建简介
  createResume(data: any) {
    return axios.post('/resume', data)
  },

  // 更新简介
  updateResume(data: any) {
    return axios.put('/resume', data)
  },

  // 删除简介
  deleteResume() {
    return axios.delete('/resume')
  }
} 