# Test 数据库完整解析文档

## 📊 数据库概览

### 基本信息
- **数据库名称**: test
- **数据库类型**: MySQL
- **主机地址**: 118.178.144.159:3307
- **字符集**: UTF8MB4
- **时区**: GMT+8 (东八区)
- **SSL 加密**: 已启用

### 数据统计
- **表总数**: 73 张
- **总数据量**: 约 18 万 + 条记录
- **主要业务**: WMS 仓储管理 + 销售采购 + 财务系统

---

## 📋 表分类汇总

### 一、核心业务表（按数据量排序）

#### 1. 销售订单模块 🔥

##### t_sale_order_entry (销售订单明细表)
- **数据量**: 30,938 条
- **数据大小**: 19.56 MB
- **索引大小**: 7.58 MB
- **主键**: fentryid (bigint, 自增)
- **外键**: fbillno (关联主表)

**核心字段**:
| 字段名 | 类型 | 说明 |
|--------|------|------|
| fentryid | bigint | 明细 ID（主键） |
| fbillno | varchar(255) | 销售订单号 |
| fplanmaterialid | varchar(50) | 计划物料 ID |
| fplanmaterialname | varchar(100) | 物料名称 |
| fqty | decimal(18,6) | 数量 |
| fprice | decimal(18,6) | 单价 |
| ftaxprice | decimal(18,6) | 含税单价 |
| fallamount | decimal(18,2) | 金额合计 |
| fdeliqty | decimal(18,4) | 已交付数量 |
| f_mz | decimal(18,4) | 毛重 |
| f_jz | decimal(18,4) | 净重 |
| f_kpdj | decimal(18,4) | 开票单价 |
| f_ygcb | decimal(18,2) | 预估成本 |
| f_hsbm | varchar(50) | 海关编码 |
| f_cplb | varchar(50) | 产品类别 |

**业务字段扩展** (金蝶 K/3 Cloud 集成):
- f_ora_* 系列字段：金蝶 ERP 同步字段
- f_cty_* 系列字段：自定义扩展字段
- f_gdtp1/2: 固定图片路径
- f_bzfs: 包装方式
- f_tsyq: 特殊要求

##### t_sale_order (销售订单主表)
- **数据量**: 3,183 条 (实际 3,032 条有效)
- **数据大小**: 1.52 MB
- **索引大小**: 1.08 MB
- **主键**: id (bigint, 自增)
- **唯一键**: FID (金蝶 ID)

**核心字段**:
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 ID |
| FID | bigint | 金蝶系统 ID（唯一） |
| Document_type | varchar(255) | 单据类型 |
| FBillNo | varchar(50) | 销售订单号 |
| orderStatus | varchar(255) | 订单状态 |
| FDate | date | 订单日期 |
| FCustId | varchar(50) | 客户 ID |
| F_khhth | varchar(100) | 客户合同号 |
| FSettleCurrId | varchar(50) | 结算币种 |
| FSalerId | varchar(50) | 销售员 ID |
| FIsIncludedTax | tinyint | 是否含税 (1=是) |
| F_shhl | decimal(10,6) | 收货含量/比例 |
| F_shzt | tinyint | 收货状态 |
| F_shje | decimal(18,2) | 收货金额 |
| FBillTaxAmount | decimal(18,2) | 税额 |
| FBillAmount | decimal(18,2) | 订单金额 |
| FExchangeRate | decimal(10,6) | 汇率 |

**金蝶集成字段**:
- F_ora_BaseProperty: 基础属性
- F_tcbl: 提成比例
- F_lrl / F_jlrl: 利润率相关
- F_gj: 国家
- F_myfs: 贸易方式
- F_yhzh: 银行账号
- F_sygs: 所属公司

##### t_sale_order_cost (销售订单成本预估表)
- **数据量**: 3,099 条
- **用途**: 订单成本核算和利润分析

---

#### 2. 采购管理模块 📦

##### purchase_instock_entry (采购入库单明细表)
- **数据量**: 29,814 条
- **数据大小**: 21.56 MB
- **索引大小**: 6.06 MB
- **关联主表**: purchase_instock

