package com.ruoyi.business.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class BymaterialDto {
    private String number;           // 编码
    private String name;             // 名称

    private MultipartFile image;            // 图片
    private String specification;    // 规格型号
    private String description1;     // 英文描述
    private String erpClsId;         // 物料属性
    private String describe;         // 描述
    private String fxlcp;   // 新老产品
    private String volume;           // 单个体积
    private MultipartFile  inspection_report;       // 验货报告
    private String materialgroup;    // 物料分组
    private String hsbm;           // HS编码
    private String product_category; // 产品类别
    private String creator;          // 创建人
    private String modifier;         // 修改人
    private String creator_time;     // 创建时间
    private String modification_time; // 修改时间
}
