package com.ruoyi.erp.service.engine;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import com.ruoyi.erp.exception.ComputedFieldException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 计算字段配置实体
 * 
 * @author JMH
 * @date 2026-03-24
 */
@Slf4j
@Component
public class ComputedFieldEngine {
    
    /**
     * 执行字段计算
     * 
     * @param data 原始数据
     * @param configs 计算配置列表
     * @return 计算后的数据
     */
    public Map<String, Object> computeFields(
            Map<String, Object> data,
            List<ComputedFieldConfig> configs) {
        
        if (CollUtil.isEmpty(configs)) {
            return data;
        }
        
        Map<String, Object> result = new HashMap<>(data);
        
        for (ComputedFieldConfig config : configs) {
            try {
                // 解析并计算公式
                Object value = evaluateFormula(config.getFormula(), result);
                
                // 精度处理
                if (value instanceof Number && config.getPrecision() != null) {
                    if (value instanceof BigDecimal) {
                        value = ((BigDecimal) value).setScale(config.getPrecision(), RoundingMode.HALF_UP);
                    } else if (value instanceof Double || value instanceof Float) {
                        value = NumberUtil.round(((Number) value).doubleValue(), config.getPrecision());
                    }
                }
                
                result.put(config.getTargetField(), value);
                
                log.debug("计算字段 {} = {}", config.getTargetField(), value);
                
            } catch (Exception e) {
                log.error("计算字段 {} 失败：{}", config.getTargetField(), e.getMessage());
                // 计算失败时抛出异常
                throw new ComputedFieldException(
                    config.getTargetField(), 
                    config.getFormula(), 
                    "计算失败：" + e.getMessage(),
                    e
                );
            }
        }
        
        return result;
    }
    
    /**
     * 批量计算字段
     */
    public List<Map<String, Object>> computeFieldsBatch(
            List<Map<String, Object>> dataList,
            List<ComputedFieldConfig> configs) {
        
        if (CollUtil.isEmpty(dataList)) {
            return dataList;
        }
        
        return dataList.stream()
            .map(data -> computeFields(data, configs))
            .toList();
    }
    
    /**
     * 解析并计算公式
     */
    private Object evaluateFormula(String formula, Map<String, Object> context) {
        // 支持的函数
        if (formula.startsWith("SUM(")) {
            return evaluateSum(formula, context);
        } else if (formula.startsWith("AVG(")) {
            return evaluateAvg(formula, context);
        } else if (formula.startsWith("COUNT(")) {
            return evaluateCount(formula, context);
        } else if (formula.startsWith("MAX(")) {
            return evaluateMax(formula, context);
        } else if (formula.startsWith("MIN(")) {
            return evaluateMin(formula, context);
        } else {
            // 普通表达式计算 (支持四则运算)
            return evaluateExpression(formula, context);
        }
    }
    
    /**
     * SUM 函数计算 - 支持嵌套列表字段汇总
     */
    private Object evaluateSum(String formula, Map<String, Object> context) {
        // 提取字段路径：SUM(entryList.fAllAmount)
        String fieldPath = formula.substring(4, formula.length() - 1);
        String[] parts = fieldPath.split("\\.");
        
        if (parts.length != 2) {
            throw new IllegalArgumentException("SUM 函数格式错误，应为：SUM(list.field)");
        }
        
        String listField = parts[0];
        String sumField = parts[1];
        
        Object listObj = context.get(listField);
        if (!(listObj instanceof List)) {
            return 0;
        }
        
        List<?> list = (List<?>) listObj;
        if (CollUtil.isEmpty(list)) {
            return 0;
        }
        
        BigDecimal sum = BigDecimal.ZERO;
        for (Object item : list) {
            if (item instanceof Map) {
                Object value = ((Map<?, ?>) item).get(sumField);
                if (value instanceof Number) {
                    sum = sum.add(new BigDecimal(value.toString()));
                }
            }
        }
        
        return sum;
    }
    
