package com.ruoyi.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.erp.service.ISuperDataPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.*;

/**
 * 通用数据权限查询服务实现类
 * 用于支持配置化页面的动态查询需求
 * 
 * @author JMH
 * @date 2026-03-23
 */
@Slf4j
@Service
public class SuperDataPermissionServiceImpl implements ISuperDataPermissionService {

    @Resource
    private JdbcTemplate jdbcTemplate;

    /**
     * 支持动态表名的分页查询（唯一入口）
     * @param moduleCode 模块编码
     * @param tableName 表名（必填，来自 JSON 配置）
     * @param pageQuery 分页参数
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    @Override
    public Page<Map<String, Object>> selectPageByModuleWithTableName(
            String moduleCode,
            String tableName,
            PageQuery pageQuery,
            QueryWrapper<Object> queryWrapper) {
        
        try {
            //  tableName 为必填参数，来自前端 JSON 配置
            if (tableName == null || tableName.trim().isEmpty()) {
                throw new ServiceException("tableName 参数不能为空，请在 JSON 配置的 pageConfig.tableName 中配置表名");
            }
            
            log.info(" 使用 JSON 配置的表名，moduleCode: {}, tableName: {}", moduleCode, tableName);
            
            // 构建 SQL 和参数
            String sql = buildSelectSql(tableName, queryWrapper);
            String countSql = buildCountSql(tableName, queryWrapper);
            
            // 从 QueryWrapper 中提取参数值
            List<Object> sqlArgs = new ArrayList<>();
            if (queryWrapper != null) {
                try {
                    java.lang.reflect.Field field = queryWrapper.getClass().getDeclaredField("paramNameValuePairs");
                    field.setAccessible(true);
                    Map<String, Object> paramMap = (Map<String, Object>) field.get(queryWrapper);
                    if (paramMap != null) {
                        // 按顺序提取参数值（MPGENVAL1, MPGENVAL2, ...）
                        int i = 1;
                        while (true) {
                            String paramName = "MPGENVAL" + i;
                            if (paramMap.containsKey(paramName)) {
                                sqlArgs.add(paramMap.get(paramName));
                                i++;
                            } else {
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("提取 QueryWrapper 参数失败", e);
                }
            }
            
            // 分页参数
            int pageNum = pageQuery.getPageNum();
            int pageSize = pageQuery.getPageSize();
            int offset = (pageNum - 1) * pageSize;
            
            // 添加分页参数
            sqlArgs.add(pageSize);
            sqlArgs.add(offset);
            
            // 查询数据
            List<Map<String, Object>> records = jdbcTemplate.queryForList(
                sql + " LIMIT ? OFFSET ?", 
                sqlArgs.toArray()
            );
            
            // 查询总数
            Long total = jdbcTemplate.queryForObject(countSql, Long.class);
            
            // 构建分页结果
            Page<Map<String, Object>> page = new Page<>(pageNum, pageSize, total);
            page.setRecords(records);
            
            log.info("动态查询成功，moduleCode: {}, tableName: {}, total: {}", 
                moduleCode, tableName, total);
            
            return page;
            
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("动态查询失败，moduleCode: {}", moduleCode, e);
            throw new ServiceException("查询失败：" + e.getMessage());
        }
    }

    @Override
    public int insertByModuleWithTableName(
            String moduleCode,
            String tableName,
            Map<String, Object> data) {
        
        try {
            if (tableName == null || tableName.trim().isEmpty()) {
                throw new ServiceException("tableName 参数不能为空");
            }
            
            if (data == null || data.isEmpty()) {
                throw new ServiceException("数据不能为空");
            }
            
            // 构建 INSERT SQL
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO ").append(tableName).append(" (");
            
            // 获取字段名（排除 id）
            List<String> columns = new ArrayList<>();
            List<Object> values = new ArrayList<>();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String key = entry.getKey();
                if (!"id".equalsIgnoreCase(key)) {
                    columns.add(key);
                    values.add(entry.getValue());
                }
            }
            
            sql.append(String.join(", ", columns));
            sql.append(") VALUES (");
            sql.append(String.join(", ", Collections.nCopies(columns.size(), "?")));
            sql.append(")");
            
            log.info("执行插入操作，moduleCode: {}, tableName: {}, columns: {}", 
                moduleCode, tableName, columns.size());
            
            return jdbcTemplate.update(sql.toString(), values.toArray());
            
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("插入数据失败，moduleCode: {}", moduleCode, e);
            throw new ServiceException("插入失败：" + e.getMessage());
        }
    }

    @Override
    public int updateByModuleWithTableName(
            String moduleCode,
            String tableName,
            Map<String, Object> data) {
        
        try {
            if (tableName == null || tableName.trim().isEmpty()) {
                throw new ServiceException("tableName 参数不能为空");
            }
            
            if (data == null || data.isEmpty()) {
                throw new ServiceException("数据不能为空");
            }
            
            Object id = data.get("id");
            if (id == null) {
                throw new ServiceException("更新数据必须包含 id 字段");
            }
            
            // 构建 UPDATE SQL
            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE ").append(tableName).append(" SET ");
            
            // 获取字段名（排除 id）
            List<String> columns = new ArrayList<>();
            List<Object> values = new ArrayList<>();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String key = entry.getKey();
                if (!"id".equalsIgnoreCase(key)) {
                    columns.add(key);
                    values.add(entry.getValue());
                }
            }
            
            // 构建 SET 子句
            List<String> setClauses = new ArrayList<>();
            for (String column : columns) {
                setClauses.add(column + " = ?");
            }
            sql.append(String.join(", ", setClauses));
            sql.append(" WHERE id = ?");
            
            values.add(id);
            
            log.info("执行更新操作，moduleCode: {}, tableName: {}, id: {}", 
                moduleCode, tableName, id);
            
            return jdbcTemplate.update(sql.toString(), values.toArray());
            
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新数据失败，moduleCode: {}", moduleCode, e);
            throw new ServiceException("更新失败：" + e.getMessage());
        }
    }

    @Override
    public int deleteByModuleWithTableName(
            String moduleCode,
            String tableName,
            Object[] ids) {
        
        try {
            if (tableName == null || tableName.trim().isEmpty()) {
                throw new ServiceException("tableName 参数不能为空");
            }
            
            if (ids == null || ids.length == 0) {
                throw new ServiceException("删除的 ID 列表不能为空");
            }
            
            // 构建 DELETE SQL
            StringBuilder sql = new StringBuilder();
            sql.append("DELETE FROM ").append(tableName);
            sql.append(" WHERE id IN (");
            sql.append(String.join(", ", Collections.nCopies(ids.length, "?")));
            sql.append(")");
            
            log.info("执行删除操作，moduleCode: {}, tableName: {}, count: {}", 
                moduleCode, tableName, ids.length);
            
            return jdbcTemplate.update(sql.toString(), ids);
            
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除数据失败，moduleCode: {}", moduleCode, e);
            throw new ServiceException("删除失败：" + e.getMessage());
        }
    }

    /**
     * 构建 SELECT SQL
     */
    private String buildSelectSql(String tableName, QueryWrapper<Object> queryWrapper) {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + tableName);
        
