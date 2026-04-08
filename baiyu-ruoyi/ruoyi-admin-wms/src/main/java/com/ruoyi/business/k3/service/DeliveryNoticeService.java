package com.ruoyi.business.k3.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.business.entity.DeliveryNotice;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;

import java.util.List;

/**
 * 发货通知单服务接口
 */
public interface DeliveryNoticeService {

    /**
     * 保存发货通知单（包含明细）
     * @param deliveryNotice 发货通知单信息
     * @return 操作结果
     */
    Result save(DeliveryNotice deliveryNotice);

    /**
     * 更新发货通知单（包含明细）
     * @param deliveryNotice 发货通知单信息
     * @return 操作结果
     */
    Result update(DeliveryNotice deliveryNotice);



    /**
     * 根据 ID 查询发货通知单（包含明细）
     * @param id 发货通知单 ID
     * @return 发货通知单信息
     */
    DeliveryNotice getById(Long id);

    /**
     * 根据单据编号查询发货通知单（包含明细）
     * @param billNo 单据编号
     * @return 发货通知单信息
     */
    DeliveryNotice getByBillNo(String billNo);

    /**
     * 分页查询发货通知单（支持条件查询）
     * @param deliveryNotice 查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    TableDataInfo<DeliveryNotice> list(DeliveryNotice deliveryNotice, PageQuery pageQuery);

    /**
     * 根据客户 ID 查询发货通知单
     * @param customerId 客户 ID
     * @return 发货通知单列表
     */
    List<DeliveryNotice> listByCustomerId(String customerId);

    /**
     * 根据单据状态查询发货通知单
     * @param status 单据状态
     * @return 发货通知单列表
     */
    List<DeliveryNotice> listByStatus(String status);


}
