package com.ruoyi.business.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.dto.CountryOrderDistributionDTO;
import com.ruoyi.business.entity.SaleOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SaleOrderMapper {
    /**
     * 根据主键查询销售订单
     * @param fid 主键ID
     * @return 销售订单实体
     */
    SaleOrder selectById(Long fid);

    /**
     * 根据自增主键查询销售订单
     * @param id 自增主键ID
     * @return 销售订单实体
     */
    SaleOrder selectByAutoId(Long id);

    /**
     * 插入销售订单
     * @param order 销售订单实体
     * @return 影响行数
     */
    int insert(SaleOrder order);

    /**
     * 根据主键删除销售订单
     * @param fid 主键ID
     * @return 影响行数
     */
    int deleteById(Long fid);

    /**
     * 批量插入销售订单
     * @param orders 销售订单列表
     * @return 影响行数
     */
    int batchInsert(List<SaleOrder> orders);

    /**
     * 批量删除销售订单
     * @param fids 主键ID列表
     * @return 影响行数
     */
    int batchDelete(List<Long> fids);

    /**
     * 根据FID列表查询已存在的FID
     * @param fids FID列表
     * @return 已存在的FID列表
     */
    List<Long> selectExistingFids(List<Long> fids);

    /**
     * 根据FID更新销售订单
     * @param order 销售订单实体
     * @return 影响行数
     */
    int updateByFid(SaleOrder order);

    /**
     * 删除所有销售订单数据
     * @return 影响行数
     */
    int deleteAll();

    /**
     * 分页查询销售订单
     *
     * @param page 分页对象
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<SaleOrder> selectPage(Page<SaleOrder> page, SaleOrder query);

    /**
     * 查询指定月份的销售员订单数量排行榜
     *
     * @param year 年份
     * @param month 月份
     * @return 销售员排行榜列表
     */
    List<Map<String, Object>> selectSalesRankingByMonth(@Param("year") Integer year, @Param("month") Integer month);


    /** 国家订单分布 */
    List<CountryOrderDistributionDTO> countOrdersByCountry(@Param("year") Integer year,
                                                           @Param("month") Integer month);

    /**
     * 查询指定月份的订单总数量
     *
     * @param year 年份
     * @param month 月份
     * @return 订单总数量
     */
    Integer selectTotalOrderCountByMonth(@Param("year") Integer year, @Param("month") Integer month);

    /** 上月订单总数 */
    Integer countLastMonthOrders(@Param("year") Integer year,
                                 @Param("month") Integer month);

    /** 当期订单总数 */
    Integer countCurrentPeriodOrders(@Param("year") Integer year,
                                     @Param("month") Integer month);

    /**
     * 根据单据编号查询销售订单
     *
     * @param billNo 单据编号
     * @return 销售订单实体
     */
    SaleOrder selectByBillNo(@Param("billNo") String billNo);
}
