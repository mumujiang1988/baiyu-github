package com.ruoyi.business.servicel;

import com.ruoyi.business.entity.AIConversation;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AIConversationHandler {
    /**
     * 处理用户问题
     * @param aiConversation 用户问题封装对象
     * @param emitter 流式输出回调
     */
    void handle(AIConversation aiConversation, SseEmitter emitter);
}
