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
      '/user': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/activity': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/prize': {
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
      }
    }
  }
})
