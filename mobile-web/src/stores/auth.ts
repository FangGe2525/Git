import { defineStore } from 'pinia'
import http from '../api/http'
import { storage, TOKEN_KEY, USER_KEY } from '../utils/storage'

export interface CurrentUser {
  userId: number
  username: string
  realName: string
  role: 'OWNER' | 'WORKER' | 'CS' | 'COMMUNITY'
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: storage.get(TOKEN_KEY),
    user: storage.getJson<CurrentUser>(USER_KEY)
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    role: (state) => state.user?.role || '',
    userId: (state) => state.user?.userId ?? 0,
    displayName: (state) => state.user?.realName || state.user?.username || '',
    isWorker: (state) => state.user?.role === 'WORKER',
    isOwner: (state) => state.user?.role === 'OWNER',
    isStaff: (state) => state.user?.role === 'CS' || state.user?.role === 'COMMUNITY'
  },
  actions: {
    async login(username: string, password: string) {
      const res: any = await http.post('/auth/login', { username, password })
      const data = res.data
      this.token = data.token
      this.user = {
        userId: data.userId,
        username: data.username,
        realName: data.realName,
        role: data.role
      }
      storage.set(TOKEN_KEY, this.token)
      storage.setJson(USER_KEY, this.user)
    },
    /**
     * 用当前 token 拉一次真实身份。
     * 防止 token 被其他标签页或旧会话替换后，界面仍显示上一个账号。
     */
    async syncUser() {
      if (!this.token) return
      const res: any = await http.get('/auth/me')
      const data = res.data
      this.user = {
        userId: data.id,
        username: data.username,
        realName: data.realName,
        role: data.role
      }
      storage.setJson(USER_KEY, this.user)
    },
    logout() {
      this.token = ''
      this.user = null
      storage.clearAuth()
    }
  }
})
