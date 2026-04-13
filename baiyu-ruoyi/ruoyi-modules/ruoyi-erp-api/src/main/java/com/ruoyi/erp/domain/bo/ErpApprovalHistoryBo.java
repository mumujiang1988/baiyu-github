package com.ruoyi.erp.domain.bo;

import com.ruoyi.common.mybatis.core.domain.BaseEntity;
import com.ruoyi.erp.domain.entity.ErpApprovalHistory;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * ERP Approval History Business Object erp_approval_history
 *
 * @author JMH
 * @date 2026-03-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ErpApprovalHistory.class, reverseConvertGenerate = false)
public class ErpApprovalHistoryBo extends BaseEntity {

    /**
     * History record ID
     */
    private Long historyId;

    /**
     * Bill ID
     */
    @NotBlank(message = "单据 ID 不能为空")
    private Long billId;

    /**
     * Module code
     */
    @NotBlank(message = "模块编码不能为空")
    private String moduleCode;

    /**
     * Flow ID
     */
    private Long flowId;

    /**
     * Current step
     */
    private Integer currentStep;

    /**
     * Approver ID
     */
    private String approverId;

    /**
     * Approval action
     */
    @NotBlank(message = "审批动作不能为空")
    private String approvalAction;

    /**
     * Approval opinion
     */
    private String approvalOpinion;
    
    /**
     * Approval time
     */
    private LocalDateTime approvalTime;

}
