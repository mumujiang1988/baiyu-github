package com.ruoyi.business.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SalesPriceItem {

    /** 主键ID */
    private Long id;

    /** 价目主表ID */
    private Long priceId;

    /** 物料编码(FMaterialId) */
    private String fMaterialId;


    /** 物料名称(FMaterialName) */
    private String fMaterialName;

    /** 规格型号(FMaterialModel) */
    private String fMaterialModel;

    /** 客户货号(F_khhh) */
    private String fKhhh;

    /** 价格(FPrice) */
    private BigDecimal fPrice;

    /** 包装要求(F_bzyq) */
    private String fBzyq;

    /** 售后调整(F_BZMX) */
    private String fBzmx;

    /** 英文品名(F_ywpm) */
    private String fYwpm;

    /** 装箱资料修改日期(Fzxzlxgrq) */
    private LocalDate fzxzlxgrq;

    /** 长(F_c) */
    private BigDecimal fC;

    /** 宽(F_k) */
    private BigDecimal fK;

    /** 高(F_g) */
    private BigDecimal fG;

    /** 毛重(F_mz) */
    private BigDecimal fMz;

    /** 净重(F_jz) */
    private BigDecimal fJz;

    /** 变更日期(Fbgrq) */
    private LocalDate fbgrq;

    /** 英文包装方式(F_ywbzfs) */
    private String fYwbzfs;

    /** 产品图片2(F_cptp2) */
    private String fCptp2;

    /** 产品图片3(F_cptp3) */
    private String fCptp3;

    /** 激光打字图片(F_jgdztp) */
    private String fJgdztp;

    /** 外箱条码(F_wxtm) */
    private String fWxtm;

    /** 外箱正唛(F_wxzm) */
    private String fWxzm;

    /** 外箱侧唛(F_wxcm) */
    private String fWxcm;

    /** 内箱正唛(F_nxzm) */
    private String fNxzm;

    /** 内箱侧唛(F_nxcm) */
    private String fNxcm;

    /** 彩贴图(F_CTT) */
    private String fCtt;

    /** 说明书图(F_SMST) */
    private String fSmst;

    /** 彩套图(F_CTAOT) */
    private String fCtaot;

    /** 彩盒图(F_CHT) */
    private String fCht;

    /** 单款称重照(F_dkczz) */
    private String fDkczz;

    /** 整箱称重照(F_zxczz) */
    private String fZxczz;

    /** 条码(Ftm) */
    private String ftm;

    /** 包装图(F_bzt) */
    private String fBzt;

    /** 供应商(F_gys) */
    private String fGys;

    /**包装品类（多选）*/
    private String packagingCategories;

}
