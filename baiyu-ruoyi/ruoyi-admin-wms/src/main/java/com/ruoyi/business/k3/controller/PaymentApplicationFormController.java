package com.ruoyi.business.k3.controller;

import com.ruoyi.business.k3.config.k3config;
import com.ruoyi.business.k3.service.PaymentApplicationService;
import com.ruoyi.business.k3.service.RectunitDetailService;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.web.core.BaseController;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/k3/PaymentApplication")
public class PaymentApplicationFormController extends BaseController {
    @Resource
    private k3config k3configks;

    @Resource
   private PaymentApplicationService applicationService;

   @Resource
   private RectunitDetailService detailService;


    /**
     * 同步金蝶付款申请单表信息
     */
    @PostMapping("/login")
    @Transactional(rollbackFor = Exception.class)  // 添加类级别的事务支持
    public Result login() {
        //获取付款申请单主表数据
        List<List<Object>> PaymentApplicationList =  k3configks.PaymentApplicationList() ;
        applicationService.syncPaymentApplicationList(PaymentApplicationList);
//        //获取付款申请单明细表
        List<List<Object>> RectunitDetailList =  k3configks.RectunitDetailList() ;
        detailService.syncRectunitDetail(RectunitDetailList);
        return Result.success();
    }

}
