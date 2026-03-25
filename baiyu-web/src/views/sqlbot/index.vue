<!-- src/views/sqlbot/index.vue -->
<template>
  <div class="app-container">
    <div class="sqlbot-container">
      <iframe
          :src="sqlbotUrl"
          frameborder="0"
          width="100%"
          height="800px"
          ref="sqlbotFrame"
      ></iframe>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const sqlbotUrl = ref('http://118.178.144.159:8000/#/ds/index')

const sqlbotFrame = ref(null)

// 自动登录逻辑（如果需要）
const autoLogin = () => {
  const iframe = sqlbotFrame.value
  if (iframe) {
    // 等待iframe加载完成后发送登录信息
    iframe.onload = () => {
      const loginData = {
        username: 'admin',
        password: 'SQLBot@123456'
      }
      // 注意：跨域限制下可能需要SQLBot支持postMessage通信
      iframe.contentWindow.postMessage(loginData, '*')
    }
  }
}

onMounted(() => {
  autoLogin()
})
</script>

<style scoped>
.sqlbot-container {
  width: 100%;
  height: calc(100vh - 84px);
  overflow: hidden;
}
</style>
