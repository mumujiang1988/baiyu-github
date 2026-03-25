package com.ruoyi.business.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**销售价目表*/
@Data
public class SalesPrice {

    /** 主键ID */
    private Long id;

    /**金蝶主键id*/
    private Long fId;

    /** 名称(FName) */
    private String fName;

    /** 编号(FNumber) */
    private String fNumber;

    /** 创建人(FCreatorId) */
    private String fCreatorId;

    /** 备注(FDescription) */
    private String fDescription;

    /** 币别(FCurrencyId) */
    private String fCurrencyId;

    /** 生效日(FEffectiveDate) */
    private LocalDate fEffectiveDate;

    /** 失效日(FExpiryDate) */
    private LocalDate fExpiryDate;

    /** 价目对象(FPriceObject) */
    private String fPriceObject;

    /** 限定客户(FLimitCustomer) */
    private String fLimitCustomer;

    /** 限定销售员(FLimitSalesMan) */
    private String fLimitSalesMan;

    /** 价格类型(FPriceType) */
    private String fPriceType;

    /** 销售员(F_xsy) */
    private String fXsy;

    /** 客户简称(F_khjc) */
    private String fKhjc;

    /** 创建日期(FCreateDate) */
    private LocalDateTime fCreateDate;

    /** 最后修改日期(FModifyDate) */
    private LocalDateTime fModifyDate;

    /** 最后修改人(FModifierId) */
    private String fModifierId;


    /** 价目明细列表 */
    private List<SalesPriceItem> itemList;
    private List<SalesPriceItemPackage> itemPackageList;
}
