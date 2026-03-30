package com.ruoyi.erp.utils;

import com.ruoyi.erp.domain.response.ErpResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * ERP 响应工具类
 * 
 * 提供便捷的响应构建方法
 * 
 * @author JMH
 * @date 2026-03-27
 */
@Slf4j
public class ErpResponseUtils {

    private ErpResponseUtils() {
        throw new IllegalStateException("Utility class");
    }

    // ==================== 成功响应 ====================

    /**
     * 构建成功响应（无数据）
     *
     * @return 成功响应
     */
    public static <T> ErpResponse<T> success() {
        return ErpResponse.ok();
    }

    /**
     * 构建成功响应（带数据）
     *
     * @param data 数据对象
     * @return 成功响应
     */
    public static <T> ErpResponse<T> success(T data) {
        return ErpResponse.ok(data);
    }

    /**
     * 构建成功响应（带消息和数据）
     *
     * @param msg 消息
     * @param data 数据对象
     * @return 成功响应
     */
    public static <T> ErpResponse<T> success(String msg, T data) {
        return ErpResponse.ok(msg, data);
    }

    /**
     * 构建成功响应（带自定义消息）
     *
     * @param msg 消息
     * @return 成功响应
     */
    public static <T> ErpResponse<T> successWithMessage(String msg) {
        return ErpResponse.ok(msg);
    }

    // ==================== 失败响应 ====================

    /**
     * 构建失败响应（默认消息）
     *
     * @return 失败响应
     */
    public static <T> ErpResponse<T> error() {
        return ErpResponse.fail();
    }

    /**
     * 构建失败响应（带消息）
     *
     * @param msg 错误消息
     * @return 失败响应
     */
    public static <T> ErpResponse<T> error(String msg) {
        return ErpResponse.fail(msg);
    }

    /**
     * 构建失败响应（带状态码和消息）
     *
     * @param code 状态码
     * @param msg 错误消息
     * @return 失败响应
     */
    public static <T> ErpResponse<T> error(int code, String msg) {
        return ErpResponse.fail(code, msg);
    }

    /**
     * 构建失败响应（带状态码、消息和详细错误信息）
     *
     * @param code 状态码
     * @param msg 错误消息
     * @param detailMsg 详细错误信息
     * @return 失败响应
     */
    public static <T> ErpResponse<T> error(int code, String msg, String detailMsg) {
        return ErpResponse.fail(code, msg, detailMsg);
    }

    /**
     * 构建失败响应（从异常对象）
     *
     * @param e 异常对象
     * @return 失败响应
     */
    public static <T> ErpResponse<T> errorFromException(Exception e) {
        log.error("操作失败", e);
        return ErpResponse.fail(500, "操作失败：" + e.getMessage(), e.getClass().getName());
    }

    /**
     * 构建失败响应（从异常对象，带自定义消息）
     *
     * @param e 异常对象
     * @param customMsg 自定义消息
     * @return 失败响应
     */
    public static <T> ErpResponse<T> errorFromException(Exception e, String customMsg) {
        log.error("{}: {}", customMsg, e.getMessage(), e);
        return ErpResponse.fail(500, customMsg + ": " + e.getMessage());
    }

    // ==================== 警告响应 ====================

    /**
     * 构建警告响应
     *
     * @param msg 警告消息
     * @return 警告响应
     */
    public static <T> ErpResponse<T> warn(String msg) {
        return ErpResponse.warn(msg);
    }

    /**
     * 构建警告响应（带数据）
     *
     * @param msg 警告消息
     * @param data 数据对象
     * @return 警告响应
     */
    public static <T> ErpResponse<T> warn(String msg, T data) {
        return ErpResponse.warn(msg, data);
    }

    // ==================== 条件响应 ====================

    /**
     * 根据条件返回成功或失败响应
     *
     * @param condition 条件
     * @param successMsg 成功消息
     * @param failMsg 失败消息
     * @return 响应对象
     */
    public static <T> ErpResponse<T> conditional(boolean condition, String successMsg, String failMsg) {
        return condition ? ErpResponse.ok(successMsg) : ErpResponse.fail(failMsg);
    }

    /**
     * 根据结果返回成功或失败响应（基于整数结果）
     *
     * @param result 执行结果（通常指数据库操作影响的行数）
     * @param successMsg 成功消息
     * @param failMsg 失败消息
     * @return 响应对象
     */
    public static <T> ErpResponse<T> fromResult(int result, String successMsg, String failMsg) {
        return result > 0 ? ErpResponse.ok(successMsg) : ErpResponse.fail(failMsg);
    }

    /**
     * 根据对象是否为空返回响应
     *
     * @param object 待检查的对象
     * @param successMsg 成功消息
     * @param notFoundMsg 未找到对象时的消息
     * @return 响应对象
     */
    public static <T> ErpResponse<T> fromObject(Object object, String successMsg, String notFoundMsg) {
        if (object == null) {
            return ErpResponse.fail(notFoundMsg);
        }
        @SuppressWarnings("unchecked")
        ErpResponse<T> response = (ErpResponse<T>) ErpResponse.ok(successMsg, object);
        return response;
    }

    // ==================== 调试辅助方法 ====================

    /**
     * 打印响应信息到日志
     *
     * @param response 响应对象
     * @param prefix 日志前缀
     */
    public static <T> void logResponse(ErpResponse<T> response, String prefix) {
        if (response.isSuccess()) {
            log.info("{} - 成功：{}", prefix, response.getMsg());
        } else if (response.isError()) {
            log.error("{} - 失败：{} (code={})", prefix, response.getMsg(), response.getCode());
        } else if (response.isWarn()) {
            log.warn("{} - 警告：{} (code={})", prefix, response.getMsg(), response.getCode());
        }
        
        if (response.getDetailMessage() != null) {
            log.debug("{} - 详细信息：{}", prefix, response.getDetailMessage());
        }
    }

    /**
     * 验证响应是否成功，如果失败则抛出异常
     *
     * @param response 响应对象
     * @throws RuntimeException 如果响应失败
     */
    public static <T> void validateSuccessOrThrow(ErpResponse<T> response) {
        if (!response.isSuccess()) {
            throw new RuntimeException(response.getMsg());
        }
    }
}
