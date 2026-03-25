package com.ruoyi.business.dto;

import lombok.Data;

@Data
public class PriceListDTO {

    /** 供应商 → FSupplierName */
    private String FSupplierName;

    /** 供应商类别 → F_GYSLB */
    private String F_GYSLB;
    /** 币别（必填） → FCurrencyID */
    private String FCurrencyID;
    /** 单价（必填） → FPrice */
    private Double FPrice;
    /** 编码 → FNumber */
    private String FNumber;
    /** 含税单价（必填） → FTaxPrice */
    private Double FTaxPrice;
    /**工厂图片*/
    private String F_TP1;
    /** 价格上限 → FUpPrice */
    private Double FUpPrice;
    /** 定制塑盒颜色起订量 → F_dzshqdl */
    private String F_dzshqdl;
    /** 包装说明（必填） → F_bzsm */
    private String F_bzsm;

    /** 价格下限 → FDownPrice */
    private Double FDownPrice;

    /** 生效日期（必填） → FEntryEffectiveDate */
    private String FEntryEffectiveDate;

    /** 包装尺寸 → F_BCGG */
    private String F_BCGG;
    /** 价格有效期（必填） → FEntryExpiryDate */
    private String FEntryExpiryDate;
    /** 起订量 → F_QDL */
    private Integer F_QDL;
}
