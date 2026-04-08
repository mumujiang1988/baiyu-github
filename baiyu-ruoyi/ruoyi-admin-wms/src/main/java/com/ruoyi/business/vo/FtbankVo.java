package com.ruoyi.business.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ruoyi.business.entity.CustomerBankCustomize;
import com.ruoyi.business.entity.Ftbank;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.List;

@Data
@AutoMapper(target = Ftbank.class)
public class FtbankVo {
    @TableField("id")
    private Long id;
    /**
     * 富通id
     * */
    private String fId;

    /**
     * 银行名称
     * */
    private String name;
    /**
     * 银行账号
     * */
    private String account;
    /**
     * 银行代码
     **/
    private String code;
    /**
     * 分支机构代码
     * */
    private String branchCode;
    /**
     * 币种
     * */
    private String currency;
    /**
     * 银行swift代码
     * */
    private String swiftCode;
    /**
     * 银行地址
     * */
    private String address;
    /**
     * 状态 0:隐藏; 1:显示
     * */
    private String flag;
    /**
     *
     * 税号
     * */
    private String taxNumber;
    /**
     * 客户银行自定义字段
     * */
    private List<CustomerBankCustomize> customerBankCustomizeList;

}
