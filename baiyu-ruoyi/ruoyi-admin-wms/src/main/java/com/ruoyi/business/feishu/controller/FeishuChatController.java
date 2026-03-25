package com.ruoyi.business.feishu.controller;

import cn.hutool.core.lang.Dict;
import com.alibaba.dashscope.common.Message;
import com.ruoyi.business.Component.FeishuMessageBuffer;
import com.ruoyi.business.Component.RedisService;
import com.ruoyi.business.feishu.service.FeishuMessageSender;
import com.ruoyi.business.util.QwenModelUtil;
import com.ruoyi.common.json.utils.JsonUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.*;

/**
 * ===============================================================
 *  飞书机器人消息事件接收控制器
 *
 *  功能：
 *   1. 接收飞书事件（url_verification / 消息事件）
 *   2. 校验 verification_token
 *   3. 解析用户单聊消息
 *   4. 执行命令（/help、/clear、/persona）
 *   5. 进入大模型（Qwen 或 ChatGPT）流式对话
 *   6. 使用 FeishuMessageBuffer 做节流推送消息
 *   7. 使用 Redis 保存上下文 + 人格设定
 *  说明：
 *   - 完整支持单聊模式
 *   - 完整支持大模型流式输出
 *   - 无循环依赖，结构完全解耦
 * ===============================================================
 */
@RestController
@RequestMapping("/feishu")
public class FeishuChatController {

    /** 飞书事件校验 token，来自飞书后台 */
    @Value("${feishu.verification_token}")
    private String verificationToken;

    /** 飞书应用 AppId */
    @Value("${feishu.app-id}")
    private String appId;

    /** 飞书应用 AppSecret */
    @Value("${feishu.app-secret}")
    private String appSecret;

    /** Redis 工具（用于存储上下文 + 人格） */
    @Autowired
    private RedisService redis;

    /** 大模型工具（支持 Qwen 流式输出） */
    @Autowired
    private QwenModelUtil qwenModelUtil;

    /** 发送消息接口（底层由 SenderImpl 实现） */
    @Autowired
    private FeishuMessageSender sender;

    /** 消息合并 + 节流组件（避免飞书限频） */
    @Autowired
    private FeishuMessageBuffer msgBuffer;


    /** 默认人格 */
    private static final String DEFAULT_PERSONA = "你是一个专业、有耐心的 AI 助手。";

    /** Redis：存储上下文的 key 前缀 */
    private static final String KEY_HISTORY = "feishu:history:";

    /** Redis：存储用户人格的 key 前缀 */
    private static final String KEY_PERSONA = "feishu:persona:";


    /**
     * ===============================================================
     *  飞书事件主入口
     *  飞书应用的事件订阅将所有事件推送到该接口
     *
     *  支持的事件类型：
     *    - url_verification：飞书验证服务器 Token
     *    - im.message.receive_v1：用户消息事件
     * ===============================================================
     */
    @PostMapping("/event")
    public Map<String, Object> receiveEvent(@RequestBody Map<String, Object> req) throws Exception {

        String type = (String) req.get("type");

        /** 1) 飞书验证服务器 URL，需要返回 challenge */
        if ("url_verification".equals(type)) {
            return Map.of("challenge", req.get("challenge"));
        }

        /** 2) 校验 token */
        Map<String, Object> header = (Map<String, Object>) req.get("header");
        if (!verificationToken.equals(header.get("token"))) {
            throw new RuntimeException("Invalid verification token");
        }

        /** 3) 处理飞书消息事件 */
        if ("im.message.receive_v1".equals(header.get("event_type"))) {

            Map<String, Object> event = (Map<String, Object>) req.get("event");

            /** chat_id：单聊 / 群聊的会话 ID */
            String chatId = (String) event.get("chat_id");

            /** open_id：用户的唯一标识（适用于单聊） */
            Map<String, Object> senderIdMap =
                (Map<String, Object>) ((Map<String, Object>) event.get("sender")).get("sender_id");

            String openId = (String) senderIdMap.get("open_id");

            /** message.content 是 JSON，需 parse */
            String contentJson = (String) ((Map<String, Object>) event.get("message")).get("content");
            Dict contentMap = JsonUtils.parseMap(contentJson);

            /** 用户实际输入文本 */
            String text = contentMap.getStr("text");

            System.out.println("收到: openId=" + openId + " | " + text);

            /** 如果是命令，则直接处理 */
            if (isCommand(text, openId, chatId)) return Map.of("status", "success");

            /** 执行 AI 流式对话 */
            processChatStream(openId, text, chatId);
        }

        return Map.of("status", "success");
    }



