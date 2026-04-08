package com.ruoyi.business.mapper;

import com.ruoyi.business.entity.KdSalesStockoutEntry;
import com.ruoyi.common.mybatis.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 金蝶销售出库单明细表 Mapper 接口
 *
 * @author aiflowy
 */
public interface KdSalesStockoutEntryMapper extends BaseMapperPlus<KdSalesStockoutEntry, KdSalesStockoutEntry> {

    /**
     * 根据主表 ID 查询明细列表
     *
     * @param fid 主表 ID
     * @return 明细列表
     */
    List<KdSalesStockoutEntry> selectByMainId(@Param("fid") String fid);

    /**
     * 批量插入明细
     *
     * @param list 明细列表
     * @return 结果
     */
    int insertBatch(@Param("list") List<KdSalesStockoutEntry> list);

    /**
     * 根据主表 ID 删除明细
     *
     * @param fid 主表 ID
     * @return 结果
     */
    int deleteByMainId(@Param("fid") String fid);
}
