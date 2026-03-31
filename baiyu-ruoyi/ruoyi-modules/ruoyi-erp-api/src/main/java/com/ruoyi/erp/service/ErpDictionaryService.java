package com.ruoyi.erp.service;

import com.ruoyi.erp.mapper.ErpDictionaryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ERP Dictionary Service - Java code merge alternative to UNION SQL
 * Completely solve collation conflict issues
 */
@Service
public class ErpDictionaryService {

    @Autowired
    private ErpDictionaryMapper erpDictionaryMapper;

    /**
     * [Core Method] Java code merge system dictionary + business dictionary
     * Replace original UNION SQL, avoid collation conflicts
     * 
     * @param dictType Dictionary type (optional)
     * @return Merged dictionary list
     */
    public List<Map<String, Object>> getUnionDict(String dictType) {
        List<Map<String, Object>> list = new ArrayList<>();
        
        // Special handling: salespersons dictionary
        if ("salespersons".equals(dictType)) {
            List<Map<String, Object>> salespersons = erpDictionaryMapper.selectSalespersonsDict();
            if (salespersons != null) {
                list.addAll(salespersons);
            }
            return list;
        }
        
        // 1. Query system dictionary
        List<Map<String, Object>> sysDicts = erpDictionaryMapper.selectSysDictData(dictType);
        if (sysDicts != null) {
            list.addAll(sysDicts);
        }
        
        // 2. Query business dictionary
        List<Map<String, Object>> bizDicts = erpDictionaryMapper.selectBizDictData(dictType);
        if (bizDicts != null) {
            list.addAll(bizDicts);
        }
        
        return list;
    }

    /**
     * Get all dictionaries (no UNION, pure Java merge)
     * 
     * @return All dictionary data
     */
    public List<Map<String, Object>> getAllDict() {
        List<Map<String, Object>> list = new ArrayList<>();
        
        // 1. Query all system dictionaries
        List<Map<String, Object>> sysDicts = erpDictionaryMapper.selectSysDictData(null);
        if (sysDicts != null) {
            list.addAll(sysDicts);
        }
        
        // 2. Query all business dictionaries
        List<Map<String, Object>> bizDicts = erpDictionaryMapper.selectBizDictData(null);
        if (bizDicts != null) {
            list.addAll(bizDicts);
        }
        
        // 3. Query salespersons dictionary
        List<Map<String, Object>> salespersons = erpDictionaryMapper.selectSalespersonsDict();
        if (salespersons != null) {
            list.addAll(salespersons);
        }
        
        // 4. Query customers dictionary
        List<Map<String, Object>> customers = erpDictionaryMapper.selectCustomersDict();
        if (customers != null) {
            list.addAll(customers);
        }
        
        // 5. Query materials dictionary
        List<Map<String, Object>> materials = erpDictionaryMapper.selectMaterialsDict();
        if (materials != null) {
            list.addAll(materials);
        }
        
        // 6. Query users dictionary (new)
        List<Map<String, Object>> users = erpDictionaryMapper.selectUsersDict();
        if (users != null) {
            list.addAll(users);
        }
        
        // 7. Query suppliers dictionary (new)
        List<Map<String, Object>> suppliers = erpDictionaryMapper.selectSuppliersDict();
        if (suppliers != null) {
            list.addAll(suppliers);
        }
        
        // 8. Query departments dictionary (new)
        List<Map<String, Object>> departments = erpDictionaryMapper.selectDepartmentsDict();
        if (departments != null) {
            list.addAll(departments);
        }
        
        // 9. Query tax rates dictionary (new)
        List<Map<String, Object>> taxRates = erpDictionaryMapper.selectTaxRatesDict();
        if (taxRates != null) {
            list.addAll(taxRates);
        }
        
        return list;
    }

    // ==================== Retain original convenience methods below ====================

    /**
     * Query system dictionary types
     */
    public List<Map<String, Object>> getDictTypes(String dictType) {
        return erpDictionaryMapper.selectDictTypes(dictType);
    }

    /**
     * Query business dictionaries by category
     */
    public List<Map<String, Object>> getBizDictByCategory(String category) {
        return erpDictionaryMapper.selectBizDictByCategory(category);
    }

    /**
     * Custom value field business dictionary query
     */
    public List<Map<String, Object>> getBizDictCustom(String category, String valueField) {
        return erpDictionaryMapper.selectBizDictCustom(category, valueField);
    }

    /**
     * Query by country ID
     */
    public Map<String, Object> getCountryById(Long id) {
        return erpDictionaryMapper.selectCountryById(id);
    }

    /**
     * Query all available countries
     */
    public List<Map<String, Object>> getAllCountries() {
        return erpDictionaryMapper.selectAllCountries();
    }

    /**
     * Search countries
     */
    public List<Map<String, Object>> searchCountries(String keyword, Integer limit) {
        return erpDictionaryMapper.searchCountries(keyword, limit);
    }
}
