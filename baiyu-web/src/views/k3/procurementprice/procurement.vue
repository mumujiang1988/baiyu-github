<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch">
      <el-form-item label="采购价目编码" prop="FNumber">
        <el-input
          v-model="queryParams.FNumber"
          placeholder="请输入采购价目编码"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="供应商" prop="FSupplierID">
        <el-input
          v-model="queryParams.FSupplierID"
          placeholder="请输入供应商"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="物料编码" prop="FMaterialId">
        <el-input
          v-model="queryParams.FMaterialId"
          placeholder="请输入物料编码"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="物料名称" prop="FMaterialName">
        <el-input
          v-model="queryParams.FMaterialName"
          placeholder="请输入物料名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>

      <el-form-item label="单据状态" prop="FDocumentStatus">
        <el-select
          v-model="queryParams.FDocumentStatus"
          placeholder="单据状态"
          clearable
          style="width: 240px"
        >
          <el-option
            v-for="dict in f_document_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮区域 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['k3:procurement:save']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="CopyDocument"
          :disabled="single"
          @click="handleCopy"
          v-hasPermi="['k3:procurement:save']"
        >复制</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['k3:procurement:update']"
        >修改</el-button>
      </el-col>

      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="CopyDocument"
          :disabled="single"
          @click="handlePushDown"
        >下推</el-button>
      </el-col>

      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['k3:procurement:delete']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="Setting"
          @click="openColumnSetting"
        >列设置</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 列设置对话框 -->
    <el-dialog
      title="列设置"
      v-model="columnSettingVisible"
      width="600px"
      append-to-body
      @close="closeColumnSetting"
    >
      <div class="column-setting-container">
        <div class="column-setting-header">
          <el-checkbox
            v-model="checkAll"
            :indeterminate="isIndeterminate"
            @change="handleCheckAllChange"
          >全选</el-checkbox>
          <el-button type="text" @click="resetColumns">重置</el-button>
        </div>
        <div class="column-setting-body">
          <draggable
            v-model="allColumns"
            handle=".drag-handle"
            item-key="prop"
            @end="onSettingDragEnd"
            v-bind="dragOptions"
          >
            <template #item="{ element }">
              <div class="column-item">
                <div class="column-item-left">
                  <el-icon class="drag-handle"><Rank /></el-icon>
                  <el-checkbox v-model="element.visible" @change="handleColumnChange">
                    {{ element.label }}
                  </el-checkbox>
                </div>
                <div class="column-item-right">
                  <el-tag v-if="element.width" size="small" type="info">{{ element.width }}</el-tag>
                </div>
              </div>
            </template>
          </draggable>
        </div>
        <div class="column-setting-footer">
          <el-button @click="closeColumnSetting">取消</el-button>
          <el-button type="primary" @click="saveColumnSettings">保存</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 表格区域 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="procurementList"
        @selection-change="handleSelectionChange"
        :key="tableKey"
        row-key="id"
        style="width: 100%"
        :height="tableHeight"
        border
        :resizable="true"
        @header-dragend="handleHeaderDragEnd"
      >
        <el-table-column type="selection" width="55" align="center" fixed="left" :resizable="false" />
        <el-table-column
          v-for="col in visibleColumns"
          :key="col.prop"
          :label="col.label"
          :align="col.align || 'left'"
          :prop="col.prop"
          :width="col.width"
          :min-width="col.minWidth"
          :show-overflow-tooltip="col.showOverflowTooltip !== false"
          :resizable="true"
        >
          <template #default="scope">
            <span
              v-if="['fentryEffectiveDate', 'fentryExpiryDate', 'createdAt'].includes(col.prop)"
            >
              {{ formatDate(scope.row[col.prop]) }}
            </span>
            <span v-else>
              {{ scope.row[col.prop] }}
            </span>
          </template>
        </el-table-column>

        <!-- 单据状态列 -->
        <el-table-column width="100px" label="单据状态" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag
              v-if="getDictLabel(f_document_status, row.fdocumentStatus)"
              :type="getDictType(f_document_status, row.fdocumentStatus) || 'info'"
              disable-transitions
            >
              {{ getDictLabel(f_document_status, row.fdocumentStatus) }}
            </el-tag>
            <span v-else>{{ row.fdocumentStatus }}</span>
          </template>
        </el-table-column>

        <!-- 扩展行 -->
        <el-table-column type="expand" width="30" :resizable="false">
          <template #default="scope">
            <div
              v-if="scope.row.entries?.length"
              class="row-expand-container"
              @mouseenter="handleExpandMouseEnter(scope.row)"
              @mouseleave="handleExpandMouseLeave(scope.row)"
            >
              <div class="expand-close-btn" @click.stop="closeExpandRow(scope.row)">
                <i class="el-icon-close"></i>
              </div>
              <el-tabs v-model="expandedRowActiveTab[scope.row.id]" class="expand-tabs">
                <el-tab-pane label="物料明细" name="contact">
                  <div class="expand-section">
                    <div class="expand-content">
                      <el-table :data="scope.row.entries" size="mini" border style="width: 100%">
                        <el-table-column prop="fmaterialId" label="物料编码" width="80" align="center" />
                        <el-table-column prop="fmaterialName" label="物料名称" width="90" align="center" />
                        <el-table-column prop="fggsm" label="规格说明" width="90" align="center" />
                        <el-table-column prop="fentryEffectiveDate" label="生效日期" width="90" align="center" />
                        <el-table-column prop="fentryExpiryDate" label="价格有效期" width="90" align="center" />
                        <el-table-column prop="fwbzc" label="单箱长(cm)" width="100" align="center" />
                        <el-table-column prop="fwbzk" label="单箱宽(cm)" width="100" align="center" />
                        <el-table-column prop="fwbzg" label="单箱高(cm)" width="100" align="center" />
                        <el-table-column prop="fmz" label="单箱毛重(KGS)" width="90" align="center" />
                        <el-table-column prop="fjz" label="单箱净重(KGS)" width="90" align="center" />
                        <el-table-column prop="fbzsm" label="包装说明" width="90" align="center" />
                        <el-table-column prop="fgchh" label="工厂货号" width="90" align="center" />
                      </el-table>
                    </div>
                  </div>
                </el-tab-pane>
              </el-tabs>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页组件 -->
    <div class="pagination-wrapper">
      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
        @update:limit="handlePageSizeChange"
        :page-sizes="[10, 20, 50, 100]"
        :layout="'total, sizes, prev, pager, next, jumper'"
      />
    </div>

    <!-- 新增/修改采购价目对话框 -->
    <el-dialog :title="title" v-model="open" width="2000px" append-to-body @close="cancel">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <!-- 主表信息 -->
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="采购组织" prop="fPurchaseOrg">
              <el-input v-model="form.fPurchaseOrg" placeholder="采购组织" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="编码" prop="fnumber">
              <el-input
                v-model="form.fnumber"
                placeholder="保存时自动生成"
                :disabled="!isFormEditable"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="名称" prop="fname">
              <el-input
                v-model="form.fname"
                placeholder="请输入名称"
                :disabled="!isFormEditable"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="价格类型" prop="fpriceType">
              <el-select
                v-model="form.fpriceType"
                placeholder="请选择价格类型"
                filterable
                style="width: 100%"
                :disabled="!isFormEditable"
              >
                <el-option
                  v-for="item in pricetypeList"
                  :key="item.fpriceType"
                  :label="item.name"
                  :value="item.fpriceType"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="币别" prop="fcurrencyID">
              <el-select
                v-model="form.fcurrencyID"
                placeholder="请选择币别"
                style="width: 100%"
                :disabled="!isFormEditable"
              >
                <el-option
                  v-for="item in currencyList"
                  :key="item.fcurrencyID"
                  :label="item.name"
                  :value="item.fcurrencyID"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="供应商" prop="fsupplierID">
              <el-select
                v-model="form.fsupplierID"
                placeholder="请选择供应商"
                filterable
                style="width: 100%"
                :disabled="!isFormEditable"
              >
                <el-option
                  v-for="item in supplierList"
                  :key="item.fsupplierID"
                  :label="item.name"
                  :value="item.fsupplierID"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="单据状态" prop="fdocumentStatus">
              <el-select
                v-model="form.fdocumentStatus"
                placeholder="请选择单据状态"
                disabled
              >
                <el-option
                  v-for="dict in f_document_status"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="价目表对象" prop="fpriceObject">
              <el-select
                v-model="form.fpriceObject"
                placeholder="请选择价目表对象"
                :disabled="!isFormEditable"
              >
                <el-option
                  v-for="dict in price_object"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 采购明细 -->
        <el-card class="section-card" style="margin-top: 20px">
          <template #header>
            <div class="section-header">
              <span>采购明细</span>
              <div>
                <el-button
                  type="primary"
                  plain
                  size="small"
                  @click="handleAddContact"
                  :disabled="!isFormEditable"
                >新增行</el-button>
                <el-button
                  type="danger"
                  plain
                  size="small"
                  @click="handleDeleteContact"
                  :disabled="!isFormEditable || !selectedEntryRows.length"
                >删除行</el-button>
                <el-button
                  type="info"
                  plain
                  size="small"
                  @click="handleBatchFill"
                  :disabled="!isFormEditable"
                >批量填充</el-button>
                <el-button type="warning" plain size="small" :disabled="!isFormEditable">操作</el-button>
                <el-button type="success" plain size="small" :disabled="!isFormEditable">附件</el-button>
                <el-button type="success" plain size="small" :disabled="!isFormEditable">业务查询</el-button>
              </div>
            </div>
          </template>
          <el-table
            :data="form.entries"
            border
            stripe
            style="width: 100%"
            @selection-change="handleEntrySelectionChange"
          >
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="fmaterialId" label="物料编码" width="140" align="center">
              <template #default="{ row }">
                <el-select
                  v-model="row.fmaterialId"
                  placeholder="Select"
                  style="width: 240px"
                  filterable
                  @change="(val) => handleMaterialChange(row, val)"
                  :disabled="!isFormEditable"
                >
                  <el-option
                    v-for="item in materialList"
                    :key="item.fmaterialId"
                    :label="item.fmaterialId"
                    :value="item.fmaterialId"
                  >
                    <span style="float: left">{{ item.fmaterialId }}</span>
                    <span style="float: right; color: var(--el-text-color-secondary); font-size: 13px;">{{ item.fmaterialName }}</span>
                  </el-option>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="fmaterialName" label="物料名称" width="140" align="center">
              <template #default="{ row }">
                <el-input v-model="row.fmaterialName" style="width: 240px" placeholder="物料名称" disabled />
              </template>
            </el-table-column>
            <el-table-column prop="specification" label="规格型号" width="140" align="center">
              <template #default="{ row }">
                <el-input v-model="row.specification" placeholder="规格型号" disabled />
              </template>
            </el-table-column>
            <el-table-column prop="fgchh" label="工厂货号" width="140" align="center">
              <template #default="{ row }">
                <el-input
                  v-model="row.fgchh"
                  size="small"
                  placeholder="工厂货号"
                  :disabled="!isFormEditable"
                />
              </template>
            </el-table-column>
            <el-table-column prop="fctyBaseProperty" label="英文品名" width="140" align="center">
              <template #default="{ row }">
                <el-input v-model="row.fctyBaseProperty" size="small" placeholder="英文品名" disabled />
              </template>
            </el-table-column>
            <el-table-column prop="ftp1" label="工厂图片*" width="140" align="center">
              <template #default="{ row }">
                <div style="display: flex; flex-direction: column; align-items: center; gap: 4px;">
                  <el-image
                    v-if="row.localPreview"
                    :src="row.localPreview"
                    style="width: 80px; height: 80px; border-radius: 4px; object-fit: cover; cursor: pointer;"
                    fit="cover"
                    :preview-src-list="[row.localPreview]"
                    :preview-teleported="true"
                  >
                    <template #error>
                      <div class="image-error-placeholder">
                        <el-icon><Picture /></el-icon>
                        <span>预览失败</span>
                      </div>
                    </template>
                  </el-image>
                  <el-image
                    v-else-if="row.ftp1"
                    :src="row.ftp1"
                    style="width: 80px; height: 80px; border-radius: 4px; object-fit: cover; cursor: pointer;"
                    fit="cover"
                    :preview-src-list="[row.ftp1]"
                    :preview-teleported="true"
                  >
                    <template #error>
                      <div class="image-error-placeholder">
                        <el-icon><Picture /></el-icon>
                        <span>图片加载失败</span>
                      </div>
                    </template>
                  </el-image>
                  <div
                    v-else
                    style="width: 80px; height: 80px; background-color: #f5f7fa; border-radius: 4px; display: flex; align-items: center; justify-content: center; color: #909399;"
                  >
                    <el-icon><Picture /></el-icon>
                  </div>
                  <div style="display: flex; gap: 8px;">
                    <el-upload
                      :show-file-list="false"
                      :auto-upload="false"
                      @change="(file) => handleFileChange(row, file)"
                      accept="image/*"
                      :disabled="!isFormEditable"
                    >
                      <el-button
                        type="primary"
                        size="small"
                        :icon="Upload"
                        circle
                        plain
                        :disabled="!isFormEditable"
                      />
                    </el-upload>
                    <el-button
                      v-if="row.ftp1 || row.localPreview"
                      type="danger"
                      size="small"
                      :icon="Delete"
                      circle
                      plain
                      @click="handleDeleteImage(row)"
                      :disabled="!isFormEditable"
                    />
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="fctyBaseProperty1" label="老产品" width="140" align="center">
              <template #default="{ row }">
                <el-input v-model="row.fctyBaseProperty1" size="small" placeholder="老产品" disabled />
              </template>
            </el-table-column>
            <el-table-column prop="fbzsm" label="包装说明" width="120" align="center">
              <template #default="{ row }">
                <el-input
                  v-model="row.fbzsm"
                  size="small"
                  placeholder="包装说明"
                  :disabled="!isFormEditable"
                />
              </template>
            </el-table-column>
            <el-table-column prop="fprice" label="单价" width="140" align="center">
              <template #default="{ row }">
                <el-input v-model="row.fprice" size="small" placeholder="单价" disabled />
              </template>
            </el-table-column>
            <el-table-column prop="ftaxPrice" label="含税单价" width="140" align="center">
              <template #default="{ row }">
                <el-input
                  v-model="row.ftaxPrice"
                  size="small"
                  placeholder="含税单价"
                  :disabled="!isFormEditable"
                />
              </template>
            </el-table-column>
            <el-table-column prop="fentryEffectiveDate" label="生效日期" width="140" align="center">
              <template #default="{ row }">
                <el-date-picker
                  v-model="row.fentryEffectiveDate"
                  type="date"
                  placeholder="选择日期"
                  size="small"
                  style="width: 100%"
                  :disabled="!isFormEditable"
                />
              </template>
            </el-table-column>
            <el-table-column prop="fwbzsl" label="单箱数量" width="140" align="center">
              <template #default="{ row }">
                <el-input
                  v-model="row.fwbzsl"
                  size="small"
                  placeholder="单箱数量"
                  :disabled="!isFormEditable"
                />
              </template>
            </el-table-column>
            <el-table-column prop="fmz" label="单箱毛重(KGS)" width="140" align="center">
              <template #default="{ row }">
                <el-input
                  v-model="row.fmz"
                  size="small"
                  placeholder="毛重"
                  :disabled="!isFormEditable"
                />
              </template>
            </el-table-column>
            <el-table-column prop="fjz" label="单箱净重(KGS)" width="140" align="center">
              <template #default="{ row }">
                <el-input
                  v-model="row.fjz"
                  size="small"
                  placeholder="净重"
                  :disabled="!isFormEditable"
                />
              </template>
            </el-table-column>
            <el-table-column prop="fwbzc" label="单箱长(cm)" width="140" align="center">
              <template #default="{ row }">
                <el-input
                  v-model="row.fwbzc"
                  size="small"
                  placeholder="长"
                  :disabled="!isFormEditable"
                />
              </template>
            </el-table-column>
            <el-table-column prop="fwbzk" label="单箱宽(cm)" width="140" align="center">
              <template #default="{ row }">
                <el-input
                  v-model="row.fwbzk"
                  size="small"
                  placeholder="宽"
                  :disabled="!isFormEditable"
                />
              </template>
            </el-table-column>
            <el-table-column prop="fwbzg" label="单箱高(cm)" width="140" align="center">
              <template #default="{ row }">
                <el-input
                  v-model="row.fwbzg"
                  size="small"
                  placeholder="高"
                  :disabled="!isFormEditable"
                />
              </template>
            </el-table-column>
            <el-table-column prop="funitID" label="计价单位" width="140" align="center">
              <template #default="{ row }">
                <el-select
                  v-model="row.funitID"
                  placeholder="请选择计价单位"
                  style="width: 240px"
                  :disabled="!isFormEditable"
                >
                  <el-option
                    v-for="item in pricingunitList"
                    :key="item.funitID"
                    :label="item.funitID"
                    :value="item.funitID"
                  >
                    <span style="float: left">{{ item.funitID }}</span>
                    <span style="float: right; color: var(--el-text-color-secondary); font-size: 13px;">{{ item.name }}</span>
                  </el-option>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="fbcgg" label="包装尺寸" width="140" align="center">
              <template #default="{ row }">
                <el-input
                  v-model="row.fbcgg"
                  size="small"
                  placeholder="包装尺寸"
                  :disabled="!isFormEditable"
                />
              </template>
            </el-table-column>
            <el-table-column prop="fwbztj" label="单箱体积(m2)" width="140" align="center">
              <template #default="{ row }">
                <el-input
                  v-model="row.fwbztj"
                  size="small"
                  placeholder="单箱体积"
                  :disabled="!isFormEditable"
                />
              </template>
            </el-table-column>
            <el-table-column prop="fentryExpiryDate" label="价格有效期" width="140" align="center">
              <template #default="{ row }">
                <el-date-picker
                  v-model="row.fentryExpiryDate"
                  type="date"
                  placeholder="选择价格有效期"
                  size="small"
                  style="width: 100%"
                  :disabled="!isFormEditable"
                />
              </template>
            </el-table-column>
            <el-table-column prop="fqdl" label="起订量" width="140" align="center">
              <template #default="{ row }">
                <el-input
                  v-model="row.fqdl"
                  size="small"
                  placeholder="起订量"
                  :disabled="!isFormEditable"
                />
              </template>
            </el-table-column>
            <el-table-column prop="fwbzdw" label="单箱单位" width="140" align="center">
              <template #default="{ row }">
                <el-select
                  v-model="row.fwbzdw"
                  placeholder="请选择单箱单位"
                  style="width: 240px"
                  :disabled="!isFormEditable"
                >
                  <el-option
                    v-for="item in pricingunitList"
                    :key="item.fwbzdw"
                    :label="item.fwbzdw"
                    :value="item.fwbzdw"
                  >
                    <span style="float: left">{{ item.fwbzdw }}</span>
                    <span style="float: right; color: var(--el-text-color-secondary); font-size: 13px;">{{ item.name }}</span>
                  </el-option>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="frecentdate" label="最近调价日期" width="140" align="center">
              <template #default="{ row }">
                <el-date-picker
                  v-model="row.frecentdate"
                  type="date"
                  placeholder="选择最近调价日期"
                  size="small"
                  style="width: 100%"
                  disabled
                />
              </template>
            </el-table-column>
            <el-table-column prop="fzxsms" label="中性说明书" width="140" align="center">
              <template #default="{ row }">
                <el-select
                  v-model="row.fzxsms"
                  placeholder="请选择中性说明书"
                  clearable
                  style="width: 100%"
                  :disabled="!isFormEditable"
                >
                  <el-option v-for="dict in f_zxsms" :key="dict.value" :label="dict.label" :value="dict.value" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="fsfyyywxj" label="是否用于业务询价" width="160" align="center">
              <template #default="{ row }">
                <el-select
                  v-model="row.fsfyyywxj"
                  placeholder="请选择是否用于业务询价"
                  clearable
                  style="width: 100%"
                  :disabled="!isFormEditable"
                >
                  <el-option v-for="dict in business_inquiry" :key="dict.value" :label="dict.label" :value="dict.value" />
                </el-select>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button
            type="primary"
            @click="submitForm"
            :loading="submitLoading"
            :disabled="!isFormEditable && isEditMode"
          >确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 下推对话框（选择单据状态） -->
    <el-dialog :title="title" v-model="opens" width="500px" append-to-body @close="cancelPushDown">
      <el-form ref="priceListRef" :model="form" :rules="pushDownRules" label-width="100px">
        <!-- 新增表单项：单据状态 -->
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitsForm">确 定</el-button>
          <el-button @click="cancelPushDown">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Procurement">
