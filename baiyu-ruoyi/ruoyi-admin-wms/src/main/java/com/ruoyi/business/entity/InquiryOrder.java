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
 * 询价单主表
 */
@Data
@TableName("inquiry_order")
public class InquiryOrder {

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

    /** 出口国家(必填项) */
    @TableField("f_ckgj")
    private String fCkgj;

    /** 客户来源(必填项) */
    @TableField("f_khly")
    private String fKhly;



    /** 固定汇率 */
    @TableField("f_gdhl")
    private BigDecimal fGdhl;

    /** 币别 */
    @TableField("f_bjbb")
    private String fBjbb;

    /** 毛净利率% */
    @TableField("f_mll")
    private BigDecimal fMll;

    /** 客户编码(必填项) */
    @TableField("f_khbm")
    private String fKhbm;

    /** 客户名称 */
    @TableField("f_cty_baseproperty")
    private String fCtyBaseproperty;

    /** 客户简称 */
    @TableField("f_cty_baseproperty1")
    private String fCtyBaseproperty1;

    /** 销售员 */
    @TableField("f_xsy1")
    private String fXsy1;

    /** 业务员 */
    @TableField("f_ywy")
    private String fYwy;

    /** 客户需求(必填项) */
    @TableField("fkhxq")
    private String fkhxq;

    /** shipping to */
    @TableField("f_qyd")
    private String fQyd;

    /** payment terms */
    @TableField("f_fkfs")
    private String fFkfs;

    /** 客户分组 */
    @TableField("f_cty_baseproperty4")
    private String fCtyBaseproperty4;

    /** 表头价税合计 */
    @TableField("f_btjshj")
    private BigDecimal fBtjshj;

    /** 价税合计本位币 */
    @TableField("f_jshjbwb")
    private BigDecimal fJshjbwb;

    /** 表头成本合计 */
    @TableField("f_btcbhj")
    private BigDecimal fBtcbhj;

    /** email */
    @TableField("f_email")
    private String fEmail;

    /** mob */
    @TableField("f_mob")
    private String fMob;

    /** tel */
    @TableField("f_tel")
    private String fTel;

    /** fax */
    @TableField("f_fax")
    private String fFax;

    /** 毛净利润 */
    @TableField("fmjlr")
    private BigDecimal fmjlr;

    /** 采购报价日期 */
    @TableField("f_cgbjrq")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fCgbjrq;

    /** 包装费 */
    @TableField("f_bzf")
    private BigDecimal fBzf;

    /** 货代费 */
    @TableField("f_hdf")
    private BigDecimal fHdf;

    /** 陆运费 */
    @TableField("f_lyf")
    private BigDecimal fLyf;

    /** 其他费用 */
    @TableField("f_qtfy")
    private BigDecimal fQtfy;

    /** 费用小计 */
    @TableField("f_fyxj")
    private BigDecimal fFyxj;

    /** 后勤报价反馈 */
    @TableField("f_hqbjfk")
    private String fHqbjfk;

    /** 询价单详情列表 */
    private List<InquiryOrderEntry> entries;
}
