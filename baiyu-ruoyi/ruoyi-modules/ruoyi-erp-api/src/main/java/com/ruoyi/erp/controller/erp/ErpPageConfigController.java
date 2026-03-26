package com.ruoyi.erp.controller.erp;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.json.utils.JsonUtils;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.web.core.BaseController;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.erp.domain.bo.ErpPageConfigBo;
import com.ruoyi.erp.domain.vo.ErpPageConfigVo;
import com.ruoyi.erp.domain.vo.ErpPageConfigHistoryVo;
import com.ruoyi.erp.service.ErpPageConfigService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * ERP 公共配置 信息操作处理
 *
 * @author ERP Development Team
 * @date 2026-03-22
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/erp/config")
public class ErpPageConfigController extends BaseController {

    private final ErpPageConfigService pageConfigService;

    /**
     * 查询配置列表
     */
    @SaCheckPermission("erp:config:list")
    @GetMapping("/list")
    public TableDataInfo<ErpPageConfigVo> list(ErpPageConfigBo bo, PageQuery pageQuery) {
        Page<ErpPageConfigVo> page = pageConfigService.selectPageList(bo, pageQuery);
        TableDataInfo<ErpPageConfigVo> info = new TableDataInfo<>();
        info.setRows(page.getRecords());
        info.setTotal(page.getTotal());
        return info;
    }

    /**
     * 获取配置详情
     *
     * @param configId 配置 ID
     */
    @SaCheckPermission("erp:config:query")
    @GetMapping("/{configId}")
    public R<ErpPageConfigVo> getInfo(@NotNull(message = "主键不能为空")
                                      @PathVariable Long configId) {
        return R.ok(pageConfigService.selectById(configId));
    }

    /**
     * 新增配置
     */
    @SaCheckPermission("erp:config:add")
    @Log(title = "ERP 公共配置", businessType = BusinessType.INSERT)
    @PostMapping()
    public R<Void> add(@Validated @RequestBody ErpPageConfigBo bo) {
        return toAjax(pageConfigService.insertByBo(bo));
    }

    /**
     * 修改配置
     */
    @SaCheckPermission("erp:config:edit")
    @Log(title = "ERP 公共配置", businessType = BusinessType.UPDATE)
    @PutMapping()
    public R<Void> edit(@Validated @RequestBody ErpPageConfigBo bo) {
        return toAjax(pageConfigService.updateByBo(bo));
    }

    /**
     * 删除配置
     *
     * @param configIds 配置 ID 串
     */
    @SaCheckPermission("erp:config:remove")
    @Log(title = "ERP 公共配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    public R<Void> remove(@PathVariable Long[] configIds) {
        return toAjax(pageConfigService.deleteByIds(configIds));
    }

    /**
     * 查询配置历史版本列表
     */
    @SaCheckPermission("erp:config:history")
    @GetMapping("/history/{configId}")
    public TableDataInfo<ErpPageConfigHistoryVo> historyList(
            @PathVariable Long configId, 
            PageQuery pageQuery) {
        log.info("[ErpPageConfigController] 查询配置历史，configId: {}", configId);
        Page<ErpPageConfigHistoryVo> page = pageConfigService.selectHistoryPage(configId, pageQuery);
        TableDataInfo<ErpPageConfigHistoryVo> info = new TableDataInfo<>();
        info.setRows(page.getRecords());
        info.setTotal(page.getTotal());
        return info;
    }

    /**
     * 查看版本详情
     */
    @SaCheckPermission("erp:config:history")
    @GetMapping("/history/{configId}/{version}")
    public R<ErpPageConfigHistoryVo> getVersionDetail(
            @PathVariable Long configId,
            @PathVariable Integer version) {
        log.info("[ErpPageConfigController] 查看版本详情，configId: {}, version: {}", configId, version);
        ErpPageConfigHistoryVo historyVo = pageConfigService.getVersionDetail(configId, version);
        return historyVo != null ? R.ok(historyVo) : R.fail("未找到该版本");
    }

    /**
     * 回滚到指定版本
     */
    @SaCheckPermission("erp:config:rollback")
    @Log(title = "配置回滚", businessType = BusinessType.UPDATE)
    @PostMapping("/rollback")
    public R<Void> rollback(@RequestBody Map<String, Object> params) {
        try {
            Long configId = Long.valueOf(params.get("configId").toString());
            Integer targetVersion = (Integer) params.get("targetVersion");
            String reason = (String) params.getOrDefault("reason", "手动回滚");
            
            log.info("[ErpPageConfigController] 配置回滚，configId: {}, targetVersion: {}, reason: {}", 
                configId, targetVersion, reason);
            
            pageConfigService.rollbackToVersion(configId, targetVersion, reason);
            return R.ok("回滚成功");
        } catch (Exception e) {
            log.error("[ErpPageConfigController] 配置回滚失败", e);
            return R.fail("回滚失败：" + e.getMessage());
        }
    }

    /**
     * 导出配置
     */
    @SaCheckPermission("erp:config:export")
    @Log(title = "配置导出", businessType = BusinessType.EXPORT)
    @GetMapping("/{id}/export")
    public void export(@PathVariable Long id, HttpServletResponse response) {
        try {
            log.info("[ErpPageConfigController] 导出配置，id: {}", id);
            pageConfigService.exportConfig(id, response);
        } catch (Exception e) {
            log.error("[ErpPageConfigController] 导出失败", e);
        }
    }

