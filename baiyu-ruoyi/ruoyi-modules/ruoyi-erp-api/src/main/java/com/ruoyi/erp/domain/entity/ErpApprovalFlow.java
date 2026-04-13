package com.ruoyi.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ERP Approval Flow Configuration Table Entity
 * 
 * @author JMH
 * @date 2026-03-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("erp_approval_flow")
public class ErpApprovalFlow implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Flow ID
     */
    @TableId(value = "flow_id", type = IdType.AUTO)
    private Long flowId;

    /**
     * Module code
     */
    @TableField("module_code")
    private String moduleCode;

    /**
     * Flow name
     */
    @TableField("flow_name")
    private String flowName;

    /**
     * Flow definition (JSON, including nodes, conditions, roles, etc.)
     */
    @TableField("flow_definition")
    private String flowDefinition;

    /**
     * Current version number
     */
    @TableField("current_version")
    private Integer currentVersion;

    /**
     * Is active
     */
    @TableField("is_active")
    private String isActive;

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
