# ERP页面配置构建方案

## 文档说明
- 编制日期：2026-03-31
- 编制目的：指导收款单、付款申请单、采购订单、收料通知单、检验单、采购入库单、采购报价单等7个业务单据的页面配置开发
- 技术栈：Spring Boot 3.x + Vue3 + Element Plus
- 配置框架：RuoYi-WMS ERP低代码配置系统

---

## 一、配置开发规范

### 1.1 配置结构（9字段JSON）
基于销售订单配置模板，所有单据配置统一采用9字段JSON结构：

| 字段名 | 说明 | 内容描述 |
|--------|------|----------|
| page_config | 页面配置 | 页面ID、标题、权限、布局、API前缀、主表名 |
| form_config | 表单配置 | 字段定义、组件类型、验证规则、默认值、布局 |
| table_config | 表格配置 | 列定义、宽度、对齐方式、渲染类型、分页 |
| search_config | 搜索配置 | 搜索字段、组件类型、查询操作符、默认值 |
| action_config | 按钮配置 | 工具栏按钮、行操作按钮、权限、确认提示 |
| api_config | API配置 | 基础URL、方法列表、接口描述 |
| dict_config | 字典配置 | 静态/动态/远程字典类型、缓存策略 |
| business_config | 业务配置 | 实体名称、消息提示、业务规则 |
| detail_config | 明细配置 | 明细表、详情展示、标签页配置 |

### 1.2 配置存储规则
- 统一存储位置：`baiyu-web\src\views\erp\脚本库`
- 文件命名：`{单据类型}初始化配置.sql`
- 数据库表：`erp_page_config`
- 版本控制：每次配置变更需递增version字段
- 状态管理：status字段标识配置状态（draft/active/archived）

### 1.3 数据库表结构验证
所有配置开发前必须验证表结构存在性，关键字段包括：
- 主表：必须有主键id、单据编号字段、状态字段、创建时间、创建人
- 明细表：必须有主键id、单据编号外键、物料编码、数量、单价等
- 索引：单据编号字段必须建立唯一索引

---

## 二、单据表结构分析

### 2.1 收款单（f_receivebill + f_receivebill_entry）

**主表：f_receivebill**
```sql
关键字段：
- id: 主键
- FBillNo: 单据编号（业务唯一键）
- FDate: 收款日期
- FBillAmount: 收款金额
- FReceiveBillType: 收款类型
- FSettleCurrId: 结算币别
- FCustId: 客户编码
- FStatus: 状态
- FCreateDate: 创建时间
- FCreatorId: 创建人
```

**明细表：f_receivebill_entry**
```sql
关键字段：
- id: 主键
- FEntryID: 明细ID
- FBillNo: 单据编号（外键）
- FPlanDate: 计划日期
- FPayAmount: 付款金额
- FRemark: 备注
```

**配置要点：**
- page_config: module_code='receivebill', tableName='f_receivebill'
- form_config: 收款日期、收款类型、客户编码、结算币别、收款金额为必填
- table_config: 单据编号、收款日期、客户、收款金额、状态、创建时间
- search_config: 收款日期范围、单据编号、客户、状态
- dict_config: 收款类型、结算币别、客户（动态字典）

---

### 2.2 付款申请单（f_rectunit_type + f_rectunit_detail）

**主表：f_rectunit_type**
```sql
关键字段：
- id: 主键
- FBillNo: 单据编号
- FDate: 申请日期
- FApplyAmount: 申请金额
- FPaymentType: 付款方式
- FSupplierId: 供应商编码
- FStatus: 状态
- FCreateDate: 创建时间
```

**明细表：f_rectunit_detail**
```sql
关键字段：
- id: 主键
- FEntryID: 明细ID
- FBillNo: 单据编号（外键）
- FAccountName: 账户名称
- FAccountNo: 账户号码
- FPayAmount: 付款金额
- FRemark: 备注
```

