package com.ruoyi.business.k3.domain.bo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.ruoyi.business.k3.domain.entity.ReceivebillEntry;
import com.ruoyi.common.mybatis.core.domain.BaseEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ReceivebillEntry.class, reverseConvertGenerate = false)
public class RectunitTypeBo extends BaseEntity {

    @TableId(value = "fid")
    private Long fId;

}
