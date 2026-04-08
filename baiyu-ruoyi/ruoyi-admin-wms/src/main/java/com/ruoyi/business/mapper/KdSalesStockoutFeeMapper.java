package com.ruoyi.business.mapper;

import com.ruoyi.business.entity.KdSalesStockoutFee;
import com.ruoyi.common.mybatis.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 金蝶销售出库单费用明细表 Mapper 接口
 *
 * @author aiflowy
 */
public interface KdSalesStockoutFeeMapper extends BaseMapperPlus<KdSalesStockoutFee, KdSalesStockoutFee> {

    /**
     * 根据主表 ID 查询费用明细列表
     *
     * @param fid 主表 ID
     * @return 费用明细列表
     */
    List<KdSalesStockoutFee> selectByMainId(@Param("fid") String fid);

    /**
     * 批量插入费用明细
     *
     * @param list 费用明细列表
     * @return 结果
     */
    int insertBatch(@Param("list") List<KdSalesStockoutFee> list);
}
