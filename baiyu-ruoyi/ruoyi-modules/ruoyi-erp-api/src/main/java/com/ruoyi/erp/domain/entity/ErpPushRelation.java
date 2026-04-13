package com.ruoyi.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ERP Push Relation Configuration Table Entity
 * 
 * @author JMH
 * @date 2026-03-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("erp_push_relation")
public class ErpPushRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Relation ID
     */
    @TableId(value = "relation_id", type = IdType.AUTO)
    private Long relationId;

    /**
     * Source module code
     */
    @TableField("source_module")
    private String sourceModule;

    /**
     * Target module code
     */
    @TableField("target_module")
    private String targetModule;

    /**
     * Relation name
     */
    @TableField("relation_name")
    private String relationName;

    /**
     * Field mapping rules (JSON)
     */
    @TableField("mapping_rules")
    private String mappingRules;

    /**
     * Data transformation rules (JSON)
     */
    @TableField("transformation_rules")
    private String transformationRules;

    /**
     * Data validation rules (JSON)
     */
    @TableField("validation_rules")
    private String validationRules;

    /**
     * Concurrency control strategy (optimistic/pessimistic)
     */
    @TableField("concurrency_control")
    private String concurrencyControl;

    /**
     * Is transaction enabled
     */
    @TableField("transaction_enabled")
    private String transactionEnabled;

    /**
     * Status (1 enabled 0 disabled)
     */
    @TableField("status")
    private String status;

    /**
     * Version number (optimistic lock)
     */
    @Version
    @TableField("version")
    private Integer version;

    /**
     * Create time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * Update time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
