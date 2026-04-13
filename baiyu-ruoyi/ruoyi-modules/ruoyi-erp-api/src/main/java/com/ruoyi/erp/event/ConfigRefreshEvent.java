package com.ruoyi.erp.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Configuration Refresh Event
 * 
 * @author JMH
 * @date 2026-03-26
 */
@Getter
public class ConfigRefreshEvent extends ApplicationEvent {
    
    /**
     * Module code
     */
    private final String moduleCode;
    
    /**
     * New version number
     */
    private final Integer version;
    
    /**
     * Constructor
     * 
     * @param source Event source
     * @param moduleCode Module code
     * @param version New version number
     */
    public ConfigRefreshEvent(Object source, String moduleCode, Integer version) {
        super(source);
        this.moduleCode = moduleCode;
        this.version = version;
    }
}
