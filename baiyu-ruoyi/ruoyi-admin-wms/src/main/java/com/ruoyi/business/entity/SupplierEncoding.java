package com.ruoyi.business.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

@Data
@TableName(value = "supplier_encoding") // 修改为实际的表名
public class SupplierEncoding {
    private Long id;

    @TableId(value = "number")
    private String number;

    /**供应商分组*/
    @ApiModelProperty(value = "供应商分组")
    @TableField(value = "supplier_group")
    private String supplierGroup;

    @ExcelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date creatorTime;     // 创建时间
}
