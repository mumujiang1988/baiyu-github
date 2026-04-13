package com.ruoyi.erp.service.engine;

import lombok.Data;

/**
 * Dictionary Configuration
 * 
 * @author JMH
 * @date 2026-03-24
 */
@Data
public class DictionaryConfig {
    
    /**
     * Dictionary type
     */
    private String dictType;
    
    /**
     * Dictionary name
     */
    private String dictName;
    
    /**
     * Data source type (system=system dictionary/api=API interface/static=static configuration)
     */
    private String dataSourceType;
    
    /**
     * API interface address (used when dataSourceType is api)
     */
    private String api;
    
    /**
     * Request method (GET/POST)
     */
    private String method;
    
    /**
     * Request parameters
     */
    private Object params;
    
    /**
     * Value field name (default: value)
     */
    private String valueField;
    
    /**
     * Label field name (default: label)
     */
    private String labelField;
    
    /**
     * Children field name (for cascading selection, default: children)
     */
    private String childrenField;
    
    /**
     * Whether to cache
     */
    private Boolean cacheable;
    
    /**
     * Cache time (seconds)
     */
    private Integer cacheTime;
}
