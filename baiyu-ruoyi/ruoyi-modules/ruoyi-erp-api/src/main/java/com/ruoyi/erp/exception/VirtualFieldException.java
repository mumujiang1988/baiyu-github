package com.ruoyi.erp.exception;

/**
 * 虚拟字段异常
 * 
 * @author JMH
 * @date 2026-03-24
 */
public class VirtualFieldException extends ErpConfigException {
    
    public VirtualFieldException(String moduleCode, String message) {
        super(moduleCode, "VIRTUAL_FIELD_ERROR", message);
    }
    
    public VirtualFieldException(String moduleCode, String message, Throwable cause) {
        super(moduleCode, "VIRTUAL_FIELD_ERROR", message, cause);
    }
}
