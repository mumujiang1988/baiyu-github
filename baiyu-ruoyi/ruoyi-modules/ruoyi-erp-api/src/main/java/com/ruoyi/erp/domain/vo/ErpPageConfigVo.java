package com.ruoyi.erp.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.ruoyi.common.excel.annotation.ExcelDictFormat;
import com.ruoyi.common.excel.convert.ExcelDictConvert;
import com.ruoyi.erp.domain.entity.ErpPageConfig;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ERP Page Configuration View Object erp_page_config
 *
 * @author JMH
 * @date 2026-03-22
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = ErpPageConfig.class)
public class ErpPageConfigVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Primary key ID
     */
    @ExcelProperty(value = "Primary Key ID")
    private Long configId;

    /**
     * Module code
     */
    @ExcelProperty(value = "Module Code")
    private String moduleCode;

    /**
     * Configuration name
     */
    @ExcelProperty(value = "Configuration Name")
    private String configName;

    /**
     * Configuration type
     */
    @ExcelProperty(value = "Configuration Type", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "erp_config_type")
    private String configType;

    /**
     * Page base configuration (page.json)
     */
    @ExcelProperty(value = "Page Configuration")
    private String pageConfig;

    /**
     * Form UI component configuration (form.json)
     */
    @ExcelProperty(value = "Form Configuration")
    private String formConfig;

    /**
     * Table query configuration (table.json)
     */
    @ExcelProperty(value = "Table Configuration")
    private String tableConfig;

    /**
     * Search form configuration (search.json)
     */
    @ExcelProperty(value = "Search Configuration")
    private String searchConfig;

    /**
     * Button operation configuration (action.json)
     */
    @ExcelProperty(value = "Button Configuration")
    private String actionConfig;

    /**
     * Dictionary data source configuration (dict.json)
     */
    @ExcelProperty(value = "Dictionary Configuration")
    private String dictConfig;

    /**
     * API interface configuration (api.json)
     */
    @ExcelProperty(value = "API Configuration")
    private String apiConfig;

    /**
     * Business rule configuration (config.json)
     */
    @ExcelProperty(value = "Business Configuration")
    private String businessConfig;

    /**
     * Detail page configuration (detail.json)
     */
    @ExcelProperty(value = "Detail Configuration")
    private String detailConfig;

    /**
     * Version number
     */
    @ExcelProperty(value = "Version Number")
    private Integer version;

    /**
     * Status
     */
    @ExcelProperty(value = "Status", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_normal_disable")
    private String status;

    /**
     * Is public configuration
     */
    @ExcelProperty(value = "Is Public", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_yes_no")
    private String isPublic;

    /**
     * Parent configuration ID
     */
    @ExcelProperty(value = "Parent Config ID")
    private Long parentConfigId;

    /**
     * Remark
     */
    @ExcelProperty(value = "Remark")
    private String remark;

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
