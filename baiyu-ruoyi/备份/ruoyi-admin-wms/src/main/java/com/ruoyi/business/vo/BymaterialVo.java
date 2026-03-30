package com.ruoyi.business.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.ruoyi.business.entity.Bymaterial;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;

@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Bymaterial.class)
public class BymaterialVo {
    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "编码*")
    @TableField(value = "number")
    private String number;           // 编码

    @ExcelProperty(value = "旧物料编码")
    @TableField(value = "former_number")
    private String formerNumber; // 旧物料编码

    @ExcelProperty(value = "名称*")
    @TableField(value = "name")
    private String name;             // 名称

    @ExcelProperty(value = "图片地址")
    private String image;            // 图片

    private String imageName;            // 图片名称

    @ExcelProperty(value = "规格型号")
    @TableField(value = "specification")
    private String specification;    // 规格型号

    @ExcelProperty(value = "原英文描述")
    @TableField(value = "description1")
    private String description1;     // 原英文描述

    @ExcelProperty(value = "英文描述")
    @TableField(value = "description2")
    private String description2;    // 英文描述

    @ExcelProperty(value = "物料属性*")
    @TableField(value = "erpClsId")
    private String erpClsId;         // 物料属性

    @ExcelProperty(value = "新老产品*")
    @TableField(value = "fxlcp")  // 明确指定列名
    private String fxlcp;   // 新老产品

    @ExcelProperty(value = "单个体积")
    @TableField(value = "volume")
    private String volume;           // 单个体积


    @ExcelProperty(value = "物料分组*")
    @TableField(value = "materialgroup")
    private String materialgroup;    // 物料分组

    @TableField(value = "hsbm")
    private String hsbm;           // HS编码

    @ExcelProperty(value = "产品类别*")
    @TableField(value = "product_category")  // 明确指定列名
    private String productCategory; // 产品类别

    @ExcelProperty(value = "英文品名")
    private String  englishProductName;// 英文品名

    @ExcelProperty(value = "交期红线")
    private String deliveryState; //交付红线状态(0：常规；1：低风险：2：中风险；3：搞风险)


}
