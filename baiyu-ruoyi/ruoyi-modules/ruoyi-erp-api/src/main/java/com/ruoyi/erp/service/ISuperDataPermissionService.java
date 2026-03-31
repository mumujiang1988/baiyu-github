package com.ruoyi.erp.service;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import java.util.Map;

/**
 * Common data permission query service interface
 * Support dynamic query requirements for configurable pages
 * 
 * @author JMH
 * @date 2026-03-23
 */
public interface ISuperDataPermissionService {

    /**
     * Page query by module code, table name and conditions (single entry point)
     * 
     * @param moduleCode Module code (e.g., saleOrder, deliveryOrder, etc.)
     * @param tableName Table name (required, from JSON config)
     * @param pageQuery Page parameters
     * @param queryConfig Query config (contains conditions, orderBy, etc.)
     * @return Page result
     */
    TableDataInfo<Map<String, Object>> selectPageByModuleWithTableName(
        String moduleCode,
        String tableName,
        PageQuery pageQuery,
        Map<String, Object> queryConfig
    );
    
    /**
     * Insert data by module code and table name
     * 
     * @param moduleCode Module code
     * @param tableName Table name (required, from JSON config)
     * @param data Data object
     * @return Affected rows
     */
    int insertByModuleWithTableName(
        String moduleCode,
        String tableName,
        Map<String, Object> data
    );
    
    /**
     * Update data by module code and table name
     * 
     * @param moduleCode Module code
     * @param tableName Table name (required, from JSON config)
     * @param data Data object (must contain id field)
     * @return Affected rows
     */
    int updateByModuleWithTableName(
        String moduleCode,
        String tableName,
        Map<String, Object> data
    );
    
    /**
     * Delete data by module code and table name
     * 
     * @param moduleCode Module code
     * @param tableName Table name (required, from JSON config)
     * @param ids Primary key ID array
     * @return Affected rows
     */
    int deleteByModuleWithTableName(
        String moduleCode,
        String tableName,
        Object[] ids
    );
}
