/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80045 (8.0.45)
 Source Host           : localhost:3306
 Source Schema         : test

 Target Server Type    : MySQL
 Target Server Version : 80045 (8.0.45)
 File Encoding         : 65001

 Date: 28/03/2026 11:59:28
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `dict_id` bigint NOT NULL COMMENT '字典主键',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_id`) USING BTREE,
  UNIQUE INDEX `dict_type`(`dict_type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, '用户性别', 'sys_user_sex', '1', 'admin', '2024-06-13 16:06:35', '', NULL, '用户性别列表');
INSERT INTO `sys_dict_type` VALUES (2, '菜单状态', 'sys_show_hide', '1', 'admin', '2024-06-13 16:06:35', '', NULL, '菜单状态列表');
INSERT INTO `sys_dict_type` VALUES (3, '系统开关', 'sys_normal_disable', '1', 'admin', '2024-06-13 16:06:35', '', NULL, '系统开关列表');
INSERT INTO `sys_dict_type` VALUES (6, '系统是否', 'sys_yes_no', '1', 'admin', '2024-06-13 16:06:35', '', NULL, '系统是否列表');
INSERT INTO `sys_dict_type` VALUES (7, '通知类型', 'sys_notice_type', '1', 'admin', '2024-06-13 16:06:35', '', NULL, '通知类型列表');
INSERT INTO `sys_dict_type` VALUES (8, '通知状态', 'sys_notice_status', '1', 'admin', '2024-06-13 16:06:35', '', NULL, '通知状态列表');
INSERT INTO `sys_dict_type` VALUES (9, '操作类型', 'sys_oper_type', '1', 'admin', '2024-06-13 16:06:35', '', NULL, '操作类型列表');
INSERT INTO `sys_dict_type` VALUES (10, '系统状态', 'sys_common_status', '1', 'admin', '2024-06-13 16:06:36', '', NULL, '登录状态列表');
INSERT INTO `sys_dict_type` VALUES (1812692454547488770, '企业类型', 'merchant_type', '1', 'admin', '2024-07-15 11:35:34', 'admin', '2024-07-16 17:41:32', '企业类型');
INSERT INTO `sys_dict_type` VALUES (1813152108564373505, '入库状态', 'wms_receipt_status', '1', 'admin', '2024-07-16 18:02:04', 'admin', '2024-07-16 18:02:17', '入库状态');
INSERT INTO `sys_dict_type` VALUES (1814219082624778242, '入库类型', 'wms_receipt_type', '1', 'admin', '2024-07-19 16:41:51', 'admin', '2024-07-19 16:41:51', NULL);
INSERT INTO `sys_dict_type` VALUES (1818848671749709825, '出库状态', 'wms_shipment_status', '1', 'admin', '2024-08-01 11:18:11', 'admin', '2024-08-01 11:18:11', NULL);
INSERT INTO `sys_dict_type` VALUES (1818848738502057985, '出库类型', 'wms_shipment_type', '1', 'admin', '2024-08-01 11:18:26', 'admin', '2024-08-01 11:18:26', NULL);
INSERT INTO `sys_dict_type` VALUES (1821066855638630402, '库存记录操作类型', 'wms_inventory_history_type', '1', 'admin', '2024-08-07 14:12:27', 'admin', '2024-08-07 14:12:27', NULL);
INSERT INTO `sys_dict_type` VALUES (1822820566366982146, '移库状态', 'wms_movement_status', '1', 'admin', '2024-08-12 10:21:04', 'admin', '2024-08-12 10:21:04', NULL);
INSERT INTO `sys_dict_type` VALUES (1823182238898274306, '盘库状态', 'wms_check_status', '1', 'admin', '2024-08-13 10:18:14', 'admin', '2024-08-13 10:18:14', NULL);
INSERT INTO `sys_dict_type` VALUES (2009451096278798337, '物料状态', 'material_status', '1', '吴晓晓', '2026-01-09 10:24:10', '吴晓晓', '2026-01-09 10:24:10', '物料状态');
INSERT INTO `sys_dict_type` VALUES (2010596734248939521, '供应商职务', 'supplier_position', '1', 'admin', '2026-01-12 14:16:31', 'admin', '2026-01-12 14:16:31', '供应商职务');
INSERT INTO `sys_dict_type` VALUES (2014631243655024641, '优先排款', 'priority_payment', '1', 'admin', '2026-01-23 17:28:13', 'admin', '2026-01-23 17:28:13', '优先排款');
INSERT INTO `sys_dict_type` VALUES (2026166405790388225, '单据状态', 'f_document_status', '1', '周艳红', '2026-02-24 13:24:50', '周艳红', '2026-02-24 13:24:50', '客户单据状态');
INSERT INTO `sys_dict_type` VALUES (2029006737896804353, '是否首单', 'f_sfsd', '1', '周艳红', '2026-03-04 09:31:18', '周艳红', '2026-03-04 09:31:18', '是否首单');
INSERT INTO `sys_dict_type` VALUES (2029009137273573378, '商标授权', 'f_sbsq', '1', '周艳红', '2026-03-04 09:40:50', '周艳红', '2026-03-04 09:40:50', '商标授权');
INSERT INTO `sys_dict_type` VALUES (2031634305997230081, '价目表对象', 'price_object', '1', 'admin', '2026-03-11 15:32:19', 'admin', '2026-03-11 15:32:19', '价目表对象');
INSERT INTO `sys_dict_type` VALUES (2032320827088068610, '是否用于业务询价', 'business_inquiry', '1', 'admin', '2026-03-13 13:00:19', 'admin', '2026-03-13 13:00:19', '是否用于业务询价');
INSERT INTO `sys_dict_type` VALUES (2032618796060413953, '中性说明书', 'f_zxsms', '1', 'admin', '2026-03-14 08:44:20', 'admin', '2026-03-14 08:44:20', '中性说明书');
INSERT INTO `sys_dict_type` VALUES (2034567890123456001, '币种', 'currency', '1', 'admin', '2026-01-09 10:00:00', '', NULL, '币种列表');
INSERT INTO `sys_dict_type` VALUES (2034567890234567002, '付款条款', 'payment_clause', '1', 'admin', '2026-01-09 10:01:00', '', NULL, '付款条款列表');
INSERT INTO `sys_dict_type` VALUES (2034567890345678003, '贸易方式', 'trade_way', '1', 'admin', '2026-01-09 10:02:00', '', NULL, '贸易方式列表');
INSERT INTO `sys_dict_type` VALUES (2034567890456789004, '产品分类', 'product_category', '1', 'admin', '2026-01-09 10:03:00', '', NULL, '产品分类列表');
INSERT INTO `sys_dict_type` VALUES (2034567890567890005, '客户分类', 'customer_category', '1', 'admin', '2026-01-09 10:04:00', '', NULL, '客户分类列表');
INSERT INTO `sys_dict_type` VALUES (2034567890678901006, '客户分组', 'customer_grouping', '1', 'admin', '2026-01-09 10:05:00', '', NULL, '客户分组列表');
INSERT INTO `sys_dict_type` VALUES (2034567890789012007, '客户来源', 'customer_source', '1', 'admin', '2026-01-09 10:06:00', '', NULL, '客户来源列表');
INSERT INTO `sys_dict_type` VALUES (2034567890890123008, '单据类型', 'document_type', '1', 'admin', '2026-01-09 10:07:00', '', NULL, '单据类型列表');
INSERT INTO `sys_dict_type` VALUES (2034567890901234009, '库存状态', 'inventory_status', '1', 'admin', '2026-01-09 10:08:00', '', NULL, '库存状态列表');
INSERT INTO `sys_dict_type` VALUES (2034567891012345010, '发票类型', 'invoice_type', '1', 'admin', '2026-01-09 10:09:00', '', NULL, '发票类型列表');
INSERT INTO `sys_dict_type` VALUES (2034567891123456011, '价格类型', 'price_type', '1', 'admin', '2026-01-09 10:10:00', '', NULL, '价格类型列表');
INSERT INTO `sys_dict_type` VALUES (2034567891234567012, '产品类型', 'product_type', '1', 'admin', '2026-01-09 10:11:00', '', NULL, '产品类型列表');
INSERT INTO `sys_dict_type` VALUES (2034567891345678013, '供应商分类', 'supplier_classification', '1', 'admin', '2026-01-09 10:12:00', '', NULL, '供应商分类列表');
INSERT INTO `sys_dict_type` VALUES (2034567891456789014, '供应类别', 'supply_category', '1', 'admin', '2026-01-09 10:13:00', '', NULL, '供应类别列表');
INSERT INTO `sys_dict_type` VALUES (2034567891567890015, '包装方式', 'manner_packing', '1', 'admin', '2026-01-09 10:14:00', '', NULL, '包装方式列表');
INSERT INTO `sys_dict_type` VALUES (2034567891678901016, '汇率类型', 'exchange_type', '1', 'admin', '2026-01-09 10:15:00', '', NULL, '汇率类型列表');
INSERT INTO `sys_dict_type` VALUES (2034567891789012017, '收款条款', 'collection_terms', '1', 'admin', '2026-01-09 10:16:00', '', NULL, '收款条款列表');
INSERT INTO `sys_dict_type` VALUES (2034567891890123018, '订单产品分类', 'order_product_category', '1', 'admin', '2026-01-09 10:17:00', '', NULL, '订单产品分类列表');
INSERT INTO `sys_dict_type` VALUES (2034567891901234019, '关税名称', 'tariff_nomenclature', '1', 'admin', '2026-01-09 10:18:00', '', NULL, '关税名称列表');
INSERT INTO `sys_dict_type` VALUES (2034567892012345020, 'ERP 分类属性', 'erpClsId_property', '1', 'admin', '2026-01-09 10:19:00', '', NULL, 'ERP 分类属性列表');

SET FOREIGN_KEY_CHECKS = 1;
