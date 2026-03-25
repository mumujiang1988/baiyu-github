package com.ruoyi.business.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
@Data
@JsonSerialize(using = ToStringSerializer.class)
@TableName(value = "supplier_groups")
public class SupplierGroups {
    private Long id;
    private Long supplierGroup;
    private String encoding;
    private Integer parentId;
    private Integer groupPing;  // 修正字段名
    private String groupName;
    private String delFlag;
}
