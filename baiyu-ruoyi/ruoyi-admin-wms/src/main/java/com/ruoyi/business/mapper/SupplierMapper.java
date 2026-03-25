package com.ruoyi.business.mapper;


import com.ruoyi.business.entity.Supplier;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SupplierMapper {

    /**
     * 插入供应商信息
     * @param supplier 供应商信息
     * @return 影响行数
     */

    int insertSupplier(Supplier supplier);


    /**
     * 根据ID更新供应商信息
     * @param supplier 供应商信息
     * @return 影响行数
     */

    int updateSupplier(Supplier supplier);

    /**
     * 根据ID删除供应商
     * @param number 供应商ID
     * @return 影响行数
     */
    @Delete("DELETE FROM supplier WHERE number = #{number}")
    int deleteById(String number);

    /**
     * 根据number查询供应商
     * @param number 供应商编码
     * @return 供应商信息
     */

    Supplier selectById(String number);

    /**
     * 通过供应商分组找最新一条数据
     * */
    Supplier selectBySupplierGroup(String supplierGroup);

    /**
     * 根据金蝶ID查询供应商
     *
     * @param supplierid 金蝶供应商ID
     * @return 供应商
     */
    Supplier getSupplierBySupplierId(@Param("supplierid") String supplierid);


    Supplier supplierID(String id);


    List<Supplier> selectAll(Supplier supplier);

    /**
     * 分页查询供应商列表
     * @param offset 偏移量
     * @param size 每页数量
     * @param supplier 查询条件
     * @return 供应商列表
     */
    List<Supplier> selectByCondition(@Param("offset") int offset, @Param("size") int size, @Param("supplier") Supplier supplier);

    /**
     * 统计供应商总数
     * @param supplier 查询条件
     * @return 总数
     */
    long countByCondition(@Param("supplier") Supplier supplier);




}
