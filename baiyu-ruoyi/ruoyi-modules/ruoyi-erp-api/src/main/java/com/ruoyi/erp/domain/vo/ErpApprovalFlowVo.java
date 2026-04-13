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
 * ERP Approval Flow Configuration View Object erp_approval_flow
 *
 * @author JMH
 * @date 2026-03-22
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = ErpApprovalFlow.class)
public class ErpApprovalFlowVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Flow ID
     */
    @ExcelProperty(value = "Flow ID")
    private Long flowId;

    /**
     * Module code
     */
    @ExcelProperty(value = "Module Code")
    private String moduleCode;

    /**
     * Flow name
     */
    @ExcelProperty(value = "Flow Name")
    private String flowName;

    /**
     * Flow definition (JSON)
     */
    @ExcelProperty(value = "Flow Definition")
    private String flowDefinition;

    /**
     * Current version number
     */
    @ExcelProperty(value = "Current Version")
    private Integer currentVersion;

    /**
     * Is active
     */
    @ExcelProperty(value = "Is Active", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_yes_no")
    private String isActive;

    /**
     * Create time
     */
    @ExcelProperty(value = "Create Time")
    private LocalDateTime createTime;

    /**
     * Update time
     */
    @ExcelProperty(value = "Update Time")
    private LocalDateTime updateTime;

}
