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
 * ERP 字典 Controller - 低代码统一调用入口
 * 所有接口无 UNION、无 collation 冲突
 */
@RestController
@RequestMapping("/erp/dict")
public class ErpDictionaryController {

    @Autowired
    private ErpDictionaryService dictionaryService;

    // ==================== 核心接口（低代码常用）====================

    /**
     * 【低代码调用】获取合并字典（系统 + 业务）
     * 
     * 使用场景：
     * 1. 低代码页面需要同时显示系统字典和业务字典
     * 2. 前端无需关心数据来源，统一处理
     * 
     * @param dictType 字典类型（可选）
     *                 - 传值：只返回该类型的字典
     *                 - 不传：返回所有字典
     * @return 合并后的字典列表
     * 
     * 示例：
     * GET /erp/dict/getUnionDict?dictType=customer_category
     * GET /erp/dict/getUnionDict
     */
    @GetMapping("/getUnionDict")
    public List<Map<String, Object>> getUnionDict(@RequestParam(required = false) String dictType) {
        return dictionaryService.getUnionDict(dictType);
    }

    /**
     * 【低代码调用】获取全部字典数据
     * 
     * 使用场景：
     * 1. 初始化时加载所有字典到缓存
     * 2. 全局字典数据一次性获取
     * 
     * @return 所有字典数据（系统 + 业务）
     * 
     * 示例：
     * GET /erp/dict/getAllDict
     */
    @GetMapping("/getAllDict")
    public List<Map<String, Object>> getAllDict() {
        return dictionaryService.getAllDict();
    }

    // ==================== 系统字典接口 ====================

    /**
     * 查询系统字典类型
     * 
     * @param dictType 字典类型（可选）
     * @return 字典类型列表
     * 
     * 示例：
     * GET /erp/dict/getDictTypes
     * GET /erp/dict/getDictTypes?dictType=system_config
     */
    @GetMapping("/getDictTypes")
    public List<Map<String, Object>> getDictTypes(@RequestParam(required = false) String dictType) {
        return dictionaryService.getDictTypes(dictType);
    }

    // ==================== 业务字典接口 ====================

    /**
     * 根据分类查询业务字典
     * 
     * @param category 字典分类（必填）
     * @return 业务字典列表
     * 
     * 示例：
     * GET /erp/dict/getBizDictByCategory?category=customer_source
     * GET /erp/dict/getBizDictByCategory?category=price_type
     */
    @GetMapping("/getBizDictByCategory")
    public List<Map<String, Object>> getBizDictByCategory(@RequestParam String category) {
        return dictionaryService.getBizDictByCategory(category);
    }

    /**
     * 自定义值字段的业务字典查询
     * 
     * @param category 字典分类（必填）
     * @param valueField 值字段名（可选，默认 kingdee）
     * @return 业务字典列表
     * 
     * 示例：
     * GET /erp/dict/getBizDictCustom?category=product_type&valueField=code
     * GET /erp/dict/getBizDictCustom?category=customer_source&valueField=kingdee
     */
    @GetMapping("/getBizDictCustom")
    public List<Map<String, Object>> getBizDictCustom(
        @RequestParam String category,
        @RequestParam(defaultValue = "kingdee") String valueField) {
        return dictionaryService.getBizDictCustom(category, valueField);
    }

    // ==================== 国家字典接口 ====================

    /**
     * 根据国家 ID 查询详情
     * 
     * @param id 国家 ID（必填）
     * @return 国家详情
     * 
     * 示例：
     * GET /erp/dict/getCountryById?id=1
     */
    @GetMapping("/getCountryById")
    public Map<String, Object> getCountryById(@RequestParam Long id) {
        return dictionaryService.getCountryById(id);
    }

    /**
     * 查询所有可用国家
     * 
     * @return 国家列表
     * 
     * 示例：
     * GET /erp/dict/getAllCountries
     */
    @GetMapping("/getAllCountries")
    public List<Map<String, Object>> getAllCountries() {
        return dictionaryService.getAllCountries();
    }

    /**
     * 搜索国家（支持关键字和分页）
     * 
     * @param keyword 搜索关键字（可选）
     * @param limit 返回数量限制（可选，默认不限制）
     * @return 国家列表
     * 
     * 示例：
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