    // =======================================================================
    //  命令系统（支持 /help /clear /persona）
    // =======================================================================

    /**
     * 判断是否是命令，并执行命令逻辑
     */
    private boolean isCommand(String text, String openId, String chatId) {
        if (!text.startsWith("/")) return false;

        switch (text.trim()) {

            /** 帮助命令 */
            case "/help" -> sender.sendText(chatId,
                """
                可用命令：
                /help - 查看帮助
                /clear - 清除上下文
                /persona xxx - 设置人格
                """);

            /** 清空上下文 */
            case "/clear" -> {
                redis.del(KEY_HISTORY + openId);
                sender.sendText(chatId, "上下文已清除。");
            }

            default -> {

                /** 设置人格 */
                if (text.startsWith("/persona")) {
                    String persona = text.replace("/persona", "").trim();

                    if (persona.isEmpty()) {
                        sender.sendText(chatId, "用法：/persona 人格描述");
                    } else {
                        redis.set(KEY_PERSONA + openId, persona);
                        sender.sendText(chatId, "人格设定成功！");
                    }

                } else {
                    sender.sendText(chatId, "未知命令，输入 /help 查看帮助");
                }
            }
        }
        return true;
    }



    // =======================================================================
    //  AI 对话（流式）
    // =======================================================================

    /**
     * 将用户消息发送给大模型（流式输出）
     * 并通过 Buffer 进行节流合并推送
     */
    private void processChatStream(String openId, String input, String chatId) {

        /** 获取历史上下文 */
        List<Message> history = getHistory(openId);

        /** 获取用户人格 */
        String persona = getPersona(openId);

        /** 拼接 prompt */
        String fullPrompt = persona + "\n用户：" + input;

        /** 调用大模型（流式返回 Flux<String>） */
        Flux<String> stream = qwenModelUtil.sendPromptStream(null, fullPrompt);

        StringBuilder finalAnswer = new StringBuilder();

        /** 订阅流式输出 */
        stream.subscribe(
            delta -> {
                /** 每一段内容都追加到最后答案中 */
                finalAnswer.append(delta);

                /** 缓存片段到节流 Buffer */
                msgBuffer.add(chatId, delta);
            },
            error -> msgBuffer.add(chatId, "发生错误：" + error.getMessage()),

            /** 完成时保存上下文 */
            () -> saveHistory(openId, input, finalAnswer.toString())
        );
    }



    // =======================================================================
    //  上下文存储（Redis）
    // =======================================================================

    /** 获取历史对话记录 */
    private List<Message> getHistory(String openId) {
        String json = redis.get(KEY_HISTORY + openId);
        if (json == null) return new ArrayList<>();
        return JsonUtils.parseArray(json, Message.class);
    }

    /** 获取用户自定义人格 */
    private String getPersona(String openId) {
        return Optional.ofNullable(redis.get(KEY_PERSONA + openId))
            .orElse(DEFAULT_PERSONA);
    }

    /** 保存新消息到历史记录 */
    private void saveHistory(String openId, String u, String a) {

        List<Message> history = getHistory(openId);

        history.add(Message.builder().role("user").content(u).build());
        history.add(Message.builder().role("assistant").content(a).build());

        /** 最多保存 10 轮，避免 prompt 过大 */
        if (history.size() > 10) {
            history = history.subList(history.size() - 10, history.size());
        }

        redis.set(KEY_HISTORY + openId, JsonUtils.toJsonString(history));
    }
}
