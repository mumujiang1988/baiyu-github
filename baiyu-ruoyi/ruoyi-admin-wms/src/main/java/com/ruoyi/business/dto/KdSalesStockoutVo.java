package com.ruoyi.business.dto;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 金蝶销售出库单主表视图对象数据 t_kd_sales_stockout
 *
 * @author aiflowy
 */
@Data
@ExcelIgnoreUnannotated
public class KdSalesStockoutVo {

    /**
     * 实体主键
     */
    @ExcelProperty(value = "实体主键")
    private String fid;

    /**
     * 单据编号
     */
    @ExcelProperty(value = "单据编号")
    private String fbillNo;

    /**
     * 单据状态
     */
    @ExcelProperty(value = "单据状态")
    private String fdocumentStatus;

    /**
     * 销售组织 (必填项)
     */
    @ExcelProperty(value = "销售组织")
    private String fsaleOrgId;

    /**
     * 日期 (必填项)
     */
    @ExcelProperty(value = "日期")
    private LocalDateTime fdate;

    /**
     * 发货组织 (必填项)
     */
    @ExcelProperty(value = "发货组织")
    private String fstockOrgId;

    /**
     * 客户 (必填项)
     */
    @ExcelProperty(value = "客户")
    private String fcustomerID;

    /**
     * 发货部门
     */
    @ExcelProperty(value = "发货部门")
    private String fdeliveryDeptID;

    /**
     * 销售部门
     */
    @ExcelProperty(value = "销售部门")
    private String fsaleDeptID;

    /**
     * 销售组
     */
    @ExcelProperty(value = "销售组")
    private String fsalesGroupID;

    /**
     * 销售员
     */
    @ExcelProperty(value = "销售员")
    private String fsalesManID;

    /**
     * 承运商 (必填项)
     */
    @ExcelProperty(value = "承运商")
    private String fcarrierID;

    /**
     * 运输单号
     */
    @ExcelProperty(value = "运输单号")
    private String fcarriageNO;

    /**
     * 收货方
     */
    @ExcelProperty(value = "收货方")
    private String freceiverID;

    /**
     * 结算方
     */
    @ExcelProperty(value = "结算方")
    private String fsettleID;

    /**
     * 付款方
     */
    @ExcelProperty(value = "付款方")
    private String fpayerID;

    /**
     * 提成比例
     */
    @ExcelProperty(value = "提成比例")
    private BigDecimal fTcblNEW;

    /**
     * 创建日期
     */
    @ExcelProperty(value = "创建日期")
    private LocalDateTime fcreateDate;

    /**
     * 最后修改人
     */
    @ExcelProperty(value = "最后修改人")
    private String fmodifierId;

    /**
     * 最后修改日期
     */
    @ExcelProperty(value = "最后修改日期")
    private LocalDateTime fmodifyDate;

    /**
     * 创建人
     */
    @ExcelProperty(value = "创建人")
    private String fcreatorId;

    /**
     * 审核人
     */
    @ExcelProperty(value = "审核人")
    private String fapproverID;

    /**
     * 审核日期
     */
    @ExcelProperty(value = "审核日期")
    private LocalDateTime fapproveDate;

    /**
     * 作废状态
     */
    @ExcelProperty(value = "作废状态")
    private String fcancelStatus;

    /**
     * 作废人
     */
    @ExcelProperty(value = "作废人")
    private String fcancellerID;

    /**
     * 作废日期
     */
    @ExcelProperty(value = "作废日期")
    private LocalDateTime fcancelDate;

    /**
     * 单据类型 (必填项)
     */
    @ExcelProperty(value = "单据类型")
    private String fbillTypeID;

    /**
     * 货主类型
     */
    @ExcelProperty(value = "货主类型")
    private String fownerTypeIdHead;

    /**
     * 货主
     */
    @ExcelProperty(value = "货主")
    private String fownerIdHead;

    /**
     * 业务类型
     */
    @ExcelProperty(value = "业务类型")
    private String fbussinessType;

    /**
     * 收货方地址
     */
    @ExcelProperty(value = "收货方地址")
    private String freceiveAddress;

    /**
     * 交货地点
     */
    @ExcelProperty(value = "交货地点")
    private String fheadLocationId;

