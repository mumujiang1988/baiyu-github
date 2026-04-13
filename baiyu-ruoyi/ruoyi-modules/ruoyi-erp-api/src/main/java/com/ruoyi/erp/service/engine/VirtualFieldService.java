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
import java.util.stream.Collectors;

/**
 * Virtual Field Service
 * 
 * @author JMH
 * @date 2026-03-24
 */
@Data
@Component
@Slf4j
public class VirtualFieldService {
    
    @Resource
    private JdbcTemplate jdbcTemplate;
    
    public List<Map<String, Object>> resolveVirtualFields(
            List<Map<String, Object>> dataList,
            List<VirtualFieldConfig> configs) {
        
        if (CollUtil.isEmpty(configs) || CollUtil.isEmpty(dataList)) {
            return dataList;
        }
        
        for (VirtualFieldConfig config : configs) {
            try {
                resolveSingleConfig(dataList, config);
            } catch (Exception e) {
                log.error("Failed to resolve virtual field {}: {}", config.getName(), e.getMessage());
            }
        }
        
        return dataList;
    }
    
    private void resolveSingleConfig(
            List<Map<String, Object>> dataList,
            VirtualFieldConfig config) {
        
        Set<Object> sourceValues = dataList.stream()
            .map(data -> data.get(config.getSourceField()))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        
        if (CollUtil.isEmpty(sourceValues)) {
            return;
        }
        
        Map<Object, Object> valueMap = batchQueryBaseData(
            config.getSourceTable(),
            config.getSourceField(),
            config.getSourceDisplayField(),
            sourceValues,
            config.getCacheable() != null && config.getCacheable()
        );
        
        for (Map<String, Object> data : dataList) {
            Object sourceValue = data.get(config.getSourceField());
            if (sourceValue != null) {
                Object displayValue = valueMap.get(sourceValue);
                data.put(config.getName(), displayValue);
                
                if (config.getDisplayConfig() != null) {
                    data.put(config.getName() + "_displayConfig", config.getDisplayConfig());
                }
            }
        }
    }
    
    private Map<Object, Object> batchQueryBaseData(
            String tableName,
            String sourceField,
            String displayField,
            Set<Object> sourceValues,
            Boolean cacheable) {
        
        Map<Object, Object> result = new HashMap<>();
        Set<Object> needQueryValues = new HashSet<>();
        
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
            log.debug("Virtual field cache hit: {}/{}", result.size(), sourceValues.size());
        } else {
            needQueryValues.addAll(sourceValues);
        }
        
        if (CollUtil.isNotEmpty(needQueryValues)) {
            try {
                String sql = buildBatchQuerySql(tableName, sourceField, displayField, needQueryValues.size());
                List<Map<String, Object>> queryResult = jdbcTemplate.queryForList(
                    sql, 
                    needQueryValues.toArray()
                );
                
                for (Map<String, Object> row : queryResult) {
                    Object sourceValue = row.get(sourceField);
                    Object displayValue = row.get(displayField);
                    result.put(sourceValue, displayValue);
                    
                    if (Boolean.TRUE.equals(cacheable)) {
                        String cacheKey = buildCacheKey(tableName, sourceField, sourceValue);
                        CacheUtils.put("virtual_field", cacheKey, displayValue);
                    }
                }
                
                log.debug("Virtual field database query: {} records", queryResult.size());
            } catch (Exception e) {
                throw new VirtualFieldException(
                    tableName, 
                    "Failed to query base data: " + e.getMessage(), 
                    e
                );
            }
        }
        
        return result;
    }
    
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
    
    private String buildCacheKey(String tableName, String sourceField, Object value) {
        return String.format("virtual_field:%s:%s:%s", tableName, sourceField, value);
    }
}
