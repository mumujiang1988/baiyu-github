package com.ruoyi.erp.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.erp.service.ISuperDataPermissionService;
import com.ruoyi.erp.utils.SqlBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 通用数据权限查询服务实现类（纯 JDBC 架构）
 * 
 * 特性：
 * 1. 完全去除 QueryWrapper - 使用 SqlBuilder 直接构建 SQL
 * 2. 零正则替换 - 直接生成标准 JDBC 占位符
 * 3. SQL 注入防护 - 字段名白名单校验
 * 4. 异常细化 - 区分不同错误类型
 * 5. 性能优化 - 无正则匹配开销
 * 
 * @author JMH
 * @date 2026-03-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SuperDataPermissionServiceImpl implements ISuperDataPermissionService {

    private final JdbcTemplate jdbcTemplate;
    private final SqlBuilder sqlBuilder;

    // ==================== 分页查询（纯 JDBC 架构） ====================
    @Override
    public Page<Map<String, Object>> selectPageByModuleWithTableName(
            String moduleCode,
            String tableName,
            PageQuery pageQuery,
            Map<String, Object> queryConfig) {
        
        // 统一参数校验
        validateTableName(tableName);
        validatePageQuery(pageQuery);

        try {
            // 1. 提取查询条件
            List<Map<String, Object>> conditions = extractConditions(queryConfig);
            List<Map<String, Object>> orderBy = extractOrderBy(queryConfig);
            
            log.info("[模块编码] {}", moduleCode);
            log.info("[表名] {}", tableName);
            log.info("[WHERE 条件数量] {}", conditions != null ? conditions.size() : 0);
            log.info("[ORDER BY 数量] {}", orderBy != null ? orderBy.size() : 0);
            
            // 2. 使用 SqlBuilder 构建 SQL（完全去除 QueryWrapper）
            SqlBuilder.SqlResult whereResult = sqlBuilder.buildWhere(conditions);
            SqlBuilder.SqlResult orderByResult = sqlBuilder.buildOrderBy(orderBy);
            
            // 3. 构建完整 SQL
            StringBuilder selectSql = new StringBuilder("SELECT * FROM ");
            selectSql.append(tableName);
            selectSql.append(whereResult.getSql());
            selectSql.append(orderByResult.getSql());
            
            StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM ");
            countSql.append(tableName);
            countSql.append(whereResult.getSql());
            
            log.info("[查询 SQL] {}", selectSql);
            log.info("[计数 SQL] {}", countSql);
            log.info("[参数列表] {}", whereResult.getParams());
            
            // 4. 分页参数
            long pageNum = pageQuery.getPageNum();
            long pageSize = pageQuery.getPageSize();
            long offset = (pageNum - 1) * pageSize;
            
            // 5. 执行分页查询
            List<Object> pageParams = new ArrayList<>(whereResult.getParams());
            pageParams.add(pageSize);
            pageParams.add(offset);
            
            String pageSql = selectSql + " LIMIT ? OFFSET ?";
            log.debug("[分页 SQL] {}", pageSql);
            
            List<Map<String, Object>> records = jdbcTemplate.queryForList(
                pageSql, pageParams.toArray());
            
            // 6. 查询总数
            Long total = jdbcTemplate.queryForObject(
                countSql.toString(), 
                Long.class, 
                whereResult.getParams().toArray());
            
            // 7. 封装结果
            Page<Map<String, Object>> page = new Page<>(pageNum, pageSize, total);
            page.setRecords(records != null ? records : new ArrayList<>());
            
            log.info("[分页查询成功] module:{}, table:{}, total:{}", moduleCode, tableName, total);
            return page;
            
        } catch (BadSqlGrammarException e) {
            log.error("[SQL 语法错误] module:{}, sql 异常", moduleCode, e);
            throw new ServiceException("SQL 语法错误，请联系管理员检查配置：" + e.getMessage());
        } catch (Exception e) {
            log.error("[查询异常] module:{}", moduleCode, e);
            throw new ServiceException("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 从 queryConfig 中提取 conditions
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractConditions(Map<String, Object> queryConfig) {
        if (queryConfig == null || queryConfig.isEmpty()) {
            log.debug("queryConfig 为空，返回空条件列表");
            return new ArrayList<>();
        }
        
        Object conditionsObj = queryConfig.get("conditions");
        if (conditionsObj instanceof List) {
            return (List<Map<String, Object>>) conditionsObj;
        }
        
        log.warn("conditions 不是 List 类型，返回空列表");
        return new ArrayList<>();
    }
    
    /**
     * 从 queryConfig 中提取 orderBy
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractOrderBy(Map<String, Object> queryConfig) {
        if (queryConfig == null || queryConfig.isEmpty()) {
            return new ArrayList<>();
        }
        
        Object orderByObj = queryConfig.get("orderBy");
        if (orderByObj instanceof List) {
            return (List<Map<String, Object>>) orderByObj;
        }
        
        return new ArrayList<>();
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

    // ==================== 辅助方法 ====================

    /**
     * 验证表名（防 SQL 注入）
     */
    private void validateTableName(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new ServiceException("表名不能为空");
        }
        
        // 表名校验：只允许字母、数字、下划线
        if (!tableName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            throw new ServiceException("非法表名格式：" + tableName);
        }
        
        // 长度校验
        if (tableName.length() > 64) {
            throw new ServiceException("表名长度超过限制");
        }
    }
    
    /**
     * 验证分页参数
     */
    private void validatePageQuery(PageQuery pageQuery) {
        if (pageQuery == null) {
            throw new ServiceException("分页参数不能为空");
        }
        
        if (pageQuery.getPageNum() <= 0) {
            throw new ServiceException("页码必须大于 0");
        }
        
        if (pageQuery.getPageSize() <= 0 || pageQuery.getPageSize() > 1000) {
            throw new ServiceException("每页大小必须在 1-1000 之间");
        }
    }
    
    /**
     * 验证字段名（防 SQL 注入）
     */
    private void validateFieldName(String fieldName) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            throw new ServiceException("字段名不能为空");
        }
        
        // 格式校验：只允许字母、数字、下划线
        if (!fieldName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            throw new ServiceException("非法字段名格式：" + fieldName);
        }
        
        // 长度校验
        if (fieldName.length() > 64) {
            throw new ServiceException("字段名长度超过限制");
        }
    }
    
    /**
     * 验证数据
     */
    private void validateData(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            throw new ServiceException("操作数据不能为空");
        }
    }
}
