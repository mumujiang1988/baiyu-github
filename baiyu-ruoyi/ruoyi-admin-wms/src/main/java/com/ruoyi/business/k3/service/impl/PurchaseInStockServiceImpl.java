package com.ruoyi.business.k3.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.ruoyi.business.entity.PurchaseInStock;
import com.ruoyi.business.entity.PurchaseInStockEntry;
import com.ruoyi.business.k3.util.K3DataUtils;
import com.ruoyi.business.mapper.PurchaseInStockMapper;
import com.ruoyi.business.k3.config.Dictionaryconfig;
import com.ruoyi.business.k3.service.IPurchaseInStockService;
import com.ruoyi.business.util.StringUtils;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.ruoyi.business.util.StringUtils.*;

/**
 * 采购入库单 Service 实现类
 */
@Service
@Slf4j
public class PurchaseInStockServiceImpl implements IPurchaseInStockService {

    @Autowired
    private PurchaseInStockMapper purchaseInStockMapper;

    @Autowired
    private Dictionaryconfig dictionaryconfig;

    @Override
    public PurchaseInStock selectById(String fid) {
        PurchaseInStock purchaseInStock = purchaseInStockMapper.selectById(fid);
        List<PurchaseInStockEntry> purchaseInStockEntry = purchaseInStockMapper.selectEntryById(purchaseInStock.getFBillNo());
        purchaseInStock.setFEntity(purchaseInStockEntry);
        return purchaseInStock;
    }

