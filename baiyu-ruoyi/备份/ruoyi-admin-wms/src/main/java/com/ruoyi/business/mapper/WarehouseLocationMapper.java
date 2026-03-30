package com.ruoyi.business.mapper;

import com.ruoyi.business.entity.WarehouseLocation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 仓库仓位 Mapper 接口
 */
public interface WarehouseLocationMapper {

    /**
     * 查询仓库仓位
     * @param id 主键
     * @return 仓库仓位
     */
    WarehouseLocation selectById(@Param("id") Long id);

    /**
     * 查询仓库仓位列表（分页）
     * @param page 起始行
     * @param pageSize 每页数量
     * @return 仓库仓位列表
     */
    List<WarehouseLocation> selectList(@Param("page") int page, @Param("pageSize") int pageSize);

    /**
     * 新增仓库仓位
     * @param warehouseLocation 仓库仓位
     * @return 影响行数
     */
    int insert(WarehouseLocation warehouseLocation);

    /**
     * 更新仓库仓位
     * @param warehouseLocation 仓库仓位
     * @return 影响行数
     */
    int update(WarehouseLocation warehouseLocation);

    /**
     * 删除仓库仓位
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量删除仓库仓位
     * @param ids 主键数组
     * @return 影响行数
     */
    int deleteByIds(@Param("ids") Long[] ids);

    /**
     * 根据仓库 ID 和仓位 ID 查询
     * @param locationId 仓位 ID
     * @return 仓库仓位
     */
    WarehouseLocation selectByWarehouseAndLocation( @Param("locationId") String locationId);
}
