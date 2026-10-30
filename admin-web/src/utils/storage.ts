/**
 * 用 sessionStorage 存登录态：每个标签页独立，避免多标签页互相覆盖 token。
 */
export const storage = {
  get(key: string): string {
    return sessionStorage.getItem(key) || ''
  },
  getJson<T>(key: string): T | null {
    const raw = sessionStorage.getItem(key)
    if (!raw) return null
    try {
      return JSON.parse(raw) as T
    } catch {
      return null
    }
  },
  set(key: string, value: string) {
    sessionStorage.setItem(key, value)
  },
  setJson(key: string, value: unknown) {
    sessionStorage.setItem(key, JSON.stringify(value))
  },
  clearAuth() {
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('user')
  }
}

export const TOKEN_KEY = 'token'
export const USER_KEY = 'user'