    @Override
    public TableDataInfo<PurchaseInStock> selectList(PurchaseInStock purchaseInStock, int pageNum, int pageSize) {
        // 使用 PageHelper 进行分页

        List<PurchaseInStock> list = purchaseInStockMapper.selectList(pageNum, pageSize, purchaseInStock);
        return TableDataInfo.build(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(PurchaseInStock purchaseInStock) {
        // TODO: 可以在这里添加业务逻辑验证
        // 例如：验证明细表是否为空
        if (purchaseInStock.getFEntity() == null || purchaseInStock.getFEntity().isEmpty()) {
            throw new RuntimeException("采购入库单明细不能为空");
        }

        // 设置主键和明细外键关联
        // TODO: 如果需要生成唯一 ID，可以使用 UUID.randomUUID().toString()

        return purchaseInStockMapper.insert(purchaseInStock);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(PurchaseInStock purchaseInStock) {
        // TODO: 可以在这里添加业务逻辑验证
        return purchaseInStockMapper.update(purchaseInStock);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(String fid) {
        return purchaseInStockMapper.deleteById(fid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(String[] fids) {
        return purchaseInStockMapper.deleteByIds(fids);
    }

    @Override
    public int syncPurchaseInStockData() {
        log.info("开始同步金蝶采购入库单主表数据...");
        int pageSize = 3000;
        int processedCount = 0;
        int startRow = 0;

        try {
            while (true) {
                // 获取采购入库单主表数据
                List<List<Object>> pageData = dictionaryconfig.queryPurchaseInStockList(startRow, pageSize);

                log.info("开始处理第 {} 行到第 {} 行的采购入库单主表数据", startRow, startRow + pageData.size());

                // 解析页面数据为实体对象
                List<PurchaseInStock> pageStocks = parsePurchaseInStockPage(pageData);
                if (!pageStocks.isEmpty()) {
                    // 批量处理新增和更新
                    int insertCount = 0;
                    int updateCount = 0;

                    // 遍历每条记录，判断是新增还是更新
                    for (PurchaseInStock stock : pageStocks) {
                        // 检查是否存在相同单据编号的记录
                        PurchaseInStock existing = purchaseInStockMapper.selectById(stock.getFid());

                        if (existing != null) {
                            purchaseInStockMapper.update(stock);
                            updateCount++;
                        } else {
                            // 不存在相同条件的记录，进行新增操作
                            purchaseInStockMapper.insert(stock);
                            insertCount++;
                        }
                    }

                    int pageCount = insertCount + updateCount;
                    processedCount += pageCount;
                    log.debug("页面处理完成，新增 {} 条，更新 {} 条，总计 {} 条", insertCount, updateCount, pageCount);
                }

                // 如果当前页数据少于页大小，说明已经是最后一页
                if (pageData.size() < pageSize) {
                    break;
                }

                startRow += pageSize;
            }

            log.info("采购入库单主表同步完成，总计处理：{} 条", processedCount);
        } catch (Exception e) {
            log.error("同步采购入库单主表异常", e);
            throw new RuntimeException(e);
        }

        return processedCount;
    }

    @Override
    public int syncPurchaseInStockEntryData() {
        log.info("开始同步金蝶采购入库单明细表数据...");
        int pageSize = 3000;
        int processedCount = 0;
        int startRow = 0;

        try {
            while (true) {
                // 获取采购入库单明细表数据
                List<List<Object>> pageData = dictionaryconfig.queryPurchaseInStockEntryList(startRow, pageSize);

                log.info("开始处理第 {} 行到第 {} 行的采购入库单明细表数据", startRow, startRow + pageData.size());

                // 解析页面数据为实体对象
                List<PurchaseInStockEntry> pageEntries = parsePurchaseInStockEntryPage(pageData);
                if (!pageEntries.isEmpty()) {
                    // 这里只需要新增，不删除（因为明细表是通过外键关联主表）
                    int insertCount = 0;

                    // 遍历每条记录
                    for (PurchaseInStockEntry entry : pageEntries) {
                        // 检查是否已存在
                        PurchaseInStockEntry existing = purchaseInStockMapper.selectEntryByFbillno(entry.getFbillno(), entry.getFmaterialId());

                        if (existing == null) {
                            // 不存在则新增
                            purchaseInStockMapper.insertEntry(entry);
                            insertCount++;
                        } else {
                            // 存在则更新
                            purchaseInStockMapper.updateEntry(entry);
                        }
                    }

                    processedCount += insertCount;
                    log.debug("页面处理完成，新增 {} 条", insertCount);
                }

                // 如果当前页数据少于页大小，说明已经是最后一页
                if (pageData.size() < pageSize) {
                    break;
                }

                startRow += pageSize;
            }

            log.info("采购入库单明细表同步完成，总计处理：{} 条", processedCount);
        } catch (Exception e) {
            log.error("同步采购入库单明细表异常", e);
            throw new RuntimeException(e);
        }

        return processedCount;
    }

    /**
     * 解析采购入库单主表页面数据
     */
    private List<PurchaseInStock> parsePurchaseInStockPage(List<List<Object>> pageData) {
        List<PurchaseInStock> stocks = new ArrayList<>();
        if (pageData == null || pageData.isEmpty()) {
            return stocks;
        }

        for (List<Object> row : pageData) {

                PurchaseInStock stock = new PurchaseInStock();
                int idx = 0;
                stock.setFid(K3DataUtils.getString(row, idx++));
                stock.setFBillNo(K3DataUtils.getString(row, idx++));
                stock.setFDocumentStatus(K3DataUtils.getString(row, idx++));
                stock.setFBillTypeID(K3DataUtils.getString(row, idx++));
                stock.setFDate(K3DataUtils.getLocalDateTime(row, idx++));
                stock.setFSupplierId(K3DataUtils.getString(row, idx++));
                stock.setFSupplyId(K3DataUtils.getString(row, idx++));
                stock.setFSettleId(K3DataUtils.getString(row, idx++));
                stock.setFChargeId(K3DataUtils.getString(row, idx++));
                stock.setFPurchaserId(K3DataUtils.getString(row, idx++));
                stock.setFCreatorId(K3DataUtils.getString(row, idx++));
                stock.setFCreateDate(K3DataUtils.getLocalDateTime(row, idx++));
                stock.setFModifierId(K3DataUtils.getString(row, idx++));
                stock.setFModifyDate(K3DataUtils.getLocalDateTime(row, idx++));
                stock.setFApproverId(K3DataUtils.getString(row, idx++));
                stock.setFApproveDate(K3DataUtils.getLocalDateTime(row, idx++));
                stock.setFConfirmerId(K3DataUtils.getString(row, idx++));
                stock.setFConfirmDate(K3DataUtils.getLocalDateTime(row, idx++));
                stock.setFSupplyAddress(K3DataUtils.getString(row, idx++));
                stock.setFStockerId(K3DataUtils.getString(row, idx++));
                stock.setFSettleCurrId(K3DataUtils.getString(row, idx++));
                stock.setFLocalCurrId(K3DataUtils.getString(row, idx++));
                stock.setFExchangeTypeId(K3DataUtils.getString(row, idx++));
                stock.setFExchangeRate(K3DataUtils.getBigDecimal(row, idx++));
                stock.setFIsIncludedTax(K3DataUtils.getInteger(row, idx++));
                stock.setFPayConditionId(K3DataUtils.getString(row, idx++));
                stock.setFSettleTypeId(K3DataUtils.getString(row, idx++));
                stock.setFPriceTimePoint(K3DataUtils.getString(row, idx++));
                stock.setFSettleOrgId(K3DataUtils.getString(row, idx++));
                stock.setFBillAmount(K3DataUtils.getBigDecimal(row, idx++));
                stock.setFBillTaxAmount(K3DataUtils.getBigDecimal(row, idx++));
                stock.setFBillAllAmount(K3DataUtils.getBigDecimal(row, idx++));

                stocks.add(stock);

        }

        return stocks;
    }

    /**
     * 解析采购入库单明细表页面数据
     */
    private List<PurchaseInStockEntry> parsePurchaseInStockEntryPage(List<List<Object>> pageData) {
        List<PurchaseInStockEntry> entries = new ArrayList<>();
        if (pageData == null || pageData.isEmpty()) {
            return entries;
        }

        for (List<Object> row : pageData) {
            try {
                PurchaseInStockEntry entry = new PurchaseInStockEntry();
                int idx = 0;

                // 基础字段
                entry.setFbillno(K3DataUtils.getString(row, idx++));
                entry.setFcplb(K3DataUtils.getString(row, idx++));
                entry.setFjqhx(K3DataUtils.getString(row, idx++));
                entry.setFmaterialId(K3DataUtils.getString(row, idx++)); // Changed from setFMaterialId to setFMaterialId
                entry.setFstockid(K3DataUtils.getString(row, idx++));
                entry.setFproducedate(K3DataUtils.getLocalDate(row, idx++));
                entry.setFnote(K3DataUtils.getString(row, idx++));
                entry.setFwwintype(K3DataUtils.getString(row, idx++));
                entry.setFlot(K3DataUtils.getString(row, idx++));
                entry.setFstocklocid(K3DataUtils.getString(row, idx++));
                entry.setFreceivelot(K3DataUtils.getString(row, idx++));
                entry.setFsupplierlot(K3DataUtils.getString(row, idx++));
                entry.setFgrossweight(K3DataUtils.getBigDecimal(row, idx++));
                entry.setFnetweight(K3DataUtils.getBigDecimal(row, idx++));
                entry.setFmaterialname(K3DataUtils.getString(row, idx++));
                entry.setFmaterialtype(K3DataUtils.getString(row, idx++));
                entry.setFuom(K3DataUtils.getString(row, idx++));
                entry.setFcontractlno(K3DataUtils.getString(row, idx++));
                entry.setFmustqty(K3DataUtils.getBigDecimal(row, idx++));
                entry.setFrealqty(K3DataUtils.getBigDecimal(row, idx++));
                entry.setFauxunitqty(K3DataUtils.getBigDecimal(row, idx++));
                entry.setFexpirydate(K3DataUtils.getLocalDate(row, idx++));
                entry.setFstockstatusid(K3DataUtils.getString(row, idx++));
                entry.setFrejectsdiscountamount(K3DataUtils.getBigDecimal(row, idx++));

                // 客户相关字段
                entry.setFkhhh(K3DataUtils.getString(row, idx++));
                entry.setFbzfyzt(K3DataUtils.getString(row, idx++));
                entry.setFkh(K3DataUtils.getString(row, idx++));
                entry.setFxsyy(K3DataUtils.getString(row, idx++));
                entry.setFgdy1(K3DataUtils.getString(row, idx++));
                entry.setFbzyq(K3DataUtils.getString(row, idx++));
                entry.setFtsyq(K3DataUtils.getString(row, idx++));

                // 产品描述字段
                entry.setFctybaseproperty(K3DataUtils.getString(row, idx++));
                entry.setFbcjsr(K3DataUtils.getString(row, idx++));
                entry.setFbcjsdh(K3DataUtils.getString(row, idx++));
                entry.setFckj(K3DataUtils.getBigDecimal(row, idx++));
                entry.setFjgtzyy(K3DataUtils.getString(row, idx++));
                entry.setFzdrq(K3DataUtils.getLocalDate(row, idx++));
                entry.setFddshrq(K3DataUtils.getLocalDate(row, idx++));
                entry.setFrkzq(K3DataUtils.getString(row, idx++));
                entry.setFkcsl(K3DataUtils.getBigDecimal(row, idx++));
                entry.setFc(K3DataUtils.getString(row, idx++));
                entry.setFsljzrq(K3DataUtils.getLocalDate(row, idx++));

                // 价格与税额字段
                entry.setFtaxamount(K3DataUtils.getBigDecimal(row, idx++));
                entry.setFcostprice(K3DataUtils.getBigDecimal(row, idx++));
                entry.setFentrytax_rate(K3DataUtils.getBigDecimal(row, idx++));
                entry.setFentrytaxamount(K3DataUtils.getBigDecimal(row, idx++));
                entry.setFdiscountrate(K3DataUtils.getBigDecimal(row, idx++));
                entry.setFpricecoefficient(K3DataUtils.getBigDecimal(row, idx++));
                entry.setFpriceunitqty(K3DataUtils.getBigDecimal(row, idx++));
                entry.setFtaxnetprice(K3DataUtils.getBigDecimal(row, idx++));
                entry.setFentrycostamount(K3DataUtils.getBigDecimal(row, idx++));
                entry.setFallamount(K3DataUtils.getBigDecimal(row, idx++));
                entry.setFtaxamountlc(K3DataUtils.getBigDecimal(row, idx++));
                entry.setFcostamountlc(K3DataUtils.getBigDecimal(row, idx++));
                entry.setFallamountlc(K3DataUtils.getBigDecimal(row, idx++));

                // 库存与单位字段
                entry.setFstockflag(K3DataUtils.getInteger(row, idx++));
                entry.setFbaseunitprice(K3DataUtils.getBigDecimal(row, idx++));
                entry.setFunitid(K3DataUtils.getString(row, idx++));
                entry.setFbaseunit_id(K3DataUtils.getString(row, idx++));
                entry.setFbaseunitqty(K3DataUtils.getBigDecimal(row, idx++));
                entry.setFauxunitid(K3DataUtils.getString(row, idx++));
                entry.setFpriceunitid(K3DataUtils.getString(row, idx++));
                entry.setFpoorderno(K3DataUtils.getString(row, idx++));
                entry.setFreceivestockstatus(K3DataUtils.getString(row, idx++));

                // 附件与其他字段
                entry.setFctytext1(K3DataUtils.getString(row, idx++));
                entry.setFpeuuattachmenttzk(K3DataUtils.getString(row, idx++));
                entry.setFtj(K3DataUtils.getBigDecimal(row, idx++));
                entry.setFpeuubasepropertyre5(K3DataUtils.getString(row, idx++));
                entry.setFssxs(K3DataUtils.getBigDecimal(row, idx++));
                entry.setForadecimal(K3DataUtils.getBigDecimal(row, idx++));
                entry.setForamulcombo(K3DataUtils.getString(row, idx++));
                entry.setForapicture(K3DataUtils.getString(row, idx++));
                entry.setFxsdd(K3DataUtils.getString(row, idx++));
                entry.setFjyzt(K3DataUtils.getString(row, idx++));
                entry.setFjydsl(K3DataUtils.getBigDecimal(row, idx++));
                entry.setFcpbm(K3DataUtils.getString(row, idx++));
                entry.setForabaseproperty(K3DataUtils.getString(row, idx++));
                entry.setFyhbl(K3DataUtils.getString(row, idx++));
                entry.setFyhrq(K3DataUtils.getDate(row, idx++));

                // 税务与对账字段
                entry.setFtaxrate(K3DataUtils.getBigDecimal(row, idx++)); //
                entry.setFtaxamount(K3DataUtils.getBigDecimal(row, idx++)); //
                entry.setFcostpercent(K3DataUtils.getBigDecimal(row, idx++)); //
                entry.setFtaxcostamount(K3DataUtils.getBigDecimal(row, idx++)); //
                entry.setFvat(K3DataUtils.getBigDecimal(row, idx++));
                entry.setFisreconciliationing(K3DataUtils.getInteger(row, idx++));
                entry.setFreconciliationbillno(K3DataUtils.getString(row, idx++));

                // 包装与唛头字段
                entry.setFctt(K3DataUtils.getString(row, idx++));
                entry.setFjgdz(K3DataUtils.getString(row, idx++));
                entry.setFwxzm(K3DataUtils.getString(row, idx++));
                entry.setFwxcm(K3DataUtils.getString(row, idx++));
                entry.setFct(K3DataUtils.getString(row, idx++));
                entry.setFch(K3DataUtils.getString(row, idx++));
                entry.setFbaozt(K3DataUtils.getString(row, idx++));
                entry.setFnxzm(K3DataUtils.getString(row, idx++));
                entry.setFnxcm(K3DataUtils.getString(row, idx++));
                entry.setFctyattachment(K3DataUtils.getString(row, idx++));

                entries.add(entry);
            } catch (Exception e) {
                log.error("解析采购入库单明细表数据失败", e);
            }
        }

        return entries;
    }



}
