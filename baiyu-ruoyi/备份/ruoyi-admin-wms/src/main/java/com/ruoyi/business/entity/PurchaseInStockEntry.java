package com.ruoyi.business.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

/**
 * 采购入库单明细表
 */
@Data
public class PurchaseInStockEntry {

    /** 主键 */
    private String id;
    /** 金蝶主键 */
    private String fbillno;

    private String fb;

    /** 产品类别 */
    private String fcplb;

    /** 交期红线 */
    private String fjqhx;

    /** 物料编码（必填项） */
    private String fmaterialId;

    /** 仓库 */
    private String fstockid;

    /** 生产日期 */
    private LocalDate fproducedate;

    /** 备注 */
    private String fnote;

    /** 入库类型 */
    private String fwwintype;

    /** 批号 */
    private String flot;

    /** 仓位 */
    private String fstocklocid;

    /** 收货批号 */
    private String freceivelot;

    /** 供应商批号 */
    private String fsupplierlot;

    /** 毛重 */
    private BigDecimal fgrossweight;

    /** 净重 */
    private BigDecimal fnetweight;

    /** 物料名称 */
    private String fmaterialname;

    /** 物料类别 */
    private String fmaterialtype;

    /** 规格型号 */
    private String fuom;

    /** 合同单号 */
    private String fcontractlno;

    /** 应收数量 */
    private BigDecimal fmustqty;

    /** 实收数量 */
    private BigDecimal frealqty;

    /** 数量（库存辅单位） */
    private BigDecimal fauxunitqty;

    /** 有效期至 */
    private LocalDate fexpirydate;

    /** 库存状态 */
    private String fstockstatusid;

    /** 不良品折让金额 */
    private BigDecimal frejectsdiscountamount;

    /** 客户货号 */
    private String fkhhh;

    /** 包装费用状态 */
    private String fbzfyzt;

    /** 客户 */
    private String fkh;

    /** 销售员 */
    private String fxsyy;

    /** 跟单员 */
    private String fgdy1;

    /** 包装要求 */
    private String fbzyq;

    /** 特殊要求 */
    private String ftsyq;

    /** 英文品名 */
    private String fctybaseproperty;

    /** 包材接收人 */
    private String fbcjsr;

    /** 包材接收人电话 */
    private String fbcjsdh;

    /** 参考价 */
    private BigDecimal fckj;

    /** 价格调整原因 */
    private String fjgtzyy;

    /** 制单日期 */
    private LocalDate fzdrq;

    /** 订单审核日期 */
    private LocalDate fddshrq;

    /** 入库周期 */
    private String frkzq;

    /** 库存数量 */
    private BigDecimal fkcsl;

    /** 长 */
    private String fc;

    /** 收料截止日期 */
    private LocalDate fsljzrq;

    /** 含税单价 */
    private BigDecimal ftaxprice;

    /** 成本价 */
    private BigDecimal fcostprice;

    /** 税率 (%) */
    private BigDecimal fentrytax_rate;

    /** 税额 */
    private BigDecimal fentrytaxamount;

    /** 折扣率 (%) */
    private BigDecimal fdiscountrate;

    /** 价格系数 */
    private BigDecimal fpricecoefficient;

    /** 计价数量 */
    private BigDecimal fpriceunitqty;

    /** 净价 */
    private BigDecimal ftaxnetprice;

    /** 总成本 */
    private BigDecimal fentrycostamount;

    /** 价税合计 */
    private BigDecimal fallamount;

    /** 税额 (本位币) */
    private BigDecimal ftaxamountlc;

    /** 总成本 (本位币) */
    private BigDecimal fcostamountlc;

    /** 价税合计 (本位币) */
    private BigDecimal fallamountlc;

    /** 入库库存更新标志 */
    private Integer fstockflag;

    /** 基本单位单价 */
    private BigDecimal fbaseunitprice;

    /** 库存单位（必填项） */
    private String funitid;

    /** 基本单位 */
    private String fbaseunit_id;

    /** 库存基本数量 */
    private BigDecimal fbaseunitqty;

    /** 库存辅单位 */
    private String fauxunitid;

    /** 计价单位（必填项） */
    private String fpriceunitid;

    /** 订单单号 */
    private String fpoorderno;

    /** 收货库存状态 */
    private String freceivestockstatus;

    /** 客户英文描述 */
    private String fctytext1;

    /** 验收报告 */
    private String fpeuuattachmenttzk;

    /** 体积 */
    private BigDecimal ftj;

    /** 新老产品 */
    private String fpeuubasepropertyre5;

    /** 实收箱数 */
    private BigDecimal fssxs;

    /** 破损数 */
    private BigDecimal foradecimal;

    /** 破损详情 */
    private String foramulcombo;

    /** 破损图片记录 */
    private String forapicture;

    /** 销售订单 */
    private String fxsdd;

    /** 检验状态 */
    private String fjyzt;

    /** 检验单数量 */
    private BigDecimal fjydsl;

    /** 产品编码 */
    private String fcpbm;

    /** 产品名称 */
    private String forabaseproperty;

    /** 验货比例 */
    private String fyhbl;

    /** 验货日期 */
    private Date fyhrq;

    /** 税率% */
    private BigDecimal ftaxrate;

    /** 税额 */
    private BigDecimal ftaxamount;

    /** 计入成本比例% */
    private BigDecimal fcostpercent;

    /** 计入成本金额 */
    private BigDecimal ftaxcostamount;

    /** 增值税 */
    private BigDecimal fvat;

    /** 对账中 */
    private Integer fisreconciliationing;

    /** 当前对账单号 */
    private String freconciliationbillno;

    /** 彩贴图 */
    private String fctt;

    /** 激光打字 */
    private String fjgdz;

    /** 外箱正唛 */
    private String fwxzm;

    /** 外箱侧唛 */
    private String fwxcm;

    /** 彩套 */
    private String fct;

    /** 彩盒 */
    private String fch;

    /** 包装图 */
    private String fbaozt;

    /** 内箱正唛 */
    private String fnxzm;

    /** 内箱侧唛 */
    private String fnxcm;

    /** 说明书附件 */
    private String fctyattachment;

}
