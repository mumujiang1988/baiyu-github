package com.ruoyi.business.k3.domain.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.business.k3.domain.entity.RectunitDetail;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = RectunitDetail.class)
public class RectunitDetailVo {

    @TableId(value = "id")
    private Long id;
    private Long fEntryId;
    //结算方式
    private String fSettletypeId;
    //应付金额
    private BigDecimal fPayamountFor;
    //对方银行账号
    private String fEachbankaccount;
    //申请付款金额
    private BigDecimal fApplyamountFor;
    //付款用途
    private String fPaypurposeId;
    //收款用途
    private String fArpurposeId;
    //源单类型
    private String fSourceType;
    //V
    private String fSrcbillNo;
    //到期日
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fEnddate;
    //期望付款日期
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fExpectpayDate;
    //对方账户名称
    private String fEeachccountName;
    //对方开户行
    private String feachbankName;
    //备注（作废
    private String fCcomment;
    //源单行内码
    private String fSrcrowId;
    //付退款关联金额
    private BigDecimal fRelatepayamount;
    //作废—收款退款关联金额
    private BigDecimal fRelaterefundamount;
    //费用项目
    private String fCostId;
    //申请还本金额
    private BigDecimal fApplypclamount;
    //申请付利息金额
    private BigDecimal fApplyinstamount;
    //关联付利息金额
    private BigDecimal fWritepclamount;
    //备注
    private String fDescription;
    //物料编码
    private String fMaterialId;
    //物料名称
    private String fMaterialName;
    //订单号
    private String fPurchaseorderNo;
    //订单行号
    private String fMaterialseq;
    //订单明细内码
    private String fOrderentryId;
    //含税单价
    private BigDecimal fPrice;
    //数量
    private BigDecimal fQty;
    //计价单位
    private String fPriceunitId;
    //付(退)款关联数量
    private String fRelatepayqty;
    //未付款金额
    private String fUnpaidamount;
    //SwiftCode
    private String fSwiftcode;
    //税额
    private BigDecimal fTaxamount;
    //银行网点
    private String fBankdetail;
    //费用承担部门
    private String fExpensedeptId;
    //应付比例（%）
    private BigDecimal fYfbl;
    //出运单号
    private String fCydh1;
    //采购订单号
    private String fCgddh;
    //费用承担部门1
    private String fFycdbm;
    //比例%
    private BigDecimal fCtyDecimal;
    //承担方式
    private String fCdfs;
    //开票状态
    private String fKpzt;
    //客户
    private String fkh;
    //客户简称
    private String fCtyBaseproperty;
    //付款日期
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fPeuuDate83g;

}
