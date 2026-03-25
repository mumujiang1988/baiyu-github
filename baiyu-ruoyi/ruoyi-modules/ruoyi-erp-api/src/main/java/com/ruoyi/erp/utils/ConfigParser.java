package com.ruoyi.erp.utils;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.erp.domain.entity.ErpPageConfig;
import com.ruoyi.erp.exception.ErpConfigException;
import com.ruoyi.erp.service.ErpPageConfigService;
import com.ruoyi.erp.service.engine.ComputedFieldConfig;
import com.ruoyi.erp.service.engine.DictionaryConfig;
import com.ruoyi.erp.service.engine.FormConfig;
import com.ruoyi.erp.service.engine.TableColumnConfig;
import com.ruoyi.erp.service.engine.VirtualFieldConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 配置解析器
 * 
 * 提供统一的配置解析功能:
 * - JSON 配置解析
 * - 表格列配置解析
 * - 表单字段配置解析
 * - 计算字段配置解析
 * - 虚拟字段配置解析
 * - 字典配置解析
 * 
 * @author ERP Development Team
 * @date 2026-03-24
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigParser {
    
    private final ErpPageConfigService erpPageConfigService;
    
    /**
     * 获取配置 JSON
     * 
     * @param moduleCode 模块编码
     * @return 配置 JSON 对象
     */
    public JSONObject getConfig(String moduleCode) {
        ErpPageConfig config = erpPageConfigService.getByModuleCode(moduleCode);
        if (config == null) {
            throw new ErpConfigException(moduleCode, "CONFIG_NOT_FOUND", "配置不存在");
        }
        return JSON.parseObject(config.getConfigContent());
    }
    
    /**
     * 解析配置列表 (通用方法)
     * 
     * @param configJson 配置 JSON
     * @param configKey 配置项名称 (如 tableConfig、formConfig)
     * @param arrayKey 数组键名 (如 columns、fields)
     * @param clazz 目标类型
     * @param <T> 泛型
     * @return 配置列表
     */
    public <T> List<T> parseConfigList(
            JSONObject configJson,
            String configKey,
            String arrayKey,
            Class<T> clazz) {
        
        JSONObject config = configJson.getJSONObject(configKey);
        if (config == null) {
            log.debug("配置不存在：configKey={}", configKey);
            return Collections.emptyList();
        }
        
        JSONArray array = config.getJSONArray(arrayKey);
        if (array == null || array.isEmpty()) {
            log.debug("配置数组为空：configKey={}, arrayKey={}", configKey, arrayKey);
            return Collections.emptyList();
        }
        
        return array.stream()
            .map(obj -> JSON.parseObject(obj.toString(), clazz))
            .collect(Collectors.toList());
    }
    
    /**
     * 解析表格列配置
     * 
     * @param configJson 配置 JSON
     * @return 表格列配置列表
     */
    public List<TableColumnConfig> parseTableColumns(JSONObject configJson) {
        return parseConfigList(configJson, "tableConfig", "columns", TableColumnConfig.class);
    }
    
    /**
     * 解析表单字段配置
     * 
     * @param configJson 配置 JSON
     * @return 表单字段配置列表
     */
    public List<FormConfig> parseFormFields(JSONObject configJson) {
        return parseConfigList(configJson, "formConfig", "fields", FormConfig.class);
    }
    
    /**
     * 解析计算字段配置
     * 
     * @param configJson 配置 JSON
     * @return 计算字段配置列表
     */
    public List<ComputedFieldConfig> parseComputedFields(JSONObject configJson) {
        return parseConfigList(configJson, "computedFieldConfig", "fields", ComputedFieldConfig.class);
    }
    
    /**
     * 解析虚拟字段配置
     * 
     * @param configJson 配置 JSON
     * @return 虚拟字段配置列表
     */
    public List<VirtualFieldConfig> parseVirtualFields(JSONObject configJson) {
        return parseConfigList(configJson, "virtualFieldConfig", "fields", VirtualFieldConfig.class);
    }
    
    /**
     * 解析字典配置
     * 
     * @param configJson 配置 JSON
     * @return 字典配置 Map
     */
    public Map<String, DictionaryConfig> parseDictionaryConfig(JSONObject configJson) {
        JSONObject dictConfig = configJson.getJSONObject("dictionaryConfig");
        if (dictConfig == null) {
            return Collections.emptyMap();
        }
        
        Map<String, DictionaryConfig> result = new HashMap<>();
        for (String key : dictConfig.keySet()) {
            result.put(key, dictConfig.getObject(key, DictionaryConfig.class));
        }
        return result;
    }
    
    /**
     * 获取表格配置
     * 
     * @param configJson 配置 JSON
     * @return 表格配置 JSON
     */
    public JSONObject getTableConfig(JSONObject configJson) {
        return configJson.getJSONObject("tableConfig");
    }
    
    /**
     * 获取表单配置
     * 
     * @param configJson 配置 JSON
     * @return 表单配置 JSON
     */
    public JSONObject getFormConfig(JSONObject configJson) {
        return configJson.getJSONObject("formConfig");
    }
    
    /**
     * 获取计算字段配置
     * 
     * @param configJson 配置 JSON
     * @return 计算字段配置 JSON
     */
    public JSONObject getComputedFieldConfig(JSONObject configJson) {
        return configJson.getJSONObject("computedFieldConfig");
    }
    
    /**
     * 获取虚拟字段配置
     * 
     * @param configJson 配置 JSON
     * @return 虚拟字段配置 JSON
     */
    public JSONObject getVirtualFieldConfig(JSONObject configJson) {
        return configJson.getJSONObject("virtualFieldConfig");
    }
}
