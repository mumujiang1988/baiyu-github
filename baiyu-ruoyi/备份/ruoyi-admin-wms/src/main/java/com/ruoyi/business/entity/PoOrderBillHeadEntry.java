package com.ruoyi.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("po_order_bill_head_entry")
public class PoOrderBillHeadEntry implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 采购订单单据编号 */
    private String fid;

    /** 采购订单行号 */
    private String fbillNo;
    /** 是否报关 */
    private Integer fSfbg;

    /** 交期红线 */
    private String fJqhx;

    /** 产品大类 */
    private String fCplb;

    /** 包装补做 */
    private Integer fBzbz;

    /** 客户货号 */
    private String fKhhh;

    /** 产品代码 */
    private String fCpdm;

    /** 产品名称 */
    private String fOraBaseproperty;

    /** 产品规格 */
    private String fOraBaseproperty1;

    /** 供应商物料编码 */
    private String fGyswlbm;

    /** 供应商物料名称 */
    private String fGyswlmc;

    /** 采购数量 */
    private BigDecimal fqty;

    /** 产品数量 */
    private BigDecimal fCpsl;

    /** 采购单位 */
    private String funitid;

    /** 计价单位 */
    private String fpriceunitid;

    /** 库存单位 */
    private String fstockunitid;

    /** 库存单位数量 */
    private BigDecimal fstockqty;

    /** 基本单位 */
    private String fbaseunitid;

    /** 交货日期 */
    private LocalDate fdeliverydate;

    /** 参考价 */
    private BigDecimal fCkj;

    /** 成本价 */
    private BigDecimal fCbj;

    /** 单价 */
    private BigDecimal fprice;

    /** 含税单价 */
    private BigDecimal ftaxprice;

    /** 加价含税单价 */
    private BigDecimal fJjhsdj;

    /** 加价价税合计 */
    private BigDecimal fJjjshj;

    /** 开票含税单价 */
    private BigDecimal fKphsdj;

    /** 折扣率% */
    private BigDecimal fentrydiscountrate;

    /** 税率% */
    private BigDecimal fentrytaxrate;

    /** 税额 */
    private BigDecimal fentrytaxamount;

    /** 价税合计 */
    private BigDecimal fallamount;

    /** 金额 */
    private BigDecimal fentryamount;

    /** 销售数量 */
    private BigDecimal fsalqty;

    /** 先收票数量 */
    private BigDecimal fstockbasefinapqty;

    /** 核算价 */
    private BigDecimal fOraDecimal;

    /** 累计验收金额 */
    private BigDecimal fsumaccallamount;

    /** 累计验收比例 */
    private BigDecimal fsumaccrate;

    /** 销售数 */
    private BigDecimal fXss;

    /** 工厂图片 */
    private String fTp;

    /** 图片2 */
    private String fTp2;

    /** 报关中文品名 */
    private String fBgzwpm;

    /** 报关英文品名 */
    private String fBgywpm;

    /** 验货数量 */
    private BigDecimal fYhs;

    /** 验货状态 */
    private String fbillstatus;

    /** 装箱数 */
    private Integer fZxs;

    /** 毛总重 */
    private BigDecimal fMzz;

    /** 总箱数 */
    private Integer fXs;

    /** 包材费用数量 */
    private BigDecimal fBcfysl;

    /** 包材费用状态 */
    private String fbillstatus1;

    /** 下推收料订单数量 */
    private BigDecimal fXtslddsl;

    /** 是否完全下推收料单 */
    private String fbillstatus3;

    /** 验货比例 */
    private BigDecimal fYhbl;

    /** 包装要求 */
    private String fBzfs;

    /** 客户简称 */
    private String fKhjc1;

    /** 客户 */
    private String fKh1;

    /** 说明书附件 */
    private String fSmsfj;

    /** 产品供应商 */
    private String fCpgys;

    /** 包材接收地 */
    private String fBcjsd;

    /** 包材接收人 */
    private String fBcjsr;

    /** 包材接收人电话 */
    private String fBcjsrdh;

    /** 报关单位 */
    private String fBgdw;

    /** 申报要素 */
    private String fSbys;

    /** 包材承运商 */
    private String fCys;

    /** 快递单号 */
    private String fKddh;

    /** 寄出日期 */
    private LocalDate fJcrq;

    /** 价格调整原因 */
    private String fJgtzyy;

    /** 产品质量要求 */
    private String fCpzlyq;

    /** 包装要求 */
    private String fbzyq;

    /** 特殊要求 */
    private String ftsyq;

    /** 产品图片 */
    private String fcptp;

    /** 包装工厂提供 */
    private Integer fbzgctg;

    /** 工厂货号 */
    private String fgchh;

    /** 销售订单号 */
    private String fxsddh;

    /** 销售订单行号 */
    private String fxsddhh;

    /** 审核日期 */
    private LocalDate fshrq;

    /** 下单到入库天数 */
    private Integer fxddrkts;

    /** 报价人 */
    private String fbjr;

    /** 推品人 */
    private String ftpr;

    /** 条码 */
    private String fTm;

    /** 产品类别 */
    private String fCplb1;

    /** 备注 */
    private String fentrynote;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
