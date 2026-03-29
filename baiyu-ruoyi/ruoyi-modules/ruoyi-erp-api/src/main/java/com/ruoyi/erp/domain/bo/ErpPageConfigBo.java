package com.ruoyi.erp.domain.bo;

import com.ruoyi.common.mybatis.core.domain.BaseEntity;
import com.ruoyi.erp.domain.entity.ErpPageConfig;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ERP 公共配置业务对象 erp_page_config
 *
 * @author JMH
 * @date 2026-03-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ErpPageConfig.class, reverseConvertGenerate = false)
public class ErpPageConfigBo extends BaseEntity {

    /**
     * 主键 ID
     */
    private Long configId;

    /**
     * 模块编码 (如 saleOrder/deliveryOrder)
     */
    @NotBlank(message = "模块编码不能为空")
    @Size(min = 0, max = 50, message = "模块编码长度不能超过{max}个字符")
    private String moduleCode;

    /**
     * 配置名称
     */
    @NotBlank(message = "配置名称不能为空")
    @Size(min = 0, max = 100, message = "配置名称长度不能超过{max}个字符")
    private String configName;

    /**
     * 配置类型 (PAGE=页面配置/DICT=字典配置/PUSH=下推配置/APPROVAL=审批配置)
     */
    @NotBlank(message = "配置类型不能为空")
    private String configType;

    /**
     * 页面基础配置 (page.json)
     */
    private String pageConfig;

    /**
     * 表单 UI 组件配置 (form.json)
     */
    private String formConfig;

    /**
     * 表格查询配置 (table.json)
     */
    private String tableConfig;

    /**
     * 查询表单配置 (search.json)
     */
    private String searchConfig;

    /**
     * 按钮操作配置 (action.json)
     */
    private String actionConfig;

    /**
     * API接口配置 (api.json)
     */
    private String apiConfig;

    /**
     * 字典数据源配置 (dict.json)
     */
    private String dictConfig;

    /**
     * 业务规则配置 (config.json)
     */
    private String businessConfig;

    /**
     * 详情页配置 (detail.json)
     */
    private String detailConfig;

    /**
     * 版本号 (每次更新 +1)
     */
    private Integer version;

    /**
     * 状态 (1 正常 0 停用)
     */
    private String status;

    /**
     * 是否公共配置 (1 是 0 否)
     */
    private String isPublic;

    /**
     * 父配置 ID(用于继承)
     */
    private Long parentConfigId;

    /**
     * 备注
     */
    @Size(min = 0, max = 500, message = "备注长度不能超过{max}个字符")
    private String remark;

    /**
     * 变更原因（用于版本更新时记录）
     */
    @Size(min = 0, max = 500, message = "变更原因长度不能超过{max}个字符")
    private String changeReason;

}
