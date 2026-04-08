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

@Data
@NoArgsConstructor
@TableName("f_sal_outbound_details")
public class SalOutboundDetails{
    @TableId(value = "id")
    private Long id;
    //实体主键
    private Long fEntryId;
    //客户物料编码
    private String fCustmatId;
    //客户物料名称
    private String fCustmatName;
    //物料编码 (必填项)
    private String fMaterialId;
    //物料名称
    private String fMaterialName;
    //规格型号
    private String fMateriaModel;
    //物料类别
    private String fMateriaType;
    //库存单位 (必填项)
    private String fUnitId;
    //应发数量
    private BigDecimal  fMustQty;

    /**
     * 应发数量
     */
    private BigDecimal fUstQty;

    /**
     * 实发数量
     */
    private BigDecimal fRealQty;

    /**
     * 仓库
     */
    private String fStockId;

    /**
     * 库存状态
     */
    private String fStocksStatusId;

    /**
     * 货主类型 (必填项)
     */
    private String fOwnerTypeId;

    /**
     * 货主 (必填项)
     */
    private String fOwnerId;

    /**
     * 保管者类型
     */
    private String fKeeperTypeId;

    /**
     * 保管者
     */
    private String fKeeperId;

    /**
     * 备注
     */
    private String fEntrynote;

    /**
     * BOM版本
     */
    private String fBomId;

    /**
     * 库存基本数量
     */
    private BigDecimal fBaseUnitQty;

    /**
     * 库存辅单位
     */
    private String fAuxunitId;

    /**
     * 库存辅单位数量
     */
    private BigDecimal fAuxunitQty;

    /**
     * 成本价（本位币）
     */
    private BigDecimal fCostPrice;

    /**
     * 总成本
     */
    private BigDecimal fEntrycostAmount;

    /**
     * 总成本(本位币)
     */
    private BigDecimal fCostAmountLc;

    /**
     * 关联退货数量
     */
    private BigDecimal fReturnQty;

    /**
     * 累计退货通知数量
     */
    private BigDecimal fSumretNoticeQty;

    /**
     * 累计退货数量
     */
    private BigDecimal fSumretStockQty;

    /**
     * 累计开票数量(作废)
     */
    private BigDecimal fInvoicedQty;

    /**
     * 累计应收数量（销售）
     */
    private BigDecimal fSuminvoicedQty;

    /**
     * 累计开票金额(作废)
     */
    private BigDecimal fSuminvoicedAmt;

    /**
     * 累计收款金额
     */
    private BigDecimal fSumreceivedAmt;

    /**
     * 关联退货数量(基本单位)
     */
    private BigDecimal fBasereturnQty;

    /**
     * 关联开票数量(基本单位)(作废)
     */
    private BigDecimal fBaseinvoicedQty;

    /**
     * 库存更新标识
     */
    private String fStockFlag;

    /**
     * 销售订单单号
     */
    private String fSoorDerno;

    /**
     * 辅助属性
     */
    private String fAuxpropId;

    /**
     * 累计退货通知数量(销售基本)
     */
    private BigDecimal fBaseSumretNoticeQty;

    /**
     * 源单类型
     */
    private String fSrcType;

    /**
     * 累计退货数量(基本单位)
     */
    private BigDecimal fBaseSumretStockQty;

    /**
     * 仓位
     */
    private String fStockLocId;

    /**
     * 仓位值集1
     */
    private String f100001;

    /**
     * 仓位
     */
    private String f100002;

    /**
     * 包材仓位
     */
    private String f100003;

    /**
     * 成品仓库-广昇
     */
    private String f100005;

    /**
     * 包材仓库-广昇
     */
    private String f100006;

    /**
     * 生产日期
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fProduceDate;

    /**
     * 有效期至
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fExpiryDate;

    /**
     * 勾稽数量（作废）
     */
    private String fJoinedQty;

    /**
     * 未勾稽数量（作废）
     */
    private String fUnjoinQty;

    /**
     * 勾稽金额（作废）
     */
    private BigDecimal fJoinedAmount;

    /**
     * 未勾稽金额（作废）
     */
    private BigDecimal fUnjoinAmount;

    /**
     * 完全勾稽（作废）
     */
    private String fUllyJoined;

    /**
     * 行勾稽状态（作废）
     */
    private String fJoinStatus;

