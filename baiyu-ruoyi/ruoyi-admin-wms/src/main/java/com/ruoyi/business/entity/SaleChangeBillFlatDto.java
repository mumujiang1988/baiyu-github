package com.ruoyi.business.entity;

import com.ruoyi.business.entity.SaleChangeDetail;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SaleChangeBillFlatDto {
    /** 主键ID */
    private Long id;

    // ========== 单据头信息 ==========
    /** 单据FID（金蝶实体主键） */
    private Long fid;

    /** 单据编号 */
    private String billNo;

    /** 数据状态 */
    private String documentStatus;//A:新增，B:审核中，C:已审核，D:审核未通过

    /** 单据日期 */
    private LocalDate billDate;

    /** 创建人 */
    private String creatorId;

    /** 创建日期 */
    private LocalDateTime createDate;

    /** 修改人 */
    private String modifierId;

    /** 修改日期 */
    private LocalDateTime modifyDate;

    /** 审核人 */
    private String auditor;

    /** 审核日期 */
    private LocalDateTime auditDate;

    /** 客户简称 */
    private String customerShortName;

    // ========== 系统字段 ==========
    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /**详情表*/
    private List<SaleChangeDetailDto> saleChangeDetails;
}
