package com.ruoyi.business.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 采购入库单主表
 */
@Data
public class PurchaseInStock {

    /** 主键 */
    private Long id;

    /** 金蝶id */
    private String fid;

    /** 单据编号 */
    private String fBillNo;

    /** 单据状态 */
    private String FDocumentStatus;

    /** 单据类型（必填项） */
    private String FBillTypeID;

    /** 入库日期 */
    private LocalDateTime FDate;

    /** 供应商（必填项） */
    private String FSupplierId;

    /** 供货方 */
    private String FSupplyId;

    /** 结算方 */
    private String FSettleId;

    /** 收款方 */
    private String FChargeId;

    /** 采购员 */
    private String FPurchaserId;

    /** 创建人 */
    private String FCreatorId;

    /** 创建日期 */
    private LocalDateTime FCreateDate;

    /** 修改人 */
    private String FModifierId;

    /** 修改日期 */
    private LocalDateTime FModifyDate;

    /** 审核人 */
    private String FApproverId;

    /** 审核日期 */
    private LocalDateTime FApproveDate;

    /** 确认人 */
    private String FConfirmerId;

    /** 确认日期 */
    private LocalDateTime FConfirmDate;

    /** 供货方地址 */
    private String FSupplyAddress;

    /** 仓管员 */
    private String FStockerId;

    /** 结算币别 */
    private String FSettleCurrId;

    /** 本位币 */
    private String FLocalCurrId;

    /** 汇率类型 */
    private String FExchangeTypeId;

    /** 汇率 */
    private BigDecimal FExchangeRate;

    /** 含税 */
    private Integer FIsIncludedTax;

    /** 付款条件 */
    private String FPayConditionId;

    /** 结算方式 */
    private String FSettleTypeId;

    /** 定价时点（必填项） */
    private String FPriceTimePoint;

    /** 结算组织（必填项） */
    private String FSettleOrgId;

    /** 金额 */
    private BigDecimal FBillAmount;

    /** 税额 */
    private BigDecimal FBillTaxAmount;

    /** 价税合计 */
    private BigDecimal FBillAllAmount;

    /** 明细信息 */
    private List<PurchaseInStockEntry> FEntity;
}
