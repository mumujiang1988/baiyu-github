package com.ruoyi.business.k3.domain.bo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.business.k3.domain.entity.FFpurPatentry;
import com.ruoyi.common.mybatis.core.domain.BaseEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = FFpurPatentry.class, reverseConvertGenerate = false)
public class FFpurPatentryBo extends BaseEntity {

    @TableId(value = "id")
    private Long id;

    private Long fEntryId;

    private String fNumber;

    private Long fMaterialId;

    private String FmaterialName;

    private String fuom01;

    private Integer fToQty;

    private BigDecimal fBeforePrice;

    private BigDecimal fAfterPrice;

    private Long fAdjustRange;

    private BigDecimal fUpPrice;

    private BigDecimal fDownPrice;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fEffectiveDate;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fExpiryDate;

    private Long fPriceListId;

    private Long fSupplierId;

    private String fCurrencyId;

    private String fAuxpropId;

    private String fmaterialtYpeId;

    private String fPatidEntity;

    private String fIsPriceListPush;

    private String fUnitId;

    private String fPriceListObject;

    private String fAdjustType;

    private String fBeforeTaxRate;

    private String fAfterTaxRate;

    private BigDecimal fBeforeTaxPrice;

    private BigDecimal fAfterTaxPrice;

    private BigDecimal fIsIncludedTax;

    private Integer fProcessOrgId;

    private Integer FPROCESSID;

    private Integer fSrcEntryId;

    private String fNote;

    private String fBeforePriceCoefficient;

    private String fAfterPriceCoefficient;

    private Integer FFROMQTY;

    private String fDefBaseDataO;

    private String fDefBaseDataT;

    private String fDefAssistantO;

    private String fDefAssistantT;

    private String fDefTextO;

    private String fDefTextT;

    private String fDefaultPriceO;

    private String fDefaultPriceT;

    private String fIsPriceExcludeTax;

    private String fMaterialGroupId;

    private String fmaterialGroupName;

    private String fHscj;

}
