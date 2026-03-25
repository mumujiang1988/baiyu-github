package com.ruoyi.business.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ruoyi.system.domain.entity.SysUser;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户主数据表（bd_customer）
 * 对应金蝶 K3 Cloud 客户主档
 */
@Data
@TableName("bd_customer")
public class Customer {


    /** 主键ID */
    @TableId("Id")
    private Long Id;

    /** 客户ID（金蝶主键 FCUSTID） */
    @TableField("fcustid")
    private Long fcustid;

    /** 客户编码（FNumber） */
    @TableField("fnumber")
    private String fnumber;

    /** 客户名称（FName，必填） */
    @TableField("fname")
    private String fname;

    /** 客户简称（FShortName，必填） */
    @TableField("fshort_name")
    private String fshortName;

    /** 客户类别（FName） */
    @TableField("fcustTypeId")
    private String fcustTypeId;
    /** 客户全称 */
    @TableField("f_khqc")
    private String fKhqc;

    /** 单据状态 */
    @TableField("fdocumentStatus")
    private String fdocumentStatus;
    /**正唛1*/
    @TableField("fZmmttp1")
    private String fZmmttp1;
    /**正唛2*/
    @TableField("fZmmttp2")
    private String fZmmttp2;
    /**正唛描述*/
    @TableField("fZmmttpMs")
    private String fZmmttpMs;

    /** 正唛唛头外箱描述 */
    @TableField("zmmtwx")
    private String zmmtwx;

    /** 正唛唛头内箱描述 */
    @TableField("zmmtnx")
    private String zmmtnx;

    /** 测唛*/
    @TableField("fcmmttp1")
    private String fcmmttp1;
    /** 测唛2*/
    @TableField("fcmmttp2")
    private String fcmmttp2;
    /** 测唛描述*/
    @TableField("fCmmttpMs")
    private String fCmmttpMs;

    /** 侧唛唛头外箱 */
    @TableField("fcmmtwx")
    private String fcmmtwx;

    /** 侧唛唛头内箱 */
    @TableField("fcmmtnx")
    private String fcmmtnx;

    /** 备注 */
    @TableField("fdescription")
    private String fdescription;

    /** 创建组织（必填） */
    @TableField("fcreate_org_id")
    private Long fcreateOrgId;

    /** 创建人 */
    @TableField("fcreator_id")
    private String fcreatorId;

    /** 审核人 */
    @TableField("fapprover_id")
    private String fapproverId;
    /** 审核日期 */
    @TableField("fapperober_date")
    private LocalDate fapperoberDate;
    /** 修改人 */
    @TableField("fmodifier_id")
    private String fmodifierId;

    /** 销售员（必填） */
    @TableField("fseller")
    private String fseller;

    /** 开发销售员 */
    @TableField("f_kfxsy1")
    @JsonProperty("fKfxsy1")
    private String fKfxsy1;

    /** 销售部门 */
    @TableField("fsal_dept_id")
    private Long fsalDeptId;

    private String fsalDeptName;

    /** 销售组 */
    @TableField("fsal_group_id")
    private String fsalGroupId;

