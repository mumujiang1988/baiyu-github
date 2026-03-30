package com.ruoyi.business.k3.service;

import com.ruoyi.business.entity.InquiryOrder;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;

import java.util.List;

/**
 * 询价单服务接口
 */
public interface InquiryOrderService {

    /**
     * 同步金蝶询价单数据到本地数据库
     * 包含主表和明细表的同步
     * @return 同步的总记录数
     */
    int syncInquiryOrdersFromK3();

    /**
     * 分页查询询价单
     * @param inquiryOrder 查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    TableDataInfo<InquiryOrder> selectPageInquiryOrder(InquiryOrder inquiryOrder, PageQuery pageQuery);

    /**
     * 根据ID查询询价单
     * @param id 询价单ID
     * @return 询价单信息
     */
    InquiryOrder selectById(String id);

    /**
     * 根据单据编号查询询价单
     * @param billNo 单据编号
     * @return 询价单信息
     */
    InquiryOrder selectByBillNo(String billNo);

    /**
     * 新增询价单
     * @param inquiryOrder 询价单信息
     * @return 操作结果
     */
    Result addInquiryOrder(InquiryOrder inquiryOrder);

    /**
     * 更新询价单
     * @param inquiryOrder 询价单信息
     * @return 操作结果
     */
    Result updateInquiryOrder(InquiryOrder inquiryOrder);

    /**
     * 删除询价单
     * @param fid 询价单ID
     * @return 操作结果
     */
    Result deleteInquiryOrder(String fid);
}
