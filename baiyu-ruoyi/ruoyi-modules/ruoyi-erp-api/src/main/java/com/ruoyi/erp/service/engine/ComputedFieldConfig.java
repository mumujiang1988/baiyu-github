package com.ruoyi.erp.service.engine;

import lombok.Data;

/**
 * Computed Field Configuration
 * 
 * @author JMH
 * @date 2026-03-24
 */
@Data
public class ComputedFieldConfig {
    
    /**
     * Target field name
     */
    private String targetField;
    
    /**
     * Calculation formula
     * Support: SUM(list.field), AVG(list.field), COUNT(list), MAX(list.field), MIN(list.field)
     * And arithmetic operations: + - * /
     */
    private String formula;
    
    /**
     * Trigger event (for frontend real-time calculation)
     * For example: entryListChange, costChange, priceChange, etc.
     */
    private String trigger;
    
    /**
     * Decimal precision, default 2 digits
     */
    private Integer precision = 2;
    
    /**
     * Calculation description
     */
    private String description;
}
