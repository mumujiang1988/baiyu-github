package com.ruoyi.erp.utils;

import cn.dev33.satoken.stp.StpUtil;
import com.ruoyi.erp.domain.entity.ErpPageConfig;
import com.ruoyi.erp.exception.ErpConfigException;
import com.ruoyi.erp.service.ErpPageConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ERP 权限检查器
 * 
 * 提供统一的权限检查功能:
 * - 模块权限检查
 * - 数据权限检查
 * - 批量权限检查
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
     * 检查模块权限
     * 
     * @param moduleCode 模块编码
     * @param operation 操作类型 (query/add/edit/delete/audit/push)
     * @throws ErpConfigException 当配置不存在、已禁用或无权限时
     */
    public void checkModulePermission(String moduleCode, String operation) {
        // 1. 检查模块配置
        ErpPageConfig config = erpPageConfigService.getByModuleCode(moduleCode);
        if (config == null) {
            throw new ErpConfigException(moduleCode, "CONFIG_NOT_FOUND", "模块配置不存在");
        }
        
        // 2. 检查模块状态
        if (!"1".equals(config.getStatus())) {
            throw new ErpConfigException(moduleCode, "MODULE_DISABLED", "模块已禁用");
        }
        
        // 3. 检查操作权限
        String permission = buildPermission(moduleCode, operation);
        StpUtil.checkPermission(permission);
        
        log.debug("权限检查通过：moduleCode={}, operation={}, permission={}", 
            moduleCode, operation, permission);
    }
    
    /**
     * 检查数据权限
     * 
     * @param moduleCode 模块编码
     * @param dataId 数据 ID
     * @param operation 操作类型
     */
    public void checkDataPermission(String moduleCode, Object dataId, String operation) {
        // 先检查是否有全局权限
        String globalPermission = buildPermission(moduleCode, "data:" + operation);
        if (StpUtil.hasPermission(globalPermission)) {
            return;
        }
        
        // TODO: 实现数据归属检查
        // 如果没有全局权限，检查数据归属权限
        log.warn("数据权限检查暂未实现完整逻辑：moduleCode={}, dataId={}, operation={}", 
            moduleCode, dataId, operation);
    }
    
    /**
     * 构建权限字符串
     * 
     * @param moduleCode 模块编码
     * @param operation 操作类型
     * @return 权限标识符
     */
    private String buildPermission(String moduleCode, String operation) {
        return String.format("k3:%s:%s", moduleCode, operation);
    }
    
    /**
     * 批量检查权限
     * 
     * @param moduleCode 模块编码
     * @param operations 操作类型数组
     */
    public void checkPermissions(String moduleCode, String... operations) {
        for (String operation : operations) {
            checkModulePermission(moduleCode, operation);
        }
    }
    
    /**
     * 检查模块是否存在 (不检查权限)
     * 
     * @param moduleCode 模块编码
     * @return 模块配置实体
     */
    public ErpPageConfig checkModuleExists(String moduleCode) {
        ErpPageConfig config = erpPageConfigService.getByModuleCode(moduleCode);
        if (config == null) {
            throw new ErpConfigException(moduleCode, "CONFIG_NOT_FOUND", "模块配置不存在");
        }
        return config;
    }
}
