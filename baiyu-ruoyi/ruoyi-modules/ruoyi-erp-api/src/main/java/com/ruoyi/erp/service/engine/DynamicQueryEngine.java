package com.ruoyi.erp.service.engine;

import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.erp.utils.SqlBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Dynamic Query Engine (Pure JDBC Architecture)
 * @author JMH
 * @date 2026-03-30
 */
@Slf4j
@Component("erpDynamicQueryEngine")
public class DynamicQueryEngine {
 
    @Autowired
    private SqlBuilder sqlBuilder;

    @SuppressWarnings("unchecked")
    public SqlBuilder.SqlResult buildQueryConditions(
            Map<String, Object> searchConfig,
            Map<String, Object> queryParams) {
        
        if (searchConfig == null || queryParams == null) {
            return new SqlBuilder.SqlResult("", new ArrayList<>());
        }

        try {
            Object fieldsObj = searchConfig.get("fields");
            if (!(fieldsObj instanceof java.util.List)) {
                log.warn("searchConfig.fields is not List type, returning empty conditions");
                return new SqlBuilder.SqlResult("", new ArrayList<>());
            }

            java.util.List<Map<String, Object>> fields = 
                (java.util.List<Map<String, Object>>) fieldsObj;
            
            log.debug("Starting to build query conditions, field count: {}", fields.size());

            List<Map<String, Object>> conditions = new ArrayList<>();
            for (Map<String, Object> fieldConfig : fields) {
                String field = (String) fieldConfig.get("field");
                String searchType = (String) fieldConfig.get("searchType");
                
                if (StringUtils.isEmpty(field)) {
                    log.warn("Field in configuration is empty, skipping this configuration");
                    continue;
                }
                
                Object value = queryParams.get(field);
                if (value == null) {
                    continue;
                }
                
                if (value instanceof String && StringUtils.isEmpty((String) value)) {
                    continue;
                }
                
                Map<String, Object> condition = new HashMap<>();
                condition.put("field", field);
                condition.put("operator", convertSearchTypeToOperator(searchType));
                condition.put("value", value);
                conditions.add(condition);
            }

            SqlBuilder.SqlResult result = sqlBuilder.buildWhere(conditions);
            
            log.info("Dynamic query conditions built successfully, field count: {}", fields.size());
            
            return result;

        } catch (Exception e) {
            log.error("Dynamic query condition building failed", e);
            throw new RuntimeException("Query condition building failed: " + e.getMessage(), e);
        }
    }

    private String convertSearchTypeToOperator(String searchType) {
        switch (StringUtils.defaultString(searchType)) {
            case "like": return "like";
            case "left_like": return "left_like";
            case "right_like": return "right_like";
            case "in": return "in";
            case "between": return "between";
            case "gt": return "gt";
            case "ge": return "ge";
            case "lt": return "lt";
            case "le": return "le";
            case "ne": return "ne";
            default: return "eq";
        }
    }

    /**
     * Parse sort configuration (New architecture - returns ORDER BY SQL fragment)
     * 
     * @param sortConfig Sort configuration
     * @return SqlBuilder.SqlResult (contains ORDER BY SQL fragment)
     */
    public SqlBuilder.SqlResult applySortConfig(Map<String, Object> sortConfig) {
        if (sortConfig == null) {
            return new SqlBuilder.SqlResult("", new ArrayList<>());
        }

        String orderBy = (String) sortConfig.get("orderBy");
        String orderDirection = (String) sortConfig.get("orderDirection");

        if (StringUtils.isNotEmpty(orderBy)) {
            List<Map<String, Object>> orderByList = new ArrayList<>();
            Map<String, Object> order = new HashMap<>();
            order.put("field", orderBy);
            order.put("direction", "desc".equalsIgnoreCase(orderDirection) ? "desc" : "asc");
            orderByList.add(order);
            
            return sqlBuilder.buildOrderBy(orderByList);
        }
        
        return new SqlBuilder.SqlResult("", new ArrayList<>());
    }

}
