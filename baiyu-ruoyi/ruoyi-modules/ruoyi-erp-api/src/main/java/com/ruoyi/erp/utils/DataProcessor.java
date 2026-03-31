package com.ruoyi.erp.utils;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.erp.service.engine.ComputedFieldConfig;
import com.ruoyi.erp.service.engine.ComputedFieldEngine;
import com.ruoyi.erp.service.engine.VirtualFieldConfig;
import com.ruoyi.erp.service.engine.VirtualFieldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Data Processor
 * 
 * Provides unified data processing functionality:
 * - Computed field processing
 * - Virtual field processing
 * - Dictionary translation
 * 
 * @author JMH
 * @date 2026-03-24
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataProcessor {
    
    private final ComputedFieldEngine computedFieldEngine;
    private final VirtualFieldService virtualFieldService;
    private final ConfigParser configParser;
    
    /**
     * Process data (computed fields + virtual fields)
     * 
     * @param dataList Original data list
     * @param config Configuration JSON
     * @return Processed data list
     */
    public List<Map<String, Object>> process(
            List<Map<String, Object>> dataList,
            JSONObject config) {
        
        if (CollUtil.isEmpty(dataList)) {
            return dataList;
        }
        
        // Execute field computation
        dataList = computeFields(dataList, config);
        
        // Resolve virtual fields
        dataList = resolveVirtualFields(dataList, config);
        
        return dataList;
    }
    
    /**
     * Execute field computation
     * 
     * @param dataList Data list
     * @param config Configuration JSON
     * @return Computed data list
     */
    private List<Map<String, Object>> computeFields(
            List<Map<String, Object>> dataList,
            JSONObject config) {
        
        List<ComputedFieldConfig> configs = configParser.parseComputedFields(config);
        if (CollUtil.isEmpty(configs)) {
            return dataList;
        }
        
        return computedFieldEngine.computeFieldsBatch(dataList, configs);
    }
    
    /**
     * Resolve virtual fields
     * 
     * @param dataList Data list
     * @param config Configuration JSON
     * @return Resolved data list
     */
    private List<Map<String, Object>> resolveVirtualFields(
            List<Map<String, Object>> dataList,
            JSONObject config) {
        
        List<VirtualFieldConfig> configs = configParser.parseVirtualFields(config);
        if (CollUtil.isEmpty(configs)) {
            return dataList;
        }
        
        return virtualFieldService.resolveVirtualFields(dataList, configs);
    }
    
    /**
     * Process single data
     * 
     * @param data Original data
     * @param config Configuration JSON
     * @return Processed data
     */
    public Map<String, Object> processSingle(
            Map<String, Object> data,
            JSONObject config) {
        
        if (data == null) {
            return data;
        }
        
        // Convert to list for processing
        List<Map<String, Object>> dataList = CollUtil.newArrayList(data);
        List<Map<String, Object>> processedList = process(dataList, config);
        
        return processedList.get(0);
    }
}
