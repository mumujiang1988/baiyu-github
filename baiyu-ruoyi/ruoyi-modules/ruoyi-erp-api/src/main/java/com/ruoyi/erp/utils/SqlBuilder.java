package com.ruoyi.erp.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Dynamic SQL Builder
 * @author JMH
 * @date 2026-03-30
 */
@Slf4j
@Component
public class SqlBuilder {
    
    // Field name validation pattern (only letters, numbers, underscore)
    private static final Pattern FIELD_NAME_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
    
    // SQL injection keyword blacklist
    private static final Set<String> SQL_KEYWORDS = new HashSet<>(Arrays.asList(
        "--", ";", "/*", "*/", "DROP", "DELETE", "UPDATE", "INSERT", 
        "TRUNCATE", "ALTER", "CREATE", "GRANT", "REVOKE"
    ));
    
    /**
     * Build WHERE clause
     * 
     * @param conditions Condition list [{field, operator, value}]
     * @return SQL fragment and parameter list
     */
    public SqlResult buildWhere(List<Map<String, Object>> conditions) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        
        if (conditions == null || conditions.isEmpty()) {
            return new SqlResult("", params);
        }
        
        boolean hasCondition = false;
        
        for (Map<String, Object> condition : conditions) {
            String field = (String) condition.get("field");
            String operator = (String) condition.get("operator");
            Object value = condition.get("value");
            
            // Skip empty value fields
            if (field == null || field.trim().isEmpty()) {
                continue;
            }
            
            // Field name validation
            validateFieldName(field);
            
            // Handle different types of conditions
            boolean conditionAdded = appendCondition(sql, params, field, operator, value);
            
            if (conditionAdded) {
                hasCondition = true;
            }
        }
        
        // If has conditions, add WHERE prefix
        if (hasCondition) {
            String whereSql = " WHERE " + sql.substring(5); // Remove leading " AND"
            return new SqlResult(whereSql, params);
        }
        
