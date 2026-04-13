package com.ruoyi.erp.controller.base;

import cn.dev33.satoken.exception.NotPermissionException;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import com.ruoyi.erp.domain.response.ErpResponse;
import com.ruoyi.erp.exception.ErpConfigException;
import com.ruoyi.erp.service.ISuperDataPermissionService;
import com.ruoyi.erp.utils.ConfigParser;
import com.ruoyi.erp.utils.DataProcessor;
import com.ruoyi.erp.utils.ErpPermissionChecker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired; 
import java.util.Map;

/**
 * ERP Controller
 * 
 * @author JMH
 * @date 2026-03-24
 */
@Slf4j
public abstract class BaseErpEngineController {
    
    @Autowired
    protected ISuperDataPermissionService superDataPermissionService;
    
    @Autowired
    protected ErpPermissionChecker permissionChecker;
    
    @Autowired
    protected ConfigParser configParser;
    
    @Autowired
    protected DataProcessor dataProcessor;
    
    /**
     * Get current module code
     * 
     * @return Module code
     */
    protected abstract String getModuleCode();
    
    /**
     * Query module data (template method)
     * 
     * @param pageQuery Pagination query parameters
     * @return Paginated results
     */
    protected ErpResponse<TableDataInfo<Map<String, Object>>> queryModuleData(PageQuery pageQuery) {
        return queryModuleData(pageQuery, new java.util.HashMap<>());
    }
    
    /**
     * Query module data (with conditions and parameters)
     * 
     * @param pageQuery Pagination parameters
     * @param queryConfig Query configuration (including conditions, orderBy, etc.)
     * @return Paginated results
     */
    protected ErpResponse<TableDataInfo<Map<String, Object>>> queryModuleData(
            PageQuery pageQuery, 
            Map<String, Object> queryConfig) {
        
        String moduleCode = getModuleCode();
        
        try {
            // 1. Permission check
            permissionChecker.checkModulePermission(moduleCode, "query");
            
            // 2. Get table name from configuration
            var moduleConfig = configParser.getConfig(moduleCode);
            String tableName = null;
            if (moduleConfig != null && moduleConfig.containsKey("pageConfig")) {
                JSONObject pageConfig = moduleConfig.getJSONObject("pageConfig");
                tableName = pageConfig.getString("tableName");
            }
            
            if (tableName == null || tableName.trim().isEmpty()) {
                log.error("Table name configuration not found for module [{}]", moduleCode);
                throw new ServiceException("Table name configuration not found for module [" + moduleCode + "]");
            }
            
            // 3. Query data (using the new selectPageByModuleWithTableName method)
            TableDataInfo<Map<String, Object>> tableDataInfo = superDataPermissionService
                .selectPageByModuleWithTableName(moduleCode, tableName, pageQuery, queryConfig);
            
            // 4. Process data (calculated fields, virtual fields, etc.)
            if (tableDataInfo != null && tableDataInfo.getRows() != null && !tableDataInfo.getRows().isEmpty()) {
                tableDataInfo.setRows(dataProcessor.process(tableDataInfo.getRows(), moduleConfig));
            }
            
            return ErpResponse.ok(tableDataInfo);
            
        } catch (Exception e) {
            log.error("Failed to query data: {}", e.getMessage(), e);
            return ErpResponse.fail("Query failed: " + e.getMessage());
        }
    }
    
    /**
     * Add module data (template method)
     * 
     * @param data Data object
     * @return Operation result
     */
    protected ErpResponse<?> addModuleData(Map<String, Object> data) {
        String moduleCode = getModuleCode();
        
        try {
            // 1. Permission check
            permissionChecker.checkModulePermission(moduleCode, "add");
            
            // 2. Data validation
            validateData(data, moduleCode);
            
            // 3. Get table name from configuration
            var moduleConfig = configParser.getConfig(moduleCode);
            String tableName = null;
            if (moduleConfig != null && moduleConfig.containsKey("pageConfig")) {
                JSONObject pageConfig = moduleConfig.getJSONObject("pageConfig");
                tableName = pageConfig.getString("tableName");
            }
            
            if (tableName == null || tableName.trim().isEmpty()) {
                log.error("Table name configuration not found for module [{}]", moduleCode);
                throw new ServiceException("Table name configuration not found for module [" + moduleCode + "]");
            }
            
            // 4. Insert data
            int result = ((com.ruoyi.erp.service.impl.SuperDataPermissionServiceImpl) superDataPermissionService)
                .insertByModuleWithTableName(moduleCode, tableName, data);
            
            log.info("Data inserted successfully, moduleCode: {}, rows: {}", moduleCode, result);
            return ErpResponse.ok();
            
        } catch (Exception e) {
            log.error("Failed to add data: {}", e.getMessage(), e);
            return ErpResponse.fail("Add failed: " + e.getMessage());
        }
    }
    
