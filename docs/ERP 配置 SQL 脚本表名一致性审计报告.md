# ERP 配置 SQL 脚本表名一致性审计报告

**审计时间**: 2026-04-01  
**审计范围**: 9 个 ERP 模块初始化 SQL 脚本  
**审计依据**: 实际数据库表名 vs 配置文件表名  

---

## 🔴 **P0 严重问题：表名大小写不一致**

### 问题描述
多个模块的 `page_config.tableName` 和 `form_config.tableName` 使用了**不同的大小写格式**，这会导致：
- ❌ MySQL 在某些操作系统下找不到表（Windows 不区分大小写，Linux 区分）
- ❌ 动态查询引擎无法正确构建 SQL
- ❌ 前端页面加载时报"表不存在"错误

### 详细对比

| 模块 | module_code | page_config.tableName | form_config.tableName | 是否一致 | 风险等级 |
|------|-------------|----------------------|----------------------|---------|---------|
| **采购订单** | purchaseorder | `po_order_bill_head` | `po_order_bill_head` | ✅ 一致 | 🟢 低 |
| **收款单** | receivebill | `f_receivebill` | `f_receivebill` | ✅ 一致 | 🟢 低 |
| **付款申请单** | paymentapply | `f_rectunit_type` | `f_rectunit_type` | ✅ 一致 | 🟢 低 |
| **销售订单** | saleorder | `t_sale_order` | `t_sale_order` | ✅ 一致 | 🟢 低 |
| **收料通知单** | receivenotice | `receive_notice` | `receive_notice` | ✅ 一致 | 🟢 低 |
| **采购入库单** | purchaseinstock | `PurchaseInStock` | `purchase_instock` | ❌ **不一致** | 🔴 **高** |
| **采购报价单** | purchasequotation | `PurchaseQuotation` | `purchase_quotation` | ❌ **不一致** | 🔴 **高** |
| **检验单** | inspection | `receipt_notice_full` | `receipt_notice_full` | ✅ 一致 | 🟢 低 |

---

## 🔴 **P0 严重问题：明细表名不一致**

### 问题描述
主表和明细表的命名风格不统一，部分使用 snake_case，部分使用 PascalCase。

### 详细对比

| 模块 | 主表名 | 明细表名 | 命名风格 | 一致性 |
|------|--------|---------|---------|-------|
| **采购订单** | po_order_bill_head | po_order_bill_head_entry | ✅ snake_case | ✅ 一致 |
| **收款单** | f_receivebill | f_receivebill_entry | ✅ snake_case | ✅ 一致 |
| **付款申请单** | f_rectunit_type | f_rectunit_detail | ⚠️ 略有差异 | 🟡 中 |
| **销售订单** | t_sale_order | t_sale_order_entry | ✅ snake_case | ✅ 一致 |
| **收料通知单** | receive_notice | receive_notice_entry | ✅ snake_case | ✅ 一致 |
| **采购入库单** | PurchaseInStock / purchase_instock | purchase_instock_entry | ❌ 混用 | 🔴 高 |
| **采购报价单** | PurchaseQuotation / purchase_quotation | purchase_quotation_entry | ❌ 混用 | 🔴 高 |
| **检验单** | receipt_notice_full | (未配置明细表) | ⚠️ 不完整 | 🟡 中 |

---

## 📊 **命名规范建议**

### 推荐标准：snake_case（全小写 + 下划线）

**理由**：
1. ✅ MySQL 官方推荐
2. ✅ Linux/Unix 系统友好（区分大小写的文件系统）
3. ✅ 与现有大部分表名风格一致
4. ✅ 避免跨平台部署时的兼容性问题

### 统一后的表名映射表

| 模块 | 建议的主表名 | 建议的明细表名 | 当前状态 | 需要修改 |
|------|------------|--------------|---------|---------|
| **采购订单** | `po_order_bill_head` | `po_order_bill_head_entry` | ✅ 已符合 | 无需修改 |
| **收款单** | `f_receivebill` | `f_receivebill_entry` | ✅ 已符合 | 无需修改 |
| **付款申请单** | `f_rectunit_type` | `f_rectunit_detail` 或 `f_rectunit_type_entry` | ⚠️ 建议统一 | 可选 |
| **销售订单** | `t_sale_order` | `t_sale_order_entry` | ✅ 已符合 | 无需修改 |
| **收料通知单** | `receive_notice` | `receive_notice_entry` | ✅ 已符合 | 无需修改 |
| **采购入库单** | `purchase_instock` | `purchase_instock_entry` | ❌ page_config 使用 PascalCase | **必须修改** |
| **采购报价单** | `purchase_quotation` | `purchase_quotation_entry` | ❌ page_config 使用 PascalCase | **必须修改** |
| **检验单** | `receipt_notice_full` | (暂未配置) | ⚠️ 表名特殊 | 建议优化 |

