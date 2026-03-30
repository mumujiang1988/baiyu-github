package com.ruoyi.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**字典表*/
@Data
@TableName(value = "bymaterial_dictionary")
public class BymaterialDictionary {

    @TableId(value = "id")
    private Long id;

    @TableField(value = "name")
    private String code;

    //新老产品
    private String fxlcp;

    //物料属性
    private String erpClsId;

    private String productCategory; // 产品类别

    private String name;

    private String category;
    private String kingdee;

    //客户来源
    private String fKhly;
    //客户币别
    private String ftradingCurrId;
    //收款条件
    private String frecConditionId;
    //客户分组
    private String fgroupId;
    //客户包装方式
    private String fBzfs;
    //客户类别
    private String fcustTypeId;

    //价格类型
    private String FPriceType;

    //采购价目
    private String FCurrencyID;

    //物料编码
    private String FMaterialId;

    //物料名称
    private String FMaterialName;

    //计价单位
    private String FUnitID;

    //单箱单位
    private String FWBZDW;

    private String categoryName;

    private Date createTime;

    private Date updateTime;
}
