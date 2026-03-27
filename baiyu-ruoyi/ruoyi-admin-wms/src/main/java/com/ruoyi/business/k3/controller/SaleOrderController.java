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
}
