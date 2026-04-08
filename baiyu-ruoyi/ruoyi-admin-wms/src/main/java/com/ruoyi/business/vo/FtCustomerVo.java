package com.ruoyi.business.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ruoyi.business.entity.*;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.List;

@Data
@AutoMapper(target = FtCustomer.class)
public class FtCustomerVo {

    /**
     * 客户ID
     */
    @TableField("id")
    private String id;

    /**
     * 富通id
     * */
    private String fId;

    /**
     * 客户编码
     */
    private String code;

    /**
     * 客户类型
     */
    private String type;

    /**
     * 客户简称
     */
    private String shortName;

    /**
     * 客户名称
     */
    private String name;
    /**
     * 国家
     * */
    private String region;

    /**
     * 主营产品
     * */
    private String mainProduct;

    /**
     * 客户等级
     * */
    private String grade;

    /**
     * 客户来源
     */
    private String source;

    /**
     * 业务类型
     * */
    private String businessType;

    /**
     * 联系地址
     * */
    private String address;

    /**
     * 审批状态 0:草稿 3:审批完成
     */
    private Integer status;

    /**
     * 审批通过时间
     */
    private String approvalTime;

    /**
     * 是否公海 0:公海; 1:私海
     */
    private Integer belong;

    /**
     * 第三方数据ID
     */
    private String extId;

    /**
     * 客户联系人信息
     * */
    private List<FtContact> contactList;

    /**
     * 客户银行信息
     * */
    private List<Ftbank> bankList;

    /**
     * 客户自定义字段信息
     */
    private List<CustomerCustomize> customerCustomizeList;

    /**
     * 系统字段数据
     */
    private List<BaseField> baseFieldList;

    /**
     * 业务员名称
     */
    private String operator;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 修改时间
     */
    private String updateTime;

    /**
     * 删除标记
     */
    private Integer deleteFlag;

}
