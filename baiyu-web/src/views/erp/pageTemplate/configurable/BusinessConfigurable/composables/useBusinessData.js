import { ref, reactive } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

/**
 * Business Data Management Composable
 */
export function useBusinessData(config) {
  // 状态管理
  const loading = ref(false)
  const submitLoading = ref(false)
  const tableData = ref([])
  const total = ref(0)
  const queryParams = reactive({
    pageNum: 1,
    pageSize: 10
  })

  /**
   * Get API Method
   */
  const getApiMethod = async (methodType) => {
    const apiConfig = config.value?.apiConfig
    
    if (!apiConfig || !apiConfig.methods) {
      return null
    }
    
    const methodConfig = apiConfig.methods[methodType]
    
    if (!methodConfig) {
      return null
    }
    
    if (typeof methodConfig === 'string') {
      return (data) => request({
        url: methodConfig,
        method: methodType === 'get' || methodType === 'entry' || methodType === 'cost' ? 'get' : 'post',
        params: methodType === 'get' || methodType === 'entry' || methodType === 'cost' ? { id: data } : undefined,
        data: methodType === 'get' || methodType === 'entry' || methodType === 'cost' ? undefined : data
      })
    }
    
    if (typeof methodConfig === 'object' && methodConfig.url) {
      return (data) => request({
        url: methodConfig.url,
        method: methodConfig.method || 'post',
        params: methodConfig.method === 'get' ? data : undefined,
        data: methodConfig.method !== 'get' ? data : undefined
      })
    }
    
    return null
  }

  /**
   * Query list data
   */
  const getList = async (tableName, queryConfig) => {
    loading.value = true
    
    try {
      const moduleCode = config.value?.moduleCode
      
      if (!moduleCode) {
        throw new Error('Missing moduleCode field in module configuration, cannot execute query')
      }
      
      const response = await request({
        url: '/erp/engine/query/execute',
        method: 'post',
        data: {
          moduleCode: moduleCode,
          tableName: tableName,
          queryConfig: queryConfig,
          pageNum: queryParams.pageNum,
          pageSize: queryParams.pageSize
        }
      })
      
      tableData.value = response.data?.rows || []
      total.value = response.data?.total || 0
      
      return {
        success: true,
        data: tableData.value,
        total: total.value
      }
    } catch (error) {
      ElMessage.error(config.value?.businessConfig?.messages?.error?.load || 'Failed to query list')
      return {
        success: false,
        error: error.message
      }
    } finally {
      loading.value = false
    }
  }

  /**
   * Build query conditions
   */
  const buildQueryConfig = (searchFields, currentParams, dateRange) => {
    const conditions = []
    
    searchFields.forEach(field => {
      let value = currentParams[field.field]
      const operator = field.queryOperator || 'eq'
      
      if (field.component === 'daterange') {
        if (Array.isArray(dateRange) && dateRange.length === 2) {
          value = dateRange
        } else {
          return
        }
      }
      
      if (value === undefined || value === null || value === '') {
        return
      }
      
      conditions.push({
        field: field.field,
        operator: operator,
        value: value
      })
    })
    
    const orderBy = config.value?.pageConfig?.orderBy || [
      { field: 'FCreateDate', direction: 'DESC' }
    ]
    
    return {
      conditions: conditions,
      orderBy: orderBy
    }
  }

  /**
   * Submit data
   */
  const submitData = async (data, isNew) => {
    submitLoading.value = true
    
    try {
      const apiMethod = await getApiMethod(isNew ? 'add' : 'update')
      
      if (!apiMethod) {
        ElMessage.error('API not configured')
        return {
          success: false,
          error: 'API not configured'
        }
      }
      
      await apiMethod(data)
      
      ElMessage.success(
        config.value?.businessConfig?.messages?.success?.[isNew ? 'add' : 'edit'] 
        || (isNew ? 'Added successfully' : 'Updated successfully')
      )
      
      return {
        success: true,
        data: data
      }
    } catch (error) {
      ElMessage.error('Save failed: ' + (error.message || 'Please check if the form is filled correctly'))
      return {
        success: false,
        error: error.message
      }
    } finally {
      submitLoading.value = false
    }
  }

  /**
   * Delete data
   */
  const deleteData = async (ids) => {
    try {
      const apiMethod = await getApiMethod('delete')
      
      if (!apiMethod) {
        ElMessage.error('API not configured')
        return {
          success: false,
          error: 'API not configured'
        }
      }
      
      await apiMethod(ids)
      
      ElMessage.success(
        config.value?.businessConfig?.messages?.success?.delete || 'Deleted successfully'
      )
      
      return {
        success: true
      }
    } catch (error) {
      ElMessage.error('Delete failed: ' + (error.message || 'Unknown error'))
      return {
        success: false,
        error: error.message
      }
    }
  }

  return {
    // 状态
    loading,
    submitLoading,
    tableData,
    total,
    queryParams,
    
    // 方法
    getList,
    buildQueryConfig,
    submitData,
    deleteData,
    getApiMethod
  }
}
