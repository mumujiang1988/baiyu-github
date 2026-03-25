package com.ruoyi.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_data_audit_log")
public class SysDataAuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;              // 主键ID

    private String tableName;     // 表名

    private String rowId;         // 被修改数据主键

    private String operator;      // 操作人

    private LocalDateTime operateTime; // 操作时间

    private String ip;            // 操作IP
    private String diffJson;            // 修改的json
}
