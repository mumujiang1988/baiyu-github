package com.ruoyi.business.entity;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 富通系统客户实体
 */
@Data
public class FtCustomer {

    /**
     * 客户ID
     */
    private String id;

    /**
     * 客户编码
     */
    private String code;

    /**
     * 客户名称
     */
    private String name;

    /**
     * 客户简称
     */
    private String shortName;

    /**
     * 客户类型
     */
    private String type;

    /**
     * 客户来源
     */
    private String source;

    /**
     * 业务员名称
     */
    private String operator;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 修改时间
     */
    private String updateTime;

    /**
     * 审批状态 0:草稿 3:审批完成
     */
    private Integer status;

    /**
     * 审批通过时间
     */
    private String approvalTime;

    /**
     * 是否公海 0:公海; 1:私海
     */
    private Integer belong;

    /**
     * 第三方数据ID
     */
    private String extId;

    /**
     * 自定义字段数据
     */
    private Map<String, Object> customizeFields;

    /**
     * 系统字段数据
     */
    private Map<String, Object> baseFields;

    /**
     * 删除标记
     */
    private Integer deleteFlag;
}
