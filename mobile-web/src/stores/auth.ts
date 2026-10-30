import { defineStore } from 'pinia'
import http from '../api/http'

export interface CurrentUser {
  userId: number
  username: string
  realName: string
  role: 'OWNER' | 'WORKER' | 'CS' | 'COMMUNITY'
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null') as CurrentUser | null
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    role: (state) => state.user?.role || '',
    userId: (state) => state.user?.userId ?? 0,
    isWorker: (state) => state.user?.role === 'WORKER',
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
      localStorage.setItem('token', this.token)
      localStorage.setItem('user', JSON.stringify(this.user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
