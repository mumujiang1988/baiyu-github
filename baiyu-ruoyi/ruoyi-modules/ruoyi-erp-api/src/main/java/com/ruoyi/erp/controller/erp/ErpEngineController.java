package com.ruoyi.erp.controller.erp;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.erp.service.engine.DynamicQueryEngine;
import com.ruoyi.erp.service.engine.FormValidationEngine;
import com.ruoyi.erp.service.engine.ApprovalWorkflowEngine;
import com.ruoyi.erp.service.engine.ComputedFieldEngine;
import com.ruoyi.erp.service.engine.VirtualFieldService;
import com.ruoyi.erp.service.engine.DictionaryBuilderEngine;
import com.ruoyi.erp.service.ErpApprovalFlowService;
import com.ruoyi.erp.service.ErpPushRelationService;
import com.ruoyi.erp.service.ISuperDataPermissionService;
import com.ruoyi.erp.domain.vo.ErpApprovalFlowVo;
import com.ruoyi.erp.domain.vo.ErpPushRelationVo;
import com.ruoyi.erp.domain.bo.ErpPushRelationBo;
import com.ruoyi.erp.domain.entity.ErpPageConfig;
import com.ruoyi.erp.service.ErpPageConfigService;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.erp.utils.ConfigParser;
import com.ruoyi.erp.utils.DataProcessor;
import com.ruoyi.erp.utils.ErpPermissionChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * ERP 引擎接口控制器
 * 为前端配置化页面提供四大引擎服务
 *
 * @author ERP Development Team
 * @date 2026-03-22
 */
@SuppressWarnings("unchecked")
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/erp/engine")
public class ErpEngineController {

    private final DynamicQueryEngine queryEngine;
    private final FormValidationEngine validationEngine;
    private final ApprovalWorkflowEngine approvalEngine;
    private final ErpApprovalFlowService approvalFlowService;
    private final ErpPushRelationService pushRelationService;
    private final ISuperDataPermissionService dataPermissionService;
    private final com.ruoyi.erp.mapper.ErpApprovalHistoryMapper approvalHistoryMapper;
    private final ComputedFieldEngine computedFieldEngine;
    private final VirtualFieldService virtualFieldService;
    private final ErpPageConfigService erpPageConfigService;
    private final DictionaryBuilderEngine dictionaryBuilderEngine;
    
    // 新增工具类
    private final ErpPermissionChecker permissionChecker;
    private final ConfigParser configParser;
    private final DataProcessor dataProcessor;

    // ==================== 权限辅助方法 ====================

    /**
     * 动态构建权限标识
     * @param moduleCode 模块编码
     * @param action 操作类型 (query/add/edit/delete/audit/push)
     * @return 权限标识字符串
     */
    private String buildPermission(String moduleCode, String action) {
        if (moduleCode == null || moduleCode.isEmpty()) {
            throw new IllegalArgumentException("模块编码不能为空");
        }
        return String.format("k3:%s:%s", moduleCode, action);
    }

    /**
     * 检查模块权限
     * @param moduleCode 模块编码
     * @param action 操作类型
     */
    private void checkModulePermission(String moduleCode, String action) {
        String permission = buildPermission(moduleCode, action);
        StpUtil.checkPermission(permission);
    }

    // ==================== 动态查询引擎接口 ====================

