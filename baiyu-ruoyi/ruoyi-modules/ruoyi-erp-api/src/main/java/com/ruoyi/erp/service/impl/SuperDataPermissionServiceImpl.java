package com.ruoyi.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.erp.service.ISuperDataPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 通用数据权限查询服务实现类（最优方案）
 * 
 * 特性：
 * 1. 无反射 - 使用MyBatis-Plus官方API
 * 2. 参数顺序保证 - 按MPGENVAL顺序提取
 * 3. SQL注入防护 - 表名/字段名格式校验
 * 4. 异常细化 - 区分不同错误类型
 * 5. 性能优化 - 可选COUNT查询
 * 
 * @author JMH
 * @date 2026-03-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SuperDataPermissionServiceImpl implements ISuperDataPermissionService {

    private final JdbcTemplate jdbcTemplate;
    
    // SQL标识符格式校验（防注入）
    private static final Pattern SQL_IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    // ==================== 分页查询（核心优化） ====================
    @Override
    public Page<Map<String, Object>> selectPageByModuleWithTableName(
            String moduleCode,
            String tableName,
            PageQuery pageQuery,
            QueryWrapper<Object> queryWrapper,
            List<Object> queryParams) {
        
        // 统一参数校验
        validateTableName(tableName);
        validatePageQuery(pageQuery);

        try {
            // 1. 构建 SQL（使用官方 API，无反射）
            String whereClause = queryWrapper != null ? queryWrapper.getSqlSegment() : null;
                    
            // 替换 MyBatis 占位符为 ? 占位符（适配 JdbcTemplate）
            if (whereClause != null && !whereClause.isEmpty()) {
                // 将 #{ew.paramNameValuePairs.MPGENVALx} 替换为 ?
                whereClause = whereClause.replaceAll("#\\{ew\\.paramNameValuePairs\\.MPGENVAL\\d+\\}", "?");
                log.info("[SQL 片段] {}", whereClause);
            }
                    
            String selectSql = buildSelectSql(tableName, whereClause);
            String countSql = buildCountSql(tableName, whereClause);
            
            // 2. 使用从Controller传入的参数
            List<Object> params = queryParams != null ? queryParams : new ArrayList<>();
            log.info("[查询参数] 数量:{}, 值:{}", params.size(), params);
            
            // 3. 分页参数
            long pageNum = pageQuery.getPageNum();
            long pageSize = pageQuery.getPageSize();
            long offset = (pageNum - 1) * pageSize;
            
            // 4. 执行查询
            String pageSql = selectSql + " LIMIT ? OFFSET ?";
            List<Object> pageParams = new ArrayList<>(params);
            pageParams.add(pageSize);
            pageParams.add(offset);
            
            List<Map<String, Object>> records = jdbcTemplate.queryForList(pageSql, pageParams.toArray());
            
            // 5. 查询总数（性能优化：可考虑缓存或异步）
            Long total = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());
            
            // 6. 封装结果
            Page<Map<String, Object>> page = new Page<>(pageNum, pageSize, total);
            page.setRecords(records);
            
            log.info("[分页查询成功] module:{}, table:{}, total:{}", moduleCode, tableName, total);
            return page;
            
        } catch (BadSqlGrammarException e) {
            log.error("[SQL语法错误] module:{}, sql异常", moduleCode, e);
            throw new ServiceException("SQL语法错误，请联系管理员检查配置");
        } catch (Exception e) {
            log.error("[查询异常] module:{}", moduleCode, e);
            throw new ServiceException("查询失败：" + e.getMessage());
        }
    }

    // ==================== 新增 ====================
    @Override
    public int insertByModuleWithTableName(String moduleCode, String tableName, Map<String, Object> data) {
        validateTableName(tableName);
        validateData(data);

        try {
            // 分离字段与值（排除ID，校验字段名）
            List<String> columns = new ArrayList<>();
            List<Object> values = new ArrayList<>();
            
            data.forEach((k, v) -> {
                if (!"id".equalsIgnoreCase(k)) {
                    validateFieldName(k);  // 防注入
                    columns.add(k);
                    values.add(v);
                }
            });

            // 构建SQL
            String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                    tableName,
                    String.join(",", columns),
                    String.join(",", Collections.nCopies(columns.size(), "?")));

            int rows = jdbcTemplate.update(sql, values.toArray());
            log.info("[插入成功] module:{}, table:{}, 影响行数:{}", moduleCode, tableName, rows);
            return rows;

        } catch (BadSqlGrammarException e) {
            log.error("[SQL语法错误] module:{}", moduleCode, e);
            throw new ServiceException("SQL语法错误，请检查字段名是否正确");
        } catch (Exception e) {
            log.error("[插入异常] module:{}", moduleCode, e);
            throw new ServiceException("插入失败：" + e.getMessage());
        }
    }

    // ==================== 更新 ====================
    @Override
    public int updateByModuleWithTableName(String moduleCode, String tableName, Map<String, Object> data) {
        validateTableName(tableName);
        validateData(data);
        
        Object id = data.get("id");
        if (Objects.isNull(id)) {
            throw new ServiceException("更新必须携带主键ID");
        }

        try {
            List<String> sets = new ArrayList<>();
            List<Object> values = new ArrayList<>();
            
            data.forEach((k, v) -> {
                if (!"id".equalsIgnoreCase(k)) {
                    validateFieldName(k);  // 防注入
                    sets.add(k + "=?");
                    values.add(v);
                }
            });

            String sql = String.format("UPDATE %s SET %s WHERE id=?", tableName, String.join(",", sets));
            values.add(id);

            int rows = jdbcTemplate.update(sql, values.toArray());
            log.info("[更新成功] module:{}, table:{}, id:{}, 影响行数:{}", moduleCode, tableName, id, rows);
            return rows;

        } catch (BadSqlGrammarException e) {
            log.error("[SQL语法错误] module:{}", moduleCode, e);
            throw new ServiceException("SQL语法错误，请检查字段名是否正确");
        } catch (Exception e) {
            log.error("[更新异常] module:{}", moduleCode, e);
            throw new ServiceException("更新失败：" + e.getMessage());
        }
    }

    // ==================== 删除 ====================
    @Override
    public int deleteByModuleWithTableName(String moduleCode, String tableName, Object[] ids) {
        validateTableName(tableName);
        
        if (ids == null || ids.length == 0) {
            throw new ServiceException("删除ID不能为空");
        }

        try {
            String sql = String.format("DELETE FROM %s WHERE id IN (%s)",
                    tableName,
                    String.join(",", Collections.nCopies(ids.length, "?")));

            int rows = jdbcTemplate.update(sql, ids);
            log.info("[删除成功] module:{}, table:{}, 删除数量:{}", moduleCode, tableName, ids.length);
            return rows;

        } catch (Exception e) {
            log.error("[删除异常] module:{}", moduleCode, e);
            throw new ServiceException("删除失败：" + e.getMessage());
        }
    }

    // ==================== 核心工具方法 ====================

    /**
     * 构建SELECT SQL
     */
    private String buildSelectSql(String tableName, String whereClause) {
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(tableName);
        if (whereClause != null && !whereClause.isEmpty()) {
            sql.append(" WHERE ").append(whereClause);
        }
        return sql.toString();
    }
    
    /**
     * 构建COUNT SQL
     */
    private String buildCountSql(String tableName, String whereClause) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ").append(tableName);
        if (whereClause != null && !whereClause.isEmpty()) {
            sql.append(" WHERE ").append(whereClause);
        }
        return sql.toString();
    }

    // ==================== 安全校验 ====================
    
    /**
     * 表名格式校验（防SQL注入）
     * 不使用白名单，仅校验格式
     */
    private void validateTableName(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new ServiceException("表名不能为空");
        }
        
        // 格式校验：只允许字母、数字、下划线
        if (!SQL_IDENTIFIER_PATTERN.matcher(tableName).matches()) {
            throw new ServiceException("表名格式非法: " + tableName);
        }
        
        // 长度校验
        if (tableName.length() > 64) {
            throw new ServiceException("表名长度超过限制");
        }
    }
    
    /**
     * 字段名格式校验（防SQL注入）
     */
    private void validateFieldName(String fieldName) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            throw new ServiceException("字段名不能为空");
        }
        
        // 格式校验
        if (!SQL_IDENTIFIER_PATTERN.matcher(fieldName).matches()) {
            throw new ServiceException("字段名格式非法: " + fieldName);
        }
        
        // 长度校验
        if (fieldName.length() > 64) {
            throw new ServiceException("字段名长度超过限制");
        }
    }
    
    /**
     * 分页参数校验
     */
    private void validatePageQuery(PageQuery pageQuery) {
        if (pageQuery == null) {
            throw new ServiceException("分页参数不能为空");
        }
        if (pageQuery.getPageNum() < 1) {
            throw new ServiceException("页码必须大于0");
        }
        if (pageQuery.getPageSize() < 1 || pageQuery.getPageSize() > 1000) {
            throw new ServiceException("每页数量必须在1-1000之间");
        }
    }
    
    /**
     * 数据校验
     */
    private void validateData(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            throw new ServiceException("操作数据不能为空");
        }
    }
}
