package com.ruoyi.business.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.entity.PurchaseInStock;
import com.ruoyi.business.entity.PurchaseInStockEntry;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 采购入库单 Mapper 接口
 */
public interface PurchaseInStockMapper {

    /**
     * 查询采购入库单
      * @param id 主键
     * @return 采购入库单
     */
    PurchaseInStock selectById(@Param("id") String id);

    /**
     * 查询采购入库单列表（分页）
     * @param page 分页对象
     * @return 采购入库单列表
     */
    List<PurchaseInStock> selectList(@Param("page") int page, @Param("pageSize") int pageSize, @Param("purchaseInStock") PurchaseInStock purchaseInStock);

    /**
     * 新增采购入库单
     * @param purchaseInStock 采购入库单
     * @return 影响行数
     */
    int insert(PurchaseInStock purchaseInStock);

    /**
     * 更新采购入库单
     * @param purchaseInStock 采购入库单
     * @return 影响行数
     */
    int update(PurchaseInStock purchaseInStock);

    /**
     * 删除采购入库单
     * @param fid 主键
     * @return 影响行数
     */
    int deleteById(@Param("fid") String fid);

    /**
     * 批量删除采购入库单
     * @param fids 主键数组
     * @return 影响行数
     */
    int deleteByIds(@Param("fids") String[] fids);

    /**
     * 新增采购入库单明细
     * @param purchaseInStockEntry 采购入库单明细
     * @return 影响行数
     */
    int insertEntry(@Param("et") PurchaseInStockEntry purchaseInStockEntry);

    /**
     * 更新采购入库单明细
     * @param entry 采购入库单明细
     * @return 影响行数
     */
    int updateEntry(@Param("et") PurchaseInStockEntry entry);

    /**
     * 根据 ID 查询采购入库单明细
      * @param fbillno 主键
     * @return 采购入库单明细
     */
    PurchaseInStockEntry selectEntryByFbillno(@Param("fbillno") String fbillno, @Param("fmaterialId") String fmaterialId);
    List<PurchaseInStockEntry> selectEntryById(@Param("fbillno") String fbillno);
}
