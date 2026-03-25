package com.ruoyi.business.k3.service.impl;


import com.ruoyi.business.entity.SupplierContactBase;
import com.ruoyi.business.k3.service.SupplierContactService;
import com.ruoyi.business.mapper.SupplierContactMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SupplierContactServiceImpl extends ServiceImpl<SupplierContactMapper, SupplierContactBase> implements SupplierContactService {

    @Override
    public int querylinkmanList(List<List<Object>> querySupplierList) {
        int total = 0; // 统计总更新/插入数
        for (List<Object> rowData : querySupplierList) {
            SupplierContactBase contactBase = new SupplierContactBase();

            contactBase.setSupplierCode(rowData.get(0) != null ? rowData.get(0).toString() : null);
            contactBase.setLocationName(rowData.get(1) != null ? rowData.get(1).toString() : null);
            contactBase.setContactName(rowData.get(2) != null ? rowData.get(2).toString() : null);
            contactBase.setPosition(rowData.get(3) != null ? rowData.get(3).toString() : null);
            contactBase.setPhone(rowData.get(4) != null ? rowData.get(4).toString() : null);
            contactBase.setMobile(rowData.get(5) != null ? rowData.get(5).toString() : null);
            contactBase.setEmail(rowData.get(6) != null ? rowData.get(6).toString() : null);
            contactBase.setQq(rowData.get(7) != null ? rowData.get(7).toString() : null);
            contactBase.setAddress(rowData.get(8) != null ? rowData.get(8).toString() : null);
            contactBase.setDefaultContact(rowData.get(9) != null ? rowData.get(9).toString() : null);


            // 检查是否已存在该联系人（根据供应商编码和联系人姓名）
            SupplierContactBase existing = this.getBaseMapper().selectByContractBseAndContactName(
                contactBase.getSupplierCode(), contactBase.getContactName());

            int result;
            if (existing != null) {
                // 已存在 → 更新
                contactBase.setId(existing.getId()); // 设置ID以便更新
                result = this.getBaseMapper().updateSupplierContact(contactBase);
            } else {
                // 不存在 → 新增
                result = this.getBaseMapper().insertSupplierContact(contactBase);
            }

            if (result > 0) {
                total += result; // 累加成功条数
            }
        }

        return total; // 返回处理总数
    }



    @Override
    public int saveSupplierContact(SupplierContactBase supplierContact) {
        return this.getBaseMapper().insert(supplierContact);

    }

    @Override
    public boolean updateSupplierContact(SupplierContactBase supplierContact) {
        return this.getBaseMapper().updateSupplierContact(supplierContact) > 0;
    }

    @Override
    public SupplierContactBase getBySupplierCodeAndContactName(String supplierCode, String contactName) {
        return this.getBaseMapper().selectByContractBseAndContactName(supplierCode, contactName);
    }

    @Override
    public List<SupplierContactBase> listBySupplierCode(String supplierCode) {
        return this.getBaseMapper().selectByContractBse(supplierCode);
    }

    @Override
    public boolean remove(String supplierCode, String contactName) {
        // 直接根据供应商编码删除联系人
        return this.getBaseMapper().deleteSupplierContact(supplierCode) > 0;
    }


}
