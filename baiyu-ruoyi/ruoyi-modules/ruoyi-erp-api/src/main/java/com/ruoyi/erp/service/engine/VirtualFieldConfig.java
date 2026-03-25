package com.ruoyi.erp.service.engine;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 虚拟字段配置
 * 
 * @author ERP Development Team
 * @date 2026-03-24
 */
@Data
public class VirtualFieldConfig {
    
    /**
     * 虚拟字段名
     */
    private String name;
    
    /**
     * 源字段名 (关联字段)
     */
    @JsonProperty("sourceField")
    private String sourceField;
    
    /**
     * 源数据表
     */
    @JsonProperty("sourceTable")
    private String sourceTable;
    
    /**
     * 显示字段名
     */
    @JsonProperty("sourceDisplayField")
    private String sourceDisplayField;
    
    /**
     * 显示类型：text/link/tag
     */
    @JsonProperty("displayType")
    private String displayType = "text";
    
    /**
     * 是否缓存
     */
    private Boolean cacheable = true;
    
    /**
     * 显示配置 (用于 link/tag 类型的额外配置)
     */
    @JsonProperty("displayConfig")
    private Object displayConfig;
    
    /**
     * 说明
     */
    private String description;
}
