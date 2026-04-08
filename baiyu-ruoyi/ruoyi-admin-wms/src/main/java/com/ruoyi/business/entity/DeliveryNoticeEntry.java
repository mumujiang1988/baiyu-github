package com.ruoyi.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 金蝶发货通知单 - 明细表
 */
@Data
@TableName("delivery_notice_entry")
public class DeliveryNoticeEntry {

    /** 主键 ID，自增 */
    @TableField("id")
    private Long id;

    /** 关联主表 ID */
    @TableField("delivery_notice_id")
    private String deliveryNoticeId;

    /** 关联主表单据编号 */
    @TableField("delivery_notice_no")
    private String deliveryNoticeNo;

    /** 金蝶主表 FID */
    @TableField("FID")
    private Long FID;


    // ================= 物料基本信息 =================

    /** 物料编码 (必填项) */
    @TableField("FMaterialID")
    private String FMaterialID;

    /** 物料名称 */
    @TableField("FMaterialName")
    private String FMaterialName;

    /** 规格型号 */
    @TableField("FMateriaModel")
    private String FMateriaModel;

    /** 物料类别 */
    @TableField("FMateriaType")
    private String FMateriaType;

    /** 销售单位 (必填项) */
    @TableField("FUnitID")
    private String FUnitID;

    /** 销售数量 */
    @TableField("FQty")
    private BigDecimal FQty;

    /** 出货仓库 */
    @TableField("FStockID")
    private String FStockID;

    /** 理货情况 */
    @TableField("FNoteEntry")
    private String FNoteEntry;

    /** 基本单位 (必填项) */
    @TableField("FBaseUnitID")
    private String FBaseUnitID;

    /** 销售基本数量 */
    @TableField("FBaseUnitQty")
    private BigDecimal FBaseUnitQty;

    /** 交货地点 */
    @TableField("FDeliveryLoc")
    private String FDeliveryLoc;

    /** 交货地址 */
    @TableField("FDeliveryLAddress")
    private String FDeliveryLAddress;

    /** 订单单号 */
    @TableField("FOrderNo")
    private String FOrderNo;

    /** 订单行号 */
    @TableField("FOrderSeq")
    private String FOrderSeq;

    /** 客户物料编码 */
    @TableField("FCustMatID")
    private String FCustMatID;


    /** 批号 */
    @TableField("FLot")
    private String FLot;

    /** 是否赠品 */
    @TableField("FIsFree")
    private Integer FIsFree;

    /** 客户物料名称 */
    @TableField("FCustMatName")
    private String FCustMatName;

    /** 未出库数量 */
    @TableField("FRemainOutQty")
    private BigDecimal FRemainOutQty;

    /** 库存状态 */
    @TableField("FStockStatusId")
    private String FStockStatusId;

    /** 计价数量 */
    @TableField("FPriceUnitQty")
    private BigDecimal FPriceUnitQty;

    /** 单价 */
    @TableField("FPrice")
    private BigDecimal FPrice;

    /** 含税单价 */
    @TableField("FTaxPrice")
    private BigDecimal FTaxPrice;

    /** 税组合 */
    @TableField("FTaxCombination")
    private String FTaxCombination;

    /** 税率% */
    @TableField("FEntryTaxRate")
    private BigDecimal FEntryTaxRate;

    /** 价格系数 */
    @TableField("FPriceCoefficient")
    private BigDecimal FPriceCoefficient;

    /** 系统定价 */
    @TableField("FSysPrice")
    private BigDecimal FSysPrice;

    /** 最低限价 */
    @TableField("FLimitDownPrice")
    private BigDecimal FLimitDownPrice;

    /** 折前金额 */
    @TableField("FBefDisAmt")
    private BigDecimal FBefDisAmt;

    /** 折前价税合计 */
    @TableField("FBefDisAllAmt")
    private BigDecimal FBefDisAllAmt;

    /** 折扣率% */
    @TableField("FDiscountRate")
    private BigDecimal FDiscountRate;


