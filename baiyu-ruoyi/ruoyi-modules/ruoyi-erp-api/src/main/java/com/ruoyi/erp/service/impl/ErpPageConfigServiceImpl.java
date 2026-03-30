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
 * ERP 公共配置 Service 业务层实现
 * 
 * @author JMH
 * @date 2026-03-22
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ErpPageConfigServiceImpl implements ErpPageConfigService {

    private final JdbcTemplate jdbcTemplate;
    private final SqlBuilder sqlBuilder;
    
    // 添加事件发布器
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public ErpPageConfigVo selectById(Long configId) {
        // 使用 SqlBuilder + JdbcTemplate 查询
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
        // 使用 SqlBuilder 构建查询条件
        List<Map<String, Object>> conditions = buildConditionsFromBo(bo);
        SqlBuilder.SqlResult sqlResult = sqlBuilder.buildWhere(conditions);
        
        // 构建完整 SQL
        StringBuilder sql = new StringBuilder("SELECT * FROM erp_page_config");
        sql.append(sqlResult.getSql());
        sql.append(" ORDER BY create_time DESC");
        
        // 执行查询
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql.toString(), sqlResult.getParams().toArray());
        
        // 转换为 VO
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
        // 使用 SqlBuilder 构建查询条件
        List<Map<String, Object>> conditions = buildConditionsFromBo(bo);
        SqlBuilder.SqlResult sqlResult = sqlBuilder.buildWhere(conditions);
        
        // 构建完整 SQL
        StringBuilder sql = new StringBuilder("SELECT * FROM erp_page_config");
        sql.append(sqlResult.getSql());
        sql.append(" ORDER BY create_time DESC");
        
        // 分页参数
        long pageNum = pageQuery.getPageNum();
        long pageSize = pageQuery.getPageSize();
        long offset = (pageNum - 1) * pageSize;
        
        // 添加分页限制
        sql.append(" LIMIT ? OFFSET ?");
        List<Object> params = new ArrayList<>(sqlResult.getParams());
        params.add(pageSize);
        params.add(offset);
        
        // 执行查询
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        
        // 查询总数
        String countSql = "SELECT COUNT(*) FROM erp_page_config" + sqlResult.getSql();
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, sqlResult.getParams().toArray());
        
        // 构建分页结果（使用 RuoYi TableDataInfo）
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
     * 从 Bo 构建查询条件
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
     * Map 转 VO
     */
    private ErpPageConfigVo convertMapToVo(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        
        ErpPageConfigVo vo = new ErpPageConfigVo();
        vo.setConfigId(getLongValue(map, "config_id"));
        vo.setModuleCode(getStringValue(map, "module_code"));
        vo.setConfigName(getStringValue(map, "config_name"));
        vo.setConfigType(getStringValue(map, "config_type"));
        vo.setStatus(getStringValue(map, "status"));
        vo.setIsPublic(getStringValue(map, "is_public"));
        vo.setParentConfigId(getLongValue(map, "parent_config_id"));
        vo.setVersion(getIntegerValue(map, "version"));
        vo.setPageConfig(getStringValue(map, "page_config"));
        vo.setFormConfig(getStringValue(map, "form_config"));
        vo.setTableConfig(getStringValue(map, "table_config"));
        vo.setSearchConfig(getStringValue(map, "search_config"));
        vo.setActionConfig(getStringValue(map, "action_config"));
        vo.setApiConfig(getStringValue(map, "api_config"));
        vo.setDictConfig(getStringValue(map, "dict_config"));
        vo.setBusinessConfig(getStringValue(map, "business_config"));
        vo.setDetailConfig(getStringValue(map, "detail_config"));
        vo.setRemark(getStringValue(map, "remark"));
        
        // 处理 LocalDateTime 类型
        Object createTime = map.get("create_time");
        if (createTime instanceof java.sql.Timestamp) {
            vo.setCreateTime(((java.sql.Timestamp) createTime).toLocalDateTime());
        }
        
        Object updateTime = map.get("update_time");
        if (updateTime instanceof java.sql.Timestamp) {
            vo.setUpdateTime(((java.sql.Timestamp) updateTime).toLocalDateTime());
        }
        
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

    private Date getDateValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof java.sql.Timestamp) {
            return (Date) value;
        }
        return null;
    }

    /**
     * Map 转 Entity
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
        
        // 处理 LocalDateTime 类型
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
     * Map 转 History VO
     */
    private ErpPageConfigHistoryVo convertHistoryMapToVo(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        
        ErpPageConfigHistoryVo vo = new ErpPageConfigHistoryVo();
        vo.setHistoryId(getLongValue(map, "history_id"));
        vo.setConfigId(getLongValue(map, "config_id"));
        vo.setModuleCode(getStringValue(map, "module_code"));
        vo.setConfigType(getStringValue(map, "config_type"));
        vo.setVersion(getIntegerValue(map, "version"));
        // History VO 只有 configContent 字段，需要从 entity 获取
        vo.setConfigContent(getStringValue(map, "config_content"));
        vo.setChangeReason(getStringValue(map, "change_reason"));
        vo.setChangeType(getStringValue(map, "change_type"));
        vo.setCreateBy(getStringValue(map, "create_by"));
        
        // 处理 LocalDateTime 类型
        Object createTime = map.get("create_time");
        if (createTime instanceof java.sql.Timestamp) {
            vo.setCreateTime(((java.sql.Timestamp) createTime).toLocalDateTime());
        }
        
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertByBo(ErpPageConfigBo bo) {
        // 检查模块编码 + 配置类型是否唯一（使用 SqlBuilder）
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
            throw new ServiceException("该模块编码和配置类型已存在");
        }
        
        ErpPageConfig config = MapstructUtils.convert(bo, ErpPageConfig.class);
        config.setVersion(1);
        
        // 使用 JdbcTemplate 插入
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
            // 清除缓存
            CacheUtils.evict(CacheNames.ERP_CONFIG, config.getModuleCode());
        }
        
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateByBo(ErpPageConfigBo bo) {
        ErpPageConfig config = MapstructUtils.convert(bo, ErpPageConfig.class);
        
        // 版本号 +1
        Integer newVersion = bo.getVersion() + 1;
        config.setVersion(newVersion);
        
        // 使用 JdbcTemplate 更新
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
            newVersion,
            config.getUpdateBy(),
            LocalDateTime.now(),
            config.getConfigId()
        );
        
        if (row > 0) {
            // 记录历史版本
            recordHistory(config, bo.getChangeReason());
            
            // 优化 1：主动清除缓存（而不是等待 TTL）
            CacheUtils.evict(CacheNames.ERP_CONFIG, config.getModuleCode());
            log.info("已清除配置缓存，moduleCode: {}, version: {}", 
                config.getModuleCode(), newVersion);
            
            // 优化 2：广播刷新事件（供其他模块监听处理）
            try {
                eventPublisher.publishEvent(
                    new ConfigRefreshEvent(this, config.getModuleCode(), newVersion)
                );
                log.info("已广播配置刷新事件，moduleCode: {}", config.getModuleCode());
            } catch (Exception e) {
                log.warn("广播配置刷新事件失败，但不影响主流程", e);
            }
        }
        
        return row;
    }

    /**
     * 记录配置历史（使用 JdbcTemplate）
     */
    private void recordHistory(ErpPageConfig config, String changeReason) {
        try {
            // 使用 JdbcTemplate 插入历史记录
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
            log.info("记录配置历史成功，configId: {}, version: {}", 
                config.getConfigId(), config.getVersion());
        } catch (Exception e) {
            log.error("记录配置历史失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(Long[] configIds) {
        if (ObjectUtil.isNotEmpty(configIds)) {
            // 先清除缓存
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
            
            // 批量删除（使用 IN 条件）
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
        // 查询配置信息并清除缓存
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
            
            // 执行删除
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
        // 先从缓存获取
        Object cached = CacheUtils.get(CacheNames.ERP_CONFIG, moduleCode);
        if (ObjectUtil.isNotNull(cached)) {
            log.info("[getPageConfig] 命中缓存，moduleCode: {}, cachedDataLength: {}",
                moduleCode, cached.toString().length());
            return cached.toString();
        }
    
        log.info("[getPageConfig] 缓存未命中，从数据库查询，moduleCode: {}", moduleCode);
    
        // 缓存未命中，从数据库查询（使用 SqlBuilder）
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
            log.warn("[getPageConfig] 未找到配置，moduleCode: {}", moduleCode);
            return null;
        }
            
        Map<String, Object> row = resultList.get(0);
        ErpPageConfig config = convertMapToEntity(row);
    
        // 标准返回：直接组合 JSON 字符串，不做解析和序列化
        // 九字段强制拆分：pageConfig, formConfig, tableConfig, searchConfig, actionConfig, apiConfig, dictConfig, businessConfig, detailConfig
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
    
        log.info("[getPageConfig] 数据库查询成功，configId: {}, moduleCode: {}, configName: {}, version: {}, status: {}, combinedJsonLength: {}",
            config.getConfigId(),
            config.getModuleCode(),
            config.getConfigName(),
            config.getVersion(),
            config.getStatus(),
            jsonString != null ? jsonString.length() : 0);
    
        // 放入缓存 (TTL: 1 小时 - 已在 CacheNames.ERP_CONFIG 中定义)
        CacheUtils.put(CacheNames.ERP_CONFIG, moduleCode, jsonString);
        log.info("[getPageConfig] 已放入缓存，moduleCode: {}", moduleCode);
    
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
        
        // 使用 SqlBuilder 构建查询条件
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
            log.warn("[getByModuleCode] 未找到配置，moduleCode: {}", moduleCode);
            return null;
        }
        
        Map<String, Object> row = resultList.get(0);
        ErpPageConfig config = convertMapToEntity(row);
        
        log.debug("[getByModuleCode] 查询成功，configId: {}, configName: {}, version: {}", 
            config.getConfigId(), config.getConfigName(), config.getVersion());
        
        return config;
    }

    @Override
    public TableDataInfo<ErpPageConfigHistoryVo> selectHistoryPage(Long configId, PageQuery pageQuery) {
        log.info("[selectHistoryPage] 查询配置历史，configId: {}", configId);
        
        // 使用 SqlBuilder 构建查询条件
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
        
        // 分页参数
        long pageNum = pageQuery.getPageNum();
        long pageSize = pageQuery.getPageSize();
        long offset = (pageNum - 1) * pageSize;
        
        // 添加分页限制
        sql.append(" LIMIT ? OFFSET ?");
        List<Object> params = new ArrayList<>(sqlResult.getParams());
        params.add(pageSize);
        params.add(offset);
        
        // 执行查询
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        
        // 查询总数
        String countSql = "SELECT COUNT(*) FROM erp_page_config_history" + sqlResult.getSql();
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, sqlResult.getParams().toArray());
        
        // 转换为 VO（使用 RuoYi TableDataInfo）
        List<ErpPageConfigHistoryVo> voList = new ArrayList<>();
        for (Map<String, Object> row : resultList) {
            ErpPageConfigHistoryVo vo = convertHistoryMapToVo(row);
            if (vo != null) {
                voList.add(vo);
            }
        }
        TableDataInfo<ErpPageConfigHistoryVo> tableDataInfo = new TableDataInfo<>(voList, total);
        
        log.info("[selectHistoryPage] 查询成功，total: {}", total);
        return tableDataInfo;
    }

    @Override
    public ErpPageConfigHistoryVo getVersionDetail(Long configId, Integer version) {
        log.info("[getVersionDetail] 查询版本详情，configId: {}, version: {}", configId, version);
        
        // 使用 SqlBuilder 构建查询条件
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
            log.warn("[getVersionDetail] 未找到版本，configId: {}, version: {}", configId, version);
            return null;
        }
        
        ErpPageConfigHistoryVo vo = convertHistoryMapToVo(resultList.get(0));
        log.info("[getVersionDetail] 查询成功，historyId: {}", vo.getHistoryId());
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
        // History Entity 有分开的配置字段
        history.setPageConfig(getStringValue(map, "page_config"));
        history.setFormConfig(getStringValue(map, "form_config"));
        history.setTableConfig(getStringValue(map, "table_config"));
        history.setSearchConfig(getStringValue(map, "search_config"));
        history.setActionConfig(getStringValue(map, "action_config"));
        history.setDictConfig(getStringValue(map, "dict_config"));
        history.setApiConfig(getStringValue(map, "api_config"));
        history.setBusinessConfig(getStringValue(map, "business_config"));
        // ErpPageConfigHistory 没有 detailConfig 字段
        history.setChangeReason(getStringValue(map, "change_reason"));
        history.setChangeType(getStringValue(map, "change_type"));
        history.setCreateBy(getStringValue(map, "create_by"));
        
        // 处理 LocalDateTime 类型
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
            // 1. 查询当前配置（使用 SqlBuilder）
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
            
            // 2. 查询目标版本（使用 SqlBuilder）
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
            
            // 3. 更新配置内容为目标版本
            currentConfig.setPageConfig(targetVersionHistory.getPageConfig());
            currentConfig.setFormConfig(targetVersionHistory.getFormConfig());
            currentConfig.setTableConfig(targetVersionHistory.getTableConfig());
            currentConfig.setApiConfig(targetVersionHistory.getApiConfig());
            currentConfig.setDictConfig(targetVersionHistory.getDictConfig());
            currentConfig.setBusinessConfig(targetVersionHistory.getBusinessConfig());
            currentConfig.setVersion(currentConfig.getVersion() + 1); // 版本号 +1
            
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
            
            // 4. 记录回滚历史（使用 JdbcTemplate）
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
            
            // 5. 清除缓存
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
            // 1. 查询配置详情
            ErpPageConfigVo config = selectById(configId);
            if (config == null) {
                throw new ServiceException("配置不存在");
            }
            
            // 2. 设置响应头
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + 
                java.net.URLEncoder.encode(config.getModuleCode() + "_config.json", "UTF-8"));
            
            // 3. 写入 JSON 数据
            // 🔧 修复：组合 6 个字段为 JSON 对象
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
            // 1. 读取文件内容
            String content = new String(file.getBytes(), "UTF-8");
            
            // 2. 解析 JSON 并验证格式
            Object parsedJson = JsonUtils.parseObject(content, Object.class);
            if (parsedJson == null) {
                throw new ServiceException("JSON 格式错误");
            }
            
            // 3. 生成模块编码（如果原文件中有则使用，否则生成新的）
            String moduleCode = "imported_" + System.currentTimeMillis();
            
            // 4. 构建配置对象
            ErpPageConfigBo bo = new ErpPageConfigBo();
            bo.setModuleCode(moduleCode);
            bo.setConfigName("导入的配置 - " + System.currentTimeMillis());
            bo.setConfigType("PAGE");
            // 🔧 修复：解析 JSON 并设置 6 个字段
            try {
                if (parsedJson instanceof Map) {
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
                // 如果解析失败，将整个内容作为 pageConfig
                bo.setPageConfig(content);
            }
            bo.setStatus("1");
            bo.setIsPublic("0");
            bo.setRemark("通过导入功能创建");
            
            // 5. 保存配置
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
            // 1. 查询原配置
            ErpPageConfigVo originalConfig = selectById(configId);
            if (originalConfig == null) {
                throw new ServiceException("原配置不存在");
            }
            
            // 2. 构建新配置
            ErpPageConfigBo newConfig = new ErpPageConfigBo();
            newConfig.setModuleCode(originalConfig.getModuleCode() + "_copy_" + System.currentTimeMillis());
            newConfig.setConfigName(originalConfig.getConfigName() + " (副本)");
            newConfig.setConfigType(originalConfig.getConfigType());
            // 🔧 修复：复制 6 个字段
            newConfig.setPageConfig(originalConfig.getPageConfig());
            newConfig.setFormConfig(originalConfig.getFormConfig());
            newConfig.setTableConfig(originalConfig.getTableConfig());
            newConfig.setApiConfig(originalConfig.getApiConfig());
            newConfig.setDictConfig(originalConfig.getDictConfig());
            newConfig.setBusinessConfig(originalConfig.getBusinessConfig());
            newConfig.setStatus("1");
            newConfig.setIsPublic("0");
            newConfig.setRemark("复制自配置 ID: " + configId);
            
            // 3. 保存新配置
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
            // 1. 查询配置（使用 SqlBuilder + JdbcTemplate）
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
            
            // 2. 验证状态值
            if (!"0".equals(status) && !"1".equals(status)) {
                throw new ServiceException("状态值必须为 0 或 1");
            }
            
            // 3. 更新状态（使用 JdbcTemplate）
            String updateSql = "UPDATE erp_page_config SET status = ?, update_time = ? WHERE config_id = ?";
            int count = jdbcTemplate.update(updateSql, status, LocalDateTime.now(), configId);
            
            if (count <= 0) {
                throw new ServiceException("更新状态失败");
            }
            
            // 4. 清除缓存
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
