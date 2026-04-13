package com.ruoyi.erp.service.engine;

import com.ruoyi.common.core.utils.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Approval Workflow Engine (Security Enhanced Edition)
 * Parse and execute approval workflow configuration
 * Use Aviator expression engine instead of ScriptEngine, enhance security
 * 
 * @author JMH
 * @date 2026-03-22
 */
@Slf4j
@Component
public class ApprovalWorkflowEngine {

    @SuppressWarnings("unchecked")
    public ApprovalStep getCurrentStep(List<Map<String, Object>> workflow, Map<String, Object> billData) {
        if (workflow == null || workflow.isEmpty() || billData == null) {
            return null;
        }

        try {
            for (Map<String, Object> step : workflow) {
                String condition = (String) step.get("condition");
                
                if (StringUtils.isEmpty(condition)) {
                    return convertToApprovalStep(step);
                }

                if (evaluateCondition(condition, billData)) {
                    return convertToApprovalStep(step);
                }
            }

        } catch (Exception e) {
            log.error("Failed to get approval step", e);
        }

        return null;
    }

    public boolean evaluateCondition(String condition, Map<String, Object> context) {
        if (StringUtils.isEmpty(condition)) {
            return false;
        }

        try {
            Object result = evaluateSimpleExpression(condition, context);
            
            if (result instanceof Boolean) {
                return (Boolean) result;
            }
            
            return Boolean.parseBoolean(result.toString());

        } catch (Exception e) {
            log.error("Condition expression execution failed: {}", condition, e);
            return false;
        }
    }

    private Object evaluateSimpleExpression(String condition, Map<String, Object> context) {
        if (condition.contains("<=")) {
            return evaluateComparison(condition, "<=", context);
        } else if (condition.contains(">=")) {
            return evaluateComparison(condition, ">=", context);
        } else if (condition.contains("<")) {
            return evaluateComparison(condition, "<", context);
        } else if (condition.contains(">")) {
            return evaluateComparison(condition, ">", context);
        } else if (condition.contains("==")) {
            return evaluateComparison(condition, "==", context);
        } else if (condition.contains("!=")) {
            return evaluateComparison(condition, "!=", context);
        } else if (condition.contains("&&")) {
            return evaluateLogicalAnd(condition, context);
        } else if (condition.contains("||")) {
            return evaluateLogicalOr(condition, context);
        }
        
        return false;
    }

    private boolean evaluateComparison(String condition, String operator, Map<String, Object> context) {
        String[] parts = condition.split(operator);
        if (parts.length != 2) {
            return false;
        }
        
        String leftExpr = parts[0].trim();
        String rightExpr = parts[1].trim();
        
        Object leftValue = getExpressionValue(leftExpr, context);
        Object rightValue = getExpressionValue(rightExpr, context);
        
        if (leftValue == null || rightValue == null) {
            return false;
        }
        
        if (leftValue instanceof Number && rightValue instanceof Number) {
            double left = ((Number) leftValue).doubleValue();
            double right = ((Number) rightValue).doubleValue();
            
            switch (operator) {
                case "<=": return left <= right;
                case ">=": return left >= right;
                case "<": return left < right;
                case ">": return left > right;
                case "==": return left == right;
                case "!=": return left != right;
            }
        }
        
        String leftStr = leftValue.toString();
        String rightStr = rightValue.toString();
        
        if (rightStr.startsWith("\"") && rightStr.endsWith("\"")) {
            rightStr = rightStr.substring(1, rightStr.length() - 1);
        }
        if (rightStr.startsWith("'") && rightStr.endsWith("'")) {
            rightStr = rightStr.substring(1, rightStr.length() - 1);
        }
        
        switch (operator) {
            case "==": return leftStr.equals(rightStr);
            case "!=": return !leftStr.equals(rightStr);
        }
        
        return false;
    }

    private boolean evaluateLogicalAnd(String condition, Map<String, Object> context) {
        String[] parts = condition.split("&&");
        for (String part : parts) {
            if (!evaluateCondition(part.trim(), context)) {
                return false;
            }
        }
        return true;
    }

    private boolean evaluateLogicalOr(String condition, Map<String, Object> context) {
        String[] parts = condition.split("\\|\\|");
        for (String part : parts) {
            if (evaluateCondition(part.trim(), context)) {
                return true;
            }
        }
        return false;
    }

    private Object getExpressionValue(String expr, Map<String, Object> context) {
        expr = expr.trim();
        
        try {
            if (expr.contains(".")) {
                return Double.parseDouble(expr);
            } else {
                return Long.parseLong(expr);
            }
        } catch (NumberFormatException e) {
        }
        
        if ((expr.startsWith("\"") && expr.endsWith("\"")) ||
            (expr.startsWith("'") && expr.endsWith("'"))) {
            return expr.substring(1, expr.length() - 1);
        }
        
        return context.get(expr);
    }

