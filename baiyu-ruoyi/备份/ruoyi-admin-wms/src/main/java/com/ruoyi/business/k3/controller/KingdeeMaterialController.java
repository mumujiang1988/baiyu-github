
package com.ruoyi.business.k3.controller;



import cn.dev33.satoken.annotation.SaCheckPermission;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.Component.K3FormProcessorFactory;
import com.ruoyi.business.bo.BymaterialBo;
import com.ruoyi.business.entity.*;
import com.ruoyi.business.k3.config.AbstractK3FormProcessor;
import com.ruoyi.business.k3.config.k3config;
import com.ruoyi.business.k3.service.KingdeeMaterialServicer;
import com.ruoyi.business.k3.service.MaterialFileService;
import com.ruoyi.business.k3.service.impl.BymaterialService;
import com.ruoyi.business.mapper.BymaterialDictionaryMapper;
import com.ruoyi.business.mapper.DictionaryTableMapper;
import com.ruoyi.business.util.*;

import com.ruoyi.business.vo.BymaterialVo;
import com.ruoyi.business.vo.Bymaterials;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.excel.utils.ExcelUtil;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/k3/material")
@Slf4j
public class KingdeeMaterialController {


    @Resource
    private k3config k3configks;

    @Autowired
    private K3FormProcessorFactory k3FormProcessorFactory;
    @Autowired
    private MinioUtil minioUtil;
    @Autowired
    private ExceptionUtil exceptionUtil;
    @Autowired
    private KingdeeMaterialServicer kingdeeMaterialServicer;
    @Autowired
    private MaterialFileService materialFileService;
    @Autowired
    private BymaterialService bymaterialService;

    @Autowired
    private DictionaryTableMapper dictionaryLookupMapper;

    @Autowired
    private BymaterialDictionaryMapper dictionaryMapper;


    /**
     * 同步金蝶物料列表（分页查询，每次最多10000条）
     */
    @PostMapping("/login")
    @Transactional(rollbackFor = Exception.class)
    public Result login() {
        // 先查询产品类别（只需查询一次）
        List<List<Object>> queryProductCategories = k3configks.queryProductCategories();

        int pageSize = 10000;  // 每页最多10000条
        int startRow = 0;
        int totalProcessed = 0;

        log.info("开始同步金蝶物料数据，每页{}条", pageSize);

        while (true) {
            // 分页查询物料数据
            List<List<Object>> materialList = k3configks.queryMaterialList(startRow, pageSize, null);

            if (materialList == null || materialList.isEmpty()) {
                log.info("物料数据同步完成，共处理{}条记录", totalProcessed);
                break;
            }

            int currentBatchSize = materialList.size();
            log.info("查询第{}页数据，起始行={}，获取记录数={}", (startRow / pageSize) + 1, startRow, currentBatchSize);

            // 调用服务层处理数据（使用原有的多线程处理逻辑）
            kingdeeMaterialServicer.queryMaterialList(materialList, queryProductCategories);

            totalProcessed += currentBatchSize;

            // 如果返回的数据小于页大小，说明已经是最后一页
            if (currentBatchSize < pageSize) {
                log.info("物料数据同步完成，共处理{}条记录", totalProcessed);
                break;
            }

            // 移动到下一页
            startRow += pageSize;
        }

        return Result.success("同步完成，共处理" + totalProcessed + "条物料数据");
    }


