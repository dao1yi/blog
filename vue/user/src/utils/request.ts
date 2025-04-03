import axios from 'axios'
import type { AxiosResponse } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import router from '@/router'

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

// Create axios instance
const request = axios.create({
  baseURL: '/api',
  timeout: 5000
})

// Request interceptor
request.interceptors.request.use(
  config => {
    // Add token to header if exists
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 用于防止多个401请求同时弹出确认框
let isShowingLoginConfirm = false

// Response interceptor
request.interceptors.response.use(
  async (response: AxiosResponse<ApiResponse<any>>) => {
    const { code, message, data } = response.data

    // 成功
    if (code === 200) {
      return data
    }

    // 401: 未登录或token过期
    if (code === 401) {
      localStorage.removeItem('token')
      // 如果已经在显示确认框，直接拒绝请求
      if (isShowingLoginConfirm) {
        return Promise.reject(new Error('未登录或登录已过期'))
      }

      try {
        isShowingLoginConfirm = true
        await ElMessageBox.confirm(
          '您需要登录才能继续操作，是否前往登录页面？',
          '提示',
          {
            confirmButtonText: '去登录',
            cancelButtonText: '取消',
            type: 'warning',
            customClass: 'login-confirm-dialog', 
          }
        )
        // 用户点击确认，清除token并跳转到登录页
        // localStorage.removeItem('token')
        router.push('/login')
      } catch {
        // 用户点击取消，不做任何操作
      } finally {
        isShowingLoginConfirm = false
      }
      return Promise.reject(new Error('未登录或登录已过期'))
    }

    // 其他业务错误
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message || '请求失败'))
  },
  error => {
    if (error.response) {
      // 服务器返回了错误状态码
      switch (error.response.status) {
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error('请求失败')
      }
    } else if (error.request) {
      // 请求发出去了但没有收到响应
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      // 请求配置出错
      ElMessage.error('请求配置错误')
    }
    return Promise.reject(error)
  }
)

export default request 