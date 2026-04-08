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

 Date: 20/03/2026 13:38:21
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_sale_order
-- ----------------------------
DROP TABLE IF EXISTS `t_sale_order`;
CREATE TABLE `t_sale_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `FID` bigint NULL DEFAULT NULL COMMENT '金蝶主键',
  `FDocumentStatus` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '单据状态',
  `Document_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '单据类型',
  `FBillNo` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '单据编号',
  `orderStatus` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '订单状态',
  `FDate` date NOT NULL COMMENT '销售合同日期',
  `FCustId` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '客户编码',
  `F_ora_BaseProperty` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '客户简称',
  `F_khhth` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '客户合同号',
  `F_kglxr` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '客户联系人',
  `F_cty_BaseProperty1` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '客户邮箱',
  `FSettleCurrId` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '结算币别',
  `F_tcbl` decimal(10, 4) NULL DEFAULT NULL COMMENT '提成比例%',
  `F_KHSD` tinyint NULL DEFAULT NULL COMMENT '客户首单（非样品）',
  `FIsIncludedTax` tinyint NULL DEFAULT NULL COMMENT '是否含税',
  `F_sfbg` tinyint NULL DEFAULT NULL COMMENT '是否报关',
  `FSalerId` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '销售员',
  `F_lrl` decimal(10, 4) NULL DEFAULT NULL COMMENT '毛净利润率%',
  `F_jlrl` decimal(10, 4) NULL DEFAULT NULL COMMENT '净利润率%',
  `fstate` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '国家',
  `F_gj` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '抵运国家',
  `F_myfs` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '贸易方式',
  `F_zyxb` tinyint NULL DEFAULT NULL COMMENT '占用信保',
  `F_yhzh` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '银行账号',
  `F_cty_Date` date NULL DEFAULT NULL COMMENT '客户交期',
  `F_sygs` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '所属公司',
  `FRecConditionId` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '收款条件',
  `Fbzfs` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '包装方式',
  `FReceiveId` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '收货方',
  `FSettleId` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '结算方',
  `FSettleAddress` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '结算方地址',
  `FChargeId` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '付款方',
  `F_shhl` decimal(10, 6) NULL DEFAULT NULL COMMENT '锁汇汇率',
  `F_shzt` tinyint NULL DEFAULT NULL COMMENT '锁汇状态',
  `F_shje` decimal(18, 2) NULL DEFAULT NULL COMMENT '锁汇金额',
  `F_cty_Date1` date NULL DEFAULT NULL COMMENT '解汇日期',
  `FCreatorId` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '创建人',
  `FCreateDate` datetime NULL DEFAULT NULL COMMENT '创建日期',
  `FModifierId` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '最后修改人',
  `FModifyDate` datetime NULL DEFAULT NULL COMMENT '最后修改日期',
  `FAllDisCount` decimal(18, 2) NULL DEFAULT NULL COMMENT '整单折扣额',
  `F_ysbl1` decimal(10, 4) NULL DEFAULT NULL COMMENT '预收比例%',
  `FBillTaxAmount` decimal(18, 2) NULL DEFAULT NULL COMMENT '税额',
  `FBillAmount` decimal(18, 2) NULL DEFAULT NULL COMMENT '金额',
  `FLocalCurrId` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '本位币',
  `FExchangeTypeId` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '汇率类型',
  `FExchangeRate` decimal(10, 6) NULL DEFAULT NULL COMMENT '汇率',
  `fplannotrecamount` decimal(18, 2) NULL DEFAULT NULL COMMENT '未收款金额',
  `fplanallrecamount` decimal(18, 2) NULL DEFAULT NULL COMMENT '累计收款金额',
  `fplanrefundamount` decimal(18, 2) NULL DEFAULT NULL COMMENT '累计退款金额',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_fid`(`FID` ASC) USING BTREE,
  INDEX `idx_sale_order_date`(`FDate` ASC) USING BTREE,
  INDEX `idx_sale_order_cust`(`FCustId` ASC) USING BTREE,
  INDEX `idx_sale_order_settle`(`FSettleId` ASC) USING BTREE,
  INDEX `idx_sale_order_yhzh`(`F_yhzh` ASC) USING BTREE,
  INDEX `idx_sale_order_salesman`(`FSalerId` ASC) USING BTREE,
  INDEX `idx_sale_order_creator`(`FCreatorId` ASC) USING BTREE,
  INDEX `idx_sale_order_modifier`(`FModifierId` ASC) USING BTREE,
  INDEX `idx_sale_order_reccond`(`FRecConditionId` ASC) USING BTREE,
  INDEX `idx_sale_order_pack`(`Fbzfs` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 47829 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '销售订单主表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
