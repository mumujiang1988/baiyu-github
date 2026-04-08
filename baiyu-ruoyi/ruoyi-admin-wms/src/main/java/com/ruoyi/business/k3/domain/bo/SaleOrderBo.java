package com.ruoyi.business.k3.domain.bo;

import com.ruoyi.common.mybatis.core.domain.BaseEntity;
import com.ruoyi.system.domain.entity.SysUser;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysUser.class, reverseConvertGenerate = false)
public class SaleOrderBo extends BaseEntity {

    /** 主键 */
    private Long id;

    /** 单据编号 */
    private String fBillNo;

    /** 客户编码 */
    private String fCustId;

    /** 客户简称 */
    private String fOraBaseProperty;

    /**订单状态*/
    private String orderStatus;



}
