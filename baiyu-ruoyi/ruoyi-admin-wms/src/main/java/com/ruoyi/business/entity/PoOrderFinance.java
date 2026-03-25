package com.ruoyi.business.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PoOrderFinance {

    /** 主键（行号） */
    private Long fentryId;

    /** 主表ID */
    private Long fid;

    /** 整单费用 */
    private BigDecimal fbillCost;

    /** 金额 */
    private BigDecimal fbillAmount;

    /** 价税合计 */
    private BigDecimal fbillAllAmount;

    /** 价目表 */
    private Long fpriceListId;

    /** 折扣表 */
    private Long fdiscountListId;

    /** 结算币别（必填） */
    private Long fsettleCurrId;

    /** 税额 */
    private BigDecimal fbillTaxAmount;

    /** 定价时点（必填） */
    private String fpriceTimePoint;

    /** 本位币 */
    private Long flocalCurrId;

    /** 金额（本位币） */
    private BigDecimal fbillAmountLc;

    /** 税额（本位币） */
    private BigDecimal fbillTaxAmountLc;

    /** 价税合计（本位币） */
    private BigDecimal fbillAllAmountLc;

    /** 汇率类型 */
    private Long fexchangeTypeId;

    /** 汇率 */
    private BigDecimal fexchangeRate;

    /** 付款条件 */
    private Long fpayConditionId;

    /** 预付单号 */
    private Long fpayAdvanceBillId;

    /** 是否含税 */
    private Boolean fisIncludedTax;

    /** 结算方式 */
    private Long fsettleModeId;

    /** 预付已核销金额 */
    private BigDecimal fpreMatchAmountFor;

    /** 单次预付额度 */
    private BigDecimal fpayadvanceamount;

    /** 单次预付额度汇率 */
    private BigDecimal fsuptoorderexchangebusrate;

    /** 价外税 */
    private Boolean fispriceexcludetax;

    /** 保证金比例% */
    private BigDecimal fdepositratio;

    /** 保证金 */
    private BigDecimal fdeposit;

    /** 关联保证金 */
    private BigDecimal frelatedeposit;

    /** 整单折扣额 */
    private BigDecimal falldiscount;
}