##### purchase_instock (采购入库单主表)
- **数据量**: 12,613 条
- **数据大小**: 3.52 MB
- **索引大小**: 0.97 MB

##### receive_notice_entry (收料通知单明细表)
- **数据量**: 16,130 条
- **数据大小**: 7.52 MB

##### receive_notice (收料通知单主表)
- **数据量**: 6,237 条

##### purchase_quotation_entry (采购报价单明细表)
- **数据量**: 5,248 条

##### purchase_quotation (采购报价单主表)
- **数据量**: 2,311 条

##### po_order_bill_head_entry (采购订单明细表)
- **数据量**: 3,603 条

##### price_list_entry (采购价目表明细表)
- **数据量**: 5,830 条

---

#### 3. 销售与价格管理 💰

##### t_sales_price_item_package (销售价目明细 - 包装物料表)
- **数据量**: 10,654 条
- **数据大小**: 1.52 MB

##### t_sales_price_item (销售价目明细表)
- **数据量**: 9,821 条

##### delivery_notice_entry (发货通知单明细表)
- **数据量**: 19,920 条
- **数据大小**: 15.55 MB

##### delivery_notice (发货通知单主表)
- **数据量**: 2,199 条

##### sale_change_bill (销售变更单 - 表头)
##### sale_change_detail (销售变更单 - 明细)

---

#### 4. 基础资料模块 🏢

##### by_material (金蝶物料信息表)
- **数据量**: 12,078 条
- **数据大小**: 4.52 MB
- **用途**: 从金蝶 K/3 Cloud 同步的物料主数据

**核心字段** (推测):
- 物料编码、物料名称、规格型号
- 计量单位、物料属性
- 金蝶同步标识字段

##### bd_customer (客户主数据表)
- **数据量**: 807 条
- **数据大小**: 约 1 MB+
- **主键**: Id (自增)
- **业务键**: fcustid (金蝶客户 ID)

**核心字段**:
| 字段名 | 类型 | 说明 |
|--------|------|------|
| fcustid | bigint | 金蝶客户 ID |
| fnumber | varchar(50) | 客户编码 |
| fname | varchar(200) | 客户名称 |
| fshort_name | varchar(200) | 客户简称 |
| fdocumentStatus | varchar(255) | 单据状态 |
| fseller | bigint | 销售员 ID |
| faddress | varchar(500) | 地址 |
| ftel | varchar(255) | 电话 |
| fcreate_org_id | bigint | 创建组织 ID |
| finvoice_type | varchar(255) | 发票类型 |
| ftax_rate | decimal(10,2) | 税率 |

**社交媒体字段** (跨境电商特性):
- f_youtube, f_linkedin, f_facebook
- f_twitter, f_instagram, f_vk
- f_wechat, f_qq, f_whatsapp
- 等 20+ 个社交媒体联系字段

##### supplier (供应商信息表)
- **数据量**: 1,141 条
- **主键**: id (自增)

**核心字段**:
| 字段名 | 类型 | 说明 |
|--------|------|------|
| number | varchar(50) | 供应商编码 |
| name | varchar(100) | 供应商名称 |
| abbreviation | varchar(100) | 简称 |
| legal_person | varchar(100) | 法人 |
| establish_date | date | 成立日期 |
| social_credit_code | varchar(100) | 统一社会信用代码 |
| settlement_method | varchar(100) | 结算方式 |
| invoice_type | varchar(255) | 发票类型 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |

**资质认证字段**:
- factory_certification: 工厂认证
- quality_control: 质量控制
- export_market: 出口市场

##### supplier_contact (联系人信息表)
- **数据量**: 4,824 条

---

#### 5. 仓储管理模块 🏭

##### wms_inventory (库存表)
- **主键**: id (bigint, 自增)
- **核心字段**:
  - sku_id: 商品 SKU ID
  - warehouse_id: 仓库 ID
  - quantity: 库存数量 (decimal 20,2)
  - remark: 备注

##### wms_inventory_history (库存历史记录表)
- **用途**: 记录所有库存变动流水

