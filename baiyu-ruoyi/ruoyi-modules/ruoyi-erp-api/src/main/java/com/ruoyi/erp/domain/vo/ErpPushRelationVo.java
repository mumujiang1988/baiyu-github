package com.ruoyi.erp.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.ruoyi.common.excel.annotation.ExcelDictFormat;
import com.ruoyi.common.excel.convert.ExcelDictConvert;
import com.ruoyi.erp.domain.entity.ErpPushRelation;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ERP Push Relation Configuration View Object erp_push_relation
 *
 * @author JMH
 * @date 2026-03-22
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = ErpPushRelation.class)
public class ErpPushRelationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Relation ID
     */
    @ExcelProperty(value = "Relation ID")
    private Long relationId;

    /**
     * Source module code
     */
    @ExcelProperty(value = "Source Module Code")
    private String sourceModule;

    /**
     * Target module code
     */
    @ExcelProperty(value = "Target Module Code")
    private String targetModule;

    /**
     * Relation name
     */
    @ExcelProperty(value = "Relation Name")
    private String relationName;

    /**
     * Field mapping rules (JSON)
     */
    @ExcelProperty(value = "Field Mapping Rules")
    private String mappingRules;

    /**
     * Data transformation rules (JSON)
     */
    @ExcelProperty(value = "Data Transformation Rules")
    private String transformationRules;

    /**
     * Data validation rules (JSON)
     */
    @ExcelProperty(value = "Data Validation Rules")
    private String validationRules;

    /**
     * Concurrency control strategy
     */
    @ExcelProperty(value = "Concurrency Control Strategy")
    private String concurrencyControl;

    /**
     * Is transaction enabled
     */
    @ExcelProperty(value = "Transaction Enabled", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_yes_no")
    private String transactionEnabled;

    /**
     * Status
     */
    @ExcelProperty(value = "Status", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_normal_disable")
    private String status;

    /**
     * Version number
     */
    @ExcelProperty(value = "Version Number")
    private Integer version;

    /**
     * Create time
     */
    @ExcelProperty(value = "Create Time")
    private LocalDateTime createTime;

    /**
     * Update time
     */
    @ExcelProperty(value = "Update Time")
    private LocalDateTime updateTime;

}
