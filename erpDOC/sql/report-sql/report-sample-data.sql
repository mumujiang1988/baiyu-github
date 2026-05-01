-- ============================================
-- 报表模块示例数据初始化脚本
-- 用途: 初始化销售订单部门汇总表示例配置
-- 作者: Lingma
-- 日期: 2026-04-30
-- 版本: v2.0
-- 
-- 【重要】使用说明:
-- 1. 执行前请确保已运行 report-module-production-ready.sql 创建表结构和基础字典
-- 2. 本脚本支持重复执行(使用 ON DUPLICATE KEY UPDATE)
-- 3. 使用命令: mysql -u root -p your_database < report-sample-data.sql
-- 4. 本脚本仅包含1个示例报表配置: 销售订单部门汇总
-- ============================================

SET NAMES utf8mb4 COLLATE utf8mb4_general_ci;
SET CHARACTER SET utf8mb4;

-- ============================================
-- 选择目标数据库(请根据实际情况修改)
-- ============================================
USE test;  -- ⚠️ 如果需要其他数据库,请修改此行
SELECT CONCAT('✅ 已切换到数据库: ', DATABASE()) AS database_info;

-- ============================================
-- 第一步: 清理旧数据(自动删除,确保完全重置)
-- ============================================
SELECT '========================================' AS '';
SELECT '⚠️  正在清理旧报表配置数据...' AS warning;
SELECT '========================================' AS '';

DELETE FROM rpt_report_config WHERE report_code = 'sales_dept_summary';
SELECT CONCAT('✅ 已删除旧配置: sales_dept_summary') AS progress;

SELECT '========================================' AS '';
SELECT '开始初始化示例报表配置...' AS progress;
SELECT '========================================' AS '';

-- ============================================
-- 第一步: 初始化示例报表数据
-- ============================================

-- 1.1 销售订单部门汇总表
INSERT INTO `rpt_report_config` (
  `report_code`, 
  `report_name`, 
  `report_type`,
  `category`,
  `parent_category`,
  `sort_order`,
  `sql_template`,
  `field_config`,
  `dimension_config`,
  `drill_config`,
  `permission_config`,
  `status`,
  `remark`,
  `create_by`
) VALUES (
  'sales_dept_summary',
  '销售订单部门汇总',
  'DEPT_ANALYSIS',
  'SALES',  -- 分类: 销售报表
  NULL,     -- 顶级分类
  1,        -- 排序号
  
  -- SQL模板
  -- 特殊占位符语法：${paramName:columnName} → 自动生成 BETWEEN 子句
  -- 示例：${orderDate:so.FDate} → so.FDate BETWEEN '2026-04-01' AND '2026-04-30'
  '{
    "query": "SELECT d.dept_id, d.dept_name, COUNT(DISTINCT so.FBillNo) AS order_count, SUM(so.FBillAmount) AS total_amount, AVG(sc.F_jlrl) AS avg_profit_rate, SUM(sc.F_jlre) AS total_profit, so.FDocumentStatus AS document_status FROM t_sale_order so LEFT JOIN sys_employee emp ON so.FSalerId = emp.salesman_id LEFT JOIN sys_dept d ON emp.f_bw = d.Kingdee_department_ID LEFT JOIN t_sale_order_cost sc ON so.f_id = sc.sale_order_id WHERE ${orderDate:so.FDate} GROUP BY d.dept_id, d.dept_name, so.FDocumentStatus ORDER BY total_amount DESC",
    "params": [
      {"name": "orderDate", "controlType": "daterange", "required": false, "defaultValue": "", "label": "订单日期"}
    ]
  }',
  
  -- 字段配置
  -- 【字段配置说明】
  -- - fieldName: 数据库字段名（必须与SQL查询结果的列名一致）
  -- - label: 显示名称
  -- - width: 列宽（像素）
  -- - format: 格式化规则（currency, #,###, #.##% 等）
  -- - dictType: 字典类型（可选），用于将代码值翻译为中文
  --   示例：{"fieldName": "status", "dictType": "f_document_status"}
  --   效果：表格中 "A" 显示为 "创建"，"B" 显示为 "审核中"
  -- - hidden: 是否隐藏（true/false）
  -- - sortable: 是否可排序（true/false）
  '[
    {"fieldName": "dept_id", "label": "部门ID", "width": 100, "hidden": true},
    {"fieldName": "dept_name", "label": "部门名称", "width": 150},
    {"fieldName": "order_count", "label": "订单数量", "width": 120, "format": "#,###", "sortable": true},
    {"fieldName": "total_amount", "label": "销售总额", "width": 150, "format": "currency", "sortable": true},
    {"fieldName": "avg_profit_rate", "label": "平均净利润率", "width": 140, "format": "#.##%", "sortable": true},
    {"fieldName": "total_profit", "label": "净利润总额", "width": 150, "format": "currency", "sortable": true},
    {"fieldName": "document_status", "label": "单据状态", "width": 120, "dictType": "f_document_status"}
  ]',
  
  -- 维度配置
  '{
    "primary": "dept",
    "time_range": {"field": "FDate", "granularity": "month"},
    "filters": ["dept", "date_range"]
  }',
  
  -- 穿透配置
  '{
    "levels": [
      {
        "level": 1,
        "name": "部门汇总",
        "field": "dept_name",
        "next_level": "salesman_detail",
        "params_mapping": {"dept_id": "dept_id"}
      },
      {
        "level": 2,
        "name": "销售员明细",
        "field": "salesman_name",
        "sql_template": "SELECT emp.fname AS salesman_name, emp.salesman_id, COUNT(*) AS order_count, SUM(so.FBillAmount) AS total_amount, AVG(sc.F_jlrl) AS avg_profit_rate FROM t_sale_order so LEFT JOIN sys_employee emp ON so.FSalerId = emp.salesman_id LEFT JOIN sys_dept d_filter ON emp.f_bw = d_filter.Kingdee_department_ID AND d_filter.dept_id = ${dept_id} LEFT JOIN t_sale_order_cost sc ON so.f_id = sc.sale_order_id WHERE ${orderDate:so.FDate} GROUP BY emp.salesman_id, emp.fname ORDER BY total_amount DESC",
        "next_level": "material_detail",
        "params_mapping": {"salesman_id": "salesman_id"}
      },
      {
        "level": 3,
        "name": "产品明细",
        "field": "material_name",
        "sql_template": "SELECT se.fplanmaterialname AS material_name, se.f_cplb AS category, SUM(se.fqty) AS qty, SUM(se.fallamount) AS amount FROM t_sale_order so LEFT JOIN t_sale_order_entry se ON so.FBillNo = se.fbillno WHERE so.FSalerId = ${salesman_id} AND ${orderDate:so.FDate} GROUP BY se.fplanmaterialname, se.f_cplb ORDER BY amount DESC",
        "next_level": "order_list",
        "params_mapping": {"material_name": "material_name"}
      },
      {
        "level": 4,
        "name": "订单列表",
        "field": "FBillNo",
        "sql_template": "SELECT so.FBillNo, so.FDate, so.FCustId, so.FBillAmount, sc.F_jlre AS profit FROM t_sale_order so LEFT JOIN t_sale_order_cost sc ON so.f_id = sc.sale_order_id LEFT JOIN t_sale_order_entry se ON so.FBillNo = se.fbillno WHERE so.FSalerId = ${salesman_id} AND se.fplanmaterialname = ${material_name} AND ${orderDate:so.FDate}",
        "next_level": null,
        "action": "navigate_to_module",
        "target_module": "saleorder",
        "params_mapping": {"bill_no": "FBillNo"}
      }
    ]
  }',
  
  -- 权限配置(统一格式: reportLevel + fieldLevel)
  '{
    "reportLevel": {
      "roles": ["*"]
    },
    "fieldLevel": {
      "total_amount": {"visibleRoles": ["SALES_MANAGER", "FINANCE_MANAGER"]},
      "avg_profit_rate": {"visibleRoles": ["SALES_MANAGER"]}
    },
    "canExport": true
  }',
  
  '1',
  '按部门统计销售订单数量、金额及净利润情况,支持穿透到销售员和订单详情',
  'system'
) ;

