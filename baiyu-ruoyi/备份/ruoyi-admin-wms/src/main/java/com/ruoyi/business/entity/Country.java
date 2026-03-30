package com.ruoyi.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "country")
public class Country {

    private String id;

    //供应商-国家
    private String nation;

    //客户-国家
    private String fcountry;

    //客户-抵运国家
    private String fdygj;

    //国家英文名称
    private String nameEn;

    //国家英文名称
    private String nameZh;

    private Integer status;

}
