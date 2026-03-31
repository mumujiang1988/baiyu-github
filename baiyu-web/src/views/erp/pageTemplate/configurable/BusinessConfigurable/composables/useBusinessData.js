import { ref, reactive } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

/**
 * 业务数据管理 Composable
 * 用于管理 ERP 业务页面的核心数据逻辑
 * @param {import('vue').Ref} config - 配置对象引用
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
   * 获取 API 方法
   * @param {string} methodType - 方法类型
   * @returns {Promise<Function|null>}
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
   * 查询列表数据
   * @param {string} tableName - 表名
   * @param {Object} queryConfig - 查询配置
   * @returns {Promise<Object>}
   */
  const getList = async (tableName, queryConfig) => {
    loading.value = true
    
    try {
      const moduleCode = config.value?.moduleCode
      
      if (!moduleCode) {
        throw new Error('模块配置中缺少 moduleCode 字段，无法执行查询')
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
      ElMessage.error(config.value?.businessConfig?.messages?.error?.load || '查询列表失败')
      return {
        success: false,
        error: error.message
      }
    } finally {
      loading.value = false
    }
  }

  /**
   * 构建查询条件
   * @param {Array} searchFields - 搜索字段配置
   * @param {Object} currentParams - 当前查询参数
   * @param {Array} dateRange - 日期范围
   * @returns {Object}
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
   * 提交数据
   * @param {Object} data - 提交数据
   * @param {boolean} isNew - 是否新增
   * @returns {Promise<Object>}
   */
  const submitData = async (data, isNew) => {
    submitLoading.value = true
    
    try {
      const apiMethod = await getApiMethod(isNew ? 'add' : 'update')
      
      if (!apiMethod) {
        ElMessage.error('API 未配置')
        return {
          success: false,
          error: 'API 未配置'
        }
      }
      
      await apiMethod(data)
      
      ElMessage.success(
        config.value?.businessConfig?.messages?.success?.[isNew ? 'add' : 'edit'] 
        || (isNew ? '新增成功' : '修改成功')
      )
      
      return {
        success: true,
        data: data
      }
    } catch (error) {
      ElMessage.error('保存失败：' + (error.message || '请检查表单填写是否正确'))
      return {
        success: false,
        error: error.message
      }
    } finally {
      submitLoading.value = false
    }
  }

  /**
   * 删除数据
   * @param {Array<string>} ids - ID 数组
   * @returns {Promise<Object>}
   */
  const deleteData = async (ids) => {
    try {
      const apiMethod = await getApiMethod('delete')
      
      if (!apiMethod) {
        ElMessage.error('API 未配置')
        return {
          success: false,
          error: 'API 未配置'
        }
      }
      
      await apiMethod(ids)
      
      ElMessage.success(
        config.value?.businessConfig?.messages?.success?.delete || '删除成功'
      )
      
      return {
        success: true
      }
    } catch (error) {
      ElMessage.error('删除失败：' + (error.message || '未知错误'))
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