    /**
     * 批号
     */
    private String fLot;

    /**
     * 保质期单位
     */
    private String fExpiryPeriodUnit;

    /**
     * 保质期
     */
    private Integer fExpiryPeriod;

    /**
     * 是否赠品
     */
    private String fIsFree;

    /**
     * 累计开票数量(计价基本)(作废)
     */
    private BigDecimal fBaseSuminvoicedQty;

    /**
     * 基本单位应发数量
     */
    private BigDecimal fBaseMustQty;

    /**
     * 基本单位
     */
    private String fBaseUnitId;

    /**
     * 到货确认
     */
    private String fArrivaIsTatus;

    /**
     * 到货确认人
     */
    private String FArrivalConfirmor;

    /**
     * 检验日期
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fValiDate;

    /**
     * 检验确认
     */
    private String fValidateStatus;

    /**
     * 检验确认人
     */
    private String fValidateConfirmor;

    /**
     * 计价单位
     */
    private String fPriceUnitId;

    /**
     * 计价数量
     */
    private BigDecimal fPriceUnitQty;

    /**
     * 单价
     */
    private BigDecimal fPrice;

    /**
     * 含税单价
     */
    private BigDecimal fTaxPrice;

    /**
     * 税组合
     */
    private String fTaxCombination;

    /**
     * 税率%
     */
    private BigDecimal fEntryTaxRate;

    /**
     * 价格系数
     */
    private BigDecimal fPriceCoefficient;

    /**
     * 系统定价
     */
    private BigDecimal fSysPrice;

    /**
     * 最低限价
     */
    private BigDecimal fLimitDownPrice;

    /**
     * 折前金额
     */
    private BigDecimal fBefDisAmt;

    /**
     * 折前价税合计
     */
    private BigDecimal fBefDisallAmt;

    /**
     * 折扣率%
     */
    private BigDecimal fDiscountRate;

    /**
     * 折扣额
     */
    private BigDecimal fDiscount;

    /**
     * 金额
     */
    private BigDecimal fAmount;

    /**
     * 金额（本位币）
     */
    private BigDecimal fAmountLc;

    /**
     * 税额
     */
    private BigDecimal fEntryTaxAmount;

    /**
     * 税额（本位币）
     */
    private BigDecimal fTaxAmountLc;

    /**
     * 价税合计
     */
    private BigDecimal fAllAmount;

    /**
     * 价税合计（本位币）
     */
    private BigDecimal fAllAmountLc;

    /**
     * 净价
     */
    private BigDecimal fTaxNetPrice;

    /**
     * 关联应收数量（计价基本）
     */
    private BigDecimal fBaseArjoinQty;

    /**
     * 到货日期
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fArrivalDate;

    /**
     * 业务流程
     */
    private String fBflowId;

    /**
     * 累计应收数量（销售基本）
     */
    private BigDecimal fBasearQty;

    /**
     * 关联应收金额
     */
    private BigDecimal fArjoinAmount;

    /**
     * 累计应收金额
     */
    private BigDecimal FAramount;

    /**
     * 服务上下文
     */
    private String fServiceContext;

    /**
     * 销售成本价
     */
    private BigDecimal fSalcostPrice;

    /**
     * 源单编号
     */
    private String fSrcBillNo;

    /**
     * 实收数量
     */
    private BigDecimal fActQty;

    /**
     * 关联入库数量(基本单位)
     */
    private BigDecimal fBaseJoininStockQty;

    /**
     * 关联入库数量
     */
    private BigDecimal fJoininStockQty;

    /**
     * 序列号单位
     */
    private String fSnunitId;

    /**
     * 序列号单位数量
     */
    private BigDecimal fSnQty;

    /**
     * 计划跟踪号
     */
    private String fMtoNo;

    /**
     * 项目编号
     */
    private String fProjectNo;

    /**
     * 拒收数量
     */
    private BigDecimal fRefuseQty;

    /**
     * 补货数量
     */
    private BigDecimal fRepairQty;

    /**
     * 是否补货
     */
    private Boolean fIsRepair;

    /**
     * 捡货部门
     */
    private String fPickDeptId;

    /**
     * 关联入库数量（辅单位）
     */
    private BigDecimal fSecJoininStockQty;

    /**
     * 关联退货数量（辅单位）
     */
    private BigDecimal fSecreTurnQty;

