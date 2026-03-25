package com.ruoyi.business.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import com.ruoyi.business.bo.BymaterialBo;
import com.ruoyi.business.entity.Bymaterial;
import com.ruoyi.business.mapper.MaterialMapper;
import com.ruoyi.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/Dify")
public class    DifyImageSearchController {

    private static final String BASE_URL = "http://118.178.144.159/v1";
    private static final String API_KEY = "app-OE6QNMB7bp6JkUBQRc1ir1yu";

    @Autowired
    private MaterialMapper materialMapper;

    // 普通聊天API
    @PostMapping("/chat")
    public R<JSONObject> chat(@org.springframework.web.bind.annotation.RequestBody JSONObject requestBody) {
        String query = requestBody.getStr("query");
        if (StrUtil.isBlank(query)) {
            return R.fail("查询内容不能为空");
        }

        try {
            // 构建请求体
            JSONObject body = new JSONObject();
            body.put("inputs", new JSONObject());
            body.put("query", query);
            body.put("response_mode", "blocking");
            body.put("user", "user-001");

            String conversationId = requestBody.getStr("conversation_id");
            if (StrUtil.isNotBlank(conversationId)) {
                body.put("conversation_id", conversationId);
            }

            // 构建请求
            OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

            Request request = new Request.Builder()
                .url(BASE_URL + "/chat-messages")
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                    body.toString(),
                    MediaType.parse("application/json")
                ))
                .build();

            log.info("发送DIFY请求: URL={}, Body={}", BASE_URL, body);

