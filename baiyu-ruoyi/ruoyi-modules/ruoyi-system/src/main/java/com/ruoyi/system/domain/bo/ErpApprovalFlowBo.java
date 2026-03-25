package com.ruoyi.system.domain.bo;

import com.ruoyi.common.mybatis.core.domain.BaseEntity;
import com.ruoyi.system.domain.entity.ErpApprovalFlow;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ERP 审批流程配置业务对象 erp_approval_flow
 *
 * @author ERP Development Team
 * @date 2026-03-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ErpApprovalFlow.class)
public class ErpApprovalFlowBo extends BaseEntity {

    /**
     * 流程 ID
     */
    private Long flowId;

    /**
     * 模块编码
     */
    @NotBlank(message = "模块编码不能为空")
    @Size(min = 0, max = 50, message = "模块编码长度不能超过{max}个字符")
    private String moduleCode;

    /**
     * 流程名称
     */
    @NotBlank(message = "流程名称不能为空")
    @Size(min = 0, max = 100, message = "流程名称长度不能超过{max}个字符")
    private String flowName;

    /**
     * 流程定义 (JSON)
     */
    @NotBlank(message = "流程定义不能为空")
    private String flowDefinition;

    /**
     * 当前版本号
     */
    private Integer currentVersion;

    /**
     * 是否激活
     */
    private String isActive;

}
