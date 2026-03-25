package com.ruoyi.business.servicel.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ruoyi.business.entity.AIConversation;
import com.ruoyi.business.entity.AiLlmModer;
import com.ruoyi.business.mapper.AIConversationMapper;
import com.ruoyi.business.mapper.AiLlmModeMapper;  // 修改这里
import com.ruoyi.business.servicel.AIBotService;
 import com.ruoyi.business.util.MinioUtil;
import com.ruoyi.business.util.PromptTemplateUtil;
import com.volcengine.ark.runtime.model.responses.constant.ResponsesConstants;
import com.volcengine.ark.runtime.model.responses.content.InputContentItemImage;
import com.volcengine.ark.runtime.model.responses.content.InputContentItemText;
import com.volcengine.ark.runtime.model.responses.item.ItemEasyMessage;
import com.volcengine.ark.runtime.model.responses.item.MessageContent;
import com.volcengine.ark.runtime.model.responses.request.CreateResponsesRequest;
import com.volcengine.ark.runtime.model.responses.request.ResponsesInput;
import com.volcengine.ark.runtime.model.responses.response.ResponseObject;
import com.volcengine.ark.runtime.service.ArkService;
import okhttp3.*;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
public class AIBotServiceimpl implements AIBotService {

    @Autowired
    private AiLlmModeMapper aiLlmModeMapper;  // 这里是正确的
    @Autowired
    private AIConversationMapper aiConversationMapper;

    // 图片识别配置
    @Value("${feishu.ark.base-url}")
    private String arkBaseUrl;

    @Value("${feishu.ark.api-key}")
    private String arkApiKey;

    @Value("${feishu.ark.model}")
    private String arkModel;

    // 知识库配置
    @Value("${feishu.knowledge-base.host:}")
    private String kbHost;

    @Value("${feishu.knowledge-base.service-chat-path:}")
    private String kbServiceChatPath;

    @Value("${feishu.knowledge-base.api-key:}")
    private String kbApiKey;

    @Value("${feishu.knowledge-base.service-resource-id:}")
    private String kbServiceResourceId;

    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    private PromptTemplateUtil promptTemplateUtil;

    private final OkHttpClient httpClient = new OkHttpClient();

    @Override
    public JSONObject recognizeImageAndSearch(MultipartFile file, String imageUrl) {
        JSONObject result = new JSONObject();

        try {
            // 1. 确定图片URL
            String finalImageUrl = imageUrl;
            if (file != null) {
                String fileName = "ai/images/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
                finalImageUrl = minioUtil.uploadFile(file, fileName);

            }

            // 2. 图片识别
            String recognizedText = recognizeImage(finalImageUrl);

            result.put("imageUrl", finalImageUrl);
            result.put("recognizedText", recognizedText);

            // 3. 使用识别结果检索知识库
            if (StrUtil.isNotBlank(recognizedText)) {
                // 检查知识库配置
                if (StrUtil.isBlank(kbHost) || StrUtil.isBlank(kbServiceChatPath)
                    || StrUtil.isBlank(kbApiKey) || StrUtil.isBlank(kbServiceResourceId)) {
                    result.put("knowledgeBaseResult", null);
                    result.put("message", "图片识别成功，但知识库配置不完整，未进行知识库检索");
                } else {
                    JSONObject knowledgeResult = searchKnowledgeBase(recognizedText, 5);
                    result.put("knowledgeBaseResult", knowledgeResult);
                }
            } else {
                result.put("knowledgeBaseResult", null);
                result.put("message", "图片识别结果为空，未进行知识库检索");
            }

            result.put("success", true);
            return result;

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    @Override
    public String recognizeImage(String imageUrl) {
        ArkService arkService = null;
        try {
            // 使用提示词模板生成外观描述提示词
            String prompt = promptTemplateUtil.generateMaterialImageAppearancePrompt();

            // 创建ArkService实例
            arkService = ArkService.builder()
                .apiKey(arkApiKey)
                .baseUrl(arkBaseUrl)
                .build();

            // 构建请求
            CreateResponsesRequest request = CreateResponsesRequest.builder()
                .model(arkModel)
                .input(ResponsesInput.builder().addListItem(
                    ItemEasyMessage.builder()
                        .role(ResponsesConstants.MESSAGE_ROLE_USER)
                        .content(
                            MessageContent.builder()
                                .addListItem(InputContentItemImage.builder()
                                    .imageUrl(imageUrl)
                                    .build())
                                .addListItem(InputContentItemText.builder()
                                    .text(prompt)
                                    .build())
                                .build()
                        )
                        .build()
                ).build())
                .build();

            ResponseObject resp = arkService.createResponse(request);

            // 提取识别内容
            if (resp != null && resp.getOutput() != null) {
                String content = resp.getOutput().toString();
                if (StrUtil.isNotBlank(content)) {
                    return content;
                }
            }
            return "";

        } catch (Exception e) {
            throw new RuntimeException("图片识别失败: " + e.getMessage());
        } finally {
            if (arkService != null) {
                arkService.shutdownExecutor();
            }
        }
    }

    @Override
    public JSONObject searchKnowledgeBase(String query, Integer topN) {
        try {
            // 检查配置
            if (StrUtil.isBlank(kbHost) || StrUtil.isBlank(kbServiceChatPath)
                || StrUtil.isBlank(kbApiKey) || StrUtil.isBlank(kbServiceResourceId)) {
                JSONObject errorResult = new JSONObject();
                errorResult.put("success", false);
                errorResult.put("message", "知识库配置不完整，请联系管理员配置 host、service-chat-path、api-key、service-resource-id");
                return errorResult;
            }

            if (topN == null) {
                topN = 5;
            }

            // 构造请求体 - 按照飞书知识库 service chat API 格式
            JSONObject requestBody = new JSONObject();
            requestBody.put("service_resource_id", kbServiceResourceId);

            // 构造 messages 数组
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", query);

            JSONObject[] messages = new JSONObject[]{message};
            requestBody.put("messages", messages);

            // 可选参数
            if (topN != null) {
                requestBody.put("top_n", topN);
            }

            // 构造请求
            String url = String.format("https://%s%s", kbHost, kbServiceChatPath);
            Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + kbApiKey)
                .build();
            // 发送请求
            Response response = httpClient.newCall(request).execute();

            // 读取响应体
            String responseBody = response.body() != null ? response.body().string() : "";

            if (response.isSuccessful()) {
                return JSONUtil.parseObj(responseBody);
            } else {
                String errorMsg = String.format("知识库检索失败: %d %s, 响应: %s",
                    response.code(), response.message(), responseBody);
                throw new RuntimeException(errorMsg);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("知识库检索失败: " + e.getMessage());
        }
    }
    @Override
    public List<AiLlmModer> selectAll() {
        return aiLlmModeMapper.selectAll();
    }

    @Override
    public String insert(AIConversation aiConversation) {
        if (aiConversation == null){
            return "对象不可为空";
        }
        if (aiConversation.getUserId()== null){
            return "用户ID不可为空";
        }
        if (aiConversation.getSessionId()== null){
            return "会话ID不可为空";
        }

        aiConversationMapper.insert(aiConversation);
        return "插入成功";
    }

    /**
     * 处理输入源：支持文件或URL
     */
    private String prepareInput(MultipartFile file, String url) throws IOException {
        if (file != null && !file.isEmpty()) {
            String base64 = Base64.getEncoder().encodeToString(file.getBytes());
            return "data:image/jpeg;base64," + base64;
        } else if (url != null && !url.isEmpty()) {
            return url;
        } else {
            return "（未检测到有效内容）";
        }
    }

}
