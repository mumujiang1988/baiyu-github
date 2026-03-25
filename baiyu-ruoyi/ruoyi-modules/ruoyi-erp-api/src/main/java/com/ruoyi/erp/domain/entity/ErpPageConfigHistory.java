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
 * @author ERP Development Team
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
     * 完整的 JSON 配置
     */
    @TableField("config_content")
    private String configContent;

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
