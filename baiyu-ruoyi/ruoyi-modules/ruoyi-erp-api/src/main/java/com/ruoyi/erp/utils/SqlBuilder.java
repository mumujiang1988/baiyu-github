package com.ruoyi.erp.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 动态 SQL 构建器（完全替代 QueryWrapper）
 * 
 * 功能特性：
 * 1. 支持所有常用查询操作符
 * 2. 自动转义 LIKE 特殊字符
 * 3. 字段名白名单校验（防 SQL 注入）
 * 4. 生成标准 JDBC 占位符 SQL
 * 
 * @author JMH
 * @date 2026-03-30
 */
@Slf4j
@Component
public class SqlBuilder {
    
    // 字段名校验正则（只允许字母、数字、下划线）
    private static final Pattern FIELD_NAME_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
    
    // SQL 注入关键词黑名单
    private static final Set<String> SQL_KEYWORDS = new HashSet<>(Arrays.asList(
        "--", ";", "/*", "*/", "DROP", "DELETE", "UPDATE", "INSERT", 
        "TRUNCATE", "ALTER", "CREATE", "GRANT", "REVOKE"
    ));
    
    /**
     * 构建 WHERE 子句
     * 
     * @param conditions 条件列表 [{field, operator, value}]
     * @return SQL 片段和参数列表
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
            
            // 跳过空值字段
            if (field == null || field.trim().isEmpty()) {
                log.debug("跳过空字段名：{}", condition);
                continue;
            }
            
            // 字段名校验
            validateFieldName(field);
            
            // 处理不同类型的条件
            boolean conditionAdded = appendCondition(sql, params, field, operator, value);
            
            if (conditionAdded) {
                hasCondition = true;
            }
        }
        
        // 如果有条件，在前面加上 WHERE
        if (hasCondition) {
            String whereSql = " WHERE " + sql.substring(5); // 去掉开头的 " AND"
            log.debug("构建 WHERE 子句：{}, 参数数量：{}", whereSql, params.size());
            return new SqlResult(whereSql, params);
        }
        
        return new SqlResult("", params);
    }
    
    /**
     * 构建 ORDER BY 子句
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
        
        // 如果没有有效的排序字段，返回空
        if (sql.length() == " ORDER BY ".length()) {
            return new SqlResult("", Collections.emptyList());
        }
        
        log.debug("构建 ORDER BY 子句：{}", sql);
        return new SqlResult(sql.toString(), Collections.emptyList());
    }
    
    /**
     * 构建 SELECT 字段列表
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
     * 字段名校验（防 SQL 注入）
     */
    private void validateFieldName(String fieldName) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            throw new IllegalArgumentException("字段名不能为空");
        }
        
        // 1. 格式校验
        if (!FIELD_NAME_PATTERN.matcher(fieldName).matches()) {
            throw new IllegalArgumentException("非法字段名格式：" + fieldName);
        }
        
        // 2. 关键词检查
        String upperField = fieldName.toUpperCase();
        for (String keyword : SQL_KEYWORDS) {
            if (upperField.contains(keyword)) {
                throw new IllegalArgumentException(
                    "字段名包含 SQL 注入关键词：" + fieldName + " (包含：" + keyword + ")");
            }
        }
        
        // 3. 长度限制
        if (fieldName.length() > 64) {
            throw new IllegalArgumentException("字段名过长：" + fieldName);
        }
        
        log.trace("字段名校验通过：{}", fieldName);
    }
    
    /**
     * 追加单个条件到 SQL
     */
    private boolean appendCondition(StringBuilder sql, List<Object> params,
                                     String field, String operator, Object value) {
        if (value == null) {
            log.debug("值为 null，跳过条件：field={}, operator={}", field, operator);
            return false;
        }
        
        switch (operator) {
            case "eq":
                sql.append(" AND ").append(field).append(" = ?");
                params.add(value);
                log.trace("添加等于条件：{} = {}", field, value);
                break;
                
            case "ne":
                sql.append(" AND ").append(field).append(" <> ?");
                params.add(value);
                log.trace("添加不等于条件：{} <> {}", field, value);
                break;
                
            case "gt":
                sql.append(" AND ").append(field).append(" > ?");
                params.add(value);
                log.trace("添加大于条件：{} > {}", field, value);
                break;
                
            case "ge":
                sql.append(" AND ").append(field).append(" >= ?");
                params.add(value);
                log.trace("添加大于等于条件：{} >= {}", field, value);
                break;
                
            case "lt":
                sql.append(" AND ").append(field).append(" < ?");
                params.add(value);
                log.trace("添加小于条件：{} < {}", field, value);
                break;
                
            case "le":
                sql.append(" AND ").append(field).append(" <= ?");
                params.add(value);
                log.trace("添加小于等于条件：{} <= {}", field, value);
                break;
                
            case "like":
                sql.append(" AND ").append(field).append(" LIKE ?");
                params.add(escapeLikeSpecialChars(value));
                log.trace("添加模糊匹配条件：{} LIKE %{}%", field, value);
                break;
                
            case "left_like":
                sql.append(" AND ").append(field).append(" LIKE ?");
                params.add("%" + escapeLikeSpecialChars(value));
                log.trace("添加左模糊条件：{} LIKE {}%", field, value);
                break;
                
            case "right_like":
                sql.append(" AND ").append(field).append(" LIKE ?");
                params.add(escapeLikeSpecialChars(value) + "%");
                log.trace("添加右模糊条件：{} LIKE %{}", field, value);
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
                        log.trace("添加 IN 条件：{} IN ({} 个值)", field, list.size());
                    } else {
                        log.debug("IN 条件值为空列表，跳过：field={}", field);
                        return false;
                    }
                } else {
                    log.warn("IN 条件的值不是 List 类型：field={}, value={}", field, value);
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
                        log.trace("添加 BETWEEN 条件：{} BETWEEN {} AND {}", 
                            field, list.get(0), list.get(1));
                    } else {
                        log.warn("BETWEEN 条件值的数量不足 2 个：{}", list.size());
                        return false;
                    }
                } else {
                    log.warn("BETWEEN 条件的值不是 List 类型：field={}, value={}", field, value);
                    return false;
                }
                break;
                
            case "isNull":
                sql.append(" AND ").append(field).append(" IS NULL");
                log.trace("添加 IS NULL 条件：{}", field);
                break;
                
            case "isNotNull":
                sql.append(" AND ").append(field).append(" IS NOT NULL");
                log.trace("添加 IS NOT NULL 条件：{}", field);
                break;
                
            default:
                log.warn("未知的操作符：operator={}, field={}", operator, field);
                return false;
        }
        
        return true;
    }
    
    /**
     * 转义 LIKE 特殊字符
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
     * SQL 构建结果
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
