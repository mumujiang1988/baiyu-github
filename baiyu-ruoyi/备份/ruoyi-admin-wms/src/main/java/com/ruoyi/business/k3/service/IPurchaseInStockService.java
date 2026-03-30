package com.ruoyi.business.k3.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.entity.PurchaseInStock;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;

import java.util.List;

/**
 * 采购入库单 Service 接口
 */
public interface IPurchaseInStockService {

    /**
     * 查询采购入库单
     * @param fid 主键
     * @return 采购入库单
     */
    PurchaseInStock selectById(String fid);

    /**
     * 查询采购入库单列表（分页）
     * @param purchaseInStock 查询条件
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    TableDataInfo<PurchaseInStock> selectList(PurchaseInStock purchaseInStock, int pageNum, int pageSize);

    /**
     * 新增采购入库单
     * @param purchaseInStock 采购入库单
     * @return 影响行数
     */
    int insert(PurchaseInStock purchaseInStock);

    /**
     * 修改采购入库单
     * @param purchaseInStock 采购入库单
     * @return 影响行数
     */
    int update(PurchaseInStock purchaseInStock);

    /**
     * 删除采购入库单
     * @param fid 主键
     * @return 影响行数
     */
    int deleteById(String fid);

    /**
     * 批量删除采购入库单
     * @param fids 主键数组
     * @return 影响行数
     */
    int deleteByIds(String[] fids);

    /**
     * 同步金蝶采购入库单主表数据
     * @return 同步的记录数
     */
    int syncPurchaseInStockData();

    /**
     * 同步金蝶采购入库单明细表数据
     * @return 同步的记录数
     */
    int syncPurchaseInStockEntryData();
}
