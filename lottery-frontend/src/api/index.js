import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    const result = response.data
    // 后端返回 Result<T> 格式：{ code, message, data }
    if (result.code === 200) {
      return result
    } else {
      ElMessage.error(result.message || '请求失败')
      return Promise.reject(new Error(result.message))
    }
  },
  error => {
    const message = error.response?.data?.message || '网络请求失败'
    ElMessage.error(message)

    // 401 错误处理：只有当没有 token 时才跳转登录页
    // 如果用户有 token 但返回 401，可能是权限不足，不应该登出
    if (error.response?.status === 401) {
      const token = localStorage.getItem('token')
      if (!token) {
        // 没有 token，跳转登录页
        window.location.href = '/login'
      }
      // 有 token 但 401，可能是权限不足，不处理（已显示错误消息）
    }

    return Promise.reject(error)
  }
)

export default api
