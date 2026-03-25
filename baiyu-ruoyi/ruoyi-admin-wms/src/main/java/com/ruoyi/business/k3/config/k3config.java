package com.ruoyi.business.k3.config;



import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kingdee.bos.webapi.entity.RepoRet;


import com.ruoyi.business.Component.K3FormProcessorFactory;
import com.ruoyi.business.entity.Bymaterial;
import com.ruoyi.business.entity.BymaterialDictionary;
import com.ruoyi.business.entity.DictionaryTable;
import com.ruoyi.business.entity.Supplier;
import com.ruoyi.business.mapper.BymaterialDictionaryMapper;
import com.ruoyi.business.mapper.DictionaryTableMapper;
import com.ruoyi.business.util.Result;

import com.ruoyi.common.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper; // 用于 JSON 转换

import com.kingdee.bos.webapi.sdk.K3CloudApi;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;


import static com.alibaba.dashscope.utils.JsonUtils.gson;


@Configuration
@Slf4j
public class k3config {

    @Autowired
    private K3FormProcessorFactory k3FormProcessorFactory;


    private static final String BASE_URL = "http://113.46.194.126/K3Cloud/";
    private static final String UPLOAD_URL = BASE_URL + "Kingdee.BOS.WebApi.ServicesStub.DynamicFormService.AttachmentUpLoad.common.kdsvc";

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // 使用延迟初始化确保只有在需要时才创建K3CloudApi实例
    private static K3CloudApi k3CloudApiClient;

    protected static K3CloudApi getK3CloudApiClient() {
        if (k3CloudApiClient == null) {
            synchronized (k3config.class) {
                if (k3CloudApiClient == null) {
                    k3CloudApiClient = new K3CloudApi();
                }
            }
        }
        return k3CloudApiClient;
    }

    @Autowired
    private DictionaryTableMapper dictionaryTableMapper;
    @Autowired
    private BymaterialDictionaryMapper bymaterialDictionaryMapper;


    /**
     * 查询采购价目主体表
     */

