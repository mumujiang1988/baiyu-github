package com.ruoyi.business.k3.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.ruoyi.business.dto.CountryOrderDistributionDTO;
import com.ruoyi.business.dto.SalesOrderDTo;
import com.ruoyi.business.dto.SalesRankingDTO;
import com.ruoyi.business.entity.SaleOrder;
import com.ruoyi.business.k3.service.SaleOrderService;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import com.ruoyi.common.web.core.BaseController;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 销售订单控制器
 *
 * @author system
 */
@Slf4j
@RestController
@RequestMapping("/k3/sale-order")
public class SaleOrderController extends BaseController {

    @Resource
    private SaleOrderService saleOrderService;

    /**
     * 同步金蝶销售订单数据到本地数据库
     *
     * @return 同步结果，包含处理的总记录数
     */
    @SaCheckPermission("k3:saleOrder:sync")
    @PostMapping("/sync")
    @Transactional(rollbackFor = Exception.class)
    public Result syncSaleOrders() {
        try {
            log.info("开始同步销售订单数据...");

            // 调用服务层同步数据
            int totalCount = saleOrderService.syncSaleOrdersFromK3();

            log.info("销售订单数据同步完成，总计处理：{} 条", totalCount);

            // 返回成功结果
            return Result.success("销售订单同步完成，总计处理：" + totalCount + " 条");

        } catch (Exception e) {
            log.error("同步销售订单数据失败", e);
            return Result.error("同步销售订单数据失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询销售订单
     */
    @SaCheckPermission("k3:saleOrder:list")
    @GetMapping("/list")
    public TableDataInfo<SaleOrder> list(SaleOrder saleOrder, PageQuery pageQuery) {
        return saleOrderService.selectPageSaleOrder(saleOrder, pageQuery);
    }

    /**
     * 根据ID查询销售订单
     *
     * @param id 订单ID
     */
    @SaCheckPermission("k3:saleOrder:query")
    @GetMapping(value = "/{id}")
    public Result getInfo(@PathVariable Long id) {
        SaleOrder saleOrder = saleOrderService.selectById(id);
        if (saleOrder == null){
            return Result.error("未找到对应的销售订单");
        }
        return Result.success(saleOrder);
    }

    /**
     * 查询指定月份销售员订单数量排行榜（前3名）
     *
     * @param year 年份
     * @param month 月份
     */
    @SaCheckPermission("k3:saleOrder:rankings")
    @GetMapping(value = "/sales-ranking")
    public Result getTop3SalesByMonth(
            @RequestParam(value = "year") Integer year,
            @RequestParam(value = "month") Integer month) {

        // 参数校验
        if (year == null || month == null) {
            return Result.error("参数不能为空：年份和月份都需要提供");
        }

        // 验证月份范围
        if (month < 1 || month > 12) {
            return Result.error("月份必须在1-12之间");
        }

        List<SalesRankingDTO> result = saleOrderService.getTop3SalesByMonth(year, month);

        return Result.success(result);
    }

    /**
     * 查询指定月份的国家订单分布
     *
     */
    @SaCheckPermission("k3:saleOrder:rankings")
    @PostMapping(value = "/country-distribution")
    public Result statistics(@RequestBody SalesOrderDTo query) {
        return Result.success(
            saleOrderService.getCountryOrderDistributionByMonth(query)
        );
    }

    /**
     * 新增销售订单
     *
     * @param saleOrder 销售订单信息
     * @return 结果
     */
    @SaCheckPermission("k3:saleOrder:add")
    @PostMapping("/add")
    @Transactional(rollbackFor = Exception.class)
    public Result add(@RequestBody SaleOrder saleOrder) {
        if (saleOrder.getFBillNo() == null){
            return Result.error("单据编号不能为空");
        }
        if (saleOrder.getFCustId() == null){
            return Result.error("客户不能为空");
        }
        return saleOrderService.insertSaleOrder(saleOrder);
    }

    /**
     * 查询销售订单明细列表
     *
     * @param fbillNo 单据编号
     * @return 销售订单明细数据
     */
    @SaCheckPermission("k3:saleOrder:query")
    @GetMapping(value = "/entry/{fbillNo}")
    public Result getEntryList(@PathVariable String fbillNo) {
        try {
            List<Map<String, Object>> entryList = saleOrderService.selectEntryList(fbillNo);
            return Result.success(entryList);
        } catch (Exception e) {
            log.error("查询销售订单明细失败", e);
            return Result.error("查询销售订单明细失败：" + e.getMessage());
        }
    }

    /**
     * 根据单据编号查询销售订单成本暂估
     *
     * @param fbillNo 单据编号
     * @return 销售订单成本数据
     */
    @SaCheckPermission("k3:saleOrder:query")
    @GetMapping(value = "/cost/{fbillNo}")
    public Result getCostData(@PathVariable String fbillNo) {
        try {
            // 直接通过 FBillNo 查询成本表
            Map<String, Object> costData = saleOrderService.selectCostDataByBillNo(fbillNo);
            if (costData == null || costData.isEmpty()) {
                return Result.success(new HashMap<>());
            }
            return Result.success(costData);
        } catch (Exception e) {
            log.error("查询销售订单成本失败", e);
            return Result.error("查询销售订单成本失败：" + e.getMessage());
        }
    }

    /**
     * 获取销售订单中存在的销售员列表
     *
     * @return 销售员列表
     */
    @SaCheckPermission("k3:saleOrder:list")
    @GetMapping(value = "/salespersons")
    public Result getSalespersons() {
        try {
            List<Map<String, Object>> salespersons = saleOrderService.getSalespersonsFromOrders();
            log.info("获取销售员列表成功，共 {} 条记录", salespersons.size());
            if (salespersons.isEmpty()) {
                log.warn("销售员列表为空，可能原因：1. t_sale_order 表中没有 FSalerId 数据 2. sys_employee 表中没有对应的销售员");
            }
            return Result.success(salespersons);
        } catch (Exception e) {
            log.error("获取销售员列表失败", e);
            return Result.error("获取销售员列表失败：" + e.getMessage());
        }
    }

    /**
     * 删除销售订单
     *
     * @param ids 订单 ID 列表
     * @return 结果
     */
    @SaCheckPermission("k3:saleOrder:delete")
    @DeleteMapping("/{ids}")
    @Transactional(rollbackFor = Exception.class)
    public Result delete(@PathVariable Long[] ids) {
        try {
            if (ids == null || ids.length == 0) {
                return Result.error("请选择要删除的数据");
            }
            
            // 检查单据状态，已审核的单据不能删除
            for (Long id : ids) {
                SaleOrder order = saleOrderService.selectById(id);
                if (order != null && "C".equals(order.getFDocumentStatus())) {
                    return Result.error("单据【" + order.getFBillNo() + "】已审核，不能删除，如需删除请先反审核");
                }
            }
            
            int result = saleOrderService.deleteSaleOrderByIds(ids);
            return result > 0 ? Result.success("删除成功") : Result.error("删除失败");
        } catch (Exception e) {
            log.error("删除销售订单失败", e);
            return Result.error("删除销售订单失败：" + e.getMessage());
        }
    }

    /**
     * 审核销售订单
     *
     * @param ids 订单 ID 列表
     * @return 操作结果
     */
    @SaCheckPermission("k3:saleOrder:audit")
    @PostMapping("/audit")
    @Transactional(rollbackFor = Exception.class)
    public Result audit(@RequestBody Long[] ids) {
        try {
            if (ids == null || ids.length == 0) {
                return Result.error("请选择要审核的数据");
            }
            return saleOrderService.auditSaleOrders(ids);
        } catch (Exception e) {
            log.error("审核销售订单失败", e);
            return Result.error("审核失败：" + e.getMessage());
        }
    }

    /**
     * 反审核销售订单
     *
     * @param ids 订单 ID 列表
     * @return 操作结果
     */
    @SaCheckPermission("k3:saleOrder:unAudit")
    @PostMapping("/unAudit")
    @Transactional(rollbackFor = Exception.class)
    public Result unAudit(@RequestBody Long[] ids) {
        try {
            if (ids == null || ids.length == 0) {
                return Result.error("请选择要反审核的数据");
            }
            return saleOrderService.unAuditSaleOrders(ids);
        } catch (Exception e) {
            log.error("反审核销售订单失败", e);
            return Result.error("反审核失败：" + e.getMessage());
        }
    }
}
