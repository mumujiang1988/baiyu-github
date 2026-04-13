package com.ruoyi.erp.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.ruoyi.common.excel.annotation.ExcelDictFormat;
import com.ruoyi.common.excel.convert.ExcelDictConvert;
import com.ruoyi.erp.domain.entity.ErpPageConfigHistory;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ERP Configuration History View Object erp_page_config_history
 *
 * @author JMH
 * @date 2026-03-22
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = ErpPageConfigHistory.class)
public class ErpPageConfigHistoryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * History record ID
     */
    @ExcelProperty(value = "History ID")
    private Long historyId;

    /**
     * Configuration ID
     */
    @ExcelProperty(value = "Configuration ID")
    private Long configId;

    /**
     * Module code
     */
    @ExcelProperty(value = "Module Code")
    private String moduleCode;

    /**
     * Configuration type
     */
    @ExcelProperty(value = "Configuration Type", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "erp_config_type")
    private String configType;

    /**
     * Version number
     */
    @ExcelProperty(value = "Version Number")
    private Integer version;

    /**
     * Complete JSON configuration
     */
    @ExcelProperty(value = "Configuration Content")
    private String configContent;

    /**
     * Change reason
     */
    @ExcelProperty(value = "Change Reason")
    private String changeReason;

    /**
     * Change type
     */
    @ExcelProperty(value = "Change Type", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "erp_change_type")
    private String changeType;

    /**
     * Creator
     */
    @ExcelProperty(value = "Creator")
    private String createBy;

    /**
     * Create time
     */
    @ExcelProperty(value = "Create Time")
    private LocalDateTime createTime;

}
