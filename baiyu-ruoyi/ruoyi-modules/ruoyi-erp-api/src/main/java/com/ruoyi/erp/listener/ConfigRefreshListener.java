package com.ruoyi.erp.listener;

import com.ruoyi.erp.event.ConfigRefreshEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Configuration Refresh Event Listener
 * 
 * @author JMH
 * @date 2026-03-26
 */
@Slf4j
@Component
public class ConfigRefreshListener {
    
    /**
     * Listen to configuration refresh event
     * 
     * @param event Configuration refresh event
     */
    @Async
    @EventListener(ConfigRefreshEvent.class)
    public void handleConfigRefresh(ConfigRefreshEvent event) {
        log.info("📢 Received configuration refresh event, moduleCode: {}, version: {}", 
            event.getModuleCode(), event.getVersion());
        
        // TODO: Can add other processing logic here
        // For example:
        // 1. Notify other systems of configuration changes
        // 2. Update search engine index
        // 3. Trigger configuration audit log
        // 4. Synchronize to other nodes
    }
}
