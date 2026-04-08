<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 右侧内容区域 -->
      <el-col :span="20" :xs="24">
        <!-- 搜索表单 -->
        <el-form
          :model="queryParams"
          ref="queryRef"
          :inline="true"
          v-show="showSearch"
        >
          <el-form-item label="客户编码" prop="fnumber">
            <el-input
              v-model="queryParams.fnumber"
              placeholder="请输入客户编码"
              clearable
              style="width: 200px"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="客户名称" prop="fname">
            <el-input
              v-model="queryParams.fname"
              placeholder="请输入客户名称"
              clearable
              style="width: 200px"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="简称" prop="fshortName">
            <el-input
              v-model="queryParams.fshortName"
              placeholder="请输入简称"
              clearable
              style="width: 200px"
              @keyup.enter="handleQuery"
            />
          </el-form-item>

          <el-form-item label="销售员" prop="fseller">
            <el-select
              v-model="queryParams.fseller"
              clearable
              placeholder="请选择销售员"
            >
              <el-option
                v-for="item in salesperson"
                :key="item.value || item.fseller"
                :label="item.nickName || item.label"
                :value="item.fseller || item.value"
              />
            </el-select>
          </el-form-item>


          <el-form-item label="客户来源" prop="fkhly">
            <el-select
              v-model="queryParams.fkhly"
              clearable
              placeholder="请选择客户来源"
            >
              <el-option
                v-for="item in customerSources"
                :key="item.value || item.fkhly"
                :label="item.name || item.label"
                :value="item.fkhly || item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="客户等级" prop="fcustTypeId">
            <el-select
              v-model="queryParams.fcustTypeId"
              clearable
              placeholder="请选择客户等级"
            >
              <el-option
                v-for="item in setwaitClient"
                :key="item.value || item.fcustTypeId"
                :label="item.name || item.label"
                :value="item.fcustTypeId || item.value"
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
              v-hasPermi="['k3:customer:save']"
            >新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button
              type="primary"
              plain
              icon="CopyDocument"
              :disabled="single"
              @click="handleCopy"
              v-hasPermi="['k3:customer:save']"
            >复制</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button
              type="success"
              plain
              icon="Edit"
              :disabled="single"
              @click="handleUpdate"
              v-hasPermi="['k3:customer:update']"
            >修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button
              type="danger"
              plain
              icon="Delete"
              :disabled="multiple"
              @click="handleDelete"
              v-hasPermi="['k3:customer:delete']"
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
          <right-toolbar
            v-model:showSearch="showSearch"
            @queryTable="getList"
          ></right-toolbar>
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
                      <el-checkbox
                        v-model="element.visible"
                        @change="handleColumnChange"
                      >
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
            :data="customerList"
            @selection-change="handleSelectionChange"
            :key="tableKey"
            row-key="id"
            style="width: 100%"
            :height="tableHeight"
            border
            :resizable="true"
            @header-dragend="handleHeaderDragEnd"
            :expand-row-keys="expandedRowKeys"
            @expand-change="handleExpandChange"
            @wheel.prevent="handleTableWheel"
          >
            <el-table-column
              type="selection"
              width="55"
              align="center"
              fixed="left"
              :resizable="false"
            />

            <!-- 客户编码列 - 固定左侧，添加悬停事件 -->
            <el-table-column
              prop="fnumber"
              label="客户编码"
              width="120"
              align="left"
              fixed="left"
              :resizable="true"
            >
              <template #default="scope">
                <div
                  class="customer-code-cell"
                  @mouseenter="handleCodeMouseEnter(scope.row)"
                  @mouseleave="handleCodeMouseLeave(scope.row)"
                >
                  {{ scope.row.fnumber }}
                </div>
              </template>
            </el-table-column>

            <!-- 客户名称列 - 固定左侧 -->
            <el-table-column prop="fname" label="客户名称" width="150" align="left" fixed="left" :resizable="true"/>

            <!-- 单据状态列 - 优化后 -->
            <el-table-column width="100px" label="单据状态" align="center" show-overflow-tooltip>
              <template #default="{ row }">
                <!-- 从字典中查找对应的状态项 -->
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
              <template #default="scope">
                <span
                  v-if="
                    col.prop === 'fcreateDate' || col.prop === 'fapperoberDate'
                  "
                >
                  {{ formatDate(scope.row[col.prop]) }}
                </span>
                <span v-else>
                  {{ scope.row[col.prop] }}
                </span>
              </template>
            </el-table-column>

            <!-- 扩展行 -->
            <el-table-column type="expand" width="30" :resizable="false">
              <template #default="scope">
                <div
                  v-if="hasContactOrFinance(scope.row)"
                  class="row-expand-container"
                  @mouseenter="handleExpandMouseEnter(scope.row)"
                  @mouseleave="handleExpandMouseLeave(scope.row)"
                >
                  <div
                    class="expand-close-btn"
                    @click.stop="closeExpandRow(scope.row)"
                  >
                    <i class="el-icon-close"></i>
                  </div>
                  <el-tabs
                    v-model="expandedRowActiveTab[scope.row.id]"
                    class="expand-tabs"
                  >
                    <!-- 银行卡信息选项卡 -->
                    <el-tab-pane v-if="scope.row.informationList && Object.keys(scope.row.informationList).length > 0" label="银行卡信息" name="contact">
                      <div class="expand-section">
                        <div class="expand-content">
                          <el-table :data="[scope.row.informationList]" size="mini" border style="width: 100%">
                            <el-table-column prop="supplierNumber" label="编码" width="80" align="center"/>
                            <el-table-column prop="nation" label="国家" width="80" align="center"/>
                            <el-table-column prop="bankAccount" label="银行账号" width="90" align="center"/>
                            <el-table-column prop="accountName" label="账户名称" width="90" align="center"/>
                            <el-table-column prop="receivingBank" label="收款银行" width="90" align="center"/>
                            <el-table-column prop="bankAddress" label="开户行地址" width="100" align="center"/>
                            <el-table-column prop="openingBank" label="开户银行" width="90" align="center"/>
                            <el-table-column prop="createdAt" label="创建时间" width="90" align="center"/>
                            <el-table-column prop="updatedAt" label="修改时间" width="90" align="center"/>
                          </el-table>
                        </div>
                      </div>
                    </el-tab-pane>

                    <!-- 客户转让选项卡 -->
                    <el-tab-pane v-if="scope.row.customerTransfer && scope.row.customerTransfer.length > 0" label="客户转让" name="finance">
                      <div class="expand-section">
                        <div class="expand-content">
                          <el-table :data="scope.row.customerTransfer" size="mini" border style="width: 100%">
                            <el-table-column prop="customerCode" label="客户编码" width="90" align="center"/>
                            <el-table-column prop="fZrr" label="转让人" width="80" align="center"/>
                            <el-table-column prop="fJsr" label="接收人" width="80" align="center"/>
                            <el-table-column prop="fZrrq" label="转让日期" width="90" align="center"/>
                            <el-table-column prop="fTcbl" label="提成比例" width="90" align="center"/>
                            <el-table-column prop="createTime" label="创建时间" width="90" align="center"/>
                            <el-table-column prop="updateTime" label="更新时间" width="90" align="center"/>
                          </el-table>
                        </div>
                      </div>
                    </el-tab-pane>

                    <!-- 客户联系人选项卡 -->
                    <el-tab-pane v-if="scope.row.supplierContactList && scope.row.supplierContactList.length > 0" label="联系人信息" name="visit">
                      <div class="expand-section">
                        <div class="expand-content">
                          <el-table :data="scope.row.supplierContactList" size="mini" border style="width: 100%" :max-height="300">
                            <el-table-column prop="customerNumber" label="供应商编码" width="100" align="center"/>
                            <el-table-column prop="contactName" label="联系人" width="80px" show-overflow-tooltip align="center"/>
                            <el-table-column prop="defaultContact" label="默认联系人" width="100px" show-overflow-tooltip align="center"/>
                            <el-table-column prop="locationName" label="地点名称" width="90px" show-overflow-tooltip align="center"/>
                            <el-table-column prop="gender" label="性别" width="80px" show-overflow-tooltip align="center"/>
                            <el-table-column prop="position" label="职务" width="80px" show-overflow-tooltip align="center"/>
                            <el-table-column prop="phone" label="电话" width="80px" show-overflow-tooltip align="center"/>
                            <el-table-column prop="qq" label="QQ" width="80px" show-overflow-tooltip align="center"/>
                            <el-table-column prop="mobile" label="手机" width="80px" show-overflow-tooltip align="center"/>
                            <el-table-column prop="email" label="电子邮箱" width="90px" show-overflow-tooltip align="center"/>
                            <el-table-column prop="address" label="通讯地址" width="90px" show-overflow-tooltip align="center"/>
                            <el-table-column prop="remark" label="描述" width="80px" show-overflow-tooltip align="center"/>
                            <el-table-column prop="type" label="类型" width="80px" show-overflow-tooltip align="center"/>
                            <el-table-column prop="vx" label="微信" width="80px" show-overflow-tooltip align="center"/>
                            <el-table-column prop="customerNumber" label="客户编码" width="90px" show-overflow-tooltip align="center"/>
                            <el-table-column prop="creator" label="创建人" width="80px" show-overflow-tooltip align="center"/>
                            <el-table-column prop="creationDate" label="创建日期" width="90px" show-overflow-tooltip align="center"/>
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

        <!-- 分页组件 - 添加滚轮事件 -->
        <div class="pagination-wrapper" @wheel.prevent="handlePaginationWheel">
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

    <!-- 新增/修改客户对话框 -->
    <el-dialog
      :title="title"
      v-model="open"
      width="1800px"
      append-to-body
      @close="cancel"
    >
      <el-tabs v-model="activeTab" class="customer-tabs">
        <!-- 头部信息标签页 (保持不变) -->
        <el-tab-pane label="头部信息" name="business">
          <el-row :gutter="24">
            <el-col :span="12">
              <div class="section-title">
                <h4>头部信息</h4>
              </div>
              <el-form :model="form" :rules="rules" ref="customerRef" label-width="120px">
                <el-row :gutter="10">
                  <el-col :span="12">
                    <el-form-item label="客户编码" prop="fnumber">
                      <el-input v-model="form.fnumber" placeholder="请输入客户编码" maxlength="100" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="客户名称" prop="fname">
                      <el-input v-model="form.fname" placeholder="请输入客户名称" maxlength="100" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="10">
                  <el-col :span="12">
                    <el-form-item label="简称" prop="fshortName">
                      <el-input v-model="form.fshortName" placeholder="请输入简称" maxlength="100" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="客户来源" prop="fkhly">
                      <el-select v-model="form.fkhly" placeholder="请选择客户来源" clearable style="width: 100%">
                        <el-option
                          v-for="item in customerSources"
                          :key="item.value || item.fkhly"
                          :label="item.name"
                          :value="item.fkhly"
                        />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="10">
                  <el-col :span="12">
                    <el-form-item label="商标授权" prop="fsbsq">
                      <el-select v-model="form.fsbsq" placeholder="请选择商标授权" clearable style="width: 100%">
                        <el-option
                          v-for="dict in f_sbsq"
                          :key="dict.value"
                          :label="dict.label"
                          :value="dict.value"
                        />
                      </el-select>
                    </el-form-item>
                  </el-col>

                  <el-col :span="12">
                    <el-form-item label="客户情况">
                      <el-input v-model="form.remark" type="textarea" placeholder="请输入客户情况"></el-input>
                    </el-form-item>
                  </el-col>

                </el-row>
              </el-form>
            </el-col>

            <el-col :span="12">
              <div class="section-title">
                <h4>基本信息</h4>
              </div>
              <el-form :model="form" :rules="rules" label-width="120px">
                <el-row :gutter="10">
                  <el-col :span="12">
                    <el-form-item label="国家" prop="fcountry">
                      <el-select v-model="form.fcountry" placeholder="请选择国家" clearable style="width: 100%">
                        <el-option
                          v-for="item in countryOptions"
                          :key="item.value || item.fcountry"
                          :label="item.nameZh"
                          :value="item.fcountry"
                        />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="抵运国家" prop="fdygj">
                      <el-select v-model="form.fdygj" placeholder="请选择抵运国家" clearable style="width: 100%">
                        <el-option
                          v-for="item in countryOptions"
                          :key="item.value || item.fdygj"
                          :label="item.nameZh"
                          :value="item.fdygj"
                        />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="10">
                  <el-col :span="12">
                    <el-form-item label="通讯地址" prop="faddress">
                      <el-input v-model="form.faddress" placeholder="请输入通讯地址" maxlength="100" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="公司网址" prop="fwebsite">
                      <el-input v-model="form.fwebsite" placeholder="请输入公司网址" maxlength="100" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="10">
                  <el-col :span="12">
                    <el-form-item label="客户主营" prop="fKhzy">
                      <el-input v-model="form.fKhzy" placeholder="请输入客户主营" maxlength="100" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="法人代表" prop="flegalPerson">
                      <el-input v-model="form.flegalPerson" placeholder="请输入法人代表" maxlength="100" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="10">
                  <el-col :span="12">
                    <el-form-item label="客户规模" prop="fKhgm">
                      <el-input v-model="form.fKhgm" placeholder="请输入客户规模" maxlength="100" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="发货要求" prop="fFhyq">
                      <el-input v-model="form.fFhyq" placeholder="请输入发货要求" maxlength="100" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="10">
                  <el-col :span="12">
                    <el-form-item label="客户邮箱" prop="fKhyx">
                      <el-input v-model="form.fKhyx" placeholder="请输入客户邮箱" maxlength="100" />
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </el-col>
          </el-row>

          <el-row :gutter="24" style="margin-top: 20px">
            <el-col :span="12">
              <div class="section-title">
                <h4>管理</h4>
              </div>
              <el-form :model="form" :rules="rules" label-width="120px">
                <el-row :gutter="10">
                  <el-col :span="12">
                    <el-form-item label="联系电话" prop="ftel">
                      <el-input v-model="form.ftel" placeholder="请输入联系电话" maxlength="100" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="提成比例" prop="fCtyDecimal">
                      <el-input v-model="form.fCtyDecimal" placeholder="请输入提成比例" maxlength="100" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="10">
                  <el-col :span="12">
                    <el-form-item label="历年毛利率%" prop="fMjll">
                      <el-input v-model="form.fMjll" placeholder="请输入历年毛利率" maxlength="100" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="客户类别" prop="fcustTypeId">
                      <el-select v-model="form.fcustTypeId" placeholder="请选择客户类别">
                        <el-option
                          v-for="item in setwaitClient"
                          :key="item.value || item.fcustTypeId"
                          :label="item.name"
                          :value="item.fcustTypeId"
                        />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="10">
                  <el-col :span="12">
                    <el-form-item label="客户分组" prop="fgroupId">
                      <el-select v-model="form.fgroupId" placeholder="请选择客户分组" clearable style="width: 100%">
                        <el-option
                          v-for="item in setwaitgroupId"
                          :key="item.value || item.fgroupId"
                          :label="item.name"
                          :value="item.fgroupId"
                        />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </el-col>

            <el-col :span="12">
              <div class="section-title">
                <h4>SNS</h4>
              </div>
              <el-form :model="form" :rules="rules" label-width="120px">
                <el-row :gutter="10">
                  <el-col :span="12">
                    <el-form-item label="Youtube" prop="fYoutube">
                      <el-input v-model="form.fYoutube" placeholder="Please enter Youtube" maxlength="100" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="linkedin" prop="fLinkedin">
                      <el-input v-model="form.fLinkedin" placeholder="Please enter linkedin" maxlength="100" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="10">
                  <el-col :span="12">
                    <el-form-item label="facebook" prop="fFacebook">
                      <el-input v-model="form.fFacebook" placeholder="Please enter facebook" maxlength="100" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="twitter" prop="fTwitter">
                      <el-input v-model="form.fTwitter" placeholder="Please enter twitter" maxlength="100" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="10">
                  <el-col :span="12">
                    <el-form-item label="instagram" prop="fInstagram">
                      <el-input v-model="form.fInstagram" placeholder="Please enter instagram" maxlength="100" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="VK" prop="fVk">
                      <el-input v-model="form.fVk" placeholder="Please enter VK" maxlength="100" />
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- 包装要求标签页 - 优化后的图片上传 -->
        <el-tab-pane label="包装要求" name="packaging">
          <el-row :gutter="24">
            <!-- 左侧：包装要求字段 -->
            <el-col :span="12">
              <div class="section-title"><h4>包装要求</h4></div>
              <!-- 第一行：客户LOGO + 包装要求 -->
              <el-row :gutter="10">
                <el-col :span="12">
                  <el-form-item label="客户LOGO" prop="fkhlogo">
                    <div class="license-upload-container">
                      <!-- 图片预览 -->
                      <div v-if="form.fkhlogo || logoFile" class="license-preview">
                        <el-image
                          :src="getLogoPreviewUrl()"
                          :preview-src-list="getLogoPreviewSrcList()"
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
                        ref="logoUploadRef"
                        class="license-uploader"
                        action="#"
                        :limit="1"
                        :before-upload="beforeLogoUpload"
                        :on-change="handleLogoUpload"
                        :on-remove="handleLogoRemove"
                        :auto-upload="false"
                        accept=".jpg,.jpeg,.png,.gif,.bmp,.webp"
                        :file-list="logoFileList"
                        :show-file-list="false"
                      >
                        <el-button type="primary" icon="Upload">选择文件</el-button>
                        <template #tip>
                          <div class="el-upload__tip">
                            支持 JPG、PNG、GIF 格式，大小不超过 2MB
                            <div v-if="form.fkhlogo">
                              <el-button type="text" @click.stop="clearLogo">清除</el-button>
                            </div>
                          </div>
                        </template>
                      </el-upload>
                    </div>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="包装要求" prop="fBzyq">
                    <el-input v-model="form.fBzyq" type="textarea" :rows="6" placeholder="请输入包装要求"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>

              <!-- 第二行：包装方式 + 平均收款周期 + 是否首单 -->
              <el-row :gutter="10" style="margin-top: 20px;">
                <el-col :span="8">
                  <el-form-item label="包装方式" prop="fbzfs">
                    <el-select v-model="form.fbzfs" placeholder="请选择包装方式" style="width: 100%">
                      <el-option
                        v-for="item in setwaitpackaging"
                        :key="item.value || item.fbzfs"
                        :label="item.name"
                        :value="item.fbzfs"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="平均收款周期" prop="fpjskzq">
                    <el-input-number v-model="form.fpjskzq" :min="1" :max="10" size="small" controls-position="right" style="width: 100%;" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="是否首单" prop="fsfsd">
                    <el-select v-model="form.fsfsd" placeholder="请选择是否首单" clearable style="width: 100%">
                      <el-option
                        v-for="dict in f_sfsd"
                        :key="dict.value"
                        :label="dict.label"
                        :value="dict.value"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-col>

            <!-- 右侧：正唛头和侧唛头区域 -->
            <el-col :span="12">
              <!-- 正唛头区域 -->
              <div class="section-title"><h4>正唛外箱</h4></div>
              <el-row :gutter="10">
                <el-col :span="12">
                  <el-form-item label="正唛外箱" prop="fzmmttp1">
                    <div class="license-upload-container">
                      <div v-if="form.fzmmttp1 || zmmttpFile" class="license-preview">
                        <el-image
                          :src="getZmmttpPreviewUrl()"
                          :preview-src-list="getZmmttpPreviewSrcList()"
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

                      <el-upload
                        ref="zmmttpUploadRef"
                        class="license-uploader"
                        action="#"
                        :limit="1"
                        :before-upload="beforeZmmttpUpload"
                        :on-change="handleZmmttpUpload"
                        :on-remove="handleZmmttpRemove"
                        :auto-upload="false"
                        accept=".jpg,.jpeg,.png,.gif,.bmp,.webp"
                        :file-list="zmmttpFileList"
                        :show-file-list="false"
                      >
                        <el-button type="primary" icon="Upload">选择文件</el-button>
                        <template #tip>
                          <div class="el-upload__tip">
                            支持 JPG、PNG、GIF 格式，大小不超过 2MB
                            <div v-if="form.fzmmttp1">
                              <el-button type="text" @click.stop="clearZmmttp">清除</el-button>
                            </div>
                          </div>
                        </template>
                      </el-upload>
                    </div>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="正唛外箱描述" prop="zmmtwx">
                    <el-input v-model="form.zmmtwx" type="textarea" :rows="3" placeholder="请输入正唛外箱描述"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="10">
                <el-col :span="12">
                  <el-form-item label="正唛内箱" prop="fzmmttp2">
                    <div class="license-upload-container">
                      <div v-if="form.fzmmttp2 || zmmttpsFile" class="license-preview">
                        <el-image
                          :src="getZmmttpsPreviewUrl()"
                          :preview-src-list="getZmmttpsPreviewSrcList()"
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

                      <el-upload
                        ref="zmmttpsUploadRef"
                        class="license-uploader"
                        action="#"
                        :limit="1"
                        :before-upload="beforeZmmttpsUpload"
                        :on-change="handleZmmttpsUpload"
                        :on-remove="handleZmmttpsRemove"
                        :auto-upload="false"
                        accept=".jpg,.jpeg,.png,.gif,.bmp,.webp"
                        :file-list="zmmttpsFileList"
                        :show-file-list="false"
                      >
                        <el-button type="primary" icon="Upload">选择文件</el-button>
                        <template #tip>
                          <div class="el-upload__tip">
                            支持 JPG、PNG、GIF 格式，大小不超过 2MB
                            <div v-if="form.fzmmttp2">
                              <el-button type="text" @click.stop="clearZmmttps">清除</el-button>
                            </div>
                          </div>
                        </template>
                      </el-upload>
                    </div>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="正唛内箱描述" prop="zmmtnx">
                    <el-input v-model="form.zmmtnx" type="textarea" :rows="3" placeholder="请输入正唛内箱"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>

              <!-- 侧唛头区域 -->
              <div class="section-title" style="margin-top: 20px;"><h4>侧唛头</h4></div>
              <el-row :gutter="10">
                <el-col :span="12">
                  <el-form-item label="侧唛外箱" prop="fcmmttp1">
                    <div class="license-upload-container">
                      <div v-if="form.fcmmttp1 || cmmttpFile" class="license-preview">
                        <el-image
                          :src="getCmmttpPreviewUrl()"
                          :preview-src-list="getCmmttpPreviewSrcList()"
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

                      <el-upload
                        ref="cmmttpUploadRef"
                        class="license-uploader"
                        action="#"
                        :limit="1"
                        :before-upload="beforeCmmttpUpload"
                        :on-change="handleCmmttpUpload"
                        :on-remove="handleCmmttpRemove"
                        :auto-upload="false"
                        accept=".jpg,.jpeg,.png,.gif,.bmp,.webp"
                        :file-list="cmmttpFileList"
                        :show-file-list="false"
                      >
                        <el-button type="primary" icon="Upload">选择文件</el-button>
                        <template #tip>
                          <div class="el-upload__tip">
                            支持 JPG、PNG、GIF 格式，大小不超过 2MB
                            <div v-if="form.fcmmttp1">
                              <el-button type="text" @click.stop="clearCmmttp">清除</el-button>
                            </div>
                          </div>
                        </template>
                      </el-upload>
                    </div>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="侧唛外箱描述" prop="fcmmtwx">
                    <el-input v-model="form.fcmmtwx" type="textarea" :rows="3" placeholder="请输入侧唛外箱描述"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="10">
                <el-col :span="12">
                  <el-form-item label="侧唛内箱" prop="fcmmttp2">
                    <div class="license-upload-container">
                      <div v-if="form.fcmmttp2 || cmmttpsFile" class="license-preview">
                        <el-image
                          :src="getCmmttpsPreviewUrl()"
                          :preview-src-list="getCmmttpsPreviewSrcList()"
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

                      <el-upload
                        ref="cmmttpsUploadRef"
                        class="license-uploader"
                        action="#"
                        :limit="1"
                        :before-upload="beforeCmmttpsUpload"
                        :on-change="handleCmmttpsUpload"
                        :on-remove="handleCmmttpsRemove"
                        :auto-upload="false"
                        accept=".jpg,.jpeg,.png,.gif,.bmp,.webp"
                        :file-list="cmmttpsFileList"
                        :show-file-list="false"
                      >
                        <el-button type="primary" icon="Upload">选择文件</el-button>
                        <template #tip>
                          <div class="el-upload__tip">
                            支持 JPG、PNG、GIF 格式，大小不超过 2MB
                            <div v-if="form.fcmmttp2">
                              <el-button type="text" @click.stop="clearCmmttps">清除</el-button>
                            </div>
                          </div>
                        </template>
                      </el-upload>
                    </div>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="侧唛内箱描述" prop="fcmmtnx">
                    <el-input v-model="form.fcmmtnx" type="textarea" :rows="3" placeholder="请输入侧唛内箱描述描述"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- 商务信息标签页（保持不变） -->
        <el-tab-pane label="商务信息" name="businessInfo">
          <el-form :model="form" :rules="rules" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="6">
                <el-form-item label="结算币别" prop="ftradingCurrId">
                  <el-select v-model="form.ftradingCurrId" placeholder="请选择结算币别">
                    <el-option
                      v-for="item in SettlementCurrency"
                      :key="item.value || item.ftradingCurrId"
                      :label="item.name"
                      :value="item.ftradingCurrId"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="结算方式" prop="fsettleTypeId">
                  <el-select v-model="form.fsettleTypeId" placeholder="请选择结算方式">
                    <el-option
                      v-for="item in settlementMethodList"
                      :key="item.value || item.fsettleTypeId"
                      :label="item.name"
                      :value="item.fsettleTypeId"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="收款条件" prop="frecConditionId">
                  <el-select v-model="form.frecConditionId" placeholder="请选择收款条件">
                    <el-option
                      v-for="item in paymentterms"
                      :key="item.value || item.frecConditionId"
                      :label="item.name"
                      :value="item.frecConditionId"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="销售员" prop="fseller">
                  <el-select v-model="form.fseller" placeholder="请选择销售员">
                    <el-option
                      v-for="item in salesperson"
                      :key="item.value || item.fseller"
                      :label="item.nickName"
                      :value="item.fseller"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="开发销售员" prop="fKfxsy1">
                  <el-input v-model="form.fKfxsy1" placeholder="请输入开发销售员" maxlength="100" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-tab-pane>

        <!-- 联系人列表标签页（保持不变） -->
        <el-tab-pane label="联系人列表" name="contacts">
          <el-card class="section-card">
            <div class="section-header">
              <h4>联系人列表</h4>
              <el-button type="primary" plain @click="handleAddContact">新增联系人</el-button>
            </div>
            <el-table :data="form.supplierContactList || []" border stripe style="width: 100%">
              <el-table-column prop="contactName" label="联系人" width="140" align="center" />
              <el-table-column width="84px" label="性别">
                <template #default="{ row }">
                  <dict-tag :options="sys_user_sex" :value="row.gender" />
                </template>
              </el-table-column>
              <el-table-column width="84px" label="职务">
                <template #default="{ row }">
                  <dict-tag :options="supplier_position" :value="row.position" />
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

        <!-- 财务信息标签页（修改为单个对象） -->
        <el-tab-pane label="财务信息" name="finances">
          <el-card class="section-card">
            <div class="section-header">
              <h4>银行信息</h4>
              <el-button type="primary" plain @click="handleAddFinance">新增银行信息</el-button>
            </div>
            <el-table :data="informationListForTable" border stripe style="width: 100%">
              <el-table-column prop="nation" label="开户国家" width="90" align="center" />
              <el-table-column prop="bankAccount" label="银行账号" width="160" align="center" />
              <el-table-column prop="accountName" label="账户名称" width="160" align="center" />
              <el-table-column prop="receivingBank" label="收款银行" width="160" align="center" />
              <el-table-column prop="bankAddress" label="开户行地址" width="220" align="center" show-overflow-tooltip />
              <el-table-column prop="openingBank" label="开户银行" width="160" align="center" />
              <el-table-column prop="createdAt" label="创建时间" width="160" align="center" />
              <el-table-column prop="updatedAt" label="更新时间" width="160" align="center" />
              <el-table-column label="操作" width="160" align="center" fixed="right">
                <template #default="{ row, $index }">
                  <el-button type="text" size="small" @click="handleEditFinance(0)">编辑</el-button>
                  <el-button type="text" size="small" @click="handleDeleteFinance">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-tab-pane>

        <!-- 回访信息标签页（保持不变） -->
        <el-tab-pane label="客户转让" name="visits">
          <el-card class="section-card">
            <div class="section-header">
              <h4>客户转让</h4>
              <el-button type="primary" plain @click="handleAddVisit">新增客户转让</el-button>
            </div>
            <el-table :data="form.customerTransfer || []" border stripe style="width: 100%" :max-height="400">
              <el-table-column prop="fZrr" label="转让人" width="120" align="center"/>
              <el-table-column prop="fJsr" label="接收人" width="120" align="center"/>
              <el-table-column label="转让日期" width="150" align="center">
                <template #default="{ row }">
                  {{ parseTime(row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="fTcbl" label="提成比例" width="120" align="center"/>
              <el-table-column label="操作" width="160" align="center" fixed="right">
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

      <!-- 联系人编辑对话框（保持不变） -->
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
                  />
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
                  />
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

      <!-- 银行信息编辑对话框（保持不变） -->
      <el-dialog
        v-model="financeDialog.visible"
        :title="financeDialog.isEdit ? '编辑银行信息' : '新增银行信息'"
        width="50%"
        append-to-body
      >
        <el-form ref="financeFormRef" :model="financeDialog.form" :rules="financeRules" label-width="100px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="国家" prop="nation">
                <el-select v-model="financeDialog.form.nation" placeholder="请选择国家">
                  <el-option
                    v-for="item in countryOptions"
                    :key="item.value || item.nation"
                    :label="item.nameZh"
                    :value="item.nation"
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

      <!-- 客户转让编辑对话框（保持不变） -->
      <el-dialog
        v-model="visitDialog.visible"
        :title="visitDialog.isEdit ? '编辑客户转让' : '新增客户转让'"
        width="50%"
        append-to-body
      >
        <el-form ref="visitFormRef" :model="visitDialog.form" :rules="visitRules" label-width="100px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="转让人" prop="fZrr">
                <el-input v-model="visitDialog.form.fZrr" placeholder="请输入转让人" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="接收人" prop="fJsr">
                <el-input v-model="visitDialog.form.fJsr" placeholder="请输入接收人" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="提成比例" prop="fTcbl">
                <el-input v-model="visitDialog.form.fTcbl" placeholder="请输入提成比例" />
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
  </div>
</template>

<script setup name="Customer">
import {
  listCustomer,
  getCustomerSources,
  getNation,
  addCustomer,
  updateCustomer,
  deleteCustomer,
  getCustomer,
  gettlementCurrency as fetchSettlementCurrencyApi,
  settlementMethod as fetchSettlementMethodApi,
  paymentterms as fetchPaymentTermsApi,
  salesperson as fetchSalespersonApi,
  awaitgroupId,
  getImagePreviewUrl,
  awaitpackaging,
  awaitClient
} from "@/api/k3/customer";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  ref,
  reactive,
  computed,
  onMounted,
  nextTick,
  getCurrentInstance,
  onBeforeUnmount,
  watch,
} from "vue";
import draggable from "vuedraggable";
import { Delete, Edit, UploadFilled, Rank, Picture } from "@element-plus/icons-vue";

const { proxy } = getCurrentInstance();

// 字典
const {
  sys_normal_disable,
  fcust_type_id,
  f_sbsq,
  f_sfsd,
  sys_user_sex,
  supplier_position,
  f_document_status
} = proxy.useDict(
  "sys_normal_disable",
  "fcust_type_id",
  "f_sbsq",
  "f_sfsd",
  "sys_user_sex",
  "supplier_position",
  "f_document_status"
);

// ==================== 图片辅助函数 ====================
/**
 * 判断是否为图片文件
 */
const isImageFile = (url) => {
  if (!url) return false;
  const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp'];
  return imageExtensions.some(ext => url.toLowerCase().includes(ext));
};

/**
 * 获取图片完整URL
 */
const getImageUrl = (url) => {
  return getImagePreviewUrl(url);
};

/**
 * 从URL中提取文件名
 */
const getFileName = (url) => {
  if (!url) return '';
  const parts = url.split('/');
  return parts[parts.length - 1];
};

/**
 * 释放对象URL，防止内存泄漏
 * @param {String} url 对象URL
 */
const revokeObjectURL = (url) => {
  if (url && url.startsWith('blob:')) {
    URL.revokeObjectURL(url);
  }
};

// ==================== 响应式数据 ====================
const customerList = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const columnSettingVisible = ref(false);
const tableKey = ref(1);
const checkAll = ref(false);
const isIndeterminate = ref(false);
const activeTab = ref("business");
const tableHeight = ref(null);
const open = ref(false);
const title = ref("");
const isEditMode = ref(false);

// 模板引用
const queryRef = ref(null);
const customerRef = ref(null);
const contactFormRef = ref(null);
const financeFormRef = ref(null);
const visitFormRef = ref(null);

// 下拉框数据
const customerSources = ref([]);
const countryOptions = ref([]);
const SettlementCurrency = ref([]);
const settlementMethodList = ref([]);
const paymentterms = ref([]);
const salesperson = ref([]);
const setwaitgroupId = ref([]);
const setwaitpackaging = ref([]);
const setwaitClient = ref([]);

// 滚轮翻页相关状态
const isScrolling = ref(false);
const scrollTimer = ref(null);

// 扩展行相关状态
const expandedRowKeys = ref([]);
const hoverCodeRowId = ref(null);
const expandCloseTimer = ref(null);
const expandedRowActiveTab = ref({});

// ==================== 图片上传相关状态 ====================
// 客户LOGO
const logoFileList = ref([]);
const logoFile = ref(null);
const logoUploadRef = ref(null);
const logoPreviewUrl = ref(''); // 用于存储当前预览的对象URL，以便释放

// 正唛外箱
const zmmttpFileList = ref([]);
const zmmttpFile = ref(null);
const zmmttpUploadRef = ref(null);
const zmmttpPreviewUrl = ref('');

//正唛内箱
const zmmttpsFileList = ref([]);
const zmmttpsFile = ref(null);
const zmmttpsUploadRef = ref(null);
const zmmttpsPreviewUrl = ref('');


// 侧唛头
const cmmttpFileList = ref([]);
const cmmttpFile = ref(null);
const cmmttpUploadRef = ref(null);
const cmmttpPreviewUrl = ref('');

//侧唛内箱
const cmmttpsFileList = ref([]);
const cmmttpsFile = ref(null);
const cmmttpsUploadRef = ref(null);
const cmmttpsPreviewUrl = ref('');


// ==================== 表单数据 ====================
const form = ref({
  id: undefined,
  fnumber: "",
  fname: "",
  fshortName: "",
  fseller: "",
  fCtyDecimal: "",
  fdocumentStatus: "",
  fdygj: "",
  fcountry: "",
  fsalDeptId: "",
  fcreateDate: "",
  fapproverId: "",
  fapperoberDate: "",
  fgroupId: "",
  fKhly: "",
  fcustTypeId: "",
  fsbsq: "",
  faddress: "",
  fwebsite: "",
  fKhzy: "",
  flegalPerson: "",
  fKhgm: "",
  fFhyq: "",
  fKhyx: "",
  ftel: "",
  fMjll: "",
  fYoutube: "",
  fLinkedin: "",
  fFacebook: "",
  fTwitter: "",
  fInstagram: "",
  fVk: "",
  fkhlogo: "",
  fBzyq: "",
  fbzfs: "",
  fpjskzq: "",
  fsfsd: "",
  fzmmttp1: "",
  zmmtwx: "",
  fzmmttp2: "",
  zmmtnx: "",
  fcmmttp1: "",
  fcmmtwx: "",
  fcmmttp2: "",
  fcmmtnx: "",
  ftradingCurrId: "",
  fsettleTypeId: "",
  frecConditionId: "",
  fKfxsy1: "",
   remark: "",
  supplierContactList: [],
  informationList: {},
  customerTransfer: [],
});

// 查询参数
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  fnumber: undefined,
  fname: undefined,
  fshortName: undefined,
  fseller: undefined,
  kingdee: undefined,
  fcustTypeId: undefined,
  fkhly: undefined
});

