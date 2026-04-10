<template>
  <div class="tags-view">
    <el-scrollbar ref="scrollContainer" class="tags-scrollbar">
      <div class="tags-container" @wheel.prevent="handleScroll">
        <router-link
          v-for="tag in tagsViewStore.visitedViews"
          :key="tag.path"
          :to="tag.fullPath || tag.path"
          custom
          v-slot="{ navigate, isExactActive }"
        >
          <div
            class="tag-item"
            :class="{ 'active': isExactActive }"
            @click="navigate"
            @click.middle.prevent="closeTag(tag)"
          >
            <span class="tag-title">{{ tag.title }}</span>
            <el-icon
              v-if="!tag.meta?.affix"
              class="close-icon"
              @click.prevent.stop="closeTag(tag)"
            >
              <Close />
            </el-icon>
          </div>
        </router-link>
      </div>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Close } from '@element-plus/icons-vue'
import { useTagsViewStore } from '@/stores/tagsView'

const route = useRoute()
const router = useRouter()
const scrollContainer = ref(null)
const tagsViewStore = useTagsViewStore()

// 监听路由变化，自动添加标签
watch(
  () => route.path,
  () => {
    if (route.name) {
      tagsViewStore.addView(route)
      tagsViewStore.persistToStorage()
    }
  },
  { immediate: true }
)

/**
 * 关闭标签
 */
function closeTag(tag) {
  const isCurrentActive = tag.path === router.currentRoute.value.path
  
  // 删除标签
  tagsViewStore.delView(tag)
  
  // 如果关闭的是当前激活的标签，跳转到最后一个
  if (isCurrentActive) {
    const views = tagsViewStore.visitedViews
    const latestView = views[views.length - 1]
    
    if (latestView) {
      router.push(latestView.fullPath || latestView.path)
    } else {
      router.push('/search')
    }
  }
}

/**
 * 滚轮横向滚动
 */
function handleScroll(e) {
  const container = scrollContainer.value?.wrapRef
  if (container) {
    const delta = e.wheelDelta || -e.deltaY * 40
    container.scrollLeft += delta / 4
  }
}
</script>

<style scoped>
.tags-view {
  height: 50px;
  background: #f8f9fa;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  padding: 0 20px;
  box-sizing: border-box;
  margin-top: 8px;
  margin-bottom: 4px;
}

.tags-scrollbar {
  height: 100%;
  flex: 1;
}

.tags-container {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 12px;
  white-space: nowrap;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  height: 36px;
  padding: 0 16px;
  margin-right: 8px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #ffffff;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  user-select: none;
  text-decoration: none;
  color: #5a6c7d;
  font-size: 13px;
  font-weight: 500;
  position: relative;
  overflow: hidden;
}

.tag-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(22, 93, 255, 0.05) 0%, rgba(14, 66, 210, 0.03) 100%);
  opacity: 0;
  transition: opacity 0.2s ease;
}

.tag-item:hover {
  border-color: #165DFF;
  color: #165DFF;
  box-shadow: 0 2px 6px rgba(22, 93, 255, 0.12);
}

.tag-item:hover::before {
  opacity: 1;
}

.tag-item.active {
  background: linear-gradient(135deg, #165DFF 0%, #0E42D2 100%);
  border-color: transparent;
  color: #ffffff;
  box-shadow: 0 2px 8px rgba(22, 93, 255, 0.25);
  font-weight: 600;
}

.tag-item.active::before {
  display: none;
}

.tag-title {
  margin-right: 6px;
  position: relative;
  z-index: 1;
}

.close-icon {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  font-size: 12px;
  transition: all 0.2s ease;
  position: relative;
  z-index: 1;
  opacity: 0.7;
}

.close-icon:hover {
  background: rgba(0, 0, 0, 0.08);
  opacity: 1;
}

.tag-item.active .close-icon {
  opacity: 0.9;
}

.tag-item.active .close-icon:hover {
  background: rgba(255, 255, 255, 0.25);
  opacity: 1;
}
</style>
