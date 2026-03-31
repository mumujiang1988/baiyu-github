package com.ruoyi.erp.service.engine;

import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.erp.utils.SqlBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 动态查询引擎（纯 JDBC 架构）
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
                log.warn("searchConfig.fields 不是 List 类型，返回空条件");
                return new SqlBuilder.SqlResult("", new ArrayList<>());
            }

            java.util.List<Map<String, Object>> fields = 
                (java.util.List<Map<String, Object>>) fieldsObj;
            
            log.debug("开始构建查询条件，字段数：{}", fields.size());

            List<Map<String, Object>> conditions = new ArrayList<>();
            for (Map<String, Object> fieldConfig : fields) {
                String field = (String) fieldConfig.get("field");
                String searchType = (String) fieldConfig.get("searchType");
                
                if (StringUtils.isEmpty(field)) {
                    log.warn("字段配置中 field 为空，跳过该配置");
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
            
            log.info("动态查询条件构建成功，字段数：{}", fields.size());
            
            return result;

        } catch (Exception e) {
            log.error("动态查询条件构建失败", e);
            throw new RuntimeException("查询条件构建失败：" + e.getMessage(), e);
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
     * 解析排序配置（新架构 - 返回 ORDER BY SQL 片段）
     * 
     * @param sortConfig 排序配置
     * @return SqlBuilder.SqlResult（包含 ORDER BY SQL 片段）
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
