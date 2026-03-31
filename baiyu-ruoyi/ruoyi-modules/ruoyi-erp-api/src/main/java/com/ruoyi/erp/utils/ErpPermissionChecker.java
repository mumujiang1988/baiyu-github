package com.ruoyi.erp.utils;

import cn.dev33.satoken.stp.StpUtil;
import com.ruoyi.erp.domain.entity.ErpPageConfig;
import com.ruoyi.erp.exception.ErpConfigException;
import com.ruoyi.erp.service.ErpPageConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author JMH
 * @date 2026-03-24
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ErpPermissionChecker {
    
    private final ErpPageConfigService erpPageConfigService;
    
    public void checkModulePermission(String moduleCode, String operation) {
        ErpPageConfig config = erpPageConfigService.getByModuleCode(moduleCode);
        if (config == null) {
            throw new ErpConfigException(moduleCode, "CONFIG_NOT_FOUND", "Module config not found");
        }
        
        if (!"1".equals(config.getStatus())) {
            throw new ErpConfigException(moduleCode, "MODULE_DISABLED", "Module has been disabled");
        }
        
        String permission = buildPermission(moduleCode, operation);
        StpUtil.checkPermission(permission);
    }
    
    public void checkDataPermission(String moduleCode, Object dataId, String operation) {
        String globalPermission = buildPermission(moduleCode, "data:" + operation);
        if (StpUtil.hasPermission(globalPermission)) {
            return;
        }
    }
    
    private String buildPermission(String moduleCode, String operation) {
        return String.format("k3:%s:%s", moduleCode, operation);
    }
    
    public void checkPermissions(String moduleCode, String... operations) {
        for (String operation : operations) {
            checkModulePermission(moduleCode, operation);
        }
    }
    
    public ErpPageConfig checkModuleExists(String moduleCode) {
        ErpPageConfig config = erpPageConfigService.getByModuleCode(moduleCode);
        if (config == null) {
            throw new ErpConfigException(moduleCode, "CONFIG_NOT_FOUND", "Module config not found");
        }
        return config;
    }
}
