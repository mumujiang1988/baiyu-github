package com.ruoyi.erp.service;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import java.util.Map;

/**
 * Common data permission query service interface
 * 
 * @author JMH
 * @date 2026-03-23
 */
public interface ISuperDataPermissionService {

    /**
     * Page query by module code, table name and conditions
     */
    TableDataInfo<Map<String, Object>> selectPageByModuleWithTableName(
        String moduleCode,
        String tableName,
        PageQuery pageQuery,
        Map<String, Object> queryConfig
    );
    
    /**
     * Insert data by module code and table name
     */
    int insertByModuleWithTableName(
        String moduleCode,
        String tableName,
        Map<String, Object> data
    );
    
    /**
     * Update data by module code and table name
     */
    int updateByModuleWithTableName(
        String moduleCode,
        String tableName,
        Map<String, Object> data
    );
    
    /**
     * Delete data by module code and table name
     */
    int deleteByModuleWithTableName(
        String moduleCode,
        String tableName,
        Object[] ids
    );
}
