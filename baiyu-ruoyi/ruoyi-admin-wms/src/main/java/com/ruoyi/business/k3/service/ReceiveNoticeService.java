package com.ruoyi.business.k3.service;

import com.ruoyi.business.entity.ReceiveNotice;
import com.ruoyi.business.entity.ReceiveNoticeEntry;
import com.ruoyi.business.util.Result;

import java.util.List;

/**
 * 收料通知单服务接口
 */
public interface ReceiveNoticeService {

    /**
     * 根据主键ID查询收料通知单
     */
    ReceiveNotice getById(Long fid);

    /**
     * 根据单据编号查询收料通知单
     */
    ReceiveNotice getByBillNo(String billNo);

    /**
     * 新增收料通知单（包括明细）
     */
    Result create(ReceiveNotice receiveNotice, List<ReceiveNoticeEntry> entries);

    /**
     * 更新收料通知单（包括明细）
     */
    Result update(ReceiveNotice receiveNotice, List<ReceiveNoticeEntry> entries);

    /**
     * 删除收料通知单（包括明细）
     */
    Result deleteById(Long fid);

    /**
     * 分页条件查询收料通知单
     */
    List<ReceiveNotice> getList(ReceiveNotice receiveNotice, int pageNum, int pageSize);

    int syncReceiveNoticeEntriesMultiThread();

    /**
     * 同步收料通知单明细数据
     */
    int syncReceiveNoticeEntryData();
}
