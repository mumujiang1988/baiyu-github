package com.ruoyi.erp.service.impl;
import cn.hutool.core.util.ObjectUtil;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import com.ruoyi.common.core.constant.CacheNames;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.MapstructUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.json.utils.JsonUtils;
import com.ruoyi.common.redis.utils.CacheUtils;
import com.ruoyi.common.satoken.utils.LoginHelper;
import com.ruoyi.erp.domain.bo.ErpPageConfigBo;
import com.ruoyi.erp.domain.entity.ErpPageConfig;
import com.ruoyi.erp.domain.entity.ErpPageConfigHistory;
import com.ruoyi.erp.domain.vo.ErpPageConfigVo;
import com.ruoyi.erp.domain.vo.ErpPageConfigHistoryVo;
import com.ruoyi.erp.event.ConfigRefreshEvent;
import com.ruoyi.erp.service.ErpPageConfigService;
import com.ruoyi.erp.utils.SqlBuilder;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.*;

/**
 * ERP Page Config Service Business Layer Implementation
 * @author JMH
 * @date 2026-03-22
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ErpPageConfigServiceImpl implements ErpPageConfigService {

    private final JdbcTemplate jdbcTemplate;
    private final SqlBuilder sqlBuilder;
    
    // Add event publisher
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public ErpPageConfigVo selectById(Long configId) {
        // Use SqlBuilder + JdbcTemplate query
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("field", "config_id");
        condition.put("operator", "eq");
        condition.put("value", configId);
        conditions.add(condition);
        
        SqlBuilder.SqlResult sqlResult = sqlBuilder.buildWhere(conditions);
        String sql = "SELECT * FROM erp_page_config" + sqlResult.getSql();
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql, sqlResult.getParams().toArray());
        
        if (resultList.isEmpty()) {
            return null;
        }
        
        return convertMapToVo(resultList.get(0));
    }

    @Override
    public List<ErpPageConfigVo> selectList(ErpPageConfigBo bo) {
        // Use SqlBuilder to build query conditions
        List<Map<String, Object>> conditions = buildConditionsFromBo(bo);
        SqlBuilder.SqlResult sqlResult = sqlBuilder.buildWhere(conditions);
        
        // Build complete SQL
        StringBuilder sql = new StringBuilder("SELECT * FROM erp_page_config");
        sql.append(sqlResult.getSql());
        sql.append(" ORDER BY create_time DESC");
        
        // Execute query
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql.toString(), sqlResult.getParams().toArray());
        
        // Convert to VO
        List<ErpPageConfigVo> voList = new ArrayList<>();
        for (Map<String, Object> row : resultList) {
            ErpPageConfigVo vo = convertMapToVo(row);
            if (vo != null) {
                voList.add(vo);
            }
        }
        
        return voList;
    }

    @Override
    public TableDataInfo<ErpPageConfigVo> selectPageList(ErpPageConfigBo bo, PageQuery pageQuery) {
        // Use SqlBuilder to build query conditions
        List<Map<String, Object>> conditions = buildConditionsFromBo(bo);
        SqlBuilder.SqlResult sqlResult = sqlBuilder.buildWhere(conditions);
        
        // Build complete SQL
        StringBuilder sql = new StringBuilder("SELECT * FROM erp_page_config");
        sql.append(sqlResult.getSql());
        sql.append(" ORDER BY create_time DESC");
        
        // Page params
        long pageNum = pageQuery.getPageNum();
        long pageSize = pageQuery.getPageSize();
        long offset = (pageNum - 1) * pageSize;
        
        // Add pagination limit
        sql.append(" LIMIT ? OFFSET ?");
        List<Object> params = new ArrayList<>(sqlResult.getParams());
        params.add(pageSize);
        params.add(offset);
        
        // Execute query
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        
        // Query total count
        String countSql = "SELECT COUNT(*) FROM erp_page_config" + sqlResult.getSql();
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, sqlResult.getParams().toArray());
        
        // Build page result (use RuoYi TableDataInfo)
        List<ErpPageConfigVo> voList = new ArrayList<>();
        for (Map<String, Object> row : resultList) {
            ErpPageConfigVo vo = convertMapToVo(row);
            if (vo != null) {
                voList.add(vo);
            }
        }
        TableDataInfo<ErpPageConfigVo> tableDataInfo = new TableDataInfo<>(voList, total);
        return tableDataInfo;
    }

    /**
     * Build query conditions from Bo
     */
    private List<Map<String, Object>> buildConditionsFromBo(ErpPageConfigBo bo) {
        List<Map<String, Object>> conditions = new ArrayList<>();
        
        if (StringUtils.isNotBlank(bo.getModuleCode())) {
            Map<String, Object> condition = new HashMap<>();
            condition.put("field", "module_code");
            condition.put("operator", "eq");
            condition.put("value", bo.getModuleCode());
            conditions.add(condition);
        }
        
        if (StringUtils.isNotBlank(bo.getConfigName())) {
            Map<String, Object> condition = new HashMap<>();
            condition.put("field", "config_name");
            condition.put("operator", "like");
            condition.put("value", bo.getConfigName());
            conditions.add(condition);
        }
        
        if (StringUtils.isNotBlank(bo.getConfigType())) {
            Map<String, Object> condition = new HashMap<>();
            condition.put("field", "config_type");
            condition.put("operator", "eq");
            condition.put("value", bo.getConfigType());
            conditions.add(condition);
        }
        
        if (StringUtils.isNotBlank(bo.getStatus())) {
            Map<String, Object> condition = new HashMap<>();
            condition.put("field", "status");
            condition.put("operator", "eq");
            condition.put("value", bo.getStatus());
            conditions.add(condition);
        }
        
        if (StringUtils.isNotBlank(bo.getIsPublic())) {
            Map<String, Object> condition = new HashMap<>();
            condition.put("field", "is_public");
            condition.put("operator", "eq");
            condition.put("value", bo.getIsPublic());
            conditions.add(condition);
        }
        
        if (ObjectUtil.isNotNull(bo.getParentConfigId())) {
            Map<String, Object> condition = new HashMap<>();
            condition.put("field", "parent_config_id");
            condition.put("operator", "eq");
            condition.put("value", bo.getParentConfigId());
            conditions.add(condition);
        }
        
        return conditions;
    }

    /**
     * Map to VO
     */
    private ErpPageConfigVo convertMapToVo(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        
        // Manual field mapping (avoid Mapstruct conversion exception)
        ErpPageConfigVo vo = new ErpPageConfigVo();
        vo.setConfigId(getLongValue(map, "config_id"));
        vo.setModuleCode(getStringValue(map, "module_code"));
        vo.setConfigName(getStringValue(map, "config_name"));
        vo.setConfigType(getStringValue(map, "config_type"));
        vo.setPageConfig(getStringValue(map, "page_config"));
        vo.setFormConfig(getStringValue(map, "form_config"));
        vo.setTableConfig(getStringValue(map, "table_config"));
        vo.setSearchConfig(getStringValue(map, "search_config"));
        vo.setActionConfig(getStringValue(map, "action_config"));
        vo.setApiConfig(getStringValue(map, "api_config"));
        vo.setDictConfig(getStringValue(map, "dict_config"));
        vo.setBusinessConfig(getStringValue(map, "business_config"));
        vo.setDetailConfig(getStringValue(map, "detail_config"));
        vo.setVersion(getIntegerValue(map, "version"));
        vo.setIsPublic(getStringValue(map, "is_public"));
        vo.setStatus(getStringValue(map, "status"));
        vo.setRemark(getStringValue(map, "remark"));
        vo.setCreateTime(getLocalDateTimeValue(map, "create_time"));
        vo.setUpdateTime(getLocalDateTimeValue(map, "update_time"));
        
        return vo;
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? Long.valueOf(value.toString()) : null;
    }

    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? Integer.valueOf(value.toString()) : null;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private LocalDateTime getLocalDateTimeValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        if (value instanceof java.util.Date) {
            return ((java.util.Date) value).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        }
        return null;
    }

    /**
     * Map to Entity
     */
    private ErpPageConfig convertMapToEntity(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        
        ErpPageConfig config = new ErpPageConfig();
        config.setConfigId(getLongValue(map, "config_id"));
        config.setModuleCode(getStringValue(map, "module_code"));
        config.setConfigName(getStringValue(map, "config_name"));
        config.setConfigType(getStringValue(map, "config_type"));
        config.setStatus(getStringValue(map, "status"));
        config.setIsPublic(getStringValue(map, "is_public"));
        config.setParentConfigId(getLongValue(map, "parent_config_id"));
        config.setVersion(getIntegerValue(map, "version"));
        config.setPageConfig(getStringValue(map, "page_config"));
        config.setFormConfig(getStringValue(map, "form_config"));
        config.setTableConfig(getStringValue(map, "table_config"));
        config.setSearchConfig(getStringValue(map, "search_config"));
        config.setActionConfig(getStringValue(map, "action_config"));
        config.setApiConfig(getStringValue(map, "api_config"));
        config.setDictConfig(getStringValue(map, "dict_config"));
        config.setBusinessConfig(getStringValue(map, "business_config"));
        config.setDetailConfig(getStringValue(map, "detail_config"));
        config.setRemark(getStringValue(map, "remark"));
        config.setCreateBy(getStringValue(map, "create_by"));
        
        // Handle LocalDateTime type
        Object createTime = map.get("create_time");
        if (createTime instanceof java.sql.Timestamp) {
            config.setCreateTime(((java.sql.Timestamp) createTime).toLocalDateTime());
        }
        
        config.setUpdateBy(getStringValue(map, "update_by"));
        Object updateTime = map.get("update_time");
        if (updateTime instanceof java.sql.Timestamp) {
            config.setUpdateTime(((java.sql.Timestamp) updateTime).toLocalDateTime());
        }
        
        return config;
    }

    /**
     * Map to History VO
     */
    private ErpPageConfigHistoryVo convertHistoryMapToVo(Map<String, Object> map) {
        // Use RuoYi MapstructUtils auto conversion
        return MapstructUtils.convert(map, ErpPageConfigHistoryVo.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertByBo(ErpPageConfigBo bo) {
        // Check module code + config type uniqueness (use SqlBuilder)
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition1 = new HashMap<>();
        condition1.put("field", "module_code");
        condition1.put("operator", "eq");
        condition1.put("value", bo.getModuleCode());
        conditions.add(condition1);
        
        Map<String, Object> condition2 = new HashMap<>();
        condition2.put("field", "config_type");
        condition2.put("operator", "eq");
        condition2.put("value", bo.getConfigType());
        conditions.add(condition2);
        
        SqlBuilder.SqlResult sqlResult = sqlBuilder.buildWhere(conditions);
        String countSql = "SELECT COUNT(*) FROM erp_page_config" + sqlResult.getSql();
        Long count = jdbcTemplate.queryForObject(countSql, Long.class, sqlResult.getParams().toArray());
        
        if (count != null && count > 0) {
            throw new ServiceException("Module code and config type already exists");
        }
        
        ErpPageConfig config = MapstructUtils.convert(bo, ErpPageConfig.class);
        config.setVersion(1);
        
        // Use JdbcTemplate insert
        String sql = """
            INSERT INTO erp_page_config (
                module_code, config_name, config_type,
                page_config, form_config, table_config,
                search_config, action_config, api_config,
                dict_config, business_config, detail_config,
                version, status, is_public, parent_config_id,
                remark, create_by, create_time
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        int row = jdbcTemplate.update(sql,
            config.getModuleCode(),
            config.getConfigName(),
            config.getConfigType(),
            config.getPageConfig(),
            config.getFormConfig(),
            config.getTableConfig(),
            config.getSearchConfig(),
            config.getActionConfig(),
            config.getApiConfig(),
            config.getDictConfig(),
            config.getBusinessConfig(),
            config.getDetailConfig(),
            config.getVersion(),
            config.getStatus(),
            config.getIsPublic(),
            config.getParentConfigId(),
            config.getRemark(),
            config.getCreateBy(),
            LocalDateTime.now()
        );
        
        if (row > 0) {
            // Clear cache
            CacheUtils.evict(CacheNames.ERP_CONFIG, config.getModuleCode());
        }
        
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateByBo(ErpPageConfigBo bo) {
        ErpPageConfig config = MapstructUtils.convert(bo, ErpPageConfig.class);
        
        // Version number +1
        Integer newVersion = bo.getVersion() + 1;
        config.setVersion(newVersion);
        
        // Manually set updater (from login user)
        String updateBy = StringUtils.substring(LoginHelper.getUsername(), 0, 64);
        config.setUpdateBy(updateBy != null ? updateBy : "admin");
        
        // Use JdbcTemplate update
        String sql = """
            UPDATE erp_page_config 
            SET page_config = ?, 
                form_config = ?,
                table_config = ?,
                search_config = ?,
                action_config = ?,
                api_config = ?,
                dict_config = ?,
                business_config = ?,
                detail_config = ?,
                status = ?,
                version = ?,
                update_by = ?,
                update_time = ?
            WHERE config_id = ?
        """;
        int row = jdbcTemplate.update(sql,
            config.getPageConfig(),
            config.getFormConfig(),
            config.getTableConfig(),
            config.getSearchConfig(),
            config.getActionConfig(),
            config.getApiConfig(),
            config.getDictConfig(),
            config.getBusinessConfig(),
            config.getDetailConfig(),
            config.getStatus(),
            newVersion,
            config.getUpdateBy(),
            LocalDateTime.now(),
            config.getConfigId()
        );
        
        if (row > 0) {
            // Record history version
            recordHistory(config, bo.getChangeReason());
            
            // Optimization 1: Actively clear cache (instead of waiting for TTL)
            CacheUtils.evict(CacheNames.ERP_CONFIG, config.getModuleCode());
            
            // Optimization 2: Broadcast refresh event (for other modules to listen and handle)
            try {
                eventPublisher.publishEvent(
                    new ConfigRefreshEvent(this, config.getModuleCode(), newVersion)
                );
            } catch (Exception e) {
                log.warn("Broadcast config refresh event failed, but does not affect main flow", e);
            }
        }
        
        return row;
    }

    /**
     * Record config history (use JdbcTemplate)
     */
    private void recordHistory(ErpPageConfig config, String changeReason) {
        try {
            // Use JdbcTemplate insert history
            String sql = """
                INSERT INTO erp_page_config_history (
                    config_id, module_code, config_type, version,
                    page_config, form_config, table_config,
                    search_config, action_config, api_config,
                    dict_config, business_config,
                    change_reason, change_type, create_by, create_time
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            jdbcTemplate.update(sql,
                config.getConfigId(),
                config.getModuleCode(),
                config.getConfigType(),
                config.getVersion(),
                config.getPageConfig(),
                config.getFormConfig(),
                config.getTableConfig(),
                config.getSearchConfig(),
                config.getActionConfig(),
                config.getApiConfig(),
                config.getDictConfig(),
                config.getBusinessConfig(),
                changeReason,
                "UPDATE",
                config.getUpdateBy(),
                LocalDateTime.now()
            );
        } catch (Exception e) {
            log.error("Record config history failed", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(Long[] configIds) {
        if (ObjectUtil.isNotEmpty(configIds)) {
            // First clear cache
            for (Long configId : configIds) {
                List<Map<String, Object>> conditions = new ArrayList<>();
                Map<String, Object> condition = new HashMap<>();
                condition.put("field", "config_id");
                condition.put("operator", "eq");
                condition.put("value", configId);
                conditions.add(condition);
                
                SqlBuilder.SqlResult sqlResult = sqlBuilder.buildWhere(conditions);
                String selectSql = "SELECT module_code FROM erp_page_config" + sqlResult.getSql();
                List<Map<String, Object>> result = jdbcTemplate.queryForList(selectSql, sqlResult.getParams().toArray());
                
                if (!result.isEmpty()) {
                    String moduleCode = (String) result.get(0).get("module_code");
                    CacheUtils.evict(CacheNames.ERP_CONFIG, moduleCode);
                }
            }
            
            // Batch delete (use IN condition)
            StringBuilder inClause = new StringBuilder();
            for (int i = 0; i < configIds.length; i++) {
                if (i > 0) inClause.append(",");
                inClause.append("?");
            }
            
            String deleteSql = "DELETE FROM erp_page_config WHERE config_id IN (" + inClause + ")";
            return jdbcTemplate.update(deleteSql, (Object[]) configIds);
        }
        return 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long configId) {
        // Query config info and clear cache
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("field", "config_id");
        condition.put("operator", "eq");
        condition.put("value", configId);
        conditions.add(condition);
        
        SqlBuilder.SqlResult sqlResult = sqlBuilder.buildWhere(conditions);
        String selectSql = "SELECT module_code FROM erp_page_config" + sqlResult.getSql();
        List<Map<String, Object>> result = jdbcTemplate.queryForList(selectSql, sqlResult.getParams().toArray());
        
        if (!result.isEmpty()) {
            String moduleCode = (String) result.get(0).get("module_code");
            CacheUtils.evict(CacheNames.ERP_CONFIG, moduleCode);
            
            // Execute delete
            String deleteSql = "DELETE FROM erp_page_config WHERE config_id = ?";
            return jdbcTemplate.update(deleteSql, configId);
        }
        return 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveWithVersion(ErpPageConfigBo bo) {
        if (ObjectUtil.isNull(bo.getConfigId())) {
            return insertByBo(bo);
        } else {
            return updateByBo(bo);
        }
    }

    @Override
    public String getPageConfig(String moduleCode) {
        // First get from cache
        Object cached = CacheUtils.get(CacheNames.ERP_CONFIG, moduleCode);
        if (ObjectUtil.isNotNull(cached)) {
            return cached.toString();
        }
    
        log.info("[getPageConfig] Cache miss, query from database, moduleCode: {}", moduleCode);
    
        // Cache miss, query from database (use SqlBuilder)
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition1 = new HashMap<>();
        condition1.put("field", "module_code");
        condition1.put("operator", "eq");
        condition1.put("value", moduleCode);
        conditions.add(condition1);
            
        Map<String, Object> condition2 = new HashMap<>();
        condition2.put("field", "status");
        condition2.put("operator", "eq");
        condition2.put("value", "1");
        conditions.add(condition2);
            
        SqlBuilder.SqlResult sqlResult = sqlBuilder.buildWhere(conditions);
        StringBuilder sql = new StringBuilder("SELECT * FROM erp_page_config");
        sql.append(sqlResult.getSql());
        sql.append(" ORDER BY version DESC LIMIT 1");
            
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql.toString(), sqlResult.getParams().toArray());
            
        if (resultList.isEmpty()) {
            return null;
        }
            
        Map<String, Object> row = resultList.get(0);
        ErpPageConfig config = convertMapToEntity(row);
    
        // Standard return: directly combine JSON string, no parsing and serialization
        // Nine-field split: pageConfig, formConfig, tableConfig, searchConfig, actionConfig, apiConfig, dictConfig, businessConfig, detailConfig
        String jsonString = String.format(
            "{\"pageConfig\":%s,\"formConfig\":%s,\"tableConfig\":%s,\"searchConfig\":%s,\"actionConfig\":%s,\"apiConfig\":%s,\"dictionaryConfig\":%s,\"businessConfig\":%s,\"detailConfig\":%s,\"moduleCode\":\"%s\",\"configName\":\"%s\",\"version\":%d}",
            config.getPageConfig(),
            config.getFormConfig(),
            config.getTableConfig(),
            config.getSearchConfig(),
            config.getActionConfig(),
            config.getApiConfig(),
            config.getDictConfig(),
            config.getBusinessConfig(),
            config.getDetailConfig(),
            escapeJson(config.getModuleCode()),
            escapeJson(config.getConfigName()),
            config.getVersion()
        );
    
        log.info("[getPageConfig] Database query successful, configId: {}, moduleCode: {}, version: {}",
            config.getConfigId(), moduleCode, config.getVersion());
    
        // Put in cache (TTL: 1 hour - already defined in CacheNames.ERP_CONFIG)
        CacheUtils.put(CacheNames.ERP_CONFIG, moduleCode, jsonString);
                
        return jsonString;
    }

    /**
     * JSON字符串转义
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"");
    }

    @Override
    public ErpPageConfig getByModuleCode(String moduleCode) {
        log.debug("[getByModuleCode] 查询模块配置，moduleCode: {}", moduleCode);
        
        // Use SqlBuilder to build query conditions
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition1 = new HashMap<>();
        condition1.put("field", "module_code");
        condition1.put("operator", "eq");
        condition1.put("value", moduleCode);
        conditions.add(condition1);
            
        Map<String, Object> condition2 = new HashMap<>();
        condition2.put("field", "status");
        condition2.put("operator", "eq");
        condition2.put("value", "1");
        conditions.add(condition2);
            
        SqlBuilder.SqlResult sqlResult = sqlBuilder.buildWhere(conditions);
        StringBuilder sql = new StringBuilder("SELECT * FROM erp_page_config");
        sql.append(sqlResult.getSql());
        sql.append(" ORDER BY version DESC LIMIT 1");
            
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql.toString(), sqlResult.getParams().toArray());
            
        if (resultList.isEmpty()) {
            return null;
        }
        
        Map<String, Object> row = resultList.get(0);
        ErpPageConfig config = convertMapToEntity(row);
        
        log.debug("[getByModuleCode] Query successful, configId: {}, configName: {}, version: {}",
            config.getConfigId(), config.getConfigName(), config.getVersion());
        
        return config;
    }

    @Override
    public TableDataInfo<ErpPageConfigHistoryVo> selectHistoryPage(Long configId, PageQuery pageQuery) {
        log.info("[selectHistoryPage] Query config history, configId: {}", configId);
        
        // Use SqlBuilder to build query conditions
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("field", "config_id");
        condition.put("operator", "eq");
        condition.put("value", configId);
        conditions.add(condition);
        
        SqlBuilder.SqlResult sqlResult = sqlBuilder.buildWhere(conditions);
        StringBuilder sql = new StringBuilder("SELECT * FROM erp_page_config_history");
        sql.append(sqlResult.getSql());
        sql.append(" ORDER BY version DESC");
        
        // Page params
        long pageNum = pageQuery.getPageNum();
        long pageSize = pageQuery.getPageSize();
        long offset = (pageNum - 1) * pageSize;
        
        // Add pagination limit
        sql.append(" LIMIT ? OFFSET ?");
        List<Object> params = new ArrayList<>(sqlResult.getParams());
        params.add(pageSize);
        params.add(offset);
        
        // Execute query
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        
        // Query total count
        String countSql = "SELECT COUNT(*) FROM erp_page_config_history" + sqlResult.getSql();
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, sqlResult.getParams().toArray());
        
        // Convert to VO (use RuoYi TableDataInfo)
        List<ErpPageConfigHistoryVo> voList = new ArrayList<>();
        for (Map<String, Object> row : resultList) {
            ErpPageConfigHistoryVo vo = convertHistoryMapToVo(row);
            if (vo != null) {
                voList.add(vo);
            }
        }
        TableDataInfo<ErpPageConfigHistoryVo> tableDataInfo = new TableDataInfo<>(voList, total);
        
        log.info("[selectHistoryPage] Query successful, total: {}", total);
        return tableDataInfo;
    }

    @Override
    public ErpPageConfigHistoryVo getVersionDetail(Long configId, Integer version) {
        log.info("[getVersionDetail] Query version detail, configId: {}, version: {}", configId, version);
        
        // Use SqlBuilder to build query conditions
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition1 = new HashMap<>();
        condition1.put("field", "config_id");
        condition1.put("operator", "eq");
        condition1.put("value", configId);
        conditions.add(condition1);
        
        Map<String, Object> condition2 = new HashMap<>();
        condition2.put("field", "version");
        condition2.put("operator", "eq");
        condition2.put("value", version);
        conditions.add(condition2);
        
        SqlBuilder.SqlResult sqlResult = sqlBuilder.buildWhere(conditions);
        StringBuilder sql = new StringBuilder("SELECT * FROM erp_page_config_history");
        sql.append(sqlResult.getSql());
        
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql.toString(), sqlResult.getParams().toArray());
        
        if (resultList.isEmpty()) {
            return null;
        }
        
        ErpPageConfigHistoryVo vo = convertHistoryMapToVo(resultList.get(0));
        return vo;
    }

    /**
     * Map 转 History Entity
     */
    private ErpPageConfigHistory convertHistoryMapToEntity(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        
        ErpPageConfigHistory history = new ErpPageConfigHistory();
        history.setHistoryId(getLongValue(map, "history_id"));
        history.setConfigId(getLongValue(map, "config_id"));
        history.setModuleCode(getStringValue(map, "module_code"));
        history.setConfigType(getStringValue(map, "config_type"));
        history.setVersion(getIntegerValue(map, "version"));
        // History Entity has separate config fields
        history.setPageConfig(getStringValue(map, "page_config"));
        history.setFormConfig(getStringValue(map, "form_config"));
        history.setTableConfig(getStringValue(map, "table_config"));
        history.setSearchConfig(getStringValue(map, "search_config"));
        history.setActionConfig(getStringValue(map, "action_config"));
        history.setDictConfig(getStringValue(map, "dict_config"));
        history.setApiConfig(getStringValue(map, "api_config"));
        history.setBusinessConfig(getStringValue(map, "business_config"));
        // ErpPageConfigHistory does not have detailConfig field
        history.setChangeReason(getStringValue(map, "change_reason"));
        history.setChangeType(getStringValue(map, "change_type"));
        history.setCreateBy(getStringValue(map, "create_by"));
        
        // Handle LocalDateTime type
        Object createTime = map.get("create_time");
        if (createTime instanceof java.sql.Timestamp) {
            history.setCreateTime(((java.sql.Timestamp) createTime).toLocalDateTime());
        }
        
        return history;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackToVersion(Long configId, Integer targetVersion, String reason) {
        log.info("[rollbackToVersion] 开始回滚，configId: {}, targetVersion: {}, reason: {}", 
            configId, targetVersion, reason);
        
        try {
            // 1. Query current config (use SqlBuilder)
            List<Map<String, Object>> conditions1 = new ArrayList<>();
            Map<String, Object> condition1 = new HashMap<>();
            condition1.put("field", "config_id");
            condition1.put("operator", "eq");
            condition1.put("value", configId);
            conditions1.add(condition1);
            
            SqlBuilder.SqlResult sqlResult1 = sqlBuilder.buildWhere(conditions1);
            StringBuilder sql1 = new StringBuilder("SELECT * FROM erp_page_config");
            sql1.append(sqlResult1.getSql());
            
            List<Map<String, Object>> currentList = jdbcTemplate.queryForList(sql1.toString(), sqlResult1.getParams().toArray());
            if (currentList.isEmpty()) {
                throw new ServiceException("配置不存在");
            }
            ErpPageConfig currentConfig = convertMapToEntity(currentList.get(0));
            
            // 2. Query target version (use SqlBuilder)
            List<Map<String, Object>> conditions2 = new ArrayList<>();
            Map<String, Object> condition2a = new HashMap<>();
            condition2a.put("field", "config_id");
            condition2a.put("operator", "eq");
            condition2a.put("value", configId);
            conditions2.add(condition2a);
            
            Map<String, Object> condition2b = new HashMap<>();
            condition2b.put("field", "version");
            condition2b.put("operator", "eq");
            condition2b.put("value", targetVersion);
            conditions2.add(condition2b);
            
            SqlBuilder.SqlResult sqlResult2 = sqlBuilder.buildWhere(conditions2);
            StringBuilder sql2 = new StringBuilder("SELECT * FROM erp_page_config_history");
            sql2.append(sqlResult2.getSql());
            
            List<Map<String, Object>> historyList = jdbcTemplate.queryForList(sql2.toString(), sqlResult2.getParams().toArray());
            if (historyList.isEmpty()) {
                throw new ServiceException("目标版本不存在");
            }
            Map<String, Object> historyRow = historyList.get(0);
            ErpPageConfigHistory targetVersionHistory = convertHistoryMapToEntity(historyRow);
            
            // 3. Update config content to target version
            currentConfig.setPageConfig(targetVersionHistory.getPageConfig());
            currentConfig.setFormConfig(targetVersionHistory.getFormConfig());
            currentConfig.setTableConfig(targetVersionHistory.getTableConfig());
            currentConfig.setApiConfig(targetVersionHistory.getApiConfig());
            currentConfig.setDictConfig(targetVersionHistory.getDictConfig());
            currentConfig.setBusinessConfig(targetVersionHistory.getBusinessConfig());
            currentConfig.setVersion(currentConfig.getVersion() + 1); // Version number +1
            
            int updateCount = jdbcTemplate.update(
                """
                    UPDATE erp_page_config 
                    SET page_config = ?, 
                        form_config = ?,
                        table_config = ?,
                        api_config = ?,
                        dict_config = ?,
                        business_config = ?,
                        version = ?,
                        update_by = ?,
                        update_time = ?
                    WHERE config_id = ?
                """,
                currentConfig.getPageConfig(),
                currentConfig.getFormConfig(),
                currentConfig.getTableConfig(),
                currentConfig.getApiConfig(),
                currentConfig.getDictConfig(),
                currentConfig.getBusinessConfig(),
                currentConfig.getVersion(),
                currentConfig.getUpdateBy(),
                LocalDateTime.now(),
                currentConfig.getConfigId()
            );
            if (updateCount <= 0) {
                throw new ServiceException("更新配置失败");
            }
            
            // 4. Record rollback history (use JdbcTemplate)
            String insertHistorySql = """
                INSERT INTO erp_page_config_history (
                    config_id, module_code, config_type, version,
                    page_config, form_config, table_config,
                    api_config, dict_config, business_config,
                    change_reason, change_type, create_by, create_time
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            jdbcTemplate.update(insertHistorySql,
                currentConfig.getConfigId(),
                currentConfig.getModuleCode(),
                currentConfig.getConfigType(),
                currentConfig.getVersion(),
                targetVersionHistory.getPageConfig(),
                targetVersionHistory.getFormConfig(),
                targetVersionHistory.getTableConfig(),
                targetVersionHistory.getApiConfig(),
                targetVersionHistory.getDictConfig(),
                targetVersionHistory.getBusinessConfig(),
                reason != null ? reason : "回滚到版本 v" + targetVersion,
                "ROLLBACK",
                currentConfig.getUpdateBy(),
                LocalDateTime.now()
            );
            
            // 5. Clear cache
            CacheUtils.evict(CacheNames.ERP_CONFIG, currentConfig.getModuleCode());
            
            log.info("[rollbackToVersion] 回滚成功，newVersion: {}", currentConfig.getVersion());
        } catch (Exception e) {
            log.error("[rollbackToVersion] 回滚失败", e);
            throw new ServiceException("回滚失败：" + e.getMessage());
        }
    }

    @Override
    public void exportConfig(Long configId, HttpServletResponse response) {
        log.info("[exportConfig] 导出配置，configId: {}", configId);
        
        try {
            // 1. Query config detail
            ErpPageConfigVo config = selectById(configId);
            if (config == null) {
                throw new ServiceException("配置不存在");
            }
            
            // 2. Set response headers
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + 
                java.net.URLEncoder.encode(config.getModuleCode() + "_config.json", "UTF-8"));
            
            // 3. Write JSON data
            // Fix: Combine 6 fields into JSON object
            Map<String, Object> jsonContent = new HashMap<>();
            jsonContent.put("pageConfig", parseJsonString(config.getPageConfig()));
            jsonContent.put("formConfig", parseJsonString(config.getFormConfig()));
            jsonContent.put("tableConfig", parseJsonString(config.getTableConfig()));
            jsonContent.put("apiConfig", parseJsonString(config.getApiConfig()));
            jsonContent.put("dictionaryConfig", parseJsonString(config.getDictConfig()));
            jsonContent.put("businessConfig", parseJsonString(config.getBusinessConfig()));
            response.getWriter().write(JsonUtils.toJsonString(jsonContent));
            response.getWriter().flush();
            
            log.info("[exportConfig] 导出成功，moduleCode: {}", config.getModuleCode());
        } catch (Exception e) {
            log.error("[exportConfig] 导出失败", e);
            throw new ServiceException("导出失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importConfig(MultipartFile file) {
        log.info("[importConfig] 导入配置，文件名：{}", file.getOriginalFilename());
        
        try {
            // 1. Read file content
            String content = new String(file.getBytes(), "UTF-8");
            
            // 2. Parse JSON and validate format
            Object parsedJson = JsonUtils.parseObject(content, Object.class);
            if (parsedJson == null) {
                throw new ServiceException("JSON 格式错误");
            }
            
            // 3. Generate module code (use from original file if exists, otherwise generate new)
            String moduleCode = "imported_" + System.currentTimeMillis();
            
            // 4. Build config object
            ErpPageConfigBo bo = new ErpPageConfigBo();
            bo.setModuleCode(moduleCode);
            bo.setConfigName("导入的配置 - " + System.currentTimeMillis());
            bo.setConfigType("PAGE");
            // Fix: Parse JSON and set 6 fields
            try {
                if (parsedJson instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> configMap = (Map<String, Object>) parsedJson;
                    bo.setPageConfig(JsonUtils.toJsonString(configMap.get("pageConfig")));
                    bo.setFormConfig(JsonUtils.toJsonString(configMap.get("formConfig")));
                    bo.setTableConfig(JsonUtils.toJsonString(configMap.get("tableConfig")));
                    bo.setApiConfig(JsonUtils.toJsonString(configMap.get("apiConfig")));
                    bo.setDictConfig(JsonUtils.toJsonString(configMap.get("dictionaryConfig")));
                    bo.setBusinessConfig(JsonUtils.toJsonString(configMap.get("businessConfig")));
                }
            } catch (Exception e) {
                log.warn("解析导入的 JSON 失败，将使用原始字符串", e);
                // If parsing failed, use entire content as pageConfig
                bo.setPageConfig(content);
            }
            bo.setStatus("1");
            bo.setIsPublic("0");
            bo.setRemark("通过导入功能创建");
            
            // 5. Save config
            insertByBo(bo);
            
            log.info("[importConfig] 导入成功，moduleCode: {}", moduleCode);
        } catch (Exception e) {
            log.error("[importConfig] 导入失败", e);
            throw new ServiceException("导入失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpPageConfigVo copyConfig(Long configId) {
        log.info("[copyConfig] 复制配置，configId: {}", configId);
        
        try {
            // 1. Query original config
            ErpPageConfigVo originalConfig = selectById(configId);
            if (originalConfig == null) {
                throw new ServiceException("原配置不存在");
            }
            
            // 2. Build new config
            ErpPageConfigBo newConfig = new ErpPageConfigBo();
            newConfig.setModuleCode(originalConfig.getModuleCode() + "_copy_" + System.currentTimeMillis());
            newConfig.setConfigName(originalConfig.getConfigName() + " (副本)");
            newConfig.setConfigType(originalConfig.getConfigType());
            // Fix: Copy 6 fields
            newConfig.setPageConfig(originalConfig.getPageConfig());
            newConfig.setFormConfig(originalConfig.getFormConfig());
            newConfig.setTableConfig(originalConfig.getTableConfig());
            newConfig.setApiConfig(originalConfig.getApiConfig());
            newConfig.setDictConfig(originalConfig.getDictConfig());
            newConfig.setBusinessConfig(originalConfig.getBusinessConfig());
            newConfig.setStatus("1");
            newConfig.setIsPublic("0");
            newConfig.setRemark("复制自配置 ID: " + configId);
            
            // 3. Save new config
            insertByBo(newConfig);
            
            log.info("[copyConfig] 复制成功，newModuleCode: {}", newConfig.getModuleCode());
            return selectById(newConfig.getConfigId());
        } catch (Exception e) {
            log.error("[copyConfig] 复制失败", e);
            throw new ServiceException("复制失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfigStatus(Long configId, String status) {
        log.info("[updateConfigStatus] 更新配置状态，configId: {}, status: {}", configId, status);
        
        try {
            // 1. Query config (use SqlBuilder + JdbcTemplate)
            List<Map<String, Object>> conditions = new ArrayList<>();
            Map<String, Object> condition = new HashMap<>();
            condition.put("field", "config_id");
            condition.put("operator", "eq");
            condition.put("value", configId);
            conditions.add(condition);
            
            SqlBuilder.SqlResult sqlResult = sqlBuilder.buildWhere(conditions);
            String selectSql = "SELECT * FROM erp_page_config" + sqlResult.getSql();
            List<Map<String, Object>> resultList = jdbcTemplate.queryForList(selectSql, sqlResult.getParams().toArray());
            
            if (resultList.isEmpty()) {
                throw new ServiceException("配置不存在");
            }
            
            Map<String, Object> row = resultList.get(0);
            String moduleCode = getStringValue(row, "module_code");
            
            // 2. Validate status value
            if (!"0".equals(status) && !"1".equals(status)) {
                throw new ServiceException("状态值必须为 0 或 1");
            }
            
            // 3. Update status (use JdbcTemplate)
            String updateSql = "UPDATE erp_page_config SET status = ?, update_time = ? WHERE config_id = ?";
            int count = jdbcTemplate.update(updateSql, status, LocalDateTime.now(), configId);
            
            if (count <= 0) {
                throw new ServiceException("更新状态失败");
            }
            
            // 4. Clear cache
            CacheUtils.evict(CacheNames.ERP_CONFIG, moduleCode);
            
            log.info("[updateConfigStatus] 更新成功，newStatus: {}", status);
        } catch (Exception e) {
            log.error("[updateConfigStatus] 更新失败", e);
            throw new ServiceException("更新失败：" + e.getMessage());
        }
    }

    /**
     * 解析 JSON 字符串为对象
     * 
     * @param jsonStr JSON 字符串
     * @return 解析后的对象，如果为空则返回 null
     */
    private Object parseJsonString(String jsonStr) {
        if (StringUtils.isEmpty(jsonStr)) {
            return null;
        }
        try {
            return JsonUtils.parseObject(jsonStr, Object.class);
        } catch (Exception e) {
            log.error("[parseJsonString] JSON 解析失败：{}", jsonStr, e);
            return null;
        }
    }
}
