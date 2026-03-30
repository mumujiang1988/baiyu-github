package com.ruoyi.business.mapper;

import com.ruoyi.business.entity.SaleChangeDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SaleChangeDetailMapper {

    /**
     * 根据源单编号查询明细
     */
    List<SaleChangeDetail> selectBySourceBillNo(@Param("sourceBillNo") String sourceBillNo);

    /**
     * 批量插入
     */
    int batchInsert(List<SaleChangeDetail> list);

    /**
     * 根据源单编号删除
     */
    int deleteBySourceBillNo(@Param("sourceBillNo") String sourceBillNo);


}