    /**
     * 收货方联系人
     */
    @ExcelProperty(value = "收货方联系人")
    private String freceiverContactID;

    /**
     * 联系电话
     */
    @ExcelProperty(value = "联系电话")
    private String flinkPhone;

    /**
     * 收货人姓名
     */
    @ExcelProperty(value = "收货人姓名")
    private String flinkMan;

    /**
     * 销售门店
     */
    @ExcelProperty(value = "销售门店")
    private String fbranchId;

    /**
     * 出运单号 (必填项)
     */
    @ExcelProperty(value = "出运单号")
    private String fCydh;

    /**
     * 客户简称
     */
    @ExcelProperty(value = "客户简称")
    private String fOraBaseProperty;

    /**
     * 贸易术语
     */
    @ExcelProperty(value = "贸易术语")
    private String fMysy;

    /**
     * 港口名称
     */
    @ExcelProperty(value = "港口名称")
    private String fGkmc;

    /**
     * 启运港
     */
    @ExcelProperty(value = "启运港")
    private String fQyg;

    /**
     * 客户全称
     */
    @ExcelProperty(value = "客户全称")
    private String fKhqc;

    /**
     * 单据报关状态
     */
    @ExcelProperty(value = "单据报关状态")
    private String fDjbgzt;

    /**
     * 清关资料
     */
    @ExcelProperty(value = "清关资料")
    private String fOraAttachment;

    /**
     * 提单
     */
    @ExcelProperty(value = "提单")
    private String fOraAttachment1;

    /**
     * 客户地址
     */
    @ExcelProperty(value = "客户地址")
    private String fKhdz;

    /**
     * 装箱单
     */
    @ExcelProperty(value = "装箱单")
    private String fZxd;

    /**
     * 交货地址
     */
    @ExcelProperty(value = "交货地址")
    private String fJhdz;

    /**
     * 核对状态
     */
    @ExcelProperty(value = "核对状态")
    private String fHdzt;

    /**
     * 发货清单
     */
    @ExcelProperty(value = "发货清单")
    private String fFhqd;

    /**
     * 退税税率
     */
    @ExcelProperty(value = "退税税率")
    private BigDecimal fTssl;

    /**
     * 包材费用
     */
    @ExcelProperty(value = "包材费用")
    private BigDecimal fBcfy;

    /**
     * 包装费
     */
    @ExcelProperty(value = "包装费")
    private BigDecimal fCtyDecimal1;

    /**
     * 售后订单
     */
    @ExcelProperty(value = "售后订单")
    private String fShdd;

    /**
     * 锁汇汇率
     */
    @ExcelProperty(value = "锁汇汇率")
    private BigDecimal fShhl;

    /**
     * 锁汇金额
     */
    @ExcelProperty(value = "锁汇金额")
    private BigDecimal fShje;

    /**
     * 解汇日期
     */
    @ExcelProperty(value = "解汇日期")
    private LocalDateTime fJhrq;

    /**
     * 锁汇状态
     */
    @ExcelProperty(value = "锁汇状态")
    private String fShzt;

    /**
     * 客户编码
     */
    @ExcelProperty(value = "客户编码")
    private String fCtyBaseProperty;

    /**
     * 包装费用承担
     */
    @ExcelProperty(value = "包装费用承担")
    private String fBzfycd;

    /**
     * 托盘费
     */
    @ExcelProperty(value = "托盘费")
    private BigDecimal fTpf;

    /**
     * 结算组织 (必填项)
     */
    @ExcelProperty(value = "结算组织")
    private String fSettleOrgID;

    /**
     * 本位币
     */
    @ExcelProperty(value = "本位币")
    private String fLocalCurrID;

    /**
     * 汇率类型
     */
    @ExcelProperty(value = "汇率类型")
    private String fExchangeTypeID;

    /**
     * 汇率
     */
    @ExcelProperty(value = "汇率")
    private BigDecimal fExchangeRate;

    /**
     * 结算币别 (必填项)
     */
    @ExcelProperty(value = "结算币别")
    private String fSettleCurrID;

    /**
     * 结算方式
     */
    @ExcelProperty(value = "结算方式")
    private String fSettleTypeID;

    /**
     * 整单折扣额
     */
    @ExcelProperty(value = "整单折扣额")
    private BigDecimal fAllDisCount;

}
