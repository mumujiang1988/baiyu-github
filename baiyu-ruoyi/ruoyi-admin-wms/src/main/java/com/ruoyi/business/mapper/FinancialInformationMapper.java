package com.ruoyi.business.mapper;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.entity.FinancialInformation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FinancialInformationMapper  extends BaseMapper<FinancialInformation> {


    /**
     * 根据编码查询
     * */

    List<FinancialInformation> selectByContractBse(String contractBse);


    /**
     * 根据ID查询
     * */
    FinancialInformation selectById(Long id);

    /**
     * 根据供应商/客户编码查询
     */
    FinancialInformation selectBySupplierNumber(@Param("supplierNumber") String supplierNumber);

    /**
     * 根据编码查询列表
     * */

    List<FinancialInformation> selectBySupplierNumbers(@Param("list") List<String> supplierNumbers);

    /**
     * 新增付款账户 信息
     */
    int insertSupplierContact(FinancialInformation contactBase);

    /**
     * 修改付款账号信息
     */
    int updateSupplierContact(@Param("contactBase") FinancialInformation contactBase);
    /**
     * 删除付款账号信息
     */
    int deleteSupplierContact(@Param("supplierNumber") String supplierNumber);

    /**
     * 根据查询条件获取列表
     */
    List<FinancialInformation> selectListByQuery(QueryWrapper<FinancialInformation> queryWrapper);

    /**
     * 查询所有员工编号
     */
    List<String> selectAllSupplierNumbers();

    /**
     * 批量插入员工银行信息
     */
    int batchInsertEmployeeBank(@Param("list") List<FinancialInformation> list);

    /**
     * 批量更新员工银行信息
     */
    int batchUpdateEmployeeBank(@Param("list") List<FinancialInformation> list);
}
