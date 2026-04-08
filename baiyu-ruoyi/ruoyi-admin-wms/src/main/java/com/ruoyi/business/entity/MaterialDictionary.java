package com.ruoyi.business.entity;

import lombok.Data;

@Data
public class MaterialDictionary {
    //物料编码
    private String FMaterialId;

    //物料名称
    private String FMaterialName;

    /** 老产品 */
    private String fctyBaseProperty1;

    /** 规划格型号 */
    private String specification;

    /**英文品名*/
    private String FctyBaseProperty;



}
