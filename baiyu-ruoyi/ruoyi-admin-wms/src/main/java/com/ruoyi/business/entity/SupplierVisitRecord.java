package com.ruoyi.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 供应商回访记录表实体
 * 对应表：supplier_visit_record
 */
@Data
public class SupplierVisitRecord {

    /** 主键ID */
    private Long id;

    /** 供应商ID（关联 supplier.number） */
    private String supplierNumber;

    /** 回访时间 */
    private String visitTime;

    /** 拜访人 / 回访人员 */
    private String visitor;

    /** 回访内容说明 */
    private String visitContent;

    /** 附件（如：回访报告、照片压缩包等，多个可逗号分隔或JSON） */
    private String attachment;

    /** 创建人 */
    private String createdBy;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /**临时字段*/
    @TableField(select = false,exist = false)
    private String attachmentFileName;
    @TableField(select = false,exist = false)
    private String tempId;
}
