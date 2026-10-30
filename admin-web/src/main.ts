import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'

const pinia = createPinia()
const app = createApp(App)
app.use(pinia).use(router).use(ElementPlus, { locale: zhCn })

// 启动时先核对 token 对应的真实身份
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
