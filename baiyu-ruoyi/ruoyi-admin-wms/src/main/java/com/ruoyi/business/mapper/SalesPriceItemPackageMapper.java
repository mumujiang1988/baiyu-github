package com.ruoyi.business.mapper;

import com.ruoyi.business.entity.SalesPriceItemPackage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SalesPriceItemPackageMapper {

    /** 批量新增包装物料 */
    int batchInsertPackage(@Param("list") List<SalesPriceItemPackage> list);

    /** 单条新增包装物料 */
    int insertPackage(SalesPriceItemPackage pkg);

    /** 单条更新包装物料 */
    int updatePackage(SalesPriceItemPackage pkg);

    /** 批量更新包装物料 */
    int batchUpdatePackage(@Param("list") List<SalesPriceItemPackage> list);

    /** 根据价目主表ID和包装编码查询 */
    SalesPriceItemPackage selectByPriceIdAndPackageCode(@Param("priceId") Long priceId, @Param("packageCode") String packageCode);

    /** 根据明细ID查询包装物料 */
    List<SalesPriceItemPackage> selectByItemId(@Param("itemId") Long itemId);

    /** 根据价目ID查询包材明细 */
    List<SalesPriceItemPackage> selectByPriceId(@Param("priceId") Long priceId);

    /** 根据包材编码列表批量查询 */
    List<SalesPriceItemPackage> selectByPackageCodes(List<String> packageCodes);

    /** 根据价目ID删除包材明细 */
    int deleteByPriceId(@Param("priceId") Long priceId);

    /** 根据ID列表批量删除 */
    int batchDeleteByIds(@Param("ids") List<Long> ids);
}
