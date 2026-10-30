import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 管理后台挂在 /admin 路径下
export default defineConfig({
  base: '/admin/',
  plugins: [vue()],
  server: {
    port: 5175,
    proxy: {
      '/api': { target: 'http://127.0.0.1:8080', changeOrigin: true },
      '/uploads': { target: 'http://127.0.0.1:8080', changeOrigin: true }
    }
  }
})