        // 添加 WHERE 条件
        String whereClause = getWhereClause(queryWrapper);
        if (whereClause != null && !whereClause.isEmpty()) {
            sql.append(" WHERE ").append(whereClause);
        }
        
        // 添加 ORDER BY
        String orderByClause = getOrderByClause(queryWrapper);
        if (orderByClause != null && !orderByClause.isEmpty()) {
            sql.append(" ORDER BY ").append(orderByClause);
        }
        
        return sql.toString();
    }

    /**
     * 构建 COUNT SQL
     */
    private String buildCountSql(String tableName, QueryWrapper<Object> queryWrapper) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM " + tableName);
        
        // 添加 WHERE 条件
        String whereClause = getWhereClause(queryWrapper);
        if (whereClause != null && !whereClause.isEmpty()) {
            sql.append(" WHERE ").append(whereClause);
        }
        
        return sql.toString();
    }

    /**
     * 从 QueryWrapper 中提取 WHERE 子句
     * 注意：这里需要解析 MyBatis-Plus 的 QueryWrapper
     */
    @SuppressWarnings("unchecked")
    private String getWhereClause(QueryWrapper<Object> queryWrapper) {
        if (queryWrapper == null) {
            return null;
        }
        
        try {
            // 使用 MyBatis-Plus 提供的公共 API 获取 SQL 片段
            String whereSql = queryWrapper.getSqlSegment();
            log.info("通过 getSqlSegment 获取 WHERE 条件：{}", whereSql);
            
            if (whereSql != null && !whereSql.isEmpty()) {
                return whereSql;
            }
            
            // 如果 getSqlSegment 返回空，尝试反射获取 children 字段
            java.lang.reflect.Field field = null;
            Class<?> clazz = queryWrapper.getClass();
            
            // 尝试获取 children 字段（可能是父类的）
            while (clazz != null && field == null) {
                try {
                    field = clazz.getDeclaredField("children");
                } catch (NoSuchFieldException e) {
                    // 继续检查父类
                    clazz = clazz.getSuperclass();
                }
            }
            
            // 如果还是找不到，尝试其他可能的字段名
            if (field == null) {
                try {
                    field = queryWrapper.getClass().getDeclaredField("expressionSegments");
                } catch (NoSuchFieldException e2) {
                    log.warn("无法找到 QueryWrapper 的条件字段，跳过 WHERE 条件构建");
                    return null;
                }
            }
            
            field.setAccessible(true);
            List<Object> segments = (List<Object>) field.get(queryWrapper);
            
            if (segments == null || segments.isEmpty()) {
                return null;
            }
            
            // 构建 WHERE 子句
            StringBuilder whereClause = new StringBuilder();
            for (Object segment : segments) {
                if (segment != null) {
                    String segmentSql = segment.toString();
                    if (segmentSql != null && !segmentSql.isEmpty()) {
                        if (whereClause.length() > 0) {
                            whereClause.append(" AND ");
                        }
                        whereClause.append(segmentSql);
                    }
                }
            }
            
            return whereClause.length() > 0 ? whereClause.toString() : null;
            
        } catch (Exception e) {
            log.error("解析 QueryWrapper 失败", e);
            return null;
        }
    }

    /**
     * 从 QueryWrapper 中提取 ORDER BY 子句
     */
    @SuppressWarnings("unchecked")
    private String getOrderByClause(QueryWrapper<Object> queryWrapper) {
        if (queryWrapper == null) {
            return null;
        }
        
        try {
            // 获取排序字段
            java.lang.reflect.Field orderField = queryWrapper.getClass()
                .getDeclaredField("orderBy");
            orderField.setAccessible(true);
            
            List<String> orderBySegments = (List<String>) orderField.get(queryWrapper);
            
            if (orderBySegments == null || orderBySegments.isEmpty()) {
                return null;
            }
            
            return String.join(", ", orderBySegments);
            
        } catch (Exception e) {
            log.warn("解析 ORDER BY 失败", e);
            return null;
        }
    }
}
