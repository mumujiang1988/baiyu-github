package com.ruoyi.business.Component;

import com.ruoyi.business.entity.PriceList;
import com.ruoyi.business.entity.PriceListEntry;
import com.ruoyi.business.k3.config.AbstractK3FormProcessor;
import com.ruoyi.business.util.MinioUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

import static cn.dev33.satoken.SaManager.log;

/**
 * 采购价目表单处理器
 * 负责处理PUR_PriceCategory表单的构建和提交
 * 继承AbstractK3FormProcessor，实现采购价目表特定的业务逻辑
 */
@Component
public class PriceListFormProcessor extends AbstractK3FormProcessor<PriceList> {

    @Autowired
    private MinioUtil minioUtil;

    /**
     * 返回采购价目表单ID
     * @return 固定返回"PUR_PriceCategory"
     */
    @Override
    protected String getFormId() {
        return "PUR_PriceCategory";
    }

    /**
     * 获取文件字段名称列表
     * 采购价目表本身没有主表文件字段，但明细可能有图片
     * 这里返回空列表，因为文件上传在明细级别处理
     */
    @Override
    protected List<String> getFileFieldNames() {
        // 采购价目表本身没有主表文件字段，但明细可能有图片
        // 这里返回空列表，因为文件上传在明细级别处理
        return new ArrayList<>();
    }

    /**
     * 获取采购价目表单据编号
     * 用于文件上传时的BillNO字段
     */
    @Override
    protected String getDocumentNumber(PriceList formData) {
        return formData.getFNumber() != null ? formData.getFNumber() : "";
    }

