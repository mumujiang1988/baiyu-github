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
    private String FDocumentType;
    /**订单状态*/
    private String FOrderStatus; //A:未关闭 B:已关闭 C:业务终止手动关闭
    /** 金蝶主键 */
    private Long FFid;
    /** 单据编号 */
    private String FBillNo;
    /** 销售合同日期 */
    private Date FDate;
    /** 客户编码 */
    private String FCustId;
    /** 客户简称 */
    private String F_ora_BaseProperty;
    /** 客户合同号 */
    private String F_khhth;
    /** 客户联系人 */
    private String F_kglxr;
    /** 客户邮箱 */
    private String F_cty_BaseProperty1;
    /** 结算币别 */
    private String FSettleCurrId;
    /** 提成比例% */
    private BigDecimal F_tcbl;
    /** 客户首单（非样品） */
    private Integer F_khsd;
    /** 是否含税 */
    private Integer FIsIncludedTax;
    /** 是否报关 */
    private Integer F_sfbg;
    /** 销售员 */
    private String FSalerId;
    /** 毛净利润率% */
    private BigDecimal F_lrl;
    /** 净利润率% */
    private BigDecimal F_jlrl;
    /**国家*/
    private String FState;
    /** 抵运国家 */
    private String F_gj;
    /** 贸易方式 */
    private String F_myfs;
    /** 占用信保 */
    private Integer F_zyxb;
    /** 银行账号 */
    private String F_yhzh;
    /** 客户交期 */
    private Date F_ctyDate;
    /** 所属公司 */
    private String F_sygs;
    /** 收款条件 */
    private String FRecConditionId;
    /** 包装方式 */
    private String F_bzfs;
    /** 收货方 */
    private String FReceiveId;
    /** 结算方 */
    private String FSettleId;
    /** 结算方地址 */
    private String FSettleAddress;
    /** 付款方 */
    private String FChargeId;
    /** 锁汇汇率 */
    private BigDecimal F_shhl;
    /** 锁汇状态 */
    private Integer F_shzt;
    /** 锁汇金额 */
    private BigDecimal F_shje;
    /** 解汇日期 */
    private Date F_ctyDate1;
    /** 创建人 */
    private String FCreatorId;
    /** 创建日期 */
    private Date FCreateDate;
    /** 最后修改人 */
    private String FModifierId;
    /** 最后修改日期 */
    private Date FModifyDate;
    /** 整单折扣额 */
    private BigDecimal FAllDisCount;
    /** 预收比例% */
    private BigDecimal F_ysbl1;
    /** 税额 */
    private BigDecimal FBillTaxAmount;
    /** 金额 */
    private BigDecimal FBillAmount;
    /** 本位币 */
    private String FLocalCurrId;
    /** 汇率类型 */
    private String FExchangeTypeId;
    /** 汇率 */
    private BigDecimal FExchangeRate;
    /** 未收款金额 */
    private BigDecimal FPlannotRecAmount;
    /** 累计收款金额 */
    private BigDecimal FPlanAllRecAmount;
    /** 累计退款金额 */
    private BigDecimal FPlanRefundAmount;

    /**
     * 明细列表*/
    private List<SaleOrderEntry> entryList;
    /**成本核算*/
    private SaleOrderCost saleOrderCost;
}
