package com.ruoyi.erp.domain.bo;

import com.ruoyi.common.mybatis.core.domain.BaseEntity;
import com.ruoyi.erp.domain.entity.ErpApprovalHistory;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ERP 审批历史记录业务对象 erp_approval_history
 *
 * @author ERP Development Team
 * @date 2026-03-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ErpApprovalHistory.class, reverseConvertGenerate = false)
public class ErpApprovalHistoryBo extends BaseEntity {

    /**
     * 历史记录 ID
     */
    private Long historyId;

    /**
     * 单据 ID
     */
    @NotBlank(message = "单据 ID 不能为空")
    private Long billId;

    /**
     * 模块编码
     */
    @NotBlank(message = "模块编码不能为空")
    private String moduleCode;

    /**
     * 流程 ID
     */
    private Long flowId;

    /**
     * 当前步骤
     */
    private Integer currentStep;

    /**
     * 审批人 ID
     */
    private String approverId;

    /**
     * 审批动作
     */
    @NotBlank(message = "审批动作不能为空")
    private String approvalAction;

    /**
     * 审批意见
     */
    private String approvalOpinion;

}
