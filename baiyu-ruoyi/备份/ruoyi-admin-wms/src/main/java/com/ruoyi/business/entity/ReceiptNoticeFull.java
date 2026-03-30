package com.ruoyi.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 收料通知单（检验单）完整实体（主表+明细表字段合并）
 */
@Data
@TableName("t_receipt_notice_full")
public class ReceiptNoticeFull {

    /**
     * 实体主键
     */
    @TableId(value = "fid", type = IdType.AUTO)
    private Long fid;

    /**
     * 单据编号
     */
    @TableField("fbill_no")
    private String fBillNo;

    /**
     * 单据状态
     */
    @TableField("fdocument_status")
    private String fDocumentStatus;

    /**
     * 审核人
     */
    @TableField("fapprover_id")
    private Long fApproverId;

    /**
     * 审核日期
     */
    @TableField(value = "fapprove_date")
    private Date fApproveDate;

    /**
     * 修改人
     */
    @TableField("fmodifier_id")
    private Long fModifierId;

    /**
     * 创建日期
     */
    @TableField(value = "fcreate_date")
    private Date fCreateDate;

    /**
     * 创建人
     */
    @TableField("fcreator_id")
    private Long fCreatorId;

    /**
     * 修改日期
     */
    @TableField(value = "fmodify_date")
    private Date fModifyDate;

    /**
     * 作废日期
     */
    @TableField(value = "fcancel_date")
    private Date fCancelDate;

    /**
     * 作废人
     */
    @TableField("fcanceler")
    private String fCanceler;

    /**
     * 单据类型 (必填项)
     */
    @TableField("fbill_type_id")
    private String fBillTypeId;

    /**
     * 业务类型 (必填项)
     */
    @TableField("fbusiness_type")
    private String fBusinessType;

    /**
     * 单据日期 (必填项)
     */
    @TableField(value = "fdate")
    private Date fDate;

    // ==================== 明细表字段 ====================

    /**
     * 物料编码 (必填项)
     */
    @TableField("fmaterial_id")
    private String fMaterialId;

    /**
     * 物料名称
     */
    @TableField("fmaterial_name")
    private String fMaterialName;

    /**
     * 规格型号
     */
    @TableField("fmaterial_model")
    private String fMaterialModel;

    /**
     * 质检方案
     */
    @TableField("fqc_scheme_id")
    private String fQcSchemeId;

    /**
     * 单位 (必填项)
     */
    @TableField("funit_id")
    private String fUnitId;

    /**
     * 基本单位
     */
    @TableField("fbase_unit_id")
    private String fBaseUnitId;

    /**
     * 检验数量
     */
    @TableField("finspect_qty")
    private BigDecimal fInspectQty;

    /**
     * 合格数
     */
    @TableField("fqualified_qty")
    private BigDecimal fQualifiedQty;

    /**
     * 不合格数
     */
    @TableField("funqualified_qty")
    private BigDecimal fUnqualifiedQty;

    /**
     * 检验结果 (必填项)
     */
    @TableField("finspect_result")
    private String fInspectResult;

    /**
     * 质检状态 (必填项)
     */
    @TableField("fqc_status")
    private String fQcStatus;

    /**
     * 供应商
     */
    @TableField("fsupplier_id")
    private String fSupplierId;

    /**
     * 仓库
     */
    @TableField("fstock_id")
    private String fStockId;

    /**
     * 批号
     */
    @TableField("flot")
    private String fLot;

    /**
     * 装箱数
     */
    @TableField("fzxs")
    private Integer fzxs;

    /**
     * 毛重
     */
    @TableField("fmz")
    private BigDecimal fmz;

    /**
     * 净重
     */
    @TableField("fjz")
    private BigDecimal fjz;

    /**
     * 外箱长
     */
    @TableField("fchang")
    private BigDecimal fChang;

    /**
     * 外箱宽
     */
    @TableField("fkuan")
    private BigDecimal fKuan;

    /**
     * 外箱高
     */
    @TableField("fgao")
    private BigDecimal fGao;

    /**
     * 总箱数
     */
    @TableField("fzxshu")
    private Integer fZxshu;

    /**
     * 体积
     */
    @TableField("ftj")
    private BigDecimal fTj;

    /**
     * 折让金额
     */
    @TableField("fdiscount_qty")
    private BigDecimal fDiscountQty;

    /**
     * 毛总重
     */
    @TableField("fmzh")
    private BigDecimal fMzh;

    /**
     * 包装要求
     */
    @TableField("fbzyq")
    private String fbzyq;

    /**
     * 特殊要求
     */
    @TableField("ftsyq")
    private String fTsyq;

    /**
     * 产品图片
     */
    @TableField("fcptp")
    private String fcptp;

    /**
     * 销售订单号
     */
    @TableField("fxsddh")
    private String fXsddh;

    /**
     * 采购订单号
     */
    @TableField("fcgddh")
    private String fCgddh;

    /**
     * 客户简称
     */
    @TableField("fkhjc")
    private String fKhjc;

    /**
     * 图片1
     */
    @TableField("f_cty_picture")
    private String fCtyPicture;

    /**
     * 图片2
     */
    @TableField("f_cty_picture1")
    private String fCtyPicture1;

    /**
     * 验收报告
     */
    @TableField("f_peuu_attachment_ca9")
    private String fPeuuAttachmentCa9;

    /**
     * 内盒长
     */
    @TableField("fnhc")
    private BigDecimal fnhc;

    /**
     * 内盒宽
     */
    @TableField("fnhk")
    private BigDecimal fnhk;

    /**
     * 内盒高
     */
    @TableField("fnhg")
    private BigDecimal fnhg;
}
