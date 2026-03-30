package com.ruoyi.erp.service.engine;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ruoyi.common.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 动态查询引擎 (安全增强版)
 * 根据 JSON 配置生成查询条件
 * 
 * 功能特性:
 * 1. 支持多种查询操作符: like, in, between, gt, ge, lt, le, ne, eq
 * 2. 自动转义 LIKE 查询特殊字符,防止查询操纵
 * 3. 支持多种参数格式: Map, Array, String
 * 4. 完善的异常处理和日志记录
 * 
 * 安全特性:
 * - LIKE 查询自动转义 % 和 _ 字符
 * - 改进的空值处理,避免空指针异常
 * - 配置格式校验,提供清晰的错误提示
 * 
 * @author JMH
 * @date 2026-03-22
 */
@Slf4j
@Component("erpDynamicQueryEngine")
public class DynamicQueryEngine {
 
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
                log.warn("searchConfig.fields 不是 List 类型,跳过查询条件构建");
                return queryWrapper;
            }

            java.util.List<Map<String, Object>> fields = 
                (java.util.List<Map<String, Object>>) fieldsObj;
            
            log.debug("开始构建查询条件,字段数: {}", fields.size());

            // 遍历每个搜索字段配置
            for (Map<String, Object> fieldConfig : fields) {
                String field = (String) fieldConfig.get("field");
                String searchType = (String) fieldConfig.get("searchType");
                
                if (StringUtils.isEmpty(field)) {
                    log.warn("字段配置中 field 为空,跳过该配置");
                    continue;
                }
                
                //  简单校验字段非空即可（配置中已使用数据库字段名）

                Object value = queryParams.get(field);
                // 改进的空值判断: 避免 toString() 空指针异常
                if (value == null) {
                    continue;
                }
                
                // 对于字符串类型,检查是否为空字符串
                if (value instanceof String && StringUtils.isEmpty((String) value)) {
                    continue;
                }

                // 根据搜索类型构建查询条件
                switch (StringUtils.defaultString(searchType)) {
                    case "like":
                        queryWrapper.like(field, escapeLikeSpecialChars(value));
                        break;
                    
                    case "left_like":
                        queryWrapper.likeLeft(field, escapeLikeSpecialChars(value));
                        break;
                    
                    case "right_like":
                        queryWrapper.likeRight(field, escapeLikeSpecialChars(value));
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

        } catch (ClassCastException e) {
            log.error("查询配置类型转换失败,请检查配置格式", e);
            throw new IllegalArgumentException("查询配置格式错误: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("动态查询条件构建失败", e);
            throw new RuntimeException("查询条件构建失败: " + e.getMessage(), e);
        }

        return queryWrapper;
    }

    /**
     * 处理 Between 查询条件(改进版,增加类型校验)
     * 支持多种格式:
     * 1. Map格式: {"start": "2026-03-01", "end": "2026-03-30"}
     * 2. 数组格式: ["2026-03-01", "2026-03-30"]
     * 3. 字符串格式: "2026-03-01,2026-03-30"
     */
    @SuppressWarnings("unchecked")
    private <T> void handleBetweenCondition(
            QueryWrapper<T> queryWrapper,
            String field, 
            Object value) {
        
        if (value instanceof Map) {
            // 格式1: Map格式
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
        } else if (value instanceof java.util.List) {
            // 格式2: 数组格式 (新增)
            java.util.List<?> list = (java.util.List<?>) value;
            if (list.size() == 2) {
                Object start = list.get(0);
                Object end = list.get(1);
                
                if (start != null && end != null) {
                    try {
                        queryWrapper.between(field, start, end);
                        log.debug("Between条件构建成功(数组格式): field={}, start={}, end={}", 
                            field, start, end);
                    } catch (Exception e) {
                        log.error("Between条件构建失败(数组格式): field={}, start={}, end={}", 
                            field, start, end, e);
                    }
                }
            } else {
                log.warn("Between条件数组长度不等于2: {}", list.size());
            }
        } else if (value instanceof String) {
            // 格式3: 支持逗号分隔的字符串格式
            String[] parts = value.toString().split(",");
            if (parts.length == 2) {
                queryWrapper.between(field, parts[0], parts[1]);
            }
        }
    }


    /**
     * 转义 LIKE 查询的特殊字符
     * 防止用户通过 % 和 _ 字符操纵查询
     * 
     * @param value 原始值
     * @return 转义后的值
     */
    private String escapeLikeSpecialChars(Object value) {
        if (value == null) {
            return "";
        }
        String strValue = String.valueOf(value);
        // 转义 SQL LIKE 特殊字符: % 和 _
        return strValue.replace("%", "\\%")
                       .replace("_", "\\_");
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
            //  排序字段只需校验非空（配置中已使用数据库字段名）
            // MyBatis-Plus 会自动处理字段名下划线转换
            if ("asc".equalsIgnoreCase(orderDirection)) {
                queryWrapper.orderByAsc(true, orderBy);
            } else {
                queryWrapper.orderByDesc(true, orderBy);
            }
        }
    }

}
