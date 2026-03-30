package com.ruoyi.business.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Employee {

    /** 主键ID */
    private Long id;

    /** 金蝶员工主键 */
    private Long fid;

    /** 员工姓名 */
    private String fname;

    /** 编码 */
    private String fnumber;

    /** 备注 */
    private String fdescription;

    /** 创建人 */
    private Long fcreatorId;

    /** 修改人 */
    private Long fmodifierId;

    /** 创建日期 */
    private LocalDateTime fcreateDate;

    /** 修改日期 */
    private LocalDateTime fmodifyDate;

    /** 员工编号 */
    private String fstaffNumber;

    /** 照片 */
    private String fphoto;

    /** 家庭地址 */
    private String faddress;

    /** 移动电话 */
    private String fmobile;

    /** 企业邮箱 */
    private String femail;

    /** 企业电话 */
    private String ftel;

    /** 紧急联系人 */
    private String fJjlxr;

    /** 紧急联系人电话 */
    private String fJjlxrdh;

    /** 身份证号 */
    private String fSfzh;

    /** 出生日期 */
    private LocalDate fOraDate;

    /** 毕业学校 */
    private String fOraText;

    /** 毕业日期 */
    private LocalDate fOraDate1;

    /** 学历 */
    private String fOraCombo;

    /** 企业QQ */
    private String fOraText1;

    /** 性别 */
    private String fXb;

    /** 入职日期 */
    private LocalDate fCtyDate;

    /** 转正日期 */
    private LocalDate fZzrq;

    /** 年龄 */
    private Integer fNl;

    /** 部门 */
    private String fBw;

    /** 户口所在地 */
    private String fHkszd;

    /** 民族 */
    private String fMz;

    /** 离职日期 */
    private LocalDate fLzrq;

    /** 社保日期 */
    private LocalDate fSbrq;

    /** 社保金额 */
    private BigDecimal fSbje;

    /** 代扣养老 */
    private BigDecimal fDkyl;

    /** 代扣医疗 */
    private BigDecimal fDkyliao;

    /** 代扣失业 */
    private BigDecimal fDksy;

    /** 计时单价 */
    private BigDecimal fJsdj;

    /** 劳动合同 */
    private String fLdht;

    /** 保险日期 */
    private LocalDate fBxrq;

    /** 基本工资 */
    private BigDecimal fJbgz;

    /** 岗位补贴 */
    private BigDecimal fGwgz;

    /** 绩效津贴 */
    private BigDecimal fJx;

    /** 英文名 */
    private String fYwm;

    /** 净利提成点 */
    private BigDecimal fJltcd;

    /** 毛利提成点 */
    private BigDecimal fMltcd;

    /** 越南提成点 */
    private BigDecimal fYmtcd;

    /** 保密津贴 */
    private BigDecimal fBmjt;

    /** 新人入职登记表 */
    private String fPeuuImagefileserverQtr;

    /** 劳动合同到期日 */
    private LocalDate fPeuuDate83g;

    /** 毕业证书 */
    private String fPeuuImagefileserverApv;

    /** 学位证书 */
    private String fPeuuImagefileserverTzk;

    /** 英语语言证书 */
    private String fPeuuImagefileserverCa9;

    /** 保密协议 */
    private String fPeuuImagefileserverUky;

    /** 公司底线 */
    private String fPeuuImagefileserverDvn;
    /**岗位名称*/
    private String fbaseProperty;

    /** 专业证书 */
    private String professionalCertificate;

    /** 身份证正面照片 */
    private String fSfzhFront;

    /** 身份证反面照片 */
    private String fSfzhReverseSide;

    /** 销售员/跟单id */
    private String salesmanId;
}
