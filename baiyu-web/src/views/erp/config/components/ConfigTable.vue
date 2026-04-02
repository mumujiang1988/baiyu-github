/**
 * 配置表格组件 - UI 组件
 * 职责：渲染配置列表表格，处理行内操作按钮
 */

<script setup lang="ts">
import type { ErpConfig } from '../types/config'
import { getConfigTypeLabel, getConfigTypeTag } from '@/constants/configTypes'

interface Props {
  configList: ErpConfig[]
  loading: boolean
}

defineProps<Props>()

const emit = defineEmits<{
  (e: 'view', row: ErpConfig): void
  (e: 'history', row: ErpConfig): void
  (e: 'delete', row: ErpConfig): void
  (e: 'copy', row: ErpConfig): void
  (e: 'export', row: ErpConfig): void
  (e: 'verify', row: ErpConfig): void
}>()

// 下拉菜单命令处理
const handleCommand = (command: string, row: ErpConfig) => {
  switch (command) {
    case 'copy':
      emit('copy', row)
      break
    case 'export':
      emit('export', row)
      break
    case 'delete':
      emit('delete', row)
      break
    case 'verify':
      emit('verify', row)
      break
  }
}
</script>

<template>
  <el-table
    v-loading="loading"
    :data="configList"
    border
    stripe
    highlight-current-row
    class="config-table"
  >
    <el-table-column type="selection" width="55" align="center" />
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
    >
      <template #default="scope">
        <el-tag :type="getConfigTypeTag(scope.row.configType)">
          {{ getConfigTypeLabel(scope.row.configType) }}
        </el-tag>
      </template>
    </el-table-column>
    
    <el-table-column
      prop="version"
      label="版本号"
      width="80"
      align="center"
    />
    
    <el-table-column
      prop="isPublic"
      label="公共"
      width="70"
      align="center"
    >
      <template #default="scope">
        <el-tag :type="scope.row.isPublic === '1' ? 'success' : 'info'">
          {{ scope.row.isPublic === '1' ? '是' : '否' }}
        </el-tag>
      </template>
    </el-table-column>
    
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
      prop="remark"
      label="备注"
      min-width="150"
      :show-overflow-tooltip="true"
    />
    
    <el-table-column
      prop="updateTime"
      label="更新时间"
      width="160"
    />
    
    <el-table-column
      label="操作"
      width="300"
      fixed="right"
      align="center"
    >
      <template #default="scope">
        <el-button
          link
          type="primary"
          icon="View"
          @click="emit('view', scope.row)"
        >
          查看
        </el-button>
        
        <el-button
          link
          type="success"
          icon="CircleCheck"
          @click="emit('verify', scope.row)"
        >
          验证
        </el-button>
        
        <el-button
          link
          type="primary"
          icon="Clock"
          @click="emit('history', scope.row)"
        >
          历史
        </el-button>
        
        <el-dropdown trigger="click" @command="(cmd: string) => handleCommand(cmd, scope.row)">
          <el-button link type="primary" icon="More">
            更多
            <el-icon><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="verify" icon="CircleCheck">
                字段验证
              </el-dropdown-item>
              <el-dropdown-item command="copy" icon="DocumentCopy">
                复制配置
              </el-dropdown-item>
              <el-dropdown-item command="export" icon="Download">
                导出配置
              </el-dropdown-item>
              <el-dropdown-item divided command="delete" icon="Delete">
                删除配置
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
    </el-table-column>
  </el-table>
</template>

<style scoped lang="scss">
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
</style>
