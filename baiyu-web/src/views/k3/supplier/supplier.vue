<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 供应商分组树形结构 -->
      <el-col :span="4" :xs="24">
        <el-button @click="resetQuery" style="width: 100%; outline: none; border: none">全部</el-button>
        <div class="head-container">
          <el-tree
            :data="supplierGroupsTreeOptions"
            :props="{ label: 'groupName', children: 'children' }"
            :expand-on-click-node="true"
            :filter-node-method="filterNode"
            ref="supplierGroupTreeRef"
            node-key="supplierGroup"
            highlight-current
            show-overflow-tooltip
            @node-click="handleNodeClick"
          />
        </div>
      </el-col>

      <!-- 右侧内容区域 -->
      <el-col :span="20" :xs="24">
        <!-- 搜索表单 -->
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
          <el-form-item label="供应商编码" prop="number">
            <el-input
              v-model="queryParams.number"
              placeholder="请输入供应商编码"
              clearable
              style="width: 200px"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="供应商名称" prop="name">
            <el-input
              v-model="queryParams.name"
              placeholder="请输入供应商名称"
              clearable
              style="width: 200px"
              @keyup.enter="handleQuery"
            />
          </el-form-item>

          <el-form-item label="供应商分组" prop="groupName">
            <el-input
              v-model="queryParams.groupName"
              placeholder="请输入供应商分组"
              clearable
              style="width: 200px"
              @keyup.enter="handleQuery"
            />
          </el-form-item>

          <el-form-item label="主营产品" prop="mainProduct">
            <el-input
              v-model="queryParams.mainProduct"
              placeholder="请输入主营产品"
              clearable
              style="width: 200px"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="供应商分类" prop="supplierCategory">
            <el-input
              v-model="queryParams.supplierCategory"
              placeholder="请输入供应商分类"
              clearable
              style="width: 200px"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="负责人" prop="manager">
            <el-input
              v-model="queryParams.manager"
              placeholder="请输入负责人"
              clearable
              style="width: 200px"
              @keyup.enter="handleQuery"
            />
          </el-form-item>

          <el-form-item label="通讯地址" prop="address">
            <el-input
              v-model="queryParams.address"
              placeholder="请输入通讯地址"
              clearable
              style="width: 200px"
              @keyup.enter="handleQuery"
            />
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
              v-hasPermi="['k3:supplier:save']"
            >新增</el-button>
          </el-col>

          <el-col :span="1.5">
            <el-button
              type="primary"
              plain
              icon="CopyDocument"
              :disabled="single"
              @click="handleCopy"
              v-hasPermi="['k3:supplier:save']"
            >复制</el-button>
          </el-col>

          <el-col :span="1.5">
            <el-button
              type="success"
              plain
              icon="Edit"
              :disabled="single"
              @click="handleUpdate"
              v-hasPermi="['k3:supplier:update']"
            >修改</el-button>
          </el-col>

          <el-col :span="1.5">
            <el-button
              type="success"
              plain
              icon="Edit"
              :disabled="single"
              @click="handleViewAuditLogs"
              v-hasPermi="['k3:supplier:auditLogs']"
            >日志</el-button>
          </el-col>

          <el-col :span="1.5">
            <el-button
              type="danger"
              plain
              icon="Delete"
              :disabled="multiple"
              @click="handleDelete"
              v-hasPermi="['k3:supplier:delete']"
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
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
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
            <div class="setting-header">
              <el-checkbox
                v-model="checkAll"
                :indeterminate="isIndeterminate"
                @change="handleCheckAllChange"
              >
                全选
              </el-checkbox>
              <el-button type="text" @click="resetColumns">重置</el-button>
            </div>

            <div class="column-list">
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
                      <i class="el-icon-rank drag-handle" style="cursor: move; margin-right: 10px; color: #909399;"></i>
                      <el-checkbox
                        v-model="element.visible"
                        @change="handleColumnChange"
                      >
                        {{ element.label }}
                      </el-checkbox>
                    </div>
                    <div class="column-item-right">
                      <el-tag v-if="element.fixed === 'left'" size="small" type="info">固定</el-tag>
                      <el-tag v-if="element.width" size="small" type="info">{{ element.width }}</el-tag>
                    </div>
                  </div>
                </template>
              </draggable>
            </div>
          </div>

          <template #footer>
            <div class="dialog-footer">
              <el-button @click="closeColumnSetting">取消</el-button>
              <el-button type="primary" @click="saveColumnSettings">保存</el-button>
            </div>
          </template>
        </el-dialog>

        <!-- 表格区域 - 优化后 -->
        <div class="table-container">
          <el-table
            v-loading="loading"
            :data="supplierlist"
            @selection-change="handleSelectionChange"
            :key="tableKey"
            row-key="id"
            style="width: 100%"
            :height="tableHeight"
            @row-mouseenter="handleRowMouseEnter"
            @row-mouseleave="handleRowMouseLeave"
            border
            :resizable="true"
            @header-dragend="handleHeaderDragEnd"
            :expand-row-keys="expandedRowKeys"
            @expand-change="handleExpandChange"
            :row-class-name="tableRowClassName"
            @wheel.prevent="handleTableWheel"
            :show-overflow-tooltip="true"
          >
            <el-table-column type="selection" width="55" align="center" fixed="left" :resizable="false" />

            <!-- 编码列 - 固定左侧 -->
            <el-table-column
              prop="number"
              label="编码"
              width="120"
              align="left"
              fixed="left"
              :resizable="true"
            >
              <template #header>
                <div class="column-header">
                  <span v-if="showDragHandles" class="drag-handle">
                    <i class="el-icon-rank" style="cursor: move; margin-right: 5px; color: #909399;"></i>
                  </span>
                  <span>编码</span>
                </div>
              </template>
              <template #default="scope">
                <div
                  class="hover-expand-trigger"
                  @mouseenter="handleCodeMouseEnter(scope.row)"
                  @mouseleave="handleCodeMouseLeave(scope.row)"
                >
                  {{ scope.row.number }}
                </div>
              </template>
            </el-table-column>

            <!-- 名称列 - 固定左侧 -->
            <el-table-column
              prop="name"
              label="名称"
              width="150"
              align="left"
              fixed="left"
              :resizable="true"
            >
              <template #header>
                <div class="column-header">
                  <span v-if="showDragHandles" class="drag-handle">
                    <i class="el-icon-rank" style="cursor: move; margin-right: 5px; color: #909399;"></i>
                  </span>
                  <span>名称</span>
                </div>
              </template>
            </el-table-column>

            <!-- 动态渲染可配置列 -->
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
              <template #header>
                <div class="column-header">
                  <span v-if="showDragHandles" class="drag-handle">
                    <i class="el-icon-rank" style="cursor: move; margin-right: 5px; color: #909399;"></i>
                  </span>
                  <span>{{ col.label }}</span>
                </div>
              </template>

              <!-- 处理特殊字段的显示 -->
              <template #default="scope">
                <!-- 营业执照特殊处理：图片预览 -->
                <div v-if="col.prop === 'businessLicense'" class="license-image-cell" @click.stop>
                  <el-image
                    v-if="scope.row.businessLicense && isImageFile(scope.row.businessLicense)"
                    :src="getImageUrl(scope.row.businessLicense)"
                    :preview-src-list="getPreviewSrcList(scope.row.businessLicense)"
                    :initial-index="0"
                    fit="contain"
                    preview-teleported
                    style="width: 60px; height: 40px; cursor: pointer;"
                    hide-on-click-modal
                    :zoom-rate="1.2"
                    :max-scale="7"
                    :min-scale="0.2"
                  >
                    <template #error>
                      <div class="image-error">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                  <span v-else>{{ scope.row[col.prop] || '-' }}</span>
                </div>

                <!-- 日期字段 -->
                <span v-else-if="col.prop === 'createdAt' || col.prop === 'updatedAt' || col.prop === 'establishDate'">
                  {{ parseTime(scope.row[col.prop]) }}
                </span>

                <!-- 百分比字段 -->
                <span v-else-if="col.prop === 'foreignShare'">
                  {{ scope.row[col.prop] ? (scope.row[col.prop] + '%') : '-' }}
                </span>

                <!-- 数值类型字段右对齐 -->
                <span v-else-if="col.prop === 'defaultTaxRate'">
                  {{ scope.row[col.prop] || '-' }}
                </span>

                <!-- 其他字段居中对齐 -->
                <span v-else>
                  {{ scope.row[col.prop] || '-' }}
                </span>
              </template>
            </el-table-column>

            <!-- 扩展行 - 用于显示联系人和财务信息 -->
            <el-table-column type="expand" width="30" :resizable="false">
              <template #default="scope">
                <div
                  v-if="hasContactOrFinance(scope.row) && expandedRowKeys.includes(scope.row.id)"
                  class="row-expand-container"
                  @mouseenter="handleExpandMouseEnter(scope.row)"
                  @mouseleave="handleExpandMouseLeave(scope.row)"
                >
                  <div class="expand-close-btn" @click.stop="closeExpandRow(scope.row)">
                    <i class="el-icon-close"></i>
                  </div>
                  <el-tabs v-model="expandedRowActiveTab[scope.row.id]" class="expand-tabs">
                    <!-- 联系人信息选项卡 -->
                    <el-tab-pane v-if="scope.row.contactInformation && scope.row.contactInformation.length > 0" label="联系人信息" name="contact">
                      <div class="expand-section">
                        <div class="expand-content">
                          <el-table :data="scope.row.contactInformation" size="mini" border style="width: 100%">
                            <el-table-column prop="contactName" label="联系人" width="120" align="center" />
                            <el-table-column width="84px" label="性别">
                              <template #default="{ row }">
                                <el-tag v-if="row.gender === '0'" type="primary">男</el-tag>
                                <el-tag v-else-if="row.gender === '1'" type="warning">女</el-tag>
                                <el-tag v-else-if="row.gender === '2'" type="info">未知</el-tag>
                              </template>
                            </el-table-column>
                            <el-table-column prop="position" width="84px" label="职务">
                              <template #default="{ row }">
                                <el-tag v-if="row.position === '0'" type="primary">总经理</el-tag>
                                <el-tag v-else-if="row.position === '1'" type="warning">采购跟单</el-tag>
                                <el-tag v-else-if="row.position === '2'" type="success">业务员</el-tag>
                              </template>
                            </el-table-column>
                            <el-table-column prop="phone" label="电话" width="130" align="center" />
                            <el-table-column prop="mobile" label="手机" width="130" align="center" />
                            <el-table-column prop="email" label="电子邮箱" width="180" align="center" />
                          </el-table>
                        </div>
                      </div>
                    </el-tab-pane>

                    <!-- 财务信息选项卡 -->
                    <el-tab-pane
                      v-if="scope.row.financialInformation && scope.row.financialInformation.length > 0"
                      label="财务信息"
                      name="finance"
                    >
                      <div class="expand-section">
                        <div class="expand-content">
                          <el-table :data="scope.row.financialInformation" size="mini" border style="width: 100%">
                            <el-table-column prop="nation" label="国家" width="100" align="center" />
                            <el-table-column prop="bankAccount" label="银行账号" width="180" align="center">
                              <template #default="{ row }">
                                {{ formatBankAccount(row.bankAccount) }}
                              </template>
                            </el-table-column>
                            <el-table-column prop="accountName" label="账户名称" width="150" align="center" />
                            <el-table-column prop="receivingBank" label="收款银行" width="150" align="center" />
                            <el-table-column prop="openingBank" label="开户银行" width="150" align="center" />
                            <el-table-column prop="bankAddress" label="开户行地址" min-width="200" align="left" show-overflow-tooltip />
                          </el-table>
                        </div>
                      </div>
                    </el-tab-pane>

                    <!-- 回访信息选项卡 -->
                    <el-tab-pane v-if="scope.row.supplierVisitRecord && scope.row.supplierVisitRecord.length > 0" label="回访信息" name="visit">
                      <div class="expand-section">
                        <div class="expand-content">
                          <el-table :data="scope.row.supplierVisitRecord" size="mini" border style="width: 100%" :max-height="300">
                            <el-table-column prop="visitTime" label="回访时间" width="150" align="center">
                              <template #default="{ row }">
                                {{ parseTime(row.visitTime) }}
                              </template>
                            </el-table-column>
                            <el-table-column prop="visitor" label="拜访人" width="120" align="center"/>
                            <el-table-column prop="visitContent" label="回访内容" width="800px" show-overflow-tooltip align="center" />
                            <el-table-column label="创建时间" width="150" align="center">
                              <template #default="{ row }">
                                {{ parseTime(row.createdAt) }}
                              </template>
                            </el-table-column>
                            <el-table-column prop="createdBy" label="创建人" width="120" align="center"/>
                            <el-table-column label="附件" width="100" align="center">
                              <template #default="{ row }">
                                <el-button v-if="row.attachment" type="text" size="small" @click.stop="downloadAttachment(row)">查看</el-button>
                                <span v-else>-</span>
                              </template>
                            </el-table-column>
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

        <!-- 分页组件 - 确保正常显示 -->
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
      </el-col>
    </el-row>

    <!-- 新增/修改供应商对话框 -->
    <el-dialog :title="title" v-model="open" width="1800px" append-to-body @close="cancel">
      <el-tabs v-model="activeTab" class="supplier-tabs">
        <!-- 基本信息标签页 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form :model="form" :rules="rules" ref="supplierRef" label-width="120px">
            <el-row :gutter="20">

              <el-col :span="6">
                <el-form-item label="供应商名称" prop="name">
                  <el-input v-model="form.name" placeholder="请输入供应商名称" maxlength="100" />
                </el-form-item>
              </el-col>

              <el-col :span="6">
                <el-form-item label="简称" prop="abbreviation">
                  <el-input v-model="form.abbreviation" placeholder="请输入简称" maxlength="50" />
                </el-form-item>
              </el-col>

              <el-col :span="6">
                <el-form-item label="国家" label-width="100px" prop="nation">
                  <el-select v-model="form.nation" placeholder="请选择国家">
                    <el-option
                      v-for="item in countryOptions"
                      :key="item.value || item.nation"
                      :label="item.nameZh"
                      :value="item.nation"
                    />
                  </el-select>
                </el-form-item>
              </el-col>

              <el-col :span="6">
                <el-form-item label="通讯地址" prop="address">
                  <el-input v-model="form.address" placeholder="请输入通讯地址" maxlength="200" />
                </el-form-item>
              </el-col>

              <el-col :span="6">
                <el-form-item label="法人代表" prop="legalPerson">
                  <el-input v-model="form.legalPerson" placeholder="请输入法人代表" maxlength="50" />
                </el-form-item>
              </el-col>

              <el-col :span="6">
                <el-form-item label="主营产品" prop="mainProduct">
                  <el-input v-model="form.mainProduct" placeholder="请输入主营产品" maxlength="200" />
                </el-form-item>
              </el-col>

              <el-col :span="6">
                <el-form-item label="供应商分组" prop="supplierGroup">
                  <el-tree-select
                    v-model="form.supplierGroup"
                    :data="supplierGroupsOptions"
                    :props="{
                      value: 'supplierGroup',
                      label: 'groupName',
                      children: 'children'
                    }"
                    placeholder="选择供应商分组"
                    check-strictly
                    style="width: 100%"
                    :disabled="isEditMode"
                  />
                </el-form-item>
              </el-col>

              <el-col :span="6">
                <el-form-item label="年度营业额" prop="turnover">
                  <el-input v-model="form.turnover" placeholder="请输入年度营业额" maxlength="200" />
                </el-form-item>
              </el-col>

              <el-col :span="6">
                <el-form-item label="定制评估" prop="customization">
                  <el-input v-model="form.customization" placeholder="请输入定制评估" maxlength="200" />
                </el-form-item>
              </el-col>

              <el-col :span="6">
                <el-form-item label="外销占比%" prop="foreignShare">
                  <el-input v-model="form.foreignShare" placeholder="请输入外销占比%" maxlength="10" />
                </el-form-item>
              </el-col>

              <el-col :span="6">
                <el-form-item label="外销市场区域" prop="exportMarket">
                  <el-input v-model="form.exportMarket" placeholder="请输入外销市场区域" maxlength="200" />
                </el-form-item>
              </el-col>

              <el-col :span="6">
                <el-form-item label="工厂人员数" prop="factoryPeople">
                  <el-input v-model="form.factoryPeople" placeholder="请输入工厂人员数" maxlength="50" />
                </el-form-item>
              </el-col>

              <el-col :span="6">
                <el-form-item label="工厂认证" prop="factoryCertification">
                  <el-input v-model="form.factoryCertification" placeholder="请输入工厂认证" maxlength="200" />
                </el-form-item>
              </el-col>

              <el-col :span="6">
                <el-form-item label="厂房面积" prop="factoryArea">
                  <el-input v-model="form.factoryArea" placeholder="请输入厂房面积" maxlength="200" />
                </el-form-item>
              </el-col>

              <el-col :span="6">
                <el-form-item label="工厂设备" prop="factoryEquipment">
                  <el-input v-model="form.factoryEquipment" placeholder="请输入工厂设备" maxlength="200" />
                </el-form-item>
              </el-col>

              <el-col :span="6">
                <el-form-item label="品控" prop="qualityControl">
                  <el-input v-model="form.qualityControl" placeholder="请输入品控" maxlength="200" />
                </el-form-item>
              </el-col>

              <el-col :span="6">
                <el-form-item label="工厂定位" prop="factoryPositioning">
                  <el-input v-model="form.factoryPositioning" placeholder="请输入工厂定位" maxlength="200" />
                </el-form-item>
              </el-col>

              <el-col :span="6">
                <el-form-item label="优先排款" prop="priorityPayment">
                  <el-select v-model="form.priorityPayment" placeholder="请选择">
                    <el-option
                      v-for="dict in priority_payment"
                      :key="dict.value"
                      :label="dict.label"
                      :value="dict.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>

              <el-col :span="6">
                <el-form-item label="成立日期" prop="establishDate">
                  <el-date-picker
                    v-model="form.establishDate"
                    type="date"
                    placeholder="选择成立日期"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>

              <el-col :span="12">
                <el-form-item label="营业执照" prop="businessLicense">
                  <div class="license-upload-container">
                    <!-- 图片预览 -->
                    <div v-if="form.businessLicense || businessLicenseFile" class="license-preview">
                      <el-image
                        :src="getPreviewImageUrl()"
                        :preview-src-list="getPreviewImageSrcList()"
                        :initial-index="0"
                        fit="contain"
                        preview-teleported
                        style="width: 200px; height: 150px; border: 1px solid #dcdfe6; border-radius: 4px; cursor: pointer;"
                        hide-on-click-modal
                        :zoom-rate="1.2"
                        :max-scale="7"
                        :min-scale="0.2"
                      >
                        <template #error>
                          <div class="image-error-preview">
                            <el-icon><Picture /></el-icon>
                            <span>图片加载失败</span>
                          </div>
                        </template>
                      </el-image>
                    </div>

                    <!-- 上传组件 -->
                    <el-upload
                      ref="licenseUploadRef"
                      class="license-uploader"
                      action="#"
                      :limit="1"
                      :before-upload="beforeLicenseUpload"
                      :on-change="handleLicenseUpload"
                      :on-remove="handleLicenseRemove"
                      :auto-upload="false"
                      accept=".jpg,.jpeg,.png,.gif,.bmp,.webp"
                      :file-list="businessLicenseFileList"
                      :show-file-list="false"
                    >
                      <el-button type="primary" icon="Upload">选择文件</el-button>
                      <template #tip>
                        <div class="el-upload__tip">
                          支持 JPG、PNG、GIF 格式，大小不超过 2MB
                          <div v-if="form.businessLicense">
                            <el-button type="text" @click.stop="clearLicense">清除</el-button>
                          </div>
                        </div>
                      </template>
                    </el-upload>

                    <!-- 已有文件信息 -->
                    <div v-if="form.businessLicense && !businessLicenseFile" class="existing-file-info">
                      <span class="file-name">{{ getFileName(form.businessLicense) }}</span>
                      <el-button type="text" size="small" @click="clearLicense">更换</el-button>
                    </div>
                  </div>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-tab-pane>

        <!-- 业务/结算/税务信息标签页 -->
        <el-tab-pane label="业务/结算/税务信息" name="business">
          <el-row :gutter="24">
            <!-- 业务信息 -->
            <el-col :span="12">
              <div class="section-title">
                <h4>业务信息</h4>
              </div>
              <el-form :model="form" :rules="rules" label-width="120px">

                <el-form-item label="供应商分类" prop="supplierCategory">
                  <el-select
                    v-model="form.supplierCategory"
                    placeholder="请选择供应商分类"
                    filterable
                    clearable
                    style="width: 100%"
                    @focus="loadDictOptions('supplierCategory')"
                  >
                    <el-option
                      v-for="item in dictOptions.supplierCategory"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>

                <el-form-item label="供应类别" prop="supplyType">
                  <el-select
                    v-model="form.supplyType"
                    placeholder="请选择供应类别"
                    filterable
                    clearable
                    style="width: 100%"
                    @focus="loadDictOptions('supplyType')"
                  >
                    <el-option
                      v-for="item in dictOptions.supplyType"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>

                <el-form-item label="供应商来源" prop="source">
                  <el-select
                    v-model="form.source"
                    placeholder="请选择供应商来源"
                    filterable
                    clearable
                    style="width: 100%"
                    @focus="loadDictOptions('source')"
                  >
                    <el-option
                      v-for="item in dictOptions.source"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>

                <el-form-item label="负责人" prop="manager">
                  <el-select
                    v-model="form.manager"
                    placeholder="请选择负责人"
                    filterable
                    clearable
                    style="width: 100%"
                    @focus="loadDictOptions('manager')"
                  >
                    <el-option
                      v-for="item in dictOptions.manager"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>

                <el-form-item label="新增原因" prop="cause">
                  <el-input v-model="form.cause" placeholder="请输入新增原因" maxlength="200" />
                </el-form-item>
              </el-form>
            </el-col>

            <!-- 结算信息 -->
            <el-col :span="12">
              <div class="section-title">
                <h4>结算信息</h4>
              </div>
              <el-form :model="form" :rules="rules" label-width="120px">
                <el-form-item label="结算币别" prop="settlementCurrency">
                  <el-select
                    v-model="form.settlementCurrency"
                    placeholder="请选择结算币别"
                    filterable
                    clearable
                    style="width: 100%"
                    @focus="loadDictOptions('settlementCurrency')"
                  >
                    <el-option
                      v-for="item in dictOptions.settlementCurrency"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="结算方式" prop="settlementMethod">
                  <el-select
                    v-model="form.settlementMethod"
                    placeholder="请选择结算方式"
                    filterable
                    clearable
                    style="width: 100%"
                    @focus="loadDictOptions('settlementMethod')"
                  >
                    <el-option
                      v-for="item in dictOptions.settlementMethod"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="付款条件" prop="paymentTerms">
                  <el-select
                    v-model="form.paymentTerms"
                    placeholder="请选择付款条件"
                    filterable
                    clearable
                    style="width: 100%"
                    @focus="loadDictOptions('paymentTerms')"
                  >
                    <el-option
                      v-for="item in dictOptions.paymentTerms"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="结算方" prop="settlementParty">
                  <el-input v-model="form.settlementParty" placeholder="请输入结算方" maxlength="100" />
                </el-form-item>
                <el-form-item label="收款方" prop="payee">
                  <el-input v-model="form.payee" placeholder="请输入收款方" maxlength="100" />
                </el-form-item>
              </el-form>
            </el-col>
          </el-row>

          <el-row :gutter="24" style="margin-top: 20px">
            <!-- 税务信息 -->
            <el-col :span="12">
              <div class="section-title">
                <h4>税务信息</h4>
              </div>
              <el-form :model="form" :rules="rules" label-width="120px">
                <el-form-item label="发票类型" prop="invoiceType">
                  <el-select
                    v-model="form.invoiceType"
                    placeholder="请选择发票类型"
                    filterable
                    clearable
                    style="width: 100%"
                    @focus="loadDictOptions('invoiceType')"
                  >
                    <el-option
                      v-for="item in dictOptions.invoiceType"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="税分类" prop="taxCategory">
                  <el-select
                    v-model="form.taxCategory"
                    placeholder="请选择税分类"
                    filterable
                    clearable
                    style="width: 100%"
                    @focus="loadDictOptions('taxCategory')"
                  >
                    <el-option
                      v-for="item in dictOptions.taxCategory"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="默认税率(%)" prop="defaultTaxRate">
                  <el-select
                    v-model="form.defaultTaxRate"
                    placeholder="请选择税率"
                    filterable
                    clearable
                    style="width: 100%"
                    @focus="loadDictOptions('defaultTaxRate')"
                  >
                    <el-option
                      v-for="item in dictOptions.defaultTaxRate"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="开票品名" prop="invoiceName">
                  <el-input v-model="form.invoiceName" placeholder="请输入开票品名" maxlength="100" />
                </el-form-item>
              </el-form>
            </el-col>

            <!-- 其他信息 -->
            <el-col :span="12">
              <div class="section-title">
                <h4>其他信息</h4>
              </div>
              <el-form :model="form" :rules="rules" label-width="120px">

                <el-form-item label="工厂问题" prop="contactInfo">
                  <el-input
                    v-model="form.contactInfo"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入工厂问题"
                    maxlength="500"
                  />
                </el-form-item>

                <el-form-item label="老板为人处事风格" label-width="124px" prop="behave">
                  <el-input
                    v-model="form.behave"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入老板为人处事风格"
                    maxlength="500"
                  />
                </el-form-item>

                <el-form-item label="跟单反馈" prop="followUpFeedback">
                  <el-input v-model="form.followUpFeedback" placeholder="请输入跟单反馈" maxlength="200" />
                </el-form-item>

              </el-form>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- 联系人列表标签页 -->
        <el-tab-pane label="联系人列表" name="contacts">
          <el-card class="section-card">
            <div class="section-header">
              <h4>联系人列表</h4>
              <el-button type="dashed" @click="handleAddContact">新增联系人</el-button>
            </div>
            <el-table :data="form.contactInformation" border stripe style="width: 100%">
              <el-table-column prop="contactName" label="联系人" width="140" align="center" />
              <el-table-column width="84px" label="性别">
                <template #default="{ row }">
                  <el-tag v-if="row.gender === '0'" type="primary">男</el-tag>
                  <el-tag v-else-if="row.gender === '1'" type="warning">女</el-tag>
                  <el-tag v-else-if="row.gender === '2'" type="success">未知</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="position" width="84px" label="职务">
                <template #default="{ row }">
                  <el-tag v-if="row.position === '0'" type="primary">总经理</el-tag>
                  <el-tag v-else-if="row.position === '1'" type="warning">采购跟单</el-tag>
                  <el-tag v-else-if="row.position === '2'" type="success">业务员</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="phone" label="电话" width="140" align="center" />
              <el-table-column prop="qq" label="QQ" width="120" align="center" />
              <el-table-column prop="mobile" label="手机" width="140" align="center" />
              <el-table-column prop="email" label="电子邮箱" width="180" align="center" />
              <el-table-column label="操作" width="160" align="center" fixed="right">
                <template #default="{ row, $index }">
                  <el-button type="text" size="small" @click="handleEditContact($index)">编辑</el-button>
                  <el-button type="text" size="small" @click="handleDeleteContact($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-tab-pane>

        <!-- 财务信息标签页 -->
        <el-tab-pane label="财务信息" name="finances">
          <el-card class="section-card">
            <div class="section-header">
              <h4>财务信息</h4>
              <el-button type="dashed" @click="handleAddFinance">新增财务信息</el-button>
            </div>
            <el-table :data="form.financialInformation" border stripe style="width: 100%">
              <el-table-column prop="nation" label="国家" width="120" align="center" />
              <el-table-column prop="bankAccount" label="银行账号" width="160" align="center" />
              <el-table-column prop="accountName" label="账户名称" width="160" align="center" />
              <el-table-column prop="receivingBank" label="收款银行" width="160" align="center" />
              <el-table-column prop="bankAddress" label="开户行地址" width="220" align="center" show-overflow-tooltip />
              <el-table-column prop="openingBank" label="开户银行" width="160" align="center" />
              <el-table-column label="操作" width="160" align="center" fixed="right">
                <template #default="{ row, $index }">
                  <el-button type="text" size="small" @click="handleEditFinance($index)">编辑</el-button>
                  <el-button type="text" size="small" @click="handleDeleteFinance($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-tab-pane>

        <!-- 回访信息标签页 -->
        <el-tab-pane label="回访信息" name="visits">
          <el-card class="section-card">
            <div class="section-header">
              <h4>回访信息</h4>
              <el-button type="dashed" @click="handleAddVisit">新增回访信息</el-button>
            </div>
            <el-table
              :data="form.supplierVisitRecord"
              border
              stripe
              style="width: 100%"
              :max-height="400"
            >
              <el-table-column
                prop="visitTime"
                label="回访时间"
                width="150"
                align="center"
              >
                <template #default="{ row }">
                  {{ parseTime(row.visitTime) }}
                </template>
              </el-table-column>
              <el-table-column
                prop="visitor"
                label="拜访人"
                width="120"
                align="center"
              />
              <el-table-column
                prop="visitContent"
                label="回访内容"
                min-width="300"
                align="left"
                show-overflow-tooltip
                class-name="visit-content-cell"
              />
              <el-table-column label="创建时间" width="150" align="center">
                <template #default="{ row }">
                  {{ parseTime(row.createdAt) }}
                </template>
              </el-table-column>

              <el-table-column prop="createdBy" label="创建人" width="120" align="center"/>
              <el-table-column
                label="附件"
                width="100"
                align="center"
              >
                <template #default="{ row }">
                  <el-button
                    v-if="row.attachment"
                    type="text"
                    size="small"
                    @click.stop="downloadAttachment(row)"
                  >
                    查看
                  </el-button>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column
                label="操作"
                width="160"
                align="center"
                fixed="right"
              >
                <template #default="{ row, $index }">
                  <el-button type="text" size="small" @click="handleEditVisit($index)">编辑</el-button>
                  <el-button type="text" size="small" @click="handleDeleteVisit($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-tab-pane>

      </el-tabs>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>

      <!-- 联系人编辑对话框 -->
      <el-dialog
        v-model="contactDialog.visible"
        :title="contactDialog.isEdit ? '编辑联系人' : '新增联系人'"
        width="50%"
        append-to-body
      >
        <el-form ref="contactFormRef" :model="contactDialog.form" :rules="contactRules" label-width="100px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="联系人" prop="contactName">
                <el-input v-model="contactDialog.form.contactName" placeholder="请输入联系人" />
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="性别" prop="gender">
                <el-select v-model="contactDialog.form.gender" placeholder="请选择">
                  <el-option
                    v-for="dict in sys_user_sex"
                    :key="dict.value"
                    :label="dict.label"
                    :value="dict.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="职务" prop="position">
                <el-select v-model="contactDialog.form.position" placeholder="请选择职务">
                  <el-option
                    v-for="dict in supplier_position"
                    :key="dict.value"
                    :label="dict.label"
                    :value="dict.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="电话" prop="phone">
                <el-input v-model="contactDialog.form.phone" placeholder="请输入电话" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="QQ" prop="qq">
                <el-input v-model="contactDialog.form.qq" placeholder="请输入QQ" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="手机" prop="mobile">
                <el-input v-model="contactDialog.form.mobile" placeholder="请输入手机" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="电子邮箱" prop="email">
                <el-input v-model="contactDialog.form.email" placeholder="请输入电子邮箱" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="contactDialog.visible = false">取消</el-button>
            <el-button type="primary" @click="handleSaveContact">确定</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- 财务信息编辑对话框 -->
      <el-dialog
        v-model="financeDialog.visible"
        :title="financeDialog.isEdit ? '编辑财务信息' : '新增财务信息'"
        width="50%"
        append-to-body
      >
        <el-form ref="financeFormRef" :model="financeDialog.form" :rules="financeRules" label-width="100px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="国家" prop="nation">
                <el-select
                  v-model="financeDialog.form.nation"
                  placeholder="请选择国家"
                  filterable
                  clearable
                  style="width: 100%"
                >
                  <el-option
                    v-for="item in countryOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="银行账号" prop="bankAccount">
                <el-input v-model="financeDialog.form.bankAccount" placeholder="请输入银行账号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="账户名称" prop="accountName">
                <el-input v-model="financeDialog.form.accountName" placeholder="请输入账户名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="收款银行" prop="receivingBank">
                <el-input v-model="financeDialog.form.receivingBank" placeholder="请输入收款银行" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="开户行地址" prop="bankAddress">
                <el-input v-model="financeDialog.form.bankAddress" placeholder="请输入开户行地址" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="开户银行" prop="openingBank">
                <el-input v-model="financeDialog.form.openingBank" placeholder="请输入开户银行" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="financeDialog.visible = false">取消</el-button>
            <el-button type="primary" @click="handleSaveFinance">确定</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- 回访信息编辑对话框 -->
      <el-dialog
        v-model="visitDialog.visible"
        :title="visitDialog.isEdit ? '编辑回访信息' : '新增回访信息'"
        width="50%"
        append-to-body
      >
        <el-form ref="visitFormRef" :model="visitDialog.form" :rules="visitRules" label-width="100px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="拜访人" prop="visitor">
                <el-input v-model="visitDialog.form.visitor" placeholder="请输入拜访人" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="回访时间" prop="visitTime">
                <el-date-picker
                  v-model="visitDialog.form.visitTime"
                  type="datetime"
                  placeholder="选择回访时间"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="回访内容" prop="visitContent">
                <el-input
                  v-model="visitDialog.form.visitContent"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入回访内容"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="附件" prop="attachment">
                <div class="attachment-upload-container">
                  <el-upload
                    ref="visitUploadRef"
                    class="visit-uploader"
                    action="#"
                    :limit="1"
                    :before-upload="beforeVisitUpload"
                    :on-change="handleVisitUpload"
                    :on-remove="handleVisitRemove"
                    :auto-upload="false"
                    :file-list="visitFileList"
                    :show-file-list="true"
                  >
                    <el-button type="primary" icon="Upload">选择文件</el-button>
                    <template #tip>
                      <div class="el-upload__tip">
                        支持所有文件格式，大小不超过 10MB
                      </div>
                    </template>
                  </el-upload>
                </div>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="visitDialog.visible = false">取消</el-button>
            <el-button type="primary" @click="handleSaveVisit">确定</el-button>
          </span>
        </template>
      </el-dialog>
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

