package com.ruoyi.erp.service.engine;

import lombok.Data;

/**
 * Table Column Configuration
 * 
 * @author JMH
 * @date 2026-03-24
 */
@Data
public class TableColumnConfig {
    
    /**
     * Field name (prop)
     */
    private String prop;
    
    /**
     * Column title (label)
     */
    private String label;
    
    /**
     * Data type (text/tag/link/image/checkbox)
     */
    private String dataType;
    
    /**
     * Display type (renderType, same as dataType)
     */
    private String renderType;
    
    /**
     * Dictionary type (for dictionary translation)
     */
    private String dictType;
    
    /**
     * Format function (formatter)
     */
    private String formatter;
    
    /**
     * Whether fixed (fixed: left/right/true)
     */
    private String fixed;
    
    /**
     * Column width (width)
     */
    private Integer width;
    
    /**
     * Minimum column width (minWidth)
     */
    private Integer minWidth;
    
    /**
     * Whether sortable (sortable)
     */
    private Boolean sortable;
    
    /**
     * Whether hidden (hidden)
     */
    private Boolean hidden;
    
    /**
     * Alignment (align: left/center/right)
     */
    private String align;
    
    /**
     * Virtual field flag
     */
    private Boolean virtualField;
}
