package com.ruoyi.erp.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.MapstructUtils;
import com.ruoyi.common.core.utils.StringUtils;
import static com.ruoyi.erp.utils.JdbcResultUtils.*; 
import com.ruoyi.erp.domain.bo.ErpPushRelationBo;
import com.ruoyi.erp.domain.vo.ErpPushRelationVo;
import com.ruoyi.erp.service.ErpPushRelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ERP Push Relation Config Service Business Layer Implementation
 * @author JMH
 * @date 2026-03-22
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ErpPushRelationServiceImpl implements ErpPushRelationService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public ErpPushRelationVo selectById(Long relationId) {
        String sql = """
            SELECT 
                relation_id, source_module, target_module, relation_name,
                mapping_rules, transformation_rules, validation_rules,
                concurrency_control, transaction_enabled, status,
                version, create_by, create_time, update_by, update_time
            FROM erp_push_relation
            WHERE relation_id = ?
        """;
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, relationId);
        if (results.isEmpty()) {
            return null;
        }
        
        // Convert to VO object
        Map<String, Object> row = results.get(0);
        ErpPushRelationVo vo = new ErpPushRelationVo();
        vo.setRelationId(getLong(row.get("relation_id")));
        vo.setSourceModule(getString(row.get("source_module")));
        vo.setTargetModule(getString(row.get("target_module")));
        vo.setRelationName(getString(row.get("relation_name")));
        vo.setMappingRules(getString(row.get("mapping_rules")));
        vo.setTransformationRules(getString(row.get("transformation_rules")));
        vo.setValidationRules(getString(row.get("validation_rules")));
        vo.setConcurrencyControl(getString(row.get("concurrency_control")));
        vo.setTransactionEnabled(getString(row.get("transaction_enabled")));
        vo.setStatus(getString(row.get("status")));
        vo.setVersion(getInteger(row.get("version")));
        return vo;
    }

    @Override
    public List<ErpPushRelationVo> selectList(ErpPushRelationBo bo) {
        StringBuilder sql = new StringBuilder("""
            SELECT 
                relation_id, source_module, target_module, relation_name,
                mapping_rules, transformation_rules, validation_rules,
                concurrency_control, transaction_enabled, status,
                version, create_by, create_time, update_by, update_time
            FROM erp_push_relation
            WHERE 1=1
        """);
        
        // Build dynamic WHERE conditions
        if (StringUtils.isNotBlank(bo.getSourceModule())) {
            sql.append(" AND source_module = ?");
        }
        if (StringUtils.isNotBlank(bo.getTargetModule())) {
            sql.append(" AND target_module = ?");
        }
        if (StringUtils.isNotBlank(bo.getRelationName())) {
            sql.append(" AND relation_name LIKE ?");
        }
        if (StringUtils.isNotBlank(bo.getStatus())) {
            sql.append(" AND status = ?");
        }
        
        // Query params
        List<Object> params = buildParams(bo);
        
        // Execute query and convert to VO list
        return queryForVoList(sql.toString(), params);
    }

    @Override
    public TableDataInfo<ErpPushRelationVo> selectPageList(ErpPushRelationBo bo, PageQuery pageQuery) {
        // Build query SQL (without ORDER BY and pagination)
        StringBuilder countSql = new StringBuilder("""
            SELECT COUNT(*) 
            FROM erp_push_relation 
            WHERE 1=1
        """);
        
        StringBuilder querySql = new StringBuilder("""
            SELECT 
                relation_id, source_module, target_module, relation_name,
                field_mapping, transform_rules, version,
                status, remark, create_by,
                create_time, update_by, update_time
            FROM erp_push_relation
            WHERE 1=1
        """);
        
        // Add conditions
        if (StringUtils.isNotBlank(bo.getSourceModule())) {
            countSql.append(" AND source_module = ?");
            querySql.append(" AND source_module = ?");
        }
        if (StringUtils.isNotBlank(bo.getTargetModule())) {
            countSql.append(" AND target_module = ?");
            querySql.append(" AND target_module = ?");
        }
        if (StringUtils.isNotBlank(bo.getRelationName())) {
            countSql.append(" AND relation_name LIKE ?");
            querySql.append(" AND relation_name LIKE ?");
        }
        if (StringUtils.isNotBlank(bo.getStatus())) {
            countSql.append(" AND status = ?");
            querySql.append(" AND status = ?");
        }
        
        List<Object> params = buildParams(bo);
        
        // Query total count
        Long total = jdbcTemplate.queryForObject(countSql.toString(), Long.class, params.toArray());
        if (total == null || total == 0) {
            return new TableDataInfo<>(new ArrayList<>(), 0L);
        }
        
        // Add ORDER BY and pagination
        querySql.append(" ORDER BY create_time DESC LIMIT ?, ?");
        params.add((pageQuery.getPageNum() - 1) * pageQuery.getPageSize());
        params.add(pageQuery.getPageSize());
        
        // Query data
        List<ErpPushRelationVo> list = queryForVoList(querySql.toString(), params);
        
        // Build page result (use RuoYi TableDataInfo)
        TableDataInfo<ErpPushRelationVo> tableDataInfo = new TableDataInfo<>(list, total);
        return tableDataInfo;
    }


    /**
     * Build query params list
     */
    private List<Object> buildParams(ErpPushRelationBo bo) {
        List<Object> params = new ArrayList<>();
        if (StringUtils.isNotBlank(bo.getSourceModule())) {
            params.add(bo.getSourceModule());
        }
        if (StringUtils.isNotBlank(bo.getTargetModule())) {
            params.add(bo.getTargetModule());
        }
        if (StringUtils.isNotBlank(bo.getRelationName())) {
            params.add("%" + bo.getRelationName() + "%");
        }
        if (StringUtils.isNotBlank(bo.getStatus())) {
            params.add(bo.getStatus());
        }
        return params;
    }
    
    /**
     * Query and convert to VO list
     */
    private List<ErpPushRelationVo> queryForVoList(String sql, List<Object> params) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params.toArray());
        return rows.stream().map(this::mapToVo).toList();
    }
    
    /**
     * Map to VO
     */
    private ErpPushRelationVo mapToVo(Map<String, Object> row) {
        // Use RuoYi MapstructUtils auto conversion
        return MapstructUtils.convert(row, ErpPushRelationVo.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertByBo(ErpPushRelationBo bo) {
        // Check source module + target module uniqueness (use JdbcTemplate)
        String checkSql = """
            SELECT COUNT(*) 
            FROM erp_push_relation 
            WHERE source_module = ? AND target_module = ?
        """;
        Long count = jdbcTemplate.queryForObject(checkSql, Long.class, bo.getSourceModule(), bo.getTargetModule());
        
        if (count != null && count > 0) {
            throw new ServiceException("Push relation from source module to target module already exists");
        }
        
        // Use JdbcTemplate insert
        String sql = """
            INSERT INTO erp_push_relation (
                source_module, target_module, relation_name,
                mapping_rules, transformation_rules, validation_rules,
                concurrency_control, transaction_enabled, status,
                version, create_by, create_time
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        int row = jdbcTemplate.update(sql,
            bo.getSourceModule(),
            bo.getTargetModule(),
            bo.getRelationName(),
            bo.getMappingRules(),
            bo.getTransformationRules(),
            bo.getValidationRules(),
            bo.getConcurrencyControl(),
            bo.getTransactionEnabled(),
            bo.getStatus(),
            1,  // version default to 1
            bo.getCreateBy(),
            LocalDateTime.now()
        );
        
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateByBo(ErpPushRelationBo bo) {
        // Use JdbcTemplate update
        String sql = """
            UPDATE erp_push_relation 
            SET relation_name = ?,
                mapping_rules = ?,
                transformation_rules = ?,
                validation_rules = ?,
                concurrency_control = ?,
                transaction_enabled = ?,
                status = ?,
                update_by = ?,
                update_time = ?
            WHERE relation_id = ?
        """;
        
        int row = jdbcTemplate.update(sql,
            bo.getRelationName(),
            bo.getMappingRules(),
            bo.getTransformationRules(),
            bo.getValidationRules(),
            bo.getConcurrencyControl(),
            bo.getTransactionEnabled(),
            bo.getStatus(),
            bo.getUpdateBy(),
            LocalDateTime.now(),
            bo.getRelationId()
        );
        
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(Long[] relationIds) {
        if (ObjectUtil.isNotEmpty(relationIds)) {
            // Batch delete (use IN condition)
            StringBuilder inClause = new StringBuilder();
            for (int i = 0; i < relationIds.length; i++) {
                if (i > 0) inClause.append(",");
                inClause.append("?");
            }
            
            String sql = "DELETE FROM erp_push_relation WHERE relation_id IN (" + inClause + ")";
            return jdbcTemplate.update(sql, (Object[]) relationIds);
        }
        return 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long relationId) {
        String sql = "DELETE FROM erp_push_relation WHERE relation_id = ?";
        return jdbcTemplate.update(sql, relationId);
    }

    @Override
    public ErpPushRelationVo getPushRelation(String sourceModule, String targetModule) {
        String sql = """
            SELECT 
                relation_id, source_module, target_module, relation_name,
                field_mapping, transform_rules, version,
                status, remark, create_by,
                create_time, update_by, update_time
            FROM erp_push_relation
            WHERE source_module = ? AND target_module = ? AND status = '1'
            LIMIT 1
        """;
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, sourceModule, targetModule);
        if (results.isEmpty()) {
            return null;
        }
        
        return mapToVo(results.get(0));
    }
}
