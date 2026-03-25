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
            :expand-on-click-node="false"
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

        <el-table v-loading="loading" :data="materialList" @selection-change="handleSelectionChange">
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

          <el-table-column label="编码" align="center" width="120px" prop="number" />
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

          <el-table-column prop="deliveryState" width="84px" label="物料状态">
            <template #default="{ row }">
              <!-- 根据不同的状态显示不同的标签 -->
              <el-tag v-if="row.fstate === 'B'" type="danger">启用</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="创建人" align="center" prop="creator" show-overflow-tooltip />
          <el-table-column label="创建时间" align="center" width="105px" prop="creator_time" />
          <el-table-column label="操作" width="180" align="center" fixed="right" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['k3:material:edit']">修改</el-button>
              <el-button link type="primary" icon="Tickets" @click="handleViewAuditLogs(scope.row)" v-hasPermi="['k3:material:auditLogs']">日志</el-button>
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['k3:material:remove']">删除</el-button>
            </template>
          </el-table-column>
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

    <!-- 更新 -->
    <el-dialog :title="title" v-model="opens" width="600px" append-to-body>
      <el-form :model="form" :rules="rules" ref="materialRef" label-width="80px">
        <!-- 图片上传 -->
        <el-row>
          <el-col :span="12">
            <el-form-item label="物料图片">
              <el-upload
                ref="imageUpload"
                :auto-upload="false"
                :limit="1"
                :on-change="handleImageChange"
                :on-remove="handleRemove"
                accept="image/*"
              >
                <el-button type="primary">选择图片</el-button>
                <template #tip>
                  <div class="el-upload__tip">支持 jpg/png 格式</div>
                </template>
              </el-upload>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="检测报告">
              <el-upload
                ref="reportUpload"
                :auto-upload="false"
                :limit="1"
                :on-change="handleReportChange"
                accept=".pdf,.doc,.docx"
              >
                <el-button type="primary">选择文件</el-button>
                <template #tip>
                  <div class="el-upload__tip">支持 pdf/doc/docx 格式</div>
                </template>
              </el-upload>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="物料图片" prop="image">
              <el-image
                style="width: 100px; height: 100px"
                :src="form.image"
                :zoom-rate="1.2"
                :max-scale="7"
                :min-scale="0.2"
                :preview-src-list="srcList"
                show-progress
                :initial-index="4"
                fit="cover"
              />
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
              <el-input v-model="form.specification" placeholder="请输入规格型号" maxlength="11" />
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
            <el-form-item label="物料分组" prop="materialgroup">
              <el-tooltip
                :content="form.materialgroup || form.materialgroup || '请选择'"
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
            <el-form-item label="交期红线" prop="number">
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
              <el-input v-model="form.englishProductName" placeholder="请输入名称" maxlength="11" />
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
        </el-row>

        <el-col :span="24">
          <!-- 原现英文描述 -->
          <el-col :span="24">
            <el-form-item label="原现英文描述" label-width="100px" prop="description1">
              <el-input v-model="form.description1" type="textarea" :rows="6" placeholder="请输入内容" disabled></el-input>
            </el-form-item>
          </el-col>
        </el-col>

        <el-col :span="24">
          <!-- 现英文描述 -->
          <el-form-item label="现英文描述" label-width="100px" prop="description2">
            <el-input
              v-model="form.description2"
              type="textarea"
              :rows="6"
              placeholder="请输入现英文描述"
              @input="handleDescriptionChange"
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
            <el-form-item label="物料状态">
              <el-select v-model="form.fstate" placeholder="请选择物料状态">
                <el-option
                  v-for="dict in material_status"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                ></el-option>
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
              <el-checkbox v-model="upload.updateSupport" />是否更新已经存在的用户数据
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
            <!-- 使用Flex布局实现横向排列 -->
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
  listMaterial, dictionaryLookupTreeSelect,updateMaterials,updateMaterial,removeBatch,getByNumber,
  handleAIFillEnglish,materialDictionary,xlproductTree,getProductCategory,getMaterialAuditLogs} from "@/api/k3/material";
import errorCode from "@/utils/errorCode";

const { proxy } = getCurrentInstance();
//系统开关,
const { sys_normal_disable,material_status } = proxy.useDict("sys_normal_disable","material_status");


const materialList = ref([]);
//物料属性
const materialDictionarys = ref([]);
//新老产品
const xlproductsTree = ref([]);

//产品类别
const productCategoryTree = ref([]);

//物料分组
const dictionaryLookupOptions = ref(undefined);

//图片上传
//const imageUpload = ref()

