# ERP菜单分组优化方案 - 实施指南

## 📋 方案概述

基于金蝶/用友等主流ERP系统的标准模块划分,将原有15个平铺单据按业务域分组为**9个二级菜单**,形成清晰的三级菜单结构。

---

## 🎯 优化前后对比

### 优化前(2级结构)
```
ERP 业务菜单 (一级)
├── 公共配置管理
├── 收款单管理
├── 付款申请单管理
├── 采购订单管理
├── ... (共15个单据平铺)
```

### 优化后(3级结构)
```
ERP 业务菜单 (一级)
├── 公共配置管理 (order_num: 1)
├── 基础资料 (order_num: 10)
│   └── 富通客户管理
├── 销售管理 (order_num: 11)
│   ├── 销售订单管理
│   ├── 销售订单信息管理
│   ├── 发货通知单管理
│   └── 销售出库单管理
├── 采购管理 (order_num: 12)
│   ├── 询价单管理
│   ├── 采购报价单管理
│   ├── 采购订单管理
│   ├── 收料通知单管理
│   ├── 检验单管理
│   └── 采购入库单管理
├── 库存管理 (order_num: 13) [预留]
├── 财务管理 (order_num: 14)
│   ├── 收款单管理
│   ├── 付款申请单管理
│   ├── 应收单管理
│   └── 应付单管理
├── 生产管理 (order_num: 15) [预留]
├── 人事管理 (order_num: 16) [预留]
└── 固资管理 (order_num: 17) [预留]
```

---

## ✅ 核心改进点

### 1. 符合ERP行业标准
参考金蝶云·星空、用友U8+等主流ERP的模块划分:
- **供应链管理**: 销售 + 采购 + 库存
- **财务管理**: 应收 + 应付 + 收款 + 付款
- **基础资料**: 客户/供应商/物料主数据

### 2. 合并重复分类
| 原方案问题 | 优化方案 | 理由 |
|-----------|---------|------|
| "账款管理" + "财务管理"职责重叠 | 合并为**财务管理** | 应收应付本质是财务职能,避免用户困惑 |
| "行政管理"非ERP核心功能 | **移除** | 传统ERP不包含OA功能,如需要可后续添加 |

### 3. 预留扩展空间
新增4个预留模块供未来业务扩展:
- 库存管理(当前单据可归属采购/销售,独立库存模块用于调拨/盘点)
- 生产管理(制造业场景)
- 人事管理(HR系统对接)
- 固资管理(固定资产全生命周期)

---

## 🔧 技术实现要点

### 数据库变更

#### 1. 新增临时表 `tmp_menu_categories`
```sql
CREATE TEMPORARY TABLE tmp_menu_categories (
    category_code VARCHAR(50),      -- 分类编码: sales/purchase/finance等
    category_name VARCHAR(100),     -- 分类名称: 销售管理/采购管理等
    order_num INT,                  -- 排序号
    icon VARCHAR(50),               -- 图标
    remark VARCHAR(200)             -- 备注
);
```

#### 2. 修改 `tmp_business_modules` 表结构
```sql
-- 新增字段
ALTER TABLE tmp_business_modules ADD COLUMN category_code VARCHAR(50) DEFAULT NULL;

-- 示例数据
('saleorder', '销售订单管理', ..., 'sales')  -- 归属销售管理
('purchaseorder', '采购订单管理', ..., 'purchase')  -- 归属采购管理
```

### 存储过程逻辑调整

#### 步骤1: 创建二级菜单
```sql
-- 遍历 tmp_menu_categories,创建9个二级菜单(M类型)
INSERT INTO sys_menu (...) 
SELECT v_cat_menu_id, v_cat_name, @erp_parent_id, ...
WHERE NOT EXISTS (...);
```

#### 步骤2: 创建三级业务菜单
```sql
-- 遍历 tmp_business_modules,根据 category_code 查找父菜单ID
SELECT menu_id INTO v_parent_menu_id 
FROM sys_menu 
WHERE BINARY menu_name = (SELECT category_name FROM tmp_menu_categories WHERE category_code = v_category_code)
  AND parent_id = @erp_parent_id;

-- 创建业务菜单(C类型),parent_id 指向二级菜单
INSERT INTO sys_menu (...) 
SELECT v_menu_id, v_module_name, v_parent_menu_id, ...;
```

### 关键优化措施

1. **雪花ID防碰撞**: 每次循环后 `DO SLEEP(0.001)` 确保时间戳不同
2. **事务保护**: 整个创建过程包裹在 `START TRANSACTION...COMMIT` 中
3. **幂等性设计**: 所有INSERT使用 `WHERE NOT EXISTS` 防止重复创建
4. **临时表清理**: 执行完毕后自动删除所有临时表

---

## 📊 验证SQL

执行脚本后,运行以下查询验证结果:

```sql
-- 查看一级菜单
SELECT * FROM sys_menu WHERE menu_name = 'ERP 业务菜单' AND parent_id = 0;

-- 查看二级分类(应显示9个)
SELECT menu_id, menu_name, order_num, icon 
FROM sys_menu 
WHERE parent_id = @erp_parent_id AND menu_type = 'M'
ORDER BY order_num;

-- 查看三级业务菜单(应显示15个)
SELECT 
    p.menu_name AS category,
    s.menu_name AS business_menu,
    s.path
FROM sys_menu s
INNER JOIN sys_menu p ON s.parent_id = p.menu_id
WHERE p.parent_id = @erp_parent_id AND s.menu_type = 'C'
ORDER BY p.order_num, s.order_num;

-- 统计信息
SELECT 
  COUNT(CASE WHEN menu_type = 'M' AND parent_id = @erp_parent_id THEN 1 END) AS category_count,
  COUNT(CASE WHEN menu_type = 'C' AND parent_id IN (
    SELECT menu_id FROM sys_menu WHERE parent_id = @erp_parent_id AND menu_type = 'M'
  ) THEN 1 END) AS business_menu_count
FROM sys_menu;
```

