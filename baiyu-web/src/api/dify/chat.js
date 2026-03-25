import request from '@/utils/request'

// 普通聊天
export function chat(data) {
  return request({
    url: '/Dify/chat',  // 修改为新的后端接口
    method: 'post',
    data: data,
    timeout: 60000 // 60秒超时
  })
}

export function uploadImage(file, options = {}) {
  const formData = new FormData();
  formData.append('file', file);

  return request({
    url: '/Dify/image-search-materials',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: options.timeout || 120000, // 默认120秒超时
    onUploadProgress: options.onUploadProgress
  })
}

// 新增：轮询获取结果
/*export async function pollImageSearchResult(taskId, maxAttempts = 30) {
  for (let i = 0; i < maxAttempts; i++) {
    try {
      const response = await checkImageSearchStatus(taskId);
      if (response.code === 200 && response.data.status === 'completed') {
        return response.data.result;
      }

      // 等待2秒后重试
      await new Promise(resolve => setTimeout(resolve, 2000));
    } catch (error) {
      console.error('轮询状态失败:', error);
      await new Promise(resolve => setTimeout(resolve, 2000));
    }
  }
  throw new Error('获取图片识别结果超时');
}*/