    /** 采购价 */
    @TableField("F_ora_Decimal")
    private BigDecimal F_ora_Decimal;

    /** 成本价 */
    @TableField("F_ora_Decimal1")
    private BigDecimal F_ora_Decimal1;

    /** 长 */
    @TableField("F_c")
    private BigDecimal F_c;

    /** 宽 */
    @TableField("F_k")
    private BigDecimal F_k;

    /** 高 */
    @TableField("F_g")
    private BigDecimal F_g;

    /** 体积 */
    @TableField("F_tj")
    private BigDecimal F_tj;

    /** 毛重 */
    @TableField("F_mz")
    private BigDecimal F_mz;

    /** 净重 */
    @TableField("F_jz")
    private BigDecimal F_jz;

    /** 毛总重 */
    @TableField("F_mzz")
    private BigDecimal F_mzz;

    /** 箱数 */
    @TableField("F_xs")
    private BigDecimal F_xs;

    /** 装箱数 */
    @TableField("F_zxs")
    private BigDecimal F_zxs;

    /** 采购总价 */
    @TableField("F_cgzj")
    private BigDecimal F_cgzj;

    /** 成本总价 */
    @TableField("F_zjzj")
    private BigDecimal F_zjzj;

    /** 开票单价 */
    @TableField("F_kpdj")
    private BigDecimal F_kpdj;

    /** 开票总价 */
    @TableField("F_kpzj")
    private BigDecimal F_kpzj;

    /** 中文报关品名 */
    @TableField("F_zwbgpm")
    private String F_zwbgpm;

    /** 英文报关品名 */
    @TableField("F_ywbgpm")
    private String F_ywbgpm;

    /** 净总重 */
    @TableField("F_jzz")
    private BigDecimal F_jzz;

    /** 进仓时间 */
    @TableField("F_jcsj")
    private Date F_jcsj;

    /** 客户货号 */
    @TableField("F_khhh")
    private String F_khhh;

    /** 是否报关 (必填项) */
    @TableField("F_sfbg")
    private Integer F_sfbg;

    /** 订单含税单价 */
    @TableField("F_ddhsdj")
    private BigDecimal F_ddhsdj;

    /** 退税率% */
    @TableField("F_tsl")
    private BigDecimal F_tsl;

    /** 报关单位 */
    @TableField("F_bgdw")
    private String F_bgdw;

    /** 提成比例 */
    @TableField("F_tcblNEW")
    private BigDecimal F_tcblNEW;

    /** 包材费用 */
    @TableField("F_BCFYNEW")
    private BigDecimal F_BCFYNEW;

    /** 关联包材费用 */
    @TableField("F_GLBCFY")
    private BigDecimal F_GLBCFY;

    /** 发货要求 */
    @TableField("F_ffyq")
    private String F_ffyq;

    /** 订单审核日期 */
    @TableField("Fddshrq")
    private Date Fddshrq;

    /** 销售订单号 */
    @TableField("Fxsddh")
    private String Fxsddh;

    /** 销售订单行号 */
    @TableField("Fxsddhh")
    private String Fxsddhh;

    /** 包装工厂提供 */
    @TableField("Fbzgctg")
    private String Fbzgctg;

    /** 样品单 */
    @TableField("Fypd")
    private Integer Fypd;

    /** 包材有库存 */
    @TableField("Fykc")
    private Integer Fykc;

    /** 产品图片 */
    @TableField("F_cty_BaseProperty")
    private String F_cty_BaseProperty;

    /** 入库审核日期 */
    @TableField("Frkshrq")
    private Date Frkshrq;

    /** 出库审核日期 */
    @TableField("Fckshrq")
    private Date Fckshrq;

    /** 包装方式 */
    @TableField("F_BZFS")
    private String F_BZFS;

    /** 创建人 (系统字段) */
    @TableField("created_by")
    private String createdBy;

    /** 创建时间 (系统字段) */
    @TableField("created_at")
    private Date createdAt;

    /** 更新时间 (系统字段) */
    @TableField("updated_at")
    private Date updatedAt;
}