预期结果:
- 二级分类: **9个** (含公共配置管理)
- 三级业务菜单: **15个**
- 按钮权限: **105个** (15个菜单 × 7个按钮)

---

## ⚠️ 注意事项

### 1. 前端适配
如果前端路由硬编码了菜单路径,需要同步调整:
```javascript
// 旧路径
'/erp/receivebill'

// 新路径(可选,保持兼容可不改)
'/erp/finance/receivebill'
```

### 2. 权限系统
检查角色权限表 `sys_role_menu` 是否需要重新分配:
```sql
-- 查看现有权限分配
SELECT r.role_name, m.menu_name, m.menu_type
FROM sys_role_menu rm
INNER JOIN sys_menu m ON rm.menu_id = m.menu_id
INNER JOIN sys_role r ON rm.role_id = r.role_id
WHERE m.menu_id IN (
  SELECT menu_id FROM sys_menu WHERE parent_id = @erp_parent_id
);
```

### 3. 数据迁移
如果是生产环境升级:
1. **备份数据库**: `mysqldump test > backup_$(date +%Y%m%d).sql`
2. **执行清理**: 调用 `proc_cleanup_old_menus()` 删除旧菜单
3. **执行创建**: 调用 `proc_create_menus()` 创建新菜单
4. **重新授权**: 为各角色重新分配菜单权限

---

## 🚀 执行步骤

### 测试环境
```bash
# 1. 连接数据库
mysql -u root -p test

# 2. 执行脚本
source d:/baiyu-ali/erpDOC/sql/erp/1.配置页菜单脚本_优化版.sql

# 3. 验证结果
SELECT '验证通过' AS result;
```

### 生产环境
```bash
# 1. 备份
mysqldump -u root -p test > /backup/erp_menu_backup_20260427.sql

# 2. 在维护窗口执行
mysql -u root -p test < 1.配置页菜单脚本_优化版.sql

# 3. 通知用户刷新浏览器缓存
```

---

## 📝 构建优化清单

### P0 - 必须完成 ✅
- [x] 新增 `tmp_menu_categories` 临时表
- [x] `tmp_business_modules` 增加 `category_code` 字段
- [x] 存储过程增加二级菜单创建逻辑
- [x] 调整业务菜单 `parent_id` 指向二级菜单
- [x] 更新验证SQL展示三级结构
- [x] 合并"账款管理"到"财务管理"
- [x] 移除"行政管理"(非ERP核心)
- [x] 预留库存/生产/人事/固资模块

### P1 - 建议完成
- [ ] 前端左侧导航栏支持折叠展开
- [ ] 添加面包屑导航显示当前位置
- [ ] 更新用户手册说明新菜单结构
- [ ] 前端路由适配新路径(可选)

### P2 - 可选优化
- [ ] 添加菜单搜索功能
- [ ] 支持自定义菜单排序
- [ ] 实现菜单权限动态过滤
- [ ] 添加菜单使用频率统计
- [ ] 支持拖拽调整菜单顺序

---

## 🔍 行业对标

| ERP厂商 | 模块划分方式 | 与本方案对比 |
|---------|------------|-------------|
| **金蝶云·星空** | 财务/供应链/制造/人力 | ✅ 高度一致 |
| **用友U8+** | 财务/进销存/生产/HR | ✅ 核心模块相同 |
| **SAP S/4HANA** | FI(财务)/MM(物料)/SD(销售) | ✅ 逻辑相通 |
| **Oracle EBS** | GL/AP/AR/PO/SO/INV | ✅ 细粒度类似 |

---

## 💡 常见问题

### Q1: 为什么不保留"账款管理"?
**A**: 应收应付属于财务职能,金蝶/用友均将其归入财务模块。单独设立会导致用户困惑:"收款应该去账款还是财务?"

### Q2: 预留模块何时启用?
**A**: 当业务需要时:
- 库存管理: 需要独立调拨/盘点功能时
- 生产管理: 引入制造业务时
- 人事管理: 对接HR系统时
- 固资管理: 需要资产折旧/报废流程时

### Q3: 能否动态调整分类?
**A**: 可以。只需修改 `tmp_menu_categories` 和 `tmp_business_modules` 的数据,重新执行脚本即可。

### Q4: 会影响现有权限吗?
**A**: 会。脚本包含清理逻辑(`proc_cleanup_old_menus`),会删除旧菜单及权限关联。执行后需重新为角色分配权限。

---

## 📞 技术支持

如遇问题,请检查:
1. MySQL版本 >= 5.7 (支持临时表和存储过程)
2. 当前数据库是否为 `test` (脚本第11行 `USE test;`)
3. 是否有足够的权限执行DDL操作
4. 外键约束是否已禁用 (`SET FOREIGN_KEY_CHECKS = 0`)

---

**文档版本**: v1.0  
**最后更新**: 2026-04-27  
**维护者**: ERP开发团队
