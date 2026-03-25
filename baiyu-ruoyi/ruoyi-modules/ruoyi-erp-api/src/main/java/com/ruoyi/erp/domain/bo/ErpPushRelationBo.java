package com.ruoyi.erp.domain.bo;

import com.ruoyi.common.mybatis.core.domain.BaseEntity;
import com.ruoyi.erp.domain.entity.ErpPushRelation;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ERP 下推关系配置业务对象 erp_push_relation
 *
 * @author ERP Development Team
 * @date 2026-03-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ErpPushRelation.class, reverseConvertGenerate = false)
public class ErpPushRelationBo extends BaseEntity {

    /**
     * 关系 ID
     */
    private Long relationId;

    /**
     * 源模块编码
     */
    @NotBlank(message = "源模块编码不能为空")
    @Size(min = 0, max = 50, message = "源模块编码长度不能超过{max}个字符")
    private String sourceModule;

    /**
     * 目标模块编码
     */
    @NotBlank(message = "目标模块编码不能为空")
    @Size(min = 0, max = 50, message = "目标模块编码长度不能超过{max}个字符")
    private String targetModule;

    /**
     * 关系名称
     */
    @NotBlank(message = "关系名称不能为空")
    @Size(min = 0, max = 100, message = "关系名称长度不能超过{max}个字符")
    private String relationName;

    /**
     * 字段映射规则 (JSON)
     */
    private String mappingRules;

    /**
     * 数据转换规则 (JSON)
     */
    private String transformationRules;

    /**
     * 数据校验规则 (JSON)
     */
    private String validationRules;

    /**
     * 并发控制策略
     */
    private String concurrencyControl;

    /**
     * 是否启用事务
     */
    private String transactionEnabled;

    /**
     * 状态
     */
    private String status;

}
