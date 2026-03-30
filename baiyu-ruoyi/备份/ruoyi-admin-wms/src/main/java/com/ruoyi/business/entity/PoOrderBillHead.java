package com.ruoyi.business.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
public class PoOrderBillHead {

    /** 主键 */
    private String fid;

    /** 单据编号 */
    private String fbillNo;

    /** 单据状态 */
    private String fdocumentStatus;

    /** 采购日期（必填） */
    private Date fdate;

    /** 供应商（必填） */
    private String fsupplierId;

    /** 创建人 */
    private String fcreatorId;

    /** 创建日期 */
    private Date fcreateDate;

    /** 最后修改人 */
    private String fmodifierId;

    /** 最后修改日期 */
    private Date fmodifyDate;

    /** 审核人 */
    private String fapproverId;

    /** 审核日期 */
    private Date fapproveDate;

    /** 作废人 */
    private String fcancellerId;

    /** 作废日期 */
    private Date fcancelDate;

    /** 入库状态 */
    private String fcloseStatus;

    /** 关闭人 */
    private String fcloserId;

    /** 关闭日期 */
    private Date fcloseDate;

    /** 单据类型（必填） */
    private String fbillTypeId;

    /** 结算方 */
    private String fsettleId;

    /** 收款方 */
    private String fchargeId;

    /** 供货方 */
    private String fproviderId;

    /** 变更人 */
    private String fchangerId;

    /** 变更日期 */
    private Date fchangeDate;

    /** 变更原因 */
    private String fchangeReason;

    /** 业务类型（必填） */
    private String fbusinessType;

    /** 供货方地址 */
    private String fproviderAddress;

    /** 指定供应商 */
    private String fassignSupplierId;
    /** 确认状态 */
    private String fconfirmStatus;

    /** 确认人 */
    private String fconfirmerId;

    /** 确认日期 */
    private Date fconfirmDate;

    /** 供货方联系人 */
    private String fproviderContactId;

    /** 职务 */
    private String fproviderJob;

    /** 手机 */
    private String fproviderPhone;

    /** 源单编号 */
    private String fsourceBillNo;

    /** 变更状态 */
    private String fchangeStatus;

    /** 验收方式 */
    private String facctype;

    /** 邮箱 */
    private String fproviderEmail;

    /** 关闭原因 */
    private String fcloseReason;

    /** 分销销售订单生成 */
    private Boolean fisUseDrpSalePoPush;

    /** 生成直运出入库 */
    private Boolean fisCreateStraightOutIn;

    /** 合同类型 */
    private String fcontractType;

    /** 样品订单 */
    private Boolean fYpdd;

    /** 采购合同到期日 */
    private String fOraDate;

    /** 质量标准及技术要求 */
    private String fZlbz;

    /** 正唛图片1 */
    private String fZmtp1;

    /** 正唛图片2 */
    private String fZmtp2;

    /** 正唛图片3 */
    private String fZmtp3;

    /** 正唛条码 */
    private String fZmtxm;

    /** 正唛描述 */
    private String fZmms;

    /** 侧唛图片1 */
    private String fCmtp1;

    /** 侧唛图片2 */
    private String fCmtp2;

    /** 侧唛描述 */
    private String fCmms;

    /** 侧唛条码 */
    private String fCmtxm;

    /** 包装标准及要求 */
    private String fBzbzjyq;

    /** 客户 */
    private String fKh;

    /** 加价比例% */
    private BigDecimal fJjdbl;

    /** 购销合同号 */
    private String fGxhth;

    /** 客户简称 */
    private String fKhjc;

    /** 总金额 */
    private BigDecimal fZje;

    /** 跟单员1 */
    private String fGdy1;

    /** 包材订单 */
    private Boolean fOraCheckbox1;

    /** 工厂包装 */
    private String fGcbz;

    /** 销售员 */
    private String fXsy;

    /** 客户交期 */
    private String fKhjqnew;

    /** 售后订单 */
    private Boolean fShdd;

    /** 订单要求 */
    private String fYebz;

    /** 变更供应商原因 */
    private String fbggysyy;

    /** 结算方式即交期 */
    private String fjsfsjjq;

    /** 合同条款 1-3 */
    private String fhttk13;
    /**付款条件*/
    private String FPayConditionId;
    /** 合同条款 5-10 */
    private String fhttk510;

    /** 供应商评级 */
    private String fCtyBaseproperty1;

    /** 工厂问题 */
    private String fCtyBaseproperty2;

    /** 完成率% */
    private BigDecimal fwcl;

    /** 最终确认交期 */
    private Date fzzqrjq;

    /** 供应商账户 */
    private String fGyszh;

    /** 私账 */
    private String fSz;
    /** 结算币别 */
    private String fSettleCurrId;

    /** 分录结算方式 */
    private String fEntrySettleModeId;

    /** 汇率类型 */
    private String fExchangeTypeId;

    /** 汇率 */
    private BigDecimal fExchangeRate;

    /** 价目表 */
    private String fPriceListId;

    /** 定价时间点 */
    private String fPriceTimePoint;
    /**折扣表*/
    private String FDiscountListId;
    /** 保证金比例 */
    private BigDecimal fDepositRatio;

    /**保证金*/
    private BigDecimal FDeposit;
    /**整单折扣额*/
    private BigDecimal FAllDisCount;
    /**含税*/
    private String FIsIncludedTax;
    /**价外税*/
    private String FIspriceExcludetax;

    /** 单据税额（本位币） */
    private BigDecimal fBillTaxAmountLc;

    /** 单据金额（本位币） */
    private BigDecimal fBillAmountLc;

    /** 单据金额 */
    private BigDecimal fBillAmount;

    /** 单据价税合计 */
    private BigDecimal fBillAllAmount;

    /** 本位币 */
    private String fLocalCurrId;
    /** 金额(本位币) */
    private BigDecimal FBillAmount_LC;
    /** 税额(本位币) */
    private BigDecimal FBillTaxAmount_LC;
    /** 单据价税合计(本位币) */
    private BigDecimal FBillAllAmount_LC;

    /** 单据税额 */
    private BigDecimal fBillTaxAmount;

    /** 价税合计 */
    private BigDecimal fAllAmount;

    /** 分录金额 */
    private BigDecimal fEntryAmount;

    /** 累计付金额 */
    private BigDecimal fAllPayAmount;

    /** 累计付款申请付款金额 */
    private BigDecimal fAllPayApplyAmount;
    /**申请单未付款金额*/
    private BigDecimal FAllAppyNoPayAmount;

    /** 已申请未付金额 */
    private BigDecimal fAllApplyNoPayAmount;

    /** 付款关联金额 */
    private BigDecimal fPayRelatAmount;

    /** 累计退款金额 */
    private BigDecimal fAllRefundAmount;

    /** 未付金额 */
    private BigDecimal fAllNoPayAmount;

    /** 采购订单详情列表 */
    private List<PoOrderBillHeadEntry> entries;

}