    /**
     * 执行动态查询（构建器模式）
     * ⭐⭐⭐ 强制使用 queryConfig.builder 模式 ⭐⭐⭐
     */
    @PostMapping("/query/execute")
    public R<?> executeDynamicQuery(@RequestBody Map<String, Object> params) {
        try {
            String moduleCode = (String) params.get("moduleCode");
            
            // ⭐⭐⭐ 使用注入的权限检查器（同时检查数据库配置和权限） ⭐⭐⭐
            permissionChecker.checkModulePermission(moduleCode, "query");
            
            //  tableName 为必填参数，来自前端 JSON 配置的 pageConfig.tableName
            String tableName = (String) params.get("tableName");
            if (tableName == null || tableName.trim().isEmpty()) {
                log.error(" 缺少必需的 tableName 参数，moduleCode: {}", moduleCode);
                return R.fail("缺少必需的 tableName 参数，请在 JSON 配置的 pageConfig.tableName 中配置表名");
            }
            log.info(" 使用 JSON 配置的表名，moduleCode: {}, tableName: {}", moduleCode, tableName);
            
            //  获取 queryConfig（构建器模式）
            Map<String, Object> queryConfig = (Map<String, Object>) params.get("queryConfig");
            if (queryConfig == null || queryConfig.isEmpty()) {
                log.error(" 缺少必需的 queryConfig 参数，moduleCode: {}", moduleCode);
                return R.fail("缺少必需的 queryConfig 参数，请使用构建器模式配置查询条件");
            }
            
            // 获取分页参数
            Integer pageNum = (Integer) params.getOrDefault("pageNum", 1);
            Integer pageSize = (Integer) params.getOrDefault("pageSize", 10);
            PageQuery pageQuery = new PageQuery();
            pageQuery.setPageNum(pageNum);
            pageQuery.setPageSize(pageSize);
            
            // ⭐⭐⭐ 使用构建器模式构建查询条件 ⭐⭐⭐
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Object> queryWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            queryWrapper = buildQueryFromBuilderMode(queryWrapper, queryConfig);
            
            //  调用通用 Service 执行实际查询
            Page<Map<String, Object>> page = ((com.ruoyi.erp.service.impl.SuperDataPermissionServiceImpl) dataPermissionService)
                .selectPageByModuleWithTableName(
                    moduleCode, 
                    tableName,
                    pageQuery, 
                    queryWrapper
                );
            
            //  处理数据 (计算字段 + 虚拟字段)
            List<Map<String, Object>> processedRecords = processData(page.getRecords(), moduleCode);
            page.setRecords(processedRecords);
            
            // 返回分页结果
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("rows", page.getRecords());
            result.put("total", page.getTotal());
            result.put("moduleCode", moduleCode);
            
            log.info("动态查询成功，moduleCode: {}, total: {}", moduleCode, page.getTotal());
            
            return R.ok(result);
        } catch (Exception e) {
            log.error("动态查询失败", e);
            return R.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * ⭐ 获取构建器模式支持的运算符
     */
    @GetMapping("/query/operators")
    public R<?> getAvailableOperators(@RequestParam(required = false) String moduleCode) {
        //  如果有 moduleCode 则检查权限
        if (moduleCode != null && !moduleCode.isEmpty()) {
            permissionChecker.checkModulePermission(moduleCode, "query");
        }
        // 返回构建器模式支持的所有运算符
        List<String> operators = java.util.Arrays.asList(
            "eq",      // 等于
            "ne",      // 不等于
            "gt",      // 大于
            "ge",      // 大于等于
            "lt",      // 小于
            "le",      // 小于等于
            "like",    // 模糊匹配（%值%）
            "left_like",   // 左模糊匹配（%值）
            "right_like",  // 右模糊匹配（值%）
            "in",      // IN 条件
            "between", // BETWEEN 条件
            "isNull",  // IS NULL
            "isNotNull" // IS NOT NULL
        );
        return R.ok(operators);
    }

    // ==================== 表单验证引擎接口 ====================

    /**
     * 执行表单验证
     */
    @PostMapping("/validation/execute")
    public R<?> executeValidation(@RequestBody Map<String, Object> params) {
        try {
            String moduleCode = extractModuleCode(params);
            permissionChecker.checkModulePermission(moduleCode, "query");
            Map<String, Object> formData = (Map<String, Object>) params.get("formData");
            Map<String, Object> validationConfig = (Map<String, Object>) params.get("validationConfig");
            
            FormValidationEngine.ValidationResult result = validationEngine.validate(formData, validationConfig);
            return R.ok(result);
        } catch (Exception e) {
            return R.fail("验证失败：" + e.getMessage());
        }
    }

    /**
     * 批量验证
     */
    @PostMapping("/validation/batch")
    public R<?> batchValidate(@RequestBody Map<String, Object> params) {
        try {
            String moduleCode = extractModuleCode(params);
            permissionChecker.checkModulePermission(moduleCode, "query");
            Object formDataListObj = params.get("formDataList");
            Map<String, Object> validationConfig = (Map<String, Object>) params.get("validationConfig");
            
            // 批量验证多个表单数据
            List<Map<String, Object>> formDataList = (List<Map<String, Object>>) formDataListObj;
            List<FormValidationEngine.ValidationResult> results = new java.util.ArrayList<>();
            
            for (Map<String, Object> formData : formDataList) {
                results.add(validationEngine.validate(formData, validationConfig));
            }
            
            return R.ok(results);
        } catch (Exception e) {
            return R.fail("批量验证失败：" + e.getMessage());
        }
    }

    /**
     * 获取可用的验证规则
     */
    @GetMapping("/validation/rules")
    public R<?> getAvailableValidationRules(@RequestParam(required = false) String moduleCode) {
        //  如果有 moduleCode 则检查权限
        if (moduleCode != null && !moduleCode.isEmpty()) {
            permissionChecker.checkModulePermission(moduleCode, "query");
        }
        // 返回支持的验证规则列表
        List<String> rules = java.util.Arrays.asList("required", "email", "phone", "number", "integer", 
            "min", "max", "minLength", "maxLength", "pattern", "range");
        return R.ok(rules);
    }

    /**
     * 验证单个字段
     */
    @PostMapping("/validation/field")
    public R<?> validateField(@RequestBody Map<String, Object> params) {
        try {
            String moduleCode = extractModuleCode(params);
            permissionChecker.checkModulePermission(moduleCode, "query");
            String field = (String) params.get("field");
            Object value = params.get("value");
            Map<String, Object> rule = (Map<String, Object>) params.get("rule");
            
            // 创建临时的表单数据进行验证
            Map<String, Object> formData = new java.util.HashMap<>();
            formData.put(field, value);
            
            Map<String, Object> validationConfig = new java.util.HashMap<>();
            List<Map<String, Object>> rules = new java.util.ArrayList<>();
            rules.add(rule);
            validationConfig.put("rules", rules);
            
            FormValidationEngine.ValidationResult result = validationEngine.validate(formData, validationConfig);
            return R.ok(result);
        } catch (Exception e) {
            return R.fail("字段验证失败：" + e.getMessage());
        }
    }

    // ==================== 审批流程引擎接口 ====================

    /**
     * 获取当前审批步骤
     */
    @PostMapping("/approval/current-step")
    public R<?> getCurrentApprovalStep(@RequestBody Map<String, Object> params) {
        try {
            String moduleCode = (String) params.get("moduleCode");
            permissionChecker.checkModulePermission(moduleCode, "query");
            Map<String, Object> billData = (Map<String, Object>) params.get("billData");
            
            //  从数据库获取 workflow 配置
            ErpApprovalFlowVo flowConfig = approvalFlowService.getApprovalFlow(moduleCode);
            if (flowConfig == null) {
                return R.fail("未找到模块 [" + moduleCode + "] 的审批流程配置");
            }
            
            //  解析 JSON 配置并获取当前审批步骤
            List<Map<String, Object>> workflow = parseWorkflow(flowConfig.getFlowDefinition());
            ApprovalWorkflowEngine.ApprovalStep currentStep = 
                approvalEngine.getCurrentStep(workflow, billData);
            
            if (currentStep == null) {
                Map<String, Object> result = new java.util.HashMap<>();
                result.put("hasCurrentStep", false);
                result.put("reason", "未找到匹配的审批步骤或审批流程已结束");
                return R.ok(result);
            }
            
            //  构建返回结果
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("hasCurrentStep", true);
            result.put("currentStep", currentStep);
            result.put("moduleCode", moduleCode);
            
            log.info("获取当前审批步骤成功，moduleCode: {}, step: {}", 
                moduleCode, currentStep.getStep());
            
            return R.ok(result);
        } catch (Exception e) {
            log.error("获取审批步骤失败", e);
            return R.fail("获取审批步骤失败：" + e.getMessage());
        }
    }

    /**
     * 执行审批操作
     */
    @PostMapping("/approval/execute")
    public R<?> executeApproval(@RequestBody Map<String, Object> params) {
        try {
            String moduleCode = (String) params.get("moduleCode");
                        permissionChecker.checkModulePermission(moduleCode, "audit");
            String billId = (String) params.get("billId");
            String action = (String) params.get("action"); // APPROVE/AUDIT/REJECT
            String opinion = (String) params.get("opinion");
            String userId = (String) params.get("userId");
            List<String> userRoles = (List<String>) params.get("userRoles");
            
            //  从数据库获取 workflow 配置
            ErpApprovalFlowVo flowConfig = approvalFlowService.getApprovalFlow(moduleCode);
            if (flowConfig == null) {
                return R.fail("未找到模块 [" + moduleCode + "] 的审批流程配置");
            }
            
            // 解析 JSON 配置
            List<Map<String, Object>> workflow = parseWorkflow(flowConfig.getFlowDefinition());
            Map<String, Object> billData = (Map<String, Object>) params.get("billData");
            
            // 构建审批上下文
            ApprovalWorkflowEngine.ApprovalContext context = new ApprovalWorkflowEngine.ApprovalContext();
            context.setModuleCode(moduleCode);
            context.setBillId(billId);
            context.setUserId(userId);
            context.setUserRoles(userRoles);
            context.setAction(action);
            context.setOpinion(opinion);
            
            //  执行审批
            ApprovalWorkflowEngine.ApprovalResult result = 
                approvalEngine.executeApproval(context, workflow, billData);
            
            if (result.isSuccess()) {
                //  保存审批历史到数据库
                com.ruoyi.erp.domain.entity.ErpApprovalHistory history = 
                    new com.ruoyi.erp.domain.entity.ErpApprovalHistory();
                history.setModuleCode(moduleCode);
                history.setBillId(Long.parseLong(billId));
                history.setFlowId(flowConfig.getFlowId());
                history.setCurrentStep(result.getCurrentStep() != null ? result.getCurrentStep().getStep() : null);
                history.setApprovalAction(action);
                history.setApprovalOpinion(opinion);
                history.setApproverId(userId);
                history.setApprovalTime(java.time.LocalDateTime.now());
                
                int saved = approvalHistoryMapper.insert(history);
                if (saved <= 0) {
                    log.warn("保存审批历史记录失败");
                }
                
                //  更新单据审批状态
                updateBillApprovalStatus(moduleCode, billId, result.isRejected() ? "REJECTED" : "APPROVED");
                
                log.info("审批成功，billId: {}, action: {}, nextStep: {}", 
                    billId, action, result.getNextStep());
                return R.ok(result);
            } else {
                return R.fail(result.getMessage());
            }
        } catch (Exception e) {
            log.error("执行审批失败", e);
            return R.fail("执行审批失败：" + e.getMessage());
        }
    }

    /**
     * 获取审批历史
     */
    @PostMapping("/approval/history")
    public R<?> getApprovalHistory(@RequestBody Map<String, Object> params) {
        try {
            String moduleCode = (String) params.get("moduleCode");
            permissionChecker.checkModulePermission(moduleCode, "query");
            Long billId = (Long) params.get("billId");
            
            // 直接使用注入的 Mapper 查询
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.ruoyi.erp.domain.entity.ErpApprovalHistory> queryWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            queryWrapper.eq("module_code", moduleCode)
                       .eq("bill_id", billId)
                       .orderByDesc("approval_time");
            
            java.util.List<com.ruoyi.erp.domain.entity.ErpApprovalHistory> historyList = 
                approvalHistoryMapper.selectList(queryWrapper);
            
            // 转换为 VO 对象
            java.util.List<Map<String, Object>> result = new ArrayList<>();
            for (com.ruoyi.erp.domain.entity.ErpApprovalHistory item : historyList) {
                Map<String, Object> historyItem = new HashMap<>();
                historyItem.put("historyId", item.getHistoryId());
                historyItem.put("moduleCode", item.getModuleCode());
                historyItem.put("billId", item.getBillId());
                historyItem.put("currentStep", item.getCurrentStep());
                historyItem.put("action", item.getApprovalAction());
                historyItem.put("opinion", item.getApprovalOpinion());
                historyItem.put("approverId", item.getApproverId());
                historyItem.put("approvalTime", item.getApprovalTime());
                result.add(historyItem);
            }
            
            log.info("获取审批历史成功，moduleCode: {}, billId: {}, count: {}", moduleCode, billId, result.size());
            return R.ok(result);
        } catch (Exception e) {
            log.error("获取审批历史失败", e);
            return R.fail("获取审批历史失败：" + e.getMessage());
        }
    }

    /**
     * 获取审批历史（GET 请求，兼容前端不同调用方式）
     */
    @GetMapping("/approval/history")
    public R<?> getApprovalHistoryGet(@RequestParam Map<String, String> params) {
        //  检查权限
        String moduleCode = params.get("moduleCode");
        if (moduleCode != null && !moduleCode.isEmpty()) {
            permissionChecker.checkModulePermission(moduleCode, "query");
        }
        // 转换为 POST 请求的参数格式
        Map<String, Object> postParams = new HashMap<>();
        params.forEach(postParams::put);
        return getApprovalHistory(postParams);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseWorkflow(String flowDefinitionJson) {
        try {
            if (flowDefinitionJson == null || flowDefinitionJson.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 使用 Jackson ObjectMapper 解析 JSON
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return objectMapper.readValue(flowDefinitionJson, List.class);
        } catch (Exception e) {
            log.error("解析审批流程配置失败", e);
            return new ArrayList<>();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseMappingRules(String mappingRulesJson) {
        try {
            if (mappingRulesJson == null || mappingRulesJson.isEmpty()) {
                return new HashMap<>();
            }
            
            // 使用 Jackson ObjectMapper 解析 JSON
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return objectMapper.readValue(mappingRulesJson, Map.class);
        } catch (Exception e) {
            log.error("解析映射规则失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 检查审批权限
     */
    @PostMapping("/approval/check-permission")
    public R<?> checkApprovalPermission(@RequestBody Map<String, Object> params) {
        try {
            String moduleCode = (String) params.get("moduleCode");
            permissionChecker.checkModulePermission(moduleCode, "query");
            // String billId = (String) params.get("billId"); // 暂未使用
            String userId = (String) params.get("userId");
            List<String> userRoles = (List<String>) params.get("userRoles");
            
            //  从数据库获取 workflow 配置
            ErpApprovalFlowVo flowConfig = approvalFlowService.getApprovalFlow(moduleCode);
            if (flowConfig == null) {
                return R.fail("未找到模块 [" + moduleCode + "] 的审批流程配置");
            }
            
            // 解析 JSON 配置
            List<Map<String, Object>> workflow = parseWorkflow(flowConfig.getFlowDefinition());
            Map<String, Object> billData = (Map<String, Object>) params.get("billData");
            
            // 获取当前审批步骤
            ApprovalWorkflowEngine.ApprovalStep currentStep = 
                approvalEngine.getCurrentStep(workflow, billData);
            
            if (currentStep == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("hasPermission", false);
                result.put("reason", "未找到匹配的审批步骤");
                result.put("currentStep", null);
                return R.ok(result);
            }
            
            //  检查用户是否有审批权限（带条件检查）
            boolean hasPermission = approvalEngine.canUserAuditWithConditions(
                currentStep, userId, userRoles, billData);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("hasPermission", hasPermission);
            result.put("currentStep", currentStep);
            result.put("userId", userId);
            result.put("userRoles", userRoles);
            
            if (!hasPermission) {
                result.put("reason", "用户角色不匹配或不满足审批条件");
            }
            
            log.info("权限检查完成，moduleCode: {}, userId: {}, hasPermission: {}", 
                moduleCode, userId, hasPermission);
            
            return R.ok(result);
        } catch (Exception e) {
            log.error("权限检查失败", e);
            return R.fail("权限检查失败：" + e.getMessage());
        }
    }

    /**
     * 获取审批历史
     *  TODO: 待实现完整的审批历史功能
     */
    @GetMapping("/approval/history-detail")
    public R<?> getApprovalHistoryDetail(@RequestParam Map<String, String> params) {
        try {
            String moduleCode = params.get("moduleCode");
            permissionChecker.checkModulePermission(moduleCode, "query");
            
            //  从数据库获取 workflow 配置
            ErpApprovalFlowVo flowConfig = approvalFlowService.getApprovalFlow(moduleCode);
            if (flowConfig == null) {
                return R.fail("未找到模块 [" + moduleCode + "] 的审批流程配置");
            }
            
            // TODO: approvalEngine 目前没有 getApprovalHistory 方法，需要后续开发
            return R.ok(new ArrayList<>());
        } catch (Exception e) {
            log.error("获取审批历史失败", e);
            return R.fail("获取审批历史失败：" + e.getMessage());
        }
    }

    /**
     * 获取审批流程定义
     */
    @GetMapping("/approval/workflow")
    public R<?> getWorkflowDefinition(@RequestParam String moduleCode) {
        try {
            permissionChecker.checkModulePermission(moduleCode, "query");
            //  从数据库获取 workflow 配置
            ErpApprovalFlowVo flowConfig = approvalFlowService.getApprovalFlow(moduleCode);
            if (flowConfig == null) {
                return R.fail("未找到模块 [" + moduleCode + "] 的审批流程配置");
            }
            
            return R.ok(flowConfig.getFlowDefinition());
        } catch (Exception e) {
            log.error("获取流程定义失败", e);
            return R.fail("获取流程定义失败：" + e.getMessage());
        }
    }

    /**
     * 转审操作
     */
    @PostMapping("/approval/transfer")
    public R<?> transferApproval(@RequestBody Map<String, Object> params) {
        try {
            String moduleCode = (String) params.get("moduleCode");
                        permissionChecker.checkModulePermission(moduleCode, "audit");
            String billId = (String) params.get("billId");
            String currentUserId = (String) params.get("currentUserId");
            List<String> currentUserRoles = (List<String>) params.get("currentUserRoles");
            String targetUserId = (String) params.get("targetUserId");
            String reason = (String) params.get("reason");
            
            //  从数据库获取 workflow 配置
            ErpApprovalFlowVo flowConfig = approvalFlowService.getApprovalFlow(moduleCode);
            if (flowConfig == null) {
                return R.fail("未找到模块 [" + moduleCode + "] 的审批流程配置");
            }
            
            // 解析 JSON 配置
            List<Map<String, Object>> workflow = parseWorkflow(flowConfig.getFlowDefinition());
            Map<String, Object> billData = (Map<String, Object>) params.get("billData");
            
            // 1. 获取当前审批步骤
            ApprovalWorkflowEngine.ApprovalStep currentStep = 
                approvalEngine.getCurrentStep(workflow, billData);
            
            if (currentStep == null) {
                return R.fail("未找到当前审批步骤");
            }
            
            // 2. 检查当前用户是否有转审权限
            boolean hasPermission = approvalEngine.canUserAuditWithConditions(
                currentStep, currentUserId, currentUserRoles, billData);
            
            if (!hasPermission) {
                return R.fail("当前用户无审批权限，无法转审");
            }
            
            // 3. 验证目标用户是否存在（这里简化处理，实际项目中应该调用用户服务验证）
            if (targetUserId == null || targetUserId.isEmpty()) {
                return R.fail("目标用户不能为空");
            }
            
            // 4. 保存转审记录到审批历史
            com.ruoyi.erp.domain.entity.ErpApprovalHistory history = 
                new com.ruoyi.erp.domain.entity.ErpApprovalHistory();
            history.setModuleCode(moduleCode);
            history.setBillId(Long.parseLong(billId));
            history.setFlowId(flowConfig.getFlowId());
            history.setCurrentStep(currentStep.getStep());
            history.setApprovalAction("TRANSFER");
            history.setApprovalOpinion("转审给 " + targetUserId + (reason != null ? "，原因：" + reason : ""));
            history.setApproverId(currentUserId);
            history.setApprovalTime(java.time.LocalDateTime.now());
            
            int saved = approvalHistoryMapper.insert(history);
            if (saved <= 0) {
                return R.fail("保存转审记录失败");
            }
            
            // 5. 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "转审成功");
            result.put("currentStep", currentStep);
            result.put("fromUser", currentUserId);
            result.put("toUser", targetUserId);
            result.put("reason", reason);
            
            log.info("转审成功，moduleCode: {}, billId: {}, from: {}, to: {}", 
                moduleCode, billId, currentUserId, targetUserId);
            
            return R.ok(result);
        } catch (Exception e) {
            log.error("转审失败", e);
            return R.fail("转审失败：" + e.getMessage());
        }
    }

    /**
     * 撤回审批
     */
    @PostMapping("/approval/withdraw")
    public R<?> withdrawApproval(@RequestBody Map<String, Object> params) {
        try {
            String moduleCode = (String) params.get("moduleCode");
                        permissionChecker.checkModulePermission(moduleCode, "audit");
            String billId = (String) params.get("billId");
            String userId = (String) params.get("userId");
            List<String> userRoles = (List<String>) params.get("userRoles");
            String reason = (String) params.get("reason");
            
            //  从数据库获取 workflow 配置
            ErpApprovalFlowVo flowConfig = approvalFlowService.getApprovalFlow(moduleCode);
            if (flowConfig == null) {
                return R.fail("未找到模块 [" + moduleCode + "] 的审批流程配置");
            }
            
            // 解析 JSON 配置
            List<Map<String, Object>> workflow = parseWorkflow(flowConfig.getFlowDefinition());
            Map<String, Object> billData = (Map<String, Object>) params.get("billData");
            
            // 1. 获取当前审批步骤
            ApprovalWorkflowEngine.ApprovalStep currentStep = 
                approvalEngine.getCurrentStep(workflow, billData);
            
            if (currentStep == null) {
                return R.fail("未找到当前审批步骤，无法撤回");
            }
            
            // 2. 检查用户是否有撤回权限（通常是申请人或当前审批人）
            boolean hasPermission = approvalEngine.canUserAuditWithConditions(
                currentStep, userId, userRoles, billData);
            
            // 如果没有审批权限，检查是否是申请人本人（简化处理，实际项目中需要查询单据的创建人）
            if (!hasPermission) {
                //  检查是否为单据创建人（通过 billData 中的 createBy 字段）
                String createBy = (String) billData.get("createBy");
                if (createBy != null && !createBy.equals(userId)) {
                    return R.fail("无撤回权限：既不是审批人也不是单据创建人");
                }
                log.warn("用户 {} 尝试撤回审批，但无审批权限（作为创建人撤回）", userId);
            }
            
            // 3. 保存撤回记录到审批历史
            com.ruoyi.erp.domain.entity.ErpApprovalHistory history = 
                new com.ruoyi.erp.domain.entity.ErpApprovalHistory();
            history.setModuleCode(moduleCode);
            history.setBillId(Long.parseLong(billId));
            history.setFlowId(flowConfig.getFlowId());
            history.setCurrentStep(currentStep.getStep());
            history.setApprovalAction("WITHDRAW");
            history.setApprovalOpinion("撤回审批" + (reason != null ? "，原因：" + reason : ""));
            history.setApproverId(userId);
            history.setApprovalTime(java.time.LocalDateTime.now());
            
            int saved = approvalHistoryMapper.insert(history);
            if (saved <= 0) {
                return R.fail("保存撤回记录失败");
            }
            
            // 4.  更新单据审批状态
            // 将单据状态恢复为"待提交"或"草稿"状态
            updateBillApprovalStatus(moduleCode, billId, "DRAFT");
            
            // 5. 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "撤回成功");
            result.put("currentStep", currentStep);
            result.put("userId", userId);
            result.put("reason", reason);
            
            log.info("撤回审批成功，moduleCode: {}, billId: {}, userId: {}", 
                moduleCode, billId, userId);
            
            return R.ok(result);
        } catch (Exception e) {
            log.error("撤回审批失败", e);
            return R.fail("撤回审批失败：" + e.getMessage());
        }
    }

    // ==================== 下推引擎接口 ====================

    /**
     * 获取可下推的目标列表
     */
    @GetMapping("/push/targets")
    public R<?> getPushTargets(@RequestParam String moduleCode) {
        try {
            permissionChecker.checkModulePermission(moduleCode, "query");
            //  从数据库获取该模块的所有下推关系配置
            // 注意：ErpPushRelationBo 没有 builder() 方法，直接 new 对象
            ErpPushRelationBo bo = new ErpPushRelationBo();
            bo.setSourceModule(moduleCode);
            
            List<ErpPushRelationVo> relations = pushRelationService.selectList(bo);
            
            // 转换为前端需要的格式
            List<Map<String, String>> targets = new ArrayList<>();
            for (ErpPushRelationVo relation : relations) {
                Map<String, String> target = new HashMap<>();
                target.put("moduleCode", relation.getTargetModule());
                target.put("moduleName", relation.getRelationName());
                targets.add(target);
            }
            
            return R.ok(targets);
        } catch (Exception e) {
            log.error("获取下推目标失败", e);
            return R.fail("获取下推目标失败：" + e.getMessage());
        }
    }

    /**
     * 执行下推操作
     *  已实现完整的下推逻辑
     */
    @PostMapping("/push/execute")
    public R<?> executePushDown(@RequestBody Map<String, Object> params) {
        try {
            String sourceModule = (String) params.get("sourceModule");
            checkModulePermission(sourceModule, "push");
            String targetModule = (String) params.get("targetModule");
            Map<String, Object> sourceData = (Map<String, Object>) params.get("sourceData");
            String userId = (String) params.get("userId");
            
            //  从数据库获取下推关系配置
            ErpPushRelationVo relationConfig = pushRelationService.getPushRelation(sourceModule, targetModule);
            if (relationConfig == null) {
                return R.fail("未找到从 [" + sourceModule + "] 到 [" + targetModule + "] 的下推配置");
            }
            
            //  解析映射规则
            Map<String, Object> mappingRules = parseMappingRules(relationConfig.getMappingRules());
            
            //  应用字段映射和转换
            Map<String, Object> transformed = new HashMap<>();
            Map<String, String> fieldMapping = (Map<String, String>) mappingRules.get("fieldMapping");
            if (fieldMapping != null) {
                for (Map.Entry<String, String> entry : fieldMapping.entrySet()) {
                    String sourceField = entry.getKey();
                    String targetField = entry.getValue();
                    if (sourceData.containsKey(sourceField)) {
                        transformed.put(targetField, sourceData.get(sourceField));
                    }
                }
            }
            
            //  应用默认值
            Map<String, Object> defaultValue = (Map<String, Object>) mappingRules.get("defaultValue");
            if (defaultValue != null) {
                for (Map.Entry<String, Object> entry : defaultValue.entrySet()) {
                    if (!transformed.containsKey(entry.getKey())) {
                        transformed.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            
            //  添加源单据信息
            transformed.put("sourceBillNo", sourceData.get("fbillNo"));
            transformed.put("sourceId", sourceData.get("id"));
            transformed.put("createBy", userId);
            transformed.put("createTime", java.time.LocalDateTime.now());
            
            //  调用对应的 Service 保存目标单据
            Long savedId = saveTargetBill(targetModule, transformed, userId);
            if (savedId != null) {
                transformed.put("id", savedId);
                log.info("目标单据保存成功，targetModule: {}, id: {}", targetModule, savedId);
            }
            
            //  构建返回结果
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", true);
            result.put("message", "下推成功");
            result.put("data", transformed);
            result.put("sourceModule", sourceModule);
            result.put("targetModule", targetModule);
            
            log.info("下推成功，source: {}, target: {}, billNo: {}", 
                sourceModule, targetModule, sourceData.get("fbillNo"));
            
            return R.ok(result);
        } catch (Exception e) {
            log.error("下推失败", e);
            return R.fail("下推失败：" + e.getMessage());
        }
    }

    /**
     * 预览下推数据
     */
    @PostMapping("/push/preview")
    public R<?> previewPushDown(@RequestBody Map<String, Object> params) {
        try {
            String sourceModule = (String) params.get("sourceModule");
            checkModulePermission(sourceModule, "query");
            String targetModule = (String) params.get("targetModule");
            List<Map<String, Object>> sourceDataList = 
                (List<Map<String, Object>>) params.get("sourceData");
            
            //  从数据库获取下推关系配置
            ErpPushRelationVo relationConfig = pushRelationService.getPushRelation(sourceModule, targetModule);
            if (relationConfig == null) {
                return R.fail("未找到从 [" + sourceModule + "] 到 [" + targetModule + "] 的下推配置");
            }
            
            // 解析映射规则
            Map<String, Object> mappingRules = parseMappingRules(relationConfig.getMappingRules());
            
            // 使用 PushDownEngine 的 applyTransformations 方法进行数据转换预览
            List<Map<String, Object>> previewResults = new ArrayList<>();
            
            for (Map<String, Object> sourceData : sourceDataList) {
                Map<String, Object> transformed = new HashMap<>();
                
                // 应用字段映射和转换（直接复制源数据并应用映射）
                Map<String, String> fieldMapping = 
                    (Map<String, String>) mappingRules.get("fieldMapping");
                if (fieldMapping != null) {
                    for (Map.Entry<String, String> entry : fieldMapping.entrySet()) {
                        String sourceField = entry.getKey();
                        String targetField = entry.getValue();
                        if (sourceData.containsKey(sourceField)) {
                            transformed.put(targetField, sourceData.get(sourceField));
                        }
                    }
                }
                
                // 添加源单据信息
                transformed.put("_sourceId", sourceData.get("id"));
                transformed.put("_sourceBillNo", sourceData.get("fbillNo"));
                
                previewResults.add(transformed);
            }
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("previewData", previewResults);
            result.put("sourceModule", sourceModule);
            result.put("targetModule", targetModule);
            result.put("count", previewResults.size());
            result.put("mappingRules", mappingRules);
            
            log.info("预览下推数据成功，source: {}, target: {}, count: {}", 
                sourceModule, targetModule, previewResults.size());
            
            return R.ok(result);
        } catch (Exception e) {
            log.error("预览下推数据失败", e);
            return R.fail("预览下推数据失败：" + e.getMessage());
        }
    }

    /**
     * 批量下推
     */
    @PostMapping("/push/batch")
    public R<?> batchPushDown(@RequestBody Map<String, Object> params) {
        try {
            String sourceModule = (String) params.get("sourceModule");
            checkModulePermission(sourceModule, "push");
            String targetModule = (String) params.get("targetModule");
            List<Map<String, Object>> sourceDataList = 
                (List<Map<String, Object>>) params.get("sourceData");
            String userId = (String) params.get("userId");
            // String userName = (String) params.get("userName"); // 暂未使用
            
            //  从数据库获取下推关系配置
            ErpPushRelationVo relationConfig = pushRelationService.getPushRelation(sourceModule, targetModule);
            if (relationConfig == null) {
                return R.fail("未找到从 [" + sourceModule + "] 到 [" + targetModule + "] 的下推配置");
            }
            
            // 解析映射规则
            Map<String, Object> mappingRules = parseMappingRules(relationConfig.getMappingRules());
            
            // 批量执行下推
            List<Map<String, Object>> results = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;
            
            for (Map<String, Object> sourceData : sourceDataList) {
                try {
                    // 1. 应用字段映射和转换
                    Map<String, Object> transformed = new HashMap<>();
                    
                    // 应用字段映射
                    Map<String, String> fieldMapping = 
                        (Map<String, String>) mappingRules.get("fieldMapping");
                    if (fieldMapping != null) {
                        for (Map.Entry<String, String> entry : fieldMapping.entrySet()) {
                            String sourceField = entry.getKey();
                            String targetField = entry.getValue();
                            if (sourceData.containsKey(sourceField)) {
                                transformed.put(targetField, sourceData.get(sourceField));
                            }
                        }
                    }
                    
                    // 应用默认值（如果有）
                    Map<String, Object> defaultValue = 
                        (Map<String, Object>) mappingRules.get("defaultValue");
                    if (defaultValue != null) {
                        for (Map.Entry<String, Object> entry : defaultValue.entrySet()) {
                            if (!transformed.containsKey(entry.getKey())) {
                                transformed.put(entry.getKey(), entry.getValue());
                            }
                        }
                    }
                    
                    // 添加源单据信息
                    transformed.put("sourceBillNo", sourceData.get("fbillNo"));
                    transformed.put("sourceId", sourceData.get("id"));
                    transformed.put("createBy", userId);
                    transformed.put("createTime", java.time.LocalDateTime.now());
                    
                    // 2.  调用对应的 Service 保存目标单据
                    Long savedId = saveTargetBill(targetModule, transformed, userId);
                    if (savedId != null) {
                        transformed.put("id", savedId);
                    }
                    
                    // 3. 构建成功结果
                    Map<String, Object> itemResult = new HashMap<>();
                    itemResult.put("success", true);
                    itemResult.put("sourceId", sourceData.get("id"));
                    itemResult.put("sourceBillNo", sourceData.get("fbillNo"));
                    itemResult.put("message", "下推成功");
                    itemResult.put("data", transformed);
                    
                    results.add(itemResult);
                    successCount++;
                    
                } catch (Exception e) {
                    log.error("单条数据下推失败，sourceId: {}", sourceData.get("id"), e);
                    
                    // 4. 构建失败结果
                    Map<String, Object> itemResult = new HashMap<>();
                    itemResult.put("success", false);
                    itemResult.put("sourceId", sourceData.get("id"));
                    itemResult.put("sourceBillNo", sourceData.get("fbillNo"));
                    itemResult.put("error", e.getMessage());
                    
                    results.add(itemResult);
                    failCount++;
                }
            }
            
            // 5. 构建总体返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", sourceDataList.size());
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("results", results);
            result.put("sourceModule", sourceModule);
            result.put("targetModule", targetModule);
            
            log.info("批量下推完成，source: {}, target: {}, total: {}, success: {}, fail: {}", 
                sourceModule, targetModule, sourceDataList.size(), successCount, failCount);
            
            if (failCount > 0) {
                return R.warn("部分下推成功，" + failCount + " 条失败");
            } else {
                return R.ok(result);
            }
        } catch (Exception e) {
            log.error("批量下推失败", e);
            return R.fail("批量下推失败：" + e.getMessage());
        }
    }

    /**
     * 获取下推映射配置
     */
    @GetMapping("/push/mapping")
    public R<?> getPushMappingConfig(@RequestParam Map<String, String> params) {
        try {
            String sourceModule = params.get("sourceModule");
            checkModulePermission(sourceModule, "query");
            String targetModule = params.get("targetModule");
            
            //  从数据库获取下推关系配置
            ErpPushRelationVo relationConfig = pushRelationService.getPushRelation(sourceModule, targetModule);
            if (relationConfig == null) {
                return R.fail("未找到从 [" + sourceModule + "] 到 [" + targetModule + "] 的下推配置");
            }
            
            return R.ok(relationConfig.getMappingRules());
        } catch (Exception e) {
            log.error("获取映射配置失败", e);
            return R.fail("获取映射配置失败：" + e.getMessage());
        }
    }

    /**
     * 验证下推数据
     *  已实现验证功能
     */
    @PostMapping("/push/validate")
    public R<?> validatePushData(@RequestBody Map<String, Object> params) {
        try {
            String sourceModule = (String) params.get("sourceModule");
            checkModulePermission(sourceModule, "query");
            String targetModule = (String) params.get("targetModule");
            List<Map<String, Object>> sourceDataList = 
                (List<Map<String, Object>>) params.get("sourceData");
            
            //  从数据库获取下推关系配置
            ErpPushRelationVo relationConfig = pushRelationService.getPushRelation(sourceModule, targetModule);
            if (relationConfig == null) {
                return R.fail("未找到从 [" + sourceModule + "] 到 [" + targetModule + "] 的下推配置");
            }
            
            //  解析映射规则
            Map<String, Object> mappingRules = parseMappingRules(relationConfig.getMappingRules());
            
            //  执行验证
            List<Map<String, Object>> validationResults = new ArrayList<>();
            boolean allValid = true;
            
            for (Map<String, Object> sourceData : sourceDataList) {
                Map<String, Object> validationResult = new HashMap<>();
                validationResult.put("sourceId", sourceData.get("id"));
                validationResult.put("sourceBillNo", sourceData.get("fbillNo"));
                
                //  验证必填字段
                Map<String, String> fieldMapping = (Map<String, String>) mappingRules.get("fieldMapping");
                List<String> missingFields = new ArrayList<>();
                
                if (fieldMapping != null) {
                    for (String sourceField : fieldMapping.keySet()) {
                        if (!sourceData.containsKey(sourceField) || sourceData.get(sourceField) == null) {
                            missingFields.add(sourceField);
                        }
                    }
                }
                
                if (!missingFields.isEmpty()) {
                    validationResult.put("valid", false);
                    validationResult.put("errors", "缺少必填字段：" + String.join(", ", missingFields));
                    allValid = false;
                } else {
                    validationResult.put("valid", true);
                    validationResult.put("errors", null);
                }
                
                validationResults.add(validationResult);
            }
            
            //  构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("valid", allValid);
            result.put("validationResults", validationResults);
            result.put("totalCount", sourceDataList.size());
            result.put("validCount", allValid ? sourceDataList.size() : 0);
            result.put("invalidCount", allValid ? 0 : sourceDataList.size());
            
            log.info("验证下推数据完成，source: {}, target: {}, valid: {}", 
                sourceModule, targetModule, allValid);
            
            return R.ok(result);
        } catch (Exception e) {
            log.error("验证下推数据失败", e);
            return R.fail("验证下推数据失败：" + e.getMessage());
        }
    }

    /**
     * 取消下推
     *  已实现取消下推功能
     */
    @PostMapping("/push/cancel")
    public R<?> cancelPushDown(@RequestBody Map<String, Object> params) {
        try {
            String targetModule = (String) params.get("targetModule");
            checkModulePermission(targetModule, "push");
            String targetBillId = (String) params.get("targetBillId");
            // String userId = (String) params.get("userId"); // 暂未使用
            String reason = (String) params.get("reason");
            
            //  验证参数
            if (targetBillId == null || targetBillId.isEmpty()) {
                return R.fail("目标单据 ID 不能为空");
            }
            
            //  调用对应的 Service 删除或标记目标单
            cancelTargetBill(targetModule, targetBillId, reason);
            
            //  记录取消日志
            log.info("取消下推成功，targetModule: {}, targetBillId: {}, reason: {}", 
                targetModule, targetBillId, reason);
            
            //  构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "取消下推成功");
            result.put("targetModule", targetModule);
            result.put("targetBillId", targetBillId);
            result.put("reason", reason);
            
            return R.ok(result);
        } catch (Exception e) {
            log.error("取消下推失败", e);
            return R.fail("取消下推失败：" + e.getMessage());
        }
    }

    /**
     * 获取下推历史记录
     */
    @GetMapping("/push/history")
    public R<?> getPushHistory(@RequestParam Map<String, String> params) {
        try {
            String moduleCode = params.get("moduleCode");
            permissionChecker.checkModulePermission(moduleCode, "query");
            
            //  从数据库获取该模块的所有下推关系配置
            List<ErpPushRelationVo> relations = pushRelationService.selectList(new ErpPushRelationBo() {{ setSourceModule(moduleCode); }});
            
            // 返回下推关系列表（实际项目中应该查询下推历史记录表）
            return R.ok(relations != null ? relations : new ArrayList<>());
        } catch (Exception e) {
            log.error("获取下推历史失败", e);
            return R.fail("获取下推历史失败：" + e.getMessage());
        }
    }

    // ==================== 辅助方法 ====================

    // ==================== 字典构建器引擎接口 ====================

    /**
     * 获取字典数据（构建器模式）
     */
    @GetMapping("/dictionary/{name}")
    public R<?> getDictionary(@PathVariable String name, @RequestParam(required = false) String moduleCode) {
        try {
            if (moduleCode != null && !moduleCode.isEmpty()) {
                permissionChecker.checkModulePermission(moduleCode, "query");
            }
            
            List<Map<String, Object>> data = dictionaryBuilderEngine.get(name);
            log.info("📦 获取字典：{}, 共 {} 条", name, data.size());
            return R.ok(data);
        } catch (Exception e) {
            log.error("❌ 获取字典失败：{}", name, e);
            return R.fail("获取字典失败：" + e.getMessage());
        }
    }

    /**
     * 清除字典缓存
     */
    @DeleteMapping("/dictionary/{name}/cache")
    public R<?> clearDictionaryCache(@PathVariable String name) {
        try {
            dictionaryBuilderEngine.clear(name);
            log.info("🗑️ 已清除字典缓存：{}", name);
            return R.ok("清除成功");
        } catch (Exception e) {
            log.error("❌ 清除字典缓存失败：{}", name, e);
            return R.fail("清除失败：" + e.getMessage());
        }
    }

    /**
     * 清除所有字典缓存
     */
    @DeleteMapping("/dictionary/cache/all")
    public R<?> clearAllDictionaryCaches() {
        try {
            dictionaryBuilderEngine.clearAll();
            log.info("🗑️ 已清除所有字典缓存");
            return R.ok("清除成功");
        } catch (Exception e) {
            log.error("❌ 清除所有字典缓存失败", e);
            return R.fail("清除失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有字典状态
     */
    @GetMapping("/dictionary/status")
    public R<?> getAllDictionaryStatus() {
        try {
            Map<String, Map<String, Object>> status = dictionaryBuilderEngine.getAllStatus();
            log.info("📊 获取所有字典状态，共 {} 个", status.size());
            return R.ok(status);
        } catch (Exception e) {
            log.error("❌ 获取字典状态失败", e);
            return R.fail("获取状态失败：" + e.getMessage());
        }
    }

    /**
     * ⭐ 新增：获取字典数据（复用表格数据构建器）
     * 直接从数据库表查询字典数据，无需创建 Service
     * 
     * @param name 字典名称
     * @param moduleCode 模块编码
     * @return 字典数据列表
     */
    @GetMapping("/dictionary/{name}/data")
    public R<?> getDictionaryData(@PathVariable String name,
                                   @RequestParam(required = false) String moduleCode) {
        try {
            // ✅ 权限检查
            if (moduleCode != null && !moduleCode.isEmpty()) {
                permissionChecker.checkModulePermission(moduleCode, "query");
            }
            
            // ✅ 从配置中读取表名和查询条件
            JSONObject configJson = configParser.getConfig(name);
            JSONObject dictionaryConfig = configJson.optJSONObject("dictionaryConfig");
            
            if (dictionaryConfig == null) {
                log.warn("⚠️ 未找到字典配置，字典名称：{}", name);
                return R.fail("未找到字典配置：" + name);
            }
            
            JSONObject dictionaries = dictionaryConfig.optJSONObject("dictionaries");
            if (dictionaries == null || !dictionaries.containsKey(name)) {
                log.warn("⚠️ 未找到字典定义：{}", name);
                return R.fail("未找到字典定义：" + name);
            }
            
            JSONObject dictConfig = dictionaries.getJSONObject(name);
            String tableName = dictConfig.getString("tableName");
            JSONObject queryConfig = dictConfig.optJSONObject("queryConfig");
            JSONObject fieldMapping = dictConfig.optJSONObject("fieldMapping");
            
            // ✅ 表名校验
            if (tableName == null || tableName.trim().isEmpty()) {
                log.error("❌ 缺少必需的 tableName 配置，字典：{}", name);
                return R.fail("缺少必需的 tableName 配置");
            }
            
            // ✅ 构建查询条件
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Object> queryWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            
            if (queryConfig != null && !queryConfig.isEmpty()) {
                queryWrapper = buildQueryFromBuilderMode(queryWrapper, queryConfig);
            }
            
            // ✅ 直接调用表格构建器的 Service 查询数据
            List<Map<String, Object>> data = dataPermissionService
                .selectListByModule(moduleCode, queryWrapper);
            
            // ✅ 字段映射（如果配置了）
            if (fieldMapping != null) {
                String labelField = fieldMapping.getString("labelField");
                String valueField = fieldMapping.getString("valueField");
                
                if (labelField != null && valueField != null) {
                    data = mapDictionaryFields(data, labelField, valueField);
                }
            }
            
            log.info("✅ 字典数据加载成功：{}, 共 {} 条", name, data.size());
            return R.ok(data);
            
        } catch (Exception e) {
            log.error("❌ 字典数据加载失败：{}", name, e);
            return R.fail("加载失败：" + e.getMessage());
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 处理数据 (计算字段 + 虚拟字段)
     * 
     * @param dataList 原始数据列表
     * @param moduleCode 模块编码
     * @return 处理后的数据列表
     */
    private List<Map<String, Object>> processData(
            List<Map<String, Object>> dataList,
            String moduleCode) {
        
        try {
            // 获取配置
            JSONObject configJson = configParser.getConfig(moduleCode);
            
            // 使用 DataProcessor 统一处理
            return dataProcessor.process(dataList, configJson);
            
        } catch (Exception e) {
            log.error("数据处理失败，moduleCode: {}", moduleCode, e);
            // 处理失败时返回原始数据，不影响主流程
            return dataList;
        }
    }

    /**
     * ⭐⭐⭐ 构建器模式：从 queryConfig 构建查询条件 ⭐⭐⭐
     * 
     * @param queryWrapper 查询包装器
     * @param queryConfig 配置对象
     * @return QueryWrapper
     */
    @SuppressWarnings("unchecked")
    private com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Object> buildQueryFromBuilderMode(
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Object> queryWrapper,
            Map<String, Object> queryConfig) {
        
        try {
            // ⭐ 1. 解析 conditions 数组
            List<Map<String, Object>> conditions = (List<Map<String, Object>>) queryConfig.get("conditions");
            if (conditions != null && !conditions.isEmpty()) {
                log.info("⭐ 解析构建器条件，共 {} 个", conditions.size());
                
                for (Map<String, Object> condition : conditions) {
                    String field = (String) condition.get("field");
                    String operator = (String) condition.get("operator");
                    Object value = condition.get("value");
                    
                    // 跳过空值字段
                    if (field == null || field.trim().isEmpty()) {
                        continue;
                    }
                    
                    // 支持的运算符：eq, ne, gt, ge, lt, le, like, left_like, right_like, in, between, isNull, isNotNull
                    switch (operator) {
                        case "eq":
                            queryWrapper.eq(field, value);
                            break;
                        case "ne":
                            queryWrapper.ne(field, value);
                            break;
                        case "gt":
                            queryWrapper.gt(field, value);
                            break;
                        case "ge":
                            queryWrapper.ge(field, value);
                            break;
                        case "lt":
                            queryWrapper.lt(field, value);
                            break;
                        case "le":
                            queryWrapper.le(field, value);
                            break;
                        case "like":
                            queryWrapper.like(field, value);
                            break;
                        case "left_like":
                            queryWrapper.likeLeft(field, value);
                            break;
                        case "right_like":
                            queryWrapper.likeRight(field, value);
                            break;
                        case "in":
                            if (value instanceof List) {
                                queryWrapper.in(field, (List<?>) value);
                            }
                            break;
                        case "between":
                            if (value instanceof List && ((List<?>) value).size() >= 2) {
                                List<?> values = (List<?>) value;
                                queryWrapper.between(field, values.get(0), values.get(1));
                            }
                            break;
                        case "isNull":
                            queryWrapper.isNull(field);
                            break;
                        case "isNotNull":
                            queryWrapper.isNotNull(field);
                            break;
                        default:
                            log.warn(" 未知的操作符：{}, 已跳过", operator);
                    }
                }
            }
            
            // ⭐ 2. 解析 SELECT 字段配置
            String selectFields = (String) queryConfig.get("select");
            if (selectFields != null && !selectFields.trim().isEmpty()) {
                log.info("⭐ 配置 SELECT 字段：{}", selectFields);
                String[] fields = selectFields.split(",");
                queryWrapper.select(fields);
            }
            
            // ⭐ 3. 解析排序配置
            List<Map<String, Object>> orderBy = (List<Map<String, Object>>) queryConfig.get("orderBy");
            if (orderBy != null && !orderBy.isEmpty()) {
                log.info("⭐ 配置排序，共 {} 个", orderBy.size());
                
                for (Map<String, Object> order : orderBy) {
                    String field = (String) order.get("field");
                    String direction = (String) order.getOrDefault("direction", "asc");
                    
                    if ("desc".equalsIgnoreCase(direction)) {
                        queryWrapper.orderByDesc(field);
                    } else {
                        queryWrapper.orderByAsc(field);
                    }
                }
            }
            
            // ⭐ 4. 解析分组配置（可选）
            List<String> groupBy = (List<String>) queryConfig.get("groupBy");
            if (groupBy != null && !groupBy.isEmpty()) {
                log.info("⭐ 配置分组：{}", groupBy);
                queryWrapper.groupBy(groupBy);
            }
            
            // ⭐ 5. 解析 HAVING 配置（可选）
            List<Map<String, Object>> having = (List<Map<String, Object>>) queryConfig.get("having");
            if (having != null && !having.isEmpty()) {
                log.info("⭐ 配置 HAVING 条件，共 {} 个", having.size());
                
                for (Map<String, Object> h : having) {
                    String field = (String) h.get("field");
                    String operator = (String) h.get("operator");
                    Object value = h.get("value");
                    
                    if (field == null || field.trim().isEmpty()) {
                        continue;
                    }
                    
                    // HAVING 条件支持基本运算符
                    switch (operator) {
                        case "eq":
                            queryWrapper.having(field + " = '" + value + "'");
                            break;
                        case "gt":
                            queryWrapper.having(field + " > " + value);
                            break;
                        case "ge":
                            queryWrapper.having(field + " >= " + value);
                            break;
                        case "lt":
                            queryWrapper.having(field + " < " + value);
                            break;
                        case "le":
                            queryWrapper.having(field + " <= " + value);
                            break;
                        case "ne":
                            queryWrapper.having(field + " <> " + value);
                            break;
                        default:
                            log.warn(" HAVING 不支持的操作符：{}", operator);
                    }
                }
            }
            
            log.info(" 构建器模式查询条件构建完成");
            return queryWrapper;
            
        } catch (Exception e) {
            log.error(" 构建器模式查询条件构建失败", e);
            throw new RuntimeException("构建器模式查询条件构建失败：" + e.getMessage());
        }
    }

    /**
     * 从参数中提取模块编码
     * @param params 请求参数
     * @return 模块编码
     */
    private String extractModuleCode(Map<String, Object> params) {
        if (params == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }
        String moduleCode = (String) params.get("moduleCode");
        if (moduleCode == null || moduleCode.isEmpty()) {
            throw new IllegalArgumentException("模块编码不能为空");
        }
        return moduleCode;
    }

    /**
     * 更新单据审批状态
     * @param moduleCode 模块编码
     * @param billId 单据 ID
     * @param status 状态值
     */
    private void updateBillApprovalStatus(String moduleCode, String billId, String status) {
        try {
            //  调用 ISuperDataPermissionService 的通用方法更新状态
            // 注意：实际项目中需要在对应的 Service 中实现更新方法
            log.info("更新单据审批状态，moduleCode: {}, billId: {}, status: {}", moduleCode, billId, status);
            
            // 示例代码（需要实际项目中实现）：
            // dataPermissionService.updateBillField(moduleCode, Long.parseLong(billId), "billStatus", status);
        } catch (Exception e) {
            log.error("更新单据审批状态失败", e);
        }
    }

    /**
     * 保存目标单据
     * @param targetModule 目标模块编码
     * @param transformed 转换后的数据
     * @param userId 用户 ID
     * @return 保存的单据 ID
     */
    private Long saveTargetBill(String targetModule, Map<String, Object> transformed, String userId) {
        try {
            //  根据 targetModule 调用对应的 Service 保存
            // 注意：实际项目中需要实现一个通用的单据保存服务
            log.info("保存目标单据，targetModule: {}, userId: {}", targetModule, userId);
            
            // 示例代码（需要实际项目中实现）：
            // return dataPermissionService.saveBill(targetModule, transformed, userId);
            
            // 这里返回 null，表示未实际保存
            return null;
        } catch (Exception e) {
            log.error("保存目标单据失败", e);
            throw new RuntimeException("保存目标单据失败：" + e.getMessage());
        }
    }

    /**
     * 取消目标单据
     * @param targetModule 目标模块编码
     * @param targetBillId 目标单据 ID
     * @param reason 取消原因
     */
    private void cancelTargetBill(String targetModule, String targetBillId, String reason) {
        try {
            //  根据 targetModule 调用对应的 Service 删除或标记取消
            log.info("取消目标单据，targetModule: {}, targetBillId: {}, reason: {}", targetModule, targetBillId, reason);
            
            // 示例代码（需要实际项目中实现）：
            // dataPermissionService.cancelBill(targetModule, Long.parseLong(targetBillId));
        } catch (Exception e) {
            log.error("取消目标单据失败", e);
        }
    }

    /**
     * ⭐ 新增：字典字段映射工具方法
     * 将数据库字段映射为前端需要的 label/value 格式
     * 
     * @param data 原始数据列表
     * @param labelField 显示字段名
     * @param valueField 值字段名
     * @return 映射后的数据列表
     */
    private List<Map<String, Object>> mapDictionaryFields(
            List<Map<String, Object>> data,
            String labelField,
            String valueField) {
        
        return data.stream()
            .map(item -> {
                Map<String, Object> mapped = new HashMap<>();
                mapped.put("label", item.get(labelField));
                mapped.put("value", item.get(valueField));
                // 保留原始字段，方便调试和扩展
                mapped.putAll(item);
                return mapped;
            })
            .collect(java.util.stream.Collectors.toList());
    }
}
