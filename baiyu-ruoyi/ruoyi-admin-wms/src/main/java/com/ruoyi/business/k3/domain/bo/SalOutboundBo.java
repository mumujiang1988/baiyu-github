package com.ruoyi.business.k3.domain.bo;

import com.ruoyi.business.k3.domain.entity.SalOutbound;
import com.ruoyi.common.mybatis.core.domain.BaseEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SalOutbound.class, reverseConvertGenerate = false)
public class SalOutboundBo extends BaseEntity {
    /** 主键ID */
    private Long id;
}
