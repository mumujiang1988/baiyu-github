package com.ruoyi.erp.domain.bo;

import com.ruoyi.common.mybatis.core.domain.BaseEntity;
import com.ruoyi.erp.domain.entity.ErpPageConfigHistory;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ERP Configuration History Business Object erp_page_config_history
 *
 * @author JMH
 * @date 2026-03-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ErpPageConfigHistory.class, reverseConvertGenerate = false)
public class ErpPageConfigHistoryBo extends BaseEntity {

    /**
     * History record ID
     */
    private Long historyId;

    /**
     * Configuration ID
     */
    @NotBlank(message = "Configuration ID cannot be empty")
    private Long configId;

    /**
     * Module code
     */
    @NotBlank(message = "Module code cannot be empty")
    @Size(min = 0, max = 50, message = "Module code length cannot exceed {max} characters")
    private String moduleCode;

    /**
     * Configuration type
     */
    @NotBlank(message = "Configuration type cannot be empty")
    private String configType;

    /**
     * Version number
     */
    @NotBlank(message = "Version number cannot be empty")
    private Integer version;

    /**
     * Complete JSON configuration
     */
    @NotBlank(message = "Configuration content cannot be empty")
    private String configContent;

    /**
     * Change reason
     */
    @Size(min = 0, max = 500, message = "Change reason length cannot exceed {max} characters")
    private String changeReason;

    /**
     * Change type
     */
    @NotBlank(message = "Change type cannot be empty")
    private String changeType;

}
