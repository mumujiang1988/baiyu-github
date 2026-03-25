package com.ruoyi.business.mapper;

import com.ruoyi.business.entity.SalesPrice;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SalesPriceMapper {

    /** 新增价目主表 */
    int insertSalesPrice(SalesPrice price);

    /** 根据ID查询完整价目（含明细和包装） */
    SalesPrice selectPriceDetail(@Param("id") Long id);

    /** 根据价目编号查询 */
    SalesPrice selectByFNumber(@Param("fNumber") String fNumber);

    /** 根据价目编号批量查询 */
    List<SalesPrice> selectByFNumbers(List<String> fNumbers);

    /** 根据K3系统主键ID查询 */
    SalesPrice selectByK3Id(@Param("k3Id") Long k3Id);

    /** 更新价目主表 */
    int updateSalesPrice(SalesPrice price);

    /**
     * 分页查询价目列表
     * @param offset 偏移量
     * @param size 每页数量
     * @param price 查询条件
     * @return 价目列表
     */
    List<SalesPrice> selectByCondition(@Param("offset") int offset, @Param("size") int size, @Param("price") SalesPrice price);

    /**
     * 统计价目总数
     * @param price 查询条件
     * @return 总数
     */
    long countByCondition(@Param("price") SalesPrice price);
}