**配置要点：**
- page_config: module_code='paymentapply', tableName='f_rectunit_type'
- form_config: 申请日期、付款方式、供应商编码、申请金额为必填
- table_config: 单据编号、申请日期、供应商、申请金额、状态
- search_config: 申请日期范围、单据编号、供应商、状态
- dict_config: 付款方式、供应商（动态字典）

---

### 2.3 采购订单（po_order_bill_head + po_order_bill_head_entry）

**主表：po_order_bill_head**
```sql
关键字段：
- id: 主键
- FBillNo: 单据编号
- FDate: 订单日期
- FSupplierId: 供应商编码
- FBillAmount: 订单金额
- FBillTaxAmount: 税额
- FSettleCurrId: 结算币别
- FPurchaseOrgId: 采购组织
- FDocumentStatus: 单据状态
- FCreateDate: 创建时间
```

**明细表：po_order_bill_head_entry**
```sql
关键字段：
- id: 主键
- FEntryID: 明细ID
- FBillNo: 单据编号（外键）
- FMaterialId: 物料编码
- FMaterialName: 物料名称
- FQty: 数量
- FPrice: 单价
- FTaxPrice: 含税单价
- FAmount: 金额
- FDeliDate: 交付日期
```

**配置要点：**
- page_config: module_code='purchaseorder', tableName='po_order_bill_head'
- form_config: 订单日期、供应商编码、结算币别为必填
- table_config: 单据编号、订单日期、供应商、订单金额、单据状态
- search_config: 订单日期范围、单据编号、供应商、单据状态
- dict_config: 结算币别、供应商、采购组织（动态字典）
- detail_config: 明细表配置（物料编码、数量、单价、交付日期）

---

### 2.4 收料通知单（receive_notice + receive_notice_entry）

**主表：receive_notice**
```sql
关键字段：
- id: 主键
- FBillNo: 单据编号
- FDate: 通知日期
- FSupplierId: 供应商编码
- FReceiveType: 收料类型
- FStatus: 状态
- FCreateDate: 创建时间
- FCreatorId: 创建人
```

**明细表：receive_notice_entry**
```sql
关键字段：
- id: 主键
- FEntryID: 明细ID
- FBillNo: 单据编号（外键）
- FMaterialId: 物料编码
- FMaterialName: 物料名称
- FQty: 通知数量
- FActualQty: 实收数量
- FReceiveDate: 收料日期
- FWarehouseId: 仓库编码
```

**配置要点：**
- page_config: module_code='receivenotice', tableName='receive_notice'
- form_config: 通知日期、供应商编码、收料类型为必填
- table_config: 单据编号、通知日期、供应商、收料类型、状态
- search_config: 通知日期范围、单据编号、供应商、状态
- dict_config: 收料类型、供应商、仓库（动态字典）
- detail_config: 明细表配置（物料编码、通知数量、实收数量、收料日期）

---

### 2.5 检验单（receipt_notice_full）

**单表：receipt_notice_full**
```sql
关键字段：
- id: 主键
- FBillNo: 单据编号
- FDate: 检验日期
- FSourceBillNo: 来源单据编号
- FSourceType: 来源类型
- FCheckResult: 检验结果
- FCheckQty: 检验数量
- FPassQty: 合格数量
- FFailQty: 不合格数量
- FInspectorId: 检验员
- FStatus: 状态
- FCreateDate: 创建时间
```

**配置要点：**
- page_config: module_code='qualitycheck', tableName='receipt_notice_full'
- form_config: 检验日期、来源单据编号、检验结果、检验数量为必填
- table_config: 单据编号、检验日期、来源单据、检验结果、检验数量、合格数量、状态
- search_config: 检验日期范围、单据编号、来源单据、检验结果、状态
- dict_config: 来源类型、检验结果、检验员（动态字典）
- detail_config: 无明细表，无需配置

---

### 2.6 采购入库单（purchase_instock + purchase_instock_entry）