    /**
     * AVG 函数计算
     */
    private Object evaluateAvg(String formula, Map<String, Object> context) {
        String fieldPath = formula.substring(4, formula.length() - 1);
        String[] parts = fieldPath.split("\\.");
        
        if (parts.length != 2) {
            throw new IllegalArgumentException("AVG 函数格式错误，应为：AVG(list.field)");
        }
        
        String listField = parts[0];
        String avgField = parts[1];
        
        Object listObj = context.get(listField);
        if (!(listObj instanceof List)) {
            return 0;
        }
        
        List<?> list = (List<?>) listObj;
        if (CollUtil.isEmpty(list)) {
            return 0;
        }
        
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        
        for (Object item : list) {
            if (item instanceof Map) {
                Object value = ((Map<?, ?>) item).get(avgField);
                if (value instanceof Number) {
                    sum = sum.add(new BigDecimal(value.toString()));
                    count++;
                }
            }
        }
        
        return count > 0 ? sum.divide(BigDecimal.valueOf(count), 10, RoundingMode.HALF_UP) : 0;
    }
    
    /**
     * COUNT 函数计算
     */
    private Object evaluateCount(String formula, Map<String, Object> context) {
        String fieldPath = formula.substring(6, formula.length() - 1);
        Object listObj = context.get(fieldPath);
        
        if (!(listObj instanceof List)) {
            return 0;
        }
        
        return ((List<?>) listObj).size();
    }
    
    /**
     * MAX 函数计算
     */
    private Object evaluateMax(String formula, Map<String, Object> context) {
        String fieldPath = formula.substring(4, formula.length() - 1);
        String[] parts = fieldPath.split("\\.");
        
        if (parts.length != 2) {
            throw new IllegalArgumentException("MAX 函数格式错误，应为：MAX(list.field)");
        }
        
        String listField = parts[0];
        String maxField = parts[1];
        
        Object listObj = context.get(listField);
        if (!(listObj instanceof List)) {
            return null;
        }
        
        List<?> list = (List<?>) listObj;
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        
        BigDecimal max = null;
        for (Object item : list) {
            if (item instanceof Map) {
                Object value = ((Map<?, ?>) item).get(maxField);
                if (value instanceof Number) {
                    BigDecimal current = new BigDecimal(value.toString());
                    if (max == null || current.compareTo(max) > 0) {
                        max = current;
                    }
                }
            }
        }
        
        return max;
    }
    
    /**
     * MIN 函数计算
     */
    private Object evaluateMin(String formula, Map<String, Object> context) {
        String fieldPath = formula.substring(4, formula.length() - 1);
        String[] parts = fieldPath.split("\\.");
        
        if (parts.length != 2) {
            throw new IllegalArgumentException("MIN 函数格式错误，应为：MIN(list.field)");
        }
        
        String listField = parts[0];
        String minField = parts[1];
        
        Object listObj = context.get(listField);
        if (!(listObj instanceof List)) {
            return null;
        }
        
        List<?> list = (List<?>) listObj;
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        
        BigDecimal min = null;
        for (Object item : list) {
            if (item instanceof Map) {
                Object value = ((Map<?, ?>) item).get(minField);
                if (value instanceof Number) {
                    BigDecimal current = new BigDecimal(value.toString());
                    if (min == null || current.compareTo(min) < 0) {
                        min = current;
                    }
                }
            }
        }
        
        return min;
    }
    