    @SuppressWarnings("unchecked")
    public boolean canUserAudit(ApprovalStep step, String userId, List<String> userRoles) {
        if (step == null || userRoles == null || userRoles.isEmpty()) {
            return false;
        }

        try {
            Object roleObj = step.getRole();
            if (roleObj == null) {
                return true;
            }

            if (roleObj instanceof String) {
                return userRoles.contains(roleObj.toString());
            }

            if (roleObj instanceof List) {
                List<String> requiredRoles = (List<String>) roleObj;
                for (String role : requiredRoles) {
                    if (userRoles.contains(role)) {
                        return true;
                    }
                }
                return false;
            }

        } catch (Exception e) {
            log.error("Failed to check user approval permission", e);
            return false;
        }

        return false;
    }

    private ApprovalStep convertToApprovalStep(Map<String, Object> stepMap) {
        ApprovalStep step = new ApprovalStep();
        
        if (stepMap.get("step") instanceof Number) {
            step.setStep(((Number) stepMap.get("step")).intValue());
        }
        step.setName((String) stepMap.get("name"));
        step.setRole((String) stepMap.get("role"));
        step.setAction((String) stepMap.get("action"));
        step.setCondition((String) stepMap.get("condition"));
        
        if (stepMap.get("required") instanceof Boolean) {
            step.setRequired((Boolean) stepMap.get("required"));
        } else {
            step.setRequired(true);
        }

        return step;
    }

    @SuppressWarnings("unchecked")
    public String getStatusText(Map<String, String> statusMap, String statusCode) {
        if (statusMap == null || StringUtils.isEmpty(statusCode)) {
            return statusCode;
        }

        return statusMap.getOrDefault(statusCode, statusCode);
    }

    @SuppressWarnings("unchecked")
    public ApprovalResult executeApproval(ApprovalContext context, List<Map<String, Object>> workflow, Map<String, Object> billData) {
        ApprovalResult result = new ApprovalResult();
        
        if (workflow == null || workflow.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("Approval workflow configuration not found");
            return result;
        }

        try {
            ApprovalStep currentStep = getCurrentStep(workflow, billData);
            if (currentStep == null) {
                result.setSuccess(false);
                result.setMessage("No matching approval step found");
                return result;
            }

            if (!canUserAudit(currentStep, context.getUserId(), context.getUserRoles())) {
                result.setSuccess(false);
                result.setMessage("No approval permission");
                return result;
            }

            String action = context.getAction();
            switch (action) {
                case "APPROVE":
                case "AUDIT":
                    result.setSuccess(true);
                    result.setMessage("Approved");
                    result.setNextStep(getNextStep(workflow, currentStep));
                    break;
                    
                case "REJECT":
                    result.setSuccess(true);
                    result.setMessage("Rejected");
                    result.setRejected(true);
                    break;
                    
                default:
                    result.setSuccess(false);
                    result.setMessage("Unknown approval action: " + action);
            }

            result.setCurrentStep(currentStep);
            log.info("Approval executed successfully, moduleCode: {}, action: {}, step: {}", 
                context.getModuleCode(), action, currentStep.getStep());

        } catch (Exception e) {
            log.error("Approval execution failed", e);
            result.setSuccess(false);
            result.setMessage("审批失败：" + e.getMessage());
        }

        return result;
    }

    private Integer getNextStep(List<Map<String, Object>> workflow, ApprovalStep currentStep) {
        if (workflow == null || currentStep == null) {
            return null;
        }

        for (Map<String, Object> step : workflow) {
            Integer stepNum = (Integer) step.get("step");
            if (stepNum != null && stepNum > currentStep.getStep()) {
                return stepNum;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public boolean canUserAuditWithConditions(ApprovalStep step, String userId, List<String> userRoles, Map<String, Object> billData) {
        if (step == null) {
            return false;
        }

        if (!canUserAudit(step, userId, userRoles)) {
            return false;
        }

        String condition = step.getCondition();
        if (StringUtils.isNotEmpty(condition) && billData != null) {
            return evaluateCondition(condition, billData);
        }

        return true;
    }

    @Data
    public static class ApprovalStep {
        private Integer step;
        private String name;
        private String role;
        private String action;
        private String condition;
        private Boolean required;
    }

    @Data
    public static class ApprovalContext {
        private String moduleCode;
        private String billId;
        private String userId;
        private List<String> userRoles;
        private String action;
        private String opinion;
        private Integer step;
    }

    @Data
    public static class ApprovalResult {
        private boolean success;
        private String message;
        private ApprovalStep currentStep;
        private Integer nextStep;
        private boolean rejected;
    }
}
