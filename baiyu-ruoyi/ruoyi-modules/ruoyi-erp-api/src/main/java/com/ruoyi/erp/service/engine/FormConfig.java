package com.ruoyi.erp.service.engine;

import lombok.Data;

/**
 * 表单字段配置
 * 
 * @author ERP Development Team
 * @date 2026-03-24
 */
@Data
public class FormConfig {
    
    /**
     * 字段名
     */
    private String field;
    
    /**
     * 字段标签
     */
    private String label;
    
    /**
     * 字段类型 (input/select/radio/checkbox/date/number/upload)
     */
    private String type;
    
    /**
     * 占位符
     */
    private String placeholder;
    
    /**
     * 是否必填
     */
    private Boolean required;
    
    /**
     * 默认值
     */
    private Object defaultValue;
    
    /**
     * 字典类型 (用于下拉选择)
     */
    private String dictType;
    
    /**
     * 数据源 (API 接口)
     */
    private String dataSource;
    
    /**
     * 验证规则
     */
    private String validation;
    
    /**
     * 最大长度
     */
    private Integer maxLength;
    
    /**
     * 最小长度
     */
    private Integer minLength;
    
    /**
     * 最大值 (数字类型)
     */
    private Number max;
    
    /**
     * 最小值 (数字类型)
     */
    private Number min;
    
    /**
     * 是否只读
     */
    private Boolean readonly;
    
    /**
     * 是否禁用
     */
    private Boolean disabled;
    
    /**
     * 是否隐藏
     */
    private Boolean hidden;
    
    /**
     * 列数 (栅格布局，1-24)
     */
    private Integer span;
    
    /**
     * 偏移量 (栅格布局)
     */
    private Integer offset;
    
    /**
     * 选项列表 (用于 radio/checkbox/select)
     */
    private Object options;
}
