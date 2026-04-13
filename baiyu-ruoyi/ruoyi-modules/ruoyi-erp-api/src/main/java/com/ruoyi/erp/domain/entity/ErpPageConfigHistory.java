package com.ruoyi.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ERP Configuration History Table Entity
 * 
 * @author JMH
 * @date 2026-03-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("erp_page_config_history")
public class ErpPageConfigHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * History record ID
     */
    @TableId(value = "history_id", type = IdType.AUTO)
    private Long historyId;

    /**
     * Configuration ID (related to erp_page_config)
     */
    @TableField("config_id")
    private Long configId;

    /**
     * Module code
     */
    @TableField("module_code")
    private String moduleCode;

    /**
     * Configuration type
     */
    @TableField("config_type")
    private String configType;

    /**
     * Version number
     */
    @TableField("version")
    private Integer version;

    /**
     * Page configuration snapshot (page.json)
     */
    @TableField("page_config")
    private String pageConfig;

    /**
     * Form configuration snapshot (form.json)
     */
    @TableField("form_config")
    private String formConfig;

    /**
     * Table configuration snapshot (table.json)
     */
    @TableField("table_config")
    private String tableConfig;

    /**
     * Search configuration snapshot (search.json)
     */
    @TableField("search_config")
    private String searchConfig;

    /**
     * Button configuration snapshot (action.json)
     */
    @TableField("action_config")
    private String actionConfig;

    /**
     * Dictionary configuration snapshot (dict.json)
     */
    @TableField("dict_config")
    private String dictConfig;

    /**
     * API interface configuration snapshot (api.json)
     */
    @TableField("api_config")
    private String apiConfig;

    /**
     * Business configuration snapshot (config.json)
     */
    @TableField("business_config")
    private String businessConfig;

    /**
     * Change reason
     */
    @TableField("change_reason")
    private String changeReason;

    /**
     * Change type (ADD/UPDATE/DELETE/ROLLBACK)
     */
    @TableField("change_type")
    private String changeType;

    /**
     * Creator
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    /**
     * Create time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
