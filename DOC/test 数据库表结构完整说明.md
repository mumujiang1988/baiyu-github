# Test 数据库完整表结构与字段说明

> **数据库**: test  
> **主机**: 118.178.144.159:3307  
> **表总数**: 73 张  
> **更新时间**: 2026-03-19  

---

## 目录

1. [基础资料模块](#一基础资料模块)
2. [销售管理模块](#二销售管理模块)
3. [采购管理模块](#三采购管理模块)
4. [仓储管理模块](#四仓储管理模块)
5. [财务管理模块](#五财务管理模块)
6. [系统管理模块](#六系统管理模块)
7. [其他业务表](#七其他业务表)

---字典表 bymaterial_dictionary  sys_dict_data  sys_dict_data  sys_dict_type

## 一、基础资料模块

### 1. bd_customer - 客户主数据表

**用途**: 存储客户基本信息，与金蝶 K/3 Cloud 同步

| 序号 | 字段名 | 类型 | 空值 | 键 | 默认值 | 说明 |
|------|--------|------|------|-----|--------|------|
| 1 | Id | int | NO | PRI | auto_increment | 主键 ID（自增） |
| 2 | fcustid | bigint | YES | MUL | None | 金蝶客户 ID |
| 3 | fnumber | varchar(50) | YES | MUL | None | 客户编码 |
| 4 | fname | varchar(200) | NO | MUL | None | 客户名称 |
| 5 | fdocumentStatus | varchar(255) | YES | | None | 单据状态 |
| 6 | fshort_name | varchar(200) | YES | | None | 客户简称 |
| 7 | f_khqc | varchar(500) | YES | | None | 客户全称 |
| 8 | fdescription | varchar(500) | YES | | None | 描述 |
| 9 | fcreate_org_id | bigint | YES | | None | 创建组织 ID |
| 10 | fcreator_id | bigint | YES | | None | 创建人 ID |
| 11 | fapperober_date | datetime | YES | | None | 审批日期 |
| 12 | fapprover_id | varchar(255) | YES | | None | 审批人 ID |
| 13 | fmodifier_id | bigint | YES | | None | 修改人 ID |
| 14 | fseller | bigint | YES | MUL | None | 销售员 ID |
| 15 | f_kfxsy1 | varchar(255) | YES | | None | 扩展属性 1 |
| 16 | fsal_dept_id | bigint | YES | | None | 销售部门 ID |
| 17 | fsal_group_id | bigint | YES | | None | 销售组 ID |
| 18 | fcreate_date | datetime | YES | | None | 创建日期 |
| 19 | fmodify_date | datetime | YES | | None | 修改日期 |
| 20 | ffound_date | date | YES | | None | 成立日期 |
| 21 | f_khzrrq | date | YES | | None | 客户准入日期 |
| 22 | f_dygj | varchar(255) | YES | | None | 电源国家 |
| 23 | fcountry | varchar(255) | YES | | None | 国家 |
| 24 | fprovincial | varchar(255) | YES | | None | 省份 |
| 25 | faddress | varchar(500) | YES | | None | 地址 |
| 26 | fregister_address | varchar(500) | YES | | None | 注册地址 |
| 27 | ftel | varchar(255) | YES | | None | 电话 |
| 28 | f_khyx | varchar(100) | YES | | None | 客户邮箱 |
| 29 | fwebsite | varchar(200) | YES | | None | 网站 |
| 30 | ftrading_curr_id | bigint | YES | | None | 交易币种 ID |
| 31 | freceive_curr_id | varchar(255) | YES | | None | 收款币种 ID |
| 32 | fsettle_type_id | bigint | YES | | None | 结算方式 ID |
| 33 | frec_condition_id | bigint | YES | | None | 收款条件 ID |
| 34 | fprice_list_id | bigint | YES | | None | 价格列表 ID |
| 35 | ftax_type | bigint | YES | | None | 税类型 ID |
| 36 | ftax_rate | decimal(10,2) | YES | | None | 税率 |
| 37 | ftax_register_code | varchar(100) | YES | | None | 税务登记码 |
| 38 | fgroup_id | bigint | YES | | None | 分组 ID |
| 39 | f_khly | varchar(255) | YES | | None | 客户来源 |
| 40 | fcustTypeId | varchar(255) | YES | | None | 客户类型 ID |
| 41 | f_ly | varchar(255) | YES | | None | 来源 |
| 42 | f_sylx | varchar(255) | YES | | None | 事业类型 |
| 43 | f_khgm | varchar(1000) | YES | | None | 公司规模 |
| 44 | f_khzy | varchar(1000) | YES | | None | 客户资质 |
| 45 | fis_group | tinyint | YES | | None | 是否集团 |
| 46 | fis_def_payer | tinyint | YES | | None | 是否默认付款人 |
| 47 | flegal_person | varchar(100) | YES | | None | 法人 |
| 48 | finvoice_type | varchar(255) | YES | | None | 发票类型 |
| 49 | fsupplier_id | bigint | YES | | None | 供应商 ID |
| 50 | f_bzyq | text | YES | | None | 包装要求 |
| 51 | fbzfs | varchar(1000) | YES | | None | 包装方式 |
| 52 | f_fhyq | text | YES | | None | 发货要求 |
| 53 | f_zlbzhjsyq | varchar(500) | YES | | None | 质量标准化建议 |
| 54 | f_sfysqs | tinyint | YES | | None | 是否有授权 |
| 55 | fsbsq | tinyint | YES | | None | 是否品牌商 |
| 56 | fsfsd | tinyint | YES | | None | 是否旗舰店 |
| 57 | fsfts | tinyint | YES | | None | 是否实体店 |
| 58 | f_cty_decimal | decimal(10,2) | YES | | None | 自定义金额 |
| 59 | f_tcfpfa | varchar(100) | YES | | None | 配套产品方案 |
| 60 | fpjskzq | int | YES | | None | 平均收款周期 |
| 61 | f_mjll | decimal(10,2) | YES | | None | 毛利润率 |
| 62 | f_cty_large_text | text | YES | | None | 自定义大文本 |
| 63 | f_khlogo | varchar(500) | YES | | None | 客户 Logo |
| 64-88 | **社交媒体字段** | varchar(255) | YES | | None | 包括 YouTube、LinkedIn、Facebook、Twitter、Instagram、VK、微信、QQ、WhatsApp 等 25 个字段 |

**索引**:
- PRIMARY KEY (Id)
- INDEX fcustid (fcustid)
- INDEX fnumber (fnumber)
- INDEX fname (fname)
- INDEX fseller (fseller)

---

### 2. supplier - 供应商信息表

| 序号 | 字段名 | 类型 | 空值 | 键 | 默认值 | 说明 |
|------|--------|------|------|-----|--------|------|
| 1 | id | bigint | NO | PRI | auto_increment | 主键 ID |
| 2 | number | varchar(50) | NO | | None | 供应商编码 |
| 3 | name | varchar(100) | NO | | None | 供应商名称 |
| 4 | abbreviation | varchar(100) | YES | | None | 简称 |
| 5 | nation | varchar(50) | YES | | None | 国家 |
| 6 | region | varchar(100) | YES | | None | 地区 |
| 7 | address | varchar(255) | YES | | None | 地址 |
| 8 | legal_person | varchar(100) | YES | | None | 法人 |
| 9 | establish_date | date | YES | | None | 成立日期 |
| 10 | foreign_share | varchar(100) | YES | | None | 外资股份 |
| 11 | manager | varchar(255) | YES | | None | 管理者 |
| 12 | supplier_category | varchar(255) | YES | | None | 供应商类别 |
| 13 | supplier_group | varchar(255) | YES | | None | 供应商分组 |
| 14 | supply_type | varchar(255) | YES | | None | 供应类型 |
| 15 | main_product | varchar(255) | YES | | None | 主要产品 |
| 16 | business_registration | varchar(100) | YES | | None | 商业注册号 |
| 17 | social_credit_code | varchar(100) | YES | | None | 统一社会信用代码 |
| 18 | source | varchar(100) | YES | | None | 来源 |
| 19 | cause | varchar(255) | YES | | None | 原因 |
| 20 | business_license | varchar(255) | YES | | None | 营业执照 |
| 21 | invoice_name | varchar(255) | YES | | None | 发票名称 |
| 22 | customization | varchar(255) | YES | | None | 定制能力 |
| 23 | turnover | varchar(255) | YES | | None | 营业额 |
| 24 | factory_people | varchar(255) | YES | | None | 工厂人数 |
| 25 | behave | varchar(255) | YES | | None | 行为表现 |
| 26 | contact_info | varchar(255) | YES | | None | 联系信息 |
| 27 | follow_up_feedback | text | YES | | None | 跟进反馈 |
| 28 | settlement_currency | varchar(255) | YES | | None | 结算币种 |
| 29 | factory_positioning | varchar(255) | YES | | None | 工厂定位 |
| 30 | quality_control | varchar(255) | YES | | None | 质量控制 |
| 31 | factory_equipment | varchar(255) | YES | | None | 工厂设备 |
| 32 | factory_area | varchar(255) | YES | | None | 工厂面积 |
| 33 | factory_certification | varchar(255) | YES | | None | 工厂认证 |
| 34 | settlement_method | varchar(100) | YES | | None | 结算方式 |
| 35 | payment_terms | varchar(100) | YES | | None | 付款条件 |
| 36 | invoice_type | varchar(255) | YES | | None | 发票类型 |
| 37 | tax_category | varchar(255) | YES | | None | 税收类别 |
| 38 | fdefaultContactId | varchar(255) | YES | | None | 默认联系人 ID |
| 39 | settlement_party | varchar(100) | YES | | None | 结算方 |
| 40 | payee | varchar(100) | YES | | None | 收款人 |
| 41 | default_tax_rate | varchar(100) | YES | | None | 默认税率 |
| 42 | created_by | varchar(100) | YES | | None | 创建人 |
| 43 | created_at | datetime | YES | | CURRENT_TIMESTAMP | 创建时间 |
| 44 | updated_by | varchar(100) | YES | | None | 更新人 |
| 45 | updated_at | datetime | YES | | CURRENT_TIMESTAMP | 更新时间 |
| 46 | supplierid | varchar(255) | YES | | None | 供应商 ID（金蝶） |
| 47 | audit_time | varchar(255) | YES | | None | 审核时间 |
| 48 | auditor | varchar(255) | YES | | None | 审核人 |
| 49 | priorityPayment | varchar(255) | YES | | None | 优先付款 |
| 50 | export_market | varchar(255) | YES | | None | 出口市场 |

---

### 3. by_material - 金蝶物料信息表

**用途**: 从金蝶 K/3 Cloud 同步的物料主数据

| 序号 | 字段名 | 类型 | 空值 | 键 | 默认值 | 说明 |
|------|--------|------|------|-----|--------|------|
| *详细字段需补充* | | | | | | 共 12,078 条记录 |

---

### 4. sys_user - 用户信息表

| 序号 | 字段名 | 类型 | 空值 | 键 | 默认值 | 说明 |
|------|--------|------|------|-----|--------|------|
| 1 | user_id | bigint | NO | PRI | None | 用户 ID |
| 2 | dept_id | bigint | YES | | None | 部门 ID |
| 3 | user_name | varchar(30) | NO | | None | 账号 |
| 4 | nick_name | varchar(30) | NO | | None | 昵称 |
| 5 | user_type | varchar(10) | YES | | sys_user | 用户类型 |
| 6 | email | varchar(50) | YES | | None | 邮箱 |
| 7 | phonenumber | varchar(11) | YES | | None | 手机号 |
| 8 | sex | char(1) | YES | | 0 | 性别 (0=男 1=女) |
| 9 | avatar | varchar(100) | YES | | None | 头像 |
| 10 | password | varchar(100) | YES | | None | 密码 (BCrypt 加密) |
| 11 | status | char(1) | YES | | 0 | 状态 (0 正常 1 停用) |
| 12 | del_flag | char(1) | YES | | 0 | 删除标志 |
| 13 | login_ip | varchar(128) | YES | | None | 最后登录 IP |
| 14 | login_date | datetime | YES | | None | 最后登录时间 |
| 15 | create_by | varchar(64) | YES | | None | 创建人 |
| 16 | create_time | datetime | YES | | None | 创建时间 |
| 17 | update_by | varchar(64) | YES | | None | 更新人 |
| 18 | update_time | datetime | YES | | None | 更新时间 |
| 19 | remark | varchar(500) | YES | | None | 备注 |
| 20 | staff_id | varchar(255) | YES | | None | 员工 ID |
| 21 | k3_key | varchar(255) | YES | MUL | None | 金蝶 K3 关联键 |

**数据量**: 78 人

---

### 5. sys_menu - 菜单权限表

| 序号 | 字段名 | 类型 | 空值 | 键 | 默认值 | 说明 |
|------|--------|------|------|-----|--------|------|
| 1 | menu_id | bigint | NO | PRI | None | 菜单 ID |
| 2 | menu_name | varchar(50) | NO | | None | 菜单名称 |
| 3 | parent_id | bigint | YES | | 0 | 父菜单 ID |
| 4 | order_num | int | YES | | 0 | 显示顺序 |
| 5 | path | varchar(200) | YES | | None | 路由地址 |
| 6 | component | varchar(255) | YES | | None | 组件路径 |
| 7 | query_param | varchar(255) | YES | | None | 路由参数 |
| 8 | is_frame | int | YES | | 1 | 是否外链 (1=是 0=否) |
| 9 | is_cache | int | YES | | 0 | 是否缓存 (1=缓存 0=不缓存) |
| 10 | menu_type | char(1) | YES | | None | 菜单类型 (M=目录 C=菜单 F=按钮) |
| 11 | visible | char(1) | YES | | 0 | 显示状态 (0 显示 1 隐藏) |
| 12 | status | char(1) | YES | | 0 | 菜单状态 (0 正常 1 停用) |
| 13 | perms | varchar(100) | YES | | None | 权限标识 |
| 14 | icon | varchar(100) | YES | | # | 图标 |
| 15 | create_by | varchar(64) | YES | | None | 创建人 |
| 16 | create_time | datetime | YES | | None | 创建时间 |
| 17 | update_by | varchar(64) | YES | | None | 更新人 |
| 18 | update_time | datetime | YES | | None | 更新时间 |
| 19 | remark | varchar(500) | YES | | None | 备注 |

---

## 二、销售管理模块

### 6. t_sale_order - 销售订单主表

**数据量**: 3,183 条 | **数据大小**: 1.52 MB | **索引大小**: 1.08 MB

| 序号 | 字段名 | 类型 | 空值 | 键 | 默认值 | 说明 |
|------|--------|------|------|-----|--------|------|
| 1 | id | bigint | NO | PRI | auto_increment | 主键 ID |
| 2 | FID | bigint | YES | UNI | None | 金蝶系统 ID（唯一） |
| 3 | Document_type | varchar(255) | YES | | None | 单据类型 |
| 4 | FBillNo | varchar(50) | YES | | None | 销售订单号 |
| 5 | orderStatus | varchar(255) | YES | | None | 订单状态 |
| 6 | FDate | date | NO | MUL | None | 订单日期 |
| 7 | FCustId | varchar(50) | NO | MUL | None | 客户 ID |
| 8 | F_ora_BaseProperty | varchar(100) | YES | | None | 金蝶基础属性 |
| 9 | F_khhth | varchar(100) | NO | | None | 客户合同号 |
| 10 | F_kglxr | varchar(50) | YES | | None | 关键联系人 |
| 11 | F_cty_BaseProperty1 | varchar(100) | YES | | None | 自定义属性 1 |
| 12 | FSettleCurrId | varchar(50) | NO | | None | 结算币种 ID |
| 13 | F_tcbl | decimal(10,4) | YES | | None | 提成比例 |
| 14 | F_KHSD | tinyint | YES | | None | 客户手动 |
| 15 | FIsIncludedTax | tinyint | YES | | None | 是否含税 |
| 16 | F_sfbg | tinyint | YES | | None | 是否变更 |
| 17 | FSalerId | varchar(50) | YES | MUL | None | 销售员 ID |
| 18 | F_lrl | decimal(10,4) | YES | | None | 利润率 |
| 19 | F_jlrl | decimal(10,4) | YES | | None | 净利率 |
| 20 | fstate | varchar(255) | YES | | None | 状态 |
| 21 | F_gj | varchar(50) | YES | | None | 国家 |
| 22 | F_myfs | varchar(50) | YES | | None | 贸易方式 |
| 23 | F_zyxb | tinyint | YES | | None | 协议版本 |
| 24 | F_yhzh | varchar(100) | YES | MUL | None | 银行账号 |
| 25 | F_cty_Date | date | YES | | None | 自定义日期 |
| 26 | F_sygs | varchar(100) | YES | | None | 所属公司 |
| 27 | FRecConditionId | varchar(50) | YES | MUL | None | 收款条件 ID |
| 28 | Fbzfs | varchar(50) | YES | MUL | None | 包装方式 |
| 29 | FReceiveId | varchar(50) | YES | | None | 收货人 ID |
| 30 | FSettleId | varchar(50) | YES | MUL | None | 结算人 ID |
| 31 | FSettleAddress | varchar(200) | YES | | None | 结算地址 |
| 32 | FChargeId | varchar(50) | YES | | None | 负责人 ID |
| 33 | F_shhl | decimal(10,6) | YES | | None | 收货含量/比例 |
| 34 | F_shzt | tinyint | YES | | None | 收货状态 |
| 35 | F_shje | decimal(18,2) | YES | | None | 收货金额 |
| 36 | F_cty_Date1 | date | YES | | None | 自定义日期 1 |
| 37 | FCreatorId | varchar(50) | YES | MUL | None | 创建人 ID |
| 38 | FCreateDate | datetime | YES | | None | 创建日期 |
| 39 | FModifierId | varchar(50) | YES | MUL | None | 修改人 ID |
| 40 | FModifyDate | datetime | YES | | None | 修改日期 |
| 41 | FAllDisCount | decimal(18,2) | YES | | None | 总折扣金额 |
| 42 | F_ysbl1 | decimal(10,4) | YES | | None | 运费比例 1 |
| 43 | FBillTaxAmount | decimal(18,2) | YES | | None | 税额 |
| 44 | FBillAmount | decimal(18,2) | YES | | None | 订单金额 |
| 45 | FLocalCurrId | varchar(50) | YES | | None | 本位币 ID |
| 46 | FExchangeTypeId | varchar(50) | YES | | None | 汇率类型 ID |
| 47 | FExchangeRate | decimal(10,6) | YES | | None | 汇率 |
| 48 | fplannotrecamount | decimal(18,2) | YES | | None | 计划未收金额 |
| 49 | fplanallrecamount | decimal(18,2) | YES | | None | 计划已收金额 |
| 50 | fplanrefundamount | decimal(18,2) | YES | | None | 计划退款金额 |

**索引**:
- PRIMARY KEY (id)
- UNIQUE FID (FID)
- INDEX FDate (FDate)
- INDEX FCustId (FCustId)
- INDEX FSalerId (FSalerId)
- INDEX F_yhzh (F_yhzh)
- INDEX FCreatorId (FCreatorId)
- INDEX FModifierId (FModifierId)
- INDEX FRecConditionId (FRecConditionId)
- INDEX Fbzfs (Fbzfs)
- INDEX FSettleId (FSettleId)

---

### 7. t_sale_order_entry - 销售订单明细表

**数据量**: 30,938 条 | **数据大小**: 19.56 MB | **索引大小**: 7.58 MB

| 序号 | 字段名 | 类型 | 空值 | 键 | 默认值 | 说明 |
|------|--------|------|------|-----|--------|------|
| 1 | fentryid | bigint | NO | PRI | auto_increment | 明细 ID |
| 2 | fbillno | varchar(255) | YES | | None | 销售订单号 |
| 3 | fbcykc | varchar(255) | YES | | None | 备用库存 |
| 4 | f_sfxp | varchar(255) | YES | MUL | None | 是否样品 |
| 5 | f_jqhx | date | YES | | None | 交期核销日期 |
| 6 | fplanmaterialid | varchar(50) | YES | MUL | None | 计划物料 ID |
| 7 | fplanmaterialname | varchar(100) | YES | | None | 物料名称 |
| 8 | fplanunitid | varchar(50) | YES | MUL | None | 计划单位 ID |
| 9 | fqty | decimal(18,6) | YES | | None | 数量 |
| 10 | fmaterialpriceunitqty | decimal(18,6) | YES | | None | 计价单位数量 |
| 11 | fmaterialpriceunitid | varchar(50) | YES | | None | 计价单位 ID |
| 12 | fprice | decimal(18,6) | YES | | None | 单价 |
| 13 | ftaxprice | decimal(18,6) | YES | | None | 含税单价 |
| 14 | fentrytaxrate | decimal(10,6) | YES | | None | 行税率 |
| 15 | fentrytaxamount | decimal(18,2) | YES | | None | 行税额 |
| 16 | fallamount | decimal(18,2) | YES | | None | 金额合计 |
| 17 | fbasecanoutqty | decimal(18,4) | YES | | None | 基本可出数量 |
| 18 | fdeliqty | decimal(18,4) | YES | | None | 已交付数量 |
| 19 | fstockbasecanoutqty | decimal(18,4) | YES | | None | 库存基本可出数量 |
| 20 | f_ora_text1 | varchar(100) | YES | | None | 金蝶文本 1 |
| 21 | f_ora_date3 | date | YES | | None | 金蝶日期 3 |
| 22 | f_fzr | varchar(50) | YES | | None | 负责人 |
| 23 | f_ora_text | varchar(100) | YES | | None | 金蝶文本 |
| 24 | f_ora_date1 | date | YES | | None | 金蝶日期 1 |
| 25 | f_ora_date2 | date | YES | | None | 金蝶日期 2 |
| 26 | fpurjoinqty | decimal(18,4) | YES | | None | 采购.join 数量 |
| 27 | f_ora_base1 | varchar(255) | YES | | None | 金蝶基础 1 |
| 28 | f_ycjjfa | varchar(255) | YES | | None | 预算计提方法 |
| 29 | f_ora_decimal | decimal(18,4) | YES | | None | 金蝶金额 |
| 30 | f_ora_date4 | date | YES | | None | 金蝶日期 4 |
| 31 | f_ora_decimal1 | decimal(18,4) | YES | | None | 金蝶金额 1 |
| 32 | f_ora_integer2 | int | YES | | None | 金蝶整数 2 |
| 33 | f_ora_date5 | date | YES | | None | 金蝶日期 5 |
| 34 | f_ora_integer3 | int | YES | | None | 金蝶整数 3 |
| 35 | f_bzjdtwo | varchar(500) | YES | | None | 保证金第二 |
| 36 | f_ora_base | varchar(100) | YES | MUL | None | 金蝶基础 |
| 37 | f_ora_integer | int | YES | | None | 金蝶整数 |
| 38 | f_ora_date | date | YES | | None | 金蝶日期 |
| 39 | f_mz | decimal(18,4) | YES | | None | 毛重 |
| 40 | f_jz | decimal(18,4) | YES | | None | 净重 |
| 41 | f_mzz | decimal(18,4) | YES | | None | 毛重重重 |
| 42 | f_zxs | int | YES | | None | 总箱数 |
| 43 | f_xs | int | YES | | None | 箱数 |
| 44 | f_gdtp1 | varchar(550) | YES | | None | 固定图片 1 |
| 45 | f_gdtp2 | varchar(555) | YES | | None | 固定图片 2 |
| 46 | f_xdgjjdtwo | varchar(1000) | YES | | None | 订单关键节点 2 |
| 47 | f_bzfs | varchar(1000) | YES | | None | 包装方式 |
| 48 | f_tsyq | varchar(255) | YES | | None | 特殊要求 |
| 49 | f_ljrksl | decimal(18,4) | YES | | None | 理论入库数量 |
| 50 | fysbz | varchar(50) | YES | | None | 费用标志 |
| 51 | f_bgrq | date | YES | | None | 变更日期 |
| 52 | f_ctt | varchar(255) | YES | | None | 测试题 |
| 53 | f_cptp | varchar(255) | YES | | None | 产品图片 |
| 54 | f_gcbz | varchar(255) | YES | | None | 工程标准 |
| 55 | fhhwrk | tinyint | YES | | None | 换货任务 |
| 56 | f_kpdj | decimal(18,4) | YES | | None | 开票单价 |
| 57 | f_bzxdzt | varchar(50) | YES | | None | 包装修订状态 |
| 58 | f_gxzt | varchar(50) | YES | | None | 更新状态 |
| 59 | f_cht | varchar(255) | YES | | None | 出货单 |
| 60 | f_jgt | varchar(255) | YES | | None | 价格调整 |
| 61 | f_smsfj | varchar(255) | YES | | None | 顺丰附件 |
| 62 | f_bchqzt | varchar(50) | YES | | None | 仓库后勤状态 |
| 63 | f_tsl | decimal(10,2) | YES | | None | 退货数量 |
| 64 | f_bgywpm | varchar(255) | YES | | None | 英文品名 |
| 65 | f_bgdw | varchar(550) | YES | | None | 变更单位 |
| 66 | f_ygcb | decimal(18,2) | YES | | None | 预估成本 |
| 67 | f_hsbm | varchar(50) | YES | | None | 海关编码 |
| 68 | f_sbys | varchar(255) | YES | | None | 设备颜色 |
| 69 | f_bcfynew | decimal(18,2) | YES | | None | 本次费用 NEW |
| 70 | f_glbcfynew | decimal(18,2) | YES | | None | 关联本次费用 NEW |
| 71 | fbzcc | varchar(100) | YES | | None | 包装存储 |
| 72 | fbzgctg | tinyint | YES | | None | 工厂通过 |
| 73 | frkshrq | date | YES | | None | 入库开始日期 |
| 74 | fbjr | varchar(50) | YES | | None | 报关员 |
| 75 | ftpr | varchar(50) | YES | | None | 贴牌 |
| 76 | f_cty_baseproperty4 | varchar(50) | YES | | None | 自定义属性 4 |
| 77 | fkhywms | varchar(255) | YES | | None | 客户海外 MS |
| 78 | f_cty_baseproperty6 | varchar(255) | YES | | None | 自定义属性 6 |
| 79 | f_ygcbdj | decimal(18,6) | YES | | None | 预估成本单价 |
| 80 | f_ygcbdj1 | decimal(18,6) | YES | | None | 预估成本单价 1 |
| 81 | f_ygcb1 | decimal(18,6) | YES | | None | 预估成本 1 |
| 82 | f_peuu_attachment_83g | varchar(255) | YES | | None | 附件 |
| 83 | fcgddshrq | date | YES | | None | 采购订单收货日期 |
| 84 | fyjsj | date | YES | | None | 佣金时间 |
| 85 | fslshrq | date | YES | | None | 实际收货日期 |
| 86 | f_xlcp | varchar(50) | YES | | None | 系列产品 |
| 87 | f_cplb | varchar(50) | YES | MUL | None | 产品类别 |
| 88 | f_ckrq | date | YES | | None | 出库日期 |
| 89 | f_cty_baseproperty7 | varchar(500) | YES | | None | 自定义属性 7 |
| 90 | f_cty_baseproperty8 | varchar(555) | YES | | None | 自定义属性 8 |

**索引**:
- PRIMARY KEY (fentryid)
- INDEX f_sfxp (f_sfxp)
- INDEX fplanmaterialid (fplanmaterialid)
- INDEX fplanunitid (fplanunitid)
- INDEX f_ora_base (f_ora_base)
- INDEX f_cplb (f_cplb)

---

### 8. t_sale_order_cost - 销售订单成本预估表

**数据量**: 3,099 条

*(详细字段待补充)*

---

### 9. t_sales_price - 销售价目表主表

*(详细字段待补充)*

---

### 10. t_sales_price_item - 销售价目明细表

**数据量**: 9,821 条

*(详细字段待补充)*

---

### 11. t_sales_price_item_package - 销售价目明细 - 包装物料表

**数据量**: 10,654 条

*(详细字段待补充)*

---

### 12. delivery_notice - 发货通知单主表

**数据量**: 2,199 条

*(详细字段待补充)*

---

### 13. delivery_notice_entry - 发货通知单明细表

**数据量**: 19,920 条 | **数据大小**: 15.55 MB

*(部分关键字段已在上文展示)*

---

### 14. sale_change_bill - 销售变更单 - 表头

*(详细字段待补充)*

---

### 15. sale_change_detail - 销售变更单 - 明细

*(详细字段待补充)*

---

## 三、采购管理模块

### 16. purchase_instock - 采购入库单主表

**数据量**: 12,613 条 | **数据大小**: 3.52 MB

*(详细字段待补充)*

---

### 17. purchase_instock_entry - 采购入库单明细表

**数据量**: 29,814 条 | **数据大小**: 21.56 MB

*(详细字段待补充)*

---

### 18. receive_notice - 收料通知单主表

**数据量**: 6,237 条

*(详细字段待补充)*

---

### 19. receive_notice_entry - 收料通知单明细表

**数据量**: 16,130 条 | **数据大小**: 7.52 MB

*(详细字段待补充)*

---

### 20. purchase_quotation - 采购报价单主表

**数据量**: 2,311 条

*(详细字段待补充)*

---

### 21. purchase_quotation_entry - 采购报价单明细表

**数据量**: 5,248 条

*(详细字段待补充)*

---

### 22. po_order_bill_head - 采购订单主表

*(详细字段待补充)*

---

### 23. po_order_bill_head_entry - 采购订单明细表

**数据量**: 3,603 条

*(详细字段待补充)*

---

### 24. price_list - 采购价目表主表

*(详细字段待补充)*

---

### 25. price_list_entry - 采购价目表明细表

**数据量**: 5,830 条

*(详细字段待补充)*

---

### 26. inquiry_order - 询价单主表

*(详细字段待补充)*

---

### 27. inquiry_order_entry - 询价单明细表

*(详细字段待补充)*

---

## 四、仓储管理模块

### 28. wms_inventory - 库存表

| 序号 | 字段名 | 类型 | 空值 | 键 | 默认值 | 说明 |
|------|--------|------|------|-----|--------|------|
| 1 | id | bigint | NO | PRI | auto_increment | 主键 ID |
| 2 | sku_id | bigint | YES | | None | SKU ID |
| 3 | warehouse_id | bigint | YES | | None | 仓库 ID |
| 4 | quantity | decimal(20,2) | YES | | None | 库存数量 |
| 5 | remark | varchar(255) | YES | | None | 备注 |
| 6 | create_by | varchar(64) | YES | | None | 创建人 |
| 7 | create_time | datetime(3) | YES | | None | 创建时间 |
| 8 | update_by | varchar(64) | YES | | None | 更新人 |
| 8 | update_time | datetime(3) | YES | | None | 更新时间 |

---

### 29. wms_inventory_history - 库存历史记录表

*(详细字段待补充)*

---

### 30. wms_warehouse - 仓库表

| 序号 | 字段名 | 类型 | 空值 | 键 | 默认值 | 说明 |
|------|--------|------|------|-----|--------|------|
| 1 | id | bigint | NO | PRI | auto_increment | 主键 ID |
| 2 | warehouse_code | varchar(20) | YES | | None | 仓库编码 |
| 3 | warehouse_name | varchar(50) | NO | | None | 仓库名称 |
| 4 | remark | varchar(255) | YES | | None | 备注 |
| 5 | order_num | bigint | YES | | 0 | 排序号 |
| 6 | create_by | varchar(64) | YES | | None | 创建人 |
| 7 | create_time | datetime(3) | YES | | None | 创建时间 |
| 8 | update_by | varchar(64) | YES | | None | 更新人 |
| 9 | update_time | datetime(3) | YES | | None | 更新时间 |

---

### 31. warehouse_location - 仓库仓位信息表

*(详细字段待补充)*

---

### 32. wms_check_order - 库存盘点单据

*(详细字段待补充)*

---

### 33. wms_check_order_detail - 库存盘点详情

*(详细字段待补充)*

---

### 34. wms_movement_order - 移库单

*(详细字段待补充)*

---

### 35. wms_movement_order_detail - 库存移动详情

*(详细字段待补充)*

---

### 36. wms_receipt_order - 入库单

*(详细字段待补充)*

---

### 37. wms_receipt_order_detail - 入库单详情

*(详细字段待补充)*

---

### 38. wms_shipment_order_detail - 出库单详情

*(详细字段待补充)*

---

### 39. receipt_notice_full - 检验单表

**数据量**: 4,769 条

*(详细字段待补充)*

---

### 40. wms_item_brand - 商品品牌表

*(详细字段待补充)*

---

### 41. wms_item_sku - SKU 信息表

*(详细字段待补充)*

---

### 42. wms_merchant - 往来单位

*(详细字段待补充)*

---

## 五、财务管理模块

### 43. financial_information - 银行信息表

**数据量**: 2,039 条

| 序号 | 字段名 | 类型 | 空值 | 键 | 默认值 | 说明 |
|------|--------|------|------|-----|--------|------|
| 1 | id | bigint | NO | PRI | auto_increment | 主键 ID |
| 2 | supplier_number | varchar(150) | YES | | None | 供应商编号 |
| 3 | nation | varchar(50) | YES | | None | 国家 |
| 4 | bank_account | varchar(250) | YES | | None | 银行账号 |
| 5 | account_name | varchar(100) | YES | | None | 账户名称 |
| 6 | receiving_bank | varchar(100) | YES | | None | 收款银行 |
| 7 | bank_address | varchar(255) | YES | | None | 银行地址 |
| 8 | opening_bank | varchar(255) | YES | | None | 开户行 |
| 9 | created_at | datetime | YES | | CURRENT_TIMESTAMP | 创建时间 |
| 10 | updated_at | datetime | YES | | CURRENT_TIMESTAMP | 更新时间 |
| 11 | k3_id | int | YES | MUL | None | 金蝶 ID |

**索引**:
- PRIMARY KEY (id)
- INDEX k3_id (k3_id)

---

### 44. settlement_method - 结算方式表

*(详细字段待补充)*

---

### 45. tax_rate - 税率列表表

*(详细字段待补充)*

---

### 46. customer_transfer - 客户转让列表

*(详细字段待补充)*

---

### 47. tmp_report_data_income - 临时报表数据 - 收入

*(详细字段待补充)*

---

## 六、系统管理模块

### 48. sys_dept - 部门表

| 序号 | 字段名 | 类型 | 空值 | 键 | 默认值 | 说明 |
|------|--------|------|------|-----|--------|------|
| 1 | dept_id | bigint | NO | PRI | None | 部门 ID |
| 2 | parent_id | bigint | YES | | 0 | 父部门 ID |
| 3 | Kingdee_department_ID | bigint | YES | | None | 金蝶部门 ID |
| 4 | ancestors | varchar(500) | YES | | | 祖级列表 |
| 5 | dept_name | varchar(30) | YES | | | 部门名称 |
| 6 | order_num | int | YES | | 0 | 显示顺序 |
| 7 | leader | varchar(20) | YES | | None | 负责人 |
| 8 | phone | varchar(11) | YES | | None | 联系电话 |
| 9 | email | varchar(50) | YES | | None | 邮箱 |
| 10 | status | char(1) | YES | | 0 | 状态 (0 正常 1 停用) |
| 11 | del_flag | char(1) | YES | | 0 | 删除标志 |
| 12 | create_by | varchar(64) | YES | | | 创建人 |
| 13 | create_time | datetime | YES | | None | 创建时间 |
| 14 | update_by | varchar(64) | YES | | | 更新人 |
| 15 | update_time | datetime | YES | | None | 更新时间 |

---

### 49. sys_role - 角色信息表

| 序号 | 字段名 | 类型 | 空值 | 键 | 默认值 | 说明 |
|------|--------|------|------|-----|--------|------|
| 1 | role_id | bigint | NO | PRI | None | 角色 ID |
| 2 | role_name | varchar(30) | NO | | None | 角色名称 |
| 3 | role_key | varchar(100) | NO | | None | 角色权限字符串 |
| 4 | role_sort | int | NO | | None | 显示顺序 |
| 5 | data_scope | char(1) | YES | | 1 | 数据范围 (1=全部 2=自定义) |
| 6 | menu_check_strictly | tinyint(1) | YES | | 1 | 菜单树选择项是否关联 |
| 7 | dept_check_strictly | tinyint(1) | YES | | 1 | 部门树选择项是否关联 |
| 8 | status | char(1) | NO | | None | 状态 (0 正常 1 停用) |
| 9 | del_flag | char(1) | YES | | 0 | 删除标志 |
| 10 | create_by | varchar(64) | YES | | | 创建人 |
| 11 | create_time | datetime | YES | | None | 创建时间 |
| 12 | update_by | varchar(64) | YES | | | 更新人 |
| 13 | update_time | datetime | YES | | None | 更新时间 |
| 14 | remark | varchar(500) | YES | | None | 备注 |

---

### 50. sys_post - 岗位信息表

| 序号 | 字段名 | 类型 | 空值 | 键 | 默认值 | 说明 |
|------|--------|------|------|-----|--------|------|
| 1 | post_id | bigint | NO | PRI | None | 岗位 ID |
| 2 | post_code | varchar(64) | NO | | None | 岗位编码 |
| 3 | post_name | varchar(50) | NO | | None | 岗位名称 |
| 4 | post_sort | int | NO | | None | 显示顺序 |
| 5 | status | char(1) | NO | | None | 状态 (0 正常 1 停用) |
| 6 | create_by | varchar(64) | YES | | | 创建人 |
| 7 | create_time | datetime | YES | | None | 创建时间 |
| 8 | update_by | varchar(64) | YES | | | 更新人 |
| 9 | update_time | datetime | YES | | None | 更新时间 |
| 10 | remark | varchar(500) | YES | | None | 备注 |

---

### 51. sys_dict_data - 字典数据表

| 序号 | 字段名 | 类型 | 空值 | 键 | 默认值 | 说明 |
|------|--------|------|------|-----|--------|------|
| 1 | dict_code | bigint | NO | PRI | None | 字典编码 |
| 2 | dict_sort | int | YES | | 0 | 字典排序 |
| 3 | dict_label | varchar(100) | YES | | | 字典标签 |
| 4 | dict_value | varchar(100) | YES | | | 字典键值 |
| 5 | dict_type | varchar(100) | YES | | | 字典类型 |
| 6 | css_class | varchar(100) | YES | | None | 样式属性 |
| 7 | list_class | varchar(100) | YES | | None | 表格回显样式 |
| 8 | is_default | char(1) | YES | | N | 是否默认 |
| 9 | status | char(1) | YES | | 0 | 状态 |
| 10 | create_by | varchar(64) | YES | | | 创建人 |
| 11 | create_time | datetime | YES | | None | 创建时间 |
| 12 | update_by | varchar(64) | YES | | | 更新人 |
| 13 | update_time | datetime | YES | | None | 更新时间 |
| 14 | remark | varchar(500) | YES | | None | 备注 |

---

### 52. sys_dict_type - 字典类型表

| 序号 | 字段名 | 类型 | 空值 | 键 | 默认值 | 说明 |
|------|--------|------|------|-----|--------|------|
| 1 | dict_id | bigint | NO | PRI | None | 字典主键 |
| 2 | dict_name | varchar(100) | YES | | | 字典名称 |
| 3 | dict_type | varchar(100) | YES | UNI | | 字典类型 |
| 4 | status | char(1) | YES | | 0 | 状态 |
| 5 | create_by | varchar(64) | YES | | | 创建人 |
| 6 | create_time | datetime | YES | | None | 创建时间 |
| 7 | update_by | varchar(64) | YES | | | 更新人 |
| 8 | update_time | datetime | YES | | None | 更新时间 |
| 9 | remark | varchar(500) | YES | | None | 备注 |

**索引**: UNIQUE (dict_type)

---

### 53. sys_config - 参数配置表

| 序号 | 字段名 | 类型 | 空值 | 键 | 默认值 | 说明 |
|------|--------|------|------|-----|--------|------|
| 1 | config_id | bigint | NO | PRI | None | 参数主键 |
| 2 | config_name | varchar(100) | YES | | | 参数名称 |
| 3 | config_key | varchar(100) | YES | | | 参数键名 |
| 4 | config_value | varchar(500) | YES | | | 参数键值 |
| 5 | config_type | char(1) | YES | | N | 系统内置 (Y 是 N 否) |
| 6 | create_by | varchar(64) | YES | | | 创建人 |
| 7 | create_time | datetime | YES | | None | 创建时间 |
| 8 | update_by | varchar(64) | YES | | | 更新人 |
| 9 | update_time | datetime | YES | | None | 更新时间 |
| 10 | remark | varchar(500) | YES | | None | 备注 |

---

### 54. sys_oss - OSS 对象存储表

| 序号 | 字段名 | 类型 | 空值 | 键 | 默认值 | 说明 |
|------|--------|------|------|-----|--------|------|
| 1 | oss_id | bigint | NO | PRI | None | 对象存储 ID |
| 2 | file_name | varchar(255) | NO | | | 文件名 |
| 3 | original_name | varchar(255) | NO | | | 原始文件名 |
| 4 | file_suffix | varchar(10) | NO | | | 文件后缀 |
| 5 | url | varchar(500) | NO | | None | URL 地址 |
| 6 | create_time | datetime | YES | | None | 创建时间 |
| 7 | create_by | varchar(64) | YES | | | 创建人 |
| 8 | update_time | datetime | YES | | None | 更新时间 |
| 9 | update_by | varchar(64) | YES | | | 更新人 |
| 10 | service | varchar(20) | NO | | minio | 服务对象 (minio/aliyun/qcloud) |

---

### 55. sys_oss_config - 对象存储配置表

| 序号 | 字段名 | 类型 | 空值 | 键 | 默认值 | 说明 |
|------|--------|------|------|-----|--------|------|
| 1 | oss_config_id | bigint | NO | PRI | None | 配置 ID |
| 2 | config_key | varchar(20) | NO | | | 配置键 |
| 3 | access_key | varchar(255) | YES | | | 访问密钥 |
| 4 | secret_key | varchar(255) | YES | | | 私有密钥 |
| 5 | bucket_name | varchar(255) | YES | | | 桶名称 |
| 6 | prefix | varchar(255) | YES | | | 前缀 |
| 7 | endpoint | varchar(255) | YES | | | 端点 |
| 8 | domain | varchar(255) | YES | | | 域名 |
| 9 | is_https | char(1) | YES | | N | 是否 HTTPS |
| 10 | region | varchar(255) | YES | | | 区域 |
| 11 | access_policy | char(1) | NO | | 1 | 访问策略 |
| 12 | status | char(1) | YES | | 1 | 状态 |
| 13 | ext1 | varchar(255) | YES | | | 扩展字段 |
| 14 | create_by | varchar(64) | YES | | | 创建人 |
| 15 | create_time | datetime | YES | | None | 创建时间 |
| 16 | update_by | varchar(64) | YES | | | 更新人 |
| 17 | update_time | datetime | YES | | None | 更新时间 |
| 18 | remark | varchar(500) | YES | | None | 备注 |

---

### 56. sys_logininfor - 系统访问记录

**数据量**: 登录日志表

| 序号 | 字段名 | 类型 | 空值 | 键 | 默认值 | 说明 |
|------|--------|------|------|-----|--------|------|
| 1 | info_id | bigint | NO | PRI | None | 访问 ID |
| 2 | user_name | varchar(50) | YES | | | 用户账号 |
| 3 | ipaddr | varchar(128) | YES | | | 登录 IP 地址 |
| 4 | login_location | varchar(255) | YES | | | 登录地点 |
| 5 | browser | varchar(50) | YES | | | 浏览器类型 |
| 6 | os | varchar(50) | YES | | | 操作系统 |
| 7 | status | char(1) | YES | MUL | 0 | 登录状态 (0 成功 1 失败) |
| 8 | msg | varchar(255) | YES | | | 提示消息 |
| 9 | login_time | datetime | YES | MUL | None | 访问时间 |

**索引**:
- INDEX status (status)
- INDEX login_time (login_time)

---

### 57. sys_oper_log - 操作日志记录

| 序号 | 字段名 | 类型 | 空值 | 键 | 默认值 | 说明 |
|------|--------|------|------|-----|--------|------|
| 1 | oper_id | bigint | NO | PRI | None | 日志主键 |
| 2 | title | varchar(50) | YES | | | 模块标题 |
| 3 | business_type | int | YES | | 0 | 业务类型 |
| 4 | method | varchar(100) | YES | | | 方法名称 |
| 5 | request_method | varchar(10) | YES | | | 请求方式 |
| 6 | operator_type | int | YES | | 0 | 操作类别 |
| 7 | oper_name | varchar(50) | YES | | | 操作人员 |
| 8 | dept_name | varchar(50) | YES | | | 部门名称 |
| 9 | oper_url | varchar(255) | YES | | | 请求 URL |
| 10 | oper_ip | varchar(128) | YES | | | 主机地址 |
| 11 | oper_location | varchar(255) | YES | | | 操作地点 |
| 12 | oper_param | varchar(2000) | YES | | | 请求参数 |
| 13 | json_result | varchar(2000) | YES | | | 返回参数 |
| 14 | status | int | YES | | 0 | 操作状态 |
| 15 | error_msg | varchar(2000) | YES | | | 错误消息 |
| 16 | oper_time | datetime | YES | | None | 操作时间 |

---

### 58. sys_notice - 通知公告表

| 序号 | 字段名 | 类型 | 空值 | 键 | 默认值 | 说明 |
|------|--------|------|------|-----|--------|------|
| 1 | notice_id | int | NO | PRI | None | 公告 ID |
| 2 | notice_title | varchar(50) | NO | | | 公告标题 |
| 3 | notice_type | char(1) | NO | | | 公告类型 (1=通知 2=公告) |
| 4 | notice_content | longblob | YES | | None | 公告内容 |
| 5 | status | char(1) | YES | | 0 | 公告状态 (0 正常 1 关闭) |
| 6 | create_by | varchar(64) | YES | | | 创建人 |
| 7 | create_time | datetime | YES | | None | 创建时间 |
| 8 | update_by | varchar(64) | YES | | | 更新人 |
| 9 | update_time | datetime | YES | | None | 更新时间 |
| 10 | remark | varchar(255) | YES | | None | 备注 |

---

### 59. sys_employee - 员工信息表

*(详细字段待补充)*

---

### 60. hr_employee_follow_up - 员工人事跟进融合表

*(详细字段待补充)*

---

### 61. sys_data_audit_log - 审计日志表（记录用户修改痕迹）

**数据量**: 7,120 条

*(详细字段需补充查询)*

---

### 62. sys_user_role - 用户和角色关联表

| 序号 | 字段名 | 类型 | 空值 | 键 | 默认值 | 说明 |
|------|--------|------|------|-----|--------|------|
| 1 | user_id | bigint | NO | PRI | None | 用户 ID |
| 2 | role_id | bigint | NO | PRI | None | 角色 ID |

**索引**: PRIMARY KEY (user_id, role_id)

---

### 63. sys_role_menu - 角色和菜单关联表

| 序号 | 字段名 | 类型 | 空值 | 键 | 默认值 | 说明 |
|------|--------|------|------|-----|--------|------|
| 1 | role_id | bigint | NO | PRI | None | 角色 ID |
| 2 | menu_id | bigint | NO | PRI | None | 菜单 ID |

**索引**: PRIMARY KEY (role_id, menu_id)

---

### 64. sys_role_dept - 角色和部门关联表

| 序号 | 字段名 | 类型 | 空值 | 键 | 默认值 | 说明 |
|------|--------|------|------|-----|--------|------|
| 1 | role_id | bigint | NO | PRI | None | 角色 ID |
| 2 | dept_id | bigint | NO | PRI | None | 部门 ID |

**索引**: PRIMARY KEY (role_id, dept_id)

---

### 65. sys_user_post - 用户与岗位关联表

| 序号 | 字段名 | 类型 | 空值 | 键 | 默认值 | 说明 |
|------|--------|------|------|-----|--------|------|
| 1 | user_id | bigint | NO | PRI | None | 用户 ID |
| 2 | post_id | bigint | NO | PRI | None | 岗位 ID |

**索引**: PRIMARY KEY (user_id, post_id)

---

### 66. gen_table - 代码生成业务表

| 序号 | 字段名 | 类型 | 空值 | 键 | 默认值 | 说明 |
|------|--------|------|------|-----|--------|------|
| 1 | table_id | bigint | NO | PRI | None | 表 ID |
| 2 | table_name | varchar(200) | YES | | None | 表名称 |
| 3 | table_comment | varchar(500) | YES | | None | 表注释 |
| 4 | sub_table_name | varchar(64) | YES | | None | 子表名称 |
| 5 | sub_table_fk_name | varchar(64) | YES | | None | 子表外键名 |
| 6 | class_name | varchar(100) | YES | | None | 类名 |
| 7 | tpl_category | varchar(200) | YES | | crud | 模板类别 |
| 8 | package_name | varchar(100) | YES | | None | 包名 |
| 9 | module_name | varchar(30) | YES | | None | 模块名 |
| 10 | business_name | varchar(30) | YES | | None | 业务名 |
| 11 | function_name | varchar(50) | YES | | None | 功能名 |
| 12 | function_author | varchar(50) | YES | | None | 作者 |
| 13 | gen_type | char(1) | YES | | 0 | 生成类型 |
| 14 | gen_path | varchar(200) | YES | | / | 生成路径 |
| 15 | options | varchar(1000) | YES | | None | 选项 |
| 16 | create_by | varchar(64) | YES | | None | 创建人 |
| 17 | create_time | datetime | YES | | None | 创建时间 |
| 18 | update_by | varchar(64) | YES | | None | 更新人 |
| 19 | update_time | datetime | YES | | None | 更新时间 |
| 20 | remark | varchar(500) | YES | | None | 备注 |

---

### 67. gen_table_column - 代码生成业务表字段

*(详细字段待补充)*

---

## 七、其他业务表

### 68. dictionary_table - 字典表

| 序号 | 字段名 | 类型 | 空值 | 键 | 默认值 | 说明 |
|------|--------|------|------|-----|--------|------|
| 1 | id | bigint | NO | PRI | auto_increment | 主键 ID |
| 2 | bilhead | int | NO | | None | 表头 ID |
| 3 | dict_name | varchar(255) | NO | | None | 字典名称 |
| 4 | dict_code | varchar(100) | NO | UNI | None | 字典编码 |
| 5 | parent_code | varchar(100) | YES | MUL | None | 父级编码 |
| 6 | create_date | datetime | YES | | CURRENT_TIMESTAMP | 创建日期 |
| 7 | update_date | datetime | YES | | CURRENT_TIMESTAMP | 更新日期 |
| 8 | remark | varchar(500) | YES | | None | 备注 |
| 9 | status | tinyint | YES | MUL | 1 | 状态 |
| 10 | sort_order | int | YES | | 0 | 排序 |

---

### 69. bymaterial_dictionary - 物料字典表

*(详细字段待补充)*

---

### 70. country - 国家基础资料表

*(详细字段待补充)*

---

### 71. supplier_contact - 联系人信息表

**数据量**: 4,824 条

*(详细字段待补充)*

---

### 72. supplier_groups - 供应商分组字典表

*(详细字段待补充)*

---

### 73. supplier_encoding - 供应商信息表

*(详细字段待补充)*

---

### 74. supplier_visit_record - 供应商回访记录表

*(详细字段待补充)*

---

### 75. supplier_visit_photo - 供应商回访照片表

*(详细字段待补充)*

---

### 76. onl_drag_table_relation - 仪表盘聚合表

*(详细字段待补充)*

---

### 77. wms_check_order_copy1 - 库存盘点单据 (副本)

*(详细字段待补充)*

---

## 附录：表索引速查

### 数据量 TOP10 表

| 排名 | 表名 | 中文名 | 数据量 |
|------|------|--------|--------|
| 1 | t_sale_order_entry | 销售订单明细 | 30,938 |
| 2 | purchase_instock_entry | 采购入库明细 | 29,814 |
| 3 | delivery_notice_entry | 发货通知明细 | 19,920 |
| 4 | receive_notice_entry | 收料通知明细 | 16,130 |
| 5 | purchase_instock | 采购入库主表 | 12,613 |
| 6 | by_material | 金蝶物料 | 12,078 |
| 7 | t_sales_price_item_package | 销售价目包装 | 10,654 |
| 8 | t_sales_price_item | 销售价目明细 | 9,821 |
| 9 | sys_data_audit_log | 审计日志 | 7,120 |
| 10 | receive_notice | 收料通知主表 | 6,237 |

---

### 主键类型统计

- **bigint 自增**: 大多数业务表使用
- **int 自增**: 部分字典表使用
- **varchar**: 少数关联表使用复合主键

---

### 外键关联规则

#### 金蝶 K/3 Cloud 集成字段前缀

| 前缀 | 含义 | 示例 |
|------|------|------|
| F_ | 金蝶标准字段 | FBillNo, FCustId |
| f_ora_ | 金蝶 ERP 扩展 | f_ora_text, f_ora_date |
| f_cty_ | 自定义扩展 | f_cty_baseproperty |
| f_ | 系统自有字段 | fentryid, fbillno |

---

**文档结束**
