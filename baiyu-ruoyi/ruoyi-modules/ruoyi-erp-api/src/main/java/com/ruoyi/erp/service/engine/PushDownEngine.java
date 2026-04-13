package com.ruoyi.erp.service.engine;
import com.ruoyi.common.core.utils.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Push Down Engine
 * @author JMH
 * @date 2026-03-22
 */
@Slf4j
@Component("erpPushDownEngine")
public class PushDownEngine {
    
    private static final Pattern VARIABLE_PATTERN = 
        Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b");

    @SuppressWarnings("unchecked")
    public PushResult execute(Map<String, Object> sourceData, Map<String, Object> mappingConfig) {
        PushResult result = new PushResult();
        
        if (sourceData == null || mappingConfig == null) {
            result.setSuccess(false);
            result.setMessage("Source data or mapping configuration is empty");
            return result;
        }

        try {
            Map<String, Object> targetData = new HashMap<>();

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

            Map<String, Object> defaultValue = 
                (Map<String, Object>) mappingConfig.get("defaultValue");
            if (defaultValue != null) {
                applyDefaults(targetData, defaultValue, sourceData);
            }

            Map<String, String> transformation = 
                (Map<String, String>) mappingConfig.get("transformation");
            if (transformation != null) {
                applyTransformations(targetData, transformation, sourceData);
            }

            result.setData(targetData);
            result.setSuccess(true);
            result.setMessage("Push down successful");

        } catch (Exception e) {
            log.error("Push down execution failed", e);
            result.setSuccess(false);
            result.setMessage("Push down failed: " + e.getMessage());
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private void applyDefaults(Map<String, Object> targetData, 
                               Map<String, Object> defaults,
                               Map<String, Object> context) {
        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            String field = entry.getKey();
            Object defaultValue = entry.getValue();

            if (targetData.containsKey(field)) {
                continue;
            }

            if (defaultValue instanceof String) {
                String strValue = (String) defaultValue;
                if ("${currentUser}".equals(strValue)) {
                    targetData.put(field, context.get("currentUserId"));
                } else if ("${now}".equals(strValue)) {
                    targetData.put(field, new Date());
                } else if (strValue.startsWith("${") && strValue.endsWith("}")) {
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
                log.error("Formula calculation failed: {}", formula, e);
            }
        }
    }

    private Object evaluateFormula(String formula, 
                                   Map<String, Object> targetData,
                                   Map<String, Object> context) {
        if (StringUtils.isEmpty(formula)) {
            return null;
        }

        try {
            Map<String, Object> allContext = new HashMap<>(context);
            allContext.putAll(targetData);

            return evaluateSimpleFormula(formula, allContext);

        } catch (Exception e) {
            log.error("Formula execution failed: {}", formula, e);
            return null;
        }
    }

    private Object evaluateSimpleFormula(String formula, Map<String, Object> context) {
        String expression = formula;
        
        java.util.regex.Matcher matcher = VARIABLE_PATTERN.matcher(formula);
        
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String varName = matcher.group(1);
            Object value = context.get(varName);
            
            if (value != null) {
                if (value instanceof Number) {
                    matcher.appendReplacement(sb, value.toString());
                } else {
                    try {
                        double numValue = Double.parseDouble(value.toString());
                        matcher.appendReplacement(sb, String.valueOf(numValue));
                    } catch (NumberFormatException e) {
                        log.warn("Variable {} value is not numeric: {}", varName, value);
                        matcher.appendReplacement(sb, "0");
                    }
                }
            } else {
                matcher.appendReplacement(sb, "0");
            }
        }
        matcher.appendTail(sb);
        expression = sb.toString();

        return calculateExpression(expression);
    }

    private Object calculateExpression(String expression) {
        try {
            return new SimpleCalculator().calculate(expression);
        } catch (Exception e) {
            log.error("Expression calculation failed: {}", expression, e);
            return null;
        }
    }

    private static class SimpleCalculator {
        private int pos = -1;
        private int ch;
        private String str;

        public double calculate(String expression) {
            this.str = expression.replaceAll("\\s+", "");
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
                    if (Math.abs(divisor) < 1e-10) {
                        throw new ArithmeticException("Divisor cannot be zero");
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

    @Data
    public static class PushResult {
        private boolean success;
        private String message;
        private Map<String, Object> data;
    }
}
