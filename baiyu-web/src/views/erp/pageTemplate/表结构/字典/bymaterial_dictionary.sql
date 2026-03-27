/*
 Navicat Premium Dump SQL

 Source Server         : test
 Source Server Type    : MySQL
 Source Server Version : 80030 (8.0.30)
 Source Host           : 118.178.144.159:3307
 Source Schema         : test

 Target Server Type    : MySQL
 Target Server Version : 80030 (8.0.30)
 File Encoding         : 65001

 Date: 20/03/2026 13:39:42
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for bymaterial_dictionary
-- ----------------------------
DROP TABLE IF EXISTS `bymaterial_dictionary`;
CREATE TABLE `bymaterial_dictionary`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '编码',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '名称',
  `kingdee` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类标识',
  `category_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_category_code`(`category` ASC, `code` ASC) USING BTREE,
  INDEX `idx_dict_cat_key`(`category` ASC, `kingdee` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 207 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '物料字典表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bymaterial_dictionary
-- ----------------------------
INSERT INTO `bymaterial_dictionary` VALUES (1, 'fdj', '发动机', '67c078780f4468', 'product_category', '产品类别', '2025-10-09 09:12:28', '2025-10-21 09:47:31');
INSERT INTO `bymaterial_dictionary` VALUES (2, 'dp', '底盘', '67c0788a0f446a', 'product_category', '产品类别', '2025-10-09 09:12:28', '2025-10-21 09:47:52');
INSERT INTO `bymaterial_dictionary` VALUES (3, 'cs', '车身', '67c0789b0f446c', 'product_category', '产品类别', '2025-10-09 09:12:28', '2025-10-21 09:48:01');
INSERT INTO `bymaterial_dictionary` VALUES (4, 'dq', '电气', '67c078a90f446e', 'product_category', '产品类别', '2025-10-09 09:12:28', '2025-10-21 09:48:07');
INSERT INTO `bymaterial_dictionary` VALUES (5, 'qtpj', '其他配件', '67c078b80f4470', 'product_category', '产品类别', '2025-10-09 09:12:28', '2025-10-21 09:48:14');
INSERT INTO `bymaterial_dictionary` VALUES (6, 'BZ', '包装', '67e0bc6a129320', 'product_category', '产品类别', '2025-10-09 09:12:28', '2025-10-21 09:48:20');
INSERT INTO `bymaterial_dictionary` VALUES (7, 'CSl', '车饰', '67e0d06e129c51', 'product_category', '产品类别', '2025-10-09 09:12:28', '2025-10-21 09:48:26');
INSERT INTO `bymaterial_dictionary` VALUES (8, 'wj', '五金', '67e0d08c129c8a', 'product_category', '产品类别', '2025-10-09 09:12:28', '2025-10-21 09:48:32');
INSERT INTO `bymaterial_dictionary` VALUES (9, '1', '新产品', '67eba32313e5e2', 'product_type', '新老产品', '2025-10-09 09:13:20', '2025-10-21 11:12:55');
INSERT INTO `bymaterial_dictionary` VALUES (10, '2', '老产品', '67eba33613e5e4', 'product_type', '新老产品', '2025-10-09 09:13:20', '2025-10-21 11:13:01');
INSERT INTO `bymaterial_dictionary` VALUES (13, '1', '外购', '1', 'erpClsId_property', '物料属性', '2025-10-09 09:13:34', '2025-12-16 08:17:54');
INSERT INTO `bymaterial_dictionary` VALUES (14, '10', '资产', '10', 'erpClsId_property', '物料属性', '2025-10-09 09:13:34', '2025-12-16 08:17:59');
INSERT INTO `bymaterial_dictionary` VALUES (15, '6', '服务', '6', 'erpClsId_property', '物料属性', '2025-10-09 09:13:34', '2025-12-16 08:18:01');
INSERT INTO `bymaterial_dictionary` VALUES (47, 'JC', '检测', '67f35889145d35', 'product_category', '产品类别', '2025-10-14 16:54:08', '2025-10-21 09:48:48');
INSERT INTO `bymaterial_dictionary` VALUES (48, '001', '潜在供应商', '638469ed8e365a', 'supplier_classification', '供应商分类', '2025-11-04 14:24:53', '2025-11-04 14:24:53');
INSERT INTO `bymaterial_dictionary` VALUES (49, '0002', '合作供应商', '638469fc8e365c', 'supplier_classification', '供应商分类', '2025-11-04 14:25:44', '2025-11-04 14:25:44');
INSERT INTO `bymaterial_dictionary` VALUES (50, '0003', '备选供应商', '652f683c70b45e', 'supplier_classification', '供应商分类', '2025-11-04 14:29:48', '2025-11-04 14:29:48');
INSERT INTO `bymaterial_dictionary` VALUES (51, '0004', '暂停合作', '6627473f3a89c5', 'supplier_classification', '供应商分类', '2025-11-04 14:30:27', '2025-11-04 14:30:27');
INSERT INTO `bymaterial_dictionary` VALUES (52, '0005', '物流/包装供应商', '66c3f3ca67970c', 'supplier_classification', '供应商分类', '2025-11-04 14:30:55', '2025-11-04 14:30:55');
INSERT INTO `bymaterial_dictionary` VALUES (53, '0006', '包材供应商', '66c3f81867971a', 'supplier_classification', '供应商分类', '2025-11-04 14:31:18', '2025-11-04 14:31:18');
INSERT INTO `bymaterial_dictionary` VALUES (54, '1688', '一六八八', '6628b3c33abd2f', 'customer_source', '供应商来源', '2025-11-04 14:49:23', '2025-11-12 09:43:08');
INSERT INTO `bymaterial_dictionary` VALUES (55, 'ME', '上海五金展会', '6628b3d23abd53', 'customer_source', '供应商来源', '2025-11-04 14:51:23', '2025-11-10 16:51:19');
INSERT INTO `bymaterial_dictionary` VALUES (57, 'CNY', '人民币', '1', 'currency', '币别', '2025-11-04 15:24:23', '2025-12-17 14:44:29');
INSERT INTO `bymaterial_dictionary` VALUES (58, 'HKD', '香港元', '2', 'currency', '币别', '2025-11-04 15:24:56', '2025-12-17 14:44:33');
INSERT INTO `bymaterial_dictionary` VALUES (59, 'EUR', '欧元', '3', 'currency', '币别', '2025-11-04 15:25:30', '2025-12-17 14:44:36');
INSERT INTO `bymaterial_dictionary` VALUES (60, 'JPY', '日本日圆', '4', 'currency', '币别', '2025-11-04 15:25:56', '2025-12-17 14:44:38');
INSERT INTO `bymaterial_dictionary` VALUES (61, 'TWD', '新台币元', '5', 'currency', '币别', '2025-11-04 15:26:34', '2025-12-17 14:44:41');
INSERT INTO `bymaterial_dictionary` VALUES (62, 'GBP', '英镑', '6', 'currency', '币别', '2025-11-04 15:27:06', '2025-12-17 14:44:43');
INSERT INTO `bymaterial_dictionary` VALUES (63, 'USD', '美元', '7', 'currency', '币别', '2025-11-04 15:27:42', '2025-12-17 14:44:47');
INSERT INTO `bymaterial_dictionary` VALUES (64, 'FKTJ01_SYS', '货到付款', '20000', 'payment_clause', '付款条件', '2025-11-04 16:29:26', '2025-11-04 16:29:26');
INSERT INTO `bymaterial_dictionary` VALUES (65, 'FKTJ02_SYS', '30天后付款', '20001', 'payment_clause', '付款条件', '2025-11-04 16:29:59', '2025-11-04 16:29:59');
INSERT INTO `bymaterial_dictionary` VALUES (66, 'FKTJ03_SYS', '月结30天', '20002', 'payment_clause', '付款条件', '2025-11-04 16:30:39', '2025-11-04 16:30:39');
INSERT INTO `bymaterial_dictionary` VALUES (67, 'FKTJ04_SYS', '多到期日（按金额），货到付款2万，30天后付款5万，余款60天后付清', '20003', 'payment_clause', '付款条件', '2025-11-04 16:31:35', '2025-11-04 16:31:35');
INSERT INTO `bymaterial_dictionary` VALUES (68, 'MONTHLY45 ', '月结45天', '181255', 'payment_clause', '付款条件\r\n', '2025-11-04 16:33:25', '2025-11-04 16:33:33');
INSERT INTO `bymaterial_dictionary` VALUES (69, 'ADVANCE_PAYMENT30%', '30%预付，尾款到货后45个工作日', '181256', 'payment_clause', '付款条件\r\n', '2025-11-04 16:35:33', '2025-11-04 16:35:33');
INSERT INTO `bymaterial_dictionary` VALUES (71, 'ADVANCE_PAYMENT50%', '50%预付款 尾款到货后30天', '181263', 'payment_clause', '付款条件\r\n', '2025-11-04 16:36:40', '2025-11-04 16:36:40');
INSERT INTO `bymaterial_dictionary` VALUES (72, 'BEFORE_DELIVERY', '款到发货', '181838', 'payment_clause', '付款条件', '2025-11-04 16:37:48', '2026-01-07 13:22:38');
INSERT INTO `bymaterial_dictionary` VALUES (73, '20-DAY PAYMENT', '20天付款', '181839', 'payment_clause', '付款条件', '2025-11-04 16:38:51', '2025-11-04 16:38:51');
INSERT INTO `bymaterial_dictionary` VALUES (74, 'BEFORE_SHIPMENT', '预付30%，尾款发货前付清', '185194', 'payment_clause', '付款条件', '2025-11-04 16:40:01', '2025-11-04 16:40:01');
INSERT INTO `bymaterial_dictionary` VALUES (75, '60Days', '月结60天', '234981', 'payment_clause', '付款条件', '2025-11-04 16:40:50', '2025-11-04 16:40:50');
INSERT INTO `bymaterial_dictionary` VALUES (76, 'ADVANCE', '20%预付款，60%款到发货；20%发货后30天', '239538', 'payment_clause', '付款条件', '2025-11-04 16:41:44', '2025-11-04 16:41:44');
INSERT INTO `bymaterial_dictionary` VALUES (77, 'PAYMENT_DUE_IN_30DAYS', '30%预付，尾款到货后30天', '239843', 'payment_clause', '付款条件', '2025-11-04 16:42:58', '2025-11-04 16:42:58');
INSERT INTO `bymaterial_dictionary` VALUES (78, 'VAT', '增值税专用发票', '1', 'Invoice_type', '发票类型', '2025-11-04 16:45:15', '2025-12-18 09:43:24');
INSERT INTO `bymaterial_dictionary` VALUES (79, 'GENERAL', '普通发票', '2', 'Invoice_type', '发票类型', '2025-11-04 16:45:51', '2026-01-21 14:07:46');
INSERT INTO `bymaterial_dictionary` VALUES (80, 'SFL02_SYS', '一般纳税人', '9e855eb97bec43e7b50c3e0e0bf51210', 'tariff_nomenclature', '税分类', '2025-11-04 16:48:38', '2025-11-04 16:48:38');
INSERT INTO `bymaterial_dictionary` VALUES (81, 'SFL01_SYS', '小规模纳税人', '386b5ca3155c492fbfcb7e8bd7861295', 'tariff_nomenclature', '税分类', '2025-11-04 16:49:57', '2025-11-04 16:49:57');
INSERT INTO `bymaterial_dictionary` VALUES (82, 'purchase', '采购', 'CG', 'Supply_category', '供应类别', '2025-11-18 09:58:42', '2026-01-20 09:41:29');
INSERT INTO `bymaterial_dictionary` VALUES (83, 'euphemistic', '委外', 'WW', 'Supply_category', '供应类别', '2025-11-18 09:59:44', '2026-01-29 10:20:07');
INSERT INTO `bymaterial_dictionary` VALUES (84, 'serve', '服务', 'FW', 'Supply_category', '供应类别', '2025-11-18 10:01:51', '2025-11-18 10:05:02');
INSERT INTO `bymaterial_dictionary` VALUES (85, 'synthesize', '综合', 'ZH', 'Supply_category', '供应类别', '2025-11-18 10:02:43', '2025-11-18 10:05:00');
INSERT INTO `bymaterial_dictionary` VALUES (86, 'cooperation', '合作', 'HZ', 'Supply_category', '供应类别', '2025-11-18 10:03:21', '2025-11-18 10:04:57');
INSERT INTO `bymaterial_dictionary` VALUES (87, 'potential', '潜在', 'QZ', 'Supply_category', '供应类别', '2025-11-18 10:04:51', '2025-11-18 10:04:51');
INSERT INTO `bymaterial_dictionary` VALUES (88, 'accept_carriage', '承运', 'CY', 'Supply_category', '供应类别', '2025-11-18 10:05:43', '2025-11-18 10:05:43');
INSERT INTO `bymaterial_dictionary` VALUES (89, 'purchase', '采购', '5', 'price_type', '价格类型', '2025-11-27 16:15:13', '2025-12-16 08:31:12');
INSERT INTO `bymaterial_dictionary` VALUES (90, 'outsource', '委外', '3', 'price_type', '价格类型', '2025-11-27 16:15:41', '2025-12-16 08:31:07');
INSERT INTO `bymaterial_dictionary` VALUES (91, 'vmi', 'VMI', '6', 'price_type', '价格类型', '2025-11-27 16:16:06', '2025-12-16 08:31:16');
INSERT INTO `bymaterial_dictionary` VALUES (92, 'process', '工序委外', '4', 'price_type', '价格类型', '2025-11-27 16:16:40', '2025-12-16 08:31:10');
INSERT INTO `bymaterial_dictionary` VALUES (93, 'cooperation', '工序协作', '2', 'price_type', '价格类型', '2025-11-27 16:17:14', '2025-12-16 08:31:03');
INSERT INTO `bymaterial_dictionary` VALUES (94, '01', '合作客户A', '100068', 'Customer_grouping', '客户分组', '2025-12-17 16:00:46', '2025-12-17 16:00:46');
INSERT INTO `bymaterial_dictionary` VALUES (95, '02', '合作客户B', '100069', 'Customer_grouping', '客户分组', '2025-12-17 16:00:46', '2025-12-17 16:00:46');
INSERT INTO `bymaterial_dictionary` VALUES (96, '03', '合作客户C', '100070', 'Customer_grouping', '客户分组', '2025-12-17 16:00:46', '2025-12-17 16:00:46');
INSERT INTO `bymaterial_dictionary` VALUES (97, '04', '潜在客户', '123833', 'Customer_grouping', '客户分组', '2025-12-17 16:00:46', '2026-03-02 08:53:32');
INSERT INTO `bymaterial_dictionary` VALUES (98, 'SKTJ01_SYS', '货到收款', '20003', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2025-12-26 16:07:36');
INSERT INTO `bymaterial_dictionary` VALUES (99, 'SKTJ02_SYS', '30天后收款', '20004', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2025-12-26 16:07:36');
INSERT INTO `bymaterial_dictionary` VALUES (100, 'SKTJ03_SYS', '月结_30天', '20005', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2026-01-24 14:52:38');
INSERT INTO `bymaterial_dictionary` VALUES (101, 'SKTJ04_SYS', '多到期日（按金额）', '20006', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2025-12-26 16:07:36');
INSERT INTO `bymaterial_dictionary` VALUES (102, '5', '预收5%', '142418', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2025-12-26 16:07:36');
INSERT INTO `bymaterial_dictionary` VALUES (103, '10', '预收10%', '142419', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2025-12-26 16:07:36');
INSERT INTO `bymaterial_dictionary` VALUES (104, '20', '预收20%', '142420', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2025-12-26 16:07:36');
INSERT INTO `bymaterial_dictionary` VALUES (105, '30', '预收30%', '142421', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2025-12-26 16:07:36');
INSERT INTO `bymaterial_dictionary` VALUES (106, '40', '预收40%', '142422', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2025-12-26 16:07:36');
INSERT INTO `bymaterial_dictionary` VALUES (107, '15', '预收15%', '142423', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2025-12-26 16:07:36');
INSERT INTO `bymaterial_dictionary` VALUES (109, '002', '款到发货', '155210', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2025-12-26 16:07:36');
INSERT INTO `bymaterial_dictionary` VALUES (110, '003', '预收30%，尾款发货后30天', '260639', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2025-12-26 16:07:36');
INSERT INTO `bymaterial_dictionary` VALUES (111, '5-new', '预收5%，尾款见提单草稿件付款', '309251', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2025-12-26 16:07:36');
INSERT INTO `bymaterial_dictionary` VALUES (112, '40-new', '预收40%，尾款见提单草稿件付款', '309256', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2025-12-26 16:07:36');
INSERT INTO `bymaterial_dictionary` VALUES (113, '20-new', '预收20%，尾款见提单草稿件付款', '309258', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2025-12-26 16:07:36');
INSERT INTO `bymaterial_dictionary` VALUES (114, '25-new', '预收25%，尾款见提单草稿件付款', '309259', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2025-12-26 16:07:36');
INSERT INTO `bymaterial_dictionary` VALUES (115, '15-new', '预收15%，尾款见提单草稿件付款', '309260', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2025-12-26 16:07:36');
INSERT INTO `bymaterial_dictionary` VALUES (116, '100-new', '预收100%', '309261', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2025-12-26 16:07:36');
INSERT INTO `bymaterial_dictionary` VALUES (117, '10-new', '预收10%，尾款见提单草稿件付款', '309262', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2025-12-26 16:07:36');
INSERT INTO `bymaterial_dictionary` VALUES (118, '30-new', '预收30%，尾款见提单草稿件付款', '309264', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2025-12-26 16:07:36');
INSERT INTO `bymaterial_dictionary` VALUES (119, '002-new', '款到发货', '309265', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2025-12-26 16:07:36');
INSERT INTO `bymaterial_dictionary` VALUES (120, '004-new', '开船后30天收款', '309524', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2025-12-26 16:07:36');
INSERT INTO `bymaterial_dictionary` VALUES (121, '005-new', '见提单草稿件付全款', '309585', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2025-12-26 16:07:36');
INSERT INTO `bymaterial_dictionary` VALUES (122, '007-new', '开船后45天收款', '309587', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2025-12-26 16:07:36');
INSERT INTO `bymaterial_dictionary` VALUES (123, '001-new', '开船后60天收款', '309588', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2025-12-26 16:07:36');
INSERT INTO `bymaterial_dictionary` VALUES (124, '008-new', '预收30%，发货前付清全款', '309590', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2025-12-26 16:07:36');
INSERT INTO `bymaterial_dictionary` VALUES (125, '50-new', '预收50%，尾款见提单草稿件付款', '314237', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2025-12-26 16:07:36');
INSERT INTO `bymaterial_dictionary` VALUES (126, '003-new', '预收30%，50%见提单草稿件付款，20%开船后60天付款', '344927', 'collection_terms', '收款条件', '2025-12-26 16:07:36', '2025-12-26 16:07:36');
INSERT INTO `bymaterial_dictionary` VALUES (127, '0001', '东宁独立站', '168244', 'customer_source', '客户来源', '2025-12-29 15:32:26', '2025-12-29 15:32:26');
INSERT INTO `bymaterial_dictionary` VALUES (128, '0002', '佰誉独立站', '168245', 'customer_source', '客户来源', '2025-12-29 15:32:26', '2025-12-29 15:32:26');
INSERT INTO `bymaterial_dictionary` VALUES (129, '0003', '东宁阿里巴巴', '168246', 'customer_source', '客户来源', '2025-12-29 15:32:26', '2026-03-03 16:53:03');
INSERT INTO `bymaterial_dictionary` VALUES (130, '0004', '中国制造网', '168247', 'customer_source', '客户来源', '2025-12-29 15:32:26', '2025-12-29 15:32:26');
INSERT INTO `bymaterial_dictionary` VALUES (131, '0005', '佰誉阿里巴巴', '168248', 'customer_source', '客户来源', '2025-12-29 15:32:26', '2025-12-29 15:32:26');
INSERT INTO `bymaterial_dictionary` VALUES (132, '0006', '上海法兰克福展', '168250', 'customer_source', '客户来源', '2025-12-29 15:32:26', '2025-12-29 15:32:26');
INSERT INTO `bymaterial_dictionary` VALUES (133, '0007', '上海五金展', '168251', 'customer_source', '客户来源', '2025-12-29 15:32:26', '2025-12-29 15:32:26');
INSERT INTO `bymaterial_dictionary` VALUES (134, '0008', '德国法兰克福汽配展', '168252', 'customer_source', '客户来源', '2025-12-29 15:32:26', '2025-12-29 15:32:26');
INSERT INTO `bymaterial_dictionary` VALUES (135, '0009', '俄罗斯五金展', '168253', 'customer_source', '客户来源', '2025-12-29 15:32:26', '2025-12-29 15:32:26');
INSERT INTO `bymaterial_dictionary` VALUES (136, '0010', 'google开发', '168254', 'customer_source', '客户来源', '2025-12-29 15:32:26', '2025-12-29 15:32:26');
INSERT INTO `bymaterial_dictionary` VALUES (137, '0014', 'Instagram', '168258', 'customer_source', '客户来源', '2025-12-29 15:32:26', '2025-12-29 15:32:26');
INSERT INTO `bymaterial_dictionary` VALUES (138, '0020', '客户主动拜访', '168264', 'customer_source', '客户来源', '2025-12-29 15:32:26', '2025-12-29 15:32:26');
INSERT INTO `bymaterial_dictionary` VALUES (139, '0021', '莫斯科汽配展', '185914', 'customer_source', '客户来源', '2025-12-29 15:32:26', '2025-12-29 15:32:26');
INSERT INTO `bymaterial_dictionary` VALUES (140, '0022', '客户推荐', '185916', 'customer_source', '客户来源', '2025-12-29 15:32:26', '2025-12-29 15:32:26');
INSERT INTO `bymaterial_dictionary` VALUES (141, '0023', '广交会', '185917', 'customer_source', '客户来源', '2025-12-29 15:32:26', '2025-12-29 15:32:26');
INSERT INTO `bymaterial_dictionary` VALUES (142, '0025', '越南展-2023', '211694', 'customer_source', '客户来源', '2025-12-29 15:32:26', '2025-12-29 15:32:26');
INSERT INTO `bymaterial_dictionary` VALUES (143, '0026', '欣翰阿里巴巴', '317699', 'customer_source', '客户来源', '2025-12-29 15:32:26', '2025-12-29 15:32:26');
INSERT INTO `bymaterial_dictionary` VALUES (144, '0027', '客户网站', '293065', 'customer_source', '客户来源', '2025-12-29 15:32:26', '2025-12-29 15:32:26');
INSERT INTO `bymaterial_dictionary` VALUES (145, 'm', '米', '10087', 'unit', '基本单位', '2026-01-06 09:38:33', '2026-01-06 09:38:33');
INSERT INTO `bymaterial_dictionary` VALUES (146, 'km', '千米', '10088', 'unit', '基本单位', '2026-01-06 09:38:33', '2026-01-06 09:38:33');
INSERT INTO `bymaterial_dictionary` VALUES (147, 'cm', '厘米', '10089', 'unit', '基本单位', '2026-01-06 09:38:33', '2026-01-06 09:38:33');
INSERT INTO `bymaterial_dictionary` VALUES (148, 'kg', '千克', '10095', 'unit', '基本单位', '2026-01-06 09:38:33', '2026-01-06 09:38:33');
INSERT INTO `bymaterial_dictionary` VALUES (149, 'ton', '吨', '10096', 'unit', '基本单位', '2026-01-06 09:38:33', '2026-01-06 09:38:33');
INSERT INTO `bymaterial_dictionary` VALUES (150, 'g', '克', '10097', 'unit', '基本单位', '2026-01-06 09:38:33', '2026-01-06 09:38:33');
INSERT INTO `bymaterial_dictionary` VALUES (151, 'L', '升', '10099', 'unit', '基本单位', '2026-01-06 09:38:33', '2026-01-06 09:38:33');
INSERT INTO `bymaterial_dictionary` VALUES (152, 'ml', '毫升', '10100', 'unit', '基本单位', '2026-01-06 09:38:33', '2026-01-06 09:38:33');
INSERT INTO `bymaterial_dictionary` VALUES (153, 'Pcs', 'Pcs', '10101', 'unit', '基本单位', '2026-01-06 09:38:33', '2026-01-06 09:38:33');
INSERT INTO `bymaterial_dictionary` VALUES (154, 'double', '双', '10102', 'unit', '基本单位', '2026-01-06 09:38:33', '2026-01-06 09:38:33');
INSERT INTO `bymaterial_dictionary` VALUES (155, 'dozen', '打', '10103', 'unit', '基本单位', '2026-01-06 09:38:33', '2026-01-06 09:38:33');
INSERT INTO `bymaterial_dictionary` VALUES (156, 'dong', '栋', '10147', 'unit', '基本单位', '2026-01-06 09:38:33', '2026-01-06 09:38:33');
INSERT INTO `bymaterial_dictionary` VALUES (157, 'tai', '台', '10148', 'unit', '基本单位', '2026-01-06 09:38:33', '2026-01-06 09:38:33');
INSERT INTO `bymaterial_dictionary` VALUES (158, 'tao', '套', '10149', 'unit', '基本单位', '2026-01-06 09:38:33', '2026-01-06 09:38:33');
INSERT INTO `bymaterial_dictionary` VALUES (159, 'liang', '辆', '10150', 'unit', '基本单位', '2026-01-06 09:38:33', '2026-01-06 09:38:33');
INSERT INTO `bymaterial_dictionary` VALUES (160, 'second', '秒', '80505', 'unit', '基本单位', '2026-01-06 09:38:33', '2026-01-06 09:38:33');
INSERT INTO `bymaterial_dictionary` VALUES (161, 'minute', '分', '80506', 'unit', '基本单位', '2026-01-06 09:38:33', '2026-01-06 09:38:33');
INSERT INTO `bymaterial_dictionary` VALUES (162, 'hour', '时', '80507', 'unit', '基本单位', '2026-01-06 09:38:33', '2026-01-06 09:38:33');
INSERT INTO `bymaterial_dictionary` VALUES (163, 'KGS', '千克', '113416', 'unit', '基本单位', '2026-01-06 09:38:33', '2026-01-06 09:38:33');
INSERT INTO `bymaterial_dictionary` VALUES (164, 'mm', '毫米', '181587', 'unit', '基本单位', '2026-01-06 09:38:33', '2026-01-06 09:38:33');
INSERT INTO `bymaterial_dictionary` VALUES (165, 'UOM001', '个', '278627', 'unit', '基本单位', '2026-01-06 09:38:33', '2026-01-06 09:38:33');
INSERT INTO `bymaterial_dictionary` VALUES (166, 'UOM002', '把', '278628', 'unit', '基本单位', '2026-01-06 09:38:33', '2026-01-06 09:38:33');
INSERT INTO `bymaterial_dictionary` VALUES (167, 'packaging', '中性包装', '1', 'manner_packing', '包装方式', '2026-01-19 14:28:38', '2026-03-10 10:18:04');
INSERT INTO `bymaterial_dictionary` VALUES (168, 'delimiter', '定牌包装', '2', 'manner_packing', '包装方式', '2026-01-19 14:29:32', '2026-01-19 14:29:32');
INSERT INTO `bymaterial_dictionary` VALUES (169, 'new_product', '公司新品', '1', 'Order_product_category', '订单产品类别', '2026-01-19 14:44:52', '2026-01-19 14:44:52');
INSERT INTO `bymaterial_dictionary` VALUES (170, ' additional_products', '采购扩品', '2', 'Order_product_category', '订单产品类别', '2026-01-19 14:46:16', '2026-01-19 14:46:18');
INSERT INTO `bymaterial_dictionary` VALUES (171, 'not_have', '无', '3', 'Order_product_category', '订单产品类别', '2026-01-19 14:46:51', '2026-01-19 14:46:51');
INSERT INTO `bymaterial_dictionary` VALUES (172, 'ordinary', '一般贸易', '6191b13c997848', 'trade_way', '贸易方式', '2026-01-20 10:16:25', '2026-01-20 10:16:25');
INSERT INTO `bymaterial_dictionary` VALUES (173, 'barter', '易货贸易', '6191b16c99784a', 'trade_way', '贸易方式', '2026-01-20 10:17:03', '2026-01-20 10:17:03');
INSERT INTO `bymaterial_dictionary` VALUES (174, 'compensation', '补偿贸易', '6191b18099784c', 'trade_way', '贸易方式', '2026-01-20 10:17:35', '2026-01-20 10:17:35');
INSERT INTO `bymaterial_dictionary` VALUES (175, 'agreement', '协定贸易', '6191b18d99784e', 'trade_way', '贸易方式', '2026-01-20 10:18:11', '2026-01-20 10:18:11');
INSERT INTO `bymaterial_dictionary` VALUES (176, 'process', '进料加工', '6191b19b997850', 'trade_way', '贸易方式', '2026-01-20 10:18:56', '2026-01-20 10:18:56');
INSERT INTO `bymaterial_dictionary` VALUES (177, 'standard', '标准销售订单', 'eacb50844fc84a10b03d7b841f3a6278', 'document_type', '单据类型', '2026-01-21 09:35:26', '2026-01-21 09:35:26');
INSERT INTO `bymaterial_dictionary` VALUES (178, 'specimen', '样品订单', '661e114d39eb35', 'document_type', '单据类型', '2026-01-21 09:36:02', '2026-01-21 09:37:32');
INSERT INTO `bymaterial_dictionary` VALUES (179, 'standard_procurement\n', '标准采购订单', '83d822ca3e374b4ab01e5dd46a0062bd', 'document_type', '单据类型', '2026-01-29 14:49:50', '2026-01-29 14:49:50');
INSERT INTO `bymaterial_dictionary` VALUES (180, 'KCZT01_SYS', '可用', '10000', 'inventory_status', '库存状态', '2026-02-10 09:55:04', '2026-02-10 09:55:04');
INSERT INTO `bymaterial_dictionary` VALUES (181, 'KCZT02_SYS', '待检', '10001', 'inventory_status', '库存状态', '2026-02-10 09:55:32', '2026-02-10 09:55:32');
INSERT INTO `bymaterial_dictionary` VALUES (182, 'KCZT03_SYS', '冻结', '10002', 'inventory_status', '库存状态', '2026-02-10 09:56:02', '2026-02-10 09:56:02');
INSERT INTO `bymaterial_dictionary` VALUES (183, 'KCZT04_SYS', '退回冻结', '10003', 'inventory_status', '库存状态', '2026-02-10 09:56:34', '2026-02-10 09:56:34');
INSERT INTO `bymaterial_dictionary` VALUES (184, 'KCZT05_SYS', '在途', '10004', 'inventory_status', '库存状态', '2026-02-10 09:57:08', '2026-02-10 09:57:08');
INSERT INTO `bymaterial_dictionary` VALUES (185, 'KCZT06_SYS', '收货冻结', '10005', 'inventory_status', '库存状态', '2026-02-10 09:57:50', '2026-02-10 09:58:18');
INSERT INTO `bymaterial_dictionary` VALUES (186, 'KCZT07_SYS', '废品', '10006', 'inventory_status', '库存状态', '2026-02-10 09:58:16', '2026-02-10 09:58:16');
INSERT INTO `bymaterial_dictionary` VALUES (187, 'KCZT08_SYS', '不良', '10257', 'inventory_status', '库存状态', '2026-02-10 09:58:46', '2026-02-10 09:58:56');
INSERT INTO `bymaterial_dictionary` VALUES (188, 'KCZT99_SYS', '外借', '103401', 'inventory_status', '库存状态', '2026-02-10 09:59:35', '2026-02-10 09:59:35');
INSERT INTO `bymaterial_dictionary` VALUES (189, 'KHLB001_SYS', '普通销售客户', '673cb7c55ea24626ae639ff2ec5adf0e', 'customer_category', '客户类别', '2026-03-02 08:59:37', '2026-03-10 09:17:05');
INSERT INTO `bymaterial_dictionary` VALUES (190, 'D', 'D', '699eb0dc066cc2', 'customer_category', '客户类别', '2026-03-02 09:29:11', '2026-03-02 09:29:11');
INSERT INTO `bymaterial_dictionary` VALUES (191, 'VIP', 'vip', '699eb08b066cba', 'customer_category', '客户类别', '2026-03-02 09:30:34', '2026-03-02 09:30:34');
INSERT INTO `bymaterial_dictionary` VALUES (192, 'C', 'C', '699eb0cf066cc0', 'customer_category', '客户类别', '2026-03-02 09:31:35', '2026-03-02 09:31:35');
INSERT INTO `bymaterial_dictionary` VALUES (193, 'B', 'B', '699eb0be066cbe', 'customer_category', '客户类别', '2026-03-02 09:32:25', '2026-03-10 09:17:41');
INSERT INTO `bymaterial_dictionary` VALUES (195, 'HLTX01_SYS', '固定汇率', '1', 'exchange_type', '汇率类型', '2026-03-10 11:20:58', '2026-03-10 13:10:32');
INSERT INTO `bymaterial_dictionary` VALUES (196, 'HLTX02_SYS', '即期汇率', '2', 'exchange_type', '汇率类型', '2026-03-10 12:37:31', '2026-03-10 12:37:31');
INSERT INTO `bymaterial_dictionary` VALUES (197, 'HLTX03_SYS', '预算汇率', '3', 'exchange_type', '汇率类型', '2026-03-10 12:38:10', '2026-03-10 12:38:17');
INSERT INTO `bymaterial_dictionary` VALUES (199, 'CK005', '工厂进仓', '118995', 'warehouse', '仓库', '2026-03-10 17:14:58', '2026-03-10 17:14:58');
INSERT INTO `bymaterial_dictionary` VALUES (200, 'CK002', '包材仓库', '107860', 'warehouse', '仓库', '2026-03-10 17:16:41', '2026-03-10 17:16:41');
INSERT INTO `bymaterial_dictionary` VALUES (201, 'CK015', '成品仓库-广昇', '352308', 'warehouse', '仓库', '2026-03-10 17:17:23', '2026-03-10 17:21:43');
INSERT INTO `bymaterial_dictionary` VALUES (202, 'CK006', '北仑仓库', '120307', 'warehouse', '仓库', '2026-03-10 17:18:24', '2026-03-10 17:18:24');
INSERT INTO `bymaterial_dictionary` VALUES (203, 'CK007', '上海仓库', '120308', 'warehouse', '仓库', '2026-03-10 17:19:00', '2026-03-10 17:19:00');
INSERT INTO `bymaterial_dictionary` VALUES (204, 'CK004', '工厂装柜', '122286', 'warehouse', '仓库', '2026-03-10 17:19:32', '2026-03-10 17:19:32');
INSERT INTO `bymaterial_dictionary` VALUES (205, 'CK014', '佰誉成品仓', '202963', 'warehouse', '仓库', '2026-03-10 17:20:47', '2026-03-10 17:20:47');
INSERT INTO `bymaterial_dictionary` VALUES (206, 'CK011', '成品仓库-仓位', '177932', 'warehouse', '仓库', '2026-03-10 17:23:29', '2026-03-10 17:23:29');

SET FOREIGN_KEY_CHECKS = 1;