<script setup name="Supplier">
import {
  listsupplier,
  addsupplier,
  updatesupplier,
  deletesupplier,
  getsupplier,
  getDictOptions,
  getImagePreviewUrl,
  listSupplierGroups,
  getSupplierAuditLogs,
  getnAtion
} from "@/api/k3/supplier";
import draggable from 'vuedraggable';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ref, reactive, computed, onMounted, nextTick, getCurrentInstance, onBeforeUnmount, watch } from 'vue';
import { Picture, Upload } from '@element-plus/icons-vue';

const { proxy } = getCurrentInstance();
// 字典
const { sys_normal_disable, sys_user_sex, supplier_position ,priority_payment } = proxy.useDict("sys_normal_disable", "sys_user_sex","supplier_position","priority_payment");

const supplierlist = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const columnSettingVisible = ref(false);
const showDragHandles = ref(false);
const tableKey = ref(1);
const checkAll = ref(false);
const isIndeterminate = ref(false);
const activeTab = ref('basic');
const tableHeight = ref(null);
const hoverRowId = ref(null);
const businessLicenseFileList = ref([]);
const businessLicenseFile = ref(null);
const licenseUploadRef = ref(null);

// 供应商分组树形结构相关
const supplierGroupsTreeOptions = ref([]);
const supplierGroupsOptions = ref([]);
const supplierGroupTreeRef = ref(null);

