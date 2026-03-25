<template>
  <!-- 悬浮聊天按钮 -->
  <div class="floating-chat-button" @click="toggleChatWindow" v-if="!isWindowVisible">
    <div class="chat-button-content">
      <el-icon class="chat-icon"><ChatDotRound /></el-icon>
      <span class="button-text">AI助手</span>
      <el-badge v-if="unreadCount > 0" :value="unreadCount" class="unread-badge" />
    </div>
  </div>

  <!-- 聊天窗口 -->
  <div class="chat-window-container" :class="{ visible: isWindowVisible, minimized: isMinimized }">
    <!-- 窗口标题栏 -->
    <div class="chat-header" @dblclick="toggleMinimize">
      <div class="header-left">
        <el-icon class="ai-icon"><ChatDotRound /></el-icon>
        <span class="title">Dify AI 助手</span>
        <el-tag v-if="isOnline" size="small" type="success" effect="plain">在线</el-tag>
        <el-tag v-else size="small" type="danger" effect="plain">离线</el-tag>
      </div>

      <div class="header-actions">
        <el-tooltip content="最小化" placement="bottom">
          <el-button size="small" text circle @click="toggleMinimize">
            <el-icon><Minus /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="关闭" placement="bottom">
          <el-button size="small" text circle @click="closeWindow">
            <el-icon><Close /></el-icon>
          </el-button>
        </el-tooltip>
      </div>
    </div>

    <!-- 最小化时显示 -->
    <div v-if="isMinimized" class="minimized-content" @click="toggleMinimize">
      <el-icon><ChatDotRound /></el-icon>
      <span>AI助手</span>
      <el-badge v-if="unreadCount > 0" :value="unreadCount" class="minimized-badge" />
    </div>

    <!-- 完整聊天窗口 -->
    <div v-else class="chat-content">
      <!-- 欢迎提示 -->
      <div class="welcome-section" v-if="messages.length === 0">
        <div class="welcome-icon">
          <el-icon><ChatDotRound /></el-icon>
        </div>
        <h3>欢迎使用 Dify AI 助手</h3>
        <p>我可以帮您：</p>
        <ul class="welcome-features">
          <li>回答各种问题</li>
          <li>通过图片识别物料</li>
          <li>提供技术支持</li>
        </ul>
        <div class="quick-questions">
          <el-button size="small" @click="sendQuickQuestion('如何搜索物料？')" link>
            如何搜索物料？
          </el-button>
          <el-button size="small" @click="sendQuickQuestion('物料识别支持哪些图片格式？')" link>
            支持哪些图片格式？
          </el-button>
        </div>
      </div>

      <!-- 聊天消息区域 -->
      <div class="chat-messages" ref="messagesContainer" @scroll="handleScroll">
        <div v-for="(message, index) in messages" :key="index"
             :class="['message', message.type]">

          <!-- 用户消息 -->
          <div v-if="message.type === 'user'" class="message-content user">
            <div class="avatar">👤</div>
            <div class="bubble">
              <div v-if="message.contentType === 'text'">{{ message.content }}</div>
              <div v-else-if="message.contentType === 'image'" class="image-message">
                <img :src="message.content" alt="上传的图片" />
              </div>
              <div class="timestamp">{{ message.timestamp }}</div>
            </div>
          </div>

          <!-- AI消息 -->
          <div v-else class="message-content ai">
            <div class="avatar">🤖</div>
            <div class="bubble">
              <!-- 普通文本回复 -->
              <div v-if="message.contentType === 'text'">
                <div v-html="formatText(message.content)"></div>

                <!-- 显示参考来源（retriever_resources） -->
                <div v-if="message.references && message.references.length > 0" class="references-section">
                  <div class="references-header">
                    <el-icon><Document /></el-icon>
                    <span>参考来源：</span>
                    <span class="references-count">{{ message.references.length }} 条</span>
                  </div>
                  <div class="references-list">
                    <div v-for="(ref, refIndex) in message.references" :key="refIndex" class="reference-item">
                      <div class="reference-content" v-html="formatReferenceContent(ref)"></div>
                      <div v-if="ref.metadata" class="reference-metadata">
                        <template v-if="ref.metadata.file_name">
                          <span class="metadata-item">
                            <el-icon><Document /></el-icon>
                            文件：{{ ref.metadata.file_name }}
                          </span>
                        </template>
                        <template v-if="ref.metadata.title">
                          <span class="metadata-item">
                            <el-icon><Reading /></el-icon>
                            标题：{{ ref.metadata.title }}
                          </span>
                        </template>
                        <template v-if="ref.metadata.dataset_name">
                          <span class="metadata-item">
                            <el-icon><Folder /></el-icon>
                            数据集：{{ ref.metadata.dataset_name }}
                          </span>
                        </template>
                        <template v-if="ref.metadata.document_id">
                          <span class="metadata-item">
                            <el-icon><Files /></el-icon>
                            ID：{{ ref.metadata.document_id.substring(0, 8) }}...
                          </span>
                        </template>
                        <template v-if="ref.score !== undefined">
                          <span class="metadata-item">
                            <el-icon><Star /></el-icon>
                            相关度：{{ (ref.score * 100).toFixed(1) }}%
                          </span>
                        </template>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 物料搜索结果 -->
              <div v-else-if="message.contentType === 'materials'" class="materials-result">
                <div class="materials-header">
                  <el-icon><Search /></el-icon>
                  <span>识别到以下物料：</span>
                </div>

                <div class="materials-list">
                  <div
                    v-for="(material, idx) in message.content"
                    :key="idx"
                    class="material-item"
                    @click="viewMaterialDetail(material)"
                  >
                    <div class="material-preview">
                      <div class="material-number">{{ material.number }}</div>
                      <div class="material-name">{{ material.name }}</div>
                      <div class="material-spec">{{ material.specification }}</div>
                      <el-tag size="mini" type="success" class="score-tag">
                        {{ (material.score * 100).toFixed(0) }}% 匹配
                      </el-tag>
                    </div>

                    <div v-if="material.image" class="material-thumbnail">
                      <img :src="material.image" :alt="material.name" />
                    </div>
                  </div>
                </div>

                <div class="materials-footer">
                  <span>共找到 {{ message.content.length }} 个物料</span>
                </div>
              </div>
              <div class="timestamp">{{ message.timestamp }}</div>
            </div>
          </div>
        </div>

        <!-- 加载指示器 -->
        <div v-if="loading" class="loading-indicator">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>AI 正在思考...</span>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <div class="input-tools">
          <el-upload
            action="#"
            :show-file-list="false"
            :before-upload="handleImageUpload"
            accept="image/*"
            :disabled="loading"
          >
            <el-button size="small" type="info" plain circle :disabled="loading">
              <el-icon><Picture /></el-icon>
            </el-button>
          </el-upload>

          <el-tooltip content="清空对话" placement="top">
            <el-button size="small" type="info" plain circle @click="clearMessages">
              <el-icon><Delete /></el-icon>
            </el-button>
          </el-tooltip>
        </div>

        <div class="input-container">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="1"
            :autosize="{ minRows: 1, maxRows: 4 }"
            placeholder="输入您的问题..."
            @keydown.enter.exact.prevent="sendMessage"
            @keyup.enter.exact="clearEnter"
            :disabled="loading"
            resize="none"
            class="message-input"
          />
          <el-button
            type="primary"
            @click="sendMessage"
            :loading="loading"
            :disabled="!inputMessage.trim()"
            class="send-btn"
            circle
          >
            <el-icon><Promotion /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ChatDotRound,
  Picture,
  Promotion,
  Search,
  Loading,
  Minus,
  Close,
  Delete,
  Document,
  Reading,
  Star,
  Folder,
  Files
} from '@element-plus/icons-vue'
import { chat, uploadImage } from '@/api/dify/chat'

