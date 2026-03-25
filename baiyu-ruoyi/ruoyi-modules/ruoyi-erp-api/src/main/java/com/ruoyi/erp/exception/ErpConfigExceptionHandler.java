package com.ruoyi.erp.exception;

import cn.dev33.satoken.exception.NotPermissionException;
import com.ruoyi.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * ERP 配置化异常处理器
 * 
 * @author ERP Development Team
 * @date 2026-03-24
 */
@RestControllerAdvice
@Slf4j
public class ErpConfigExceptionHandler {
    
    /**
     * ERP 配置化业务异常
     */
    @ExceptionHandler(ErpConfigException.class)
    public R<?> handleErpConfigException(ErpConfigException e) {
        log.error("ERP 配置化异常 - 模块：{}, 错误码：{}, 错误：{}", 
            e.getModuleCode(), e.getErrorCode(), e.getMessage());
        
        return R.fail(String.format("配置错误 [%s]: %s", e.getModuleCode(), e.getMessage()));
    }
    
    /**
     * 计算字段异常
     */
    @ExceptionHandler(ComputedFieldException.class)
    public R<?> handleComputedFieldException(ComputedFieldException e) {
        log.error("计算字段异常 - 模块：{}, 字段：{}, 公式：{}, 错误：{}", 
            e.getModuleCode(), e.getFieldName(), e.getFormula(), e.getMessage());
        
        return R.fail(String.format("计算错误：字段%s计算失败 (%s)", e.getFieldName(), e.getMessage()));
    }
    
    /**
     * 虚拟字段异常
     */
    @ExceptionHandler(VirtualFieldException.class)
    public R<?> handleVirtualFieldException(VirtualFieldException e) {
        log.error("虚拟字段异常 - 模块：{}, 错误：{}", e.getModuleCode(), e.getMessage());
        
        return R.fail(String.format("虚拟字段解析失败：%s", e.getMessage()));
    }
    
    /**
     * 权限异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public R<?> handlePermissionException(NotPermissionException e) {
        log.warn("权限不足：{}", e.getMessage());
        return R.fail("权限不足，无法访问该资源");
    }
    
    /**
     * 配置解析异常 (JSON 格式错误)
     */
    @ExceptionHandler(com.fasterxml.jackson.core.JsonParseException.class)
    public R<?> handleJsonParseException(com.fasterxml.jackson.core.JsonParseException e) {
        log.error("配置解析异常：{}", e.getMessage());
        return R.fail("配置格式错误，请检查配置内容");
    }
    
    /**
     * 通用运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public R<?> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常", e);
        return R.fail("系统异常：" + e.getMessage());
    }
}
