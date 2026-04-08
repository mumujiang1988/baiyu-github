package com.ruoyi.business.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.business.dto.KdSalesStockoutVo;
import com.ruoyi.business.entity.KdSalesStockout;
import com.ruoyi.common.mybatis.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 金蝶销售出库单主表 Mapper 接口
 *
 * @author aiflowy
 */
public interface KdSalesStockoutMapper extends BaseMapperPlus<KdSalesStockout, KdSalesStockoutVo> {

    /**
     * 根据单据编号查询
     *
     * @param billNo 单据编号
     * @return 销售出库单
     */
    KdSalesStockoutVo selectByBillNo(@Param("billNo") String billNo);

    /**
     * 查询销售出库单列表（带明细）
     *
     * @param wrapper 查询条件
     * @return 销售出库单列表
     */
    List<KdSalesStockoutVo> selectStockoutWithEntries(@Param("ew") LambdaQueryWrapper<KdSalesStockout> wrapper);
}
