package com.ruoyi.erp.service.engine;

import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.erp.utils.SqlBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 动态查询引擎（纯 JDBC 架构 - 重构版）
 * 根据 JSON 配置生成查询条件
 * 
 * 功能特性:
 * 1. 支持多种查询操作符：like, in, between, gt, ge, lt, le, ne, eq
 * 2. 自动转义 LIKE 查询特殊字符，防止查询操纵
 * 3. 支持多种参数格式：Map, Array, String
 * 4. 完善的异常处理和日志记录
 * 
 * 安全特性:
 * - LIKE 查询自动转义 % 和 _ 字符
 * - 改进的空值处理，避免空指针异常
 * - 配置格式校验，提供清晰的错误提示
 * 
 * 架构优势:
 * - 完全去除 QueryWrapper 依赖，降低框架耦合
 * - 复用 SqlBuilder，避免重复代码
 * - 返回标准 SQL 片段，易于组合和测试
 * 
 * @author JMH
 * @date 2026-03-30 (重构)
 */
@Slf4j
@Component("erpDynamicQueryEngine")
public class DynamicQueryEngine {
 
    @Autowired
    private SqlBuilder sqlBuilder;

    /**
     * 根据配置构建查询条件（新架构 - 返回 SQL 片段）
     * 
     * @param searchConfig 搜索配置
     * @param queryParams 查询参数
     * @return SqlBuilder.SqlResult（包含 SQL 片段和参数列表）
     */
    @SuppressWarnings("unchecked")
    public SqlBuilder.SqlResult buildQueryConditions(
            Map<String, Object> searchConfig,
            Map<String, Object> queryParams) {
        
        if (searchConfig == null || queryParams == null) {
            return new SqlBuilder.SqlResult("", new ArrayList<>());
        }

        try {
            // 获取字段配置
            Object fieldsObj = searchConfig.get("fields");
            if (!(fieldsObj instanceof java.util.List)) {
                log.warn("searchConfig.fields 不是 List 类型，返回空条件");
                return new SqlBuilder.SqlResult("", new ArrayList<>());
            }

            java.util.List<Map<String, Object>> fields = 
                (java.util.List<Map<String, Object>>) fieldsObj;
            
            log.debug("开始构建查询条件，字段数：{}", fields.size());

            // 转换为 SqlBuilder 支持的格式
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
                
                // 转换为条件对象
                Map<String, Object> condition = new HashMap<>();
                condition.put("field", field);
                condition.put("operator", convertSearchTypeToOperator(searchType));
                condition.put("value", value);
                conditions.add(condition);
            }

            // 使用 SqlBuilder 构建 SQL
            SqlBuilder.SqlResult result = sqlBuilder.buildWhere(conditions);
            
            log.info("动态查询条件构建成功，字段数：{}", fields.size());
            
            return result;

        } catch (Exception e) {
            log.error("动态查询条件构建失败", e);
            throw new RuntimeException("查询条件构建失败：" + e.getMessage(), e);
        }
    }

    /**
     * 将 searchType 转换为 operator
     */
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