    /**
     * 新增物料
     */
    @SaCheckPermission("k3:material:add")
    @PostMapping(value = "/add",produces = "application/json;charset=UTF-8")
    // 新增：接收独立的文件参数（required = false 允许为空）
    @Transactional(rollbackFor = Exception.class)
    public Result addMaterial(@RequestPart("bymaterial") Bymaterial bymaterial,
                              @RequestPart(value = "image", required = false) MultipartFile image,
                              @RequestPart(value = "inspectionReport", required = false) MultipartFile inspectionReport) {

        try {

            // 检查物料编码是否已存在
            if (bymaterial.getNumber() != null && !bymaterial.getNumber().isEmpty()) {
                Bymaterial existingMaterial = kingdeeMaterialServicer.getMaterialByNumberDirect(bymaterial.getNumber());
                if (existingMaterial != null) {
                    return Result.error("物料编码已存在: " + bymaterial.getNumber());
                }
            }
            // 如果有图片文件，上传到MinIO
            if (image != null && !image.isEmpty()) {
                String imageUrl = materialFileService.uploadMaterialFile(image, bymaterial.getNumber(), "images");
                if (imageUrl != null) {
                    bymaterial.setImage(imageUrl);
                }
            }

            // 如果有验货报告文件，上传到MinIO
            if (inspectionReport != null && !inspectionReport.isEmpty()) {
                String originalFilename = inspectionReport.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String fileName = "material/inspection_reports/" +
                        java.time.LocalDate.now().toString().replace("-", "/") + "/" +
                        bymaterial.getNumber() + fileExtension;

                // 上传文件到MinIO
                String reportUrl = minioUtil.uploadFile(inspectionReport, fileName);
                bymaterial.setInspectionReport(reportUrl);
            }
            // 使用新的工厂模式处理物料表单
            AbstractK3FormProcessor<Bymaterial> processor = k3FormProcessorFactory.getProcessor("BD_MATERIAL");
            MultipartFile[] files = {image, inspectionReport}; // 顺序对应 FImageFileServer, Fyhbg
            Result submitResult = processor.processForm(files, bymaterial);


            if (submitResult.isEmpty()||!submitResult.isSuccess()){
                log.error("物料推送金蝶失败: " + submitResult);
                return Result.error("物料推送金蝶失败: ");
            }
            return kingdeeMaterialServicer.addMaterial(bymaterial);
        } catch (Exception e) {
            log.error("添加物料失败", e);
            return Result.error("物料添加失败: " + e.getMessage());
        }
    }

    /**
     * 更新物料（支持图片上传）
     */
    @SaCheckPermission("k3:material:edit")
    @PutMapping("/update")
    @Transactional(rollbackFor = Exception.class)
    public Result updateMaterial(@RequestPart("material") Bymaterial bymaterial,
                                 @RequestPart(value = "image", required = false) MultipartFile image,
                                 @RequestPart(value = "inspectionReport", required = false) MultipartFile inspectionReport) {
        try {
//            LoginAccount loginAccount = SaTokenUtil.getLoginAccount();

            // 如果有图片文件，上传到MinIO
            if (image != null && !image.isEmpty()) {
                String imageUrl = materialFileService.uploadMaterialFile(image, bymaterial.getNumber(), "images");
                if (imageUrl != null) {
                    bymaterial.setImage(imageUrl);
                }
            }
            // 如果有验货报告文件，上传到MinIO
            if (inspectionReport != null && !inspectionReport.isEmpty()) {
                // 生成文件路径：material/inspection_reports/年/月/日/文件名
                String originalFilename = inspectionReport.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String fileName = "material/inspection_reports/" +
                        java.time.LocalDate.now().toString().replace("-", "/") + "/" +
                        bymaterial.getNumber() + fileExtension;

                // 上传文件到MinIO
                String reportUrl = minioUtil.uploadFile(inspectionReport, fileName);
                bymaterial.setInspectionReport(reportUrl);
            }

//            bymaterial.setModifier(loginAccount.getLoginName());
            // 设置修改时间
            bymaterial.setModification_time(LocalDateTime.now());
            // 修改操作暂时保留旧方法，后续可扩展 AbstractK3FormProcessor 支持修改
            //Result submitResult = k3configks.updateMaterialFormData(image, inspectionReport, bymaterial);
            // 1. 判断返回是否为错误
            /*if (!submitResult.isSuccess()) {  // Result.error 返回 false
                // 直接返回错误信息，不继续下面逻辑
                return submitResult;
            }*/

            return kingdeeMaterialServicer.updateMaterial(bymaterial);
        } catch (Exception e) {
            log.error("更新物料失败", e);
            return Result.error("物料更新失败: " + e.getMessage());
        }
    }

    /**
     * 根据id查询物料
     */
    @SaCheckPermission("k3:material:query")
    @GetMapping("/getByNumber")
    public R<Bymaterial> getMaterialByNumber(@RequestParam("id") String id) {
        return R.ok(kingdeeMaterialServicer.getMaterialById(Long.valueOf(id)));
    }