SELECT '✅ 示例报表: 销售订单部门汇总 插入成功' AS progress;

-- ============================================
-- 第二步: 验证结果
-- ============================================

SELECT '========================================' AS '';
SELECT '✅ 示例报表配置初始化完成!' AS result;
SELECT '========================================' AS '';
SELECT CONCAT('已初始化示例报表: ', COUNT(*)) AS summary FROM rpt_report_config WHERE report_code = 'sales_dept_summary';
SELECT '========================================' AS '';

-- ============================================
-- 验证提示
-- ============================================
SELECT '验证命令:' AS tip;
SELECT 'SELECT report_code, report_name, category FROM rpt_report_config ORDER BY category, sort_order;' AS cmd;
SELECT 'SELECT dict_type, dict_label, dict_value FROM rpt_dict_config ORDER BY dict_type, sort_order;' AS cmd;

-- ============================================
-- 第三步: 创建性能优化索引
-- ============================================

-- 销售订单表日期索引（核心报表依赖）
CREATE INDEX IF NOT EXISTS idx_sale_order_fdate ON t_sale_order(FDate);

-- JOIN关联字段索引（提升多表查询性能）
CREATE INDEX IF NOT EXISTS idx_employee_salesman_id ON sys_employee(salesman_id);
CREATE INDEX IF NOT EXISTS idx_dept_kingdee_id ON sys_dept(Kingdee_department_ID);
CREATE INDEX IF NOT EXISTS idx_cost_sale_order_id ON t_sale_order_cost(sale_order_id);

-- 采购订单表日期索引（如果有采购报表）
-- CREATE INDEX IF NOT EXISTS idx_purchase_order_date ON t_purchase_order(order_date);

-- 库存盘点表日期索引（如果有库存报表）
-- CREATE INDEX IF NOT EXISTS idx_inventory_check_date ON t_inventory(check_date);

SELECT '✅ 性能优化索引创建完成' AS progress;
