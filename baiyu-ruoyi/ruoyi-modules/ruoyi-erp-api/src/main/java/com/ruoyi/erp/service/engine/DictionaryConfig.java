package com.ruoyi.erp.service.engine;

import lombok.Data;

/**
 * 字典配置
 * 
 * @author JMH
 * @date 2026-03-24
 */
@Data
public class DictionaryConfig {
    
    /**
     * 字典类型
     */
    private String dictType;
    
    /**
     * 字典名称
     */
    private String dictName;
    
    /**
     * 数据源类型 (system=系统字典/api=API 接口/static=静态配置)
     */
    private String dataSourceType;
    
    /**
     * API 接口地址 (当 dataSourceType 为 api 时使用)
     */
    private String api;
    
    /**
     * 请求方法 (GET/POST)
     */
    private String method;
    
    /**
     * 请求参数
     */
    private Object params;
    
    /**
     * 值字段名 (默认 value)
     */
    private String valueField;
    
    /**
     * 标签字段名 (默认 label)
     */
    private String labelField;
    
    /**
     * 子项字段名 (用于级联选择，默认 children)
     */
    private String childrenField;
    
    /**
     * 是否缓存
     */
    private Boolean cacheable;
    
    /**
     * 缓存时间 (秒)
     */
    private Integer cacheTime;
}
