package com.ruoyi.business.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ruoyi.business.entity.Ftbank;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AutoMapper(target = Ftbank.class, reverseConvertGenerate = false)
public class FtbankBo {
    @TableField("id")
    private Long id;
}
