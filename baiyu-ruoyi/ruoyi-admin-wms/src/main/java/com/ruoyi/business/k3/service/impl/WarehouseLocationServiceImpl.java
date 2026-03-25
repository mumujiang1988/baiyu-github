package com.ruoyi.business.k3.service.impl;

import com.ruoyi.business.entity.WarehouseLocation;
import com.ruoyi.business.k3.config.Dictionaryconfig;
import com.ruoyi.business.k3.service.WarehouseLocationService;
import com.ruoyi.business.mapper.WarehouseLocationMapper;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 仓库仓位 Service 实现类
 */
@Service
@Slf4j
public class WarehouseLocationServiceImpl implements WarehouseLocationService {

    @Autowired
    private WarehouseLocationMapper warehouseLocationMapper;

    @Autowired
    private Dictionaryconfig dictionaryconfig;

    @Override
    public WarehouseLocation selectById(Long id) {
        return warehouseLocationMapper.selectById(id);
    }

    @Override
    public TableDataInfo<WarehouseLocation> selectList(int pageNum, int pageSize) {
        List<WarehouseLocation> list = warehouseLocationMapper.selectList(pageNum, pageSize);
        return TableDataInfo.build(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(WarehouseLocation warehouseLocation) {
        return warehouseLocationMapper.insert(warehouseLocation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(WarehouseLocation warehouseLocation) {
        return warehouseLocationMapper.update(warehouseLocation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id) {
        return warehouseLocationMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(Long[] ids) {
        return warehouseLocationMapper.deleteByIds(ids);
    }

    @Override
    public int syncWarehouseLocationData() {
        log.info("开始同步金蝶仓库仓位数据...");
        int processedCount = 0;

        try {
            // 一次性获取所有仓库仓位数据（不超过 500 条）
            List<List<Object>> pageData = dictionaryconfig.queryWarehousePositionList(0, 1000);

            log.info("开始处理 {} 条仓库仓位数据", pageData.size());

            // 解析页面数据为实体对象
            List<WarehouseLocation> pageLocations = parseWarehouseLocationPage(pageData);
            if (!pageLocations.isEmpty()) {
                // 批量处理新增和更新
                int insertCount = 0;
                int updateCount = 0;

                // 遍历每条记录，判断是新增还是更新
                for (WarehouseLocation location : pageLocations) {
                    // 检查是否已存在
                    WarehouseLocation existing = warehouseLocationMapper.selectByWarehouseAndLocation(location.getLocationId()
                    );

                    if (existing != null) {
                        // 存在则更新，设置 ID
                        location.setId(existing.getId());
                        warehouseLocationMapper.update(location);
                        updateCount++;
                    } else {
                        // 不存在则新增
                        warehouseLocationMapper.insert(location);
                        insertCount++;
                    }
                }

                int pageCount = insertCount + updateCount;
                processedCount += pageCount;
                log.debug("数据处理完成，新增 {} 条，更新 {} 条，总计 {} 条", insertCount, updateCount, pageCount);
            }

            log.info("仓库仓位同步完成，总计处理：{} 条", processedCount);
        } catch (Exception e) {
            log.error("同步仓库仓位异常", e);
            throw new RuntimeException(e);
        }

        return processedCount;
    }

    /**
     * 解析仓库仓位分页数据
     * @param pageData 分页数据
     * @return 仓库仓位列表
     */
    private List<WarehouseLocation> parseWarehouseLocationPage(List<List<Object>> pageData) {
        List<WarehouseLocation> locations = new ArrayList<>();

        if (pageData == null || pageData.isEmpty()) {
            return locations;
        }

        for (List<Object> row : pageData) {
            try {
                WarehouseLocation location = new WarehouseLocation();

                // 根据 K3 Cloud API 返回的字段顺序解析
                // FStockId, FNumber, FName, FFlexEntryId, FFlexEntryName
                if (row.size() > 0 && row.get(0) != null) {
                    location.setWarehouseId(row.get(0).toString());
                }

                if (row.size() > 1 && row.get(1) != null) {
                    location.setWarehouseNumber(row.get(1).toString());
                }
                if (row.size() > 2 && row.get(2) != null) {
                    location.setWarehouseName(row.get(2).toString());
                }
                if (row.size() > 3 && row.get(3) != null) {
                    location.setLocationId(row.get(3).toString());
                }
                if (row.size() > 4 && row.get(4) != null) {
                    location.setLocationName(row.get(4).toString());
                }

                locations.add(location);
            } catch (Exception e) {
                log.warn("解析仓库仓位数据失败：{}", row, e);
            }
        }

        return locations;
    }
}
