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

 Date: 25/03/2026 11:09:39
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_sale_order_entry
-- ----------------------------
DROP TABLE IF EXISTS `t_sale_order_entry`;
CREATE TABLE `t_sale_order_entry`  (
  `fentryid` bigint NOT NULL AUTO_INCREMENT COMMENT '明细主键',
  `fbillno` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '销售订单编号',
  `fbcykc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '包材有库存',
  `f_sfxp` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '产品类别',
  `f_jqhx` date NULL DEFAULT NULL COMMENT '交期红线',
  `fplanmaterialid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '物料编码',
  `fplanmaterialname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '物料名称',
  `fplanunitid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '销售单位',
  `fqty` decimal(18, 6) NULL DEFAULT NULL COMMENT '销售数量',
  `fmaterialpriceunitqty` decimal(18, 6) NULL DEFAULT NULL COMMENT '计价数量',
  `fmaterialpriceunitid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '计价单位',
  `fprice` decimal(18, 6) NULL DEFAULT NULL COMMENT '单价',
  `ftaxprice` decimal(18, 6) NULL DEFAULT NULL COMMENT '含税单价',
  `fentrytaxrate` decimal(10, 6) NULL DEFAULT NULL COMMENT '税率%',
  `fentrytaxamount` decimal(18, 2) NULL DEFAULT NULL COMMENT '税额',
  `fallamount` decimal(18, 2) NULL DEFAULT NULL COMMENT '价税合计',
  `fbasecanoutqty` decimal(18, 4) NULL DEFAULT NULL COMMENT '可出数量（销售基本）',
  `fdeliqty` decimal(18, 4) NULL DEFAULT NULL COMMENT '累计发货通知数量',
  `fstockbasecanoutqty` decimal(18, 4) NULL DEFAULT NULL COMMENT '可出数量（库存基本）',
  `f_ora_text1` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '客户货号',
  `f_ora_date3` date NULL DEFAULT NULL COMMENT '发货日期',
  `f_fzr` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '负责人',
  `f_ora_text` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '采购合同号',
  `f_ora_date1` date NULL DEFAULT NULL COMMENT '供应商实际完成订单日期',
  `f_ora_date2` date NULL DEFAULT NULL COMMENT '实际收货日期',
  `fpurjoinqty` decimal(18, 4) NULL DEFAULT NULL COMMENT '关联采购/生产数量',
  `f_ora_base1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '延迟原因',
  `f_ycjjfa` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '延迟解决方案',
  `f_ora_decimal` decimal(18, 4) NULL DEFAULT NULL COMMENT '采购价',
  `f_ora_date4` date NULL DEFAULT NULL COMMENT '业务员提供包装资料日期',
  `f_ora_decimal1` decimal(18, 4) NULL DEFAULT NULL COMMENT '成本价',
  `f_ora_integer2` int NULL DEFAULT NULL COMMENT '包装资料晚交天数',
  `f_ora_date5` date NULL DEFAULT NULL COMMENT '包装寄出日期',
  `f_ora_integer3` int NULL DEFAULT NULL COMMENT '包装寄出延迟天数',
  `f_bzjdtwo` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '包装进度',
  `f_ora_base` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '供应商',
  `f_ora_integer` int NULL DEFAULT NULL COMMENT '工厂交货期',
  `f_ora_date` date NULL DEFAULT NULL COMMENT '采购合同日期',
  `f_mz` decimal(18, 4) NULL DEFAULT NULL COMMENT '毛重',
  `f_jz` decimal(18, 4) NULL DEFAULT NULL COMMENT '净重',
  `f_mzz` decimal(18, 4) NULL DEFAULT NULL COMMENT '毛总重',
  `f_zxs` int NULL DEFAULT NULL COMMENT '装箱数',
  `f_xs` int NULL DEFAULT NULL COMMENT '箱数',
  `f_gdtp1` varchar(550) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '跟单图片1',
  `f_gdtp2` varchar(555) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '跟单图片2',
  `f_xdgjjdtwo` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '跟进情况',
  `f_bzfs` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '包装要求',
  `f_tsyq` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '特殊要求',
  `f_ljrksl` decimal(18, 4) NULL DEFAULT NULL COMMENT '累计入库数量',
  `fysbz` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '颜色标识',
  `f_bgrq` date NULL DEFAULT NULL COMMENT '变更日期',
  `f_ctt` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '彩贴图',
  `f_cptp` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '产品图片',
  `f_gcbz` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '工厂包装',
  `fhhwrk` tinyint NULL DEFAULT NULL COMMENT '货好未入库',
  `f_kpdj` decimal(18, 4) NULL DEFAULT NULL COMMENT '开票单价',
  `f_bzxdzt` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '包装下单状态',
  `f_gxzt` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '更新状态',
  `f_cht` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '彩盒图',
  `f_jgt` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '禁告贴',
  `f_smsfj` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '说明书附件',
  `f_bchqzt` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '包材获取状态',
  `f_tsl` decimal(10, 2) NULL DEFAULT NULL COMMENT '退税率%',
  `f_bgywpm` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '报关英文品名',
  `f_bgdw` varchar(550) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '报关单位',
  `f_ygcb` decimal(18, 2) NULL DEFAULT NULL COMMENT '实际成本',
  `f_hsbm` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT 'HS编码',
  `f_sbys` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '申报要素',
  `f_bcfynew` decimal(18, 2) NULL DEFAULT NULL COMMENT '包材费用',
  `f_glbcfynew` decimal(18, 2) NULL DEFAULT NULL COMMENT '关联包材费用',
  `fbzcc` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '包装尺寸',
  `fbzgctg` tinyint NULL DEFAULT NULL COMMENT '包装工厂提供',
  `frkshrq` date NULL DEFAULT NULL COMMENT '入库审核日期',
  `fbjr` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '报价人',
  `ftpr` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '推品人',
  `f_cty_baseproperty4` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '修改状态',
  `fkhywms` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '客户英文描述',
  `f_cty_baseproperty6` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '延迟原因',
  `f_ygcbdj` decimal(18, 6) NULL DEFAULT NULL COMMENT '实际成本单价',
  `f_ygcbdj1` decimal(18, 6) NULL DEFAULT NULL COMMENT '预估成本单价',
  `f_ygcb1` decimal(18, 6) NULL DEFAULT NULL COMMENT '预估成本',
  `f_peuu_attachment_83g` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '验货报告',
  `fcgddshrq` date NULL DEFAULT NULL COMMENT '采购订单审核日期',
  `fyjsj` date NULL DEFAULT NULL COMMENT '预警时间',
  `fslshrq` date NULL DEFAULT NULL COMMENT '收料审核日期',
  `f_xlcp` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '新老产品',
  `f_cplb` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '产品大类',
  `f_ckrq` date NULL DEFAULT NULL COMMENT '出库日期',
  `f_cty_baseproperty7` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '供应商评级',
  `f_cty_baseproperty8` varchar(555) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '工厂问题',
  PRIMARY KEY (`fentryid`) USING BTREE,
  INDEX `idx_sale_entry_sfxp`(`f_sfxp` ASC) USING BTREE,
  INDEX `idx_sale_entry_unit`(`fplanunitid` ASC) USING BTREE,
  INDEX `idx_sale_entry_cplb`(`f_cplb` ASC) USING BTREE,
  INDEX `idx_sale_entry_material`(`fplanmaterialid` ASC) USING BTREE,
  INDEX `idx_sale_entry_supplier`(`f_ora_base` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 266185 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '销售订单明细表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
