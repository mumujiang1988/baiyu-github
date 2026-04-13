package com.ruoyi.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ERP Page Configuration Table Entity
 * 
 * @author JMH
 * @date 2026-03-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("erp_page_config")
public class ErpPageConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key ID
     */
    @TableId(value = "config_id", type = IdType.AUTO)
    private Long configId;

    /**
     * Module code (e.g., saleOrder/deliveryOrder)
     */
    @TableField("module_code")
    private String moduleCode;

    /**
     * Configuration name
     */
    @TableField("config_name")
    private String configName;

    /**
     * Configuration type (PAGE=page configuration/DICT=dictionary configuration/PUSH=push configuration/APPROVAL=approval configuration)
     */
    @TableField("config_type")
    private String configType;

    /**
     * Page base configuration (page.json)
     */
    @TableField("page_config")
    private String pageConfig;

    /**
     * Form UI component configuration (form.json)
     */
    @TableField("form_config")
    private String formConfig;

    /**
     * Table query configuration (table.json)
     */
    @TableField("table_config")
    private String tableConfig;

    /**
     * Search form configuration (search.json)
     */
    @TableField("search_config")
    private String searchConfig;

    /**
     * Button operation configuration (action.json)
     */
    @TableField("action_config")
    private String actionConfig;

    /**
     * API interface configuration (api.json)
     */
    @TableField("api_config")
    private String apiConfig;

    /**
     * Dictionary data source configuration (dict.json)
     */
    @TableField("dict_config")
    private String dictConfig;

    /**
     * Business rule configuration (config.json)
     */
    @TableField("business_config")
    private String businessConfig;

    /**
     * Detail page configuration (detail.json)
     */
    @TableField("detail_config")
    private String detailConfig;

    /**
     * Version number (incremented by 1 on each update)
     */
    @TableField("version")
    private Integer version;

    /**
     * Status (1 active 0 inactive)
     */
    @TableField("status")
    private String status;

    /**
     * Is public configuration (1 yes 0 no)
     */
    @TableField("is_public")
    private String isPublic;

    /**
     * Parent configuration ID (for inheritance)
     */
    @TableField("parent_config_id")
    private Long parentConfigId;

    /**
     * Remark
     */
    @TableField("remark")
    private String remark;

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

    /**
     * Updater
     */
    @TableField(value = "update_by", fill = FieldFill.UPDATE)
    private String updateBy;

    /**
     * Update time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
