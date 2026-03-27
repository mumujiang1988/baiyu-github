package com.ruoyi.erp.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 配置刷新事件
 * 
 * @author JMH
 * @date 2026-03-26
 */
@Getter
public class ConfigRefreshEvent extends ApplicationEvent {
    
    /**
     * 模块编码
     */
    private final String moduleCode;
    
    /**
     * 新版本号
     */
    private final Integer version;
    
    /**
     * 构造函数
     * 
     * @param source 事件源
     * @param moduleCode 模块编码
     * @param version 新版本号
     */
    public ConfigRefreshEvent(Object source, String moduleCode, Integer version) {
        super(source);
        this.moduleCode = moduleCode;
        this.version = version;
    }
}
