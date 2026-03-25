package com.ruoyi.erp.domain.bo;

import com.ruoyi.common.mybatis.core.domain.BaseEntity;
import com.ruoyi.erp.domain.entity.ErpPageConfigHistory;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ERP 配置历史业务对象 erp_page_config_history
 *
 * @author ERP Development Team
 * @date 2026-03-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ErpPageConfigHistory.class, reverseConvertGenerate = false)
public class ErpPageConfigHistoryBo extends BaseEntity {

    /**
     * 历史记录 ID
     */
    private Long historyId;

    /**
     * 配置 ID
     */
    @NotBlank(message = "配置 ID 不能为空")
    private Long configId;

    /**
     * 模块编码
     */
    @NotBlank(message = "模块编码不能为空")
    @Size(min = 0, max = 50, message = "模块编码长度不能超过{max}个字符")
    private String moduleCode;

    /**
     * 配置类型
     */
    @NotBlank(message = "配置类型不能为空")
    private String configType;

    /**
     * 版本号
     */
    @NotBlank(message = "版本号不能为空")
    private Integer version;

    /**
     * 完整的 JSON 配置
     */
    @NotBlank(message = "配置内容不能为空")
    private String configContent;

    /**
     * 变更原因
     */
    @Size(min = 0, max = 500, message = "变更原因长度不能超过{max}个字符")
    private String changeReason;

    /**
     * 变更类型
     */
    @NotBlank(message = "变更类型不能为空")
    private String changeType;

}