##### wms_check_order (库存盘点单据)
##### wms_check_order_detail (库存盘点详情)

##### wms_warehouse (仓库表)
- **主键**: id (bigint, 自增)
- **字段**:
  - warehouse_code: 仓库编码
  - warehouse_name: 仓库名称
  - order_num: 排序号

##### warehouse_location (仓库仓位信息表)

##### wms_movement_order (移库单)
##### wms_movement_order_detail (库存移动详情)

##### wms_receipt_order (入库单)
##### wms_receipt_order_detail (入库单详情)

##### wms_shipment_order_detail (出库单详情)

##### receipt_notice_full (检验单表)
- **数据量**: 4,769 条

---

#### 6. 系统管理模块 ⚙️

##### sys_user (用户信息表)
- **数据量**: 78 人
- **主键**: user_id (bigint)

**核心字段**:
| 字段名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| user_id | bigint | - | 用户 ID |
| dept_id | bigint | - | 部门 ID |
| user_name | varchar(30) | - | 账号 |
| nick_name | varchar(30) | - | 昵称 |
| user_type | varchar(10) | sys_user | 用户类型 |
| email | varchar(50) | - | 邮箱 |
| phonenumber | varchar(11) | - | 手机号 |
| sex | char(1) | 0 | 性别 |
| password | varchar(100) | - | 密码 (BCrypt 加密) |
| status | char(1) | 0 | 状态 (0 正常 1 停用) |
| del_flag | char(1) | 0 | 删除标志 |
| login_ip | varchar(128) | - | 最后登录 IP |
| login_date | datetime | - | 最后登录时间 |
| staff_id | varchar(255) | - | 员工 ID |
| k3_key | varchar(255) | - | 金蝶 K3 关联键 |

**示例数据**:
```sql
user_id=1, user_name='admin', nick_name='周艳红'
user_id=100045, user_name='罗元标'
user_id=100055, user_name='戴幸奇'
```

##### sys_menu (菜单权限表)
- **字段**: menu_id, menu_name, parent_id, path, component, perms, icon 等
- **用途**: 系统菜单和按钮权限配置

##### sys_role (角色信息表)
##### sys_dept (部门表)
##### sys_post (岗位信息表)

##### sys_user_role (用户角色关联表)
##### sys_role_menu (角色菜单关联表)
##### sys_role_dept (角色部门关联表)
##### sys_user_post (用户岗位关联表)

##### sys_dict_data (字典数据表)
##### sys_dict_type (字典类型表)

##### sys_config (参数配置表)

##### sys_oss (OSS 对象存储表)
##### sys_oss_config (对象存储配置表)

##### sys_logininfor (系统访问记录)
##### sys_oper_log (操作日志记录)

##### sys_employee (员工信息表)
##### hr_employee_follow_up (员工人事跟进融合表)

---

#### 7. 审计与日志模块 📝

##### sys_data_audit_log (审计日志表)
- **数据量**: 7,120 条
- **数据大小**: 1.52 MB
- **用途**: 记录用户修改痕迹，满足合规要求

---

#### 8. 财务管理模块 💵

##### financial_information (银行信息表)
- **数据量**: 2,039 条
- **用途**: 银行账户和结算信息管理

##### settlement_method (结算方式表)
##### tax_rate (税率列表表)

##### tmp_report_data_income (临时报表数据 - 收入相关)

---

#### 9. 代码生成模块 🔧

##### gen_table (代码生成业务表)
##### gen_table_column (代码生成业务表字段)
- **用途**: RuoYi 代码生成器配置表

---

#### 10. 其他业务表

##### inquiry_order (询价单主表)
##### inquiry_order_entry (询价单明细表)

##### customer_transfer (客户转让列表)

##### supplier_visit_record (供应商回访记录表)
##### supplier_visit_photo (供应商回访照片表)

##### dictionary_table (字典表)
##### bymaterial_dictionary (物料字典表)

##### country (国家基础资料表)
##### wms_item_brand (商品品牌表)
##### wms_item_sku (SKU 信息表)
##### wms_merchant (往来单位)

##### onl_drag_table_relation (仪表盘聚合表)

