import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Vant from 'vant'
import 'vant/lib/index.css'
import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'

const pinia = createPinia()
const app = createApp(App)
app.use(pinia).use(router).use(Vant)

// 启动时先核对 token 对应的真实身份，避免沿用旧会话的用户信息
const auth = useAuthStore(pinia)
if (auth.token) {
  auth
    .syncUser()
    .catch(() => {
      auth.logout()
    })
    .finally(() => app.mount('#app'))
} else {
  app.mount('#app')
}