// 响应式数据
const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const messagesContainer = ref(null)
const conversationId = ref(null)
const isWindowVisible = ref(false)
const isMinimized = ref(false)
const isOnline = ref(true)
const unreadCount = ref(0)
const lastScrollTop = ref(0)

// 初始化
onMounted(() => {
  // 从本地存储恢复聊天记录
  const savedMessages = localStorage.getItem('dify_chat_messages')
  if (savedMessages) {
    messages.value = JSON.parse(savedMessages)
  }

  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
  handleResize()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

// 处理窗口大小变化
const handleResize = () => {
  // 可以根据窗口大小调整聊天窗口位置
  const chatWindow = document.querySelector('.chat-window-container')
  if (chatWindow && isWindowVisible.value && !isMinimized.value) {
    const windowHeight = window.innerHeight
    chatWindow.style.maxHeight = `${windowHeight - 100}px`
  }
}

// 切换聊天窗口显示
const toggleChatWindow = () => {
  isWindowVisible.value = !isWindowVisible.value
  if (isWindowVisible.value) {
    unreadCount.value = 0
    isMinimized.value = false
    nextTick(() => {
      scrollToBottom()
    })
  }
}

// 关闭窗口
const closeWindow = () => {
  isWindowVisible.value = false
  isMinimized.value = false
}

// 切换最小化
const toggleMinimize = () => {
  isMinimized.value = !isMinimized.value
  if (!isMinimized.value) {
    unreadCount.value = 0
    nextTick(() => {
      scrollToBottom()
    })
  }
}

// 发送消息
const sendMessage = async () => {
  const text = inputMessage.value.trim()
  if (!text) return

  // 添加用户消息
  addMessage({
    type: 'user',
    contentType: 'text',
    content: text
  })

  inputMessage.value = ''
  loading.value = true

  try {
    const requestData = {
      query: text,
      conversation_id: conversationId.value
    }

    const response = await chat(requestData)

    if (response.code === 200) {
      const result = response.data
      conversationId.value = result.conversation_id || conversationId.value

      const answer = result.answer || result.data?.answer || '收到回复，但内容为空。'

      // 提取 retriever_resources 数据 - 检查不同可能的路径
      let references = []
      if (result.retriever_resources) {
        references = result.retriever_resources
      } else if (result.data?.retriever_resources) {
        references = result.data.retriever_resources
      } else if (result.metadata?.retriever_resources) {
        references = result.metadata.retriever_resources
      }

      console.log('提取的参考资源:', references) // 调试用

      addMessage({
        type: 'ai',
        contentType: 'text',
        content: answer,
        references: references.map(ref => ({
          content: ref.content || ref.text || '',
          metadata: ref.metadata || {},
          score: ref.score || 0
        }))
      })

      // 保存到本地存储
      saveMessages()
    } else {
      throw new Error(response.msg || '请求失败')
    }
  } catch (error) {
    console.error('发送消息失败:', error)

    addMessage({
      type: 'ai',
      contentType: 'text',
      content: '抱歉，处理您的请求时出错了。错误信息：' + error.message
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

// 快速问题
const sendQuickQuestion = (question) => {
  inputMessage.value = question
  sendMessage()
}

// 处理图片上传
const handleImageUpload = async (file) => {
  // 检查文件大小
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过10MB')
    return false
  }

  // 添加用户消息
  addMessage({
    type: 'user',
    contentType: 'image',
    content: URL.createObjectURL(file)
  })

  loading.value = true

  try {
    const response = await uploadImage(file)

    if (response.code === 200) {
      addMessage({
        type: 'ai',
        contentType: 'materials',
        content: response.data
      })
      saveMessages()
    } else {
      throw new Error(response.msg || '图片识别失败')
    }
  } catch (error) {
    console.error('图片上传失败:', error)

    addMessage({
      type: 'ai',
      contentType: 'text',
      content: '抱歉，图片识别失败。请确保图片清晰且包含物料信息。'
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }

  return false
}

// 查看物料详情
const viewMaterialDetail = (material) => {
  ElMessageBox.alert(
    `
    <div class="material-detail">
      <h4>物料详情</h4>
      <p><strong>物料编码：</strong>${material.number}</p>
      <p><strong>物料名称：</strong>${material.name}</p>
      <p><strong>规格型号：</strong>${material.specification}</p>
      <p><strong>外观：</strong>${material.appearance}</p>
      <p><strong>核心描述：</strong>${material.description1}</p>
      <p><strong>匹配度：</strong>${(material.score * 100).toFixed(1)}%</p>
      ${material.image ? `<img src="${material.image}" style="max-width: 200px; margin-top: 10px;" />` : ''}
    </div>
    `,
    '物料详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭'
    }
  )
}

// 清空消息
const clearMessages = () => {
  ElMessageBox.confirm('确定要清空当前对话吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    messages.value = []
    conversationId.value = null
    localStorage.removeItem('dify_chat_messages')
    ElMessage.success('对话已清空')
  })
}

// 添加消息
const addMessage = (message) => {
  messages.value.push({
    ...message,
    timestamp: new Date().toLocaleTimeString('zh-CN', {
      hour12: false,
      hour: '2-digit',
      minute: '2-digit'
    })
  })

  // 如果窗口最小化或隐藏，增加未读计数
  if (isMinimized.value || !isWindowVisible.value) {
    unreadCount.value++
  }

  scrollToBottom()
}

// 滚动处理
const handleScroll = () => {
  if (messagesContainer.value) {
    lastScrollTop.value = messagesContainer.value.scrollTop
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// 保存消息到本地存储
const saveMessages = () => {
  try {
    // 只保存最近50条消息
    const recentMessages = messages.value.slice(-50)
    localStorage.setItem('dify_chat_messages', JSON.stringify(recentMessages))
  } catch (error) {
    console.error('保存消息失败:', error)
  }
}

// 清除输入框的换行符
const clearEnter = (event) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    inputMessage.value = inputMessage.value.trim()
  }
}

// 格式化参考内容
const formatReferenceContent = (ref) => {
  if (!ref) return ''

  let content = ref.content || ''

  // 如果内容过长，截断显示
  if (content.length > 200) {
    content = content.substring(0, 200) + '...'
  }

  return formatText(content)
}

// 格式化文本
const formatText = (text) => {
  if (!text) return ''
  return text
    .replace(/\n/g, '<br>')
    .replace(/\s{2,}/g, ' &nbsp;')
    .replace(/(https?:\/\/[^\s]+)/g, '<a href="$1" target="_blank">$1</a>')
    // 添加高亮关键词处理
    .replace(/<em>(.*?)<\/em>/g, '<span class="highlight">$1</span>')
    .replace(/<strong>(.*?)<\/strong>/g, '<span class="highlight">$1</span>')
    .replace(/<b>(.*?)<\/b>/g, '<span class="highlight">$1</span>')
}
</script>

<style scoped>
/* 悬浮聊天按钮 */
.floating-chat-button {
  position: fixed;
  bottom: 30px;
  left: 30px;
  z-index: 9998;
  background: linear-gradient(135deg, #409eff, #67c23a);
  color: white;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;
  animation: float 3s ease-in-out infinite;
}

.floating-chat-button:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 25px rgba(64, 158, 255, 0.4);
}

.chat-button-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.chat-icon {
  font-size: 24px;
}

.button-text {
  font-size: 12px;
  margin-top: 2px;
}

.unread-badge {
  position: absolute;
  top: -8px;
  right: -8px;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

/* 聊天窗口容器 */
.chat-window-container {
  position: fixed;
  bottom: 30px;
  left: 30px;
  width: 420px;
  max-height: 600px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  z-index: 9999;
  display: none;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #e4e7ed;
}

.chat-window-container.visible {
  display: flex;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 窗口标题栏 */
.chat-header {
  background: linear-gradient(135deg, #409eff, #67c23a);
  color: white;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: move;
  user-select: none;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.ai-icon {
  font-size: 18px;
}

.title {
  font-weight: 600;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 4px;
}

.header-actions .el-button {
  color: white;
}

.header-actions .el-button:hover {
  background: rgba(255, 255, 255, 0.2);
}

/* 最小化状态 */
.chat-window-container.minimized {
  width: 200px;
  height: 40px;
}

.minimized-content {
  background: linear-gradient(135deg, #409eff, #67c23a);
  color: white;
  padding: 8px 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
}

.minimized-badge {
  margin-left: auto;
}

/* 聊天内容区域 */
.chat-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* 欢迎区域 */
.welcome-section {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #f0f0f0;
}

.welcome-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 12px;
}

.welcome-section h3 {
  margin: 0 0 12px 0;
  color: #333;
}

.welcome-section p {
  margin: 0 0 12px 0;
  color: #666;
}

.welcome-features {
  text-align: left;
  margin: 12px 0;
  padding-left: 20px;
  color: #666;
}

.welcome-features li {
  margin-bottom: 4px;
}

.quick-questions {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 消息区域 */
.chat-messages {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  max-height: 400px;
  background-color: #fafafa;
}

.message {
  margin-bottom: 16px;
}

.message-content {
  display: flex;
  gap: 8px;
}

.message-content.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
  margin-top: 4px;
}

.message-content.ai .avatar {
  background: #67c23a;
}

.bubble {
  max-width: 75%;
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
}

.message-content.user .bubble {
  background: #409eff;
  color: white;
  border-bottom-right-radius: 4px;
}

.message-content.ai .bubble {
  background: white;
  color: #333;
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #e8e8e8;
}

.timestamp {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
  text-align: right;
}

.message-content.user .timestamp {
  color: rgba(255, 255, 255, 0.8);
}

/* 图片消息 */
.image-message img {
  max-width: 200px;
  max-height: 150px;
  border-radius: 6px;
  margin-bottom: 4px;
}

/* 参考来源样式 */
.references-section {
  margin-top: 16px;
  border-top: 1px solid #e8e8e8;
  padding-top: 12px;
  background-color: #f9fafc;
  border-radius: 8px;
  padding: 12px;
  border: 1px solid #e8f4ff;
}

.references-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  color: #409eff;
  font-size: 13px;
  font-weight: 600;
}

.references-count {
  font-size: 12px;
  color: #67c23a;
  background: #f0f9eb;
  padding: 2px 6px;
  border-radius: 10px;
  margin-left: 4px;
}

.references-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.reference-item {
  margin-bottom: 8px;
  padding: 10px;
  background: white;
  border-radius: 6px;
  border-left: 3px solid #409eff;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  border: 1px solid #e4e7ed;
}

.reference-item:last-child {
  margin-bottom: 0;
}

.reference-content {
  font-size: 12px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 8px;
}

.reference-content :deep(a) {
  color: #409eff;
  text-decoration: none;
}

.reference-content :deep(a:hover) {
  text-decoration: underline;
}

/* 添加高亮样式 */
.reference-content :deep(.highlight) {
  background-color: #fffacd;
  color: #e6a23c;
  font-weight: 500;
  padding: 0 2px;
  border-radius: 2px;
}

.reference-metadata {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 10px;
  color: #999;
  padding-top: 6px;
  border-top: 1px dashed #eee;
}

.metadata-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-right: 12px;
  margin-top: 4px;
  font-size: 11px;
  color: #666;
}

.metadata-item .el-icon {
  font-size: 10px;
}

/* 物料搜索结果 */
.materials-result {
  max-width: 100%;
}

.materials-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  color: #409eff;
  font-size: 13px;
  font-weight: 500;
}

.materials-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.material-item {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 8px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.material-item:hover {
  border-color: #409eff;
  background: #f5f9ff;
}

.material-preview {
  flex: 1;
}

.material-number {
  font-weight: bold;
  color: #409eff;
  font-size: 12px;
}

.material-name {
  font-size: 12px;
  color: #333;
  margin: 2px 0;
}

.material-spec {
  font-size: 11px;
  color: #666;
}

.score-tag {
  margin-top: 4px;
}

.material-thumbnail img {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  object-fit: cover;
}

.materials-footer {
  margin-top: 8px;
  text-align: center;
  color: #999;
  font-size: 12px;
}

/* 加载指示器 */
.loading-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #999;
  font-size: 13px;
  padding: 8px;
  justify-content: center;
}

/* 输入区域 */
.input-area {
  padding: 12px 16px;
  border-top: 1px solid #e4e7ed;
  background: #fafafa;
}

.input-tools {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.input-container {
  display: flex;
  gap: 8px;
  align-items: flex-end;
}

.message-input {
  flex: 1;
}

.message-input :deep(.el-textarea__inner) {
  border-radius: 18px;
  padding: 8px 16px;
  font-size: 14px;
  background: white;
}

.send-btn {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-window-container {
    width: 100vw;
    height: 100vh;
    max-height: 100vh;
    bottom: 0;
    left: 0;
    border-radius: 0;
  }

  .chat-window-container:not(.minimized) {
    width: 100vw;
    height: 100vh;
  }

  .chat-messages {
    max-height: calc(100vh - 180px);
  }

  .floating-chat-button {
    bottom: 20px;
    left: 20px;
    width: 50px;
    height: 50px;
  }

  .bubble {
    max-width: 85%;
  }
}
</style>
