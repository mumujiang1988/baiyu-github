package com.ruoyi.erp.domain.bo;

import com.ruoyi.common.mybatis.core.domain.BaseEntity;
import com.ruoyi.erp.domain.entity.ErpPageConfig;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ERP Page Configuration Business Object erp_page_config
 *
 * @author JMH
 * @date 2026-03-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ErpPageConfig.class, reverseConvertGenerate = false)
public class ErpPageConfigBo extends BaseEntity {

    /**
     * Primary key ID
     */
    private Long configId;

    /**
     * Module code (e.g., saleOrder/deliveryOrder)
     */
    @NotBlank(message = "Module code cannot be empty")
    @Size(min = 0, max = 50, message = "Module code length cannot exceed {max} characters")
    private String moduleCode;

    /**
     * Configuration name
     */
    @NotBlank(message = "Configuration name cannot be empty")
    @Size(min = 0, max = 100, message = "Configuration name length cannot exceed {max} characters")
    private String configName;

    /**
     * Configuration type (PAGE=page configuration/DICT=dictionary configuration/PUSH=push configuration/APPROVAL=approval configuration)
     */
    @NotBlank(message = "Configuration type cannot be empty")
    private String configType;

    /**
     * Page base configuration (page.json)
     */
    private String pageConfig;

    /**
     * Form UI component configuration (form.json)
     */
    private String formConfig;

    /**
     * Table query configuration (table.json)
     */
    private String tableConfig;

    /**
     * Search form configuration (search.json)
     */
    private String searchConfig;

    /**
     * Button operation configuration (action.json)
     */
    private String actionConfig;

    /**
     * API interface configuration (api.json)
     */
    private String apiConfig;

    /**
     * Dictionary data source configuration (dict.json)
     */
    private String dictConfig;

    /**
     * Business rule configuration (config.json)
     */
    private String businessConfig;

    /**
     * Detail page configuration (detail.json)
     */
    private String detailConfig;

    /**
     * Version number (incremented by 1 on each update)
     */
    private Integer version;

    /**
     * Status (1 active 0 inactive)
     */
    private String status;

    /**
     * Is public configuration (1 yes 0 no)
     */
    private String isPublic;

    /**
     * Parent configuration ID (for inheritance)
     */
    private Long parentConfigId;

    /**
     * Remark
     */
    @Size(min = 0, max = 500, message = "Remark length cannot exceed {max} characters")
    private String remark;

    /**
     * Change reason (used to record during version updates)
     */
    @Size(min = 0, max = 500, message = "Change reason length cannot exceed {max} characters")
    private String changeReason;

}
