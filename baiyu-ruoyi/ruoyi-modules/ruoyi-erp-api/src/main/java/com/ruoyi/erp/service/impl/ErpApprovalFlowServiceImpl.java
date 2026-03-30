package com.ruoyi.erp.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.MapstructUtils;
import com.ruoyi.common.core.utils.StringUtils;
import static com.ruoyi.erp.utils.JdbcResultUtils.*;
import com.ruoyi.common.redis.utils.RedisUtils;
import com.ruoyi.erp.domain.bo.ErpApprovalFlowBo;
import com.ruoyi.erp.domain.vo.ErpApprovalFlowVo;
import com.ruoyi.erp.service.ErpApprovalFlowService;
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
 * ERP 审批流程配置 Service 业务层实现
 * 
 * @author JMH
 * @date 2026-03-22
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ErpApprovalFlowServiceImpl implements ErpApprovalFlowService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public ErpApprovalFlowVo selectById(Long flowId) {
        String sql = """
            SELECT 
                flow_id, module_code, flow_name, flow_definition,
                current_version, is_active, create_by,
                create_time, update_by, update_time
            FROM erp_approval_flow
            WHERE flow_id = ?
        """;
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, flowId);
        if (results.isEmpty()) {
            return null;
        }
        
        // 转换为 VO 对象
        Map<String, Object> row = results.get(0);
        ErpApprovalFlowVo vo = new ErpApprovalFlowVo();
        vo.setFlowId(getLong(row.get("flow_id")));
        vo.setModuleCode(getString(row.get("module_code")));
        vo.setFlowName(getString(row.get("flow_name")));
        vo.setFlowDefinition(getString(row.get("flow_definition")));
        vo.setCurrentVersion(getInteger(row.get("current_version")));
        vo.setIsActive(getString(row.get("is_active")));
        return vo;
    }

    @Override
    public List<ErpApprovalFlowVo> selectList(ErpApprovalFlowBo bo) {
        StringBuilder sql = new StringBuilder("""
            SELECT 
                flow_id, module_code, flow_name, flow_definition,
                current_version, is_active, create_by,
                create_time, update_by, update_time
            FROM erp_approval_flow
            WHERE 1=1
        """);
        
        // 构建动态 WHERE 条件
        if (StringUtils.isNotBlank(bo.getModuleCode())) {
            sql.append(" AND module_code = ?");
        }
        if (StringUtils.isNotBlank(bo.getFlowName())) {
            sql.append(" AND flow_name LIKE ?");
        }
        if (StringUtils.isNotBlank(bo.getIsActive())) {
            sql.append(" AND is_active = ?");
        }
        
        // 查询参数
        List<Object> params = buildParams(bo);
        
        // 执行查询并转换为 VO 列表
        return queryForVoList(sql.toString(), params);
    }

    @Override
    public TableDataInfo<ErpApprovalFlowVo> selectPageList(ErpApprovalFlowBo bo, PageQuery pageQuery) {
        // 构建查询 SQL（不含排序和分页）
        StringBuilder countSql = new StringBuilder("""
            SELECT COUNT(*) 
            FROM erp_approval_flow 
            WHERE 1=1
        """);
        
        StringBuilder querySql = new StringBuilder("""
            SELECT 
                flow_id, module_code, flow_name, flow_definition,
                current_version, is_active, create_by,
                create_time, update_by, update_time
            FROM erp_approval_flow
            WHERE 1=1
        """);
        
        // 添加条件
        if (StringUtils.isNotBlank(bo.getModuleCode())) {
            countSql.append(" AND module_code = ?");
            querySql.append(" AND module_code = ?");
        }
        if (StringUtils.isNotBlank(bo.getFlowName())) {
            countSql.append(" AND flow_name LIKE ?");
            querySql.append(" AND flow_name LIKE ?");
        }
        if (StringUtils.isNotBlank(bo.getIsActive())) {
            countSql.append(" AND is_active = ?");
            querySql.append(" AND is_active = ?");
        }
        
        List<Object> params = buildParams(bo);
        
        // 查询总数
        Long total = jdbcTemplate.queryForObject(countSql.toString(), Long.class, params.toArray());
        if (total == null || total == 0) {
            return new TableDataInfo<>(new ArrayList<>(), 0L);
        }
        
        // 添加排序和分页
        querySql.append(" ORDER BY create_time DESC LIMIT ?, ?");
        params.add((pageQuery.getPageNum() - 1) * pageQuery.getPageSize());
        params.add(pageQuery.getPageSize());
        
        // 查询数据
        List<ErpApprovalFlowVo> list = queryForVoList(querySql.toString(), params);
        
        // 构建分页结果（使用 RuoYi TableDataInfo）
        TableDataInfo<ErpApprovalFlowVo> tableDataInfo = new TableDataInfo<>(list, total);
        return tableDataInfo;
    }

    /**
     * 构建查询参数列表
     */
    private List<Object> buildParams(ErpApprovalFlowBo bo) {
        List<Object> params = new ArrayList<>();
        if (StringUtils.isNotBlank(bo.getModuleCode())) {
            params.add(bo.getModuleCode());
        }
        if (StringUtils.isNotBlank(bo.getFlowName())) {
            params.add("%" + bo.getFlowName() + "%");
        }
        if (StringUtils.isNotBlank(bo.getIsActive())) {
            params.add(bo.getIsActive());
        }
        return params;
    }
    
    /**
     * 查询并转换为 VO 列表
     */
    private List<ErpApprovalFlowVo> queryForVoList(String sql, List<Object> params) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params.toArray());
        return rows.stream().map(this::mapToVo).toList();
    }
    
    /**
     * Map 转 VO
     */
    private ErpApprovalFlowVo mapToVo(Map<String, Object> row) {
        // 使用 RuoYi MapstructUtils 自动转换
        return MapstructUtils.convert(row, ErpApprovalFlowVo.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertByBo(ErpApprovalFlowBo bo) {
        // 检查模块编码是否唯一（使用 JdbcTemplate）
        String checkSql = """
            SELECT COUNT(*) 
            FROM erp_approval_flow 
            WHERE module_code = ? AND is_active = '1'
        """;
        Long count = jdbcTemplate.queryForObject(checkSql, Long.class, bo.getModuleCode(), "1");
        
        if (count != null && count > 0) {
            throw new ServiceException("该模块的激活审批流程已存在");
        }
        
        // 使用 JdbcTemplate 插入
        String sql = """
            INSERT INTO erp_approval_flow (
                module_code, flow_name, flow_definition,
                current_version, is_active, create_by, create_time
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        
        int row = jdbcTemplate.update(sql,
            bo.getModuleCode(),
            bo.getFlowName(),
            bo.getFlowDefinition(),
            1,  // current_version 默认为 1
            bo.getIsActive(),
            bo.getCreateBy(),
            LocalDateTime.now()
        );
        
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateByBo(ErpApprovalFlowBo bo) {
        // 版本号 +1
        Integer newVersion = bo.getCurrentVersion() + 1;
        
        // 使用 JdbcTemplate 更新
        String sql = """
            UPDATE erp_approval_flow 
            SET flow_name = ?,
                flow_definition = ?,
                current_version = ?,
                is_active = ?,
                update_by = ?,
                update_time = ?
            WHERE flow_id = ?
        """;
        
        int row = jdbcTemplate.update(sql,
            bo.getFlowName(),
            bo.getFlowDefinition(),
            newVersion,
            bo.getIsActive(),
            bo.getUpdateBy(),
            LocalDateTime.now(),
            bo.getFlowId()
        );
        
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(Long[] flowIds) {
        if (ObjectUtil.isNotEmpty(flowIds)) {
            // 批量删除（使用 IN 条件）
            StringBuilder inClause = new StringBuilder();
            for (int i = 0; i < flowIds.length; i++) {
                if (i > 0) inClause.append(",");
                inClause.append("?");
            }
            
            String sql = "DELETE FROM erp_approval_flow WHERE flow_id IN (" + inClause + ")";
            return jdbcTemplate.update(sql, (Object[]) flowIds);
        }
        return 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long flowId) {
        String sql = "DELETE FROM erp_approval_flow WHERE flow_id = ?";
        return jdbcTemplate.update(sql, flowId);
    }

    @Override
    public ErpApprovalFlowVo getApprovalFlow(String moduleCode) {
        String sql = """
            SELECT 
                flow_id, module_code, flow_name, flow_nodes,
                current_version, is_active, remark, create_by,
                create_time, update_by, update_time
            FROM erp_approval_flow
            WHERE module_code = ? AND is_active = '1'
            ORDER BY current_version DESC
            LIMIT 1
        """;
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, moduleCode);
        if (results.isEmpty()) {
            return null;
        }
        
        return mapToVo(results.get(0));
    }
}
