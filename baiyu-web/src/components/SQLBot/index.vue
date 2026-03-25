<!-- src/components/SQLBot/index.vue -->
<template>
  <div class="copilot-container">
    <div ref="copilotRef" class="copilot"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  embeddedId: {
    type: String,
    default: '7420700770393133056'
  },
  scriptUrl: {
    type: String,
    default: 'http://118.178.144.159:8000/xpack_static/sqlbot-embedded-dynamic.umd.js'
  }
})

const copilotRef = ref(null)
let script = null
let timer = null

// 加载脚本
const loadScript = () => {
  return new Promise((resolve) => {
    // 检查是否已加载
    if (window.sqlbot_embedded_handler) {
      resolve(true)
      return
    }

    script = document.createElement('script')
    script.defer = true
    script.async = true
    script.src = props.scriptUrl
    script.onload = () => resolve(true)
    script.onerror = () => resolve(false)

    document.head.appendChild(script)
  })
}

// 初始化 SQLBot
const initSQLBot = () => {
  return new Promise((resolve) => {
    if (!window.sqlbot_embedded_handler?.mounted) {
      timer = setInterval(() => {
        if (window.sqlbot_embedded_handler?.mounted) {
          window.sqlbot_embedded_handler.mounted('.copilot', {
            embeddedId: props.embeddedId
          })
          clearInterval(timer)
          timer = null
          resolve(true)
        }
      }, 1000)
    } else {
      window.sqlbot_embedded_handler.mounted('.copilot', {
        embeddedId: props.embeddedId
      })
      resolve(true)
    }
  })
}

// 暴露方法
defineExpose({
  loadAndInit: async () => {
    const loaded = await loadScript()
    if (loaded) {
      await initSQLBot()
    }
  }
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
  if (script) {
    document.head.removeChild(script)
  }
})
</script>

<style scoped>
.copilot-container {
  width: 100%;
  height: 100%;
}
</style>
