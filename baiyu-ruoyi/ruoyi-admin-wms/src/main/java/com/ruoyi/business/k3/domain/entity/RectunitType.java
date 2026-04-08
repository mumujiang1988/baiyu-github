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
 * 付款申请单
 * */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("f_rectunit_type")
public class RectunitType extends BaseEntity {
    @TableId(value = "fid")
    private Long fId;
    //单据编号
    private String fbillNo;
    //单据状态
    private String fdocumentStatus;
    //审核人
    private String fapproverId;
   //结算组织
    private String fsettleorgId;
    //审核日期
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fapproveDate;
    //采购组织
    private String fpurchaseorgId;
    //应付金额
    private BigDecimal fpayamountforh;
    //币别
    private String fcurrencyId;
    //申请日期
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fdate;
    //修改日期
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fmodifyDate;
    //单据类型
    private String fbilltypeId;
    //采购员
    private String fpurchaserId;
    //采购部门
    private String fpurchasedeptId;
    //申请付款金额
    private BigDecimal fapplyamountforh;
    //会计核算体系
    private String faccountsystem;
    //作废人
    private String fcancellerId;
    //作废状态
     private  String fcancelStatus;
     //作废状态
     @TableField(fill = FieldFill.INSERT)
     @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fcancelDate;
     //往来单位类型
    private String fcontactunitType;
    //往来单位
    private String fcontactunit;
    //收款单位类型
    private String frectunitType;
    //收款单位
    private String frectunit;
    //业务类型
    private String fbusinessType;
    //部门
    private String fdepartment;
    //付款组织
    private String fpayorgId;
    //销售组织
    private String fsaleorgId;
    //销售部门
    private String fsaledeptId;
    //销售员
    private String fsaleerId;
    //本位币
    private String fmainbookId;
    //汇率
    private String fexchangerate;
    //汇率类型
    private String fexchangeType;
    //来源系统
    private String f_sourcesystem;
    //是否信贷业务
    private String fiscredit;
    //银行账号
    private String fbankactId;
    //申请组织
    private String fapplyorgId;
    //扫描点
    private String fscanPoint;
    //关闭状态
    private String fclosesTatus;
    //关闭人
    private String fcloserId;
    //关闭日期
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fcloseDate;
    //结算币别
    private String fsettlecur;
    //实际申请金额
    private BigDecimal frealapplyamountfor;
    //结算汇率
    private String fselttlerate;
    //申请付款金额本位币
    private String fapplyamount;
    //采购组
    private String fpurchasergroupId;
    //销售组
    private String fsalegroupId;
    //是否其他预付
    private String fisborrow;
    //是否下推携带汇率到结算汇率
    private String fiscarryrate;
    //多收款人
    private String fmorereceive;
    //Convert to string literal
    private String fpresetbase1;
    //预设基础资料字段2
    private String fpresetbase2;
    //预设辅助资料字段1
    private String fpresetassistant1;
    //预设辅助资料字段2
    private String fpresetassistant2;
    //预设文本字段1
    private String fpresettext1;
    //预设文本字段2
    private String fpresettext2;
    //实报实付
    private BigDecimal frealPay;
    //销售购销合同号
    private String fxsgxht;
    //出运单号
    private String fcydh;
    //表头备注
    private String fbtbz;
    //销售部门
    private String foraBase;
    //销售员
    private String foraBase1;
    //包材订单
    private String fbcdd;
    //付款公司
    private String ffkgs;
    //费用类
    private String ffyl;

}