    /**
     * 查询物料修改审计日志（分页）
     * @param materialId 物料ID
     * @param pageNum 页码，默认1
     * @param pageSize 每页大小，默认10
     * @return 审计日志分页数据
     */
    @SaCheckPermission("k3:material:auditLogs")
    @GetMapping("/auditLogs")
    public R<Page<SysDataAuditLog>> getMaterialAuditLogs(
            @RequestParam("materialId") String materialId,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        try {
            // 查询 by_material 表的审计日志
            Page<SysDataAuditLog> auditLogs = kingdeeMaterialServicer.getAuditLogsByTableAndId(
                "by_material",
                materialId,
                pageNum,
                pageSize
            );

            return R.ok(auditLogs);
        } catch (Exception e) {
            log.error("查询物料审计日志失败，物料ID：{}", materialId, e);
            return R.fail("查询审计日志失败：" + e.getMessage());
        }
    }



    /**
     * 回滚已上传的文件
     * @param uploadedFiles 已上传的文件信息列表
     */
    private void rollbackUploadedFiles(List<Map<String, Object>> uploadedFiles) {
        for (Map<String, Object> fileInfo : uploadedFiles) {
            try {
                String imageUrl = (String) fileInfo.get("imageUrl");
                if (imageUrl != null) {
                    materialFileService.deleteMaterialFile(imageUrl);
                }
            } catch (Exception e) {
                log.error("回滚文件失败: " + fileInfo.get("materialNumber"), e);
            }
        }
    }

    /**
     * 分页查询物料列表 /k3/material
     */
    @SaCheckPermission("k3:material:list")
    @GetMapping("/page")
    public Page<Bymaterial> listMaterials(Bymaterial condition, PageQuery pageQuery) {

            int pageSize  =  pageQuery.getPageSize();
            int pageNumber  =  pageQuery.getPageNum();
            String isAsc = pageQuery.getIsAsc();
            return kingdeeMaterialServicer.listMaterials(condition, pageNumber, pageSize,isAsc);

    }


    /**
     * 批量删除物料
     */
    @SaCheckPermission("k3:material:remove")
    @PostMapping("/removeBatch")
    public Result batchDeleteMaterials(@RequestBody List<String> ids) {

        if (ids == null || ids.isEmpty()) {
            return Result.error("未指定要删除的物料编码");
        }

        List<Map<String, Object>> results = new ArrayList<>();

        for (String id : ids) {
            try {
                // 先根据编码查询物料，获取图片URL
                Bymaterial material = kingdeeMaterialServicer.getMaterialByNumberDirect(id);

                // 如果物料有图片，从MinIO删除图片文件
                if (material != null && material.getImage() != null && !material.getImage().isEmpty()) {
                    try {
                        minioUtil.deleteFileByUrl(material.getImage());
                    } catch (Exception e) {
                        log.warn("删除物料图片失败，物料编码: " + id, e);
                        // 不中断删除流程，仅记录警告
                    }
                }

                // 执行删除操作
                Result result = kingdeeMaterialServicer.deleteMaterial(Long.valueOf(id));
                if (result.isSuccess()) {
                    results.add(Map.of(
                            "id", id,
                            "message", "删除成功",
                            "success", true
                    ));
                } else {
                    results.add(Map.of(
                            "id", id,
                            "message", "删除失败: " + result.failMessage(),
                            "success", false
                    ));
                }
            } catch (Exception e) {
                // 捕获单条物料删除异常，保证循环继续
                log.error("删除物料失败，物料编码: " + id, e);
                results.add(Map.of(
                        "number", id,
                        "message", "删除异常: " + e.getMessage(),
                        "success", false
                ));
            }
        }

        return Result.success(results);
    }


