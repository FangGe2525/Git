import axios from 'axios'
import { showToast } from 'vant'
import router from '../router'
import { storage, TOKEN_KEY } from '../utils/storage'

const http = axios.create({
  baseURL: '/api',
  timeout: 20000
})

http.interceptors.request.use((config) => {
  const token = storage.get(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || '网络异常，请稍后重试'
    if (status === 401) {
      storage.clearAuth()
      if (router.currentRoute.value.path !== '/login') {
        router.replace('/login')
      }
    } else {
      showToast(message)
    }
    return Promise.reject(error)
  }
)

export default http
