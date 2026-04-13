package com.ruoyi.erp.service.engine;

import lombok.Data;

/**
 * Form Field Configuration
 * 
 * @author JMH
 * @date 2026-03-24
 */
@Data
public class FormConfig {
    
    /**
     * Field name
     */
    private String field;
    
    /**
     * Field label
     */
    private String label;
    
    /**
     * Field type (input/select/radio/checkbox/date/number/upload)
     */
    private String type;
    
    /**
     * Placeholder
     */
    private String placeholder;
    
    /**
     * Whether required
     */
    private Boolean required;
    
    /**
     * Default value
     */
    private Object defaultValue;
    
    /**
     * Dictionary type (for dropdown selection)
     */
    private String dictType;
    
    /**
     * Data source (API interface)
     */
    private String dataSource;
    
    /**
     * Validation rules
     */
    private String validation;
    
    /**
     * Maximum length
     */
    private Integer maxLength;
    
    /**
     * Minimum length
     */
    private Integer minLength;
    
    /**
     * Maximum value (numeric type)
     */
    private Number max;
    
    /**
     * Minimum value (numeric type)
     */
    private Number min;
    
    /**
     * Whether read-only
     */
    private Boolean readonly;
    
    /**
     * Whether disabled
     */
    private Boolean disabled;
    
    /**
     * Whether hidden
     */
    private Boolean hidden;
    
    /**
     * Column count (grid layout, 1-24)
     */
    private Integer span;
    
    /**
     * Offset (grid layout)
     */
    private Integer offset;
    
    /**
     * Option list (for radio/checkbox/select)
     */
    private Object options;
}
