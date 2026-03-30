package com.ruoyi.business.util;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ValidatorUtil {
    private static final Pattern ALL_DIGITS_PATTERN = Pattern.compile("\\d+");
    private static final Pattern INTEGER_PATTERN = Pattern.compile("-?\\d+");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

    /**
     * 检查字符串是否全是数字（非负整数）
     */
    public static boolean isAllDigits(String str) {
        return str != null && !str.isEmpty() && ALL_DIGITS_PATTERN.matcher(str).matches();
    }

    /**
     * 检查字符串是否是整数
     */
    public static boolean isInteger(String str) {
        return str != null && !str.isEmpty() && INTEGER_PATTERN.matcher(str).matches();
    }

    /**
     * 检查字符串是否是数值（整数或小数）
     */
    public static boolean isNumeric(String str) {
        return str != null && !str.isEmpty() && NUMERIC_PATTERN.matcher(str).matches();
    }

    /**
     * 检查字符串是否是正整数
     */
    public static boolean isPositiveInteger(String str) {
        return isAllDigits(str) && !str.equals("0") && str.charAt(0) != '0';
    }

    /**
     * 检查字符串是否在指定范围内（整数）
     */
    public static boolean isIntegerInRange(String str, int min, int max) {
        if (!isInteger(str)) {
            return false;
        }
        try {
            int value = Integer.parseInt(str);
            return value >= min && value <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // 判断是否包含大写字母
    public static boolean containsUpperCase(String str) {
        for (char c : str.toCharArray()) {
            if (Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;
    }

    //判断是否为真实的url地址
    public static boolean isCompleteUrl(String url) {
        if (url == null) {
            return false;
        }
        url = url.trim().toLowerCase();
        return url.startsWith("http://") || url.startsWith("https://");
    }

    // 方法1：使用正则表达式判断是否全是中文（基本汉字）
    public static boolean isAllChinese(String str) {
        if (str == null) {
            return false;
        }
        // 匹配基本汉字（CJK Unified Ideographs）
        return str.matches("^[\u4e00-\u9fa5]+$");
    }


}
