package com.ruoyi.business.k3.domain.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ruoyi.business.entity.PriceListEntry;
import com.ruoyi.business.k3.domain.entity.PriceList;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 金蝶采购价目表 - 主表
 * 一对多：主表（PriceList） -> 明细（PriceListEntry）
 */
@Data
@AutoMapper(target = PriceList.class)
public class PriceListVo {

    /** 主键ID，自增 */
    @TableField("id")
    private Long id;

    /**金蝶主键id*/
    private Long priceListId;
    /** 名称（必填） → 对应金蝶字段：FName */
    private String FName;

    /** 编码 → FNumber */
    private String FNumber;

    /** 备注 → FDescription */
    private String FDescription;

    /** 币别（必填） → FCurrencyID */
    private String FCurrencyID;

    /** 供应商 → FSupplierID */
    private String FSupplierID;

    /** 供应商类别 → F_GYSLB */
    private String F_GYSLB;

    /** 定价员 → FPricer */
    private String FPricer;

    /** 价目表对象（必填） → FPriceObject */
    private String FPriceObject;

    /** 价格类型（必填）→ FPriceType */
    private String FPriceType;

    /** 默认价目表 → FDefPriceListId */
    private Long FDefPriceListId;

    /** 备注（另一字段）→ FNote */
    private String FNote;

    private String FDocumentStatus;

    /** 明细列表，一对多关系,不存数据库 */
    // @Column(ignore = true) // MyBatis-Flex 默认不会映射 List 类型，无需标注
    private List<PriceListEntry> entries;

    /** 创建人 */
    private String createdBy;
    /** 创建时间 */
    private Date createdAt;

    /** 更新时间 */
    private Date updatedAt;

}
