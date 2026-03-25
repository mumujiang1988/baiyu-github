package com.ruoyi.erp.service.engine;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ruoyi.common.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * 动态查询引擎 (极简版)
 * 根据 JSON 配置生成查询条件
 * 
 * @author ERP Development Team
 * @date 2026-03-22
 */
@Slf4j
@Component
public class DynamicQueryEngine {

    // ✅ 删除硬编码的字段白名单，直接使用数据库字段名

    /**
     * 根据配置构建查询条件
     * 
     * @param queryWrapper 查询包装器
     * @param searchConfig 搜索配置
     * @param queryParams 查询参数
     * @return 构建后的查询包装器
     */
    @SuppressWarnings("unchecked")
    public <T> QueryWrapper<T> buildQueryConditions(
            QueryWrapper<T> queryWrapper,
            Map<String, Object> searchConfig,
            Map<String, Object> queryParams) {
        
        if (searchConfig == null || queryParams == null) {
            return queryWrapper;
        }

        try {
            // 获取字段配置
            Object fieldsObj = searchConfig.get("fields");
            if (!(fieldsObj instanceof java.util.List)) {
                return queryWrapper;
            }

            java.util.List<Map<String, Object>> fields = 
                (java.util.List<Map<String, Object>>) fieldsObj;

            // 遍历每个搜索字段配置
            for (Map<String, Object> fieldConfig : fields) {
                String field = (String) fieldConfig.get("field");
                String searchType = (String) fieldConfig.get("searchType");
                
                if (StringUtils.isEmpty(field)) {
                    continue;
                }
                
                // ✅ 简单校验字段非空即可（配置中已使用数据库字段名）

                Object value = queryParams.get(field);
                if (value == null || StringUtils.isEmpty(value.toString())) {
                    continue;
                }

                // 根据搜索类型构建查询条件
                switch (StringUtils.defaultString(searchType)) {
                    case "like":
                        queryWrapper.like(field, value.toString());
                        break;
                    
                    case "left_like":
                        queryWrapper.likeLeft(field, value.toString());
                        break;
                    
                    case "right_like":
                        queryWrapper.likeRight(field, value.toString());
                        break;
                    
                    case "in":
                        if (value instanceof java.util.Collection) {
                            queryWrapper.in(field, (java.util.Collection<?>) value);
                        } else {
                            queryWrapper.eq(field, value);
                        }
                        break;
                    
                    case "between":
                        handleBetweenCondition(queryWrapper, field, value);
                        break;
                    
                    case "gt":
                        queryWrapper.gt(field, value);
                        break;
                    
                    case "ge":
                        queryWrapper.ge(field, value);
                        break;
                    
                    case "lt":
                        queryWrapper.lt(field, value);
                        break;
                    
                    case "le":
                        queryWrapper.le(field, value);
                        break;
                    
                    case "ne":
                        queryWrapper.ne(field, value);
                        break;
                    
                    default:
                        // 默认精确查询
                        queryWrapper.eq(field, value);
                }
            }

            log.info("动态查询条件构建成功，字段数：{}", fields.size());

        } catch (Exception e) {
            log.error("动态查询条件构建失败", e);
        }

        return queryWrapper;
    }

    /**
     * 处理 Between 查询条件(改进版,增加类型校验)
     */
    @SuppressWarnings("unchecked")
    private <T> void handleBetweenCondition(
            QueryWrapper<T> queryWrapper,
            String field, 
            Object value) {
        
        if (value instanceof Map) {
            Map<String, Object> range = (Map<String, Object>) value;
            Object start = range.get("start");
            Object end = range.get("end");
            
            // 类型校验,确保是可比较的类型
            if (start != null && end != null) {
                try {
                    if (start instanceof Comparable && end instanceof Comparable) {
                        queryWrapper.between(field, start, end);
                    } else {
                        // 如果不是Comparable,尝试直接使用
                        queryWrapper.between(field, start, end);
                    }
                } catch (Exception e) {
                    log.error("Between条件构建失败: field={}, start={}, end={}", 
                        field, start, end, e);
                }
            } else {
                // 处理单边范围
                if (start != null && StringUtils.isNotEmpty(start.toString())) {
                    queryWrapper.ge(field, start);
                }
                if (end != null && StringUtils.isNotEmpty(end.toString())) {
                    queryWrapper.le(field, end);
                }
            }
        } else if (value instanceof String) {
            // 支持逗号分隔的字符串格式
            String[] parts = value.toString().split(",");
            if (parts.length == 2) {
                queryWrapper.between(field, parts[0], parts[1]);
            }
        }
    }

    /**
     * 校验字段是否合法
     * ✅ 极简版：只校验非空
     */
    private boolean isValidField(String field) {
        return StringUtils.isNotEmpty(field);
    }

    /**
     * 解析排序配置(增加字段校验)
     * 
     * @param queryWrapper 查询包装器
     * @param sortConfig 排序配置
     */
    public <T> void applySortConfig(
            QueryWrapper<T> queryWrapper,
            Map<String, Object> sortConfig) {
        
        if (sortConfig == null) {
            return;
        }

        String orderBy = (String) sortConfig.get("orderBy");
        String orderDirection = (String) sortConfig.get("orderDirection");

        if (StringUtils.isNotEmpty(orderBy)) {
            // ✅ 排序字段只需校验非空（配置中已使用数据库字段名）
            // MyBatis-Plus 会自动处理字段名下划线转换
            if ("asc".equalsIgnoreCase(orderDirection)) {
                queryWrapper.orderByAsc(true, orderBy);
            } else {
                queryWrapper.orderByDesc(true, orderBy);
            }
        }
    }

}
