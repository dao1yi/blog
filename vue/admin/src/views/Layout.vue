<template>
  <el-container class="layout-container">
    <el-header>
      <div class="header-left">
        <!-- <img src="../assets/logo.png" alt="logo" class="logo-img"> -->
        <h2>智己书阁后台</h2>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="el-dropdown-link">
            管理员
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    
    <el-container class="main-container">
      <el-aside width="220px">
        <el-menu
          :default-active="route.path"
          router
        >
          <el-menu-item index="/dashboard">
            <el-icon><DataBoard /></el-icon>
            <span>数据统计</span>
          </el-menu-item>
          <el-menu-item index="/articles">
            <el-icon><Document /></el-icon>
            <span>文章管理</span>
          </el-menu-item>
          <el-menu-item index="/files">
            <el-icon><Folder /></el-icon>
            <span>文件管理</span>
          </el-menu-item>
          <el-menu-item index="/user/list">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/resume">
            <el-icon><UserFilled /></el-icon>
            <span>简介管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-main>
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ArrowDown, Document, Folder, User, DataBoard, UserFilled } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const handleCommand = (command: string) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f6f8fa;
}

.main-container {
  flex: 1;
  overflow: hidden;
}

.el-header {
  background: linear-gradient(to right, #ffffff, #f8f9fa);
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1000;
  border-bottom: 1px solid #edf0f5;
}

.header-left h2 {
  margin: 0;
  font-size: 1.6rem;
  font-weight: 600;
  color: #1f2f3d;
  background: linear-gradient(120deg, var(--primary-color), #1d39c4);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-color);
  padding: 8px 16px;
  border-radius: 24px;
  transition: all 0.3s ease;
  background: #fff;
  border: 1px solid #edf0f5;
}

.el-dropdown-link:hover {
  background: #f6f8fa;
  border-color: var(--primary-color);
  transform: translateY(-1px);
}

.el-aside {
  background: linear-gradient(180deg, #ffffff 0%, #f8f9fa 100%);
  border-right: 1px solid #edf0f5;
  overflow-y: auto;
  transition: all 0.3s ease;
  width: 220px;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.4rem;
  font-weight: 600;
  background: linear-gradient(120deg, var(--primary-color), #1d39c4);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 0.5px;
  border-bottom: 1px solid #edf0f5;
}

.el-menu {
  border-right: none;
  padding: 12px;
  background: transparent;
}

.el-menu-item {
  height: 48px;
  line-height: 48px;
  margin: 4px 0;
  border-radius: 8px;
  color: #666;
}

.el-menu-item.is-active {
  background: linear-gradient(120deg, var(--primary-color), #1d39c4) !important;
  color: white !important;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
}

.el-menu-item:hover:not(.is-active) {
  background: rgba(24, 144, 255, 0.1) !important;
  color: var(--primary-color) !important;
}

.el-menu-item .el-icon {
  margin-right: 8px;
  font-size: 18px;
  transition: transform 0.3s ease;
}

.el-menu-item:hover .el-icon {
  transform: translateX(4px);
}

.el-main {
  padding: 20px;
  overflow-y: auto;
  background-color: #f6f8fa;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-thumb {
  background: #d0d7de;
  border-radius: 6px;
}

::-webkit-scrollbar-track {
  background: #f6f8fa;
  border-radius: 6px;
}

/* 添加阴影过渡效果 */
.el-aside, .el-header {
  transition: box-shadow 0.3s ease;
}

.el-aside:hover {
  box-shadow: 4px 0 16px rgba(0, 0, 0, 0.05);
}

/* 添加 logo 样式 */
.logo-img {
  height: 32px;
  margin-right: 12px;
}
</style> 