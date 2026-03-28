package com.ruoyi.business.servicel.impl;

import com.ruoyi.business.entity.*;

import com.ruoyi.business.k3.domain.vo.SupplierVo;
import com.ruoyi.business.mapper.BymaterialDictionaryMapper;
import com.ruoyi.business.mapper.DictionaryTableMapper;
import com.ruoyi.business.mapper.SettlementMethodMapper;
import com.ruoyi.business.mapper.TaxRateMapper;
import com.ruoyi.business.servicel.DictionaryLookupServicel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DictionaryLookupServicelimpl implements DictionaryLookupServicel {
    @Autowired
    private DictionaryTableMapper dictionaryLookupMapper;
    @Autowired
    private BymaterialDictionaryMapper bymaterialDictionaryMapper;
    @Autowired
    private SettlementMethodMapper settlementMethodMapper;
    @Autowired
    private TaxRateMapper taxRateMapper;

    @Override
    public List<DictionaryTable> getDictionaryLookup() {
        // 1. 分别查询系统字典和业务字典
        List<DictionaryTable> systemDictionaries = dictionaryLookupMapper.selectAll();
        List<BymaterialDictionary> businessDictionaries = bymaterialDictionaryMapper.selectMaterialAll();
        
        // 2. 将业务字典转换为 DictionaryTable 格式
        List<DictionaryTable> businessDictTables = convertToDictionaryTables(businessDictionaries);
        
        // 3. 合并两个列表
        List<DictionaryTable> allDictionaries = new ArrayList<>();
        if (systemDictionaries != null) {
            allDictionaries.addAll(systemDictionaries);
        }
        if (businessDictTables != null) {
            allDictionaries.addAll(businessDictTables);
        }
        
        return allDictionaries;
    }
    
    /**
     * 将业务字典转换为 DictionaryTable 格式
     */
    private List<DictionaryTable> convertToDictionaryTables(List<BymaterialDictionary> businessDictionaries) {
        if (businessDictionaries == null || businessDictionaries.isEmpty()) {
            return Collections.emptyList();
        }
        
        return businessDictionaries.stream()
            .map(businessDict -> {
                DictionaryTable dictTable = new DictionaryTable();
                dictTable.setId(Long.valueOf(businessDict.getId()));
                dictTable.setBilhead(businessDict.getKingdee());
                dictTable.setDictName(businessDict.getName());
                dictTable.setDictCode(businessDict.getCode());
                dictTable.setParentCode(null); // 业务字典暂无父级概念
                dictTable.setSourceType("business"); // 标记为业务字典
                return dictTable;
            })
            .collect(Collectors.toList());
    }

    /**
     * 供应商分组
     * */
    @Override
    public List<SupplierGroups> getSupplierGroups() {
        List<SupplierGroups> suppliergroups = dictionaryLookupMapper.selectSupplierGroupsAll();
        return suppliergroups;
    }

    /**
     * 根据k3id查询供应商分组字典
     *
     * @param supplierGroup
     * @return 供应商分组字典信息
     */
    @Override
    public SupplierGroups SupplierGroupsK3id(String supplierGroup) {
        return dictionaryLookupMapper.SupplierGroupsK3id(supplierGroup);
    }

    /*物料属性*/
    @Override
    public List<BymaterialDictionary> categoryName(String categoryName, String category) {
        List<BymaterialDictionary> dictionaryLookup = bymaterialDictionaryMapper.selectCategoryName(categoryName.toString(),category);
        return  dictionaryLookup;
    }

    /**
     * 通code获取来源名称
     * */
    @Override
    public BymaterialDictionary categoryCode(String code) {
        return bymaterialDictionaryMapper.selectCategoryCode(code);
    }

    @Override
    public List<SettlementMethod> settlementMethod() {

        return  settlementMethodMapper.selectAll() ;
    }

    @Override
    public List<TaxRate> getByKingdee() {
        return taxRateMapper.selectAll();
    }

    /**
     * 供应商
    * */
    @Override
    public List<SupplierVo> listSuppliers() {
        return bymaterialDictionaryMapper.listSuppliers();
    }

    /**
    * 物料
    * */
    @Override
    public List<BymaterialDictionary> selectmaterial() {
        return bymaterialDictionaryMapper.selectmaterial();
    }


}