    /**
     * 计算普通表达式 (支持四则运算)
     */
    private Object evaluateExpression(String expression, Map<String, Object> context) {
        try {
            // 替换变量为实际值
            String evaluatedExpr = expression;
            for (Map.Entry<String, Object> entry : context.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Number) {
                    evaluatedExpr = evaluatedExpr.replaceAll("\\b" + key + "\\b", value.toString());
                }
            }
            
            // 使用简单的表达式计算 (仅支持四则运算)
            return calculateSimpleExpression(evaluatedExpr);
        } catch (Exception e) {
            throw new RuntimeException("表达式计算失败：" + expression, e);
        }
    }
    
    /**
     * 简单四则运算计算器(修复运算符优先级)
     * 优先级: 括号 > 乘除 > 加减
     */
    private BigDecimal calculateSimpleExpression(String expression) {
        // 移除空格
        expression = expression.replaceAll("\\s+", "");
        
        // 处理括号(最高优先级)
        int parenStart = expression.lastIndexOf('(');
        if (parenStart >= 0) {
            int parenEnd = expression.indexOf(')', parenStart);
            if (parenEnd < 0) {
                throw new IllegalArgumentException("括号不匹配");
            }
            
            // 递归计算括号内的表达式
            String before = expression.substring(0, parenStart);
            String inside = expression.substring(parenStart + 1, parenEnd);
            String after = expression.substring(parenEnd + 1);
            
            BigDecimal insideValue = calculateSimpleExpression(inside);
            return calculateSimpleExpression(before + insideValue + after);
        }
        
        // 处理加减法(最低优先级,从右往左找)
        int plusIndex = findOperatorIndex(expression, '+');
        if (plusIndex > 0) {
            BigDecimal left = calculateSimpleExpression(expression.substring(0, plusIndex));
            BigDecimal right = calculateSimpleExpression(expression.substring(plusIndex + 1));
            return left.add(right);
        }
        
        int minusIndex = findOperatorIndex(expression, '-');
        if (minusIndex > 0) {
            // 处理负数情况: -5 或 3*-5
            if (minusIndex == 0) {
                // 整个表达式是负数
                return calculateSimpleExpression(expression.substring(1)).negate();
            }
            char prevChar = expression.charAt(minusIndex - 1);
            if (prevChar == '+' || prevChar == '-' || prevChar == '*' || prevChar == '/') {
                // 负号,不是减号,继续找下一个减号
                int nextMinus = findOperatorIndex(expression.substring(0, minusIndex), '-');
                if (nextMinus > 0) {
                    minusIndex = nextMinus;
                } else {
                    // 整个是负数表达式
                    return calculateSimpleExpression(expression.substring(1)).negate();
                }
            }
            
            BigDecimal left = calculateSimpleExpression(expression.substring(0, minusIndex));
            BigDecimal right = calculateSimpleExpression(expression.substring(minusIndex + 1));
            return left.subtract(right);
        }
        
        // 处理乘除法(高优先级)
        int multiplyIndex = expression.indexOf('*');
        int divideIndex = expression.indexOf('/');
        
        // 优先处理先出现的运算符(从左往右)
        if (multiplyIndex > 0 && (divideIndex < 0 || multiplyIndex < divideIndex)) {
            BigDecimal left = calculateSimpleExpression(expression.substring(0, multiplyIndex));
            BigDecimal right = calculateSimpleExpression(expression.substring(multiplyIndex + 1));
            return left.multiply(right);
        }
        
        if (divideIndex > 0) {
            BigDecimal left = calculateSimpleExpression(expression.substring(0, divideIndex));
            BigDecimal right = calculateSimpleExpression(expression.substring(divideIndex + 1));
            if (right.compareTo(BigDecimal.ZERO) == 0) {
                throw new ArithmeticException("除数不能为零");
            }
            return left.divide(right, 10, RoundingMode.HALF_UP);
        }
        
        // 解析数字
        try {
            return new BigDecimal(expression);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无法解析数字：" + expression);
        }
    }
    
    /**
     * 查找运算符索引 (忽略括号内的运算符)
     */
    private int findOperatorIndex(String expression, char operator) {
        int parenDepth = 0;
        for (int i = expression.length() - 1; i >= 0; i--) {
            char c = expression.charAt(i);
            if (c == ')') {
                parenDepth++;
            } else if (c == '(') {
                parenDepth--;
            } else if (c == operator && parenDepth == 0) {
                // 确保不是负号 (前面是运算符或开头)
                if (i > 0) {
                    char prev = expression.charAt(i - 1);
                    if (prev == '+' || prev == '-' || prev == '*' || prev == '/') {
                        continue;
                    }
                }
                return i;
            }
        }
        return -1;
    }
}
