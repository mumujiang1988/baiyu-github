package com.ruoyi.business.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ruoyi.business.entity.FtCustomer;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AutoMapper(target = FtCustomer.class, reverseConvertGenerate = false)
public class FtCustomerBo {
    /**
     * 客户ID
     */
    @TableField("id")
    private String id;
}
