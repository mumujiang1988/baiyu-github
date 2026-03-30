package com.ruoyi.business.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class TaxRate {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 金蝶对应编码
     */
    private String k3Code;

    /**
     * 税率（%）
     */
    private BigDecimal taxRate;

    /**
     * 税收制度
     */
    private String taxSystem;

    /**
     * 税种
     */
    private String taxCategory;

    /**
     * 创建时间
     */
    private Date createTime;
}
