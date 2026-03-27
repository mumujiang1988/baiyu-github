package com.ruoyi.erp.listener;

import com.ruoyi.erp.event.ConfigRefreshEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 配置刷新事件监听器
 * 
 * @author JMH
 * @date 2026-03-26
 */
@Slf4j
@Component
public class ConfigRefreshListener {
    
    /**
     * 监听配置刷新事件
     * 
     * @param event 配置刷新事件
     */
    @Async
    @EventListener(ConfigRefreshEvent.class)
    public void handleConfigRefresh(ConfigRefreshEvent event) {
        log.info("📢 收到配置刷新事件，moduleCode: {}, version: {}", 
            event.getModuleCode(), event.getVersion());
        
        // TODO: 可以在这里添加其他需要处理逻辑
        // 例如：
        // 1. 通知其他系统配置已变更
        // 2. 更新搜索引擎索引
        // 3. 触发配置审计日志
        // 4. 同步到其他节点
    }
}