**主表：purchase_instock**
```sql
关键字段：
- id: 主键
- FBillNo: 单据编号
- FDate: 入库日期
- FSourceBillNo: 来源单据编号
- FSourceType: 来源类型
- FSupplierId: 供应商编码
- FWarehouseId: 仓库编码
- FInStockQty: 入库数量
- FStatus: 状态
- FCreateDate: 创建时间
```

**明细表：purchase_instock_entry**
```sql
关键字段：
- id: 主键
- FEntryID: 明细ID
- FBillNo: 单据编号（外键）
- FSourceEntryId: 来源明细ID
- FMaterialId: 物料编码
- FMaterialName: 物料名称
- FQty: 入库数量
- FUnitId: 单位
- FWarehouseId: 仓库编码
- FStockId: 库位
```

**配置要点：**
- page_config: module_code='purchaseinstock', tableName='purchase_instock'
- form_config: 入库日期、来源单据编号、供应商编码、仓库编码为必填
- table_config: 单据编号、入库日期、来源单据、供应商、入库数量、状态
- search_config: 入库日期范围、单据编号、来源单据、供应商、状态
- dict_config: 来源类型、供应商、仓库（动态字典）
- detail_config: 明细表配置（物料编码、入库数量、单位、仓库、库位）

---

### 2.7 采购报价单（purchase_quotation + purchase_quotation_entry）

**主表：purchase_quotation**
```sql
关键字段：
- id: 主键
- FBillNo: 单据编号
- FDate: 报价日期
- FSupplierId: 供应商编码
- FValidDate: 有效日期
- FSettleCurrId: 结算币别
- FBillAmount: 报价金额
- FStatus: 状态
- FCreateDate: 创建时间
```

**明细表：purchase_quotation_entry**
```sql
关键字段：
- id: 主键
- FEntryID: 明细ID
- FBillNo: 单据编号（外键）
- FMaterialId: 物料编码
- FMaterialName: 物料名称
- FQty: 数量
- FPrice: 单价
- FAmount: 金额
- FRemark: 备注
```

**配置要点：**
- page_config: module_code='purchasequotation', tableName='purchase_quotation'
- form_config: 报价日期、供应商编码、结算币别、有效日期为必填
- table_config: 单据编号、报价日期、供应商、报价金额、状态、有效日期
- search_config: 报价日期范围、单据编号、供应商、状态
- dict_config: 结算币别、供应商（动态字典）
- detail_config: 明细表配置（物料编码、数量、单价、金额）

---

## 三、配置设计原则

### 3.1 page_config设计
```json
{
  "pageId": "{module_code}",
  "title": "{entityName}管理",
  "permission": "k3:{module_code}:query",
  "layout": "standard",
  "apiPrefix": "/erp/engine",
  "tableName": "{主表名}"
}
```

**命名规范：**
- module_code: 使用英文小写+下划线，如receivebill、paymentapply
- entityName: 中文名称，如收款单、付款申请单
- permission: 权限前缀统一为k3:{module_code}

### 3.2 form_config设计
**必填字段优先级：**
- 第一优先级：单据编号、日期、业务伙伴（客户/供应商）
- 第二优先级：金额字段、币别、状态相关字段
- 第三优先级：辅助信息（联系人、地址、备注）

**组件选择规范：**
- 单据编号：input（自动生成时readonly）
- 日期字段：date（valueFormat: "YYYY-MM-DD"）
- 日期范围：daterange
- 业务伙伴：select（dictionary绑定，filterable）
- 金额字段：input-number（precision: 2）
- 状态字段：select（dictionary绑定，renderType: tag）
- 下拉选项：select（options或dictionary）

### 3.3 table_config设计
**列定义规范：**
- 列宽：单据编号150、日期140、金额140、状态120、其他120-180
- 对齐：金额/数量右对齐，状态居中，文本左对齐
- 固定列：单据编号、操作列fixed: 'left'
- 渲染类型：金额currency、日期date、状态tag、数字number

**默认列排序：**
1. selection
2. expand（有明细时）
3. 单据编号
4. 日期
5. 业务伙伴
6. 金额
7. 状态
8. 创建时间

