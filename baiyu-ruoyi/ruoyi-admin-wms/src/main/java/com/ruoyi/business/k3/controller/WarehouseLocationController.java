package com.ruoyi.business.k3.controller;

import com.ruoyi.business.entity.WarehouseLocation;
import com.ruoyi.business.k3.service.WarehouseLocationService;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 仓库仓位 Controller
 */
@RestController
@RequestMapping("/business/warehouseLocation")
@Slf4j
public class WarehouseLocationController {

    @Autowired
    private WarehouseLocationService warehouseLocationService;

    /**
     * 查询仓库仓位详情
     * @param id 主键
     * @return 仓库仓位详情
     */
    @GetMapping("/{id}")
    public WarehouseLocation getInfo(@PathVariable Long id) {
        return warehouseLocationService.selectById(id);
    }

    /**
     * 查询仓库仓位列表（分页）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 仓库仓位列表
     */
    @GetMapping("/list")
    public TableDataInfo<WarehouseLocation> list(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        try {
            return warehouseLocationService.selectList(pageNum, pageSize);
        } catch (Exception e) {
            log.error("查询仓库仓位列表失败", e);
            return new TableDataInfo<>(null, 0L);
        }
    }

    /**
     * 新增仓库仓位
     * @param warehouseLocation 仓库仓位
     * @return 结果
     */
    @Log(title = "仓库仓位", businessType = BusinessType.INSERT)
    @PostMapping
    public Result add(@RequestBody WarehouseLocation warehouseLocation) {
        int result = warehouseLocationService.insert(warehouseLocation);
        if (result > 0) {
            return Result.success("新增成功");
        }
        return Result.error("新增失败");
    }

    /**
     * 修改仓库仓位
     * @param warehouseLocation 仓库仓位
     * @return 结果
     */
    @Log(title = "仓库仓位", businessType = BusinessType.UPDATE)
    @PutMapping
    public Result edit(@RequestBody WarehouseLocation warehouseLocation) {
        int result = warehouseLocationService.update(warehouseLocation);
        if (result > 0) {
            return Result.success("修改成功");
        }
        return Result.error("修改失败");
    }

    /**
     * 删除仓库仓位
     * @param ids 主键数组
     * @return 结果
     */
    @Log(title = "仓库仓位", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public Result remove(@PathVariable Long[] ids) {
        int result = warehouseLocationService.deleteByIds(ids);
        if (result > 0) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 同步金蝶仓库仓位数据
     * @return 结果
     */
    @Log(title = "仓库仓位同步", businessType = BusinessType.OTHER)
    @PostMapping("/sync")
    public Result syncWarehouseLocation() {
        try {
            int count = warehouseLocationService.syncWarehouseLocationData();
            return Result.success(String.format("仓库仓位同步完成，总计处理：%d 条", count));
        } catch (Exception e) {
            log.error("同步仓库仓位失败", e);
            return Result.error("同步失败：" + e.getMessage());
        }
    }
}
