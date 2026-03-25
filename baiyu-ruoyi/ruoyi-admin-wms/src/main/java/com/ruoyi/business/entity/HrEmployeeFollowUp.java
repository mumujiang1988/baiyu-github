package com.ruoyi.business.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工人事跟进融合表
 * 一条记录 = 一次员工人事跟进事项（沟通 / 调岗 / 薪资 / 转正）
 */
@Data
public class HrEmployeeFollowUp {

    /** 主键 */
    private Long id;

    /** 员工编号 FStaffNumber **/
    private String employeeNumber;

    /** 员工姓名 FName **/
    private String employeeName;

    /** 在职状态 Fzzzt */
    private String status;

    /** 沟通日期 Fgtrq */
    private LocalDate followDate;

    /** 沟通事项 Fgtsx */
    private String followContent;

    /** 改善情况 Fgsqk */
    private String followResult;

    /** 工作调岗 Fgztg */
    private String jobTransfer;

    /** 薪资调整时间 Fxztzsj */
    private LocalDate salaryAdjustTime;

    /** 薪资调整原因 Fxztzyy */
    private String salaryAdjustReason;

    /** 工资调整单旧 Fgztzd */
    private String salaryAdjustDocOld;

    /** 岗位调整单旧 Fgwtzd */
    private String positionAdjustDocOld;

    /** 业务数据表旧 Fywsjb */
    private String businessDataDocOld;

    /** 工资调整单 F_gztzd */
    private String salaryAdjustDoc;

    /** 岗位调整单 F_gwtzd */
    private String positionAdjustDoc;

    /** 业务数据表 F_ywsjb */
    private String businessDataDoc;

    /** 转正通知函 F_zztzh */
    private String regularNoticeDoc;

    /** 沟通跟进表 F_gttzb */
    private String communicationFollowDoc;

    /** 跟进类型：COMM沟通 / TRANSFER调岗 / SALARY薪资 / REGULAR转正 */
    private String eventType;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
