package com.ruoyi.business.k3.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kingdee.bos.webapi.entity.RepoRet;
import com.kingdee.bos.webapi.sdk.K3CloudApi;

import com.ruoyi.business.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;


import java.io.InputStream;
import java.util.*;
import java.util.Base64;

import static cn.dev33.satoken.SaManager.log;

/**
 * 金蝶K3表单处理抽象模板类
 * 使用模板方法模式定义表单处理的完整流程
 *
 * @param <T> 表单数据类型
 */
@Slf4j
public abstract class AbstractK3FormProcessor<T> {

    // 使用延迟初始化确保只有在需要时才创建K3CloudApi实例
    private static K3CloudApi clientInstance;

    protected static K3CloudApi getClient() {
        if (clientInstance == null) {
            synchronized (AbstractK3FormProcessor.class) {
                if (clientInstance == null) {
                    clientInstance = new K3CloudApi();
                }
            }
        }
        return clientInstance;
    }

    protected static final ObjectMapper objectMapper = new ObjectMapper();
    protected static final Gson gson = new Gson();

    /**
     * 创建包含标准控制参数的请求结构
     *
     * @param model 模型数据
     * @return 包含标准控制参数的请求结构
     */
    protected Map<String, Object> createRequestStructure(Map<String, Object> model) {
        Map<String, Object> request = new HashMap<>();

        // 添加标准控制参数
        request.put("NeedUpDateFields", new ArrayList<>());
        request.put("NeedReturnFields", new ArrayList<>());
        request.put("IsDeleteEntry", "true");
        request.put("SubSystemId", "");
        request.put("IsVerifyBaseDataField", "false");
        request.put("IsEntryBatchFill", "true");
        request.put("ValidateFlag", "true");
        request.put("NumberSearch", "true");
        request.put("IsAutoAdjustField", "false");
        request.put("InterationFlags", "");
        request.put("IgnoreInterationFlag", "");
        request.put("IsControlPrecision", "false");
        request.put("ValidateRepeatJson", "false");

        // 添加模型数据和表单ID
        request.put("Model", model);
        request.put("formid", getFormId());

        return request;
    }

    /**
     * 模板方法 - 定义表单处理的完整流程
     * 1. 构建模型数据 → 2. 暂存表单 → 3. 上传文件 → 4. 保存表单 → 5. 提交表单
     *
     * @param files 文件数组，按顺序对应getFileFieldNames()返回的字段
     * @param formData 表单数据对象
     * @return 处理结果
     */
    public Result processForm(MultipartFile[] files, T formData) {
        String fid = null;
        List<String> uploadedFileIds = new ArrayList<>();

        try {
            log.info("开始处理{}表单数据", getFormId());

            // 步骤1: 构建模型数据
            Map<String, Object> model = buildModel(formData);
            // 步骤2: 暂存表单（创建草稿）
            log.info("步骤2: 暂存表单");
            fid = draftForm(model);
            if (fid == null) {
                return Result.error("暂存失败");
            }

            // 步骤3: 上传文件并绑定到模型
            log.info("步骤3: 上传文件并绑定");
            uploadedFileIds = uploadAndBindFiles(files, model, fid, formData);

//            // 步骤3.5: 上传条目级文件（如果子类支持）
//            log.info("步骤3.5: 上传条目级文件");
//            List<String> entryFileIds = uploadEntryLevelFiles(model, fid, formData);
//            uploadedFileIds.addAll(entryFileIds);

            // 步骤4: 保存表单（带文件绑定）
            log.info("步骤4: 保存表单");
            String saveFid = saveForm(model, fid);
            if (saveFid == null) {
                cleanupOnFailure(fid, uploadedFileIds);
                return Result.error("保存失败");
            }

            // 步骤5: 提交表单
            log.info("步骤5: 提交表单");
            return submitForm(saveFid);

        } catch (Exception e) {
            log.error("处理表单异常", e);
            cleanupOnFailure(fid, uploadedFileIds);
            return Result.error("处理表单异常: " + e.getMessage());
        }
    }

    // ================= 抽象方法 - 必须由子类实现 =================

    /**
     * 获取表单ID（如：BD_MATERIAL, BD_Supplier）
     */
    protected abstract String getFormId();

    /**
     * 构建金蝶K3所需的模型数据
     *
     * @param formData 前端传入的表单数据
     * @return 金蝶K3格式的模型数据
     */
    protected abstract Map<String, Object> buildModel(T formData);

    /**
     * 获取文件字段名称列表
     * 返回的字段名顺序需要与传入的files数组顺序一致
     * 例如：["FImageFileServer", "Fyhbg"] 对应 [图片文件, 验货报告文件]
     */
    protected abstract List<String> getFileFieldNames();

    /**
     * 获取单据编号（用于文件上传时的BillNO字段）
     */
    protected abstract String getDocumentNumber(T formData);

    // ================= 可重写的方法 =================