##### supplier_groups (供应商分组字典表)
##### supplier_encoding (供应商信息表)

---

## 🔗 表关系分析

### 销售订单关联关系
```
t_sale_order (主表)
    └── t_sale_order_entry (明细表 1:N)
    └── t_sale_order_cost (成本表 1:1)
    └── bd_customer (客户 N:1)
    └── sys_user (销售员 N:1)
```

### 采购订单关联关系
```
po_order_bill_head (采购订单主表)
    └── po_order_bill_head_entry (明细表 1:N)
    
purchase_instock (入库主表)
    └── purchase_instock_entry (明细表 1:N)
    
receive_notice (收料通知主表)
    └── receive_notice_entry (明细表 1:N)
```

### 仓储管理关联关系
```
wms_warehouse (仓库)
    └── warehouse_location (仓位 1:N)
    └── wms_inventory (库存 1:N)
        └── wms_inventory_history (历史 1:N)
```

### 基础资料关联关系
```
supplier (供应商)
    └── supplier_contact (联系人 1:N)
    
bd_customer (客户)
    └── customer_transfer (转让记录 1:N)
```

---

## 📊 数据量 TOP20 表排名

| 排名 | 表名 | 中文名 | 数据量 | 数据大小 | 索引大小 |
|------|------|--------|--------|----------|----------|
| 1 | t_sale_order_entry | 销售订单明细 | 30,938 | 19.56 MB | 7.58 MB |
| 2 | purchase_instock_entry | 采购入库明细 | 29,814 | 21.56 MB | 6.06 MB |
| 3 | delivery_notice_entry | 发货通知明细 | 19,920 | 15.55 MB | 3.91 MB |
| 4 | receive_notice_entry | 收料通知明细 | 16,130 | 7.52 MB | 2.00 MB |
| 5 | purchase_instock | 采购入库主表 | 12,613 | 3.52 MB | 0.97 MB |
| 6 | by_material | 金蝶物料 | 12,078 | 4.52 MB | - |
| 7 | t_sales_price_item_package | 销售价目包装 | 10,654 | 1.52 MB | - |
| 8 | t_sales_price_item | 销售价目明细 | 9,821 | 2.52 MB | - |
| 9 | sys_data_audit_log | 审计日志 | 7,120 | 1.52 MB | - |
| 10 | receive_notice | 收料通知主表 | 6,237 | 2.52 MB | 0.25 MB |
| 11 | price_list_entry | 采购价目明细 | 5,830 | 2.52 MB | 0.23 MB |
| 12 | purchase_quotation_entry | 采购报价明细 | 5,248 | 3.52 MB | 0.67 MB |
| 13 | supplier_contact | 供应商联系人 | 4,824 | 1.52 MB | 0.16 MB |
| 14 | receipt_notice_full | 检验单 | 4,769 | 2.52 MB | 1.09 MB |
| 15 | po_order_bill_head_entry | 采购订单明细 | 3,603 | 1.52 MB | 0.11 MB |
| 16 | t_sale_order_cost | 销售订单成本 | 3,099 | 1.52 MB | - |
| 17 | t_sale_order | 销售订单主表 | 3,032 | 1.52 MB | 1.08 MB |
| 18 | purchase_quotation | 采购报价主表 | 2,311 | 0.47 MB | 0.33 MB |
| 19 | delivery_notice | 发货通知主表 | 2,199 | 1.52 MB | 0.37 MB |
| 20 | financial_information | 银行信息 | 2,039 | 0.27 MB | 0.08 MB |

---

## 🎯 业务领域划分

### 跨境电商 ERP 特性

#### 1. 国际贸易支持
- **多币种结算**: FSettleCurrId, FExchangeRate
- **多语言支持**: 英文字段命名 + 中文注释
- **海关编码**: f_hsbm (HS Code)
- **国家地区**: F_gj, country 表

#### 2. 税务管理
- **含税价格**: FIsIncludedTax, ftaxprice
- **税率计算**: tax_rate, ftax_rate
- **发票管理**: finvoice_type

