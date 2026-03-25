package com.ruoyi.business.feishu.config;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.gson.JsonParser;
import com.lark.oapi.Client;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.service.corehr.v2.model.BatchGetEmployeeReq;
import com.lark.oapi.service.corehr.v2.model.BatchGetEmployeeReqBody;
import com.lark.oapi.service.corehr.v2.model.BatchGetEmployeeResp;
import com.ruoyi.business.entity.ButtonInfo;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Configuration
public class BatchGetEmployeeConfig {

    private static final String WEBHOOK_URL = "https://open.feishu.cn/open-apis/bot/v2/hook/c9ccfc5e-e97c-4451-add5-ed74fb3e18d8";
    private static final String SECRET = "Jq5xZzdVkHXkTt0sboSSsg";

    /* --------------------- 签名生成 ---------------------- */
    private static String genSign(String secret, long timestamp) throws Exception {
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(stringToSign.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.encodeBase64String(mac.doFinal(new byte[]{}));
    }

    /* --------------------- 通用卡片发送 ---------------------- */
    public void sendFeishuCardMessage(
        String title,
        String color,                          // blue / green / red / orange
        List<Map<String, String>> sections,    // 大段落（每段内可多个字段）
        List<ButtonInfo> buttons               // 按钮
    ) throws Exception {

        long timestamp = System.currentTimeMillis() / 1000;
        String sign = genSign(SECRET, timestamp);

        Map<String, Object> payload = new HashMap<>();
        payload.put("timestamp", String.valueOf(timestamp));
        payload.put("sign", sign);
        payload.put("msg_type", "interactive");

        /* ---------------------- Card 开始 ---------------------- */
        Map<String, Object> card = new HashMap<>();

        // Header
        Map<String, Object> header = new HashMap<>();
        header.put("template", color);
        header.put("title", Map.of(
            "tag", "plain_text",
            "content", title
        ));
        card.put("header", header);

        // Config
        card.put("config", Map.of("wide_screen_mode", true));

        // Elements
        List<Object> elements = new ArrayList<>();

        /* --------- 多段内容 Section --------- */
        for (Map<String, String> group : sections) {

            Map<String, Object> div = new HashMap<>();
            div.put("tag", "div");

            List<Object> fields = new ArrayList<>();

            group.forEach((k, v) -> {
                fields.add(Map.of(
                    "is_short", false,
                    "text", Map.of(
                        "tag", "lark_md",
                        "content", "**" + k + "：** " + v
                    )
                ));
            });

            div.put("fields", fields);
            elements.add(div);
        }

        /* --------- 按钮区域 --------- */
        if (buttons != null && !buttons.isEmpty()) {
            List<Object> btnList = new ArrayList<>();
            for (ButtonInfo b : buttons) {
                btnList.add(Map.of(
                    "tag", "button",
                    "text", Map.of("tag", "lark_md", "content", b.text),
                    "type", b.type,
                    "url", b.url
                ));
            }
            elements.add(Map.of("tag", "action", "actions", btnList));
        }

        card.put("elements", elements);
        payload.put("card", card);

        /* ---------------------- 发送 ---------------------- */
        OkHttpClient client = new OkHttpClient();
        okhttp3.RequestBody body = okhttp3.RequestBody.create(
            Jsons.DEFAULT.toJson(payload),
            MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder().url(WEBHOOK_URL).post(body).build();
        Response response = client.newCall(request).execute();
        System.out.println(response.body().string());
    }

    /**
     * 通用推送模板（支持丰富 UI）
     */
    public void sendCommonPushCard(
        String businessTitle,
        Map<String, String> fields,
        String url,
        String buttonName
    ) throws Exception {

        List<ButtonInfo> buttons = new ArrayList<>();
        if (url != null && !url.isEmpty()) {
            buttons.add(new ButtonInfo(buttonName, url, "primary"));
        }

        List<Map<String, String>> sections = List.of(fields);

        sendFeishuCardMessage(
            "中台 → " + businessTitle + " 通知",
            "blue",
            sections,
            buttons
        );
    }


    /**
     * 审批提醒
     */
    public void sendApprovalReminderCard(
        String applicant,
        String time,
        String task,
        String url
    ) throws Exception {

        List<Map<String, String>> sections = List.of(
            Map.of(
                "申请人", applicant,
                "申请时间", time,
                "审批任务", task
            )
        );

        sendFeishuCardMessage(
            "待审批任务提醒",
            "orange",
            sections,
            List.of(new ButtonInfo("去审批", url, "primary"))
        );
    }

    /**
     * 数据变更提醒
     */
    public void sendDataChangeCard(
        String operator,
        String field,
        String oldValue,
        String newValue,
        String time
    ) throws Exception {

        List<Map<String, String>> sections = List.of(
            Map.of(
                "操作人", operator,
                "字段", field,
                "原值", oldValue,
                "新值", newValue,
                "时间", time
            )
        );

        sendFeishuCardMessage(
            "数据变更提醒",
            "green",
            sections,
            null
        );
    }
}
