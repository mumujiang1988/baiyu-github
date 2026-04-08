package com.ruoyi.business.k3.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.ruoyi.business.entity.DeliveryNotice;
import com.ruoyi.business.k3.config.k3config;
import com.ruoyi.business.k3.service.DeliveryNoticeService;
import com.ruoyi.business.k3.service.impl.KingdeeDeliveryNoticeSyncService;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import com.ruoyi.common.web.core.BaseController;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * 发货通知单管理控制器
 */
@RestController
@RequestMapping("/k3/delivery-notice")
@Slf4j
public class DeliveryNoticeController extends BaseController {

    @Resource
    private DeliveryNoticeService deliveryNoticeService;
    @Autowired
    private KingdeeDeliveryNoticeSyncService syncService;

    /**
     * 同步金蝶发货通知单数据到本地数据库
     *
     * @return 同步结果
     */
    @PostMapping("/sync")
    public Result syncDeliveryNoticesFromK3() {
        try {
            log.info("开始执行金蝶发货通知单数据同步...");

            int totalCount = syncService.syncDeliveryNoticesFromK3();

            log.info("金蝶发货通知单数据同步完成，共处理 {} 条数据", totalCount);

            return Result.success("发货通知单数据同步成功，共处理 " + totalCount + " 条数据");

        } catch (Exception e) {
            log.error("同步金蝶发货通知单数据失败", e);
            return Result.error("同步发货通知单数据失败：" + e.getMessage());
        }
    }

    /**
     * 添加发货通知单（包含明细）
     *
     * @param deliveryNotice 发货通知单信息（包含 entries 明细列表）
     * @return 操作结果
     */
    @PostMapping("/save")
    @Transactional(rollbackFor = Exception.class)
    public Result save(@RequestBody DeliveryNotice deliveryNotice) {
        try {
            return deliveryNoticeService.save(deliveryNotice);
        } catch (Exception e) {
            return Result.error("添加发货通知单失败：" + e.getMessage());
        }
    }

    /**
     * 更新发货通知单信息（包含明细）
     *
     * @param deliveryNotice 发货通知单信息
     * @return 操作结果
     */
    @PostMapping("/update")
    @SaCheckPermission("/api/v1/delivery-notice/update")
    @Transactional(rollbackFor = Exception.class)
    public Result update(@RequestBody DeliveryNotice deliveryNotice) {
        try {
            return deliveryNoticeService.update(deliveryNotice);
        } catch (Exception e) {
            return Result.error("更新发货通知单失败：" + e.getMessage());
        }
    }



    /**
     * 根据 ID 查询发货通知单（包含明细）
     *
     * @param id 发货通知单 ID
     * @return 发货通知单信息
     */
    @GetMapping("/get/{id}")
    @SaCheckPermission("/api/v1/delivery-notice/get/{id}")
    public Result getById(@PathVariable Long id) {
        try {
            DeliveryNotice deliveryNotice = deliveryNoticeService.getById(id);
            if (deliveryNotice != null) {
                return Result.success(deliveryNotice);
            } else {
                return Result.error("未找到该发货通知单");
            }
        } catch (Exception e) {
            return Result.error("查询发货通知单失败：" + e.getMessage());
        }
    }

    /**
     * 根据单据编号查询发货通知单（包含明细）
     *
     * @param billNo 单据编号
     * @return 发货通知单信息
     */
    @GetMapping("/get/billno/{billNo}")
    public Result getByBillNo(@PathVariable String billNo) {
        try {
            DeliveryNotice deliveryNotice = deliveryNoticeService.getByBillNo(billNo);
            if (deliveryNotice != null) {
                return Result.success(deliveryNotice);
            } else {
                return Result.error("未找到该发货通知单");
            }
        } catch (Exception e) {
            return Result.error("查询发货通知单失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询发货通知单列表（支持条件查询）
     *
     * @param deliveryNotice 查询条件
     * @param pageQuery 分页参数
     * @return 发货通知单列表
     */
    @GetMapping("/list")
    public TableDataInfo<DeliveryNotice> list(DeliveryNotice deliveryNotice, PageQuery pageQuery) {
        return deliveryNoticeService.list(deliveryNotice, pageQuery);
    }

    /**
     * 根据客户 ID 查询发货通知单
     *
     * @param customerId 客户 ID
     * @return 发货通知单列表
     */
    @GetMapping("/list/customer/{customerId}")
    public Result listByCustomerId(@PathVariable String customerId) {
        try {
            var deliveryNotices = deliveryNoticeService.listByCustomerId(customerId);
            return Result.success(deliveryNotices);
        } catch (Exception e) {
            return Result.error("查询发货通知单失败：" + e.getMessage());
        }
    }
}
