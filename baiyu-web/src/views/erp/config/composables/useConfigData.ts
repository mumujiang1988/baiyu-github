/**
 * 配置数据管理 Composable
 * 注意：本 composable 专注于数据逻辑，错误提示由调用方处理
 */

import { ref, reactive } from 'vue'
import type { ErpConfig, ConfigQueryParams, EditFormData } from '../types/config'
import { listConfig, delConfig, getConfig, saveConfig } from '../api'

export function useConfigData() {
  // 列表状态
  const loading = ref(false)
  const configList = ref<ErpConfig[]>([])
  const total = ref(0)
  
  // 查询参数
  const queryParams = reactive<ConfigQueryParams>({
    pageNum: 1,
    pageSize: 10,
    moduleCode: '',
    configName: '',
    configType: '',
    status: ''
  })
  
  // 编辑表单初始数据
  const initialEditForm: EditFormData = {
    configId: null,
    moduleCode: '',
    configName: '',
    configType: '',
    pageConfig: '',
    formConfig: '',
    tableConfig: '',
    searchConfig: '',
    actionConfig: '',
    apiConfig: '',
    dictConfig: '',
    businessConfig: '',
    detailConfig: '',
    status: '1',
    isPublic: '0',
    remark: '',
    changeReason: '',
    version: 1
  }
  
  /**
   * 加载配置列表
   */
  const getList = async () => {
    loading.value = true
    try {
      const res = await listConfig(queryParams)
      // res 的类型是 ApiResponse<ConfigListResponse>，所以 data 是 ConfigListResponse
      const configData = res.data
      configList.value = configData?.rows || []
      total.value = configData?.total !== undefined ? configData.total : 0
    } catch (error: any) {
      console.error('查询配置列表失败:', error)
      configList.value = []
      total.value = 0
      // 错误提示由调用方统一处理
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 搜索
   */
  const handleQuery = () => {
    queryParams.pageNum = 1
    getList()
  }
  
  /**
   * 重置查询
   */
  const resetQuery = () => {
    queryParams.pageNum = 1
    queryParams.moduleCode = ''
    queryParams.configName = ''
    queryParams.configType = ''
    queryParams.status = ''
    getList()
  }
  
  /**
   * 删除配置
   */
  const handleDelete = async (row: ErpConfig) => {
    try {
      await delConfig(row.configId!)
      // 成功提示由调用方统一处理
      await getList()
    } catch (error: any) {
      console.error('删除配置失败:', error)
      throw error // 抛出错误，由调用方处理
    }
  }
  
  /**
   * 加载配置详情
   */
  const loadConfigDetail = async (configId: number): Promise<ErpConfig> => {
    try {
      const res = await getConfig(configId)
      let data
      if (res.code === 200 || res.code === 0) {
        data = res.data || {}
      } else {
        throw new Error(res.msg || '加载配置失败')
      }
      
      if (!data.configId) {
        throw new Error('配置不存在或已删除')
      }
      
      return data
    } catch (error: any) {
      console.error('加载配置详情失败:', error)
      throw error  
    }
  }
  
  /**
   * 保存配置
   */
  const handleSave = async (data: Partial<EditFormData>, isNew: boolean) => {
    try {
      await saveConfig(data)
      // 成功提示由调用方统一处理
      await getList()
      return true
    } catch (error: any) {
      console.error('保存配置失败:', error)
      throw error // 抛出错误，由调用方处理
    }
  }
  
  return {
    // State
    loading,
    configList,
    total,
    queryParams,
    initialEditForm,
    
    // Methods
    getList,
    handleQuery,
    resetQuery,
    handleDelete,
    loadConfigDetail,
    handleSave
  }
}
