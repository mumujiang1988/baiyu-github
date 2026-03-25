package com.ruoyi.erp.exception;

import lombok.Getter;

/**
 * 计算字段异常
 * 
 * @author ERP Development Team
 * @date 2026-03-24
 */
@Getter
public class ComputedFieldException extends ErpConfigException {
    
    /**
     * 字段名
     */
    private final String fieldName;
    
    /**
     * 计算公式
     */
    private final String formula;
    
    /**
     * 计算字段异常
     * 
     * @param fieldName 字段名
     * @param formula 计算公式
     * @param message 错误消息
     */
    public ComputedFieldException(String fieldName, String formula, String message) {
        super("ERP", "COMPUTE_ERROR", message);
        this.fieldName = fieldName;
        this.formula = formula;
    }
    
    /**
     * 计算字段异常
     * 
     * @param fieldName 字段名
     * @param formula 计算公式
     * @param message 错误消息
     * @param cause 原始异常
     */
    public ComputedFieldException(String fieldName, String formula, String message, Throwable cause) {
        super("ERP", "COMPUTE_ERROR", message, cause);
        this.fieldName = fieldName;
        this.formula = formula;
    }
    
    /**
     * 计算字段异常 (带模块编码)
     * 
     * @param moduleCode 模块编码
     * @param fieldName 字段名
     * @param formula 计算公式
     * @param message 错误消息
     */
    public ComputedFieldException(String moduleCode, String fieldName, String formula, String message) {
        super(moduleCode, "COMPUTE_ERROR", message);
        this.fieldName = fieldName;
        this.formula = formula;
    }
    
    /**
     * 计算字段异常 (带模块编码和原因)
     * 
     * @param moduleCode 模块编码
     * @param fieldName 字段名
     * @param formula 计算公式
     * @param message 错误消息
     * @param cause 原始异常
     */
    public ComputedFieldException(String moduleCode, String fieldName, String formula, String message, Throwable cause) {
        super(moduleCode, "COMPUTE_ERROR", message, cause);
        this.fieldName = fieldName;
        this.formula = formula;
    }
}
