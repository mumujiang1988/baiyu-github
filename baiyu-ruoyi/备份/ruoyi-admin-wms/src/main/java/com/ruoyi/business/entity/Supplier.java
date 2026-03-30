package com.ruoyi.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;


@Data
public class Supplier {

    /**
     * 主键ID
     */
    private Long id;
    /**金蝶主键ID
     * */
    private String supplierid;

    /**
     * 编码
     */
    private String number;

    /**
     * 名称
     */
    private String name;

    /**默认联系人*/
    private String fdefaultContactId;
    /**
     * 简称
     */
    private String abbreviation;

    /**
     * 国家
     */
    private String nation;

    /**
     * 地区
     */
    private String region;

    /**
     * 通讯地址
     */
    private String address;

    /**
     * 法人代表
     */
    private String legalPerson;

    /**
     * 成立日期
     */
    // 添加日期格式化注解
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date establishDate;

    /**
     * 国外占比（%）
     */
    private String foreignShare;

    /**
     * 负责人
     */
    @ApiModelProperty(value = "负责人")
    @TableField(value = "manager")
    private String manager;

    /**
     * 供应商分类
     */
    @ApiModelProperty(value = "供应商分类")
    @TableField(value = "supplier_category")
    private String supplierCategory;

    /**供应商分组*/
    @ApiModelProperty(value = "供应商分组")
    @TableField(value = "supplier_group")
    private String supplierGroup;

    /** 供应商分组名称 */
    @TableField(select = false,exist = false)
    private String groupName;

    /**
     * 供应类别
     */
    @ApiModelProperty(value = "供应类别")
    @TableField(value = "supply_type")
    private String supplyType;

    /**
     * 主营产品
     */
    @ApiModelProperty(value = "主营产品")
    @TableField(value = "main_product")
    private String mainProduct;

    /**
     * 工商登记号
     */
    @ApiModelProperty(value = "工商登记号")
    @TableField(value = "business_registration")
    private String businessRegistration;

    /**
     * 统一社会信用代码
     */
    private String socialCreditCode;

    /**
     * 来源
     */
    @ApiModelProperty(value = "来源")
    @TableField(value = "source")
    private String source;
    /**
     * 新增原因
     * */
    @ApiModelProperty(value = "新增原因")
    @TableField(value = "cause")
    private String cause;

    /**
     * 营业执照
     */
    private String businessLicense;

    /**
     * 开票品名
     */
    @ApiModelProperty(value = "开票品名")
    @TableField(value = "invoice_name")
    private String invoiceName;

    /**
     * 定制评估
     */
    @ApiModelProperty(value = "定制评估")
    @TableField(value = "customization")
    private String customization;

    /**
     * 年度营业额
     */
    @ApiModelProperty(value = "年度营业额")
    @TableField(value = "turnover")
    private String turnover;


    /**
     * 工厂人数
     */
    @ApiModelProperty(value = "工厂人数")
    @TableField(value = "factory_people")
    private String factoryPeople;

    /**
     * 老板为人
     */
    private String behave;

    /**
     * 工厂问题
     */
    private String contactInfo;

    /**
     * 跟单反馈
     */
    @ApiModelProperty(value = "跟单反馈")
    @TableField(value = "follow_up_feedback")
    private String followUpFeedback;

    /**
     * 结算币别
     */
    @ApiModelProperty(value = "结算币别")
    @TableField(value = "settlement_currency")
    private String settlementCurrency;

    /**
     * 工厂定位
     */
    private String factoryPositioning;

    /**
     * 品控
     */
    private String qualityControl;

    /**
     * 工厂设备
     */
    private String factoryEquipment;

    /**
     * 厂房面积
     */
    private String factoryArea;

    /**
     * 工厂认证
     */
    private String factoryCertification;

    /**
     * 外销市场区域
     */
    private String exportMarket;

    /**
     * 结算方式
     */
    private String settlementMethod;

    /**
     * 付款条件
     */
    private String paymentTerms;

    /**
     * 发票类型
     */
    private String invoiceType;

    /**
     * 税分类
     */
    private String taxCategory;

    /**
     * 结算方
     */
    private String settlementParty;

    /**
     * 收款方
     */
    private String payee;

    /**
     * 默认税率（%）
     */
    private String defaultTaxRate;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建日期
     */
    private Date createdAt;
    /**审核人*/

    private String auditor;
    /**审核时间*/
    private String auditTime;
    /**
     * 修改人
     */
    private String updatedBy;

    /**优先排款*/
    private String priorityPayment;
    /**
     * 修改日期
     */
    private Date updatedAt;
    /**
     * 供应商付款信息
     * */
    private List<FinancialInformation> financialInformation;

    @TableField(select = false,exist = false)
    private String bankAccount;

    @TableField(select = false,exist = false)
    private String receivingBank;

    /**
     * 供应商联系人信息
     * */
    private List<SupplierContactBase> contactInformation;

    /**
     * 回访信息
     * */
    private List<SupplierVisitRecord> supplierVisitRecord;

    // import java.util.Map;

   /* private Map<String, AiLlmModer> moderMap;*/ // key: 字段名, value: 对应 AiLlmModer

}
