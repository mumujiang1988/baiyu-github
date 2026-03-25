package com.ruoyi.business.k3.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.entity.ReceiptNoticeFull;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;

import java.util.List;

public interface InspectionBillService {
    void syncInspectionBill();

    /**
     * 新增检验单数据
     * @param receiptNoticeFull 检验单数据
     * @return 是否成功
     */
    boolean addInspectionBill(ReceiptNoticeFull receiptNoticeFull);

    /**
     * 分页查询检验单数据
     * @param current 当前页码
     * @param size 页大小
     * @param receiptNoticeFull 查询条件
     * @return 分页结果
     */
    List<ReceiptNoticeFull> pageInspectionBills(long current, long size, ReceiptNoticeFull receiptNoticeFull);

    /**
     * 根据 id 查询检验单详情
     * @param id 主键 ID
     * @return 检验单详情
     */
    ReceiptNoticeFull getInspectionBillById(Long id);
}
