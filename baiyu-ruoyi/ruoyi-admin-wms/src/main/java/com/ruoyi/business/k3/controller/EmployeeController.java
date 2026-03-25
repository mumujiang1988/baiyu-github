package com.ruoyi.business.k3.controller;

import com.ruoyi.business.entity.Employee;
import com.ruoyi.business.k3.service.EmployeeService;
import com.ruoyi.business.util.Result;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

/**
 * 员工信息接口
 */
@RestController
@RequestMapping("/k3/employee")
@Validated
public class EmployeeController {

    @Resource
    private EmployeeService employeeService;

    /**
     * 同步金蝶员工主表数据到本地数据库
     * @return 操作结果
     */
    @PostMapping("/sync/employee")
    @Transactional(rollbackFor = Exception.class)
    public Result syncEmployeeData() {
        int total = employeeService.syncEmployeeData();
        return Result.success("员工主表同步完成，总计处理：" + total + " 条");
    }

    /**
     * 同步金蝶员工银行信息到本地数据库
     * @return 操作结果
     */
    @PostMapping("/sync/bank")
    @Transactional(rollbackFor = Exception.class)
    public Result syncEmployeeBankData() {
        int total = employeeService.syncEmployeeBankData();
        return Result.success("员工银行信息同步完成，总计处理：" + total + " 条");
    }

    /**
     * 同步金蝶员工跟进信息到本地数据库
     * @return 操作结果
     */
    @PostMapping("/sync/followup")
    @Transactional(rollbackFor = Exception.class)
    public Result syncEmployeeFollowUpData() {
        int total = employeeService.syncEmployeeFollowUpData();
        return Result.success("员工跟进信息同步完成，总计处理：" + total + " 条");
    }

    /**
     * 同步所有员工相关数据（员工主表、银行表、跟进表）
     * @return 操作结果
     */
    @PostMapping("/sync/all")
    @Transactional(rollbackFor = Exception.class)
    public Result syncAllEmployeeData() {
        int total = employeeService.syncAllEmployeeData();
        return Result.success("所有员工数据同步完成，总计处理：" + total + " 条");
    }

    /**
     * 根据ID查询员工
     */
    @GetMapping("/{fid}")
    public Employee getById(@PathVariable("fid") Long fid) {
        return employeeService.getById(fid);
    }
}