### 3.4 search_config设计
**默认搜索字段：**
- 日期范围（默认值：["2010-01-01", "today"]）
- 单据编号（right_like模糊查询）
- 业务伙伴（like模糊查询）
- 状态（eq精确查询）

**搜索组件宽度规范：**
- 日期范围：240px
- 单据编号：160px
- 业务伙伴：130px
- 状态：120px

### 3.5 action_config设计
**标准工具栏按钮：**
```json
[
  {"key": "add", "label": "新增", "icon": "Plus", "type": "primary", "position": "left"},
  {"key": "edit", "label": "修改", "icon": "Edit", "type": "success", "position": "left", "disabled": "single"},
  {"key": "delete", "label": "删除", "icon": "Delete", "type": "danger", "position": "left", "disabled": "multiple"},
  {"key": "export", "label": "导出", "icon": "Download", "type": "warning", "position": "left", "disabled": "multiple"}
]
```

**审核类单据额外按钮：**
```json
[
  {"key": "audit", "label": "审核", "icon": "CircleCheck", "type": "success", "position": "left", "disabled": "multiple"},
  {"key": "unAudit", "label": "反审核", "icon": "Close", "type": "warning", "position": "left", "disabled": "multiple"}
]
```

### 3.6 dict_config设计
**字典类型选择：**
- 静态字典：type为static，options直接定义（如是否含税）
- 动态字典：type为dynamic，从数据库表查询（如客户、供应商）
- API字典：type为api，调用字典接口（如币别、销售员）
- 远程字典：type为remote，支持模糊搜索（如国家）

**字典配置模板：**
```json
{
  "customers": {
    "type": "dynamic",
    "table": "bd_customer",
    "conditions": [{"field": "deleted", "operator": "isNull"}],
    "orderBy": [{"field": "fname", "direction": "ASC"}],
    "fieldMapping": {"valueField": "fnumber", "labelField": "fname"},
    "config": {
      "api": "/erp/engine/dict/union/customers",
      "useGlobalCache": true
    }
  }
}
```

### 3.7 detail_config设计
**明细表配置要素：**
- enabled: true（有明细表时启用）
- displayType: drawer（抽屉式详情）或dialog（弹窗式详情）
- tabs: 标签页配置（单明细单标签，多明细多标签）
- dataField: 明细数据字段名
- tableName: 明细表名
- queryConfig: 明细查询配置（默认按单据编号查询）
- table: 明细表格列配置

**明细列配置规范：**
- 物料编码：120px
- 物料名称：180px（showOverflowTooltip）
- 数量：100px（右对齐，precision: 4）
- 单价：100px（右对齐，precision: 8）
- 金额：120px（右对齐，precision: 2）
- 日期：140px

---

## 四、配置生成实施计划

### 4.1 配置生成顺序
```
1. 收款单初始化配置.sql
2. 付款申请单初始化配置.sql
3. 采购订单初始化配置.sql
4. 收料通知单初始化配置.sql
5. 检验单初始化配置.sql
6. 采购入库单初始化配置.sql
7. 采购报价单初始化配置.sql
```

### 4.2 配置生成步骤
**步骤1：创建配置SQL文件**
- 复制销售订单初始化配置.sql为模板
- 修改module_code、config_name、tableName
- 替换entityName、entityNameSingular

**步骤2：配置form_config**
- 根据表结构设计字段定义
- 设置必填规则、验证规则
- 配置组件类型和默认值
- 设置字典绑定

**步骤3：配置table_config**
- 定义表格列（默认列+业务列）
- 设置列宽、对齐方式、渲染类型
- 配置分页参数

**步骤4：配置search_config**
- 设置默认搜索字段
- 配置查询操作符
- 设置默认值

**步骤5：配置action_config**
- 配置标准工具栏按钮
- 设置权限标识
- 配置确认提示

**步骤6：配置api_config**
- 设置baseUrl
- 定义API方法列表

