package com.ruoyi.erp.controller;
import com.ruoyi.common.core.constant.CacheNames;
import com.ruoyi.common.redis.utils.CacheUtils;
import com.ruoyi.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * ERP 缓存调试接口
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/erp/cache")
public class ErpCacheController {

    /**
     * 清理 ERP 配置缓存
     */
    @DeleteMapping("/clear/{moduleCode}")
    public R<Void> clearConfigCache(@PathVariable String moduleCode) {
        CacheUtils.evict(CacheNames.ERP_CONFIG, moduleCode);
        return R.ok("缓存已清理：" + moduleCode);
    }

    /**
     * 获取缓存值
     */
    @GetMapping("/get/{moduleCode}")
    public R<String> getConfigCache(@PathVariable String moduleCode) {
        String cacheValue = CacheUtils.get(CacheNames.ERP_CONFIG, moduleCode);
        return R.ok(cacheValue != null ? cacheValue : "缓存不存在");
    }

    /**
     * 清理所有 ERP 配置缓存
     */
    @DeleteMapping("/clear-all")
    public R<Void> clearAllConfigCache() {
        CacheUtils.clear(CacheNames.ERP_CONFIG);
        return R.ok("所有 ERP 配置缓存已清理");
    }
}
