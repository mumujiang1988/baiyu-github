<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch">
      <el-form-item label="采购调价编码" prop="fBillNo">
        <el-input
          v-model="queryParams.fBillNo"
          placeholder="请输入采购调价编码"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="名称" prop="fName">
        <el-input
          v-model="queryParams.fName"
          placeholder="请输入名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="供应商" prop="fSupplierId">
        <el-input
          v-model="queryParams.fSupplierId"
          placeholder="请输入供应商"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>

      <el-form-item label="物料名称" prop="fmaterialName">
        <el-input
          v-model="queryParams.fmaterialName"
          placeholder="请输入物料名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>

      <el-form-item label="物料编码" prop="fMaterialId">
        <el-input
          v-model="queryParams.fMaterialId"
          placeholder="请输入物料编码"
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
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['k3:billhead:update']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['k3:billhead:delete']"
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
        :data="billHeadList"
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
              v-if="['fentryEffectiveDate', 'fentryExpiryDate', 'createdAt', 'feffectiveDate', 'fexpiryDate', 'fCreateDate'].includes(col.prop)"
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

    <!-- 修改/新增采购调价对话框 -->
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
            <el-form-item label="单据编码" prop="fbillNo">
              <el-input
                v-model="form.fbillNo"
                placeholder="单据编码"
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
            <el-form-item label="日期" prop="fDate">
              <el-date-picker
                v-model="form.fDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                disabled
              />
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
        </el-row>

        <!-- 调价明细 -->
        <el-card class="section-card" style="margin-top: 20px">
          <template #header>
            <div class="section-header">
              <span>调价明细</span>
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
            :data="form.patentries"
            border
            stripe
            style="width: 100%"
            @selection-change="handleEntrySelectionChange"
            max-height="400"
          >
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column type="index" label="序号" width="60" align="center" />

            <el-table-column prop="fadjustType" label="调价类型" width="140" align="center">
              <template #default="{ row }">
                <el-input v-model="row.fadjustType" placeholder="调价类型" disabled />
              </template>
            </el-table-column>

            <el-table-column prop="fpriceListId" label="价目表" width="140" align="center">
              <template #default="{ row }">
                <el-input v-model="row.fpriceListId" placeholder="价目表" disabled />
              </template>
            </el-table-column>

            <el-table-column prop="fsupplierId" label="供应商" width="180" align="center">
              <template #default="{ row }">
                <el-select
                  v-model="row.fsupplierId"
                  placeholder="请选择供应商"
                  filterable
                  clearable
                  style="width: 100%"
                  :disabled="!isFormEditable"
                >
                  <el-option
                    v-for="item in supplierList"
                    :key="item.supplierId"
                    :label="item.supplierName"
                    :value="item.supplierId"
                  />
                </el-select>
              </template>
            </el-table-column>

            <el-table-column prop="fcurrencyId" label="币别" width="120" align="center">
              <template #default="{ row }">
                <el-select
                  v-model="row.fcurrencyId"
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
              </template>
            </el-table-column>

            <el-table-column prop="fpriceListObject" label="调价对象" width="140" align="center">
              <template #default="{ row }">
                <el-select
                  v-model="row.fpriceListObject"
                  placeholder="请选择调价对象"
                  style="width: 100%"
                  :disabled="!isFormEditable"
                >
                  <el-option
                    v-for="dict in price_object"
                    :key="dict.value"
                    :label="dict.label"
                    :value="dict.value"
                  />
                </el-select>
              </template>
            </el-table-column>

            <el-table-column label="含税" width="100" align="center">
              <template #default="{ row }">
                <el-checkbox-group v-model="row.fIsIncludedTax">
                  <el-checkbox
                    v-for="item in f_is_included_tax"
                    :key="item.value"
                    :label="item.value"
                    :disabled="!isFormEditable"
                  >
                    {{ item.label }}
                  </el-checkbox>
                </el-checkbox-group>
              </template>
            </el-table-column>

            <el-table-column label="价外税" width="100" align="center">
              <template #default="{ row }">
                <el-checkbox-group v-model="row.fIsIncludedTax">
                  <el-checkbox
                    v-for="item in f_is_included_tax"
                    :key="item.value"
                    :label="item.value"
                    :disabled="!isFormEditable"
                  >
                    {{ item.label }}
                  </el-checkbox>
                </el-checkbox-group>
              </template>
            </el-table-column>

            <el-table-column prop="fmaterialId" label="物料编码" width="150" align="center">
              <template #default="{ row }">
                <el-input v-model="row.fmaterialId" placeholder="物料编码" disabled />
              </template>
            </el-table-column>

            <el-table-column prop="fmaterialName" label="物料名称" width="150" align="center">
              <template #default="{ row }">
                <el-input v-model="row.fmaterialName" placeholder="物料名称" disabled />
              </template>
            </el-table-column>

            <el-table-column prop="fuom01" label="规格型号" width="140" align="center">
              <template #default="{ row }">
                <el-input v-model="row.fuom01" placeholder="规格型号" disabled />
              </template>
            </el-table-column>

            <el-table-column prop="fmaterialGroupId" label="物料分组" width="140" align="center">
              <template #default="{ row }">
                <el-input v-model="row.fmaterialGroupId" placeholder="物料分组" disabled />
              </template>
            </el-table-column>

            <el-table-column prop="fmaterialGroupName" label="物料分组名称" width="140" align="center">
              <template #default="{ row }">
                <el-input v-model="row.fmaterialGroupName" placeholder="物料分组名称" disabled />
              </template>
            </el-table-column>

            <el-table-column prop="fUnitId" label="计价单位" width="120" align="center">
              <template #default="{ row }">
                <el-select
                  v-model="row.fUnitId"
                  placeholder="请选择单位"
                  filterable
                  clearable
                  style="width: 100%"
                  :disabled="!isFormEditable"
                >
                  <el-option
                    v-for="item in pricingunitList"
                    :key="item.unitId"
                    :label="item.unitName"
                    :value="item.unitId"
                  />
                </el-select>
              </template>
            </el-table-column>

            <el-table-column prop="ffromqty" label="从" width="120" align="center">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.ffromqty"
                  :precision="2"
                  :min="0"
                  controls-position="right"
                  style="width: 100%"
                  :disabled="!isFormEditable"
                />
              </template>
            </el-table-column>

            <el-table-column prop="ftoQty" label="至" width="120" align="center">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.ftoQty"
                  :precision="2"
                  :min="0"
                  controls-position="right"
                  style="width: 100%"
                  :disabled="!isFormEditable"
                />
              </template>
            </el-table-column>

            <el-table-column prop="fbeforePrice" label="调前单价" width="120" align="center">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.fbeforePrice"
                  :precision="4"
                  controls-position="right"
                  style="width: 100%"
                  disabled
                />
              </template>
            </el-table-column>

            <el-table-column prop="fafterPrice" label="调后单价" width="120" align="center">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.fafterPrice"
                  :precision="4"
                  :min="0"
                  controls-position="right"
                  style="width: 100%"
                  :disabled="!isFormEditable"
                />
              </template>
            </el-table-column>

            <el-table-column prop="fbeforeTaxPrice" label="调前含税单价" width="130" align="center">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.fbeforeTaxPrice"
                  :precision="4"
                  controls-position="right"
                  style="width: 100%"
                  disabled
                />
              </template>
            </el-table-column>

            <el-table-column prop="fafterTaxPrice" label="调后含税单价" width="130" align="center">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.fafterTaxPrice"
                  :precision="4"
                  :min="0"
                  controls-position="right"
                  style="width: 100%"
                  :disabled="!isFormEditable"
                />
              </template>
            </el-table-column>

            <el-table-column prop="fhscj" label="含税差价" width="120" align="center">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.fhscj"
                  :precision="4"
                  controls-position="right"
                  style="width: 100%"
                  disabled
                />
              </template>
            </el-table-column>

            <el-table-column prop="fadjustRange" label="调价幅度%" width="120" align="center">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.fadjustRange"
                  :precision="2"
                  :min="-100"
                  :max="100"
                  controls-position="right"
                  style="width: 100%"
                  :disabled="!isFormEditable"
                />
              </template>
            </el-table-column>

            <el-table-column prop="fbeforeTaxRate" label="调前税率" width="120" align="center">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.fbeforeTaxRate"
                  :precision="2"
                  :min="0"
                  :max="100"
                  controls-position="right"
                  style="width: 100%"
                  disabled
                />
              </template>
            </el-table-column>

            <el-table-column prop="fafterTaxRate" label="调后税率" width="120" align="center">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.fafterTaxRate"
                  :precision="2"
                  :min="0"
                  :max="100"
                  controls-position="right"
                  style="width: 100%"
                  :disabled="!isFormEditable"
                />
              </template>
            </el-table-column>

            <el-table-column prop="fbeforePriceCoefficient" label="调前价格系数" width="130" align="center">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.fbeforePriceCoefficient"
                  :precision="4"
                  controls-position="right"
                  style="width: 100%"
                  disabled
                />
              </template>
            </el-table-column>

            <el-table-column prop="fafterPriceCoefficient" label="调后价格系数" width="130" align="center">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.fafterPriceCoefficient"
                  :precision="4"
                  controls-position="right"
                  style="width: 100%"
                  :disabled="!isFormEditable"
                />
              </template>
            </el-table-column>

            <el-table-column prop="fupPrice" label="价格上限" width="120" align="center">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.fupPrice"
                  :precision="4"
                  :min="0"
                  controls-position="right"
                  style="width: 100%"
                  :disabled="!isFormEditable"
                />
              </template>
            </el-table-column>

            <el-table-column prop="fdownPrice" label="价格下限" width="120" align="center">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.fdownPrice"
                  :precision="4"
                  :min="0"
                  controls-position="right"
                  style="width: 100%"
                  :disabled="!isFormEditable"
                />
              </template>
            </el-table-column>

            <el-table-column prop="feffectiveDate" label="生效日期" width="130" align="center">
              <template #default="{ row }">
                <el-date-picker
                  v-model="row.feffectiveDate"
                  type="date"
                  placeholder="选择生效日期"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                  :disabled="!isFormEditable"
                />
              </template>
            </el-table-column>

            <el-table-column prop="fexpiryDate" label="失效日期" width="130" align="center">
              <template #default="{ row }">
                <el-date-picker
                  v-model="row.fexpiryDate"
                  type="date"
                  placeholder="选择失效日期"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                  :disabled="!isFormEditable"
                />
              </template>
            </el-table-column>

            <el-table-column prop="fprocessOrgId" label="需求组织" width="140" align="center">
              <template #default="{ row }">
                <el-input v-model="row.fprocessOrgId" placeholder="需求组织" disabled />
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
  </div>
