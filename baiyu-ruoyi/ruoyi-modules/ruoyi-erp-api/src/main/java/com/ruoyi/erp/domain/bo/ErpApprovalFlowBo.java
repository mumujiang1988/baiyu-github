package com.ruoyi.erp.domain.bo;

import com.ruoyi.common.mybatis.core.domain.BaseEntity;
import com.ruoyi.erp.domain.entity.ErpApprovalFlow;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ERP Approval Flow Configuration Business Object erp_approval_flow
 *
 * @author JMH
 * @date 2026-03-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ErpApprovalFlow.class)
public class ErpApprovalFlowBo extends BaseEntity {

    /**
     * Flow ID
     */
    private Long flowId;

    /**
     * Module code
     */
    @NotBlank(message = "Module code cannot be empty")
    @Size(min = 0, max = 50, message = "Module code length cannot exceed {max} characters")
    private String moduleCode;

    /**
     * Flow name
     */
    @NotBlank(message = "Flow name cannot be empty")
    @Size(min = 0, max = 100, message = "Flow name length cannot exceed {max} characters")
    private String flowName;

    /**
     * Flow definition (JSON)
     */
    @NotBlank(message = "Flow definition cannot be empty")
    private String flowDefinition;

    /**
     * Current version number
     */
    private Integer currentVersion;

    /**
     * Is active
     */
    private String isActive;

}
