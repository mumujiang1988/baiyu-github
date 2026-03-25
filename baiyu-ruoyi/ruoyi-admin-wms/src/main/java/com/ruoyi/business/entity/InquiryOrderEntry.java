package com.ruoyi.business.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 询价单详情表
 */
@Data
@TableName("inquiry_order_entry")
public class InquiryOrderEntry {

    /** 实体主键 */
    @TableId("id")
    private String id;

    /** 主表fid关联 */
    @TableField("fid")
    private String fid;
    /** 单据编号 */
    @TableField("fbillno")
    private String fbillno;
    /** 产品类别 */
    @TableField("f_cplb")
    private String fCplb;

    /** 交期风险 */
    @TableField("f_jqfx")
    private String fJqfx;

    /** 产品代码 */
    @TableField("f_wldm")
    private String fWldm;

    /** 含税单价 */
    @TableField("f_hsdj")
    private BigDecimal fHsdj;

    /** 英文品名 */
    @TableField("f_ywpm")
    private String fYwpm;

    /** 产品名称 */
    @TableField("f_cty_baseproperty2")
    private String fCtyBaseproperty2;

    /** 成本合计 */
    @TableField("f_cbhj")
    private BigDecimal fCbhj;

    /** 规格型号 */
    @TableField("f_cty_baseproperty3")
    private String fCtyBaseproperty3;

    /** 销售订单关联数 */
    @TableField("f_xsddgls")
    private BigDecimal fXsddgls;

    /** 销售订单下推状态 */
    @TableField("f_xsddxtzt")
    private String fXsddxtzt;

    /** 产品类型 */
    @TableField("f_cty_baseproperty5")
    private String fCtyBaseproperty5;

    /** 客户包装要求(必填项) */
    @TableField("f_khbzyq")
    private String fKhbzyq;

    /** 下单量 */
    @TableField("f_xdl")
    private BigDecimal fXdl;

    /** 成本价（含税） */
    @TableField("f_hscgj")
    private BigDecimal fHscgj;

    /** 采购新品描述 */
    @TableField("f_cgxpms")
    private String fCgxpms;

    /** 采购包装方式 */
    @TableField("f_cgbzfs")
    private String fCgbzfs;

    /** 起订量 */
    @TableField("f_qdl")
    private BigDecimal fQdl;

    /** 价税合计 */
    @TableField("f_jshj")
    private BigDecimal fJshj;

    /** 业务图片1 */
    @TableField("f_cptp1")
    private String fCptp1;

    /** 业务图片2 */
    @TableField("f_tp")
    private String fTp;

    /** 采购图片1 */
    @TableField("f_cgtp1")
    private String fCgtp1;

    /** 采购图片2 */
    @TableField("f_cgtp2")
    private String fCgtp2;

    /** 价格失效日期 */
    @TableField("f_jgsxrq")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fJgsxrq;

    /** 预计交期 */
    @TableField("f_yjjq")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fYjjq;

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

    /** 关联数量 */
    @TableField("f_glsl")
    private BigDecimal fGlsl;

    /** 工厂图片 */
    @TableField("f_gctp")
    private String fGctp;

    /** 工厂英文描述 */
    @TableField("fgcywms")
    private String fgcywms;

    /** 工厂常塑盒颜色 */
    @TableField("fgccshys")
    private String fgccshys;

    /** 包装尺寸 */
    @TableField("fbzcc")
    private String fbzcc;

    /** 客户货号 */
    @TableField("fkhhh")
    private String fkhhh;

    /** 最近下单含税价 */
    @TableField("fzjxdhsj")
    private BigDecimal fzjxdhsj;

    /** 最近下单数量 */
    @TableField("fzjxdsl")
    private BigDecimal fzjxdsl;

    /** 默认采购含税价 */
    @TableField("fmrcghsj")
    private BigDecimal fmrcghsj;

    /** 销售价 */
    @TableField("fxsj")
    private BigDecimal fxsj;

    /** 供应商 */
    @TableField("fgys")
    private String fgys;

    /** 推品人 */
    @TableField("ftpr")
    private String ftpr;

    /** 业务图片3 */
    @TableField("f_tp3")
    private String fTp3;

    /** 报价人 */
    @TableField("fbjr")
    private String fbjr;

    /** 产品优势 */
    @TableField("fcpys")
    private String fcpys;

    /** 英文描述 */
    @TableField("f_ywms")
    private String fYwms;
}
