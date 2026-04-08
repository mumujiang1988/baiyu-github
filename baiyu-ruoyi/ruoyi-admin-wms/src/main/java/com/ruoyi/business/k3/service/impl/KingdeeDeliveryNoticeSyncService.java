package com.ruoyi.business.k3.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.business.entity.DeliveryNotice;
import com.ruoyi.business.entity.DeliveryNoticeEntry;
import com.ruoyi.business.k3.config.Dictionaryconfig;
import com.ruoyi.business.k3.util.K3DataUtils;
import com.ruoyi.business.mapper.DeliveryNoticeEntryMapper;
import com.ruoyi.business.mapper.DeliveryNoticeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 金蝶发货通知单同步服务实现类
 * 负责从金蝶系统同步发货通知单数据到本地数据库
 *
 * @author system
 */
@Slf4j
@Service
public class KingdeeDeliveryNoticeSyncService {

    @Autowired
    private Dictionaryconfig dictionaryconfig;

    @Autowired
    private DeliveryNoticeMapper deliveryNoticeMapper;

    @Autowired
    private DeliveryNoticeEntryMapper deliveryNoticeEntryMapper;

    private static final int PAGE_SIZE = 5000;

    /**
     * 同步金蝶发货通知单数据到本地数据库
     * @return 同步的总记录数
     */
    @Transactional(rollbackFor = Exception.class)
    public int syncDeliveryNoticesFromK3() {
        log.info("开始同步金蝶发货通知单数据...");

        int totalCount = 0;

        try {
            // ============ 1. 同步发货通知单主表 ============
//            int mainTableCount = syncDeliveryNoticeMainTable();
//            totalCount += mainTableCount;
//            log.info("发货通知单主表同步完成，共处理 {} 条数据", mainTableCount);

            // ============ 2. 同步发货通知单明细表 ============
            int detailTableCount = syncDeliveryNoticeDetailTable();
            totalCount += detailTableCount;
            log.info("发货通知单明细表同步完成，共处理 {} 条数据", detailTableCount);

            log.info("金蝶发货通知单数据同步完成，总计处理 {} 条数据", totalCount);
            return totalCount;

        } catch (Exception e) {
            log.error("同步发货通知单数据失败", e);
            throw new RuntimeException("同步发货通知单数据失败：" + e.getMessage(), e);
        }
    }

    /**
     * 同步发货通知单主表数据
     */
    private int syncDeliveryNoticeMainTable() {
        int totalCount = 0;
        int startRow = 0;

        while (true) {
            log.info("查询发货通知单主表数据，起始行：{}, 页大小：{}", startRow, PAGE_SIZE);

            List<List<Object>> pageData = dictionaryconfig.queryDeliveryNoticeList(startRow, PAGE_SIZE);

            if (pageData == null || pageData.isEmpty()) {
                log.info("未查询到更多发货通知单主表数据，同步结束");
                break;
            }

            log.info("本次查询到 {} 条发货通知单主表数据", pageData.size());

            for (List<Object> rowData : pageData) {
                try {
                    DeliveryNotice deliveryNotice = parseDeliveryNotice(rowData);

                    if (deliveryNotice != null) {
                        // 检查是否已存在
                        DeliveryNotice existing = deliveryNoticeMapper.selectByBillNo(deliveryNotice.getFBillNo());

                        if (existing != null) {
                            // 更新现有记录
                            deliveryNotice.setId(existing.getId());
                            deliveryNoticeMapper.updateDeliveryNotice(deliveryNotice);
                            log.debug("更新发货通知单：{}", deliveryNotice.getFBillNo());
                        } else {
                            // 新增记录
                            deliveryNoticeMapper.insertDeliveryNotice(deliveryNotice);
                            log.debug("新增发货通知单：{}", deliveryNotice.getFBillNo());
                        }

                        totalCount++;
                    }
                } catch (Exception e) {
                    log.error("解析或保存发货通知单主表数据失败，行数据：{}", rowData, e);
                }
            }

            // 如果返回的数据小于页大小，说明已经是最后一页
            if (pageData.size() < PAGE_SIZE) {
                break;
            }

            startRow += PAGE_SIZE;
        }

        return totalCount;
    }

