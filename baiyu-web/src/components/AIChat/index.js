import { createApp } from 'vue'
import AIChatDialog from '@/views/components/DifyChatModal/AIChatDialog.vue'  // 或者你的组件路径

const AIChat = {
  install(app) {
    // 创建容器
    const container = document.createElement('div')
    container.id = 'ai-chat-container'
    document.body.appendChild(container)

    // 创建并挂载组件
    const aiApp = createApp(AIChatDialog)

    // 如果需要，可以在这里提供全局配置
    aiApp.config.globalProperties.$aiConfig = {
      apiKey: 'your-api-key',
      // 其他配置
    }

    aiApp.mount(container)

    // 暴露实例以便调试
    if (process.env.NODE_ENV === 'development') {
      window.$aiChat = aiApp
    }
  }
}

export default AIChat