    /** 创建日期 */
    @TableField("fcreate_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fcreateDate;

    /** 修改日期 */
    @TableField("fmodify_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fmodifyDate;

    /** 创立日期 */
    @TableField("ffound_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate ffoundDate;

    /** 客户转让日期 */
    @TableField("f_khzrrq")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fKhzrrq;

    /** 国家（必填） */
    @TableField("fcountry")
    private String fcountry;

    /** 抵运国家 */
    @TableField("f_dygj")
    private String fdygj;
    /** 地区 / 省份 */
    @TableField("fprovincial")
    private String fprovincial;

    /** 通讯地址 */
    @TableField("faddress")
    @JsonProperty("faddress")
    private String faddress;

    /** 注册地址 */
    @TableField("fregister_address")
    private String fregisterAddress;

    /** 联系电话 */
    @TableField("ftel")
    private String ftel;

    /** 客户邮箱 */
    @TableField("f_khyx")
    @JsonProperty("fKhyx")
    private String fKhyx;

    /** 公司网址 */
    @TableField("fwebsite")
    private String fwebsite;

    /** 结算币别（必填） */
    @TableField("ftrading_curr_id")
    private String ftradingCurrId;

    /** 收款币别 */
    @TableField("freceive_curr_id")
    private String freceiveCurrId;

    /** 结算方式 */
    @TableField("fsettle_type_id")
    private Long fsettleTypeId;

    /** 收款条件（必填） */
    @TableField("frec_condition_id")
    private String frecConditionId;

    /** 价目表 */
    @TableField("fprice_list_id")
    private Long fpriceListId;

    /** 税分类 */
    @TableField("ftax_type")
    private Long ftaxType;

    /** 默认税率 */
    @TableField("ftax_rate")
    private BigDecimal ftaxRate;

    /** 纳税登记号 */
    @TableField("ftax_register_code")
    private String ftaxRegisterCode;

    /** 客户分组（必填） */
    @TableField("fgroup_id")
    private String fgroupId;

    /** 客户来源（必填） */
    @TableField("f_khly")
    private String fKhly;

    /** 来源 */
    @TableField("f_ly")
    private String fLy;

    /** 商业类型 */
    @TableField("f_sylx")
    private String fSylx;

    /** 客户规模 */
    @TableField("f_khgm")
    @JsonProperty("fKhgm")
    private String fKhgm;

    /** 客户主营 */
    @TableField("f_khzy")
    @JsonProperty("fKhzy")
    private String fKhzy;

    /** 是否集团客户 */
    @TableField("fis_group")
    private Integer fisGroup;

    /** 是否默认付款方 */
    @TableField("fis_def_payer")
    private Integer fisDefPayer;

    /** 法人代表 */
    @TableField("flegal_person")
    @JsonProperty("flegalPerson")
    private String flegalPerson;


    /** 发票类型 */
    @TableField("finvoice_type")
    private String finvoiceType;

    /** 对应供应商 */
    @TableField("fsupplier_id")
    private Long fsupplierId;

    /** 包装要求 */
    @TableField("f_bzyq")
    @JsonProperty("fBzyq")
    private String fBzyq;

    /** 包装方式（必填） */
    @TableField("fbzfs")
    private String fBzfs;

    /** 发货要求 */
    @TableField("f_fhyq")
    @JsonProperty("fFhyq")
    private String fFhyq;

    /** 质量标准和技术要求 */
    @TableField("f_zlbzhjsyq")
    private String fZlbzhjsyq;

    /** 是否有授权书 */
    @TableField("f_sfysqs")
    private Integer fSfysqs;

    /** 商标授权 */
    @TableField("fsbsq")
    private String fsbsq;

    /** 是否首单 */
    @TableField("fsfsd")
    private String fsfsd;

    /** 是否特殊客户 */
    @TableField("fsfts")
    private Integer fsfts;

    /** 提成比例 */
    @TableField("f_cty_decimal")
    @JsonProperty("fCtyDecimal")
    private BigDecimal fCtyDecimal;

    /** 提成分配方案 */
    @TableField("f_tcfpfa")
    private String fTcfpfa;

    /** 平均收款周期（天） */
    @TableField("fpjskzq")
    private Integer fpjskzq;

    /** 历年毛净利率 % */
    @TableField("f_mjll")
    @JsonProperty("fMjll")
    private BigDecimal fMjll;

    /** 公司简介 */
    @TableField("f_cty_large_text")
    private String fCtyLargeText;

    /** 客户 LOGO */
    @TableField("f_khlogo")
    private String fKhlogo;

    /** 订货平台管理员编码 */
    @TableField("fcp_admin_code")
    private String fcpAdminCode;

    /** Youtube */
    @TableField("f_youtube")
    @JsonProperty("fYoutube")
    private String fYoutube;

    /** LinkedIn */
    @TableField("f_linkedin")
    @JsonProperty("fLinkedin")
    private String fLinkedin;

    /** Facebook */
    @TableField("f_facebook")
    @JsonProperty("fFacebook")
    private String fFacebook;

    /** Twitter */
    @TableField("f_twitter")
    @JsonProperty("fTwitter")
    private String fTwitter;

    /** Instagram */
    @TableField("f_instagram")
    @JsonProperty("fInstagram")
    private String fInstagram;

    /** VK */
    @TableField("f_vk")
    @JsonProperty("fVk")
    private String fVk;

    /** Facebook message */
    @TableField("f_facebookmess")
    private String fFacebookmess;

    /** Skype */
    @TableField("f_skype")
    private String fSkype;

    /** WhatsApp */
    @TableField("f_whatsapp")
    private String fWhatsapp;

    /** WeChat */
    @TableField("fwechat")
    private String fwechat;

    /** QQ */
    @TableField("f_qq")
    private String fQq;

    /** Telegram Messenger */
    @TableField("ftn")
    private String ftn;

    /** Yolo */
    @TableField("f_yolo")
    private String fYolo;

    /** Hangouts */
    @TableField("f_hangouts")
    private String fHangouts;

    /** Viber */
    @TableField("f_viber")
    private String fViber;

    /** 用户信息 */
    @TableField(exist = false)
    private SysUser user;

    /** 客户银行卡信息 */
    private FinancialInformation informationList;
    /**客户转让记录*/
    private List<CustomerTransfer> customerTransfer;
    /**供应商联系人*/
    private List<SupplierContactBase> supplierContactList;


}
