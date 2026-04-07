<template>
  <div class="product-ingest">
    <el-card class="main-card">
      <el-tabs v-model="activeTab" type="border-card" style="height: 100%; display: flex; flex-direction: column;">
        <!-- 单产品入库页签 -->
        <el-tab-pane label="单产品入库" name="single">
          <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
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
                :file-list="fileList"
                accept="image/*"
                list-type="picture-card"
                multiple
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
              <div class="upload-tip">支持上传多张图片，建议上传不同角度的产品图片</div>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="handleSubmit" :loading="submitting" :disabled="progress > 0 && progress < 100">
                <el-icon><Upload /></el-icon>
                {{ submitting ? '入库中...' : '提交入库' }}
              </el-button>
              <el-button @click="resetForm" :disabled="submitting">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>

            <!-- 单产品入库进度条 -->
            <el-form-item v-if="progress > 0 && progress < 100">
              <div class="progress-container">
                <div class="progress-info">
                  <span class="progress-text">正在处理图片 {{ currentImageIndex }} / {{ totalImages }}</span>
                  <span class="progress-percent">{{ progress.toFixed(2) }}%</span>
                </div>
                <el-progress
                  :percentage="progress"
                  :status="progressStatus"
                  :stroke-width="20"
                  :text-inside="true"
                />
                <div class="progress-detail">
                  <el-icon class="loading-icon"><Loading /></el-icon>
                  <span>{{ progressDetail }}</span>
                </div>
              </div>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 批量入库页签 -->
        <el-tab-pane label="批量入库" name="batch">
          <div class="batch-upload-container">
            <!-- 目录结构配置 -->
            <el-card shadow="never" style="margin-bottom: 20px; background-color: #f5f7fa;">
              <template #header>
                <div style="display: flex; align-items: center; gap: 10px;">
                  <el-icon><Setting /></el-icon>
                  <span>目录结构配置</span>
                </div>
              </template>
              <el-form :inline="true" size="small">
                <el-form-item label="目录层级">
                  <el-select v-model="folderStructure" style="width: 200px" @change="handleStructureChange">
                    <el-option label="标准模式 (父/产品/图片)" value="standard" />
                    <el-option label="场景模式 (父/产品/场景/图片)" value="scene" />
                  </el-select>
                </el-form-item>
                <el-form-item v-if="folderStructure === 'scene'" label="场景文件夹名称">
                  <el-input 
                    v-model="sceneFolderNames" 
                    placeholder="多个用逗号分隔，如：正面,侧面,背面"
                    style="width: 300px"
                  />
                  <span style="margin-left: 10px; color: #909399; font-size: 12px;">
                    留空则自动识别所有子文件夹为场景
                  </span>
                </el-form-item>
              </el-form>
            </el-card>

            <!-- 操作按钮组 -->
            <div class="batch-actions">
              <el-button type="primary" size="large" @click="triggerBatchUpload" :disabled="batchSubmitting">
                <el-icon><FolderOpened /></el-icon>
                导入产品
              </el-button>
              <el-button 
                type="success" 
                size="large" 
                @click="startBatchIngest" 
                :disabled="batchResults.length === 0 || batchSubmitting"
                :loading="batchSubmitting"
              >
                <el-icon><Upload /></el-icon>
                {{ batchSubmitting ? '入库中...' : '产品入库' }}
              </el-button>
              <el-button 
                type="warning" 
                size="large" 
                @click="clearBatchProducts" 
                :disabled="batchResults.length === 0 || batchSubmitting"
              >
                <el-icon><Delete /></el-icon>
                清空产品
              </el-button>
            </div>
            <input
              ref="batchInputRef"
              type="file"
              webkitdirectory
              directory
              multiple
              style="display: none"
              @change="handleBatchFileSelect"
            />

            <!-- 批量入库结果表格 -->
            <el-table 
              v-if="batchResults.length > 0" 
              :data="batchResults" 
              style="margin-top: 20px"
              border
              stripe
            >
              <el-table-column prop="folderName" label="文件夹名称" min-width="150" />
              <el-table-column prop="productCode" label="产品编码" width="120" />
              <el-table-column prop="productName" label="产品名称" min-width="150" />
              <el-table-column prop="spec" label="规格" width="100" />
              <el-table-column prop="category" label="分类" width="100" />
              <el-table-column prop="imageCount" label="图片数量" width="100" align="center" />
              <el-table-column label="进度" width="200" align="center">
                <template #default="{ row }">
                  <div v-if="row.status === 'processing' || row.status === 'success' || row.status === 'error'">
                    <el-progress 
                      :percentage="row.progress || 0" 
                      :status="row.status === 'success' ? 'success' : row.status === 'error' ? 'exception' : ''"
                      :stroke-width="16"
                    />
                  </div>
                  <span v-else style="color: #909399">-</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag v-if="row.status === 'pending'" type="info">待处理</el-tag>
                  <el-tag v-else-if="row.status === 'processing'" type="warning">
                    <el-icon class="is-loading"><Loading /></el-icon>
                    处理中
                  </el-tag>
                  <el-tag v-else-if="row.status === 'success'" type="success">成功</el-tag>
                  <el-tag v-else-if="row.status === 'error'" type="danger">失败</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="消息" min-width="150">
                <template #default="{ row }">
                  <span :style="{ color: row.status === 'error' ? '#f56c6c' : '#67c23a' }">
                    {{ row.message || '-' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" align="center" fixed="right">
                <template #default="{ row, $index }">
                  <el-button 
                    type="danger" 
                    size="small" 
                    link
                    @click="removeProduct($index)"
                    :disabled="row.status === 'processing' || batchSubmitting"
                  >
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 批量入库统计 -->
            <el-descriptions 
              v-if="batchResults.length > 0" 
              :column="4" 
              border 
              style="margin-top: 20px"
            >
              <el-descriptions-item label="总数">
                <el-tag>{{ batchResults.length }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="成功">
                <el-tag type="success">{{ successCount }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="失败">
                <el-tag type="danger">{{ failCount }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="成功率">
                <el-tag :type="successRate >= 80 ? 'success' : successRate >= 50 ? 'warning' : 'danger'">
                  {{ successRate }}%
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-tab-pane>
      </el-tabs>
    
    <!-- 入库结果 -->
    <el-card v-if="ingestResult" class="result-card">
      <template #header>
        <span>入库结果</span>
      </template>
      
      <el-result
        :icon="ingestResult.success ? 'success' : 'error'"
        :title="ingestResult.message"
      >
        <template #extra>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="产品编码">{{ ingestResult.product_code }}</el-descriptions-item>
            <el-descriptions-item label="成功数量">
              <el-tag type="success">{{ ingestResult.success_count }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="失败数量">
              <el-tag :type="ingestResult.fail_count > 0 ? 'danger' : 'info'">
                {{ ingestResult.fail_count }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="耗时">{{ ingestResult.ingest_time_ms }}ms</el-descriptions-item>
          </el-descriptions>
          
          <div v-if="ingestResult.errors && ingestResult.errors.length > 0" class="error-list">
            <el-divider>错误详情</el-divider>
            <el-alert
              v-for="(error, index) in ingestResult.errors"
              :key="index"
              type="error"
              :closable="false"
              show-icon
              style="margin-bottom: 10px"
            >
              <template #title>
                <div class="error-title">{{ error }}</div>
              </template>
            </el-alert>

            <!-- 显示解决建议 -->
            <el-alert
              v-if="ingestResult.suggestion"
              type="info"
              :closable="false"
              show-icon
              style="margin-top: 15px"
            >
              <template #title>
                <div class="suggestion-title">
                  <el-icon><InfoFilled /></el-icon>
                  解决建议: {{ ingestResult.suggestion }}
                </div>
              </template>
            </el-alert>
          </div>
        </template>
      </el-result>
    </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ingestProduct } from '../api/search'
import { showSuccess, handleApiError } from '../utils/messageHandler'

// 表单数据
const form = reactive({
  product_code: '',
  name: '',
  spec: '',
  category: ''
})

// 页签控制
const activeTab = ref('single')

// 文件列表
const fileList = ref([])
const submitting = ref(false)
const ingestResult = ref(null)
const formRef = ref(null)

// 进度条相关
const progress = ref(0)
const currentImageIndex = ref(0)
const totalImages = ref(0)
const progressStatus = ref('')
const progressDetail = ref('准备上传...')

// 批量入库相关
const batchInputRef = ref(null)
const batchSubmitting = ref(false)
const batchProgress = ref(0)
const batchResults = ref([])
const successCount = ref(0)
const failCount = ref(0)

// 目录结构配置
const folderStructure = ref('standard') // 'standard' | 'scene'
const sceneFolderNames = ref('') // 场景文件夹名称，逗号分隔
const currentProductIndex = ref(0)
const totalProducts = ref(0)
const batchProgressStatus = ref('')
const batchProgressDetail = ref('准备处理...')

// 计算成功率
const successRate = computed(() => {
  const total = successCount.value + failCount.value
  return total > 0 ? ((successCount.value / total) * 100).toFixed(2) : 0
})

// 表单验证规则
const rules = {
  product_code: [
    { required: true, message: '请输入产品编码', trigger: 'blur' }
  ]
}

// 文件变化处理
const handleFileChange = (file, files) => {
  fileList.value = files
}

// 文件移除处理
const handleFileRemove = (file, files) => {
  fileList.value = files
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
      // 模拟上传进度
      const simulateProgress = () => {
        return new Promise((resolve) => {
          const interval = setInterval(() => {
            if (progress.value < 30) {
              // 上传阶段 0-30%
              progress.value += Math.random() * 5
              progressDetail.value = '正在上传图片...'
            } else if (progress.value < 70) {
              // 处理阶段 30-70%
              currentImageIndex.value = Math.min(
                Math.floor((progress.value - 30) / 40 * totalImages.value) + 1,
                totalImages.value
              )
              progress.value += Math.random() * 3
              progressDetail.value = `正在处理图片 ${currentImageIndex.value}/${totalImages.value}...`
            } else if (progress.value < 95) {
              // 特征提取阶段 70-95%
              progress.value += Math.random() * 2
              progressDetail.value = '正在提取图片特征...'
            } else {
              // 完成阶段
              clearInterval(interval)
              resolve()
            }

            if (progress.value > 100) progress.value = 100
          }, 100)
        })
      }

      // 同时执行进度模拟和实际上传
      const [uploadResponse] = await Promise.all([
        ingestProduct(
          form.product_code,
          form.name,
          fileList.value.map(f => f.raw),
          form.spec,
          form.category
        ),
        simulateProgress()
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
      }
    } catch (error) {
      handleApiError(error.response || error, '入库失败')
      progressStatus.value = 'exception'
      progressDetail.value = '入库失败'
    } finally {
      // 延迟重置进度条,让用户看到完成状态
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
  if (submitting.value || batchSubmitting.value) return

  formRef.value?.resetFields()
  fileList.value = []
  ingestResult.value = null
  progress.value = 0
  currentImageIndex.value = 0
  totalImages.value = 0
  progressStatus.value = ''
  progressDetail.value = '准备上传...'
}

// 监听页签切换，清空批量入库数据
watch(activeTab, (newTab) => {
  if (newTab === 'batch' && !batchSubmitting.value) {
    // 如果不在处理中，可以选择是否清空数据
    // 这里保留数据，让用户可以查看历史结果
  }
})

// 触发批量上传
const triggerBatchUpload = () => {
  if (batchInputRef.value) {
    batchInputRef.value.click()
  }
}

// 处理目录结构变化
const handleStructureChange = () => {
  // 当切换模式时，如果已有数据，提示用户
  if (batchResults.value.length > 0) {
    ElMessageBox.confirm(
      '切换目录结构将清空当前产品列表，是否继续？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      clearBatchProducts()
    }).catch(() => {
      // 取消操作，恢复原值
      // 这里不做处理，让用户手动改回
    })
  }
}

// 处理批量文件选择
const handleBatchFileSelect = async (event) => {
  const files = event.target.files
  if (!files || files.length === 0) {
    ElMessage.warning('未选择任何文件')
    return
  }

  // 按文件夹分组文件
  const folderMap = new Map()
  
  for (const file of files) {
    // webkitRelativePath 格式: "parentFolder/productFolder/filename.png" 或 "parentFolder/productFolder/sceneFolder/filename.png"
    const pathParts = file.webkitRelativePath.split('/')
    
    let folderName = ''
    
    if (folderStructure.value === 'standard') {
      // 标准模式：父/产品/图片
      // 路径长度 >= 3: parent/product/file.png -> 取 pathParts[1]
      // 路径长度 == 2: product/file.png -> 取 pathParts[0]
      if (pathParts.length >= 3) {
        folderName = pathParts[1]
      } else if (pathParts.length === 2) {
        folderName = pathParts[0]
      } else {
        continue
      }
    } else if (folderStructure.value === 'scene') {
      // 场景模式：父/产品/场景/图片
      // 路径长度 >= 4: parent/product/scene/file.png -> 取 pathParts[1]
      // 路径长度 == 3: product/scene/file.png -> 取 pathParts[0]
      if (pathParts.length >= 4) {
        folderName = pathParts[1]
      } else if (pathParts.length === 3) {
        folderName = pathParts[0]
      } else {
        continue
      }
      
      // 如果配置了场景文件夹名称，需要验证
      if (sceneFolderNames.value.trim()) {
        const configuredScenes = sceneFolderNames.value.split(',').map(s => s.trim()).filter(s => s)
        if (configuredScenes.length > 0) {
          const sceneFolder = pathParts[pathParts.length - 2] // 倒数第二个是场景文件夹
          if (!configuredScenes.includes(sceneFolder)) {
            // 跳过不在配置中的场景文件夹
            continue
          }
        }
      }
    }
    
    if (!folderName) {
      continue
    }
    
    if (!folderMap.has(folderName)) {
      folderMap.set(folderName, [])
    }
    
    // 只处理图片文件
    if (file.type.startsWith('image/')) {
      folderMap.get(folderName).push(file)
    }
  }

  if (folderMap.size === 0) {
    ElMessage.warning('未找到有效的产品文件夹')
    return
  }

  // 将文件夹信息添加到表格中（不立即执行入库）
  for (const [folderName, imageFiles] of folderMap.entries()) {
    // 检查是否已存在
    const exists = batchResults.value.some(item => item.folderName === folderName)
    if (exists) {
      ElMessage.warning(`文件夹 ${folderName} 已存在，跳过`)
      continue
    }

    // 解析文件夹名称：编码_名称_规格_分类
    const parts = folderName.split('_')
    const productCode = parts[0]?.trim() || folderName
    const productName = parts[1]?.trim() || productCode
    const spec = parts[2]?.trim() || ''
    const category = parts[3]?.trim() || ''

    console.log('解析产品:', {
      folderName,
      productCode,
      productName,
      spec,
      category,
      imageCount: imageFiles.length
    })

    batchResults.value.push({
      folderName,
      productCode,
      productName,
      spec,
      category,
      imageCount: imageFiles.length,
      status: 'pending',
      progress: 0,
      message: '',
      imageFiles: imageFiles  // 保存文件引用供后续入库使用
    })
  }

  ElMessage.success(`成功导入 ${folderMap.size} 个产品，请确认后点击“产品入库”`)
  
  // 清空文件输入
  event.target.value = ''
}

// 开始批量入库
const startBatchIngest = async () => {
  if (batchResults.value.length === 0) {
    ElMessage.warning('请先导入产品')
    return
  }

  // 确认入库
  try {
    await ElMessageBox.confirm(
      `确认对 ${batchResults.value.length} 个产品执行入库操作？`,
      '入库确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }

  // 开始批量入库
  batchSubmitting.value = true
  batchProgress.value = 0
  currentProductIndex.value = 0
  totalProducts.value = batchResults.value.length
  batchProgressStatus.value = ''
  batchProgressDetail.value = '准备处理...'
  
  // 重置统计
  successCount.value = 0
  failCount.value = 0

  try {
    let index = 0
    for (const product of batchResults.value) {
      // 跳过已经成功的
      if (product.status === 'success') {
        index++
        continue
      }

      currentProductIndex.value++
      index++
      
      batchProgressDetail.value = `正在处理: ${product.folderName}`
      
      // 更新当前处理项的状态为 processing
      product.status = 'processing'
      product.progress = 0

      try {
        // 模拟单个产品的上传进度
        const simulateProductProgress = () => {
          return new Promise((resolve) => {
            let currentProgress = 0
            const interval = setInterval(() => {
              if (currentProgress < 30) {
                currentProgress += Math.random() * 8
              } else if (currentProgress < 70) {
                currentProgress += Math.random() * 6
              } else if (currentProgress < 95) {
                currentProgress += Math.random() * 4
              } else {
                currentProgress = 95
                clearInterval(interval)
                resolve()
                return
              }
              
              if (currentProgress > 95) currentProgress = 95
              product.progress = Math.floor(currentProgress)
            }, 80)
          })
        }

        // 同时执行进度模拟和实际上传
        const [response] = await Promise.all([
          ingestProduct(product.productCode, product.productName, product.imageFiles, product.spec, product.category),
          simulateProductProgress()
        ])
        
        // 完成进度
        product.progress = 100
        
        // 更新表格中的状态
        if (response.success) {
          product.status = 'success'
          // 如果有部分图片失败，显示详细信息
          if (response.fail_count > 0) {
            product.message = `${response.success_count} 张成功, ${response.fail_count} 张失败`
            if (response.errors && response.errors.length > 0) {
              // 只显示前3个错误
              const errorSummary = response.errors.slice(0, 3).join('; ')
              product.message += ` (${errorSummary}${response.errors.length > 3 ? '...' : ''})`
            }
          } else {
            product.message = response.message || '入库成功'
          }
          successCount.value++
        } else {
          product.status = 'error'
          product.message = response.message || '入库失败'
          failCount.value++
        }
      } catch (error) {
        failCount.value++
        product.status = 'error'
        product.progress = 100
        product.message = error.message || '入库失败'
      }

      // 更新总体进度
      batchProgress.value = (currentProductIndex.value / totalProducts.value) * 100
    }

    // 完成
    batchProgress.value = 100
    batchProgressStatus.value = 'success'
    batchProgressDetail.value = `批量入库完成！成功: ${successCount.value}, 失败: ${failCount.value}`

    // 显示结果
    if (failCount.value === 0) {
      ElMessage.success(`批量入库完成！共 ${successCount.value} 个产品`)
    } else {
      ElMessage.warning(`批量入库完成！成功: ${successCount.value}, 失败: ${failCount.value}`)
    }

  } catch (error) {
    batchProgressStatus.value = 'exception'
    batchProgressDetail.value = '批量入库失败'
    handleApiError(error.response || error, '批量入库失败')
  } finally {
    // 延迟重置
    setTimeout(() => {
      batchSubmitting.value = false
      batchProgress.value = 0
      batchProgressStatus.value = ''
      batchProgressDetail.value = '准备处理...'
    }, 3000)
  }
}

// 清空产品表格
const clearBatchProducts = async () => {
  if (batchResults.value.length === 0) {
    return
  }

  try {
    await ElMessageBox.confirm(
      '确认清空所有产品？此操作不可恢复。',
      '清空确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }

  batchResults.value = []
  successCount.value = 0
  failCount.value = 0
  batchProgress.value = 0
  ElMessage.success('已清空产品列表')
}

// 删除单个产品
const removeProduct = (index) => {
  const product = batchResults.value[index]
  if (product.status === 'processing') {
    ElMessage.warning('正在处理的产品不能删除')
    return
  }

  batchResults.value.splice(index, 1)
  ElMessage.success('已删除')
}
</script>

<style scoped>
.product-ingest {
  width: 100%;
  height: 100%;  /* 占满父容器 */
  margin: 0 auto;
  display: flex;
  flex-direction: column;
}

.main-card {
  flex: 1;  /* 自动填充剩余空间 */
  min-height: 0;  /* 允许收缩 */
  overflow: hidden;  /* 防止溢出 */
  display: flex;
  flex-direction: column;
}

/* 让 tabs 占满 card */
:deep(.el-tabs) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

:deep(.el-tabs__content) {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.batch-upload-container {
  padding: 20px;
}

.batch-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-bottom: 20px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.result-card {
  margin-top: 20px;
}

.error-list {
  margin-top: 20px;
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

.error-title {
  word-break: break-word;
  line-height: 1.6;
}

.suggestion-title {
  display: flex;
  align-items: center;
  gap: 8px;
  word-break: break-word;
  line-height: 1.6;
}

.suggestion-title .el-icon {
  flex-shrink: 0;
  color: #409eff;
}
</style>
