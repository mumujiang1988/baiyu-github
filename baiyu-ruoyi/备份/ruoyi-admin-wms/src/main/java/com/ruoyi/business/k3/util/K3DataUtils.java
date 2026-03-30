package com.ruoyi.business.k3.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 金蝶数据转换工具类
 * 用于统一处理金蝶接口返回的List<Object>数据解析
 */
public class K3DataUtils {

    /**
     * 从List中指定索引位置获取String值
     * 
     * @param rowData 数据行
     * @param index 索引位置
     * @return String值，不存在则返回null
     */
    public static String getString(List<Object> rowData, int index) {
        if (rowData == null || index >= rowData.size() || rowData.get(index) == null) {
            return null;
        }
        return rowData.get(index).toString();
    }

    /**
     * 从List中指定索引位置获取Long值
     * 
     * @param rowData 数据行
     * @param index 索引位置
     * @return Long值，解析失败返回null
     */
    public static Long getLong(List<Object> rowData, int index) {
        String value = getString(rowData, index);
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 从List中指定索引位置获取Integer值
     * 
     * @param rowData 数据行
     * @param index 索引位置
     * @return Integer值，解析失败返回null
     */
    public static Integer getInteger(List<Object> rowData, int index) {
        String value = getString(rowData, index);
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 从List中指定索引位置获取BigDecimal值
     * 
     * @param rowData 数据行
     * @param index 索引位置
     * @return BigDecimal值，解析失败返回null
     */
    public static BigDecimal getBigDecimal(List<Object> rowData, int index) {
        String value = getString(rowData, index);
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 从List中指定索引位置获取LocalDate值
     * 
     * @param rowData 数据行
     * @param index 索引位置
     * @return LocalDate值，解析失败返回null
     */
    public static LocalDate getLocalDate(List<Object> rowData, int index) {
        return DateUtils.getLocalDate(rowData, index);
    }

    /**
     * 从List中指定索引位置获取LocalDateTime值
     * 
     * @param rowData 数据行
     * @param index 索引位置
     * @return LocalDateTime值，解析失败返回null
     */
    public static LocalDateTime getLocalDateTime(List<Object> rowData, int index) {
        return DateUtils.getLocalDateTime(rowData, index);
    }

    /**
     * 解析Boolean类型
     * 
     * @param obj 对象
     * @return Boolean值，如果为null则返回null
     */
    public static Boolean getBoolean(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue() != 0;
        }
        try {
            String str = obj.toString().toLowerCase().trim();
            return "true".equals(str) || "1".equals(str) || "yes".equals(str) || "y".equals(str);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从List中指定索引位置获取Boolean值
     * 
     * @param rowData 数据行
     * @param index 索引位置
     * @return Boolean值，解析失败返回null
     */
    public static Boolean getBoolean(List<Object> rowData, int index) {
        if (rowData == null || index >= rowData.size()) {
            return null;
        }
        return getBoolean(rowData.get(index));
    }

    /**
     * 从List中指定索引位置获取Date值
     * 
     * @param rowData 数据行
     * @param index 索引位置
     * @return Date值，解析失败返回null
     */
    public static java.util.Date getDate(List<Object> rowData, int index) {
        return DateUtils.getDate(rowData, index);
    }

    /**
     * 从List中指定索引位置获取Date值（仅日期部分）
     * 
     * @param rowData 数据行
     * @param index 索引位置
     * @return Date值，解析失败返回null
     */
    public static java.util.Date getDateOnly(List<Object> rowData, int index) {
        return DateUtils.getDateOnly(rowData, index);
    }
}
