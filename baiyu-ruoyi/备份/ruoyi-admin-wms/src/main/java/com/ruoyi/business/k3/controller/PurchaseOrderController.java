package com.ruoyi.business.k3.controller;

import com.ruoyi.business.entity.PoOrderBillHead;
import com.ruoyi.business.k3.config.k3config;
import com.ruoyi.business.k3.service.PurchaseOrderService;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import com.ruoyi.common.web.core.BaseController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 采购订单Controller
 */
@Slf4j
@RestController
@RequestMapping("/k3/purchase-order")
public class PurchaseOrderController extends BaseController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private k3config k3Config;


    /**
     * 同步金蝶采购订单数据到本地数据库
     * @return 操作结果
     */
    @PostMapping("/sync")
    @Transactional(rollbackFor = Exception.class)
    public Result syncPurchaseOrders() {
            //int total = purchaseOrderService.syncPurchaseOrdersMultiThread();
            // 同步采购订单详情表数据
            int entryTotal = purchaseOrderService.syncPurchaseOrderEntries(k3Config);
            return Result.success("采购订单同步完成，总计处理：" + entryTotal + " 条, 采购订单详情同步完成，总计处理：" + entryTotal + " 条");
    }
    /**
     * 分页查询采购订单
     */
    @GetMapping("/list")
    public TableDataInfo<PoOrderBillHead> listPurchaseOrders(PoOrderBillHead poOrderBillHead, PageQuery pageQuery) {
        return purchaseOrderService.list(poOrderBillHead, pageQuery);
    }
    /**
     * 获取采购订单详细信息
     */
    @GetMapping("/getPurchaseOrder/{id}")
    public Result getPurchaseOrder(@PathVariable Long id) {
        try {
            PoOrderBillHead purchaseOrder = purchaseOrderService.getPurchaseOrder(id);
            if (purchaseOrder != null) {
                return Result.success(purchaseOrder);
            } else {
                return Result.error("采购订单不存在，ID: " + id);
            }
        } catch (Exception e) {
            log.error("获取采购订单异常，ID: {}", id, e);
            return Result.error("获取采购订单异常: " + e.getMessage());
        }
    }
    /**
     * 新增采购订单
     * @param poOrderBillHead 采购订单信息
     * @return 操作结果
     */
    @PostMapping("/add")
    @Transactional(rollbackFor = Exception.class)
    public Result addPurchaseOrder(@RequestBody PoOrderBillHead poOrderBillHead) {
        try {
            // 必填字段校验
            if (poOrderBillHead.getFdate() == null) {
                return Result.error("采购日期不能为空");
            }
            if (poOrderBillHead.getFsupplierId() == null || poOrderBillHead.getFsupplierId().trim().isEmpty()) {
                return Result.error("供应商不能为空");
            }
            if (poOrderBillHead.getFbillTypeId() == null || poOrderBillHead.getFbillTypeId().trim().isEmpty()) {
                return Result.error("单据类型不能为空");
            }
            return purchaseOrderService.addPurchaseOrder(poOrderBillHead);
        } catch (Exception e) {
            log.error("新增采购订单异常", e);
            return Result.error("新增采购订单异常: " + e.getMessage());
        }
    }

}
