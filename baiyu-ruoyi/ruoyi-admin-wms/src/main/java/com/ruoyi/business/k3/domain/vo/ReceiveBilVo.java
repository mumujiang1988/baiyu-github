package com.ruoyi.business.k3.domain.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.business.k3.domain.entity.ReceiveBill;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = ReceiveBill.class)
public class ReceiveBilVo {

    //实体主键
    @TableId(value = "id")
    private Long id;
    //单据编号
    @TableField("f_bill_no")
    private String fbillNo;
    //'单据状态'
    @TableField("f_document_status")
    private String fdocumentStatus;
    //审核人
    @TableField("f_approver_id")
    private String fapproverId;
    //结算组织
    @TableField("f_settleorg_id")
    private String fsettleorgId;
    //审核日期
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime faproveDate;
    //销售组织
    @TableField("f_saleorg_id")
    private  String  fsaleorgId;
    //汇率类型
    @TableField("f_exchange_type")
    private String fexchangeType;
    //表头-应收金额
    @TableField("f_receivea_mountfor_h")
    private BigDecimal freceiveamountforH;
    //本位币
    @TableField("f_mainbookcur_id")
    private String FmainbookcurId;
    //表头-应收金额本位币
    @TableField("f_receiveamount_h")
    private BigDecimal freceiveamountH;
    //币别
    @TableField("f_currency_id")
    private String fcurrencyId;
    //业务日期
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fdate;
    //汇率
    @TableField("f_exchangerate")
    private BigDecimal fexchangerate;
    //核销状态
    @TableField("f_writtenoff_status")
    private String fwrittenoffStatus;
    //销售员
    @TableField("f_saleer_id")
    private String fsaleerId;
    //销售组
    @TableField("f_sale_group_id")
    private String fsaleGroupId;
    //销售部门
    @TableField("f_sale_dept_id")
    private String fsaleDeptId;
    //单据类型
    @TableField("f_bill_type_id")
    private String fbillTypeId;
    //表头-实收金额
    @TableField("f_realrecamountfor")
    private BigDecimal frealrecamountfor;
    //表头-实收金额本位币
    @TableField("f_realrecamount_h")
    private BigDecimal frealrecamountH;
    //会计核算体系
    @TableField("f_accountsystem")
    private String faccountsystem;
    //作废日期
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fcancelDate;
    //作废状态
    @TableField("f_cancel_status")
    private String fcancelStatus;
    //作废人
    @TableField("f_canceller_id")
    private String fcancellerId;
    //往来单位类型
    @TableField("f_contactunit_type")
    private String fcontactunitType;
    //往来单位
    @TableField("f_contactunit")
    private String fcontactunit;

    //付款单位类型
    @TableField("f_payunit_type")
    private String fpayunitType;
    //付款单位
    @TableField("f_payuni")
    private String fpayuni;
    //业务类型
    @TableField("f_business_type")
    private String fbusinessType;
    //信用检查结果
    @TableField("f_credit_check_result")
    private String fcreditCheckResult;
    //是否期初单据
    @TableField("f_isinit")
    private String fisinit;
    //部门
    @TableField("f_department")
    private String fdepartment;
    //收款组织
    @TableField("f_payorg_id")
    private String fpayorgId;
    //是否相同组织
    @TableField("f_is_same_org")
    private String fisSameOrg;
    //来源系统
    @TableField("f_source_system")
    private String fsourceSystem;
    //现销
    @TableField("f_cashsale")
    private String fcashsale;
    //结算币别
    @TableField("f_settcur")
    private String fsettcur;
    //结算汇率
    @TableField("f_settlerate")
    private BigDecimal fsettlerate;
    //收款金额
    @TableField("f_recamountfor")
    private BigDecimal frecamountfor;
    //表头-收款金额本位币
    @TableField("f_recamount")
    private BigDecimal frecamount;
    //B2C业务
    @TableField("f_isb2c")
    private String fIsb2c;
    //流水号/对账码
    @TableField("f_wbsettleno")
    private String fwbsettleno;
    //是否转销
    @TableField("f_is_write_off")
    private String fisWriteOff;
    //核销方式
    @TableField("f_match_method_id")
    private String fmatchMethodId;
    //扫描点
    @TableField("f_scan_point")
    private String fscanPoint;
    //金蝶支付流水号
    @TableField("f_kdpay_on")
    private String fkdpayOn;
    //备注
    @TableField("f_remark")
    private String fremark;
    //第三方单据编号
    @TableField("f_thirdbillno")
    private String fthirdbillno;
    //结算本位币
    @TableField("f_settlemainbook_id")
    private String fsettlemainbookId;
    //结算汇率类型
    @TableField("F_settleexchang_type")
    private String fsettleexchangType;
    //转出往来单位
    @TableField("f_outcontact_id")
    private String foutcontactId;
    //转出往来单位类型
    @TableField("f_outcontact_type")
    private String foutcontactType;
    //管易财务流水内码
    @TableField("f_gyaccountwater_id")
    private String fgyaccountwaterId;
    //是否下推携带汇率到结算汇率
    @TableField("f_iscarryrate")
    private String fiscarryrate;
    //预设基础资料字段1
    @TableField("f_presetbase1")
    private String fpresetbase1;
    //预设基础资料字段2
    @TableField("f_presetbase2")
    private String fpresetbase2;
    //预设辅助资料字段1
    @TableField("f_presetssistant1")
    private String fpresetssistant1;
    //预设文本字段2
    @TableField("f_presetssistant2")
    private String fpresetssistant2;
    //预设文本字段1
    @TableField("f_presettext1")
    private String fpresettext1;
    //预设文本字段2
    @TableField("f_presettext2")
    private String fpresettext2;
    //来源销售订单下推
    @TableField("f_is_fromsalorder")
    private String fisfromsalorder;
    //是否同一核算组织
    @TableField("f_virIs_same_acct_org")
    private String fvirIsAameAcctOrg;
    //来源票据号
    @TableField("f_source_bill_number")
    private String fsourceBillNumber;
    //出运单号
    @TableField("f_cydh")
    private String fcydh;
    //银行水单单号
    @TableField("f_yhsddh")
    private String fyhsddh;
    //原始汇率
    @TableField("f_yshl")
    private String fyshl;
    //客户代码
    @TableField("f_khdm")
    private String fkhdm;
    //客户简称
    @TableField("f_cty_base_property")
    private String fctyBaseProperty;
    //水单
    @TableField("f_sd")
    private String fsd;

}
