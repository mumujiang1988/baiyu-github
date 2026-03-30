package com.ruoyi.business.entity;

import lombok.Data;

/**销售价目表包材明细表
 * */
@Data
public class SalesPriceItemPackage {

    /** 主键ID */
    private Long id;

    /** 价目主表编码 */
    private Long priceId;

    /** 包装编码(F_BZBM) */
    private String fBzbm;

    /** 包装名称(F_BZMC) */
    private String fBzmc;

    /** 包装规格(F_BZGG) */
    private String fBzgg;

    /** 单位(F_DW) */
    private String fDw;

    /** 用量(F_YL) */
    private Integer fYl;

    /** 备注(F_BZ) */
    private String fBz;

    /** 图片(F_TP) */
    private String fTp;

    /** 包材发往供应商(F_BCFWGYS) */
    private String fBcfwgys;

    /** 产品供应商(F_CPGYS) */
    private String fCpgys;

    /** 即时库存(F_cty_BaseProperty) */
    private Integer fCtyBaseProperty;
}
