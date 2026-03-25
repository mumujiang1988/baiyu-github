package com.ruoyi.business.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CustomerTransfer {

    /** 客户编码（主键） */
    private String customerCode;

    /** 转让人 */
    @JsonProperty("fZrr")
    private String fZrr;

    /** 接收人 */
    @JsonProperty("fJsr")
    private String fJsr;

    /** 转让日期 */
    private LocalDate fZrrq;

    /** 提成比例 */
    @JsonProperty("fTcbl")
    private BigDecimal fTcbl;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
