package com.ruoyi.business.k3.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.Component.K3FormProcessorFactory;
import com.ruoyi.business.entity.DeliveryNotice;
import com.ruoyi.business.entity.DeliveryNoticeEntry;
import com.ruoyi.business.k3.config.k3config;
import com.ruoyi.business.k3.service.DeliveryNoticeService;
import com.ruoyi.business.mapper.DeliveryNoticeEntryMapper;
import com.ruoyi.business.mapper.DeliveryNoticeMapper;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 发货通知单服务实现类
 */
@Service
@Slf4j
public class DeliveryNoticeServiceImpl implements DeliveryNoticeService {

    @Resource
    private DeliveryNoticeMapper deliveryNoticeMapper;

    @Resource
    private DeliveryNoticeEntryMapper deliveryNoticeEntryMapper;

    @Resource
    private k3config k3configks;

    @Resource
    private K3FormProcessorFactory k3FormProcessorFactory;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result save(DeliveryNotice deliveryNotice) {
        try {
            // 1. 校验必填字段
            if (deliveryNotice.getFDate() == null) {
                return Result.error("日期不能为空");
            }
            if (deliveryNotice.getFCustomerID() == null || deliveryNotice.getFCustomerID().isEmpty()) {
                return Result.error("客户不能为空");
            }
            if (deliveryNotice.getFBillTypeID() == null || deliveryNotice.getFBillTypeID().isEmpty()) {
                return Result.error("单据类型不能为空");
            }
            if (deliveryNotice.getFSettleCurrID() == null || deliveryNotice.getFSettleCurrID().isEmpty()) {
                return Result.error("结算币别不能为空");
            }

            // 2. 检查单据编号是否已存在
            if (deliveryNotice.getFBillNo() != null && !deliveryNotice.getFBillNo().isEmpty()) {
                DeliveryNotice existing = deliveryNoticeMapper.selectByBillNo(deliveryNotice.getFBillNo());
                if (existing != null) {
                    return Result.error("单据编号 " + deliveryNotice.getFBillNo() + " 已存在");
                }
            }

            // 3. 设置默认值
            if (deliveryNotice.getFDocumentStatus() == null) {
                deliveryNotice.setFDocumentStatus("Z"); // 暂存状态
            }
            if (deliveryNotice.getFCreateDate() == null) {
                deliveryNotice.setFCreateDate(new Date());
            }

            // 4. 插入主表
            int result = deliveryNoticeMapper.insertDeliveryNotice(deliveryNotice);
            if (result <= 0) {
                return Result.error("发货通知单保存失败");
            }

            // 5. 插入明细（如果存在）
            if (deliveryNotice.getEntries() != null && !deliveryNotice.getEntries().isEmpty()) {
                List<DeliveryNoticeEntry> entryList = new ArrayList<>();
                for (DeliveryNoticeEntry entry : deliveryNotice.getEntries()) {
                    // 校验明细必填字段
                    if (entry.getFMaterialID() == null || entry.getFMaterialID().isEmpty()) {
                        return Result.error("物料编码不能为空");
                    }
                    if (entry.getFUnitID() == null || entry.getFUnitID().isEmpty()) {
                        return Result.error("销售单位不能为空");
                    }
                    if (entry.getFBaseUnitID() == null || entry.getFBaseUnitID().isEmpty()) {
                        return Result.error("基本单位不能为空");
                    }

                    // 设置关联关系
                    entry.setDeliveryNoticeId(deliveryNotice.getFID());
                    if (deliveryNotice.getFBillNo() != null) {
                        entry.setDeliveryNoticeNo(deliveryNotice.getFBillNo());
                    }

                    entryList.add(entry);
                }

                // 批量插入明细
                deliveryNoticeEntryMapper.batchInsert(entryList);
            }

            return Result.success("发货通知单保存成功", deliveryNotice);
        } catch (Exception e) {
            log.error("保存发货通知单失败", e);
            throw new RuntimeException("保存发货通知单失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result update(DeliveryNotice deliveryNotice) {
        try {
            // 1. 校验 ID
            if (deliveryNotice.getId() == null) {
                return Result.error("发货通知单 ID 不能为空");
            }

            // 2. 检查是否存在
            DeliveryNotice existing = deliveryNoticeMapper.selectById(deliveryNotice.getFID());
            if (existing == null) {
                return Result.error("发货通知单不存在");
            }

            // 3. 更新主表
            if (deliveryNoticeMapper.updateDeliveryNotice(deliveryNotice) <= 0) {
                return Result.error("发货通知单更新失败");
            }

            // 4. 更新明细（先删除后新增）
            if (deliveryNotice.getEntries() != null) {
                // 删除原有明细
                deliveryNoticeEntryMapper.deleteByDeliveryNoticeId(deliveryNotice.getFBillNo());

                // 新增明细
                if (!deliveryNotice.getEntries().isEmpty()) {
                    List<DeliveryNoticeEntry> entryList = new ArrayList<>();
                    for (DeliveryNoticeEntry entry : deliveryNotice.getEntries()) {
                        entry.setDeliveryNoticeId(deliveryNotice.getFID());
                        if (deliveryNotice.getFBillNo() != null) {
                            entry.setDeliveryNoticeNo(deliveryNotice.getFBillNo());
                        }
                        entryList.add(entry);
                    }
                    deliveryNoticeEntryMapper.batchInsert(entryList);
                }
            }

            return Result.success("发货通知单更新成功");
        } catch (Exception e) {
            log.error("更新发货通知单失败", e);
            throw new RuntimeException("更新发货通知单失败：" + e.getMessage(), e);
        }
    }



    @Override
    public DeliveryNotice getById(Long id) {
        // 查询主表
        DeliveryNotice deliveryNotice = deliveryNoticeMapper.selectById(id);
        if (deliveryNotice != null) {
            // 查询明细
            List<DeliveryNoticeEntry> entries = deliveryNoticeEntryMapper.selectByDeliveryNoticeNo(deliveryNotice.getFBillNo());
            deliveryNotice.setEntries(entries);
        }
        return deliveryNotice;
    }

    @Override
    public DeliveryNotice getByBillNo(String billNo) {
        // 查询主表
        DeliveryNotice deliveryNotice = deliveryNoticeMapper.selectByBillNo(billNo);
        if (deliveryNotice != null) {
            // 查询明细
            List<DeliveryNoticeEntry> entries = deliveryNoticeEntryMapper.selectByDeliveryNoticeNo(billNo);
            deliveryNotice.setEntries(entries);
        }
        return deliveryNotice;
    }

    @Override
    public TableDataInfo<DeliveryNotice> list(DeliveryNotice deliveryNotice, PageQuery pageQuery) {
        // 使用 MyBatis Plus 的分页插件
        Page<DeliveryNotice> page = pageQuery.build();

        // 执行分页查询
        IPage<DeliveryNotice> pageResult = deliveryNoticeMapper.selectPage(page, deliveryNotice);

        return TableDataInfo.build(pageResult);
    }

    @Override
    public List<DeliveryNotice> listByCustomerId(String customerId) {
        return deliveryNoticeMapper.selectByCustomerId(customerId);
    }

    @Override
    public List<DeliveryNotice> listByStatus(String status) {
        return deliveryNoticeMapper.selectByStatus(status);
    }

}
