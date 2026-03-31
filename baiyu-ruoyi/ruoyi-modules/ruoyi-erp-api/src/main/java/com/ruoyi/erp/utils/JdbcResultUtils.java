package com.ruoyi.erp.utils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

/**
 * JDBC ResultSet Utility Class
 * 
 * Features:
 * 1. Type-safe conversion
 * 2. Null value handling
 * 3. Date/time type conversion
 * 4. Map helper methods
 * 
 * @author JMH
 * @date 2026-03-30
 */
public final class JdbcResultUtils {
    
    private JdbcResultUtils() {
        // Utility class, prevent instantiation
    }
    
    // ==================== Basic Type Conversion ====================
    
    /**
     * Safely get Long value
     * 
     * @param value Raw value
     * @return Long value, null if conversion failed
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
     * Safely get Integer value
     * 
     * @param value Raw value
     * @return Integer value, null if conversion failed
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
     * Safely get String value
     * 
     * @param value Raw value
     * @return String value, null if value is null
     */
    public static String getString(Object value) {
        return value != null ? value.toString() : null;
    }
    
    /**
     * Safely get Double value
     * 
     * @param value Raw value
     * @return Double value, null if conversion failed
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
     * Safely get BigDecimal value
     * 
     * @param value Raw value
     * @return BigDecimal value, null if conversion failed
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
     * Safely get Boolean value
     * 
     * @param value Raw value
     * @return Boolean value, null if conversion failed
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
    
    // ==================== Date/Time Type Conversion ====================
    
    /**
     * Safely get LocalDateTime value
     * 
     * @param value Raw value
     * @return LocalDateTime value, null if conversion failed
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
     * Safely get Date value
     * 
     * @param value Raw value
     * @return Date value, null if conversion failed
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
    
    // ==================== Map Helper Methods ====================
    
    /**
     * Safely get Long value from Map
     * 
     * @param map Map data
     * @param key Key name
     * @return Long value
     */
    public static Long getLong(Map<String, Object> map, String key) {
        return getLong(map.get(key));
    }
    
    /**
     * Safely get Integer value from Map
     * 
     * @param map Map data
     * @param key Key name
     * @return Integer value
     */
    public static Integer getInteger(Map<String, Object> map, String key) {
        return getInteger(map.get(key));
    }
    
    /**
     * Safely get String value from Map
     * 
     * @param map Map data
     * @param key Key name
     * @return String value
     */
    public static String getString(Map<String, Object> map, String key) {
        return getString(map.get(key));
    }
    
    /**
     * Safely get Double value from Map
     * 
     * @param map Map data
     * @param key Key name
     * @return Double value
     */
    public static Double getDouble(Map<String, Object> map, String key) {
        return getDouble(map.get(key));
    }
    
    /**
     * Safely get BigDecimal value from Map
     * 
     * @param map Map data
     * @param key Key name
     * @return BigDecimal value
     */
    public static BigDecimal getBigDecimal(Map<String, Object> map, String key) {
        return getBigDecimal(map.get(key));
    }
    
    /**
     * Safely get Boolean value from Map
     * 
     * @param map Map data
     * @param key Key name
     * @return Boolean value
     */
    public static Boolean getBoolean(Map<String, Object> map, String key) {
        return getBoolean(map.get(key));
    }
    
    /**
     * Safely get LocalDateTime value from Map
     * 
     * @param map Map data
     * @param key Key name
     * @return LocalDateTime value
     */
    public static LocalDateTime getLocalDateTime(Map<String, Object> map, String key) {
        return getLocalDateTime(map.get(key));
    }
    
    /**
     * Safely get Date value from Map
     * 
     * @param map Map data
     * @param key Key name
     * @return Date value
     */
    public static Date getDate(Map<String, Object> map, String key) {
        return getDate(map.get(key));
    }
}
