package com.ruoyi.erp.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.ruoyi.common.excel.annotation.ExcelDictFormat;
import com.ruoyi.common.excel.convert.ExcelDictConvert;
import com.ruoyi.erp.domain.entity.ErpApprovalFlow;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ERP 审批流程配置视图对象 erp_approval_flow
 *
 * @author ERP Development Team
 * @date 2026-03-22
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = ErpApprovalFlow.class)
public class ErpApprovalFlowVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 流程 ID
     */
    @ExcelProperty(value = "流程 ID")
    private Long flowId;

    /**
     * 模块编码
     */
    @ExcelProperty(value = "模块编码")
    private String moduleCode;

    /**
     * 流程名称
     */
    @ExcelProperty(value = "流程名称")
    private String flowName;

    /**
     * 流程定义 (JSON)
     */
    @ExcelProperty(value = "流程定义")
    private String flowDefinition;

    /**
     * 当前版本号
     */
    @ExcelProperty(value = "当前版本号")
    private Integer currentVersion;

    /**
     * 是否激活
     */
    @ExcelProperty(value = "是否激活", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_yes_no")
    private String isActive;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @ExcelProperty(value = "更新时间")
    private LocalDateTime updateTime;

}
