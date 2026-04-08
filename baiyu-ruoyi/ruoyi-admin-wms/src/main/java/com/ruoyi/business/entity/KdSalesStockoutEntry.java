package com.ruoyi.business.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 金蝶销售出库单明细表 t_kd_sales_stockout_entry
 *
 * @author aiflowy
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_kd_sales_stockout_entry")
@ExcelIgnoreUnannotated
public class KdSalesStockoutEntry extends BaseEntity {

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
     * 客户货号
     */
    @ExcelProperty(value = "客户货号")
    private String fKhhh;

    /**
     * 是否报关
     */
    @ExcelProperty(value = "是否报关")
    private String fSfbg;

    /**
     * 客户物料编码
     */
    @ExcelProperty(value = "客户物料编码")
    private String fCustMatID;

    /**
     * 客户物料名称
     */
    @ExcelProperty(value = "客户物料名称")
    private String fCustMatName;

    /**
     * 物料编码 (必填项)
     */
    @ExcelProperty(value = "物料编码")
    private String fMaterialID;

    /**
     * 物料名称
     */
    @ExcelProperty(value = "物料名称")
    private String fMaterialName;

    /**
     * 规格型号
     */
    @ExcelProperty(value = "规格型号")
    private String fMateriaModel;

    /**
     * 物料创建日期
     */
    @ExcelProperty(value = "物料创建日期")
    private LocalDateTime fCtyBaseProperty1;

    /**
     * 应发数量
     */
    @ExcelProperty(value = "应发数量")
    private BigDecimal fMustQty;

    /**
     * 实发数量
     */
    @ExcelProperty(value = "实发数量")
    private BigDecimal fRealQty;

    /**
     * 单价
     */
    @ExcelProperty(value = "单价")
    private BigDecimal fPrice;

    /**
     * 含税单价
     */
    @ExcelProperty(value = "含税单价")
    private BigDecimal fTaxPrice;

    /**
     * 是否赠品
     */
    @ExcelProperty(value = "是否赠品")
    private String fIsFree;

    /**
     * 批号
     */
    @ExcelProperty(value = "批号")
    private String fLot;

    /**
     * 销售订单单号
     */
    @ExcelProperty(value = "销售订单单号")
    private String fSoorDerno;

    /**
     * 采购合同号
     */
    @ExcelProperty(value = "采购合同号")
    private String fCghth;

    /**
     * 价税合计
     */
    @ExcelProperty(value = "价税合计")
    private BigDecimal fAllAmount;

    /**
     * 仓库
     */
    @ExcelProperty(value = "仓库")
    private String fStockID;

    /**
     * 库存状态
     */
    @ExcelProperty(value = "库存状态")
    private String fStockStatusID;

    /**
     * 质量类型
     */
    @ExcelProperty(value = "质量类型")
    private String fQualifyType;

    /**
     * 计划跟踪号
     */
    @ExcelProperty(value = "计划跟踪号")
    private String fMtoNo;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String fEntrynote;

    /**
     * 累计收款金额
     */
    @ExcelProperty(value = "累计收款金额")
    private BigDecimal fSumReceivedAMT;

    /**
     * 长
     */
    @ExcelProperty(value = "长")
    private BigDecimal fC;

    /**
     * 宽
     */
    @ExcelProperty(value = "宽")
    private BigDecimal fK;

    /**
     * 高
     */
    @ExcelProperty(value = "高")
    private BigDecimal fG;

    /**
     * 体积
     */
    @ExcelProperty(value = "体积")
    private BigDecimal fTj;

    /**
     * 毛重
     */
    @ExcelProperty(value = "毛重")
    private BigDecimal fMz;

    /**
     * 净重
     */
    @ExcelProperty(value = "净重")
    private BigDecimal fJz;

    /**
     * 毛总重
     */
    @ExcelProperty(value = "毛总重")
    private BigDecimal fMzz;

    /**
     * 箱数
     */
    @ExcelProperty(value = "箱数")
    private BigDecimal fXs;

    /**
     * 装箱数
     */
    @ExcelProperty(value = "装箱数")
    private BigDecimal fZxs;

    /**
     * 供应商
     */
    @ExcelProperty(value = "供应商")
    private String fGys;

    /**
     * 采购单价
     */
    @ExcelProperty(value = "采购单价")
    private BigDecimal fCgdj;

    /**
     * 采购总价
     */
    @ExcelProperty(value = "采购总价")
    private BigDecimal fCgzj;

    /**
     * 成本价
     */
    @ExcelProperty(value = "成本价")
    private BigDecimal fJdj;

    /**
     * 成本总价
     */
    @ExcelProperty(value = "成本总价")
    private BigDecimal fJdzj;

    /**
     * 发票单价
     */
    @ExcelProperty(value = "发票单价")
    private BigDecimal fFpdj;

    /**
     * 发票总价
     */
    @ExcelProperty(value = "发票总价")
    private BigDecimal fFpzj;

    /**
     * 中文报关品名
     */
    @ExcelProperty(value = "中文报关品名")
    private String fZwbgpm;

    /**
     * 英文报关品名
     */
    @ExcelProperty(value = "英文报关品名")
    private String fYwbgpm;

    /**
     * 报关数量
     */
    @ExcelProperty(value = "报关数量")
    private BigDecimal fBgsl;

    /**
     * 报关状态
     */
    @ExcelProperty(value = "报关状态")
    private String fBGZT;

    /**
     * 货运结算数量
     */
    @ExcelProperty(value = "货运结算数量")
    private BigDecimal fHYJSSL;

    /**
     * 订单含税单价
     */
    @ExcelProperty(value = "订单含税单价")
    private BigDecimal fDdhsdj;

    /**
     * 退税率%
     */
    @ExcelProperty(value = "退税率%")
    private BigDecimal fTsl;

    /**
     * 订单审核日期
     */
    @ExcelProperty(value = "订单审核日期")
    private LocalDateTime fddshrq;

    /**
     * 出库周期
     */
    @ExcelProperty(value = "出库周期")
    private Integer fckzq;

    /**
     * 制单日期
     */
    @ExcelProperty(value = "制单日期")
    private LocalDateTime fzdrq;

    /**
     * 包材工厂提供
     */
    @ExcelProperty(value = "包材工厂提供")
    private String fbcgctg;

    /**
     * 包材有库存
     */
    @ExcelProperty(value = "包材有库存")
    private String fbcykc;

    /**
     * 样品单
     */
    @ExcelProperty(value = "样品单")
    private String fypd;

    /**
     * 新老产品
     */
    @ExcelProperty(value = "新老产品")
    private String fPEUUBaseProperty83g;

}
