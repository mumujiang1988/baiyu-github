<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!--物料分组-->
      <el-col :span="4" :xs="24">
        <el-button @click="resetQuery" style="width: 100%; outline: none; border: none">全部</el-button>
        <div class="head-container">
          <el-tree
            :data="dictionaryLookupOptions"
            :props="{ label: 'name', children: 'children' }"
            :expand-on-click-node="true"
            :filter-node-method="filterNode"
            ref="dictionaryTreeRef"
            node-key="materialgroup"
            highlight-current
            show-overflow-tooltip
            @node-click="handleNodeClick"
          />
        </div>
      </el-col>
      <!--物料数据-->
      <el-col :span="20" :xs="24">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
          <el-form-item label="编码" prop="number">
            <el-input
              v-model="queryParams.number"
              placeholder="请输入编码"
              clearable
              style="width: 200px"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="名称" prop="name">
            <el-input
              v-model="queryParams.name"
              placeholder="请输入名称"
              clearable
              style="width: 200px"
              @keyup.enter="handleQuery"
            />
          </el-form-item>

          <el-form-item label="规格型号" prop="specification">
            <el-input
              v-model="queryParams.specification"
              placeholder="请输入规格型号"
              clearable
              style="width: 200px"
              @keyup.enter="handleQuery"
            />
          </el-form-item>

          <el-form-item label="新老产品" prop="fxlcp">
            <el-select v-model="queryParams.fxlcp" clearable placeholder="请选择新老产品">
              <el-option
                v-for="item in xlproductsTree"
                :key="item.value || item.fxlcp"
                :label="item.name"
                :value="item.fxlcp"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="物料属性" prop="erpClsId">
            <el-select v-model="queryParams.erpClsId" clearable placeholder="请选择物料属性">
              <el-option
                v-for="item in materialDictionarys"
                :key="item.value || item.erpClsId"
                :label="item.name"
                :value="item.erpClsId"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="物料产品类别" label-width="100px" prop="productCategory">
            <el-select v-model="queryParams.productCategory" clearable placeholder="请选择产品类别">
              <el-option
                v-for="item in productCategoryTree"
                :key="item.value || item.productCategory"
                :label="item.name"
                :value="item.productCategory"
              />
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button
              type="primary"
              plain
              icon="Plus"
              @click="handleAdd"
              v-hasPermi="['k3:material:add']"
            >物料新增</el-button>
          </el-col>

          <el-col :span="1.5">
            <el-button
              type="success"
              plain
              icon="Edit"
              :disabled="single"
              @click="handleUpdate"
              v-hasPermi="['k3:material:edit']"
            >修改</el-button>
          </el-col>

          <!-- 新增复制按钮 -->
          <el-col :span="1.5">
            <el-button
              type="primary"
              plain
              icon="CopyDocument"
              :disabled="single"
              @click="handleCopy"
              v-hasPermi="['k3:material:add']"
            >复制</el-button>
          </el-col>

          <el-col :span="1.5">
            <el-button
              type="success"
              plain
              icon="Logs"
              :disabled="single"
              @click="handleViewAuditLogs"
              v-hasPermi="['k3:material:auditLogs']"
            >日志</el-button>
          </el-col>

          <el-col :span="1.5">
            <el-button
              type="success"
              plain
              :disabled="single"
              @click="AIintelligentanalysis"
              v-hasPermi="['k3:material:auditLogs']"
            >AI智能分析</el-button>
          </el-col>

          <el-col :span="1.5">
            <el-button
              type="danger"
              plain
              icon="Delete"
              :disabled="multiple"
              @click="handleDelete"
              v-hasPermi="['k3:material:remove']"
            >删除</el-button>
          </el-col>

          <el-col :span="1.5">
            <el-button
              type="info"
              plain
              icon="Upload"
              @click="handleImport"
              v-hasPermi="['k3:material:importExcel']"
            >导入</el-button>
          </el-col>

          <el-col :span="1.5">
            <el-button
              type="warning"
              plain
              icon="Download"
              @click="handleExport"
              v-hasPermi="['k3:material:export']"
            >导出</el-button>
          </el-col>
          <!--     交付红线     -->
          <el-col :span="4">
            <div class="delivery-section">
              <el-tree
                ref="deliveryTree"
                style="width: 60%; max-width: 400px; height: auto;"
                v-model:checked-keys="checkedKeys"
                :data="treeData"
                node-key="deliveryState"
                highlight-current
                @node-click="handleCheck"
                :disabled="multiple"
                :props="{ label: 'label', children: 'children' }"
                :default-expanded-keys="['defaultExpandedKey']"
                :show-checkbox="false"
              />
            </div>
          </el-col>

          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="materialList"
          @selection-change="handleSelectionChange"
          @sort-change="handleSortChange"
          :default-sort="{prop: 'number', order: 'ascending'}"
        >
          <el-table-column type="selection" width="55" align="center" />

          <el-table-column label="图片" width="120">
            <template #default="scope">
              <div
                class="image-hover-container"
                @mouseenter="handleMouseEnter(scope.row.image)"
                @mouseleave="handleMouseLeave"
              >
                <el-image
                  style="width: 80px; height: 80px"
                  :src="scope.row.image"
                  :preview-src-list="[scope.row.image]"
                  fit="cover"
                  :preview-teleported="true"
                  ref="imageRef"
                />
              </div>
            </template>
          </el-table-column>

          <!-- 编码列添加排序功能 -->
          <el-table-column label="编码" align="center" width="120px" prop="number" sortable="custom" :sort-orders="['ascending', 'descending']"/>
          <el-table-column label="名称" align="center" width="170px" prop="name" show-overflow-tooltip />
          <el-table-column label="英文品名" align="center" width="170px" prop="englishProductName" show-overflow-tooltip />
          <el-table-column label="规格型号" align="center" width="160px" prop="specification" show-overflow-tooltip />
          <el-table-column label="原英文描述" align="center" width="120px" prop="description1" show-overflow-tooltip />
          <el-table-column label="新老产品" align="center" width="84px" prop="fxlcpName" />
          <el-table-column label="现英文描述" align="center" width="120px" prop="description2" show-overflow-tooltip />
          <el-table-column label="物料分组" align="center" width="115px" prop="materialgroup" show-overflow-tooltip />
          <el-table-column label="物料属性" align="center" width="84px" prop="erpName" />
          <el-table-column label="单个体积" align="center" width="84px" prop="volume" />
          <el-table-column label="HS编码" align="center" prop="hsbm" />
          <el-table-column label="旧物料编码" align="center" width="120px" prop="formerNumber" />
          <el-table-column label="产品类别" align="center" width="84px" prop="productName" />
          <!--     交付红线     -->
          <el-table-column prop="deliveryState" width="84px" label="交付红线 ">
            <template #default="{ row }">
              <!-- 根据不同的状态显示不同的标签 -->
              <el-tag v-if="row.deliveryState === '1'" type="primary">常规</el-tag>
              <el-tag v-else-if="row.deliveryState === '2'" type="warning">低风险</el-tag>
              <el-tag v-else-if="row.deliveryState === '3'" type="success">中风险</el-tag>
              <el-tag v-else-if="row.deliveryState === '4'" type="danger">高风险</el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="fstate" width="84px" label="物料状态">
            <template #default="{ row }">
              <!-- 根据不同的状态显示不同的标签 -->
              <el-tag v-if="row.fstate === 'A'" type="primary">启用</el-tag>
              <el-tag v-else-if="row.fstate === 'B'" type="info">停用</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="创建人" align="center" prop="creator" show-overflow-tooltip />
          <el-table-column label="创建时间" align="center" width="105px" prop="creator_time" />
        </el-table>

        <pagination
          v-show="total > 0"
          :total="total"
          v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
        />
      </el-col>
    </el-row>

    <!-- 新增物料 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form :model="form" :rules="rules" ref="materialRef" label-width="80px">
        <!-- 图片上传 -->
        <el-row>
          <el-col :span="12">
            <el-form-item label="物料图片">
              <div
                class="upload-area"
                @click="triggerImageUpload"
                @dragover.prevent="handleDragOver"
                @dragleave.prevent="handleDragLeave"
                @drop.prevent="handleImageDrop"
                @paste="handleImagePaste"
                :class="{ 'drag-over': isImageDragOver }"
              >
                <div v-if="imagePreviewUrl || form.image" class="preview-container">
                  <el-image
                    style="width: 100px; height: 100px"
                    :src="imagePreviewUrl || form.image"
                    fit="cover"
                    class="preview-image"
                  />
                  <div class="preview-overlay">
                    <el-icon class="edit-icon"><Edit /></el-icon>
                    <div class="preview-tip">点击更换图片</div>
                  </div>
                </div>
                <div v-else class="upload-placeholder">
                  <el-icon class="upload-icon"><UploadFilled /></el-icon>
                  <div class="upload-tips">
                    <div>点击或拖拽图片到此处</div>
                    <div class="small-tip">支持 JPG、PNG 格式，大小不超过 5MB</div>
                    <div class="small-tip">也可直接粘贴图片</div>
                  </div>
                </div>
                <input
                  type="file"
                  ref="imageInputRef"
                  style="display: none"
                  accept="image/*"
                  @change="handleImageFileSelect"
                />
              </div>
              <div class="upload-actions" v-if="imagePreviewUrl || form.image">
                <el-button type="primary" size="small" @click="triggerImageUpload">
                  <el-icon><UploadFilled /></el-icon> 更换图片
                </el-button>
                <el-button type="danger" size="small" @click="removeImage" v-if="form.image || selectedImage">
                  <el-icon><Delete /></el-icon> 移除
                </el-button>
              </div>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="检测报告">
              <div
                class="upload-area"
                @click="triggerReportUpload"
                @dragover.prevent="handleReportDragOver"
                @dragleave.prevent="handleReportDragLeave"
                @drop.prevent="handleReportDrop"
                @paste="handleReportPaste"
                :class="{ 'drag-over': isReportDragOver }"
              >
                <div v-if="reportPreviewInfo || form.inspectionReport" class="file-preview">
                  <el-icon class="file-icon" size="40">
                    <Document v-if="reportPreviewInfo?.type === 'pdf'" />
                    <Document v-else-if="reportPreviewInfo?.type === 'doc' || reportPreviewInfo?.type === 'docx'" />
                    <Document v-else />
                  </el-icon>
                  <div class="file-info">
                    <div class="file-name">{{ reportPreviewInfo?.name || '检测报告' }}</div>
                    <div class="file-size" v-if="reportPreviewInfo?.size">
                      {{ formatFileSize(reportPreviewInfo.size) }}
                    </div>
                  </div>
                </div>
                <div v-else class="upload-placeholder">
                  <el-icon class="upload-icon"><UploadFilled /></el-icon>
                  <div class="upload-tips">
                    <div>点击或拖拽文件到此处</div>
                    <div class="small-tip">支持 PDF、DOC、DOCX 格式</div>
                    <div class="small-tip">大小不超过 10MB</div>
                  </div>
                </div>
                <input
                  type="file"
                  ref="reportInputRef"
                  style="display: none"
                  accept=".pdf,.doc,.docx"
                  @change="handleReportFileSelect"
                />
              </div>
              <div class="upload-actions" v-if="reportPreviewInfo || form.inspectionReport">
                <el-button type="primary" size="small" @click="triggerReportUpload">
                  <el-icon><UploadFilled /></el-icon> 更换文件
                </el-button>
                <el-button type="danger" size="small" @click="removeReport" v-if="form.inspectionReport || selectedReport">
                  <el-icon><Delete /></el-icon> 移除
                </el-button>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="编码" prop="number">
              <el-input v-model="form.number" placeholder="请输入编码" maxlength="30" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入名称" maxlength="11" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="规格型号" prop="specification">
              <el-input v-model="form.specification" placeholder="请输入规格型号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物料属性" prop="erpClsId">
              <el-select v-model="form.erpClsId" placeholder="请选择物料属性">
                <el-option
                  v-for="item in materialDictionarys"
                  :key="item.value || item.erpClsId"
                  :label="item.name"
                  :value="item.erpClsId"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="新老产品" prop="fxlcp">
              <el-select v-model="form.fxlcp" placeholder="请选择新老产品">
                <el-option
                  v-for="item in xlproductsTree"
                  :key="item.value || item.fxlcp"
                  :label="item.name"
                  :value="item.fxlcp"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单个体积" prop="volume">
              <el-input v-model="form.volume" placeholder="请输入单个体积" maxlength="30" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="交期红线" prop="deliveryState">
              <el-select v-model="form.deliveryState" placeholder="请选择">
                <el-option
                  v-for="item in options"
                  :key="item.deliveryState"
                  :label="item.label"
                  :value="item.deliveryState"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="英文品名" prop="englishProductName">
              <el-input v-model="form.englishProductName" placeholder="请输入英文品名" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="物料分组" prop="materialgroup">
              <el-tooltip
                :content="form.materialgroup || '请选择'"
                placement="top"
                :disabled="!form.materialgroup"
              >
                <el-tree-select
                  v-model="form.materialgroup"
                  :data="dictionaryLookupOptions"
                  :props="{ value: 'materialgroup', label: 'name', children: 'children' }"
                  value-key="materialgroup"
                  placeholder="请选择物料分组"
                  check-strictly
                />
              </el-tooltip>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="HS编码" prop="hsbm">
              <el-input v-model="form.hsbm" placeholder="请输入HS编码" maxlength="11" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="物料产品类别" label-width="100px" prop="productCategory">
              <el-select v-model="form.productCategory" placeholder="请选择产品类别">
                <el-option
                  v-for="item in productCategoryTree"
                  :key="item.value || item.productCategory"
                  :label="item.name"
                  :value="item.productCategory"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="旧物料编码" label-width="100px" prop="formerNumber">
              <el-input v-model="form.formerNumber" placeholder="请输入旧物料编码" maxlength="30" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-col :span="24">
          <!-- 原英文描述 -->
          <el-form-item label="原英文描述" label-width="100px" prop="description1">
            <el-input v-model="form.description1" type="textarea" :rows="3" placeholder="请输入原英文描述" />
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <!-- 现英文描述 -->
          <el-form-item label="现英文描述" label-width="100px" prop="description2">
            <el-input
              v-model="form.description2"
              type="textarea"
              :rows="4"
              placeholder="请输入现英文描述"
            />
            <el-button
              type="primary"
              size="small"
              style="margin-top: 8px"
              @click="handleAIFillEnglishDesc()"
              :loading="aiLoading"
            >
              AI生成英文描述
            </el-button>
          </el-form-item>
        </el-col>

        <el-row>
          <el-col :span="12">
            <el-form-item label="物料状态" prop="fstate">
              <el-select v-model="form.fstate" placeholder="请选择物料状态">
                <el-option
                  v-for="dict in material_status"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 更新 -->
    <el-dialog :title="title" v-model="opens" width="600px" append-to-body>
      <el-form :model="form" :rules="rules" ref="materialRef" label-width="80px">
        <!-- 图片上传 -->
        <el-row>
          <el-col :span="12">
            <el-form-item label="物料图片">
              <div
                class="upload-area"
                @click="triggerImageUploadEdit"
                @dragover.prevent="handleDragOverEdit"
                @dragleave.prevent="handleDragLeaveEdit"
                @drop.prevent="handleImageDropEdit"
                @paste="handleImagePasteEdit"
                :class="{ 'drag-over': isImageDragOverEdit }"
              >
                <!-- 当前图片预览 -->
                <div v-if="form.image" class="preview-container">
                  <el-image
                    style="width: 100px; height: 100px"
                    :src="form.image"
                    fit="cover"
                    class="preview-image"
                  />
                  <div class="preview-overlay">
                    <el-icon class="edit-icon"><Edit /></el-icon>
                    <div class="preview-tip">点击更换图片</div>
                  </div>
                </div>
                <div v-else-if="imagePreviewUrlEdit" class="preview-container">
                  <el-image
                    style="width: 100px; height: 100px"
                    :src="imagePreviewUrlEdit"
                    fit="cover"
                    class="preview-image"
                  />
                  <div class="preview-overlay">
                    <el-icon class="edit-icon"><Edit /></el-icon>
                    <div class="preview-tip">点击更换图片</div>
                  </div>
                </div>
                <div v-else class="upload-placeholder">
                  <el-icon class="upload-icon"><UploadFilled /></el-icon>
                  <div class="upload-tips">
                    <div>点击或拖拽图片到此处</div>
                    <div class="small-tip">支持 JPG、PNG 格式</div>
                    <div class="small-tip">也可直接粘贴图片</div>
                  </div>
                </div>
                <input
                  type="file"
                  ref="imageInputRefEdit"
                  style="display: none"
                  accept="image/*"
                  @change="handleImageFileSelectEdit"
                />
              </div>
              <div class="upload-actions" v-if="form.image || imagePreviewUrlEdit">
                <el-button type="primary" size="small" @click="triggerImageUploadEdit">
                  <el-icon><UploadFilled /></el-icon> 更换图片
                </el-button>
                <el-button type="danger" size="small" @click="removeImageEdit">
                  <el-icon><Delete /></el-icon> 移除
                </el-button>
              </div>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="检测报告">
              <div
                class="upload-area"
                @click="triggerReportUploadEdit"
                @dragover.prevent="handleReportDragOverEdit"
                @dragleave.prevent="handleReportDragLeaveEdit"
                @drop.prevent="handleReportDropEdit"
                @paste="handleReportPasteEdit"
                :class="{ 'drag-over': isReportDragOverEdit }"
              >
                <div v-if="reportPreviewInfoEdit || form.inspectionReport" class="file-preview">
                  <el-icon class="file-icon" size="40">
                    <Document v-if="(reportPreviewInfoEdit?.type || '').includes('pdf')" />
                    <Document v-else-if="(reportPreviewInfoEdit?.type || '').includes('doc')" />
                    <Document v-else />
                  </el-icon>
                  <div class="file-info">
                    <div class="file-name">{{ reportPreviewInfoEdit?.name || (form.inspectionReport ? '检测报告' : '') }}</div>
                    <div class="file-size" v-if="reportPreviewInfoEdit?.size">
                      {{ formatFileSize(reportPreviewInfoEdit.size) }}
                    </div>
                  </div>
                </div>
                <div v-else class="upload-placeholder">
                  <el-icon class="upload-icon"><UploadFilled /></el-icon>
                  <div class="upload-tips">
                    <div>点击或拖拽文件到此处</div>
                    <div class="small-tip">支持 PDF、DOC、DOCX 格式</div>
                  </div>
                </div>
                <input
                  type="file"
                  ref="reportInputRefEdit"
                  style="display: none"
                  accept=".pdf,.doc,.docx"
                  @change="handleReportFileSelectEdit"
                />
              </div>
              <div class="upload-actions" v-if="reportPreviewInfoEdit || form.inspectionReport">
                <el-button type="primary" size="small" @click="triggerReportUploadEdit">
                  <el-icon><UploadFilled /></el-icon> 更换文件
                </el-button>
                <el-button type="danger" size="small" @click="removeReportEdit">
                  <el-icon><Delete /></el-icon> 移除
                </el-button>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="编码" prop="number">
              <el-input v-model="form.number" placeholder="请输入编码" maxlength="30" disabled />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入名称" maxlength="11" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="规格型号" prop="specification">
              <el-input v-model="form.specification" placeholder="请输入规格型号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物料属性" prop="erpClsId">
              <el-select v-model="form.erpClsId" placeholder="请选择物料属性">
                <el-option
                  v-for="item in materialDictionarys"
                  :key="item.value || item.erpClsId"
                  :label="item.name"
                  :value="item.erpClsId"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="新老产品" prop="fxlcp">
              <el-select v-model="form.fxlcp" placeholder="请选择新老产品">
                <el-option
                  v-for="item in xlproductsTree"
                  :key="item.value || item.fxlcp"
                  :label="item.name"
                  :value="item.fxlcp"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单个体积" prop="volume">
              <el-input v-model="form.volume" placeholder="请输入单个体积" maxlength="30" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="交期红线" prop="deliveryState">
              <el-select v-model="form.deliveryState" placeholder="请选择">
                <el-option
                  v-for="item in options"
                  :key="item.deliveryState"
                  :label="item.label"
                  :value="item.deliveryState"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="英文品名" prop="englishProductName">
              <el-input v-model="form.englishProductName" placeholder="请输入英文品名"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="物料分组" prop="materialgroup">
              <el-tooltip
                :content="form.materialgroup || '请选择'"
                placement="top"
                :disabled="!form.materialgroup"
              >
                <el-tree-select
                  v-model="form.materialgroup"
                  :data="dictionaryLookupOptions"
                  :props="{ value: 'materialgroup', label: 'name', children: 'children' }"
                  value-key="materialgroup"
                  placeholder="请选择物料分组"
                  check-strictly
                />
              </el-tooltip>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="HS编码" prop="hsbm">
              <el-input v-model="form.hsbm" placeholder="请输入HS编码" maxlength="11" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="物料产品类别" label-width="100px" prop="productCategory">
              <el-select v-model="form.productCategory" placeholder="请选择产品类别">
                <el-option
                  v-for="item in productCategoryTree"
                  :key="item.value || item.productCategory"
                  :label="item.name"
                  :value="item.productCategory"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="旧物料编码" prop="formerNumber">
              <el-input v-model="form.formerNumber" placeholder="请输入旧物料编码" maxlength="30" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-col :span="24">
          <!-- 原英文描述 -->
          <el-form-item label="原英文描述" label-width="100px" prop="description1">
            <el-input v-model="form.description1" type="textarea" :rows="3" placeholder="原英文描述" disabled />
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <!-- 现英文描述 -->
          <el-form-item label="现英文描述" label-width="100px" prop="description2">
            <el-input
              v-model="form.description2"
              type="textarea"
              :rows="4"
              placeholder="请输入现英文描述"
            />
            <el-button
              type="primary"
              size="small"
              style="margin-top: 8px"
              @click="handleAIFillEnglishDesc()"
              :loading="aiLoading"
            >
              AI生成英文描述
            </el-button>
          </el-form-item>
        </el-col>

        <el-row>
          <el-col :span="12">
            <el-form-item label="物料状态" prop="fstate">
              <el-select v-model="form.fstate" placeholder="请选择物料状态">
                <el-option
                  v-for="dict in material_status"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitsForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 物料导入对话框 -->
    <el-dialog :title="upload.title" v-model="upload.open" width="400px" append-to-body>
      <el-upload
        ref="uploadRef"
        :limit="1"
        accept=".xlsx, .xls"
        :headers="upload.headers"
        :action="upload.url + '?updateSupport=' + upload.updateSupport"
        :disabled="upload.isUploading"
        :on-progress="handleFileUploadProgress"
        :on-success="handleFileSuccess"
        :auto-upload="false"
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip text-center">
            <div class="el-upload__tip">
              <el-checkbox v-model="upload.updateSupport" />是否更新已经存在的物料数据
            </div>
            <span>仅允许导入xls、xlsx格式文件。</span>
            <el-link type="primary" :underline="false" style="font-size:12px;vertical-align: baseline;" @click="importTemplate">下载模板</el-link>
          </div>
        </template>
      </el-upload>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitFileForm">确 定</el-button>
          <el-button @click="upload.open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 审计日志对话框 -->
    <el-dialog :title="auditLogsTitle" v-model="auditLogsDialog" width="80%" append-to-body>
      <el-table v-loading="auditLogsLoading" :data="auditLogsList" style="width: 100%">
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="operateTime" label="操作时间" width="180">
          <template #default="scope">
            {{ scope.row.operateTime }}
          </template>
        </el-table-column>
        <el-table-column label="操作类型" width="100">
          <template #default="scope">
            {{ scope.row.operationType || '修改' }}
          </template>
        </el-table-column>
        <el-table-column label="变更详情" min-width="400">
          <template #default="scope">
            <div class="changes-container">
              <div v-for="(change, index) in scope.row.changes" :key="index" class="change-item-horizontal">
                <div class="change-field-horizontal">
                  <div class="field-name">{{ change.fieldName }}:</div>
                  <div class="value-container">
                    <div v-if="change.oldValue !== undefined && change.oldValue !== ''" class="old-value-section">
                      <el-tag class="tagWithLine" type="info" size="small">{{ change.oldValue }}</el-tag>
                      <i class="el-icon-arrow-right" style="margin: 0 3px; font-size: 12px;"></i>
                    </div>
                    <el-tag class="new-value" type="primary" size="small">{{ change.newValue }}</el-tag>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <pagination
          v-show="auditLogsTotal > 0"
          :total="auditLogsTotal"
          v-model:page="auditLogsCurrentPage"
          v-model:limit="auditLogsPageSize"
          @pagination="handleAuditLogsPaginationChange"
        />
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Material">
import { getToken } from "@/utils/auth";
import {
  listMaterial, dictionaryLookupTreeSelect, addMaterial, updateMaterials, updateMaterial,
  removeBatch, getByNumber, handleAIFillEnglish, materialDictionary, xlproductTree,
  getProductCategory, getMaterialAuditLogs
} from "@/api/k3/material";
import { UploadFilled, Delete, Edit, Document } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
// 系统开关和物料状态字典
const { sys_normal_disable, material_status } = proxy.useDict("sys_normal_disable", "material_status");

