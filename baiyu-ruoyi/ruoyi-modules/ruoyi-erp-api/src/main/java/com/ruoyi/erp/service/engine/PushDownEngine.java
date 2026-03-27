package com.ruoyi.erp.service.engine;

import com.ruoyi.common.core.utils.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 下推引擎(安全加固版)
 * 根据配置执行下推操作 (字段映射、数据转换等)
 * 使用安全的表达式解析器替代ScriptEngine
 * 
 * @author JMH
 * @date 2026-03-22
 */
@Slf4j
@Component("erpPushDownEngine")
public class PushDownEngine {
    
    /**
     *  正则表达式编译为静态常量,避免重复编译
     */
    private static final Pattern VARIABLE_PATTERN = 
        Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b");

    /**
     * 执行下推操作
     * 
     * @param sourceData 源单数据
     * @param mappingConfig 映射配置
     * @return 目标单数据
     */
    @SuppressWarnings("unchecked")
    public PushResult execute(Map<String, Object> sourceData, Map<String, Object> mappingConfig) {
        PushResult result = new PushResult();
        
        if (sourceData == null || mappingConfig == null) {
            result.setSuccess(false);
            result.setMessage("源单数据或映射配置为空");
            return result;
        }

        try {
            Map<String, Object> targetData = new HashMap<>();

            // 1. 主表字段映射
            Map<String, String> sourceToTarget = 
                (Map<String, String>) mappingConfig.get("sourceToTarget");
            if (sourceToTarget != null) {
                for (Map.Entry<String, String> entry : sourceToTarget.entrySet()) {
                    String sourceField = entry.getKey();
                    String targetField = entry.getValue();
                    
                    Object value = getNestedValue(sourceData, sourceField);
                    if (value != null) {
                        setNestedValue(targetData, targetField, value);
                    }
                }
            }

            // 2. 明细表映射
            Map<String, String> entryMapping = 
                (Map<String, String>) mappingConfig.get("entryMapping");
            List<Map<String, Object>> sourceEntryList = 
                (List<Map<String, Object>>) sourceData.get("entryList");
            
            if (entryMapping != null && sourceEntryList != null) {
                List<Map<String, Object>> targetEntryList = new ArrayList<>();
                
                for (Map<String, Object> sourceEntry : sourceEntryList) {
                    Map<String, Object> targetEntry = new HashMap<>();
                    
                    for (Map.Entry<String, String> entry : entryMapping.entrySet()) {
                        String sourceField = entry.getKey();
                        String targetField = entry.getValue();
                        
                        Object value = sourceEntry.get(sourceField);
                        if (value != null) {
                            targetEntry.put(targetField, value);
                        }
                    }
                    
                    targetEntryList.add(targetEntry);
                }
                
                targetData.put("entryList", targetEntryList);
            }

            // 3. 应用默认值
            Map<String, Object> defaultValue = 
                (Map<String, Object>) mappingConfig.get("defaultValue");
            if (defaultValue != null) {
                applyDefaults(targetData, defaultValue, sourceData);
            }

            // 4. 数据转换
            Map<String, String> transformation = 
                (Map<String, String>) mappingConfig.get("transformation");
            if (transformation != null) {
                applyTransformations(targetData, transformation, sourceData);
            }

            result.setData(targetData);
            result.setSuccess(true);
            result.setMessage("下推成功");

        } catch (Exception e) {
            log.error("下推执行失败", e);
            result.setSuccess(false);
            result.setMessage("下推失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 应用默认值
     */
    @SuppressWarnings("unchecked")
    private void applyDefaults(Map<String, Object> targetData, 
                               Map<String, Object> defaults,
                               Map<String, Object> context) {
        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            String field = entry.getKey();
            Object defaultValue = entry.getValue();

            // 如果目标字段已有值，跳过
            if (targetData.containsKey(field)) {
                continue;
            }

            // 处理特殊变量
            if (defaultValue instanceof String) {
                String strValue = (String) defaultValue;
                if ("${currentUser}".equals(strValue)) {
                    targetData.put(field, context.get("currentUserId"));
                } else if ("${now}".equals(strValue)) {
                    targetData.put(field, new Date());
                } else if (strValue.startsWith("${") && strValue.endsWith("}")) {
                    // 其他上下文变量
                    String varName = strValue.substring(2, strValue.length() - 1);
                    targetData.put(field, context.get(varName));
                } else {
                    targetData.put(field, defaultValue);
                }
            } else {
                targetData.put(field, defaultValue);
            }
        }
    }

    /**
     * 应用数据转换
     */
    @SuppressWarnings("unchecked")
    private void applyTransformations(Map<String, Object> targetData,
                                      Map<String, String> transformations,
                                      Map<String, Object> context) {
        for (Map.Entry<String, String> entry : transformations.entrySet()) {
            String targetField = entry.getKey();
            String formula = entry.getValue();

            try {
                Object result = evaluateFormula(formula, targetData, context);
                targetData.put(targetField, result);
            } catch (Exception e) {
                log.error("公式计算失败：{}", formula, e);
            }
        }
    }

    /**
     * 评估转换公式(安全实现)
     */
    private Object evaluateFormula(String formula, 
                                   Map<String, Object> targetData,
                                   Map<String, Object> context) {
        if (StringUtils.isEmpty(formula)) {
            return null;
        }

        try {
            // 合并数据上下文
            Map<String, Object> allContext = new HashMap<>(context);
            allContext.putAll(targetData);

            // 使用安全的表达式解析
            return evaluateSimpleFormula(formula, allContext);

        } catch (Exception e) {
            log.error("公式执行失败：{}", formula, e);
            return null;
        }
    }

    /**
     * 简单公式解析(性能优化版)
     * 支持基本的算术运算
     */
    private Object evaluateSimpleFormula(String formula, Map<String, Object> context) {
        // 替换变量为实际值
        String expression = formula;
        
        //  使用静态Pattern,避免重复编译
        java.util.regex.Matcher matcher = VARIABLE_PATTERN.matcher(formula);
        
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String varName = matcher.group(1);
            Object value = context.get(varName);
            
            if (value != null) {
                // 替换为数值
                if (value instanceof Number) {
                    matcher.appendReplacement(sb, value.toString());
                } else {
                    // 非数值类型,尝试转换
                    try {
                        double numValue = Double.parseDouble(value.toString());
                        matcher.appendReplacement(sb, String.valueOf(numValue));
                    } catch (NumberFormatException e) {
                        log.warn("变量 {} 的值不是数值: {}", varName, value);
                        matcher.appendReplacement(sb, "0");
                    }
                }
            } else {
                // 变量不存在,使用0
                matcher.appendReplacement(sb, "0");
            }
        }
        matcher.appendTail(sb);
        expression = sb.toString();

        // 计算表达式
        return calculateExpression(expression);
    }

    /**
     * 计算数学表达式
     */
    private Object calculateExpression(String expression) {
        try {
            // 简单的四则运算计算器
            // 支持加减乘除
            return new SimpleCalculator().calculate(expression);
        } catch (Exception e) {
            log.error("表达式计算失败: {}", expression, e);
            return null;
        }
    }

    /**
     * 简单计算器(支持四则运算)
     */
    private static class SimpleCalculator {
        private int pos = -1;
        private int ch;
        private String str;

        public double calculate(String expression) {
            this.str = expression.replaceAll("\\s+", ""); // 移除空格
            nextChar();
            double x = parseExpression();
            if (pos < str.length()) {
                throw new RuntimeException("Unexpected: " + (char)ch);
            }
            return x;
        }

        private void nextChar() {
            ch = (++pos < str.length()) ? str.charAt(pos) : -1;
        }

        private boolean eat(int charToEat) {
            while (ch == ' ') nextChar();
            if (ch == charToEat) {
                nextChar();
                return true;
            }
            return false;
        }

        private double parseExpression() {
            double x = parseTerm();
            for (;;) {
                if (eat('+')) x += parseTerm();
                else if (eat('-')) x -= parseTerm();
                else return x;
            }
        }

        private double parseTerm() {
            double x = parseFactor();
            for (;;) {
                if (eat('*')) {
                    x *= parseFactor();
                } else if (eat('/')) {
                    double divisor = parseFactor();
                    //  检查除数是否为0
                    if (Math.abs(divisor) < 1e-10) { // 浮点数精度问题,使用极小值判断
                        throw new ArithmeticException("除数不能为零");
                    }
                    x /= divisor;
                } else {
                    return x;
                }
            }
        }

        private double parseFactor() {
            if (eat('+')) return parseFactor();
            if (eat('-')) return -parseFactor();

            double x;
            int startPos = this.pos;
            if (eat('(')) {
                x = parseExpression();
                eat(')');
            } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                x = Double.parseDouble(str.substring(startPos, this.pos));
            } else {
                throw new RuntimeException("Unexpected: " + (char)ch);
            }

            return x;
        }
    }

