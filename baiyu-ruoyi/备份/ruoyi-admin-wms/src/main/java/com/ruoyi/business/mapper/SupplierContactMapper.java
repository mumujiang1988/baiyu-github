package com.ruoyi.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.entity.SupplierContactBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SupplierContactMapper extends BaseMapper<SupplierContactBase> {


    /**
     * 根据编码查询
     * */

    List<SupplierContactBase> selectByContractBse(String contractBse);

    /**
     * 根据客户编码查询
     * */
    List<SupplierContactBase> selectByinformationList(String customerNumber);

    /**
     * 根据供应商编码和联系人姓名查询
     * */
    SupplierContactBase selectByContractBseAndContactName(@Param("supplierCode") String supplierCode, @Param("contactName") String contactName);

    /**
     * 根据客户编码和联系人姓名查询
     * */
    SupplierContactBase selectByCustomerNumberAndContactName(@Param("customerNumber") String customerNumber, @Param("contactName") String contactName);

    /**
     * 根据id查询
     * */
    SupplierContactBase selectById(Long id);


    /**
     * 新增联系人
     */
    int insertSupplierContact(SupplierContactBase contactBase);
    List<SupplierContactBase> selectByContractBseList(@Param("supplierNumbers") List<String> supplierNumbers);

    /**
     * 修改联系人信息
     */
    int updateSupplierContact(SupplierContactBase contactBase);

    /**
     * 根据编码删除联系人
     * */
    int deleteSupplierContact(@Param("contactBase") String contactBase);

    /*
    * 根据客户编码删除联系人
    * */
    int deleteByCustomerNumber(@Param("customerNumber") String customerNumber);

}
