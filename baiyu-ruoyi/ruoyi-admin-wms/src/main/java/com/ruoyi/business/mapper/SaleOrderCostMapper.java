package com.ruoyi.business.mapper;

import com.ruoyi.business.entity.SaleOrderCost;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SaleOrderCostMapper {
    /**
     * 根据主键查询销售订单成本
     * @param fid 主键ID（销售订单FID）
     * @return 销售订单成本实体
     */
    SaleOrderCost selectById(Long fid);
    
    /**
     * 插入销售订单成本
     * @param cost 销售订单成本实体
     * @return 影响行数
     */
    int insert(SaleOrderCost cost);
    
    /**
     * 根据主键删除销售订单成本
     * @param fid 主键ID（销售订单FID）
     * @return 影响行数
     */
    int deleteById(Long fid);
    
    /**
     * 批量插入销售订单成本
     * @param costs 销售订单成本列表
     * @return 影响行数
     */
    int batchInsert(List<SaleOrderCost> costs);
    
    /**
     * 批量删除销售订单成本
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
     * 删除所有销售订单成本数据
     * @return 影响行数
     */
    int deleteAll();
    
    /**
     * 根据FID更新销售订单成本
     * @param cost 销售订单成本实体
     * @return 影响行数
     */
    int updateByFid(SaleOrderCost cost);
}
