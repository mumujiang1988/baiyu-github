package com.ruoyi.business.k3.service;

import com.ruoyi.business.entity.WarehouseLocation;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;

/**
 * 仓库仓位 Service 接口
 */
public interface WarehouseLocationService {

    /**
     * 查询仓库仓位
     * @param id 主键
     * @return 仓库仓位
     */
    WarehouseLocation selectById(Long id);

    /**
     * 查询仓库仓位列表（分页）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 仓库仓位列表
     */
    TableDataInfo<WarehouseLocation> selectList(int pageNum, int pageSize);

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
    int deleteById(Long id);

    /**
     * 批量删除仓库仓位
     * @param ids 主键数组
     * @return 影响行数
     */
    int deleteByIds(Long[] ids);

    /**
     * 同步金蝶仓库仓位数据
     * @return 同步数量
     */
    int syncWarehouseLocationData();
}
