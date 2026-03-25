<!-- src/views/your-page/index.vue -->
<template>
  <div class="page-container">
    <!-- 添加加载状态 -->
    <el-skeleton v-if="loading" :rows="6" animated />

    <!-- SQLBot 容器 -->
    <div v-show="!loading" ref="sqlbotContainer" class="sqlbot-container">
      <div class="copilot"></div>
    </div>

    <!-- 错误提示 -->
    <el-alert
      v-if="error"
      :title="error"
      type="error"
      show-icon
      @close="error = ''"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(true)
const error = ref('')
const sqlbotContainer = ref(null)

let script = null
let timer = null

// 加载 SQLBot 脚本
const loadSQLBotScript = () => {
  return new Promise((resolve) => {
    if (window.sqlbot_embedded_handler) {
      resolve(true)
      return
    }

    script = document.createElement('script')
    script.defer = true
    script.async = true
    script.src = 'http://118.178.144.159:8000/xpack_static/sqlbot-embedded-dynamic.umd.js'
    script.onload = () => resolve(true)
    script.onerror = () => {
      error.value = 'SQLBot 脚本加载失败'
      resolve(false)
    }
    document.head.appendChild(script)
  })
}

// 初始化 SQLBot
const initSQLBot = () => {
  return new Promise((resolve) => {
    if (!window.sqlbot_embedded_handler?.mounted) {
      timer = setInterval(() => {
        if (window.sqlbot_embedded_handler?.mounted) {
          try {
            // 确保容器已渲染
            if (sqlbotContainer.value) {
              window.sqlbot_embedded_handler.mounted('.copilot', {
                embeddedId: '7420702230258388992'
              })
              loading.value = false
              clearInterval(timer)
              resolve(true)
            } else {
              error.value = 'SQLBot 容器未找到'
              resolve(false)
            }
          } catch (err) {
            error.value = 'SQLBot 初始化失败: ' + err.message
            resolve(false)
          }
        }

        // 设置超时
        setTimeout(() => {
          if (timer) {
            clearInterval(timer)
            error.value = 'SQLBot 初始化超时'
            resolve(false)
          }
        }, 10000)
      }, 1000)
    }
  })
}

// 初始化页面
const initPage = async () => {
  try {
    loading.value = true

    // 等待 DOM 渲染完成
    await nextTick()

    // 加载脚本
    const scriptLoaded = await loadSQLBotScript()
    if (!scriptLoaded) return

    // 初始化 SQLBot
    await initSQLBot()
  } catch (err) {
    error.value = '页面初始化失败: ' + err.message
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 延迟初始化，确保所有依赖都已加载
  setTimeout(() => {
    initPage()
  }, 500)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (script) {
    document.head.removeChild(script)
  }
})
</script>

<style scoped>
.page-container {
  width: 100%;
  height: 100%;
  min-height: 500px;
}

.sqlbot-container {
  width: 100%;
  height: 600px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}
</style>
