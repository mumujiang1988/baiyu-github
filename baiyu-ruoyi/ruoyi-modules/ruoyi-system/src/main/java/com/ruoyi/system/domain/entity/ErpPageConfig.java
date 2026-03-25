package com.ruoyi.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ERP 公共配置表实体类
 * 
 * @author ERP Development Team
 * @date 2026-03-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("erp_page_config")
public class ErpPageConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "config_id", type = IdType.AUTO)
    private Long configId;

    /**
     * 模块编码 (如 saleOrder/deliveryOrder)
     */
    @TableField("module_code")
    private String moduleCode;

    /**
     * 配置名称
     */
    @TableField("config_name")
    private String configName;

    /**
     * 配置类型 (PAGE=页面配置/DICT=字典配置/PUSH=下推配置/APPROVAL=审批配置)
     */
    @TableField("config_type")
    private String configType;

    /**
     * 完整的 JSON 配置内容
     */
    @TableField("config_content")
    private String configContent;

    /**
     * 版本号 (每次更新 +1)
     */
    @TableField("version")
    private Integer version;

    /**
     * 状态 (1 正常 0 停用)
     */
    @TableField("status")
    private String status;

    /**
     * 是否公共配置 (1 是 0 否)
     */
    @TableField("is_public")
    private String isPublic;

    /**
     * 父配置 ID(用于继承)
     */
    @TableField("parent_config_id")
    private Long parentConfigId;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

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

    /**
     * 更新者
     */
    @TableField(value = "update_by", fill = FieldFill.UPDATE)
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
