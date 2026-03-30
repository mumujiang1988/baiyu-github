package com.ruoyi.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

@Data
@JsonSerialize(using = ToStringSerializer.class)
@TableName(value = "dictionary_table")
public class DictionaryTable {

    // DictionaryTable实体类应包含的字段
    private Long id;           // 主键ID
    @TableField("dict_name")
    private String dictName;   // 字典名称
    @TableField("bilhead")
    private String bilhead;   // 金蝶映射表头
    @TableField("dict_code")
    private String dictCode;   // 字典编码
    @TableField("parent_code")
    private String parentCode; // 父级字典编码
    @TableField("createDate")
    private Date createDate;   // 创建日期
    private Date updateDate;   // 修改日期
    private String remark;     // 备注
    @TableField("createBy")
    private Integer status;    // 状态
    private Integer sortOrder; // 排序

    public String getDictName() {
        if (dictName != null) {
            return dictName.replace("_", "");
        }
        return dictName;
    }

    // 如果需要修改实体本身的属性，可以在 setter 中处理
    public void setDictName(String dictName) {
        if (dictName != null) {
            this.dictName = dictName.replace("_", "");
        } else {
            this.dictName = dictName;
        }
    }

}
