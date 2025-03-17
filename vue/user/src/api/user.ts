import request from '@/utils/request'

export interface User {
  id: number
  username: string
  email: string
  avatar: string
  role: string
  status: number
  createdTime: string
  updatedTime: string
}

// 获取当前用户信息
export function getUserInfo() {
  return request<User>({
    url: '/user/info',
    method: 'GET'
  })
} 