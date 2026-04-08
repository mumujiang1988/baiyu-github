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
* 收款单明细
* */
@Data
@NoArgsConstructor
@TableName("f_receivebill_entry")
public class ReceivebillEntry{
    @TableId(value = "id")
    private Long id;

    @TableField("f_entry_id")
    private String fentryId;
    //结算方式
    @TableField("f_settle_type_id")
    private String fsettleTypeId;
    //折后金额
    @TableField("f_setterecamountfor")
    private BigDecimal fsetterecamountfor;
    //现金折扣
    @TableField("f_settledistamountfor")
    private BigDecimal fsettledistamountfor;
    //表体-应收金额
    @TableField("f_rectotalamountfor")
    private BigDecimal frectotalamountfor;
    //折后金额本位币
    @TableField("f_settlerecamount")
    private BigDecimal fsettlerecamount;
    //现金折扣本位币
    @TableField("f_settledistamount")
    private BigDecimal fsettledistamount;
    //表体-应收金额本位币
    @TableField("f_rectotalamount")
    private BigDecimal frectotalamount;
    //表体明细-核销状态
    @TableField("f_writtenoffstatus")
    private String fwrittenoffstatus;
    //表体明细-已核销金额
    @TableField("f_writtenoffmountfor_d")
    private BigDecimal fwrittenoffmountforD;
    //备注
    @TableField("f_comment")
    private String fcomment;
    //对方银行账号
    @TableField("f_oppositebankaccount")
    private String foppositebankaccount;
    //对方账户名称
    @TableField("f_oppositeccount_name")
    private String foppositeccountName;
    //预收销售订单
    @TableField("f_receivitem")
    private String freceivitem;
    //手续费
    @TableField("f_handlingchargefor")
    private BigDecimal fhandlingchargefor;
    //手续费本位币
    @TableField("f_handlingcharge")
    private BigDecimal fhandlingcharge;
    //表体-实收金额本位币
    @TableField("f_realrecamountfor_d")
    private BigDecimal frealrecamountforD;
    // 表体-实收金额本位币
    @TableField("f_realrecamount_d")
    private BigDecimal frealrecamountd;
    //关联总金额
    @TableField("F_asstotalamountfor")
    private BigDecimal fasstotalamountfor;
    //预收项目类型
    @TableField("F_receivitemt_ype")
    private String freceivitemType;
    //预收销售订单号内码
    @TableField("f_sale_order_id")
    private String fsaleOrderId;
    //我方银行账号
    @TableField("f_account_id")
    private String faccountId;
    //我方账户名称
    @TableField("f_recaccount_name")
    private String frecaccountName;
    //我方开户行
    @TableField("f_recbank_id")
    private String frecbankId;
    //长短款
    @TableField("f_overunderamountfor")
    private BigDecimal foverunderamountfor;
    //对方开户行
    @TableField("f_oppositebank_name")
    private String foppositebankName;
    //结算号
    @TableField("f_settleno")
    private String fsettleno;
    //勾对
    @TableField("f_blend")
    private String fblend;
    //收款用途
    @TableField("f_purpose_id")
    private String fpurposeId;
    //长短款本位币
    @TableField("f_overunderamount")
    private BigDecimal foverunderamount;
    //内部账号
    @TableField("f_inneraccount_id")
    private String finneraccountId;
    //内部账户名称
    @TableField("f_inneraccount_name")
    private String finneraccountName;
    //退款关联金额
    @TableField("f_refundamount")
    private String frefundamount;
    //现金账号
    @TableField("f_cash_account")
    private String fcashAccount;
    //收款金额
    @TableField("f_recamountfor_e")
    private BigDecimal frecamountfore;
    //收款金额本位币
    @TableField("f_recamount_e")
    private BigDecimal frecamounte;
    //登账日期
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fPostDate;
    //是否登账
    @TableField("f_is_post")
    private String fisPost;
    //物料编码
    @TableField("f_material_id")
    private String fmaterialId;
    //物料名称
    @TableField("f_material_name")
    private String fmaterialName;
    //销售订单号
    @TableField("f_saleorder_no")
    private String fsaleorderNo;
    //订单行号
    @TableField("f_material_seq")
    private String fmaterialseq;

    //销售订单明细内码
    @TableField("f_orderentry_id")
    private String forderentryId;
    //保证金转货款金额
    @TableField("f_topayment_amount_for")
    private BigDecimal ftopaymentamountfor;

    //保证金转货款金额
    @TableField("f_topaymentamountrom")
    private BigDecimal ftopaymentamountrom;
    //已核销金额本位币
    @TableField("f_writtenoffamount")
    private BigDecimal fwrittenoffamount;
    //未核销金额
    @TableField("f_notverificateamount")
    private BigDecimal fnotverificateamount;
    //计价单位
    @TableField("f_priceunit_id")
    private String fpriceunitId;
    //含税单价
    @TableField("f_price")
    private BigDecimal fprice;
    //数量
    @TableField("f_qty")
    private BigDecimal fqty;
    //费用项目
    @TableField("f_costid")
    private String fcostid;
    //费用承担部门
    @TableField("f_costdepartmentid")
    private String fcostdepartmentid;
    //费用项目名称
    @TableField("f_cost_name")
    private String fcostName;
    //关联行ID
    @TableField("f_link_rowId")
    private String flinkRowId;
    //销售订单（基础资料）
    @TableField("f_saleorder_base")
    private String fsaleorderBase;
    //销售订单（基础资料）
    @TableField("f_relate_refund_amount")
    private BigDecimal frelateRefundAmount;
    //预估税率(%)
    @TableField("f_entrytaxrate")
    private BigDecimal fentrytaxrate;
    //税额
    @TableField("f_taxamountfor")
    private BigDecimal ftaxamountfor;
    //税额本位币
    @TableField("f_taxamount")
    private BigDecimal ftaxamount;
    //应收金额不含税
    @TableField("f_recnotaxamountfor")
    private BigDecimal frecnotaxamountfor;
    //应收金额不含税本位币
    @TableField("f_recnotaxamount")
    private BigDecimal frecnotaxamount;
    //已核销税额
    @TableField("f_writtenoffaxamountfor")
    private BigDecimal fwrittenoffaxamountfor;
    //已核销税额本位币
    @TableField("F_writtenofftaxamount")
    private BigDecimal Fwrittenofftaxamount;
    //出运单号
    @TableField("f_cydh_djt")
    private String fcydhDjt;
    //费用项目
    @TableField("f_dxfyx")
    private String fdxfyx;

    //费用承担部门
    @TableField("f_fycdbm")
    private String ffycdbm;
    //客户
    @TableField("f_kh")
    private String fkh;

}
