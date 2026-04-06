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

    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }

    return Promise.reject(error)
  }
)

export default api
