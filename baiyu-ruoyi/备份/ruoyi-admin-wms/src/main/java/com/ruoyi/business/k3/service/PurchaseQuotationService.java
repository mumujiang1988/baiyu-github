package com.ruoyi.business.k3.service;

import com.ruoyi.business.entity.PurchaseQuotation;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;

/**
 * 采购报价单服务接口
 */
public interface PurchaseQuotationService {

    /**
     * 同步金蝶采购报价单数据到本地数据库
     * 包含主表和明细表的同步
     * @return 同步的总记录数
     */
    int syncPurchaseQuotationsFromK3();

    /**
     * 分页查询采购报价单
     * @param purchaseQuotation 查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    TableDataInfo<PurchaseQuotation> selectPagePurchaseQuotation(PurchaseQuotation purchaseQuotation, PageQuery pageQuery);

    /**
     * 根据ID查询采购报价单
     * @param id 采购报价单ID
     * @return 采购报价单信息
     */
    PurchaseQuotation selectById(String id);

    /**
     * 根据单据编号查询采购报价单
     * @param billNo 单据编号
     * @return 采购报价单信息
     */
    PurchaseQuotation selectByBillNo(String billNo);

    /**
     * 新增采购报价单
     * @param purchaseQuotation 采购报价单信息
     * @return 操作结果
     */
    Result addPurchaseQuotation(PurchaseQuotation purchaseQuotation);

    /**
     * 更新采购报价单
     * @param purchaseQuotation 采购报价单信息
     * @return 操作结果
     */
    Result updatePurchaseQuotation(PurchaseQuotation purchaseQuotation);

    /**
     * 删除采购报价单
     * @param id 采购报价单ID
     * @return 操作结果
     */
    Result deletePurchaseQuotation(String id);
}