            try (Response response = client.newCall(request).execute()) {
                String responseText = response.body().string();

                if (!response.isSuccessful()) {
                    log.error("DIFY API错误: 状态码={}, 响应={}", response.code(), responseText);
                    return handleDifyError(response.code(), responseText);
                }

                try {
                    JSONObject result = new JSONObject(responseText);
                    log.info("DIFY请求成功: {}", result);
                    return R.ok(result);
                } catch (JSONException e) {
                    log.error("DIFY响应解析失败: {}", responseText, e);
                    return R.fail("响应格式异常");
                }
            }
        } catch (IOException e) {
            log.error("DIFY网络请求异常", e);
            return R.fail("网络连接异常");
        } catch (Exception e) {
            log.error("DIFY聊天处理异常", e);
            return R.fail("系统处理异常");
        }
    }

    private R<JSONObject> handleDifyError(int statusCode, String errorBody) {
        String errorMsg;
        switch (statusCode) {
            case 400:
                errorMsg = "请求参数错误";
                break;
            case 401:
                errorMsg = "API密钥无效";
                break;
            case 403:
                errorMsg = "访问被拒绝";
                break;
            case 404:
                errorMsg = "API接口不存在";
                break;
            case 500:
                errorMsg = "Dify服务内部错误";
                break;
            case 502:
            case 503:
            case 504:
                errorMsg = "Dify服务不可用";
                break;
            default:
                errorMsg = "服务异常，状态码：" + statusCode;
        }

        // 尝试从HTML中提取错误信息
        if (errorBody.contains("<html") || errorBody.contains("<HTML")) {
            log.warn("DIFY返回了HTML错误页面");
            // 可以添加HTML解析逻辑提取具体错误信息
        }

        return R.fail("Dify服务错误: " + errorMsg);
    }


    @PostMapping("/image-search-materials")
    public R<List<BymaterialBo>> imageSearchMaterials(@RequestParam("file") MultipartFile file) throws Exception {

        String fileId = uploadImage(file);

        JSONObject fileObj = new JSONObject();
        fileObj.put("type", "image");
        fileObj.put("transfer_method", "local_file");
        fileObj.put("upload_file_id", fileId);

        JSONArray files = new JSONArray();
        files.add(fileObj);

        JSONObject body = new JSONObject();
        body.put("inputs", new JSONObject());
        body.put("query",
            "请从图片中识别物料，严格按以下格式输出：\n" +
                "1. 物料编码：XXXX\n" +
                "物料名称：XXXX\n" +
                "规格型号：XXXX\n" +
                "核心描述：XXXX\n" +
                "外观：XXXX\n\n" +
                "禁止输出任何总结语");
        body.put("response_mode", "blocking");
        body.put("user", "user-001");
        body.put("files", files);

        OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

        Request request = new Request.Builder()
            .url(BASE_URL + "/chat-messages")
            .addHeader("Authorization", "Bearer " + API_KEY)
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
            .build();

        try (Response resp = client.newCall(request).execute()) {
            String respText = resp.body().string();
            log.info("DIFY 原始返回JSON：\n{}", respText);

            JSONObject root = new JSONObject(respText);
            String answer = root.getStr("answer");

            log.info("DIFY 真实TXT：\n{}", answer);

            List<BymaterialBo> list = parseTxtToMaterials(answer);

            // 为每个物料填充图片信息
            for (BymaterialBo materialBo : list) {
                if (materialBo.getNumber() != null && !materialBo.getNumber().isEmpty()) {
                    // 根据物料编码查询数据库中的物料信息
                    Bymaterial existingMaterial = materialMapper.selectByNumber(materialBo.getNumber());
                    if (existingMaterial != null && existingMaterial.getImage() != null) {
                        // 将数据库中的图片URL填充到返回的物料对象中
                        materialBo.setImage(existingMaterial.getImage());
                    }
                }
            }

            return R.ok(list);
        }
    }

    // ================= TXT 解析核心 =================

    private List<BymaterialBo> parseTxtToMaterials(String txt) {

        List<BymaterialBo> list = new ArrayList<>();

        String[] blocks = txt.split("\\n\\s*\\d+\\.\\s*物料编码");

        for (String block : blocks) {

            block = block.trim();
            if (block.isEmpty()) continue;
            block = "物料编码" + block;

            BymaterialBo bo = new BymaterialBo();
            bo.setNumber(extract(block, "物料编码"));
            bo.setName(extract(block, "物料名称"));
            bo.setAppearance(extract(block, "外观"));
            bo.setSpecification(extract(block, "规格型号"));
            bo.setDescription1(extract(block, "核心描述"));
            bo.setScore(0.9d);

            if (bo.getNumber() != null) {
                list.add(bo);
            }
        }
        return list;
    }

    private String extract(String text, String key) {
        Pattern p = Pattern.compile(key + "[:：]\\s*(.*)");
        Matcher m = p.matcher(text);
        if (m.find()) {
            String v = m.group(1);
            int idx = v.indexOf("\n");
            if (idx > 0) v = v.substring(0, idx);
            return v.trim();
        }
        return null;
    }

    // ================= 图片上传 =================

    private String uploadImage(MultipartFile file) throws Exception {

        OkHttpClient client = new OkHttpClient();

        log.info("开始上传图片: 文件名={}, 大小={} bytes, 类型={}",
            file.getOriginalFilename(), file.getSize(), file.getContentType());

        MultipartBody body = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.getOriginalFilename(),
                RequestBody.create(file.getBytes(), MediaType.parse(file.getContentType())))
            .addFormDataPart("user", "user-001")
            .build();

        String uploadUrl = BASE_URL + "/files/upload";
        log.info("上传图片 URL: {}", uploadUrl);

        Request request = new Request.Builder()
            .url(uploadUrl)
            .addHeader("Authorization", "Bearer " + API_KEY)
            .post(body)
            .build();

        try (Response resp = client.newCall(request).execute()) {
            String text = resp.body().string();
            log.info("上传图片返回: {}", text);

            // 检查响应状态码
            if (!resp.isSuccessful()) {
                log.error("上传图片失败，状态码: {}, 响应内容: {}", resp.code(), text);
                throw new RuntimeException("上传图片失败: " + resp.code() + " - " + resp.message());
            }

            // 检查响应内容是否为 JSON 格式
            if (text == null || text.trim().isEmpty()) {
                throw new RuntimeException("上传图片响应内容为空");
            }

            // 尝试解析 JSON
            try {
                JSONObject jsonObject = new JSONObject(text);
                String fileId = jsonObject.getStr("id");
                if (fileId == null || fileId.trim().isEmpty()) {
                    log.error("响应中未找到 id 字段: {}", text);
                    throw new RuntimeException("响应中未找到文件 ID");
                }
                return fileId;
            } catch (Exception e) {
                log.error("解析上传图片响应失败: {}", text, e);
                throw new RuntimeException("解析上传图片响应失败: " + e.getMessage());
            }
        }
    }
}
