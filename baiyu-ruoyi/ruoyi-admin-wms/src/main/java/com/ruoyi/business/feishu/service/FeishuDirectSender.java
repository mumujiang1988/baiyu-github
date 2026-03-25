package com.ruoyi.business.feishu.service;

import cn.hutool.http.HttpRequest;
import com.ruoyi.business.util.FeishuTokenUtil;
import com.ruoyi.common.json.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Feishu direct message sender (no buffering)
 */
@Service
public class FeishuDirectSender {

    @Autowired
    private FeishuTokenUtil tokenUtil;

    public void sendTextDirect(String chatId, String text) throws Exception {
        String token = tokenUtil.getTenantAccessToken();

        Map<String, Object> body = Map.of(
            "receive_id", chatId,
            "msg_type", "text",
            "content", JsonUtils.toJsonString(Map.of("text", text))
        );

        String url = "https://open.feishu.cn/open-apis/im/v1/messages?receive_id_type=chat_id";

        HttpRequest.post(url)
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .body(JsonUtils.toJsonString(body))
            .execute();
    }
}
