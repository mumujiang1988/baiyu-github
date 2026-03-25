package com.ruoyi.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ERP 审批流程配置表实体类
 * 
 * @author ERP Development Team
 * @date 2026-03-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("erp_approval_flow")
public class ErpApprovalFlow implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 流程 ID
     */
    @TableId(value = "flow_id", type = IdType.AUTO)
    private Long flowId;

    /**
     * 模块编码
     */
    @TableField("module_code")
    private String moduleCode;

    /**
     * 流程名称
     */
    @TableField("flow_name")
    private String flowName;

    /**
     * 流程定义 (JSON，包含节点、条件、角色等)
     */
    @TableField("flow_definition")
    private String flowDefinition;

    /**
     * 当前版本号
     */
    @TableField("current_version")
    private Integer currentVersion;

    /**
     * 是否激活
     */
    @TableField("is_active")
    private String isActive;

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
