package com.ruoyi.business.servicel.impl;

import com.ruoyi.business.entity.AIConversation;
import com.ruoyi.business.mapper.AIConversationMapper;
import com.ruoyi.business.servicel.AIConversationHandler;
import com.ruoyi.business.util.MinioUtil;
import com.ruoyi.business.util.QwenModelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@Component
public class FileIdentificationHandler implements AIConversationHandler {


    @Autowired
    private MinioUtil minioUtil;
    @Autowired
    private QwenModelUtil qwenModelUtil;

    @Autowired
    private AIConversationMapper aiConversationMapper;
    @Override
    public void handle(AIConversation aiConversation, SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().data("正在识别图片..."));

            // 检查文件是否为空
            if (aiConversation.getFile() == null || aiConversation.getFile().isEmpty()) {
                emitter.send(SseEmitter.event().data("未上传图片文件"));
                emitter.complete();
                return;
            }

          String imageUrl=  minioUtil.uploadFile(aiConversation.getFile());

            // Step 3: 调用大模型 OCR / 视觉接口
            String prompt = aiConversation.getUserQuestion() != null ?
                    aiConversation.getUserQuestion() : "请识别图片内容";


            // Step 4: 解析模型输出并返回前端（流式输出）
            // 使用流式输出方式处理OCR识别
            qwenModelUtil.ocrImageStream(imageUrl, prompt)
                    .doOnNext(data -> {
                        // 收集AI响应数据，用于后续保存到数据库
                        StringBuilder responseBuilder = new StringBuilder();
                        responseBuilder.append(data);
                        aiConversation.setAiResponse(responseBuilder.toString());
                    })
                    .doOnComplete(() -> {
                        try {
                            saveConversationLog(aiConversation);
                            emitter.send(SseEmitter.event().data("[识别完成]"));
                            emitter.complete();
                        } catch (Exception ignored) {}
                    })
                    .doOnError(throwable -> {
                        try {

                            emitter.send(SseEmitter.event().data("识别失败：" + throwable.getMessage()));
                            emitter.complete();
                        } catch (Exception ignored) {}
                    })
                    .subscribe(data -> {
                        try {
                            emitter.send(SseEmitter.event().data(data));
                        } catch (Exception ignored) {}
                    });

        } catch (Exception e) {
            try {
                emitter.send(SseEmitter.event().data("识别失败：" + e.getMessage()));
                emitter.complete();
            } catch (Exception ignored) {}
        }
    }

    /**
     * 保存对话记录到数据库
     * @param aiConversation 用户对话对象
     */
    private void saveConversationLog(AIConversation aiConversation) {
        try {
            // 保存到数据库
            aiConversationMapper.insert(aiConversation);
        } catch (Exception e) {
            // 日志保存失败不应影响主要功能，仅记录错误
            e.printStackTrace();
        }
    }
}
