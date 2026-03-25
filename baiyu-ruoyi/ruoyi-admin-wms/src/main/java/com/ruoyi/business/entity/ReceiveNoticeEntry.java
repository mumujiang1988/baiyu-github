package com.ruoyi.business.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 收料通知单明细表（receive_notice_entry）
 * 对应金蝶 K3 Cloud 收料通知单明细表
 */
@Data
@TableName("receive_notice_entry")
public class ReceiveNoticeEntry {

    /** 明细主键 */
    @TableId("ID")
    private Long id;

    /**金蝶主键id*/
    @TableField("FID")
    private String fid;
    /** 单据编号（关联主表） */
    @TableField("FBillNo")
    private String fBillNo;

    /** 包装补做 */
    @TableField("F_bzbz")
    private String fbzbz;

    /** 红线交期 */
    @TableField("F_hxjq")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fhxjq;

    /** 产品类别 */
    @TableField("F_cplb")
    private String fcplb;

    /** 物料编码 */
    @TableField("FMaterialId")
    private String fMaterialId;

    /** 物料名称 */
    @TableField("FMaterialName")
    private String fMaterialName;

    /** 交货数量 */
    @TableField("FActReceiveQty")
    private BigDecimal fActReceiveQty;

    /** 预计到货日期 */
    @TableField("FPreDeliveryDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fPreDeliveryDate;

    /** 计价单位 */
    @TableField("FPriceUnitId")
    private String fPriceUnitId;

    /** 计价数量 */
    @TableField("FPriceUnitQty")
    private BigDecimal fPriceUnitQty;

    /** 库存状态 */
    @TableField("FStockStatusId")
    private String fStockStatusId;

    /** 批号 */
    @TableField("FLot")
    private String fLot;

    /** 拒收数量 */
    @TableField("FRejectQty")
    private BigDecimal fRejectQty;

    /** 拒收原因 */
    @TableField("FRejectReason")
    private String fRejectReason;

    /** 是否赠品(0否1是) */
    @TableField("FGiveAway")
    private Integer fGiveAway;

    /** 含税单价 */
    @TableField("FTaxPrice")
    private BigDecimal fTaxPrice;

    /** 库存单位 */
    @TableField("FStockUnitID")
    private String fStockUnitID;

    /** 库存单位数量 */
    @TableField("FStockQty")
    private BigDecimal fStockQty;

    /** 单箱长 */
    @TableField("F_WBZC")
    private BigDecimal fWBZC;

    /** 单箱宽 */
    @TableField("F_WBZK")
    private BigDecimal fWBZK;

    /** 单箱高 */
    @TableField("F_WBZG")
    private BigDecimal fWBZG;

    /** 单箱体积(m³) */
    @TableField("F_WBZTJ")
    private BigDecimal fWBZTJ;

    /** 单箱毛重(KGS) */
    @TableField("F_MZ")
    private BigDecimal fmz;

    /** 单箱净重(KGS) */
    @TableField("F_JZ")
    private BigDecimal fjz;

    /** 零箱毛重(KGS) */
    @TableField("F_LXZ")
    private BigDecimal flxz;

    /** 销售订单 */
    @TableField("F_xsdd")
    private String fxsdd;

    /** 零箱数 */
    @TableField("F_lxs")
    private Integer flxs;

    /** 总体积 */
    @TableField("F_ztj")
    private BigDecimal fztj;

    /** 毛总重 */
    @TableField("F_mzz")
    private BigDecimal fmzz;

    /** 是否对账中 */
    @TableField("FIsReconciliationing")
    private Integer fIsReconciliationing;

    /** 不良品折让金额 */
    @TableField("FRejectsDiscountAmount")
    private BigDecimal fRejectsDiscountAmount;

    /** 入库关联不良品折让金额 */
    @TableField("FJoinRejectsDiscountAmount")
    private BigDecimal fJoinRejectsDiscountAmount;

    /** 当前对账单号 */
    @TableField("FReconciliationBillNo")
    private String fReconciliationBillNo;

    /** 历史对账单号 */
    @TableField("FAllReconciliationBillNo")
    private String fAllReconciliationBillNo;

    /** 客户货号 */
    @TableField("F_KHHH")
    private String fKHHH;

    /** 客户简称 */
    @TableField("F_khjc")
    private String fkhjc;

    /** 客户名称 */
    @TableField("F_kh1")
    private String fkh1;

    /** 销售员 */
    @TableField("F_XSY")
    private String fxsy;

    /** 跟单员 */
    @TableField("F_gdy1")
    private String fgdy1;

    /** 包装要求 */
    @TableField("F_bzyq")
    private String fbzyq;

    /** 特殊要求 */
    @TableField("F_tsyq")
    private String ftsyq;

    /** 彩贴图 */
    @TableField("F_CTt")
    private String fCTt;

    /** 激光打字 */
    @TableField("F_jgdz")
    private String fjgdz;

    /** 包材接收地 */
    @TableField("F_bcjsd")
    private String fbcjsd;

    /** 包材接收人 */
    @TableField("F_bcjsr")
    private String fbcjsr;

    /** 包材接收人电话 */
    @TableField("F_bcjsrdh")
    private String fbcjsrdh;

    /** 参考价 */
    @TableField("F_Ckj")
    private BigDecimal fCkj;

    /** 价格调整原因 */
    @TableField("F_jgtzyy")
    private String fjgtzyy;

    /** 订单审核日期 */
    @TableField("Fddshrq")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fddshrq;

    /** 销售订单号 */
    @TableField("Fxsddh")
    private String fxsddh;

    /** 销售订单行号 */
    @TableField("Fxsddhh")
    private String fxsddhh;

    /** 采购订单号 */
    @TableField("Fcgddh")
    private String fcgddh;

    /** 销售部门 */
    @TableField("Fxsbm")
    private String fxsbm;

    /** 入库审核日期 */
    @TableField("Frkshrq")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime frkshrq;

    /** 质量标准及技术要求 */
    @TableField("Fzlbzjjsyq")
    private String fzlbzjjsyq;

    /** 包装标准及要求 */
    @TableField("Fbzbzjyq")
    private String fbzbzjyq;

    /** 客户英文描述 */
    @TableField("Fkhywms")
    private String fkhywms;

    /** 验货报告 */
    @TableField("F_PEUU_Attachment_ap")
    private String fPEUUAttachmentap;

}
