<template>
  <el-form :model="form" label-width="100px" :rules="FORM_RULES" ref="formRef">
    <el-form-item label="产品编码" prop="product_code">
      <el-input v-model="form.product_code" placeholder="请输入产品编码" />
    </el-form-item>
    
    <el-form-item label="产品名称">
      <el-input v-model="form.name" placeholder="请输入产品名称（可选）" />
    </el-form-item>
    
    <el-form-item label="规格">
      <el-input v-model="form.spec" placeholder="请输入规格" />
    </el-form-item>
    
    <el-form-item label="分类">
      <el-input v-model="form.category" placeholder="请输入分类" />
    </el-form-item>
    
    <el-form-item label="产品图片" prop="files">
      <el-upload
        ref="uploadRef"
        action="#"
        :auto-upload="false"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
        :on-preview="handlePicturePreview"
        :file-list="fileList"
        accept="image/*"
        list-type="picture-card"
        multiple
      >
        <el-icon><Plus /></el-icon>
      </el-upload>
      <div class="upload-tip">支持上传多张图片，建议上传不同角度的产品图片</div>
      
      <!-- 一键抠图按钮 -->
      <div v-if="fileList.length > 0" style="margin-top: 10px;">
        <el-button 
          type="success" 
          size="small"
          @click="removeBackgroundForAllImages"
          :loading="removingBg"
          :disabled="submitting"
        >
          <el-icon><MagicStick /></el-icon>
          一键抠图 ({{ fileList.length }} 张图片)
        </el-button>
        <span style="margin-left: 10px; color: #909399; font-size: 12px;">
          使用 AI 自动移除图片背景，提高检索准确度
        </span>
      </div>
    </el-form-item>
    
    <el-form-item>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        <el-icon><Upload /></el-icon>
        {{ submitting ? '入库中...' : '提交入库' }}
      </el-button>
      <el-button @click="resetForm" :disabled="submitting">
        <el-icon><Refresh /></el-icon>
        重置
      </el-button>
    </el-form-item>
  </el-form>

  <!-- 图片预览器 -->
  <el-image-viewer
    v-if="showImageViewer"
    :url-list="[previewImageUrl]"
    @close="showImageViewer = false"
  />
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElImageViewer } from 'element-plus'
import { Plus, Upload, Refresh, MagicStick } from '@element-plus/icons-vue'
import { useIngestStore } from '../../stores/ingest'
import { removeBackground } from '../../api/image'
import { useBlobUrl } from '../../composables/useBlobUrl'

// 使用 Pinia Store
const ingestStore = useIngestStore()

// Blob URL 管理
const { createUrl } = useBlobUrl()

// 表单数据
const form = reactive({
  product_code: '',
  name: '',
  spec: '',
  category: ''
})

// 文件列表
const fileList = ref([])
const submitting = ref(false)
const removingBg = ref(false)
const formRef = ref(null)
const uploadRef = ref(null)

// 图片预览
const showImageViewer = ref(false)
const previewImageUrl = ref('')

// 表单验证规则
const FORM_RULES = {
  product_code: [
    { required: true, message: '请输入产品编码', trigger: 'blur' }
  ],
  files: [
    { 
      validator: (rule, value, callback) => {
        if (fileList.value.length === 0) {
          callback(new Error('请至少上传一张图片'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ]
}

// 处理文件选择
const handleFileChange = (file, files) => {
  fileList.value = files
}

// 处理文件移除
const handleFileRemove = (file, files) => {
  fileList.value = files
}

// 处理图片预览
const handlePicturePreview = (file) => {
  // 如果文件已经有 URL（上传后），直接使用
  if (file.url) {
    previewImageUrl.value = file.url
    showImageViewer.value = true
  } 
  // 如果是本地文件，创建临时 URL
  else if (file.raw) {
    const tempUrl = createUrl(file.raw)  //  使用统一管理器
    previewImageUrl.value = tempUrl
    showImageViewer.value = true
    // 不再需要 setTimeout 清理，由 useBlobUrl 自动管理
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    if (fileList.value.length === 0) {
      ElMessage.warning('请至少上传一张图片')
      return
    }
    
    submitting.value = true
    
    try {
      const files = fileList.value.map(f => f.raw)
      
      await ingestStore.ingestSingleProduct({
        productCode: form.product_code,
        name: form.name,
        spec: form.spec,
        category: form.category,
        files: files
      })
      
      ElMessage.success('产品入库成功')
      resetForm()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '入库失败')
    } finally {
      submitting.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  form.product_code = ''
  form.name = ''
  form.spec = ''
  form.category = ''
  fileList.value = []
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 一键抠图
const removeBackgroundForAllImages = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请先上传图片')
    return
  }
  
  removingBg.value = true
  
  try {
    for (let i = 0; i < fileList.value.length; i++) {
      const fileItem = fileList.value[i]
      const originalFile = fileItem.raw
      
      // 调用抠图 API
      const transparentBlob = await removeBackground(originalFile)
      
      // 创建新的 File 对象
      const transparentFile = new File(
        [transparentBlob],
        originalFile.name.replace(/\.[^.]+$/, '') + '_nobg.png',
        { type: 'image/png' }
      )
      
      // 更新文件列表
      fileList.value[i].raw = transparentFile
      fileList.value[i].url = URL.createObjectURL(transparentBlob)
    }
    
    ElMessage.success(`已为 ${fileList.value.length} 张图片移除背景`)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.response?.data?.detail || '抠图失败')
  } finally {
    removingBg.value = false
  }
}
</script>

<style scoped>
.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.progress-container {
  width: 100%;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.progress-text {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.progress-percent {
  font-size: 16px;
  color: #409eff;
  font-weight: bold;
}

.progress-detail {
  display: flex;
  align-items: center;
  margin-top: 10px;
  font-size: 13px;
  color: #909399;
}

.loading-icon {
  margin-right: 8px;
  animation: rotate 2s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>

<style>
/* 全局样式：错误详情对话框 */
.error-detail-dialog {
  max-width: 600px !important;
}

.error-detail-dialog .el-message-box__message {
  white-space: pre-line !important;
  line-height: 1.6 !important;
  text-align: left !important;
}
</style>
