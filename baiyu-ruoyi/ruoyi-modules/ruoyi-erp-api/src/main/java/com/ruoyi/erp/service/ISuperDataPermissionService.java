package com.ruoyi.erp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.mybatis.core.page.PageQuery;

import java.util.List;
import java.util.Map;

/**
 * 通用数据权限查询服务接口
 * 用于支持配置化页面的动态查询需求
 * 
 * @author JMH
 * @date 2026-03-23
 */
public interface ISuperDataPermissionService {

    /**
     * 根据模块编码、表名和查询条件分页查询（唯一入口）
     * 
     * @param moduleCode 模块编码（如：saleOrder, deliveryOrder 等）
     * @param tableName 表名（必填，来自 JSON 配置）
     * @param pageQuery 分页参数
     * @param queryConfig 查询配置（包含 conditions, orderBy 等）
     * @return 分页结果
     */
    Page<Map<String, Object>> selectPageByModuleWithTableName(
        String moduleCode,
        String tableName,
        PageQuery pageQuery,
        Map<String, Object> queryConfig
    );
    
    /**
     * 根据模块编码和表名新增数据
     * 
     * @param moduleCode 模块编码
     * @param tableName 表名（必填，来自 JSON 配置）
     * @param data 数据对象
     * @return 影响行数
     */
    int insertByModuleWithTableName(
        String moduleCode,
        String tableName,
        Map<String, Object> data
    );
    
    /**
     * 根据模块编码和表名修改数据
     * 
     * @param moduleCode 模块编码
     * @param tableName 表名（必填，来自 JSON 配置）
     * @param data 数据对象（必须包含 id 字段）
     * @return 影响行数
     */
    int updateByModuleWithTableName(
        String moduleCode,
        String tableName,
        Map<String, Object> data
    );
    
    /**
     * 根据模块编码和表名删除数据
     * 
     * @param moduleCode 模块编码
     * @param tableName 表名（必填，来自 JSON 配置）
     * @param ids 主键 ID 数组
     * @return 影响行数
     */
    int deleteByModuleWithTableName(
        String moduleCode,
        String tableName,
        Object[] ids
    );
}
