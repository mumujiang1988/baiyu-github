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

 Date: 25/03/2026 11:09:35
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_sale_order_cost
-- ----------------------------
DROP TABLE IF EXISTS `t_sale_order_cost`;
CREATE TABLE `t_sale_order_cost`  (
  `FID` bigint NOT NULL COMMENT '主键=销售订单FID',
  `FBillNo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '订单编号',
  `F_hyf` decimal(18, 6) NULL DEFAULT NULL COMMENT '海运费（外币）',
  `FBillAllAmount` decimal(18, 6) NULL DEFAULT NULL COMMENT '价税合计',
  `FBillAllAmount_LC` decimal(18, 6) NULL DEFAULT NULL COMMENT '价税合计（本位币）',
  `F_bxf` decimal(18, 2) NULL DEFAULT NULL COMMENT '保险费（外币）',
  `F_gwyhfy` decimal(18, 6) NULL DEFAULT NULL COMMENT '国外银行费用',
  `F_qtwbfy` decimal(18, 6) NULL DEFAULT NULL COMMENT '其他外币费用',
  `F_mxcbhj` decimal(18, 6) NULL DEFAULT NULL COMMENT '明细成本合计',
  `F_mxtshj` decimal(18, 6) NULL DEFAULT NULL COMMENT '明细退税合计',
  `F_cbxj` decimal(18, 6) NULL DEFAULT NULL COMMENT '成本小计RMB',
  `F_bzf` decimal(18, 2) NULL DEFAULT NULL COMMENT '包装费',
  `F_dlf` decimal(18, 2) NULL DEFAULT NULL COMMENT '代理费',
  `F_rzf` decimal(18, 2) NULL DEFAULT NULL COMMENT '认证费',
  `F_kdf` decimal(18, 2) NULL DEFAULT NULL COMMENT '快递费成本',
  `F_hdf` decimal(18, 2) NULL DEFAULT NULL COMMENT '货贷费',
  `F_lyf` decimal(18, 2) NULL DEFAULT NULL COMMENT '陆运费',
  `F_qtfy` decimal(18, 2) NULL DEFAULT NULL COMMENT '其他费用',
  `F_mjf` decimal(18, 2) NULL DEFAULT NULL COMMENT '模具费',
  `F_jcf` decimal(18, 2) NULL DEFAULT NULL COMMENT '进仓费',
  `F_fyxj` decimal(18, 2) NULL DEFAULT NULL COMMENT '费用小计',
  `F_lrl` decimal(10, 4) NULL DEFAULT NULL COMMENT '毛净利润率%',
  `F_jlrl` decimal(10, 4) NULL DEFAULT NULL COMMENT '净利润率',
  `F_wbyk` decimal(18, 2) NULL DEFAULT NULL COMMENT '外币盈亏',
  `F_jlre` decimal(18, 2) NULL DEFAULT NULL COMMENT '净利润额',
  PRIMARY KEY (`FID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '销售订单成本预估表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
