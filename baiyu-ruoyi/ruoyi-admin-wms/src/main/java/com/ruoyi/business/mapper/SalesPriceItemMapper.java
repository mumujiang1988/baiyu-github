package com.ruoyi.business.mapper;

import com.ruoyi.business.entity.SalesPriceItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SalesPriceItemMapper {

    /** 批量新增价目明细 */
    int batchInsertItem(@Param("list") List<SalesPriceItem> list);

    /** 单条新增价目明细 */
    int insertItem(SalesPriceItem item);

    /** 单条更新价目明细 */
    int updateItem(SalesPriceItem item);

    /** 批量更新价目明细 */
    int batchUpdateItem(@Param("list") List<SalesPriceItem> list);

    /** 根据价目主表ID和物料编码查询 */
    SalesPriceItem selectByPriceIdAndMaterialId(@Param("priceId") Long priceId, @Param("materialId") String materialId);

    /** 根据价目ID查询明细 */
    List<SalesPriceItem> selectByPriceId(@Param("priceId") Long priceId);

    /** 根据物料编码列表批量查询 */
    List<SalesPriceItem> selectByMaterialIds(List<String> materialIds);

    /** 根据价目ID删除明细 */
    int deleteByPriceId(@Param("priceId") Long priceId);

}