    /**
     * 同步发货通知单明细表数据（无事务）
     */
    private int syncDeliveryNoticeDetailTable() {
        int totalCount = 0;
        int startRow = 0;
        int retryCount = 0;
        final int maxRetries = 3;

        while (true) {
            try {
                log.info("查询发货通知单明细表数据，起始行：{}, 页大小：{}", startRow, PAGE_SIZE);

                List<List<Object>> pageData = dictionaryconfig.queryDeliveryNoticeEntryList(startRow, PAGE_SIZE);


                log.info("本次查询到 {} 条发货通知单明细表数据", pageData.size());

                for (List<Object> rowData : pageData) {
                    try {
                        DeliveryNoticeEntry entry = parseDeliveryNoticeEntry(rowData);

                        if (entry != null) {

                            DeliveryNoticeEntry existingList = deliveryNoticeEntryMapper.selectByDeliveryNoticeId(entry.getDeliveryNoticeId(), entry.getFMaterialID());

                            boolean exists = false;
                            if (existingList != null && existingList.getFID() != null) {

                                if (existingList.getFID() != null &&
                                    existingList.getFID().equals(entry.getFID())) {
                                        // 更新现有记录
                                        entry.setId(existingList.getId());
                                        deliveryNoticeEntryMapper.updateEntry(entry);
                                        log.debug("更新发货通知单明细：{} - 分录 {}", entry.getDeliveryNoticeNo(), entry.getFID());
                                        break;
                                    }

                            }

                            if (!exists) {
                                // 新增记录
                                deliveryNoticeEntryMapper.insertEntry(entry);
                                log.debug("新增发货通知单明细：{} - 分录 {}", entry.getDeliveryNoticeNo(), entry.getFID());
                            }

                            totalCount++;
                        }
                    } catch (Exception e) {
                        log.error("解析或保存发货通知单明细表数据失败，行数据：{}", rowData, e);
                    }
                }

                // 如果返回的数据小于页大小，说明已经是最后一页
                if (pageData.size() < PAGE_SIZE) {
                    break;
                }
                startRow += PAGE_SIZE;

            }  catch (Exception e) {
                // 其他异常直接抛出
                log.error("同步发货通知单明细表数据失败", e);
                throw new RuntimeException("同步发货通知单明细表数据失败：" + e.getMessage(), e);
            }
        }

        return totalCount;
    }