// 扩展行相关状态
const expandedRowActiveTab = ref({});
const expandedRowKeys = ref([]); // 控制展开的行
const hoverCodeRowId = ref(null); // 当前鼠标悬停的编码行ID
const expandCloseTimer = ref(null); // 扩展行关闭计时器

// 新增/修改相关
const open = ref(false);
const title = ref("");

// 审计日志相关数据
const auditLogsDialog = ref(false);
const auditLogsList = ref([]);
const auditLogsLoading = ref(false);
const auditLogsTotal = ref(0);
const auditLogsCurrentPage = ref(1);
const auditLogsPageSize = ref(10);
const auditLogsTitle = ref('');
const currentSupplierId = ref(null);

// 在 ref 变量区域添加
const isEditMode = ref(false);

// 表单数据
const form = ref({
  // 基本信息
  id: undefined,
  number: '',
  name: '',
  abbreviation: '',
  nation: '',
  address: '',
  legalPerson: '',
  establishDate: '',
  foreignShare: '',
  businessLicense: '',
  mainProduct: '',
  k3Id: '', // 供应商分组ID
  turnover: '',
  customization: '',
  exportMarket: '',
  factoryPeople: '',
  factoryCertification: '',
  factoryArea: '',
  factoryEquipment: '',
  qualityControl: '',
  factoryPositioning: '',

  // 新增字段
  groupName: '',
  supplierGroup: '',
  businessRegistration: '',
  contactInfo: '',
  followUpFeedback: '',
  behave: '',
  socialCreditCode: '',
  priorityPayment: '',
  auditTime: '',
  auditor: '',
  moderMap: '',

  // 业务信息
  supplierCategory: '',
  supplyType: '',
  source: '',
  manager: '',
  cause: '',

  // 结算信息
  settlementCurrency: '',
  settlementMethod: '',
  paymentTerms: '',
  settlementParty: '',
  payee: '',

  // 税务信息
  invoiceType: '',
  taxCategory: '',
  defaultTaxRate: '',
  invoiceName: '',

  // 联系人列表
  contactInformation: [],

  // 财务信息
  financialInformation: [],

  // 回访信息
  supplierVisitRecord: []
});

