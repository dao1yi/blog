<template>
  <div class="login-container">
    <el-card class="login-card">
      <div class="login-header">
        <img src="../assets/logo.png" alt="logo" class="logo-img">
        <h2>智己书阁管理系统</h2>
      </div>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef">
        <el-form-item prop="account">
          <el-input v-model="loginForm.account" placeholder="用户名/邮箱">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="密码">
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" style="width: 100%">登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { User, Lock } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import type { FormInstance } from 'element-plus'
import axios from '../axios'

const router = useRouter()
const loginFormRef = ref<FormInstance>()

const loginForm = reactive({
  account: '',
  password: ''
})

const rules = {
  account: [{ required: true, message: '请输入用户名/邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const { data } = await axios.post('/user/login', loginForm)
        localStorage.setItem('token', data)
        router.push('/')
      } catch (error) {
        console.error('登录失败:', error)
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1890ff 0%, #1d39c4 100%);
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  width: 200%;
  height: 200%;
  top: -50%;
  left: -50%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 1px, transparent 1px);
  background-size: 24px 24px;
  transform: rotate(30deg);
  animation: moveBackground 60s linear infinite;
}

@keyframes moveBackground {
  0% {
    transform: rotate(30deg) translateY(0);
  }
  100% {
    transform: rotate(30deg) translateY(-50%);
  }
}

.login-card {
  width: 420px;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  padding: 40px;
  position: relative;
  z-index: 1;
  animation: cardAppear 0.6s ease-out;
}

@keyframes cardAppear {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.logo-img {
  height: 64px;
  margin-bottom: 16px;
}

.el-form-item {
  margin-bottom: 24px;
}

.el-input {
  --el-input-height: 46px;
  --el-input-border-radius: 8px;
  --el-input-bg-color: #f8f9fa;
  --el-input-border-color: transparent;
}

.el-input__wrapper {
  background-color: #f8f9fa !important;
  border: 2px solid transparent !important;
  box-shadow: none !important;
  transition: all 0.3s ease !important;
}

.el-input__wrapper:hover {
  background-color: #f0f2f5 !important;
  border-color: var(--primary-color) !important;
}

.el-input__wrapper.is-focus {
  background-color: #fff !important;
  border-color: var(--primary-color) !important;
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1) !important;
}

.el-button {
  height: 46px;
  font-size: 16px;
  font-weight: 500;
  width: 100%;
  border-radius: 8px;
  background: linear-gradient(120deg, var(--primary-color), #1d39c4);
  border: none;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.el-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    120deg,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  );
  transition: 0.5s;
}

.el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(24, 144, 255, 0.25);
}

.el-button:hover::before {
  left: 100%;
}

.el-icon {
  font-size: 20px;
  color: #8c8c8c;
  transition: color 0.3s ease;
}

.is-focus .el-icon {
  color: var(--primary-color);
}
</style> 