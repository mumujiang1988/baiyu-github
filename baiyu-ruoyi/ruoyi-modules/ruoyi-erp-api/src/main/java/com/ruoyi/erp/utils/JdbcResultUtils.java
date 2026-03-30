package com.ruoyi.erp.utils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

/**
 * JDBC 结果集转换工具类
 * 
 * 功能:
 * 1. 类型安全转换
 * 2. 空值处理
 * 3. 时间类型转换
 * 4. Map 辅助方法
 * 
 * @author JMH
 * @date 2026-03-30
 */
public final class JdbcResultUtils {
    
    private JdbcResultUtils() {
        // 工具类，禁止实例化
    }
    
    // ==================== 基础类型转换 ====================
    
    /**
     * 安全获取 Long 值
     * 
     * @param value 原始值
     * @return Long 值，如果转换失败返回 null
     */
    public static Long getLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * 安全获取 Integer 值
     * 
     * @param value 原始值
     * @return Integer 值，如果转换失败返回 null
     */
    public static Integer getInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * 安全获取 String 值
     * 
     * @param value 原始值
     * @return String 值，如果为 null 返回 null
     */
    public static String getString(Object value) {
        return value != null ? value.toString() : null;
    }
    
    /**
     * 安全获取 Double 值
     * 
     * @param value 原始值
     * @return Double 值，如果转换失败返回 null
     */
    public static Double getDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * 安全获取 BigDecimal 值
     * 
     * @param value 原始值
     * @return BigDecimal 值，如果转换失败返回 null
     */
    public static BigDecimal getBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * 安全获取 Boolean 值
     * 
     * @param value 原始值
     * @return Boolean 值，如果转换失败返回 null
     */
    public static Boolean getBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String strValue = value.toString().trim();
        if ("1".equals(strValue) || "true".equalsIgnoreCase(strValue)) {
            return true;
        }
        if ("0".equals(strValue) || "false".equalsIgnoreCase(strValue)) {
            return false;
        }
        return null;
    }
    
    // ==================== 时间类型转换 ====================
    
    /**
     * 安全获取 LocalDateTime 值
     * 
     * @param value 原始值
     * @return LocalDateTime 值，如果转换失败返回 null
     */
    public static LocalDateTime getLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        if (value instanceof Date) {
            return ((Date) value).toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        }
        return null;
    }
    
    /**
     * 安全获取 Date 值
     * 
     * @param value 原始值
     * @return Date 值，如果转换失败返回 null
     */
    public static Date getDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return (Date) value;
        }
        if (value instanceof LocalDateTime) {
            return Date.from(((LocalDateTime) value)
                .atZone(ZoneId.systemDefault())
                .toInstant());
        }
        return null;
    }
    
    // ==================== Map 辅助方法 ====================
    
    /**
     * 从 Map 中安全获取 Long 值
     * 
     * @param map Map 数据
     * @param key 键名
     * @return Long 值
     */
    public static Long getLong(Map<String, Object> map, String key) {
        return getLong(map.get(key));
    }
    
    /**
     * 从 Map 中安全获取 Integer 值
     * 
     * @param map Map 数据
     * @param key 键名
     * @return Integer 值
     */
    public static Integer getInteger(Map<String, Object> map, String key) {
        return getInteger(map.get(key));
    }
    
    /**
     * 从 Map 中安全获取 String 值
     * 
     * @param map Map 数据
     * @param key 键名
     * @return String 值
     */
    public static String getString(Map<String, Object> map, String key) {
        return getString(map.get(key));
    }
    
    /**
     * 从 Map 中安全获取 Double 值
     * 
     * @param map Map 数据
     * @param key 键名
     * @return Double 值
     */
    public static Double getDouble(Map<String, Object> map, String key) {
        return getDouble(map.get(key));
    }
    
    /**
     * 从 Map 中安全获取 BigDecimal 值
     * 
     * @param map Map 数据
     * @param key 键名
     * @return BigDecimal 值
     */
    public static BigDecimal getBigDecimal(Map<String, Object> map, String key) {
        return getBigDecimal(map.get(key));
    }
    
    /**
     * 从 Map 中安全获取 Boolean 值
     * 
     * @param map Map 数据
     * @param key 键名
     * @return Boolean 值
     */
    public static Boolean getBoolean(Map<String, Object> map, String key) {
        return getBoolean(map.get(key));
    }
    
    /**
     * 从 Map 中安全获取 LocalDateTime 值
     * 
     * @param map Map 数据
     * @param key 键名
     * @return LocalDateTime 值
     */
    public static LocalDateTime getLocalDateTime(Map<String, Object> map, String key) {
        return getLocalDateTime(map.get(key));
    }
    
    /**
     * 从 Map 中安全获取 Date 值
     * 
     * @param map Map 数据
     * @param key 键名
     * @return Date 值
     */
    public static Date getDate(Map<String, Object> map, String key) {
        return getDate(map.get(key));
    }
}
