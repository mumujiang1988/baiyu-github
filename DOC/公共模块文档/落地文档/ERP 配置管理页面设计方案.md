# ERP 配置管理页面设计方案

> 📅 **版本**: v1.0  
> 🎯 **目标**: 基于前后端配置化方案，设计统一的配置管理界面  
> 📦 **适用范围**: RuoYi-WMS + Vue 3 + Element Plus  
> 🕐 **创建时间**: 2026-03-22

---

## 📋 目录

1. [页面架构设计](#页面架构设计)
2. [配置列表管理页面](#配置列表管理页面)
3. [配置编辑器页面](#配置编辑器页面)
4. [配置历史版本页面](#配置历史版本页面)
5. [API 接口设计](#api 接口设计)
6. [实施步骤](#实施步骤)

---

## 🏗️ 页面架构设计

### 整体布局

```
┌─────────────────────────────────────────────────────┐
│              配置管理平台                            │
├─────────────────────────────────────────────────────┤
│  侧边栏导航  │  主内容区                            │
│             │                                       │
│ • 配置列表   │  ┌─────────────────────────────┐    │
│ • 新增配置   │  │  配置列表/详情/编辑器        │    │
│ • 历史记录   │  │                             │    │
│ • 模板管理   │  └─────────────────────────────┘    │
│ • 系统设置   │                                       │
└─────────────┴───────────────────────────────────────┘
```

### 文件结构

```
views/erp/config/
├── index.vue              # 配置列表管理页面
├── editor.vue             # 配置编辑器页面
├── history.vue            # 配置历史版本页面
├── template.vue           # 配置模板管理页面
└── components/
    ├── ConfigList.vue     # 配置列表组件
    ├── ConfigEditor.vue   # 配置编辑器组件
    ├── JsonEditor.vue     # JSON 编辑器组件
    ├── VersionHistory.vue # 版本历史组件
    └── PreviewPanel.vue   # 配置预览面板
```

---

## 📝 配置列表管理页面

### 页面功能

**主要功能**:
- ✅ 配置列表展示（分页）
- ✅ 搜索和筛选
- ✅ 新增/编辑/删除配置
- ✅ 查看配置详情
- ✅ 版本历史管理
- ✅ 启用/停用配置

### 核心代码结构

```vue
<!-- views/erp/config/index.vue -->
<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" :inline="true">
      <el-form-item label="模块编码">
        <el-input v-model="queryParams.moduleCode" placeholder="请输入模块编码" />
      </el-form-item>
      <el-form-item label="配置名称">
        <el-input v-model="queryParams.configName" placeholder="请输入配置名称" />
      </el-form-item>
      <el-form-item label="配置类型">
        <el-select v-model="queryParams.configType" placeholder="请选择配置类型">
          <el-option label="页面配置" value="PAGE" />
          <el-option label="字典配置" value="DICT" />
          <el-option label="下推配置" value="PUSH" />
          <el-option label="审批配置" value="APPROVAL" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格列表 -->
    <el-table v-loading="loading" :data="configList">
      <el-table-column label="序号" type="index" width="60" />
      <el-table-column prop="configName" label="配置名称" />
      <el-table-column prop="moduleCode" label="模块编码" />
      <el-table-column prop="configType" label="配置类型">
        <template #default="scope">
          <el-tag>{{ getConfigTypeLabel(scope.row.configType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="version" label="版本号" />
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <el-tag :type="scope.row.status === '1' ? 'success' : 'danger'">
            {{ scope.row.status === '1' ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280">
        <template #default="scope">
          <el-button link type="primary" @click="handleView(scope.row)">查看</el-button>
          <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button link type="primary" @click="handleHistory(scope.row)">历史</el-button>
          <el-dropdown>
            <el-button link>更多</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleCopy(scope.row)">复制</el-dropdown-item>
                <el-dropdown-item @click="handleExport(scope.row)">导出</el-dropdown-item>
                <el-dropdown-item @click="handleDelete(scope.row)">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination v-show="total > 0" v-model:page="pageNum" v-model:limit="pageSize" :total="total" />
  </div>
</template>

<script setup name="ErpConfig">
import { listConfig, delConfig } from '@/api/erp/config'
import { ref, reactive } from 'vue'

const loading = ref(false)
const configList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const queryParams = reactive({
  moduleCode: '',
  configName: '',
  configType: '',
  status: ''
})

function getList() {
  loading.value = true
  listConfig(queryParams).then(res => {
    configList.value = res.rows
    total.value = res.total
  }).finally(() => {
    loading.value = false
  })
}

function handleQuery() {
  pageNum.value = 1
  getList()
}

function resetQuery() {
  Object.keys(queryParams).forEach(key => {
    queryParams[key] = ''
  })
  getList()
}

function handleView(row) {
  // 查看详情
}

function handleEdit(row) {
  // 编辑配置
}

function handleHistory(row) {
  // 查看历史
}

function handleDelete(row) {
  // 删除配置
}

getList()
</script>
```

---

## ✍️ 配置编辑器页面

### 页面功能

**主要功能**:
- ✅ 可视化配置编辑
- ✅ JSON 代码编辑
- ✅ 实时预览
- ✅ 配置验证
- ✅ 保存配置

### 核心代码结构

```vue
<!-- views/erp/config/editor.vue -->
<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑配置' : '新增配置' }}</span>
          <div>
            <el-button @click="handlePreview">预览</el-button>
            <el-button type="primary" @click="handleSubmit">保存</el-button>
            <el-button @click="handleBack">返回</el-button>
          </div>
        </div>
      </template>

      <el-form :model="formData" label-width="120px">
        <el-row :gutter="20">
          <!-- 左侧：基本信息 -->
          <el-col :span="12">
            <el-form-item label="模块编码" prop="moduleCode">
              <el-input v-model="formData.moduleCode" :disabled="isEdit" />
            </el-form-item>
            <el-form-item label="配置名称" prop="configName">
              <el-input v-model="formData.configName" />
            </el-form-item>
            <el-form-item label="配置类型" prop="configType">
              <el-select v-model="formData.configType" :disabled="isEdit">
                <el-option label="页面配置" value="PAGE" />
                <el-option label="字典配置" value="DICT" />
                <el-option label="下推配置" value="PUSH" />
                <el-option label="审批配置" value="APPROVAL" />
              </el-select>
            </el-form-item>
            <el-form-item label="是否公共" prop="isPublic">
              <el-radio-group v-model="formData.isPublic">
                <el-radio label="1">是</el-radio>
                <el-radio label="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input v-model="formData.remark" type="textarea" :rows="3" />
            </el-form-item>
          </el-col>

          <!-- 右侧：配置内容 -->
          <el-col :span="12">
            <el-form-item label="配置内容">
              <div class="editor-actions">
                <el-button size="small" @click="formatJson">格式化</el-button>
                <el-button size="small" @click="validateJson">验证 JSON</el-button>
                <el-button size="small" @click="loadTemplate">加载模板</el-button>
              </div>
              <codemirror
                v-model="formData.configContent"
                :extensions="[json()]"
                :style="{ height: '700px' }"
              />
              <el-alert v-if="jsonError" type="error">{{ jsonError }}</el-alert>
              <el-alert v-else-if="jsonValid" type="success">JSON 格式正确</el-alert>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="变更原因">
          <el-input v-model="formData.changeReason" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup name="ErpConfigEditor">
import { getConfig, saveConfig } from '@/api/erp/config'
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Codemirror } from 'vue-codemirror'
import { json } from '@codemirror/lang-json'

const route = useRoute()
const router = useRouter()

const formData = reactive({
  configId: null,
  moduleCode: '',
  configName: '',
  configType: '',
  configContent: '',
  isPublic: '0',
  remark: '',
  changeReason: ''
})

const isEdit = ref(false)
const jsonValid = ref(false)
const jsonError = ref('')

function loadConfig() {
  const id = route.params.id
  if (id) {
    getConfig(id).then(res => {
      Object.assign(formData, res.data)
      isEdit.value = true
    })
  }
}

function formatJson() {
  try {
    const parsed = JSON.parse(formData.configContent)
    formData.configContent = JSON.stringify(parsed, null, 2)
    jsonValid.value = true
    jsonError.value = ''
  } catch (e) {
    jsonError.value = 'JSON 格式错误：' + e.message
  }
}

function validateJson() {
  try {
    JSON.parse(formData.configContent)
    jsonValid.value = true
    jsonError.value = ''
  } catch (e) {
    jsonError.value = 'JSON 格式错误：' + e.message
  }
}

function handleSubmit() {
  saveConfig(formData).then(() => {
    router.back()
  })
}

function handleBack() {
  router.back()
}

loadConfig()
</script>
```

---

## 📚 配置历史版本页面

### 页面功能

**主要功能**:
- ✅ 查看配置历史版本
- ✅ 版本对比
- ✅ 版本回滚
- ✅ 版本详情

### 核心代码结构

```vue
<!-- views/erp/config/history.vue -->
<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>配置历史版本</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>

      <el-alert title="当前配置信息">
        <el-descriptions :column="3">
          <el-descriptions-item label="配置名称">{{ currentConfig.configName }}</el-descriptions-item>
          <el-descriptions-item label="模块编码">{{ currentConfig.moduleCode }}</el-descriptions-item>
          <el-descriptions-item label="当前版本">
            <el-tag>v{{ currentConfig.version }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </el-alert>

      <el-table v-loading="loading" :data="versionList">
        <el-table-column label="序号" type="index" width="60" />
        <el-table-column prop="version" label="版本号">
          <template #default="scope">
            <el-tag :type="scope.row.version === currentConfig.version ? 'success' : 'info'">
              v{{ scope.row.version }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="changeType" label="变更类型">
          <template #default="scope">
            <el-tag>{{ getChangeTypeLabel(scope.row.changeType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="changeReason" label="变更原因" />
        <el-table-column prop="createBy" label="操作人" />
        <el-table-column prop="createTime" label="操作时间" />
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <el-button link @click="handleViewVersion(scope.row)">查看</el-button>
            <el-button link @click="handleCompareVersion(scope.row)">对比</el-button>
            <el-button 
              v-if="scope.row.version !== currentConfig.version"
              link 
              type="danger" 
              @click="handleRollback(scope.row)"
            >
              回滚
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup name="ErpConfigHistory">
import { getConfigHistory, rollbackToVersion } from '@/api/erp/config'
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const versionList = ref([])
const currentConfig = ref({})

function loadHistory() {
  const configId = route.query.id
  getConfigHistory(configId).then(res => {
    versionList.value = res.data
    currentConfig.value = route.query.current || {}
  })
}

function handleViewVersion(row) {
  // 查看版本详情
}

function handleCompareVersion(row) {
  // 对比版本
}

function handleRollback(row) {
  // 回滚到指定版本
  rollbackToVersion({
    configId: route.query.id,
    targetVersion: row.version
  }).then(() => {
    loadHistory()
  })
}

function handleBack() {
  router.back()
}

loadHistory()
</script>
```

---

## 🔌 API 接口设计

### 配置管理 API

```javascript
// api/erp/config.js
import request from '@/utils/request'

/** 查询配置列表 */
export function listConfig(query) {
  return request({
    url: '/erp/config/list',
    method: 'get',
    params: query
  })
}

/** 查询配置详情 */
export function getConfig(id) {
  return request({
    url: `/erp/config/${id}`,
    method: 'get'
  })
}

/** 新增配置 */
export function addConfig(data) {
  return request({
    url: '/erp/config/add',
    method: 'post',
    data: data
  })
}

/** 修改配置 */
export function updateConfig(data) {
  return request({
    url: '/erp/config/edit',
    method: 'put',
    data: data
  })
}

/** 保存配置 */
export function saveConfig(data) {
  return data.configId ? updateConfig(data) : addConfig(data)
}

/** 删除配置 */
export function delConfig(id) {
  return request({
    url: `/erp/config/${id}`,
    method: 'delete'
  })
}

/** 查询历史版本 */
export function getConfigHistory(configId) {
  return request({
    url: `/erp/config/history/${configId}`,
    method: 'get'
  })
}

/** 回滚版本 */
export function rollbackToVersion(data) {
  return request({
    url: '/erp/config/rollback',
    method: 'post',
    data: data
  })
}
```

---

## 📅 实施步骤

### 阶段一：基础页面（3 天）

**目标**: 完成配置列表和基础 CRUD

**任务**:
- [ ] 创建路由配置
- [ ] 实现配置列表页面
- [ ] 实现 API 接口调用
- [ ] 测试基础功能

**交付物**:
- ✅ 配置列表管理页面
- ✅ 基础增删改查功能

---

### 阶段二：编辑器（5 天）

**目标**: 实现配置编辑器

**任务**:
- [ ] 集成 CodeMirror 编辑器
- [ ] 实现 JSON 验证和格式化
- [ ] 实现模板加载功能
- [ ] 实现表单验证

**交付物**:
- ✅ 配置编辑器页面
- ✅ JSON 编辑和验证功能

---

### 阶段三：版本管理（3 天）

**目标**: 实现版本历史管理

**任务**:
- [ ] 实现历史版本列表
- [ ] 实现版本详情查看
- [ ] 实现版本对比功能
- [ ] 实现版本回滚

**交付物**:
- ✅ 配置历史版本页面
- ✅ 版本管理和回滚功能

---

### 阶段四：优化完善（4 天）

**目标**: 用户体验优化和功能完善

**任务**:
- [ ] 实现可视化预览
- [ ] 实现 diff 对比
- [ ] 权限控制集成
- [ ] 性能优化

**交付物**:
- ✅ 完整功能
- ✅ 良好用户体验

---

## ✨ 总结

### 核心功能

✅ **配置列表** - 分页展示、搜索筛选、批量操作  
✅ **配置编辑** - JSON 编辑、实时验证、模板加载  
✅ **版本管理** - 历史追溯、版本对比、一键回滚  
✅ **权限控制** - 细粒度权限管理  

### 技术特点

✅ **CodeMirror 集成** - 专业的代码编辑体验  
✅ **实时验证** - JSON 格式自动验证  
✅ **响应式设计** - 适配不同屏幕尺寸  
✅ **组件化开发** - 高复用性  

---

**文档版本**: v1.0  
**创建时间**: 2026-03-22
