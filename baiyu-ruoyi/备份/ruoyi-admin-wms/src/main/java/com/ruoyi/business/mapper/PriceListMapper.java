package com.ruoyi.business.mapper;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.dto.PriceListDTO;
import com.ruoyi.business.entity.Customer;
import com.ruoyi.business.entity.PriceList;
import com.ruoyi.business.k3.domain.vo.CustomerVo;
import com.ruoyi.business.k3.domain.vo.PriceListVo;
import com.ruoyi.common.mybatis.annotation.DataColumn;
import com.ruoyi.common.mybatis.annotation.DataPermission;
import com.ruoyi.common.mybatis.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PriceListMapper extends BaseMapperPlus<PriceList, PriceListVo> {

    /**
     * 插入价目表主表
     * @param priceList 价目表信息
     * @return 影响行数
     */
    int insertPriceList(PriceList priceList);

    /**根据ID查询
     * */
    PriceList selectById(@Param("id") Long id);

    /**
     * 根据ID更新价目表
     * @param priceList 价目表信息
     * @return 影响行数
     */
    int updatePriceList(PriceList priceList);

    /**
     * 根据编码删除价目表
     * @param FNumber 价目表编码
     * @return 影响行数
     */
    int deleteByNumber(@Param("FNumber") String FNumber);

    /**
     * 根据编码查询价目表
     * @param id 价目表编码
     * @return 价目表信息
     */
    PriceList selectByNumber(@Param("id") String id);

    /**
     * 分页查询价目表
     * @param page 分页参数
     * @param priceList 查询条件
     * @return 分页结果
     */
    IPage<PriceList> selectPage(Page<PriceList> page, PriceList priceList);


    /**
     * 获取客户列表（支持条件和分页）
     * @param page 查询条件对象
     * @return 客户列表
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "d.dept_id"),
        @DataColumn(key = "userName", value = "su.user_id")
    })
    Page<PriceList> selectPage(@Param("page") Page<PriceList> page, @Param(Constants.WRAPPER) Wrapper<PriceList> queryWrapper);


    /**
     * 根据供应商ID查询价目表
     * @param supplierId 供应商ID
     * @return 价目表列表
     */
    List<PriceList> selectBySupplierId(@Param("supplierId") Long supplierId);

    /**
     * 根据供应商类别查询价目表
     * @param supplierCategory 供应商类别
     * @return 价目表列表
     */
    List<PriceList> selectBySupplierCategory(@Param("supplierCategory") Long supplierCategory);


    /**
     * 查询物料对应供应商、
     * */
    List<PriceListDTO> selectByMaterialId(@Param("materialId") String materialId);
}