//文件上传
const reportUpload = ref()

//新增弹窗
const open = ref(false);

//编辑弹窗
const opens = ref(false);

const loading = ref(true);
const showSearch = ref(true);
/*给下拉框赋值*/
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const fxlcp = ref(''); // 初始值设为空字符串

const value = ref('');

/*** 物料导入参数 */
const upload = reactive({
  // 是否显示弹出层（物料导入）
  open: false,
  // 弹出层标题（物料 导入）
  title: "",
  // 是否禁用上传
  isUploading: false,
  // 是否更新已经存在的物料数据
  updateSupport: 0,
  // 设置上传的请求头部
  headers: { Authorization: "Bearer " + getToken() },
  // 上传的地址
  url: import.meta.env.VITE_APP_BASE_API + "/k3/material/importExcel"
});

const options = [
  {
    deliveryState: '1',
    label: '常规',
  },
  {
    deliveryState: '2',
    label: '低风险',
  },
  {
    deliveryState: '3',
    label: '中风险',
  },
  {
    deliveryState: '4',
    label: '高风险',
  }
]


const treeData = ref([
  {
    label: '交期红线',
    deliveryState: 'root',
    children: [
      {
        label: '常规',
        deliveryState: '1'
      },
      {
        label: '低风险',
        deliveryState: '2'
      },
      {
        label: '中风险',
        deliveryState: '3'
      },
      {
        label: '高风险',
        deliveryState: '4'
      }
    ]
  }
])


const checkedKeys = ref([])
const deliveryState = ref('') // 用于select的v-model

//批量修交付红线
const handleCheck = (deliveryState) => {
  const id = ids.value;
  const state = deliveryState.deliveryState;
  // 更严格的判断
  if (!id || typeof id !== 'object' || !Array.isArray(id) || id.length === 0) {
    proxy.$modal.msgWarning('请先选择要操作的物料');
    return;
  }
  proxy.$modal.confirm(`是否确认修改物料交期红线为 ------ ${deliveryState.label}？`)
    .then(() => {
      return updateMaterials(id,state); // 假设后端接口是 updateMaterialStatus
    })
    .then(() => {
      getList(); // 重新获取列表
      proxy.$modal.msgSuccess('修改成功');
    })
    .catch(() => {
      // 用户取消或请求失败
    });
}

