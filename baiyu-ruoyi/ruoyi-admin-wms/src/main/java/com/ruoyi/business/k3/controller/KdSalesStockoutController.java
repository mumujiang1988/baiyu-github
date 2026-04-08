package com.ruoyi.business.k3.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.ruoyi.business.dto.KdSalesStockoutVo;
import com.ruoyi.business.entity.KdSalesStockoutBo;
import com.ruoyi.business.k3.service.KdSalesStockoutService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.excel.utils.ExcelUtil;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import com.ruoyi.common.web.core.BaseController;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 金蝶销售出库单主表信息操作处理
 *
 * @author aiflowy
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/kd/sales/stockout")
public class KdSalesStockoutController extends BaseController {

    private final KdSalesStockoutService kdSalesStockoutService;

    /**
     * 查询金蝶销售出库单主表列表
     */
    @SaCheckPermission("kd:sales:stockout:list")
    @GetMapping("/list")
    public TableDataInfo<KdSalesStockoutVo> list(KdSalesStockoutBo kdSalesStockout, PageQuery pageQuery) {
        return kdSalesStockoutService.selectPageKdSalesStockoutList(kdSalesStockout, pageQuery);
    }

    /**
     * 导出金蝶销售出库单主表列表
     */
    @Log(title = "金蝶销售出库单", businessType = BusinessType.EXPORT)
    @SaCheckPermission("kd:sales:stockout:export")
    @PostMapping("/export")
    public void export(KdSalesStockoutBo kdSalesStockout, HttpServletResponse response) {
        List<KdSalesStockoutVo> list = kdSalesStockoutService.selectKdSalesStockoutList(kdSalesStockout);
        ExcelUtil.exportExcel(list, "金蝶销售出库单数据", KdSalesStockoutVo.class, response);
    }

    /**
     * 根据实体主键获取详细信息
     *
     * @param fid 金蝶销售出库单主键
     */
    @SaCheckPermission("kd:sales:stockout:query")
    @GetMapping(value = "/{fid}")
    public R<KdSalesStockoutVo> getInfo(@PathVariable String fid) {
        return R.ok(kdSalesStockoutService.selectKdSalesStockoutByFid(fid));
    }

    /**
     * 新增金蝶销售出库单主表
     */
    @SaCheckPermission("kd:sales:stockout:add")
    @Log(title = "金蝶销售出库单", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated @RequestBody KdSalesStockoutBo kdSalesStockout) {
        kdSalesStockoutService.insertKdSalesStockout(kdSalesStockout);
        return R.ok();
    }

    /**
     * 修改金蝶销售出库单主表
     */
    @SaCheckPermission("kd:sales:stockout:edit")
    @Log(title = "金蝶销售出库单", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated @RequestBody KdSalesStockoutBo kdSalesStockout) {
        kdSalesStockoutService.updateKdSalesStockout(kdSalesStockout);
        return R.ok();
    }

    /**
     * 删除金蝶销售出库单主表
     *
     * @param fids 实体主键串
     */
    @SaCheckPermission("kd:sales:stockout:remove")
    @Log(title = "金蝶销售出库单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{fids}")
    public R<Void> remove(@PathVariable String[] fids) {
        kdSalesStockoutService.deleteKdSalesStockoutByFids(fids);
        return R.ok();
    }
}
