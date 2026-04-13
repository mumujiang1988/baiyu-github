package com.ruoyi.erp.controller;
import com.ruoyi.erp.service.ErpDictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

/**
 * ERP Dictionary Controller   
 */
@RestController
@RequestMapping("/erp/dict")
public class ErpDictionaryController {

    @Autowired
    private ErpDictionaryService dictionaryService;

    // ==================== Core Interfaces (Commonly used in low-code) ====================

    /**
     * @param dictType Dictionary type (optional) 
     * @return Merged dictionary list
     * GET /erp/dict/getUnionDict?dictType=customer_category
     * GET /erp/dict/getUnionDict
     */
    @GetMapping("/getUnionDict")
    public List<Map<String, Object>> getUnionDict(@RequestParam(required = false) String dictType) {
        return dictionaryService.getUnionDict(dictType);
    }

    /**
     * [Low-code call] Get all dictionary data
     * 
     * Usage scenarios:
     * 1. Load all dictionaries to cache during initialization
     * 2. One-time retrieval of global dictionary data
     * 
     * @return All dictionary data (system + business)
     * 
     * Example:
     * GET /erp/dict/getAllDict
     */
    @GetMapping("/getAllDict")
    public List<Map<String, Object>> getAllDict() {
        return dictionaryService.getAllDict();
    }

    // ==================== System Dictionary Interfaces ====================

    /**
     * Query system dictionary types
     * 
     * @param dictType Dictionary type (optional)
     * @return Dictionary type list
     * 
     * Example:
     * GET /erp/dict/getDictTypes
     * GET /erp/dict/getDictTypes?dictType=system_config
     */
    @GetMapping("/getDictTypes")
    public List<Map<String, Object>> getDictTypes(@RequestParam(required = false) String dictType) {
        return dictionaryService.getDictTypes(dictType);
    }

    // ==================== Business Dictionary Interfaces ====================

    /**
     * Query business dictionary by category
     * 
     * @param category Dictionary category (required)
     * @return Business dictionary list
     * 
     * Example:
     * GET /erp/dict/getBizDictByCategory?category=customer_source
     * GET /erp/dict/getBizDictByCategory?category=price_type
     */
    @GetMapping("/getBizDictByCategory")
    public List<Map<String, Object>> getBizDictByCategory(@RequestParam String category) {
        return dictionaryService.getBizDictByCategory(category);
    }

    /**
     * Business dictionary query with custom value field
     * 
     * @param category Dictionary category (required)
     * @param valueField Value field name (optional, default is kingdee)
     * @return Business dictionary list
     * 
     * Example:
     * GET /erp/dict/getBizDictCustom?category=product_type&valueField=code
     * GET /erp/dict/getBizDictCustom?category=customer_source&valueField=kingdee
     */
    @GetMapping("/getBizDictCustom")
    public List<Map<String, Object>> getBizDictCustom(
        @RequestParam String category,
        @RequestParam(defaultValue = "kingdee") String valueField) {
        return dictionaryService.getBizDictCustom(category, valueField);
    }

    // ==================== Country Dictionary Interfaces ====================

    /**
     * Query country details by ID
     * 
     * @param id Country ID (required)
     * @return Country details
     * 
     * Example:
     * GET /erp/dict/getCountryById?id=1
     */
    @GetMapping("/getCountryById")
    public Map<String, Object> getCountryById(@RequestParam Long id) {
        return dictionaryService.getCountryById(id);
    }

    /**
     * Query all available countries
     * 
     * @return Country list
     * 
     * Example:
     * GET /erp/dict/getAllCountries
     */
    @GetMapping("/getAllCountries")
    public List<Map<String, Object>> getAllCountries() {
        return dictionaryService.getAllCountries();
    }

    /**
     * Search countries (supports keyword and pagination)
     * 
     * @param keyword Search keyword (optional)
     * @param limit Return quantity limit (optional, no limit by default)
     * @return Country list
     * 
     * Example:
     * GET /erp/dict/searchCountries?keyword=中国
     * GET /erp/dict/searchCountries?keyword=america&limit=10
     */
    @GetMapping("/searchCountries")
    public List<Map<String, Object>> searchCountries(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Integer limit) {
        return dictionaryService.searchCountries(keyword, limit);
    }
}
