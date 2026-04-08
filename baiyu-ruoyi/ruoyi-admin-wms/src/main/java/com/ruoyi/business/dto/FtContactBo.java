package com.ruoyi.business.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ruoyi.business.entity.FtContact;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AutoMapper(target = FtContact.class, reverseConvertGenerate = false)
public class FtContactBo {
    /**
     * 客户ID
     */
    @TableField("id")
    private String id;
}
