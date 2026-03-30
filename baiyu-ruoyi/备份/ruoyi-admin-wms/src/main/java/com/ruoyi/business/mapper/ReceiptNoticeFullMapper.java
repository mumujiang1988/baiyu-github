package com.ruoyi.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.entity.ReceiptNoticeFull;
import com.ruoyi.business.entity.ReceiveNoticeEntry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 检验单完整数据 Mapper接口
 */
@Mapper
public interface ReceiptNoticeFullMapper extends BaseMapper<ReceiptNoticeFull> {


    /**
     * 根据单据编号查询完整数据
     *
     * @param fBillNo 单据编号
     * @return 检验单数据
     */
    ReceiptNoticeFull selectReceiptNoticeFullByBillNo(@Param("fBillNo") String fBillNo);

    /**
     * 查询完整数据详情
     *
     * @param id 主键ID
     * @return 检验单数据
     */
    ReceiptNoticeFull selectReceiptNoticeFullById(@Param("id") Long id);

    /**
     * 新增收料通知单完整数据
     *
     * @param receiptNoticeFull 检验单数据
     * @return 结果
     */
    int insertReceiptNotice(ReceiptNoticeFull receiptNoticeFull);

    /**
     * 修改收料通知单完整数据
     *
     * @param receiptNoticeFull 检验单数据
     * @return 结果
     */
    int updateReceiptNotice(ReceiptNoticeFull receiptNoticeFull);

    /**
     * 删除收料通知单完整数据
     *
     * @param fid 主键ID
     * @return 结果
     */
    int deleteReceiptNoticeById(@Param("fid") Long fid);

    /**
     * 批量删除收料通知单完整数据
     *
     * @param ids 主键ID列表
     * @return 结果
     */
    int deleteReceiptNoticeBatch(@Param("list") List<Long> ids);

    /**
     * 根据单据编号删除收料通知单完整数据
     *
     * @param fBillNo 单据编号
     * @return 结果
     */
    int deleteReceiptNoticeByBillNo(@Param("fBillNo") String fBillNo);

    /**
     * 分页查询收料通知单完整数据
     *
     * @param receiptNoticeFull 查询条件
     * @param offset 偏移量
     * @param pageSize 每页大小
     * @return 检验单数据集合
     */
    List<ReceiptNoticeFull> selectReceiptNoticeFullByPage(
            @Param("receiptNoticeFull") ReceiptNoticeFull receiptNoticeFull,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);
}
