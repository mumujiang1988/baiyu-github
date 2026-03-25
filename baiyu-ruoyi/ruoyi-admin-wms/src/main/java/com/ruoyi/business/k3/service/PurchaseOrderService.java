package com.ruoyi.business.k3.service;

import com.ruoyi.business.entity.PoOrderBillHead;
import com.ruoyi.business.k3.config.k3config;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;

import java.util.List;

/**
 * 采购订单Service接口
 */
public interface PurchaseOrderService {


    /** 主入口：分页 + 多线程同步采购订单 */
    int syncPurchaseOrdersMultiThread();

    /**
     * 同步采购订单详情表数据
     * @param k3Config 金蝶配置
     * @return 处理的记录数
     */
    int syncPurchaseOrderEntries(k3config k3Config);

    /**
     * 获取采购订单详细信息
     * @param id 采购订单ID
     * @return 采购订单详细信息
     */
    PoOrderBillHead getPurchaseOrder(Long id);

    /**
     * 分页查询采购订单列表
     * @param poOrderBillHead 查询条件
     * @param pageQuery 分页参数
     * @return 采购订单分页列表
     */
    TableDataInfo<PoOrderBillHead> list(PoOrderBillHead poOrderBillHead, PageQuery pageQuery);

    /**
     * 新增采购订单
     * @param poOrderBillHead 采购订单信息
     * @return 操作结果
     */
    Result addPurchaseOrder(PoOrderBillHead poOrderBillHead);
}
