package com.ruoyi.business.k3.service.impl;

import com.ruoyi.business.dto.SalesOrderDTo;
import com.ruoyi.business.dto.SalesRankingDTO;
import com.ruoyi.business.entity.SaleOrder;
import com.ruoyi.business.entity.SaleOrderCost;
import com.ruoyi.business.entity.SaleOrderEntry;
import com.ruoyi.business.feishu.config.BatchGetEmployeeConfig;
import com.ruoyi.business.k3.config.Dictionaryconfig;
import com.ruoyi.business.k3.service.SaleOrderService;
import com.ruoyi.business.k3.util.SaleOrderDataConverter;
import com.ruoyi.business.entity.BymaterialDictionary;
import com.ruoyi.business.mapper.*;

import com.ruoyi.business.util.Result;
import com.ruoyi.business.util.ThreadPoolUtil;
import com.ruoyi.business.utils.SaleOrderCostCalculator;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.satoken.utils.LoginHelper;
import com.ruoyi.system.domain.entity.SysUser;
import com.ruoyi.system.mapper.SysUserMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 销售订单服务实现类
 * 负责从金蝶同步销售订单相关数据到本地数据库
 *
 * @author system
 */
@Slf4j
@Service
public class SaleOrderServiceImpl implements SaleOrderService {

    @Resource
    private SaleOrderMapper saleOrderMapper;

    @Resource
    private SaleOrderEntryMapper saleOrderEntryMapper;

    @Resource
    private SaleOrderCostMapper saleOrderCostMapper;

    @Resource
    private Dictionaryconfig dictionaryconfig;
    @Autowired
    private BatchGetEmployeeConfig batchGetEmployeeConfig;
    @Resource
    private BymaterialDictionaryMapper bymaterialDictionaryMapper;
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private EmployeeMapper employeeMapper;

