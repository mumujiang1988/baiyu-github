package com.ruoyi.business.k3.controller;

import com.ruoyi.business.k3.config.k3config;
import com.ruoyi.business.k3.service.ReceiveBillService;
import com.ruoyi.business.k3.service.ReceivebillEntryService;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.web.core.BaseController;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/k3/receivebill")
public class ReceiveBillController extends BaseController {

    @Resource
    private k3config k3configks;

    @Resource
    private ReceiveBillService receivebillservice;

    @Resource
    private ReceivebillEntryService receivebillEntryService;

    /**
     *  同步金蝶收款单表信息
    * */
    @PostMapping("/login")
    @Transactional(rollbackFor = Exception.class)  // 添加类级别的事务支持
    public Result login() {
        //获取收款单主表数据
        List<List<Object>> receivebillList =  k3configks.ReceiveBillList();
        receivebillservice.syncReceiveBillList(receivebillList);
//        //获取收款单明细表
        List<List<Object>> receivebillentrylist =  k3configks.ReceivebillEntryList() ;
        receivebillEntryService.ReceivebillEntryList(receivebillentrylist);
        return Result.success();
    }

}
