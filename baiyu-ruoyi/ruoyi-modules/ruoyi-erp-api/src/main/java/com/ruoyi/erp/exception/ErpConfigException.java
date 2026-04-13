package com.ruoyi.erp.exception;

import lombok.Getter;

/**
 * ERP Configuration Business Exception
 * 
 * @author JMH
 * @date 2026-03-24
 */
@Getter
public class ErpConfigException extends RuntimeException {
    
    /**
     * Module code
     */
    private final String moduleCode;
    
    /**
     * Error code
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