#### 3. 物流跟踪
- **收货状态**: F_shzt, F_shhl
- **发货通知**: delivery_notice 系列
- **重量信息**: f_mz (毛重), f_jz (净重)

#### 4. 成本控制
- **成本预估**: t_sale_order_cost
- **利润核算**: F_lrl (利润率), F_ygcb (预估成本)

---

## 🔐 数据安全性

### 敏感数据加密
- **密码加密**: BCrypt ($2a$10$...)
- **逻辑删除**: del_flag 字段
- **审计日志**: sys_data_audit_log 记录所有修改

### 权限控制
- **用户角色**: sys_user_role
- **菜单权限**: sys_role_menu
- **数据权限**: sys_role_dept

---

## 📈 性能优化建议

### 1. 大表优化
**问题表**: 
- t_sale_order_entry (3 万+)
- purchase_instock_entry (3 万-)

**建议**:
- ✅ 已有索引：fbillno, fplanmaterialid 等
- ✅ 考虑分区：按日期或订单号范围分区
- ⚠️ 定期归档：历史订单数据归档

### 2. 索引优化
**已观察到的索引**:
- FBillNo (销售订单号) - 复合索引
- FCustId (客户 ID) - 外键索引
- FSalerId (销售员) - 查询优化
- k3_key (金蝶关联键) - 集成查询

### 3. 查询优化
```sql
-- 推荐：使用覆盖索引
SELECT fentryid, fqty, fprice 
FROM t_sale_order_entry 
WHERE fbillno = 'SA190102';

-- 避免：全表扫描
SELECT * FROM t_sale_order_entry 
WHERE f_ora_text LIKE '%keyword%';
```

---

## 🔄 金蝶 K/3 Cloud 集成

### 同步字段规范

#### 前缀规则
- **F_**: 金蝶标准字段 (如 FBillNo, FCustId)
- **f_ora_**: 金蝶 ERP 扩展字段
- **f_cty_**: 自定义扩展字段
- **f_**: 系统自有字段

#### 数据流向
```
金蝶 K/3 Cloud → 中间数据库 (test)
    ↓
WMS 系统 → 业务处理
    ↓
回写金蝶 → 更新状态
```

### 关键集成点
1. **物料同步**: by_material 表
2. **客户同步**: bd_customer 表 (fcustid 关联)
3. **订单同步**: t_sale_order (FID 为金蝶 ID)
4. **员工同步**: sys_user (k3_key 关联)

---

## 📝 字段命名规范

### 匈牙利命名法
- **F**: 金蝶标准字段前缀
- **f**: 一般字段前缀
- **t_**: 表前缀 (临时表)

### 缩写规则
- **qty**: quantity (数量)
- **amt**: amount (金额)
- **tax**: taxation (税务)
- **cust**: customer (客户)
- **sal**: sales (销售)
- **pur**: purchase (采购)
- **whs**: warehouse (仓库)
- **sku**: Stock Keeping Unit (库存单位)

### 中英文对照
| 英文 | 中文 | 字段示例 |
|------|------|----------|
| Customer | 客户 | FCustId, bd_customer |
| Supplier | 供应商 | supplier, supplier_contact |
| Order | 订单 | t_sale_order, po_order |
| Inventory | 库存 | wms_inventory |
| Warehouse | 仓库 | wms_warehouse |
| Price | 价格 | fprice, t_sales_price |

---

## 🛠️ 维护建议

### 1. 数据备份策略
```bash
# 每日增量备份
mysqldump --single-transaction test > test_$(date +%Y%m%d).sql

# 每周全量备份
mysqldump --all-databases > full_backup_$(date +%Y%m%d).sql
```

### 2. 数据清理
- **sys_logininfor**: 保留最近 6 个月
- **sys_oper_log**: 保留最近 1 年
- **sys_data_audit_log**: 永久保存（合规要求）
- **wms_inventory_history**: 保留最近 2 年

### 3. 监控指标
- 连接数监控 (maxPoolSize: 20)
- 慢查询日志 (p6spy 已开启)
- 表空间增长趋势
- 索引碎片率

---

## 📞 技术支持

