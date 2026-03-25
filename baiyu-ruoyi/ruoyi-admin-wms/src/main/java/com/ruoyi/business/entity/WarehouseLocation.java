package com.ruoyi.business.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 仓库仓位信息
 */
@Data
public class WarehouseLocation {

    /** 主键 ID */
    private Long id;

    /** 仓库 ID */
    private String warehouseId;

    /** 仓库名称 */
    private String warehouseName;
    /** 仓库编号 */
    private String warehouseNumber;

    /** 仓位 ID */
    private String locationId;

    /** 仓位名称 */
    private String locationName;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
