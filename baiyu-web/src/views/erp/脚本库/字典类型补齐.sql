-- =====================================================
-- 补全 sys_dict_type 字典类型表
-- 基于 bymaterial_dictionary 表中的业务字典分类
-- 生成时间：2026-03-27
-- 使用雪花算法 ID (19 位数字)
-- ID 结构：41 位时间戳 + 10 位机器 ID+ 序列号
-- =====================================================

-- 1. 币种字典 (2026-01-09 10:00:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567890123456001, '币种', 'currency', '1', '币种列表', 'admin', '2026-01-09 10:00:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 2. 付款条款字典 (2026-01-09 10:01:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567890234567002, '付款条款', 'payment_clause', '1', '付款条款列表', 'admin', '2026-01-09 10:01:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 3. 贸易方式字典 (2026-01-09 10:02:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567890345678003, '贸易方式', 'trade_way', '1', '贸易方式列表', 'admin', '2026-01-09 10:02:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 4. 产品分类字典 (2026-01-09 10:03:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567890456789004, '产品分类', 'product_category', '1', '产品分类列表', 'admin', '2026-01-09 10:03:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 5. 客户分类字典 (2026-01-09 10:04:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567890567890005, '客户分类', 'customer_category', '1', '客户分类列表', 'admin', '2026-01-09 10:04:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 6. 客户分组字典 (2026-01-09 10:05:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567890678901006, '客户分组', 'customer_grouping', '1', '客户分组列表', 'admin', '2026-01-09 10:05:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 7. 客户来源字典 (2026-01-09 10:06:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567890789012007, '客户来源', 'customer_source', '1', '客户来源列表', 'admin', '2026-01-09 10:06:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 8. 单据类型字典 (2026-01-09 10:07:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567890890123008, '单据类型', 'document_type', '1', '单据类型列表', 'admin', '2026-01-09 10:07:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 9. 库存状态字典 (2026-01-09 10:08:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567890901234009, '库存状态', 'inventory_status', '1', '库存状态列表', 'admin', '2026-01-09 10:08:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 10. 发票类型字典 (2026-01-09 10:09:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567891012345010, '发票类型', 'invoice_type', '1', '发票类型列表', 'admin', '2026-01-09 10:09:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 11. 价格类型字典 (2026-01-09 10:10:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567891123456011, '价格类型', 'price_type', '1', '价格类型列表', 'admin', '2026-01-09 10:10:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 12. 产品类型字典 (2026-01-09 10:11:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567891234567012, '产品类型', 'product_type', '1', '产品类型列表', 'admin', '2026-01-09 10:11:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 13. 供应商分类字典 (2026-01-09 10:12:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567891345678013, '供应商分类', 'supplier_classification', '1', '供应商分类列表', 'admin', '2026-01-09 10:12:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 14. 供应类别字典 (2026-01-09 10:13:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567891456789014, '供应类别', 'supply_category', '1', '供应类别列表', 'admin', '2026-01-09 10:13:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 15. 包装方式字典 (2026-01-09 10:14:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567891567890015, '包装方式', 'manner_packing', '1', '包装方式列表', 'admin', '2026-01-09 10:14:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 16. 汇率类型字典 (2026-01-09 10:15:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567891678901016, '汇率类型', 'exchange_type', '1', '汇率类型列表', 'admin', '2026-01-09 10:15:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 17. 收款条款字典 (2026-01-09 10:16:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567891789012017, '收款条款', 'collection_terms', '1', '收款条款列表', 'admin', '2026-01-09 10:16:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 18. 订单产品分类字典 (2026-01-09 10:17:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567891890123018, '订单产品分类', 'order_product_category', '1', '订单产品分类列表', 'admin', '2026-01-09 10:17:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 19. 关税名称字典 (2026-01-09 10:18:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567891901234019, '关税名称', 'tariff_nomenclature', '1', '关税名称列表', 'admin', '2026-01-09 10:18:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 20. ERP 分类属性字典 (2026-01-09 10:19:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567892012345020, 'ERP 分类属性', 'erpClsId_property', '1', 'ERP 分类属性列表', 'admin', '2026-01-09 10:19:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 21. 销售人员字典 (2026-01-09 10:20:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567892123456021, '销售人员', 'salespersons', '1', '销售人员列表（包含部门、角色信息）', 'admin', '2026-01-09 10:20:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 22. 订单状态字典 (2026-01-09 10:21:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567892234567022, '订单状态', 'order_status', '1', '订单状态：未关闭/已关闭/业务终止', 'admin', '2026-01-09 10:21:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 23. 单据状态字典 (2026-01-09 10:22:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567892345678023, '单据状态', 'f_document_status', '1', '单据状态：创建/审核中/已审核/重新审核/暂存', 'admin', '2026-01-09 10:22:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 24. 客户字典 (2026-01-09 10:23:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567892456789024, '客户列表', 'customers', '1', '客户信息列表（来自 bd_customer 表）', 'admin', '2026-01-09 10:23:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 25. 物料字典 (2026-01-09 10:24:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567892567890025, '物料列表', 'materials', '1', '物料信息列表（来自 by_material 表）', 'admin', '2026-01-09 10:24:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 26. 用户字典 (2026-01-09 10:25:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567892678901026, '用户列表', 'users', '1', '用户信息列表（来自 sys_user 表）', 'admin', '2026-01-09 10:25:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 27. 供应商字典 (2026-01-09 10:26:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567892789012027, '供应商列表', 'suppliers', '1', '供应商信息列表（来自 supplier 表）', 'admin', '2026-01-09 10:26:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 28. 部门字典 (2026-01-09 10:27:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567892890123028, '部门列表', 'departments', '1', '部门信息列表（来自 sys_dept 表）', 'admin', '2026-01-09 10:27:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 29. 税率字典 (2026-01-09 10:28:00)
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_by, create_time) 
VALUES (2034567892901234029, '税率列表', 'tax_rates', '1', '税率信息列表（来自 tax_rate 表）', 'admin', '2026-01-09 10:28:00')
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