// 审计日志相关数据
const auditLogsDialog = ref(false);
const auditLogsList = ref([]);
const auditLogsLoading = ref(false);
const auditLogsTotal = ref(0);
const auditLogsCurrentPage = ref(1);
const auditLogsPageSize = ref(10);
const auditLogsTitle = ref('');
const currentMaterialId = ref(null);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    fstate: "B",
    number: undefined,
    name: undefined,
    specification: undefined
  },
  rules: {
    number: [{ required: true, message: "编码不能为空", trigger: "blur" }],
    name: [{ required: true, message: "名称不能为空", trigger: "blur" }],
    specification: [{ required: true, message: "规格型号不能为空", trigger: "blur" }],
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询物料列表 */
function getList() {
  loading.value = true;
  listMaterial(queryParams.value).then(response => {
    //records
    materialList.value = response.records;
    total.value = response.total;
    loading.value = false;
  });
}

const imageRef = ref(null)
let previewTimer = null

const handleMouseEnter = (imageUrl) => {
  // 延迟300ms触发预览，避免鼠标快速划过时触发
  previewTimer = setTimeout(() => {
    if (imageRef.value) {
      // 手动触发预览
      const previewInstance = imageRef.value.previewInstance
      if (previewInstance) {
        previewInstance.setPreviewImages([imageUrl])
        previewInstance.setIndex(0)
        previewInstance.show()
      }
    }
  })
}

const handleMouseLeave = () => {
  clearTimeout(previewTimer)
  // 鼠标离开时关闭预览
  if (imageRef.value) {
    const previewInstance = imageRef.value.previewInstance
    if (previewInstance) {
      previewInstance.close()
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


//物料属性
function getMaterialDictionary(){
  const categoryName = '物料属性' || categoryName.value;
  materialDictionary(categoryName).then(response => {
    materialDictionarys.value = response.data;
  });
}

//新老产品
function getxlproduct(){
  const categoryName = '新老产品' || categoryName.value;
  xlproductTree(categoryName).then(response => {
    xlproductsTree.value = response.data;
  });
}

//产品类别
function ProductCategory() {
  const categoryName = '产品类别' || categoryName.value;
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
    postId: undefined,
    postCode: undefined,
    postName: undefined,
    postSort: 0,
    status: "1",
    remark: undefined
  };
  proxy.resetForm("materialRef");
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  queryParams.value.materialgroup = undefined;
  proxy.$refs.dictionaryTreeRef.setCurrentKey(null);
  handleQuery();
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 修改按钮操作 */
function handleUpdate(records) {
  reset();
  const id = records.id || ids.value;
  getByNumber(id).then(response => {
    form.value = response.data;
    opens.value = true;
    title.value = "修改物料";
  });
}

// 这里需要替换为实际的API调用
function handleAIFillEnglishDesc() {

  proxy.$refs["materialRef"].validate(valid => {
    if (valid) {
      handleAIFillEnglish(form.value).then(response => {
          if (response.errorCode === 0) {
            const descriptionText = response.data || response.description1 || response.description || response
            const finalText = typeof descriptionText === 'string' ? descriptionText : JSON.stringify(descriptionText)
            this.form.description2 = finalText
            proxy.$modal.msgSuccess("英文描述生成成功");
          } else {
            proxy.$modal.msgSuccess("英文描述生成失败");
          }
        }
      );
    }
  });
}

// 处理图片选择
let selectedImage = null
const handleImageChange = (file) => {
  selectedImage = file.raw
}

const handleRemove = (file) => {
  selectedImage = file.raw
}


// 处理报告选择
let selectedReport = null
const handleReportChange = (file) => {
  selectedReport = file.raw
}

function submitsForm(){
  proxy.$refs["materialRef"].validate(valid => {
    if (valid) {
      if (form.value.id != undefined) {
        updateMaterial(form.value,selectedImage,selectedReport).then(response => {
          proxy.$modal.msgSuccess("更新成功");
          opens.value = false;
          getList();
        });
      } else {
        proxy.$modal.msgSuccess("更新失败");
        opens.value = false;
        getList();
      };
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const ids = row.id || ids.value;
  proxy.$modal.confirm('是否确认删除物料编号为"' + ids + '"的数据项？').then(function() {
    return removeBatch(ids);
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

/**文件上传中处理 */
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
  }else {
    proxy.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.data + "</div>", "导入结果", { dangerouslyUseHTMLString: true });
  }

  getList();
};

/** 下载模板操作 */
function importTemplate() {
  proxy.download("/k3/material/importTemplate", {
  }, `material_template_${new Date().getTime()}.xlsx`);
};

/** 提交上传文件 */
function submitFileForm() {
  proxy.$refs["uploadRef"].submit();
};

/** 查看审计日志 */
function handleViewAuditLogs(row) {
  currentMaterialId.value = row.id;
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
            // 如果解析失败，尝试其他方式处理
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
    'product_category': '产品类别',
    'english_product_name': '英文品名',
    'number': '编码',
    'name': '名称',
    'specification': '规格型号',
    'fxlcp': '新老产品',
    'delivery_state': '交付红线',
    'hsbm': 'HS编码',
    'description1': '原英文描述',
    'description2': '现英文描述',
    'englishProductName': '英文品名',
    'creator': '创建人',
    'creator_time': '创建时间',
    'image': '图片',
    'inspectionReport': '检测报告',
    'f_state': '物料状态'
  };
  return fieldMap[fieldKey] || fieldKey;
}

/** 审计日志分页变化 */
function handleAuditLogsPaginationChange(page) {
  auditLogsCurrentPage.value = page;
  getMaterialAuditLogsList();
}

getList();

//物料分组
getDictionaryLookup();
//物料属性
getMaterialDictionary();

//新老产品
getxlproduct();

//产品类别
ProductCategory();

</script>

<style scoped>
/*中划线样式*/
.tagWithLine {
  text-decoration: line-through;
  color: #999; /* 可选：调整颜色 */
}

/* 横向排列的变更详情容器 */
.changes-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 16px; /* 行间距 列间距 */
}

/* 横向排列的变更项 */
.change-item-horizontal {
  flex: 0 0 calc(25% - 12px); /* 每行显示4个，减去间隙 */
  min-width: 150px; /* 最小宽度 */
  max-width: 250px; /* 最大宽度 */
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
    flex: 0 0 calc(33.33% - 11px); /* 中等屏幕显示3个 */
  }
}

@media (max-width: 768px) {
  .change-item-horizontal {
    flex: 0 0 calc(50% - 8px); /* 小屏幕显示2个 */
  }
}

@media (max-width: 480px) {
  .change-item-horizontal {
    flex: 0 0 100%; /* 超小屏幕显示1个 */
  }
}

/* 原有样式保持 */
.dialog-footer {
  text-align: right;
  margin-top: 16px;
}


</style>