        return new SqlResult("", params);
    }
    
    /**
     * Build ORDER BY clause
     */
    public SqlResult buildOrderBy(List<Map<String, Object>> orderBy) {
        StringBuilder sql = new StringBuilder();
        
        if (orderBy == null || orderBy.isEmpty()) {
            return new SqlResult("", Collections.emptyList());
        }
        
        sql.append(" ORDER BY ");
        
        for (int i = 0; i < orderBy.size(); i++) {
            Map<String, Object> order = orderBy.get(i);
            String field = (String) order.get("field");
            String direction = (String) order.getOrDefault("direction", "asc");
            
            if (field == null || field.trim().isEmpty()) {
                continue;
            }
            
            validateFieldName(field);
            
            if (i > 0) {
                sql.append(", ");
            }
            
            sql.append(field);
            sql.append(" ASC".equalsIgnoreCase(direction) ? " ASC" : " DESC");
        }
        
        // If no valid sort fields, return empty
        if (sql.length() == " ORDER BY ".length()) {
            return new SqlResult("", Collections.emptyList());
        }
        
        return new SqlResult(sql.toString(), Collections.emptyList());
    }
    
    /**
     * Build SELECT field list
     */
    public String buildSelectFields(List<String> fields) {
        if (fields == null || fields.isEmpty()) {
            return "*";
        }
        
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < fields.size(); i++) {
            String field = fields.get(i);
            if (field == null || field.trim().isEmpty()) {
                continue;
            }
            
            validateFieldName(field);
            
            if (i > 0) {
                result.append(", ");
            }
            result.append(field);
        }
        
        return result.length() > 0 ? result.toString() : "*";
    }
    
    /**
     * Field name validation (prevent SQL injection)
     */
    private void validateFieldName(String fieldName) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            throw new IllegalArgumentException("Field name cannot be empty");
        }
        
        // 1. Format validation
        if (!FIELD_NAME_PATTERN.matcher(fieldName).matches()) {
            throw new IllegalArgumentException("Invalid field name format: " + fieldName);
        }
        
        // 2. Keyword check
        String upperField = fieldName.toUpperCase();
        for (String keyword : SQL_KEYWORDS) {
            if (upperField.contains(keyword)) {
                throw new IllegalArgumentException(
                    "Field contains SQL injection keyword: " + fieldName + " (contains: " + keyword + ")");
            }
        }
        
        // 3. Length limit
        if (fieldName.length() > 64) {
            throw new IllegalArgumentException("Field name too long: " + fieldName);
        }
        
        log.trace("Field name validation passed: {}", fieldName);
    }
    
    /**
     * Append single condition to SQL
     */
    private boolean appendCondition(StringBuilder sql, List<Object> params,
                                     String field, String operator, Object value) {
        if (value == null) {
            return false;
        }
        
        switch (operator) {
            case "eq":
                sql.append(" AND ").append(field).append(" = ?");
                params.add(value);
                log.trace("Add equal condition: {} = {}", field, value);
                break;
                
            case "ne":
                sql.append(" AND ").append(field).append(" <> ?");
                params.add(value);
                log.trace("Add not equal condition: {} <> {}", field, value);
                break;
                
            case "gt":
                sql.append(" AND ").append(field).append(" > ?");
                params.add(value);
                log.trace("Add greater than condition: {} > {}", field, value);
                break;
                
            case "ge":
                sql.append(" AND ").append(field).append(" >= ?");
                params.add(value);
                log.trace("Add greater than or equal condition: {} >= {}", field, value);
                break;
                
            case "lt":
                sql.append(" AND ").append(field).append(" < ?");
                params.add(value);
                log.trace("Add less than condition: {} < {}", field, value);
                break;
                
            case "le":
                sql.append(" AND ").append(field).append(" <= ?");
                params.add(value);
                log.trace("Add less than or equal condition: {} <= {}", field, value);
                break;
                
            case "like":
                sql.append(" AND ").append(field).append(" LIKE ?");
                params.add(escapeLikeSpecialChars(value));
                log.trace("Add like condition: {} LIKE %{}%", field, value);
                break;
                
            case "left_like":
                sql.append(" AND ").append(field).append(" LIKE ?");
                params.add("%" + escapeLikeSpecialChars(value));
                log.trace("Add left like condition: {} LIKE {}%", field, value);
                break;
                
            case "right_like":
                sql.append(" AND ").append(field).append(" LIKE ?");
                params.add(escapeLikeSpecialChars(value) + "%");
                log.trace("Add right like condition: {} LIKE %{}", field, value);
                break;
                
            case "in":
                if (value instanceof List) {
                    List<?> list = (List<?>) value;
                    if (!list.isEmpty()) {
                        sql.append(" AND ").append(field).append(" IN (");
                        for (int i = 0; i < list.size(); i++) {
                            if (i > 0) sql.append(", ");
                            sql.append("?");
                            params.add(list.get(i));
                        }
                        sql.append(")");
                        log.trace("Add IN condition: {} IN ({} values)", field, list.size());
                    } else {
                        return false;
                    }
                } else {
                    log.warn("IN condition value is not List type: field={}, value={}", field, value);
                    return false;
                }
                break;
                
            case "between":
                if (value instanceof List) {
                    List<?> list = (List<?>) value;
                    if (list.size() >= 2) {
                        sql.append(" AND ").append(field).append(" BETWEEN ? AND ?");
                        params.add(list.get(0));
                        params.add(list.get(1));
                        log.trace("Add BETWEEN condition: {} BETWEEN {} AND {}", 
                            field, list.get(0), list.get(1));
                    } else {
                        log.warn("BETWEEN condition value count less than 2: {}", list.size());
                        return false;
                    }
                } else {
                    log.warn("BETWEEN condition value is not List type: field={}, value={}", field, value);
                    return false;
                }
                break;
                
            case "isNull":
                sql.append(" AND ").append(field).append(" IS NULL");
                log.trace("Add IS NULL condition: {}", field);
                break;
                
            case "isNotNull":
                sql.append(" AND ").append(field).append(" IS NOT NULL");
                log.trace("Add IS NOT NULL condition: {}", field);
                break;
                
            default:
                log.warn("Unknown operator: operator={}, field={}", operator, field);
                return false;
        }
        
        return true;
    }
    
    /**
     * Escape LIKE special characters
     */
    private String escapeLikeSpecialChars(Object value) {
        if (value == null) {
            return "";
        }
        String strValue = String.valueOf(value);
        return strValue.replace("%", "\\%")
                      .replace("_", "\\_");
    }
    
    /**
     * SQL build result
     */
    public static class SqlResult {
        private final String sql;
        private final List<Object> params;
        
        public SqlResult(String sql, List<Object> params) {
            this.sql = sql;
            this.params = params != null ? params : new ArrayList<>();
        }
        
        public String getSql() {
            return sql;
        }
        
        public List<Object> getParams() {
            return Collections.unmodifiableList(params);
        }
        
        public int getParamCount() {
            return params.size();
        }
        
        @Override
        public String toString() {
            return "SqlResult{sql='" + sql + "', paramCount=" + params.size() + "}";
        }
    }
}
