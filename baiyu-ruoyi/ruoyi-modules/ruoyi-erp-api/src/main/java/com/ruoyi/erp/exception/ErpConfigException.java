package com.ruoyi.erp.exception;

import lombok.Getter;

/**
 * ERP 配置化业务异常
 * 
 * @author ERP Development Team
 * @date 2026-03-24
 */
@Getter
public class ErpConfigException extends RuntimeException {
    
    /**
     * 模块编码
     */
    private final String moduleCode;
    
    /**
     * 错误码
     */
    private final String errorCode;
    
    public ErpConfigException(String moduleCode, String message) {
        super(message);
        this.moduleCode = moduleCode;
        this.errorCode = "CONFIG_ERROR";
    }
    
    public ErpConfigException(String moduleCode, String errorCode, String message) {
        super(message);
        this.moduleCode = moduleCode;
        this.errorCode = errorCode;
    }
    
    public ErpConfigException(String moduleCode, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.moduleCode = moduleCode;
        this.errorCode = errorCode;
    }
}
