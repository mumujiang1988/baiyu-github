package com.ruoyi.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.entity.PurchaseQuotationEntry;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 采购报价单明细表Mapper接口
 */
public interface PurchaseQuotationEntryMapper extends BaseMapper<PurchaseQuotationEntry> {

    /**
     * 根据主表ID查询明细列表
     */
    List<PurchaseQuotationEntry> selectByFid(@Param("fid") String fid);

    /**
     * 根据单据编号查询明细列表
     */
    List<PurchaseQuotationEntry> selectByBillNo(@Param("billNo") String billNo);

    /**
     * 批量插入明细
     */
    int insertBatch(@Param("entries") List<PurchaseQuotationEntry> entries);

    /**
     * 根据主表ID删除明细
     */
    int deleteByFid(@Param("id") String id);

    /**
     * 根据单据编号和产品名称查询明细
     */
    List<PurchaseQuotationEntry> selectByBillNoAndProductName(@Param("billNo") String billNo, @Param("productName") String productName);
}
