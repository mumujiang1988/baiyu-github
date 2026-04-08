package com.ruoyi.business.k3.domain.bo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.ruoyi.business.k3.domain.entity.PaymentApplicationForm;
import com.ruoyi.common.mybatis.core.domain.BaseEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = PaymentApplicationForm.class, reverseConvertGenerate = false)
public class PaymentApplicationFormBo extends BaseEntity {
    //实体主键
    @TableId(value = "id")
    private Long id;
}