**步骤7：配置dict_config**
- 定义业务字典
- 设置缓存策略
- 配置远程搜索

**步骤8：配置business_config**
- 设置实体名称
- 配置消息提示
- 设置对话框标题

**步骤9：配置detail_config（有明细表时）**
- 配置明细查询
- 定义明细表格列
- 设置标签页

### 4.3 配置验证清单
- [ ] module_code唯一性验证
- [ ] tableName与数据库表名一致
- [ ] permission权限标识已定义
- [ ] form_config必填字段设置正确
- [ ] table_config列定义完整
- [ ] search_config查询字段与表字段匹配
- [ ] dict_config字典数据源存在
- [ ] detail_config明细表配置正确
- [ ] JSON格式正确（无语法错误）
- [ ] version版本号递增

---

## 五、配置SQL生成模板

### 5.1 完整配置SQL结构
```sql
-- ERP 配置 JSON 强制拆分方案 - {单据类型}模块导入 SQL（字典重构版）
-- 版本：v4.0 (9字段拆分 + API配置版)
-- 创建日期：{date}

INSERT INTO `erp_page_config` (
  `module_code`,
  `config_name`,
  `config_type`,
  `page_config`,
  `form_config`,
  `table_config`,
  `search_config`,
  `action_config`,
  `api_config`,
  `dict_config`,
  `business_config`,
  `detail_config`,
  `version`,
  `status`,
  `is_public`,
  `create_by`,
  `remark`
) VALUES (
  '{module_code}',
  '{entityName}管理',
  'PAGE',
  '{page_config_json}',
  '{form_config_json}',
  '{table_config_json}',
  '{search_config_json}',
  '{action_config_json}',
  '{api_config_json}',
  '{dict_config_json}',
  '{business_config_json}',
  '{detail_config_json}',
  '1.0.0',
  'active',
  1,
  'admin',
  '{单据类型}页面配置 - 初始版本'
);
```

### 5.2 JSON格式化规范
- 使用4空格缩进
- 字段名使用双引号
- 字符串值使用双引号
- 数组使用中括号
- 对象使用大括号
- 最后一个元素后不加逗号

---

## 六、注意事项

### 6.1 配置开发注意事项
1. 表结构验证：配置前必须验证表结构，确保字段存在
2. 字典数据：确保字典数据已录入或字典表已初始化
3. 权限配置：确保权限标识在系统权限表中已定义
4. 索引优化：单据编号字段必须建立索引
5. 版本管理：每次配置变更需递增version字段
6. 配置备份：配置更新前先备份现有配置

### 6.2 性能优化建议
1. 字典缓存：启用全局缓存，TTL设置为5分钟
2. 远程搜索：大数据量字典使用远程搜索（防抖300ms）
3. 分页查询：默认每页10条，支持10/20/50/100
4. 懒加载：明细数据使用懒加载策略
5. 查询限制：所有查询必须包含WHERE条件，限制1000条

### 6.3 兼容性保障
1. 向后兼容：配置变更需保持向后兼容
2. 渐进升级：复杂功能拆分多个版本迭代
3. 灰度发布：新配置先在测试环境验证
4. 回滚预案：准备配置回滚SQL脚本

---

## 七、附录

### 7.1 术语表
- ERP: Enterprise Resource Planning（企业资源计划）
- 低代码: 通过配置而非编码快速开发应用的软件开发方法
- 配置驱动: 通过JSON配置定义页面结构和行为
- 动态字典: 从数据库表动态查询的字典数据
- 远程搜索: 支持模糊查询的字典数据源
- 下推: 将上游单据数据推送到下游单据的业务操作
- 审批: 单据提交审核的流程控制

### 7.2 参考文档
- RuoYi-WMS开发规范
- ERP低代码配置系统使用手册
- 销售订单配置模板
- 6大引擎API文档

### 7.3 联系方式
- 技术支持：MM（ERP低代码开发专家）
- 文档维护：RuoYi-WMS开发团队

---

**文档版本：v1.0**
**最后更新：2026-03-31**
