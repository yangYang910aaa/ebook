import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 开发环境通过 Vite 代理转发到后端 8088，前端请求同源，无需处理 CORS
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 8080,
    proxy: {
      '/user': 'http://localhost:8088',
      '/ebook': 'http://localhost:8088',
      '/category': 'http://localhost:8088',
      '/doc': 'http://localhost:8088',
      '/ebook-snapshot': 'http://localhost:8088',
      '/ws': { target: 'ws://localhost:8088', ws: true }
    }
  }
})
