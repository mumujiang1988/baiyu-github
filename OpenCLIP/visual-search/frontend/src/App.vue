<template>
  <div id="app">
    <el-container>
      <el-header>
        <div class="header-content">
          <h1>以图搜品系统</h1>
          <el-menu mode="horizontal" :default-active="activeMenu" @select="handleMenuSelect">
            <el-menu-item index="search">图像检索</el-menu-item>
            <el-menu-item index="text-search">文本搜索</el-menu-item>
            <el-menu-item index="ingest">产品入库</el-menu-item>
            <el-menu-item index="products">产品管理</el-menu-item>
          </el-menu>
        </div>
      </el-header>
      
      <el-main>
        <ImageSearch v-if="activeMenu === 'search'" />
        <TextSearch v-if="activeMenu === 'text-search'" />
        <ProductIngest v-if="activeMenu === 'ingest'" />
        <ProductList v-if="activeMenu === 'products'" />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import ImageSearch from './components/ImageSearch.vue'
import TextSearch from './components/TextSearch.vue'
import ProductIngest from './components/ProductIngest/index.vue'
import ProductList from './components/ProductList.vue'

const activeMenu = ref('search')

const handleMenuSelect = (index) => {
  activeMenu.value = index
}
</script>

<style>
#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  height: 100%;
  width: 100%;
}

.el-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.el-header {
  background-color: #409EFF;
  color: white;
  padding: 0 20px;
  height: 60px;
  flex-shrink: 0;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
}

.header-content h1 {
  margin: 0;
  font-size: 24px;
}

.el-menu--horizontal {
  background-color: transparent;
  border-bottom: none;
}

.el-menu--horizontal .el-menu-item {
  color: white;
}

.el-menu--horizontal .el-menu-item:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.el-menu--horizontal .el-menu-item.is-active {
  background-color: rgba(255, 255, 255, 0.2);
  border-bottom: 2px solid white;
}

.el-main {
  padding: 20px;
  background-color: #f5f7fa;
  height: calc(100vh - 60px);
  overflow-y: auto;
  overflow-x: hidden;
  flex: 1;
  min-height: 0;
}
</style>
