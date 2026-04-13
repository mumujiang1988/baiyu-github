package com.ruoyi.erp.domain.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * ERP Low-code Module Standard Response Body
 * <p>
 * Independent response wrapper separate from RuoYi framework, specifically for ERP low-code modules
 * </p>
 *
 * @param <T> Data type
 * @author JMH
 * @date 2026-03-27
 */
@Data
@NoArgsConstructor
public class ErpResponse<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * Success status code
     */
    public static final int SUCCESS = 200;

    /**
     * Failure status code
     */
    public static final int FAIL = 500;

    /**
     * Warning status code
     */
    public static final int WARN = 300;

    /**
     * Status code
     */
    private int code;

    /**
     * Return message
     */
    private String msg;

    /**
     * Detailed error information (optional)
     */
    private String detailMessage;

    /**
     * Return data
     */
    private T data;

    /**
     * Timestamp
     */
    private long timestamp;

    /**
     * Constructor - for internal use
     */
    private ErpResponse(int code, String msg, T data, String detailMessage) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.detailMessage = detailMessage;
        this.timestamp = System.currentTimeMillis();
    }

    // ==================== Success Methods ====================

    /**
     * Return success response (without data)
     *
     * @return Success response
     */
    public static <T> ErpResponse<T> ok() {
        return new ErpResponse<>(SUCCESS, "Operation successful", null, null);
    }

    /**
     * Return success response (with data)
     *
     * @param data Data object
     * @return Success response
     */
    public static <T> ErpResponse<T> ok(T data) {
        return new ErpResponse<>(SUCCESS, "Operation successful", data, null);
    }

    /**
     * Return success response (with message)
     *
     * @param msg Return message
     * @return Success response
     */
    public static <T> ErpResponse<T> ok(String msg) {
        return new ErpResponse<>(SUCCESS, msg, null, null);
    }

    /**
     * Return success response (with message and data)
     *
     * @param msg Return message
     * @param data Data object
     * @return Success response
     */
    public static <T> ErpResponse<T> ok(String msg, T data) {
        return new ErpResponse<>(SUCCESS, msg, data, null);
    }

    // ==================== Failure Methods ====================

    /**
     * Return failure response
     *
     * @return Failure response
     */
    public static <T> ErpResponse<T> fail() {
        return new ErpResponse<>(FAIL, "Operation failed", null, null);
    }

    /**
     * Return failure response (with message)
     *
     * @param msg Return message
     * @return Failure response
     */
    public static <T> ErpResponse<T> fail(String msg) {
        return new ErpResponse<>(FAIL, msg, null, null);
    }

    /**
     * Return failure response (with data and message)
     *
     * @param msg Return message
     * @param data Data object
     * @return Failure response
     */
    public static <T> ErpResponse<T> fail(String msg, T data) {
        return new ErpResponse<>(FAIL, msg, data, null);
    }

    /**
     * Return failure response (with status code and message)
     *
     * @param code Status code
     * @param msg Return message
     * @return Failure response
     */
    public static <T> ErpResponse<T> fail(int code, String msg) {
        return new ErpResponse<>(code, msg, null, null);
    }

    /**
     * Return failure response (with status code, message and detailed error information)
     *
     * @param code Status code
     * @param msg Return message
     * @param detailMessage Detailed error information
     * @return Failure response
     */
    public static <T> ErpResponse<T> fail(int code, String msg, String detailMessage) {
        return new ErpResponse<>(code, msg, null, detailMessage);
    }

    // ==================== Warning Methods ====================

    /**
     * Return warning response
     *
     * @param msg Warning message
     * @return Warning response
     */
    public static <T> ErpResponse<T> warn(String msg) {
        return new ErpResponse<>(WARN, msg, null, null);
    }

    /**
     * Return warning response (with data)
     *
     * @param msg Warning message
     * @param data Data object
     * @return Warning response
     */
    public static <T> ErpResponse<T> warn(String msg, T data) {
        return new ErpResponse<>(WARN, msg, data, null);
    }

    // ==================== Custom Build Methods ====================

    /**
     * Custom success response
     *
     * @param data Data object
     * @param msg Return message
     * @return Success response
     */
    public static <T> ErpResponse<T> success(T data, String msg) {
        return new ErpResponse<>(SUCCESS, msg, data, null);
    }

    /**
     * Custom failure response
     *
     * @param code Status code
     * @param msg Return message
     * @param data Data object
     * @return Failure response
     */
    public static <T> ErpResponse<T> error(int code, String msg, T data) {
        return new ErpResponse<>(code, msg, data, null);
    }

    // ==================== Utility Methods ====================

    /**
     * Check if successful
     *
     * @return true-success, false-failure
     */
    public boolean isSuccess() {
        return this.code == SUCCESS;
    }

    /**
     * Check if failed
     *
     * @return true-failure, false-success
     */
    public boolean isError() {
        return this.code != SUCCESS;
    }

    /**
     * Check if warning
     *
     * @return true-warning, false-not warning
     */
    public boolean isWarn() {
        return this.code == WARN;
    }

    /**
     * Set detailed error information
     *
     * @param detailMessage Detailed error information
     * @return Current response object
     */
    public ErpResponse<T> withDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }

    /**
     * Set timestamp
     *
     * @param timestamp Timestamp
     * @return Current response object
     */
    public ErpResponse<T> withTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }
}
