package com.ruoyi.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.entity.PurchaseQuotation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 采购报价单主表Mapper接口
 */
public interface PurchaseQuotationMapper extends BaseMapper<PurchaseQuotation> {

    /**
     * 根据单据编号查询
     */
    PurchaseQuotation selectByBillNo(@Param("billNo") String billNo);
    PurchaseQuotation selectById(@Param("id") String id);

    /**
     * 根据单据编号查询（用于更新时排除自身）
     */
    PurchaseQuotation selectByBillNoForUpdate(@Param("billNo") String billNo, @Param("excludeFid") String excludeFid);

    /**
     * 采购报价单列表查询
     */
    List<PurchaseQuotation> selectList(@Param("purchaseQuotation") PurchaseQuotation purchaseQuotation);

    /**
     * 新增采购报价单
     */
    int insertPurchaseQuotation(@Param("purchaseQuotation") PurchaseQuotation purchaseQuotation);

    /**
     * 更新采购报价单
     */
    int updatePurchaseQuotation(@Param("purchaseQuotation") PurchaseQuotation purchaseQuotation);

    /**
     * 根据ID删除采购报价单
     */
    int deleteById(@Param("id") String id);

    /**
     * 分页查询采购报价单
     * @param page 分页参数
     * @param purchaseQuotation 查询条件
     * @return 分页结果
     */
    IPage<PurchaseQuotation> selectPage(Page<PurchaseQuotation> page, @Param("purchaseQuotation") PurchaseQuotation purchaseQuotation);
}
