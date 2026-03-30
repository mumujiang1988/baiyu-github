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

import java.util.List;
import java.util.Map;

/**
 * ERP 引擎公共基类 Controller
 * 
 * 提供统一的:
 * - 权限检查
 * - 数据查询
 * - 配置解析
 * - 异常处理
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
     * 获取当前模块编码
     * 
     * @return 模块编码
     */
    protected abstract String getModuleCode();
    
    /**
     * 查询模块数据 (模板方法)
     * 
     * @param pageQuery 分页查询参数
     * @return 分页结果
     */
    protected ErpResponse<TableDataInfo<Map<String, Object>>> queryModuleData(PageQuery pageQuery) {
        return queryModuleData(pageQuery, new java.util.HashMap<>());
    }
    
    /**
     * 查询模块数据 (带条件和参数)
     * 
     * @param pageQuery 分页参数
     * @param queryConfig 查询配置（包含 conditions, orderBy 等）
     * @return 分页结果
     */
    protected ErpResponse<TableDataInfo<Map<String, Object>>> queryModuleData(
            PageQuery pageQuery, 
            Map<String, Object> queryConfig) {
        
        String moduleCode = getModuleCode();
        
        try {
            // 1. 权限检查
            permissionChecker.checkModulePermission(moduleCode, "query");
            
            // 2. 从配置中获取表名
            var moduleConfig = configParser.getConfig(moduleCode);
            String tableName = null;
            if (moduleConfig != null && moduleConfig.containsKey("pageConfig")) {
                JSONObject pageConfig = moduleConfig.getJSONObject("pageConfig");
                tableName = pageConfig.getString("tableName");
            }
            
            if (tableName == null || tableName.trim().isEmpty()) {
                log.error("未找到模块 [{}] 对应的表名配置", moduleCode);
                throw new ServiceException("未找到模块 [" + moduleCode + "] 对应的表名配置");
            }
            
            // 3. 查询数据（使用新的 selectPageByModuleWithTableName 方法）
            TableDataInfo<Map<String, Object>> tableDataInfo = superDataPermissionService
                .selectPageByModuleWithTableName(moduleCode, tableName, pageQuery, queryConfig);
            
            // 4. 处理数据 (计算字段、虚拟字段等)
            if (tableDataInfo != null && tableDataInfo.getRows() != null && !tableDataInfo.getRows().isEmpty()) {
                tableDataInfo.setRows(dataProcessor.process(tableDataInfo.getRows(), moduleConfig));
            }
            
            return ErpResponse.ok(tableDataInfo);
            
        } catch (Exception e) {
            log.error("查询数据失败：{}", e.getMessage(), e);
            return ErpResponse.fail("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 新增模块数据 (模板方法)
     * 
     * @param data 数据对象
     * @return 操作结果
     */
    protected ErpResponse<?> addModuleData(Map<String, Object> data) {
        String moduleCode = getModuleCode();
        
        try {
            // 1. 权限检查
            permissionChecker.checkModulePermission(moduleCode, "add");
            
            // 2. 数据验证
            validateData(data, moduleCode);
            
            // 3. 从配置中获取表名
            var moduleConfig = configParser.getConfig(moduleCode);
            String tableName = null;
            if (moduleConfig != null && moduleConfig.containsKey("pageConfig")) {
                JSONObject pageConfig = moduleConfig.getJSONObject("pageConfig");
                tableName = pageConfig.getString("tableName");
            }
            
            if (tableName == null || tableName.trim().isEmpty()) {
                log.error("未找到模块 [{}] 对应的表名配置", moduleCode);
                throw new ServiceException("未找到模块 [" + moduleCode + "] 对应的表名配置");
            }
            
            // 4. 新增数据（使用新的 insertByModuleWithTableName 方法）
            int result = ((com.ruoyi.erp.service.impl.SuperDataPermissionServiceImpl) superDataPermissionService)
                .insertByModuleWithTableName(moduleCode, tableName, data);
            
            log.info("新增数据成功，moduleCode: {}, rows: {}", moduleCode, result);
            return ErpResponse.ok();
            
        } catch (Exception e) {
            log.error("新增数据失败：{}", e.getMessage(), e);
            return ErpResponse.fail("新增失败：" + e.getMessage());
        }
    }
    
    /**
     * 修改模块数据 (模板方法)
     * 
     * @param data 数据对象
     * @return 操作结果
     */
    protected ErpResponse<?> updateModuleData(Map<String, Object> data) {
        String moduleCode = getModuleCode();
        
        try {
            // 1. 权限检查
            permissionChecker.checkModulePermission(moduleCode, "edit");
            
            // 2. 数据验证
            validateData(data, moduleCode);
            
            // 3. 从配置中获取表名
            var moduleConfig = configParser.getConfig(moduleCode);
            String tableName = null;
            if (moduleConfig != null && moduleConfig.containsKey("pageConfig")) {
                JSONObject pageConfig = moduleConfig.getJSONObject("pageConfig");
                tableName = pageConfig.getString("tableName");
            }
            
            if (tableName == null || tableName.trim().isEmpty()) {
                log.error("未找到模块 [{}] 对应的表名配置", moduleCode);
                throw new ServiceException("未找到模块 [" + moduleCode + "] 对应的表名配置");
            }
            
            // 4. 修改数据（使用新的 updateByModuleWithTableName 方法）
            int result = ((com.ruoyi.erp.service.impl.SuperDataPermissionServiceImpl) superDataPermissionService)
                .updateByModuleWithTableName(moduleCode, tableName, data);
            
            log.info("修改数据成功，moduleCode: {}, rows: {}", moduleCode, result);
            return ErpResponse.ok();
            
        } catch (Exception e) {
            log.error("修改数据失败：{}", e.getMessage(), e);
            return ErpResponse.fail("修改失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除模块数据 (模板方法)
     * 
     * @param ids ID 数组
     * @return 操作结果
     */
    protected ErpResponse<?> deleteModuleData(Object[] ids) {
        String moduleCode = getModuleCode();
        
        try {
            // 1. 权限检查
            permissionChecker.checkModulePermission(moduleCode, "delete");
            
            // 2. 从配置中获取表名
            var moduleConfig = configParser.getConfig(moduleCode);
            String tableName = null;
            if (moduleConfig != null && moduleConfig.containsKey("pageConfig")) {
                JSONObject pageConfig = moduleConfig.getJSONObject("pageConfig");
                tableName = pageConfig.getString("tableName");
            }
            
            if (tableName == null || tableName.trim().isEmpty()) {
                log.error("未找到模块 [{}] 对应的表名配置", moduleCode);
                throw new ServiceException("未找到模块 [" + moduleCode + "] 对应的表名配置");
            }
            
            // 3. 删除数据（使用新的 deleteByModuleWithTableName 方法）
            int result = ((com.ruoyi.erp.service.impl.SuperDataPermissionServiceImpl) superDataPermissionService)
                .deleteByModuleWithTableName(moduleCode, tableName, ids);
            
            log.info("删除数据成功，moduleCode: {}, count: {}", moduleCode, result);
            return ErpResponse.ok();
            
        } catch (Exception e) {
            log.error("删除数据失败：{}", e.getMessage(), e);
            return ErpResponse.fail("删除失败：" + e.getMessage());
        }
    }
    
    /**
     * 审核模块数据 (模板方法)
     * 
     * @param ids ID 数组
     * @param auditResult 审核结果 (PASS/REJECT)
     * @param auditRemark 审核备注
     * @return 操作结果
     */
    protected ErpResponse<?> auditModuleData(Object[] ids, String auditResult, String auditRemark) {
        String moduleCode = getModuleCode();
        
        try {
            // 1. 权限检查
            permissionChecker.checkModulePermission(moduleCode, "audit");
            
            // 2. 调用审核服务
            // TODO: 实现审核逻辑
            
            return ErpResponse.ok();
            
        } catch (Exception e) {
            log.error("审核数据失败：{}", e.getMessage(), e);
            return ErpResponse.fail("审核失败：" + e.getMessage());
        }
    }
    
    /**
     * 数据验证 (子类可重写)
     * 
     * @param data 数据对象
     * @param moduleCode 模块编码
     */
    protected void validateData(Map<String, Object> data, String moduleCode) {
        // 默认验证逻辑
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("数据不能为空");
        }
    }
    
    /**
     * 异常处理
     * 
     * @param message 错误消息
     * @param e 异常对象
     * @return 统一响应
     */
    protected ErpResponse<?> handleException(String message, Exception e) {
        log.error("{}: {}", message, e.getMessage(), e);
        
        if (e instanceof NotPermissionException) {
            return ErpResponse.fail("权限不足");
        } else if (e instanceof ErpConfigException) {
            return ErpResponse.fail(e.getMessage());
        } else if (e instanceof IllegalArgumentException) {
            return ErpResponse.fail(e.getMessage());
        } else {
            return ErpResponse.fail(message + ": " + e.getMessage());
        }
    }
}
