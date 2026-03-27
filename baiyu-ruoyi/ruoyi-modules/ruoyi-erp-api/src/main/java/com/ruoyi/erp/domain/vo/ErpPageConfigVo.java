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
 * @author JMH
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
     * 页面基础配置 (page.json)
     */
    @ExcelProperty(value = "页面配置")
    private String pageConfig;

    /**
     * 表单 UI 组件配置 (form.json)
     */
    @ExcelProperty(value = "表单配置")
    private String formConfig;

    /**
     * 表格查询配置 (table.json)
     */
    @ExcelProperty(value = "表格配置")
    private String tableConfig;

    /**
     * 查询表单配置 (search.json)
     */
    @ExcelProperty(value = "搜索配置")
    private String searchConfig;

    /**
     * 按钮操作配置 (action.json)
     */
    @ExcelProperty(value = "按钮配置")
    private String actionConfig;

    /**
     * 字典数据源配置 (dict.json)
     */
    @ExcelProperty(value = "字典配置")
    private String dictConfig;

    /**
     * 业务规则配置 (config.json)
     */
    @ExcelProperty(value = "业务配置")
    private String businessConfig;

    /**
     * 详情页配置 (detail.json)
     */
    @ExcelProperty(value = "详情配置")
    private String detailConfig;

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