    public List<List<java.lang.Object>> PriceLarsList( ) {
        try {
            String jsonData = "{" +
                    "\"FormId\":\"PUR_PriceCategory\"," +
                    "\"FieldKeys\":\"FID,FName,FNumber,FDescription,FCurrencyID,FSupplierID,F_GYSLB,FPricer,FPriceObject,FPriceType,FCreatorId\n\"," +
                    "\"OrderString\":\"\"," +
                    "\"TopRowCount\":0,\"StartRow\":0," +
                    "\"Limit\":5000000,\"SubSystemId\":\"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>(); // 返回空列表而不是null
    }

    /**
     * 查询采购价目详情表
     */

    public List<List<java.lang.Object>> PriceParticularsList() {
        try {
            String jsonData = "{" +
                    "\"FormId\":\"PUR_PriceCategory\"," +
                    "\"FieldKeys\":\"FNumber,FPrice,FTaxPrice,FMaterialId,FMaterialName,F_GCHH,F_cty_BaseProperty,F_GGSM,FNote,\n" +
                    "FTaxRate,F100t5,FUnitID,FPriceCoefficient,FDownPrice,FUpPrice,FEntryEffectiveDate,FEntryExpiryDate,F_WBZC,F_WBZG,F_WBZTJ,F_WBZSL,\n" +
                    "F_WBZDW,F_MZ,F_JZ,F_BCGG,F_bzsm,FRECENTDATE,F_cgshys,F_dzshqdl,F_ZXSMS,F_ZXSMSTP,F_kppm,Fxjr,Fsfyyywxj,Fcpzlyq,F_QDL\n\"," +
                    "\"OrderString\":\"\"," +
                    "\"TopRowCount\":0,\"StartRow\":0," +
                    "\"Limit\":5000000,\"SubSystemId\":\"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>(); // 返回空列表而不是null
    }
    /**
     * 查询供应商支付信息
     */

    public List<List<java.lang.Object>> querypaymentList( ) {
        try {
            String jsonData = "{" +
                    "\"FormId\":\"BD_Supplier\"," +
                    "\"FieldKeys\":\"FNumber ,FBankCountry ,FBankCode,FBankHolder ,FBankTypeRec ,FOpenAddressRec,FOpenBankName    \"," +
                    "\"OrderString\":\"\"," +
                    "\"TopRowCount\":0,\"StartRow\":0," +
                    "\"Limit\":5000000,\"SubSystemId\":\"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>(); // 返回空列表而不是null
    }

    /**
     * 查询供应商联系人
     */

    public List<List<java.lang.Object>> querylinkmanList() {
        try {
            String jsonData = "{" +
                    "\"FormId\":\"BD_Supplier\"," +
                    "\"FieldKeys\":\"FNumber,FName,FContact,FPost,FTel,FMobile,FEMail,F_ora_BaseProperty,FLocAddress,FDefaultContactId\"," +
                    "\"OrderString\":\"\"," +
                    "\"TopRowCount\":0,\"StartRow\":0," +
                    "\"Limit\":5000000,\"SubSystemId\":\"\"}";

            return  getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>(); // 返回空列表而不是null
    }

    /**
     * 查询供应商列表
     */

    public List<List<java.lang.Object>> querySupplierList() {
        try {
            String jsonData = "{" +
                    "\"FormId\":\"BD_Supplier\"," +
                    "\"FieldKeys\":\"\n" +
                    "FName,FNumber,FShortName,FCountry,FProvincial,FAddress,FLegalPerson,FFoundDate,F_gwzb,F_fze,FSupplierClassify,FSupplyClassify,F_ora_Text2,FRegisterCode,FSOCIALCRECODE,Fly,\n" +
                    "F_yyzz,F_KPPM,Fgcwt,F_gdfk,FPayCurrencyId,FSettleTypeId,FPayCondition,FInvoiceType,FTaxType,FSettleId,FChargeId,FTaxRateId,FCreatorId,Fxzyy1,FCreateDate,FSupplierId,FGroup,FModifierId,F_ndyye," +
                "F_dzpg,F_wxscqy,F_gcrs,F_gcrz,F_cfmj,F_gcsb,F_pk,F_gcdw,F_lbcsfg,F_YXPK,FAuditorId,FAuditDate,FDefaultContactId\"," +
                    "\"OrderString\":\"\"," +
                    "\"TopRowCount\":0,\"StartRow\":0," +
                    "\"Limit\":5000000,\"SubSystemId\":\"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>(); // 返回空列表而不是null
    }
    /**供应商拜访记录*/
    public List<List<java.lang.Object>> querySuppliervisitingList() {
        try {
            String jsonData = "{" +
                "\"FormId\":\"BD_Supplier\"," +
                "\"FieldKeys\":\"\n" + "FNumber,F_hfsj,F_bfr,F_hfnr1\"," +
                "\"OrderString\":\"\"," +
                "\"OrderString\":\"\"," +
                "\"TopRowCount\":0,\"StartRow\":0," +
                "\"Limit\":5000000,\"SubSystemId\":\"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>(); // 返回空列表而不是null
    }


    /**
     * 查询物料列表（分页）
     * @param startRow 起始行
     * @param pageSize 每页大小（最大10000）
     * @return 物料数据列表
     */
    public List<List<java.lang.Object>> queryMaterialList(int startRow, int pageSize,String materialNumber) {
        try {
            String filterString = "";

            // 如果传了物料编码，就加过滤条件
            if (materialNumber != null && !materialNumber.trim().isEmpty()) {

                 filterString = "FNumber like '%" + materialNumber.trim() + "%'";
            }
            String jsonData = "{" +
                    "\"FormId\":\"BD_MATERIAL\"," +
                    "\"FieldKeys\":\"FName,FNumber,FSpecification,FDescription1,FErpClsID,FMaterialGroup,F_XLCP,FVOLUME,Fyhbg,F_HSBM1,F_cplb,FCreatorId,FCreateDate,FMATERIALID,FOldNumber," +
                "FForbidStatus,FForbidderId,FForbidDate,F_BCFWGYS\"," +
                "\"FilterString\":\"" + filterString + "\"," +
                "\"OrderString\":\"FMATERIALID ASC\"," +
                    "\"TopRowCount\":0," +
                    "\"StartRow\":" + startRow + "," +
                    "\"Limit\":" + pageSize + "," +
                    "\"SubSystemId\":\"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            log.error("分页查询物料列表失败，startRow={}, pageSize={}", startRow, pageSize, e);
        }
        return new ArrayList<>();
    }

    /**
     * 查询物料列表（兼容旧方法，默认查询全部）
     */
    @Deprecated
    public List<List<java.lang.Object>> queryMaterialList() {
        return queryMaterialList(0, 10000, null);
    }

    public List<List<java.lang.Object>> queryProductCategories() {
        try {
            String jsonData = "{" +
                    "\"FormId\":\"BOS_ASSISTANTDATA_DETAIL\"," +
                    "\"FieldKeys\":\"FEntryID,FDataValue,FNumber\"," +
                    "\"FilterString\":[]," +
                    "\"OrderString\":\"\"," +
                    "\"TopRowCount\":0,\"StartRow\":0," +
                    "\"Limit\":150000,\"SubSystemId\":\"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }



    public String submitMaterialList(String fNumber) {
        try {
            // 查询所有产品类别
            List<List<Object>> productCategories = queryProductCategories();

            // 遍历查找匹配的FNumber
            for (List<Object> category : productCategories) {
                // category结构: [FEntryID, FDataValue, FNumber]
                if (category.size() >= 3 && category.get(1) != null) {
                    String categoryNumber = category.get(1).toString();
                    if (categoryNumber.equals(fNumber)) {
                        // 返回对应的FEntryID
                        return category.get(0) != null ? category.get(0).toString() : null;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }



            /**
             * 上传文件到金蝶（单据附件接口）
             */
    public String uploadMaterialImageToKingdee(MultipartFile file, String materialId, String materialNumber) {
        try {
            // 将文件转为Base64字符串
            byte[] fileBytes = file.getBytes();
            String base64Data = Base64.getEncoder().encodeToString(fileBytes);

            // 构造请求参数（按照金蝶标准格式）
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\"FileName\":\"").append(file.getOriginalFilename()).append("\",");
            jsonBuilder.append("\"FEntryKey\":\"\",");
            jsonBuilder.append("\"FormId\": \"BD_MATERIAL\",");
            jsonBuilder.append("\"IsLast\": true,");


            // 对于新增物料，不传InterId或者传空值
                jsonBuilder.append("\"InterId\": ").append(materialId).append(",");


            jsonBuilder.append("\"BillNO\": \"").append(materialNumber).append("\",");
            jsonBuilder.append("\"AliasFileName\": \"").append(file.getOriginalFilename()).append("\",");
            jsonBuilder.append("\"SendByte\": \"").append(base64Data).append("\"}");

            // 调用金蝶上传接口
            String resultJson = getK3CloudApiClient().attachmentUpload(jsonBuilder.toString());
            return resultJson;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 上传文件到金蝶（单据附件接口）
     */
    public String uploadSupplierImageToKingdee(MultipartFile file, String materialId, String materialNumber,String formId) {
        try {
            // 将文件转为Base64字符串
            byte[] fileBytes = file.getBytes();
            String base64Data = Base64.getEncoder().encodeToString(fileBytes);

            // 构造请求参数（按照金蝶标准格式）
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\"FileName\":\"").append(file.getOriginalFilename()).append("\",");
            jsonBuilder.append("\"FEntryKey\":\"\",");
            jsonBuilder.append("\"FormId\":\"").append(formId).append("\",");
            jsonBuilder.append("\"IsLast\": true,");


            // 对于新增物料，不传InterId或者传空值
            jsonBuilder.append("\"InterId\": ").append(materialId).append(",");


            jsonBuilder.append("\"BillNO\": \"").append(materialNumber).append("\",");
            jsonBuilder.append("\"AliasFileName\": \"").append(file.getOriginalFilename()).append("\",");
            jsonBuilder.append("\"SendByte\": \"").append(base64Data).append("\"}");

            // 调用金蝶上传接口
            String resultJson = getK3CloudApiClient().attachmentUpload(jsonBuilder.toString());
            return resultJson;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 保存金蝶供应商表单数据
     * @deprecated #processForm(MultipartFile[], Supplier) 替代
     * 通过工厂类获取：K3FormProcessorFactory.getProcessor("BD_Supplier")
     * */
    @Deprecated
    public Result   SsaveSupplierlFormData(MultipartFile image, Supplier supplier) {
        try {
            //业务对象标识
            String formId = "BD_Supplier";
            // 构造请求参数
            Map<String, Object> model = saveSupplier(supplier);

            model.put("FNumber", supplier.getNumber() != null ? supplier.getNumber() : "");

            Map<String, Object> draftParam = new HashMap<>();
            draftParam.put("Model", model);
            draftParam.put("formid", formId);

            // 将参数转换为JSON字符串
            String draftJson = new ObjectMapper().writeValueAsString(draftParam);
            String draftResp = getK3CloudApiClient().draft(formId, draftJson);
            RepoRet repoDraft = gson.fromJson(draftResp, RepoRet.class);

            if (!repoDraft.isSuccessfully()) {
                log.error("暂存失败: {}", draftResp);
                return Result.error("暂存失败: {}");
            }

            String fid = repoDraft.getResult().getId();
            log.info("暂存成功，FID={}", fid);


            //  上传图片并绑定 FID
            String fileId = null;
            if (image != null && !image.isEmpty()) {
                String uploadResult = uploadSupplierImageToKingdee(image, fid, supplier.getNumber(), formId);
                JsonObject uploadObj = gson.fromJson(uploadResult, JsonObject.class);
                fileId = uploadObj.getAsJsonObject("Result").get("FileId").getAsString();

                model.put("FImageFileServer", fileId);
                log.info("图片上传成功并绑定 FileId={}", fileId);
            }

            //  更新保存（带 FID）
            model.put("FMATERIALID", fid);
            Map<String, Object> saveParam = new HashMap<>();
            saveParam.put("formid", formId);
            saveParam.put("Model", model);

            String jsonSave = new ObjectMapper().writeValueAsString(saveParam);
            String saveResp = getK3CloudApiClient().save(formId, jsonSave);
            log.info("[Save Response] {}", saveResp);

            RepoRet repoSave = gson.fromJson(saveResp, RepoRet.class);
            if (!repoSave.isSuccessfully()) {
                log.error("保存失败: {}", saveResp);
                cleanupOnFailure(getK3CloudApiClient(), formId, fid, fileId);
                return Result.success(saveResp);
            }


            //  提交（Submit）
            String saveFid = repoSave.getResult()
                    .getResponseStatus()
                    .getSuccessEntitys()
                    .get(0)
                    .getId();
            Map<String, Object> submitParam = new HashMap<>();
            submitParam.put("Ids", saveFid); // 这里是字符串类型

            ObjectMapper mapper = new ObjectMapper();
            String jsonParam = mapper.writeValueAsString(submitParam);
            String submitResp = getK3CloudApiClient().submit(formId, jsonParam);
            RepoRet repoSubmit = gson.fromJson(submitResp, RepoRet.class);

            if (!repoSubmit.isSuccessfully()) {
                log.error("提交失败: {}", submitResp);
                cleanupOnFailure(getK3CloudApiClient(), formId, fid, fileId);
                return Result.error( submitResp);
            }

            log.info("供应商列表提交成功！FID={}", saveFid);
            return Result.success(saveFid);
        } catch (Exception e) {
            log.error("保存物料异常", e);
            return Result.error("保存物料异常: " + e.getMessage());

        }
    }

    /**
     * 保存金蝶物料表单数据 - 按照金蝶官方文档规范实现
     *
     * @deprecated 请(MultipartFile[], Bymaterial) 替代
     * 通过工厂类获取：K3FormProcessorFactory.getProcessor("BD_MATERIAL")
     * @return 保存结果JSON字符串
     */
    @Deprecated
    public Result saveMaterialFormData(MultipartFile image,MultipartFile inspectionReport,Bymaterial bymateria) {

        try {
            //业务对象标识
            String formId = "BD_MATERIAL";
            // 构造请求参数
            Map<String, Object> model = buildMaterialModel(bymateria);

            model.put("FNumber", bymateria.getNumber() != null ? bymateria.getNumber() : "");

            Map<String, Object> draftParam = new HashMap<>();
            draftParam.put("Model", model);
            draftParam.put("formid", formId);


            // 将参数转换为JSON字符串
            String draftJson = new ObjectMapper().writeValueAsString(draftParam);
            String draftResp = getK3CloudApiClient().draft(formId, draftJson);
            RepoRet repoDraft = gson.fromJson(draftResp, RepoRet.class);

            if (!repoDraft.isSuccessfully()) {
                log.error("暂存失败: {}", draftResp);
                return Result.error("暂存失败: {}");
            }

            String fid = repoDraft.getResult().getId();
            log.info("暂存成功，FID={}", fid);

            //  上传图片并绑定 FID
            String fileId = null;
            if (image != null && !image.isEmpty()) {
                String uploadResult = uploadMaterialImageToKingdee(image, fid, bymateria.getNumber());
                JsonObject uploadObj = gson.fromJson(uploadResult, JsonObject.class);
                fileId = uploadObj.getAsJsonObject("Result").get("FileId").getAsString();

                model.put("FImageFileServer", fileId);
                log.info("图片上传成功并绑定 FileId={}", fileId);
            }
                //上传验货报告
            if (inspectionReport != null && !inspectionReport.isEmpty()) {
                String uploadResult = uploadMaterialImageToKingdee(inspectionReport, fid, bymateria.getNumber());
                JsonObject uploadObj = gson.fromJson(uploadResult, JsonObject.class);
             String   fyhbgimagel = uploadObj.getAsJsonObject("Result").get("FileId").getAsString();
                model.put("Fyhbg", fyhbgimagel);
            }


            //  更新保存（带 FID）
            model.put("FMATERIALID", fid);
            Map<String, Object> saveParam = new HashMap<>();
            saveParam.put("formid", formId);
            saveParam.put("Model", model);

            String jsonSave = new ObjectMapper().writeValueAsString(saveParam);
            String saveResp = getK3CloudApiClient().save(formId, jsonSave);
            log.info("[Save Response] {}", saveResp);

            RepoRet repoSave = gson.fromJson(saveResp, RepoRet.class);
            if (!repoSave.isSuccessfully()) {
                log.error("保存失败: {}", saveResp);
                cleanupOnFailure(getK3CloudApiClient(), formId, fid, fileId);
                return Result.success(saveResp);
            }


            //  提交（Submit）
            String saveFid = repoSave.getResult()
                    .getResponseStatus()
                    .getSuccessEntitys()
                    .get(0)
                    .getId();
            Map<String, Object> submitParam = new HashMap<>();
            submitParam.put("Ids", saveFid); // 这里是字符串类型

            ObjectMapper mapper = new ObjectMapper();
            String jsonParam = mapper.writeValueAsString(submitParam);
            String submitResp = getK3CloudApiClient().submit(formId, jsonParam);
            RepoRet repoSubmit = gson.fromJson(submitResp, RepoRet.class);

            if (!repoSubmit.isSuccessfully()) {
                log.error("提交失败: {}", submitResp);
                cleanupOnFailure(getK3CloudApiClient(), formId, fid, fileId);
                return Result.error( submitResp);
            }

            log.info("物料提交成功！FID={}", saveFid);
            return Result.success(saveFid);
        } catch (Exception e) {
            log.error("保存物料异常", e);
            return Result.error("保存物料异常: " + e.getMessage());
        }
    }

    /**
     * 修改表单数据
     * */
    public Result updateMaterialFormData(MultipartFile image, MultipartFile inspectionReport, Bymaterial bymateria) {
        String formId = "BD_MATERIAL";

        try {
            // ================= 1. 查询物料是否存在 =================
            JsonArray materialRows = queryMaterialId(bymateria.getNumber());
            if (materialRows == null || materialRows.size() == 0) {
                log.error("未找到物料编码对应的物料: {}", bymateria.getNumber());
                return Result.error("物料编码未存在");
            }

            JsonArray firstRow = materialRows.get(0).getAsJsonArray();
            String fNumber = firstRow.get(0).getAsString();
            String materialId = firstRow.get(1).getAsString();
            String documentStatus = firstRow.get(2).getAsString();

            // ================= 2. 检查审核状态 =================
            if ("B".equalsIgnoreCase(documentStatus)) { // 假设 "C" 代表审核中
                log.warn("物料 {} 当前状态为审核中，无法修改", fNumber);
                return Result.error("物料 " + fNumber + "当前状态为审核中，无法修改");
            }
            if ("D".equalsIgnoreCase(documentStatus)) { // 假设 "C" 代表审核中
                log.warn("物料 {} 当前状态为审核中，无法修改", fNumber);
                return Result.error("物料 " + fNumber + "单据已作废或关闭");
            }

            log.info("查询到物料 FID={}，状态={}", materialId, documentStatus);

            // ================= 2. 构建 Model =================
            Map<String, Object> model = buildMaterialModel(bymateria);
            model.put("FMATERIALID", materialId);
           // model.put("FNumber", bymateria.getNumber()); // 保留编码字段

            // 上传图片
            String fileId = null;
            if (image != null && !image.isEmpty()) {
                String uploadResult = uploadMaterialImageToKingdee(image, materialId, bymateria.getNumber());
                JsonObject uploadObj = gson.fromJson(uploadResult, JsonObject.class);
                fileId = uploadObj.getAsJsonObject("Result").get("FileId").getAsString();

                model.put("FImageFileServer", fileId);
                log.info("图片上传成功并绑定 FileId={}", fileId);
            }

            // 上传验货报告
            if (inspectionReport != null && !inspectionReport.isEmpty()) {
                String uploadResult = uploadMaterialImageToKingdee(inspectionReport, materialId, bymateria.getNumber());
                JsonObject uploadObj = gson.fromJson(uploadResult, JsonObject.class);
                String fyhbgImage = uploadObj.getAsJsonObject("Result").get("FileId").getAsString();
                model.put("F_yhbg", fyhbgImage);
                log.info("验货报告上传成功并绑定 FileId={}", fyhbgImage);
            }

            // ================= 3. 保存更新 =================
            Map<String, Object> saveParam = new HashMap<>();
            saveParam.put("formid", formId);
            saveParam.put("NeedUpDateFields", Arrays.asList(
                    "FName", "FSpecification", "FDescription1",
                    "FImageFileServer", "F_yhbg",
                    "F_cplb", "FMaterialGroup", "F_XLCP", "F_HSBM1"
            ));
            saveParam.put("Model", model);

            String jsonSave = new ObjectMapper().writeValueAsString(saveParam);
            String saveResp = getK3CloudApiClient().save(formId, jsonSave);
            log.info("[Save Response] {}", saveResp);

            RepoRet repoSave = gson.fromJson(saveResp, RepoRet.class);
            if (!repoSave.isSuccessfully()) {
                log.error("保存失败: {}", saveResp);
                cleanupOnFailure(getK3CloudApiClient(), formId, materialId, fileId);
                return Result.error("保存失败: " + saveResp);
            }

            // ================= 4. 提交 =================
            String saveFid = repoSave.getResult()
                    .getResponseStatus()
                    .getSuccessEntitys()
                    .get(0)
                    .getId();

            Map<String, Object> submitParam = new HashMap<>();
            submitParam.put("CreateOrgId", 0);
            submitParam.put("Ids", saveFid);
            submitParam.put("Numbers", new ArrayList<>());
            submitParam.put("SelectedPostId", 0);
            submitParam.put("NetworkCtrl", false);
            submitParam.put("IgnoreInterationFlag", true);
            submitParam.put("UseOrgId", 0);

            ObjectMapper mapper = new ObjectMapper();
            String jsonSubmit = mapper.writeValueAsString(submitParam);

            String submitResp = getK3CloudApiClient().submit(formId, jsonSubmit);
            log.info("[Submit Response] {}", submitResp);

            RepoRet repoSubmit = gson.fromJson(submitResp, RepoRet.class);
            if (!repoSubmit.isSuccessfully()) {
                log.error("提交失败: {}", submitResp);
                cleanupOnFailure(getK3CloudApiClient(), formId, materialId, fileId);
                return Result.error("提交失败: " + submitResp);
            }

            // ================= 5. 审核（可选） =================
            String auditResp = getK3CloudApiClient().audit(formId, jsonSubmit);
            log.info("[Audit Response] {}", auditResp);

            return Result.success("审核成功", auditResp);

        } catch (Exception e) {
            log.error("更新物料异常", e);
            return Result.success("{\"Error\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * 查询物料是否存在
     * @param fNumber 物料编码
     * @return 物料 Id，如果不存在返回 null
     */
    public JsonArray queryMaterialId(String fNumber) {
        // 构建 FilterString JSON 数组，保证符合 K3Cloud 要求
        String jsonData = String.format(
                "{\n" +
                        "  \"FormId\":\"BD_MATERIAL\",\n" +
                        "  \"FieldKeys\":\"FNumber,FMATERIALID,FDocumentStatus,FOldNumber\",\n" +
                        "  \"FilterString\":\"FNumber='%s'\",\n" +
                        "  \"OrderString\":\"\",\n" +
                        "  \"TopRowCount\":0,\n" +
                        "  \"StartRow\":0,\n" +
                        "  \"Limit\":1,\n" +
                        "  \"SubSystemId\":\"\"\n" +
                        "}", fNumber);


        try {
            // 调用 K3Cloud 接口
            String resultJson = String.valueOf(getK3CloudApiClient().executeBillQuery(jsonData));
            // 解析 JSON
            JsonArray rows = JsonParser.parseString(resultJson).getAsJsonArray();
            return rows;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /** 失败后清理 */
    private void cleanupOnFailure(K3CloudApi client, String formId, String fid, String fileId) {
        try {
            if (StringUtils.isNotBlank(fileId)) {
                cleanupFailedMaterial(formId, fid, fileId);
            }
            if (StringUtils.isNotBlank(fid)) {
                getK3CloudApiClient().delete(formId, fid);
                log.warn("已删除失败的草稿 FID={}", fid);
            }
        } catch (Exception e) {
            log.warn("清理失败: {}", e.getMessage());
        }
    }
    /**
     * 清理失败的物料附件或关联文件
     * 用于在保存/提交失败时，删除暂存状态下的上传图片等资源，防止垃圾数据堆积
     */
    private void cleanupFailedMaterial(String formId, String fid, String fileId) {
        try {
            log.warn("开始清理失败记录：formId={}, fid={}, fileId={}", formId, fid, fileId);

            //  删除上传但未绑定的附件（如果有接口可用）
            if (StringUtils.isNotBlank(fileId)) {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("Id", fileId); // 必须是 Id，不是 FileId

                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("FormId", "BOS_Attachment");
                paramMap.put("Data", dataMap);

                ObjectMapper objectMapper = new ObjectMapper();
                String json = objectMapper.writeValueAsString(paramMap);

                String delFileResp = getK3CloudApiClient().delete("BOS_Attachment", json);
                log.info("删除未绑定附件结果: {}", delFileResp);
            }

            //  删除失败的草稿单据（防止下次保存时编码重复）
            if (StringUtils.isNotBlank(fid)) {
                String delResp = getK3CloudApiClient().delete(formId, fid);
                log.info("删除失败草稿结果: {}", delResp);
            }

        } catch (Exception e) {
            log.error("清理失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 构建供应商Model包
     * */
    public Map<String, Object> saveSupplier(Supplier dto) throws Exception {

        // 构建金蝶 Model 数据结构
        // -------------------------
        Map<String, Object> model = new HashMap<>();

        // 单据头（必填项必须放进去）
        model.put("FSupplierId", 0);
        model.put("FNumber", dto.getNumber() != null ? dto.getNumber() : "");
        model.put("FName", dto.getName() != null ? dto.getName() : "");
        model.put("FShortName", dto.getAbbreviation() != null ? dto.getAbbreviation() : "");

    // 添加 F_fze 字段（与 FBaseInfo 同级）- 必须存在，即使为空
    Map<String, Object> fzeObj = new HashMap<>();
    if (dto.getManager() != null && !dto.getManager().isEmpty()) {
        fzeObj.put("FSTAFFNUMBER", dto.getManager());
    } else {
        fzeObj.put("FSTAFFNUMBER", "");
    }
    model.put("F_fze", fzeObj);

    // 业务字段 - 必须存在，即使为空
    model.put("F_gwzb", dto.getForeignShare() != null ? dto.getForeignShare() : "");
    model.put("F_KPPM", dto.getInvoiceName() != null ? dto.getInvoiceName() : "");
    model.put("Fgcwt", dto.getContactInfo() != null ? dto.getContactInfo() : "");
    model.put("Fxzyy1", dto.getCause() != null ? dto.getCause() : "");

    // 字典字段（需要转换为金蝶编码格式）- 必须存在，即使为空
    putFNumber(model, "Fly", dto.getSupplyType() != null ? dto.getSupplyType() : "");
    putFNumber(model, "FSupplierClassify", dto.getSupplierCategory() != null ? dto.getSupplierCategory() : "");

    // 基础信息子表
    Map<String, Object> baseInfo = new HashMap<>();
    baseInfo.put("FEntryId", 0);
    baseInfo.put("FRegisterCode", dto.getBusinessRegistration() != null ? dto.getBusinessRegistration() : "");
    baseInfo.put("FSOCIALCRECODE", dto.getSocialCreditCode() != null ? dto.getSocialCreditCode() : "");
    baseInfo.put("FLegalPerson", dto.getLegalPerson() != null ? dto.getLegalPerson() : "");
    baseInfo.put("FAddress", dto.getAddress() != null ? dto.getAddress() : "");

    // FFoundDate 应该是字符串格式
    if (dto.getEstablishDate() != null) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        baseInfo.put("FFoundDate", sdf.format(dto.getEstablishDate()));
    } else {
        baseInfo.put("FFoundDate", "");
    }

    // 添加负责人到基础信息子表
    putFNumber(baseInfo, "FStaffId", dto.getManager() != null ? dto.getManager() : "");

    // 添加供应商分类到基础信息子表
    putFNumber(baseInfo, "FSupplierClassify", dto.getSupplierCategory() != null ? dto.getSupplierCategory() : "");

    // 添加主营产品到基础信息子表
    baseInfo.put("F_ora_Text2", dto.getMainProduct() != null ? dto.getMainProduct() : "");

    // 添加供应类别到基础信息子表
    baseInfo.put("FSupplyClassify", dto.getSupplyType() != null ? dto.getSupplyType() : "");

    // 添加注册地址到基础信息子表
    baseInfo.put("FRegisterAddress", dto.getRegion() != null ? dto.getRegion() : "");

    // 添加省份信息到基础信息子表
    putFNumber(baseInfo, "FProvincial", "");

    model.put("FBaseInfo", baseInfo);

    // 业务信息子表
    Map<String, Object> businessInfo = new HashMap<>();
    businessInfo.put("FEntryId", 0);
    model.put("FBusinessInfo", businessInfo);

    // 财务信息子表
    Map<String, Object> financeInfo = new HashMap<>();
    financeInfo.put("FEntryId", 0);

    // FInvoiceType 应该是直接的字符串值，而不是对象
    financeInfo.put("FInvoiceType", dto.getInvoiceType() != null ? dto.getInvoiceType() : "");

    // 其他财务字段 - 必须存在，即使为空
    putFNumber(financeInfo, "FTaxType", dto.getTaxCategory() != null ? dto.getTaxCategory() : "");
    putFNumber(financeInfo, "FPayCurrencyId", dto.getSettlementCurrency() != null ? dto.getSettlementCurrency() : "");
    putFNumber(financeInfo, "FSettleTypeId", dto.getSettlementMethod() != null ? dto.getSettlementMethod() : "");
    putFNumber(financeInfo, "FPayCondition", dto.getPaymentTerms() != null ? dto.getPaymentTerms() : "");
    putFNumber(financeInfo, "FSettleId", dto.getSettlementParty() != null ? dto.getSettlementParty() : "");
    putFNumber(financeInfo, "FChargeId", dto.getPayee() != null ? dto.getPayee() : "");
    putFNumber(financeInfo, "FTaxRateId", dto.getDefaultTaxRate() != null ? dto.getDefaultTaxRate() : "");

    model.put("FFinanceInfo", financeInfo);

    // 银行信息子表（数组）
    List<Map<String, Object>> bankInfoList = new ArrayList<>();
    Map<String, Object> bankInfo = new HashMap<>();
    bankInfo.put("FBankId", 0);
    bankInfo.put("FBankCode", "");
    bankInfo.put("FBankHolder", "");

    // FBankTypeRec 需要使用对象格式 {"FNUMBER": ""}
    Map<String, Object> bankTypeRec = new HashMap<>();
    bankTypeRec.put("FNUMBER", "");
    bankInfo.put("FBankTypeRec", bankTypeRec);

    bankInfo.put("FOpenAddressRec", "");
    bankInfo.put("FOpenBankName", "");

    // FBankCurrencyId 需要使用对象格式 {"FNumber": ""}
    Map<String, Object> bankCurrencyId = new HashMap<>();
    bankCurrencyId.put("FNumber", "");
    bankInfo.put("FBankCurrencyId", bankCurrencyId);

    bankInfo.put("FBankDesc", "");
    bankInfoList.add(bankInfo);
    model.put("FBankInfo", bankInfoList);

    // 地点信息子表（数组）
    List<Map<String, Object>> locationInfoList = new ArrayList<>();
    Map<String, Object> locationInfo = new HashMap<>();
    locationInfo.put("FLocationId", 0);
    locationInfo.put("FLocName", "");

    // FLocNewContact 需要使用对象格式 {"FNUMBER": ""}
    Map<String, Object> locNewContact = new HashMap<>();
    locNewContact.put("FNUMBER", "");
    locationInfo.put("FLocNewContact", locNewContact);

    locationInfo.put("FLocAddress", "");
    locationInfo.put("FLocMobile", "");
    locationInfoList.add(locationInfo);
    model.put("FLocationInfo", locationInfoList);

    // 联系人信息子表（数组）
    List<Map<String, Object>> supplierContactList = new ArrayList<>();
    Map<String, Object> supplierContact = new HashMap<>();
    supplierContact.put("FContactId", 0);
    supplierContactList.add(supplierContact);
    model.put("FSupplierContact", supplierContactList);

    return model;
}
    private void putFNumber(Map<String, Object> model, String field, String number) {
        // 总是添加字段，即使number为空（与SupplierFormProcessor中的实现保持一致）
        Map<String, Object> inner = new HashMap<>();
        if (number != null && !number.isEmpty()) {
            inner.put("FNumber", number);
            log.debug("字段 {} 转换为FNumber格式: {}", field, number);
        } else {
            // 如果number为空，使用空的FNumber对象
            inner.put("FNumber", "");
            log.debug("字段 {} 的值为空，添加空的FNumber对象", field);
        }
        model.put(field, inner);
    }


    private Map<String, Object> map(String key, Object value) {
        Map<String, Object> m = new HashMap<>();
        m.put(key, value);
        return m;
    }

    /** 构建物料 Model */
    private Map<String, Object> buildMaterialModel(Bymaterial bymateria) {
        // 构造Model数据包
        Map<String, Object> Model = new HashMap<>();
        if (bymateria != null) {

         //   Model.put("FNumber", bymateria.getNumber() != null ? bymateria.getNumber() : "");
            Model.put("FName", bymateria.getName() != null ? bymateria.getName() : "");
            Model.put("FSpecification", bymateria.getSpecification() != null ? bymateria.getSpecification() : "");
            Model.put("FDescription1", bymateria.getDescription1() != null ? bymateria.getDescription1() : "");


            //物料属性暂时不推
            //Model.put("FErpClsID", bymateria.getErpClsId() != null ? bymateria.getErpClsId() : "");

            // 改为字典格式
            Map<String, Object> materialGroupMap = new HashMap<>();
            DictionaryTable dictionaryTable=new DictionaryTable();
            if (bymateria.getMaterialgroup() != null && !bymateria.getMaterialgroup().isEmpty()) {
                dictionaryTable.setDictName(bymateria.getMaterialgroup());
                dictionaryTable= dictionaryTableMapper.selectByCondition(dictionaryTable) ;
                materialGroupMap.put("FNumber", dictionaryTable.getDictCode());
            }
            Model.put("FMaterialGroup", materialGroupMap);

            //新老产品
            if (bymateria.getFxlcp() != null) {
                Map<String, Object> productCategoryMap = new HashMap<>();
                BymaterialDictionary bymaterialDictionary=  bymaterialDictionaryMapper.selectByCategoryAndCode("product_type",bymateria.getFxlcp());
                productCategoryMap.put("FNumber", bymaterialDictionary.getKingdee());
                Model.put("F_XLCP",  productCategoryMap);
            }
            // Model.put("FVOLUME", bymateria.getVolume() != null ? bymateria.getVolume() : "");

                Model.put("Fyhbg", bymateria.getInspectionReport() != null ? bymateria.getInspectionReport() : "");
            // HSBM
                Model.put("F_HSBM1", bymateria.getHsbm() != null ? bymateria.getHsbm() : "");
            // 产品类别
            if (bymateria.getProductCategory() != null) {
                BymaterialDictionary bymaterialDictionary=  bymaterialDictionaryMapper.selectByCategoryAndName(bymateria.getProductCategory());
                Map<String, Object> productCategoryMap = new HashMap<>();
                productCategoryMap.put("FNumber", bymaterialDictionary.getCode());
                Model.put("F_cplb", productCategoryMap);
            }
        }
        return Model;
    }

    /**
     * 分页查询采购订单列表数据
     */
    public List<List<Object>> queryPurchaseOrderPage(int startRow, int pageSize) {

        try {

            String jsonData = "{"
                + "\"FormId\":\"PUR_PurchaseOrder\","
                + "\"FieldKeys\":\"FID,FBillNo,FDate,FBusinessType,FBillTypeID,FSupplierId,FCreatorId ,FCreateDate,FModifierId,FModifyDate,FApproverId,FApproveDate,FCloseStatus ,F_gyszh,FACCTYPE,F_gdy1,\n" +
                "F_ora_Date,FPayConditionId,F_XSY,F_KHJQNEW,FChangeReason,F_gcbz,F_cty_BaseProperty2,F_ZLBZ,F_BZBZJYQ,Fjsfsjjq,Fhttk13,Fhttk510,\n" +
                "FProviderContactId,FProviderJob,FProviderAddress,FSettleId,FChargeId, FSettleCurrId,FEntrySettleModeId,FExchangeTypeId,\n" +
                "FExchangeRate,FPriceListId,FPriceTimePoint,FDepositRatio,FBillTaxAmount_LC,FBillAmount_LC,FBillAllAmount,FLocalCurrId,FBillTaxAmount,\n" +
                "FAllAmount,FEntryAmount,FIsIncludedTax,FISPRICEEXCLUDETAX,FDeposit,FAllDisCount,\n" +
                "FALLPAYAMOUNT,FALLPAYAPPLYAMOUNT,FALLAPPYNOPAYAMOUNT,FPAYRELATAMOUNT,FALLREFUNDAMOUNT,FALLNOPAYAMOUNT,FBillAmount,FBillAllAmount_LC\n\","
                + "\"OrderString\":\"FCreateDate ASC\","
                + "\"TopRowCount\":0,"
                + "\"StartRow\":" + startRow + ","
                + "\"Limit\":" + pageSize + ","
                + "\"SubSystemId\":\"\""
                + "}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            log.error("查询采购订单异常", e);
            return null;
        }
    }
    /**
     * 分页查询采购订单详情列表数据
     */
    public List<List<Object>> purchaseOrderBillheadEentry(int startRow, int pageSize) {

        try {

            String jsonData = "{"
                + "\"FormId\":\"PUR_PurchaseOrder\","
                + "\"FieldKeys\":\"FID,FBillNo,F_sfbg,F_jqhx,F_cplb,F_bzbz,F_khhh,F_cpdm,F_ora_BaseProperty,F_ora_BaseProperty1,F_GYSWLBM,F_GYSWLMC,FQty,F_Cpsl,FUnitId,FPriceUnitId,\n" +
                "FDeliveryDate,F_Ckj,F_cbj,FPrice,FTaxPrice,F_jjhsdj,F_kphsdj,FEntryDiscountRate,FEntryTaxRate,FEntryTaxAmount,FAllAmount,FEntryAmount,FEntryNote,\n" +
                "FBillStatus,F_zxs,F_mzz,F_xs,F_bcfysl,FBillStatus1,F_xtslddsl,FBillStatus3,F_yhbl,F_bzfs,F_smsfj,F_cpgys,F_bcjsd,F_bcjsr,F_bcjsrdh,F_bgdw,F_sbys," +
                "F_CYS,F_KDDH,F_JCRQ,F_jgtzyy,F_cpzlyq,Fbzyq,Ftsyq,Fcptp,Fbzgctg,Fgchh,Fxsddh,Fshrq,Fxddrkts,Fbjr,F_CPLB1\n\","
                + "\"OrderString\":\"FCreateDate ASC\","
                + "\"TopRowCount\":0,"
                + "\"StartRow\":" + startRow + ","
                + "\"Limit\":" + pageSize + ","
                + "\"SubSystemId\":\"\""
                + "}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            log.error("查询采购订单异常", e);
            return null;
        }
    }


    /**
     * 查询金蝶客户主表数据（全量）
     * @return 客户主表数据列表
     */
    public List<List<Object>> queryCustomerList() {
        try {
            String jsonData = "{"
                + "\"FormId\":\"BD_Customer\","
                + "\"FieldKeys\":\"FCUSTID,FNumber,FName,FDocumentStatus,FShortName,F_khqc,FDescription,"
                + "FCreateOrgId,FCreatorId,FModifierId,FSeller,F_kfxsy1,FSalDeptId,FSalGroupId,FCustTypeId,"
                + "FCreateDate,FModifyDate,FFoundDate,F_khzrrq,F_ZMMTTP1,F_ZMMTTP2,F_ZMMTMS,F_CMMTTP1,F_CMMTTP2,F_CMMTMS,"
                + "FCountry,FProvincial,FAddress,FRegisterAddress,FTel,FWebsite,"
                + "FTradingCurrId,FReceiveCurrId,FSettleTypeId,FRecConditionId,FPriceListId,"
                + "FTaxType,FTaxRate,FGroup,F_khly,F_ly,F_sylx,F_khgm,F_khzy,FIsGroup,FIsDefPayer,FLegalPerson,FInvoiceType,"
                + "FSupplierId,F_bzyq,Fbzfs,F_fhyq,F_zlbzhjsyq,F_sfysqs,Fsbsq,Fsfsd,Fsfts,"
                + "F_cty_Decimal,F_tcfpfa,Fpjskzq,F_mjll,F_DYGJ,FAPPROVEDATE,FAPPROVERID,"
                + "F_cty_LargeText,F_KHLOGO,FCPAdminCode,F_Youtube,F_linkedin,F_facebook,F_twitter,F_instagram,F_vk,F_facebookmess,F_skype,F_whatsapp," +
                "FWeChat,F_qq,Ftn,F_Yolo,F_Hangouts,F_Viber,\","
                + "\"OrderString\":\"FCreateDate ASC\","
                + "\"TopRowCount\":0,"
                + "\"StartRow\":0,"
                + "\"Limit\":5000000,"
                + "\"SubSystemId\":\"\""
                + "}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            log.error("查询客户主表数据异常", e);
            return new ArrayList<>();
        }
    }

    /**
     * 查询金蝶客户银行信息数据
     * @return 客户银行信息数据列表
     */
    public List<List<Object>> queryCustomerBankList() {
        try {
            String jsonData = "{"
                + "\"FormId\":\"BD_Customer\","
                + "\"FieldKeys\":\"FNumber,FCOUNTRY1,FBANKCODE,FACCOUNTNAME,FBankTypeRec,FOpenAddressRec,FOPENBANKNAME\n\","
                + "\"OrderString\":\"FCreateDate ASC\","
                + "\"TopRowCount\":0,"
                + "\"StartRow\":0,"
                + "\"Limit\":5000000,"
                + "\"SubSystemId\":\"\""
                + "}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            log.error("查询客户银行信息数据异常", e);
            return new ArrayList<>();
        }
    }

    /**
     * 查询金蝶客户转让信息数据（全量）
     * @return 客户转让信息数据列表
     */
    public List<List<Object>> queryCustomerTransferList() {
        try {
            String jsonData = "{"
                + "\"FormId\":\"BD_Customer\","
                + "\"FieldKeys\":\"FNumber,F_zrr,F_jsr,F_zrrq,F_tcbl\","
                + "\"OrderString\":\"FCreateDate ASC\","
                + "\"TopRowCount\":0,"
                + "\"StartRow\":0,"
                + "\"Limit\":5000000,"
                + "\"SubSystemId\":\"\""
                + "}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            log.error("查询客户转让信息数据异常", e);
            return new ArrayList<>();
        }
    }

    /**
     * 查询客户联系人
     */


    public List<List<java.lang.Object>> queryCommonContactList() {
        try {
            String jsonData = "{" +
                "\"FormId\": \"BD_CommonContact\",\n" +
                "\"FieldKeys\":\"FCONTACTID,FNumber,FName,FCreatorId,FCreateDate,FDescription,Fex,FPost,FCompanyType,FMobile,FEmail,FBizLocation,FBizAddress,F_ora_Text,F_wx\n\"," +
                "\"OrderString\":\"\"," +
                "\"TopRowCount\":0,\"StartRow\":0," +
                "\"Limit\":5000000,\"SubSystemId\":\"\"}";

            return  getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>(); // 返回空列表而不是null
    }
    /**
     * 查询员工列表
     */

    public List<List<java.lang.Object>> queryCommonEmployeeList() {
        try {
            String jsonData = "{" +
                "\"FormId\":\"BD_Empinfo\"," +
                "\"FieldKeys\":\"FID,FName,FDescription,FStaffNumber,F_ywm ,F_SFZH,F_cty_Date,F_zzrq,F_nl,FMobile,FAddress,\n" +
                "F_ora_Text,F_ora_Date1,F_ora_Combo,F_ora_Text1,F_XB,F_hkszd,F_mz,F_lzrq,F_sbrq,F_sbje,F_dkyl,F_dkyliao,F_dksy,\n" +
                "F_jsdj,F_ldht,F_bxrq,F_gwgz,F_jx,F_mltcd,F_ymtcd,F_BMJT,F_PEUU_ImageFileServer_qtr,F_PEUU_Date_83g,F_PEUU_ImageFileServer_apv,\n" +
                "F_PEUU_ImageFileServer_tzk,F_PEUU_ImageFileServer_ca9,F_PEUU_ImageFileServer_uky,F_PEUU_ImageFileServer_dvn,\n" +
                "FTel,FEmail,F_bw,F_JJLXR,F_JJLXRDH,F_jbgz,F_jltcd,F_ora_Date,FCreatorId,FCreateDate,FModifierId,FModifyDate,FBaseProperty \n\n\"," +
                "\"OrderString\":\"\"," +
                "\"TopRowCount\":0,\"StartRow\":0," +
                "\"Limit\":5000000,\"SubSystemId\":\"\"}";

            return  getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>(); // 返回空列表而不是null
    }
    /**
     * 查询员工财务列表
     */

    public List<List<java.lang.Object>> queryEmployeeBankList() {
        try {
            String jsonData = "{" +
                "\"FormId\":\"BD_Empinfo\"," +
                "\"FieldKeys\":\"FStaffNumber,FName,FBankCode,FOpenBankName,FBankTypeRec,FBankHolder,FOpenAddressRec\n\"," +
                "\"OrderString\":\"\"," +
                "\"TopRowCount\":0,\"StartRow\":0," +
                "\"Limit\":5000000,\"SubSystemId\":\"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>(); // 返回空列表而不是null
    }
    /**
     * 查询员工跟进情况记录列表
     */

    public List<List<Object>> queryEmployeeTransferPositionList() {
        try {
            String jsonData = "{" +
                "\"FormId\":\"BD_Empinfo\"," +
                "\"FieldKeys\":\"FStaffNumber,FName,Fzzzt,Fgtrq,Fgtsx,Fgsqk,Fgztg,Fxztzsj,Fxztzyy,Fgztzd,Fgwtzd,Fywsjb,F_gztzd,F_gwtzd,F_ywsjb,F_zztzh,F_gttzb\"," +
                "\"OrderString\":\"\"," +
                "\"TopRowCount\":0,\"StartRow\":0," +
                "\"Limit\":5000000,\"SubSystemId\":\"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>(); // 返回空列表而不是null
    }
}