// 表单验证规则
const rules = ref({
  fnumber: [{ required: true, message: "客户编码不能为空", trigger: "blur" }],
  fname: [{ required: true, message: "客户名称不能为空", trigger: "blur" }],
  fshortName: [{ required: true, message: "简称不能为空", trigger: "blur" }],
});

// ==================== 列配置 ====================
const allColumns = ref([
  { prop: "fseller", label: "销售员", visible: true, width: "100px", align: "left", showOverflowTooltip: true },
  { prop: "fshortName", label: "简称", visible: true, width: "100px", align: "left", showOverflowTooltip: true },
  { prop: "fCtyDecimal", label: "提成比例%", visible: true, width: "100px", align: "center", showOverflowTooltip: true },
  { prop: "fdygj", label: "抵运国家", visible: true, width: "120px", align: "left", showOverflowTooltip: true },
  { prop: "fcountry", label: "国家", visible: true, width: "100px", align: "left", showOverflowTooltip: true },
  { prop: "fsalDeptName", label: "销售部门", visible: true, width: "120px", align: "left", showOverflowTooltip: true },
  { prop: "fapproverId", label: "审核人", visible: true, width: "100px", align: "left", showOverflowTooltip: true },
  { prop: "fapperoberDate", label: "审核时间", visible: true, width: "160px", align: "center", showOverflowTooltip: true },
  { prop: "fgroupId", label: "客户分组", visible: true, width: "120px", align: "left", showOverflowTooltip: true },
  { prop: "fkhly", label: "客户来源", visible: true, width: "120px", align: "left", showOverflowTooltip: true },
  { prop: "fcustTypeId", label: "客户等级", visible: true, width: "100px", align: "center", showOverflowTooltip: true },
  { prop: "remark", label: "客户情况", visible: true, width: "100px", align: "center", showOverflowTooltip: true },
  { prop: "fcreateDate", label: "创建时间", visible: true, width: "160px", align: "center", showOverflowTooltip: true },
  { prop: "fmodifyDate", label: "更新时间", visible: true, width: "160px", align: "center", showOverflowTooltip: true }
]);

