package com.ruoyi.business.k3.service;

import com.ruoyi.business.entity.Employee;

/**
 * 员工服务接口
 */
public interface EmployeeService {

    /**
     * 同步金蝶员工数据到本地数据库
     * @return 处理的记录总数
     */
    int syncEmployeeData();

    /**
     * 同步金蝶员工银行信息到本地数据库
     * @return 处理的记录总数
     */
    int syncEmployeeBankData();

    /**
     * 同步金蝶员工跟进信息到本地数据库
     * @return 处理的记录总数
     */
    int syncEmployeeFollowUpData();

    /**
     * 同步所有员工相关数据（员工主表、银行表、跟进表）
     * @return 处理的记录总数
     */
    int syncAllEmployeeData();

    /**
     * 根据主键ID查询员工
     */
    Employee getById(Long fid);
}