    /**
     * AI填充物料英文描述
     */
    @SaCheckPermission("k3:material:fillEnglishDesc")
    @PostMapping("/fillEnglishDesc")
    public Result fillEnglishDesc(@RequestBody(required = false) Bymaterial condition) {
        try {
            // 检查请求体是否为空
            if (condition == null) {
                return Result.error("请求参数不能为空");
            }
            // 检查规格型号是否为空
            if (condition.getSpecification() == null || condition.getSpecification().isEmpty()) {
                return Result.error("规格型号不能为空");
            }

            // 执行填充操作
            return Result.success(kingdeeMaterialServicer.fillEnglishDesc(condition));


        } catch (Exception e) {
            log.error("填充物料英文描述失败，物料编码: " + condition.getNumber(), e);
            return Result.error("填充物料英文描述失败: " + e.getMessage());
        }
    }

    /**
     * Excel导入物料功能  @RequestPart("file") MultipartFile file, boolean updateSupport   @RequestParam("excelFile") MultipartFile excelFile
     */
    @Log(title = "物料导入管理", businessType = BusinessType.IMPORT)
    @SaCheckPermission("k3:material:importExcel")
    @PostMapping(value = "/importExcel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional(rollbackFor = Exception.class)
    public Result importMaterialsFromExcel(@RequestPart("file") MultipartFile file, boolean updateSupport) {
        try {
            // 1. 参数验证
            if (file == null || file.isEmpty()) {
                return Result.error("请选择要上传的Excel文件");
            }

            // 2. 解析Excel文件（支持内嵌图片）
            ExcelParseResult parseResult = exceptionUtil.parseExcelFileWithImages(file);

            if (parseResult.getDataList().isEmpty()) {
                return Result.error("Excel文件中没有数据");
            }

            // 3. 数据校验
            List<Map<String, Object>> dataList = parseResult.getDataList();

            // 获取图片映射关系
            Map<String, byte[]> imageMap = parseResult.getImageMap();
            Map<Integer, List<String>> rowImageMap = parseResult.getRowImageMap();

            log.info("解析结果: 数据行数={}, 图片数={}, 行图片映射数={}",
                dataList.size(), imageMap.size(), rowImageMap.size());

            // 打印详细的图片匹配信息
            for (Map.Entry<Integer, List<String>> entry : rowImageMap.entrySet()) {
                int rowIndex = entry.getKey();
                List<String> imageNames = entry.getValue();
                log.info("行 {} 匹配到图片: {}", rowIndex + 1, imageNames);
            }

            // 将所有图片转换为MultipartFile
            List<MultipartFile> imageFiles = new ArrayList<>();
            for (Map.Entry<String, byte[]> entry : imageMap.entrySet()) {
                String fileName = entry.getKey();
                byte[] content = entry.getValue();

                if (content == null || content.length == 0) {
                    log.warn("图片 {} 内容为空，跳过处理", fileName);
                    continue;
                }

                MultipartFile multipartFile = new ByteArrayMultipartFile(
                    fileName,
                    fileName,
                    exceptionUtil.getFileExtension(fileName),
                    content
                );
                imageFiles.add(multipartFile);
            }

            log.info("处理后的图片数量: {}", imageFiles.size());

            // 处理每一行数据
            List<Bymaterials> materialsList = new ArrayList<>();
            List<Map<String, Object>> processedDataList = new ArrayList<>();
            for (int i = 0; i < dataList.size(); i++) {
                Map<String, Object> rowData = dataList.get(i);

                Bymaterials material = new Bymaterials();
                // 物料基本字段设置
                String number = exceptionUtil.getMaterialNumberFromRow(rowData);
                material.setNumber(number);


                // ============ 改进的图片匹配逻辑 ============
                String imageUrl = null;

                // 方法1: 使用解析器返回的行-图片映射
                if (rowImageMap.containsKey(i)) {
                    List<String> imageNames = rowImageMap.get(i);
                    if (imageNames != null && !imageNames.isEmpty()) {
                        // 取第一个匹配的图片
                        String matchedImageName = imageNames.get(0);

                        // 查找对应的MultipartFile
                        for (MultipartFile imgFile : imageFiles) {
                            if (imgFile.getOriginalFilename().equals(matchedImageName)) {
                                try {
                                    imageUrl = materialFileService.uploadMaterialFile(
                                        imgFile,
                                        number,
                                        "images"
                                    );
                                    log.info("通过行图片映射找到图片: 行{} -> {} -> {}",
                                        i + 1, number, imageUrl);
                                    break;
                                } catch (Exception e) {
                                    log.error("上传映射图片失败: {}", matchedImageName, e);
                                }
                            }
                        }
                    }
                }

                // 方法2: 如果映射没找到，使用原有的查找逻辑
                if (imageUrl == null) {
                    // 检查图片地址列
                    Object imageObj = rowData.get("图片地址");
                    if (imageObj instanceof String && StringUtils.isNotBlank((String) imageObj)) {
                        String imagePath = (String) imageObj;
                        String fileNameFromPath = exceptionUtil.extractFileName(imagePath);
                        if (StringUtils.isNotBlank(fileNameFromPath)) {
                            imageUrl = exceptionUtil.findAndUploadImageByFileName(
                                fileNameFromPath, number, imageFiles, i);
                        }
                    }

                    // 方法3: 使用物料编码匹配
                    if (imageUrl == null) {
                        imageUrl = exceptionUtil.findAndUploadImageForMaterial(number, i, imageFiles);
                    }
                }

                // 设置图片URL
                material.setImage(imageUrl);

                if (imageUrl != null) {
                    rowData.put("图片地址", imageUrl);
                    log.info("行{}: 物料 {} 图片匹配成功 -> {}",
                        i + 1, number, imageUrl);
                } else {
                    log.warn("行{}: 物料 {} 未找到匹配的图片", i + 1, number);
                    rowData.put("图片地址", "");
                }
                // ====================================

                materialsList.add(material);
                processedDataList.add(rowData);

                // ====================================

                // 数据校验前打印 rowData 内容
                log.info("行{}: 准备校验数据，rowData内容: {}", i + 1, rowData);
                log.info("行{}: rowData的键集合: {}", i + 1, rowData.keySet());

                // 数据校验
                ImportResult validationResult = materialFileService.validateMaterialData(rowData, i, material.getNumber());
                if (!validationResult.isSuccess()) {
                    log.error("数据校验失败，行号: {}，错误信息: {}", i, validationResult.getMessage());
                    return Result.error("物料编码[" + validationResult.getNumber() + "]" + validationResult.getMessage());
                }
            }

            // 4. 批量导入物料数据
            List<ImportResult> results = materialFileService.batchImportMaterials(dataList);

            // 5. 统计导入结果
            ImportSummary summary = buildImportSummary(results);
            if (summary.getSuccessCount() == 0){
                return Result.error("导入物料失败: ");
            }
            String message = String.format(
                "导入完成",
                summary.getSuccessCount(),
                summary.getFailureCount(),
                materialsList.stream().filter(m -> m.getImage() != null).count()
            );

            return Result.success(message);
        } catch (Exception e) {
            log.error("导入物料Excel失败", e);
            return Result.error("导入物料失败: " + e.getMessage());
        }
    }

    /**
     * 获取导入模板
     */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtil.exportTemplateWithRequiredStyle("物料数据", BymaterialVo.class, response);
    }

