package com.ruoyi.business.k3.domain.bo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.ruoyi.business.k3.domain.entity.SalOutboundDetails;
import com.ruoyi.common.mybatis.core.domain.BaseEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SalOutboundDetails.class, reverseConvertGenerate = false)
public class SalOutboundDetailsBo extends BaseEntity {
    @TableId(value = "id")
    private Long id;
}
