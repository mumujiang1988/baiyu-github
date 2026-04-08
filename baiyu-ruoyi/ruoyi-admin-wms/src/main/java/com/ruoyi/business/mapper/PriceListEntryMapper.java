package com.ruoyi.business.mapper;


import com.ruoyi.business.entity.PriceListEntry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PriceListEntryMapper  {

    /**
     * 插入价目表明细
     * @param entry 价目表明细信息
     * @return 影响行数
     */
    int insertEntry(PriceListEntry entry);

    /**
     * 根据ID更新价目表明细
     * @param entry 价目表明细信息
     * @return 影响行数
     */
    int updateEntry(PriceListEntry entry);

    /**
     * 根据主表ID删除所有明细
     * @param priceListId 主表ID
     * @return 影响行数
     */
    int deleteByPriceListId(@Param("priceListId") Long priceListId);

    /**
     * 根据ID删除明细
     * @param id 明细ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据物料编码查询明细
     * @param FMaterialId 主表ID
     * @return 价目表明细列表
     */
    PriceListEntry selectMaterialId(@Param("FMaterialId") String FMaterialId);

    /**
     * 根据物料编码+主表ID查询明细
     * @param FMaterialId 物料编码
     * @param priceListNumber 主表编号
     * @return 价目表明细列表
     */
    List<PriceListEntry> selectByPriceListIdAndMaterialId(@Param("FMaterialId") String FMaterialId, @Param("priceListNumber") String priceListNumber);

    /**
     * 根据主表ID查询所有明细
     * @param priceListNumber 主表ID
     * @return 价目表明细列表
     */
    List<PriceListEntry> selectByPriceListId(@Param("priceListNumber") String priceListNumber);



    /**
     * 查询所有价目表明细（支持条件查询）
     * @param entry 查询条件
     * @return 价目表明细列表
     */
    List<PriceListEntry> selectAll(PriceListEntry entry);

    /**
     * 批量插入价目表明细
     * @param entryList 价目表明细列表
     * @return 影响行数
     */
    int batchInsert(@Param("entryList") List<PriceListEntry> entryList);
}
