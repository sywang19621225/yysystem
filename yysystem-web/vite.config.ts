import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    host: true,
    strictPort: true,
    cors: {
      origin: '*',
      methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
      allowedHeaders: ['Content-Type', 'Authorization', 'X-Requested-With'],
      credentials: true
    },
    allowedHosts: ['localhost', '127.0.0.1', 'yuanyismart.xicp.io', '.xicp.io'],
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/files': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/asm': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      }
    }
  }
})
