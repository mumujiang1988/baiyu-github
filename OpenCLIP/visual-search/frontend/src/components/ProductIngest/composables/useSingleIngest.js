/**
 * 单产品入库逻辑
 */
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ingestProduct } from '../../api/search'
import { removeBackground } from '../../api/image'  // 需要添加此 API
import { showSuccess, handleApiError } from '../../utils/messageHandler'
import { simulateSingleProductProgress } from '../utils/progressSimulator'
import { FORM_RULES, DEFAULT_FORM } from '../constants'

export function useSingleIngest() {
  // 表单数据
  const form = reactive({ ...DEFAULT_FORM })
  const fileList = ref([])
  const submitting = ref(false)
  const removingBg = ref(false) // 抠图加载状态
  const ingestResult = ref(null)
  const formRef = ref(null)
  
  // 进度条相关
  const progress = ref(0)
  const currentImageIndex = ref(0)
  const totalImages = ref(0)
  const progressStatus = ref('')
  const progressDetail = ref('准备上传...')
  
  // 文件变化处理
  const handleFileChange = (file, files) => {
    fileList.value = files
  }
  
  // 文件移除处理
  const handleFileRemove = (file, files) => {
    fileList.value = files
  }
  
  // 一键抠图
  const removeBackgroundForAllImages = async () => {
    if (fileList.value.length === 0) {
      ElMessage.warning('请先上传图片')
      return
    }
    
    removingBg.value = true
    
    try {
      let successCount = 0
      let failCount = 0
      
      for (let i = 0; i < fileList.value.length; i++) {
        const fileItem = fileList.value[i]
        try {
          const response = await removeBackground(fileItem.raw)
          
          // 将抠图后的图片替换原图片
          const blob = new Blob([response], { type: 'image/png' })
          const newFile = new File([blob], fileItem.name.replace(/\.[^.]+$/, '') + '_nobg.png', {
            type: 'image/png'
          })
          
          // 更新文件列表
          fileList.value[i] = {
            ...fileItem,
            raw: newFile,
            name: newFile.name
          }
          
          successCount++
        } catch (error) {
          console.error(`图片 ${fileItem.name} 抠图失败:`, error)
          failCount++
        }
      }
      
      if (failCount === 0) {
        ElMessage.success(`成功对 ${successCount} 张图片进行抠图`)
      } else {
        ElMessage.warning(`抠图完成：成功 ${successCount} 张，失败 ${failCount} 张`)
      }
    } catch (error) {
      handleApiError(error.response || error, '抠图失败')
    } finally {
      removingBg.value = false
    }
  }
  
  // 提交入库
  const handleSubmit = async () => {
    if (!formRef.value) return
    
    await formRef.value.validate(async (valid) => {
      if (!valid) return
      
      if (fileList.value.length === 0) {
        ElMessage.warning('请至少上传一张图片')
        return
      }
      
      submitting.value = true
      
      // 初始化进度条
      progress.value = 0
      currentImageIndex.value = 0
      totalImages.value = fileList.value.length
      progressStatus.value = ''
      progressDetail.value = '准备上传...'
      
      try {
        // 同时执行进度模拟和实际上传
        const [uploadResponse] = await Promise.all([
          ingestProduct(
            form.product_code,
            form.name,
            fileList.value.map(f => f.raw),
            form.spec,
            form.category
          ),
          simulateSingleProductProgress({
            totalImages: fileList.value.length,
            onProgress: (prog, detail) => {
              progress.value = prog
              progressDetail.value = detail
            },
            onComplete: () => {}
          })
        ])
        
        // 完成进度
        progress.value = 100
        progressDetail.value = '入库完成!'
        progressStatus.value = 'success'
        
        ingestResult.value = uploadResponse
        
        if (uploadResponse.success) {
          showSuccess(uploadResponse.message)
        } else {
          handleApiError(uploadResponse, '入库失败')
          progressStatus.value = 'exception'
          progressDetail.value = '入库失败'
          
          // 提取错误摘要用于重试对话框
          const errorSummary = extractErrorSummary(uploadResponse.message || uploadResponse.detail || '未知错误')
          
          // Show retry option with error details
          setTimeout(async () => {
            try {
              await ElMessageBox.confirm(
                `产品 ${form.product_code} 入库失败\n\n${uploadResponse.message || '未知错误'}\n\n是否重试？`,
                '重试提示',
                {
                  confirmButtonText: '重试',
                  cancelButtonText: '取消',
                  type: 'warning',
                  dangerouslyUseHTMLString: false,
                  customClass: 'error-detail-dialog'
                }
              )
              // Reset form for retry
              resetForm()
              ElMessage.info('请重新填写产品信息后提交')
            } catch (e) {
              // User cancelled
            }
          }, 500)
        }
      } catch (error) {
        handleApiError(error.response || error, '入库失败')
        progressStatus.value = 'exception'
        progressDetail.value = '入库失败'
        
        // 提取错误摘要
        const errorData = error.response?.data
        const errorSummary = extractErrorSummary(errorData?.message || errorData?.detail || error.message || '未知错误')
        
        // Show retry option for exceptions too
        setTimeout(async () => {
          try {
            await ElMessageBox.confirm(
              `产品 ${form.product_code} 入库失败\n\n${errorData?.message || errorData?.detail || error.message || '未知错误'}\n\n是否重试？`,
              '重试提示',
              {
                confirmButtonText: '重试',
                cancelButtonText: '取消',
                type: 'warning',
                dangerouslyUseHTMLString: false,
                customClass: 'error-detail-dialog'
              }
            )
            // Reset form for retry
            resetForm()
            ElMessage.info('请重新填写产品信息后提交')
          } catch (e) {
            // User cancelled
          }
        }, 500)
      } finally {
        // 延迟重置进度条
        setTimeout(() => {
          submitting.value = false
          progress.value = 0
          progressStatus.value = ''
          progressDetail.value = '准备上传...'
        }, 2000)
      }
    })
  }
  
  // 重置表单
  const resetForm = () => {
    if (submitting.value) return
    
    formRef.value?.resetFields()
    Object.assign(form, DEFAULT_FORM)
    fileList.value = []
    ingestResult.value = null
    progress.value = 0
    currentImageIndex.value = 0
    totalImages.value = 0
    progressStatus.value = ''
    progressDetail.value = '准备上传...'
  }
  
  // 提取错误摘要(取前3行)
  const extractErrorSummary = (detail) => {
    if (!detail) return '未知错误'
    
    const lines = detail.split('\n').filter(line => line.trim())
    // 取前3行作为摘要，显示更多详细信息
    const summary = lines.slice(0, 3).join('\n')
    return summary || '未知错误'
  }
  
  return {
    // 状态
    form,
    fileList,
    submitting,
    removingBg,
    ingestResult,
    formRef,
    progress,
    currentImageIndex,
    totalImages,
    progressStatus,
    progressDetail,
    
    // 方法
    handleFileChange,
    handleFileRemove,
    handleSubmit,
    resetForm,
    removeBackgroundForAllImages,
    
    // 常量
    FORM_RULES
  }
}