    /**
     * 同步金蝶销售订单数据到本地数据库
     * @return 同步的总记录数
     */
    @Transactional(rollbackFor = Exception.class)
    public int syncSaleOrdersFromK3() {
        log.info("开始同步金蝶销售订单数据...");

        // 每页查询数量
        int pageSize = 5000;
        int totalCount = 0;

        try {
            // 预先获取金蝶中的数据
            List<List<List<Object>>> mainPages = preloadAllMainPages(pageSize);
            List<List<List<Object>>> detailPages = preloadAllDetailPages(pageSize);
            List<List<List<Object>>> costPages = preloadAllCostPages(pageSize);

            // ============ 1. 同步销售订单主表 ============
            int mainTableCount = syncSaleOrderMainTable(pageSize, mainPages);
            totalCount += mainTableCount;
            log.info("销售订单主表同步完成，共处理 {} 条数据", mainTableCount);

            // ============ 2. 同步销售订单明细表 ============
            int detailTableCount = syncSaleOrderDetailTable(pageSize, detailPages);
            totalCount += detailTableCount;
            log.info("销售订单明细表同步完成，共处理 {} 条数据", detailTableCount);

            // ============ 3. 同步销售订单成本表 ============
            int costTableCount = syncSaleOrderCostTable(pageSize, costPages);
           totalCount += costTableCount;

            log.info("销售订单数据同步完成，总计处理 {} 条数据", totalCount);
            return totalCount;

        } catch (Exception e) {
            log.error("同步销售订单数据失败", e);
            throw new RuntimeException("同步销售订单数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 预加载所有销售订单主表分页数据
     * @param pageSize 每页查询数量
     * @return 所有分页数据列表
     */
    private List<List<List<Object>>> preloadAllMainPages(int pageSize) {
        List<List<List<Object>>> allPages = new ArrayList<>();
        int startRow = 0;

        while (true) {
            List<List<Object>> pageData = dictionaryconfig.querySalesOrderProcessing(startRow, pageSize);

            allPages.add(pageData);
            log.info("获取销售订单主表数据页，大小：{}，起始行：{}", pageData.size(), startRow);

            if (pageData.size() < pageSize) {
                break; // 最后一页
            }
            startRow += pageSize;
        }

        return allPages;
    }

    /**
     * 解析单页销售订单主表数据
     * @param pageData 单页原始数据
     * @return 解析后的实体列表
     */
    private List<SaleOrder> parseSingleMainPage(List<List<Object>> pageData) {
        List<SaleOrder> saleOrders = new ArrayList<>();
        for (List<Object> row : pageData) {
            try {
                // 解析数据并转换为实体对象
                SaleOrder saleOrder = SaleOrderDataConverter.parseSaleOrderFromRow(row);
                saleOrders.add(saleOrder);
            } catch (Exception e) {
                log.error("解析销售订单主表数据失败，行数据：{}", row, e);
            }
        }
        return saleOrders;
    }

    /**
     * 同步销售订单主表数据
     * 优化：使用多线程并行解析数据，提高处理效率
     * 改为先查询后更新/新增的模式
     * @param pageSize 每页查询数量
     * @return 同步的记录数
     */
    private int syncSaleOrderMainTable(int pageSize, List<List<List<Object>>> preloadedPages) {
        int processedCount = 0;

        if (preloadedPages.isEmpty()) {
            log.info("销售订单主表无数据需要同步");
            return 0;
        }

        // 使用线程池并行解析所有页面的数据
        ExecutorService executor = ThreadPoolUtil.createFixedThreadPool("SaleOrderMainParse");

        try {
            List<CompletableFuture<List<SaleOrder>>> parseFutures = preloadedPages.stream()
                .map(pageData -> CompletableFuture.supplyAsync(() -> parseSingleMainPage(pageData), executor))
                .collect(Collectors.toList());

            // 等待所有解析任务完成
            List<SaleOrder> allSaleOrders = parseFutures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());

            if (!allSaleOrders.isEmpty()) {
                // 提取所有FID，查询数据库中已存在的记录
                Set<Long> allFids = allSaleOrders.stream().map(SaleOrder::getFid).collect(Collectors.toSet());
                List<Long> fidList = new ArrayList<>(allFids);

                log.debug("准备查询数据库中已存在的FID，FID列表大小：{}", fidList.size());

                // 查询数据库中已存在的FID
                List<Long> existingFids = saleOrderMapper.selectExistingFids(fidList);
                Set<Long> existingFidSet = new HashSet<>(existingFids);

                log.debug("数据库中已存在的FID数量：{}，具体FIDs：{}", existingFids.size(), existingFids);

                // 分离需要更新和新增的记录
                List<SaleOrder> ordersToUpdate = new ArrayList<>();
                List<SaleOrder> ordersToInsert = new ArrayList<>();

                for (SaleOrder order : allSaleOrders) {
                    if (existingFidSet.contains(order.getFid())) {
                        ordersToUpdate.add(order);
                    } else {
                        ordersToInsert.add(order);
                    }
                }

                log.info("销售订单主表：需要更新 {} 条，新增 {} 条", ordersToUpdate.size(), ordersToInsert.size());

                // 执行更新操作
                AtomicInteger updateCount = new AtomicInteger(0);
                for (SaleOrder order : ordersToUpdate) {
                    int result = saleOrderMapper.updateByFid(order);
                    if (result > 0) {
                        updateCount.incrementAndGet();
                    } else {
                        log.warn("更新失败，FID: {}", order.getFid());
                    }
                }

                // 执行新增操作
                AtomicInteger insertCount = new AtomicInteger(0);
                for (SaleOrder order : ordersToInsert) {

                    try {
                        // 在插入前再次检查该记录是否已存在（以防并发问题）
                        SaleOrder existingOrder = saleOrderMapper.selectById(order.getFid());
                        if (existingOrder == null) {
                            saleOrderMapper.insert(order);
                        } else {
                            log.warn("销售订单FID {} 已存在，跳过插入", order.getFid());
                            // 如果记录存在，改为更新操作
                            saleOrderMapper.updateByFid(order);
                        }
                    } catch (Exception e) {
                        log.error("插入销售订单失败，FID: {}，错误: {}", order.getFid(), e.getMessage());

                        throw e;
                    }
                    insertCount.incrementAndGet();
                }

                processedCount = updateCount.get() + insertCount.get();
                log.debug("销售订单主表处理完成，更新 {} 条，新增 {} 条，总计 {} 条",
                    updateCount.get(), insertCount.get(), processedCount);
            }
        } catch (Exception e) {
            throw e; // 重新抛出异常，让事务回滚
        } finally {
            // 关闭线程池
            ThreadPoolUtil.shutdown((ThreadPoolExecutor) executor);
        }

        return processedCount;
    }

    /**
     * 预加载所有销售订单明细表分页数据
     * @param pageSize 每页查询数量
     * @return 所有分页数据列表
     */
    private List<List<List<Object>>> preloadAllDetailPages(int pageSize) {
        List<List<List<Object>>> allPages = new ArrayList<>();
        int startRow = 0;

        while (true) {
            List<List<Object>> pageData = dictionaryconfig.querySalesOrderProcessingDetailList(startRow, pageSize);

            allPages.add(pageData);
            log.info("获取销售订单明细表数据页，大小：{}，起始行：{}", pageData.size(), startRow);

            if (pageData.size() < pageSize) {
                break; // 最后一页
            }

            startRow += pageSize;
        }

        return allPages;
    }

    /**
     * 解析单页销售订单明细数据
     * @param pageData 单页原始数据
     * @return 按FID分组的解析后数据
     */
    private Map<String, List<SaleOrderEntry>> parseSinglePage(List<List<Object>> pageData) {
        Map<String, List<SaleOrderEntry>> groupedByFid = new HashMap<>();

        // 解析数据并按FID分组
        for (List<Object> row : pageData) {
            try {
                // 解析数据并转换为实体对象
                SaleOrderEntry entry = SaleOrderDataConverter.parseSaleOrderEntryFromRow(row);

                // 按FID分组
                groupedByFid.computeIfAbsent(entry.getFbillNo(), k -> new ArrayList<>()).add(entry);

            } catch (Exception e) {
                log.error("处理销售订单明细表数据失败，行数据：{}", row, e);
            }
        }

        log.debug("解析完成，处理 {} 条记录，FID数量：{}",
            pageData.size(),
            groupedByFid.size());

        return groupedByFid;
    }

    /**
     * 同步销售订单明细表数据
     * 改为先删除再插入的模式
     * @param pageSize 每页查询数量
     * @return 同步的记录数
     */
    private int syncSaleOrderDetailTable(int pageSize, List<List<List<Object>>> preloadedDetailPages) {
        int processedCount = 0;

        if (preloadedDetailPages.isEmpty()) {
            log.info("销售订单明细表无数据需要同步");
            return 0;
        }

        // 使用线程池并行解析所有页面的数据
        ExecutorService executor = ThreadPoolUtil.createFixedThreadPool("SaleOrderDetailParse");

        try {
            // 并行处理各页数据解析
            List<CompletableFuture<Map< String, List<SaleOrderEntry>>>> parseFutures = preloadedDetailPages.stream()
                .map(pageData -> CompletableFuture.supplyAsync(() -> parseSinglePage(pageData), executor))
                .collect(Collectors.toList());

            // 等待所有解析任务完成
            List<Map<String, List<SaleOrderEntry>>> parsedResults = parseFutures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());


            // 合并所有解析结果
            Map<String, List<SaleOrderEntry>> allGroupedByFid = new HashMap<>();
            for (Map<String, List<SaleOrderEntry>> partialResult : parsedResults) {
                for (Map.Entry<String, List<SaleOrderEntry>> entry : partialResult.entrySet()) {
                    allGroupedByFid.computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                        .addAll(entry.getValue());
                }
            }

            // 现在执行数据库操作（在事务中串行执行）
            if (!allGroupedByFid.isEmpty()) {
                // 提取所有FID，用于删除和插入操作
                List<String> allFids = new ArrayList<>(allGroupedByFid.keySet());

                log.debug("准备删除数据库中已存在的明细表FID，FID列表大小：{}", allFids.size());

                // 先批量删除数据库中已存在的记录
                int deleteCount = 0;
                if (!allFids.isEmpty()) {
                    // 分批删除，避免SQL语句过长
                    final int deleteBatchSize = 1000; // 每批删除1000个FID
                    for (int i = 0; i < allFids.size(); i += deleteBatchSize) {
                        int endIndex = Math.min(i + deleteBatchSize, allFids.size());
                        List<String> batchFids = allFids.subList(i, endIndex);
                        saleOrderEntryMapper.deleteByFids(batchFids);
                    }
                    deleteCount = allFids.size(); // 假设所有FID相关的记录都被删除
                    log.debug("批量删除完成，删除记录数：{}", deleteCount);
                }

                // 准备所有需要插入的记录
                List<SaleOrderEntry> allEntriesToInsert = new ArrayList<>();
                for (List<SaleOrderEntry> entries : allGroupedByFid.values()) {
                    allEntriesToInsert.addAll(entries);
                }

                log.info("销售订单明细表：准备删除 {} 个FID相关的记录，准备插入 {} 条新记录", allFids.size(), allEntriesToInsert.size());

                // 批量插入所有新记录
                int insertCount = 0;
                if (!allEntriesToInsert.isEmpty()) {
                    // 分批处理大批量数据，避免SQL语句过长
                    final int insertBatchSize = 1000; // 每批处理1000条
                    for (int i = 0; i < allEntriesToInsert.size(); i += insertBatchSize) {
                        int endIndex = Math.min(i + insertBatchSize, allEntriesToInsert.size());
                        List<SaleOrderEntry> batch = allEntriesToInsert.subList(i, endIndex);
                        saleOrderEntryMapper.batchInsert(batch);
                    }
                    insertCount = allEntriesToInsert.size(); // 实际插入的记录数
                }

                processedCount = deleteCount + insertCount;
            }

        } finally {
            // 关闭线程池
            ThreadPoolUtil.shutdown((ThreadPoolExecutor) executor);
        }

        return processedCount;
    }

    /**
     * 预加载所有销售订单成本表分页数据
     * @param pageSize 每页查询数量
     * @return 所有分页数据列表
     */
    private List<List<List<Object>>> preloadAllCostPages(int pageSize) {
        List<List<List<Object>>> allPages = new ArrayList<>();
        int startRow = 0;

        while (true) {
            List<List<Object>> pageData = dictionaryconfig.querySalesOrderCostEstimation(startRow, pageSize);

            if (pageData == null || pageData.isEmpty()) {
                break;
            }

            allPages.add(pageData);
            log.info("获取销售订单成本表数据页，大小：{}，起始行：{}", pageData.size(), startRow);

            if (pageData.size() < pageSize) {
                break; // 最后一页
            }

            startRow += pageSize;
        }

        return allPages;
    }

    /**
     * 解析单页销售订单成本表数据
     * @param pageData 单页原始数据
     * @return 解析后的实体列表
     */
    private List<SaleOrderCost> parseSingleCostPage(List<List<Object>> pageData) {
        List<SaleOrderCost> saleOrderCosts = new ArrayList<>();
        for (List<Object> row : pageData) {
            try {
                // 解析数据并转换为实体对象
                SaleOrderCost cost = SaleOrderDataConverter.parseSaleOrderCostFromRow(row);
                saleOrderCosts.add(cost);
            } catch (Exception e) {
                log.error("处理销售订单成本表数据失败，行数据：{}", row, e);
            }
        }
        return saleOrderCosts;
    }

    /**
     * 同步销售订单成本表数据
     * 改为先查询后更新/新增的模式
     * @param pageSize 每页查询数量
     * @return 同步的记录数
     */
    private int syncSaleOrderCostTable(int pageSize, List<List<List<Object>>> preloadedCostPages) {
        int processedCount = 0;

        if (preloadedCostPages.isEmpty()) {
            log.info("销售订单成本表无数据需要同步");
            return 0;
        }

        // 使用线程池并行解析所有页面的数据
        ExecutorService executor = ThreadPoolUtil.createFixedThreadPool("SaleOrderCostParse");

        try {
            // 并行处理各页数据解析
            List<CompletableFuture<List<SaleOrderCost>>> parseFutures = preloadedCostPages.stream()
                .map(pageData -> CompletableFuture.supplyAsync(() -> parseSingleCostPage(pageData), executor))
                .collect(Collectors.toList());

            // 等待所有解析任务完成
            List<SaleOrderCost> allSaleOrderCosts = parseFutures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());

            if (!allSaleOrderCosts.isEmpty()) {
                // 提取所有FID，查询数据库中已存在的记录
                Set<Long> allFids = allSaleOrderCosts.stream().map(SaleOrderCost::getFid).collect(Collectors.toSet());
                List<Long> fidList = new ArrayList<>(allFids);

                log.debug("准备查询数据库中已存在的成本表FID，FID列表大小：{}", fidList.size());

                // 查询数据库中已存在的FID
                List<Long> existingFids = saleOrderCostMapper.selectExistingFids(fidList);
                Set<Long> existingFidSet = new HashSet<>(existingFids);

                log.debug("数据库中已存在的成本表FID数量：{}，具体FIDs：{}", existingFids.size(), existingFids);

                // 分离需要更新和新增的记录
                List<SaleOrderCost> costsToUpdate = new ArrayList<>();
                List<SaleOrderCost> costsToInsert = new ArrayList<>();

                for (SaleOrderCost cost : allSaleOrderCosts) {
                    if (existingFidSet.contains(cost.getFid())) {
                        costsToUpdate.add(cost);
                    } else {
                        costsToInsert.add(cost);
                    }
                }

                log.info("销售订单成本表：需要更新 {} 条，新增 {} 条", costsToUpdate.size(), costsToInsert.size());

                // 执行更新操作
                AtomicInteger updateCount = new AtomicInteger(0);
                for (SaleOrderCost cost : costsToUpdate) {
                    log.debug("正在更新销售订单成本，FID: {}", cost.getFid());
                    int result = saleOrderCostMapper.updateByFid(cost);
                    if (result > 0) {
                        updateCount.incrementAndGet();
                    } else {
                        log.warn("更新成本失败，FID: {}", cost.getFid());
                    }
                }

                // 执行新增操作
                AtomicInteger insertCount = new AtomicInteger(0);
                for (SaleOrderCost cost : costsToInsert) {
                    log.debug("正在插入销售订单成本，FID: {}", cost.getFid());
                    try {
                        saleOrderCostMapper.insert(cost);
                    } catch (Exception e) {
                        log.error("插入销售订单成本失败，FID: {}，错误: {}", cost.getFid(), e.getMessage());
                        // 重新查询该FID是否存在，以确认问题原因
                        SaleOrderCost existingCost = saleOrderCostMapper.selectById(cost.getFid());
                        if (existingCost != null) {
                            log.warn("成本表FID {} 在数据库中确实已存在，但之前的查询未返回它", cost.getFid());
                        }
                        throw e;
                    }
                    insertCount.incrementAndGet();
                }

                processedCount = updateCount.get() + insertCount.get();
                log.debug("销售订单成本表处理完成，更新 {} 条，新增 {} 条，总计 {} 条",
                    updateCount.get(), insertCount.get(), processedCount);
            }
        } catch (Exception e) {
            log.error("处理销售订单成本表数据失败", e);
            throw e; // 重新抛出异常，让事务回滚
        } finally {
            // 关闭线程池
            ThreadPoolUtil.shutdown((ThreadPoolExecutor) executor);
        }

        return processedCount;
    }

