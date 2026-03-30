package com.ruoyi.business.k3.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.ruoyi.business.Component.CustomerFormProcessor;
import com.ruoyi.business.entity.Customer;
import com.ruoyi.business.k3.domain.bo.CustomerBo;
import com.ruoyi.business.k3.service.CustomerService;
import com.ruoyi.business.util.MinioUtil;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import org.springframework.web.multipart.MultipartFile;

/**客户列表
 * */
@RestController
@RequestMapping("/k3/customer")
@Validated
@Slf4j
public class CustomerController {

    @Autowired
    private  CustomerService customerService;

    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    private CustomerFormProcessor customerFormProcessor;

    /**
     * 同步金蝶客户列表到本地数据库
     * @return 操作结果
     */
    @PostMapping("/sync")
    @Transactional(rollbackFor = Exception.class)
    public Result syncPurchaseOrders() {
        // 同步客户主数据
        int totalCustomer = customerService.syncCustomerMultiThread();
        // 同步客户银行信息
        int totalBankInfo = customerService.syncCustomerBankInfo();
        // 同步客户转让人信息
        int totalTransferInfo = customerService.syncCustomerTransferInfo();
        // 同步客户联系人
        int totalContact = customerService.syncCustomerContactList();

       // return Result.success();
        return Result.success(String.format(
            "客户同步完成：主表%d条，银行信息%d条，转让信息%d条，联系人%d条",
            totalCustomer, totalBankInfo, totalTransferInfo, totalContact
        ));
    }

    /**
     * 根据ID查询客户
     */
    @SaCheckPermission("k3:customer:query")
    @GetMapping("/query")
    public Result getById(@RequestParam("id") Long id) {
        Customer customer = customerService.getById(id);
        return Result.success(customer);
    }

    /**
     * 新增客户
     */
    @SaCheckPermission("k3:customer:save")
    @PostMapping(value = "/save",produces = "application/json;charset=UTF-8")
    @Transactional(rollbackFor = Exception.class)
    public Result create(
        @RequestPart("customer") Customer customer,
        @RequestPart(value = "logoFile", required = false) MultipartFile logoFile,
        @RequestPart(value = "zmmttpFile", required = false) MultipartFile zmmttpFile,
        @RequestPart(value = "zmmttpsFile", required = false) MultipartFile zmmttpsFile,
        @RequestPart(value = "cmmttpFile", required = false) MultipartFile cmmttpFile,
        @RequestPart(value = "cmmttpsFile", required = false) MultipartFile cmmttpsFile) {

        log.info("接收到的客户数据: {}", customer);
        log.info("客户名称字段: '{}'", customer.getFname());
        log.info("客户名称是否为null: {}", customer.getFname() == null);

        MultipartFile[] files = {logoFile, zmmttpFile, cmmttpFile};

        //推送客户数据到金蝶
        try {
            // 使用表单处理器直接处理推送
            Result k3Result = customerFormProcessor.processForm(files, customer);
            if (!k3Result.isSuccess()) {
                log.warn("客户数据推送金蝶失败: {}", k3Result.failMessage());
            }
        } catch (Exception e) {
            log.warn("客户数据推送金蝶异常: {}", e.getMessage());
        }
        //1.如果有客户LOGO图片，先上传到MinIO（本地备份）
        if (logoFile != null && !logoFile.isEmpty()) {
            String businessLicenseUrl = minioUtil.uploadFile(logoFile);
            customer.setFKhlogo(businessLicenseUrl);
        }
        //2.如果有正唛外箱图片，先上传到MinIO（本地备份）
        if (zmmttpFile != null && !zmmttpFile.isEmpty()) {
            String businessLicenseUrl = minioUtil.uploadFile(zmmttpFile);
            customer.setFZmmttp1(businessLicenseUrl);
        }

        //3.如果有正唛内箱图片，先上传到MinIO（本地备份）
        if (zmmttpsFile != null && !zmmttpsFile.isEmpty()) {
            String businessLicenseUrl = minioUtil.uploadFile(zmmttpsFile);
            customer.setFZmmttp2(businessLicenseUrl);
        }

        //4.如果有正唛外箱图片，先上传到MinIO（本地备份）
        if (cmmttpFile != null && !cmmttpFile.isEmpty()) {
            String businessLicenseUrl = minioUtil.uploadFile(cmmttpFile);
            customer.setFcmmttp1(businessLicenseUrl);
        }

        //5.如果有正唛内箱图片，先上传到MinIO（本地备份）
        if (cmmttpsFile != null && !cmmttpsFile.isEmpty()) {
            String businessLicenseUrl = minioUtil.uploadFile(cmmttpsFile);
            customer.setFcmmttp2(businessLicenseUrl);
        }

        return customerService.create(customer);
    }

    /**
     * 删除客户列表
     * @param id 客户id
     * @return 操作结果
     */
    @SaCheckPermission("k3:customer:delete")
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable String id) {
        boolean result = customerService.removeById(id);
        if (result) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }


    /**
     * 更新客户
     */
    @SaCheckPermission("k3:customer:update")
    @PostMapping(value = "/update",produces = "application/json;charset=UTF-8")
    public boolean update(
        @RequestPart("customer") Customer customer,
        @RequestPart(value = "logoFile", required = false) MultipartFile logoFile,
        @RequestPart(value = "zmmttpFile", required = false) MultipartFile zmmttpFile,
        @RequestPart(value = "zmmttpsFile", required = false) MultipartFile zmmttpsFile,
        @RequestPart(value = "cmmttpFile", required = false) MultipartFile cmmttpFile,
        @RequestPart(value = "cmmttpsFile", required = false) MultipartFile cmmttpsFile) {
        //1.如果有客户LOGO图片，先上传到MinIO（本地备份）
        if (logoFile != null && !logoFile.isEmpty()) {
            String businessLicenseUrl = minioUtil.uploadFile(logoFile);
            customer.setFKhlogo(businessLicenseUrl);
        }
        //2.如果有正唛外箱图片，先上传到MinIO（本地备份）
        if (zmmttpFile != null && !zmmttpFile.isEmpty()) {
            String businessLicenseUrl = minioUtil.uploadFile(zmmttpFile);
            customer.setFZmmttp1(businessLicenseUrl);
        }

        //3.如果有正唛外箱图片，先上传到MinIO（本地备份）
        if (zmmttpsFile != null && !zmmttpsFile.isEmpty()) {
            String businessLicenseUrl = minioUtil.uploadFile(zmmttpsFile);
            customer.setFZmmttp2(businessLicenseUrl);
        }

        //4.如果有正唛外箱图片，先上传到MinIO（本地备份）
        if (cmmttpFile != null && !cmmttpFile.isEmpty()) {
            String businessLicenseUrl = minioUtil.uploadFile(cmmttpFile);
            customer.setFcmmttp1(businessLicenseUrl);
        }

        //5.如果有正唛内箱图片，先上传到MinIO（本地备份）
        if (cmmttpsFile != null && !cmmttpsFile.isEmpty()) {
            String businessLicenseUrl = minioUtil.uploadFile(cmmttpsFile);
            customer.setFcmmttp2(businessLicenseUrl);
        }
        return customerService.update(customer);
    }

    /**
     * 分页条件查询客户列表
     */
    @SaCheckPermission("k3:customer:list")
    @GetMapping("/list")
    public TableDataInfo<Customer> list(CustomerBo customer, PageQuery pageQuery) {

        // 使用服务层的条件分页查询方法
        return customerService.getList(customer, pageQuery);
    }
}