import {
  priceList,
  procurementdetails,
  procurementdelete,
  gettlementCurrency,
  getSupplierList,
  getMaterialList,
  getpricetype,
  getPricingUnit,
  addProcurement,
  updateProcurement,
  addupdateProcurementPriceAdjustment // 新增导入下推API
} from "@/api/k3/procurement";
import { ElMessage, ElMessageBox } from "element-plus";
import { ref, computed, onMounted, nextTick, watch, getCurrentInstance, onBeforeUnmount } from "vue";
import draggable from "vuedraggable";
import { Rank, Upload, Delete, Picture } from "@element-plus/icons-vue";
import dayjs from "dayjs";
import { debounce } from "lodash-es";

const { proxy } = getCurrentInstance();
const { sys_normal_disable, price_object, business_inquiry, f_zxsms, f_document_status } = proxy.useDict(
  "sys_normal_disable", "price_object", "business_inquiry", "f_zxsms", "f_document_status"
);

// ==================== 列表状态 ====================
const procurementList = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const tableKey = ref(1);
const tableHeight = ref(null);
const queryRef = ref(null);

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  FNumber: undefined,
  FSupplierID: undefined,
  FMaterialId: undefined,
  FMaterialName: undefined,
  FDocumentStatus: undefined
});

// ==================== 表单状态 ====================
const open = ref(false);
const title = ref("");
const isEditMode = ref(false);
const submitLoading = ref(false);
const formRef = ref(null);
const selectedEntryRows = ref([]);
const expandedRowActiveTab = ref({});

