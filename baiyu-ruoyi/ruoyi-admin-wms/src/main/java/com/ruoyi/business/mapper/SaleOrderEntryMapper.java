package com.ruoyi.business.mapper;

import com.ruoyi.business.entity.SaleOrderEntry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SaleOrderEntryMapper {
    /**
     * 根据订单编号查询销售订单明细列表
     * @param fbillNo 订单单据编号
     * @return 销售订单明细列表
     */
    List<SaleOrderEntry> selectByOrderId(@Param("fbillNo") String fbillNo);

    /**
     * 插入销售订单明细
     * @param entry 销售订单明细实体
     * @return 影响行数
     */
    int insert(SaleOrderEntry entry);

    /**
     * 根据订单ID删除销售订单明细
     * @param fid 订单主键ID
     * @return 影响行数
     */
    int deleteByOrderId(Long fid);




    void deleteByFids(@Param("fids") List<String> fids);

    void batchInsert(@Param("list") List<SaleOrderEntry> list);


}
