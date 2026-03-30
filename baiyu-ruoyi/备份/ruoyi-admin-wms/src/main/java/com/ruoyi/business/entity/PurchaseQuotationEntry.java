package com.ruoyi.business.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 采购报价单详情表实体
 */
@Data
@TableName("purchase_quotation_entry")
public class PurchaseQuotationEntry {

    /** 实体主键 */
    @TableId("id")
    private String id;

    /** 主表fid关联 */
    @TableField("fid")
    private String fid;

    /** 单据编号 */
    @TableField("fbillno")
    private String fbillno;

    /** 计量单位 */
    @TableField("f_ora_unitid")
    private String fOraUnitId;

    /** 产品优势(必填项) */
    @TableField("f_bz")
    private String fBz;

    /** 采购图片1 */
    @TableField("f_tp")
    private String fTp;

    /** 供应商 */
    @TableField("f_gys")
    private String fGys;

    /** 采购图片2 */
    @TableField("f_cptp1")
    private String fCptp1;

    /** 价格有效期 */
    @TableField("f_jgyxq")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fJgyxq;

    /** 采购价 */
    @TableField("f_cgjg")
    private BigDecimal fCgjg;

    /** 起订量 */
    @TableField("f_zxqdl")
    private BigDecimal fZxqdl;

    /** 采购包装方式(必填项) */
    @TableField("f_xcpms")
    private String fXcpms;

    /** 新供应商 */
    @TableField("f_stgys")
    private String fStgys;

    /** 下单量 */
    @TableField("f_xdl")
    private BigDecimal fXdl;

    /** 产品代码 */
    @TableField("f_cpdm1")
    private String fCpdm1;

    /** 新品描述 */
    @TableField("f_ywbz")
    private String fYwbz;

    /** 物料图片 */
    @TableField("f_wltp")
    private String fWltp;

    /** 产品名称 */
    @TableField("f_cpms1")
    private String fCpms1;

    /** 包装明细 */
    @TableField("f_bzmx")
    private String fBzmx;

    /** 规格说明 */
    @TableField("f_cty_baseproperty4")
    private String fCtyBaseproperty4;

    /** 业务图片1 */
    @TableField("f_ywtp1")
    private String fYwtp1;

    /** 业务图片2 */
    @TableField("f_ywtp2")
    private String fYwtp2;

    /** 装箱数 */
    @TableField("f_zxs")
    private BigDecimal fZxs;

    /** 毛重 */
    @TableField("f_mz")
    private BigDecimal fMz;

    /** 净重 */
    @TableField("f_jz")
    private BigDecimal fJz;

    /** 长 */
    @TableField("f_c")
    private BigDecimal fC;

    /** 宽 */
    @TableField("f_k")
    private BigDecimal fK;

    /** 高 */
    @TableField("f_g")
    private BigDecimal fG;

    /** 成本价（含税） */
    @TableField("f_cghsj")
    private BigDecimal fCghsj;

    /** 产品类型 */
    @TableField("f_cty_baseproperty")
    private String fCtyBaseproperty;

    /** 工厂图片 */
    @TableField("fgctp")
    private String fgctp;

    /** 预计交货日期 */
    @TableField("fyjjhrq")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fyjjhrq;

    /** 包装尺寸 */
    @TableField("fbzcc")
    private String fbzcc;

    /** 常规塑盒颜色 */
    @TableField("fcgshys")
    private String fcgshys;

    /** 报价人员 */
    @TableField("f_fbjry")
    private String fFbjry;

    /** 英文品名 */
    @TableField("f_ywpm")
    private String fYwpm;

    /** 英文描述 */
    @TableField("f_ywms")
    private String fYwms;

    /** 规格描述 */
    @TableField("f_cty_text")
    private String fCtyText;

    /** 客户货号 */
    @TableField("fkhhh")
    private String fkhhh;

    /** 库存数量 */
    @TableField("fqty_kc")
    private BigDecimal fqtyKc;

    /** 总箱数 */
    @TableField("fzxss")
    private BigDecimal fzxss;

    /** 体积 */
    @TableField("ftj")
    private BigDecimal ftj;

    /** 毛总重 */
    @TableField("fmzz")
    private BigDecimal fmzz;

    /** 客户需求 */
    @TableField("fkhxq")
    private String fkhxq;

    /** 采购新品描述 */
    @TableField("fcgxpms")
    private String fcgxpms;

    /** 新供应商联系人 */
    @TableField("fxgyslxr")
    private String fxgyslxr;

    /** 新供应商联系方式 */
    @TableField("fxgyslxfs")
    private String fxgyslxfs;
}
