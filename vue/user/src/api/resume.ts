import request from '@/utils/request'
import type { Resume } from '@/types/resume'

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

// 获取简历信息
export function getResume() {
  return request<Resume>({
    url: '/resume',
    method: 'GET'
  })
}

// 更新简历信息
export function updateResume(data: Resume) {
  return request<ApiResponse<void>>({
    url: '/resume',
    method: 'PUT',
    data
  })
} 