package com.ruoyi.business.feishu.service;

public interface FeishuMessageSender {
    void sendText(String chatId, String text);
    void sendTextDirect(String chatId, String text) throws Exception;
}