    /**
     * 上传条目级文件（针对子表中的文件字段）
     * 子类可以重写此方法来处理条目级别的文件上传
     *
     * @param model 模型数据
     * @param fid 表单FID
     * @param formData 表单数据
     * @return 成功上传的文件ID列表
     */
    protected List<String> uploadEntryLevelFiles(Map<String, Object> model, String fid, T formData) throws Exception {
        // 默认实现：不处理条目级文件
        return new ArrayList<>();
    }

    // ================= 具体步骤实现 =================

    /**
     * 步骤2: 暂存表单（创建草稿）
     *
     * @param model 模型数据
     * @return 草稿FID，失败返回null
     */
    private String draftForm(Map<String, Object> model) throws Exception {
        // 使用标准请求结构
        Map<String, Object> draftParam = createRequestStructure(model);

        String draftJson = objectMapper.writeValueAsString(draftParam);
        log.debug("暂存请求JSON: {}", draftJson);

        String draftResp = getClient().draft(getFormId(), draftJson);
        log.debug("暂存响应: {}", draftResp);

        RepoRet repoDraft = gson.fromJson(draftResp, RepoRet.class);

        if (!repoDraft.isSuccessfully()) {
            log.error("暂存失败: {}", draftResp);
            return null;
        }

        String fid = repoDraft.getResult().getId();
        log.info("暂存成功，FID={}", fid);
        return fid;
    }

    /**
     * 步骤3: 上传文件并绑定到模型字段
     *
     * @param files 文件数组
     * @param model 模型数据（会修改此对象，添加文件ID）
     * @param fid 表单FID
     * @param formData 表单数据
     * @return 成功上传的文件ID列表（用于失败时清理）
     */
    private List<String> uploadAndBindFiles(MultipartFile[] files, Map<String, Object> model,
                                           String fid, T formData) throws Exception {
        List<String> uploadedFileIds = new ArrayList<>();

        if (files == null || files.length == 0) {
            log.info("没有需要上传的文件");
            return uploadedFileIds;
        }

        List<String> fileFieldNames = getFileFieldNames();
        String documentNumber = getDocumentNumber(formData);

        for (int i = 0; i < Math.min(files.length, fileFieldNames.size()); i++) {
            MultipartFile file = files[i];
            if (file != null && !file.isEmpty()) {
                String fieldName = fileFieldNames.get(i);

                String fileId = uploadFile(file, fid, documentNumber, fieldName);
                if (fileId != null) {
                    model.put(fieldName, fileId);
                    uploadedFileIds.add(fileId);
                    log.info("文件上传成功并绑定到字段 {}: FileId={}", fieldName, fileId);
                }
            }
        }

        return uploadedFileIds;
    }

    /**
     * 上传单个文件到金蝶（主表级别）
     */
    private String uploadFile(MultipartFile file, String fid, String documentNumber, String fieldName) throws Exception {
        return uploadFileWithEntryKey(file, fid, documentNumber, fieldName, "");
    }

    /**
     * 上传单个文件到金蝶（支持条目级别）
     *
     * @param file 文件
     * @param fid 表单ID
     * @param documentNumber 单据编号
     * @param fieldName 字段名
     * @param entryKey 条目键（用于子表文件上传，主表传空字符串）
     */
    protected String uploadFileWithEntryKey(MultipartFile file,
                                            String fid,
                                            String documentNumber,
                                            String fieldName,
                                            String entryKey) throws Exception {

        int blockSize = 1024 * 512; // 512KB
        byte[] buffer = new byte[blockSize];
        String fileId = "";

        try (InputStream in = file.getInputStream()) {

            while (true) {
                int len = in.read(buffer);
                if (len == -1) break;

                boolean isLast = len < blockSize;

                byte[] uploadBytes = Arrays.copyOf(buffer, len);
                String base64Content = Base64.getEncoder()
                    .encodeToString(uploadBytes);

                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("SendByte", base64Content);
                paramMap.put("FileName", file.getOriginalFilename());
                paramMap.put("FormId", getFormId());
                paramMap.put("IsLast", isLast);
                paramMap.put("InterId", fid);              // 单据内码
                paramMap.put("BillNO", documentNumber);   // 单据编号
                paramMap.put("AliasFileName", fieldName); // 绑定字段
                paramMap.put("EntryKey", entryKey == null ? "" : entryKey);
                paramMap.put("FileId", fileId);           // 续传关键

                String uploadJson = objectMapper.writeValueAsString(paramMap);
                String resultJson = getClient().attachmentUpload(uploadJson);

                JsonObject resultObj = gson.fromJson(resultJson, JsonObject.class)
                    .getAsJsonObject("Result");

                if (resultObj == null || !resultObj.has("FileId")) {
                    throw new RuntimeException("附件上传失败：" + resultJson);
                }

                fileId = resultObj.get("FileId").getAsString();

                if (isLast) break;
            }
        }

        return fileId;
    }

