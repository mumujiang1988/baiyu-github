package com.ruoyi.erp.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.ruoyi.erp.domain.entity.ErpApprovalHistory;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ERP Approval History View Object erp_approval_history
 *
 * @author JMH
 * @date 2026-03-22
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = ErpApprovalHistory.class)
public class ErpApprovalHistoryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * History record ID
     */
    @ExcelProperty(value = "History ID")
    private Long historyId;

    /**
     * Bill ID
     */
    @ExcelProperty(value = "Bill ID")
    private Long billId;

    /**
     * Module code
     */
    @ExcelProperty(value = "Module Code")
    private String moduleCode;

    /**
     * Flow ID
     */
    @ExcelProperty(value = "Flow ID")
    private Long flowId;

    /**
     * Current step
     */
    @ExcelProperty(value = "Current Step")
    private Integer currentStep;

    /**
     * Approver ID
     */
    @ExcelProperty(value = "Approver ID")
    private String approverId;

    /**
     * Approval action
     */
    @ExcelProperty(value = "Approval Action")
    private String approvalAction;

    /**
     * Approval opinion
     */
    @ExcelProperty(value = "Approval Opinion")
    private String approvalOpinion;

    /**
     * Approval time
     */
    @ExcelProperty(value = "Approval Time")
    private LocalDateTime approvalTime;

}
