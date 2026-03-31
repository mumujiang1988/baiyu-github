package com.ruoyi.erp.utils;

import cn.dev33.satoken.stp.StpUtil;
import com.ruoyi.erp.domain.entity.ErpPageConfig;
import com.ruoyi.erp.exception.ErpConfigException;
import com.ruoyi.erp.service.ErpPageConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ERP Permission Checker
 * 
 * Provides unified permission checking functionality:
 * - Module permission check
 * - Data permission check
 * - Batch permission check
 * 
 * @author JMH
 * @date 2026-03-24
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ErpPermissionChecker {
    
    private final ErpPageConfigService erpPageConfigService;
    
    /**
     * Check module permission
     * 
     * @param moduleCode Module code
     * @param operation Operation type (query/add/edit/delete/audit/push)
     * @throws ErpConfigException When config not found, disabled or no permission
     */
    public void checkModulePermission(String moduleCode, String operation) {
        // 1. Check module config
        ErpPageConfig config = erpPageConfigService.getByModuleCode(moduleCode);
        if (config == null) {
            throw new ErpConfigException(moduleCode, "CONFIG_NOT_FOUND", "Module config not found");
        }
        
        // 2. Check module status
        if (!"1".equals(config.getStatus())) {
            throw new ErpConfigException(moduleCode, "MODULE_DISABLED", "Module has been disabled");
        }
        
        // 3. Check operation permission
        String permission = buildPermission(moduleCode, operation);
        StpUtil.checkPermission(permission);
    }
    
    /**
     * Check data permission
     * 
     * @param moduleCode Module code
     * @param dataId Data ID
     * @param operation Operation type
     */
    public void checkDataPermission(String moduleCode, Object dataId, String operation) {
        // First check if has global permission
        String globalPermission = buildPermission(moduleCode, "data:" + operation);
        if (StpUtil.hasPermission(globalPermission)) {
            return;
        }
        
        // TODO: Implement data ownership check
        // If no global permission, check data ownership permission
    }
    
    /**
     * Build permission string
     * 
     * @param moduleCode Module code
     * @param operation Operation type
     * @return Permission identifier
     */
    private String buildPermission(String moduleCode, String operation) {
        return String.format("k3:%s:%s", moduleCode, operation);
    }
    
    /**
     * Batch check permissions
     * 
     * @param moduleCode Module code
     * @param operations Operation types
     */
    public void checkPermissions(String moduleCode, String... operations) {
        for (String operation : operations) {
            checkModulePermission(moduleCode, operation);
        }
    }
    
    /**
     * Check if module exists (no permission check)
     * 
     * @param moduleCode Module code
     * @return Module config entity
     */
    public ErpPageConfig checkModuleExists(String moduleCode) {
        ErpPageConfig config = erpPageConfigService.getByModuleCode(moduleCode);
        if (config == null) {
            throw new ErpConfigException(moduleCode, "CONFIG_NOT_FOUND", "Module config not found");
        }
        return config;
    }
}
