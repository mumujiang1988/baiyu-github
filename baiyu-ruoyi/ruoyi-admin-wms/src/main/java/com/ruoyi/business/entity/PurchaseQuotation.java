package com.ruoyi.business.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 采购报价单主表实体
 */
@Data
@TableName("purchase_quotation")
public class PurchaseQuotation {

    /** 实体主键 */
    @TableId("fid")
    private String fid;

    /** 单据编号 */
    @TableField("fbillno")
    private String fbillno;

    /** 数据状态 */
    @TableField("fdocumentstatus")
    private String fdocumentstatus;

    /** 创建人 */
    @TableField("fcreatorid")
    private String fcreatorid;

    /** 创建日期 */
    @TableField("fcreatedate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fcreatedate;

    /** 审核人 */
    @TableField("fshr")
    private String fshr;

    /** 审核日期 */
    @TableField("fdate_sh")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fdateSh;

    /** 日期(必填项) */
    @TableField("fdate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fdate;

    /** 销售员 */
    @TableField("f_xsy")
    private String fxSy;

    /** 客户简称 */
    @TableField("f_khjc")
    private String fKhjc;

    /** 客户编码 */
    @TableField("f_kh")
    private String fKh;

    /** 客户名称 */
    @TableField("f_khmc")
    private String fKhmc;

    /** 出口国家 */
    @TableField("f_ckgj")
    private String fCkgj;

    /** 客户类型 */
    @TableField("f_khlx")
    private String fKhlx;

    /** Email */
    @TableField("f_email")
    private String fEmail;

    /** Mob */
    @TableField("f_mob")
    private String fMob;

    /** Tel */
    @TableField("f_tel")
    private String fTel;

    /** Fax */
    @TableField("f_fax")
    private String fFax;

    /** Shipping To */
    @TableField("f_qyd")
    private String fQyd;

    /** Payment terms */
    @TableField("f_fktj")
    private String fFktj;

    /** 采购报价单详情列表 */
    private List<PurchaseQuotationEntry> entries;
}
