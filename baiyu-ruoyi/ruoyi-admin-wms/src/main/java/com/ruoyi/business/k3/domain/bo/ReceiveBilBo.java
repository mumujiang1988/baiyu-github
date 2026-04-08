package com.ruoyi.business.k3.domain.bo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.ruoyi.business.k3.domain.entity.ReceiveBill;
import com.ruoyi.common.mybatis.core.domain.BaseEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ReceiveBill.class, reverseConvertGenerate = false)
public class ReceiveBilBo extends BaseEntity {
    @TableId(value = "fId")
    private Long fId;

}
