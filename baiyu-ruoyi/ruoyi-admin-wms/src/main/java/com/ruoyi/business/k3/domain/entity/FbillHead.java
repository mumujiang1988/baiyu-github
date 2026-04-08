package com.ruoyi.business.k3.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("f_billhead")
public class FbillHead extends BaseEntity {

    @TableId(value = "id")
    private Long id;

    // k3主键id
    private Long fId;

    //单据编号
    private String fBillNo;

    //状态(A：创建，B：审核中；C：已审核；D：重新审核；Z：暂存)
    private String fDocumentStatus;

    //日期 (必填项)
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fDate;

    //采购组织(必填项
    private String fPurchaseOrgId;

    //名称 (必填项)
    private String fName;

    // 描述
    private String fDescription;

    // 调价原因 (必填项)
    private String fPaReason;

    //使用组织
    private String fUseOrgId;

    //禁用
    private String fForbidStatus;

    //审核人
    private String fApproverId;

    // 审核日期
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fApproveDate;

    //采购调价明细表
    @TableField(exist = false)
    private List<FFpurPatentry> patentries;

    //禁用人
    private String fforbiderId;

    //禁用日期
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fforbidDate;

    //生效状态
    private String feffectivestatus;

    //生效人
    private String feffectiveuserid;


}
