package com.ruoyi.erp.service.engine;
import com.ruoyi.common.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 表单验证引擎
 * 根据配置验证表单数据
 * 
 * @author JMH
 * @date 2026-03-22
 */
@Slf4j
@Component("erpFormValidationEngine")
public class FormValidationEngine {

    @SuppressWarnings("unchecked")
    public ValidationResult validate(Map<String, Object> formData, Map<String, Object> validationConfig) {
        ValidationResult result = new ValidationResult();
        
        if (formData == null || validationConfig == null) {
            return result;
        }

        try {
            List<Map<String, Object>> rules = (List<Map<String, Object>>) validationConfig.get("rules");
            if (rules == null || rules.isEmpty()) {
                result.setValid(true);
                return result;
            }

            for (Map<String, Object> rule : rules) {
                String field = (String) rule.get("field");
                String ruleType = (String) rule.get("rule");
                String message = (String) rule.get("message");

                if (StringUtils.isEmpty(field)) {
                    continue;
                }

                Object value = formData.get(field);

                // 执行验证规则
                boolean passed = validateRule(field, value, ruleType, rule);
                
                if (!passed) {
                    result.addError(field, StringUtils.defaultString(message, field + " 验证失败"));
                }
            }

            result.setValid(result.getErrors().isEmpty());

        } catch (Exception e) {
            log.error("表单验证执行失败", e);
            result.setValid(false);
            result.addError("system", "验证系统异常：" + e.getMessage());
        }

        return result;
    }

    private boolean validateRule(String field, Object value, String ruleType, Map<String, Object> rule) {
        if (value == null || StringUtils.isEmpty(value.toString())) {
            return "required".equals(ruleType);
        }

        String strValue = value.toString();

        switch (StringUtils.defaultString(ruleType)) {
            case "required":
                return StringUtils.isNotEmpty(strValue);
            
            case "email":
                return isValidEmail(strValue);
            
            case "phone":
                return isValidPhone(strValue);
            
            case "number":
                return isNumeric(strValue);
            
            case "integer":
                return isInteger(strValue);
            
            case "min":
                return validateMin(value, rule);
            
            case "max":
                return validateMax(value, rule);
            
            case "minLength":
                return validateMinLength(strValue, rule);
            
            case "maxLength":
                return validateMaxLength(strValue, rule);
            
            case "pattern":
                return validatePattern(strValue, rule);
            
            case "range":
                return validateRange(value, rule);
            
            default:
                // 未知规则类型，默认通过
                return true;
        }
    }

    private boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        return email.matches(regex);
    }

    private boolean isValidPhone(String phone) {
        String regex = "^1[3-9]\\d{9}$";
        return phone.matches(regex);
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean validateMin(Object value, Map<String, Object> rule) {
        Object minObj = rule.get("value");
        if (minObj == null) {
            return true;
        }

        if (value instanceof Number && minObj instanceof Number) {
            return ((Number) value).doubleValue() >= ((Number) minObj).doubleValue();
        }

        return value.toString().compareTo(minObj.toString()) >= 0;
    }

    private boolean validateMax(Object value, Map<String, Object> rule) {
        Object maxObj = rule.get("value");
        if (maxObj == null) {
            return true;
        }

        if (value instanceof Number && maxObj instanceof Number) {
            return ((Number) value).doubleValue() <= ((Number) maxObj).doubleValue();
        }

        return value.toString().compareTo(maxObj.toString()) <= 0;
    }

    private boolean validateMinLength(String strValue, Map<String, Object> rule) {
        Object minLenObj = rule.get("value");
        if (minLenObj == null) {
            return true;
        }

        int minLen = Integer.parseInt(minLenObj.toString());
        return strValue.length() >= minLen;
    }

    private boolean validateMaxLength(String strValue, Map<String, Object> rule) {
        Object maxLenObj = rule.get("value");
        if (maxLenObj == null) {
            return true;
        }

        int maxLen = Integer.parseInt(maxLenObj.toString());
        return strValue.length() <= maxLen;
    }

    private boolean validatePattern(String strValue, Map<String, Object> rule) {
        Object patternObj = rule.get("pattern");
        if (patternObj == null) {
            return true;
        }

        String regex = patternObj.toString();
        return strValue.matches(regex);
    }

    private boolean validateRange(Object value, Map<String, Object> rule) {
        Object minObj = rule.get("min");
        Object maxObj = rule.get("max");

        if (minObj == null || maxObj == null) {
            return true;
        }

        if (value instanceof Number && minObj instanceof Number && maxObj instanceof Number) {
            double numValue = ((Number) value).doubleValue();
            double min = ((Number) minObj).doubleValue();
            double max = ((Number) maxObj).doubleValue();
            return numValue >= min && numValue <= max;
        }

        return false;
    }

    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private boolean valid;
        private List<String> errors;

        public ValidationResult() {
            this.valid = true;
            this.errors = new ArrayList<>();
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void addError(String field, String message) {
            this.errors.add("[" + field + "] " + message);
        }

        public String getErrorMessage() {
            if (errors.isEmpty()) {
                return "";
            }
            return String.join("; ", errors);
        }
    }
}
