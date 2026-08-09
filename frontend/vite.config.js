import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 构建产物输出到 Spring Boot 的 static 目录
// 开发时通过 proxy 转发 /rpc 到后端 8686 端口
export default defineConfig({
  plugins: [vue()],
  base: './',
  build: {
    outDir: 'dist',
    assetsDir: 'assets'
  },
  server: {
    port: 5173,
    proxy: {
      '/rpc': {
        target: 'http://127.0.0.1:8686',
        changeOrigin: true
      }
    }
  }
})