// 表单验证规则
const rules = ref({
  name: [
    { required: true, message: "供应商名称不能为空", trigger: "blur" }
  ],
  abbreviation: [
    { required: true, message: "简称不能为空", trigger: "change" }
  ],
  nation: [
    { required: true, message: "国家不能为空", trigger: "change" }
  ],
  address: [
    { required: true, message: "通讯地址不能为空", trigger: "change" }
  ],
  legalPerson: [
    { required: true, message: "法人代表不能为空", trigger: "change" }
  ],
  mainProduct: [
    { required: true, message: "主营产品不能为空", trigger: "change" }
  ],
  supplierGroup: [
    { required: true, message: "供应商分组能为空", trigger: "change" }
  ],
  supplierCategory: [
    { required: true, message: "供应商分类不能为空", trigger: "change" }
  ],
  settlementMethod: [
    { required: true, message: "结算方式不能为空", trigger: "change" }
  ],
  paymentTerms: [
    { required: true, message: "付款条件不能为空", trigger: "change" }
  ],
  manager: [
    { required: true, message: "负责人不能为空", trigger: "change" }
  ],
  cause: [
    { required: true, message: "新增原因不能为空", trigger: "change" }
  ],
  invoiceType: [
    { required: true, message: "发票类型不能为空", trigger: "change" }
  ],
  invoiceName: [
    { required: true, message: "开票品名不能为空", trigger: "change" }
  ],
  source: [
    { required: true, message: "供应商来源不能为空", trigger: "change" }
  ],
  settlementCurrency: [
    { required: true, message: "结算币别不能为空", trigger: "change" }
  ]
});

// 联系人验证规则
const contactRules = {
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  mobile: [{
    required: true,
    message: '请输入手机号码',
    trigger: 'blur'
  }, {
    pattern: /^1[3-9]\d{9}$/,
    message: '请输入正确的手机号码',
    trigger: 'blur'
  }]
};

// 财务信息验证规则
const financeRules = {
  accountName: [{ required: true, message: '请输入账户名称', trigger: 'blur' }],
  bankAccount: [{ required: true, message: '请输入银行账号', trigger: 'blur' }],
  nation: [{ required: true, message: '请选择国家', trigger: 'change' }]
};

// 回访信息验证规则
const visitRules = {
  visitor: [{ required: true, message: '请输入拜访人', trigger: 'blur' }],
  visitTime: [{ required: true, message: '请选择回访时间', trigger: 'change' }],
  visitContent: [{ required: true, message: '请输入回访内容', trigger: 'blur' }]
};

// 列配置
const allColumns = ref([
  { prop: 'abbreviation', label: '简称', visible: true, width: null, align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'nation', label: '国家', visible: false, width: null, align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'address', label: '通讯地址', visible: true, width: '84px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'legalPerson', label: '法人代表', visible: false, width: '84px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'establishDate', label: '成立日期', visible: false, width: '84px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'foreignShare', label: '国外占比（%）', visible: false, width: '122px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'manager', label: '负责人', visible: true, width: null, align: 'center', fixed: false, showOverflowTooltip: true },
  { prop: 'supplierCategory', label: '供应商分类', visible: true, width: '97px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'groupName', label: '供应商分组', visible: true, width: '97px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'supplyType', label: '供应类别', visible: true, width: '84px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'mainProduct', label: '主营产品', visible: true, width: '84px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'source', label: '来源', visible: false, width: null, align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'cause', label: '新增原因', visible: true, width: '84px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'businessLicense', label: '营业执照', visible: false, width: '84px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'invoiceName', label: '开票品名', visible: true, width: '84px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'contactInfo', label: '工厂问题', visible: true, width: '84px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'followUpFeedback', label: '跟单反馈', visible: false, width: '84px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'settlementCurrency', label: '结算币别', visible: false, width: '84px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'settlementMethod', label: '结算方式', visible: false, width: '84px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'paymentTerms', label: '付款条件', visible: true, width: '84px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'invoiceType', label: '发票类型', visible: false, width: '84px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'taxCategory', label: '税分类', visible: false, width: null, align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'settlementParty', label: '结算方', visible: false, width: null, align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'payee', label: '收款方', visible: false, width: null, align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'defaultTaxRate', label: '默认税率（%）', visible: false, width: '122px', align: 'left', fixed: false, showOverflowTooltip: true },
  // 新增字段列
  { prop: 'turnover', label: '年度营业额', visible: false, width: '100px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'customization', label: '定制评估', visible: false, width: '100px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'exportMarket', label: '外销市场区域', visible: false, width: '120px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'factoryPeople', label: '工厂人员数', visible: false, width: '100px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'factoryCertification', label: '工厂认证', visible: false, width: '100px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'factoryArea', label: '厂房面积', visible: false, width: '100px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'factoryEquipment', label: '工厂设备', visible: false, width: '100px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'qualityControl', label: '品控', visible: false, width: '80px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'factoryPositioning', label: '工厂定位', visible: false, width: '100px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'behave', label: '老板为人处事风格', visible: false, width: '150px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'priorityPayment', label: '优先排款', visible: true, width: '100px', align: 'center', fixed: false, showOverflowTooltip: true },
  { prop: 'createdBy', label: '创建人', visible: true, width: null, align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'createdAt', label: '创建时间', visible: true, width: '180px', align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'updatedBy', label: '修改人', visible: true, width: null, align: 'left', fixed: false, showOverflowTooltip: true },
  { prop: 'updatedAt', label: '修改日期', visible: true, width: '180px', align: 'left', fixed: false, showOverflowTooltip: true }
]);

// 当前显示的列
const visibleColumns = computed(() => {
  return allColumns.value.filter(col => col.visible);
});

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    number: undefined,
    name: undefined,
    mainProduct: undefined,
    supplierCategory: undefined,
    manager: undefined,
    groupName: undefined,
    address: undefined,
    supplierGroup: undefined // 新增：供应商分组查询参数
  }
});

const { queryParams } = toRefs(data);

// 新增功能相关状态
const countryOptions = ref([]);
const dictOptions = reactive({
  supplierCategory: [],
  supplyType: [],
  source: [],
  manager: [],
  settlementCurrency: [],
  settlementMethod: [],
  paymentTerms: [],
  invoiceType: [],
  taxCategory: [],
  defaultTaxRate: []
});

// 弹窗状态
const contactDialog = reactive({
  visible: false,
  isEdit: false,
  editIndex: -1,
  form: {
    contactName: '',
    locationName: '',
    gender: '',
    position: '',
    phone: '',
    qq: '',
    mobile: '',
    email: '',
    address: ''
  }
});

const financeDialog = reactive({
  visible: false,
  isEdit: false,
  editIndex: -1,
  form: {
    nation: '',
    bankAccount: '',
    accountName: '',
    receivingBank: '',
    bankAddress: '',
    openingBank: ''
  }
});

// 回访信息弹窗状态
const visitDialog = reactive({
  visible: false,
  isEdit: false,
  editIndex: -1,
  form: {
    visitor: '',
    visitTime: '',
    visitContent: '',
    attachment: '',
    attachmentFile: null
  }
});

// 回访信息文件上传相关
const visitFileList = ref([]);
const visitFile = ref(null);
const visitUploadRef = ref(null);

// 滚轮翻页相关状态
const isScrolling = ref(false);
const scrollTimer = ref(null);

// 计算属性
const isChina = computed(() => {
  return ['CN', '中国', 'China', '北京', '北京市'].includes(form.value.nation);
});

// 检查行是否有联系人或财务信息或回访信息
const hasContactOrFinance = computed(() => {
  return (row) => {
    if (!row) return false;

    const hasContacts = row.contactInformation &&
      Array.isArray(row.contactInformation) &&
      row.contactInformation.length > 0;

    const hasFinances = row.financialInformation &&
      Array.isArray(row.financialInformation) &&
      row.financialInformation.length > 0;

    const hasVisits = row.supplierVisitRecord &&
      Array.isArray(row.supplierVisitRecord) &&
      row.supplierVisitRecord.length > 0;

    return hasContacts || hasFinances || hasVisits;
  };
});

// draggable配置
const dragOptions = computed(() => ({
  animation: 200,
  group: "columns",
  disabled: false,
  ghostClass: "ghost"
}));

// 图片相关功能
const isImageFile = (url) => {
  if (!url) return false;
  const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp'];
  return imageExtensions.some(ext => url.toLowerCase().includes(ext));
};

const getImageUrl = (url) => {
  return getImagePreviewUrl(url);
};

// 获取预览图片的src列表
const getPreviewSrcList = (url) => {
  if (url && isImageFile(url)) {
    return [getImageUrl(url)];
  }
  return [];
};

