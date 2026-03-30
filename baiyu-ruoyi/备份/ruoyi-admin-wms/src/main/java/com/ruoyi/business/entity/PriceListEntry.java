package com.ruoyi.business.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 金蝶采购价目表 - 明细表（Entry）
 * 多行明细对应一个主表 PriceList
 */
@Data
@TableName("price_list_entry")
public class PriceListEntry {

    /** 主键，自增 */

    private Long id;

    /** 外键：关联主表 price_list.id → PriceList.id */
    private String priceListNumber;

    /** 供应商物料编码 → F_GYSWLBM */
    private String FMaterialId;

    /** 供应商物料名称 → F_GYSWLMC */
    private String FMaterialName;

    private String specification;    // 规格型号
    private String description1;     // 原英文描述
    /**英文品名*/
    private String FctyBaseProperty;
    /**工厂货号 FGCHH*/
    private String FGCHH;
    /**工厂图片*/
    private String FTP1;

    /** 单价（必填） → FPrice */
    private Double FPrice;
    /**规格说明*/
    private String FGGSM;
    /**备注*/
    private String FNote;

    /** 含税单价（必填） → FTaxPrice */
    private Double FTaxPrice;

    /** 计价单位（必填） → FUnitID */
    private String FUnitID;

    /** 税率% → FTaxRate */
    private Double FTaxRate;

    /** 50套内5% → F100t5 */
    private Double F100t5;

    /** 价格系数 → FPriceCoefficient */
    private Double FPriceCoefficient;

    /** 价格上限 → FUpPrice */
    private Double FUpPrice;

    /** 价格下限 → FDownPrice */
    private Double FDownPrice;

    /** 生效日期（必填） → FEntryEffectiveDate */
    private String FEntryEffectiveDate;

    /** 价格有效期（必填） → FEntryExpiryDate */
    private String FEntryExpiryDate;

    /** 单箱长(cm) → F_WBZC */
    private Double FWBZC;

    /** 单箱宽(cm) → F_WBZK */
    private Double FWBZK;

    /** 单箱高(cm) → F_WBZG */
    private Double FWBZG;

    /** 单箱体积(m³) → F_WBZTJ */
    private Double FWBZTJ;

    /** 单箱数量 → F_WBZSL */
    private Double FWBZSL;

    /** 单箱单位 → F_WBZDW */
    private String FWBZDW;

    /** 单箱毛重(KG) → F_MZ */
    private Double FMZ;

    /** 单箱净重(KG) → F_JZ */
    private Double FJZ;

    /** 包装说明（必填） → F_bzsm */
    private String Fbzsm;

    /** 最近调价日期 → FRECENTDATE */
    private String FRECENTDATE;

    /** 常规塑盒颜色 → F_cgshys */
    private String Fcgshys;

    /** 定制塑盒颜色起订量 → F_dzshqdl */
    private String Fdzshqdl;

    /** 中性说明书 → F_ZXSMS */
    private String FZXSMS;

    /** 中性说明书图片（URL）→ F_ZXSMSTP */
    private String FZXSMSTP;

    /** 包装尺寸 → F_BCGG */
    private String FBCGG;

    /** 开票品名 → F_kppm */
    private String Fkppm;

    /** 询价人 → Fxjr */
    private String Fxjr;

    /** 是否用于业务询价 → Fsfyyywxj */
    private String Fsfyyywxj;

    /** 产品质量要求 → Fcpzlyq */
    private String Fcpzlyq;

    /** 起订量 → F_QDL */
    private Integer FQDL;
    /**创建人*/
    private String createdBy;

    /** 创建时间 */
    private String createdAt;

    /** 更新时间 */
    private Date updatedAt;
}
