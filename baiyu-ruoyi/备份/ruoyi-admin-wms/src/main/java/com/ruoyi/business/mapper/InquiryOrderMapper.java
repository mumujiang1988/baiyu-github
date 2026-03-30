package com.ruoyi.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.entity.InquiryOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 询价单主表 Mapper接口
 */
public interface InquiryOrderMapper extends BaseMapper<InquiryOrder> {

    /**
     * 根据单据编号查询
     */
    InquiryOrder selectByBillNo(@Param("fid") String fid);
    InquiryOrder selectById(@Param("id") String id);

    /**
     * 根据单据编号查询（用于更新时排除自身）
     */
    InquiryOrder selectByBillNoForUpdate(@Param("billNo") String billNo, @Param("excludeFid") String excludeFid);

    /**
     * 根据客户编码查询
     */
    List<InquiryOrder> selectByCustomerCode(@Param("customerCode") String customerCode);

    /**
     * 询价单列表查询
     */
    List<InquiryOrder> selectList(InquiryOrder inquiryOrder);
}
