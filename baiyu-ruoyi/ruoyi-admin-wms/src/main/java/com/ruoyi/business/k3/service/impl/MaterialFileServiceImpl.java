package com.ruoyi.business.k3.service.impl;




import com.ruoyi.business.k3.service.KingdeeMaterialServicer;
import com.ruoyi.business.k3.service.MaterialFileService;
import com.ruoyi.business.util.ImportResult;
import com.ruoyi.business.util.MinioUtil;
import com.ruoyi.business.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static cn.dev33.satoken.SaManager.log;


@Service
public class MaterialFileServiceImpl implements MaterialFileService {

    @Autowired
    private MinioUtil minioUtil;
    @Autowired
    private KingdeeMaterialServicer kingdeeMaterialService;
    /**
     * 上传物料相关文件到MinIO
     * @param file 文件
     * @param number 物料编码
     * @param fileType 文件类型 (images/inspection_reports)
     * @return 文件URL
     */
    public String uploadMaterialFile (MultipartFile file, String number, String fileType) throws Exception {
        if (file == null || file.isEmpty() || number == null || number.isEmpty()) {
            return null;
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String safeNumber = number.replace("/", "_");
        String fileName = "material/" + fileType + "/" +
                java.time.LocalDate.now().toString().replace("-", "/") + "/" +
                safeNumber + fileExtension;

        return minioUtil.uploadFile(file, fileName);
    }

    @Override
    public void deleteMaterialFile(String fileUrl) {
        minioUtil.deleteFile(fileUrl);
    }

    @Override
    public List<ImportResult> batchImportMaterials(List<Map<String, Object>> dataList) {
        List<CompletableFuture<ImportResult>> futures = new ArrayList<>();

        // 使用线程池处理导入任务
          for (int i = 0; i < dataList.size(); i++) {
                final int index = i;
                final Map<String, Object> rowData = dataList.get(i);

//                if (isDebugMode) {
                    // 同步执行便于调试
                    try {
                        ImportResult result = kingdeeMaterialService.processSingleDataRow(rowData, index);
                        futures.add(CompletableFuture.completedFuture(result));
                    } catch (Exception e) {
                        String materialNumber = rowData != null ?
                                StringUtils.getStringValue(rowData.get("编码")) : "未知编码";
                        log.error("同步处理数据行失败: {}", materialNumber, e);
                        ImportResult errorResult = new ImportResult(materialNumber, "处理失败: " + e.getMessage(), false);
                        futures.add(CompletableFuture.completedFuture(errorResult));
                    }
                }
            return futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
//
    }
    /**
     * 校验Excel导入的物料数据
     * @param data Excel行数据
     * @param index 行索引
     * @param number 物料编码
     * @return 校验结果，如果校验通过返回null，否则返回ImportResult
     */
    public ImportResult validateMaterialData(Map<String, Object> data, int index, String number) {
        try {

            // 1. 基础字段非空校验
            // 尝试带*和不带*两种键名
            String name = StringUtils.getStringValue(data.get("名称*"));


            log.info("提取的名称字段值: {}", name);


            if (StringUtils.isEmpty(number)) {
                return new ImportResult("第" + (index + 1) + "行", "编码不能为空", false);
            }
            if (StringUtils.isEmpty(name)) {
                return new ImportResult(number, "名称*不能为空", false);
            }
            // if (StringUtils.isEmpty(specification)) {
            //     return new ImportResult(specification, "规格型号不能为空", false);
            // }

            // 2. 物料属性、产品类别、新老产品校验
            // 尝试带*和不带*两种键名
            String materialGroup = StringUtils.getStringValue(data.get("物料分组*"));

            String erpClsId = StringUtils.getStringValue(data.get("物料属性*"));
            String productCategory = StringUtils.getStringValue(data.get("产品类别*"));


            String isNewProduct = StringUtils.getStringValue(data.get("新老产品*"));

            log.info("提取的字段值 - 物料分组: {}, 物料属性: {}, 产品类别: {}, 新老产品: {}",
                materialGroup, erpClsId, productCategory, isNewProduct);
            if (StringUtils.isEmpty(materialGroup)){
                return new ImportResult(number, "物料分组不能为空", false);
            }
            if (StringUtils.isEmpty(erpClsId)){
                return new ImportResult(number, "物料属性不能为空", false);
            }
            if (StringUtils.isEmpty(productCategory)){
                return new ImportResult(number, "产品类别不能为空", false);
            }
//            if (StringUtils.isEmpty(isNewProduct)){
//                return new ImportResult(number, "新老产品不能为空", false);
//            }


            // 校验物料分组是否存在
            if (kingdeeMaterialService.isMaterialGroupExists(materialGroup) != true) {
                log.error("导入物料失败：物料编码[" + number + "]，物料分组[" + materialGroup + "]不存在");
                return new ImportResult(number, "物料分组【"+materialGroup+"】不存在",false);
            }
            // 校验物料属性是否存在
            if (kingdeeMaterialService.isErpClsIdExists(erpClsId) != true) {
                log.error("导入物料失败：物料编码[" + number + "]，物料属性[" + erpClsId + "]不存在");
                return new ImportResult(number, "物料属性[" + erpClsId + "]不存在", false);
            }

            // 校验产品类别是否存在
            if (kingdeeMaterialService.isProductCategoryExists(productCategory) != true) {
                log.error("导入物料失败：物料编码[" + number + "]，产品类别[" + productCategory + "]不存在");
                return new ImportResult(number, "产品类别【"+productCategory+"】不存在", false);
            }

//            // 校验新老产品标识是否有效
//            if (kingdeeMaterialService.isNewProductFlagValid(isNewProduct) != true) {
//                log.error("导入物料失败：物料编码[" + number + "]，新老产品标识[" + isNewProduct + "]无效");
//                return new ImportResult(number, "新老产品【" + isNewProduct + "】无效", false);
//            }
        } catch (Exception e) {
            log.error("校验物料数据失败，行号: {}", index, e);
            return new ImportResult("第" + (index + 1) + "行", "数据校验失败: " + e.getMessage(), false);
        }

        // 校验通过，返回物料编码
        return new ImportResult(number, "校验通过", true);
    }


}
