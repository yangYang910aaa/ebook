import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 开发环境通过 Vite 代理转发到后端 8088，前端请求同源，无需处理 CORS
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 8080,
    proxy: {
      '/user': 'http://localhost:8088',
      '/ebook': {
        target: 'http://localhost:8088',
        bypass: (req) => {
          // SPA 路由 /ebook（含查询参数）走前端，其余 /ebook/* 为 API 请求转后端
          if (req.url === '/ebook' || (req.url?.startsWith('/ebook?'))) return req.url
        }
      },
      '/category': 'http://localhost:8088',
      '/doc': {
        target: 'http://localhost:8088',
        bypass: (req) => {
          // SPA 路由 /doc（含查询参数）走前端，其余 /doc/* 为 API 请求转后端
          if (req.url === '/doc' || (req.url?.startsWith('/doc?'))) return req.url
        }
      },
      '/ebook-snapshot': 'http://localhost:8088',
      '/ws': { target: 'ws://localhost:8088', ws: true }
    }
  }
})
