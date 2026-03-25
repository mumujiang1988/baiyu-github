package com.ruoyi.business.k3.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.entity.Employee;
import com.ruoyi.business.entity.PoOrderBillHead;
import com.ruoyi.business.entity.PoOrderBillHeadEntry;

import com.ruoyi.business.entity.Supplier;
import com.ruoyi.business.k3.config.k3config;
import com.ruoyi.business.k3.service.PurchaseOrderService;
import com.ruoyi.business.mapper.*;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import com.ruoyi.common.satoken.utils.LoginHelper;
import com.ruoyi.system.domain.vo.SysUserVo;
import com.ruoyi.system.mapper.SysUserMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicInteger;
import com.ruoyi.business.util.ThreadPoolUtil;
import com.ruoyi.business.k3.util.PurchaseOrderDataConverter;

@Slf4j
@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    @Resource
    private PoOrderBillHeadMapper poOrderBillHeadMapper;

    @Resource
    private k3config k3Config;
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private EmployeeMapper employeeMapper;

    @Resource
    private BymaterialDictionaryMapper bymaterialDictionaryMapper;
    @Resource
    private SupplierMapper supplierMapper;
    @Resource
    private PurchaseOrderEntryMapper purchaseOrderEntryMapper;

    /**
     * 多线程同步采购订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncPurchaseOrdersMultiThread() {
        log.info("开始同步金蝶采购订单数据...");

        int pageSize = 3000;
        int processedCount = 0;

        try {
            // 预先获取所有页面数据
            List<List<List<Object>>> preloadedPages = preloadAllPages(pageSize);

            if (preloadedPages.isEmpty()) {
                log.info("采购订单无数据需要同步");
                return 0;
            }

            // 使用线程池并行解析所有页面的数据
            ExecutorService executor = ThreadPoolUtil.createFixedThreadPool("PurchaseOrderParse");

            try {
                List<CompletableFuture<List<PoOrderBillHead>>> parseFutures = preloadedPages.stream()
                    .map(pageData -> CompletableFuture.supplyAsync(() -> parseSinglePage(pageData), executor))
                    .collect(Collectors.toList());

                // 等待所有解析任务完成
                List<PoOrderBillHead> allOrders = parseFutures.stream()
                    .map(CompletableFuture::join)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());


                if (!allOrders.isEmpty()) {
                    log.info("总共解析到 {} 条采购订单数据，开始处理新增/更新判断", allOrders.size());

                    Set<String> allK3Ids = allOrders.stream().map(PoOrderBillHead::getFid).collect(Collectors.toSet());
                    List<String> k3IdList = new ArrayList<>(allK3Ids);

                    // 查询数据库中已存在的K3 ID
                    List<String> existingK3Ids = poOrderBillHeadMapper.selectExistingK3Ids(k3IdList);
                    Set<String> existingK3IdSet = existingK3Ids != null ? new HashSet<>(existingK3Ids) : new HashSet<>();

                    log.debug("数据库中已存在的K3 ID数量：{}，具体K3 IDs：{}", existingK3Ids != null ? existingK3Ids.size() : 0, existingK3Ids);

                    // 分离需要更新和新增的记录
                    List<PoOrderBillHead> ordersToUpdate = new ArrayList<>();
                    List<PoOrderBillHead> ordersToInsert = new ArrayList<>();

                    // 使用Map来确保每个K3 ID只处理一次，避免重复处理
                    Map<String, PoOrderBillHead> uniqueOrdersMap = new HashMap<>();
                    for (PoOrderBillHead order : allOrders) {
                        // 如果K3 ID已存在，则更新；否则插入
                        // 如果有重复的K3 ID，保留最后解析到的数据
                        uniqueOrdersMap.put(order.getFid(), order);
                    }

                    for (Map.Entry<String, PoOrderBillHead> entry : uniqueOrdersMap.entrySet()) {
                        PoOrderBillHead order = entry.getValue();
                        if (existingK3IdSet.contains(order.getFid())) {
                            ordersToUpdate.add(order);
                        } else {
                            ordersToInsert.add(order);
                        }
                    }

                    log.info("采购订单：需要更新 {} 条，新增 {} 条", ordersToUpdate.size(), ordersToInsert.size());

                    // 执行更新操作
                    AtomicInteger updateCount = new AtomicInteger(0);
                    for (PoOrderBillHead order : ordersToUpdate) {
                        log.debug("正在更新采购订单，K3 ID: {}", order.getFid());
                        int result = poOrderBillHeadMapper.updateByK3Id(order);
                        if (result > 0) {
                            updateCount.incrementAndGet();
                        } else {
                            log.warn("更新失败，K3 ID: {}", order.getFid());
                        }
                    }

                    // 执行新增操作
                    AtomicInteger insertCount = new AtomicInteger(0);
                    for (PoOrderBillHead order : ordersToInsert) {
                        log.debug("正在插入采购订单，K3 ID: {}", order.getFid());
                        try {
                            // 在插入前再次检查该记录是否已存在（以防并发问题）
                            PoOrderBillHead existingOrder = poOrderBillHeadMapper.selectByK3Id(order.getFid());
                            if (existingOrder == null) {
                                poOrderBillHeadMapper.insert(order);
                            } else {
                                log.warn("采购订单K3 ID {} 已存在，跳过插入", order.getFid());
                                // 如果记录存在，改为更新操作
                                poOrderBillHeadMapper.updateByK3Id(order);
                            }
                        } catch (Exception e) {
                            log.error("插入采购订单失败，K3 ID: {}，错误: {}", order.getFid(), e.getMessage());
                            throw e;
                        }
                        insertCount.incrementAndGet();
                    }

                    processedCount = updateCount.get() + insertCount.get();
                    log.debug("采购订单处理完成，更新 {} 条，新增 {} 条，总计 {} 条",
                        updateCount.get(), insertCount.get(), processedCount);
                }
            } finally {
                // 关闭线程池
                ThreadPoolUtil.shutdown((ThreadPoolExecutor) executor);
            }
        } catch (Exception e) {
            log.error("同步采购订单异常", e);
            throw new RuntimeException(e);
        }

        return processedCount;
    }

    /**
     * 同步采购订单详情表数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncPurchaseOrderEntries(k3config k3Config) {
        log.info("开始同步金蝶采购订单详情数据...");

        int pageSize = 3000;
        int processedCount = 0;
        int startRow = 0;

        try {
            while (true) {
                // 获取采购订单详情数据
                List<List<Object>> pageData = k3Config.purchaseOrderBillheadEentry(startRow, pageSize);

                if (pageData == null || pageData.isEmpty()) {
                    break;
                }

                log.info("开始处理第 {} 行到第 {} 行的采购订单详情数据", startRow, startRow + pageData.size());

                // 解析页面数据为实体对象
                List<PoOrderBillHeadEntry> pageEntries = parsePurchaseOrderEntryPage(pageData);

                if (!pageEntries.isEmpty()) {
                    log.info("页面解析到 {} 条采购订单详情数据，开始批量处理", pageEntries.size());

                    // 批量处理新增和更新
                    AtomicInteger insertCount = new AtomicInteger(0);
                    AtomicInteger updateCount = new AtomicInteger(0);

                    // 遍历每条详情记录，判断是新增还是更新
                    for (PoOrderBillHeadEntry entry : pageEntries) {
                        // 先检查是否有旧的查询方法调用
                        if (entry.getFid() != null && entry.getFGyswlbm() != null) {
                            // 查询数据库中是否已存在相同单据编号和供应商物料编码的记录
                            List<PoOrderBillHeadEntry> existingEntries = purchaseOrderEntryMapper.selectByFidAndFgyswlbm(entry.getFid(), entry.getFGyswlbm());

                            if (!existingEntries.isEmpty()) {
                                // 存在相同单据编号和供应商物料编码的记录，进行更新操作
                                purchaseOrderEntryMapper.updateByFidAndFgyswlbm(entry);
                                updateCount.incrementAndGet();
                            } else {
                                // 不存在相同条件的记录，进行新增操作
                                purchaseOrderEntryMapper.insert(entry);
                                insertCount.incrementAndGet();
                            }
                        } else {
                            // 缺少关键字段，直接插入
                            purchaseOrderEntryMapper.insert(entry);
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

            log.info("采购订单详情同步完成，总计处理：{} 条", processedCount);
        } catch (Exception e) {
            log.error("同步采购订单详情异常", e);
            throw new RuntimeException(e);
        }

        return processedCount;
    }

    /**
     * 预加载所有页面数据
     */
    private List<List<List<Object>>> preloadAllPages(int pageSize) {
        List<List<List<Object>>> allPages = new ArrayList<>();
        int startRow = 0;

        while (true) {
            List<List<Object>> pageData = k3Config.queryPurchaseOrderPage(startRow, pageSize);
            if (pageData == null || pageData.isEmpty()) {
                break;
            }
            allPages.add(pageData);
            if (pageData.size() < pageSize) {
                break;
            }
            startRow += pageSize;
        }

        log.info("预加载采购订单页面数量：{}", allPages.size());
        return allPages;
    }

    /**
     * 解析单个页面的数据
     */
    private List<PoOrderBillHead> parseSinglePage(List<List<Object>> pageData) {
        List<PoOrderBillHead> pageOrders = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;
        int nullBillNoCount = 0;

        for (List<Object> row : pageData) {
            try {
                PoOrderBillHead order = PurchaseOrderDataConverter.parsePurchaseOrderFromRow(row);
                if (order != null) {
                    if (order.getFid() != null && !order.getFid().isEmpty()) {
                        pageOrders.add(order);
                        successCount++;
                    } else {
                        nullBillNoCount++;
                    }
                }
            } catch (Exception e) {
                log.error("解析采购订单行数据失败: {}", row, e);
                errorCount++;
            }
        }

        log.debug("页面解析统计：原始行数 {}，成功解析 {}，单据号为空 {}，解析错误 {}",
            pageData.size(), successCount, nullBillNoCount, errorCount);
        return pageOrders;
    }

    /**
     * 解析采购订单详情单页数据
     */
    private List<PoOrderBillHeadEntry> parsePurchaseOrderEntryPage(List<List<Object>> pageData) {
        List<PoOrderBillHeadEntry> pageEntries = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        for (List<Object> row : pageData) {
            try {
                PoOrderBillHeadEntry entry = PurchaseOrderDataConverter.parsePurchaseOrderEntryFromRow(row);
                if (entry != null) {
                    pageEntries.add(entry);
                    successCount++;
                }
            } catch (Exception e) {
                log.error("解析采购订单详情行数据失败: {}", row, e);
                errorCount++;
            }
        }

        log.debug("页面解析统计：原始行数 {}，成功解析 {}，解析错误 {}", pageData.size(), successCount, errorCount);
        return pageEntries;
    }



    @Override
    public TableDataInfo<PoOrderBillHead> list(PoOrderBillHead poOrderBillHead, PageQuery pageQuery) {
        // 使用 MyBatis Plus 的分页插件
       Page<PoOrderBillHead> mpPage = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());

        // 执行分页查询
        Page<PoOrderBillHead> pageResult = poOrderBillHeadMapper.selectPage(mpPage, poOrderBillHead);

        return TableDataInfo.build(pageResult);
    }

    @Override
    public PoOrderBillHead getPurchaseOrder(Long id) {
        log.info("查询采购订单详细信息，ID: {}", id);

        // 通过ID查询采购订单主表信息
        PoOrderBillHead purchaseOrder = poOrderBillHeadMapper.selectById(id);

        if (purchaseOrder == null) {
            log.warn("采购订单不存在，ID: {}", id);
            return null;
        }

        // 查询采购订单详情表数据
        List<PoOrderBillHeadEntry> purchaseOrderEntries = purchaseOrderEntryMapper.selectByBillNo(purchaseOrder.getFbillNo());

        // 设置到采购订单对象中
        purchaseOrder.setEntries(purchaseOrderEntries);

        log.debug("查询到采购订单: {}，包含 {} 条详情记录", purchaseOrder.getFbillNo(), purchaseOrderEntries.size());

        return purchaseOrder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addPurchaseOrder(PoOrderBillHead poOrderBillHead) {
        log.info("新增采购订单，单据编号: {}", poOrderBillHead.getFbillNo());

        try {

            Supplier supplier = supplierMapper.selectById(poOrderBillHead.getFsupplierId()); // 检查供应商编号是否存在
                // 检查单据编号是否已存在
                PoOrderBillHead existingOrder = poOrderBillHeadMapper.selectByBillNo(poOrderBillHead.getFbillNo());
                if (existingOrder != null) {
                    return Result.error("单据编号已存在: " + poOrderBillHead.getFbillNo());
                }
            Long userId = LoginHelper.getUserId();
            SysUserVo user = sysUserMapper.selectVoById(userId);
            SysUserVo modifier = sysUserMapper.selectVoById(Long.valueOf(poOrderBillHead.getFmodifierId()));
            SysUserVo approver = sysUserMapper.selectVoById(Long.valueOf(poOrderBillHead.getFapproverId()));
            Employee employee = employeeMapper.selectByExactStaffNumber(poOrderBillHead.getFGdy1());
            poOrderBillHead.setFassignSupplierId(supplier.getSupplierid());
            poOrderBillHead.setFcreatorId(user.getK3Key());
            poOrderBillHead.setFmodifierId(modifier.getK3Key());
            poOrderBillHead.setFapproverId(approver.getK3Key());
            poOrderBillHead.setFGdy1(employee.getSalesmanId());
            // 插入主表数据
            int result = poOrderBillHeadMapper.insert(poOrderBillHead);

            if (result > 0) {
                log.info("新增采购订单成功，单据编号: {}", poOrderBillHead.getFbillNo());

                // 如果有详情数据，一并插入
                if (poOrderBillHead.getEntries() != null && !poOrderBillHead.getEntries().isEmpty()) {
                    for (PoOrderBillHeadEntry entry : poOrderBillHead.getEntries()) {
                        entry.setFid(poOrderBillHead.getFid()); // 关联主表ID
                        entry.setFbillNo(poOrderBillHead.getFbillNo()); // 设置单据编号
                        purchaseOrderEntryMapper.insert(entry); // 插入详情表
                    }
                    log.info("新增采购订单详情 {} 条", poOrderBillHead.getEntries().size());
                }

                return Result.success("新增采购订单成功", poOrderBillHead);
            } else {
                log.error("新增采购订单失败，单据编号: {}", poOrderBillHead.getFbillNo());
                return Result.error("新增采购订单失败");
            }
        } catch (Exception e) {
            log.error("新增采购订单异常，单据编号: {}", poOrderBillHead.getFbillNo(), e);
            return Result.error("新增采购订单异常: " + e.getMessage());
        }
    }
}
