package com.ruoyi.erp.service.engine;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Virtual Field Configuration
 * 
 * @author JMH
 * @date 2026-03-24
 */
@Data
public class VirtualFieldConfig {
    
    /**
     * Virtual field name
     */
    private String name;
    
    /**
     * Source field name (related field)
     */
    @JsonProperty("sourceField")
    private String sourceField;
    
    /**
     * Source data table
     */
    @JsonProperty("sourceTable")
    private String sourceTable;
    
    /**
     * Display field name
     */
    @JsonProperty("sourceDisplayField")
    private String sourceDisplayField;
    
    /**
     * Display type: text/link/tag
     */
    @JsonProperty("displayType")
    private String displayType = "text";
    
    /**
     * Whether cacheable
     */
    private Boolean cacheable = true;
    
    /**
     * Display configuration (extra configuration for link/tag types)
     */
    @JsonProperty("displayConfig")
    private Object displayConfig;
    
    /**
     * Description
     */
    private String description;
}
