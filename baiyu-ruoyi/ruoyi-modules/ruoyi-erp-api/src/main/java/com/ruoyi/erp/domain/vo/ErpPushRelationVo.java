package com.ruoyi.erp.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.ruoyi.common.excel.annotation.ExcelDictFormat;
import com.ruoyi.common.excel.convert.ExcelDictConvert;
import com.ruoyi.erp.domain.entity.ErpPushRelation;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ERP 下推关系配置视图对象 erp_push_relation
 *
 * @author ERP Development Team
 * @date 2026-03-22
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = ErpPushRelation.class)
public class ErpPushRelationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关系 ID
     */
    @ExcelProperty(value = "关系 ID")
    private Long relationId;

    /**
     * 源模块编码
     */
    @ExcelProperty(value = "源模块编码")
    private String sourceModule;

    /**
     * 目标模块编码
     */
    @ExcelProperty(value = "目标模块编码")
    private String targetModule;

    /**
     * 关系名称
     */
    @ExcelProperty(value = "关系名称")
    private String relationName;

    /**
     * 字段映射规则 (JSON)
     */
    @ExcelProperty(value = "字段映射规则")
    private String mappingRules;

    /**
     * 数据转换规则 (JSON)
     */
    @ExcelProperty(value = "数据转换规则")
    private String transformationRules;

    /**
     * 数据校验规则 (JSON)
     */
    @ExcelProperty(value = "数据校验规则")
    private String validationRules;

    /**
     * 并发控制策略
     */
    @ExcelProperty(value = "并发控制策略")
    private String concurrencyControl;

    /**
     * 是否启用事务
     */
    @ExcelProperty(value = "是否启用事务", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_yes_no")
    private String transactionEnabled;

    /**
     * 状态
     */
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_normal_disable")
    private String status;

    /**
     * 版本号
     */
    @ExcelProperty(value = "版本号")
    private Integer version;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @ExcelProperty(value = "更新时间")
    private LocalDateTime updateTime;

}