// 下推对话框相关
const opens = ref(false);
const priceListRef = ref(null);

// 下推表单校验规则
const pushDownRules = {
  targetDocumentStatus: [{ required: true, message: "请选择单据状态", trigger: "change" }]
};

// 表单默认值
const defaultForm = {
  fPurchaseOrg: "佰誉云",
  fnumber: "",
  fname: "",
  fpriceType: undefined,
  fcurrencyID: undefined,
  fsupplierID: undefined,
  fpriceObject: undefined,
  entries: [],
  targetDocumentStatus: "" // 下推时临时使用
};

const form = ref({ ...defaultForm });

// 表单验证规则
const rules = {
  fsupplierID: [{ required: true, message: "请选择供应商", trigger: "change" }],
  fcurrencyID: [{ required: true, message: "请选择币别", trigger: "change" }],
  fpriceType: [{ required: true, message: "请选择价格类型", trigger: "change" }],
};

// ==================== 下拉数据 ====================
const currencyList = ref([]);
const pricetypeList = ref([]);
const supplierList = ref([]);
const materialList = ref([]);
const pricingunitList = ref([]);

// ==================== 列设置 ====================
const columnSettingVisible = ref(false);
const checkAll = ref(false);
const isIndeterminate = ref(false);

const defaultColumns = [
  { prop: "fnumber", label: "采购价目编码", visible: true, width: "120px", align: "left", showOverflowTooltip: true },
  { prop: "fname", label: "名称", visible: true, width: "120px", align: "left", showOverflowTooltip: true },
  { prop: "fsupplierID", label: "供应商", visible: true, width: "120px", align: "left", showOverflowTooltip: true },
  { prop: "fmaterialId", label: "物料编码", visible: true, width: "120px", align: "left", showOverflowTooltip: true },
  { prop: "fmaterialName", label: "物料名称", visible: true, width: "120px", align: "left", showOverflowTooltip: true },
  { prop: "fggsm", label: "规格说明", visible: true, width: "120px", align: "left", showOverflowTooltip: true },
  { prop: "fprice", label: "单价", visible: true, width: "100px", align: "right", showOverflowTooltip: true },
  { prop: "ftaxPrice", label: "含税单价", visible: true, width: "100px", align: "right", showOverflowTooltip: true },
  { prop: "fentryEffectiveDate", label: "生效日期", visible: true, width: "100px", align: "center", showOverflowTooltip: true },
  { prop: "fwbzc", label: "单箱长(cm)", visible: true, width: "100px", align: "right", showOverflowTooltip: true },
  { prop: "fwbzk", label: "单箱宽(cm)", visible: true, width: "100px", align: "right", showOverflowTooltip: true },
  { prop: "fwbzg", label: "单箱高(cm)", visible: true, width: "100px", align: "right", showOverflowTooltip: true },
  { prop: "fmz", label: "毛重(KGS)", visible: true, width: "100px", align: "right", showOverflowTooltip: true },
  { prop: "fjz", label: "净重(KGS)", visible: true, width: "100px", align: "right", showOverflowTooltip: true },
  { prop: "fbzsm", label: "包装说明", visible: true, width: "120px", align: "left", showOverflowTooltip: true },
  { prop: "fgchh", label: "工厂货号", visible: true, width: "120px", align: "left", showOverflowTooltip: true },
  { prop: "createdBy", label: "创建人", visible: true, width: "100px", align: "left", showOverflowTooltip: true },
  { prop: "createdAt", label: "创建时间", visible: true, width: "160px", align: "center", showOverflowTooltip: true },
];

