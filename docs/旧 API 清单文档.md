# 旧 API 清单文档

**生成时间**: 2026-03-28  
**项目**: RuoYi-WMS ERP 系统  
**说明**: 本文档记录低代码引擎（ruoyi-erp-api）之外的所有标准后端 API 接口

---

## 目录

- [一、业务接口（ruoyi-admin-wms）](#一业务接口 ruoyi-admin-wms)
  - [1.1 AI 相关接口](#11-ai-相关接口)
  - [1.2 Dify 集成接口](#12-dify 集成接口)
  - [1.3 飞书 API 接口](#13-飞书 api 接口)
  - [1.4 金蝶字典查询接口](#14-金蝶字典查询接口)
- [二、金蝶 K3 Cloud 集成接口](#二金蝶 k3-cloud 集成接口)
  - [2.1 基础数据同步](#21-基础数据同步)
  - [2.2 采购管理](#22-采购管理)
  - [2.3 销售管理](#23-销售管理)
  - [2.4 库存管理](#24-库存管理)
  - [2.5 财务管理](#25-财务管理)
  - [2.6 系统配置](#26-系统配置)
- [三、RuoYi 系统接口](#三 ruoyi 系统接口)
  - [3.1 系统管理](#31-系统管理)
  - [3.2 认证授权](#32-认证授权)
  - [3.3 系统监控](#33-系统监控)
- [四、代码生成接口](#四代码生成接口)

---

## 一、业务接口（ruoyi-admin-wms）

### 1.1 AI 相关接口

**基础路径**: `/ai/bot`  
**Controller**: `AIBotController.java`  
**文件大小**: 5.2KB

#### 接口列表

| 方法 | 路径 | 功能 | 参数 | 返回值 |
|------|------|------|------|--------|
| POST | `/chat` | AI 对话 | `AIConversation`, `MultipartFile` | `SseEmitter` (SSE 流式输出) |
| POST | `/queryAll` | 查询所有机器人 | 无 | `Result` |
| POST | `/feishu-image-recognition` | 飞书图片识别 | `AIConversation` | `SseEmitter` |

#### 核心功能

- **业务类型**:
  - `SIMPLE_QNA`: 简单问答
  - `KNOWLEDGE_BASE`: 知识库问答
  - `IMAGE_RECOGNITION`: 图片识别

- **特性**:
  - 支持文件上传（图片、文档）
  - SSE 流式输出，永不超时
  - 基于工厂模式的处理器选择
  - Prompt 模板支持

#### 关键配置

```properties
feishu.ark.base-url=
feishu.ark.api-key=
feishu.ark.model=
```

---

### 1.2 Dify 集成接口

**基础路径**: `/Dify`  
**Controller**: `DifyImageSearchController.java`  
**文件大小**: 10.8KB

#### 接口列表

| 方法 | 路径 | 功能 | 参数 | 返回值 |
|------|------|------|------|--------|
| POST | `/chat` | Dify 对话 | `JSONObject(query, conversation_id)` | `R<JSONObject>` |
| POST | `/image-search-materials` | 图片搜索物料 | `MultipartFile` | `R<List<Bymaterial>>` |

#### 核心功能

- **Dify Chat**:
  - 支持多轮对话（conversation_id）
  - blocking 响应模式
  - 自定义 API 地址：`http://118.178.144.159/v1`

- **图片搜索物料**:
  - 调用 Dify Vision 模型识别图片
  - 根据识别结果匹配本地物料库
  - 返回匹配的物料列表

#### 错误处理

- 状态码处理：4xx、5xx
- JSON 解析异常处理
- 网络连接异常处理

---

### 1.3 飞书 API 接口

**基础路径**: `/ft/api`  
**Controller**: `FtApiController.java`  
**文件大小**: 2.9KB

#### 接口列表

| 方法 | 路径 | 功能 | 参数 | 返回值 |
|------|------|------|------|--------|
| GET | `/getTgtToken` | 获取免登 TGT Token | 无 | `R<String>` |
| POST | `/getCustomers` | 查询客户列表 | `FtCustomerQueryRequest` | `R<List<FtCustomer>>` |
| GET | `/getAccessToken` | 获取 AccessToken（便捷接口） | 无 | `R<String>` |

#### 核心功能

- **Token 管理**:
  - TGT Token → AccessToken 转换
  - 自动 Token 刷新机制

- **客户查询**:
  - 支持分页查询
  - 支持条件过滤
  - 访问令牌自动注入

---

### 1.4 金蝶字典查询接口

**基础路径**: `/api/v1/kingdee`  
**Controller**: `DictionaryLookupcontroller.java`  
**文件大小**: 22.1KB

#### 接口列表

| 方法 | 路径 | 功能 | 参数 | 返回值 |
|------|------|------|------|--------|
| POST | `/dictionaryLookup` | 字典查找（树形结构） | 无 | `Result<List<DictNode>>` |
| POST | `/suppliergroups` | 供应商分组（树形结构） | 无 | `Result<List<DictSupplierGroupsNode>>` |
| GET | `/getnAtion` | 获取国家 | 无 | `Result` |
| GET | `/MaterialDictionary` | 物料字典 | 无 | `Result` |
| GET | `/xlproduct` | 系列产品 | 无 | `Result` |
| GET | `/ProductCategory` | 产品类别 | 无 | `Result` |
| GET | `/supplierclassification` | 供应商分类 | 无 | `Result` |
| GET | `/suppliersource` | 供应商来源 | 无 | `Result` |
| GET | `/currency` | 币种 | 无 | `Result` |
| GET | `/paymentterms` | 付款条件 | 无 | `Result` |
| GET | `/payment` | 付款方式 | 无 | `Result` |
| GET | `/groupId` | 组 ID | 无 | `Result` |
| GET | `/invoicetype` | 发票类型 | 无 | `Result` |
| GET | `/taxclassification` | 税收分类 | 无 | `Result` |

#### 树形结构构建

**dictionaryLookup**:
```java
// 构建树形结构逻辑
Map<String, DictNode> map = new HashMap<>();
// 1. 创建节点映射
// 2. 遍历构建父子关系
// 3. 提取根节点
```

**suppliergroups**:
```java
// 双映射表构建
Map<String, DictSupplierGroupsNode> nodeMap;  // ID -> Node
Map<Integer, String> idMapping;  // 原始 ID -> 节点 ID
```

#### 返回数据结构

**DictNode**:
```json
{
  "id": "1",
  "name": "节点名称",
  "materialgroup": "物料组",
  "parentCode": "父级编码",
  "children": []
}
```

---

## 二、金蝶 K3 Cloud 集成接口

### 2.1 基础数据同步

#### 2.1.1 客户管理

**基础路径**: `/k3/customer`  
**Controller**: `CustomerController.java`  
**文件大小**: 7.9KB  
**权限前缀**: `k3:customer`

| 方法 | 路径 | 功能 | 权限 | 参数 | 返回值 |
|------|------|------|------|------|--------|
| POST | `/sync` | 同步客户到本地数据库 | `k3:customer:query` | 无 | `Result(String)` |
| GET | `/query` | 根据 ID 查询客户 | `k3:customer:query` | `id` | `Result<Customer>` |
| POST | `/save` | 新增客户 | `k3:customer:save` | `Customer`, `MultipartFile[]` | `Result` |
| POST | `/update` | 更新客户 | - | `Customer`, `MultipartFile[]` | `Result` |
| GET | `/list` | 客户列表（分页） | - | `PageQuery` | `TableDataInfo` |

**同步内容**:
- 客户主数据：`syncCustomerMultiThread()`
- 银行信息：`syncCustomerBankInfo()`
- 转让人信息：`syncCustomerTransferInfo()`
- 联系人：`syncCustomerContactList()`

**文件上传**:
- `logoFile`: 客户 Logo
- `zmmttpFile`: 证明文件
- `zmmttpsFile`: 附件
- `cmmttpFile`: 联系人文件

---

#### 2.1.2 供应商管理

**基础路径**: `/k3/supplier`  
**Controller**: `SupplierController.java`  
**文件大小**: 15.5KB  
**权限前缀**: `k3:supplier`

| 方法 | 路径 | 功能 | 权限 | 参数 | 返回值 |
|------|------|------|------|------|--------|
| POST | `/login` | 同步供应商列表 | `k3:supplier:login` | 无 | `Result` |
| POST | `/sync` | 同步供应商 | - | 无 | `Result` |
| GET | `/query` | 查询供应商 | - | `id` | `Result` |
| POST | `/save` | 新增供应商 | - | `Supplier`, `MultipartFile[]` | `Result` |
| POST | `/update` | 更新供应商 | - | `Supplier`, `MultipartFile[]` | `Result` |
| GET | `/list` | 供应商列表（分页） | - | `PageQuery` | `TableDataInfo` |

**同步内容**:
- 供应商主数据
- 联系人信息
- 财务信息
- 银行信息

**关联服务**:
- `SupplierService`: 供应商主数据
- `SupplierContactService`: 联系人
- `FinancialInformationService`: 财务信息
- `MaterialFileService`: 附件管理

---

#### 2.1.3 员工管理

**基础路径**: `/k3/employee`  
**Controller**: `EmployeeController.java`  
**文件大小**: 2.3KB

| 方法 | 路径 | 功能 | 参数 | 返回值 |
|------|------|------|------|--------|
| POST | `/sync/employee` | 同步员工信息 | 无 | `Result` |
| POST | `/sync/bank` | 同步银行信息 | 无 | `Result` |
| POST | `/sync/followup` | 同步跟进人 | 无 | `Result` |
| POST | `/sync/all` | 全部同步 | 无 | `Result` |
| GET | `/{fid}` | 根据 FID 查询 | `fid` | `Result` |

---

#### 2.1.4 物料管理

**基础路径**: `/k3/material`  
**Controller**: `KingdeeMaterialController.java`  
**文件大小**: 24.5KB

| 方法 | 路径 | 功能 | 参数 | 返回值 |
|------|------|------|------|--------|
| POST | `/sync` | 同步物料 | 无 | `Result` |
| GET | `/query` | 查询物料 | `id` | `Result` |
| POST | `/save` | 新增物料 | `Material`, `MultipartFile[]` | `Result` |
| POST | `/update` | 更新物料 | `Material`, `MultipartFile[]` | `Result` |
| GET | `/list` | 物料列表（分页） | `PageQuery` | `TableDataInfo` |

**核心功能**:
- 物料同步（多线程）
- 物料分类管理
- 物料属性管理
- 图片上传（MinIO）

---

### 2.2 采购管理

#### 2.2.1 采购订单

**基础路径**: `/k3/purchase-order`  
**Controller**: `PurchaseOrderController.java`  
**文件大小**: 3.5KB

| 方法 | 路径 | 功能 | 参数 | 返回值 |
|------|------|------|------|--------|
| POST | `/sync` | 同步采购订单 | 无 | `Result` |
| POST | `/save` | 新增采购订单 | `PurchaseOrder` | `Result` |
| POST | `/update` | 更新采购订单 | `PurchaseOrder` | `Result` |
| GET | `/get/{id}` | 根据 ID 查询 | `id` | `Result` |
| GET | `/list` | 采购订单列表 | `PageQuery` | `TableDataInfo` |

---

#### 2.2.2 采购报价

**基础路径**: `/k3/purchase-quotation`  
**Controller**: `PurchaseQuotationController.java`  
**文件大小**: 6.3KB

| 方法 | 路径 | 功能 | 参数 | 返回值 |
|------|------|------|------|--------|
| POST | `/sync` | 同步采购报价 | 无 | `Result` |
| POST | `/save` | 新增采购报价 | `PurchaseQuotation` | `Result` |
| POST | `/update` | 更新采购报价 | `PurchaseQuotation` | `Result` |
| GET | `/get/{id}` | 根据 ID 查询 | `id` | `Result` |
| GET | `/list` | 采购报价列表 | `PageQuery` | `TableDataInfo` |

---

#### 2.2.3 采购入库

**基础路径**: `/k3/purchase-in-stock`  
**Controller**: `PurchaseInStockController.java`  
**文件大小**: 4.1KB

| 方法 | 路径 | 功能 | 参数 | 返回值 |
|------|------|------|------|--------|
| POST | `/sync` | 同步采购入库单 | 无 | `Result` |
| POST | `/save` | 新增采购入库 | `PurchaseInStock` | `Result` |
| POST | `/update` | 更新采购入库 | `PurchaseInStock` | `Result` |
| GET | `/get/{id}` | 根据 ID 查询 | `id` | `Result` |

---

### 2.3 销售管理

#### 2.3.1 销售订单

**基础路径**: `/k3/sale-order`  
**Controller**: `SaleOrderController.java`  
**文件大小**: 4.3KB

| 方法 | 路径 | 功能 | 参数 | 返回值 |
|------|------|------|------|--------|
| POST | `/sync` | 同步销售订单 | 无 | `Result` |
| POST | `/save` | 新增销售订单 | `SaleOrder` | `Result` |
| POST | `/update` | 更新销售订单 | `SaleOrder` | `Result` |
| GET | `/get/{id}` | 根据 ID 查询 | `id` | `Result` |
| GET | `/list` | 销售订单列表 | `PageQuery` | `TableDataInfo` |

---

#### 2.3.2 发货通知

**基础路径**: `/k3/delivery-notice`  
**Controller**: `DeliveryNoticeController.java`  
**文件大小**: 5.1KB

| 方法 | 路径 | 功能 | 参数 | 返回值 |
|------|------|------|------|--------|
| POST | `/sync` | 同步发货通知 | 无 | `Result` |
| POST | `/save` | 新增发货通知 | `DeliveryNotice` | `Result` |
| POST | `/update` | 更新发货通知 | `DeliveryNotice` | `Result` |
| GET | `/get/{id}` | 根据 ID 查询 | `id` | `Result` |
| GET | `/get/billno/{billNo}` | 根据单号查询 | `billNo` | `Result` |
| GET | `/list` | 发货通知列表 | `PageQuery` | `TableDataInfo` |
| GET | `/list/customer/{customerId}` | 根据客户 ID 查询 | `customerId` | `List<DeliveryNotice>` |

---

### 2.4 库存管理

#### 2.4.1 仓库库位

**基础路径**: `/k3/warehouse-location`  
**Controller**: `WarehouseLocationController.java`  
**文件大小**: 3.5KB

| 方法 | 路径 | 功能 | 参数 | 返回值 |
|------|------|------|------|--------|
| POST | `/sync` | 同步库位 | 无 | `Result` |
| GET | `/list` | 库位列表 | 无 | `Result` |

---

#### 2.4.2 收货通知

**基础路径**: `/k3/receive-notice`  
**Controller**: `ReceiveNoticeController.java`  
**文件大小**: 2.1KB

| 方法 | 路径 | 功能 | 参数 | 返回值 |
|------|------|------|------|--------|
| POST | `/sync` | 同步收货通知 | 无 | `Result` |

---

#### 2.4.3 检验单

**基础路径**: `/k3/inspection-bill`  
**Controller**: `InspectionBillController.java`  
**文件大小**: 3.5KB

| 方法 | 路径 | 功能 | 参数 | 返回值 |
|------|------|------|------|--------|
| POST | `/sync` | 同步检验单 | 无 | `Result` |
| GET | `/list` | 检验单列表 | `PageQuery` | `TableDataInfo` |

---

### 2.5 财务管理

#### 2.5.1 财务信息

**基础路径**: `/k3/financial-information`  
**Controller**: `FinancialInformationController.java`  
**文件大小**: 1.5KB

| 方法 | 路径 | 功能 | 参数 | 返回值 |
|------|------|------|------|--------|
| POST | `/sync` | 同步财务信息 | 无 | `Result` |

---

#### 2.5.2 价格表

**基础路径**: `/k3/price-list`  
**Controller**: `PriceListController.java`  
**文件大小**: 7.3KB

| 方法 | 路径 | 功能 | 参数 | 返回值 |
|------|------|------|------|--------|
| POST | `/sync` | 同步价格表 | 无 | `Result` |
| GET | `/list` | 价格表列表 | `PageQuery` | `TableDataInfo` |

---

#### 2.5.3 销售价格

**基础路径**: `/k3/sales-price`  
**Controller**: `SalesPriceController.java`  
**文件大小**: 5.6KB

| 方法 | 路径 | 功能 | 参数 | 返回值 |
|------|------|------|------|--------|
| POST | `/sync` | 同步销售价格 | 无 | `Result` |
| GET | `/list` | 销售价格列表 | `PageQuery` | `TableDataInfo` |

---

### 2.6 系统配置

#### 2.6.1 字典表

**基础路径**: `/k3/dictionary`  
**Controller**: `DictionarytableController.java`  
**文件大小**: 3.9KB

| 方法 | 路径 | 功能 | 参数 | 返回值 |
|------|------|------|------|--------|
| POST | `/add` | 添加字典 | `DictionaryTable` | `Result` |
| GET | `/get/{id}` | 获取字典 | `id` | `Result` |
| GET | `/listTree` | 树形字典列表 | 无 | `Result` |

---

#### 2.6.2 询价单

**基础路径**: `/k3/inquiry-order`  
**Controller**: `InquiryOrderController.java`  
**文件大小**: 6.1KB

| 方法 | 路径 | 功能 | 参数 | 返回值 |
|------|------|------|------|--------|
| POST | `/sync` | 同步询价单 | 无 | `Result` |
| POST | `/save` | 新增询价单 | `InquiryOrder` | `Result` |
| POST | `/update` | 更新询价单 | `InquiryOrder` | `Result` |
| GET | `/get/{id}` | 根据 ID 查询 | `id` | `Result` |
| GET | `/list` | 询价单列表 | `PageQuery` | `TableDataInfo` |

---

#### 2.6.3 供应商联系人

**基础路径**: `/k3/supplier-contact`  
**Controller**: `SupplierContactController.java`  
**文件大小**: 3.4KB

| 方法 | 路径 | 功能 | 参数 | 返回值 |
|------|------|------|------|--------|
| POST | `/sync` | 同步联系人 | 无 | `Result` |
| GET | `/list` | 联系人列表 | `PageQuery` | `TableDataInfo` |

---

## 三、RuoYi 系统接口（ruoyi-system）

### 3.1 系统管理

#### 3.1.1 用户管理

**基础路径**: `/system/user`  
**Controller**: `SysUserController.java`  
**文件大小**: 53 行注解

| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| GET | `/list` | 用户列表 | `system:user:list` |
| GET | `/{userId}` | 用户详情 | `system:user:query` |
| POST | `/` | 新增用户 | `system:user:add` |
| PUT | `/{userId}` | 修改用户 | `system:user:edit` |
| DELETE | `/{userIds}` | 删除用户 | `system:user:remove` |
| POST | `/import` | 导入用户 | `system:user:import` |
| PUT | `/resetPwd` | 重置密码 | `system:user:resetPwd` |

---

#### 3.1.2 角色管理

**基础路径**: `/system/role`  
**Controller**: `SysRoleController.java`

| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| GET | `/list` | 角色列表 | `system:role:list` |
| GET | `/{roleId}` | 角色详情 | `system:role:query` |
| POST | `/` | 新增角色 | `system:role:add` |
| PUT | `/{roleId}` | 修改角色 | `system:role:edit` |
| DELETE | `/{roleIds}` | 删除角色 | `system:role:remove` |
| PUT | `/dataScope` | 分配数据权限 | `system:role:edit` |

---

#### 3.1.3 菜单管理

**基础路径**: `/system/menu`  
**Controller**: `SysMenuController.java`

| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| GET | `/treeselect` | 菜单树 | `system:menu:list` |
| GET | `/{menuId}` | 菜单详情 | `system:menu:query` |
| POST | `/` | 新增菜单 | `system:menu:add` |
| PUT | `/{menuId}` | 修改菜单 | `system:menu:edit` |
| DELETE | `/{menuIds}` | 删除菜单 | `system:menu:remove` |

---

#### 3.1.4 部门管理

**基础路径**: `/system/dept`  
**Controller**: `SysDeptController.java`

| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| GET | `/treeselect` | 部门树 | `system:dept:list` |
| GET | `/{deptId}` | 部门详情 | `system:dept:query` |
| POST | `/` | 新增部门 | `system:dept:add` |
| PUT | `/{deptId}` | 修改部门 | `system:dept:edit` |
| DELETE | `/{deptIds}` | 删除部门 | `system:dept:remove` |

---

#### 3.1.5 岗位管理

**基础路径**: `/system/post`  
**Controller**: `SysPostController.java`

| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| GET | `/list` | 岗位列表 | `system:post:list` |
| GET | `/{postId}` | 岗位详情 | `system:post:query` |
| POST | `/` | 新增岗位 | `system:post:add` |
| PUT | `/{postId}` | 修改岗位 | `system:post:edit` |
| DELETE | `/{postIds}` | 删除岗位 | `system:post:remove` |

---

#### 3.1.6 字典管理

**基础路径**: `/system/dict`

**字典类型**: `/type`
| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| GET | `/list` | 字典类型列表 | `system:dict:list` |
| GET | `/{dictId}` | 字典类型详情 | `system:dict:query` |
| POST | `/` | 新增字典类型 | `system:dict:add` |
| PUT | `/{dictId}` | 修改字典类型 | `system:dict:edit` |
| DELETE | `/{dictIds}` | 删除字典类型 | `system:dict:remove` |

**字典数据**: `/data`
| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| GET | `/list` | 字典数据列表 | `system:dict:list` |
| GET | `/{dictCode}` | 字典数据详情 | `system:dict:query` |
| POST | `/` | 新增字典数据 | `system:dict:add` |
| PUT | `/{dictCode}` | 修改字典数据 | `system:dict:edit` |
| DELETE | `/{dictCodes}` | 删除字典数据 | `system:dict:remove` |

---

#### 3.1.7 参数配置

**基础路径**: `/system/config`  
**Controller**: `SysConfigController.java`

| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| GET | `/list` | 参数列表 | `system:config:list` |
| GET | `/{configId}` | 参数详情 | `system:config:query` |
| POST | `/` | 新增参数 | `system:config:add` |
| PUT | `/{configId}` | 修改参数 | `system:config:edit` |
| DELETE | `/{configIds}` | 删除参数 | `system:config:remove` |

---

#### 3.1.8 通知公告

**基础路径**: `/system/notice`  
**Controller**: `SysNoticeController.java`

| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| GET | `/list` | 公告列表 | `system:notice:list` |
| GET | `/{noticeId}` | 公告详情 | `system:notice:query` |
| POST | `/` | 新增公告 | `system:notice:add` |
| PUT | `/{noticeId}` | 修改公告 | `system:notice:edit` |
| DELETE | `/{noticeIds}` | 删除公告 | `system:notice:remove` |

---

#### 3.1.9 OSS 配置

**基础路径**: `/system/oss`

**OSS 对象**: `/oss`
| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| GET | `/list` | OSS 对象列表 | `tool:oss:list` |
| POST | `/upload` | 上传文件 | - |
| DELETE | `/{ids}` | 删除 OSS 对象 | `tool:oss:remove` |

**OSS 配置**: `/config`
| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| GET | `/list` | OSS 配置列表 | `tool:oss:list` |
| POST | `/save` | 保存 OSS 配置 | `tool:oss:edit` |

---

### 3.2 认证授权

#### 3.2.1 登录

**基础路径**: `/system/login`  
**Controller**: `SysLoginController.java`

| 方法 | 路径 | 功能 | 参数 |
|------|------|------|------|
| POST | `/login` | 用户登录 | `username`, `password`, `code`, `uuid` |
| POST | `/logout` | 用户退出 | - |

---

#### 3.2.2 注册

**基础路径**: `/system/register`  
**Controller**: `SysRegisterController.java`

| 方法 | 路径 | 功能 | 参数 |
|------|------|------|------|
| POST | `/register` | 用户注册 | `username`, `password` |

---

#### 3.2.3 验证码

**基础路径**: `/captcha`  
**Controller**: `CaptchaController.java`

| 方法 | 路径 | 功能 | 参数 |
|------|------|------|------|
| GET | `/captchaImage` | 图形验证码 | - |

---

#### 3.2.4 个人信息

**基础路径**: `/system/user/profile`  
**Controller**: `SysProfileController.java`

| 方法 | 路径 | 功能 | 参数 |
|------|------|------|------|
| GET | `/` | 获取个人信息 | - |
| PUT | `/` | 修改个人信息 | `SysUser` |
| PUT | `/updatePwd` | 修改密码 | `oldPassword`, `newPassword` |
| POST | `/avatar` | 上传头像 | `MultipartFile` |

---

### 3.3 系统监控

#### 3.3.1 登录日志

**基础路径**: `/monitor/logininfor`  
**Controller**: `SysLogininforController.java`

| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| GET | `/list` | 登录日志列表 | `system:logininfor:list` |
| DELETE | `/{infoIds}` | 删除登录日志 | `system:logininfor:remove` |
| POST | `/unlock` | 解锁账户 | `system:logininfor:unlock` |

---

#### 3.3.2 操作日志

**基础路径**: `/monitor/operlog`  
**Controller**: `SysOperlogController.java`

| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| GET | `/list` | 操作日志列表 | `system:operlog:list` |
| GET | `/{operId}` | 操作日志详情 | `system:operlog:query` |
| DELETE | `/{operIds}` | 删除操作日志 | `system:operlog:remove` |

---

#### 3.3.3 在线用户

**基础路径**: `/monitor/online`  
**Controller**: `SysUserOnlineController.java`

| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| GET | `/list` | 在线用户列表 | `system:online:list` |
| POST | `/batchLogout` | 强退用户 | `system:online:force` |

---

#### 3.3.4 缓存监控

**基础路径**: `/monitor/cache`  
**Controller**: `CacheController.java`

| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| GET | `/` | 缓存信息 | `system:cache:list` |
| GET | `/getNames` | 缓存名称列表 | - |
| GET | `/getKeys` | 缓存键名列表 | `cacheName` |
| GET | `/getValue` | 缓存内容 | `cacheName`, `cacheKey` |
| DELETE | `/clearCacheName` | 清理缓存 | `cacheName` |
| DELETE | `/clearCacheAll` | 清理全部缓存 | - |

---

## 四、代码生成接口（ruoyi-generator）

### 4.1 代码生成

**基础路径**: `/tool/gen`  
**Controller**: `GenController.java`

| 方法 | 路径 | 功能 | 权限 | 参数 |
|------|------|------|------|------|
| GET | `/list` | 表列表 | `tool:gen:list` | `PageQuery` |
| GET | `/db/list` | 数据库表列表 | `tool:gen:list` | `PageQuery` |
| GET | `/{tableId}` | 表详情 | `tool:gen:query` | `tableId` |
| PUT | `/{tableId}` | 修改表配置 | `tool:gen:edit` | `GenTable` |
| DELETE | `/{tableIds}` | 删除表 | `tool:gen:remove` | `tableIds` |
| POST | `/importTable` | 导入表 | `tool:gen:import` | `tables` |
| POST | `/preview` | 预览代码 | `tool:gen:preview` | `tableId` |
| GET | `/download` | 下载代码 | `tool:gen:code` | `tableName` |
| POST | `/batchGenCode` | 批量生成代码 | `tool:gen:code` | `tableNames` |

---

## 五、接口统计

### 5.1 Controller 数量统计

| 模块 | Controller 数量 | 接口数量估算 |
|------|----------------|-------------|
| **业务接口** | 4 | ~25 |
| - AI Bot | 1 | 3 |
| - Dify | 1 | 2 |
| - 飞书 | 1 | 3 |
| - 金蝶字典 | 1 | 17 |
| **金蝶 K3 集成** | 18 | ~90 |
| - 基础数据 | 4 | ~25 |
| - 采购管理 | 3 | ~15 |
| - 销售管理 | 2 | ~12 |
| - 库存管理 | 3 | ~8 |
| - 财务管理 | 3 | ~10 |
| - 系统配置 | 3 | ~20 |
| **RuoYi 系统** | 20 | ~80 |
| - 系统管理 | 9 | ~45 |
| - 认证授权 | 4 | ~10 |
| - 系统监控 | 4 | ~15 |
| - 其他 | 3 | ~10 |
| **代码生成** | 1 | ~9 |
| **总计** | **43** | **~204** |

---

### 5.2 技术栈分析

#### 框架依赖

- **Spring Boot**: 3.x
- **MyBatis-Plus**: ORM 框架
- **Sa-Token**: 权限认证
- **Lombok**: 代码简化
- **Hutool**: 工具类库

#### 数据格式

- **请求**: JSON (`application/json`)
- **响应**: `Result<T>`, `R<T>`, `TableDataInfo`
- **文件上传**: `multipart/form-data`

#### 安全控制

- **权限注解**: `@SaCheckPermission("xxx:xxx:xxx")`
- **事务管理**: `@Transactional(rollbackFor = Exception.class)`
- **参数校验**: `@Validated`

---

### 5.3 接口类型分布

| 接口类型 | 数量占比 | 说明 |
|---------|---------|------|
| RESTful GET | ~40% | 查询操作 |
| RESTful POST | ~35% | 新增、同步、复杂查询 |
| RESTful PUT | ~15% | 修改操作 |
| RESTful DELETE | ~10% | 删除操作 |

---

## 六、特殊说明

### 6.1 低代码引擎对比

**低代码引擎 API** (`/api/erp/*`):
- 动态配置驱动
- 通用 CRUD 接口
- 运行时解析执行

**传统后端 API** (本文档):
- 硬编码业务逻辑
- 固定接口定义
- 编译时确定

### 6.2 数据同步策略

**金蝶 K3 同步**:
- 多线程并发同步
- 主从表分别同步
- 事务保证一致性

**示例**:
```java
@PostMapping("/sync")
@Transactional(rollbackFor = Exception.class)
public Result sync() {
    int main = syncMainData();      // 主表
    int bank = syncBankInfo();      // 银行
    int contact = syncContacts();   // 联系人
    return Result.success(String.format(
        "同步完成：主表%d条，银行%d条，联系人%d条",
        main, bank, contact
    ));
}
```

### 6.3 文件上传规范

**统一使用 MinIO**:
```java
@Autowired
private MinioUtil minioUtil;

@PostMapping("/save")
public Result save(@RequestPart("file") MultipartFile file) {
    String url = minioUtil.upload(file);
    // ...
}
```

---

## 七、附录

### 7.1 通用响应格式

**成功响应**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

**失败响应**:
```json
{
  "code": 500,
  "msg": "错误信息",
  "data": null
}
```

**分页响应**:
```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [],
  "total": 100
}
```

---

### 7.2 权限命名规范

**格式**: `{模块}:{功能}:{操作}`

**示例**:
- `system:user:list` - 系统管理 - 用户 - 查询
- `k3:supplier:save` - K3 集成 - 供应商 - 新增
- `tool:gen:code` - 工具 - 生成器 - 生成代码

---

### 7.3 异常处理

**统一异常处理**:
- 业务异常：返回错误码和消息
- 系统异常：记录日志并返回通用错误
- 网络异常：返回连接失败提示

---

**文档结束**

