package com.ruoyi.business.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.ruoyi.business.config.AIConversationHandlerFactory;
import com.ruoyi.business.entity.AIConversation;
import com.ruoyi.business.entity.AiLlmModer;
import com.ruoyi.business.servicel.AIBotService;
import com.ruoyi.business.servicel.AIConversationHandler;
import com.ruoyi.business.util.MinioUtil;
import com.ruoyi.business.util.PromptTemplateUtil;
import com.ruoyi.common.core.domain.R;
import jakarta.servlet.http.HttpServletResponse;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/ai/bot")
public class AIBotController {

    private static final Logger log = LoggerFactory.getLogger(AIBotController.class);

    // 飞书豆包配置 - 从配置文件读取
    @Value("${feishu.ark.base-url}")
    private String arkBaseUrl;

    @Value("${feishu.ark.api-key}")
    private String arkApiKey;

    @Value("${feishu.ark.model}")
    private String arkModel;

    @Autowired
    private AIConversationHandlerFactory handlerFactory;
    @Autowired
    private AIBotService aiBotService;
    @Autowired
    private PromptTemplateUtil promptTemplateUtil;
    @Autowired
    private MinioUtil minioUtil;

    /**
     *大模型对话
     */
    @PostMapping("/chat")
    public SseEmitter chat(@RequestBody AIConversation aiConversation,
                           @RequestPart(value = "file", required = false) MultipartFile file,
                           HttpServletResponse response) {
        SseEmitter emitter = new SseEmitter(0L); // 0 = 永不超时
        // 设置SSE必要的响应头
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("X-Accel-Buffering", "no"); // 禁用nginx缓冲
        // LoginAccount loginAccount = SaTokenUtil.getLoginAccount();
        //  aiConversation.setUserId(loginAccount.getLoginName());
        CompletableFuture.runAsync(() -> {

            try {
                //  判断用户问题是否为空
                if (aiConversation.getUserQuestion() == null || aiConversation.getUserQuestion().isEmpty()) {
                    emitter.send(SseEmitter.event().data("用户问题不能为空"));
                    emitter.complete();
                    return;
                }
                //  根据业务类型获取处理器
                aiConversation.setFile(file);
                // SIMPLE_QNA / KNOWLEDGE_BASE / IMAGE_RECOGNITION
                AIConversationHandler handler =  handlerFactory.getHandler(aiConversation.getBusinessType());

                //  调用处理器处理问题，处理器内部使用 SSE 流式输出
                handler.handle(aiConversation, emitter);
            } catch (Exception e) {
                log.error(() -> "AI对话处理异常", e);
                try {
                    emitter.send(SseEmitter.event().data("处理异常: " + e.getMessage()));
                } catch (Exception ex) {
                    log.error(() -> "AI对话处理异常", e);
                }
                emitter.complete();
            }
        });

        return emitter;

    }


    /**
     * 查询大模型
     * */
    @PostMapping("/queryAll")
    public List<AiLlmModer> queryALL() {
        return aiBotService.selectAll() ;

    }
    /**
     * 飞书豆包图片外观识别并检索知识库接口
     * @param file 图片文件
     * @param imageUrl 图片URL(二选一,优先使用file)
     * @return 图片识别结果和知识库检索结果
     */
    @PostMapping("/feishu-image-recognition")
    public R<JSONObject> feishuImageRecognition(
        @RequestParam(value = "file", required = false) MultipartFile file,
        @RequestParam(value = "imageUrl", required = false) String imageUrl) {
        try {
            // 参数校验
            if (file == null && StrUtil.isBlank(imageUrl)) {
                return R.fail("图片文件和图片URL至少提供一个");
            }
            // 检查API KEY
            if (StrUtil.isBlank(arkApiKey)) {
                return R.fail("飞书豆包API配置错误,请联系管理员");
            }

            // 调用服务层处理
            JSONObject result = aiBotService.recognizeImageAndSearch(file, imageUrl);

            if (result.getBool("success", false)) {
                System.out.println(result);
                return R.ok(result);
            } else {
                return R.fail(result.getStr("error", "处理失败"));
            }

        } catch (Exception e) {
            log.error(() -> "图片识别和知识库检索失败", e);
            return R.fail("处理失败: " + e.getMessage());
        }
    }
}
