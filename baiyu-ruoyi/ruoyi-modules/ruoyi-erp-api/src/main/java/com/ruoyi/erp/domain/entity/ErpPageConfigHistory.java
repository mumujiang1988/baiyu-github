package com.ruoyi.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ERP 配置历史表实体类
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
     * 历史记录 ID
     */
    @TableId(value = "history_id", type = IdType.AUTO)
    private Long historyId;

    /**
     * 配置 ID(关联 erp_page_config)
     */
    @TableField("config_id")
    private Long configId;

    /**
     * 模块编码
     */
    @TableField("module_code")
    private String moduleCode;

    /**
     * 配置类型
     */
    @TableField("config_type")
    private String configType;

    /**
     * 版本号
     */
    @TableField("version")
    private Integer version;

    /**
     * 页面配置快照 (page.json)
     */
    @TableField("page_config")
    private String pageConfig;

    /**
     * 表单配置快照 (form.json)
     */
    @TableField("form_config")
    private String formConfig;

    /**
     * 表格配置快照 (table.json)
     */
    @TableField("table_config")
    private String tableConfig;

    /**
     * 搜索配置快照 (search.json)
     */
    @TableField("search_config")
    private String searchConfig;

    /**
     * 按钮配置快照 (action.json)
     */
    @TableField("action_config")
    private String actionConfig;

    /**
     * 字典配置快照 (dict.json)
     */
    @TableField("dict_config")
    private String dictConfig;

    /**
     * 业务配置快照 (config.json)
     */
    @TableField("business_config")
    private String businessConfig;

    /**
     * 变更原因
     */
    @TableField("change_reason")
    private String changeReason;

    /**
     * 变更类型 (ADD/UPDATE/DELETE/ROLLBACK)
     */
    @TableField("change_type")
    private String changeType;

    /**
     * 创建者
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