const visibleColumns = computed(() => allColumns.value.filter((col) => col.visible));

const dragOptions = computed(() => ({
  animation: 200,
  group: "columns",
  disabled: false,
  ghostClass: "ghost",
}));

// ==================== 图片上传处理方法 ====================
/**
 * 通用上传前校验
 */
const beforeImageUpload = (file) => {
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

/**
 * 客户LOGO上传处理
 */
const beforeLogoUpload = beforeImageUpload;
const handleLogoUpload = (file, fileList) => {
  if (!beforeLogoUpload(file.raw)) {
    logoUploadRef.value?.clearFiles();
    return;
  }
  // 释放之前的预览URL
  if (logoPreviewUrl.value) {
    revokeObjectURL(logoPreviewUrl.value);
  }
  logoFile.value = file.raw;
  form.value.fkhlogo = file.name;
  logoFileList.value = [file];
  logoPreviewUrl.value = URL.createObjectURL(file.raw);
};
const handleLogoRemove = () => {
  if (logoPreviewUrl.value) {
    revokeObjectURL(logoPreviewUrl.value);
    logoPreviewUrl.value = '';
  }
  logoFile.value = null;
  form.value.fkhlogo = '';
  logoFileList.value = [];
};
const clearLogo = () => {
  handleLogoRemove();
  logoUploadRef.value?.clearFiles();
};
const getLogoPreviewUrl = () => {
  if (logoFile.value) {
    // 如果已有缓存的预览URL，直接返回，否则创建并缓存
    if (!logoPreviewUrl.value) {
      logoPreviewUrl.value = URL.createObjectURL(logoFile.value);
    }
    return logoPreviewUrl.value;
  } else if (form.value.fkhlogo) {
    return getImageUrl(form.value.fkhlogo);
  }
  return '';
};
const getLogoPreviewSrcList = () => {
  const url = getLogoPreviewUrl();
  return url ? [url] : [];
};

/**
 * 正唛头上传处理
 */
const beforeZmmttpUpload = beforeImageUpload;
const handleZmmttpUpload = (file, fileList) => {
  if (!beforeZmmttpUpload(file.raw)) {
    zmmttpUploadRef.value?.clearFiles();
    return;
  }
  if (zmmttpPreviewUrl.value) {
    revokeObjectURL(zmmttpPreviewUrl.value);
  }
  zmmttpFile.value = file.raw;
  form.value.fzmmttp1 = file.name;
  zmmttpFileList.value = [file];
  zmmttpPreviewUrl.value = URL.createObjectURL(file.raw);
};
const handleZmmttpRemove = () => {
  if (zmmttpPreviewUrl.value) {
    revokeObjectURL(zmmttpPreviewUrl.value);
    zmmttpPreviewUrl.value = '';
  }
  zmmttpFile.value = null;
  form.value.fzmmttp1 = '';
  zmmttpFileList.value = [];
};
const clearZmmttp = () => {
  handleZmmttpRemove();
  zmmttpUploadRef.value?.clearFiles();
};
const getZmmttpPreviewUrl = () => {
  if (zmmttpFile.value) {
    if (!zmmttpPreviewUrl.value) {
      zmmttpPreviewUrl.value = URL.createObjectURL(zmmttpFile.value);
    }
    return zmmttpPreviewUrl.value;
  } else if (form.value.fzmmttp1) {
    return getImageUrl(form.value.fzmmttp1);
  }
  return '';
};
const getZmmttpPreviewSrcList = () => {
  const url = getZmmttpPreviewUrl();
  return url ? [url] : [];
};


/**
 * 正唛内箱上传处理
 * */
const beforeZmmttpsUpload = beforeImageUpload;
const handleZmmttpsUpload = (file, fileList) => {
  if (!beforeZmmttpsUpload(file.raw)) {
    zmmttpsUploadRef.value?.clearFiles();
    return;
  }
  if (zmmttpsPreviewUrl.value) {
    revokeObjectURL(zmmttpsPreviewUrl.value);
  }
  zmmttpsFile.value = file.raw;
  form.value.fzmmttp2 = file.name;
  zmmttpsFileList.value = [file];
  zmmttpsPreviewUrl.value = URL.createObjectURL(file.raw);
};
const handleZmmttpsRemove = () => {
  if (zmmttpsPreviewUrl.value) {
    revokeObjectURL(zmmttpsPreviewUrl.value);
    zmmttpsPreviewUrl.value = '';
  }
  zmmttpsFile.value = null;
  form.value.fzmmttp2 = '';
  zmmttpsFileList.value = [];
};
const clearZmmttps = () => {
  handleZmmttpsRemove();
  zmmttpsUploadRef.value?.clearFiles();
};
const getZmmttpsPreviewUrl = () => {
  if (zmmttpsFile.value) {
    if (!zmmttpsPreviewUrl.value) {
      zmmttpsPreviewUrl.value = URL.createObjectURL(zmmttpsFile.value);
    }
    return zmmttpsPreviewUrl.value;
  } else if (form.value.fzmmttp2) {
    return getImageUrl(form.value.fzmmttp2);
  }
  return '';
};
const getZmmttpsPreviewSrcList = () => {
  const url = getZmmttpsPreviewUrl();
  return url ? [url] : [];
};

/**
 * 侧唛外箱传处理
 */
const beforeCmmttpUpload = beforeImageUpload;
const handleCmmttpUpload = (file, fileList) => {
  if (!beforeCmmttpUpload(file.raw)) {
    cmmttpUploadRef.value?.clearFiles();
    return;
  }
  if (cmmttpPreviewUrl.value) {
    revokeObjectURL(cmmttpPreviewUrl.value);
  }
  cmmttpFile.value = file.raw;
  form.value.fcmmttp1 = file.name;
  cmmttpFileList.value = [file];
  cmmttpPreviewUrl.value = URL.createObjectURL(file.raw);
};
const handleCmmttpRemove = () => {
  if (cmmttpPreviewUrl.value) {
    revokeObjectURL(cmmttpPreviewUrl.value);
    cmmttpPreviewUrl.value = '';
  }
  cmmttpFile.value = null;
  form.value.fcmmttp1 = '';
  cmmttpFileList.value = [];
};
const clearCmmttp = () => {
  handleCmmttpRemove();
  cmmttpUploadRef.value?.clearFiles();
};
const getCmmttpPreviewUrl = () => {
  if (cmmttpFile.value) {
    if (!cmmttpPreviewUrl.value) {
      cmmttpPreviewUrl.value = URL.createObjectURL(cmmttpFile.value);
    }
    return cmmttpPreviewUrl.value;
  } else if (form.value.fcmmttp1) {
    return getImageUrl(form.value.fcmmttp1);
  }
  return '';
};
const getCmmttpPreviewSrcList = () => {
  const url = getCmmttpPreviewUrl();
  return url ? [url] : [];
};


/**
 * 侧唛内箱传处理
 * */
const beforeCmmttpsUpload = beforeImageUpload;
const handleCmmttpsUpload = (file, fileList) => {
  if (!beforeCmmttpsUpload(file.raw)) {
    cmmttpsUploadRef.value?.clearFiles();
    return;
  }
  if (cmmttpsPreviewUrl.value) {
    revokeObjectURL(cmmttpsPreviewUrl.value);
  }
  cmmttpsFile.value = file.raw;
  form.value.fcmmttp2 = file.name;
  cmmttpsFileList.value = [file];
  cmmttpsPreviewUrl.value = URL.createObjectURL(file.raw);
};
const handleCmmttpsRemove = () => {
  if (cmmttpsPreviewUrl.value) {
    revokeObjectURL(cmmttpsPreviewUrl.value);
    cmmttpsPreviewUrl.value = '';
  }
  cmmttpsFile.value = null;
  form.value.fcmmttp2 = '';
  cmmttpsFileList.value = [];
};
const clearCmmttps = () => {
  handleCmmttpsRemove();
  cmmttpsUploadRef.value?.clearFiles();
};
const getCmmttpsPreviewUrl = () => {
  if (cmmttpsFile.value) {
    if (!cmmttpsPreviewUrl.value) {
      cmmttpsPreviewUrl.value = URL.createObjectURL(cmmttpsFile.value);
    }
    return cmmttpsPreviewUrl.value;
  } else if (form.value.fcmmttp2) {
    return getImageUrl(form.value.fcmmttp2);
  }
  return '';
};
const getCmmttpsPreviewSrcList = () => {
  const url = getCmmttpsPreviewUrl();
  return url ? [url] : [];
};

// ==================== 数据获取方法 ====================
const getList = async () => {
  loading.value = true;
  try {
    const response = await listCustomer(queryParams.value);
    if (response && response.rows) {
      customerList.value = response.rows.map((item) => {
        const row = {
          ...item,
          id: item.id || item.fnumber,
          supplierContactList: item.supplierContactList || [],
          informationList: Array.isArray(item.informationList)
            ? (item.informationList[0] || {})
            : (item.informationList || {}),
          customerTransfer: item.customerTransfer || []
        };
        if (!expandedRowActiveTab.value[row.id]) {
          if (row.informationList && Object.keys(row.informationList).length) {
            expandedRowActiveTab.value[row.id] = 'contact';
          } else if (row.customerTransfer.length) {
            expandedRowActiveTab.value[row.id] = 'finance';
          } else if (row.supplierContactList.length) {
            expandedRowActiveTab.value[row.id] = 'visit';
          }
        }
        return row;
      });
      total.value = response.total || 0;
    } else {
      customerList.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error("获取客户列表失败:", error);
    ElMessage.error("获取客户列表失败");
    customerList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
    nextTick(() => {
      tableKey.value += 1;
      calculateTableHeight();
    });
  }
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
  return item ? item.listClass : ''; // listClass 通常为 'success'/'info'/'warning'/'danger'
}


const getCustomerDropdown = async () => {
  try {
    const response = await getCustomerSources();
    customerSources.value = response.data || [];
  } catch (error) {
    console.error("获取客户来源失败:", error);
    ElMessage.error("获取客户来源失败");
  }
};

const getCustomerCountry = async () => {
  try {
    const response = await getNation();
    countryOptions.value = response.data || [];
  } catch (error) {
    console.error("获取国家列表失败:", error);
    ElMessage.error("获取国家列表失败");
  }
};

const fetchSettlementCurrency = async () => {
  try {
    const response = await fetchSettlementCurrencyApi();
    SettlementCurrency.value = response.data || [];
  } catch (error) {
    console.error("获取结算币别失败:", error);
    ElMessage.error("获取结算币别失败");
  }
};

const fetchSettlementMethod = async () => {
  try {
    const response = await fetchSettlementMethodApi();
    settlementMethodList.value = response.data || [];
  } catch (error) {
    console.error("获取结算方式失败:", error);
    ElMessage.error("获取结算方式失败");
  }
};

const fetchPaymentTerms = async () => {
  try {
    const response = await fetchPaymentTermsApi();
    paymentterms.value = response.data || [];
  } catch (error) {
    console.error("获取收款条件失败:", error);
    ElMessage.error("获取收款条件失败");
  }
};

const fetchSalesperson = async () => {
  try {
    const response = await fetchSalespersonApi();
    salesperson.value = response.data || [];
  } catch (error) {
    console.error("获取销售员失败:", error);
    ElMessage.error("获取销售员失败");
  }
};

const getawaitgroupId = async () =>{
  try {
    const response = await awaitgroupId();
    setwaitgroupId.value = response.data || [];
  } catch (error) {
    console.error("获取客户分组失败:", error);
    ElMessage.error("获取客户分组失败");
  }
};

const getawaitpackaging = async () =>{
  try {
    const response = await awaitpackaging();
    setwaitpackaging.value = response.data || [];
  } catch (error) {
    console.error("获取包装方式失败:", error);
    ElMessage.error("获取包装方式失败");
  }
};

const getawaitClient = async () =>{
  try {
    const response = await awaitClient();
    setwaitClient.value = response.data || [];
  } catch (error) {
    console.error("获取客户类别失败:", error);
    ElMessage.error("获取客户类别失败");
  }
};

const formatDate = (date) => {
  if (!date) return "-";
  try {
    return proxy.$dayjs(date).format("YYYY-MM-DD HH:mm:ss");
  } catch (error) {
    return date;
  }
};

const handlePageSizeChange = (newSize) => {
  queryParams.value.pageSize = newSize;
  queryParams.value.pageNum = 1;
  getList();
};

const calculateTableHeight = () => {
  nextTick(() => {
    const container = document.querySelector(".app-container");
    if (!container) return;

    const searchArea = document.querySelector(".el-form");
    const operationArea = document.querySelector(".mb8");
    const paginationArea = document.querySelector(".pagination-wrapper");

    const containerHeight = container.clientHeight;
    const searchHeight = searchArea ? searchArea.clientHeight : 0;
    const operationHeight = operationArea ? operationArea.clientHeight : 0;
    const paginationHeight = paginationArea ? paginationArea.clientHeight : 56;
    const marginHeight = 40;

    let availableHeight = containerHeight - searchHeight - operationHeight - paginationHeight - marginHeight;
    availableHeight = Math.max(availableHeight, 200);
    availableHeight = Math.min(availableHeight, 800);
    tableHeight.value = availableHeight;
  });
};

// 列设置相关
const loadColumnSettings = () => {
  const savedColumns = localStorage.getItem("customerColumns");
  if (savedColumns) {
    try {
      const parsedColumns = JSON.parse(savedColumns);
      const mergedColumns = allColumns.value.map((defaultCol) => {
        const savedCol = parsedColumns.find((c) => c.prop === defaultCol.prop);
        if (savedCol) {
          return {
            ...defaultCol,
            visible: savedCol.visible !== undefined ? savedCol.visible : defaultCol.visible,
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
    localStorage.setItem("customerColumns", JSON.stringify(allColumns.value));
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

const handleHeaderDragEnd = (column, newWidth, oldWidth) => {
  const columnProp = column.property;
  if (columnProp) {
    const columnIndex = allColumns.value.findIndex((col) => col.prop === columnProp);
    if (columnIndex !== -1) {
      allColumns.value[columnIndex].width = newWidth + "px";
      saveColumnSettings();
    }
  }
};

// 表格操作
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

const tableRowClassName = () => "";

// 公共数据填充方法（用于复制和修改）
const setFormData = (data) => {
  Object.keys(form.value).forEach(key => {
    if (data[key] !== undefined && data[key] !== null) {
      form.value[key] = data[key];
    }
  });

  // 回显子表数据
  form.value.supplierContactList = data.supplierContactList || [];
  // informationList 如果是数组则取第一个元素
  form.value.informationList = Array.isArray(data.informationList)
    ? (data.informationList[0] || {})
    : (data.informationList || {});
  form.value.customerTransfer = data.customerTransfer || [];

  // 回显图片
  const setFileList = (url, fileListRef, fileRef) => {
    if (url) {
      fileListRef.value = [{ name: getFileName(url), url: getImageUrl(url) }];
      fileRef.value = null; // 表示是已有文件，不是新上传的
    }
  };
  setFileList(data.fkhlogo, logoFileList, logoFile);
  setFileList(data.fzmmttp1, zmmttpFileList, zmmttpFile);
  setFileList(data.fzmmttp2, zmmttpsFileList, zmmttpsFile);
  setFileList(data.fcmmttp1, cmmttpFileList, cmmttpFile);
  setFileList(data.fcmmttp2, cmmttpsFileList, cmmttpsFile);
};

// 新增/修改/复制/删除
const reset = () => {
  // 释放所有预览URL
  [logoPreviewUrl, zmmttpPreviewUrl, cmmttpPreviewUrl].forEach(urlRef => {
    if (urlRef.value) {
      revokeObjectURL(urlRef.value);
      urlRef.value = '';
    }
  });

  form.value = {
    id: undefined,
    fnumber: "",
    fname: "",
    fshortName: "",
    fseller: "",
    fCtyDecimal: "",
    fdocumentStatus: "",
    fdygj: "",
    fcountry: "",
    fsalDeptId: "",
    fcreateDate: "",
    fapproverId: "",
    fapperoberDate: "",
    fgroupId: "",
    fkhly: "",
    fcustTypeId: "",
    fsbsq: "",
    faddress: "",
    fwebsite: "",
    fKhzy: "",
    flegalPerson: "",
    fKhgm: "",
    fFhyq: "",
    fKhyx: "",
    ftel: "",
    fMjll: "",
    fYoutube: "",
    fLinkedin: "",
    fFacebook: "",
    fTwitter: "",
    fInstagram: "",
    fVk: "",
    fkhlogo: "",
    fBzyq: "",
    fbzfs: "",
    fpjskzq: "",
    fsfsd: "",
    fzmmttp1: "",
    zmmtwx: "",
    fzmmttp2: "",
    zmmtnx: "",
    fcmmttp1: "",
    fcmmtwx: "",
    fcmmttp2: "",
    fcmmtnx: "",
    ftradingCurrId: "",
    fsettleTypeId: "",
    frecConditionId: "",
    fKfxsy1: "",
    supplierContactList: [],
    informationList: {},
    customerTransfer: []
  };
  // 重置图片状态
  logoFile.value = null;
  logoFileList.value = [];
  logoUploadRef.value?.clearFiles();

  zmmttpFile.value = null;
  zmmttpFileList.value = [];
  zmmttpUploadRef.value?.clearFiles();

  zmmttpsFile.value = null;
  zmmttpsFileList.value = [];
  zmmttpsUploadRef.value?.clearFiles();

  cmmttpFile.value = null;
  cmmttpFileList.value = [];
  cmmttpUploadRef.value?.clearFiles();

  cmmttpsFile.value = null;
  cmmttpsFileList.value = [];
  cmmttpsUploadRef.value?.clearFiles();

  activeTab.value = "business";
};

const cancel = () => {
  open.value = false;
  reset();
};

const handleAdd = () => {
  reset();
  open.value = true;
  title.value = "新增客户";
  isEditMode.value = false;
};

const handleCopy = async (row) => {
  reset();
  const id = row?.id || (ids.value.length === 1 ? ids.value[0] : null);
  if (!id) {
    ElMessage.warning("请选择一条数据");
    return;
  }
  loading.value = true;
  try {
    const response = await getCustomer(id);
    const data = response.data || response;
    setFormData(data);
    form.value.id = undefined;
    form.value.fnumber = "";
    if (form.value.fname) {
      form.value.fname = `${form.value.fname} - 副本`;
    }
    open.value = true;
    title.value = "复制新增客户";
    isEditMode.value = false;
    ElMessage.success("数据已复制到表单，请修改后保存");
  } catch (error) {
    console.error("获取客户详情失败:", error);
    ElMessage.error("获取客户详情失败");
  } finally {
    loading.value = false;
  }
};

const handleUpdate = async (row) => {
  reset();
  const id = row?.id || (ids.value.length === 1 ? ids.value[0] : null);
  if (!id) {
    ElMessage.warning("请选择一条数据");
    return;
  }
  try {
    const response = await getCustomer(id);
    const data = response.data || response;
    setFormData(data);
    open.value = true;
    title.value = "修改客户";
    isEditMode.value = true;
  } catch (error) {
    console.error("获取客户详情失败:", error);
    ElMessage.error("获取客户详情失败");
  }
};

const submitForm = async () => {
  try {
    await customerRef.value.validate();
    const submitData = { ...form.value };

    // 如果有新上传的文件，添加到提交数据中（字段名与后端控制器参数名一致）
    // 注意：API层会将 fKhlogoFile 映射为 logoFile，此处直接使用原字段名
    if (logoFile.value) submitData.fKhlogoFile = logoFile.value;
    if (zmmttpFile.value) submitData.fZmmttpFile = zmmttpFile.value;
    if (zmmttpsFile.value) submitData.fZmmttpsFile = zmmttpsFile.value;
    if (cmmttpFile.value) submitData.fcmmttpFile = cmmttpFile.value;
    if (cmmttpsFile.value) submitData.fcmmttpsFile = cmmttpsFile.value;
    if (isEditMode.value && form.value.id) {
      await updateCustomer(submitData);
      ElMessage.success("修改成功");
    } else {
      await addCustomer(submitData);
      ElMessage.success("新增成功");
    }
    open.value = false;
    await getList();
  } catch (error) {
    if (error !== "validate") {
      console.error("保存失败:", error);
      ElMessage.error("保存失败: " + (error.message || "请检查表单填写是否正确"));
    }
  }
};

const handleDelete = async (row) => {
  const id = row?.id || (ids.value.length > 0 ? ids.value : []);
  if (!id || (Array.isArray(id) && id.length === 0)) {
    ElMessage.warning("请选择要删除的数据");
    return;
  }
  const idsToDelete = Array.isArray(id) ? id : [id];
  try {
    await ElMessageBox.confirm("是否确认删除选中的客户？", "警告", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });
    await deleteCustomer(idsToDelete);
    ElMessage.success("删除成功");
    await getList();
  } catch (error) {
    if (error !== "cancel") {
      console.error("删除失败:", error);
      ElMessage.error("删除失败");
    }
  }
};

// ==================== 联系人/财务/转让对话框 ====================
const contactDialog = reactive({
  visible: false,
  isEdit: false,
  editIndex: -1,
  form: {
    contactName: "",
    gender: "",
    position: "",
    phone: "",
    qq: "",
    mobile: "",
    email: "",
  },
});

const contactRules = ref({
  email: [{ type: "email", message: "请输入正确的邮箱地址", trigger: ["blur", "change"] }],
});

const handleAddContact = () => {
  contactDialog.isEdit = false;
  contactDialog.editIndex = -1;
  contactDialog.form = {
    contactName: "",
    gender: "",
    position: "",
    phone: "",
    qq: "",
    mobile: "",
    email: "",
  };
  contactDialog.visible = true;
};

const handleEditContact = (index) => {
  const contact = form.value.supplierContactList[index];
  contactDialog.isEdit = true;
  contactDialog.editIndex = index;
  contactDialog.form = { ...contact };
  contactDialog.visible = true;
};

const handleDeleteContact = (index) => {
  form.value.supplierContactList.splice(index, 1);
};

const handleSaveContact = () => {
  contactFormRef.value.validate((valid) => {
    if (valid) {
      if (contactDialog.isEdit) {
        form.value.supplierContactList[contactDialog.editIndex] = { ...contactDialog.form };
      } else {
        if (!form.value.supplierContactList) form.value.supplierContactList = [];
        form.value.supplierContactList.push({ ...contactDialog.form });
      }
      contactDialog.visible = false;
    }
  });
};

// 银行信息相关（修改为单个对象）
const financeDialog = reactive({
  visible: false,
  isEdit: false,
  form: {
    nation: "",
    bankAccount: "",
    accountName: "",
    receivingBank: "",
    bankAddress: "",
    openingBank: "",
  },
});

const financeRules = ref({});

const informationListForTable = computed(() => {
  return form.value.informationList && Object.keys(form.value.informationList).length > 0
    ? [form.value.informationList]
    : [];
});

const handleAddFinance = () => {
  financeDialog.isEdit = false;
  financeDialog.form = {
    nation: "",
    bankAccount: "",
    accountName: "",
    receivingBank: "",
    bankAddress: "",
    openingBank: "",
  };
  financeDialog.visible = true;
};

const handleEditFinance = (index) => {
  financeDialog.isEdit = true;
  financeDialog.form = { ...form.value.informationList };
  financeDialog.visible = true;
};

const handleDeleteFinance = () => {
  form.value.informationList = {};
  ElMessage.success("已删除银行信息");
};

const handleSaveFinance = () => {
  financeFormRef.value.validate((valid) => {
    if (valid) {
      form.value.informationList = { ...financeDialog.form };
      financeDialog.visible = false;
    }
  });
};

// 客户转让相关（保持不变）
const visitDialog = reactive({
  visible: false,
  isEdit: false,
  editIndex: -1,
  form: {
    fZrr: "",
    fJsr: "",
    fTcbl: ""
  },
});

const visitRules = ref({});

const handleAddVisit = () => {
  visitDialog.isEdit = false;
  visitDialog.editIndex = -1;
  visitDialog.form = {
    fZrr: "",
    fJsr: "",
    fTcbl: ""
  };
  visitDialog.visible = true;
};

const handleEditVisit = (index) => {
  const visit = form.value.customerTransfer[index];
  visitDialog.isEdit = true;
  visitDialog.editIndex = index;
  visitDialog.form = { ...visit };
  visitDialog.visible = true;
};

const handleDeleteVisit = (index) => {
  form.value.customerTransfer.splice(index, 1);
};

const handleSaveVisit = () => {
  visitFormRef.value.validate((valid) => {
    if (valid) {
      if (visitDialog.isEdit) {
        form.value.customerTransfer[visitDialog.editIndex] = { ...visitDialog.form };
      } else {
        if (!form.value.customerTransfer) form.value.customerTransfer = [];
        form.value.customerTransfer.push({ ...visitDialog.form });
      }
      visitDialog.visible = false;
    }
  });
};

// ==================== 扩展行相关方法 ====================
const hasContactOrFinance = (row) => {
  return (
    (row.informationList && Object.keys(row.informationList).length > 0) ||
    (row.customerTransfer && row.customerTransfer.length > 0) ||
    (row.supplierContactList && row.supplierContactList.length > 0)
  );
};

const handleCodeMouseEnter = (row) => {
  if (!hasContactOrFinance(row)) return;

  clearTimeout(expandCloseTimer.value);
  hoverCodeRowId.value = row.id;

  if (!expandedRowKeys.value.includes(row.id)) {
    expandedRowKeys.value = [row.id];
  }

  if (!expandedRowActiveTab.value[row.id]) {
    if (row.informationList && Object.keys(row.informationList).length) {
      expandedRowActiveTab.value[row.id] = 'contact';
    } else if (row.customerTransfer?.length) {
      expandedRowActiveTab.value[row.id] = 'finance';
    } else if (row.supplierContactList?.length) {
      expandedRowActiveTab.value[row.id] = 'visit';
    }
  }
};

const handleCodeMouseLeave = (row) => {
  if (hoverCodeRowId.value === row.id) {
    hoverCodeRowId.value = null;

    clearTimeout(expandCloseTimer.value);
    expandCloseTimer.value = setTimeout(() => {
      if (hoverCodeRowId.value !== row.id) {
        const index = expandedRowKeys.value.indexOf(row.id);
        if (index > -1) {
          expandedRowKeys.value.splice(index, 1);
        }
      }
    }, 40);
  }
};

const handleExpandMouseEnter = (row) => {
  hoverCodeRowId.value = row.id;
  clearTimeout(expandCloseTimer.value);
};

const handleExpandMouseLeave = (row) => {
  clearTimeout(expandCloseTimer.value);
  expandCloseTimer.value = setTimeout(() => {
    if (hoverCodeRowId.value !== row.id) {
      const index = expandedRowKeys.value.indexOf(row.id);
      if (index > -1) {
        expandedRowKeys.value.splice(index, 1);
      }
    }
  }, 40);
};

const closeExpandRow = (row) => {
  const index = expandedRowKeys.value.indexOf(row.id);
  if (index > -1) {
    expandedRowKeys.value.splice(index, 1);
  }
  hoverCodeRowId.value = null;
  clearTimeout(expandCloseTimer.value);
};

const handleExpandChange = (row, expandedRows) => {
  const isExpanded = expandedRows.includes(row.id);

  if (isExpanded) {
    if (!expandedRowKeys.value.includes(row.id)) {
      expandedRowKeys.value.push(row.id);
    }
    if (!expandedRowActiveTab.value[row.id]) {
      if (row.informationList && Object.keys(row.informationList).length) {
        expandedRowActiveTab.value[row.id] = 'contact';
      } else if (row.customerTransfer?.length) {
        expandedRowActiveTab.value[row.id] = 'finance';
      } else if (row.supplierContactList?.length) {
        expandedRowActiveTab.value[row.id] = 'visit';
      }
    }
  } else {
    const index = expandedRowKeys.value.indexOf(row.id);
    if (index > -1) {
      expandedRowKeys.value.splice(index, 1);
    }
  }
};

// ==================== 滚轮翻页功能 ====================
const handleTableWheel = (event) => {
  const tableBody = event.currentTarget.querySelector('.el-table__body-wrapper');
  if (tableBody && tableBody.scrollHeight > tableBody.clientHeight) {
    return;
  }
  handleWheelPagination(event);
};

const handlePaginationWheel = (event) => {
  handleWheelPagination(event);
};

const handleWheelPagination = (event) => {
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
};

// ==================== 生命周期 ====================
onMounted(() => {
  loadColumnSettings();
  getList();
  getCustomerDropdown();
  getCustomerCountry();
  fetchSettlementCurrency();
  fetchSettlementMethod();
  fetchPaymentTerms();
  fetchSalesperson();
  getawaitgroupId();
  getawaitpackaging();
  getawaitClient();

  setTimeout(() => {
    calculateTableHeight();
  }, 300);

  window.addEventListener("resize", calculateTableHeight);
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", calculateTableHeight);
  clearTimeout(scrollTimer.value);
  clearTimeout(expandCloseTimer.value);
  // 释放所有可能残留的对象URL
  [logoPreviewUrl, zmmttpPreviewUrl, zmmttpsPreviewUrl, cmmttpPreviewUrl, cmmttpsPreviewUrl].forEach(urlRef => {
    if (urlRef.value) {
      revokeObjectURL(urlRef.value);
    }
  });
});

watch(visibleColumns, () => {
  nextTick(() => {
    calculateTableHeight();
  });
}, { deep: true });
</script>

<style scoped>
/* 样式保持不变 */
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

.customer-code-cell {
  cursor: default;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 12px;
}

.customer-code-cell:hover {
  background-color: #f0f9ff;
  color: #1890ff;
  font-weight: 500;
}

.customer-tabs {
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
  margin-bottom: 16px;
}

/* 列设置样式 */
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

:deep(.hover-row) {
  background-color: #f5f7fa !important;
}

:deep(.hover-row td) {
  background-color: #f5f7fa !important;
}

/* 图片上传样式 */
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

.license-uploader {
  width: 100%;
}

:deep(.el-upload) {
  width: 100%;
}

:deep(.el-upload .el-button) {
  width: 100%;
}

/* 扩展行相关样式 */
.row-expand-container {
  position: relative;
  z-index: 100;
  background-color: #fff;
  border-top: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

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
</style>
