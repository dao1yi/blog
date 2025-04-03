import request from '@/utils/request'

// 发送留言
export function sendMessage(message: string) {
  return request<void>({
    url: '/message',
    method: 'POST',
    params: { message }
  })
} 