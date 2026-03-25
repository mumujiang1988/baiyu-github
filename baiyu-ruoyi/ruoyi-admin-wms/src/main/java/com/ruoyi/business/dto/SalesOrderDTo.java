package com.ruoyi.business.dto;


import lombok.Data;

import java.util.List;

@Data
public class SalesOrderDTo {

    /**订单总数*/
    private Integer totalOrderCount;
    /**上月订单数*/
    private Integer lastMonthOrderCount;

    /**年份*/
    private Integer year;
    /**月份*/
    private Integer month;
    /**国家*/
    private List<CountryOrderDistributionDTO> country;
    /**销售员*/
    private List<SalesRankingDTO> province;
}
