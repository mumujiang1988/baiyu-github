package com.ruoyi.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.system.service.ISuperDataPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.*;

/**
 * 通用数据权限查询服务实现类
 * 用于支持配置化页面的动态查询需求
 * 
 * @author ERP Development Team
 * @date 2026-03-23
 */
@Slf4j
@Service
public class SuperDataPermissionServiceImpl implements ISuperDataPermissionService {

    @Resource
    private JdbcTemplate jdbcTemplate;

    /**
     * 模块编码与表名映射关系
     * 根据实际业务需要扩展
     */
    private static final Map<String, String> MODULE_TABLE_MAPPING = new HashMap<>();
    
    static {
        // 销售订单
        MODULE_TABLE_MAPPING.put("saleOrder", "t_sale_order");
        MODULE_TABLE_MAPPING.put("saleOrderEntry", "t_sale_order_entry");
        
        // 采购订单
        MODULE_TABLE_MAPPING.put("purchaseOrder", "t_purchase_order");
        MODULE_TABLE_MAPPING.put("purchaseOrderEntry", "t_purchase_order_entry");
        
        // 发货通知单
        MODULE_TABLE_MAPPING.put("deliveryOrder", "t_delivery_order");
        MODULE_TABLE_MAPPING.put("deliveryOrderEntry", "t_delivery_order_entry");
        
        // 出库单
        MODULE_TABLE_MAPPING.put("outboundOrder", "t_outbound_order");
        MODULE_TABLE_MAPPING.put("outboundOrderEntry", "t_outbound_order_entry");
        
        // 入库单
        MODULE_TABLE_MAPPING.put("inboundOrder", "t_inbound_order");
        MODULE_TABLE_MAPPING.put("inboundOrderEntry", "t_inbound_order_entry");
        
        // 库存管理
        MODULE_TABLE_MAPPING.put("inventory", "t_inventory");
        
        // 客户管理
        MODULE_TABLE_MAPPING.put("customer", "t_customer");
        
        // 供应商管理
        MODULE_TABLE_MAPPING.put("supplier", "t_supplier");
        
        // 物料管理
        MODULE_TABLE_MAPPING.put("material", "t_material");
    }

    @Override
    public Page<Map<String, Object>> selectPageByModule(
            String moduleCode,
            PageQuery pageQuery,
            QueryWrapper<Object> queryWrapper) {
        
        try {
            // 获取表名
            String tableName = getTableNameByModuleCode(moduleCode);
            
            // 构建 SQL
            String sql = buildSelectSql(tableName, queryWrapper);
            String countSql = buildCountSql(tableName, queryWrapper);
            
            // 分页参数
            int pageNum = pageQuery.getPageNum();
            int pageSize = pageQuery.getPageSize();
            int offset = (pageNum - 1) * pageSize;
            
            // 查询数据
            List<Map<String, Object>> records = jdbcTemplate.queryForList(
                sql + " LIMIT ? OFFSET ?", 
                pageSize, 
                offset
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
    public List<Map<String, Object>> selectListByModule(
            String moduleCode,
            QueryWrapper<Object> queryWrapper) {
        
        try {
            String tableName = getTableNameByModuleCode(moduleCode);
            String sql = buildSelectSql(tableName, queryWrapper);
            
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
            
            log.info("动态查询列表成功，moduleCode: {}, tableName: {}, size: {}", 
                moduleCode, tableName, result.size());
            
            return result;
            
        } catch (Exception e) {
            log.error("动态查询列表失败，moduleCode: {}", moduleCode, e);
            throw new ServiceException("查询失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> selectById(String moduleCode, Long id) {
        
        try {
            String tableName = getTableNameByModuleCode(moduleCode);
            String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
            
            Map<String, Object> result = jdbcTemplate.queryForMap(sql, id);
            
            log.info("根据 ID 查询成功，moduleCode: {}, id: {}", moduleCode, id);
            
            return result;
            
        } catch (Exception e) {
            log.error("根据 ID 查询失败，moduleCode: {}, id: {}", moduleCode, id, e);
            throw new ServiceException("查询失败：" + e.getMessage());
        }
    }

    @Override
    public String getTableNameByModuleCode(String moduleCode) {
        String tableName = MODULE_TABLE_MAPPING.get(moduleCode);
        
        if (tableName == null || tableName.isEmpty()) {
            throw new ServiceException("未找到模块 [" + moduleCode + "] 对应的表名配置");
        }
        
        return tableName;
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
        
        // 获取 ExpressionSegment 集合
        try {
            java.lang.reflect.Field field = queryWrapper.getClass()
                .getDeclaredField("children");
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
            log.warn("解析 QueryWrapper 失败，使用默认条件", e);
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
