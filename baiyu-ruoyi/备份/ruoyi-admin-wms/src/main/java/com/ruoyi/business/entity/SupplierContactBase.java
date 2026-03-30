package com.ruoyi.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 供应商/客户联系人信息表 实体类基础类。
 *
 * @author yourname
 * @since 2025-11-05
 */
@Data
@TableName(value = "supplier_contact")
public class SupplierContactBase implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 客户id*/
    @TableField(value = "k3_id")
    private Long k3id;

    /**
     * 供应商编码
     */
    private String supplierCode;


    /**
     * 联系人
     */
    private String contactName;

    /**
     * 默认联系人
     */
    private String defaultContact;
    /**
     * 地点名称
     */
    private String locationName;

    /**
     * 性别
     */
    private String gender;

    /**
     * 职务
     */
    private String position;

    /**
     * 电话
     */
    private String phone;

    /**
     * QQ
     */
    private String qq;

    /**
     * 手机
     */
    private String mobile;

    /**
     * 电子邮箱
     */
    private String email;


    /**
     * 通讯地址
     */
    private String address;

    /**
     * 描述
     */
    private String remark;

    /**
     * 类型
     */
    private String type;

    /**
     * 微信
     */
    private String vx;

    /**
     * 客户编码
     */
    private String customerNumber;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 创建日期
     */
    private LocalDateTime creationDate;

}