const getPreviewImageUrl = () => {
  if (businessLicenseFile.value) {
    return URL.createObjectURL(businessLicenseFile.value);
  } else if (form.value.businessLicense) {
    return getImageUrl(form.value.businessLicense);
  }
  return '';
};

// 获取预览图片的src列表（用于新增/修改页面）
const getPreviewImageSrcList = () => {
  if (businessLicenseFile.value) {
    return [URL.createObjectURL(businessLicenseFile.value)];
  } else if (form.value.businessLicense) {
    return [getImageUrl(form.value.businessLicense)];
  }
  return [];
};

const getFileName = (url) => {
  if (!url) return '';
  const parts = url.split('/');
  return parts[parts.length - 1];
};

// 上传前校验
const beforeLicenseUpload = (file) => {
  const isImage = file.type.startsWith('image/');
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error('只能上传图片文件!');
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!');
    return false;
  }
  return true;
};

// 回访附件上传前校验
const beforeVisitUpload = (file) => {
  const isLt10M = file.size / 1024 / 1024 < 10;

  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!');
    return false;
  }
  return true;
};

// 处理营业执照上传
const handleLicenseUpload = (file, fileList) => {
  if (!beforeLicenseUpload(file.raw)) {
    licenseUploadRef.value.clearFiles();
    return;
  }

  businessLicenseFile.value = file.raw;
  form.value.businessLicense = file.name;
  businessLicenseFileList.value = [file];
};

// 处理回访附件上传
const handleVisitUpload = (file, fileList) => {
  if (!beforeVisitUpload(file.raw)) {
    visitUploadRef.value.clearFiles();
    return;
  }

  visitFile.value = file.raw;
  visitDialog.form.attachment = file.name;
  visitDialog.form.attachmentFile = file.raw; // 存储文件对象
  visitFileList.value = [file];
};

// 处理营业执照移除
const handleLicenseRemove = () => {
  businessLicenseFile.value = null;
  form.value.businessLicense = '';
  businessLicenseFileList.value = [];
};

// 处理回访附件移除
const handleVisitRemove = () => {
  visitFile.value = null;
  visitDialog.form.attachment = '';
  visitDialog.form.attachmentFile = null;
  visitFileList.value = [];
};

// 清除营业执照
const clearLicense = () => {
  businessLicenseFile.value = null;
  form.value.businessLicense = '';
  businessLicenseFileList.value = [];
  if (licenseUploadRef.value) {
    licenseUploadRef.value.clearFiles();
  }
};

// 监听可见列变化
watch(visibleColumns, () => {
  nextTick(() => {
    calculateMinTableWidth();
  });
}, { deep: true });

// 计算最小表格宽度
const calculateMinTableWidth = () => {
  let totalWidth = 0;
  const selectionColumnWidth = 55;
  const operationColumnWidth = 220;
  const expandColumnWidth = 30;
  const columnPadding = 12;

  // 固定列的宽度
  const fixedNumberWidth = 120;
  const fixedNameWidth = 150;

  // 计算动态列的宽度
  visibleColumns.value.forEach(col => {
    if (col.width) {
      const widthStr = col.width.toString();
      if (widthStr.includes('px')) {
        totalWidth += parseInt(widthStr) || 100;
      } else {
        totalWidth += parseInt(widthStr) || 100;
      }
    } else {
      totalWidth += 100;
    }
    totalWidth += columnPadding;
  });

  totalWidth += selectionColumnWidth + fixedNumberWidth + fixedNameWidth + operationColumnWidth + expandColumnWidth + (columnPadding * 3);

  // 不再需要设置最小宽度，让表格自适应
  calculateTableHeight();
};

// 表格高度计算方法 - 优化后
const calculateTableHeight = () => {
  nextTick(() => {
    const container = document.querySelector('.app-container');
    if (!container) return;

    const searchArea = document.querySelector('.el-form');
    const operationArea = document.querySelector('.mb8');
    const paginationArea = document.querySelector('.pagination-wrapper');

    const containerHeight = container.clientHeight;
    const searchHeight = searchArea ? searchArea.clientHeight : 0;
    const operationHeight = operationArea ? operationArea.clientHeight : 0;
    const paginationHeight = paginationArea ? paginationArea.clientHeight : 56;
    const marginHeight = 40;

    // 计算可用高度
    const availableHeight = containerHeight - searchHeight - operationHeight - paginationHeight - marginHeight;

    // 设置最小和最大高度限制
    const minHeight = 200;
    const maxHeight = 800;

    let calculatedHeight = Math.max(availableHeight, minHeight);
    calculatedHeight = Math.min(calculatedHeight, maxHeight);

    tableHeight.value = calculatedHeight;
  });
};

// 加载保存的列设置
const loadColumnSettings = () => {
  const savedColumns = localStorage.getItem('supplierColumns');
  if (savedColumns) {
    try {
      const parsedColumns = JSON.parse(savedColumns);
      const mergedColumns = allColumns.value.map(defaultCol => {
        const savedCol = parsedColumns.find(c => c.prop === defaultCol.prop);
        if (savedCol) {
          return {
            ...defaultCol,
            visible: savedCol.visible !== undefined ? savedCol.visible : defaultCol.visible,
            width: savedCol.width || defaultCol.width
          };
        }
        return defaultCol;
      });
      allColumns.value = mergedColumns;
    } catch (error) {
      console.error('加载列设置失败:', error);
    }
  }
};

/** 更新全选状态 */
const updateCheckAllState = () => {
  const visibleCount = allColumns.value.filter(col => col.visible).length;
  const totalCount = allColumns.value.length;

  checkAll.value = visibleCount === totalCount;
  isIndeterminate.value = visibleCount > 0 && visibleCount < totalCount;
};

// 加载供应商分组数据
const loadSupplierGroups = () => {
  listSupplierGroups().then(response => {
    if (response && response.data) {
      // 处理树形数据
      const processTreeData = (nodes) => {
        return nodes.map(node => {
          const processedNode = {
            ...node,
            supplierGroup: node.supplierGroup || node.k3Id || '',
            groupName: node.groupName || '',
            children: node.children ? processTreeData(node.children) : []
          };
          return processedNode;
        });
      };

      supplierGroupsTreeOptions.value = processTreeData(response.data);
      supplierGroupsOptions.value = processTreeData(response.data);
    }
  }).catch(error => {
    console.error('加载供应商分组失败:', error);
    ElMessage.error('加载供应商分组失败');
  });
};

/** 通过条件过滤节点 */
const filterNode = (value, data) => {
  if (!value) return true;
  return data.groupName.indexOf(value) !== -1;
};

/** 节点单击事件 */
function handleNodeClick(data) {
  queryParams.value.supplierGroup = data.supplierGroup;
  handleQuery();
}

onMounted(() => {
  loadColumnSettings();
  updateCheckAllState();
  loadCountryOptions();
  loadSupplierGroups(); // 加载供应商分组数据
  getList();

  setTimeout(() => {
    calculateTableHeight();
    calculateMinTableWidth();
  }, 300);

  window.addEventListener('resize', calculateTableHeight);
});

// 组件卸载时移除监听器
onBeforeUnmount(() => {
  window.removeEventListener('resize', calculateTableHeight);
  clearTimeout(expandCloseTimer.value);
  clearTimeout(scrollTimer.value);
});

/** 查询供应商列表 */
function getList() {
  loading.value = true;
  listsupplier(queryParams.value).then(response => {
    if (response && response.records) {
      supplierlist.value = response.records.map(item => {
        const row = {
          // 基本信息
          id: item.id || item.id,
          number: item.number || item.number,
          name: item.name || item.name,
          abbreviation: item.abbreviation || '-',
          nation: item.nation || '-',
          address: item.address || '-',
          legalPerson: item.legalPerson || '-',
          establishDate: item.establishDate || '-',
          foreignShare: item.foreignShare || '0%',
          manager: item.manager || '-',
          supplierCategory: item.supplierCategory || '-',
          groupName: item.groupName || '-',
          supplyType: item.supplyType || '-',
          mainProduct: item.mainProduct || '-',
          source: item.source || '-',
          cause: item.cause || '-',
          businessLicense: item.businessLicense || '',
          invoiceName: item.invoiceName || '-',
          contactInfo: item.contactInfo || '-',
          followUpFeedback: item.followUpFeedback || '-',
          settlementCurrency: item.settlementCurrency || '-',
          settlementMethod: item.settlementMethod || '-',
          paymentTerms: item.paymentTerms || '-',
          invoiceType: item.invoiceType || '-',
          taxCategory: item.taxCategory || '-',
          settlementParty: item.settlementParty || '-',
          payee: item.payee || '-',
          defaultTaxRate: item.defaultTaxRate || '-',
          createdBy: item.createdBy || '-',
          createdAt: item.createdAt || '-',
          updatedBy: item.updatedBy || '-',
          updatedAt: item.updatedAt || '-',
          priorityPayment: item.priorityPayment || '_',
          // 新增字段
          turnover: item.turnover || '-',
          customization: item.customization || '-',
          exportMarket: item.exportMarket || '-',
          factoryPeople: item.factoryPeople || '-',
          factoryCertification: item.factoryCertification || '-',
          factoryArea: item.factoryArea || '-',
          factoryEquipment: item.factoryEquipment || '-',
          qualityControl: item.qualityControl || '-',
          factoryPositioning: item.factoryPositioning || '-',
          behave: item.behave || '-',
          contactInformation: item.contactInformation || [],
          financialInformation: item.financialInformation || [],
          // 回访信息
          supplierVisitRecord: item.supplierVisitRecord || []
        };

        // 为有联系人或财务信息的行初始化选项卡状态
        const hasContacts = row.contactInformation &&
          Array.isArray(row.contactInformation) &&
          row.contactInformation.length > 0;
        const hasFinances = row.financialInformation &&
          Array.isArray(row.financialInformation) &&
          row.financialInformation.length > 0;
        const hasVisits = row.supplierVisitRecord &&
          Array.isArray(row.supplierVisitRecord) &&
          row.supplierVisitRecord.length > 0;

        if ((hasContacts || hasFinances || hasVisits) && !expandedRowActiveTab.value[row.id]) {
          if (hasContacts) {
            expandedRowActiveTab.value[row.id] = 'contact';
          } else if (hasFinances) {
            expandedRowActiveTab.value[row.id] = 'finance';
          } else if (hasVisits) {
            expandedRowActiveTab.value[row.id] = 'visit';
          }
        }

        return row;
      });
      total.value = response.total || 0;
    } else {
      supplierlist.value = [];
      total.value = 0;
    }

    loading.value = false;

    nextTick(() => {
      tableKey.value += 1;
      calculateTableHeight();
      calculateMinTableWidth();
    });
  }).catch((error) => {
    console.error('获取供应商列表失败:', error);
    loading.value = false;
    supplierlist.value = [];
    total.value = 0;
    ElMessage.error('获取供应商列表失败');
  });
}

/** 分页条数变化处理 */
function handlePageSizeChange(newSize) {
  queryParams.value.pageSize = newSize;
  queryParams.value.pageNum = 1;
  getList();

  nextTick(() => {
    calculateTableHeight();
  });
}

/** 表格滚轮事件处理 */
function handleTableWheel(event) {
  const tableBody = event.currentTarget.querySelector('.el-table__body-wrapper');
  if (tableBody && tableBody.scrollHeight > tableBody.clientHeight) {
    return;
  }

  handleWheelPagination(event);
}

/** 分页区域滚轮事件处理 */
function handlePaginationWheel(event) {
  handleWheelPagination(event);
}

