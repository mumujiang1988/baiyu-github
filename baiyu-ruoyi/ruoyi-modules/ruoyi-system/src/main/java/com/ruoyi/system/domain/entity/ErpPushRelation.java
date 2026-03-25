package com.ruoyi.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ERP 下推关系配置表实体类
 * 
 * @author ERP Development Team
 * @date 2026-03-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("erp_push_relation")
public class ErpPushRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关系 ID
     */
    @TableId(value = "relation_id", type = IdType.AUTO)
    private Long relationId;

    /**
     * 源模块编码
     */
    @TableField("source_module")
    private String sourceModule;

    /**
     * 目标模块编码
     */
    @TableField("target_module")
    private String targetModule;

    /**
     * 关系名称
     */
    @TableField("relation_name")
    private String relationName;

    /**
     * 字段映射规则 (JSON)
     */
    @TableField("mapping_rules")
    private String mappingRules;

    /**
     * 数据转换规则 (JSON)
     */
    @TableField("transformation_rules")
    private String transformationRules;

    /**
     * 数据校验规则 (JSON)
     */
    @TableField("validation_rules")
    private String validationRules;

    /**
     * 并发控制策略 (optimistic/pessimistic)
     */
    @TableField("concurrency_control")
    private String concurrencyControl;

    /**
     * 是否启用事务
     */
    @TableField("transaction_enabled")
    private String transactionEnabled;

    /**
     * 状态 (1 启用 0 停用)
     */
    @TableField("status")
    private String status;

    /**
     * 版本号 (乐观锁)
     */
    @Version
    @TableField("version")
    private Integer version;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
