package com.ruoyi.business.entity;

import lombok.Data;

import java.util.Date;
/**
 * 公共付款信息
 * */
@Data
public class FinancialInformation {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 供应商编码
     */
    private String supplierNumber;

    /**
     * 国家
     */
    private String nation;

    /**
     * 银行账号
     */
    private String bankAccount;

    /**
     * 账户名称
     */
    private String accountName;

    /**
     * 收款银行
     */
    private String receivingBank;

    /**
     * 开户行地址
     */
    private String bankAddress;

    /**
     * 开户银行
     */
    private String openingBank;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 修改时间
     */
    private Date updatedAt;

    /**
     * 金蝶主键ID
     */
    private Long k3Id;

}
