package com.ruoyi.business.util;

/**
 * 字符串处理工具类
 */
public class StringUtils {

    /**
     * 获取字符串值（处理空值情况）
     * @param obj 对象
     * @return 字符串值，如果对象为空则返回null
     */
    public static String getStringValue(Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.toString().trim();
    }

    /**
     * 判断字符串是否为空
     * @param str 字符串
     * @return true: 为空, false: 不为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判断字符串是否不为空
     * @param str 字符串
     * @return true: 不为空, false: 为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

    public static boolean isBlank(String str) {
        return !isNotBlank(str);
    }

}
