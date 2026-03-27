package com.ruoyi.business.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class SaleOrder {

    /** 主键 */
    private Long id;

    /**单据类型*/
    private String documentType;
    /**订单状态*/
    private String orderStatus; //A:未关闭 B:已关闭 C:业务终止手动关闭
    /** 金蝶主键 */
    private Long fid;
    /** 单据编号 */
    private String fBillNo;
    /** 销售合同日期 */
    private Date fDate;
    /** 客户编码 */
    private String fCustId;
    /** 客户简称 */
    private String fOraBaseProperty;
    /** 客户合同号 */
    private String fKhhth;
    /** 客户联系人 */
    private String fKglxr;
    /** 客户邮箱 */
    private String fCtyBaseProperty1;
    /** 结算币别 */
    private String fSettleCurrId;
    /** 提成比例% */
    private BigDecimal fTcbl;
    /** 客户首单（非样品） */
    private Integer fKhsd;
    /** 是否含税 */
    private Integer fIsIncludedTax;
    /** 是否报关 */
    private Integer fSfbg;
    /** 销售员 */
    private String fSalerId;
    /** 毛净利润率% */
    private BigDecimal fLrl;
    /** 净利润率% */
    private BigDecimal fJlrl;
    /**国家*/
    private String fstate;
    /** 抵运国家 */
    private String fGj;
    /** 贸易方式 */
    private String fMyfs;
    /** 占用信保 */
    private Integer fZyxb;
    /** 银行账号 */
    private String fYhzh;
    /** 客户交期 */
    private Date fCtyDate;
    /** 所属公司 */
    private String fSygs;
    /** 收款条件 */
    private String fRecConditionId;
    /** 包装方式 */
    private String fbzfs;
    /** 收货方 */
    private String fReceiveId;
    /** 结算方 */
    private String fSettleId;
    /** 结算方地址 */
    private String fSettleAddress;
    /** 付款方 */
    private String fChargeId;
    /** 锁汇汇率 */
    private BigDecimal fShhl;
    /** 锁汇状态 */
    private Integer fShzt;
    /** 锁汇金额 */
    private BigDecimal fShje;
    /** 解汇日期 */
    private Date fCtyDate1;
    /** 创建人 */
    private String fCreatorId;
    /** 创建日期 */
    private Date fCreateDate;
    /** 最后修改人 */
    private String fModifierId;
    /** 最后修改日期 */
    private Date fModifyDate;
    /** 整单折扣额 */
    private BigDecimal fAllDisCount;
    /** 预收比例% */
    private BigDecimal fYsbl1;
    /** 税额 */
    private BigDecimal fBillTaxAmount;
    /** 金额 */
    private BigDecimal fBillAmount;
    /** 本位币 */
    private String fLocalCurrId;
    /** 汇率类型 */
    private String fExchangeTypeId;
    /** 汇率 */
    private BigDecimal fExchangeRate;
    /** 未收款金额 */
    private BigDecimal fPlannotRecAmount;
    /** 累计收款金额 */
    private BigDecimal fPlanAllRecAmount;
    /** 累计退款金额 */
    private BigDecimal fPlanRefundAmount;

    /**
     * 明细列表*/
    private List<SaleOrderEntry> entryList;
    /**成本核算*/
    private SaleOrderCost saleOrderCost;
}
