package com.ruoyi.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.mybatis.core.page.PageQuery;

import java.util.List;
import java.util.Map;

/**
 * 通用数据权限查询服务接口
 * 用于支持配置化页面的动态查询需求
 * 
 * @author ERP Development Team
 * @date 2026-03-23
 */
public interface ISuperDataPermissionService {

    /**
     * 根据模块编码和查询条件分页查询
     * 
     * @param moduleCode 模块编码（如：saleOrder, deliveryOrder 等）
     * @param pageQuery 分页参数
     * @param queryWrapper 查询条件包装器
     * @return 分页结果
     */
    Page<Map<String, Object>> selectPageByModule(
        String moduleCode,
        PageQuery pageQuery,
        QueryWrapper<Object> queryWrapper
    );

    /**
     * 根据模块编码和查询条件查询全部数据
     * 
     * @param moduleCode 模块编码
     * @param queryWrapper 查询条件包装器
     * @return 数据列表
     */
    List<Map<String, Object>> selectListByModule(
        String moduleCode,
        QueryWrapper<Object> queryWrapper
    );

    /**
     * 根据模块编码和 ID 查询单条数据
     * 
     * @param moduleCode 模块编码
     * @param id 主键 ID
     * @return 数据对象
     */
    Map<String, Object> selectById(
        String moduleCode,
        Long id
    );

    /**
     * 获取模块对应的表名
     * 
     * @param moduleCode 模块编码
     * @return 表名
     */
    String getTableNameByModuleCode(String moduleCode);
}
