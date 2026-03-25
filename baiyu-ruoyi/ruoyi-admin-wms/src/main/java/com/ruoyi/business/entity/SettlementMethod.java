package com.ruoyi.business.entity;

import lombok.Data;

import java.sql.Timestamp;

/**
 * 结算方式表 实体类。
 */
@Data
public class SettlementMethod {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 客户付款条件
     * */
    private Long fsettleTypeId;

    /**
     * 结算方式编码
     */
    private String code;

    /**
     * 结算方式名称
     */
    private String name;

    /**
     * 结算方式类别
     */
    private String category;

    /**
     * 业务分类
     */
    private String businessType;

    /**
     * 是否支持手续费
     */
    private Boolean supportFee;

    /**
     * 手续费承担方
     */
    private String undertaker;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 创建时间
     */
    private Timestamp createTime;
}
