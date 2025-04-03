import { defineStore } from 'pinia'
import { getUserInfo, type User } from '@/api/user'
import { log } from 'console'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null as User | null,
    token: localStorage.getItem('token')
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo?.username  ,
    avatar: (state) => state.userInfo?.avatar 
  },
  
  actions: {
    setToken(token: string) {
      this.token = token
      localStorage.setItem('token', token)
    },
    
    clearToken() {
      this.token = null
      localStorage.removeItem('token')
    },
    
    async fetchUserInfo() {
      if (!this.token) return null
      
      try {
        const  data  = await getUserInfo()
        this.userInfo = data
        return data
      } catch (error) {
        console.error('获取用户信息失败:', error)
        return null
      }
    },
    
    logout() {
      this.userInfo = null
      this.clearToken()
    }
  },
  
  persist: true
}) 