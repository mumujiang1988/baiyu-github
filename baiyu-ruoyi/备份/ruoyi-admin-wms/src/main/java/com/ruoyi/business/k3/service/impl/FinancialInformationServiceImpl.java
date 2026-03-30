package com.ruoyi.business.k3.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ruoyi.business.entity.FinancialInformation;
import com.ruoyi.business.k3.service.FinancialInformationService;
import com.ruoyi.business.mapper.FinancialInformationMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FinancialInformationServiceImpl implements FinancialInformationService {

    @Resource
    private FinancialInformationMapper financialInformationMapper;
    @Override
    public List<FinancialInformation> listBySupplierNumber(String supplierNumber) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("supplier_number", supplierNumber);
        return financialInformationMapper.selectListByQuery(queryWrapper);
    }

    @Override
    public int removeById(Long id) {
        return financialInformationMapper.deleteById(id);
    }

    @Override
    public boolean saveBatch(List<FinancialInformation> financialInfoList) {
        int count = 0;
        for (FinancialInformation financialInfo : financialInfoList) {
            count += financialInformationMapper.insert(financialInfo);
        }
        return count == financialInfoList.size();    }

    @Override
    public int insert(FinancialInformation financialInfoList) {


        return 0;
    }

    @Override
    public int querylinkmanList(List<List<Object>> querySupplierList) {
        int total = 0; // 统计总更新/插入数
        for (List<Object> rowData : querySupplierList) {
            FinancialInformation contactBase = new FinancialInformation();

            contactBase.setSupplierNumber(rowData.get(0) != null ? rowData.get(0).toString() : null);
            contactBase.setNation(rowData.get(1) != null ? rowData.get(1).toString() : null);
            contactBase.setBankAccount(rowData.get(2) != null ? rowData.get(2).toString() : null);
            contactBase.setAccountName(rowData.get(3) != null ? rowData.get(3).toString() : null);
            contactBase.setReceivingBank(rowData.get(4) != null ? rowData.get(4).toString() : null);
            contactBase.setBankAddress(rowData.get(5) != null ? rowData.get(5).toString() : null);
            contactBase.setOpeningBank(rowData.get(6) != null ? rowData.get(6).toString() : null);

          List<  FinancialInformation> existing = financialInformationMapper.selectByContractBse(contactBase.getSupplierNumber());

            int result;
            if (existing != null && !existing.isEmpty()) {
                // 已存在 → 更新
                result = financialInformationMapper.updateSupplierContact(contactBase);
            } else {
                // 不存在 → 新增
                result = financialInformationMapper.insertSupplierContact(contactBase);
            }

            if (result > 0) {
                total += result;
            }
        }

        return total; // 返回处理总数
    }
}
