package com.ruoyi.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.entity.DeliveryNotice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 发货通知单 Mapper 接口
 */
@Mapper
public interface DeliveryNoticeMapper extends BaseMapper<DeliveryNotice> {

    /**
     * 插入发货通知单主表
     * @param deliveryNotice 发货通知单信息
     * @return 影响行数
     */
    int insertDeliveryNotice(DeliveryNotice deliveryNotice);

    /**
     * 根据 ID 查询发货通知单
     * @param fid 发货通知单 ID
     * @return 发货通知单信息
     */
    DeliveryNotice selectById(@Param("fid") Long fid);

    /**
     * 根据单据编号查询发货通知单
     * @param billNo 单据编号
     * @return 发货通知单信息
     */
    DeliveryNotice selectByBillNo(@Param("billNo") String billNo);

    /**
     * 根据 ID 更新发货通知单
     * @param deliveryNotice 发货通知单信息
     * @return 影响行数
     */
    int updateDeliveryNotice(DeliveryNotice deliveryNotice);

    /**
     * 根据 ID 删除发货通知单
     * @param id 发货通知单 ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据单据编号删除发货通知单
     * @param billNo 单据编号
     * @return 影响行数
     */
    int deleteByBillNo(@Param("billNo") String billNo);

    /**
     * 分页查询发货通知单
     * @param page 分页参数
     * @param deliveryNotice 查询条件
     * @return 分页结果
     */
    IPage<DeliveryNotice> selectPage(Page<DeliveryNotice> page, DeliveryNotice deliveryNotice);

    /**
     * 根据客户 ID 查询发货通知单
     * @param customerId 客户 ID
     * @return 发货通知单列表
     */
    List<DeliveryNotice> selectByCustomerId(@Param("customerId") String customerId);

    /**
     * 根据单据状态查询发货通知单
     * @param status 单据状态
     * @return 发货通知单列表
     */
    List<DeliveryNotice> selectByStatus(@Param("status") String status);
}
