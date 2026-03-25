package com.ruoyi.business.util;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MessageManager;

import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.Constants;
import com.ruoyi.business.entity.LlmRequest;
import io.reactivex.Flowable;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;



import java.util.*;

/**
 * 通义千问模型工具类
 * 用于与通义千问API进行交互
 */
@Component
public class QwenModelUtil {

    @Value("${spring.qwen.apikey}")
    private String apiKey;

    @Value("${spring.qwen.model-name}")
    private String modelName;

    private Generation generation;
    private MultiModalConversation multiModalConversation;
    @PostConstruct
    public void init() {
        this.generation = new Generation();
        // 设置API密钥
        if (apiKey != null && !apiKey.isEmpty()) {
            Constants.apiKey = apiKey;
        }
    }

    /**
     * 发送单轮对话请求
     *
     * @param prompt 用户输入的提示词
     * @return 模型回复内容
     * @throws ApiException API异常
     * @throws NoApiKeyException 缺少API密钥异常
     * @throws InputRequiredException 输入缺失异常
     */
    public String sendPrompt(String prompt)
            throws ApiException, NoApiKeyException, InputRequiredException {
        return sendPrompt(prompt, null);
    }

    /**
     * 发送带历史对话的多轮对话请求
     *
     * @param prompt 用户输入的提示词
     * @param history 历史对话记录
     * @return 模型回复内容
     * @throws ApiException API异常
     * @throws NoApiKeyException 缺少API密钥异常
     * @throws InputRequiredException 输入缺失异常
     */
    public String sendPrompt(String prompt, List<Message> history)
            throws ApiException, NoApiKeyException, InputRequiredException {

        MessageManager msgManager = new MessageManager(3); // 保留最近10条消息

        // 添加历史消息
        if (history != null && !history.isEmpty()) {
            for (Message msg : history) {
                msgManager.add(msg);
            }
        }

        // 添加当前用户消息
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(prompt)
                .build();
        msgManager.add(userMsg);

        // 构建请求参数
        GenerationParam param = GenerationParam.builder()
                .model(modelName)
                .messages(msgManager.get())
                .resultFormat("message")
                .build();

        // 发送请求并获取结果
        GenerationResult result = generation.call(param);
        return result.getOutput().getChoices().get(0).getMessage().getContent();
    }

    /**
     * 简单的文本生成方法
     *
     * @param prompt 提示词
     * @return 生成的文本内容
     */
    public String generateText(String prompt) {
        try {
            return sendPrompt(prompt);
        } catch (Exception e) {
            throw new RuntimeException("调用通义千问API失败: " + e.getMessage(), e);
        }
    }

    /**
     * 基于模板的文本生成
     *
     * @param template 模板文本，使用{0},{1}等作为占位符
     * @param args 填充模板的参数
     * @return 生成的文本内容
     */
    public String generateFromTemplate(String template, Object... args) {
        try {
            String prompt = formatTemplate(template, args);
            return sendPrompt(prompt);
        } catch (Exception e) {
            throw new RuntimeException("基于模板生成文本失败: " + e.getMessage(), e);
        }
    }

