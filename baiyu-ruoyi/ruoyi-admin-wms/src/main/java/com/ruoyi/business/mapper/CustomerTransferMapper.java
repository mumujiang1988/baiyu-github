package com.ruoyi.business.mapper;


import com.ruoyi.business.entity.CustomerTransfer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomerTransferMapper {

    /** 新增客户转让 */
    int insert(CustomerTransfer transfer);

    /** 更新客户转让 */
    int update(CustomerTransfer transfer);

    /** 根据客户编码查询 */
    CustomerTransfer selectByCustomerCode(@Param("customerCode") String customerCode);

    /** 客户转让 */
    List<CustomerTransfer> selectByCustomerCodeList(@Param("customerCode") String customerCode);

    /** 客户转让列表 */
    List<CustomerTransfer> selectList(CustomerTransfer transfer);

    /** 查询所有客户转让编码 */
    List<String> selectAllCustomerCodes();

    /** 根据客户编码删除客户转让 */
    int deleteCustomerTransfer(@Param("customerCode") String customerCode);
}
