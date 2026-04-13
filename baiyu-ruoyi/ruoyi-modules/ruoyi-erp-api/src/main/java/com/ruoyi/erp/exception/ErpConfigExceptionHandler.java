package com.ruoyi.erp.exception;

import cn.dev33.satoken.exception.NotPermissionException;
import com.ruoyi.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * ERP Configuration Exception Handler
 * 
 * @author JMH
 * @date 2026-03-24
 */
@RestControllerAdvice
@Slf4j
public class ErpConfigExceptionHandler {
    
    /**
     * ERP configuration business exception
     */
    @ExceptionHandler(ErpConfigException.class)
    public R<?> handleErpConfigException(ErpConfigException e) {
        log.error("ERP configuration exception - Module: {}, Error Code: {}, Error: {}", 
            e.getModuleCode(), e.getErrorCode(), e.getMessage());
        
        return R.fail(String.format("Configuration error [%s]: %s", e.getModuleCode(), e.getMessage()));
    }
    
    /**
     * Computed field exception
     */
    @ExceptionHandler(ComputedFieldException.class)
    public R<?> handleComputedFieldException(ComputedFieldException e) {
        log.error("Computed field exception - Module: {}, Field: {}, Formula: {}, Error: {}", 
            e.getModuleCode(), e.getFieldName(), e.getFormula(), e.getMessage());
        
        return R.fail(String.format("Calculation error: Field %s calculation failed (%s)", e.getFieldName(), e.getMessage()));
    }
    
    /**
     * Virtual field exception
     */
    @ExceptionHandler(VirtualFieldException.class)
    public R<?> handleVirtualFieldException(VirtualFieldException e) {
        log.error("Virtual field exception - Module: {}, Error: {}", e.getModuleCode(), e.getMessage());
        
        return R.fail(String.format("Virtual field parsing failed: %s", e.getMessage()));
    }
    
    /**
     * Permission exception
     */
    @ExceptionHandler(NotPermissionException.class)
    public R<?> handlePermissionException(NotPermissionException e) {
        log.warn("Insufficient permissions: {}", e.getMessage());
        return R.fail("Insufficient permissions, cannot access this resource");
    }
    
    /**
     * Configuration parsing exception (JSON format error)
     */
    @ExceptionHandler(com.fasterxml.jackson.core.JsonParseException.class)
    public R<?> handleJsonParseException(com.fasterxml.jackson.core.JsonParseException e) {
        log.error("Configuration parsing exception: {}", e.getMessage());
        return R.fail("Configuration format error, please check the configuration content");
    }
    
    /**
     * General runtime exception
     */
    @ExceptionHandler(RuntimeException.class)
    public R<?> handleRuntimeException(RuntimeException e) {
        log.error("Runtime exception", e);
        return R.fail("System exception: " + e.getMessage());
    }
}