const materialList = ref([]);
// 物料属性
const materialDictionarys = ref([]);
// 新老产品
const xlproductsTree = ref([]);
// 产品类别
const productCategoryTree = ref([]);
// 物料分组
const dictionaryLookupOptions = ref(undefined);

// 新增弹窗
const open = ref(false);
// 编辑弹窗
const opens = ref(false);

const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const fxlcp = ref('');
const value = ref('');
const aiLoading = ref(false);

// 图片相关
const imageRef = ref(null)
let previewTimer = null

// 树形组件引用
const dictionaryTreeRef = ref(null);
// 表格引用
const tableRef = ref(null);

/*** 物料导入参数 */
const upload = reactive({
  open: false,
  title: "",
  isUploading: false,
  updateSupport: 0,
  headers: { Authorization: "Bearer " + getToken() },
  url: import.meta.env.VITE_APP_BASE_API + "/k3/material/importExcel"
});

const options = [
  { deliveryState: '1', label: '常规' },
  { deliveryState: '2', label: '低风险' },
  { deliveryState: '3', label: '中风险' },
  { deliveryState: '4', label: '高风险' }
]

const treeData = ref([
  {
    label: '交期红线',
    deliveryState: 'root',
    children: [
      { label: '常规', deliveryState: '1' },
      { label: '低风险', deliveryState: '2' },
      { label: '中风险', deliveryState: '3' },
      { label: '高风险', deliveryState: '4' }
    ]
  }
])

