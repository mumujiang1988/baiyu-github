package com.ruoyi.business.dto;

import lombok.Data;

/**
 * 销售员月度销售订单排行榜数据传输对象
 */
@Data
public class SalesRankingDTO {

    /**
     * 销售员姓名
     */
    private String salerName;

    /**
     * 订单数量
     */
    private Integer orderCount;

    public SalesRankingDTO() {}

    public SalesRankingDTO(String salerName, Integer orderCount) {
        this.salerName = salerName;
        this.orderCount = orderCount;
    }
}