</template>

<script setup name="Procurement">
import {
  billHeadsList,
  billHeaddelete,
  billheaddetails
} from "@/api/k3/billHead";
import { ElMessage, ElMessageBox } from "element-plus";
import { ref, computed, onMounted, nextTick, watch, getCurrentInstance, onBeforeUnmount } from "vue";
import draggable from "vuedraggable";
import { Rank } from "@element-plus/icons-vue";
import dayjs from "dayjs";
import { debounce } from "lodash-es";

const { proxy } = getCurrentInstance();
const {
  sys_normal_disable,
  price_object,
  business_inquiry,
  f_zxsms,
  f_document_status,
  f_is_included_tax
} = proxy.useDict(
  "sys_normal_disable", "price_object", "business_inquiry", "f_zxsms", "f_document_status", "f_is_included_tax"
);

// ==================== 列表状态 ====================
const billHeadList = ref([]);
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
  fBillNo: undefined,
  fName: undefined,
  fSupplierId: undefined,
  fmaterialName: undefined,
  fMaterialId: undefined,
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

// 表单默认值（使用 patentries 与后端数据结构保持一致）
const defaultForm = {
  fPurchaseOrg: "佰誉云",
  fbillNo: "",
  fname: "",
  fcurrencyID: undefined,
  fdocumentStatus: "",
  fDate: "",
  patentries: []
};

