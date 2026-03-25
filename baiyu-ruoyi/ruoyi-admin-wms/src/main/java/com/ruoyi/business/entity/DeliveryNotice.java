package com.ruoyi.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 金蝶发货通知单 - 主表
 * 一对多：主表（DeliveryNotice） -> 明细（DeliveryNoticeEntry）
 */
@Data
@TableName("delivery_notice")
public class DeliveryNotice {

    /** 主键 ID，自增 */
    @TableField("id")
    private Long id;

    /** 金蝶主键 ID */
    @TableField("FID")
    private Long FID;

    /** 单据编号 */
    @TableField("FBillNo")
    private String FBillNo;

    /** 单据状态 */
    @TableField("FDocumentStatus")
    private String FDocumentStatus;

    /** 日期 (必填项) */
    @TableField("FDate")
    private Date FDate;

    /** 客户 (必填项) */
    @TableField("FCustomerID")
    private String FCustomerID;

    /** 销售员 */
    @TableField("FSalesManID")
    private String FSalesManID;

    /** 承运商 */
    @TableField("FCarrierID")
    private String FCarrierID;

    /** 收货方 */
    @TableField("FReceiverID")
    private String FReceiverID;

    /** 结算方 */
    @TableField("FSettleID")
    private String FSettleID;

    /** 付款方 */
    @TableField("FPayerID")
    private String FPayerID;

    /** 创建人 */
    @TableField("FCreatorId")
    private String FCreatorId;

    /** 创建日期 */
    @TableField("FCreateDate")
    private Date FCreateDate;

    /** 最后修改日期 */
    @TableField("FModifyDate")
    private Date FModifyDate;

    /** 审核日期 */
    @TableField("FApproveDate")
    private Date FApproveDate;

    /** 作废日期 */
    @TableField("FCancelDate")
    private Date FCancelDate;

    /** 最后修改人 */
    @TableField("FModifierId")
    private String FModifierId;

    /** 单据类型 (必填项) */
    @TableField("FBillTypeID")
    private String FBillTypeID;

    /** 审核人 */
    @TableField("FApproverID")
    private String FApproverID;

    /** 作废人 */
    @TableField("FCancellerID")
    private String FCancellerID;

    /** 作废状态 */
    @TableField("FCancelStatus")
    private String FCancelStatus;

    /** 货主 */
    @TableField("FOwnerIdHead")
    private String FOwnerIdHead;

    /** 收款条件 */
    @TableField("FRECEIPTCONDITIONID")
    private String FRECEIPTCONDITIONID;

    /** 交货方式 */
    @TableField("FHeadDeliveryWay")
    private String FHeadDeliveryWay;

    /** 收货方地址 */
    @TableField("FReceiveAddress")
    private String FReceiveAddress;

    /** 交货地点 */
    @TableField("FHeadLocId")
    private String FHeadLocId;

    /** 本位币 */
    @TableField("FLocalCurrID")
    private String FLocalCurrID;

    /** 汇率类型 */
    @TableField("FExchangeTypeID")
    private String FExchangeTypeID;

    /** 汇率 */
    @TableField("FExchangeRate")
    private BigDecimal FExchangeRate;

    /** 结算方式 */
    @TableField("FSettleTypeID")
    private String FSettleTypeID;

    /** 结算币别 (必填项) */
    @TableField("FSettleCurrID")
    private String FSettleCurrID;

    /** 税额 */
    @TableField("FBillTaxAmount")
    private BigDecimal FBillTaxAmount;

    /** 金额 */
    @TableField("FBillAmount")
    private BigDecimal FBillAmount;

    /** 价税合计 */
    @TableField("FBillAllAmount")
    private BigDecimal FBillAllAmount;

    /** 是否含税 */
    @TableField("FIsIncludedTax")
    private Integer FIsIncludedTax;

    /** 整单折扣额 */
    @TableField("FAllDisCount")
    private BigDecimal FAllDisCount;

    /** 明细列表，一对多关系，不存数据库 */
    @TableField(exist = false)
    private List<DeliveryNoticeEntry> entries;

    /** 创建人 (系统字段) */
    @TableField("created_by")
    private String createdBy;

    /** 创建时间 (系统字段) */
    @TableField("created_at")
    private Date createdAt;

    /** 更新时间 (系统字段) */
    @TableField("updated_at")
    private Date updatedAt;
}
