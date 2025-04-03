import request from '@/utils/request'

export interface LoginData {
  account: string  // 修改为account，支持用户名或邮箱登录
  password: string
}

export interface RegisterData {
  username: string  // 注册时使用username
  password: string
  email: string
  code: string     // 添加验证码字段
}

export interface TokenData {
  token: string
}

// 发送验证码
export function sendCode(email: string) {
  return request({
    url: '/user/code',
    method: 'POST',
    data: { email }
  })
}

// 用户登录
export function login(data: LoginData) {
  return request<string>({  // 返回类型为string（token）
    url: '/user/login',
    method: 'POST',
    data
  })
}

// 用户注册
export function register(data: RegisterData) {
  return request({
    url: '/user/register',
    method: 'POST',
    data
  })
} 