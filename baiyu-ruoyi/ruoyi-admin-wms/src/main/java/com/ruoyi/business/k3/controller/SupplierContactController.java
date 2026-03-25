package com.ruoyi.business.k3.controller;

import com.ruoyi.business.entity.SupplierContactBase;
import com.ruoyi.business.k3.service.SupplierContactService;
import com.ruoyi.common.core.domain.R;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 供应商联系人信息表 控制层。
 *
 * @author yourname
 * @since 2025-11-05
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/k3/supplierContact")
@Slf4j
public class SupplierContactController {

    @Resource
    private SupplierContactService supplierContactService;

    /**
     * 添加供应商联系人
     *
     * @param supplierContact 供应商联系人信息
     * @return 操作结果
     */
    @PostMapping("/save")
    public R save(@RequestBody SupplierContactBase supplierContact) {
        boolean result = supplierContactService.save(supplierContact);
        if (result) {
            return R.ok("添加成功");
        } else {
            return R.fail("添加失败");
        }
    }

    /**
     * 更新供应商联系人信息
     *
     * @param supplierContact 供应商联系人信息
     * @return 操作结果
     */
    @PostMapping("/update")
    public R update(@RequestBody SupplierContactBase supplierContact) {
        boolean result = supplierContactService.updateById(supplierContact);
        if (result) {
            return R.ok("更新成功");
        } else {
            return R.fail("更新失败");
        }
    }

    /**
     * 删除供应商联系人
     *
     * @param supplierCode 供应商编码
     * @param contactName  联系人姓名
     * @return 操作结果
     */
    @PostMapping("/remove")
    public R remove(@RequestParam String supplierCode, @RequestParam String contactName) {
        boolean result = supplierContactService.remove(supplierCode, contactName);
        if (result) {
            return R.ok("删除成功");
        } else {
            return R.fail("删除失败");
        }
    }

    /**
     * 根据供应商编码和联系人姓名查询联系人信息
     *
     * @param supplierCode 供应商编码
     * @param contactName  联系人姓名
     * @return 供应商联系人信息
     */
    @GetMapping("/detail")
    public R detail(@RequestParam String supplierCode, @RequestParam String contactName) {
        SupplierContactBase supplierContact = supplierContactService.getBySupplierCodeAndContactName(supplierCode, contactName);
        if (supplierContact != null) {
            return R.ok(supplierContact);
        } else {
            return R.fail("未找到该供应商联系人");
        }
    }

    /**
     * 根据供应商编码查询所有联系人
     *
     * @param supplierCode 供应商编码
     * @return 供应商联系人列表
     */
    @GetMapping("/listBySupplier")
    public R listBySupplier(@RequestParam String supplierCode) {
        List<SupplierContactBase> supplierContacts = supplierContactService.listBySupplierCode(supplierCode);
        return R.ok(supplierContacts);
    }

    /**
     * 查询所有供应商联系人
     *
     * @return 供应商联系人列表
     */
    @GetMapping("/list")
    public R list() {
        List<SupplierContactBase> supplierContacts = supplierContactService.list();
        return R.ok(supplierContacts);
    }
}
