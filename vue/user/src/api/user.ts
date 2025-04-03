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

// 更新用户信息
export function updateUserInfo(id: number, data: { avatar?: string; isReceivePush?: number }) {
  return request<void>({
    url: `/user/info/${id}`,
    method: 'PUT',
    data
  })
} 