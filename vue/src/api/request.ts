/* axios 实例封装：统一 baseURL、超时、请求拦截器（带 token）、响应拦截器（统一错误处理） */
import axios from 'axios'
import { message } from 'ant-design-vue'
import { useUserStore } from '../store/user'

// 创建 axios 实例，baseURL 从环境变量读取，超时 10 秒
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '',
  timeout: 10000
})

// 请求拦截器：已登录时在请求头中携带 token
request.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.token = userStore.token
  }
  return config
})

// 响应拦截器：统一处理后端返回结构 { success, message, content }
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 业务失败：弹出错误信息并 reject
    if (res.success === false) {
      message.error(res.message || '操作失败')
      // 无权限时清除登录态并跳转首页
      if (res.message === '用户操作没有权限') {
        const userStore = useUserStore()
        userStore.clearUser()
        window.location.href = '/'
      }
      return Promise.reject(new Error(res.message))
    }
    // 成功时直接返回 content 字段，调用方无需再解构
    return res.content
  },
  // 网络/HTTP 层错误：统一提示
  (error) => {
    message.error(error.message || '网络异常，请稍后重试')
    return Promise.reject(error)
  }
)

export default request
