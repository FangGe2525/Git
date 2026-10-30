import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 移动端挂在 /m 路径下，与 admin-web 的 /admin 区分
export default defineConfig({
  base: '/m/',
  plugins: [vue()],
  server: {
    port: 5174,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      }
    }
  }
})