    /**
     * 获取嵌套字段的值 (支持点号分隔)
     */
    @SuppressWarnings("unchecked")
    private Object getNestedValue(Map<String, Object> data, String fieldPath) {
        if (StringUtils.isEmpty(fieldPath)) {
            return null;
        }

        String[] parts = fieldPath.split("\\.");
        Object current = data;

        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else {
                return null;
            }
        }

        return current;
    }

    /**
     * 设置嵌套字段的值
     */
    @SuppressWarnings("unchecked")
    private void setNestedValue(Map<String, Object> data, String fieldPath, Object value) {
        if (StringUtils.isEmpty(fieldPath)) {
            return;
        }

        String[] parts = fieldPath.split("\\.");
        
        if (parts.length == 1) {
            data.put(fieldPath, value);
            return;
        }

        Map<String, Object> current = data;
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            Object next = current.get(part);
            
            if (next == null) {
                next = new HashMap<String, Object>();
                current.put(part, (Map<String, Object>) next);
            }
            
            if (next instanceof Map) {
                current = (Map<String, Object>) next;
            } else {
                return;
            }
        }

        current.put(parts[parts.length - 1], value);
    }

    /**
     * 下推结果
     */
    @Data
    public static class PushResult {
        /**
         * 是否成功
         */
        private boolean success;

        /**
         * 结果消息
         */
        private String message;

        /**
         * 目标单数据
         */
        private Map<String, Object> data;
    }
}
