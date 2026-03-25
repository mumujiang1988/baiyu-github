package com.ruoyi.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.entity.TaxRate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


import java.util.List;
import java.util.Map;

@Mapper
public interface TaxRateMapper extends BaseMapper<TaxRate> {

    /**
     * 根据编码查询税率信息
     * @param code 编码
     * @return 税率信息
     */
    TaxRate selectByCode(@Param("code") String code);

    /**
     * 根据金蝶编码查询税率信息
     * @param k3Code 金蝶编码
     * @return 税率信息
     */
    TaxRate selectByK3Code(@Param("k3Code") String k3Code);

    List<Map<String, Object>> selectByK3Codes(@Param("codes") List<String> codes);


    TaxRate selectK3Cod(@Param("k3Code") String k3Code);

    /**
     * 根据税种查询税率列表
     * @param taxCategory 税种
     * @return 税率列表
     */
    List<TaxRate> selectByTaxCategory(@Param("taxCategory") String taxCategory);

    /**
     * 查询所有税率信息
     * @return 税率信息列表
     */
    List<TaxRate> selectAll();
}
