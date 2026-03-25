package com.ruoyi.business.k3.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

/**
 * 日期工具类
 * 用于统一处理金蝶数据中的日期时间解析
 */
public class DateUtils {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 将ISO 8601格式的日期时间字符串转换为Date
     * 例如: "1984-12-05T00:00:00" -> Date
     *
     * @param dateStr ISO 8601格式的日期时间字符串
     * @return Date对象，如果解析失败则返回null
     */
    public static Date parseIsoDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateStr);
            return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * 从Object解析为LocalDateTime
     * 支持多种格式：
     * - ISO格式："2024-01-01T12:00:00"
     * - 标准格式："2024-01-01 12:00:00"
     * - 日期格式："2024-01-01" (转为当天开始时间)
     *
     * @param obj 待解析的对象
     * @return LocalDateTime对象，解析失败返回null
     */
    public static LocalDateTime parseLocalDateTime(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            String s = obj.toString().trim();
            // 去除毫秒部分
            s = s.split("\\.")[0];

            if (s.isEmpty()) {
                return null;
            }

            // 包含T的ISO格式
            if (s.contains("T")) {
                return LocalDateTime.parse(s.replace("T", " "), DATETIME_FORMATTER);
            }
            // 仅日期格式 (yyyy-MM-dd)
            else if (s.length() == 10) {
                return LocalDate.parse(s).atStartOfDay();
            }
            // 标准日期时间格式
            else {
                return LocalDateTime.parse(s, DATETIME_FORMATTER);
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从Object解析为LocalDate
     * 支持多种格式：
     * - ISO格式："2024-01-01T12:00:00" (取日期部分)
     * - 日期格式："2024-01-01"
     *
     * @param obj 待解析的对象
     * @return LocalDate对象，解析失败返回null
     */
    public static LocalDate parseLocalDate(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            String s = obj.toString().trim();
            // 去除时间部分（如果存在T）
            s = s.split("T")[0];

            if (s.isEmpty()) {
                return null;
            }

            return LocalDate.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

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
     * 从List中指定索引位置获取LocalDateTime值
     *
     * @param rowData 数据行
     * @param index 索引位置
     * @return LocalDateTime值，解析失败返回null
     */
    public static LocalDateTime getLocalDateTime(List<Object> rowData, int index) {
        return parseLocalDateTime(rowData != null && index < rowData.size() ? rowData.get(index) : null);
    }

    /**
     * 从List中指定索引位置获取LocalDate值
     *
     * @param rowData 数据行
     * @param index 索引位置
     * @return LocalDate值，解析失败返回null
     */
    public static LocalDate getLocalDate(List<Object> rowData, int index) {
        return parseLocalDate(rowData != null && index < rowData.size() ? rowData.get(index) : null);
    }

    /**
     * 从List中指定索引位置获取Date值
     *
     * @param rowData 数据行
     * @param index 索引位置
     * @return Date值，解析失败返回null
     */
    public static Date getDate(List<Object> rowData, int index) {
        LocalDateTime localDateTime = parseLocalDateTime(rowData != null && index < rowData.size() ? rowData.get(index) : null);
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 从List中指定索引位置获取Date值（仅日期部分）
     *
     * @param rowData 数据行
     * @param index 索引位置
     * @return Date值，解析失败返回null
     */
    public static Date getDateOnly(List<Object> rowData, int index) {
        LocalDate localDate = parseLocalDate(rowData != null && index < rowData.size() ? rowData.get(index) : null);
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
