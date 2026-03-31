package com.ruoyi.erp.utils;

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
 * Configuration Parser
 * 
 * Provides unified configuration parsing for JSON, table columns,
 * form fields, computed fields, virtual fields, and dictionary configs.
 * 
 * @author JMH
 * @date 2026-03-24
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigParser {
    
    private final ErpPageConfigService erpPageConfigService;
    
    /**
     * Get configuration JSON
     * 
     * @param moduleCode Module code
     * @return Configuration JSON object
     */
    public JSONObject getConfig(String moduleCode) {
        ErpPageConfig config = erpPageConfigService.getByModuleCode(moduleCode);
        if (config == null) {
            throw new ErpConfigException(moduleCode, "CONFIG_NOT_FOUND", "Config not found");
        }
        // Fix: Combine 5 JSON fields into one object
        JSONObject result = new JSONObject();
        result.put("pageConfig", parseJson(config.getPageConfig()));
        result.put("formConfig", parseJson(config.getFormConfig()));
        result.put("tableConfig", parseJson(config.getTableConfig()));
        result.put("dictionaryConfig", parseJson(config.getDictConfig()));
        result.put("businessConfig", parseJson(config.getBusinessConfig()));
        result.put("moduleCode", config.getModuleCode());
        result.put("configName", config.getConfigName());
        result.put("version", config.getVersion());
        return result;
    }
    
    /**
     * Parse JSON string to object
     * @param jsonStr JSON string
     * @return Parsed object, null if empty
     */
    private Object parseJson(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            return null;
        }
        try {
            return JSON.parse(jsonStr);
        } catch (Exception e) {
            log.error("[parseJson] JSON parsing failed: {}", jsonStr, e);
            return null;
        }
    }
    
    /**
     * Parse configuration list (generic)
     * 
     * @param configJson Configuration JSON
     * @param configKey Config item name (e.g., tableConfig, formConfig)
     * @param arrayKey Array key (e.g., columns, fields)
     * @param clazz Target type
     * @param <T> Generic type
     * @return Configuration list
     */
    public <T> List<T> parseConfigList(
            JSONObject configJson,
            String configKey,
            String arrayKey,
            Class<T> clazz) {
        
        JSONObject config = configJson.getJSONObject(configKey);
        if (config == null) {
            return Collections.emptyList();
        }
        
        JSONArray array = config.getJSONArray(arrayKey);
        if (array == null || array.isEmpty()) {
            return Collections.emptyList();
        }
        
        return array.stream()
            .map(obj -> JSON.parseObject(obj.toString(), clazz))
            .collect(Collectors.toList());
    }
    
    /**
     * Parse table column configuration
     * @param configJson Configuration JSON
     * @return Table column configuration list
     */
    public List<TableColumnConfig> parseTableColumns(JSONObject configJson) {
        return parseConfigList(configJson, "tableConfig", "columns", TableColumnConfig.class);
    }
    
    /**
     * Parse form field configuration
     * @param configJson Configuration JSON
     * @return Form field configuration list
     */
    public List<FormConfig> parseFormFields(JSONObject configJson) {
        return parseConfigList(configJson, "formConfig", "fields", FormConfig.class);
    }
    
    /**
     * Parse computed field configuration
     * @param configJson Configuration JSON
     * @return Computed field configuration list
     */
    public List<ComputedFieldConfig> parseComputedFields(JSONObject configJson) {
        return parseConfigList(configJson, "computedFieldConfig", "fields", ComputedFieldConfig.class);
    }
    
    /**
     * Parse virtual field configuration
     * @param configJson Configuration JSON
     * @return Virtual field configuration list
     */
    public List<VirtualFieldConfig> parseVirtualFields(JSONObject configJson) {
        return parseConfigList(configJson, "virtualFieldConfig", "fields", VirtualFieldConfig.class);
    }
    
    /**
     * Parse dictionary configuration
     * @param configJson Configuration JSON
     * @return Dictionary configuration map
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
     * Get table configuration
     * @param configJson Configuration JSON
     * @return Table configuration JSON
     */
    public JSONObject getTableConfig(JSONObject configJson) {
        return configJson.getJSONObject("tableConfig");
    }
    
    /**
     * Get form configuration
     * @param configJson Configuration JSON
     * @return Form configuration JSON
     */
    public JSONObject getFormConfig(JSONObject configJson) {
        return configJson.getJSONObject("formConfig");
    }
    
    /**
     * Get computed field configuration
     * @param configJson Configuration JSON
     * @return Computed field configuration JSON
     */
    public JSONObject getComputedFieldConfig(JSONObject configJson) {
        return configJson.getJSONObject("computedFieldConfig");
    }
    
    /**
     * Get virtual field configuration
     * @param configJson Configuration JSON
     * @return Virtual field configuration JSON
     */
    public JSONObject getVirtualFieldConfig(JSONObject configJson) {
        return configJson.getJSONObject("virtualFieldConfig");
    }
}