const allColumns = ref([...defaultColumns]);

const visibleColumns = computed(() => allColumns.value.filter((col) => col.visible));

const dragOptions = {
  animation: 200,
  group: "columns",
  disabled: false,
  ghostClass: "ghost",
};

// 根据字典值和字典列表获取对应的标签文本
const getDictLabel = (dictList, value) => {
  if (!dictList || !value) return '';
  const item = dictList.find(item => item.value == value);
  return item ? item.label : '';
}

// 根据字典值和字典列表获取对应的标签类型（用于 el-tag 的 type）
const getDictType = (dictList, value) => {
  if (!dictList || !value) return '';
  const item = dictList.find(item => item.value == value);
  return item ? item.listClass : '';
}

// ==================== 表单可编辑性控制 ====================
// 从字典中提取允许编辑的状态值（暂存、创建、重新审核）
const editableStatusValues = computed(() => {
  const dict = f_document_status.value || [];
  const labels = ['暂存', '创建', '重新审核']; // 根据实际字典label调整
  return dict.filter(item => labels.includes(item.label)).map(item => item.value);
});

// 判断当前表单是否可编辑：新增模式始终可编辑；修改模式根据单据状态判断
const isFormEditable = computed(() => {
  if (!isEditMode.value) return true; // 新增/复制模式
  const status = form.value.fdocumentStatus;
  return editableStatusValues.value.includes(status);
});