    /**
     * 步骤4: 保存表单（带文件绑定）
     */
    private String saveForm(Map<String, Object> model, String fid) throws Exception {
        // 设置物料ID
        model.put("FMATERIALID", fid);

        // 使用标准请求结构
        Map<String, Object> saveParam = createRequestStructure(model);

        String jsonSave = objectMapper.writeValueAsString(saveParam);
        log.debug("保存请求JSON: {}", jsonSave);

        String saveResp = getClient().save(getFormId(), jsonSave);
        log.debug("保存响应: {}", saveResp);

        RepoRet repoSave = gson.fromJson(saveResp, RepoRet.class);
        if (!repoSave.isSuccessfully()) {
            log.error("保存失败: {}", saveResp);
            // 尝试解析并记录更详细的错误信息
            try {
                if (repoSave.getResult() != null && repoSave.getResult().getResponseStatus() != null) {
                    log.error("保存失败详细信息 - ErrorCode: {}, IsSuccess: {}, Errors: {}",
                        repoSave.getResult().getResponseStatus().getErrorCode(),
                        repoSave.getResult().getResponseStatus().getErrors());
                }
            } catch (Exception e) {
                log.error("解析保存失败详细信息时出错: {}", e.getMessage());
            }
            return null;
        }

        // 安全地获取 saveFid
        try {
            String saveFid = null;
            if (repoSave.getResult() != null &&
                repoSave.getResult().getResponseStatus() != null &&
                repoSave.getResult().getResponseStatus().getSuccessEntitys() != null &&
                !repoSave.getResult().getResponseStatus().getSuccessEntitys().isEmpty()) {

                saveFid = repoSave.getResult().getResponseStatus().getSuccessEntitys().get(0).getId();
                log.info("保存成功，SaveFID={}", saveFid);
                return saveFid;
            } else {
                log.error("保存响应中缺少 SuccessEntitys 或 ID 信息: {}", saveResp);
                return null;
            }
        } catch (Exception e) {
            log.error("解析保存响应时出错: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 步骤5: 提交表单
     */
    private Result submitForm(String saveFid) throws Exception {
        // 创建提交参数结构
        Map<String, Object> submitParam = createRequestStructure(new HashMap<>());

        // 特殊处理：提交操作需要将Ids放在根级别
        submitParam.remove("Model");
        submitParam.put("Ids", saveFid);

        String jsonParam = objectMapper.writeValueAsString(submitParam);
        log.debug("提交请求JSON: {}", jsonParam);

        String submitResp = getClient().submit(getFormId(), jsonParam);
        log.debug("提交响应: {}", submitResp);

        RepoRet repoSubmit = gson.fromJson(submitResp, RepoRet.class);
        if (!repoSubmit.isSuccessfully()) {
            log.error("提交失败: {}", submitResp);
            // 尝试解析并记录更详细的错误信息
            try {
                if (repoSubmit.getResult() != null && repoSubmit.getResult().getResponseStatus() != null) {
                    log.error("提交失败详细信息 - ErrorCode: {}, IsSuccess: {}, Errors: {}",
                        repoSubmit.getResult().getResponseStatus().getErrorCode(),
                        repoSubmit.getResult().getResponseStatus().getErrors());
                }
            } catch (Exception e) {
                log.error("解析提交失败详细信息时出错: {}", e.getMessage());
            }
            return Result.error("提交失败: " + submitResp);
        }

        log.info("{}表单提交成功！FID={}", getFormId(), saveFid);
        return Result.success(saveFid);
    }

    /**
     * 失败清理：删除草稿和已上传的文件
     */
    private void cleanupOnFailure(String fid, List<String> fileIds) {
        try {
            log.warn("开始清理失败资源: FID={}, FileIds={}", fid, fileIds);

            // 清理上传的文件（如果有）
            if (fileIds != null && !fileIds.isEmpty()) {
                for (String fileId : fileIds) {
                    cleanupFile(fileId);
                }
            }

            // 清理草稿（如果有）
            if (fid != null) {
                cleanupDraft(fid);
            }

        } catch (Exception e) {
            log.warn("清理资源时发生异常: {}", e.getMessage());
        }
    }

    /**
     * 清理上传的文件
     */
    private void cleanupFile(String fileId) {
        try {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("Id", fileId);

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("FormId", "BOS_Attachment");
            paramMap.put("Data", dataMap);

            String json = objectMapper.writeValueAsString(paramMap);
            String delFileResp = getClient().delete("BOS_Attachment", json);
            log.info("删除文件结果: {}", delFileResp);

        } catch (Exception e) {
            log.error("删除文件失败 FileId={}: {}", fileId, e.getMessage());
        }
    }

    /**
     * 清理草稿
     */
    private void cleanupDraft(String fid) {
        try {
            String delResp = getClient().delete(getFormId(), fid);
            log.info("删除草稿结果: {}", delResp);

        } catch (Exception e) {
            log.error("删除草稿失败 FID={}: {}", fid, e.getMessage());
        }
    }
}
