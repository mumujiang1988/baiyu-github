package com.ruoyi.business.k3.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *付款申请单
 * */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("f_rectunit_type")
public class PaymentApplicationForm extends BaseEntity {
    //实体主键
    @TableId(value = "id")
    private Long id;
    //单据编号
    private String fBillNo;
    //单据状态
    private String fDocumentStatus;
    //审核人
    private String fApproverId;
    //结算组织
    private String fSettleorgId;
    //审核日期
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fApproveDate;
    //采购组织
    private String fPurchaseorgId;
    //应付金额
    private BigDecimal fPayamountforH;
    //币别
    private String fCurrencyId;
    //申请日期
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fDate;
    //单据类型
    private String fBilltypeId;
    //采购员
    private String fPurchaserId;
    //采购部门
    private String fPurchasedeptId;
    //申请付款金额
    private BigDecimal fApplyamountforH;
    //会计核算体系
    private String fAccountSystem;
    //作废人
    private  String fCancellerId;
    //作废状态
    private String fCancelStatus;
    //作废日期
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fCancelDate;
    //往来单位类型
    private String fContactunitType;
    //往来单位
    private String fContactunit;
    //收款单位类型
    private String fRectunitType;
    //收款单位
    private String fRectunit;
    //业务类型
    private String fBusinessType;
    //部门
    private String fDepartment;
    //付款组织
    private String fPayorgId;
    //销售组织
    private String fSaleorgId;
    //销售部门
    private String fSaledeptId;
    //销售员
    private String fSaleerId;
    //本位币
    private String fMainbookId;
    //汇率
    private BigDecimal fExchangerate;
    //汇率类型
    private String fExchangeType;
    //来源系统
    private String fSourceSystem;
    //是否信贷业务
    private String fIscredit;
    //银行账号
    private String fBankactId;
    //申请组织
    private String fApplyorgId;
    //扫描点
    private String fScanPoint;
    //关闭状态
    private String fClosesTatus;
    //关闭人
    private String fCloserId;
    //关闭日期
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fCloseDate;
    //结算币别
    private String fSettlecur;
    //实际申请金额
    private BigDecimal fRealapplyamountFor;
    //结算汇率
    private BigDecimal fSelttlerate;
    //申请付款金额本位币
    private BigDecimal fApplyamount;
    //采购组
    private String fPurchasergroupId;
    //销售组
    private String fSalegroupId;
    //是否其他预付
    private String fIsBorrow;
    //是否下推携带汇率到结算汇率
    private String fIsCarryrate;
    //多收款人
    private String fMorereceive;
    //预设基础资料字段1
    private String fPresetbase1;
    //预设基础资料字段2
    private String fPresetbase2;
    //预设辅助资料字段1
    private String fPresetassistant1;
    //预设辅助资料字段2
    private String fPresetassistant2;
    //预设文本字段1
    private String fPresettext1;
    //预设文本字段2
    private String fPresettext2;
    //实报实付
    private String fRealPay;
    //销售购销合同号
    private String fXsgxht;
    //出运单号
    private String fCydh;
    //表头备注
    private String fBtbz;
    //销售部门
    private String fOraBase;
    //销售员
    private String fOraBase1;
    //包材订单
    private String fBcdd;
    //付款公司
    private String fFkgs;
    //费用类
    private String fFyl;

}
