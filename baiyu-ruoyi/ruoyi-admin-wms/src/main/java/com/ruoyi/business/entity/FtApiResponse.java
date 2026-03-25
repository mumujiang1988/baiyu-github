package com.ruoyi.business.entity;

import lombok.Data;

/**
 * 富通系统API通用响应实体
 */
@Data
public class FtApiResponse<T> {

    /**
     * 返回码
     */
    private String code;

    /**
     * 数据
     */
    private T data;

    /**
     * 请求状态
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String message;

    public boolean isSuccess() {
        return success != null && success;
    }
}
