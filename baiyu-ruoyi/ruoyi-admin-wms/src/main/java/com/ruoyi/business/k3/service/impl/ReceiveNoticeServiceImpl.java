package com.ruoyi.business.k3.service.impl;

import com.ruoyi.business.entity.ReceiveNotice;
import com.ruoyi.business.entity.ReceiveNoticeEntry;
import com.ruoyi.business.k3.config.Dictionaryconfig;
import com.ruoyi.business.k3.service.ReceiveNoticeService;
import com.ruoyi.business.k3.util.K3DataUtils;
import com.ruoyi.business.mapper.ReceiveNoticeEntryMapper;
import com.ruoyi.business.mapper.ReceiveNoticeMapper;

import com.ruoyi.business.util.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ruoyi.business.k3.util.K3DataUtils.getLong;

@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class ReceiveNoticeServiceImpl implements ReceiveNoticeService {

    @Resource
    private ReceiveNoticeMapper receiveNoticeMapper;

    @Resource
    private ReceiveNoticeEntryMapper receiveNoticeEntryMapper;

    @Resource
    private Dictionaryconfig dictionaryconfig;


    @Override
    public ReceiveNotice getById(Long fid) {
        ReceiveNotice receiveNotice = receiveNoticeMapper.selectById(fid);
        if (receiveNotice != null){
            receiveNotice.setReceiveNoticeEntry(receiveNoticeEntryMapper.selectByBillNo(receiveNotice.getFBillNo()));
        }
        return receiveNotice;
    }

    @Override
    public ReceiveNotice getByBillNo(String billNo) {
        return receiveNoticeMapper.selectByBillNo(billNo);
    }

    @Override
    public Result create(ReceiveNotice receiveNotice, List<ReceiveNoticeEntry> entries) {
        Assert.notNull(receiveNotice, "收料通知单对象不能为空");

        // 检查单据编号是否已存在
        if (receiveNotice.getFBillNo() != null) {
            ReceiveNotice existing = receiveNoticeMapper.selectByBillNo(receiveNotice.getFBillNo());
            if (existing != null) {
                return Result.error("单据编号已存在: " + receiveNotice.getFBillNo());
            }
        }

        // 插入主表
        int result = receiveNoticeMapper.insert(receiveNotice);
        if (result <= 0) {
            return Result.error("保存收料通知单失败");
        }

        // 插入明细表
        if (entries != null && !entries.isEmpty()) {
            for (ReceiveNoticeEntry entry : entries) {
                entry.setFBillNo(receiveNotice.getFBillNo()); // 关联单据编号
            }
            receiveNoticeEntryMapper.batchInsert(entries);
        }

        return Result.success("收料通知单保存成功");
    }

    @Override
    public Result update(ReceiveNotice receiveNotice, List<ReceiveNoticeEntry> entries) {
        Assert.notNull(receiveNotice, "收料通知单对象不能为空");
        Assert.notNull(receiveNotice.getFBillNo(), "收料通知单ID不能为空");

        // 更新主表
        int result = receiveNoticeMapper.updateById(receiveNotice);
        if (result <= 0) {
            return Result.error("更新收料通知单失败");
        }

        // 先删除原有明细
        receiveNoticeEntryMapper.deleteByBillNo(receiveNotice.getFBillNo());

        // 重新插入明细
        if (entries != null && !entries.isEmpty()) {
            for (ReceiveNoticeEntry entry : entries) {
                entry.setFBillNo(receiveNotice.getFBillNo()); // 确保关联正确的单据编号
            }
            receiveNoticeEntryMapper.batchInsert(entries);
        }

        return Result.success("收料通知单更新成功");
    }

    @Override
    public Result deleteById(Long fid) {
        Assert.notNull(fid, "收料通知单ID不能为空");

        // 根据ID获取单据信息
        ReceiveNotice notice = receiveNoticeMapper.selectById(fid);
        if (notice == null) {
            return Result.error("收料通知单不存在");
        }

        // 先删除明细表记录
        receiveNoticeEntryMapper.deleteByBillNo(notice.getFBillNo());

        // 删除主表记录
        int result = receiveNoticeMapper.deleteById(fid);
        if (result <= 0) {
            return Result.error("删除收料通知单失败");
        }

        return Result.success("收料通知单删除成功");
    }

    @Override
    public List<ReceiveNotice> getList(ReceiveNotice receiveNotice, int pageNum, int pageSize) {
        return receiveNoticeMapper.selectListByConditionPage(pageNum, pageSize, receiveNotice);
    }



    @Override
    public int syncReceiveNoticeEntriesMultiThread() {
        log.info("开始同步金蝶收料通知单数据...");
        int pageSize = 3000;
        int processedCount = 0;
        int startRow = 0;

        try {
            while (true) {
                // 获取收料通知单数据
                List<List<Object>> pageData = dictionaryconfig.queryReceiveNoticeList(startRow, pageSize);

                log.info("开始处理第 {} 行到第 {} 行的收料通知单数据", startRow, startRow + pageData.size());

                // 解析页面数据为实体对象
                List<ReceiveNotice> pageNotices = parseReceiveNoticePage(pageData);
                if (!pageNotices.isEmpty()) {
                    // 批量处理新增和更新
                    AtomicInteger insertCount = new AtomicInteger(0);
                    AtomicInteger updateCount = new AtomicInteger(0);

                    // 遍历每条记录，判断是新增还是更新
                    for (ReceiveNotice notice : pageNotices) {
                        // 检查是否存在相同单据编号的记录
                        ReceiveNotice existing = receiveNoticeMapper.selectById(notice.getFid());

                        if (existing != null) {
                            // 存在相同单据编号的记录，进行更新操作
                            notice.setFid(existing.getFid()); // 设置主键
                            receiveNoticeMapper.updateById(notice);
                            updateCount.incrementAndGet();
                        } else {
                            // 不存在相同条件的记录，进行新增操作
                            receiveNoticeMapper.insert(notice);
                            insertCount.incrementAndGet();
                        }
                    }

                    int pageCount = insertCount.get() + updateCount.get();
                    processedCount += pageCount;
                    log.debug("页面处理完成，新增 {} 条，更新 {} 条，总计 {} 条", insertCount.get(), updateCount.get(), pageCount);
                }

                // 如果当前页数据少于页大小，说明已经是最后一页
                if (pageData.size() < pageSize) {
                    break;
                }

                startRow += pageSize;
            }

            log.info("收料通知单同步完成，总计处理：{} 条", processedCount);
        } catch (Exception e) {
            log.error("同步收料通知单异常", e);
            throw new RuntimeException(e);
        }

        return processedCount;
    }

    /**
     * 解析收料通知单页面数据
     */
    private List<ReceiveNotice> parseReceiveNoticePage(List<List<Object>> pageData) {
        List<ReceiveNotice> notices = new ArrayList<>();

        for (List<Object> rowData : pageData) {
            try {
                ReceiveNotice notice = convertToReceiveNotice(rowData);
                notices.add(notice);
            } catch (Exception e) {
                log.error("解析收料通知单数据失败: {}", e.getMessage());
                // 继续处理下一条数据
            }
        }

        return notices;
    }

    /**
     * 将金蝶数据转换为ReceiveNotice实体
     */
    private ReceiveNotice convertToReceiveNotice(List<Object> rowData) {
        ReceiveNotice notice = new ReceiveNotice();

        // 使用K3DataUtils进行数据转换
        notice.setFid(getLong(rowData, 0)); // FID (ID)
        notice.setFBillNo(K3DataUtils.getString(rowData, 1)); // FBillNo
        notice.setFDocumentStatus(K3DataUtils.getString(rowData, 2)); // FDocumentStatus
        notice.setFBillTypeID(getLong(rowData, 3)); // FBillTypeID
        notice.setFDate(K3DataUtils.getLocalDate(rowData, 4)); // FDate
        notice.setFReceiveDeptId(getLong(rowData, 5)); // FReceiveDeptId
        notice.setFPurOrgId(getLong(rowData, 6)); // FPurOrgId
        notice.setFReceiverId(getLong(rowData, 7)); // FReceiverId
        notice.setFjcf(K3DataUtils.getBigDecimal(rowData, 8)); // F_jcf
        notice.setFSupplierId(K3DataUtils.getString(rowData, 9)); // FSupplierId
        notice.setFgcbz(K3DataUtils.getString(rowData, 10)); // F_GCBZ
        notice.setFshdz(K3DataUtils.getString(rowData, 11)); // F_SHDZ
        notice.setFDATEjz(K3DataUtils.getLocalDate(rowData, 12)); // F_DATE_jz
        notice.setFdjtjcf(K3DataUtils.getBigDecimal(rowData, 13)); // F_djtjcf
        notice.setFSupplyId(K3DataUtils.getString(rowData, 14)); // F_SUPPLY_ID
        notice.setFProviderContactId(getLong(rowData, 15)); // FProviderContactId
        notice.setFSupplyAddress(K3DataUtils.getString(rowData, 16)); // FSupplyAddress
        notice.setFSettleId(K3DataUtils.getString(rowData, 17)); // FSettleId
        notice.setFChargeId(K3DataUtils.getString(rowData, 18)); // FChargeId
        notice.setFSettleModeId(K3DataUtils.getString(rowData, 19)); // FSettleModeId
        notice.setFPayConditionId(K3DataUtils.getString(rowData, 20)); // FPayConditionId
        notice.setFSettleCurrId(K3DataUtils.getString(rowData, 21)); // FSettleCurrId
        notice.setFAllDisCount(K3DataUtils.getBigDecimal(rowData, 22)); // FAllDisCount
        notice.setFCreatorId(K3DataUtils.getString(rowData, 23)); // FCreatorId
        notice.setFCreateDate(K3DataUtils.getLocalDateTime(rowData, 24)); // FCreateDate
        notice.setFModifierId(K3DataUtils.getString(rowData, 25)); // FModifierId
        notice.setFModifyDate(K3DataUtils.getLocalDateTime(rowData, 26)); // FModifyDate
        notice.setFApproveDate(K3DataUtils.getLocalDateTime(rowData, 27)); // FApproveDate
        notice.setFApproverId(K3DataUtils.getString(rowData, 28)); // FApproverId
        notice.setFctyDate(K3DataUtils.getLocalDateTime(rowData, 29)); // F_cty_Date

        return notice;
    }

    @Override
    public int syncReceiveNoticeEntryData() {
        log.info("开始同步金蝶收料通知单明细数据...");
        int pageSize = 3000;
        int processedCount = 0;
        int startRow = 0;
        AtomicInteger totalInsertCount = new AtomicInteger(0);
        AtomicInteger totalUpdateCount = new AtomicInteger(0);

        try {
            while (true) {
                // 获取收料通知单明细数据
                List<List<Object>> pageData = dictionaryconfig.queryReceiveNoticeDetailList(startRow, pageSize);
                log.info("开始处理第 {} 行到第 {} 行的收料通知单明细数据", startRow, startRow + pageData.size());

                // 遍历每条明细记录
                for (List<Object> notice : pageData) {
                    try {
                        ReceiveNoticeEntry entry = convertToReceiveNoticeEntry(notice);
                        // 根据FID查询是否存在该明细记录
                        ReceiveNoticeEntry existingEntry = receiveNoticeEntryMapper.selectById(entry.getFBillNo(), entry.getFMaterialId());

                        if (existingEntry != null) {
                            // 存在则更新
                            receiveNoticeEntryMapper.updateById(entry);
                            totalUpdateCount.incrementAndGet();
                        } else {
                            // 不存在则新增
                            receiveNoticeEntryMapper.insert(entry);
                            totalInsertCount.incrementAndGet();
                        }
                        processedCount++;
                    } catch (Exception e) {
                        log.error("处理明细数据失败: {}", e.getMessage());
                    }
                }

                // 如果当前页数据少于页大小，说明已经是最后一页
                if (pageData.size() < pageSize) {
                    break;
                }
                startRow += pageSize;
            }

            log.info("收料通知单明细数据同步完成，总计处理：{} 条，新增 {} 条，更新 {} 条",
                    processedCount, totalInsertCount.get(), totalUpdateCount.get());
        } catch (Exception e) {
            log.error("同步收料通知单明细数据异常", e);
            throw new RuntimeException(e);
        }

        return processedCount;
    }

    /**
     * 将金蝶数据转换为ReceiveNoticeEntry实体
     */
    private ReceiveNoticeEntry convertToReceiveNoticeEntry(List<Object> rowData) {
        ReceiveNoticeEntry entry = new ReceiveNoticeEntry();

        // 使用K3DataUtils进行数据转换
        entry.setFid(K3DataUtils.getString(rowData, 0)); // FID
        entry.setFBillNo(K3DataUtils.getString(rowData, 1)); // FBillNo
        entry.setFbzbz(K3DataUtils.getString(rowData, 2)); // F_bzbz
        entry.setFhxjq(K3DataUtils.getLocalDate(rowData, 3)); // F_hxjq
        entry.setFcplb(K3DataUtils.getString(rowData, 4)); // F_cplb
        entry.setFMaterialId(K3DataUtils.getString(rowData, 5)); // FMaterialId
        entry.setFMaterialName(K3DataUtils.getString(rowData, 6)); // FMaterialName
        entry.setFActReceiveQty(K3DataUtils.getBigDecimal(rowData, 7)); // FActReceiveQty
        entry.setFPreDeliveryDate(K3DataUtils.getLocalDate(rowData, 8)); // FPreDeliveryDate
        entry.setFPriceUnitId(K3DataUtils.getString(rowData, 9)); // FPriceUnitId
        entry.setFPriceUnitQty(K3DataUtils.getBigDecimal(rowData, 10)); // FPriceUnitQty
        entry.setFStockStatusId(K3DataUtils.getString(rowData, 11)); // FStockStatusId
        entry.setFLot(K3DataUtils.getString(rowData, 12)); // FLot
        entry.setFRejectQty(K3DataUtils.getBigDecimal(rowData, 13)); // FRejectQty
        entry.setFRejectReason(K3DataUtils.getString(rowData, 14)); // FRejectReason
        entry.setFGiveAway(K3DataUtils.getInteger(rowData, 15)); // FGiveAway
        entry.setFTaxPrice(K3DataUtils.getBigDecimal(rowData, 16)); // FTaxPrice
        entry.setFStockUnitID(K3DataUtils.getString(rowData, 17)); // FStockUnitID
        entry.setFStockQty(K3DataUtils.getBigDecimal(rowData, 18)); // FStockQty
        entry.setFWBZC(K3DataUtils.getBigDecimal(rowData, 19)); // F_WBZC
        entry.setFWBZK(K3DataUtils.getBigDecimal(rowData, 20)); // F_WBZK
        entry.setFWBZG(K3DataUtils.getBigDecimal(rowData, 21)); // F_WBZG
        entry.setFWBZTJ(K3DataUtils.getBigDecimal(rowData, 22)); // F_WBZTJ
        entry.setFmz(K3DataUtils.getBigDecimal(rowData, 23)); // F_MZ
        entry.setFjz(K3DataUtils.getBigDecimal(rowData, 24)); // F_JZ
        entry.setFlxz(K3DataUtils.getBigDecimal(rowData, 25)); // F_LXZ
        entry.setFxsdd(K3DataUtils.getString(rowData, 26)); // F_xsdd
        entry.setFlxs(K3DataUtils.getInteger(rowData, 27)); // F_lxs
        entry.setFztj(K3DataUtils.getBigDecimal(rowData, 28)); // F_ztj
        entry.setFmzz(K3DataUtils.getBigDecimal(rowData, 29)); // F_mzz
        entry.setFIsReconciliationing(K3DataUtils.getInteger(rowData, 30)); // FIsReconciliationing
        entry.setFRejectsDiscountAmount(K3DataUtils.getBigDecimal(rowData, 31)); // FRejectsDiscountAmount
        entry.setFJoinRejectsDiscountAmount(K3DataUtils.getBigDecimal(rowData, 32)); // FJoinRejectsDiscountAmount
        entry.setFReconciliationBillNo(K3DataUtils.getString(rowData, 33)); // FReconciliationBillNo
        entry.setFAllReconciliationBillNo(K3DataUtils.getString(rowData, 34)); // FAllReconciliationBillNo
        entry.setFKHHH(K3DataUtils.getString(rowData, 35)); // F_KHHH
        entry.setFkhjc(K3DataUtils.getString(rowData, 36)); // F_khjc
        entry.setFkh1(K3DataUtils.getString(rowData, 37)); // F_kh1
        entry.setFxsy(K3DataUtils.getString(rowData, 38)); // F_XSY
        entry.setFgdy1(K3DataUtils.getString(rowData, 39)); // F_gdy1
        entry.setFbzyq(K3DataUtils.getString(rowData, 40)); // F_bzyq
        entry.setFtsyq(K3DataUtils.getString(rowData, 41)); // F_tsyq
        entry.setFCTt(K3DataUtils.getString(rowData, 42)); // F_CTt
        entry.setFjgdz(K3DataUtils.getString(rowData, 43)); // F_jgdz
        entry.setFbcjsd(K3DataUtils.getString(rowData, 44)); // F_bcjsd
        entry.setFbcjsr(K3DataUtils.getString(rowData, 45)); // F_bcjsr
        entry.setFbcjsrdh(K3DataUtils.getString(rowData, 46)); // F_bcjsrdh
        entry.setFCkj(K3DataUtils.getBigDecimal(rowData, 47)); // F_Ckj
        entry.setFjgtzyy(K3DataUtils.getString(rowData, 48)); // F_jgtzyy
        entry.setFddshrq(K3DataUtils.getLocalDateTime(rowData, 49)); // Fddshrq
        entry.setFxsddh(K3DataUtils.getString(rowData, 50)); // Fxsddh
        entry.setFxsddhh(K3DataUtils.getString(rowData, 51)); // Fxsddhh
        entry.setFcgddh(K3DataUtils.getString(rowData, 52)); // Fcgddh
        entry.setFxsbm(K3DataUtils.getString(rowData, 53)); // Fxsbm
        entry.setFrkshrq(K3DataUtils.getLocalDateTime(rowData, 54)); // Frkshrq
        entry.setFzlbzjjsyq(K3DataUtils.getString(rowData, 55)); // Fzlbzjjsyq
        entry.setFbzbzjyq(K3DataUtils.getString(rowData, 56)); // Fbzbzjyq
        entry.setFkhywms(K3DataUtils.getString(rowData, 57)); // Fkhywms
        entry.setFPEUUAttachmentap(K3DataUtils.getString(rowData, 58)); // F_PEUU_Attachment_ap

        return entry;
    }

}