### 数据库管理员
- **负责人**: 系统管理员
- **联系方式**: zccbbg@qq.com
- **文档版本**: v1.0
- **更新时间**: 2026-03-19

### 相关文档
- [数据库配置说明](./数据库配置说明.md)
- HikariCP 连接池配置文档
- MyBatis-Plus 映射配置
- 金蝶 K/3 Cloud 集成接口文档

---

## 附录：完整表清单（73 张）

### A-C
1. bd_customer - 客户主数据表
2. by_material - 金蝶物料信息表
3. bymaterial_dictionary - 物料字典表
4. country - 国家基础资料表
5. customer_transfer - 客户转让列表

### D-F
6. delivery_notice - 发货通知单主表
7. delivery_notice_entry - 发货通知单明细表
8. dictionary_table - 字典表
9. financial_information - 银行信息表

### G-H
10. gen_table - 代码生成业务表
11. gen_table_column - 代码生成业务表字段
12. hr_employee_follow_up - 员工人事跟进融合表

### I-O
13. inquiry_order - 询价单主表
14. inquiry_order_entry - 询价单明细表
15. onl_drag_table_relation - 仪表盘聚合表
16. po_order_bill_head - 采购订单主表
17. po_order_bill_head_entry - 采购订单明细表
18. price_list - 采购价目表主表
19. price_list_entry - 采购价目表明细表
20. purchase_instock - 采购入库单主表
21. purchase_instock_entry - 采购入库单明细表
22. purchase_quotation - 采购报价单主表
23. purchase_quotation_entry - 采购报价单明细表

### P-R
24. receipt_notice_full - 检验单表
25. receive_notice - 收料通知单主表
26. receive_notice_entry - 收料通知单明细表

### S
27. sale_change_bill - 销售变更单 - 表头
28. sale_change_detail - 销售变更单 - 明细
29. settlement_method - 结算方式表
30. supplier - 供应商信息表
31. supplier_contact - 联系人信息表
32. supplier_encoding - 供应商信息表
33. supplier_groups - 供应商分组字典表
34. supplier_visit_photo - 供应商回访照片表
35. supplier_visit_record - 供应商回访记录表
36. sys_config - 参数配置表
37. sys_data_audit_log - 审计日志表
38. sys_dept - 部门表
39. sys_dict_data - 字典数据表
40. sys_dict_type - 字典类型表
41. sys_employee - 员工信息表
42. sys_logininfor - 系统访问记录
43. sys_menu - 菜单权限表
44. sys_notice - 通知公告表
45. sys_oper_log - 操作日志记录
46. sys_oss - OSS 对象存储表
47. sys_oss_config - 对象存储配置表
48. sys_post - 岗位信息表
49. sys_role - 角色信息表
50. sys_role_dept - 角色和部门关联表
51. sys_role_menu - 角色和菜单关联表
52. sys_user - 用户信息表
53. sys_user_post - 用户与岗位关联表
54. sys_user_role - 用户和角色关联表

### T
55. t_sale_order - 销售订单主表
56. t_sale_order_cost - 销售订单成本预估表
57. t_sale_order_entry - 销售订单明细表
58. t_sales_price - 销售价目表主表
59. t_sales_price_item - 销售价目明细表
60. t_sales_price_item_package - 销售价目明细 - 包装物料表
61. tax_rate - 税率列表表
62. tmp_report_data_income - 临时报表数据

### W
63. warehouse_location - 仓库仓位信息表
64. wms_check_order - 库存盘点单据
65. wms_check_order_copy1 - 库存盘点单据 (副本)
66. wms_check_order_detail - 库存盘点单据详情
67. wms_inventory - 库存表
68. wms_inventory_history - 库存记录
69. wms_item_brand - 商品品牌表
70. wms_item_sku - sku 信息
71. wms_merchant - 往来单位
72. wms_movement_order - 移库单
73. wms_movement_order_detail - 库存移动详情
74. wms_receipt_order - 入库单
75. wms_receipt_order_detail - 入库单详情
76. wms_shipment_order_detail - 出库单详情
77. wms_warehouse - 仓库

---

**文档结束**
