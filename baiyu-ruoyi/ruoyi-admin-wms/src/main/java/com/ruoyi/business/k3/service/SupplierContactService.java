package com.ruoyi.business.k3.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.business.entity.SupplierContactBase;

import java.util.List;

public interface SupplierContactService extends IService<SupplierContactBase> {



    /**
     * 同步金蝶供应商联系人信息
     * */

    int querylinkmanList(List<List<Object>> querySupplierList);



    /**
     * 新增供应商联系人
     * @param supplierContact 供应商联系人信息
     * @return 是否新增成功
     */
    int saveSupplierContact(SupplierContactBase supplierContact);

    /**
     * 更新供应商联系人信息
     * @param supplierContact 供应商联系人信息
     * @return 是否更新成功
     */
    boolean updateSupplierContact(SupplierContactBase supplierContact);

    /**
     * 根据供应商编码和联系人姓名获取联系人信息
     * @param supplierCode 供应商编码
     * @param contactName 联系人姓名
     * @return 联系人信息
     */
    SupplierContactBase getBySupplierCodeAndContactName(String supplierCode, String contactName);

    /**
     * 根据供应商编码获取所有联系人
     * @param supplierCode 供应商编码
     * @return 联系人列表
     */
    List<SupplierContactBase> listBySupplierCode(String supplierCode);

    /**
     * 根据供应商编码和联系人姓名删除联系人
     * @param supplierCode 供应商编码
     * @param contactName 联系人姓名
     * @return 是否删除成功
     */
    boolean remove(String supplierCode, String contactName);
}
