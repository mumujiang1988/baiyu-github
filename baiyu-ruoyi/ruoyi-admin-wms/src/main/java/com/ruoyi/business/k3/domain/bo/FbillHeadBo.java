package com.ruoyi.business.k3.domain.bo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.business.k3.domain.entity.FbillHead;
import com.ruoyi.common.mybatis.core.domain.BaseEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = FbillHead.class, reverseConvertGenerate = false)
public class FbillHeadBo extends BaseEntity {

    @TableId(value = "id")
    private Long id;

    private Long fId;

    private String fBillNo;

    private String fDocumentStatus;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fDate;

    private Long fPurchaseOrgId;

    private String fName;

    private String fDescription;

    private String fPaReason;

    private Long fModifierId;

    //供应商
    private String fSupplierId;

    // 物料名称
    private String FmaterialName;

    //物料编码
    private String fMaterialId;

    //创建人
    private String fCreatorId;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fCreateDate;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fModifyDate;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fApproveDate;

    private Long fEffectiveUserId;



}
