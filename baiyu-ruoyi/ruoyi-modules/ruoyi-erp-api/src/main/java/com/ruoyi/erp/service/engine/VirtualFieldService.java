package com.ruoyi.erp.service.engine;

import cn.hutool.core.collection.CollUtil;
import com.ruoyi.common.redis.utils.CacheUtils;
import com.ruoyi.erp.exception.VirtualFieldException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 虚拟字段配置
 * 
 * @author ERP Development Team
 * @date 2026-03-24
 */
@Data
@Component
@Slf4j
public class VirtualFieldService {
    
    @Resource
    private JdbcTemplate jdbcTemplate;
    
    /**
     * 解析虚拟字段
     * 
     * @param dataList 原始数据列表
     * @param configs 虚拟字段配置列表
     * @return 解析后的数据列表
     */
    public List<Map<String, Object>> resolveVirtualFields(
            List<Map<String, Object>> dataList,
            List<VirtualFieldConfig> configs) {
        
        if (CollUtil.isEmpty(configs) || CollUtil.isEmpty(dataList)) {
            return dataList;
        }
        
        // 按配置逐个解析
        for (VirtualFieldConfig config : configs) {
            try {
                resolveSingleConfig(dataList, config);
            } catch (Exception e) {
                log.error("解析虚拟字段 {} 失败：{}", config.getName(), e.getMessage());
                // 虚拟字段解析失败不影响主数据
            }
        }
        
        return dataList;
    }
    
    /**
     * 解析单个配置
     */
    private void resolveSingleConfig(
            List<Map<String, Object>> dataList,
            VirtualFieldConfig config) {
        
        // 1. 收集所有源字段值
        Set<Object> sourceValues = dataList.stream()
            .map(data -> data.get(config.getSourceField()))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        
        if (CollUtil.isEmpty(sourceValues)) {
            return;
        }
        
        // 2. 批量查询基础资料
        Map<Object, Object> valueMap = batchQueryBaseData(
            config.getSourceTable(),
            config.getSourceField(),
            config.getSourceDisplayField(),
            sourceValues,
            config.getCacheable() != null && config.getCacheable()
        );
        
        // 3. 设置虚拟字段值
        for (Map<String, Object> data : dataList) {
            Object sourceValue = data.get(config.getSourceField());
            if (sourceValue != null) {
                Object displayValue = valueMap.get(sourceValue);
                data.put(config.getName(), displayValue);
                
                // 如果配置了显示类型，同时存储显示配置
                if (config.getDisplayConfig() != null) {
                    data.put(config.getName() + "_displayConfig", config.getDisplayConfig());
                }
            }
        }
    }
    
    /**
     * 批量查询基础资料
     */
    private Map<Object, Object> batchQueryBaseData(
            String tableName,
            String sourceField,
            String displayField,
            Set<Object> sourceValues,
            Boolean cacheable) {
        
        Map<Object, Object> result = new HashMap<>();
        Set<Object> needQueryValues = new HashSet<>();
        
        // 1. 尝试从缓存获取
        if (Boolean.TRUE.equals(cacheable)) {
            for (Object value : sourceValues) {
                String cacheKey = buildCacheKey(tableName, sourceField, value);
                Object cachedValue = CacheUtils.get("virtual_field", cacheKey);
                if (cachedValue != null) {
                    result.put(value, cachedValue);
                } else {
                    needQueryValues.add(value);
                }
            }
            log.debug("虚拟字段缓存命中：{}/{}", result.size(), sourceValues.size());
        } else {
            needQueryValues.addAll(sourceValues);
        }
        
        // 2. 查询数据库
        if (CollUtil.isNotEmpty(needQueryValues)) {
            try {
                String sql = buildBatchQuerySql(tableName, sourceField, displayField, needQueryValues.size());
                List<Map<String, Object>> queryResult = jdbcTemplate.queryForList(
                    sql, 
                    needQueryValues.toArray()
                );
                
                // 3. 缓存查询结果
                for (Map<String, Object> row : queryResult) {
                    Object sourceValue = row.get(sourceField);
                    Object displayValue = row.get(displayField);
                    result.put(sourceValue, displayValue);
                    
                    if (Boolean.TRUE.equals(cacheable)) {
                        String cacheKey = buildCacheKey(tableName, sourceField, sourceValue);
                        CacheUtils.put("virtual_field", cacheKey, displayValue);
                    }
                }
                
                log.debug("虚拟字段数据库查询：{} 条记录", queryResult.size());
            } catch (Exception e) {
                throw new VirtualFieldException(
                    tableName, 
                    "查询基础资料失败：" + e.getMessage(), 
                    e
                );
            }
        }
        
        return result;
    }
    
    /**
     * 构建批量查询 SQL
     */
    private String buildBatchQuerySql(String tableName, String sourceField, String displayField, int paramCount) {
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < paramCount; i++) {
            if (i > 0) {
                placeholders.append(",");
            }
            placeholders.append("?");
        }
        
        return String.format(
            "SELECT %s, %s FROM %s WHERE %s IN (%s)",
            sourceField, displayField, tableName, sourceField, placeholders
        );
    }
    
    /**
     * 构建缓存键
     */
    private String buildCacheKey(String tableName, String sourceField, Object value) {
        return String.format("virtual_field:%s:%s:%s", tableName, sourceField, value);
    }
}
