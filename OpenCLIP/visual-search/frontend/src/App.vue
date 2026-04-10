<template>
  <div id="app">
    <el-container class="app-container">
      <!-- 侧边栏 -->
      <el-aside width="240px" class="sidebar-wrapper">
        <SidebarMenu />
      </el-aside>

      <!-- 主内容区 -->
      <el-container class="main-container">
        <!-- 页签栏 -->
        <TagsView />

        <!-- 内容区 -->
        <el-main class="main-content">
          <router-view v-slot="{ Component }">
            <keep-alive :include="cachedViews">
              <component :is="Component" :key="route.fullPath" />
            </keep-alive>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useTagsViewStore } from '@/stores/tagsView'
import SidebarMenu from '@/components/Layout/SidebarMenu.vue'
import TagsView from '@/components/Layout/TagsView.vue'

const route = useRoute()
const tagsViewStore = useTagsViewStore()

// 缓存的视图列表
const cachedViews = computed(() => tagsViewStore.cachedViews)

// 初始化：从 localStorage 恢复标签
onMounted(() => {
  tagsViewStore.restoreFromStorage()
})
</script>

<style>
.app-container {
  height: 100%;
  display: flex;
}

/* 侧边栏 - 深 slate 灰 */
.sidebar-wrapper {
  height: 100%;
  background: #1E293B;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.08);
  z-index: 100;
  transition: all 0.3s ease;
}

/* 主容器 */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #F5F7FA;
  min-width: 0;
}

/* 顶部标签栏 */
.tags-header {
  height: 60px;
  background: #f8f9fa;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-sizing: border-box;
}

/* 主内容区 */
.main-content {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
  overflow-x: hidden;
  background: #F5F7FA;
}
</style>
