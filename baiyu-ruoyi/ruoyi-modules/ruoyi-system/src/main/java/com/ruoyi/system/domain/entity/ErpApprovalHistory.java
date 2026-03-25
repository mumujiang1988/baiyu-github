package com.ruoyi.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ERP 审批历史记录表实体类
 * 
 * @author ERP Development Team
 * @date 2026-03-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("erp_approval_history")
public class ErpApprovalHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 历史记录 ID
     */
    @TableId(value = "history_id", type = IdType.AUTO)
    private Long historyId;

    /**
     * 单据 ID
     */
    @TableField("bill_id")
    private Long billId;

    /**
     * 模块编码
     */
    @TableField("module_code")
    private String moduleCode;

    /**
     * 流程 ID
     */
    @TableField("flow_id")
    private Long flowId;

    /**
     * 当前步骤
     */
    @TableField("current_step")
    private Integer currentStep;

    /**
     * 审批人 ID
     */
    @TableField("approver_id")
    private String approverId;

    /**
     * 审批动作 (AUDIT/REJECT/TRANSFER)
     */
    @TableField("approval_action")
    private String approvalAction;

    /**
     * 审批意见
     */
    @TableField("approval_opinion")
    private String approvalOpinion;

    /**
     * 审批时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "approval_time", fill = FieldFill.INSERT)
    private LocalDateTime approvalTime;
}