    /**
     * 上传条目级文件（针对子表中的文件字段）
     * 处理价目表明细中的F_TP1字段文件上传
     * 需要先暂存主表和明细表，然后使用返回的明细表FID作为上传文件的绑定FId
     */
    @Override
    protected List<String> uploadEntryLevelFiles(Map<String, Object> model, String fid, PriceList formData) throws Exception {
        List<String> uploadedFileIds = new ArrayList<>();

        // 获取明细列表
        List<PriceListEntry> entries = formData.getEntries();
        if (entries == null || entries.isEmpty()) {
            return uploadedFileIds;
        }

        // 获取FPriceListEntry（明细列表）
        Object fEntityObj = model.get("FPriceListEntry");
        if (!(fEntityObj instanceof List)) {
            return uploadedFileIds;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> fEntityList = (List<Map<String, Object>>) fEntityObj;

        // 遍历每个明细条目，处理文件上传
        for (int i = 0; i < Math.min(entries.size(), fEntityList.size()); i++) {
            PriceListEntry entry = entries.get(i);
            Map<String, Object> entryMap = fEntityList.get(i);

            // 检查该明细是否有文件URL需要上传到金蝶
            if (entry.getFTP1() != null && !entry.getFTP1().isEmpty()) {
                try {
                    // 获取条目中的FEntryID（根据金蝶API文档，这是实际的条目ID）
                    Object entryIdObj = entryMap.get("FEntryID");
                    if (entryIdObj == null) {
                        log.warn("价目表明细缺少FEntryID，跳过文件上传: 条目索引={}", i);
                        continue;
                    }

                    // 上传文件到金蝶，指定条目键
                    // 条目键格式：FPriceListEntry_FEntryID（根据金蝶API文档）
                    String entryKey = "FPriceListEntry_" + entryIdObj.toString();
                    String fileId = uploadFileWithEntryKey(
                        createMultipartFileFromUrl(entry.getFTP1(), entry.getFMaterialId()),
                        fid,
                        getDocumentNumber(formData),
                        "F_TP1",
                        entryKey
                    );

                    if (fileId != null) {
                        // 将文件ID绑定到条目模型中（替换原来的URL）
                        entryMap.put("F_TP1", fileId);
                        uploadedFileIds.add(fileId);
                        log.info("价目表明细文件上传成功: 条目ID={}, FileId={}", entryIdObj, fileId);
                    }
                } catch (Exception e) {
                    log.error("上传价目表明细文件失败: 条目索引={}, 错误={}", i, e.getMessage(), e);
                }
            }
        }

        return uploadedFileIds;
    }

    /**
     * 根据文件URL创建MultipartFile对象
     * 从MinIO下载文件内容并创建MultipartFile
     */
    private MultipartFile createMultipartFileFromUrl(String fileUrl, String materialId) {
        try {
            // 从URL中提取对象名称
            String objectName = minioUtil.extractObjectNameFromUrl(fileUrl);
            if (objectName == null || objectName.isEmpty()) {
                log.warn("无法从URL提取对象名称: {}", fileUrl);
                return null;
            }

            // 从MinIO下载文件
            InputStream inputStream = minioUtil.downloadFile(objectName);
            if (inputStream == null) {
                log.warn("无法从MinIO下载文件: {}", objectName);
                return null;
            }

            // 读取文件内容到字节数组
            byte[] fileBytes = inputStreamToBytes(inputStream);

            // 获取文件名
            String fileName = "price_list_entry_image.jpg"; // 默认文件名
            if (objectName.contains("/")) {
                fileName = objectName.substring(objectName.lastIndexOf("/") + 1);
            }

            return new ByteArrayMultipartFile(fileBytes, fileName, fileName, "image/jpeg");
        } catch (Exception e) {
            log.error("从URL创建MultipartFile失败: url={}, materialId={}", fileUrl, materialId, e);
            return null;
        }
    }

    /**
     * 将InputStream转换为字节数组
     */
    private byte[] inputStreamToBytes(InputStream inputStream) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    /**
     * ByteArrayMultipartFile - 用于从字节数组创建MultipartFile
     */
    private static class ByteArrayMultipartFile implements MultipartFile {
        private final byte[] content;
        private final String name;
        private final String originalFilename;
        private final String contentType;

        public ByteArrayMultipartFile(byte[] content, String name, String originalFilename, String contentType) {
            this.content = content;
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content == null || content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(java.io.File dest) throws java.io.IOException {
            java.nio.file.Files.write(dest.toPath(), content);
        }
    }

    /**
     * 构建采购价目表模型数据
     * 将前端传入的PriceList对象转换为金蝶K3所需的格式
     */
    @Override
    protected Map<String, Object> buildModel(PriceList priceList) {
        log.info("开始构建采购价目表模型数据，价目表编号: {}", priceList.getFNumber());

        Map<String, Object> model = new HashMap<>();

        // ================= 基础字段 =================
        model.put("FID", priceList.getPriceListId() != null ? priceList.getPriceListId() : 0); // 新增时为0，更新时为实际ID
        model.put("FName", priceList.getFName() != null ? priceList.getFName() : "");
        model.put("FNumber", priceList.getFNumber() != null ? priceList.getFNumber() : "");
        model.put("FDescription", priceList.getFDescription() != null ? priceList.getFDescription() : "");
        model.put("F_GYSLB", priceList.getFGYSLB() != null ? priceList.getFGYSLB() : "");

        // 币别字段处理
        if (priceList.getFCurrencyID() != null) {
            Map<String, Object> currencyObj = new HashMap<>();
            currencyObj.put("FNumber", priceList.getFCurrencyID().toString());
            model.put("FCurrencyID", currencyObj);
        }

        // 供应商字段处理
            Map<String, Object> supplierObj = new HashMap<>();
        if (priceList.getFSupplierID() != null&& priceList.getFSupplierID().isEmpty()) {
            supplierObj.put("FNumber", priceList.getFSupplierID());
        }else {
            supplierObj.put("FNumber", "");
        }


        // 供应商类别字段处理


        // 定价人字段处理
        if (priceList.getFPricer() != null) {
            model.put("FPricer", priceList.getFPricer());
        }

        // 价目表对象字段处理
        if (priceList.getFPriceObject() != null) {
            model.put("FPriceObject", priceList.getFPriceObject());
        }

        // 价格类型字段处理
        if (priceList.getFPriceType() != null) {
            model.put("FPriceType", priceList.getFPriceType());
        }

        // ================= 价目表明细子表 =================
        if (priceList.getEntries() != null && !priceList.getEntries().isEmpty()) {
            List<Map<String, Object>> entryList = buildEntryList(priceList.getEntries());
            model.put("FPriceListEntry", entryList);
        } else {
            // 如果没有明细，添加一个空的明细列表
            model.put("FPriceListEntry", new ArrayList<Map<String, Object>>());
        }

        log.debug("采购价目表模型构建完成: {}", model);
        return model;
    }

    /**
     * 构建价目表明细列表
     * 将价目表明细对象转换为金蝶子表格式
     *
     * @param entries 价目表明细列表
     * @return 金蝶格式的明细数组
     */
    private List<Map<String, Object>> buildEntryList(List<PriceListEntry> entries) {
        List<Map<String, Object>> entryList = new ArrayList<>();

        for (int i = 0; i < entries.size(); i++) {
            PriceListEntry entry = entries.get(i);
            Map<String, Object> entryMap = new HashMap<>();

            // 子表序号（从1开始）
            entryMap.put("FEntryID", i + 1);

            // 物料相关信息
            if (entry.getFMaterialId() != null) {
                Map<String, Object> materialObj = new HashMap<>();
                materialObj.put("FNumber", entry.getFMaterialId());
                entryMap.put("FMaterialId", materialObj);
            } else {
                entryMap.put("FMaterialId", "");
            }
            entryMap.put("FMaterialName", entry.getFMaterialName() != null ? entry.getFMaterialName() : "");

            // 价格信息
            entryMap.put("FPrice", entry.getFPrice() != null ? entry.getFPrice() : 0);
            entryMap.put("FTaxPrice", entry.getFTaxPrice() != null ? entry.getFTaxPrice() : 0);

            // 工厂货号
            if (entry.getFGCHH() != null) {
                entryMap.put("F_GCHH", entry.getFGCHH());
            }

            // 基础属性
            if (entry.getFctyBaseProperty() != null) {
                entryMap.put("F_cty_BaseProperty", entry.getFctyBaseProperty());
            }

            // 规格说明
            if (entry.getFGGSM() != null) {
                entryMap.put("F_GGSM", entry.getFGGSM());
            }

            // 备注
            if (entry.getFNote() != null) {
                entryMap.put("FNote", entry.getFNote());
            }

            // 税率
            entryMap.put("FTaxRate", entry.getFTaxRate() != null ? entry.getFTaxRate() : 0);

            // 计价单位
            if (entry.getFUnitID() != null) {
                Map<String, Object> unitObj = new HashMap<>();
                unitObj.put("FNumber", entry.getFUnitID());
                entryMap.put("FUnitID", unitObj);
            } else {
                entryMap.put("FUnitID", "");
            }


            // 生效日期和失效日期
            if (entry.getFEntryEffectiveDate() != null) {
                entryMap.put("FEntryEffectiveDate", entry.getFEntryEffectiveDate());
            }

            if (entry.getFEntryExpiryDate() != null) {
                entryMap.put("FEntryExpiryDate", entry.getFEntryExpiryDate());
            }

            // 包装说明
            if (entry.getFbzsm() != null) {
                entryMap.put("F_bzsm", entry.getFbzsm());
            }

            // 工厂图片
            if (entry.getFTP1() != null) {
                entryMap.put("F_TP1", entry.getFTP1());
            }

            entryList.add(entryMap);
            log.debug("添加价目表明细: 物料={}, 价格={}", entry.getFMaterialName(), entry.getFPrice());
        }

        return entryList;
    }
}
