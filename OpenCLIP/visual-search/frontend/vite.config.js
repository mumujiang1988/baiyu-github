import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  // 加载环境变量
  const env = loadEnv(mode, process.cwd(), '')
  
  // 从环境变量读取后端地址，默认为 localhost:8080（本地开发）
  const backendUrl = env.VITE_BACKEND_URL  
  
  console.log(`🔧 后端代理地址: ${backendUrl}`)
  
  return {
    plugins: [vue()],
    server: {
      host: '0.0.0.0',
      port: 3000,
      proxy: {
        '/api': {
          target: backendUrl,
          changeOrigin: true,
          rewrite: (path) => path  // 保持路径不变
        }
      }
    }
  }
})
