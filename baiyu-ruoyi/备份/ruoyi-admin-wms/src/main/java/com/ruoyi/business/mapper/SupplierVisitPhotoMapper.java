package com.ruoyi.business.mapper;

import com.ruoyi.business.entity.SupplierVisitPhoto;
import java.util.List;

/**
 * 供应商回访照片数据层
 */
public interface SupplierVisitPhotoMapper {

    /**
     * 新增供应商回访照片
     *
     * @param photo 供应商回访照片
     * @return 结果
     */
    int insertSupplierVisitPhoto(SupplierVisitPhoto photo);

    /**
     * 修改供应商回访照片
     *
     * @param photo 供应商回访照片
     * @return 结果
     */
    int updateSupplierVisitPhoto(SupplierVisitPhoto photo);

    /**
     * 删除供应商回访照片
     *
     * @param id 供应商回访照片ID
     * @return 结果
     */
    int deleteSupplierVisitPhotoById(Long id);

    /**
     * 批量删除供应商回访照片
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteSupplierVisitPhotoByIds(Long[] ids);

    /**
     * 查询供应商回访照片
     *
     * @param id 供应商回访照片ID
     * @return 供应商回访照片
     */
    SupplierVisitPhoto selectSupplierVisitPhotoById(Long id);

    /**
     * 查询供应商回访照片列表
     *
     * @param photo 供应商回访照片
     * @return 供应商回访照片集合
     */
    List<SupplierVisitPhoto> selectSupplierVisitPhotoList(SupplierVisitPhoto photo);

    /**
     * 根据供应商ID查询回访照片
     *
     * @param supplierId 供应商ID
     * @return 供应商回访照片集合
     */
    List<SupplierVisitPhoto> selectSupplierVisitPhotoBySupplierId(Long supplierId);

    /**
     * 根据供应商ID删除回访照片
     *
     * @param supplierId 供应商ID
     * @return 结果
     */
    int deleteSupplierVisitPhotoBySupplierId(Long supplierId);
}