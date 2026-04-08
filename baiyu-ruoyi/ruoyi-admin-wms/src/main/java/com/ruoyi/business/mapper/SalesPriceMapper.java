package com.ruoyi.business.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.entity.PriceList;
import com.ruoyi.business.entity.SalesPrice;
import com.ruoyi.business.k3.domain.vo.SalesPricesVo;
import com.ruoyi.common.mybatis.annotation.DataColumn;
import com.ruoyi.common.mybatis.annotation.DataPermission;
import com.ruoyi.common.mybatis.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface SalesPriceMapper extends BaseMapperPlus<SalesPrice, SalesPricesVo> {

    /** 新增价目主表 */
    int insertSalesPrice(SalesPrice price);

    /** 根据ID查询完整价目（含明细和包装） */
    SalesPrice selectPriceDetail(@Param("id") Long id);

    /** 根据价目编号查询 */
    SalesPrice selectByFNumber(@Param("fNumber") String fNumber);

    /** 根据价目编号批量查询 */
    List<SalesPrice> selectByFNumbers(List<String> fNumbers);

    /** 根据K3系统主键ID查询 */
    SalesPrice selectByK3Id(@Param("k3Id") Long k3Id);

    /** 更新价目主表 */
    int updateSalesPrice(SalesPrice price);

    /**
     * 分页查询价目列表
     * @param page 查询条件
     * @return 价目列表
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "d.dept_id"),
        @DataColumn(key = "userName", value = "su.user_id")
    })
    Page<SalesPrice> selectPage(@Param("page") Page<SalesPrice> page, @Param(Constants.WRAPPER) Wrapper<SalesPrice> queryWrapper);

    /**
     * 统计价目总数
     * @param price 查询条件
     * @return 总数
     */
    long countByCondition(@Param("price") SalesPrice price);
}
