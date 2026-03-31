package com.ruoyi.erp.service.engine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 字典构建器引擎 - 构建器模式
 * 
 * @author JMH
 * @date 2026-03-25
 */
@Slf4j
@Component
public class DictionaryBuilderEngine {
    
    private final Map<String, DictionaryCache> dictionaryCache = new ConcurrentHashMap<>();
    
    private static final long DEFAULT_TTL = 5 * 60 * 1000;
    
    private static class DictionaryCache {
        List<Map<String, Object>> data;
        long timestamp;
        long ttl;
        boolean loaded;
        String error;
        
        public DictionaryCache(List<Map<String, Object>> data, long ttl) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
            this.ttl = ttl;
            this.loaded = true;
            this.error = null;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > ttl;
        }
    }
    
    public DictionaryBuilderEngine buildStatic(String name, List<Map<String, Object>> options) {
        dictionaryCache.put(name, new DictionaryCache(options, Long.MAX_VALUE));
        log.info(" 构建静态字典：{}, 共 {} 条", name, options.size());
        return this;
    }
    
    public DictionaryBuilderEngine buildDynamic(String name, Map<String, Object> config) {
        Map<String, Object> cacheConfig = new HashMap<>();
        cacheConfig.put("type", "dynamic");
        cacheConfig.putAll(config);
        
        dictionaryCache.put(name, new DictionaryCache(null, DEFAULT_TTL));
        log.info(" 构建动态字典：{}", name);
        return this;
    }
    
    public DictionaryBuilderEngine buildRemoteSearch(String name, Map<String, Object> config) {
        Map<String, Object> cacheConfig = new HashMap<>();
        cacheConfig.put("type", "remote");
        cacheConfig.putAll(config);
        
        dictionaryCache.put(name, new DictionaryCache(new ArrayList<>(), DEFAULT_TTL));
        log.info(" 构建远程搜索字典：{}", name);
        return this;
    }
    
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> load(String name, DictionaryLoader loader) {
        DictionaryCache cache = dictionaryCache.get(name);
        
        if (cache == null) {
            log.warn(" 字典不存在：{}", name);
            return new ArrayList<>();
        }
        
        if (cache.data != null && !cache.isExpired()) {
            log.debug("💾 使用缓存字典：{}", name);
            return cache.data;
        }
        
        try {
            log.info("🌐 加载字典：{}", name);
            List<Map<String, Object>> data = loader.load();
            
            dictionaryCache.put(name, new DictionaryCache(data, cache.ttl));
            log.info(" 字典加载成功：{}, 共 {} 条", name, data.size());
            
            return data;
        } catch (Exception e) {
            log.error("字典加载失败：{}", name, e);
            cache.error = e.getMessage();
            return new ArrayList<>();
        }
    }
    
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> search(String name, String keyword, DictionarySearcher searcher) {
        DictionaryCache cache = dictionaryCache.get(name);
        
        if (cache == null) {
            log.warn(" 字典不存在：{}", name);
            return new ArrayList<>();
        }
        
        try {
            log.info("搜索字典：{}, 关键词：{}", name, keyword);
            List<Map<String, Object>> data = searcher.search(keyword);
            
            dictionaryCache.put(name, new DictionaryCache(data, cache.ttl));
            log.info(" 字典搜索成功：{}, 共 {} 条", name, data.size());
            
            return data;
        } catch (Exception e) {
            log.error("字典搜索失败：{}", name, e);
            return new ArrayList<>();
        }
    }
    
    public List<Map<String, Object>> get(String name) {
        DictionaryCache cache = dictionaryCache.get(name);
        if (cache == null || cache.data == null) {
            return new ArrayList<>();
        }
        return cache.data;
    }
    
    public void clear(String name) {
        DictionaryCache cache = dictionaryCache.get(name);
        if (cache != null) {
            cache.loaded = false;
            cache.data = null;
            cache.timestamp = 0;
            log.info("已清除字典缓存：{}", name);
        }
    }
    
    public void clearAll() {
        dictionaryCache.forEach((name, cache) -> {
            cache.loaded = false;
            cache.data = null;
            cache.timestamp = 0;
        });
        log.info("已清除所有字典缓存");
    }
    
    public Map<String, Object> getStatus(String name) {
        DictionaryCache cache = dictionaryCache.get(name);
        Map<String, Object> status = new HashMap<>();
        
        if (cache == null) {
            status.put("exists", false);
            return status;
        }
        
        status.put("exists", true);
        status.put("loaded", cache.loaded);
        status.put("expired", cache.isExpired());
        status.put("size", cache.data != null ? cache.data.size() : 0);
        status.put("error", cache.error);
        
        return status;
    }
    
    public Map<String, Map<String, Object>> getAllStatus() {
        Map<String, Map<String, Object>> allStatus = new HashMap<>();
        
        dictionaryCache.forEach((name, cache) -> {
            Map<String, Object> status = new HashMap<>();
            status.put("loaded", cache.loaded);
            status.put("expired", cache.isExpired());
            status.put("size", cache.data != null ? cache.data.size() : 0);
            status.put("error", cache.error);
            
            allStatus.put(name, status);
        });
        
        return allStatus;
    }
    
    @FunctionalInterface
    public interface DictionaryLoader {
        List<Map<String, Object>> load() throws Exception;
    }
    
    @FunctionalInterface
    public interface DictionarySearcher {
        List<Map<String, Object>> search(String keyword) throws Exception;
    }
}
