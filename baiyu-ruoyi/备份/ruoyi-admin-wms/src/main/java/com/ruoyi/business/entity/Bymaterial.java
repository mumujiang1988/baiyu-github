package com.ruoyi.business.entity;


import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.business.dto.PriceListDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "by_material") // 修改为实际的表名
public class Bymaterial {

    private Long id;

    @TableId(value = "k3_id")
    private String k3Id;

    @ExcelProperty(value = "编码")
    @TableField(value = "number")
    private String number;           // 编码

    @ExcelProperty(value = "名称")
    @TableField(value = "name")
    private String name;             // 名称

    @ExcelProperty(value = "图片地址")
    private String image;            // 图片

    private String imageName;            // 图片名称

    /**数据状态*/
    @TableField(value = "f_state")
    private String  fstate;


    /**禁用人*/
    @TableField(value = "fstate_name")
    private String  fstateName;

    /**禁用时间*/
    @TableField(value = "fstateTime")
    private String  fstateTime;

    @ExcelProperty(value = "规格型号")
    @TableField(value = "specification")
    private String specification;    // 规格型号

    @ExcelProperty(value = "原英文描述")
    @TableField(value = "description1")
    private String description1;     // 原英文描述

    @ExcelProperty(value = "包材发往供应商")
    @TableField(value = "bcfwgys")
    private String bcfwgys;     // 原英文描述

    @ExcelProperty(value = "英文描述")
    @TableField(value = "description2")
    private String description2;    // 英文描述


    /** 客户编码（FNumber） */
    @TableField("fnumber")
    private String fnumber;

    @ExcelProperty(value = "物料属性")
    @TableField(value = "erpClsId")
    private String erpClsId;         // 物料属性

    @TableField(select = false,exist = false)
    private String erpName; // 物料属性名称

    @ExcelProperty(value = "新老产品")
    @TableField(value = "fxlcp")  // 明确指定列名
    private String fxlcp;   // 新老产品

    @TableField(select = false,exist = false)
    private String fxlcpName; // 新老产品名称

    @ExcelProperty(value = "单个体积")
    @TableField(value = "volume")
    private String volume;           // 单个体积

    @TableField(value = "inspection_report")
    private String  inspectionReport;       // 验货报告

    @ExcelProperty(value = "物料分组")
    @TableField(value = "materialgroup")
    private String materialgroup;    // 物料分组

    @TableField(value = "hsbm")
    private String hsbm;           // HS编码

    @ExcelProperty(value = "产品类别")
    @TableField(value = "product_category")  // 明确指定列名
    private String productCategory; // 产品类别

    @TableField(select = false,exist = false)
    private String productName;//产品类别名称

    @ExcelProperty(value = "创建人")
    private String creator;          // 创建人

    private String modifier;         // 修改人

    @ExcelProperty(value = "英文品名")
    @TableField(value = "english_product_name")
    private String  englishProductName;// 英文品名

    @ExcelProperty(value = "交期红线")
    @TableField(value = "delivery_state")
    private String deliveryState; //交付红线状态(1：常规；2：低风险：3：中风险；4：搞风险)
    @ExcelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime creator_time;     // 创建时间
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime modification_time; // 修改时间
    @ExcelProperty(value = "旧物料编码")
    @TableField(value = "former_number")
    private String formerNumber; // 旧物料编码


    // 供应商列表
    private List<PriceListDTO> priceListDTOS;
}
