package com.ruoyi.erp.domain.bo;

import com.ruoyi.common.mybatis.core.domain.BaseEntity;
import com.ruoyi.erp.domain.entity.ErpPushRelation;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ERP Push Relation Configuration Business Object erp_push_relation
 *
 * @author JMH
 * @date 2026-03-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ErpPushRelation.class, reverseConvertGenerate = false)
public class ErpPushRelationBo extends BaseEntity {

    /**
     * Relation ID
     */
    private Long relationId;

    /**
     * Source module code
     */
    @NotBlank(message = "Source module code cannot be empty")
    @Size(min = 0, max = 50, message = "Source module code length cannot exceed {max} characters")
    private String sourceModule;

    /**
     * Target module code
     */
    @NotBlank(message = "Target module code cannot be empty")
    @Size(min = 0, max = 50, message = "Target module code length cannot exceed {max} characters")
    private String targetModule;

    /**
     * Relation name
     */
    @NotBlank(message = "Relation name cannot be empty")
    @Size(min = 0, max = 100, message = "Relation name length cannot exceed {max} characters")
    private String relationName;

    /**
     * Field mapping rules (JSON)
     */
    private String mappingRules;

    /**
     * Data transformation rules (JSON)
     */
    private String transformationRules;

    /**
     * Data validation rules (JSON)
     */
    private String validationRules;

    /**
     * Concurrency control strategy
     */
    private String concurrencyControl;

    /**
     * Is transaction enabled
     */
    private String transactionEnabled;

    /**
     * Status
     */
    private String status;

}
