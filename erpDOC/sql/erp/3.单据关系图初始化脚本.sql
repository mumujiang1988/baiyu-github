-- =============================================
-- 单据关系图 V1.0 - 数据库初始化脚本
-- 版本: V1.0.1（含优化补丁）
-- 日期: 2026-04-14
-- 说明: 创建配置表、初始数据、存储过程及性能优化
-- =============================================

-- 1. 创建单据基础配置表
DROP TABLE IF EXISTS `erp_bill_relation_config`;
CREATE TABLE `erp_bill_relation_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `table_code` varchar(100) NOT NULL COMMENT '单据表名（单据类型，如t_sale_order）',
  `bill_name` varchar(100) NOT NULL COMMENT '单据显示名称（前端展示用）',
  `pk_field` varchar(50) NOT NULL DEFAULT 'id' COMMENT '单据表主键字段（默认id）',
  `bill_no_field` varchar(50) NOT NULL COMMENT '单据编号字段（如order_no、po_no，必须配置）',
  `predecessor_field` varchar(50) DEFAULT NULL COMMENT '前置关联字段（如f_post_position）',
  `successor_field` varchar(50) DEFAULT NULL COMMENT '后置关联字段（如f_front）',
  `show_fields` varchar(1000) DEFAULT NULL COMMENT '额外展示字段，逗号分隔（如create_time,total_amount）',
  `sort_num` int DEFAULT 0 COMMENT '关系链排序号（控制单据展示顺序）',
  `enable` tinyint(1) DEFAULT 1 COMMENT '是否启用：1=启用，0=停用（停用后不参与关系链查询）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_table_code` (`table_code`) COMMENT '单据表名唯一，避免重复配置'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单据关系图-单据基础配置表';

-- 【优化】添加复合索引提升查询性能
ALTER TABLE erp_bill_relation_config 
ADD INDEX idx_enable_sort (enable, sort_num) COMMENT '优化启用状态+排序查询';

-- 2. 插入单据基础配置初始数据
-- 【重要】bill_no_field 必须与实际表结构中的单据编号字段一致 
INSERT INTO `test`.`erp_bill_relation_config` (`id`, `table_code`, `bill_name`, `pk_field`, `bill_no_field`, `predecessor_field`, `successor_field`, `show_fields`, `sort_num`, `enable`, `remark`) VALUES (1, 't_sale_xsddxx', '销售订单信息', 'id', 'f_bill_no', 'f_post_position', 'f_front', 'f_bill_no', 1, 1, '销售订单原始信息');
INSERT INTO `test`.`erp_bill_relation_config` (`id`, `table_code`, `bill_name`, `pk_field`, `bill_no_field`, `predecessor_field`, `successor_field`, `show_fields`, `sort_num`, `enable`, `remark`) VALUES (2, 't_sale_order', '销售订单', 'id', 'FBillNo', 'f_post_position', 'f_front', 'FBillNo', 2, 1, '正式销售订单');
INSERT INTO `test`.`erp_bill_relation_config` (`id`, `table_code`, `bill_name`, `pk_field`, `bill_no_field`, `predecessor_field`, `successor_field`, `show_fields`, `sort_num`, `enable`, `remark`) VALUES (3, 'delivery_notice', '发货通知单', 'id', 'FBillNo', 'f_post_position', 'f_front', 'FBillNo', 7, 1, '销售发货通知');
INSERT INTO `test`.`erp_bill_relation_config` (`id`, `table_code`, `bill_name`, `pk_field`, `bill_no_field`, `predecessor_field`, `successor_field`, `show_fields`, `sort_num`, `enable`, `remark`) VALUES (4, 'f_sal_outbound', '销售出库单', 'id', 'f_bill_no', 'f_post_position', 'f_front', 'f_bill_no', 8, 1, '销售出库记录');
INSERT INTO `test`.`erp_bill_relation_config` (`id`, `table_code`, `bill_name`, `pk_field`, `bill_no_field`, `predecessor_field`, `successor_field`, `show_fields`, `sort_num`, `enable`, `remark`) VALUES (5, 't_ar_receivable', '应收单', 'id', 'f_bill_no', 'f_post_position', 'f_front', 'f_bill_no', 9, 1, '销售应收记录');
INSERT INTO `test`.`erp_bill_relation_config` (`id`, `table_code`, `bill_name`, `pk_field`, `bill_no_field`, `predecessor_field`, `successor_field`, `show_fields`, `sort_num`, `enable`, `remark`) VALUES (6, 'po_order_bill_head', '采购订单', 'id', 'fbill_no', 'f_post_position', 'f_front', 'fbill_no', 3, 1, '正式采购订单');
INSERT INTO `test`.`erp_bill_relation_config` (`id`, `table_code`, `bill_name`, `pk_field`, `bill_no_field`, `predecessor_field`, `successor_field`, `show_fields`, `sort_num`, `enable`, `remark`) VALUES (7, 'receive_notice', '收料通知单', 'id', 'FBillNo', 'f_post_position', 'f_front', 'FBillNo', 4, 1, '采购收料通知');
INSERT INTO `test`.`erp_bill_relation_config` (`id`, `table_code`, `bill_name`, `pk_field`, `bill_no_field`, `predecessor_field`, `successor_field`, `show_fields`, `sort_num`, `enable`, `remark`) VALUES (8, 'purchase_instock', '采购入库单', 'id', 'f_bill_no', 'f_post_position', 'f_front', 'f_bill_no', 5, 1, '采购入库记录');
INSERT INTO `test`.`erp_bill_relation_config` (`id`, `table_code`, `bill_name`, `pk_field`, `bill_no_field`, `predecessor_field`, `successor_field`, `show_fields`, `sort_num`, `enable`, `remark`) VALUES (9, 'f_scp_payable', '应付单', 'id', 'f_bill_no', 'f_post_position', 'f_front', 'f_bill_no', 6, 1, '采购应付记录');




