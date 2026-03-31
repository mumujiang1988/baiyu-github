package com.ruoyi.erp.service;

import com.ruoyi.erp.mapper.ErpDictionaryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ERP 字典 Service - Java 代码合并替代 UNION SQL
 * 彻底解决 collation 冲突问题
 */
@Service
public class ErpDictionaryService {

    @Autowired
    private ErpDictionaryMapper erpDictionaryMapper;

    /**
     * 【核心方法】Java 代码合并系统字典 + 业务字典
     * 替代原有的 UNION SQL，避免 collation 冲突
     * 
     * @param dictType 字典类型（可选）
     * @return 合并后的字典列表
     */
    public List<Map<String, Object>> getUnionDict(String dictType) {
        List<Map<String, Object>> list = new ArrayList<>();
        
        // 特殊处理：销售人员字典
        if ("salespersons".equals(dictType)) {
            List<Map<String, Object>> salespersons = erpDictionaryMapper.selectSalespersonsDict();
            if (salespersons != null) {
                list.addAll(salespersons);
            }
            return list;
        }
        
        // 1. 查询系统字典
        List<Map<String, Object>> sysDicts = erpDictionaryMapper.selectSysDictData(dictType);
        if (sysDicts != null) {
            list.addAll(sysDicts);
        }
        
        // 2. 查询业务字典
        List<Map<String, Object>> bizDicts = erpDictionaryMapper.selectBizDictData(dictType);
        if (bizDicts != null) {
            list.addAll(bizDicts);
        }
        
        return list;
    }

    /**
     * 获取全部字典（无 UNION 纯 Java 合并）
     * 
     * @return 所有字典数据
     */
    public List<Map<String, Object>> getAllDict() {
        List<Map<String, Object>> list = new ArrayList<>();
        
        // 1. 查询所有系统字典
        List<Map<String, Object>> sysDicts = erpDictionaryMapper.selectSysDictData(null);
        if (sysDicts != null) {
            list.addAll(sysDicts);
        }
        
        // 2. 查询所有业务字典
        List<Map<String, Object>> bizDicts = erpDictionaryMapper.selectBizDictData(null);
        if (bizDicts != null) {
            list.addAll(bizDicts);
        }
        
        // 3. 查询销售人员字典
        List<Map<String, Object>> salespersons = erpDictionaryMapper.selectSalespersonsDict();
        if (salespersons != null) {
            list.addAll(salespersons);
        }
        
        // 4. 查询客户字典
        List<Map<String, Object>> customers = erpDictionaryMapper.selectCustomersDict();
        if (customers != null) {
            list.addAll(customers);
        }
        
        // 5. 查询物料字典
        List<Map<String, Object>> materials = erpDictionaryMapper.selectMaterialsDict();
        if (materials != null) {
            list.addAll(materials);
        }
        
        // 6. 查询用户字典（新增）
        List<Map<String, Object>> users = erpDictionaryMapper.selectUsersDict();
        if (users != null) {
            list.addAll(users);
        }
        
        // 7. 查询供应商字典（新增）
        List<Map<String, Object>> suppliers = erpDictionaryMapper.selectSuppliersDict();
        if (suppliers != null) {
            list.addAll(suppliers);
        }
        
        // 8. 查询部门字典（新增）
        List<Map<String, Object>> departments = erpDictionaryMapper.selectDepartmentsDict();
        if (departments != null) {
            list.addAll(departments);
        }
        
        // 9. 查询税率字典（新增）
        List<Map<String, Object>> taxRates = erpDictionaryMapper.selectTaxRatesDict();
        if (taxRates != null) {
            list.addAll(taxRates);
        }
        
        return list;
    }

    // ==================== 以下保留原有便捷方法 ====================

    /**
     * 查询系统字典类型
     */
    public List<Map<String, Object>> getDictTypes(String dictType) {
        return erpDictionaryMapper.selectDictTypes(dictType);
    }

    /**
     * 根据分类查询业务字典
     */
    public List<Map<String, Object>> getBizDictByCategory(String category) {
        return erpDictionaryMapper.selectBizDictByCategory(category);
    }

    /**
     * 自定义值字段的业务字典查询
     */
    public List<Map<String, Object>> getBizDictCustom(String category, String valueField) {
        return erpDictionaryMapper.selectBizDictCustom(category, valueField);
    }

    /**
     * 根据国家 ID 查询
     */
    public Map<String, Object> getCountryById(Long id) {
        return erpDictionaryMapper.selectCountryById(id);
    }

    /**
     * 查询所有可用国家
     */
    public List<Map<String, Object>> getAllCountries() {
        return erpDictionaryMapper.selectAllCountries();
    }

    /**
     * 搜索国家
     */
    public List<Map<String, Object>> searchCountries(String keyword, Integer limit) {
        return erpDictionaryMapper.searchCountries(keyword, limit);
    }
}
