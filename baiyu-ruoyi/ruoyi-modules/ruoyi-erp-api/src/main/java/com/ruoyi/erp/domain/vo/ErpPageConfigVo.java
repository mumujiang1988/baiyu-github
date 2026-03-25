package com.ruoyi.erp.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.ruoyi.common.excel.annotation.ExcelDictFormat;
import com.ruoyi.common.excel.convert.ExcelDictConvert;
import com.ruoyi.erp.domain.entity.ErpPageConfig;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ERP 公共配置视图对象 erp_page_config
 *
 * @author ERP Development Team
 * @date 2026-03-22
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = ErpPageConfig.class)
public class ErpPageConfigVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @ExcelProperty(value = "主键 ID")
    private Long configId;

    /**
     * 模块编码
     */
    @ExcelProperty(value = "模块编码")
    private String moduleCode;

    /**
     * 配置名称
     */
    @ExcelProperty(value = "配置名称")
    private String configName;

    /**
     * 配置类型
     */
    @ExcelProperty(value = "配置类型", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "erp_config_type")
    private String configType;

    /**
     * 完整的 JSON 配置内容
     */
    @ExcelProperty(value = "配置内容")
    private String configContent;

    /**
     * 版本号
     */
    @ExcelProperty(value = "版本号")
    private Integer version;

    /**
     * 状态
     */
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_normal_disable")
    private String status;

    /**
     * 是否公共配置
     */
    @ExcelProperty(value = "是否公共", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_yes_no")
    private String isPublic;

    /**
     * 父配置 ID
     */
    @ExcelProperty(value = "父配置 ID")
    private Long parentConfigId;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;

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
