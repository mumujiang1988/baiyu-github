package com.ruoyi.business.k3.controller;


import com.ruoyi.business.entity.FinancialInformation;
import com.ruoyi.business.k3.service.FinancialInformationService;
import com.ruoyi.business.util.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 供应商付款信息 控制层。
 */
@RestController
@RequestMapping("/k3/financialInformation")
public class FinancialInformationController {

    @Resource
    private FinancialInformationService financialInformationService;

    /**
     * 新增供应商付款信息
     *
     * @param financialInformation 付款信息
     * @return 操作结果
     */
    @PostMapping("/save")
    public Result save(@RequestBody FinancialInformation financialInformation) {
        int result = financialInformationService.insert(financialInformation);
        if (result>0) {
            return Result.success("新增成功");
        } else {
            return Result.error("物料添加失败: ");
        }
    }





    /**
     * 根据供应商编码查询所有付款信息
     *
     * @param supplierNumber 供应商编码
     * @return 付款信息列表
     */
    @GetMapping("/listBySupplier")
    public Result listBySupplier(@RequestParam String supplierNumber) {
        List<FinancialInformation> financialInfoList = financialInformationService.listBySupplierNumber(supplierNumber);
        return Result.success(financialInfoList);
    }


}
