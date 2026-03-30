package com.ruoyi.business.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SaleChangeDetailDto {
    /** 主键ID */
    private Long id;

    // ========== 明细信息 ==========

    /** 源单编号 */
    private String sourceBillNo;

    /** 源单类型 */
    private String sourceBillType;

    /** 产品编码 */
    private String productCode;

    /** 产品名称 */
    private String productName;

    /** 规格型号 */
    private String specModel;

    /** 旧供应商 */
    private String oldSupplier;

    /** 新供应商 */
    private String newSupplier;

    /** 变更原因（必填） */
    private String changeReason;

    // ========== 旧数据 ==========
    /** 旧包装要求 */
    private String oldPackageReq;

    /** 旧毛重 */
    private BigDecimal oldGrossWeight;

    /** 旧净重 */
    private BigDecimal oldNetWeight;

    /** 旧长 */
    private BigDecimal oldLength;

    /** 旧宽 */
    private BigDecimal oldWidth;

    /** 旧高 */
    private BigDecimal oldHeight;

    /** 旧装箱数 */
    private Integer oldBoxQty;

    // ========== 新数据 ==========
    /** 新包装要求 */
    private String newPackageReq;

    /** 新毛重 */
    private BigDecimal newGrossWeight;

    /** 新净重 */
    private BigDecimal newNetWeight;

    /** 新长 */
    private BigDecimal newLength;

    /** 新宽 */
    private BigDecimal newWidth;

    /** 新高 */
    private BigDecimal newHeight;

    /** 新装箱数 */
    private Integer newBoxQty;

    // ========== 系统字段 ==========
    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
