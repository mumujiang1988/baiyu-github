package com.ruoyi.business.k3.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.ruoyi.business.Component.K3FormProcessorFactory;
import com.ruoyi.business.entity.*;
import com.ruoyi.business.k3.config.AbstractK3FormProcessor;
import com.ruoyi.business.k3.config.k3config;
import com.ruoyi.business.k3.service.*;
import com.ruoyi.business.servicel.DictionaryLookupServicel;
import com.ruoyi.business.util.MinioUtil;
import com.ruoyi.business.util.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.util.SequenceGeneratorUtil;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 供应商管理控制器
 */

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/k3/supplier")
@Slf4j
public class SupplierController {

    @Resource
    private SupplierService supplierService;
    @Resource
    private k3config k3configks;

    @Autowired
    private K3FormProcessorFactory k3FormProcessorFactory;
    @Autowired
    private SupplierContactService supplierContactService;
    @Autowired
    private FinancialInformationService financialInformationService;
    @Autowired
    private MinioUtil minioUtil;
    @Autowired
    private MaterialFileService materialFileService;
    @Autowired
    private KingdeeMaterialServicer kingdeeMaterialServicer;
    @Autowired
    private DictionaryLookupServicel dictionaryLookupServicel ;

    @Autowired
    private SupplierEncodingService supplierEncodingService;

    /**
     * 同步供应商列表
     */
    @SaCheckPermission("k3:supplier:login")
    @PostMapping("/login")
    @Transactional(rollbackFor = Exception.class)  // 添加类级别的事务支持
    public Result login() {

        List<List<Object>> querySupplierList = k3configks.querySupplierList();

        List<List<Object>> querylinkmanList = k3configks.querylinkmanList();
        supplierContactService.querylinkmanList(querylinkmanList);

        List<List<Object>> querypaymentList = k3configks.querypaymentList();
        financialInformationService.querylinkmanList(querypaymentList);
        List<List<Object>> querySuppliervisitingList = k3configks.querySuppliervisitingList();
        supplierService.syncSupplierVisitRecordFromK3(querySuppliervisitingList);

        supplierService.querySupplierList(querySupplierList);

        return Result.create(true);
    }

    /**
     * 添加供应商（支持联系人列表）
     *
     * @param supplier 供应商信息（包含contactInformation字段）
     * @param image 营业执照图片
     * @return 操作结果
     */
    @SaCheckPermission("k3:supplier:save")
    @PostMapping(value = "/save",produces = "application/json;charset=UTF-8")
    @Transactional(rollbackFor = Exception.class)
    public Result save(@RequestPart("data") Supplier supplier,
                       @RequestPart(value = "image", required = false) MultipartFile image,
                       @RequestPart(value ="visitFile", required = false) MultipartFile visitFile) {
        try {
            // 1.通code获取来源名称
            if (!supplier.getSource().trim().isEmpty()){
                BymaterialDictionary bymaterialDictionary  = dictionaryLookupServicel.categoryCode(supplier.getSource());
                supplier.setSource(bymaterialDictionary.getName());
            }

            //
            //查找供应商分组k3_id查询最新一天数据
            //Supplier supplieres = supplierService.selectBySupplierGroup(supplier.getSupplierGroup());
            SupplierEncoding supplierEncoding = supplierEncodingService.selectBySupplierGroup(supplier.getSupplierGroup());
            //获取编号
            SequenceGeneratorUtil.CodeGenerator generator2 = SequenceGeneratorUtil.CodeGenerator.fromMaxCode(supplierEncoding.getNumber(), 3);
            //获取数字进行相加
            String mumbers = generator2.next();
            //获取供应商编码拼接
            supplier.setNumber(mumbers);

            // 2. 如果有营业执照图片，先上传到MinIO（本地备份）
            if (image != null && !image.isEmpty()) {
                String businessLicenseUrl = minioUtil.uploadFile(image);
                supplier.setBusinessLicense(businessLicenseUrl);
            }

            //3.如果有回访附件，先上传到MinIO（本地备份）
            if (visitFile != null && !visitFile.isEmpty()){
                String attachment = minioUtil.uploadFile(visitFile);
                supplier.getSupplierVisitRecord().forEach(supplierVisitRecord -> {
                    supplierVisitRecord.setAttachment(attachment);
                });
            }


            // 3. 使用新的工厂模式提交到金蝶（包含主表+联系人子表）
            AbstractK3FormProcessor<Supplier> processor =
                k3FormProcessorFactory.getProcessor("BD_Supplier");

            MultipartFile[] files = {image};  // 文件数组（按顺序对应 FImageFileServer）
            Result k3Result = processor.processForm(files, supplier);

            // 4. 判断金蝶提交结果
            if (!k3Result.isSuccess()) {
                log.error("提交到金蝶失败: {}", k3Result.failMessage());
                return k3Result;
            }

            log.info("供应商 {} 已成功提交到金蝶，包含 {} 条联系人",
                supplier.getNumber(),
                supplier.getContactInformation() != null ? supplier.getContactInformation().size() : 0);

            // 5. 保存到本地数据库
            return supplierService.save(supplier);

        } catch (Exception e) {
            log.error("添加供应商失败", e);
            return Result.error("供应商添加失败: " + e.getMessage());
        }
    }

