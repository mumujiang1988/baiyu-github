package com.ruoyi.business.util;

import cn.hutool.http.HttpRequest;
import com.ruoyi.common.json.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FeishuTokenUtil {

    @Value("${feishu.app-id}")
    private String appId;

    @Value("${feishu.app-secret}")
    private String appSecret;

    public String getTenantAccessToken() {
        String url = "https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal";
        String body = JsonUtils.toJsonString(Map.of(
            "app_id", appId,
            "app_secret", appSecret
        ));

        String resp = HttpRequest.post(url)
            .header("Content-Type", "application/json")
            .body(body)
            .execute()
            .body();

        return (String) JsonUtils.parseMap(resp).get("tenant_access_token");
    }
}
