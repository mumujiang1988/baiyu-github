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
@EqualsAndHashCode(callSuper = true)
@TableName("f_fpur_patentry")
public class FFpurPatentry extends BaseEntity {

    @TableId(value = "id")
    private Long id;

    //实体主键
    private String fEntryId;

    //采购调价主表编码
    private String fNumber;

    //物料编码
    private String fMaterialId;

    // 物料名称
    private String FmaterialName;

    // 规格型号
    private String fuom01;

    //至
    private String fToQty;

    //调前单价
    private BigDecimal fBeforePrice;

    // 调后单价
    private BigDecimal fAfterPrice;

    //调价幅度%
    private BigDecimal fAdjustRange;

    //价格上限
    private BigDecimal fUpPrice;

    //价格下限
    private BigDecimal fDownPrice;

    //生效日期
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fEffectiveDate;

    //失效日期
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fExpiryDate;

    // 价目表
    private String fPriceListId;

    //供应商
    private String fSupplierId;

    //币别
    private String fCurrencyId;

    //辅助属性
    private String fAuxpropId;

    // 物料类别
    private String fmaterialtYpeId;

    //调价唯一性标示
    private String fPatidEntity;

    //价目表下推
    private String fIsPriceListPush;

    //计价单位
    private String fUnitId;

    //调价对象
    private String fPriceListObject;

    //调价类型
    private String fAdjustType;

    // 调前税率
    private String fBeforeTaxRate;

    //调后税率
    private BigDecimal fAfterTaxRate;

    // 调前含税单价
    private BigDecimal fBeforeTaxPrice;

    //调后含税单价
    private BigDecimal fAfterTaxPrice;

    //含税
    private String fIsIncludedTax;

    //需求组织
    private String fProcessOrgId;

    //作业
    private String FprocessId;

    //价目表分录内码
    private String fSrcEntryId;

    // 备注
    private String fNote;

    // 调前价格系数
    private String fBeforePriceCoefficient;

    //调后价格系数
    private String fAfterPriceCoefficient;

    //从
    private String Ffromqty;

    //自定义基础资料1
    private String fDefBaseDataO;

    //自定义基础资料2
    private String fDefBaseDataT;

    //自定义辅助资料1
    private String fDefAssistantO;

    //自定义辅助资料2
    private String fDefAssistantT;

    // 自定义文本1
    private String fDefTextO;

    //自定义文本2
    private String fDefTextT;

    //自定义价格1
    private BigDecimal fDefaultPriceO;

    //自定义价格2
    private BigDecimal fDefaultPriceT;

    //价外税
    private String fIsPriceExcludeTax;

    //物料分组编码
    private String fMaterialGroupId;

    //物料分组名称
    private String fmaterialGroupName;

    //含税差价
    private BigDecimal fHscj;

}
