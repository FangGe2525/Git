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
    roleLabel: (state) => {
      const map: Record<string, string> = {
        CS: '物业客服',
        COMMUNITY: '社区工作人员',
        WORKER: '维修工',
        OWNER: '业主'
      }
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
      storage.set(TOKEN_KEY, this.token)
      storage.setJson(USER_KEY, this.user)
    },
    /** 启动时用 token 核对真实身份，避免旧会话信息残留 */
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
