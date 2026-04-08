package com.ruoyi.business.k3.domain.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.business.k3.domain.entity.ReceivebillEntry;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = ReceivebillEntry.class)
public class ReceivebillEntryVo {
    @TableId(value = "fentryId")
    private Long fentryId;
    //结算方式
    private String fsettleTypeId;
    //折后金额
    private BigDecimal fsetterecamountfor;
    //现金折扣
    private BigDecimal fsettledistamountfor;
    //表体-应收金额
    private String frectotalamountfor;
    //折后金额本位币
    private String fsettlerecamount;
    //现金折扣本位币
    private BigDecimal fsettledistamount;
    //表体-应收金额本位币
    private BigDecimal frectotalamount;
    //表体明细-核销状态
    private String f_writtenoffstatus;
    //表体明细-已核销金额
    private BigDecimal fwrittenoffmountforD;
    //备注
    private String fcomment;
    //对方银行账号
    private String foppositebankaccount;
    //对方账户名称
    private String foppositeccountName;
    //预收销售订单
    private String freceivitem;
    //手续费
    private String fhandlingchargefor;
    //手续费本位币
    private String fhandlingcharge;
    //手续费本位币
    private BigDecimal frealrecamountforD;
    //关联总金额
    private BigDecimal fasstotalamountfor;
    //预收项目类型
    private String freceivitemType;
    //预收销售订单号内码
    private String fsaleOrderId;
    //我方银行账号
    private String faccountId;
    //我方账户名称
    private String frecaccountName;
    //我方开户行
    private String frecbankId;
    //长短款
    private BigDecimal foverunderamountfor;
    //对方开户行
    private String foppositebankName;
    //结算号
    private String fsettleno;
    //勾对
    private String fblend;
    //收款用途
    private String fpurposeId;
    //长短款本位币
    private String foverunderamount;
    //内部账号
    private String finneraccountId;
    //内部账户名称
    private String finneraccountName;
    //内部账户名称
    private BigDecimal frefundamount;
    //现金账号
    private String fcashAccount;
    //收款金额
    private BigDecimal frecamountfore;
    //收款金额本位币
    private String frecamounte;
    //登账日期
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fpostdate;
    //是否登账
    private String fisPost;
    //物料编码
    private String fmaterialId;
    //物料名称
    private String fmaterialName;
    //销售订单号
    private String fsaleorderNo;
    //销售订单明细内码
    private String forderentryId;
    //保证金转货款金额
    private BigDecimal ftopaymentamountrom;
    //已核销金额本位币
    private String fwrittenoffamount;
    //未核销金额
    private BigDecimal fnotverificateamount;
    //计价单位
    private String fpriceunitId;
    //含税单价
    private BigDecimal fprice;
    //数量
    private Integer fqty;
    //费用项目
    private String fcostid;
    //费用承担部门
    private String fcostdepartmentid;
    //费用项目名称
    private String fcostName;
    //关联行ID
    private String flinkRowId;
    //销售订单（基础资料）
    private String fsaleorderBase;
    //销售订单（基础资料）
    private BigDecimal frelateRefundAmount;
    //预估税率(%)
    private String fentrytaxrate;
    //税额
    private BigDecimal ftaxamountfor;
    //税额本位币
    private String ftaxamount;
    //应收金额不含税
    private BigDecimal frecnotaxamountfor;
    //应收金额不含税本位币
    private BigDecimal frecnotaxamount;
    //已核销税额
    private BigDecimal fwrittenoffaxamountfor;
    //已核销税额本位币
    private BigDecimal Fwrittenofftaxamount;
    private String fcydhDjt;
    //费用项目
    private String fdxfyx;
    //费用承担部门
    private String ffycdbm;
    //客户
    private String fkh;

}
