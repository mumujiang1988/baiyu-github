package com.ruoyi.erp.controller;
import com.ruoyi.common.core.constant.CacheNames;
import com.ruoyi.common.redis.utils.CacheUtils;
import com.ruoyi.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * ERP Cache Debugging Interface
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/erp/cache")
public class ErpCacheController {

    /**
     * Clear ERP configuration cache
     */
    @DeleteMapping("/clear/{moduleCode}")
    public R<Void> clearConfigCache(@PathVariable String moduleCode) {
        CacheUtils.evict(CacheNames.ERP_CONFIG, moduleCode);
        return R.ok("Cache cleared: " + moduleCode);
    }

    /**
     * Get cached value
     */
    @GetMapping("/get/{moduleCode}")
    public R<String> getConfigCache(@PathVariable String moduleCode) {
        String cacheValue = CacheUtils.get(CacheNames.ERP_CONFIG, moduleCode);
        return R.ok(cacheValue != null ? cacheValue : "Cache does not exist");
    }

    /**
     * Clear all ERP configuration caches
     */
    @DeleteMapping("/clear-all")
    public R<Void> clearAllConfigCache() {
        CacheUtils.clear(CacheNames.ERP_CONFIG);
        return R.ok("All ERP configuration caches cleared");
    }
}
