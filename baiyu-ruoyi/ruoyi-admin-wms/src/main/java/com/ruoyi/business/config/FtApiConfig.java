package com.ruoyi.business.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 富通系统API配置
 */
@Component
@ConfigurationProperties(prefix = "ft.api")
public class FtApiConfig {

    /**
     * 富通系统API基础URL
     */
    private String baseUrl = "https://openapi.joinf.com";

    /**
     * 登录名
     */
    private String loginName;

    /**
     * 密钥
     */
    private String secretKey;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
