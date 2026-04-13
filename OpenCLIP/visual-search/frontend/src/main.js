import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import { ElMessage } from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import './assets/global.css'
import App from './App.vue'
import router from './router'

// 输出编译时间日志
console.log(`📦 Frontend Build Time: ${__BUILD_TIME__}`)

const app = createApp(App)
const pinia = createPinia()

// 全局错误处理器
app.config.errorHandler = (err, instance, info) => {
  console.error('Vue Error:', err)
  
  // 开发环境显示详细错误
  if (import.meta.env.DEV) {
    console.error('Component:', instance)
    console.error('Info:', info)
  }
  
  // 生产环境显示友好提示
  ElMessage.error(`应用错误: ${err.message || '未知错误'}`)
  
  // 可上报到监控系统
  // reportError(err, instance, info)
}

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(ElementPlus)
app.use(router)
app.mount('#app')
