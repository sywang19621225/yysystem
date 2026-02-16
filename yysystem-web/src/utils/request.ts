import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api', // Vite proxy to http://localhost:8080
  timeout: 5000
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      if (res.code === 401) {
        ElMessage.error(res.message || '登录已过期，请重新登录')
        localStorage.removeItem('token')
        location.href = '/login'
        return Promise.reject(new Error(res.message || 'Unauthorized'))
      }
      // 403 权限不足，不显示错误提示（菜单已控制，正常情况不会访问到）
      if (res.code === 403) {
        console.warn('权限不足，已拦截请求:', response.config?.url, res.message)
        return Promise.reject(new Error(res.message || 'Forbidden'))
      }
      ElMessage.error(res.message || '系统错误')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res.data
  },
  error => {
    console.error('err' + error)
    if (error.response && error.response.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      location.href = '/login'
    } else if (error.response && error.response.status === 403) {
      // 403 权限不足，不显示错误提示（菜单已控制，正常情况不会访问到）
      console.warn('权限不足，已拦截请求:', error.config?.url)
    } else {
      const url = (error.config && (error.config as any).url) ? (error.config as any).url : ''
      const method = (error.config && (error.config as any).method) ? String((error.config as any).method).toUpperCase() : ''
      const status = error.response?.status
      const serverMsg = (error.response && (error.response.data && (error.response.data.message || error.response.data.msg))) ? (error.response.data.message || error.response.data.msg) : ''
      const msg = serverMsg ? serverMsg : error.message
      ElMessage.error(`${status || ''} ${method} ${url}：${msg}`)
    }
    return Promise.reject(error)
  }
)

export default request