---

## 🔧 **立即修复方案**

### 1. 采购入库单初始化配置.sql

**问题位置**: 第 67 行和第 140 行

```sql
-- 修复前
"tableName": "PurchaseInStock"  -- 第 67 行 (page_config)
...
"tableName": "purchase_instock",  -- 第 140 行 (form_config)

-- 修复后
"tableName": "purchase_instock"  -- 统一为 snake_case
```

### 2. 采购报价单初始化配置.sql

**问题位置**: 第 67 行和第 167 行

```sql
-- 修复前
"tableName": "PurchaseQuotation"  -- 第 67 行 (page_config)
...
"tableName": "purchase_quotation",  -- 第 167 行 (form_config)

-- 修复后
"tableName": "purchase_quotation"  -- 统一为 snake_case
```

### 3. 检验单初始化配置.sql

**问题**: 表名 `receipt_notice_full` 看起来像是一个视图或临时表，不是标准的业务表名。

**建议**: 
- 确认数据库中是否真实存在此表
- 如果不存在，应该使用 `inspection` 或 `inspection_entry`
- 如果存在，需要在文档中说明这个表的特殊性

---

## 📋 **验证步骤**

### 第一步：检查数据库中的实际表名

```sql
-- 在 MySQL 中执行
SHOW TABLES LIKE '%purchase%';
SHOW TABLES LIKE '%quotation%';
SHOW TABLES LIKE '%instock%';
SHOW TABLES LIKE '%inspection%';
```

### 第二步：确认表名大小写

```sql
-- 检查表的确切名称（包括大小写）
SELECT TABLE_NAME 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'test' 
  AND (TABLE_NAME LIKE '%Purchase%' OR TABLE_NAME LIKE '%purchase%');
```

### 第三步：验证配置一致性

```sql
-- 检查 erp_page_config 中的 tableName 配置
SELECT 
  module_code,
  JSON_UNQUOTE(JSON_EXTRACT(page_config, '$.tableName')) AS page_table_name,
  JSON_UNQUOTE(JSON_EXTRACT(form_config, '$.tableName')) AS form_table_name,
  CASE 
    WHEN JSON_UNQUOTE(JSON_EXTRACT(page_config, '$.tableName')) = 
         JSON_UNQUOTE(JSON_EXTRACT(form_config, '$.tableName')) 
    THEN '✅ 一致'
    ELSE '❌ 不一致'
  END AS consistency
FROM erp_page_config
WHERE module_code IN (
  'purchaseorder', 'receivebill', 'paymentapply', 'saleorder',
  'receivenotice', 'purchaseinstock', 'purchasequotation', 'inspection'
);
```

---

## ⚠️ **风险评估**

| 风险项 | 影响范围 | 发生概率 | 严重程度 | 综合风险 |
|--------|---------|---------|---------|---------|
| 表名大小写不一致导致查询失败 | 生产环境 (Linux) | 高 | 高 | 🔴 **高风险** |
| 明细表命名不规范 | 代码可维护性 | 中 | 中 | 🟡 **中风险** |
| 检验单表名特殊 | 功能完整性 | 中 | 中 | 🟡 **中风险** |

---

## 🎯 **修复优先级**

### P0 - 立即修复（阻塞上线）
1. ✅ 采购入库单：统一为 `purchase_instock`
2. ✅ 采购报价单：统一为 `purchase_quotation`

### P1 - 尽快修复（影响维护）
1. ⚠️ 付款申请单：统一明细表名为 `f_rectunit_type_entry`
2. ⚠️ 检验单：确认并规范化表名

### P2 - 后续优化（提升质量）
1. 📝 建立表名命名规范文档
2. 📝 添加表名自动校验机制

---

## 📌 **行动清单**

- [ ] **立即执行**: 修复采购入库单的表名大小写
- [ ] **立即执行**: 修复采购报价单的表名大小写
- [ ] **验证**: 在 Linux 环境测试所有模块
- [ ] **文档**: 创建表名映射字典
- [ ] **规范**: 制定表名命名标准
- [ ] **检查**: 审计其他模块的配置一致性

---

**审计结论**: 发现 2 个 P0 级别问题，需立即修复后方可上线！