    /**
     * 消耗汇总
     */
    private Boolean fIsConsumeSum;

    /**
     * 关联应收数量
     */
    private BigDecimal fArjoinQty;

    /**
     * 控制出库数量
     */
    private BigDecimal fOutcontrol;

    /**
     * 辅单位
     */
    private String fExtauxUnitId;

    /**
     * 实发数量(辅单位)
     */
    private BigDecimal fExtauxunitQty;

    /**
     * 零售条形码
     */
    private String fBarCode;

    /**
     * 门店供货价
     */
    private BigDecimal fProPrice;

    /**
     * 供货金额
     */
    private BigDecimal ProAmount;

    /**
     * 是否零售促销
     */
    private Boolean fRetailSaleProm;

    /**
     * 当前库存
     */
    private BigDecimal fInventoryQty;

    /**
     * 销售单位
     */
    private String fSalunitId;

    /**
     * 销售数量
     */
    private BigDecimal fSalunitQty;

    /**
     * 销售基本数量
     */
    private BigDecimal fSalBaseQty;

    /**
     * 计价基本数量
     */
    private BigDecimal fPriceBaseQty;

    /**
     * 质量类型
     */
    private String fQualifyType;

    /**
     * 销售基本分子
     */
    private BigDecimal fSalBaseNum;

    /**
     * 库存基本分母
     */
    private BigDecimal fStockBaseDen;

    /**
     * 关联退货数量（库存基本）
     */
    private BigDecimal fStockBaseReturnQty;

    /**
     * 累计退货数量（库存基本）
     */
    private BigDecimal fStockBaseSumretStockQty;

    /**
     * 关联应收数量（库存基本）
     */
    private BigDecimal fStockBaseArjoinQty;

    /**
     * 携带的主业务单位
     */
    private String fSrcBizunitId;

    /**
     * 是否生成产品档案
     */
    private Boolean fIscreateProdoc;

    /**
     * 明细货主供应商
     */
    private String fEownerSupplierId;

    /**
     * 组织间结算跨法人标识
     */
    private Boolean fIsoverLegalOrg;

    /**
     * 明细结算组织客户
     */
    private String fEsettleCustomerId;

    /**
     * 关联应收数量（销售基本）
     */
    private BigDecimal fSalBaseArjoinQty;

    /**
     * 关联入库数量（采购基本）
     */
    private BigDecimal fPurBaseJoininStockQty;

    /**
     * 行价目表
     */
    private String fPricelistEntry;

    /**
     * 未关联应收数量（计价单位）
     */
    private BigDecimal fArnotjoinQty;

    /**
     * 库存请检单EntryID
     */
    private String fQmentryId;

    /**
     * 库存状态转换单ENTRYID
     */
    private String fConvertEntryId;

    /**
     * B2C订单明细Id
     */
    private String fB2corderDetailId;

    /**
     * 销售订单EntryId
     */
    private String fSoentryId;

    /**
     * 预留锁库EntryId
     */
    private String fReserveEntryId;

    /**
     * 拆单数量（计价）
     */
    private BigDecimal fDispriceQty;

    /**
     * 拆单前原计价数量
     */
    private BigDecimal fBeforeDispriceQty;

    /**
     * 产品类型
     */
    private String fRowType;

    /**
     * 父项产品
     */
    private String fParentMatId;

    /**
     * 行标识
     */
    private String fRowId;

    /**
     * 父行标识
     */
    private String FParentRowId;

    /**
     * 签收数量
     */
    private BigDecimal fSignQty;

    /**
     * 管易订单单号
     */
    private String fThirdEntryId;

    /**
     * 发货检验
     */
    private Boolean fCheckDelivery;

    /**
     * 第三方单据ID
     */
    private String fEthirdBillId;

    /**
     * 第三方单据编号
     */
    private String fEthirdBillNo;

    /**
     * 管易是否到账
     */
    private Boolean fQyfinStatus;

    /**
     * 管易到账时间
     */
    private LocalDateTime fQyfinDate;

    /**
     * 单价折扣
     */
    private BigDecimal fPriceDiscount;

    /**
     * 尾差处理标识
     */
    private Boolean fTaildiffFlag;

    /**
     * 冲销数量(计价基本)
     */
    private BigDecimal fWriteOffpriceBaseQty;

    /**
     * 冲销数量(销售基本)
     */
    private BigDecimal fWriteOffsaleBaseQty;

