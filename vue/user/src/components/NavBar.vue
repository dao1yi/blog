<template>
  <div class="nav-container">
    <div class="nav-content">
      <div class="nav-left">
        <router-link to="/" class="logo">
          <span class="logo-text">个人博客</span>
        </router-link>
        <div class="nav-links">
          <router-link to="/" class="nav-link" active-class="active">首页</router-link>
          <router-link to="/articles" class="nav-link" active-class="active">文章</router-link>
        </div>
      </div>
      <div class="nav-right">
        <template v-if="token">
          <router-link to="/user" class="user-section">
            <div class="avatar-container">
              <img v-if="userInfo?.avatar" :src="userInfo.avatar" alt="头像" />
              <span v-else>我的</span>
            </div>
          </router-link>
          <el-button class="logout-button" @click="handleLogout">退出</el-button>
        </template>
        <template v-else>
          <router-link to="/login">
            <el-button class="login-button">登录 / 注册</el-button>
          </router-link>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserInfo, type User } from '@/api/user'

const router = useRouter()
const token = ref(localStorage.getItem('token'))
const userInfo = ref<User | null>(null)

const fetchUserInfo = async () => {
  if (!token.value) {
    userInfo.value = null
    return
  }
  
  try {
    const response = await getUserInfo()
    userInfo.value = response.data
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

const handleLogout = () => {
  localStorage.removeItem('token')
  token.value = null
  userInfo.value = null
  ElMessage.success('退出登录成功')
  router.push('/login')
}

// 监听token变化
watch(() => localStorage.getItem('token'), (newToken) => {
  token.value = newToken
  fetchUserInfo()
})

// 提供刷新用户信息的方法
const refreshUserInfo = () => {
  token.value = localStorage.getItem('token')
  fetchUserInfo()
}

// 定义要暴露的属性和方法
defineExpose({
  refreshUserInfo
})

onMounted(() => {
  if (token.value) {
    fetchUserInfo()
  }
})
</script>

<style scoped>
.nav-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: var(--header-height);
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
  z-index: 1000;
}

.nav-content {
  max-width: 1100px;
  height: 100%;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 40px;
}

.logo {
  text-decoration: none;
  transition: transform 0.2s;
}

.logo:hover {
  transform: scale(1.05);
}

.logo-text {
  font-size: 24px;
  font-weight: bold;
  background: linear-gradient(135deg, #42b983, #2e8b57);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.nav-links {
  display: flex;
  gap: 30px;
}

.nav-link {
  text-decoration: none;
  color: #495057;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.2s;
  position: relative;
  padding: 8px 0;
}

.nav-link:hover,
.nav-link.active {
  color: #42b983;
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(135deg, #42b983, #2e8b57);
  transition: width 0.2s;
}

.nav-link:hover::after,
.nav-link.active::after {
  width: 100%;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-section {
  text-decoration: none;
}

.avatar-container {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  background: linear-gradient(135deg, #42b983, #2e8b57);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 500;
  font-size: 18px;
  cursor: pointer;
  transition: transform 0.2s;
  border: 2px solid transparent;
}

.avatar-container:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.avatar-container img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.login-button,
.logout-button {
  padding: 10px 24px;
  background: linear-gradient(135deg, #42b983, #2e8b57);
  color: white;
  border: none;
  border-radius: 30px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: transform 0.2s;
}

.login-button:hover,
.logout-button:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

/* 响应式布局 */
@media (max-width: 768px) {
  .nav-container {
    height: var(--mobile-header-height);
  }

  .nav-content {
    padding: 0 16px;
  }

  .logo-text {
    font-size: 20px;
  }

  .nav-left {
    gap: 20px;
  }

  .nav-links {
    gap: 20px;
  }

  .nav-link {
    font-size: 14px;
  }

  .avatar-container {
    width: 36px;
    height: 36px;
    font-size: 16px;
  }

  .login-button,
  .logout-button {
    padding: 8px 20px;
    font-size: 14px;
  }
}
</style>