import axios from "axios";
import router from "./router";
import { ElMessage } from "element-plus";

// 创建 axios 实例
const instance = axios.create({
  baseURL: '/api/',
  timeout: 5000,
});

// 请求拦截器
instance.interceptors.request.use(
  (config) => {
    // 添加 token
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = token;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
instance.interceptors.response.use(
  response => {
    const { code, message, data } = response.data;
    
    // 成功
    if (code === 200) {
      return { data, message };
    }
    
    // 错误处理
    switch (code) {
      case 401: // 未登录或token已过期
        ElMessage.error(message || '请重新登录');
        localStorage.removeItem('token');
        router.push('/login');
        break;
        
      case 403: // 没有操作权限
        ElMessage.error(message || '没有操作权限');
        break;
        
      case 404: // 资源不存在
        ElMessage.error(message || '请求的资源不存在');
        break;
        
      case 400: // 参数错误
        ElMessage.error(message || '参数错误');
        break;
        
      case 500: // 服务器错误
        ElMessage.error(message || '服务器内部错误');
        break;
        
      // 业务相关错误码
      case 2001: // 数据不存在
        ElMessage.error(message || '数据不存在');
        break;
        
      case 2004: // 医院不存在
        ElMessage.error(message || '医院不存在');
        break;
        
      case 2005: // 套餐不存在
        ElMessage.error(message || '套餐不存在');
        break;
        
      default:
        ElMessage.error(message || '操作失败');
    }
    
    return Promise.reject(new Error(message || '操作失败'));
  },
  error => {
    // 网络错误等
    if (error.response) {
      const { status, data } = error.response;
      switch (status) {
        case 401:
          ElMessage.error('请重新登录');
          localStorage.removeItem('token');
          router.push('/login');
          break;
        case 403:
          ElMessage.error('没有操作权限');
          break;
        case 404:
          ElMessage.error('请求的资源不存在');
          break;
        case 500:
          ElMessage.error('服务器内部错误');
          break;
        default:
          ElMessage.error(data?.message || '操作失败');
      }
    } else if (error.message.includes('timeout')) {
      ElMessage.error('请求超时');
    } else {
      ElMessage.error('网络错误');
    }
    return Promise.reject(error);
  }
);

export default instance;