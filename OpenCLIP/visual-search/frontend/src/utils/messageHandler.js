/**
 * 统一的消息提示处理器
 * 提供更友好的错误提示和解决建议
 */
import { ElMessage, ElNotification } from 'element-plus'

// 错误类型映射表
const ERROR_TYPE_MAP = {
  // 数据库相关
  'duplicate': {
    type: 'warning',
    title: '数据重复',
    suggestion: '系统已自动去重，请使用其他图片'
  },
  'database': {
    type: 'error',
    title: '数据库错误',
    suggestion: '请检查网络连接，或联系管理员'
  },
  'mysql': {
    type: 'error',
    title: '数据库错误',
    suggestion: '请稍后重试，或联系管理员'
  },

  // 图片相关
  'invalid_image': {
    type: 'error',
    title: '图片格式错误',
    suggestion: '请确保上传有效的图片文件(JPG、PNG、BMP等)'
  },
  'image_too_large': {
    type: 'warning',
    title: '图片过大',
    suggestion: '请压缩图片文件大小后再上传'
  },
  'image_too_small': {
    type: 'warning',
    title: '图片过小',
    suggestion: '请上传尺寸更大的清晰图片'
  },

  // 模型相关
  'model': {
    type: 'error',
    title: '模型错误',
    suggestion: '请稍后重试，或联系管理员检查模型服务'
  },
  'feature_extract': {
    type: 'error',
    title: '特征提取失败',
    suggestion: '请检查图片是否清晰，或尝试其他图片'
  },

  // 通用错误
  'network': {
    type: 'error',
    title: '网络错误',
    suggestion: '请检查网络连接'
  },
  'timeout': {
    type: 'warning',
    title: '请求超时',
    suggestion: '请稍后重试，或检查网络连接'
  },
  'unknown': {
    type: 'error',
    title: '未知错误',
    suggestion: '请联系管理员'
  }
}

/**
 * 解析错误类型
 * @param {string} errorMessage - 错误信息
 * @returns {string} 错误类型
 */
function parseErrorType(errorMessage) {
  const lowerMessage = errorMessage.toLowerCase()

  if (lowerMessage.includes('duplicate') || lowerMessage.includes('重复') || lowerMessage.includes('已存在')) {
    return 'duplicate'
  }
  if (lowerMessage.includes('database') || lowerMessage.includes('mysql') || lowerMessage.includes('数据库')) {
    return 'database'
  }
  if (lowerMessage.includes('invalid') || lowerMessage.includes('格式') || lowerMessage.includes('不支持')) {
    return 'invalid_image'
  }
  if (lowerMessage.includes('large') || lowerMessage.includes('过大')) {
    return 'image_too_large'
  }
  if (lowerMessage.includes('small') || lowerMessage.includes('过小')) {
    return 'image_too_small'
  }
  if (lowerMessage.includes('model') || lowerMessage.includes('模型')) {
    return 'model'
  }
  if (lowerMessage.includes('feature') || lowerMessage.includes('特征')) {
    return 'feature_extract'
  }
  if (lowerMessage.includes('network') || lowerMessage.includes('网络') || lowerMessage.includes('fetch')) {
    return 'network'
  }
  if (lowerMessage.includes('timeout') || lowerMessage.includes('超时')) {
    return 'timeout'
  }

  return 'unknown'
}

/**
 * 显示成功消息
 * @param {string} message - 消息内容
 * @param {Object} options - 额外选项
 */
export function showSuccess(message, options = {}) {
  return ElMessage.success({
    message,
    duration: 3000,
    ...options
  })
}

/**
 * 显示警告消息
 * @param {string} message - 消息内容
 * @param {Object} options - 额外选项
 */
export function showWarning(message, options = {}) {
  return ElMessage.warning({
    message,
    duration: 4000,
    ...options
  })
}

/**
 * 显示错误消息
 * @param {string} message - 错误信息
 * @param {Object} options - 额外选项
 */
export function showError(message, options = {}) {
  const errorType = parseErrorType(message)
  const errorConfig = ERROR_TYPE_MAP[errorType] || ERROR_TYPE_MAP['unknown']

  // 如果有解决建议，使用通知而不是消息
  if (errorConfig.suggestion && !options.disableSuggestion) {
    return ElNotification({
      title: errorConfig.title,
      message: `
        <div style="margin-bottom: 8px;">${message}</div>
        <div style="color: #409eff; font-size: 13px;">
          <strong>💡 ${errorConfig.suggestion}</strong>
        </div>
      `,
      type: errorConfig.type,
      dangerouslyUseHTMLString: true,
      duration: 6000,
      ...options
    })
  }

  return ElMessage.error({
    message,
    duration: 5000,
    ...options
  })
}

/**
 * 显示带建议的错误消息
 * @param {string} message - 错误信息
 * @param {string} suggestion - 解决建议
 * @param {Object} options - 额外选项
 */
export function showErrorWithSuggestion(message, suggestion, options = {}) {
  return ElNotification({
    title: '操作失败',
    message: `
      <div style="margin-bottom: 12px; line-height: 1.6;">${message}</div>
      <div style="padding: 12px; background: #f0f9ff; border-left: 3px solid #409eff; border-radius: 4px;">
        <div style="color: #409eff; font-weight: bold; margin-bottom: 4px;">💡 解决建议</div>
        <div style="color: #606266; font-size: 13px; line-height: 1.6;">${suggestion}</div>
      </div>
    `,
    type: 'error',
    dangerouslyUseHTMLString: true,
    duration: 8000,
    ...options
  })
}

/**
 * 显示信息消息
 * @param {string} message - 消息内容
 * @param {Object} options - 额外选项
 */
export function showInfo(message, options = {}) {
  return ElMessage.info({
    message,
    duration: 3000,
    ...options
  })
}

/**
 * 显示加载消息
 * @param {string} message - 消息内容
 * @returns {Function} 关闭函数
 */
export function showLoading(message = '加载中...') {
  const loadingInstance = ElMessage({
    message,
    duration: 0,
    type: 'info'
  })

  return () => {
    loadingInstance.close()
  }
}

/**
 * 处理API响应错误
 * @param {Object} response - API响应对象
 * @param {string} defaultMessage - 默认错误消息
 */
export function handleApiError(response, defaultMessage = '操作失败') {
  const message = response?.data?.message || response?.message || defaultMessage
  const suggestion = response?.data?.suggestion || response?.suggestion

  if (suggestion) {
    showErrorWithSuggestion(message, suggestion)
  } else {
    showError(message)
  }
}

export default {
  showSuccess,
  showWarning,
  showError,
  showErrorWithSuggestion,
  showInfo,
  showLoading,
  handleApiError
}
