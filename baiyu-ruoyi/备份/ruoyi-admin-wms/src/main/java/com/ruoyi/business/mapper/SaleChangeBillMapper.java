package com.ruoyi.business.mapper;

import com.ruoyi.business.entity.SaleChangeBillFlat;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SaleChangeBillMapper {

    /**
     * 根据FID查询
     */
    SaleChangeBillFlat selectByFid(@Param("fid") Long fid);

    /**
     * 根据FID查询最新记录
     */
    SaleChangeBillFlat selectLatestByFid(@Param("fid") Long fid);

    /**
     * 根据ID查询
     */
    SaleChangeBillFlat selectById(@Param("id") Long id);

    /**
     * 插入单条记录
     */
    int insert(SaleChangeBillFlat record);

    /**
     * 批量插入
     */
    int batchInsert(List<SaleChangeBillFlat> list);

    /**
     * 更新
     */
    int update(SaleChangeBillFlat record);

    /**
     * 根据FID删除
     */
    int deleteByFid(@Param("fid") Long fid);
}
