import axios from '../axios'

export const userApi = {
  // 用户登录
  login(data: {
    account: string
    password: string
  }) {
    return axios.post('/user/login', data)
  },

  // 获取用户信息
  getUserInfo() {
    return axios.get('/user/info')
  },

  // 获取用户列表
  getUsers(params: {
    page: number
    size: number
    username?: string
    email?: string
    status?: number
  }) {
    return axios.get('/user/list', { params })
  },

  // 修改用户状态
  updateUserStatus(id: number, status: number) {
    return axios.put(`/user/${id}/status`, null, {
      params: { status }
    })
  },

  // 更新用户信息
  updateUser(id: number, data: {
    username: string
    email: string
    role: string
  }) {
    return axios.put(`/user/${id}`, data)
  }
} 