import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig(({ mode }) => {
  // 加载环境变量
  const env = loadEnv(mode, process.cwd(), '')
  
  // 从环境变量读取后端地址，默认为 localhost:8080（本地开发）
  const backendUrl = env.VITE_BACKEND_URL  
  
  console.log(`🔧 后端代理地址: ${backendUrl}`)
  
  return {
    plugins: [
      vue(),
      {
        name: 'build-time-inject',
        config() {
          // 获取东八区时间 (UTC+8)
          const now = new Date()
          const utc8Time = new Date(now.getTime() + (8 * 60 * 60 * 1000))
          const buildTime = utc8Time.toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: false
          })
          
          return {
            define: {
              __BUILD_TIME__: JSON.stringify(buildTime)
            }
          }
        }
      }
    ],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    },
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
