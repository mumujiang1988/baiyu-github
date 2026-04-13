package com.ruoyi.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ERP Approval History Table Entity
 * 
 * @author JMH
 * @date 2026-03-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("erp_approval_history")
public class ErpApprovalHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * History record ID
     */
    @TableId(value = "history_id", type = IdType.AUTO)
    private Long historyId;

    /**
     * Bill ID
     */
    @TableField("bill_id")
    private Long billId;

    /**
     * Module code
     */
    @TableField("module_code")
    private String moduleCode;

    /**
     * Flow ID
     */
    @TableField("flow_id")
    private Long flowId;

    /**
     * Current step
     */
    @TableField("current_step")
    private Integer currentStep;

    /**
     * Approver ID
     */
    @TableField("approver_id")
    private String approverId;

    /**
     * Approval action (AUDIT/REJECT/TRANSFER)
     */
    @TableField("approval_action")
    private String approvalAction;

    /**
     * Approval opinion
     */
    @TableField("approval_opinion")
    private String approvalOpinion;

    /**
     * Approval time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "approval_time", fill = FieldFill.INSERT)
    private LocalDateTime approvalTime;
}
