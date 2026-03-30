package com.ruoyi.business.k3.service.impl;

import com.ruoyi.business.entity.PurchaseQuotation;
import com.ruoyi.business.entity.PurchaseQuotationEntry;
import com.ruoyi.business.k3.config.Dictionaryconfig;
import com.ruoyi.business.k3.service.PurchaseQuotationService;
import com.ruoyi.business.mapper.PurchaseQuotationEntryMapper;
import com.ruoyi.business.mapper.PurchaseQuotationMapper;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ruoyi.business.k3.util.DateUtils;
import com.ruoyi.business.k3.util.K3DataUtils;

/**
 * 采购报价单服务实现类
 */
@Slf4j
@Service
public class PurchaseQuotationServiceImpl implements PurchaseQuotationService {

    @Autowired
    private PurchaseQuotationMapper purchaseQuotationMapper;

    @Autowired
    private PurchaseQuotationEntryMapper purchaseQuotationEntryMapper;

    @Resource
    private Dictionaryconfig dictionaryConfig;

    // 创建固定大小的线程池
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncPurchaseQuotationsFromK3() {
        log.info("开始同步金蝶采购报价单数据...");

        // 每页查询数量
        int pageSize = 5000;
        int totalCount = 0;

        try {
            // 预先获取金蝶中的数据
//            List<List<List<Object>>> mainPages = preloadAllMainPages(pageSize);
            List<List<List<Object>>> detailPages = preloadAllDetailPages(pageSize);

            // ============ 1. 同步采购报价单主表 ============
//            int mainTableCount = syncPurchaseQuotationMainTable(mainPages);
//            totalCount += mainTableCount;
//            log.info("采购报价单主表同步完成，共处理 {} 条数据", mainTableCount);

            // ============ 2. 同步采购报价单明细表 ============
            int detailTableCount = syncPurchaseQuotationDetailTable(detailPages);
            totalCount += detailTableCount;
            log.info("采购报价单明细表同步完成，共处理 {} 条数据", detailTableCount);

            log.info("采购报价单数据同步完成，总计处理 {} 条数据", totalCount);
            return totalCount;

        } catch (Exception e) {
            log.error("同步采购报价单数据失败", e);
            throw new RuntimeException("同步采购报价单数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 预加载所有采购报价单主表分页数据
     * @param pageSize 每页查询数量
     * @return 所有分页数据列表
     */
    private List<List<List<Object>>> preloadAllMainPages(int pageSize) {
        List<List<List<Object>>> allPages = new ArrayList<>();
        int startRow = 0;

        while (true) {
            List<List<Object>> pageData = dictionaryConfig.queryPurchaseQuotationList(startRow, pageSize);
            if (pageData == null || pageData.isEmpty()) {
                break;
            }
            allPages.add(pageData);
            if (pageData.size() < pageSize) {
                break;
            }
            startRow += pageSize;
        }

        log.info("预加载采购报价单主表数据完成，共 {} 页", allPages.size());
        return allPages;
    }

    /**
     * 预加载所有采购报价单明细表分页数据
     * @param pageSize 每页查询数量
     * @return 所有分页数据列表
     */
    private List<List<List<Object>>> preloadAllDetailPages(int pageSize) {
        List<List<List<Object>>> allPages = new ArrayList<>();
        int startRow = 0;

        while (true) {
            List<List<Object>> pageData = dictionaryConfig.queryPurchaseQuotationEntryList(startRow, pageSize);
            if (pageData == null || pageData.isEmpty()) {
                break;
            }
            allPages.add(pageData);
            if (pageData.size() < pageSize) {
                break;
            }
            startRow += pageSize;
        }

        log.info("预加载采购报价单明细表数据完成，共 {} 页", allPages.size());
        return allPages;
    }

    /**
     * 同步采购报价单主表数据
     */
    private int syncPurchaseQuotationMainTable(List<List<List<Object>>> mainPages) {
        if (mainPages == null || mainPages.isEmpty()) {
            return 0;
        }

        int totalProcessed = 0;

        for (List<List<Object>> page : mainPages) {
            for (List<Object> rowData : page) {
                try {
                    // 创建或更新采购报价单主表数据
                    PurchaseQuotation purchaseQuotation = mapToPurchaseQuotation(rowData);

                    // 首先尝试根据FID查找，如果FID不存在则根据单据编号查找
                    PurchaseQuotation existing = null;

                    if (existing == null && purchaseQuotation.getFbillno() != null && !purchaseQuotation.getFbillno().isEmpty()) {
                        // 根据单据编号查找
                        existing = purchaseQuotationMapper.selectByBillNo(purchaseQuotation.getFbillno());
                    }

                    if (existing != null) {
                        // 更新现有记录
                        purchaseQuotation.setFid(existing.getFid()); // 保持原有ID
                        purchaseQuotationMapper.updatePurchaseQuotation(purchaseQuotation);
                    } else {
                        // 插入新记录
                        purchaseQuotationMapper.insertPurchaseQuotation(purchaseQuotation);
                    }

                    totalProcessed++;
                } catch (Exception e) {
                    log.error("同步采购报价单主表数据失败，行数据: {}", rowData, e);
                }
            }
        }

        return totalProcessed;
    }

    /**
     * 同步采购报价单明细表数据
     */
    private int syncPurchaseQuotationDetailTable(List<List<List<Object>>> detailPages) {
        if (detailPages == null || detailPages.isEmpty()) {
            return 0;
        }
        int totalProcessed = 0;

        for (List<List<Object>> page : detailPages) {
            for (List<Object> rowData : page) {
                try {
                    // 创建或更新采购报价单明细数据
                    PurchaseQuotationEntry entry = mapToPurchaseQuotationEntry(rowData);

                    // 检查是否已存在该明细数据
                    // 根据单据编号和产品名称等业务字段来判断是否存在
                    PurchaseQuotationEntry existing = null;
                    if (entry.getFbillno() != null && !entry.getFbillno().isEmpty() ) {
                        // 根据单据编号和产品名称查找现有记录
                        existing = findExistingEntry(entry.getFbillno(), entry.getFCpms1());
                    }

                    if (existing != null) {
                        // 更新现有记录
                        entry.setFid(existing.getFid()); // 保持原有ID
                        purchaseQuotationEntryMapper.updateById(entry);
                    } else {
                        // 插入新记录
                        purchaseQuotationEntryMapper.insert(entry);
                    }

                    totalProcessed++;
                } catch (Exception e) {
                    log.error("同步采购报价单明细表数据失败，行数据: {}", rowData, e);
                }
            }
        }

        return totalProcessed;
    }

    /**
     * 将金蝶采购报价单主表数据映射到实体对象
     * @param rowData 金蝶返回的一行数据
     * @return 采购报价单实体对象
     */
    private PurchaseQuotation mapToPurchaseQuotation(List<Object> rowData) {
        PurchaseQuotation purchaseQuotation = new PurchaseQuotation();

        int index = 0;
        // FID
        purchaseQuotation.setFid(K3DataUtils.getString(rowData, index++));
        // FBillNo
        purchaseQuotation.setFbillno(K3DataUtils.getString(rowData, index++));
        // FDocumentStatus
        purchaseQuotation.setFdocumentstatus(K3DataUtils.getString(rowData, index++));
        // FCreatorId
        purchaseQuotation.setFcreatorid(K3DataUtils.getString(rowData, index++));
        // FCreateDate
        purchaseQuotation.setFcreatedate(DateUtils.parseLocalDateTime(rowData.get(index++)));
        // Fshr
        purchaseQuotation.setFshr(K3DataUtils.getString(rowData, index++));
        // FDATE_sh
        purchaseQuotation.setFdateSh(DateUtils.parseLocalDateTime(rowData.get(index++)));
        // FDate
        purchaseQuotation.setFdate(DateUtils.parseLocalDate(rowData.get(index++)));
        // F_xsy
        purchaseQuotation.setFxSy(K3DataUtils.getString(rowData, index++));
        // F_KHJC
        purchaseQuotation.setFKhjc(K3DataUtils.getString(rowData, index++));
        // F_kh
        purchaseQuotation.setFKh(K3DataUtils.getString(rowData, index++));
        // F_KHMC
        purchaseQuotation.setFKhmc(K3DataUtils.getString(rowData, index++));
        // F_Ckgj
        purchaseQuotation.setFCkgj(K3DataUtils.getString(rowData, index++));
        // F_khlx
        purchaseQuotation.setFKhlx(K3DataUtils.getString(rowData, index++));
        // F_Email
        purchaseQuotation.setFEmail(K3DataUtils.getString(rowData, index++));
        // F_Mob
        purchaseQuotation.setFMob(K3DataUtils.getString(rowData, index++));
        // F_Tel
        purchaseQuotation.setFTel(K3DataUtils.getString(rowData, index++));
        // F_Fax
        purchaseQuotation.setFFax(K3DataUtils.getString(rowData, index++));
        // F_qyd
        purchaseQuotation.setFQyd(K3DataUtils.getString(rowData, index++));
        // F_fktj
        purchaseQuotation.setFFktj(K3DataUtils.getString(rowData, index++));

        return purchaseQuotation;
    }

    /**
     * 将金蝶采购报价单明细表数据映射到实体对象
     * @param rowData 金蝶返回的一行数据
     * @return 采购报价单明细实体对象
     */
    private PurchaseQuotationEntry mapToPurchaseQuotationEntry(List<Object> rowData) {
        PurchaseQuotationEntry entry = new PurchaseQuotationEntry();

        int index = 0;
        // FBillNo
        entry.setFbillno(K3DataUtils.getString(rowData, index++));
        entry.setFid(K3DataUtils.getString(rowData, index++));
        // F_ora_UnitID
        entry.setFOraUnitId(K3DataUtils.getString(rowData, index++));
        // F_BZ
        entry.setFBz(K3DataUtils.getString(rowData, index++));
        // F_TP
        entry.setFTp(K3DataUtils.getString(rowData, index++));
        // F_gys
        entry.setFGys(K3DataUtils.getString(rowData, index++));
        // F_Cptp1
        entry.setFCptp1(K3DataUtils.getString(rowData, index++));
        // F_jgyxq
        entry.setFJgyxq(DateUtils.parseLocalDate(rowData.get(index++)));
        // F_cgjg
        entry.setFCgjg(K3DataUtils.getBigDecimal(rowData, index++));
        // F_zxqdl
        entry.setFZxqdl(K3DataUtils.getBigDecimal(rowData, index++));
        // F_xcpms
        entry.setFXcpms(K3DataUtils.getString(rowData, index++));
        // F_stgys
        entry.setFStgys(K3DataUtils.getString(rowData, index++));
        // F_xdl
        entry.setFXdl(K3DataUtils.getBigDecimal(rowData, index++));
        // F_cpdm1
        entry.setFCpdm1(K3DataUtils.getString(rowData, index++));
        // F_ywbz
        entry.setFYwbz(K3DataUtils.getString(rowData, index++));
        // F_wltp
        entry.setFWltp(K3DataUtils.getString(rowData, index++));
        // F_CPMS1
        entry.setFCpms1(K3DataUtils.getString(rowData, index++));
        // F_BZMX
        entry.setFBzmx(K3DataUtils.getString(rowData, index++));
        // F_cty_BaseProperty4
        entry.setFCtyBaseproperty4(K3DataUtils.getString(rowData, index++));
        // F_ywtp1
        entry.setFYwtp1(K3DataUtils.getString(rowData, index++));
        // F_ywtp2
        entry.setFYwtp2(K3DataUtils.getString(rowData, index++));
        // F_zxs
        entry.setFZxs(K3DataUtils.getBigDecimal(rowData, index++));
        // F_mz
        entry.setFMz(K3DataUtils.getBigDecimal(rowData, index++));
        // F_jz
        entry.setFJz(K3DataUtils.getBigDecimal(rowData, index++));
        // F_C
        entry.setFC(K3DataUtils.getBigDecimal(rowData, index++));
        // F_k
        entry.setFK(K3DataUtils.getBigDecimal(rowData, index++));
        // F_g
        entry.setFG(K3DataUtils.getBigDecimal(rowData, index++));
        // F_Cghsj
        entry.setFCghsj(K3DataUtils.getBigDecimal(rowData, index++));
        // F_cty_BaseProperty
        entry.setFCtyBaseproperty(K3DataUtils.getString(rowData, index++));
        // Fgctp
        entry.setFgctp(K3DataUtils.getString(rowData, index++));
        // Fyjjhrq
        entry.setFyjjhrq(DateUtils.parseLocalDate(rowData.get(index++)));
        // Fbzcc
        entry.setFbzcc(K3DataUtils.getString(rowData, index++));
        // Fcgshys
        entry.setFcgshys(K3DataUtils.getString(rowData, index++));
        // F_Fbjry
        entry.setFFbjry(K3DataUtils.getString(rowData, index++));
        // F_ywpm
        entry.setFYwpm(K3DataUtils.getString(rowData, index++));
        // F_ywms
        entry.setFYwms(K3DataUtils.getString(rowData, index++));
        // F_cty_Text
        entry.setFCtyText(K3DataUtils.getString(rowData, index++));
        // Fkhhh
        entry.setFkhhh(K3DataUtils.getString(rowData, index++));
        // FQTY_kc
        entry.setFqtyKc(K3DataUtils.getBigDecimal(rowData, index++));
        // Fzxs
        entry.setFzxss(K3DataUtils.getBigDecimal(rowData, index++));
        // Ftj
        entry.setFtj(K3DataUtils.getBigDecimal(rowData, index++));
        // Fmzz
        entry.setFmzz(K3DataUtils.getBigDecimal(rowData, index++));
        // Fkhxq
        entry.setFkhxq(K3DataUtils.getString(rowData, index++));

        // Fxgyslxr
        entry.setFxgyslxr(K3DataUtils.getString(rowData, index++));
        // Fxgyslxfs
        entry.setFxgyslxfs(K3DataUtils.getString(rowData, index++));

        return entry;
    }

    /**
     * 根据单据编号和产品名称查找已存在的明细记录
     * @param billNo 单据编号
     * @param productName 产品名称
     * @return 已存在的明细记录，如果不存在则返回null
     */
    private PurchaseQuotationEntry findExistingEntry(String billNo, String productName) {
        // 创建查询条件
        PurchaseQuotationEntry condition = new PurchaseQuotationEntry();
        condition.setFbillno(billNo);
        condition.setFCpms1(productName); // 使用产品名称作为查找条件

        // 调用Mapper的查询方法
        List<PurchaseQuotationEntry> existingEntries = purchaseQuotationEntryMapper.selectByBillNoAndProductName(billNo, productName);

        if (existingEntries != null && !existingEntries.isEmpty()) {
            // 返回第一个匹配的记录
            return existingEntries.get(0);
        }

        return null;
    }

    @Override
    public TableDataInfo<PurchaseQuotation> selectPagePurchaseQuotation(PurchaseQuotation purchaseQuotation, PageQuery pageQuery) {
        Page<PurchaseQuotation> page = pageQuery.build();
        // 使用MyBatis-Plus的分页查询，传入查询条件
        IPage<PurchaseQuotation> result = purchaseQuotationMapper.selectPage(page, purchaseQuotation);
        return TableDataInfo.build(result);
    }

    @Override
    public PurchaseQuotation selectById(String id) {
        // 根据ID查询主表数据
        PurchaseQuotation main = purchaseQuotationMapper.selectById(id);
        if (main != null) {
            // 查询明细数据
            List<PurchaseQuotationEntry> entries = purchaseQuotationEntryMapper.selectByBillNo(main.getFbillno());
            main.setEntries(entries);
        }
        return main;
    }

    @Override
    public PurchaseQuotation selectByBillNo(String billNo) {
        // 根据单据编号查询主表数据
        PurchaseQuotation main = purchaseQuotationMapper.selectByBillNo(billNo);
        if (main != null) {
            // 查询明细数据
            List<PurchaseQuotationEntry> entries = purchaseQuotationEntryMapper.selectByBillNo(billNo);
            main.setEntries(entries);
        }
        return main;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addPurchaseQuotation(PurchaseQuotation purchaseQuotation) {
        try {
            // 插入主表数据
            int mainResult = purchaseQuotationMapper.insert(purchaseQuotation);

            if (mainResult > 0) {
                // 如果有明细数据，则插入明细表
                if (purchaseQuotation.getEntries() != null && !purchaseQuotation.getEntries().isEmpty()) {
                    for (PurchaseQuotationEntry entry : purchaseQuotation.getEntries()) {
                        entry.setFid(purchaseQuotation.getFid()); // 设置关联主表ID
                        entry.setFbillno(purchaseQuotation.getFbillno()); // 设置单据编号
                        purchaseQuotationEntryMapper.insert(entry);
                    }
                }
                return Result.success("新增采购报价单成功");
            } else {
                return Result.error("新增采购报价单失败");
            }
        } catch (Exception e) {
            log.error("新增采购报价单异常", e);
            throw e; // 让事务回滚
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updatePurchaseQuotation(PurchaseQuotation purchaseQuotation) {
        try {
            // 更新主表数据
            int mainResult = purchaseQuotationMapper.updateById(purchaseQuotation);

            if (mainResult > 0) {
                // 先删除原有的明细数据
                purchaseQuotationEntryMapper.deleteByFid(purchaseQuotation.getFid());

                // 重新插入新的明细数据
                if (purchaseQuotation.getEntries() != null && !purchaseQuotation.getEntries().isEmpty()) {
                    for (PurchaseQuotationEntry entry : purchaseQuotation.getEntries()) {
                        entry.setFid(purchaseQuotation.getFid()); // 设置关联主表ID
                        entry.setFbillno(purchaseQuotation.getFbillno()); // 设置单据编号
                        purchaseQuotationEntryMapper.insert(entry);
                    }
                }
                return Result.success("更新采购报价单成功");
            } else {
                return Result.error("更新采购报价单失败");
            }
        } catch (Exception e) {
            log.error("更新采购报价单异常", e);
            throw e; // 让事务回滚
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deletePurchaseQuotation(String id) {
        try {
            // 先删除明细数据
            int detailResult = purchaseQuotationEntryMapper.deleteByFid(id);

            // 再删除主表数据
            int mainResult = purchaseQuotationMapper.deleteById(id);

            if (mainResult > 0) {
                return Result.success("删除采购报价单成功");
            } else {
                return Result.error("删除采购报价单失败");
            }
        } catch (Exception e) {
            log.error("删除采购报价单异常", e);
            throw e; // 让事务回滚
        }
    }
}
