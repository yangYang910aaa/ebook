import axios from 'axios'
import { message } from 'ant-design-vue'
import { useUserStore } from '../store/user'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '',
  timeout: 10000
})

request.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.token = userStore.token
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.success === false) {
      message.error(res.message || '操作失败')
      if (res.message === '用户操作没有权限') {
        const userStore = useUserStore()
        userStore.clearUser()
        window.location.href = '/'
      }
      return Promise.reject(new Error(res.message))
    }
    return res.content
  },
  (error) => {
    message.error(error.message || '网络异常，请稍后重试')
    return Promise.reject(error)
  }
)

export default request