    /**
     * 解析发货通知单主表数据
     */
    private DeliveryNotice parseDeliveryNotice(List<Object> rowData) {
        if (rowData == null || rowData.isEmpty()) {
            return null;
        }

        DeliveryNotice notice = new DeliveryNotice();

        try {
            // 按照 FieldKeys 的顺序解析字段
            int index = 0;

            notice.setFID(K3DataUtils.getString(rowData, index++));              // FID
            notice.setFBillNo(K3DataUtils.getString(rowData, index++));        // FBillNo
            notice.setFDocumentStatus(K3DataUtils.getString(rowData, index++));// FDocumentStatus
            notice.setFDate(K3DataUtils.getDate(rowData, index++));            // FDate
            notice.setFCustomerID(K3DataUtils.getString(rowData, index++));    // FCustomerID
            notice.setFSalesManID(K3DataUtils.getString(rowData, index++));    // FSalesManID
            notice.setFCarrierID(K3DataUtils.getString(rowData, index++));     // FCarrierID
            notice.setFReceiverID(K3DataUtils.getString(rowData, index++));    // FReceiverID
            notice.setFSettleID(K3DataUtils.getString(rowData, index++));      // FSettleID
            notice.setFPayerID(K3DataUtils.getString(rowData, index++));       // FPayerID
            notice.setFCreatorId(K3DataUtils.getString(rowData, index++));     // FCreatorId
            notice.setFCreateDate(K3DataUtils.getDate(rowData, index++));      // FCreateDate
            notice.setFModifyDate(K3DataUtils.getDate(rowData, index++));      // FModifyDate
            notice.setFApproveDate(K3DataUtils.getDate(rowData, index++));     // FApproveDate
            notice.setFCancelDate(K3DataUtils.getDate(rowData, index++));      // FCancelDate
            notice.setFModifierId(K3DataUtils.getString(rowData, index++));    // FModifierId
            notice.setFBillTypeID(K3DataUtils.getString(rowData, index++));    // FBillTypeID
            notice.setFApproverID(K3DataUtils.getString(rowData, index++));    // FApproverID
            notice.setFCancellerID(K3DataUtils.getString(rowData, index++));   // FCancellerID
            notice.setFCancelStatus(K3DataUtils.getString(rowData, index++));  // FCancelStatus
            notice.setFOwnerIdHead(K3DataUtils.getString(rowData, index++));   // FOwnerIdHead
            notice.setFRECEIPTCONDITIONID(K3DataUtils.getString(rowData, index++)); // FRECEIPTCONDITIONID
            notice.setFHeadDeliveryWay(K3DataUtils.getString(rowData, index++));    // FHeadDeliveryWay
            notice.setFReceiveAddress(K3DataUtils.getString(rowData, index++));     // FReceiveAddress
            notice.setFHeadLocId(K3DataUtils.getString(rowData, index++));          // FHeadLocId
            notice.setFLocalCurrID(K3DataUtils.getString(rowData, index++));        // FLocalCurrID
            notice.setFExchangeTypeID(K3DataUtils.getString(rowData, index++));     // FExchangeTypeID
            notice.setFExchangeRate(K3DataUtils.getBigDecimal(rowData, index++));   // FExchangeRate
            notice.setFSettleTypeID(K3DataUtils.getString(rowData, index++));       // FSettleTypeID
            notice.setFSettleCurrID(K3DataUtils.getString(rowData, index++));       // FSettleCurrID
            notice.setFBillTaxAmount(K3DataUtils.getBigDecimal(rowData, index++));  // FBillTaxAmount
            notice.setFBillAmount(K3DataUtils.getBigDecimal(rowData, index++));     // FBillAmount
            notice.setFBillAllAmount(K3DataUtils.getBigDecimal(rowData, index++));  // FBillAllAmount
            notice.setFIsIncludedTax(K3DataUtils.getInteger(rowData, index++));         // FIsIncludedTax
            notice.setFAllDisCount(K3DataUtils.getBigDecimal(rowData, index++));    // FAllDisCount

            return notice;

        } catch (Exception e) {
            log.error("解析发货通知单主表数据失败", e);
            return null;
        }
    }

