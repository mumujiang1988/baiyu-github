package com.ruoyi.erp.service.engine;

import lombok.Data;

/**
 * 计算字段配置
 * 
 * @author JMH
 * @date 2026-03-24
 */
@Data
public class ComputedFieldConfig {
    
    /**
     * 目标字段名
     */
    private String targetField;
    
    /**
     * 计算公式
     * 支持：SUM(list.field), AVG(list.field), COUNT(list), MAX(list.field), MIN(list.field)
     * 以及四则运算：+ - * /
     */
    private String formula;
    
    /**
     * 触发事件 (用于前端实时计算)
     * 例如：entryListChange, costChange, priceChange 等
     */
    private String trigger;
    
    /**
     * 小数精度，默认 2 位
     */
    private Integer precision = 2;
    
    /**
     * 计算说明
     */
    private String description;
}
