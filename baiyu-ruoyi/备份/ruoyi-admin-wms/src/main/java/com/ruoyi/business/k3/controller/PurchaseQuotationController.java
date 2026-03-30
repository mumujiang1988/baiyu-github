package com.ruoyi.business.k3.controller;

import com.ruoyi.business.Component.K3FormProcessorFactory;
import com.ruoyi.business.entity.PurchaseQuotation;
import com.ruoyi.business.k3.config.AbstractK3FormProcessor;
import com.ruoyi.business.k3.service.PurchaseQuotationService;
import com.ruoyi.business.util.Result;

import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 采购报价单管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/k3/purchase-quotation")
public class PurchaseQuotationController {

    @Autowired
    private PurchaseQuotationService purchaseQuotationService;

    @Autowired
    private K3FormProcessorFactory k3FormProcessorFactory;

    /**
     * 同步金蝶采购报价单数据到本地数据库
     * @return 同步结果，包含处理的总记录数
     */
    @PostMapping("/sync")
    @Transactional(rollbackFor = Exception.class)
    public Result syncPurchaseQuotations() {
        try {
            log.info("开始同步采购报价单数据...");

            // 调用服务层同步数据
            int totalCount = purchaseQuotationService.syncPurchaseQuotationsFromK3();

            log.info("采购报价单数据同步完成，总计处理：{} 条", totalCount);

            // 返回成功结果
            return Result.success("采购报价单同步完成，总计处理：" + totalCount + " 条");

        } catch (Exception e) {
            log.error("同步采购报价单数据失败", e);
            return Result.error("同步采购报价单数据失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询采购报价单
     */
    @GetMapping("/list")
    public TableDataInfo<PurchaseQuotation> listPurchaseQuotations(PurchaseQuotation purchaseQuotation, PageQuery pageQuery) {
        return purchaseQuotationService.selectPagePurchaseQuotation(purchaseQuotation, pageQuery);
    }

    /**
     * 根据ID查询采购报价单
     * @param id 采购报价单ID
     */
    @GetMapping("/{id}")
    public Result getInfo(@PathVariable String id) {
        PurchaseQuotation purchaseQuotation = purchaseQuotationService.selectById(id);
        if (purchaseQuotation == null) {
            return Result.error("未找到对应的采购报价单");
        }
        return Result.success(purchaseQuotation);
    }

    /**
     * 根据单据编号查询采购报价单
     * @param billNo 单据编号
     */
    @GetMapping("/bill/{billNo}")
    public Result getByBillNo(@PathVariable String billNo) {
        if (billNo == null || billNo.isEmpty()) {
            return Result.error("单据编号不能为空");
        }
        PurchaseQuotation purchaseQuotation = purchaseQuotationService.selectByBillNo(billNo);
        if (purchaseQuotation == null) {
            return Result.error("未找到单据编号为 " + billNo + " 的采购报价单");
        }
        return Result.success(purchaseQuotation);
    }

    /**
     * 新增采购报价单
     * @param purchaseQuotation 采购报价单信息
     * @return 操作结果
     */
    @PostMapping("/add")
    @Transactional(rollbackFor = Exception.class)
    public Result add(@RequestBody PurchaseQuotation purchaseQuotation) {
        try {
            // 必填字段校验
            if (purchaseQuotation.getFbillno() == null || purchaseQuotation.getFbillno().trim().isEmpty()) {
                return Result.error("单据编号不能为空");
            }
            if (purchaseQuotation.getFdate() == null) {
                return Result.error("日期不能为空");
            }
            if (purchaseQuotation.getFKh() == null || purchaseQuotation.getFKh().trim().isEmpty()) {
                return Result.error("客户编码不能为空");
            }
            if (purchaseQuotation.getFCkgj() == null || purchaseQuotation.getFCkgj().trim().isEmpty()) {
                return Result.error("出口国家不能为空");
            }
            if (purchaseQuotation.getFKhlx() == null || purchaseQuotation.getFKhlx().trim().isEmpty()) {
                return Result.error("客户类型不能为空");
            }

            // 使用新的工厂模式处理采购报价单表单
            AbstractK3FormProcessor<PurchaseQuotation> processor = k3FormProcessorFactory.getProcessor("k350c7af6cd7149238edfaa94b2946fd5");
            Result submitResult = processor.processForm(new MultipartFile[0], purchaseQuotation);

            if (submitResult.isSuccess()) {
                // 如果成功提交到金蝶，则同时保存到本地数据库
                return purchaseQuotationService.addPurchaseQuotation(purchaseQuotation);
            } else {
                return submitResult; // 返回金蝶提交失败的信息
            }
        } catch (Exception e) {
            log.error("新增采购报价单异常", e);
            return Result.error("新增采购报价单异常: " + e.getMessage());
        }
    }

    /**
     * 修改采购报价单
     * @param purchaseQuotation 采购报价单信息
     * @return 操作结果
     */
    @PutMapping
    @Transactional(rollbackFor = Exception.class)
    public Result edit(@RequestBody PurchaseQuotation purchaseQuotation) {
        try {
            if (purchaseQuotation.getFid() == null) {
                return Result.error("采购报价单ID不能为空");
            }

            return purchaseQuotationService.updatePurchaseQuotation(purchaseQuotation);
        } catch (Exception e) {
            log.error("修改采购报价单异常", e);
            return Result.error("修改采购报价单异常: " + e.getMessage());
        }
    }

    /**
     * 删除采购报价单
     * @param id 采购报价单ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result remove(@PathVariable String id) {
        try {
            return purchaseQuotationService.deletePurchaseQuotation(id);
        } catch (Exception e) {
            log.error("删除采购报价单异常", e);
            return Result.error("删除采购报价单异常: " + e.getMessage());
        }
    }
}