    /**
     * 解析发货通知单明细表数据
     */
    private DeliveryNoticeEntry parseDeliveryNoticeEntry(List<Object> rowData) {
        if (rowData == null || rowData.isEmpty()) {
            log.warn("明细表数据行为空");
            return null;
        }

        // 检查数据长度是否足够
        if (rowData.size() < 5) {
            log.error("明细表数据长度不足，实际长度：{}, 数据内容：{}", rowData.size(), rowData);
            return null;
        }

        DeliveryNoticeEntry entry = new DeliveryNoticeEntry();

        try {
            // 按照 FieldKeys 的顺序解析字段
            int index = 0;

            // 安全获取数据，避免 IndexOutOfBoundsException
            entry.setFID(K3DataUtils.getLong(rowData, index++));                  // FID
            entry.setDeliveryNoticeNo(K3DataUtils.getString(rowData, index++));                  // FID
            entry.setFMaterialID(K3DataUtils.getString(rowData, index++));        // FMaterialID
            entry.setFMaterialName(K3DataUtils.getString(rowData, index++));      // FMaterialName
            entry.setFMateriaModel(K3DataUtils.getString(rowData, index++));      // FMateriaModel
            entry.setFMateriaType(K3DataUtils.getString(rowData, index++));       // FMateriaType
            entry.setFUnitID(K3DataUtils.getString(rowData, index++));            // FUnitID
            entry.setFQty(K3DataUtils.getBigDecimal(rowData, index++));           // FQty
            entry.setFStockID(K3DataUtils.getString(rowData, index++));           // FStockID
            entry.setFNoteEntry(K3DataUtils.getString(rowData, index++));         // FNoteEntry
            entry.setFBaseUnitID(K3DataUtils.getString(rowData, index++));        // FBaseUnitID
            entry.setFBaseUnitQty(K3DataUtils.getBigDecimal(rowData, index++));   // FBaseUnitQty
            entry.setFDeliveryLoc(K3DataUtils.getString(rowData, index++));       // FDeliveryLoc
            entry.setFDeliveryLAddress(K3DataUtils.getString(rowData, index++));  // FDeliveryLAddress
            entry.setFOrderNo(K3DataUtils.getString(rowData, index++));           // FOrderNo
            entry.setFOrderSeq(K3DataUtils.getString(rowData, index++));          // FOrderSeq
            entry.setFCustMatID(K3DataUtils.getString(rowData, index++));         // FCustMatID
            entry.setFLot(K3DataUtils.getString(rowData, index++));               // FLot
            entry.setFIsFree(K3DataUtils.getInteger(rowData, index++));               // FIsFree
            entry.setFCustMatName(K3DataUtils.getString(rowData, index++));       // FCustMatName
            entry.setFRemainOutQty(K3DataUtils.getBigDecimal(rowData, index++));  // FRemainOutQty
            entry.setFStockStatusId(K3DataUtils.getString(rowData, index++));     // FStockStatusId
            entry.setFPriceUnitQty(K3DataUtils.getBigDecimal(rowData, index++));  // FPriceUnitQty
            entry.setFPrice(K3DataUtils.getBigDecimal(rowData, index++));         // FPrice
            entry.setFTaxPrice(K3DataUtils.getBigDecimal(rowData, index++));      // FTaxPrice
            entry.setFTaxCombination(K3DataUtils.getString(rowData, index++));    // FTaxCombination
            entry.setFEntryTaxRate(K3DataUtils.getBigDecimal(rowData, index++));  // FEntryTaxRate
            entry.setFPriceCoefficient(K3DataUtils.getBigDecimal(rowData, index++)); // FPriceCoefficient
            entry.setFSysPrice(K3DataUtils.getBigDecimal(rowData, index++));      // FSysPrice
            entry.setFLimitDownPrice(K3DataUtils.getBigDecimal(rowData, index++));// FLimitDownPrice
            entry.setFBefDisAmt(K3DataUtils.getBigDecimal(rowData, index++));     // FBefDisAmt
            entry.setFBefDisAllAmt(K3DataUtils.getBigDecimal(rowData, index++));  // FBefDisAllAmt
            entry.setFDiscountRate(K3DataUtils.getBigDecimal(rowData, index++));  // FDiscountRate
            entry.setF_ora_Decimal(K3DataUtils.getBigDecimal(rowData, index++));  // F_ora_Decimal
            entry.setF_ora_Decimal1(K3DataUtils.getBigDecimal(rowData, index++)); // F_ora_Decimal1
            entry.setF_c(K3DataUtils.getBigDecimal(rowData, index++));            // F_c
            entry.setF_k(K3DataUtils.getBigDecimal(rowData, index++));            // F_k
            entry.setF_g(K3DataUtils.getBigDecimal(rowData, index++));            // F_g
            entry.setF_tj(K3DataUtils.getBigDecimal(rowData, index++));           // F_tj
            entry.setF_mz(K3DataUtils.getBigDecimal(rowData, index++));           // F_mz
            entry.setF_jz(K3DataUtils.getBigDecimal(rowData, index++));           // F_jz
            entry.setF_mzz(K3DataUtils.getBigDecimal(rowData, index++));          // F_mzz
            entry.setF_xs(K3DataUtils.getBigDecimal(rowData, index++));           // F_xs
            entry.setF_zxs(K3DataUtils.getBigDecimal(rowData, index++));          // F_zxs
            entry.setF_cgzj(K3DataUtils.getBigDecimal(rowData, index++));         // F_cgzj
            entry.setF_zjzj(K3DataUtils.getBigDecimal(rowData, index++));         // F_zjzj
            entry.setF_kpdj(K3DataUtils.getBigDecimal(rowData, index++));         // F_kpdj
            entry.setF_kpzj(K3DataUtils.getBigDecimal(rowData, index++));         // F_kpzj
            entry.setF_zwbgpm(K3DataUtils.getString(rowData, index++));           // F_zwbgpm
            entry.setF_ywbgpm(K3DataUtils.getString(rowData, index++));           // F_ywbgpm
            entry.setF_jzz(K3DataUtils.getBigDecimal(rowData, index++));          // F_jzz
            entry.setF_jcsj(K3DataUtils.getDate(rowData, index++));               // F_jcsj
            entry.setF_khhh(K3DataUtils.getString(rowData, index++));             // F_khhh
            entry.setF_sfbg(K3DataUtils.getInteger(rowData, index++));                // F_sfbg
            entry.setF_ddhsdj(K3DataUtils.getBigDecimal(rowData, index++));       // F_ddhsdj
            entry.setF_tsl(K3DataUtils.getBigDecimal(rowData, index++));          // F_tsl
            entry.setF_bgdw(K3DataUtils.getString(rowData, index++));             // F_bgdw
            entry.setF_tcblNEW(K3DataUtils.getBigDecimal(rowData, index++));      // F_tcblNEW
            entry.setF_BCFYNEW(K3DataUtils.getBigDecimal(rowData, index++));      // F_BCFYNEW
            entry.setF_GLBCFY(K3DataUtils.getBigDecimal(rowData, index++));       // F_GLBCFY
            entry.setF_ffyq(K3DataUtils.getString(rowData, index++));             // F_ffyq
            entry.setFddshrq(K3DataUtils.getDate(rowData, index++));              // Fddshrq
            entry.setFxsddh(K3DataUtils.getString(rowData, index++));             // Fxsddh
            entry.setFxsddhh(K3DataUtils.getString(rowData, index++));            // Fxsddhh
            entry.setFbzgctg(K3DataUtils.getString(rowData, index++));            // Fbzgctg
            entry.setFypd(K3DataUtils.getInteger(rowData, index++));                  // Fypd
            entry.setFykc(K3DataUtils.getInteger(rowData, index++));                  // Fykc
            entry.setF_cty_BaseProperty(K3DataUtils.getString(rowData, index++)); // F_cty_BaseProperty
            entry.setFrkshrq(K3DataUtils.getDate(rowData, index++));              // Frkshrq
            entry.setFckshrq(K3DataUtils.getDate(rowData, index++));              // Fckshrq
            entry.setF_BZFS(K3DataUtils.getString(rowData, index++));             // F_BZFS

            // 设置关联关系
            // 注意：需要从主表数据中获取 delivery_notice_id 和 delivery_notice_no
            // 这里假设 FBillNo 在明细数据中也存在，如果没有需要调整
            String billNo = findBillNoByFID(entry.getFID());
            if (billNo != null) {
                entry.setDeliveryNoticeNo(billNo);
            }

            return entry;

        } catch (Exception e) {
            log.error("解析发货通知单明细表数据失败，数据长度：{}, 数据内容：{}",
                     rowData.size(), rowData, e);
            return null;
        }
    }

    /**
     * 根据 FID 查找对应的单据编号
     */
    private String findBillNoByFID(Long FID) {
        if (FID == null) {
            return null;
        }

        try {
            // 使用 selectById 代替 selectOne，避免 MybatisMapperProxy 的 DefaultMethodInvoker 问题
            DeliveryNotice notice = deliveryNoticeMapper.selectById(FID);
            return notice != null ? notice.getFBillNo() : null;
        } catch (Exception e) {
            log.error("根据 FID 查询单据编号失败，FID: {}", FID, e);
            return null;
        }
    }
}