    /**
     * 构建导入结果统计
     */
    private ImportSummary buildImportSummary(List<ImportResult> results) {
        ImportSummary summary = new ImportSummary();
        summary.setTotalCount(results.size());
        summary.setSuccessCount((int) results.stream().filter(ImportResult::isSuccess).count());
        summary.setFailureCount((int) results.stream().filter(r -> !r.isSuccess()).count());        summary.setResults(results);
        return summary;
    }

    /**
     * 导出物料列表
     */
    @Log(title = "物料管理", businessType = BusinessType.EXPORT)
    @SaCheckPermission("k3:material:export")
    @PostMapping("/export")
    public void export(BymaterialBo bymaterialBo, HttpServletResponse response) {
        List<BymaterialVo> list = bymaterialService.selectBymaterialList(bymaterialBo);
        ExcelUtil.exportExcel(list, "物料数据", BymaterialVo.class, response);
    }


    /**
     *
     * 批量更新交付红线
     * */
    @Log(title = "交付红线", businessType = BusinessType.UPDATE)
    @PutMapping("/updateMaterials")
    public Result updateMaterials(@RequestParam("ids") Long[] ids,@RequestParam("state") String state) {
        return Result.success(kingdeeMaterialServicer.updateMaterials(ids,state));
    }


}
