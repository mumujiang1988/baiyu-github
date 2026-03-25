package com.ruoyi.business.dto;

import lombok.Data;

/**
 * 国家订单分布数据传输对象
 */
@Data
public class CountryOrderDistributionDTO {

    /**
     * 国家名称（中文）
     */
    private String countryName;

    /**
     * 国家名称（英文）
     */
    private String countryNameEn;

    /**
     * 订单数量
     */
    private Integer orderCount;

    public CountryOrderDistributionDTO() {}

    public CountryOrderDistributionDTO(String countryName, String countryNameEn, Integer orderCount) {
        this.countryName = countryName;
        this.countryNameEn = countryNameEn;
        this.orderCount = orderCount;
    }

    public CountryOrderDistributionDTO(String countryName, String countryNameEn, Integer orderCount, Integer totalOrderCount) {
        this.countryName = countryName;
        this.countryNameEn = countryNameEn;
        this.orderCount = orderCount;
    }
}
