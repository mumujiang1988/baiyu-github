<script setup name="ErpConfig">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import JsonEditor from './components/JsonEditor.vue'

// 工具函数：统一错误处理
function getErrorMessage(error, defaultMsg) {
  return error.response?.data?.msg || error.message || defaultMsg
}

// State
const loading = ref(false)
const configList = ref([])
const total = ref(0)
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  moduleCode: '',
  configName: '',
  configType: '',
  status: ''
})

// Dialog 状态
const viewDialogVisible = ref(false)
const combinedConfigContent = ref('{}')

// 获取配置列表
async function getList() {
  loading.value = true
  try {
    const res = await request({
      url: '/erp/config/list',
      method: 'get',
      params: queryParams.value
    })
    configList.value = res.rows || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取配置列表失败:', error)
    ElMessage.error(getErrorMessage(error, '获取配置列表失败'))
    configList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索
async function handleQuery() {
  queryParams.value.pageNum = 1
  await getList()
}

// 重置
function resetQuery() {
  queryParams.value.pageNum = 1
  queryParams.value.moduleCode = ''
  queryParams.value.configName = ''
  queryParams.value.configType = ''
  queryParams.value.status = ''
  getList()
}

// 清理缓存
async function handleClearCache() {
  try {
    await ElMessageBox.confirm(
      '确定要清理所有 ERP 配置缓存吗？',
      '提示',
      { 
        confirmButtonText: '确定', 
        cancelButtonText: '取消', 
        type: 'warning',
        distinguishCancelAndClose: true
      }
    )
    
    await request({ url: '/erp/cache/clear-all', method: 'delete' })
    ElMessage.success('缓存清理成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清理缓存失败:', error)
      ElMessage.error(getErrorMessage(error, '缓存清理失败'))
    }
  }
}

// 查看配置JSON
async function handleView(row) {
  try {
    const res = await request({
      url: `/erp/config/${row.configId}`,
      method: 'get'
    })
    
    if (!res.data) {
      ElMessage.warning('配置数据为空')
      return
    }
    
    combinedConfigContent.value = JSON.stringify(res.data, null, 2)
    viewDialogVisible.value = true
  } catch (error) {
    console.error('加载配置详情失败:', error)
    ElMessage.error(getErrorMessage(error, '加载配置失败'))
  }
}

// Lifecycle
onMounted(() => {
  getList()
})
</script>

<template>
  <div class="app-container">
    <el-card shadow="never">
      <!-- Header -->
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">ERP 配置管理 - JSON 查看器</span>
          </div>
          <div class="header-right">
            <el-button type="warning" icon="Delete" @click="handleClearCache">
              清理缓存
            </el-button>
            <el-button icon="Refresh" @click="resetQuery">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :model="queryParams" :inline="true" label-width="80px" class="search-form">
        <el-row :gutter="12">
          <el-col :span="6">
            <el-form-item label="模块编码">
              <el-input
                v-model="queryParams.moduleCode"
                placeholder="请输入模块编码"
                clearable
                style="width: 100%"
                @keyup.enter="handleQuery"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="6">
            <el-form-item label="配置名称">
              <el-input
                v-model="queryParams.configName"
                placeholder="请输入配置名称"
                clearable
                style="width: 100%"
                @keyup.enter="handleQuery"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="6">
            <el-form-item label="配置类型">
              <el-select
                v-model="queryParams.configType"
                placeholder="请选择配置类型"
                clearable
                style="width: 100%"
              >
                <el-option label="页面配置" value="PAGE" />
                <el-option label="表单配置" value="FORM" />
                <el-option label="表格配置" value="TABLE" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="6">
            <el-form-item label="状态">
              <el-select
                v-model="queryParams.status"
                placeholder="请选择状态"
                clearable
                style="width: 100%"
              >
                <el-option label="正常" value="1" />
                <el-option label="停用" value="0" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row>
          <el-col :span="24" style="text-align: right;">
            <el-button type="primary" icon="Search" @click="handleQuery">
              搜索
            </el-button>
            <el-button icon="Refresh" @click="resetQuery">
              重置
            </el-button>
          </el-col>
        </el-row>
      </el-form>
      
      <!-- 配置列表表格 -->
      <el-table
        v-loading="loading"
        :data="configList"
        border
        stripe
        highlight-current-row
        class="config-table"
      >
        <el-table-column label="序号" type="index" width="60" align="center" />
        
        <el-table-column
          prop="configName"
          label="配置名称"
          min-width="200"
          :show-overflow-tooltip="true"
        />
        
        <el-table-column
          prop="moduleCode"
          label="模块编码"
          width="150"
        />
        
        <el-table-column
          prop="configType"
          label="配置类型"
          width="120"
          align="center"
        />
        
        <el-table-column
          prop="version"
          label="版本号"
          width="80"
          align="center"
        />
        
        <el-table-column
          prop="status"
          label="状态"
          width="70"
          align="center"
        >
          <template #default="scope">
            <el-tag :type="scope.row.status === '1' ? 'success' : 'danger'">
              {{ scope.row.status === '1' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column
          prop="updateTime"
          label="更新时间"
          width="160"
        />
        
        <el-table-column
          label="操作"
          width="120"
          fixed="right"
          align="center"
        >
          <template #default="scope">
            <el-button
              link
              type="primary"
              icon="View"
              @click="handleView(scope.row)"
            >
              查看JSON
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <el-pagination
        v-show="total > 0"
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="getList"
        @current-change="getList"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 配置查看对话框 - 只显示JSON -->
    <el-dialog
      v-model="viewDialogVisible"
      title="配置JSON数据"
      width="90%"
      top="5vh"
      class="config-view-dialog"
    >
      <JsonEditor
        v-model="combinedConfigContent"
        readonly
        height="calc(100vh - 300px)"
      />
      
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .title {
      font-size: 16px;
      font-weight: 500;
      color: var(--el-text-color-primary);
    }
  }
  
  .header-right {
    display: flex;
    gap: 8px;
  }
}

.search-form {
  margin-bottom: 16px;
  
  :deep(.el-form-item) {
    margin-right: 0;
    margin-bottom: 12px;
  }
  
  :deep(.el-form-item__label) {
    font-size: 13px;
  }
  
  :deep(.el-input__wrapper),
  :deep(.el-select-wrapper) {
    height: 32px;
    font-size: 13px;
  }
  
  :deep(.el-button) {
    padding: 8px 12px;
    font-size: 13px;
  }
}

.config-table {
  :deep(.el-table__header) {
    th {
      background-color: var(--el-fill-color-light);
      color: var(--el-text-color-primary);
      font-weight: 500;
    }
  }
  
  :deep(.el-table__row:hover) {
    background-color: var(--el-fill-color-lighter);
  }
}

.config-view-dialog {
  :deep(.el-dialog__header) {
    padding-bottom: 12px;
  }
}
</style>
