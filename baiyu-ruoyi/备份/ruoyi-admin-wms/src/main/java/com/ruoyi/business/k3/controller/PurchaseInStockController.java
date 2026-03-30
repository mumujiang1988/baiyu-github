package com.ruoyi.business.k3.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.entity.PurchaseInStock;
import com.ruoyi.business.k3.service.IPurchaseInStockService;

import com.ruoyi.business.util.Result;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 采购入库单 Controller
 */
@RestController
@RequestMapping("/business/purchaseInStock")
@Slf4j
public class PurchaseInStockController {

    @Autowired
    private IPurchaseInStockService purchaseInStockService;

    /**
     * 查询采购入库单详情
     * @param fid 主键
     * @return 采购入库单详情
     */
    @GetMapping("/{fid}")
    public PurchaseInStock getInfo(@PathVariable String fid) {
        PurchaseInStock purchaseInStock = purchaseInStockService.selectById(fid);
        return purchaseInStock;
    }

    /**
     * 查询采购入库单列表（分页）
     * @param purchaseInStock 查询条件
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 采购入库单列表
     */
    @GetMapping("/list")
    public TableDataInfo<PurchaseInStock> list(PurchaseInStock purchaseInStock,
                                         @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        try {
            TableDataInfo<PurchaseInStock> result = purchaseInStockService.selectList(purchaseInStock, pageNum, pageSize);
            return result;
        } catch (Exception e) {
            log.error("查询采购入库单列表失败", e);
            return new TableDataInfo<>(null, 0L);
        }
    }

    /**
     * 新增采购入库单
     * @param purchaseInStock 采购入库单
     * @return 结果
     */
    @Log(title = "采购入库单", businessType = BusinessType.INSERT)
    @PostMapping
    public Result add(@RequestBody PurchaseInStock purchaseInStock) {
        // 设置创建人
        int result = purchaseInStockService.insert(purchaseInStock);
        if (result > 0) {
            return Result.success("新增成功");
        }
        return Result.error("新增失败");
    }

    /**
     * 修改采购入库单
     * @param purchaseInStock 采购入库单
     * @return 结果
     */
    @Log(title = "采购入库单", businessType = BusinessType.UPDATE)
    @PutMapping
    public Result edit(@RequestBody PurchaseInStock purchaseInStock) {
        // 设置修改人

        int result = purchaseInStockService.update(purchaseInStock);
        if (result > 0) {
            return Result.success("修改成功");
        }
        return Result.error("修改失败");
    }

    /**
     * 删除采购入库单
     * @param fids 主键数组
     * @return 结果
     */
    @Log(title = "采购入库单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{fids}")
    public Result remove(@PathVariable String[] fids) {
        int result = purchaseInStockService.deleteByIds(fids);
        if (result > 0) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 同步金蝶采购入库单数据
     * @return 结果
     */
    @Log(title = "采购入库单同步", businessType = BusinessType.OTHER)
    @PostMapping("/sync")
    public Result syncPurchaseInStock() {
        try {
            // 同步主表数据
           // int masterCount = purchaseInStockService.syncPurchaseInStockData();

            // 同步明细表数据
            int entryCount = purchaseInStockService.syncPurchaseInStockEntryData();

            return Result.success(String.format("采购入库单同步完成，主表：%d 条，明细表：%d 条", 0, entryCount));
        } catch (Exception e) {
            log.error("同步采购入库单失败", e);
            return Result.error("同步失败：" + e.getMessage());
        }
    }

}
