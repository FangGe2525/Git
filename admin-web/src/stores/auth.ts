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
    roleLabel: (state) => {
      const map: Record<string, string> = { CS: '物业客服', COMMUNITY: '社区工作人员', WORKER: '维修工', OWNER: '业主' }
      return state.user?.role ? map[state.user.role] : ''
    }
  },
  actions: {
    async login(username: string, password: string) {
      const res: any = await http.post('/auth/login', { username, password })
      const data = res.data
      if (data.role !== 'CS' && data.role !== 'COMMUNITY') {
        throw new Error('该账号无权登录管理后台')
      }
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
