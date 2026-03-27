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
 * 数据处理器
 * 
 * 提供统一的数据处理功能:
 * - 计算字段处理
 * - 虚拟字段处理
 * - 字典翻译处理
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
     * 处理数据 (计算字段 + 虚拟字段)
     * 
     * @param dataList 原始数据列表
     * @param config 配置 JSON
     * @return 处理后的数据列表
     */
    public List<Map<String, Object>> process(
            List<Map<String, Object>> dataList,
            JSONObject config) {
        
        if (CollUtil.isEmpty(dataList)) {
            log.debug("数据列表为空，跳过处理");
            return dataList;
        }
        
        // 1. 执行字段计算
        dataList = computeFields(dataList, config);
        
        // 2. 解析虚拟字段
        dataList = resolveVirtualFields(dataList, config);
        
        log.debug("数据处理完成，共处理{}条记录", dataList.size());
        return dataList;
    }
    
    /**
     * 执行字段计算
     * 
     * @param dataList 数据列表
     * @param config 配置 JSON
     * @return 计算后的数据列表
     */
    private List<Map<String, Object>> computeFields(
            List<Map<String, Object>> dataList,
            JSONObject config) {
        
        List<ComputedFieldConfig> configs = configParser.parseComputedFields(config);
        if (CollUtil.isEmpty(configs)) {
            log.debug("无计算字段配置，跳过计算");
            return dataList;
        }
        
        log.debug("开始执行字段计算，共{}个计算规则", configs.size());
        return computedFieldEngine.computeFieldsBatch(dataList, configs);
    }
    
    /**
     * 解析虚拟字段
     * 
     * @param dataList 数据列表
     * @param config 配置 JSON
     * @return 解析后的数据列表
     */
    private List<Map<String, Object>> resolveVirtualFields(
            List<Map<String, Object>> dataList,
            JSONObject config) {
        
        List<VirtualFieldConfig> configs = configParser.parseVirtualFields(config);
        if (CollUtil.isEmpty(configs)) {
            log.debug("无虚拟字段配置，跳过解析");
            return dataList;
        }
        
        log.debug("开始解析虚拟字段，共{}个映射规则", configs.size());
        return virtualFieldService.resolveVirtualFields(dataList, configs);
    }
    
    /**
     * 处理单条数据
     * 
     * @param data 原始数据
     * @param config 配置 JSON
     * @return 处理后的数据
     */
    public Map<String, Object> processSingle(
            Map<String, Object> data,
            JSONObject config) {
        
        if (data == null) {
            return data;
        }
        
        // 转换为列表处理
        List<Map<String, Object>> dataList = CollUtil.newArrayList(data);
        List<Map<String, Object>> processedList = process(dataList, config);
        
        return processedList.get(0);
    }
}