const checkedKeys = ref([])

// 审计日志相关数据
const auditLogsDialog = ref(false);
const auditLogsList = ref([]);
const auditLogsLoading = ref(false);
const auditLogsTotal = ref(0);
const auditLogsCurrentPage = ref(1);
const auditLogsPageSize = ref(10);
const auditLogsTitle = ref('');
const currentMaterialId = ref(null);

// 新增对话框的上传相关变量
const imageInputRef = ref(null);
const reportInputRef = ref(null);
const imagePreviewUrl = ref('');
const reportPreviewInfo = ref(null);
const isImageDragOver = ref(false);
const isReportDragOver = ref(false);

// 编辑对话框的上传相关变量
const imageInputRefEdit = ref(null);
const reportInputRefEdit = ref(null);
const imagePreviewUrlEdit = ref('');
const reportPreviewInfoEdit = ref(null);
const isImageDragOverEdit = ref(false);
const isReportDragOverEdit = ref(false);

// 原有的 selectedImage 和 selectedReport 变量
let selectedImage = null;
let selectedReport = null;

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    fstate: "A",
    number: undefined,
    name: undefined,
    specification: undefined,
    fxlcp: undefined,
    erpClsId: undefined,
    productCategory: undefined,
    materialgroup: undefined,
    // 添加排序参数
    orderByColumn: undefined,
    isAsc: undefined
  },
  rules: {
    number: [{ required: true, message: "编码不能为空", trigger: "blur" }],
    name: [{ required: true, message: "名称不能为空", trigger: "blur" }],
    specification: [{ required: true, message: "规格型号不能为空", trigger: "blur" }],
    erpClsId: [{ required: true, message: "物料属性不能为空", trigger: "change" }],
    fxlcp: [{ required: true, message: "新老产品不能为空", trigger: "change" }],
    materialgroup: [{ required: true, message: "物料分组不能为空", trigger: "change" }],
    fstate: [{ required: true, message: "物料状态不能为空", trigger: "change" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询物料列表 */
function getList() {
  loading.value = true;
  listMaterial(queryParams.value).then(response => {
    materialList.value = response.records;
    total.value = response.total;
    loading.value = false;
  }).catch(() => {
    loading.value = false;
  });
}

/** 复制按钮操作 */
function handleCopy(row) {
  reset();
  const id = row.id || (ids.value.length === 1 ? ids.value[0] : null);
  getByNumber(id).then(response => {
    const data = response.data;
    form.value = {
      ...data,
      id: undefined, // 清除id
      number: data.number, // 清空编码，让用户重新输入
      image: '', // 清空图片
      inspectionReport: '', // 清空检测报告
      // 保留其他字段用于复制
      name: data.name,
      specification: data.specification,
      erpClsId: data.erpClsId,
      fxlcp: data.fxlcp,
      volume: data.volume,
      deliveryState: data.deliveryState,
      englishProductName: data.englishProductName,
      materialgroup: data.materialgroup,
      hsbm: data.hsbm,
      productCategory: data.productCategory,
      description1: data.description1,
      description2: data.description2,
      formerNumber: data.formerNumber,
      fstate: 'A' // 默认启用状态
    };

    // 清空上传的文件
    selectedImage = null;
    selectedReport = null;
    imagePreviewUrl.value = '';
    reportPreviewInfo.value = null;

    open.value = true;
    title.value = "复制新增物料";
  });
}

/** 批量修交付红线 */
const handleCheck = (deliveryState) => {
  const id = ids.value;
  const state = deliveryState.deliveryState;
  if (!id || typeof id !== 'object' || !Array.isArray(id) || id.length === 0) {
    proxy.$modal.msgWarning('请先选择要操作的物料');
    return;
  }
  proxy.$modal.confirm(`是否确认修改物料交期红线为 ------ ${deliveryState.label}？`)
    .then(() => {
      return updateMaterials(id, state);
    })
    .then(() => {
      getList();
      proxy.$modal.msgSuccess('修改成功');
    })
    .catch(() => {
      // 用户取消或请求失败
    });
}

/** 排序事件处理 */
function handleSortChange(column) {
  if (column.prop && column.order) {
    // 设置排序字段和排序方式
    queryParams.value.orderByColumn = column.prop;
    queryParams.value.isAsc = column.order === 'ascending' ? 'asc' : 'desc';
  } else {
    // 清除排序
    queryParams.value.orderByColumn = undefined;
    queryParams.value.isAsc = undefined;
  }
  // 重新加载数据
  getList();
}

/** 重置排序状态 */
function resetSort() {
  queryParams.value.orderByColumn = undefined;
  queryParams.value.isAsc = undefined;
}

const handleMouseEnter = (imageUrl) => {
  previewTimer = setTimeout(() => {
    if (imageRef.value) {
      const previewInstance = imageRef.value.previewInstance;
      if (previewInstance) {
        previewInstance.setPreviewImages([imageUrl]);
        previewInstance.setIndex(0);
        previewInstance.show();
      }
    }
  }, 300);
}

const handleMouseLeave = () => {
  clearTimeout(previewTimer);
  if (imageRef.value) {
    const previewInstance = imageRef.value.previewInstance;
    if (previewInstance) {
      previewInstance.close();
    }
  }
}

/** 查询选择物料分组 */
function getDictionaryLookup() {
  dictionaryLookupTreeSelect().then(response => {
    dictionaryLookupOptions.value = response.data;
  });
}

/** 通过条件过滤节点  */
const filterNode = (value, data) => {
  if (!value) return true;
  return data.label.indexOf(value) !== -1;
};

/** 节点单击事件 */
function handleNodeClick(data) {
  queryParams.value.materialgroup = data.materialgroup;
  handleQuery();
};

/** 查询物料属性 */
function getMaterialDictionary(){
  const categoryName = '物料属性';
  materialDictionary(categoryName).then(response => {
    materialDictionarys.value = response.data;
  });
}

/** 查询新老产品 */
function getxlproduct(){
  const categoryName = '新老产品';
  xlproductTree(categoryName).then(response => {
    xlproductsTree.value = response.data;
  });
}

/** 查询产品类别 */
function ProductCategory() {
  const categoryName = '产品类别';
  getProductCategory(categoryName).then(response => {
    productCategoryTree.value = response.data;
  });
}

/** 取消按钮 */
function cancel() {
  open.value = false;
  opens.value = false;
  reset();
}

/** 表单重置 */
function reset() {
  form.value = {
    id: undefined,
    number: '',
    name: '',
    specification: '',
    erpClsId: undefined,
    fxlcp: undefined,
    volume: '',
    deliveryState: '1',
    englishProductName: '',
    materialgroup: '',
    hsbm: '',
    productCategory: undefined,
    description1: '',
    description2: '',
    formerNumber: '',
    image: '',
    inspectionReport: '',
    fstate: 'A'
  };
  selectedImage = null;
  selectedReport = null;
  imagePreviewUrl.value = '';
  reportPreviewInfo.value = null;
  imagePreviewUrlEdit.value = '';
  reportPreviewInfoEdit.value = null;
  if (proxy.$refs["materialRef"]) {
    proxy.resetForm("materialRef");
  }
}

/** 查询按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  queryParams.value.materialgroup = undefined;
  // 重置排序状态
  resetSort();

  if (proxy.$refs.dictionaryTreeRef) {
    proxy.$refs.dictionaryTreeRef.setCurrentKey(null);
  }

  // 如果有排序状态，需要重置表格排序
  if (tableRef.value) {
    tableRef.value.clearSort();
  }

  handleQuery();
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  // 获取树形组件当前选中的节点
  if (dictionaryTreeRef.value) {
    const currentNode = dictionaryTreeRef.value.getCurrentNode();
    if (currentNode && currentNode.materialgroup) {
      // 将选中的物料分组值赋给表单
      form.value.materialgroup = currentNode.materialgroup;
    }
  }
  open.value = true;
  title.value = "新增物料";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const id = row.id || (ids.value.length === 1 ? ids.value[0] : null);
  getByNumber(id).then(response => {
    form.value = response.data;
    opens.value = true;
    title.value = "修改物料";
  });
}

/** AI智能分析按钮 */
function AIintelligentanalysis() {
  const id = ids.value.length === 1 ? ids.value[0] : null;
  if (!id) {
    proxy.$modal.msgWarning('请选择一条数据进行分析');
    return;
  }
  proxy.$modal.msgInfo("AI智能分析功能开发中，选中物料ID: " + id);
}

/** AI生成英文描述 */
function handleAIFillEnglishDesc() {
  aiLoading.value = true;
  proxy.$refs["materialRef"].validate(valid => {
    if (valid) {
      handleAIFillEnglish(form.value).then(response => {
        if (response.errorCode === 0) {
          const descriptionText = response.data || response.description1 || response.description || response;
          const finalText = typeof descriptionText === 'string' ? descriptionText : JSON.stringify(descriptionText);
          form.value.description2 = finalText;
          proxy.$modal.msgSuccess("英文描述生成成功");
        } else {
          proxy.$modal.msgError("英文描述生成失败");
        }
        aiLoading.value = false;
      }).catch(() => {
        aiLoading.value = false;
        proxy.$modal.msgError("请求失败");
      });
    } else {
      aiLoading.value = false;
    }
  });
}

/** ========== 新增对话框 - 图片上传相关方法 ========== */
const triggerImageUpload = () => {
  imageInputRef.value?.click();
};

const handleImageFileSelect = (event) => {
  const file = event.target.files[0];
  if (file && file.type.startsWith('image/')) {
    handleImageFile(file);
  } else {
    proxy.$modal.msgError('请选择图片文件');
  }
  event.target.value = '';
};

const handleImageFile = (file) => {
  if (file.size > 5 * 1024 * 1024) {
    proxy.$modal.msgError('图片大小不能超过5MB');
    return;
  }

  selectedImage = file;
  imagePreviewUrl.value = URL.createObjectURL(file);
  // 清空原有的图片URL
  form.value.image = '';
};

const handleDragOver = () => {
  isImageDragOver.value = true;
};

const handleDragLeave = () => {
  isImageDragOver.value = false;
};

const handleImageDrop = (event) => {
  isImageDragOver.value = false;
  const files = event.dataTransfer.files;
  if (files.length > 0) {
    const file = files[0];
    if (file.type.startsWith('image/')) {
      handleImageFile(file);
    } else {
      proxy.$modal.msgError('请拖拽图片文件');
    }
  }
};

const handleImagePaste = (event) => {
  const items = event.clipboardData?.items;
  if (items) {
    for (const item of items) {
      if (item.type.indexOf('image') !== -1) {
        const file = item.getAsFile();
        handleImageFile(file);
        break;
      }
    }
  }
};

const removeImage = () => {
  selectedImage = null;
  imagePreviewUrl.value = '';
  form.value.image = '';
  if (imageInputRef.value) {
    imageInputRef.value.value = '';
  }
};

/** ========== 新增对话框 - 检测报告上传相关方法 ========== */
const triggerReportUpload = () => {
  reportInputRef.value?.click();
};

const handleReportFileSelect = (event) => {
  const file = event.target.files[0];
  if (file && isValidReportFile(file)) {
    handleReportFile(file);
  } else {
    proxy.$modal.msgError('请选择 PDF、DOC 或 DOCX 格式的文件');
  }
  event.target.value = '';
};

const isValidReportFile = (file) => {
  const validTypes = ['application/pdf', 'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
  const validExtensions = ['.pdf', '.doc', '.docx'];
  const fileName = file.name.toLowerCase();

  return validTypes.includes(file.type) ||
    validExtensions.some(ext => fileName.endsWith(ext));
};

const handleReportFile = (file) => {
  if (file.size > 10 * 1024 * 1024) {
    proxy.$modal.msgError('文件大小不能超过10MB');
    return;
  }

  selectedReport = file;
  reportPreviewInfo.value = {
    name: file.name,
    size: file.size,
    type: file.name.split('.').pop().toLowerCase()
  };
  // 清空原有的报告
  form.value.inspectionReport = '';
};

const handleReportDragOver = () => {
  isReportDragOver.value = true;
};

const handleReportDragLeave = () => {
  isReportDragOver.value = false;
};

const handleReportDrop = (event) => {
  isReportDragOver.value = false;
  const files = event.dataTransfer.files;
  if (files.length > 0) {
    const file = files[0];
    if (isValidReportFile(file)) {
      handleReportFile(file);
    } else {
      proxy.$modal.msgError('请拖拽 PDF、DOC 或 DOCX 格式的文件');
    }
  }
};

const handleReportPaste = (event) => {
  // 检测报告通常不支持粘贴，这里可以留空或提示
  proxy.$modal.msgInfo('检测报告暂不支持粘贴功能，请使用拖拽或点击上传');
};

const removeReport = () => {
  selectedReport = null;
  reportPreviewInfo.value = null;
  form.value.inspectionReport = '';
  if (reportInputRef.value) {
    reportInputRef.value.value = '';
  }
};

/** ========== 编辑对话框 - 图片上传相关方法 ========== */
const triggerImageUploadEdit = () => {
  imageInputRefEdit.value?.click();
};

const handleImageFileSelectEdit = (event) => {
  const file = event.target.files[0];
  if (file && file.type.startsWith('image/')) {
    handleImageFileEdit(file);
  } else {
    proxy.$modal.msgError('请选择图片文件');
  }
  event.target.value = '';
};

const handleImageFileEdit = (file) => {
  if (file.size > 5 * 1024 * 1024) {
    proxy.$modal.msgError('图片大小不能超过5MB');
    return;
  }

  selectedImage = file;
  imagePreviewUrlEdit.value = URL.createObjectURL(file);
  // 注意：这里不清空 form.value.image，因为可能还需要保留原图
};

const handleDragOverEdit = () => {
  isImageDragOverEdit.value = true;
};

const handleDragLeaveEdit = () => {
  isImageDragOverEdit.value = false;
};

const handleImageDropEdit = (event) => {
  isImageDragOverEdit.value = false;
  const files = event.dataTransfer.files;
  if (files.length > 0) {
    const file = files[0];
    if (file.type.startsWith('image/')) {
      handleImageFileEdit(file);
    } else {
      proxy.$modal.msgError('请拖拽图片文件');
    }
  }
};

const handleImagePasteEdit = (event) => {
  const items = event.clipboardData?.items;
  if (items) {
    for (const item of items) {
      if (item.type.indexOf('image') !== -1) {
        const file = item.getAsFile();
        handleImageFileEdit(file);
        break;
      }
    }
  }
};

const removeImageEdit = () => {
  selectedImage = null;
  imagePreviewUrlEdit.value = '';
  form.value.image = '';
  if (imageInputRefEdit.value) {
    imageInputRefEdit.value.value = '';
  }
};

/** ========== 编辑对话框 - 检测报告上传相关方法 ========== */
const triggerReportUploadEdit = () => {
  reportInputRefEdit.value?.click();
};

const handleReportFileSelectEdit = (event) => {
  const file = event.target.files[0];
  if (file && isValidReportFile(file)) {
    handleReportFileEdit(file);
  } else {
    proxy.$modal.msgError('请选择 PDF、DOC 或 DOCX 格式的文件');
  }
  event.target.value = '';
};

const handleReportFileEdit = (file) => {
  if (file.size > 10 * 1024 * 1024) {
    proxy.$modal.msgError('文件大小不能超过10MB');
    return;
  }

  selectedReport = file;
  reportPreviewInfoEdit.value = {
    name: file.name,
    size: file.size,
    type: file.name.split('.').pop().toLowerCase()
  };
};

const handleReportDragOverEdit = () => {
  isReportDragOverEdit.value = true;
};

const handleReportDragLeaveEdit = () => {
  isReportDragOverEdit.value = false;
};

const handleReportDropEdit = (event) => {
  isReportDragOverEdit.value = false;
  const files = event.dataTransfer.files;
  if (files.length > 0) {
    const file = files[0];
    if (isValidReportFile(file)) {
      handleReportFileEdit(file);
    } else {
      proxy.$modal.msgError('请拖拽 PDF、DOC 或 DOCX 格式的文件');
    }
  }
};

const handleReportPasteEdit = (event) => {
  proxy.$modal.msgInfo('检测报告暂不支持粘贴功能，请使用拖拽或点击上传');
};

const removeReportEdit = () => {
  selectedReport = null;
  reportPreviewInfoEdit.value = null;
  form.value.inspectionReport = '';
  if (reportInputRefEdit.value) {
    reportInputRefEdit.value.value = '';
  }
};

/** 提交新增表单 */
function submitForm() {
  proxy.$refs["materialRef"].validate(valid => {
    if (valid) {
      addMaterial(form.value, selectedImage, selectedReport).then(response => {
        if (response.errorCode === 0) {
          proxy.$modal.msgSuccess("新增成功");
          selectedImage = null;
          selectedReport = null;
          open.value = false;
          getList();
        } else {
          proxy.$modal.msgError("物料添加失败");
        }
      }).catch(() => {
        proxy.$modal.msgError("请求失败");
      });
    }
  });
}

/** 提交修改表单 */
function submitsForm() {
  proxy.$refs["materialRef"].validate(valid => {
    if (valid) {
      if (form.value.id != undefined) {
        updateMaterial(form.value, selectedImage, selectedReport).then(response => {
          proxy.$modal.msgSuccess("更新成功");
          opens.value = false;
          getList();
        }).catch(() => {
          proxy.$modal.msgError("更新失败");
        });
      } else {
        proxy.$modal.msgError("更新失败");
        opens.value = false;
        getList();
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const id = row.id || ids.value;
  proxy.$modal.confirm('是否确认删除物料编号为"' + id + '"的数据项？').then(function() {
    return removeBatch(ids.value);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("/k3/material/export", {
    ...queryParams.value
  }, `bymaterial_${new Date().getTime()}.xlsx`);
}

/** 导入按钮操作 */
function handleImport() {
  upload.title = "物料导入";
  upload.open = true;
};

/** 文件上传中处理 */
const handleFileUploadProgress = (event, file, fileList) => {
  upload.isUploading = true;
};

/** 文件上传成功处理 */
const handleFileSuccess = (response, file, fileList) => {
  upload.open = false;
  upload.isUploading = false;
  proxy.$refs["uploadRef"].handleRemove(file);
  if (response.errorCode == 1){
    proxy.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.message + "</div>", { dangerouslyUseHTMLString: true });
  } else {
    proxy.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.data + "</div>", "导入结果", { dangerouslyUseHTMLString: true });
  }
  getList();
};

/** 下载模板操作 */
function importTemplate() {
  proxy.download("/k3/material/importTemplate", {}, `material_template_${new Date().getTime()}.xlsx`);
};

/** 提交上传文件 */
function submitFileForm() {
  proxy.$refs["uploadRef"].submit();
};

/** 查看审计日志 */
function handleViewAuditLogs(row) {
  const id = row.id || (ids.value.length === 1 ? ids.value[0] : null);
  if (!id) {
    proxy.$modal.msgWarning('请选择一条数据查看日志');
    return;
  }
  currentMaterialId.value = id;
  auditLogsTitle.value = `物料 [${row.number}] 审计日志`;
  auditLogsCurrentPage.value = 1;
  getMaterialAuditLogsList();
  auditLogsDialog.value = true;
}

/** 获取审计日志列表 */
async function getMaterialAuditLogsList() {
  try {
    auditLogsLoading.value = true;
    const response = await getMaterialAuditLogs(
      currentMaterialId.value,
      auditLogsCurrentPage.value,
      auditLogsPageSize.value
    );

    if (response.code === 200) {
      auditLogsList.value = response.data.records || [];
      auditLogsTotal.value = response.data.total || 0;

      // 处理审计日志数据，解析变更内容
      auditLogsList.value = auditLogsList.value.map(log => {
        let changes = [];
        if (log.diffJson) {
          try {
            const contentObj = JSON.parse(log.diffJson);
            for (const [key, value] of Object.entries(contentObj)) {
              if (value && typeof value === 'object') {
                const changeInfo = {
                  field: key,
                  fieldName: getFieldName(key),
                  oldValue: value.old !== undefined ? value.old : '',
                  newValue: value.new !== undefined ? value.new : ''
                };
                changes.push(changeInfo);
              }
            }
          } catch (e) {
            console.error('解析审计日志内容失败:', e);
            changes.push({
              field: 'unknown',
              fieldName: '未知字段',
              oldValue: '',
              newValue: log.diffJson
            });
          }
        }
        log.changes = changes;
        return log;
      });
    } else {
      proxy.$modal.msgError('获取审计日志失败: ' + response.msg);
    }
  } catch (error) {
    console.error('获取审计日志失败:', error);
    proxy.$modal.msgError('获取审计日志失败');
  } finally {
    auditLogsLoading.value = false;
  }
}

/** 获取字段名称 */
function getFieldName(fieldKey) {
  const fieldMap = {
    'erpClsId': '物料属性',
    'volume': '单个体积',
    'materialgroup': '物料分组',
    'productCategory': '产品类别',
    'englishProductName': '英文品名',
    'number': '编码',
    'name': '名称',
    'specification': '规格型号',
    'fxlcp': '新老产品',
    'deliveryState': '交付红线',
    'hsbm': 'HS编码',
    'description1': '原英文描述',
    'description2': '现英文描述',
    'formerNumber': '旧物料编码',
    'creator': '创建人',
    'creator_time': '创建时间',
    'image': '图片',
    'inspectionReport': '检测报告',
    'fstate': '物料状态'
  };
  return fieldMap[fieldKey] || fieldKey;
}

/** 审计日志分页变化 */
function handleAuditLogsPaginationChange(page) {
  auditLogsCurrentPage.value = page;
  getMaterialAuditLogsList();
}

/** 工具函数 */
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

// 初始化
onMounted(() => {
  // 设置默认排序为编码升序
/*  queryParams.value.orderByColumn = 'number';
  queryParams.value.isAsc = 'asc';*/

  getList();
  getDictionaryLookup();
  getMaterialDictionary();
  getxlproduct();
  ProductCategory();
});
</script>

<style scoped>
/* 中划线样式 */
.tagWithLine {
  text-decoration: line-through;
  color: #999;
}

/* 横向排列的变更详情容器 */
.changes-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 16px;
}

/* 横向排列的变更项 */
.change-item-horizontal {
  flex: 0 0 calc(25% - 12px);
  min-width: 150px;
  max-width: 250px;
  box-sizing: border-box;
}

/* 横向变更字段样式 */
.change-field-horizontal {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #e9ecef;
}

/* 字段名称样式 */
.field-name {
  font-weight: 600;
  color: #495057;
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 值容器 */
.value-container {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 旧值部分 */
.old-value-section {
  display: flex;
  align-items: center;
}

/* 标签样式优化 */
.tagWithLine,
.new-value {
  flex-shrink: 0;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .change-item-horizontal {
    flex: 0 0 calc(33.33% - 11px);
  }
}

@media (max-width: 768px) {
  .change-item-horizontal {
    flex: 0 0 calc(50% - 8px);
  }
}

@media (max-width: 480px) {
  .change-item-horizontal {
    flex: 0 0 100%;
  }
}

.dialog-footer {
  text-align: right;
  margin-top: 16px;
}

.delivery-section {
  margin-top: 5px;
}

.image-hover-container {
  cursor: pointer;
}

.mb8 {
  margin-bottom: 8px;
}

/* 上传区域样式 */
.upload-area {
  border: 2px dashed #dcdfe6;
  border-radius: 6px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  min-height: 150px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: relative;
}

.upload-area:hover {
  border-color: #409eff;
  background-color: #f5f7fa;
}

.upload-area.drag-over {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.upload-icon {
  font-size: 40px;
  color: #c0c4cc;
}

.upload-tips {
  color: #606266;
  font-size: 14px;
}

.upload-tips .small-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

/* 图片预览样式 */
.preview-container {
  position: relative;
  width: 100px;
  height: 100px;
}

.preview-image {
  width: 100%;
  height: 100%;
  border-radius: 4px;
  object-fit: cover;
}

.preview-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.preview-container:hover .preview-overlay {
  opacity: 1;
}

.edit-icon {
  color: white;
  font-size: 24px;
  margin-bottom: 5px;
}

.preview-tip {
  color: white;
  font-size: 12px;
}

/* 文件预览样式 */
.file-preview {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #e9ecef;
  width: 100%;
}

.file-icon {
  color: #409eff;
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-weight: 500;
  color: #495057;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-size {
  font-size: 12px;
  color: #6c757d;
  margin-top: 2px;
}

/* 上传按钮区域 */
.upload-actions {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  justify-content: center;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .el-col-12 {
    width: 100%;
  }

  .upload-area {
    min-height: 120px;
    padding: 15px;
  }
}
</style>
