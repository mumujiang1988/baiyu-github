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
 *销售出库单
 * */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("f_sal_outbound")
public class SalOutbound extends BaseEntity {

    //实体主键
    @TableId(value = "id")
    private Long id;
    //单据编号
    private String fBillNo;
    //单据状态
    private String fDocumentStatus;
    //销售组织 (必填项)
    private String fSaleorgId;
    //日期 (必填项)
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fDate;
    //发货组织 (必填项)
    private String fStockorgId;
    //客户 (必填项)
    private String fCustomerId;
    //发货部门
    private String fDeliverydeptId;
    //销售部门
    private String fSaledeptId;
    //库存组
    private String fStockergroupId;
    //仓管员
    private String fStockerId;
    //销售组
    private String fSalesgroupId;
    //销售员
    private String fSalesmanId;
    //承运商
    private String fCarrierId;
    //运输单号
    private String fCarriageNo;
    //收货方
    private String fReceiverId;
    //结算方
    private String fSettleId;
    //付款方
    private String fPayerId;
    //审核人
    private String FApproverId;
    //审核日期
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fApproveDate;
    //作废状态
    private String fSancelStatus;
    //作废人
    private String fCancellerId;
    //作废日期
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fCancelDate;
    //单据类型 (必填项)
    private String fBilltypeId;
    //货主类型
    private String fOwnerTypeidHead;
    //货主
    private String fOwneridHead;
    //业务类型
    private String fBussinessType;
    //收货方地址
    private String fReceiveAddress;
    //交货地点
    private String fHeadlocationId;
    //信用检查结果
    private String fCreditcheckResult;
    //跨组织业务类型
    private String fTransferbizType;
    //对应组织
    private String fCorrespondorgId;
    //收货方联系人
    private String fReceivercontactId;
    //组织间结算跨法人标识
    private String fIsinterlegalPerson;
    //零售单日结生成
    private String fGenfromposCmk;
    //联系电话
    private String fLinkPhone;
    //收货人姓名
    private String fLinkMan;
    //销售门店
    private String fBranchId;
    //序列号上传
    private String fScanBox;
    //创建日期偏移单位
    private String fCdateoffsetUnit;
    //创建日期偏移量
    private String fCdateoffsetValue;
    //交货明细执行地址(后台用)
    private String fPlanrecAddress;
    //整单服务或费用
    private String fIstotalServiceOrcost;
    //备注
    private String fNote;
    //拆单新单标识
    private String fDisassemblyFlag;
    //网店编码
    private String fShopNumber;
    //管易发货日期
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fGyDate;
    //销售渠道
    private String fSaleChannel;
    //物流单号
    private String fLogisticsnos;
    //预设基础资料字段1
    private String fPresetbase1;
    //预设基础资料字段2
    private String fPresetbase2;
    //预设辅助资料字段1
    private String fPresetassistant1;
    //预设辅助资料字段2
    private String fPresetassistant2;
    //关联应收状态
    private String fArStatus;
    //出运单号 (必填项)
    private String fCydh;
    //客户简称
    private String fOraBaseproperty;
    //贸易术语
    private String fMysy;
    //港口名称
    private String fGkmc;
    //启运港
    private String fQyg;
    //客户全称
    private String fKhqc;
    //单据报关状态
    private String fDjbgzt;
    //清关资料
    private String fOraAttachment;
    //提单
    private String fOraAttachment1;
    //电放保函
    private String fOraAttachment2;
    //预录入单
    private String fOraAttachment3;
    //放行单
    private String fOraAttachment4;
    //客户地址
    private String fKhdz;
    //提成比例-作废
    private BigDecimal fTcbl;
    //装箱单
    private String fZxd;
    //交货地址
    private String fJhdz;
    //核对状态
    private String fHdzt;
    //发货清单
    private String fFhqd;
    //退税税率
    private BigDecimal fTssl;
    //包材费用
    private BigDecimal fBcfy;
    //包装费
    private BigDecimal fCtyDecimal1;
    //售后订单
    private String fShdd;
    //锁汇汇率
    private BigDecimal fShhl;
    //锁汇金额
    private BigDecimal fShje;
    //解汇日期
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fJhrq;
    //锁汇状态
    private String fShzt;
    //客户编码
    private String fCtyBaseproperty;
    //包装费用承担
    private String fBzfycd;
    //托盘费
    private BigDecimal fTpf;

}


