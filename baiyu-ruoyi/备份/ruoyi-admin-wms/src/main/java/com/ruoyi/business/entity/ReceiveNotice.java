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
 * Receive Notice Main Table (receive_notice)
 * Corresponds to Kingdee K3 Cloud Receive Notice main table
 */
@Data
@TableName("receive_notice")
public class ReceiveNotice {

    /** 实体主键 */
    @TableId("FID")
    private Long fid;

    /** 单据编号 */
    @TableField("FBillNo")
    private String fBillNo;


    /** Document Status */
    @TableField("FDocumentStatus")
    private String fDocumentStatus;

    /** 单据类型 */
    @TableField("FBillTypeID")
    private Long fBillTypeID;

    /** 收料日期 */
    @TableField("FDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fDate;

    /** 收料部门ID */
    @TableField("FReceiveDeptId")
    private Long fReceiveDeptId;

    /** 采购组织ID */
    @TableField("FPurOrgId")
    private Long fPurOrgId;

    /** 收料人ID */
    @TableField("FReceiverId")
    private Long fReceiverId;

    /** 仓库费 */
    @TableField("F_jcf")
    private BigDecimal fjcf;

    /** 供应商ID */
    @TableField("FSupplierId")
    private String fSupplierId;

    /** 厂家包装 */
    @TableField("F_gcbz")
    private String fgcbz;

    /** 交货地址 */
    @TableField("F_shdz")
    private String fshdz;

    /** 截止日期Date */
    @TableField("F_DATE_jz")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fDATEjz;

    /** 总计仓库费 */
    @TableField("F_djtjcf")
    private BigDecimal fdjtjcf;

    /** 供货方 ID */
    @TableField("FSupplyId")
    private String fSupplyId;

    /** 供应商联系人ID */
    @TableField("FProviderContactId")
    private Long fProviderContactId;

    /** 供货方地址 */
    @TableField("FSupplyAddress")
    private String fSupplyAddress;

    /** 结算ID */
    @TableField("FSettleId")
    private String fSettleId;

    /** 负责人ID */
    @TableField("FChargeId")
    private String fChargeId;

    /** 结算方式ID */
    @TableField("FSettleModeId")
    private String fSettleModeId;

    /** 供货方联系人 Condition ID */
    @TableField("FPayConditionId")
    private String fPayConditionId;

    /** 结算币种 ID */
    @TableField("FSettleCurrId")
    private String fSettleCurrId;

    /** 总折扣金额 */
    @TableField("FAllDisCount")
    private BigDecimal fAllDisCount;

    /** 创建者ID */
    @TableField("FCreatorId")
    private String fCreatorId;

    /** 创建日期 */
    @TableField("FCreateDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fCreateDate;

    /** 最后修改人 ID */
    @TableField("FModifierId")
    private String fModifierId;

    /** 最后修改日期 */
    @TableField("FModifyDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fModifyDate;

    /** 审核日期 Date */
    @TableField("FApproveDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fApproveDate;

    /** 审核人 ID */
    @TableField("FApproverId")
    private String fApproverId;

    /** 订单审批日期 */
    @TableField("F_cty_Date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fctyDate;

    /** 收料通知单详情 */
    private List<ReceiveNoticeEntry> receiveNoticeEntry;
}
