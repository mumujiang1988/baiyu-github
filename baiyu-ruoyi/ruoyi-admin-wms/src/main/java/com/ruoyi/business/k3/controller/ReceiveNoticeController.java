package com.ruoyi.business.k3.controller;


import com.ruoyi.business.entity.ReceiveNotice;
import com.ruoyi.business.k3.service.ReceiveNoticeService;
import com.ruoyi.business.util.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 收料通知单控制器
 */
@RestController
@RequestMapping("/receive-notice")
public class ReceiveNoticeController {

    @Resource
    private ReceiveNoticeService receiveNoticeService;

    /**
     * 同步收料通知单数据
     */
    @PostMapping("/sync")
    public Result syncReceiveNotices() {
        try {
            // 调用服务层的多线程同步方法
            int totalProcessed = receiveNoticeService.syncReceiveNoticeEntriesMultiThread();
            // 调用服务层的明细数据同步方法
           // int totalProcessedEntries = receiveNoticeService.syncReceiveNoticeEntryData();
            return Result.success("收料通知单同步完成，共处理 " + totalProcessed + " 条记录");
        } catch (Exception e) {
            return Result.error("同步过程中发生错误: " + e.getMessage());
        }
    }
    /**
     * 根据ID查询收料通知单
     */
    @GetMapping("/{fid}")
    public ReceiveNotice getById(@PathVariable("fid") Long fid) {
        return receiveNoticeService.getById(fid);
    }

    /**
     * 根据单据编号查询收料通知单
     */
    @GetMapping("/bill/{billNo}")
    public Result getByBillNo(@PathVariable("billNo") String billNo) {
        if (billNo == null || billNo.isEmpty()){
            return Result.error("单据编号不能为空");
        }
        return Result.success(receiveNoticeService.getByBillNo(billNo));
    }



    /**
     * 删除收料通知单
     */
    @DeleteMapping("/{fid}")
    public Result deleteById(@PathVariable("fid") Long fid) {
        return receiveNoticeService.deleteById(fid);
    }

    /**分页查询*/
    @GetMapping("/list")
    public Result getList(ReceiveNotice receiveNotice, int pageNum, int pageSize) {
        return Result.success(receiveNoticeService.getList(receiveNotice, pageNum, pageSize));
    }
}
