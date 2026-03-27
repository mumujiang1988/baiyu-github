package com.ruoyi.erp.domain.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * ERP 低代码模块标准响应信息主体
 * <p>
 * 独立于 RuoYi 框架的返回封装，专用于 ERP 低代码模块
 * </p>
 *
 * @param <T> 数据类型
 * @author JMH
 * @date 2026-03-27
 */
@Data
@NoArgsConstructor
public class ErpResponse<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 成功状态码
     */
    public static final int SUCCESS = 200;

    /**
     * 失败状态码
     */
    public static final int FAIL = 500;

    /**
     * 警告状态码
     */
    public static final int WARN = 300;

    /**
     * 状态码
     */
    private int code;

    /**
     * 返回消息
     */
    private String msg;

    /**
     * 详细错误信息（可选）
     */
    private String detailMessage;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 时间戳
     */
    private long timestamp;

    /**
     * 构造方法 - 内部使用
     */
    private ErpResponse(int code, String msg, T data, String detailMessage) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.detailMessage = detailMessage;
        this.timestamp = System.currentTimeMillis();
    }

    // ==================== 成功方法 ====================

    /**
     * 返回成功响应（无数据）
     *
     * @return 成功响应
     */
    public static <T> ErpResponse<T> ok() {
        return new ErpResponse<>(SUCCESS, "操作成功", null, null);
    }

    /**
     * 返回成功响应（带数据）
     *
     * @param data 数据对象
     * @return 成功响应
     */
    public static <T> ErpResponse<T> ok(T data) {
        return new ErpResponse<>(SUCCESS, "操作成功", data, null);
    }

    /**
     * 返回成功响应（带消息）
     *
     * @param msg 返回消息
     * @return 成功响应
     */
    public static <T> ErpResponse<T> ok(String msg) {
        return new ErpResponse<>(SUCCESS, msg, null, null);
    }

    /**
     * 返回成功响应（带消息和数据）
     *
     * @param msg 返回消息
     * @param data 数据对象
     * @return 成功响应
     */
    public static <T> ErpResponse<T> ok(String msg, T data) {
        return new ErpResponse<>(SUCCESS, msg, data, null);
    }

    // ==================== 失败方法 ====================

    /**
     * 返回失败响应
     *
     * @return 失败响应
     */
    public static <T> ErpResponse<T> fail() {
        return new ErpResponse<>(FAIL, "操作失败", null, null);
    }

    /**
     * 返回失败响应（带消息）
     *
     * @param msg 返回消息
     * @return 失败响应
     */
    public static <T> ErpResponse<T> fail(String msg) {
        return new ErpResponse<>(FAIL, msg, null, null);
    }

    /**
     * 返回失败响应（带数据和消息）
     *
     * @param msg 返回消息
     * @param data 数据对象
     * @return 失败响应
     */
    public static <T> ErpResponse<T> fail(String msg, T data) {
        return new ErpResponse<>(FAIL, msg, data, null);
    }

    /**
     * 返回失败响应（带状态码和消息）
     *
     * @param code 状态码
     * @param msg 返回消息
     * @return 失败响应
     */
    public static <T> ErpResponse<T> fail(int code, String msg) {
        return new ErpResponse<>(code, msg, null, null);
    }

    /**
     * 返回失败响应（带状态码、消息和详细错误信息）
     *
     * @param code 状态码
     * @param msg 返回消息
     * @param detailMessage 详细错误信息
     * @return 失败响应
     */
    public static <T> ErpResponse<T> fail(int code, String msg, String detailMessage) {
        return new ErpResponse<>(code, msg, null, detailMessage);
    }

    // ==================== 警告方法 ====================

    /**
     * 返回警告响应
     *
     * @param msg 警告消息
     * @return 警告响应
     */
    public static <T> ErpResponse<T> warn(String msg) {
        return new ErpResponse<>(WARN, msg, null, null);
    }

    /**
     * 返回警告响应（带数据）
     *
     * @param msg 警告消息
     * @param data 数据对象
     * @return 警告响应
     */
    public static <T> ErpResponse<T> warn(String msg, T data) {
        return new ErpResponse<>(WARN, msg, data, null);
    }

    // ==================== 自定义构建方法 ====================

    /**
     * 自定义成功响应
     *
     * @param data 数据对象
     * @param msg 返回消息
     * @return 成功响应
     */
    public static <T> ErpResponse<T> success(T data, String msg) {
        return new ErpResponse<>(SUCCESS, msg, data, null);
    }

    /**
     * 自定义失败响应
     *
     * @param code 状态码
     * @param msg 返回消息
     * @param data 数据对象
     * @return 失败响应
     */
    public static <T> ErpResponse<T> error(int code, String msg, T data) {
        return new ErpResponse<>(code, msg, data, null);
    }

    // ==================== 工具方法 ====================

    /**
     * 判断是否成功
     *
     * @return true-成功，false-失败
     */
    public boolean isSuccess() {
        return this.code == SUCCESS;
    }

    /**
     * 判断是否失败
     *
     * @return true-失败，false-成功
     */
    public boolean isError() {
        return this.code != SUCCESS;
    }

    /**
     * 判断是否为警告
     *
     * @return true-警告，false-非警告
     */
    public boolean isWarn() {
        return this.code == WARN;
    }

    /**
     * 设置详细错误信息
     *
     * @param detailMessage 详细错误信息
     * @return 当前响应对象
     */
    public ErpResponse<T> withDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }

    /**
     * 设置时间戳
     *
     * @param timestamp 时间戳
     * @return 当前响应对象
     */
    public ErpResponse<T> withTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }
}