// ==================== 工具函数 ====================
const formatDate = (date) => {
  if (!date) return "-";
  try {
    return dayjs(date).format("YYYY-MM-DD HH:mm:ss");
  } catch {
    return date;
  }
};

// 计算表格高度（防抖）
const calculateTableHeight = debounce(() => {
  nextTick(() => {
    const container = document.querySelector(".app-container");
    if (!container) return;
    const searchArea = document.querySelector(".el-form");
    const operationArea = document.querySelector(".mb8");
    const paginationArea = document.querySelector(".pagination-wrapper");
    const containerHeight = container.clientHeight;
    const searchHeight = searchArea?.clientHeight || 0;
    const operationHeight = operationArea?.clientHeight || 0;
    const paginationHeight = paginationArea?.clientHeight || 56;
    const marginHeight = 40;
    let availableHeight = containerHeight - searchHeight - operationHeight - paginationHeight - marginHeight;
    availableHeight = Math.max(availableHeight, 200);
    availableHeight = Math.min(availableHeight, 800);
    tableHeight.value = availableHeight;
  });
}, 100);

// 释放本地预览 URL
const revokeLocalPreview = (row) => {
  if (row.localPreview) {
    URL.revokeObjectURL(row.localPreview);
    row.localPreview = null;
  }
};

