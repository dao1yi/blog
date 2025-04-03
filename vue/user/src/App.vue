<template>
  <div class="app-container">
    <nav-bar />
    <div class="main-content">
      <router-view></router-view>
    </div>
  </div>
</template>

<script setup lang="ts">
import NavBar from '@/components/NavBar.vue'
</script>

<style>
:root {
  --primary-color: #409EFF;
  --text-primary: #303133;
  --text-regular: #606266;
  --text-secondary: #909399;
  --border-color: #DCDFE6;
  --page-background: #f0f7ff;
  --card-background: #ffffff;
  --header-height: 64px;
  --mobile-header-height: 56px;
  --max-content-width: 1200px;
  --border-radius: 8px;
  --box-shadow: 0 2px 12px rgba(64, 158, 255, 0.08);
}
.login-confirm-dialog {
  margin-top: 10px;
}

/* 重置body样式 */
body {
  margin: 0;
  padding: 0;
  overflow: hidden;
  height: 100vh;
}

.app-container {
  height: 100vh;
  overflow: hidden;
  background-color: var(--page-background);
  display: flex;
  flex-direction: column;
}

.main-content {
  max-width: var(--max-content-width);
  margin: 0 auto;
  padding: 20px 24px;
  width: 100%;
  height: calc(100vh - var(--header-height));
  margin-top: 1px;
  overflow-y: auto;
  box-sizing: border-box;
}
/* 卡片样式 */
.card {
  background-color: var(--card-background);
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow);
  padding: 24px;
  margin-bottom: 20px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

/* 标题样式 */
.page-title {
  font-size: 28px;
  color: var(--text-primary);
  margin-bottom: 24px;
  font-weight: 600;
}

.section-title {
  font-size: 20px;
  color: var(--text-primary);
  margin-bottom: 16px;
  font-weight: 500;
}

/* 美化滚动条 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: var(--page-background);
}

::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: var(--text-secondary);
}

/* 全局过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .main-content {
    padding: calc(var(--mobile-header-height) + 16px) 16px 24px;
  }

  .card {
    padding: 16px;
    margin-bottom: 16px;
  }

  .page-title {
    font-size: 24px;
    margin-bottom: 20px;
  }

  .section-title {
    font-size: 18px;
    margin-bottom: 14px;
  }
}

/* 文本样式 */
.text-primary {
  color: var(--primary-color);
}

.text-secondary {
  color: var(--text-secondary);
}

.text-regular {
  color: var(--text-regular);
}

/* 布局工具类 */
.flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

.flex-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.flex-column {
  display: flex;
  flex-direction: column;
}

.gap-sm {
  gap: 8px;
}

.gap-md {
  gap: 16px;
}

.gap-lg {
  gap: 24px;
}
</style>