    @Override
    public TableDataInfo<SaleOrder> selectPageSaleOrder(SaleOrder saleOrder, PageQuery pageQuery) {
        Page<SaleOrder> page = pageQuery.build();
        // 使用MyBatis-Plus的分页查询，传入查询条件
        IPage<SaleOrder> result = saleOrderMapper.selectPage(page, saleOrder);
        return TableDataInfo.build(result);
    }

    @Override
    public SaleOrder selectById(Long id) {
        SaleOrder saleOrder = saleOrderMapper.selectByAutoId(id);
        if (saleOrder != null) {
            saleOrder.setSaleOrderCost(saleOrderCostMapper.selectById(saleOrder.getFid()));
            saleOrder.setEntryList(saleOrderEntryMapper.selectByOrderId(saleOrder.getFBillNo()));
        }
        return saleOrder;
    }

    @Override
    public List<SalesRankingDTO> getTop3SalesByMonth(Integer year, Integer month) {
        // 参数校验
        if (year == null || month == null) {
            log.error("参数不能为空：年份和月份都需要提供");
            return new ArrayList<>();
        }

        // 验证月份范围
        if (month < 1 || month > 12) {
            log.error("月份必须在1-12之间");
            return new ArrayList<>();
        }

        try {
            // 查询指定月份的销售员排行榜
            List<Map<String, Object>> rankingList = saleOrderMapper.selectSalesRankingByMonth(year, month);

            // 转换为SalesRankingDTO对象列表，并只取前3名
            if (rankingList != null && !rankingList.isEmpty()) {
                return rankingList.stream()
                    .limit(3)  // 只取前3名
                    .map(map -> new SalesRankingDTO(
                        (String) map.get("salerName"),
                        ((Number) map.get("orderCount")).intValue()
                    ))
                    .collect(Collectors.toList());
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("查询销售员排行榜时发生错误", e);
            return new ArrayList<>();
        }
    }

    @Override
    public SalesOrderDTo getCountryOrderDistributionByMonth(SalesOrderDTo query) {

        Integer year = query.getYear();
        Integer month = query.getMonth();

        // 当期订单数
        Integer currentCount =
            saleOrderMapper.countCurrentPeriodOrders(year, month);

        // 上月订单数
        Integer lastMonthCount =
            saleOrderMapper.countLastMonthOrders(year, month);

        query.setTotalOrderCount(currentCount);
        query.setLastMonthOrderCount(lastMonthCount);

        // 国家分布
        query.setCountry(
            saleOrderMapper.countOrdersByCountry(year, month)
        );

        return query;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result insertSaleOrder(SaleOrder saleOrder) {
        try {
            //获取当前用户ID
            Long userId = LoginHelper.getUserId();
            //通过userId查询数据库获取当前用户
            SysUser user = sysUserMapper.selectById(userId);
            //将用户中的k3
            saleOrder.setFCreatorId(user.getK3Key());
            // 这里应该从当前登录用户获取，暂时设置为默认值
            // 参数校验
            if (saleOrder == null) {
                return Result.error("销售订单信息不能为空");
            }

            // 字典值转换：将前端传来的名称转换为对应的字典值
            if (saleOrder.getDocumentType() != null) {
                String documentTypeCode = convertDictNameToCode(saleOrder.getDocumentType(), "document_type");
                if (documentTypeCode != null) {
                    saleOrder.setDocumentType(documentTypeCode);
                }
            }

            if (saleOrder.getFSettleCurrId() != null) {
                String settleCurrIdCode = convertDictNameToCode(saleOrder.getFSettleCurrId(), "currency");
                if (settleCurrIdCode != null) {
                    saleOrder.setFSettleCurrId(settleCurrIdCode);
                }
            }

            if (saleOrder.getFbzfs() != null) {
                String bzfsCode = convertDictNameToCode(saleOrder.getFbzfs(), "manner_packing");
                if (bzfsCode != null) {
                    saleOrder.setFbzfs(bzfsCode);
                }
            }
            if (saleOrder.getFLocalCurrId() != null) {
                String localCurrIdCode = convertDictNameToCode(saleOrder.getFLocalCurrId(), "currency");
                if (localCurrIdCode != null) {
                    saleOrder.setFLocalCurrId(localCurrIdCode);
                }
            }


            // 检查是否存在相同单据编号的订单
            if (saleOrder.getFBillNo() != null) {
                SaleOrder existingOrder = saleOrderMapper.selectByBillNo(saleOrder.getFBillNo());
                if (existingOrder != null) {
                    return Result.error("单据编号已存在，请使用其他编号");
                }
            }

            // 插入主订单信息
            int result = saleOrderMapper.insert(saleOrder);

            if (result > 0) {
                log.info("销售订单新增成功，单据编号：{}", saleOrder.getFBillNo());

                // 如果有明细信息，也需要保存
                if (saleOrder.getEntryList() != null && !saleOrder.getEntryList().isEmpty()) {
                    for (SaleOrderEntry entry : saleOrder.getEntryList()) {
                        entry.setFbillNo(saleOrder.getFBillNo()); // 关联到主订单

                        // 对明细表中的字典字段进行转换
                        if (entry.getFPlanUnitId() != null) {
                            String planUnitIdCode = convertDictNameToCode(entry.getFPlanUnitId(), "unit");
                            if (planUnitIdCode != null) {
                                entry.setFPlanUnitId(planUnitIdCode);
                            }
                        }

                        if (entry.getFSfxp() != null) {
                            String sfxpCode = convertDictNameToCode(entry.getFSfxp(), "product_category");
                            if (sfxpCode != null) {
                                entry.setFSfxp(sfxpCode);
                            }
                        }

                        if (entry.getFCplb() != null) {
                            String cplbCode = convertDictNameToCode(entry.getFCplb(), "product_big_category");
                            if (cplbCode != null) {
                                entry.setFCplb(cplbCode);
                            }
                        }

                        saleOrderEntryMapper.insert(entry);
                    }
                }

                // 如果有成本信息，也需要保存
                if (saleOrder.getSaleOrderCost() != null) {
                    saleOrder.getSaleOrderCost().setFid(saleOrder.getFid()); // 关联到主订单

                    // 确保明细列表存在，否则传入空列表以避免计算错误
                    List<SaleOrderEntry> entryList = saleOrder.getEntryList();
                    if (entryList == null) {
                        entryList = new ArrayList<>();
                    }

                    SaleOrderCostCalculator.calculateCostFields(saleOrder, saleOrder.getSaleOrderCost(), entryList);

                    saleOrderCostMapper.insert(saleOrder.getSaleOrderCost());

                }
                Map<String, String> fields = new HashMap<>();
                fields.put("客户简称名称", saleOrder.getFOraBaseProperty());
                fields.put("销售订单编码", saleOrder.getFBillNo());
                fields.put("提交人", user.getNickName());

                batchGetEmployeeConfig.sendCommonPushCard(
                    "销售订单表推送",
                    fields,
                    "http://113.46.194.126/k3cloud",
                    "打开金蝶系统"
                );
                return Result.success(saleOrder);
            } else {
                log.error("销售订单新增失败，单据编号：{}", saleOrder.getFBillNo());
                return Result.error("销售订单新增失败");
            }
        } catch (Exception e) {
            log.error("新增销售订单时发生异常", e);
            return Result.error("新增销售订单失败：" + e.getMessage());
        }
    }

    /**
     * 根据字典名称和类别查询对应的字典编码
     *
     * @param dictName 字典名称
     * @param category 字典类别
     * @return 字典编码，如果未找到则返回 null
     */
    private String convertDictNameToCode(String dictName, String category) {
        try {
            // 查询字典表，根据名称和类别获取对应的编码
            BymaterialDictionary dict = bymaterialDictionaryMapper.selectByKingdeeCategory(dictName, category);
            if (dict != null) {
                return dict.getKingdee();
            } else {
                log.warn("未找到对应的字典值，名称：{}，类别：{}", dictName, category);
                return null;
            }
        } catch (Exception e) {
            log.error("查询字典值失败，名称：{}，类别：{}", dictName, category, e);
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> selectEntryList(String fbillNo) {
        try {
            log.debug("开始查询销售订单明细，fbillNo: {}", fbillNo);
            List<SaleOrderEntry> entryList = saleOrderEntryMapper.selectByOrderId(fbillNo);
            if (entryList == null || entryList.isEmpty()) {
                log.warn("未查询到销售订单明细数据，fbillNo: {}", fbillNo);
                return new ArrayList<>();
            }
            
            log.info("查询到销售订单明细 {} 条，fbillNo: {}", entryList.size(), fbillNo);
            // 将 SaleOrderEntry 转换为 Map 返回
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (SaleOrderEntry entry : entryList) {
                Map<String, Object> entryMap = new HashMap<>();
                entryMap.put("fEntryId", entry.getFEntryId());
                entryMap.put("fbillNo", entry.getFbillNo());
                entryMap.put("fPlanMaterialId", entry.getFPlanMaterialId());
                entryMap.put("fPlanMaterialName", entry.getFPlanMaterialName());
                entryMap.put("fQty", entry.getFQty());
                entryMap.put("fPrice", entry.getFPrice());
                entryMap.put("fTaxPrice", entry.getFTaxPrice());
                entryMap.put("fAllAmount", entry.getFAllAmount());
                entryMap.put("fDeliQty", entry.getFDeliQty());
                entryMap.put("f_mz", entry.getFmz());
                entryMap.put("f_jz", entry.getFjz());
                entryMap.put("f_kpdj", entry.getFKpdj());
                entryMap.put("f_ygcb", entry.getFYgcb());
                entryMap.put("f_hsbm", entry.getFHsbm());
                entryMap.put("f_cplb", entry.getFCplb());
                resultList.add(entryMap);
            }
            return resultList;
        } catch (Exception e) {
            log.error("查询销售订单明细失败，fbillNo: {}", fbillNo, e);
            throw new RuntimeException("查询销售订单明细失败：" + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> selectCostDataByBillNo(String fbillNo) {
        try {
            log.debug("开始查询销售订单成本，fbillNo: {}", fbillNo);
            // 直接通过 FBillNo 查询成本表，无需关联主表
            SaleOrderCost cost = saleOrderCostMapper.selectByBillNo(fbillNo);
            if (cost == null) {
                log.warn("未查询到销售订单成本数据，fbillNo: {}", fbillNo);
                return new HashMap<>();
            }
            
            log.info("查询到销售订单成本，fbillNo: {}", fbillNo);
            // 将 SaleOrderCost 转换为 Map 返回
            Map<String, Object> costMap = new HashMap<>();
            costMap.put("fid", cost.getFid());
            costMap.put("fbillno", cost.getFbillno());
            costMap.put("fHyf", cost.getFHyf());
            costMap.put("fBillAllAmount", cost.getFBillAllAmount());
            costMap.put("fBillAllAmountLc", cost.getFBillAllAmountLc());
            costMap.put("fBxf", cost.getFBxf());
            costMap.put("fGwyhfy", cost.getFGwyhfy());
            costMap.put("fQtwbfy", cost.getFQtwbfy());
            costMap.put("fMxcbhj", cost.getFMxcbhj());
            costMap.put("fMxtshj", cost.getFMxtshj());
            costMap.put("fCbxj", cost.getFCbxj());
            costMap.put("fBzf", cost.getFBzf());
            costMap.put("fDlf", cost.getFDlf());
            costMap.put("fRzf", cost.getFRzf());
            costMap.put("fKdf", cost.getFKdf());
            costMap.put("fHdf", cost.getFHdf());
            costMap.put("fLyf", cost.getFLyf());
            costMap.put("fQtfy", cost.getFQtfy());
            costMap.put("fMjf", cost.getFMjf());
            costMap.put("fJcf", cost.getFJcf());
            costMap.put("fFyxj", cost.getFFyxj());
            costMap.put("fWbyk", cost.getFWbyk());
            costMap.put("fJlre", cost.getFJlre());
            costMap.put("fLrl", cost.getFLrl());
            costMap.put("fJlrl", cost.getFJlrl());
            return costMap;
        } catch (Exception e) {
            log.error("查询销售订单成本失败，fbillNo: {}", fbillNo, e);
            throw new RuntimeException("查询销售订单成本失败：" + e.getMessage(), e);
        }
    }

    @Override
    public SaleOrder selectByBillNo(String billNo) {
        return saleOrderMapper.selectByBillNo(billNo);
    }

    @Override
    public List<Map<String, Object>> getSalespersonsFromOrders() {
        return saleOrderMapper.selectSalespersonsFromOrders();
    }

    @Override
    public int deleteSaleOrderByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            log.warn("删除销售订单失败，ID 列表为空");
            return 0;
        }

        try {
            int totalCount = 0;

            // 遍历每个 ID 进行删除
            for (Long id : ids) {
                // 查询订单信息（用于获取 FBillNo）
                SaleOrder order = saleOrderMapper.selectByAutoId(id);
                if (order == null) {
                    log.warn("订单不存在，跳过删除，ID: {}", id);
                    continue;
                }

                String fbillNo = order.getFBillNo();
                log.info("开始删除销售订单，ID: {}, FBillNo: {}", id, fbillNo);

                // 1. 删除明细表数据
                if (fbillNo != null && !fbillNo.isEmpty()) {
                    List<String> fbillNoList = Arrays.asList(fbillNo);
                    saleOrderEntryMapper.deleteByFids(fbillNoList);
                    log.debug("删除明细表数据成功，FBillNo: {}", fbillNo);
                }

                // 2. 删除成本表数据
                SaleOrderCost cost = saleOrderCostMapper.selectByBillNo(fbillNo);
                if (cost != null) {
                    saleOrderCostMapper.deleteById(cost.getFid());
                    log.debug("删除成本表数据成功，FBillNo: {}", fbillNo);
                }

                // 3. 删除主表数据
                int result = saleOrderMapper.deleteByAutoId(id);
                if (result > 0) {
                    totalCount++;
                    log.info("删除销售订单成功，ID: {}, FBillNo: {}", id, fbillNo);
                } else {
                    log.error("删除销售订单失败，ID: {}", id);
                }
            }

            log.info("批量删除销售订单完成，总计删除 {} 条记录", totalCount);
            return totalCount;

        } catch (Exception e) {
            log.error("批量删除销售订单失败", e);
            throw new RuntimeException("批量删除销售订单失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result auditSaleOrders(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return Result.error("请选择要审核的数据");
        }

        try {
            int successCount = 0;
            int skipCount = 0;
            StringBuilder skipBillNos = new StringBuilder();

            // 获取当前用户信息
            Long userId = LoginHelper.getUserId();
            SysUser user = sysUserMapper.selectById(userId);
            String auditorName = user != null ? user.getUserName() : "Unknown";

            for (Long id : ids) {
                SaleOrder order = saleOrderMapper.selectByAutoId(id);
                if (order == null) {
                    log.warn("订单不存在，无法审核，ID: {}", id);
                    continue;
                }

                // 检查单据状态，已审核的单据不能再次审核
                if ("C".equals(order.getFDocumentStatus())) {
                    skipCount++;
                    if (skipBillNos.length() > 0) {
                        skipBillNos.append(", ");
                    }
                    skipBillNos.append(order.getFBillNo());
                    continue;
                }

                // 更新单据状态为已审核（C）
                order.setFDocumentStatus("C");
                int result = saleOrderMapper.updateByFid(order);
                if (result > 0) {
                    successCount++;
                    log.info("审核销售订单成功，ID: {}, FBillNo: {}, 审核人：{}", id, order.getFBillNo(), auditorName);
                }
            }

            String message = String.format("审核完成！成功：%d 条", successCount);
            if (skipCount > 0) {
                message += String.format("，跳过（已审核）：%d 条 [%s]", skipCount, skipBillNos.toString());
            }

            return Result.success(message);

        } catch (Exception e) {
            log.error("审核销售订单失败", e);
            return Result.error("审核失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result unAuditSaleOrders(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return Result.error("请选择要反审核的数据");
        }

        try {
            int successCount = 0;
            int skipCount = 0;
            StringBuilder skipBillNos = new StringBuilder();

            for (Long id : ids) {
                SaleOrder order = saleOrderMapper.selectByAutoId(id);
                if (order == null) {
                    log.warn("订单不存在，无法反审核，ID: {}", id);
                    continue;
                }

                // 检查单据状态，只有已审核的单据才能反审核
                if (!"C".equals(order.getFDocumentStatus())) {
                    skipCount++;
                    if (skipBillNos.length() > 0) {
                        skipBillNos.append(", ");
                    }
                    skipBillNos.append(order.getFBillNo());
                    continue;
                }

                // 更新单据状态为创建（A）
                order.setFDocumentStatus("A");
                int result = saleOrderMapper.updateByFid(order);
                if (result > 0) {
                    successCount++;
                    log.info("反审核销售订单成功，ID: {}, FBillNo: {}", id, order.getFBillNo());
                }
            }

            String message = String.format("反审核完成！成功：%d 条", successCount);
            if (skipCount > 0) {
                message += String.format("，跳过（未审核）：%d 条 [%s]", skipCount, skipBillNos.toString());
            }

            return Result.success(message);

        } catch (Exception e) {
            log.error("反审核销售订单失败", e);
            return Result.error("反审核失败：" + e.getMessage());
        }
    }

}