// 处理明细行物料选择变化
const handleMaterialChange = (row, materialId) => {
  if (!materialId) return;
  const selectedMaterial = materialList.value.find((item) => item.fmaterialId === materialId);
  if (selectedMaterial) {
    row.fmaterialName = selectedMaterial.fmaterialName || "";
    row.fctyBaseProperty1 = selectedMaterial.fctyBaseProperty1 || "";
    row.specification = selectedMaterial.specification || "";
    row.fctyBaseProperty = selectedMaterial.fctyBaseProperty || "";
  }
};

// ==================== 列表 API ====================
const getList = async () => {
  loading.value = true;
  try {
    const response = await priceList(queryParams.value);
    if (response?.rows) {
      procurementList.value = response.rows.map((row) => {
        if (row.entries?.length) {
          const first = row.entries[0];
          Object.assign(row, {
            fmaterialId: first.fmaterialId,
            fmaterialName: first.fmaterialName,
            fggsm: first.fggsm,
            fprice: first.fprice,
            ftaxPrice: first.ftaxPrice,
            fentryEffectiveDate: first.fentryEffectiveDate,
            fentryExpiryDate: first.fentryExpiryDate,
            fwbzc: first.fwbzc,
            fwbzk: first.fwbzk,
            fwbzg: first.fwbzg,
            fmz: first.fmz,
            fjz: first.fjz,
            fbzsm: first.fbzsm,
            fgchh: first.fgchh,
            fwbzsl: first.fwbzsl,
            fwbzdw: first.fwbzdw,
          });
        } else {
          const emptyFields = [
            "fmaterialId",
            "fmaterialName",
            "fggsm",
            "ftaxPrice",
            "fentryEffectiveDate",
            "fentryExpiryDate",
            "fwbzc",
            "fwbzk",
            "fwbzg",
            "fmz",
            "fjz",
            "fbzsm",
            "fgchh",
            "fwbzsl",
            "fwbzdw",
          ];
          emptyFields.forEach((field) => (row[field] = ""));
        }
        return row;
      });
      total.value = response.total || 0;
    } else {
      procurementList.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error("获取采购价目表失败:", error);
    ElMessage.error("获取采购价目表失败");
    procurementList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
    calculateTableHeight();
  }
};

const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

const resetQuery = () => {
  queryRef.value?.resetFields();
  handleQuery();
};

const handleSelectionChange = (selection) => {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
};

const handleDelete = async (row) => {
  const delIds = row?.id ? [row.id] : ids.value;
  if (!delIds.length) {
    ElMessage.warning("请选择要删除的数据");
    return;
  }
  try {
    await ElMessageBox.confirm("是否确认删除选中的采购价目？", "警告", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });
    await procurementdelete(delIds);
    ElMessage.success("删除成功");
    await getList();
  } catch (error) {
    if (error !== "cancel") {
      console.error("删除失败:", error);
      ElMessage.error("删除失败");
    }
  }
};

const handlePageSizeChange = (newSize) => {
  queryParams.value.pageSize = newSize;
  queryParams.value.pageNum = 1;
  getList();
};

// ==================== 表单操作 ====================
const resetForm = () => {
  form.value = { ...defaultForm };
};

const cancel = () => {
  open.value = false;
  form.value.entries?.forEach((row) => {
    revokeLocalPreview(row);
    row.localFile = null;
  });
  resetForm();
  formRef.value?.clearValidate();
};

const handleAdd = () => {
  resetForm();
  open.value = true;
  title.value = "新增采购价目";
  isEditMode.value = false;
};

