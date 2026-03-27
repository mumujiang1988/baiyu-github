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
 * ERP 审批历史记录视图对象 erp_approval_history
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
     * 历史记录 ID
     */
    @ExcelProperty(value = "历史记录 ID")
    private Long historyId;

    /**
     * 单据 ID
     */
    @ExcelProperty(value = "单据 ID")
    private Long billId;

    /**
     * 模块编码
     */
    @ExcelProperty(value = "模块编码")
    private String moduleCode;

    /**
     * 流程 ID
     */
    @ExcelProperty(value = "流程 ID")
    private Long flowId;

    /**
     * 当前步骤
     */
    @ExcelProperty(value = "当前步骤")
    private Integer currentStep;

    /**
     * 审批人 ID
     */
    @ExcelProperty(value = "审批人 ID")
    private String approverId;

    /**
     * 审批动作
     */
    @ExcelProperty(value = "审批动作")
    private String approvalAction;

    /**
     * 审批意见
     */
    @ExcelProperty(value = "审批意见")
    private String approvalOpinion;

    /**
     * 审批时间
     */
    @ExcelProperty(value = "审批时间")
    private LocalDateTime approvalTime;

}
