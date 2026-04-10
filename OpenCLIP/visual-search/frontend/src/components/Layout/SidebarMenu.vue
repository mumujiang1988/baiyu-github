<template>
  <div class="sidebar-menu">
    <!-- Logo区域 -->
    <div class="logo-container">
      <div class="logo-icon">
        <el-icon :size="28"><Search /></el-icon>
      </div>
      <div class="logo-text">
        <h2>以图搜品</h2>
        <span class="subtitle">Visual Search</span>
      </div>
    </div>

    <!-- 菜单列表 -->
    <el-menu
      :default-active="activeMenu"
      class="menu-list"
      @select="handleMenuSelect"
    >
      <el-menu-item
        v-for="item in menuItems"
        :key="item.path"
        :index="item.path"
      >
        <el-icon><component :is="item.icon" /></el-icon>
        <span>{{ item.title }}</span>
      </el-menu-item>
    </el-menu>

    <!-- 底部信息 -->
    <div class="sidebar-footer">
      <el-divider />
      <div class="version-info">
        <el-icon><InfoFilled /></el-icon>
        <span>v1.0.0</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search, Upload, Management, InfoFilled } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 菜单配置
const menuItems = [
  {
    path: '/search',
    title: '图像检索',
    icon: 'Search'
  },
  {
    path: '/ingest',
    title: '产品入库',
    icon: 'Upload'
  },
  {
    path: '/products',
    title: '产品管理',
    icon: 'Management'
  }
]

// 菜单选择处理
const handleMenuSelect = (index) => {
  router.push(index)
}
</script>

<style scoped>
.sidebar-menu {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #1E293B;
  overflow: hidden;
}

/* Logo区域 */
.logo-container {
  padding: 28px 24px;
  display: flex;
  align-items: center;
  gap: 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(0, 0, 0, 0.15);
}

.logo-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #165DFF 0%, #0E42D2 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(22, 93, 255, 0.3);
  transition: all 0.3s ease;
}

.logo-icon:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(22, 93, 255, 0.4);
}

.logo-text {
  flex: 1;
}

.logo-text h2 {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
  color: #ffffff;
  letter-spacing: 0.3px;
}

.subtitle {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
  text-transform: uppercase;
  letter-spacing: 1.2px;
  margin-top: 2px;
  display: block;
}

/* 菜单列表 */
.menu-list {
  flex: 1;
  border-right: none;
  background: transparent;
  padding: 16px 12px;
  overflow-y: auto;
}

:deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
  margin: 6px 0;
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.65);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 14px;
  font-weight: 500;
}

:deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08) !important;
  color: #ffffff;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(22, 93, 255, 0.2) 0%, rgba(14, 66, 210, 0.15) 100%) !important;
  color: #ffffff;
  position: relative;
  font-weight: 600;
}

:deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 24px;
  background: linear-gradient(180deg, #165DFF 0%, #0E42D2 100%);
  border-radius: 0 2px 2px 0;
}

:deep(.el-menu-item .el-icon) {
  margin-right: 12px;
  font-size: 18px;
  transition: transform 0.25s ease;
}

:deep(.el-menu-item:hover .el-icon) {
  transform: scale(1.1);
}

/* 底部信息 */
.sidebar-footer {
  padding: 16px 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(0, 0, 0, 0.15);
}

:deep(.el-divider) {
  margin: 0 0 12px 0;
  border-color: rgba(255, 255, 255, 0.08);
}

.version-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
  font-weight: 500;
}

.version-info .el-icon {
  font-size: 14px;
}
</style>