    /**
     * 格式化模板字符串
     *
     * @param template 模板字符串
     * @param args 参数数组
     * @return 格式化后的字符串
     */
    private String formatTemplate(String template, Object... args) {
        for (int i = 0; i < args.length; i++) {
            template = template.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return template;
    }

    /**
     * 设置API密钥
     *
     * @param apiKey API密钥
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        Constants.apiKey = apiKey;
    }

    /**
     * 设置模型名称
     *
     * @param modelName 模型名称
     */
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    /**
     * 构建简单的消息历史
     *
     * @param userMessage 用户消息
     * @param assistantMessage 助手回复
     * @return 消息列表
     */
    public List<Message> buildSimpleHistory(String userMessage, String assistantMessage) {
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(userMessage)
                .build();

        Message assistantMsg = Message.builder()
                .role(Role.ASSISTANT.getValue())
                .content(assistantMessage)
                .build();

        return Arrays.asList(userMsg, assistantMsg);
    }
    /**
     * 流式输出大模型对话 (使用WebFlux)
     * @param request 请求参数
     * @return Flux<String> 响应式流
     */
    public Flux<String> sendPromptStream(LlmRequest request, String prompt ) {
        if (request.getApiKey() != null && !request.getApiKey().isEmpty()) {
            Constants.apiKey = request.getApiKey();
        }

        String usedModel = (request.getModel() != null && !request.getModel().isEmpty())
                ? request.getModel()
                : this.modelName;

        //消息列表最长5轮
        MessageManager msgManager = new MessageManager(5);
        msgManager.add(Message.builder()
                .role(Role.USER.getValue())
                .content(request.getPrompt())
                .build());

        GenerationParam param = GenerationParam.builder()
                .prompt(prompt)
                .model(usedModel)
                .messages(msgManager.get())
                .resultFormat("message")
                .incrementalOutput(true) // 启用流式输出
                .temperature(0.5f)  // 更确定性的输出
                //.topP(0.9f)         // 更多样化的输出
                .maxTokens(500)    // 更长的回复
                .enableSearch(false) // 禁用搜索功能，避免搜索结果触发内容过滤
                .build();


        return Flux.<String>create(sink -> {
                    try {
                        Flowable<GenerationResult> flowable = generation.streamCall(param);

                        flowable.subscribe(
                                result -> {
                                    if (result == null || result.getOutput() == null) return;
                                    var choices = result.getOutput().getChoices();
                                    if (choices == null || choices.isEmpty()) return;
                                    String content = choices.get(0).getMessage().getContent();
                                    if (content != null && !content.isEmpty()) {
                                        sink.next(content); // 🚀 每个chunk立即发出
                                    }
                                },
                                sink::error,
                                sink::complete
                        );
                    } catch (Exception e) {
                        sink.error(e);
                    }
                })
                // 关键：异步调度，防止阻塞，保证立刻推送

                .publishOn(Schedulers.boundedElastic());
    }

    /**
     * 流式输出对话解析文件
     * */
    /**
     * 流式输出OCR识别图片内容
     * @param imageUrl 图片URL地址
     * @param prompt 提示词
     * @return Flux<String> 响应式流
     */
    public Flux<String> ocrImageStream(String imageUrl, String prompt) {
        // 设置API密钥
        if (apiKey != null && !apiKey.isEmpty()) {
            Constants.apiKey = apiKey;
        }

        return Flux.<String>create(sink -> {
                    try {
                        Map<String, Object> imageContent = new HashMap<>();
                        imageContent.put("image", imageUrl);

                        Map<String, Object> textContent = new HashMap<>();
                        textContent.put("text", prompt != null && !prompt.isEmpty() ? prompt : "请识别图片内容");

                        MultiModalMessage message = MultiModalMessage.builder()
                                .role(Role.USER.getValue())
                                .content(Arrays.asList(imageContent, textContent))
                                .build();

                        MultiModalConversationParam param = MultiModalConversationParam.builder()
                                .model("qwen-VL-Max")
                                .messages(Collections.singletonList(message))
                                .build();

                        Flowable<MultiModalConversationResult> flowable = multiModalConversation.streamCall(param);

                        flowable.subscribe(
                                result -> {
                                    if (result == null || result.getOutput() == null) return;
                                    var choices = result.getOutput().getChoices();
                                    if (choices == null || choices.isEmpty()) return;
                                    String content = choices.get(0).getMessage().getContent().toString();
                                    if (content != null && !content.isEmpty()) {
                                        sink.next(content);
                                    }
                                },
                                sink::error,
                                sink::complete
                        );
                    } catch (Exception e) {
                        sink.error(e);
                    }
                })
                .publishOn(Schedulers.boundedElastic());
    }
}
