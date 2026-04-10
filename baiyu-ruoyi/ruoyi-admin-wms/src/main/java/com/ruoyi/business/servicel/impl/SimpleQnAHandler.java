package com.ruoyi.business.servicel.impl;



import com.ruoyi.business.entity.AIConversation;
import com.ruoyi.business.entity.AiLlmModer;
import com.ruoyi.business.entity.LlmRequest;
import com.ruoyi.business.mapper.AIConversationMapper;
import com.ruoyi.business.mapper.AiLlmModeMapper;
import com.ruoyi.business.servicel.AIConversationHandler;
import com.ruoyi.business.util.PromptTemplateUtil;
import com.ruoyi.business.util.QwenModelUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import reactor.core.Disposable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static cn.dev33.satoken.SaManager.log;


@Component
public class SimpleQnAHandler implements AIConversationHandler {

    @Resource
   private QwenModelUtil qwenModelUtil;
    @Autowired
    private PromptTemplateUtil promptTemplateUtil;

    @Autowired
    private AiLlmModeMapper aiLlmModeMapper;
    @Autowired
    private AIConversationMapper aiConversationMapper;

    private static final Logger logger = LoggerFactory.getLogger(SimpleQnAHandler.class);
    /**
     * 普通问答
     * */
    @Override
    public void handle(AIConversation aiConversation, SseEmitter emitter) {
        try {
            String question = aiConversation.getUserQuestion();
            if (question == null || question.isEmpty()) {
                emitter.send(SseEmitter.event().data("用户问题不能为空"));
                emitter.complete();
                return;
            }

            AiLlmModer aiLlmModer = aiLlmModeMapper.selectByTitle(aiConversation.getModelName());
            if (aiLlmModer == null) {
                emitter.send(SseEmitter.event().data("未找到模型：" + aiConversation.getModelName()));
                emitter.complete();
                return;
            }

            LlmRequest request = LlmRequest.builder()
                    .prompt(question)
                    .model(aiLlmModer.getLlmModel())
                    .apiKey(aiLlmModer.getLlmApiKey())
                    .build();



            AtomicReference<Disposable> subscriptionRef = new AtomicReference<>();
            // 用于收集AI响应的完整内容
            StringBuilder aiResponseBuilder = new StringBuilder();

            Disposable subscription = qwenModelUtil.sendPromptStream(request, promptTemplateUtil.getGeneralPrompt())
                    .doOnNext(list -> {
                        String combined = String.join("", list);
                        if (!combined.trim().isEmpty()) {
                            aiResponseBuilder.append(combined);
                            sendSSEData(emitter, combined, false);
                        }
                    })
                    .doOnComplete(() -> {
                        sendSSEData(emitter, "[DONE]", true);
                        //  流完成后再保存对话记录
                        saveConversationRecord(aiConversation, aiResponseBuilder.toString());
                    })
                    .doOnError(e -> sendSSEData(emitter, "调用异常：" + e.getMessage(), true))
                    .subscribe();

            subscriptionRef.set(subscription);

            emitter.onCompletion(() -> dispose(subscriptionRef));
            emitter.onTimeout(() -> dispose(subscriptionRef));
            emitter.onError(e -> dispose(subscriptionRef));

        } catch (Exception e) {
            sendSSEData(emitter, "系统异常：" + e.getMessage(), true);
        }
    }


    private void dispose(AtomicReference<Disposable> ref) {
        Disposable disposable = ref.get();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
    /**
     * 保存对话记录到数据库
     * @param aiConversation 用户对话对象
     * @param aiResponse AI响应内容
     */
    private void saveConversationRecord(AIConversation aiConversation, String aiResponse) {
        try {
            // 创建要保存的对话记录
            AIConversation conversationToSave = AIConversation.builder()
                    .userId(aiConversation.getUserId())
                    .sessionId(String.valueOf((int) (Math.random() * 900000 + 100000)))
                    .modelName(aiConversation.getModelName())
                    .businessType(aiConversation.getBusinessType())
                    .knowledgeBaseName(aiConversation.getKnowledgeBaseName())
                    .userQuestion(aiConversation.getUserQuestion())
                    .aiResponse(aiResponse) // 初始化为空字符串
                    .build();

            // 保存到数据库
            aiConversationMapper.insert(conversationToSave);
            logger.info(" 已保存对话记录: {}", conversationToSave.getSessionId());
        } catch (Exception e) {
            logger.error("保存对话记录失败", e);
        }
    }
    private void sendSSEData(SseEmitter emitter, String data, boolean complete) {
        try {
            if (complete) {
                emitter.send(SseEmitter.event().data(data));
                emitter.complete();
            } else {
                emitter.send(SseEmitter.event().data(data));
            }
        } catch (IllegalStateException e) {
            // emitter 已经完成，忽略这个异常
            logger.warn("SseEmitter already completed, ignoring data: " + data);
        } catch (IOException e) {
            if (complete) {
                try {
                    emitter.complete();
                } catch (IllegalStateException ignored) {
                    // emitter 已经完成，忽略
                }
            }
        }
    }

}
