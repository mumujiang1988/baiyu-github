package com.ruoyi.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.entity.DeliveryNoticeEntry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 发货通知单明细 Mapper 接口
 */
@Mapper
public interface DeliveryNoticeEntryMapper extends BaseMapper<DeliveryNoticeEntry> {

    /**
     * 插入发货通知单明细
     * @param entry 明细信息
     * @return 影响行数
     */
    int insertEntry(DeliveryNoticeEntry entry);

    /**
     * 批量插入发货通知单明细
     * @param entries 明细列表
     * @return 影响行数
     */
    int batchInsert(@Param("entries") List<DeliveryNoticeEntry> entries);

    /**
     * 根据主表 ID 查询明细列表
     * @param deliveryNoticeId 主表 ID
     * @return 明细列表
     */
    DeliveryNoticeEntry selectByDeliveryNoticeId(@Param("deliveryNoticeId") String deliveryNoticeId, @Param("fmaterialId") String fmaterialId);

    /**
     * 根据主表单据编号查询明细列表
     * @param deliveryNoticeNo 主表单据编号
     * @return 明细列表
     */
    List<DeliveryNoticeEntry> selectByDeliveryNoticeNo(@Param("deliveryNoticeNo") String deliveryNoticeNo);

    /**
     * 根据主表 ID 删除明细
     * @param deliveryNoticeNo 主表 ID
     * @return 影响行数
     */
    int deleteByDeliveryNoticeId(@Param("deliveryNoticeNo") String deliveryNoticeNo);

    /**
     * 根据主表单据编号删除明细
     * @param deliveryNoticeNo 主表单据编号
     * @return 影响行数
     */
    int deleteByDeliveryNoticeNo(@Param("deliveryNoticeNo") String deliveryNoticeNo);

    /**
     * 更新发货通知单明细
     * @param entry 明细信息
     * @return 影响行数
     */
    int updateEntry(DeliveryNoticeEntry entry);

    /**
     * 根据物料编码查询明细
     * @param materialId 物料编码
     * @return 明细信息
     */
    DeliveryNoticeEntry selectByMaterialId(@Param("materialId") String materialId);
}
