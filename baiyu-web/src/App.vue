
<template>
  <router-view />
  <!-- 使用正确的组件名称 -->
  <DifyChatModal />
</template>
<script setup>
import { onMounted, nextTick } from 'vue'
import useSettingsStore from '@/store/modules/settings'
import { handleThemeStyle } from '@/utils/theme'

onMounted(() => {
  nextTick(() => {
    // 初始化主题样式
    handleThemeStyle(useSettingsStore().theme)
  })
})
</script>

<style>
.anchorBL {
  display: none !important;
}

/* 确保聊天组件不被其他元素遮挡 */
#app {
  position: relative;
}

/* 给聊天组件留出空间 */
@media (max-width: 768px) {
  .dify-chat-trigger,
  .minimized-chat {
    left: 16px;
    bottom: 16px;
  }
}

</style>
