package com.ruoyi.business.bo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.system.domain.entity.SysPost;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@AutoMapper(target = SysPost.class, reverseConvertGenerate = false)
public class BymaterialBo {

    private Long id;
    private String k3Id;
    private String number;           // 编码
    private String formerNumber; // 旧物料编码
    private String name;             // 名称
    private String image;            // 图片
    private String imageName;            // 图片名称
    private String specification;    // 规格型号
    private String appearance;    // 外观描述
    private String description1;     // 原英文描述
    private String description2;    // 英文描述
    private String erpClsId;         // 物料属性
    private String erpName; // 物料属性名称
    private String fxlcp;   // 新老产品
    private String fxlcpName; // 新老产品名称
    private String volume;           // 单个体积
    private String  inspectionReport;       // 验货报告
    private String materialgroup;    // 物料分组
    private String hsbm;           // HS编码
    private String productCategory; // 产品类别
    private String productName;//产品类别名称
    private String creator;          // 创建人
    private String modifier;         // 修改人
    private String  englishProductName;// 英文品名
    private String deliveryState; //交付红线状态(0：常规；1：低风险：2：中风险；3：搞风险)
    private Double score; // 匹配度分数
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime creator_time;     // 创建时间
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime modification_time;


}
