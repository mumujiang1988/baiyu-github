package com.ruoyi.business.servicel.impl;


import com.ruoyi.business.entity.AIConversation;
import com.ruoyi.business.servicel.AIConversationHandler;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


public class KnowledgeBaseHandler implements AIConversationHandler {

    @Override
    public void handle(AIConversation aiConversation, SseEmitter emitter) {
        try {
            String question = aiConversation.getUserQuestion();
            if (question == null || question.isEmpty()) {
                emitter.send(SseEmitter.event().data("用户问题不能为空"));
                emitter.complete();
                return;
            }

            String knowledge = retrieveKnowledgeFromBase(aiConversation.getKnowledgeBaseName(), question);
            String prompt = buildPromptWithKnowledge(question, knowledge);

            // 模拟大模型流式输出
            for (int i = 1; i <= 3; i++) {
                emitter.send(SseEmitter.event().data("知识库问答输出片段 " + i + ": " + prompt));
                Thread.sleep(500);
            }

            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    private String retrieveKnowledgeFromBase(String baseName, String question) {
        // 模拟知识库检索
        return "相关知识内容";
    }

    private String buildPromptWithKnowledge(String question, String knowledge) {
        return "结合知识回答: " + question + " | 知识: " + knowledge;
    }
}