    /**
     * 更新供应商信息
     *
     * @param supplier 供应商信息
     * @return 操作结果
     */
    @SaCheckPermission("k3:supplier:update")
    @PostMapping("/update")
    public Result update(@RequestPart("data") Supplier supplier,
                         @RequestPart(value = "image", required = false) MultipartFile image,
                         @RequestPart(value ="visitFile", required = false) List<MultipartFile> visitFile) {
        if (image != null) {
            supplier.setBusinessLicense(minioUtil.uploadFile(image));
        }

        if (visitFile != null && !visitFile.isEmpty() && supplier.getSupplierVisitRecord() != null && !supplier.getSupplierVisitRecord().isEmpty()) {

            // 确保文件数量与回访记录数量匹配
            int minSize = Math.min(visitFile.size(), supplier.getSupplierVisitRecord().size());

            for (int i = 0; i < minSize; i++) {
                MultipartFile file = visitFile.get(i);
                SupplierVisitRecord record = supplier.getSupplierVisitRecord().get(i);

                if (file != null && !file.isEmpty()) {
                    try {
                        String attachmentUrl = minioUtil.uploadFile(file);
                        record.setAttachment(attachmentUrl);
                    } catch (Exception e) {
                        log.error("上传回访记录附件失败，记录ID: {}", record.getId(), e);
                    }
                }
            }
        }

        boolean result = supplierService.updateById(supplier);
        if (result) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }

    /**
     * 查询供应商修改审计日志（分页）
     * @param supplierId 供应商ID
     * @param pageNum 页码，默认1
     * @param pageSize 每页大小，默认10
     * @return 审计日志分页数据
     */
    @SaCheckPermission("k3:supplier:auditLogs")
    @GetMapping("/auditLogs")
    public R<Page<SysDataAuditLog>> getMaterialAuditLogs(
        @RequestParam("supplierId") String supplierId,
        @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        try {
            // 查询 by_material 表的审计日志
            Page<SysDataAuditLog> auditLogs = supplierService.getAuditLogsByTableAndId(
                "supplier",
                supplierId,
                pageNum,
                pageSize
            );

            return R.ok(auditLogs);
        } catch (Exception e) {
            log.error("查询供应商审计日志失败，供应商ID：{}", supplierId, e);
            return R.fail("查询审计日志失败：" + e.getMessage());
        }
    }


