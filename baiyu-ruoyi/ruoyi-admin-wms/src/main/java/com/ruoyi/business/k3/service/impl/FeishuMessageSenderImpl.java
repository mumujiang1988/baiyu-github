package com.ruoyi.business.k3.service.impl;


import com.ruoyi.business.Component.FeishuMessageBuffer;
import com.ruoyi.business.feishu.service.FeishuDirectSender;
import com.ruoyi.business.feishu.service.FeishuMessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeishuMessageSenderImpl implements FeishuMessageSender {

    @Autowired
    private FeishuMessageBuffer buffer;

    @Autowired
    private FeishuDirectSender directSender;

    @Override
    public void sendText(String chatId, String text) {
        buffer.add(chatId, text);
    }

    @Override
    public void sendTextDirect(String chatId, String text) throws Exception {
        directSender.sendTextDirect(chatId, text);
    }
}
