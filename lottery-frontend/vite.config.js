import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  build: {
    outDir: '../src/main/resources/static',
    emptyOutDir: true,
    rollupOptions: {
      output: {
        assetFileNames: 'assets/[name].[ext]'
      }
    }
  },
  server: {
    port: 3000,
    proxy: {
      // 只代理 API 请求，不代理页面路由
      '/user/admin': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/user/sendEmailCode': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/user/emailRegister': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/user/register': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/user/getListInfo': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/activity/create': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/activity/queryList': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/activity/getDetail': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/prize/getList': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/prize/create': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/drawPrize': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/getWinningRecords': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/picture': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