const handleCopy = async () => {
  if (ids.value.length !== 1) {
    ElMessage.warning("请选择一条数据");
    return;
  }
  loading.value = true;
  try {
    const response = await procurementdetails(ids.value[0]);
    const data = response.data || response;
    const { id, ...rest } = data;
    form.value = { ...rest, id: undefined };
    form.value.entries?.forEach((row) => {
      row.localPreview = null;
      row.localFile = null;
    });
    open.value = true;
    title.value = "复制采购价目";
    isEditMode.value = false;
    ElMessage.success("数据已复制到表单，请修改后保存");
  } catch (error) {
    console.error("获取详情失败:", error);
    ElMessage.error("获取详情失败");
  } finally {
    loading.value = false;
  }
};

const handleUpdate = async () => {
  if (ids.value.length !== 1) {
    ElMessage.warning("请选择一条数据");
    return;
  }
  try {
    const response = await procurementdetails(ids.value[0]);
    const data = response.data || response;
    form.value = { ...data };
    form.value.entries?.forEach((row) => {
      row.localPreview = null;
      row.localFile = null;
    });
    open.value = true;
    title.value = "修改采购价目";
    isEditMode.value = true;
  } catch (error) {
    console.error("获取详情失败:", error);
    ElMessage.error("获取详情失败");
  }
};

/** 下推 */
const handlePushDown = async () => {
  if (ids.value.length !== 1) {
    ElMessage.warning("请选择一条数据");
    return;
  }
  try {
    const response = await procurementdetails(ids.value[0]);
    const data = response.data || response;
    form.value = { ...data, targetDocumentStatus: "" };
    opens.value = true;
    title.value = "选择单据状态";
  } catch (error) {
    console.error("获取详情失败:", error);
    ElMessage.error("获取详情失败");
  }
};

/** 关闭下推对话框 */
const cancelPushDown = () => {
  opens.value = false;
  form.value.targetDocumentStatus = "";
  priceListRef.value?.clearValidate();
};

/** 下推提交按钮 */
function submitsForm() {
  priceListRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await addupdateProcurementPriceAdjustment(form.value);
        ElMessage.success("下推成功");
        opens.value = false;
        getList();
      } catch (error) {
        console.error("下推失败:", error);
        ElMessage.error("下推失败: " + (error.message || ""));
      }
    }
  });
}

const submitForm = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true;
      try {
        const files = [];
        const entriesCopy = form.value.entries.map((entry) => {
          const { localPreview, localFile, ...entryCopy } = entry;
          if (localFile) {
            entryCopy._fileIndex = files.length;
            files.push(localFile);
          }
          return entryCopy;
        });

        const submitData = {
          ...form.value,
          entries: entriesCopy,
          ftp1File: files,
        };
        delete submitData.localPreview;
        delete submitData.localFile;

        if (isEditMode.value) {
          await updateProcurement(submitData);
        } else {
          await addProcurement(submitData);
        }
        ElMessage.success(isEditMode.value ? "修改成功" : "新增成功");
        open.value = false;
        await getList();
      } catch (error) {
        console.error("保存失败:", error);
        ElMessage.error("保存失败: " + (error.message || "请检查表单填写是否正确"));
      } finally {
        submitLoading.value = false;
      }
    }
  });
};

// 明细表格操作
const handleEntrySelectionChange = (selection) => {
  selectedEntryRows.value = selection;
};

const handleAddContact = () => {
  if (!form.value.entries) form.value.entries = [];
  form.value.entries.push({
    id: Date.now() + Math.random(),
    fmaterialId: "",
    fmaterialName: "",
    fggsm: "",
    fgchh: "",
    fctyBaseProperty: "",
    ftp1: "",
    fctyBaseProperty1: "",
    fbzsm: "",
    ftaxPrice: "",
    fentryEffectiveDate: "",
    fwbzsl: "",
    fmz: "",
    fjz: "",
    fwbzc: "",
    fwbzk: "",
    fwbzg: "",
    fprice: "",
    funitID: "",
    fbcgg: "",
    fwbztj: "",
    fentryExpiryDate: "",
    fqdl: "",
    fwbzdw: "",
    frecentdate: "",
    fzxsms: "",
    fsfyyywxj: "",
    localPreview: null,
    localFile: null,
  });
};

const handleDeleteContact = () => {
  if (!selectedEntryRows.value.length) {
    ElMessage.warning("请选择要删除的行");
    return;
  }
  selectedEntryRows.value.forEach((row) => {
    revokeLocalPreview(row);
  });
  const idsToRemove = selectedEntryRows.value.map((row) => row.id);
  form.value.entries = form.value.entries.filter((row) => !idsToRemove.includes(row.id));
  selectedEntryRows.value = [];
};

const handleBatchFill = () => {
  ElMessage.info("批量填充功能开发中");
};

// ==================== 图片处理 ====================
const handleFileChange = (row, uploadFile) => {
  const file = uploadFile.raw;
  if (!file) return;
  const isImage = file.type.startsWith("image/");
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isImage) {
    ElMessage.error("只能上传图片文件！");
    return false;
  }
  if (!isLt2M) {
    ElMessage.error("图片大小不能超过 2MB！");
    return false;
  }
  revokeLocalPreview(row);
  row.localPreview = URL.createObjectURL(file);
  row.localFile = file;
  row.ftp1 = "";
};

const handleDeleteImage = (row) => {
  revokeLocalPreview(row);
  row.localFile = null;
  row.ftp1 = "";
};

