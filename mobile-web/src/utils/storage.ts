/**
 * 用 sessionStorage 存登录态：每个标签页独立。
 * 这样可以在两个标签页里分别登录业主和维修工，互不覆盖。
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
  remove(key: string) {
    sessionStorage.removeItem(key)
  },
  clearAuth() {
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('user')
  }
}

export const TOKEN_KEY = 'token'
export const USER_KEY = 'user'
