package com.ruoyi.business.k3.service;

import com.ruoyi.business.dto.KdSalesStockoutVo;
import com.ruoyi.business.entity.KdSalesStockoutBo;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;

import java.util.List;

/**
 * 金蝶销售出库单主表 Service 业务层接口
 *
 * @author aiflowy
 */
public interface KdSalesStockoutService {

    /**
     * 查询金蝶销售出库单主表
     *
     * @param fid 金蝶销售出库单主表主键
     * @return 金蝶销售出库单主表
     */
    KdSalesStockoutVo selectKdSalesStockoutByFid(String fid);

    /**
     * 查询金蝶销售出库单主表列表（分页）
     *
     * @param bo 金蝶销售出库单主表
     * @param pageQuery 分页参数
     * @return 金蝶销售出库单主表集合
     */
    TableDataInfo<KdSalesStockoutVo> selectPageKdSalesStockoutList(KdSalesStockoutBo bo, PageQuery pageQuery);

    /**
     * 查询金蝶销售出库单主表列表
     *
     * @param bo 金蝶销售出库单主表
     * @return 金蝶销售出库单主表集合
     */
    List<KdSalesStockoutVo> selectKdSalesStockoutList(KdSalesStockoutBo bo);

    /**
     * 新增金蝶销售出库单主表
     *
     * @param bo 金蝶销售出库单主表
     * @return 结果
     */
    int insertKdSalesStockout(KdSalesStockoutBo bo);

    /**
     * 修改金蝶销售出库单主表
     *
     * @param bo 金蝶销售出库单主表
     * @return 结果
     */
    int updateKdSalesStockout(KdSalesStockoutBo bo);

    /**
     * 批量删除金蝶销售出库单主表
     *
     * @param fids 需要删除的金蝶销售出库单主表主键
     * @return 结果
     */
    int deleteKdSalesStockoutByFids(String[] fids);

    /**
     * 删除金蝶销售出库单主表信息
     *
     * @param fid 金蝶销售出库单主表主键
     * @return 结果
     */
    int deleteKdSalesStockoutByFid(String fid);
}
