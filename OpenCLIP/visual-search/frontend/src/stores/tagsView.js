import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { logger } from '../utils/logger'

// 常量定义
const MAX_TAGS = 15 // 最大标签数

/**
 * 页签视图状态管理
 * 支持多页面页签、保持激活状态、关闭页签等功能
 */
export const useTagsViewStore = defineStore('tagsView', () => {
  // State
  const visitedViews = ref([]) // 已访问的页签列表
  const cachedViews = ref([])  // 缓存的组件名称（用于 keep-alive）

  // Getters
  const hasVisitedViews = computed(() => visitedViews.value.length > 0)

  // Actions
  
  /**
   * 添加页签（带数量限制）
   */
  function addView(view) {
    if (!view || !view.path) {
      logger.warn('[TagsView] Invalid view:', view)
      return
    }
    
    addVisitedView(view)
    addCachedView(view)
  }

  /**
   * 添加已访问页签
   */
  function addVisitedView(view) {
    // 检查是否已存在
    const index = visitedViews.value.findIndex(v => v.path === view.path)
    
    if (index !== -1) {
      // 更新现有页签，保留原有 title
      visitedViews.value[index] = {
        ...visitedViews.value[index],
        ...view,
        title: view.meta?.title || visitedViews.value[index].title || '未命名',
        fullPath: view.fullPath || visitedViews.value[index].fullPath
      }
    } else {
      // 限制最大标签数
      if (visitedViews.value.length >= MAX_TAGS) {
        // 移除第一个非固定标签
        const firstNonAffixIndex = visitedViews.value.findIndex(v => !v.meta?.affix)
        if (firstNonAffixIndex !== -1) {
          const removed = visitedViews.value.splice(firstNonAffixIndex, 1)[0]
          delCachedViewByName(removed.name)
        }
      }
      
      // 添加新页签
      visitedViews.value.push({
        ...view,
        title: view.meta?.title || '未命名',
        name: view.name,
        fullPath: view.fullPath
      })
    }
  }

  /**
   * 添加缓存视图
   */
  function addCachedView(view) {
    if (!view?.name) return
    
    if (!cachedViews.value.includes(view.name)) {
      cachedViews.value.push(view.name)
    }
  }
  
  function delCachedViewByName(name) {
    const index = cachedViews.value.indexOf(name)
    if (index !== -1) {
      cachedViews.value.splice(index, 1)
    }
  }

  /**
   * 删除页签
   */
  function delView(view) {
    delVisitedView(view)
    delCachedView(view)
  }

  /**
   * 删除已访问页签
   */
  function delVisitedView(view) {
    const index = visitedViews.value.findIndex(v => v.path === view.path)
    if (index !== -1) {
      visitedViews.value.splice(index, 1)
    }
  }

  /**
   * 删除缓存视图
   */
  function delCachedView(view) {
    if (!view?.name) return
    delCachedViewByName(view.name)
  }

  /**
   * 删除其他页签
   */
  function delOthersViews(view) {
    delOthersVisitedViews(view)
    delOthersCachedViews(view)
  }

  /**
   * 删除其他已访问页签（保留固定的）
   */
  function delOthersVisitedViews(view) {
    visitedViews.value = visitedViews.value.filter(v => 
      v.path === view.path || v.meta?.affix
    )
  }

  /**
   * 删除其他缓存视图
   */
  function delOthersCachedViews(view) {
    if (!view?.name) return
    
    cachedViews.value = cachedViews.value.filter(name => 
      name === view.name
    )
  }

  /**
   * 删除所有页签
   */
  function delAllViews() {
    delAllVisitedViews()
    delAllCachedViews()
  }

  /**
   * 删除所有已访问页签（保留固定的）
   */
  function delAllVisitedViews() {
    visitedViews.value = visitedViews.value.filter(v => v.meta?.affix)
  }

  /**
   * 删除所有缓存视图
   */
  function delAllCachedViews() {
    cachedViews.value = []
  }

  /**
   * 更新页签标题
   */
  function updateVisitedView(view) {
    if (!view?.path) return
    
    const index = visitedViews.value.findIndex(v => v.path === view.path)
    if (index !== -1) {
      visitedViews.value[index] = { ...visitedViews.value[index], ...view }
    }
  }
  
  /**
   * 持久化到 localStorage
   */
  function persistToStorage() {
    try {
      localStorage.setItem('visitedViews', JSON.stringify(visitedViews.value))
    } catch (e) {
      logger.warn('[TagsView] Failed to persist:', e)
    }
  }
  
  /**
   * 从 localStorage 恢复
   */
  function restoreFromStorage() {
    try {
      const saved = localStorage.getItem('visitedViews')
      if (saved) {
        const views = JSON.parse(saved)
        // 只恢复固定的标签
        const affixViews = views.filter(v => v.meta?.affix)
        if (affixViews.length > 0) {
          visitedViews.value = affixViews
          // 同步缓存
          cachedViews.value = visitedViews.value.map(v => v.name).filter(Boolean)
        }
      }
    } catch (e) {
      logger.warn('[TagsView] Failed to restore:', e)
    }
  }

  return {
    // State
    visitedViews,
    cachedViews,
    
    // Getters
    hasVisitedViews,
    
    // Actions
    addView,
    addVisitedView,
    addCachedView,
    delView,
    delVisitedView,
    delCachedView,
    delOthersViews,
    delOthersVisitedViews,
    delOthersCachedViews,
    delAllViews,
    delAllVisitedViews,
    delAllCachedViews,
    updateVisitedView,
    persistToStorage,
    restoreFromStorage
  }
})
