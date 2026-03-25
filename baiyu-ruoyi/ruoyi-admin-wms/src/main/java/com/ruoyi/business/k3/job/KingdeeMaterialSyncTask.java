
package com.ruoyi.business.k3.job;


import com.ruoyi.business.entity.Bymaterial;
import com.ruoyi.business.k3.config.k3config;
import com.ruoyi.business.k3.service.KingdeeMaterialServicer;
import com.ruoyi.business.mapper.MaterialMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import static cn.dev33.satoken.SaManager.log;

@Slf4j
@Component
public class KingdeeMaterialSyncTask {

    @Resource
    private k3config k3configks;

    @Autowired
    private KingdeeMaterialServicer kingdeeMaterialServicer;
    @Autowired
    private MaterialMapper materialMapper;

    /**
     * 每天凌晨2点执行金蝶物料同步
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void syncKingdeeMaterials() {
        long startTime = System.currentTimeMillis();
        try {
            log.info("开始执行金蝶物料同步任务");

            List<List<Object>> materialList = k3configks.queryMaterialList();
            log.info("从金蝶获取到 {} 条物料数据", materialList.size());

            List<List<Object>> queryProductCategories = k3configks.queryMaterialList();
            kingdeeMaterialServicer.queryMaterialList(materialList, queryProductCategories);

            long endTime = System.currentTimeMillis();
            log.info("金蝶物料同步任务执行完成，耗时: {} ms", endTime - startTime);
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("金蝶物料同步任务出现错误，耗时: {} ms", endTime - startTime, e);
        }
    }

    //填充物料英文描述
    @Scheduled(cron = "0 0 1 * * ?")
    public void fillMaterialEnglishDesc() {
        log.info("开始执行填充物料英文描述任务");
         List<Bymaterial> materials =  materialMapper.selectByConditionList();
         if (materials != null){
             for (Bymaterial material : materials) {
                 if (material.getDescription1() == null) {

                     materialMapper.updateByNumber(material);
                 }
             }
         }

        log.info("填充物料英文描述任务执行完成");
    }
}
