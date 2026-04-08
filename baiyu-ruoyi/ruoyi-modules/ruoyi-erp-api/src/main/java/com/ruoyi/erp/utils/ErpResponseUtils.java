package com.ruoyi.erp.utils;

import com.ruoyi.erp.domain.response.ErpResponse;
import lombok.extern.slf4j.Slf4j;

/** 
 * 
 * @author JMH
 * @date 2026-03-27
 */
@Slf4j
public class ErpResponseUtils {

    private ErpResponseUtils() {
        throw new IllegalStateException("Utility class");
    }

    // ==================== Success Responses ====================

    /**
     * Build success response (no data)
     *
     * @return Success response
     */
    public static <T> ErpResponse<T> success() {
        return ErpResponse.ok();
    }

    /**
     * Build success response (with data)
     *
     * @param data Data object
     * @return Success response
     */
    public static <T> ErpResponse<T> success(T data) {
        return ErpResponse.ok(data);
    }

    /**
     * Build success response (with message and data)
     *
     * @param msg Message
     * @param data Data object
     * @return Success response
     */
    public static <T> ErpResponse<T> success(String msg, T data) {
        return ErpResponse.ok(msg, data);
    }

    /**
     * Build success response (with custom message)
     *
     * @param msg Message
     * @return Success response
     */
    public static <T> ErpResponse<T> successWithMessage(String msg) {
        return ErpResponse.ok(msg);
    }

    // ==================== Error Responses ====================

    /**
     * Build error response (default message)
     *
     * @return Error response
     */
    public static <T> ErpResponse<T> error() {
        return ErpResponse.fail();
    }

    /**
     * Build error response (with message)
     *
     * @param msg Error message
     * @return Error response
     */
    public static <T> ErpResponse<T> error(String msg) {
        return ErpResponse.fail(msg);
    }

    /**
     * Build error response (with code and message)
     *
     * @param code Status code
     * @param msg Error message
     * @return Error response
     */
    public static <T> ErpResponse<T> error(int code, String msg) {
        return ErpResponse.fail(code, msg);
    }

    /**
     * Build error response (with code, message and detail)
     *
     * @param code Status code
     * @param msg Error message
     * @param detailMsg Detail error information
     * @return Error response
     */
    public static <T> ErpResponse<T> error(int code, String msg, String detailMsg) {
        return ErpResponse.fail(code, msg, detailMsg);
    }

    /**
     * Build error response (from exception)
     *
     * @param e Exception object
     * @return Error response
     */
    public static <T> ErpResponse<T> errorFromException(Exception e) {
        log.error("Operation failed", e);
        return ErpResponse.fail(500, "Operation failed: " + e.getMessage(), e.getClass().getName());
    }

    /**
     * Build error response (from exception with custom message)
     *
     * @param e Exception object
     * @param customMsg Custom message
     * @return Error response
     */
    public static <T> ErpResponse<T> errorFromException(Exception e, String customMsg) {
        log.error("{}: {}", customMsg, e.getMessage(), e);
        return ErpResponse.fail(500, customMsg + ": " + e.getMessage());
    }

    // ==================== Warning Responses ====================

    /**
     * Build warning response
     *
     * @param msg Warning message
     * @return Warning response
     */
    public static <T> ErpResponse<T> warn(String msg) {
        return ErpResponse.warn(msg);
    }

    /**
     * Build warning response (with data)
     *
     * @param msg Warning message
     * @param data Data object
     * @return Warning response
     */
    public static <T> ErpResponse<T> warn(String msg, T data) {
        return ErpResponse.warn(msg, data);
    }

    // ==================== Conditional Responses ====================

    /**
     * Build success or error response based on condition
     *
     * @param condition Condition
     * @param successMsg Success message
     * @param failMsg Fail message
     * @return Response object
     */
    public static <T> ErpResponse<T> conditional(boolean condition, String successMsg, String failMsg) {
        return condition ? ErpResponse.ok(successMsg) : ErpResponse.fail(failMsg);
    }

    /**
     * Build success or error response based on result (integer result)
     *
     * @param result Execution result (usually database operation affected rows)
     * @param successMsg Success message
     * @param failMsg Fail message
     * @return Response object
     */
    public static <T> ErpResponse<T> fromResult(int result, String successMsg, String failMsg) {
        return result > 0 ? ErpResponse.ok(successMsg) : ErpResponse.fail(failMsg);
    }

    /**
     * Build response based on whether object is null
     *
     * @param object Object to check
     * @param successMsg Success message
     * @param notFoundMsg Not found message
     * @return Response object
     */
    public static <T> ErpResponse<T> fromObject(Object object, String successMsg, String notFoundMsg) {
        if (object == null) {
            return ErpResponse.fail(notFoundMsg);
        }
        @SuppressWarnings("unchecked")
        ErpResponse<T> response = (ErpResponse<T>) ErpResponse.ok(successMsg, object);
        return response;
    }

    // ==================== Debug Helper Methods ====================

    /**
     * Log response to console
     *
     * @param response Response object
     * @param prefix Log prefix
     */
    public static <T> void logResponse(ErpResponse<T> response, String prefix) {
        if (response.isSuccess()) {
            log.info("{} - Success: {}", prefix, response.getMsg());
        } else if (response.isError()) {
            log.error("{} - Error: {} (code={})", prefix, response.getMsg(), response.getCode());
        } else if (response.isWarn()) {
            log.warn("{} - Warning: {} (code={})", prefix, response.getMsg(), response.getCode());
        }
        
        if (response.getDetailMessage() != null) {
            log.debug("{} - Detail: {}", prefix, response.getDetailMessage());
        }
    }

    /**
     * Validate if response is successful, throw exception if failed
     *
     * @param response Response object
     * @throws RuntimeException If response is failed
     */
    public static <T> void validateSuccessOrThrow(ErpResponse<T> response) {
        if (!response.isSuccess()) {
            throw new RuntimeException(response.getMsg());
        }
    }
}
