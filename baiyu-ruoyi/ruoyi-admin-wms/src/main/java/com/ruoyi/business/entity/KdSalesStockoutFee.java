package com.ruoyi.business.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 金蝶销售出库单费用明细表 t_kd_sales_stockout_fee
 *
 * @author aiflowy
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_kd_sales_stockout_fee")
@ExcelIgnoreUnannotated
public class KdSalesStockoutFee extends BaseEntity {

    /**
     * 实体主键
     */
    @ExcelProperty(value = "实体主键")
    @TableId(value = "FEntryID")
    private String fentryID;

    /**
     * 主表 ID
     */
    @ExcelProperty(value = "主表 ID")
    private String fid;

    /**
     * 费用 (旧)
     */
    @ExcelProperty(value = "费用 (旧)")
    private String fOraEntity;

    /**
     * 费用项目
     */
    @ExcelProperty(value = "费用项目")
    private String fFyxm;

    /**
     * 金额
     */
    @ExcelProperty(value = "金额")
    private BigDecimal fJe;

    /**
     * 关联付款金额
     */
    @ExcelProperty(value = "关联付款金额")
    private BigDecimal fGlfkje;

    /**
     * 付款状态
     */
    @ExcelProperty(value = "付款状态")
    private String fBillStatus;

    /**
     * 承担方式
     */
    @ExcelProperty(value = "承担方式")
    private String fCdfs;

    /**
     * 业务员
     */
    @ExcelProperty(value = "业务员")
    private String fYwy;

    /**
     * 供应商
     */
    @ExcelProperty(value = "供应商")
    private String fGys;

    /**
     * 申请付款金额
     */
    @ExcelProperty(value = "申请付款金额")
    private BigDecimal fSqfkje;

    /**
     * 申请付款状态
     */
    @ExcelProperty(value = "申请付款状态")
    private String fsqfkzz;

}
