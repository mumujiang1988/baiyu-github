package com.ruoyi.business.k3.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.entity.PoOrderBillHead;
import com.ruoyi.business.entity.ReceiptNoticeFull;
import com.ruoyi.business.k3.config.Dictionaryconfig;
import com.ruoyi.business.k3.service.InspectionBillService;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 检验单Controller接口
 * 用于同步金蝶数据到数据库
 */
@RestController
@RequestMapping("/k3/inspection")
@Slf4j
public class InspectionBillController {


    @Autowired
    private InspectionBillService inspectionBillService;

    /**
     *同步金蝶检验单数据到本地数据库
     *
     * @return同步结果
     */
    @PostMapping("/sync")
    public Result syncInspectionBillMasterData() {
        try {
            log.info("开始同步金蝶检验单数据到本地数据库");
            inspectionBillService.syncInspectionBill();
            return Result.success("检验单数据同步成功");
        } catch (Exception e) {
            log.error("同步检验单数据失败", e);
            return Result.error("同步检验单数据失败: " + e.getMessage());
        }
    }

    /**
     * 新增检验单数据
     *
     * @param receiptNoticeFull 检验单数据
     * @return 新增结果
     */
    @PostMapping("/add")
    public Result addInspectionBill(@RequestBody ReceiptNoticeFull receiptNoticeFull) {
        try {
            log.info("开始新增检验单数据");
            boolean result = inspectionBillService.addInspectionBill(receiptNoticeFull);
            if (result) {
                return Result.success("检验单数据新增成功");
            } else {
                return Result.error("检验单数据新增失败");
            }
        } catch (Exception e) {
            log.error("新增检验单数据失败", e);
            return Result.error("新增检验单数据失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询检验单数据
     *
     * @param current 当前页码
     * @param size 页大小
     * @param receiptNoticeFull 查询条件
     * @return 分页查询结果
     */
    @PostMapping("/page")
    public Result pageInspectionBills(@RequestParam(defaultValue = "1") long current,
                                                              @RequestParam(defaultValue = "10") long size,
                                                              @RequestBody(required = false) ReceiptNoticeFull receiptNoticeFull) {
            log.info("开始分页查询检验单数据，当前页：{},页大小：{}", current, size);
            List<ReceiptNoticeFull> result = inspectionBillService.pageInspectionBills(current, size, receiptNoticeFull);
            return Result.success(result);

    }

    /**
     * 根据 id 查询检验单详情
     * @param id 主键 ID
     * @return 检验单详情
     */
    @GetMapping("/{id}")
    public Result getInspectionBillById(@PathVariable Long id) {
            log.info("开始根据 ID 查询检验单详情，ID: {}", id);
            ReceiptNoticeFull result = inspectionBillService.getInspectionBillById(id);
            if (result != null) {
                return Result.success(result);
            } else {
                return Result.error("未找到该检验单数据");
            }

    }
}