const form = ref({ ...defaultForm });

// 表单验证规则
const rules = {
  fcurrencyID: [{ required: true, message: "请选择币别", trigger: "change" }]
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

// 列定义 - 修正 prop 字段与后端映射字段一致
const defaultColumns = [
  { prop: "fbillNo", label: "采购调价编码", visible: true, width: "120px", align: "left", showOverflowTooltip: true },
  { prop: "fname", label: "名称", visible: true, width: "120px", align: "left", showOverflowTooltip: true },
  { prop: "fsupplierID", label: "供应商", visible: true, width: "120px", align: "left", showOverflowTooltip: true },
  { prop: "fmaterialId", label: "物料编码", visible: true, width: "120px", align: "left", showOverflowTooltip: true },
  { prop: "fmaterialName", label: "物料名称", visible: true, width: "120px", align: "left", showOverflowTooltip: true },
  { prop: "ffromqty", label: "从", visible: true, width: "100px", align: "right", showOverflowTooltip: true },
  { prop: "ftoQty", label: "至", visible: true, width: "100px", align: "right", showOverflowTooltip: true },
  { prop: "fafterPrice", label: "调后单价", visible: true, width: "100px", align: "center", showOverflowTooltip: true },
  { prop: "fafterTaxPrice", label: "调后含税单价", visible: true, width: "120px", align: "right", showOverflowTooltip: true },
  { prop: "feffectiveDate", label: "生效日期", visible: true, width: "100px", align: "right", showOverflowTooltip: true },
  { prop: "fexpiryDate", label: "失效日期", visible: true, width: "100px", align: "right", showOverflowTooltip: true },
  { prop: "fcurrencyId", label: "币别", visible: true, width: "100px", align: "right", showOverflowTooltip: true },
  { prop: "fafterPriceCoefficient", label: "调后价格系数", visible: true, width: "120px", align: "left", showOverflowTooltip: true },
  { prop: "createBy", label: "创建人", visible: true, width: "100px", align: "left", showOverflowTooltip: true },
  { prop: "createTime", label: "创建时间", visible: true, width: "160px", align: "center", showOverflowTooltip: true }
];

const allColumns = ref([...defaultColumns]);

const visibleColumns = computed(() => allColumns.value.filter((col) => col.visible));

const dragOptions = {
  animation: 200,
  group: "columns",
  disabled: false,
  ghostClass: "ghost"
};

// 根据字典值和字典列表获取对应的标签文本
const getDictLabel = (dictList, value) => {
  if (!dictList || !value) return '';
  const item = dictList.find(item => item.value == value);
  return item ? item.label : '';
};

// 根据字典值和字典列表获取对应的标签类型（用于 el-tag 的 type）
const getDictType = (dictList, value) => {
  if (!dictList || !value) return '';
  const item = dictList.find(item => item.value == value);
  return item ? item.listClass : '';
};

// ==================== 表单可编辑性控制 ====================
// 从字典中提取允许编辑的状态值（暂存、创建、重新审核）
const editableStatusValues = computed(() => {
  const dict = f_document_status.value || [];
  const labels = ['暂存', '创建', '重新审核'];
  return dict.filter(item => labels.includes(item.label)).map(item => item.value);
});

// 判断当前表单是否可编辑：新增模式始终可编辑；修改模式根据单据状态判断
const isFormEditable = computed(() => {
  if (!isEditMode.value) return true;
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

// ==================== 列表 API ====================
const getList = async () => {
  loading.value = true;
  try {
    const response = await billHeadsList(queryParams.value);
    if (response?.rows) {
      billHeadList.value = response.rows.map((row) => {
        const firstEntry = row.patentries?.[0] || {};
        return {
          ...row,
          ffromqty: firstEntry.ffromqty ?? "",
          ftoQty: firstEntry.ftoQty ?? "",
          fafterPrice: firstEntry.fafterPrice ?? "",
          fafterTaxPrice: firstEntry.fafterTaxPrice ?? "",
          feffectiveDate: firstEntry.feffectiveDate ?? "",
          fexpiryDate: firstEntry.fexpiryDate ?? "",
          fcurrencyId: firstEntry.fcurrencyId ?? "",
          fafterPriceCoefficient: firstEntry.fafterPriceCoefficient ?? "",
          fmaterialId: firstEntry.fmaterialId ?? "",
          fmaterialName: firstEntry.fmaterialName ?? "",
          fsupplierID: firstEntry.fsupplierId ?? ""
        };
      });
      total.value = response.total || 0;
    } else {
      billHeadList.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error("获取采购调价表失败:", error);
    ElMessage.error("获取采购调价表失败");
    billHeadList.value = [];
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
    await ElMessageBox.confirm("是否确认删除选中的采购调价？", "警告", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning"
    });
    await billHeaddelete(delIds);
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
  form.value = JSON.parse(JSON.stringify(defaultForm));
};

const cancel = () => {
  open.value = false;
  resetForm();
  formRef.value?.clearValidate();
};

const handleUpdate = async () => {
  if (ids.value.length !== 1) {
    ElMessage.warning("请选择一条数据");
    return;
  }
  try {
    const response = await billheaddetails(ids.value[0]);
    const data = response.data || response;
    // 确保明细字段使用 patentries
    form.value = {
      ...data,
      patentries: data.patentries || []
    };
    open.value = true;
    title.value = "修改采购调价表";
    isEditMode.value = true;
  } catch (error) {
    console.error("获取详情失败:", error);
    ElMessage.error("获取详情失败");
  }
};

const submitForm = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true;
      try {
        const submitData = JSON.parse(JSON.stringify(form.value));
        // 确保明细字段名为 patentries
        if (submitData.patentries) {
          submitData.patentries = submitData.patentries.map(entry => {
            // 清理临时字段（如 id 等）
            const { id, ...cleanEntry } = entry;
            return cleanEntry;
          });
        }

        // 保存接口待补充
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

// 新增明细行（初始化所有字段，与数据结构对齐）
const handleAddContact = () => {
  if (!form.value.patentries) form.value.patentries = [];
  form.value.patentries.push({
    id: Date.now() + Math.random(),
    fadjustType: "",
    fpriceListId: "",
    fsupplierId: "",
    fcurrencyId: "",
    fpriceListObject: "",
    fIsIncludedTax: [],
    fmaterialId: "",
    fmaterialName: "",
    fuom01: "",
    fmaterialGroupId: "",
    fmaterialGroupName: "",
    fUnitId: "",
    ffromqty: 0,
    ftoQty: 0,
    fbeforePrice: 0,
    fafterPrice: 0,
    fbeforeTaxPrice: 0,
    fafterTaxPrice: 0,
    fhscj: 0,
    fadjustRange: 0,
    fbeforeTaxRate: 0,
    fafterTaxRate: 0,
    fbeforePriceCoefficient: 0,
    fafterPriceCoefficient: 0,
    fupPrice: 0,
    fdownPrice: 0,
    feffectiveDate: "",
    fexpiryDate: "",
    fprocessOrgId: ""
  });
};

const handleDeleteContact = () => {
  if (!selectedEntryRows.value.length) {
    ElMessage.warning("请选择要删除的行");
    return;
  }
  const idsToRemove = selectedEntryRows.value.map((row) => row.id);
  form.value.patentries = form.value.patentries.filter((row) => !idsToRemove.includes(row.id));
  selectedEntryRows.value = [];
};

const handleBatchFill = () => {
  ElMessage.info("批量填充功能开发中");
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
            width: savedCol.width || defaultCol.width
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
    // 此处调用实际接口获取币别、供应商、计价单位等下拉数据
    // 示例：
    // currencyList.value = await getCurrencyList();
    // supplierList.value = await getSupplierList();
    // pricingunitList.value = await getUnitList();
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
});

watch(visibleColumns, () => {
  calculateTableHeight();
});
</script>

<style scoped>
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
