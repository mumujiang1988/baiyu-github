package com.ruoyi.erp.service.engine;

import lombok.Data;

/**
 * 表格列配置
 * 
 * @author JMH
 * @date 2026-03-24
 */
@Data
public class TableColumnConfig {
    
    /**
     * 字段名 (prop)
     */
    private String prop;
    
    /**
     * 列标题 (label)
     */
    private String label;
    
    /**
     * 数据类型 (text/tag/link/image/checkbox)
     */
    private String dataType;
    
    /**
     * 显示类型 (renderType, 同 dataType)
     */
    private String renderType;
    
    /**
     * 字典类型 (用于字典翻译)
     */
    private String dictType;
    
    /**
     * 格式化函数 (formatter)
     */
    private String formatter;
    
    /**
     * 是否固定 (fixed: left/right/true)
     */
    private String fixed;
    
    /**
     * 列宽 (width)
     */
    private Integer width;
    
    /**
     * 最小列宽 (minWidth)
     */
    private Integer minWidth;
    
    /**
     * 是否可排序 (sortable)
     */
    private Boolean sortable;
    
    /**
     * 是否隐藏 (hidden)
     */
    private Boolean hidden;
    
    /**
     * 对齐方式 (align: left/center/right)
     */
    private String align;
    
    /**
     * 虚拟字段标识
     */
    private Boolean virtualField;
}
