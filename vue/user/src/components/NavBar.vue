<template>
  <div class="nav-container">
    <div class="nav-content">
      <div class="nav-left">
        <router-link to="/" class="logo">
          <span class="logo-text">智己书阁</span>
        </router-link>
        <div class="nav-links">
          <router-link to="/" class="nav-link" active-class="active">首页</router-link>
          <router-link to="/articles" class="nav-link" active-class="active">文章</router-link>
        </div>
      </div>
      <div class="nav-right">
        <div v-if="userStore.isLoggedIn" class="user-info">
          <el-dropdown trigger="click">
            <div class="user-profile">
              <span class="username">{{ userStore.username }}</span>
              <div class="avatar-wrapper">
                <img v-if="userStore.avatar" :src="userStore.avatar" class="avatar" />
                <div v-else class="avatar default-avatar">
                  {{ userStore.username?.charAt(0)?.toUpperCase() }}
                </div>
              </div>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/user')">
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item @click="handleLogout">
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <el-button v-else type="primary" @click="router.push('/login')">
          登录
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store'

const router = useRouter()
const userStore = useUserStore()

// 处理登出
const handleLogout = () => {
  userStore.logout()
  router.push('/')
}

// 只在有 token 时获取用户信息
onMounted(async () => {
  if (userStore.isLoggedIn && !userStore.userInfo) {
    await userStore.fetchUserInfo()
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
  box-shadow: 0 5px 15px rgba(64, 158, 255, 0.08);
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
  background: linear-gradient(135deg, #409EFF, #1677ff);
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
  color: #409EFF;
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(135deg, #409EFF, #1677ff);
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

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 2px;
  border-radius: 20px;
  transition: background-color 0.2s;
}

.user-profile:hover {
  background-color: rgba(64, 158, 255, 0.1);
}

.username {
  font-size: 14px;
  color: #6797f5;
  font-weight: 500;
  margin-left: 8px;
}

.avatar-wrapper {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  background: linear-gradient(135deg, #409EFF, #1677ff);
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

.avatar-wrapper:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 10px rgba(64, 158, 255, 0.2);
}

.avatar-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.default-avatar {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 500;
}

.login-button {
  padding: 10px 24px;
  background: linear-gradient(135deg, #409EFF, #1677ff);
  color: white;
  border: none;
  border-radius: 30px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: transform 0.2s;
}

.login-button:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 10px rgba(64, 158, 255, 0.2);
  background: linear-gradient(135deg, #1677ff, #0958d9);
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

  .avatar-wrapper {
    width: 36px;
    height: 36px;
    font-size: 16px;
    background: linear-gradient(135deg, #409EFF, #1677ff);
  }

  .login-button {
    padding: 8px 20px;
    font-size: 14px;
    background: linear-gradient(135deg, #1677ff, #0958d9);
  }

  .username {
    display: none;
  }
  
  .user-profile {
    gap: 0;
  }
}
</style>