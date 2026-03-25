package com.ruoyi.business.k3.service;


import com.ruoyi.business.entity.FinancialInformation;

import java.util.List;

public interface FinancialInformationService  {

    /**
     * 根据供应商编码查询所有付款信息
     * @param supplierNumber 供应商编码
     * @return 付款信息列表
     */
    List<FinancialInformation> listBySupplierNumber(String supplierNumber);

    /**
     * 根据ID删除付款信息
     *
     * @param id 付款信息ID
     * @return 是否删除成功
     */
    int removeById(Long id);

    /**
     * 批量保存付款信息
     * @param financialInfoList 付款信息列表
     * @return 是否保存成功
     */
    boolean saveBatch(List<FinancialInformation> financialInfoList);


    /**
     * 新增付款信息
     * */
    int insert(FinancialInformation financialInfoList);

    /**
     * 同步金蝶供应商付款信息
     * */

    int querylinkmanList(List<List<Object>> querySupplierList);
}