/** 滚轮翻页处理函数 */
function handleWheelPagination(event) {
  if (isScrolling.value) return;

  const deltaY = event.deltaY;
  const deltaThreshold = 50;

  if (Math.abs(deltaY) < deltaThreshold) return;

  isScrolling.value = true;

  const currentPage = queryParams.value.pageNum;
  const totalPages = Math.ceil(total.value / queryParams.value.pageSize);

  if (deltaY > 0 && currentPage < totalPages) {
    queryParams.value.pageNum = currentPage + 1;
    getList();
  } else if (deltaY < 0 && currentPage > 1) {
    queryParams.value.pageNum = currentPage - 1;
    getList();
  }

  clearTimeout(scrollTimer.value);
  scrollTimer.value = setTimeout(() => {
    isScrolling.value = false;
  }, 300);
}

/** 打开列设置对话框 */
function openColumnSetting() {
  columnSettingVisible.value = true;
  updateCheckAllState();
}

/** 关闭列设置对话框 */
function closeColumnSetting() {
  columnSettingVisible.value = false;
}

/** 保存列设置 */
function saveColumnSettings() {
  try {
    localStorage.setItem('supplierColumns', JSON.stringify(allColumns.value));
    ElMessage.success('列设置已保存');
    columnSettingVisible.value = false;
    tableKey.value += 1;
    calculateTableHeight();
    calculateMinTableWidth();
  } catch (error) {
    console.error('保存列设置失败:', error);
    ElMessage.error('保存列设置失败');
  }
}

/** 重置列设置 */
function resetColumns() {
  allColumns.value.forEach(col => {
    col.visible = true;
  });
  updateCheckAllState();
}

/** 全选/全不选 */
function handleCheckAllChange(val) {
  allColumns.value.forEach(col => {
    col.visible = val;
  });
  isIndeterminate.value = false;
}

/** 单个列选择变化 */
function handleColumnChange() {
  updateCheckAllState();
}

/** 设置对话框中的列拖拽结束 */
function onSettingDragEnd() {
  // 列顺序已通过v-model自动更新
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  queryParams.value.supplierGroup = undefined;

  // 重置树选择
  if (proxy.$refs.supplierGroupTreeRef) {
    proxy.$refs.supplierGroupTreeRef.setCurrentKey(null);
  }

  handleQuery();
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

/** 时间格式化函数 */
function parseTime(time) {
  if (!time) return '';
  try {
    return proxy.$dayjs(time).format('YYYY-MM-DD HH:mm:ss');
  } catch (error) {
    return time;
  }
}

/** 表头拖拽调整列宽事件 */
function handleHeaderDragEnd(column, newWidth, oldWidth) {
  const columnProp = column.property;
  if (columnProp) {
    const columnIndex = allColumns.value.findIndex(col => col.prop === columnProp);
    if (columnIndex !== -1) {
      allColumns.value[columnIndex].width = newWidth;
      saveColumnSettings();
    }
  }

  calculateMinTableWidth();
}

/** 鼠标进入行 */
function handleRowMouseEnter(row) {
  hoverRowId.value = row.id;
}

/** 鼠标离开行 */
function handleRowMouseLeave(row) {
  hoverRowId.value = null;
}

/** 编码单元格鼠标进入事件 */
function handleCodeMouseEnter(row) {
  const hasContacts = row.contactInformation &&
    Array.isArray(row.contactInformation) &&
    row.contactInformation.length > 0;
  const hasFinances = row.financialInformation &&
    Array.isArray(row.financialInformation) &&
    row.financialInformation.length > 0;
  const hasVisits = row.supplierVisitRecord &&
    Array.isArray(row.supplierVisitRecord) &&
    row.supplierVisitRecord.length > 0;

  if (hasContacts || hasFinances || hasVisits) {
    hoverCodeRowId.value = row.id;

    if (!expandedRowActiveTab.value[row.id]) {
      if (hasContacts) {
        expandedRowActiveTab.value[row.id] = 'contact';
      } else if (hasFinances) {
        expandedRowActiveTab.value[row.id] = 'finance';
      } else if (hasVisits) {
        expandedRowActiveTab.value[row.id] = 'visit';
      }
    }

    clearTimeout(expandCloseTimer.value);

    if (!expandedRowKeys.value.includes(row.id)) {
      expandedRowKeys.value = [row.id];
    }
  }
}

/** 编码单元格鼠标离开事件 */
function handleCodeMouseLeave(row) {
  if (hoverCodeRowId.value === row.id) {
    hoverCodeRowId.value = null;

    expandCloseTimer.value = setTimeout(() => {
      if (hoverCodeRowId.value !== row.id) {
        const index = expandedRowKeys.value.indexOf(row.id);
        if (index > -1) {
          expandedRowKeys.value.splice(index, 1);
        }
      }
    }, 40);
  }
}

/** 扩展行容器鼠标进入事件 */
function handleExpandMouseEnter(row) {
  hoverCodeRowId.value = row.id;
  clearTimeout(expandCloseTimer.value);
}

/** 扩展行容器鼠标离开事件 */
function handleExpandMouseLeave(row) {
  expandCloseTimer.value = setTimeout(() => {
    if (hoverCodeRowId.value !== row.id) {
      const index = expandedRowKeys.value.indexOf(row.id);
      if (index > -1) {
        expandedRowKeys.value.splice(index, 1);
      }
    }
  }, 100);
}

/** 手动关闭扩展行 */
function closeExpandRow(row) {
  const index = expandedRowKeys.value.indexOf(row.id);
  if (index > -1) {
    expandedRowKeys.value.splice(index, 1);
  }
  hoverCodeRowId.value = null;
}

/** 处理展开变化事件 */
function handleExpandChange(row, expandedRows) {
  if (expandedRows.includes(row)) {
    if (!expandedRowKeys.value.includes(row.id)) {
      expandedRowKeys.value = [row.id];
    }
  } else {
    const index = expandedRowKeys.value.indexOf(row.id);
    if (index > -1) {
      expandedRowKeys.value.splice(index, 1);
    }
  }
}

/** 添加行类名方法，用于自定义样式 */
const tableRowClassName = ({ row }) => {
  if (expandedRowKeys.value.includes(row.id)) {
    return 'expanded-row';
  }
  return '';
};

/** 格式化银行账号 */
function formatBankAccount(account) {
  if (!account) return '';
  const cleanAccount = account.replace(/\s+/g, '');
  return cleanAccount.replace(/(.{4})/g, '$1 ').trim();
}

/** 表单重置 */
function reset() {
  form.value = {
    id: undefined,
    number: '',
    name: '',
    abbreviation: '',
    nation: '',
    address: '',
    legalPerson: '',
    establishDate: '',
    foreignShare: '',
    businessLicense: '',
    mainProduct: '',
    k3Id: '',
    turnover: '',
    customization: '',
    exportMarket: '',
    factoryPeople: '',
    factoryCertification: '',
    factoryArea: '',
    factoryEquipment: '',
    qualityControl: '',
    factoryPositioning: '',

    // 新增字段
    groupName: '',
    supplierGroup: '',
    businessRegistration: '',
    contactInfo: '',
    followUpFeedback: '',
    behave: '',
    socialCreditCode: '',
    priorityPayment: '',
    auditTime: '',
    auditor: '',
    moderMap: '',

    // 业务信息
    supplierCategory: '',
    supplyType: '',
    source: '',
    manager: '',
    cause: '',

    // 结算信息
    settlementCurrency: '',
    settlementMethod: '',
    paymentTerms: '',
    settlementParty: '',
    payee: '',

    // 税务信息
    invoiceType: '',
    taxCategory: '',
    defaultTaxRate: '',
    invoiceName: '',

    // 联系人列表
    contactInformation: [],

    // 财务信息
    financialInformation: [],

    // 回访信息
    supplierVisitRecord: []
  };

  businessLicenseFile.value = null;
  businessLicenseFileList.value = [];
  visitFile.value = null;
  visitFileList.value = [];

  if (supplierRef.value) {
    supplierRef.value.resetFields();
  }
  activeTab.value = 'basic';
}

/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  loadSupplierGroups(); // 加载供应商分组
  open.value = true;
  title.value = "新增供应商";
  isEditMode.value = false;

  // 获取树形组件当前选中的节点
  if (supplierGroupTreeRef.value) {
    const currentNode = supplierGroupTreeRef.value.getCurrentNode();
    if (currentNode && currentNode.supplierGroup) {
      form.value.supplierGroup = currentNode.supplierGroup;
    }
  }
}

/** 复制按钮操作 */
function handleCopy(row) {
  reset();
  const id = row.id || (ids.value.length === 1 ? ids.value[0] : null);

  if (!id) {
    ElMessage.warning('请选择一条数据');
    return;
  }

  loading.value = true;

  getsupplier(id).then(response => {
    const data = response.data || response;

    const formKeys = Object.keys(form.value);

    formKeys.forEach(key => {
      if (key !== 'id' &&
        key !== 'number' &&
        key !== 'contactInformation' &&
        key !== 'financialInformation' &&
        key !== 'supplierVisitRecord' &&
        data[key] !== undefined) {

        if (data[key] === null || data[key] === 'null') {
          form.value[key] = '';
        } else {
          form.value[key] = data[key];
        }
      }
    });

    form.value.id = undefined;
    form.value.number = '';

    if (form.value.name) {
      form.value.name = form.value.name + ' - 更改';
    }

    if (data.contactInformation && Array.isArray(data.contactInformation)) {
      form.value.contactInformation = data.contactInformation.map(item => ({
        contactName: item.contactName || '',
        locationName: item.locationName || '',
        gender: item.gender || '',
        position: item.position || '',
        phone: item.phone || '',
        qq: item.qq || '',
        mobile: item.mobile || '',
        email: item.email || '',
        address: item.address || ''
      }));
    } else {
      form.value.contactInformation = [];
    }

    if (data.financialInformation && Array.isArray(data.financialInformation)) {
      form.value.financialInformation = data.financialInformation.map(item => ({
        nation: item.nation || '',
        bankAccount: item.bankAccount || '',
        accountName: item.accountName || '',
        receivingBank: item.receivingBank || '',
        bankAddress: item.bankAddress || '',
        openingBank: item.openingBank || ''
      }));
    } else {
      form.value.financialInformation = [];
    }

    if (data.supplierVisitRecord && Array.isArray(data.supplierVisitRecord)) {
      form.value.supplierVisitRecord = data.supplierVisitRecord.map(item => ({
        visitor: item.visitor || '',
        visitTime: item.visitTime || '',
        visitContent: item.visitContent || '',
        attachment: item.attachment || '',
        attachmentFile: null,
        createdAt: item.createdAt || '',
        createdBy: item.createdBy || ''
      }));
    } else {
      form.value.supplierVisitRecord = [];
    }

    if (data.businessLicense) {
      form.value.businessLicense = data.businessLicense;
      businessLicenseFileList.value = [{
        name: getFileName(data.businessLicense),
        url: getImageUrl(data.businessLicense)
      }];
    }

    open.value = true;
    title.value = "复制新增供应商";
    isEditMode.value = false;

    loadSupplierGroups();

    ElMessage.success('数据已复制到表单，请修改后保存');

  }).catch(error => {
    console.error('获取供应商详情失败:', error);
    ElMessage.error('获取供应商详情失败');
  }).finally(() => {
    loading.value = false;
  });
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const number = row.number || (ids.value.length === 1 ? ids.value[0] : null);
  if (number) {
    getsupplier(number).then(response => {
      const data = response.data || response;

      const formKeys = Object.keys(form.value);

      formKeys.forEach(key => {
        if (key !== 'contactInformation' &&
          key !== 'financialInformation' &&
          key !== 'supplierVisitRecord' &&
          data[key] !== undefined) {
          if (data[key] === null || data[key] === 'null') {
            form.value[key] = '';
          } else {
            form.value[key] = data[key];
          }
        }
      });

      if (data.turnover !== undefined) {
        form.value.turnover = data.turnover || '';
      }

      if (data.customization !== undefined) {
        form.value.customization = data.customization || '';
      }

      if (data.factoryCertification !== undefined) {
        form.value.factoryCertification = data.factoryCertification || '';
      }

      if (data.qualityControl !== undefined) {
        form.value.qualityControl = data.qualityControl || '';
      }

      if (data.factoryPositioning !== undefined) {
        form.value.factoryPositioning = data.factoryPositioning || '';
      }

      if (data.factoryArea !== undefined) {
        form.value.factoryArea = data.factoryArea || '';
      }

      if (data.factoryEquipment !== undefined) {
        form.value.factoryEquipment = data.factoryEquipment || '';
      }

      if (data.foreignShare !== undefined) {
        form.value.foreignShare = data.foreignShare || '';
      }

      if (data.exportMarket !== undefined) {
        form.value.exportMarket = data.exportMarket || '';
      }

      if (data.factoryPeople !== undefined) {
        form.value.factoryPeople = data.factoryPeople || '';
      }

      if (data.supplierGroup !== undefined) {
        form.value.supplierGroup = data.supplierGroup || '';
      }

      if (data.groupName !== undefined) {
        form.value.groupName = data.groupName || '';
      }

      if (data.behave !== undefined) {
        form.value.behave = data.behave || '';
      }

      if (data.contactInfo !== undefined) {
        form.value.contactInfo = data.contactInfo || '';
      }

      if (data.followUpFeedback !== undefined) {
        form.value.followUpFeedback = data.followUpFeedback || '';
      }

      if (data.contactInformation && Array.isArray(data.contactInformation)) {
        form.value.contactInformation = data.contactInformation.map(item => ({
          id: item.id || '',
          contactName: item.contactName || '',
          locationName: item.locationName || '',
          gender: item.gender || '',
          position: item.position || '',
          phone: item.phone || '',
          qq: item.qq || '',
          mobile: item.mobile || '',
          email: item.email || '',
          address: item.address || ''
        }));
      } else {
        form.value.contactInformation = [];
      }

      if (data.financialInformation && Array.isArray(data.financialInformation)) {
        form.value.financialInformation = data.financialInformation.map(item => ({
          id: item.id || '',
          nation: item.nation || '',
          bankAccount: item.bankAccount || '',
          accountName: item.accountName || '',
          receivingBank: item.receivingBank || '',
          bankAddress: item.bankAddress || '',
          openingBank: item.openingBank || ''
        }));
      } else {
        form.value.financialInformation = [];
      }

      if (data.supplierVisitRecord && Array.isArray(data.supplierVisitRecord)) {
        form.value.supplierVisitRecord = data.supplierVisitRecord.map(item => ({
          id: item.id || '',
          supplierId: item.supplierId || '',
          visitTime: item.visitTime || '',
          visitor: item.visitor || '',
          visitContent: item.visitContent || '',
          attachment: item.attachment || '',
          attachmentFile: null,
          createdAt: item.createdAt || '',
          createdBy: item.createdBy || ''
        }));
      } else {
        form.value.supplierVisitRecord = [];
      }

      if (data.businessLicense) {
        form.value.businessLicense = data.businessLicense;
        businessLicenseFileList.value = [{
          name: getFileName(data.businessLicense),
          url: getImageUrl(data.businessLicense)
        }];
      }

      open.value = true;
      title.value = "修改供应商";
      isEditMode.value = true;
    }).catch(error => {
      console.error('获取供应商详情失败:', error);
      ElMessage.error('获取供应商详情失败');
    });
  } else {
    ElMessage.warning('请选择一条数据');
  }
}

