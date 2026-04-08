package com.ruoyi.business.k3.controller;

import com.ruoyi.business.k3.config.k3config;
import com.ruoyi.business.k3.service.SalOutboundDetailService;
import com.ruoyi.business.k3.service.SalOutboundService;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.web.core.BaseController;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/k3/saloutbound")
public class SalOutboundController extends BaseController {

    @Resource
    private k3config k3configks;

    @Resource
    private SalOutboundService outboundService;

    @Resource
    private SalOutboundDetailService detailService;

    @Resource
    private SalOutboundDetailService outboundDetailService;

    @PostMapping("/login")
    @Transactional(rollbackFor = Exception.class)  // 添加类级别的事务支持
    public Result login() {
        //同步销售出库单主表数据
/*        List<List<Object>> salOutboundList =  k3configks.SalOutboundList() ;
        outboundService.syncSalOutboundList(salOutboundList);*/
//        //同步销售出库单明细表
        List<List<Object>> detailsList =  k3configks.SalOutboundDetailsList() ;
        outboundDetailService.syncSalOutboundDetails(detailsList);
        return Result.success();
    }


}
