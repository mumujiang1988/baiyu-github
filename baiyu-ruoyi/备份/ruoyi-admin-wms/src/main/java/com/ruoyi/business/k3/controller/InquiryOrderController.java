package com.ruoyi.business.k3.controller;
import com.ruoyi.business.Component.K3FormProcessorFactory;
import com.ruoyi.business.entity.InquiryOrder;
import com.ruoyi.business.k3.config.AbstractK3FormProcessor;
import com.ruoyi.business.k3.service.InquiryOrderService;
import com.ruoyi.business.util.Result;

import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 询价单管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/k3/inquiry-order")
public class InquiryOrderController {

    @Autowired
    private InquiryOrderService inquiryOrderService;
    @Autowired
    private K3FormProcessorFactory k3FormProcessorFactory;
    /**
     * 同步金蝶询价单数据到本地数据库
     * @return 同步结果，包含处理的总记录数
     */
    @PostMapping("/sync")
    @Transactional(rollbackFor = Exception.class)
    public Result syncInquiryOrders() {
        try {
            log.info("开始同步询价单数据...");

            // 调用服务层同步数据
            int totalCount = inquiryOrderService.syncInquiryOrdersFromK3();

            log.info("询价单数据同步完成，总计处理：{} 条", totalCount);

            // 返回成功结果
            return Result.success("询价单同步完成，总计处理：" + totalCount + " 条");

        } catch (Exception e) {
            log.error("同步询价单数据失败", e);
            return Result.error("同步询价单数据失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询询价单
     */
    @GetMapping("/list")
    public TableDataInfo<InquiryOrder> listInquiryOrders(InquiryOrder inquiryOrder, PageQuery pageQuery) {
        return inquiryOrderService.selectPageInquiryOrder(inquiryOrder, pageQuery);
    }

    /**
     * 根据ID查询询价单
     * @param id 询价单ID
     */
    @GetMapping("/{id}")
    public Result getInfo(@PathVariable String id) {
        InquiryOrder inquiryOrder = inquiryOrderService.selectById(id);
        if (inquiryOrder == null) {
            return Result.error("未找到对应的询价单");
        }
        return Result.success(inquiryOrder);
    }

    /**
     * 根据单据编号查询询价单
     * @param billNo 单据编号
     */
    @GetMapping("/bill/{billNo}")
    public Result getByBillNo(@PathVariable String billNo) {
        if (billNo == null || billNo.isEmpty()) {
            return Result.error("单据编号不能为空");
        }
        InquiryOrder inquiryOrder = inquiryOrderService.selectByBillNo(billNo);
        if (inquiryOrder == null) {
            return Result.error("未找到单据编号为 " + billNo + " 的询价单");
        }
        return Result.success(inquiryOrder);
    }

    /**
     * 新增询价单
     * @param inquiryOrder 询价单信息
     * @return 操作结果
     */

    @PostMapping("/add")
    @Transactional(rollbackFor = Exception.class)
    public Result add(@RequestBody InquiryOrder inquiryOrder) {
        try {
            // 必填字段校验
            if (inquiryOrder.getFbillno() == null || inquiryOrder.getFbillno().trim().isEmpty()) {
                return Result.error("单据编号不能为空");
            }
            if (inquiryOrder.getFdate() == null) {
                return Result.error("日期不能为空");
            }
            if (inquiryOrder.getFKhbm() == null || inquiryOrder.getFKhbm().trim().isEmpty()) {
                return Result.error("客户编码不能为空");
            }
            if (inquiryOrder.getFCkgj() == null || inquiryOrder.getFCkgj().trim().isEmpty()) {
                return Result.error("出口国家不能为空");
            }
            if (inquiryOrder.getFKhly() == null || inquiryOrder.getFKhly().trim().isEmpty()) {
                return Result.error("客户来源不能为空");
            }
            if (inquiryOrder.getFkhxq() == null || inquiryOrder.getFkhxq().trim().isEmpty()) {
                return Result.error("客户需求不能为空");
            }

            // 使用新的工厂模式处理询价单表单
            AbstractK3FormProcessor<InquiryOrder> processor = k3FormProcessorFactory.getProcessor("SAL_Inquiry");
            Result submitResult = processor.processForm(new MultipartFile[0], inquiryOrder);

            if (submitResult.isSuccess()) {
                // 如果成功提交到金蝶，则同时保存到本地数据库
                return inquiryOrderService.addInquiryOrder(inquiryOrder);
            } else {
                return submitResult; // 返回金蝶提交失败的信息
            }
        } catch (Exception e) {
            log.error("新增询价单异常", e);
            return Result.error("新增询价单异常: " + e.getMessage());
        }
    }

    /**
     * 修改询价单
     * @param inquiryOrder 询价单信息
     * @return 操作结果
     */
    @PutMapping
    @Transactional(rollbackFor = Exception.class)
    public Result edit(@RequestBody InquiryOrder inquiryOrder) {
        try {
            if (inquiryOrder.getFid() == null) {
                return Result.error("询价单ID不能为空");
            }

            return inquiryOrderService.updateInquiryOrder(inquiryOrder);
        } catch (Exception e) {
            log.error("修改询价单异常", e);
            return Result.error("修改询价单异常: " + e.getMessage());
        }
    }

    /**
     * 删除询价单
     * @param fid 询价单ID
     * @return 操作结果
     */
    @DeleteMapping("/{fid}")
    @Transactional(rollbackFor = Exception.class)
    public Result remove(@PathVariable String fid) {
        try {
            return inquiryOrderService.deleteInquiryOrder(fid);
        } catch (Exception e) {
            log.error("删除询价单异常", e);
            return Result.error("删除询价单异常: " + e.getMessage());
        }
    }
}