    /**
     * 导入配置
     */
    @SaCheckPermission("erp:config:import")
    @Log(title = "配置导入", businessType = BusinessType.IMPORT)
    @PostMapping("/import")
    public R<Void> upload(@RequestParam MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return R.fail("上传文件不能为空");
            }
            
            log.info("[ErpPageConfigController] 导入配置，文件名：{}", file.getOriginalFilename());
            pageConfigService.importConfig(file);
            return R.ok("导入成功");
        } catch (Exception e) {
            log.error("[ErpPageConfigController] 导入失败", e);
            return R.fail("导入失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除配置
     */
    @SaCheckPermission("erp:config:remove")
    @Log(title = "批量删除配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/batch")
    public R<Void> batchDelete(@RequestBody Long[] configIds) {
        if (configIds == null || configIds.length == 0) {
            return R.fail("请至少选择一个配置");
        }
        
        log.info("[ErpPageConfigController] 批量删除，ids: {}", java.util.Arrays.toString(configIds));
        pageConfigService.deleteByIds(configIds);
        return R.ok("删除成功");
    }

    /**
     * 复制配置
     */
    @SaCheckPermission("erp:config:copy")
    @Log(title = "复制配置", businessType = BusinessType.INSERT)
    @PostMapping("/{id}/copy")
    public R<ErpPageConfigVo> copy(@PathVariable Long id) {
        try {
            log.info("[ErpPageConfigController] 复制配置，id: {}", id);
            ErpPageConfigVo copiedConfig = pageConfigService.copyConfig(id);
            return R.ok(copiedConfig);
        } catch (Exception e) {
            log.error("[ErpPageConfigController] 复制失败", e);
            return R.fail("复制失败：" + e.getMessage());
        }
    }

    /**
     * 更新配置状态
     */
    @SaCheckPermission("erp:config:status")
    @Log(title = "更新配置状态", businessType = BusinessType.UPDATE)
    @PutMapping("/status")
    public R<Void> updateStatus(@RequestBody Map<String, Object> params) {
        try {
            Long configId = Long.valueOf(params.get("configId").toString());
            String status = (String) params.get("status");
            
            log.info("[ErpPageConfigController] 更新配置状态，configId: {}, status: {}", configId, status);
            pageConfigService.updateConfigStatus(configId, status);
            return R.ok("更新成功");
        } catch (Exception e) {
            log.error("[ErpPageConfigController] 更新状态失败", e);
            return R.fail("更新失败：" + e.getMessage());
        }
    }

    /**
     * 获取页面配置 (供业务页面使用)
     *
     * @param moduleCode 模块编码
     */
    @SaCheckPermission("erp:config:query")
    @GetMapping("/get/{moduleCode}")
    public R<Object> getPageConfig(@PathVariable String moduleCode) {
        log.info("🔍 [ErpPageConfigController] 请求页面配置，moduleCode: {}", moduleCode);
        String config = pageConfigService.getPageConfig(moduleCode);

        if (config == null) {
            log.error("❌ [ErpPageConfigController] 返回失败，未找到配置，moduleCode: {}", moduleCode);
            return R.fail("未找到配置");
        }

        log.info("✅ [ErpPageConfigController] 返回成功，moduleCode: {}, configLength: {}",
            moduleCode, config.length());

        // 打印配置内容的前 200 个字符用于调试
        String preview = config.length() > 200 ? config.substring(0, 200) + "..." : config;
        log.info("📝 [ErpPageConfigController] 配置预览：{}", preview);

        // 修复：将配置数据放在 data 字段中，而不是 msg 字段中
        // 使用 R.ok(String msg, T data) 方法，将配置字符串作为 data 字段
        return R.ok("获取配置成功", JsonUtils.parseMap(config));
    }

    /**
     * 获取详情页配置 (供抽屉页使用)
     *
     * @param moduleCode 模块编码
     */
    @SaCheckPermission("erp:config:query")
    @GetMapping("/detail/{moduleCode}")
    public R<Object> getDetailConfig(@PathVariable String moduleCode) {
        log.info("🔍 [ErpPageConfigController] 请求详情配置，moduleCode: {}", moduleCode);
        
        try {
            // 获取完整配置
            String config = pageConfigService.getPageConfig(moduleCode);
            if (config == null) {
                log.error("❌ [ErpPageConfigController] 未找到配置，moduleCode: {}", moduleCode);
                return R.fail("未找到配置");
            }
            
            // 解析 JSON 并提取 detail_config
            Map<String, Object> configMap = JsonUtils.parseMap(config);
            Object detailConfig = configMap.get("detailConfig");
            
            if (detailConfig == null) {
                log.warn("⚠️ [ErpPageConfigController] 未找到详情配置，moduleCode: {}", moduleCode);
                return R.ok("未找到详情配置", null);
            }
            
            log.info("✅ [ErpPageConfigController] 详情配置获取成功，moduleCode: {}", moduleCode);
            return R.ok("获取详情配置成功", detailConfig);
            
        } catch (Exception e) {
            log.error("❌ [ErpPageConfigController] 获取详情配置失败", e);
            return R.fail("获取失败：" + e.getMessage());
        }
    }
}
