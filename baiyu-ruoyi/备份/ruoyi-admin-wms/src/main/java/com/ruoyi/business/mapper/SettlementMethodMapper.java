package com.ruoyi.business.mapper;

import com.ruoyi.business.entity.SettlementMethod;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


import java.util.List;

@Mapper
public interface SettlementMethodMapper {

    // 根据ID查询
    SettlementMethod selectById(@Param("id") Long id);

    //
    SettlementMethod selectByTlementMethod(@Param("tlementMethod") SettlementMethod tlementMethod);

    // 查询所有
    List<SettlementMethod> selectAll();

    // 插入
    int insert(SettlementMethod settlementMethod);

    List<SettlementMethod> selectBySupplierNumbers(@Param("code") List<String> supplierNumbers);


    // 根据ID更新
    int updateById(SettlementMethod settlementMethod);

    // 根据ID删除
    int deleteById(@Param("id") Long id);

    // 根据编码查询
    SettlementMethod selectByCode(@Param("code") String code);
}