    /**
     * 冲销数量(库存基本)
     */
    private BigDecimal fWriteoffStockBaseQty;

    /**
     * 冲销金额
     */
    private BigDecimal fWriteoffAmount;

    /**
     * 按子项结算
     */
    private Boolean settleByson;

    /**
     * BOM分录内码
     */
    private String fBomEntryId;

    /**
     * 价税合计（折前）
     */
    private BigDecimal fAllamountExceptDiscount;

    /**
     * 批号拣货结果标识
     */
    private Boolean fLotpickFlag;

    /**
     * 管易发货时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fQyenterTime;

    /**
     * 物料（销售组织）
     */
    private String fMaterialidSal;

    /**
     * 入库单编码_序号
     */
    private String fInStockBillNo;

    /**
     * 入库单分录ID
     */
    private String fInStockEntryId;

    /**
     * VMI业务
     */
    private Boolean fVmiBusinessStatus;

    /**
     * 收料单编码_序号
     */
    private String fReceiveBillNo;

    /**
     * 收料单分录ID
     */
    private String fReceiveEntryId;

    /**
     * 是否替代出库
     */
    private Boolean fIsrePlaceOut;

    /**
     * 替代原物料编码
     */
    private String fReplaceMaterialId;

    /**
     * 行关联应收状态
     */
    private Boolean fRowarStatus;

    /**
     * 长
     */
    private BigDecimal fC;

    /**
     * 宽
     */
    private BigDecimal fK;

    /**
     * 高
     */
    private BigDecimal fG;

    /**
     * 体积
     */
    private BigDecimal fTj;

    /**
     * 毛重
     */
    private BigDecimal fMz;

    /**
     * 净重
     */
    private BigDecimal fJz;

    /**
     * 毛总重
     */
    private BigDecimal fMzz;

    /**
     * 箱数
     */
    private BigDecimal fXs;

    /**
     * 装箱数
     */
    private BigDecimal fZxs;

    /**
     * 供应商
     */
    private String fGys;

    /**
     * 采购合同号
     */
    private String fCghth;

    /**
     * 采购单价
     */
    private BigDecimal fCgdj;

    /**
     * 采购总价
     */
    private BigDecimal fCgzj;

    /**
     * 成本价
     */
    private BigDecimal fJdj;

    /**
     * 成本总价
     */
    private BigDecimal fJdzj;

    /**
     * 发票单价
     */
    private BigDecimal fFpdj;

    /**
     * 发票总价
     */
    private BigDecimal fFpzj;

    /**
     * 中文报关品名
     */
    private String fZwbgpm;

    /**
     * 英文报关品名
     */
    private String fYwbgpm;

    /**
     * HS编码
     */
    private String fHsbm;

    /**
     * 净总重
     */
    private BigDecimal fJzz;

    /**
     * 报关数量
     */
    private BigDecimal fBgsl;

    /**
     * 报关状态
     */
    private String fBgzt;

    /**
     * 客户货号
     */
    private String fKhhh;

    /**
     * 是否报关
     */
    private Boolean fSfbg;

    /**
     * 货运结算数量
     */
    private BigDecimal fHyjssl;

    /**
     * 订单含税单价
     */
    private BigDecimal fDdhsdj;

    /**
     * 退税率%
     */
    private BigDecimal fTsl;

    /**
     * 单价（本位币）
     */
    private BigDecimal fDjbwb;

    /**
     * 含税单价（本位币）
     */
    private BigDecimal fHsdjbwb;

    /**
     * 提成比例
     */
    private BigDecimal fTcblnew;

    /**
     * 包材费用
     */
    private BigDecimal fBcfynew;

    /**
     * 订单审核日期
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fDdshrq;

    /**
     * 出库周期
     */
    private Integer fCkzq;

    /**
     * 制单日期
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fZdrq;

    /**
     * 包材工厂提供
     */
    private String fBcgctg;

    /**
     * 包材有库存
     */
    private Boolean fBcykc;

    /**
     * 样品单
     */
    private Boolean fYpd;

    /**
     * 物料创建日期
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fCtyBaseProperty1;

    /**
     * 是否报关-扫码携带专用
     */
    private Boolean fPeuuTextApv;

    /**
     * 新老产品
     */
    private String fPeuuBaseProperty83g;
}
