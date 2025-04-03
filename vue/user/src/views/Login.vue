<template>
  <div class="login-container">
    <div class="login-box">
      <h2 class="title">{{ isLogin ? '登录' : '注册' }}</h2>
      
      <el-form :model="formData" :rules="rules" ref="formRef" class="login-form">
        <template v-if="isLogin">
          <el-form-item prop="account">
            <el-input
              v-model="formData.account"
              placeholder="用户名或邮箱"
              prefix-icon="User"
            />
          </el-form-item>
        </template>
        
        <template v-else>
          <el-form-item prop="username">
            <el-input
              v-model="formData.username"
              placeholder="用户名"
              prefix-icon="User"
            />
          </el-form-item>
          
          <el-form-item prop="email">
            <el-input
              v-model="formData.email"
              placeholder="邮箱"
              prefix-icon="Message"
            >
              <template #append>
                <el-button 
                  :disabled="sendingCode || countdown > 0" 
                  @click="handleSendCode"
                >
                  {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="code">
            <el-input
              v-model="formData.code"
              placeholder="验证码"
              prefix-icon="Lock"
            />
          </el-form-item>
        </template>
        
        <el-form-item prop="password">
          <el-input
            v-model="formData.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item v-if="!isLogin" prop="confirmPassword">
          <el-input
            v-model="formData.confirmPassword"
            type="password"
            placeholder="确认密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="submit-btn" @click="handleSubmit" :loading="loading">
            {{ isLogin ? '登录' : '注册' }}
          </el-button>
        </el-form-item>

        <div class="form-footer">
          <span class="switch-text" @click="switchMode">
            {{ isLogin ? '没有账号？去注册' : '已有账号？去登录' }}
          </span>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Lock, Message } from '@element-plus/icons-vue'
import { login, register, sendCode } from '@/api/auth'
import { useUserStore } from '@/store'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const isLogin = ref(true)
const sendingCode = ref(false)
const countdown = ref(0)

const formData = reactive({
  account: '',    // 登录时使用
  username: '',   // 注册时使用
  password: '',
  confirmPassword: '',
  email: '',
  code: ''       // 验证码
})

// 发送验证码
const handleSendCode = async () => {
  if (!formData.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  
  try {
    sendingCode.value = true
    await sendCode(formData.email)
    ElMessage.success('验证码已发送，请查收邮箱')
    
    // 开始倒计时
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败:', error)
  } finally {
    sendingCode.value = false
  }
}

const validatePass2 = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== formData.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = reactive<FormRules>({
  account: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度应为6位', trigger: 'blur' }
  ]
})

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    if (isLogin.value) {
      await handleLogin()
    } else {
      await handleRegister()
    }
  } catch (error) {
    console.error('操作失败:', error)
  } finally {
    loading.value = false
  }
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  try {
    loading.value = true
    await formRef.value.validate()
    
    // 登录获取 token
    const token = await login({
      account: formData.account,
      password: formData.password
    })
    
    // 存储 token 到 store
    userStore.setToken(token)
    
    // 获取并存储用户信息
    await userStore.fetchUserInfo()
    
    // 获取重定向地址或默认跳转到首页
    const redirect = (router.currentRoute.value.query.redirect as string) || '/'
    router.push(redirect)
    
    ElMessage.success('登录成功')
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error('登录失败，请检查账号密码')
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  try {
    loading.value = true
    await register({
      username: formData.username,
      password: formData.password,
      email: formData.email,
      code: formData.code
    })
    ElMessage.success('注册成功，请登录')
    isLogin.value = true
    formRef.value.resetFields()
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}

const switchMode = () => {
  isLogin.value = !isLogin.value
  formRef.value?.resetFields()
}

// 处理登出
const handleLogout = () => {
  userStore.logout()  // 调用 store 的登出方法
  router.push('/login')
}
</script>

<style scoped>
.login-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f0f4ff, #e6f7ff);
  overflow: hidden;
}

/* 背景装饰 */
.login-container::before,
.login-container::after {
  content: '';
  position: absolute;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color), #2e8b57);
  opacity: 0.1;
  z-index: 0;
}

.login-container::before {
  top: -100px;
  left: -100px;
}

.login-container::after {
  bottom: -100px;
  right: -100px;
}

.login-box {
  width: 100%;
  max-width: 440px;
  padding: 40px;
  background: var(--card-background);
  border-radius: var(--border-radius);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  position: relative;
  z-index: 1;
  transition: transform 0.3s, box-shadow 0.3s;
}

.login-box:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
}

.title {
  text-align: center;
  color: var(--text-primary);
  margin-bottom: 35px;
  font-size: 28px;
  font-weight: 600;
  position: relative;
  padding-bottom: 15px;
}

.title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 3px;
  background: linear-gradient(135deg, var(--primary-color), #2e8b57);
  border-radius: 3px;
}

.login-form {
  .el-form-item {
    margin-bottom: 25px;
  }

  :deep(.el-input__wrapper) {
    background: rgba(255, 255, 255, 0.8);
    border: 1px solid rgba(0, 0, 0, 0.08);
    border-radius: var(--border-radius);
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);
    transition: all 0.3s;
    padding: 4px 11px;
  }

  :deep(.el-input__wrapper:hover),
  :deep(.el-input__wrapper.is-focus) {
    border-color: var(--primary-color);
    box-shadow: 0 2px 8px rgba(66, 185, 131, 0.1);
  }

  :deep(.el-input__inner) {
    height: 42px;
    font-size: 15px;
  }

  :deep(.el-button) {
    border-radius: var(--border-radius);
    height: 42px;
  }
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  background: linear-gradient(135deg, var(--primary-color), #2e8b57);
  border: none;
  box-shadow: 0 4px 12px rgba(46, 139, 87, 0.2);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 15px rgba(46, 139, 87, 0.3);
    background: linear-gradient(135deg, #2e8b57, #1f6f43);
  }

  &:active {
    transform: translateY(1px);
    box-shadow: 0 2px 8px rgba(46, 139, 87, 0.2);
  }
}

.form-footer {
  text-align: center;
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.switch-text {
  color: var(--primary-color);
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.3s;
  padding: 8px 16px;
  border-radius: 20px;
  
  &:hover {
    color: #2e8b57;
    background: rgba(46, 139, 87, 0.08);
  }
}

@media (max-width: 768px) {
  .login-box {
    margin: 0 20px;
    padding: 30px 25px;
    max-height: 90vh;
    overflow-y: auto;
  }

  .title {
    font-size: 24px;
    margin-bottom: 30px;
  }

  .submit-btn {
    height: 42px;
    font-size: 15px;
  }

  .login-form {
    :deep(.el-input__inner) {
      height: 40px;
    }
  }

  .switch-text {
    font-size: 14px;
  }
}
</style> 