    /**
     * 删除供应商
     *
     * @param number 供应商编码
     * @return 操作结果
     */
    @SaCheckPermission("k3:supplier:delete")
    @DeleteMapping("/delete/{number}")
    public Result delete(@PathVariable String number) {
        boolean result = supplierService.removeById(number);
        if (result) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

    /**
     * 根据编码查询供应商
     *
     * @param number 供应商编码
     * @return 供应商信息
     */
    @SaCheckPermission("k3:supplier:query")
    @GetMapping("/query")
    public Result get(@RequestParam("number") String number) {
        Supplier supplier = supplierService.getById(number);
        if (supplier != null) {
            return Result.success(supplier);
        } else {
            return Result.error("未找到该供应商");
        }
    }

    /**
     * 分页查询供应商列表
     *
     * @param supplier 查询条件
     * @param pageQuery 分页参数
     * @return 分页数据
     */
    @SaCheckPermission("k3:supplier:page")
    @GetMapping("/page")
    public Page<Supplier> list(Supplier supplier, PageQuery pageQuery) {
        int pageSize  =  pageQuery.getPageSize();
        int pageNumber  =  pageQuery.getPageNum();
        return supplierService.listSuppliers(supplier, pageNumber, pageSize);
    }

    /**
     * 批量导入营业执照图片
     */
    @SaCheckPermission("k3:supplier:importBusinessLicense")
    @PostMapping(value = "/importBusinessLicense", produces = "application/json;charset=UTF-8")
    public ApiResponse batchUploadImages(@RequestParam("images") MultipartFile[] images) {
        List<Map<String, Object>> uploadedFiles = new ArrayList<>();

        try {
            // 用于存储处理结果
            List<Map<String, Object>> results = new ArrayList<>();

            // 第一步：验证所有文件
            for (MultipartFile image : images) {
                if (image.isEmpty()) {
                    continue;
                }

                String originalFilename = image.getOriginalFilename();
                if (originalFilename == null || originalFilename.isEmpty()) {
                    return ApiResponse.error("文件名为空");
                }  // 提取不带扩展名的文件名作为物料编码
                String materialNumber = originalFilename;
                if (originalFilename.contains(".")) {
                    materialNumber = originalFilename.substring(0, originalFilename.lastIndexOf("."));
                }
                // 查询供应商是否存在
                Supplier existingMaterial = supplierService.getMaterialByNumberDirect(materialNumber);
                if (existingMaterial == null) {
                    return ApiResponse.error("供应商编码" + materialNumber + "不存在:" + "不可新增图片");
                }
            }
            // 第二步：如果所有验证通过，开始上传文件
            for (MultipartFile image : images) {
                if (image.isEmpty()) {
                    continue;
                }

                String originalFilename = image.getOriginalFilename();
                String materialNumber = originalFilename;
                if (originalFilename.contains(".")) {
                    materialNumber = originalFilename.substring(0, originalFilename.lastIndexOf("."));
                }
                materialNumber = materialNumber.replace("_", "/");

                try {
                    // 上传图片到MinIO
                    String imageUrl = materialFileService.uploadMaterialFile(image, materialNumber, "inspection_reports");

                    // 记录上传成功的文件信息，用于可能的回滚
                    Map<String, Object> uploadedFileInfo = new HashMap<>();
                    uploadedFileInfo.put("materialNumber", materialNumber);
                    uploadedFileInfo.put("imageUrl", imageUrl);
                    uploadedFiles.add(uploadedFileInfo);

                    // 查询供应商信息
                    Supplier existingMaterial = supplierService.getMaterialByNumberDirect(materialNumber);

                    // 删除旧图片
                    if (existingMaterial.getBusinessLicense() != null) {
                        materialFileService.deleteMaterialFile(existingMaterial.getBusinessLicense());
                    }
                    // 更新供应商信息
                    Supplier supplier = new Supplier();
                    supplier.setNumber(existingMaterial.getNumber());
                    supplier.setBusinessLicense(imageUrl);
                    supplierService.updateById(supplier);

                    // 添加成功结果
                    Map<String, Object> successResult = new HashMap<>();
                    successResult.put("materialNumber", materialNumber);
                    successResult.put("filename", originalFilename);
                    successResult.put("status", "success");
                    successResult.put("message", "图片上传成功");
                    results.add(successResult);

                } catch (Exception e) {
                    log.error("处理图片失败: " + originalFilename, e);
                    // 出现错误，执行回滚操作
                    rollbackUploadedFiles(uploadedFiles);
                    return ApiResponse.error("处理图片失败: " + originalFilename + ", 错误信息: " + e.getMessage());
                }
            }

            // 所有文件都处理成功
            return ApiResponse.success("全部导入完成", results);

        } catch (Exception e) {
            log.error("批量上传图片失败", e);
            // 出现未预期的错误，执行回滚操作
            rollbackUploadedFiles(uploadedFiles);
            return ApiResponse.error("批量上传图片失败: " + e.getMessage());
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
}
