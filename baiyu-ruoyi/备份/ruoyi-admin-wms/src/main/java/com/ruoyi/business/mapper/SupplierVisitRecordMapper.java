package com.ruoyi.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.entity.SupplierContactBase;
import com.ruoyi.business.entity.SupplierVisitRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 供应商回访记录数据层
 */
public interface SupplierVisitRecordMapper extends BaseMapper<SupplierVisitRecord> {

    /**
     * 新增供应商回访记录
     *
     * @param record 供应商回访记录
     * @return 结果
     */
    int insertSupplierVisitRecord(SupplierVisitRecord record);

    /**
     * 修改供应商回访记录
     *
     * @param record 供应商回访记录
     * @return 结果
     */
    int updateSupplierVisitRecord(SupplierVisitRecord record);

    /**
     * 删除供应商回访记录
     *
     * @param id 供应商回访记录ID
     * @return 结果
     */
    int deleteSupplierVisitRecordById(Long id);

    /**
     * 批量删除供应商回访记录
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteSupplierVisitRecordByIds(Long[] ids);

    /**
     * 查询供应商回访记录
     *
     * @param supplierNumber 供应商回访记录ID
     * @return 供应商回访记录
     */
    List<SupplierVisitRecord> selectSupplierVisitRecordById(String supplierNumber);

    /**
     * 查询供应商回访记录列表
     *
     * @param record 供应商回访记录
     * @return 供应商回访记录集合
     */
    List<SupplierVisitRecord> selectSupplierVisitRecordList(SupplierVisitRecord record);

    /**
     * 根据供应商ID查询回访记录
     *
     * @param supplierNumber 供应商编码
     * @return 供应商回访记录集合
     */
    List<SupplierVisitRecord> selectSupplierVisitRecordBySupplierId(String supplierNumber);

    /**
     * 根据供应商ID和访问时间查询回访记录
     *
     * @param supplierId 供应商ID
     * @param visitTime 访问时间
     * @return 供应商回访记录
     */
    SupplierVisitRecord selectSupplierVisitRecordBySupplierIdAndTime(@Param("supplierId") Long supplierId, @Param("visitTime") String visitTime);

    /**
     * 根据供应商ID删除回访记录
     *
     * @param supplierNumber 供应商编码
     * @return 结果
     */
    int deleteSupplierVisitRecordBySupplierId(@Param("supplierNumber") String supplierNumber);
}
