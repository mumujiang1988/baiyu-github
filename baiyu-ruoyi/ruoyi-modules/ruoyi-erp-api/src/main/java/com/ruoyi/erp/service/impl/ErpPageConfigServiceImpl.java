package com.ruoyi.erp.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.constant.CacheNames;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.MapstructUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.json.utils.JsonUtils;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.redis.utils.CacheUtils;
import com.ruoyi.erp.domain.bo.ErpPageConfigBo;
import com.ruoyi.erp.domain.entity.ErpPageConfig;
import com.ruoyi.erp.domain.entity.ErpPageConfigHistory;
import com.ruoyi.erp.domain.vo.ErpPageConfigVo;
import com.ruoyi.erp.domain.vo.ErpPageConfigHistoryVo;
import com.ruoyi.erp.event.ConfigRefreshEvent;
import com.ruoyi.erp.mapper.ErpPageConfigMapper;
import com.ruoyi.erp.mapper.ErpPageConfigHistoryMapper;
import com.ruoyi.erp.service.ErpPageConfigService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ERP 公共配置 Service 业务层实现
 * 
 * @author JMH
 * @date 2026-03-22
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ErpPageConfigServiceImpl implements ErpPageConfigService {

    private final ErpPageConfigMapper pageConfigMapper;
    private final ErpPageConfigHistoryMapper historyMapper;
    
    // 添加事件发布器
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public ErpPageConfigVo selectById(Long configId) {
        return pageConfigMapper.selectVoById(configId);
    }

    @Override
    public List<ErpPageConfigVo> selectList(ErpPageConfigBo bo) {
        LambdaQueryWrapper<ErpPageConfig> lqw = buildQueryWrapper(bo);
        return pageConfigMapper.selectVoList(lqw);
    }

    @Override
    public Page<ErpPageConfigVo> selectPageList(ErpPageConfigBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ErpPageConfig> lqw = buildQueryWrapper(bo);
        Page<ErpPageConfigVo> result = pageConfigMapper.selectVoPage(pageQuery.build(), lqw);
        return result;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<ErpPageConfig> buildQueryWrapper(ErpPageConfigBo bo) {
        LambdaQueryWrapper<ErpPageConfig> lqw = Wrappers.lambdaQuery();
        
        lqw.eq(StringUtils.isNotBlank(bo.getModuleCode()), 
            ErpPageConfig::getModuleCode, bo.getModuleCode());
        lqw.like(StringUtils.isNotBlank(bo.getConfigName()), 
            ErpPageConfig::getConfigName, bo.getConfigName());
        lqw.eq(StringUtils.isNotBlank(bo.getConfigType()), 
            ErpPageConfig::getConfigType, bo.getConfigType());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), 
            ErpPageConfig::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getIsPublic()), 
            ErpPageConfig::getIsPublic, bo.getIsPublic());
        lqw.eq(ObjectUtil.isNotNull(bo.getParentConfigId()), 
            ErpPageConfig::getParentConfigId, bo.getParentConfigId());
        
        return lqw;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertByBo(ErpPageConfigBo bo) {
        // 检查模块编码 + 配置类型是否唯一
        Long count = pageConfigMapper.selectCount(new LambdaQueryWrapper<ErpPageConfig>()
            .eq(ErpPageConfig::getModuleCode, bo.getModuleCode())
            .eq(ErpPageConfig::getConfigType, bo.getConfigType()));
        
        if (count > 0) {
            throw new ServiceException("该模块编码和配置类型已存在");
        }
        
        ErpPageConfig config = MapstructUtils.convert(bo, ErpPageConfig.class);
        config.setVersion(1);
        int row = pageConfigMapper.insert(config);
        
        if (row > 0) {
            // 清除缓存
            CacheUtils.evict(CacheNames.ERP_CONFIG, config.getModuleCode());
        }
        
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateByBo(ErpPageConfigBo bo) {
        ErpPageConfig config = MapstructUtils.convert(bo, ErpPageConfig.class);
        
        // 版本号 +1
        Integer newVersion = bo.getVersion() + 1;
        config.setVersion(newVersion);
        
        int row = pageConfigMapper.updateById(config);
        
        if (row > 0) {
            // 记录历史版本
            recordHistory(config, bo.getChangeReason());
            
            // 优化 1：主动清除缓存（而不是等待 TTL）
            CacheUtils.evict(CacheNames.ERP_CONFIG, config.getModuleCode());
            log.info("已清除配置缓存，moduleCode: {}, version: {}", 
                config.getModuleCode(), newVersion);
            
            // 优化 2：广播刷新事件（供其他模块监听处理）
            try {
                eventPublisher.publishEvent(
                    new ConfigRefreshEvent(this, config.getModuleCode(), newVersion)
                );
                log.info("已广播配置刷新事件，moduleCode: {}", config.getModuleCode());
            } catch (Exception e) {
                log.warn("广播配置刷新事件失败，但不影响主流程", e);
            }
        }
        
        return row;
    }

    /**
     * 记录配置历史
     */
    private void recordHistory(ErpPageConfig config, String changeReason) {
        try {
            ErpPageConfigHistory history = new ErpPageConfigHistory();
            history.setConfigId(config.getConfigId());
            history.setModuleCode(config.getModuleCode());
            history.setConfigType(config.getConfigType());
            history.setVersion(config.getVersion()); 
            history.setPageConfig(config.getPageConfig());
            history.setFormConfig(config.getFormConfig());
            history.setTableConfig(config.getTableConfig());
            history.setSearchConfig(config.getSearchConfig());
            history.setActionConfig(config.getActionConfig());
            history.setDictConfig(config.getDictConfig());
            history.setBusinessConfig(config.getBusinessConfig());
            history.setChangeReason(changeReason);
            history.setChangeType("UPDATE");
            history.setCreateBy(config.getUpdateBy());
            
            historyMapper.insert(history);
            log.info("记录配置历史成功，configId: {}, version: {}", 
                config.getConfigId(), config.getVersion());
        } catch (Exception e) {
            log.error("记录配置历史失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(Long[] configIds) {
        if (ObjectUtil.isNotEmpty(configIds)) {
            for (Long configId : configIds) {
                ErpPageConfig config = pageConfigMapper.selectById(configId);
                if (ObjectUtil.isNotNull(config)) {
                    CacheUtils.evict(CacheNames.ERP_CONFIG, config.getModuleCode());
                }
            }
            return pageConfigMapper.deleteBatchIds(Arrays.asList(configIds));
        }
        return 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long configId) {
        ErpPageConfig config = pageConfigMapper.selectById(configId);
        if (ObjectUtil.isNotNull(config)) {
            CacheUtils.evict(CacheNames.ERP_CONFIG, config.getModuleCode());
            return pageConfigMapper.deleteById(configId);
        }
        return 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveWithVersion(ErpPageConfigBo bo) {
        if (ObjectUtil.isNull(bo.getConfigId())) {
            return insertByBo(bo);
        } else {
            return updateByBo(bo);
        }
    }

    @Override
    public String getPageConfig(String moduleCode) {
        // 先从缓存获取
        Object cached = CacheUtils.get(CacheNames.ERP_CONFIG, moduleCode);
        if (ObjectUtil.isNotNull(cached)) {
            log.info("[getPageConfig] 命中缓存，moduleCode: {}, cachedDataLength: {}",
                moduleCode, cached.toString().length());
            return cached.toString();
        }

        log.info("[getPageConfig] 缓存未命中，从数据库查询，moduleCode: {}", moduleCode);

        // 缓存未命中，从数据库查询
        ErpPageConfig config = pageConfigMapper.selectOne(new LambdaQueryWrapper<ErpPageConfig>()
            .eq(ErpPageConfig::getModuleCode, moduleCode)
            .eq(ErpPageConfig::getStatus, "1")
            .orderByDesc(ErpPageConfig::getVersion)
            .last("LIMIT 1"));

        if (ObjectUtil.isNull(config)) {
            log.warn("[getPageConfig] 未找到配置，moduleCode: {}", moduleCode);
            return null;
        }

        // 标准返回：直接组合JSON字符串，不做解析和序列化
        // 八字段强制拆分：pageConfig, formConfig, tableConfig, searchConfig, actionConfig, dictConfig, businessConfig, detailConfig
        String jsonString = String.format(
            "{\"pageConfig\":%s,\"formConfig\":%s,\"tableConfig\":%s,\"searchConfig\":%s,\"actionConfig\":%s,\"dictionaryConfig\":%s,\"businessConfig\":%s,\"detailConfig\":%s,\"moduleCode\":\"%s\",\"configName\":\"%s\",\"version\":%d}",
            config.getPageConfig(),
            config.getFormConfig(),
            config.getTableConfig(),
            config.getSearchConfig(),
            config.getActionConfig(),
            config.getDictConfig(),
            config.getBusinessConfig(),
            config.getDetailConfig(),
            escapeJson(config.getModuleCode()),
            escapeJson(config.getConfigName()),
            config.getVersion()
        );

        log.info("[getPageConfig] 数据库查询成功，configId: {}, moduleCode: {}, configName: {}, version: {}, status: {}, combinedJsonLength: {}",
            config.getConfigId(),
            config.getModuleCode(),
            config.getConfigName(),
            config.getVersion(),
            config.getStatus(),
            jsonString != null ? jsonString.length() : 0);

        // 放入缓存 (TTL: 1 小时 - 已在 CacheNames.ERP_CONFIG 中定义)
        CacheUtils.put(CacheNames.ERP_CONFIG, moduleCode, jsonString);
        log.info("[getPageConfig] 已放入缓存，moduleCode: {}", moduleCode);

        return jsonString;
    }

    /**
     * JSON字符串转义
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"");
    }

    @Override
    public ErpPageConfig getByModuleCode(String moduleCode) {
        log.debug("[getByModuleCode] 查询模块配置，moduleCode: {}", moduleCode);
        
        ErpPageConfig config = pageConfigMapper.selectOne(new LambdaQueryWrapper<ErpPageConfig>()
            .eq(ErpPageConfig::getModuleCode, moduleCode)
            .eq(ErpPageConfig::getStatus, "1")
            .orderByDesc(ErpPageConfig::getVersion)
            .last("LIMIT 1"));
        
        if (ObjectUtil.isNull(config)) {
            log.warn("[getByModuleCode] 未找到配置，moduleCode: {}", moduleCode);
            return null;
        }
        
        log.debug("[getByModuleCode] 查询成功，configId: {}, configName: {}, version: {}", 
            config.getConfigId(), config.getConfigName(), config.getVersion());
        
        return config;
    }

    @Override
    public Page<ErpPageConfigHistoryVo> selectHistoryPage(Long configId, PageQuery pageQuery) {
        log.info("[selectHistoryPage] 查询配置历史，configId: {}", configId);
        
        // 构建查询条件
        LambdaQueryWrapper<ErpPageConfigHistory> lqw = Wrappers.lambdaQuery();
        lqw.eq(ErpPageConfigHistory::getConfigId, configId)
           .orderByDesc(ErpPageConfigHistory::getVersion);
        
        // 分页查询
        Page<ErpPageConfigHistory> historyPage = historyMapper.selectPage(pageQuery.build(), lqw);
        
        // 转换为 VO
        Page<ErpPageConfigHistoryVo> voPage = new Page<>();
        voPage.setTotal(historyPage.getTotal());
        voPage.setRecords(MapstructUtils.convert(historyPage.getRecords(), ErpPageConfigHistoryVo.class));
        
        log.info("[selectHistoryPage] 查询成功，total: {}", historyPage.getTotal());
        return voPage;
    }

    @Override
    public ErpPageConfigHistoryVo getVersionDetail(Long configId, Integer version) {
        log.info("[getVersionDetail] 查询版本详情，configId: {}, version: {}", configId, version);
        
        // 查询指定版本
        ErpPageConfigHistory history = historyMapper.selectOne(
            Wrappers.lambdaQuery(ErpPageConfigHistory.class)
                .eq(ErpPageConfigHistory::getConfigId, configId)
                .eq(ErpPageConfigHistory::getVersion, version)
        );
        
        if (history == null) {
            log.warn("[getVersionDetail] 未找到版本，configId: {}, version: {}", configId, version);
            return null;
        }
        
        log.info("[getVersionDetail] 查询成功，historyId: {}", history.getHistoryId());
        return MapstructUtils.convert(history, ErpPageConfigHistoryVo.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackToVersion(Long configId, Integer targetVersion, String reason) {
        log.info("[rollbackToVersion] 开始回滚，configId: {}, targetVersion: {}, reason: {}", 
            configId, targetVersion, reason);
        
        try {
            // 1. 查询当前配置
            ErpPageConfig currentConfig = pageConfigMapper.selectById(configId);
            if (currentConfig == null) {
                throw new ServiceException("配置不存在");
            }
            
            // 2. 查询目标版本
            ErpPageConfigHistory targetVersionHistory = historyMapper.selectOne(
                Wrappers.lambdaQuery(ErpPageConfigHistory.class)
                    .eq(ErpPageConfigHistory::getConfigId, configId)
                    .eq(ErpPageConfigHistory::getVersion, targetVersion)
            );
            
            if (targetVersionHistory == null) {
                throw new ServiceException("目标版本不存在");
            }
            
            // 3. 更新配置内容为目标版本
            currentConfig.setPageConfig(targetVersionHistory.getPageConfig());
            currentConfig.setFormConfig(targetVersionHistory.getFormConfig());
            currentConfig.setTableConfig(targetVersionHistory.getTableConfig());
            currentConfig.setDictConfig(targetVersionHistory.getDictConfig());
            currentConfig.setBusinessConfig(targetVersionHistory.getBusinessConfig());
            currentConfig.setVersion(currentConfig.getVersion() + 1); // 版本号 +1
            
            int updateCount = pageConfigMapper.updateById(currentConfig);
            if (updateCount <= 0) {
                throw new ServiceException("更新配置失败");
            }
            
            // 4. 记录回滚历史
            ErpPageConfigHistory rollbackHistory = new ErpPageConfigHistory();
            rollbackHistory.setConfigId(configId);
            rollbackHistory.setModuleCode(currentConfig.getModuleCode());
            rollbackHistory.setConfigType(currentConfig.getConfigType());
            rollbackHistory.setVersion(currentConfig.getVersion());
            // 🔧 修复：设置 5 个独立的 JSON 字段
            rollbackHistory.setPageConfig(targetVersionHistory.getPageConfig());
            rollbackHistory.setFormConfig(targetVersionHistory.getFormConfig());
            rollbackHistory.setTableConfig(targetVersionHistory.getTableConfig());
            rollbackHistory.setDictConfig(targetVersionHistory.getDictConfig());
            rollbackHistory.setBusinessConfig(targetVersionHistory.getBusinessConfig());
            rollbackHistory.setChangeReason(reason != null ? reason : "回滚到版本 v" + targetVersion);
            rollbackHistory.setChangeType("ROLLBACK");
            rollbackHistory.setCreateBy(currentConfig.getUpdateBy());
            
            historyMapper.insert(rollbackHistory);
            
            // 5. 清除缓存
            CacheUtils.evict(CacheNames.ERP_CONFIG, currentConfig.getModuleCode());
            
            log.info("[rollbackToVersion] 回滚成功，newVersion: {}", currentConfig.getVersion());
        } catch (Exception e) {
            log.error("[rollbackToVersion] 回滚失败", e);
            throw new ServiceException("回滚失败：" + e.getMessage());
        }
    }

    @Override
    public void exportConfig(Long configId, HttpServletResponse response) {
        log.info("[exportConfig] 导出配置，configId: {}", configId);
        
        try {
            // 1. 查询配置详情
            ErpPageConfigVo config = selectById(configId);
            if (config == null) {
                throw new ServiceException("配置不存在");
            }
            
            // 2. 设置响应头
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + 
                java.net.URLEncoder.encode(config.getModuleCode() + "_config.json", "UTF-8"));
            
            // 3. 写入 JSON 数据
            // 🔧 修复：组合 5 个字段为 JSON 对象
            Map<String, Object> jsonContent = new HashMap<>();
            jsonContent.put("pageConfig", parseJsonString(config.getPageConfig()));
            jsonContent.put("formConfig", parseJsonString(config.getFormConfig()));
            jsonContent.put("tableConfig", parseJsonString(config.getTableConfig()));
            jsonContent.put("dictionaryConfig", parseJsonString(config.getDictConfig()));
            jsonContent.put("businessConfig", parseJsonString(config.getBusinessConfig()));
            response.getWriter().write(JsonUtils.toJsonString(jsonContent));
            response.getWriter().flush();
            
            log.info("[exportConfig] 导出成功，moduleCode: {}", config.getModuleCode());
        } catch (Exception e) {
            log.error("[exportConfig] 导出失败", e);
            throw new ServiceException("导出失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importConfig(MultipartFile file) {
        log.info("[importConfig] 导入配置，文件名：{}", file.getOriginalFilename());
        
        try {
            // 1. 读取文件内容
            String content = new String(file.getBytes(), "UTF-8");
            
            // 2. 解析 JSON 并验证格式
            Object parsedJson = JsonUtils.parseObject(content, Object.class);
            if (parsedJson == null) {
                throw new ServiceException("JSON 格式错误");
            }
            
            // 3. 生成模块编码（如果原文件中有则使用，否则生成新的）
            String moduleCode = "imported_" + System.currentTimeMillis();
            
            // 4. 构建配置对象
            ErpPageConfigBo bo = new ErpPageConfigBo();
            bo.setModuleCode(moduleCode);
            bo.setConfigName("导入的配置 - " + System.currentTimeMillis());
            bo.setConfigType("PAGE");
            // 🔧 修复：解析 JSON 并设置 5 个字段
            try {
                if (parsedJson instanceof Map) {
                    Map<String, Object> configMap = (Map<String, Object>) parsedJson;
                    bo.setPageConfig(JsonUtils.toJsonString(configMap.get("pageConfig")));
                    bo.setFormConfig(JsonUtils.toJsonString(configMap.get("formConfig")));
                    bo.setTableConfig(JsonUtils.toJsonString(configMap.get("tableConfig")));
                    bo.setDictConfig(JsonUtils.toJsonString(configMap.get("dictionaryConfig")));
                    bo.setBusinessConfig(JsonUtils.toJsonString(configMap.get("businessConfig")));
                }
            } catch (Exception e) {
                log.warn("解析导入的 JSON 失败，将使用原始字符串", e);
                // 如果解析失败，将整个内容作为 pageConfig
                bo.setPageConfig(content);
            }
            bo.setStatus("1");
            bo.setIsPublic("0");
            bo.setRemark("通过导入功能创建");
            
            // 5. 保存配置
            insertByBo(bo);
            
            log.info("[importConfig] 导入成功，moduleCode: {}", moduleCode);
        } catch (Exception e) {
            log.error("[importConfig] 导入失败", e);
            throw new ServiceException("导入失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpPageConfigVo copyConfig(Long configId) {
        log.info("[copyConfig] 复制配置，configId: {}", configId);
        
        try {
            // 1. 查询原配置
            ErpPageConfigVo originalConfig = selectById(configId);
            if (originalConfig == null) {
                throw new ServiceException("原配置不存在");
            }
            
            // 2. 构建新配置
            ErpPageConfigBo newConfig = new ErpPageConfigBo();
            newConfig.setModuleCode(originalConfig.getModuleCode() + "_copy_" + System.currentTimeMillis());
            newConfig.setConfigName(originalConfig.getConfigName() + " (副本)");
            newConfig.setConfigType(originalConfig.getConfigType());
            // 🔧 修复：复制 5 个字段
            newConfig.setPageConfig(originalConfig.getPageConfig());
            newConfig.setFormConfig(originalConfig.getFormConfig());
            newConfig.setTableConfig(originalConfig.getTableConfig());
            newConfig.setDictConfig(originalConfig.getDictConfig());
            newConfig.setBusinessConfig(originalConfig.getBusinessConfig());
            newConfig.setStatus("1");
            newConfig.setIsPublic("0");
            newConfig.setRemark("复制自配置 ID: " + configId);
            
            // 3. 保存新配置
            insertByBo(newConfig);
            
            log.info("[copyConfig] 复制成功，newModuleCode: {}", newConfig.getModuleCode());
            return selectById(newConfig.getConfigId());
        } catch (Exception e) {
            log.error("[copyConfig] 复制失败", e);
            throw new ServiceException("复制失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfigStatus(Long configId, String status) {
        log.info("[updateConfigStatus] 更新配置状态，configId: {}, status: {}", configId, status);
        
        try {
            // 1. 查询配置
            ErpPageConfig config = pageConfigMapper.selectById(configId);
            if (config == null) {
                throw new ServiceException("配置不存在");
            }
            
            // 2. 验证状态值
            if (!"0".equals(status) && !"1".equals(status)) {
                throw new ServiceException("状态值必须为 0 或 1");
            }
            
            // 3. 更新状态
            config.setStatus(status);
            int count = pageConfigMapper.updateById(config);
            
            if (count <= 0) {
                throw new ServiceException("更新状态失败");
            }
            
            // 4. 清除缓存
            CacheUtils.evict(CacheNames.ERP_CONFIG, config.getModuleCode());
            
            log.info("[updateConfigStatus] 更新成功，newStatus: {}", status);
        } catch (Exception e) {
            log.error("[updateConfigStatus] 更新失败", e);
            throw new ServiceException("更新失败：" + e.getMessage());
        }
    }

    /**
     * 解析 JSON 字符串为对象
     * 
     * @param jsonStr JSON 字符串
     * @return 解析后的对象，如果为空则返回 null
     */
    private Object parseJsonString(String jsonStr) {
        if (StringUtils.isEmpty(jsonStr)) {
            return null;
        }
        try {
            return JsonUtils.parseObject(jsonStr, Object.class);
        } catch (Exception e) {
            log.error("[parseJsonString] JSON 解析失败：{}", jsonStr, e);
            return null;
        }
    }
}