// ==================== 列设置 ====================
const loadColumnSettings = () => {
  const savedColumns = localStorage.getItem("procurementColumns");
  if (savedColumns) {
    try {
      const parsedColumns = JSON.parse(savedColumns);
      const mergedColumns = defaultColumns.map((defaultCol) => {
        const savedCol = parsedColumns.find((c) => c.prop === defaultCol.prop);
        if (savedCol) {
          return {
            ...defaultCol,
            visible: savedCol.visible ?? defaultCol.visible,
            width: savedCol.width || defaultCol.width,
          };
        }
        return defaultCol;
      });
      allColumns.value = mergedColumns;
    } catch (error) {
      console.error("加载列设置失败:", error);
    }
  }
};

const updateCheckAllState = () => {
  const visibleCount = allColumns.value.filter((col) => col.visible).length;
  const totalCount = allColumns.value.length;
  checkAll.value = visibleCount === totalCount;
  isIndeterminate.value = visibleCount > 0 && visibleCount < totalCount;
};

const openColumnSetting = () => {
  columnSettingVisible.value = true;
  updateCheckAllState();
};

const closeColumnSetting = () => {
  columnSettingVisible.value = false;
};

const saveColumnSettings = () => {
  try {
    localStorage.setItem("procurementColumns", JSON.stringify(allColumns.value));
    ElMessage.success("列设置已保存");
    columnSettingVisible.value = false;
    tableKey.value += 1;
    calculateTableHeight();
  } catch (error) {
    console.error("保存列设置失败:", error);
    ElMessage.error("保存列设置失败");
  }
};

const resetColumns = () => {
  allColumns.value.forEach((col) => (col.visible = true));
  updateCheckAllState();
};

const handleCheckAllChange = (val) => {
  allColumns.value.forEach((col) => (col.visible = val));
  isIndeterminate.value = false;
};

const handleColumnChange = () => {
  updateCheckAllState();
};

const onSettingDragEnd = () => {};

const handleHeaderDragEnd = (column, newWidth) => {
  const columnProp = column.property;
  if (columnProp) {
    const col = allColumns.value.find((c) => c.prop === columnProp);
    if (col) {
      col.width = newWidth + "px";
      saveColumnSettings();
    }
  }
};

// ==================== 下拉数据获取 ====================
const fetchAllDropdownData = async () => {
  try {
    const [currencyRes, pricetypeRes, supplierRes, materialRes, pricingunitRes] = await Promise.allSettled([
      gettlementCurrency(),
      getpricetype(),
      getSupplierList(),
      getMaterialList(),
      getPricingUnit(),
    ]);
    if (currencyRes.status === "fulfilled") currencyList.value = currencyRes.value.data || [];
    if (pricetypeRes.status === "fulfilled") pricetypeList.value = pricetypeRes.value.data || [];
    if (supplierRes.status === "fulfilled") supplierList.value = supplierRes.value.data || [];
    if (materialRes.status === "fulfilled") materialList.value = materialRes.value.data || [];
    if (pricingunitRes.status === "fulfilled") pricingunitList.value = pricingunitRes.value.data || [];
  } catch (error) {
    console.error("获取下拉数据失败:", error);
  }
};

// ==================== 扩展行占位方法 ====================
const handleExpandMouseEnter = () => {};
const handleExpandMouseLeave = () => {};
const closeExpandRow = () => {};

// ==================== 生命周期 ====================
onMounted(() => {
  loadColumnSettings();
  getList();
  fetchAllDropdownData();
  window.addEventListener("resize", calculateTableHeight);
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", calculateTableHeight);
  if (form.value.entries) {
    form.value.entries.forEach((row) => {
      if (row.localPreview) URL.revokeObjectURL(row.localPreview);
    });
  }
});

watch(visibleColumns, () => {
  calculateTableHeight();
});
</script>

<style scoped>
/* 样式与原文件保持一致 */
.app-container {
  margin: 0 20px;
  height: calc(100vh - 84px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.mb8 {
  flex-shrink: 0;
  margin-bottom: 20px;
}
.table-container {
  flex: 1;
  position: relative;
  overflow: hidden;
  margin-bottom: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #fff;
}
.pagination-wrapper {
  flex-shrink: 0;
  padding: 12px 16px;
  background-color: #fff;
  border-top: 1px solid #ebeef5;
}
.column-setting-container {
  max-height: 500px;
  overflow-y: auto;
}
.column-setting-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}
.column-setting-body {
  max-height: 400px;
  overflow-y: auto;
  margin-bottom: 20px;
}
.column-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  margin-bottom: 8px;
  background-color: #f5f7fa;
  border-radius: 4px;
  transition: all 0.3s;
}
.column-item:hover {
  background-color: #ebeef5;
}
.column-item-left {
  display: flex;
  align-items: center;
  flex: 1;
}
.column-item-right {
  display: flex;
  gap: 8px;
}
.drag-handle {
  cursor: move;
  margin-right: 10px;
  color: #909399;
  font-size: 16px;
}
.column-setting-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.row-expand-container {
  position: relative;
  padding: 16px;
  background-color: #fafafa;
}
.expand-close-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  cursor: pointer;
  font-size: 16px;
  color: #909399;
  z-index: 1;
}
.expand-close-btn:hover {
  color: #f56c6c;
}
.expand-tabs {
  margin-top: 8px;
}
.expand-section {
  margin-top: 12px;
}
.expand-content {
  background-color: #fff;
  border-radius: 4px;
  padding: 12px;
}
.image-error-placeholder {
  width: 80px;
  height: 80px;
  background-color: #f5f7fa;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 12px;
}
.image-error-placeholder .el-icon {
  font-size: 24px;
  margin-bottom: 4px;
}
</style>