/** 提交表单 */
function submitForm() {
  supplierRef.value.validate(valid => {
    if (valid) {
      const submitData = {
        ...form.value,
        contactInformation: form.value.contactInformation || [],
        financialInformation: form.value.financialInformation || [],
        supplierVisitRecord: form.value.supplierVisitRecord || [],
        businessLicenseFile: businessLicenseFile.value
      };

      if (submitData.supplierVisitRecord && submitData.supplierVisitRecord.length > 0) {
        submitData.supplierVisitRecord = submitData.supplierVisitRecord.map(record => {
          if (record.attachmentFile && record.attachmentFile instanceof File) {
            return {
              ...record,
              attachment: record.attachmentFile.name
            };
          }
          return record;
        });
      }

      if (form.value.id) {
        submitData.id = form.value.id;

        updatesupplier(submitData).then(response => {
          ElMessage.success("修改成功");
          open.value = false;
          getList();
        }).catch(error => {
          console.error('修改失败:', error);
          ElMessage.error("修改失败: " + (error.message || '请检查网络连接'));
        });
      } else {
        addsupplier(submitData).then(response => {
          ElMessage.success("新增成功");
          open.value = false;
          getList();
        }).catch(error => {
          console.error('新增失败:', error);
          ElMessage.error("新增失败: " + (error.message || '请检查网络连接'));
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const number = row.number || (ids.value.length > 0 ? ids.value : []);
  if (!number || (Array.isArray(number) && number.length === 0)) {
    ElMessage.warning("请选择要删除的数据");
    return;
  }

  const numbers = Array.isArray(number) ? number : [number];

  ElMessageBox.confirm('是否确认删除选中的供应商？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    return deletesupplier(numbers);
  }).then(() => {
    ElMessage.success("删除成功");
    getList();
  }).catch(() => {});
}

/** 加载国家选项 */
async function loadCountryOptions() {
  getnAtion().then(response => {
    countryOptions.value = response.data;
  });
}

/** 加载字典选项 */
async function loadDictOptions(type) {
  if (dictOptions[type] && dictOptions[type].length > 0) return;

  try {
    const response = await getDictOptions(type);
    const raw = response.data || response;
    const list = Array.isArray(raw) ? raw :
      Array.isArray(raw?.data) ? raw.data :
        Array.isArray(raw?.items) ? raw.items :
          Array.isArray(raw?.result) ? raw.result : [];

    dictOptions[type] = list.map(item => {
      if (type === 'manager') {
        return {
          label: item?.nickName ?? item?.name ?? item?.label ?? String(item),
          value: item?.staffId ?? item?.id ?? item?.value ?? String(item)
        };
      }
      if (type === 'defaultTaxRate') {
        return {
          label: item?.name ?? item?.label ?? String(item),
          value: item?.code ?? item?.id ?? item?.value ?? String(item)
        };
      }
      return {
        label: item?.name ?? item?.label ?? String(item),
        value: item?.productCategory ?? item?.productCategory ?? item?.id ?? item?.value ?? String(item)
      };
    });
  } catch (error) {
    console.error(`加载${type}字典失败:`, error);
    dictOptions[type] = [];
  }
}

/** 联系人管理 */
function handleAddContact() {
  contactDialog.isEdit = false;
  contactDialog.editIndex = -1;
  contactDialog.form = {
    contactName: '',
    locationName: '',
    gender: '',
    position: '',
    phone: '',
    qq: '',
    mobile: '',
    email: '',
    address: ''
  };
  contactDialog.visible = true;
}

function handleEditContact(index) {
  contactDialog.isEdit = true;
  contactDialog.editIndex = index;
  contactDialog.form = { ...form.value.contactInformation[index] };
  contactDialog.visible = true;
}

function handleDeleteContact(index) {
  ElMessageBox.confirm('确定删除该联系人吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    form.value.contactInformation.splice(index, 1);
    ElMessage.success('删除成功');
  }).catch(() => {});
}

function handleSaveContact() {
  if (contactFormRef.value) {
    contactFormRef.value.validate(valid => {
      if (valid) {
        if (contactDialog.isEdit && contactDialog.editIndex >= 0) {
          form.value.contactInformation.splice(contactDialog.editIndex, 1, { ...contactDialog.form });
        } else {
          form.value.contactInformation.push({ ...contactDialog.form });
        }
        contactDialog.visible = false;
      }
    });
  }
}

/** 财务管理 */
function handleAddFinance() {
  financeDialog.isEdit = false;
  financeDialog.editIndex = -1;
  financeDialog.form = {
    nation: '',
    bankAccount: '',
    accountName: '',
    receivingBank: '',
    bankAddress: '',
    openingBank: ''
  };
  financeDialog.visible = true;
}

function handleEditFinance(index) {
  financeDialog.isEdit = true;
  financeDialog.editIndex = index;
  financeDialog.form = { ...form.value.financialInformation[index] };
  financeDialog.visible = true;
}

function handleDeleteFinance(index) {
  ElMessageBox.confirm('确定删除该财务信息吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    form.value.financialInformation.splice(index, 1);
    ElMessage.success('删除成功');
  }).catch(() => {});
}

function handleSaveFinance() {
  if (financeFormRef.value) {
    financeFormRef.value.validate(valid => {
      if (valid) {
        if (financeDialog.isEdit && financeDialog.editIndex >= 0) {
          form.value.financialInformation.splice(financeDialog.editIndex, 1, { ...financeDialog.form });
        } else {
          form.value.financialInformation.push({ ...financeDialog.form });
        }
        financeDialog.visible = false;
      }
    });
  }
}

/** 回访信息管理 */
function handleAddVisit() {
  visitDialog.isEdit = false;
  visitDialog.editIndex = -1;
  visitDialog.form = {
    visitor: '',
    visitTime: '',
    visitContent: '',
    attachment: '',
    attachmentFile: null
  };
  visitFileList.value = [];
  visitFile.value = null;
  visitDialog.visible = true;
}

function handleEditVisit(index) {
  visitDialog.isEdit = true;
  visitDialog.editIndex = index;
  visitDialog.form = { ...form.value.supplierVisitRecord[index] };

  visitFile.value = null;
  visitFileList.value = [];
  visitDialog.form.attachmentFile = null;

  if (visitDialog.form.attachment) {
    visitFileList.value = [{
      name: visitDialog.form.attachment,
      url: visitDialog.form.attachment
    }];
  }

  visitDialog.visible = true;
}

function handleDeleteVisit(index) {
  ElMessageBox.confirm('确定删除该回访信息吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    form.value.supplierVisitRecord.splice(index, 1);
    ElMessage.success('删除成功');
  }).catch(() => {});
}

function handleSaveVisit() {
  if (visitFormRef.value) {
    visitFormRef.value.validate(valid => {
      if (valid) {
        const visitData = {
          ...visitDialog.form,
          id: visitDialog.form.id || '',
          supplierId: form.value.id,
          createdAt: visitDialog.form.createdAt || '',
          createdBy: visitDialog.form.createdBy || ''
        };

        if (visitFile.value) {
          visitData.attachmentFile = visitFile.value;
          visitData.attachment = visitFile.value.name;
        }

        if (visitDialog.isEdit && visitDialog.editIndex >= 0) {
          form.value.supplierVisitRecord.splice(visitDialog.editIndex, 1, visitData);
        } else {
          form.value.supplierVisitRecord.push(visitData);
        }

        visitFile.value = null;
        visitFileList.value = [];

        visitDialog.visible = false;
      }
    });
  }
}

/** 下载附件 */
function downloadAttachment(row) {
  if (row.attachment) {
    window.open(row.attachment, '_blank');
  }
}

/** 查看审计日志 */
function handleViewAuditLogs(row) {
  const number = row.number || (ids.value.length === 1 ? ids.value[0] : null);
  currentSupplierId.value = number;
  auditLogsTitle.value = `供应商审计日志`;
  auditLogsCurrentPage.value = 1;
  getSupplierAuditLogsList();
  auditLogsDialog.value = true;
}

/** 获取审计日志列表 */
async function getSupplierAuditLogsList() {
  try {
    auditLogsLoading.value = true;
    const response = await getSupplierAuditLogs(
      currentSupplierId.value,
      auditLogsCurrentPage.value,
      auditLogsPageSize.value
    );

    if (response.code === 200) {
      auditLogsList.value = response.data.records || [];
      auditLogsTotal.value = response.data.total || 0;

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
      ElMessage.error('获取审计日志失败: ' + response.msg);
    }
  } catch (error) {
    console.error('获取审计日志失败:', error);
    ElMessage.error('获取审计日志失败');
  } finally {
    auditLogsLoading.value = false;
  }
}

/** 获取字段名称 */
function getFieldName(fieldKey) {
  const fieldMap = {
    'name': '供应商名称',
    'abbreviation': '简称',
    'nation': '国家',
    'address': '通讯地址',
    'legal_person': '法人代表',
    'establishDate': '成立日期',
    'foreign_share': '外销占比',
    'businessLicense': '营业执照',
    'main_product': '主营产品',
    'supplier_category': '供应商分类',
    'supply_type': '供应类别',
    'source': '来源',
    'manager': '负责人',
    'cause': '新增原因',
    'settlement_currency': '结算币别',
    'settlement_method': '结算方式',
    'payment_terms': '付款条件',
    'settlement_party': '结算方',
    'payee': '收款方',
    'invoice_type': '发票类型',
    'tax_category': '税分类',
    'default_tax_rate': '默认税率',
    'invoice_name': '开票品名',
    'contact_info': '工厂问题',
    'follow_up_feedback': '跟单反馈',
    'createdBy': '创建人',
    'updated_by': '修改人',
    'turnover': '年度营业额',
    'customization': '定制评估',
    'export_market': '外销市场区域',
    'factory_people': '工厂人员数',
    'factory_certification': '工厂认证',
    'factory_area': '厂房面积',
    'factory_equipment': '工厂设备',
    'quality_control': '品控',
    'factory_positioning': '工厂定位',
    'behave': '老板为人处事风格',
    'supplierVisitRecord': '回访信息'
  };
  return fieldMap[fieldKey] || fieldKey;
}

/** 审计日志分页变化 */
function handleAuditLogsPaginationChange(page) {
  auditLogsCurrentPage.value = page;
  getSupplierAuditLogsList();
}

// 添加缺失的ref引用
const supplierRef = ref();
const contactFormRef = ref();
const financeFormRef = ref();
const visitFormRef = ref();
const queryRef = ref();
</script>

<style scoped>
/* 修改 .app-container 添加左右边距和高度 */
.app-container {
  margin: 0 20px;
  height: calc(100vh - 84px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 左侧树形结构样式 */
.head-container {
  margin-top: 10px;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #fff;
  height: calc(100vh - 180px);
  overflow-y: auto;
}

:deep(.head-container .el-tree) {
  background-color: transparent;
}

:deep(.head-container .el-tree-node__content) {
  height: 36px;
}

:deep(.head-container .el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content) {
  background-color: #ecf5ff;
  color: #409eff;
}

/* 搜索表单区域 */
.el-form {
  flex-shrink: 0;
  margin-bottom: 20px;
}

/* 操作按钮区域 */
.mb8 {
  flex-shrink: 0;
  margin-bottom: 20px;
}

/* 表格容器 - 优化后 */
.table-container {
  flex: 1;
  position: relative;
  overflow: hidden;
  margin-bottom: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #fff;
}

/* 移除 .draggable-container 相关样式，直接使用表格容器 */

:deep(.el-table) {
  width: 100% !important;
}

:deep(.el-table__body-wrapper) {
  overflow-y: auto !important;
  overflow-x: auto !important;
}

:deep(.el-table__header-wrapper) {
  position: sticky;
  top: 0;
  z-index: 2;
  background-color: #fff;
}

:deep(.el-table .el-table__cell.is-fixed-left),
:deep(.el-table .el-table__cell.is-fixed-right) {
  background-color: #fff;
  z-index: 3;
}

.column-header {
  display: flex;
  align-items: center;
  cursor: move;
}

.drag-handle {
  cursor: move;
}

/* 分页区域包装器 - 确保正常显示 */
.pagination-wrapper {
  flex-shrink: 0;
  padding: 12px 16px;
  background-color: #fff;
  border-top: 1px solid #ebeef5;
  cursor: pointer;
  position: relative;
  z-index: 10; /* 确保分页在最上层 */
}

.pagination-wrapper:hover {
  background-color: #f5f7fa;
}

/* 图片相关样式 */
.license-image-cell {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 50px;
}

.license-image-cell .image-error {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 40px;
  background-color: #f5f5f5;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  color: #909399;
}

.license-upload-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.license-preview {
  display: flex;
  justify-content: flex-start;
}

.image-error-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 200px;
  height: 150px;
  background-color: #f5f5f5;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  color: #909399;
}

.image-error-preview .el-icon {
  font-size: 40px;
  margin-bottom: 8px;
}

.existing-file-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.existing-file-info .file-name {
  color: #606266;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
}

/* 添加悬停触发区域样式 */
.hover-expand-trigger {
  cursor: pointer;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 12px;
}

.hover-expand-trigger:hover {
  background-color: #f0f9ff;
  color: #1890ff;
  font-weight: 500;
}

/* 扩展行容器样式 */
.row-expand-container {
  position: relative;
  z-index: 100;
  background-color: #fff;
  border-top: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 扩展行关闭按钮样式 */
.expand-close-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 1001;
  cursor: pointer;
  color: #909399;
  font-size: 14px;
}

.expand-close-btn:hover {
  color: #1890ff;
}

/* 扩展行选项卡样式 */
.expand-tabs {
  margin: 0;
}

:deep(.expand-tabs .el-tabs__header) {
  margin: 0;
  padding: 0 16px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #ebeef5;
}

:deep(.expand-tabs .el-tabs__nav-wrap::after) {
  display: none;
}

:deep(.expand-tabs .el-tabs__item) {
  padding: 0 16px;
  height: 36px;
  line-height: 36px;
}

:deep(.expand-tabs .el-tabs__content) {
  padding: 16px;
}

/* 扩展行内容 */
.expand-section {
  margin-bottom: 0;
}

.expand-content {
  background-color: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
  max-height: 400px;
  overflow-y: auto;
}

:deep(.expand-content .el-table) {
  min-width: auto !important;
}

:deep(.expand-content .el-table__header-wrapper) {
  background-color: #f8f9fa;
}

:deep(.expand-content .el-table th) {
  background-color: #f8f9fa;
  padding: 6px 0;
  font-weight: 500;
}

:deep(.expand-content .el-table td) {
  padding: 4px 0;
}

:deep(.expand-content .el-table .cell) {
  font-size: 12px;
}

/* 回访信息表格样式优化 */
:deep(.expand-content .visit-content-cell) {
  white-space: normal !important;
  line-height: 1.4;
  max-height: 60px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.column-setting-container {
  max-height: 500px;
  overflow-y: auto;
}

.setting-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.column-list {
  margin-top: 10px;
}

.column-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
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

.ghost {
  opacity: 0.5;
  background: #c8ebfb;
}

/* 新增功能的样式 */
.supplier-tabs {
  margin: -20px -20px 0 -20px;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.section-title h4 {
  margin: 0;
  font-weight: 600;
  font-size: 16px;
  color: #1890ff;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h4 {
  margin: 0;
  font-weight: 600;
  font-size: 16px;
  color: #1890ff;
}

.section-card {
  margin-top: 20px;
}

.license-uploader {
  width: 100%;
}

:deep(.el-upload) {
  width: 100%;
}

:deep(.el-upload .el-button) {
  width: 100%;
}

/* 附件上传样式 */
.attachment-upload-container {
  width: 100%;
}

.visit-uploader {
  width: 100%;
}

:deep(.visit-uploader .el-upload) {
  width: 100%;
}

:deep(.visit-uploader .el-upload .el-button) {
  width: 100%;
}

/* 确保表格单元格内容可见 */
:deep(.el-table .cell) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 空数据样式 */
:deep(.el-table__empty-block) {
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 滚动条优化 */
.table-container::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.table-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.table-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.table-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 表格内部滚动条 */
:deep(.el-table__body-wrapper)::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

:deep(.el-table__body-wrapper)::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

:deep(.el-table__body-wrapper)::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

:deep(.el-table__body-wrapper)::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 表格列对齐优化 */
:deep(.el-table td),
:deep(.el-table th) {
  padding: 8px 0;
}

:deep(.el-table__header th) {
  font-weight: 600;
  background-color: #f8f9fa;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background-color: #fafafa;
}

:deep(.el-table .el-button--text) {
  padding: 0;
  min-height: auto;
}

:deep(.el-table .el-button--text + .el-button--text) {
  margin-left: 8px;
}

/* 扩展列箭头样式优化 */
:deep(.el-table__expand-icon) {
  transition: transform 0.2s;
}

:deep(.el-table__expand-icon--expanded) {
  transform: rotate(90deg);
}

:deep(.el-table__expand-icon:hover) {
  color: #1890ff;
}

/* 悬停行样式 */
:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

/* 悬停行样式优化 */
:deep(.el-table__row.expanded-row) {
  background-color: #f0f9ff !important;
}

:deep(.el-table__row.expanded-row:hover) {
  background-color: #e6f7ff !important;
}

/* 扩展行展开动画 - 优化为即时显示 */
:deep(.el-table__expanded-cell) {
  padding: 0 !important;
  background-color: #fff !important;
  border-top: none !important;
  transition: none !important;
  animation: none !important;
}

/* 确保扩展行内容不被遮挡 */
:deep(.el-table__expanded-cell .row-expand-container) {
  position: relative;
  z-index: 1000;
}

/* 列宽调整手柄样式 */
:deep(.el-table__header-wrapper .el-table__header th .cell) {
  position: relative;
}

:deep(.el-table__header-wrapper .el-table__header th .cell::after) {
  content: '';
  position: absolute;
  right: 0;
  top: 0;
  width: 6px;
  height: 100%;
  cursor: col-resize;
  z-index: 1;
}

/* 固定列阴影效果 */
:deep(.el-table__fixed::before),
:deep(.el-table__fixed-right::before) {
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

/* 图片预览对话框样式 */
:deep(.el-image-viewer__wrapper) {
  z-index: 9999 !important;
}

:deep(.el-image-viewer__close) {
  color: #fff;
}

/* 审计日志样式 */
.tagWithLine {
  text-decoration: line-through;
  color: #999;
}

.changes-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 16px;
}

.change-item-horizontal {
  flex: 0 0 calc(25% - 12px);
  min-width: 150px;
  max-width: 250px;
  box-sizing: border-box;
}

.change-field-horizontal {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #e9ecef;
}

.field-name {
  font-weight: 600;
  color: #495057;
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.value-container {
  display: flex;
  align-items: center;
  gap: 4px;
}

.old-value-section {
  display: flex;
  align-items: center;
}

.tagWithLine,
.new-value {
  flex-shrink: 0;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.el-dialog__body) {
  padding: 20px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .app-container {
    margin: 0 10px;
    height: calc(100vh - 100px);
  }

  :deep(.expand-tabs .el-tabs__item) {
    padding: 0 8px;
    font-size: 12px;
  }

  .expand-content {
    max-height: 300px;
  }

  .hover-expand-trigger {
    padding: 0 8px;
  }

  .row-expand-container {
    max-width: 100vw;
    overflow-x: auto;
  }
}

/* 扩展行悬停区域 */
:deep(.el-table__expanded-cell):hover {
  background-color: #f5f7fa !important;
}

/* 回访信息附件按钮样式 */
:deep(.expand-content .el-button--text) {
  color: #1890ff;
  font-size: 12px;
}

:deep(.expand-content .el-button--text:hover) {
  color: #40a9ff;
}

/* 表格行高调整 */
:deep(.expand-content .el-table__row) {
  height: 45px;
}

:deep(.expand-content .el-table__body tr:hover > td) {
  background-color: #f0f9ff;
}
</style>
