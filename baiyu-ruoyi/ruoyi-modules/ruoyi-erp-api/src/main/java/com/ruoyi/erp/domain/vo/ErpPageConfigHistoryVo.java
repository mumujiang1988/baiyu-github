package com.ruoyi.erp.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.ruoyi.common.excel.annotation.ExcelDictFormat;
import com.ruoyi.common.excel.convert.ExcelDictConvert;
import com.ruoyi.erp.domain.entity.ErpPageConfigHistory;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ERP 配置历史视图对象 erp_page_config_history
 *
 * @author JMH
 * @date 2026-03-22
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = ErpPageConfigHistory.class)
public class ErpPageConfigHistoryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 历史记录 ID
     */
    @ExcelProperty(value = "历史记录 ID")
    private Long historyId;

    /**
     * 配置 ID
     */
    @ExcelProperty(value = "配置 ID")
    private Long configId;

    /**
     * 模块编码
     */
    @ExcelProperty(value = "模块编码")
    private String moduleCode;

    /**
     * 配置类型
     */
    @ExcelProperty(value = "配置类型", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "erp_config_type")
    private String configType;

    /**
     * 版本号
     */
    @ExcelProperty(value = "版本号")
    private Integer version;

    /**
     * 完整的 JSON 配置
     */
    @ExcelProperty(value = "配置内容")
    private String configContent;

    /**
     * 变更原因
     */
    @ExcelProperty(value = "变更原因")
    private String changeReason;

    /**
     * 变更类型
     */
    @ExcelProperty(value = "变更类型", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "erp_change_type")
    private String changeType;

    /**
     * 创建者
     */
    @ExcelProperty(value = "创建者")
    private String createBy;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private LocalDateTime createTime;

}