    /**
     * Update module data (template method)
     * 
     * @param data Data object
     * @return Operation result
     */
    protected ErpResponse<?> updateModuleData(Map<String, Object> data) {
        String moduleCode = getModuleCode();
        
        try {
            // 1. Permission check
            permissionChecker.checkModulePermission(moduleCode, "edit");
            
            // 2. Data validation
            validateData(data, moduleCode);
            
            // 3. Get table name from configuration
            var moduleConfig = configParser.getConfig(moduleCode);
            String tableName = null;
            if (moduleConfig != null && moduleConfig.containsKey("pageConfig")) {
                JSONObject pageConfig = moduleConfig.getJSONObject("pageConfig");
                tableName = pageConfig.getString("tableName");
            }
            
            if (tableName == null || tableName.trim().isEmpty()) {
                log.error("Table name configuration not found for module [{}]", moduleCode);
                throw new ServiceException("Table name configuration not found for module [" + moduleCode + "]");
            }
            
            // 4. Update data (using the new updateByModuleWithTableName method)
            int result = ((com.ruoyi.erp.service.impl.SuperDataPermissionServiceImpl) superDataPermissionService)
                .updateByModuleWithTableName(moduleCode, tableName, data);
            
            log.info("Data updated successfully, moduleCode: {}, rows: {}", moduleCode, result);
            return ErpResponse.ok();
            
        } catch (Exception e) {
            log.error("Failed to update data: {}", e.getMessage(), e);
            return ErpResponse.fail("Update failed: " + e.getMessage());
        }
    }
    
    /**
     * Delete module data (template method)
     * 
     * @param ids ID array
     * @return Operation result
     */
    protected ErpResponse<?> deleteModuleData(Object[] ids) {
        String moduleCode = getModuleCode();
        
        try {
            // 1. Permission check
            permissionChecker.checkModulePermission(moduleCode, "delete");
            
            // 2. Get table name from configuration
            var moduleConfig = configParser.getConfig(moduleCode);
            String tableName = null;
            if (moduleConfig != null && moduleConfig.containsKey("pageConfig")) {
                JSONObject pageConfig = moduleConfig.getJSONObject("pageConfig");
                tableName = pageConfig.getString("tableName");
            }
            
            if (tableName == null || tableName.trim().isEmpty()) {
                log.error("Table name configuration not found for module [{}]", moduleCode);
                throw new ServiceException("Table name configuration not found for module [" + moduleCode + "]");
            }
            
            // 3. Delete data (using the new deleteByModuleWithTableName method)
            int result = ((com.ruoyi.erp.service.impl.SuperDataPermissionServiceImpl) superDataPermissionService)
                .deleteByModuleWithTableName(moduleCode, tableName, ids);
            
            log.info("Data deleted successfully, moduleCode: {}, count: {}", moduleCode, result);
            return ErpResponse.ok();
            
        } catch (Exception e) {
            log.error("Failed to delete data: {}", e.getMessage(), e);
            return ErpResponse.fail("Delete failed: " + e.getMessage());
        }
    }
    
    /**
     * Audit module data (template method)
     * 
     * @param ids ID array
     * @param auditResult Audit result (PASS/REJECT)
     * @param auditRemark Audit remark
     * @return Operation result
     */
    protected ErpResponse<?> auditModuleData(Object[] ids, String auditResult, String auditRemark) {
        String moduleCode = getModuleCode();
        
        try {
            // 1. Permission check
            permissionChecker.checkModulePermission(moduleCode, "audit");
            
            // 2. Call audit service
            // TODO: Implement audit logic
            
            return ErpResponse.ok();
            
        } catch (Exception e) {
            log.error("Failed to audit data: {}", e.getMessage(), e);
            return ErpResponse.fail("Audit failed: " + e.getMessage());
        }
    }
    
    /**
     * Data validation (can be overridden by subclasses)
     * 
     * @param data Data object
     * @param moduleCode Module code
     */
    protected void validateData(Map<String, Object> data, String moduleCode) {
        // Default validation logic
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data cannot be empty");
        }
    }
    
    /**
     * Exception handling
     * 
     * @param message Error message
     * @param e Exception object
     * @return Unified response
     */
    protected ErpResponse<?> handleException(String message, Exception e) {
        log.error("{}: {}", message, e.getMessage(), e);
        
        if (e instanceof NotPermissionException) {
            return ErpResponse.fail("Insufficient permissions");
        } else if (e instanceof ErpConfigException) {
            return ErpResponse.fail(e.getMessage());
        } else if (e instanceof IllegalArgumentException) {
            return ErpResponse.fail(e.getMessage());
        } else {
            return ErpResponse.fail(message + ": " + e.getMessage());
        }
    }
}