INSERT INTO `test`.`t_identifier_dictionary` (`id`, `unique_identifier`, `business_name`, `module_code`, `source`, `goal`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (1, '25a571b5-a3ef-4675-b819-6533e420ade4', 't_sale_xsddxx', 'saleorderinfo', 't_sale_xsddxx', 't_sale_order', '销售订单信息到销售订单', '100881', '2026-04-09 16:06:21', '', NULL);
INSERT INTO `test`.`t_identifier_dictionary` (`id`, `unique_identifier`, `business_name`, `module_code`, `source`, `goal`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (2, 'b8278634-c73e-4e6a-b4a0-5a37a27d3d6f', 'po_order_bill_head', 'purchaseorder', 't_sale_order', 'po_order_bill_head', '销售订单至采购订单', '100881', '2026-04-09 16:15:46', '', NULL);
INSERT INTO `test`.`t_identifier_dictionary` (`id`, `unique_identifier`, `business_name`, `module_code`, `source`, `goal`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (3, 'db344d6b-7c77-4fb5-8119-b8f7dd1ec1ef', 'receive_notice', 'receivenotice', 'po_order_bill_head', 'receive_notice', '采购订单至收料通知单', '100881', '2026-04-09 16:19:03', '', NULL);
INSERT INTO `test`.`t_identifier_dictionary` (`id`, `unique_identifier`, `business_name`, `module_code`, `source`, `goal`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (4, 'c4debc71-4a52-4c73-877b-d7716a33c612', 'purchase_instock', 'purchaseinstock', 'receive_notice', 'purchase_instock', '采购收料单至采购入库单', '100881', '2026-04-09 16:25:37', '', NULL);
INSERT INTO `test`.`t_identifier_dictionary` (`id`, `unique_identifier`, `business_name`, `module_code`, `source`, `goal`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (5, '2808145c-0a15-46ca-9977-a64e40ac4b76', 'f_scp_payable', 'payable', 'purchase_instock', 'f_scp_payable', '采购入库单至应付单', '100881', '2026-04-09 16:26:45', '', NULL);
INSERT INTO `test`.`t_identifier_dictionary` (`id`, `unique_identifier`, `business_name`, `module_code`, `source`, `goal`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (6, 'a32b3262-87ab-4c53-802f-f992bd06782a', 'delivery_notice', 'deliverynotice', 't_sale_order', 'delivery_notice', '销售订单至发货通知单', '100881', '2026-04-09 16:28:44', '', NULL);
INSERT INTO `test`.`t_identifier_dictionary` (`id`, `unique_identifier`, `business_name`, `module_code`, `source`, `goal`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (7, 'd8f8fedc-885b-45cd-a703-69296ae86ee6', 'f_sal_outbound', 'saloutbound', 'delivery_notice', 'f_sal_outbound', '发货通知单至销售出库单', '100881', '2026-04-09 16:29:50', '', NULL);
INSERT INTO `test`.`t_identifier_dictionary` (`id`, `unique_identifier`, `business_name`, `module_code`, `source`, `goal`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (8, '29839606-71d6-4293-9dc3-44fc866b4894', 't_ar_receivable', 'receivable', 'f_sal_outbound', 't_ar_receivable', '销售出库单至应收单', '100881', '2026-04-09 16:30:53', '', NULL);
