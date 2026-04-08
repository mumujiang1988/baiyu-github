package com.ruoyi.business.k3.service;

import com.ruoyi.business.dto.CountryOrderDistributionDTO;
import com.ruoyi.business.dto.SalesOrderDTo;
import com.ruoyi.business.dto.SalesRankingDTO;
import com.ruoyi.business.entity.PriceList;
import com.ruoyi.business.entity.SaleOrder;
import com.ruoyi.business.k3.domain.bo.PriceListBo;
import com.ruoyi.business.k3.domain.bo.SaleOrderBo;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;

import java.util.List;
import java.util.Map;

/**
 * 销售订单服务接口
 * 负责从金蝶同步销售订单数据到本地数据库
 *
 * @author system
 */
public interface SaleOrderService {

    /**
     * 同步金蝶销售订单数据到本地数据库
     * 包含三个表的同步：
     * 1. 销售订单主表 (t_sale_order)
     * 2. 销售订单明细表 (t_sale_order_entry)
     * 3. 销售订单成本表 (t_sale_order_cost)
     *
     * @return 同步的总记录数
     */
    int syncSaleOrdersFromK3();

    /**
     * 分页查询销售订单
     *
     * @param saleOrder 查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    TableDataInfo<SaleOrder> selectPageSaleOrder(SaleOrderBo saleOrder, PageQuery pageQuery);

    /**
     * 根据ID查询销售订单
     *
     * @param fid 订单ID
     * @return 销售订单
     */
    SaleOrder selectById(Long fid);

    /**
     * 查询指定月份销售员订单数量排行榜（前3名）
     *
     * @param year 年份
     * @param month 月份
     * @return 销售员排行榜数据
     */
    List<SalesRankingDTO> getTop3SalesByMonth(Integer year, Integer month);

    /**
     * 查询指定月份的国家订单分布
     */
    SalesOrderDTo getCountryOrderDistributionByMonth(SalesOrderDTo query) ;

    /**
     * 新增销售订单
     *
     * @param saleOrder 销售订单信息
     * @return 结果
     */
    Result insertSaleOrder(SaleOrder saleOrder);

}
