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
 * Computed Field Engine
 * 
 * @author JMH
 * @date 2026-03-24
 */
@Slf4j
@Component
public class ComputedFieldEngine {
    
    public Map<String, Object> computeFields(
            Map<String, Object> data,
            List<ComputedFieldConfig> configs) {
        
        if (CollUtil.isEmpty(configs)) {
            return data;
        }
        
        Map<String, Object> result = new HashMap<>(data);
        
        for (ComputedFieldConfig config : configs) {
            try {
                Object value = evaluateFormula(config.getFormula(), result);
                
                if (value instanceof Number && config.getPrecision() != null) {
                    if (value instanceof BigDecimal) {
                        value = ((BigDecimal) value).setScale(config.getPrecision(), RoundingMode.HALF_UP);
                    } else if (value instanceof Double || value instanceof Float) {
                        value = NumberUtil.round(((Number) value).doubleValue(), config.getPrecision());
                    }
                }
                
                result.put(config.getTargetField(), value);
                
                log.debug("Computed field {} = {}", config.getTargetField(), value);
                
            } catch (Exception e) {
                log.error("Failed to compute field {}: {}", config.getTargetField(), e.getMessage());
                throw new ComputedFieldException(
                    config.getTargetField(), 
                    config.getFormula(), 
                    "Calculation failed: " + e.getMessage(),
                    e
                );
            }
        }
        
        return result;
    }
    
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
    
    private Object evaluateFormula(String formula, Map<String, Object> context) {
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
            return evaluateExpression(formula, context);
        }
    }
    
    private Object evaluateSum(String formula, Map<String, Object> context) {
        String fieldPath = formula.substring(4, formula.length() - 1);
        String[] parts = fieldPath.split("\\.");
        
        if (parts.length != 2) {
            throw new IllegalArgumentException("SUM function format error, should be: SUM(list.field)");
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
    
    private Object evaluateAvg(String formula, Map<String, Object> context) {
        String fieldPath = formula.substring(4, formula.length() - 1);
        String[] parts = fieldPath.split("\\.");
        
        if (parts.length != 2) {
            throw new IllegalArgumentException("AVG function format error, should be: AVG(list.field)");
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
    
    private Object evaluateCount(String formula, Map<String, Object> context) {
        String fieldPath = formula.substring(6, formula.length() - 1);
        Object listObj = context.get(fieldPath);
        
        if (!(listObj instanceof List)) {
            return 0;
        }
        
        return ((List<?>) listObj).size();
    }
    
    private Object evaluateMax(String formula, Map<String, Object> context) {
        String fieldPath = formula.substring(4, formula.length() - 1);
        String[] parts = fieldPath.split("\\.");
        
        if (parts.length != 2) {
            throw new IllegalArgumentException("MAX function format error, should be: MAX(list.field)");
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
    
    private Object evaluateMin(String formula, Map<String, Object> context) {
        String fieldPath = formula.substring(4, formula.length() - 1);
        String[] parts = fieldPath.split("\\.");
        
        if (parts.length != 2) {
            throw new IllegalArgumentException("MIN function format error, should be: MIN(list.field)");
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
    
    private Object evaluateExpression(String expression, Map<String, Object> context) {
        try {
            String evaluatedExpr = expression;
            for (Map.Entry<String, Object> entry : context.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Number) {
                    evaluatedExpr = evaluatedExpr.replaceAll("\\b" + key + "\\b", value.toString());
                }
            }
            
            return calculateSimpleExpression(evaluatedExpr);
        } catch (Exception e) {
            throw new RuntimeException("Expression calculation failed: " + expression, e);
        }
    }
    
    private BigDecimal calculateSimpleExpression(String expression) {
        expression = expression.replaceAll("\\s+", "");
        
        int parenStart = expression.lastIndexOf('(');
        if (parenStart >= 0) {
            int parenEnd = expression.indexOf(')', parenStart);
            if (parenEnd < 0) {
                throw new IllegalArgumentException("Parentheses mismatch");
            }
            
            String before = expression.substring(0, parenStart);
            String inside = expression.substring(parenStart + 1, parenEnd);
            String after = expression.substring(parenEnd + 1);
            
            BigDecimal insideValue = calculateSimpleExpression(inside);
            return calculateSimpleExpression(before + insideValue + after);
        }
        
        int plusIndex = findOperatorIndex(expression, '+');
        if (plusIndex > 0) {
            BigDecimal left = calculateSimpleExpression(expression.substring(0, plusIndex));
            BigDecimal right = calculateSimpleExpression(expression.substring(plusIndex + 1));
            return left.add(right);
        }
        
        int minusIndex = findOperatorIndex(expression, '-');
        if (minusIndex > 0) {
            if (minusIndex == 0) {
                return calculateSimpleExpression(expression.substring(1)).negate();
            }
            char prevChar = expression.charAt(minusIndex - 1);
            if (prevChar == '+' || prevChar == '-' || prevChar == '*' || prevChar == '/') {
                int nextMinus = findOperatorIndex(expression.substring(0, minusIndex), '-');
                if (nextMinus > 0) {
                    minusIndex = nextMinus;
                } else {
                    return calculateSimpleExpression(expression.substring(1)).negate();
                }
            }
            
            BigDecimal left = calculateSimpleExpression(expression.substring(0, minusIndex));
            BigDecimal right = calculateSimpleExpression(expression.substring(minusIndex + 1));
            return left.subtract(right);
        }
        
        int multiplyIndex = expression.indexOf('*');
        int divideIndex = expression.indexOf('/');
        
        if (multiplyIndex > 0 && (divideIndex < 0 || multiplyIndex < divideIndex)) {
            BigDecimal left = calculateSimpleExpression(expression.substring(0, multiplyIndex));
            BigDecimal right = calculateSimpleExpression(expression.substring(multiplyIndex + 1));
            return left.multiply(right);
        }
        
        if (divideIndex > 0) {
            BigDecimal left = calculateSimpleExpression(expression.substring(0, divideIndex));
            BigDecimal right = calculateSimpleExpression(expression.substring(divideIndex + 1));
            if (right.compareTo(BigDecimal.ZERO) == 0) {
                throw new ArithmeticException("Divisor cannot be zero");
            }
            return left.divide(right, 10, RoundingMode.HALF_UP);
        }
        
        try {
            return new BigDecimal(expression);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot parse number: " + expression);
        }
    }
    
    private int findOperatorIndex(String expression, char operator) {
        int parenDepth = 0;
        for (int i = expression.length() - 1; i >= 0; i--) {
            char c = expression.charAt(i);
            if (c == ')') {
                parenDepth++;
            } else if (c == '(') {
                parenDepth--;
            } else if (c == operator && parenDepth == 0) {
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
