package com.ruoyi.business.k3.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.entity.InquiryOrder;
import com.ruoyi.business.entity.InquiryOrderEntry;
import com.ruoyi.business.feishu.config.BatchGetEmployeeConfig;
import com.ruoyi.business.k3.config.Dictionaryconfig;
import com.ruoyi.business.k3.service.InquiryOrderService;
import com.ruoyi.business.k3.util.K3DataUtils;
import com.ruoyi.business.mapper.InquiryOrderEntryMapper;
import com.ruoyi.business.mapper.InquiryOrderMapper;
import com.ruoyi.business.k3.util.DateUtils;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import com.ruoyi.common.satoken.utils.LoginHelper;
import com.ruoyi.system.domain.vo.SysUserVo;
import com.ruoyi.system.mapper.SysUserMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 询价单服务实现类
 */
@Slf4j
@Service
public class InquiryOrderServiceImpl implements InquiryOrderService, AutoCloseable {

    @Resource
    private InquiryOrderMapper inquiryOrderMapper;

    @Resource
    private InquiryOrderEntryMapper inquiryOrderEntryMapper;
    @Resource
    private BatchGetEmployeeConfig batchGetEmployeeConfig;
    @Resource
    private Dictionaryconfig dictionaryconfig;
    @Autowired
    private SysUserMapper userMapper;
    // 创建固定大小的线程池
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncInquiryOrdersFromK3() {
        log.info("开始同步金蝶询价单数据...");

        // 每页查询数量
        int pageSize = 5000;
        int totalCount = 0;

        try {
            // 预先获取金蝶中的数据
      //      List<List<List<Object>>> mainPages = preloadAllMainPages(pageSize);
            List<List<List<Object>>> detailPages = preloadAllDetailPages(pageSize);

            // ============ 1. 同步询价单主表 ============
//            int mainTableCount = syncInquiryOrderMainTable( mainPages);
//            totalCount += mainTableCount;
//            log.info("询价单主表同步完成，共处理 {} 条数据", mainTableCount);

            // ============ 2. 同步询价单明细表 ============
            int detailTableCount = syncInquiryOrderDetailTable( detailPages);
            totalCount += detailTableCount;
            log.info("询价单明细表同步完成，共处理 {} 条数据", detailTableCount);

            log.info("询价单数据同步完成，总计处理 {} 条数据", totalCount);
            return totalCount;

        } catch (Exception e) {
            log.error("同步询价单数据失败", e);
            throw new RuntimeException("同步询价单数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 预加载所有询价单主表分页数据
     * @param pageSize 每页查询数量
     * @return 所有分页数据列表
     */
    private List<List<List<Object>>> preloadAllMainPages(int pageSize) {
        List<List<List<Object>>> allPages = new ArrayList<>();
        int startRow = 0;

        while (true) {
            List<List<Object>> pageData = dictionaryconfig.queryInquirylList(startRow, pageSize);
            if (pageData == null || pageData.isEmpty()) {
                break;
            }
            allPages.add(pageData);
            if (pageData.size() < pageSize) {
                break;
            }
            startRow += pageSize;
        }

        log.info("预加载询价单主表数据完成，共 {} 页", allPages.size());
        return allPages;
    }

    /**
     * 预加载所有询价单明细表分页数据
     * @param pageSize 每页查询数量
     * @return 所有分页数据列表
     */
    private List<List<List<Object>>> preloadAllDetailPages(int pageSize) {
        List<List<List<Object>>> allPages = new ArrayList<>();
        int startRow = 0;

        while (true) {
            List<List<Object>> pageData = dictionaryconfig.queryInquiryEntryList(startRow, pageSize);
            if (pageData == null || pageData.isEmpty()) {
                break;
            }
            allPages.add(pageData);
            if (pageData.size() < pageSize) {
                break;
            }
            startRow += pageSize;
        }

        log.info("预加载询价单明细表数据完成，共 {} 页", allPages.size());
        return allPages;
    }

    /**
     * 同步询价单主表数据
     * @param allPages 所有分页数据
     * @return 处理的记录数
     */
    private int syncInquiryOrderMainTable(List<List<List<Object>>> allPages) {
        int totalProcessed = 0;
        int totalInsertCount = 0;
        int totalUpdateCount = 0;

        try {
            // 使用CompletableFuture并行处理每个页面的数据
            List<CompletableFuture<Integer[]>> futures = allPages.stream()
                    .map(pageData -> CompletableFuture.supplyAsync(() -> {
                        int localProcessed = 0;
                        int localInsertCount = 0;
                        int localUpdateCount = 0;

                        for (List<Object> rowData : pageData) {
                            try {
                                // 转换为询价单实体
                                InquiryOrder inquiryOrder = convertToInquiryOrder(rowData);

                                if (inquiryOrder.getFid() == null || inquiryOrder.getFbillno() == null) {
                                    log.warn("询价单数据不完整，跳过处理: FID={}, BillNo={}",
                                            inquiryOrder.getFid(), inquiryOrder.getFbillno());
                                    continue;
                                }

                                // 查询数据库是否存在
                                InquiryOrder existing = inquiryOrderMapper.selectByBillNo(inquiryOrder.getFbillno());
                                if (existing != null) {
                                    // 存在则更新
                                    inquiryOrder.setFid(existing.getFid()); // 保持原有ID

                                        inquiryOrderMapper.updateById(inquiryOrder);
                                        localUpdateCount++;

                                } else {
                                    // 不存在则新增
                                    // 检查是否会有重复插入
                                    InquiryOrder duplicateCheck = inquiryOrderMapper.selectByBillNo(inquiryOrder.getFbillno());
                                    if (duplicateCheck == null) {
                                        inquiryOrderMapper.insert(inquiryOrder);
                                        localInsertCount++;
                                    } else {
                                        log.warn("发现重复的单据编号，跳过插入: {}", inquiryOrder.getFbillno());
                                        // 如果确实存在但查询没找到，尝试更新
                                        inquiryOrder.setFid(duplicateCheck.getFid());
                                        inquiryOrderMapper.updateById(inquiryOrder);
                                        localUpdateCount++;
                                    }
                                }
                                localProcessed++;
                            } catch (Exception e) {
                                log.error("处理询价单主表数据失败: {}", rowData, e);
                            }
                        }

                        return new Integer[]{localProcessed, localInsertCount, localUpdateCount};
                    }, executorService))
                    .collect(Collectors.toList());

            // 等待所有异步任务完成并收集结果
            for (CompletableFuture<Integer[]> future : futures) {
                Integer[] result = future.join();
                totalProcessed += result[0];
                totalInsertCount += result[1];
                totalUpdateCount += result[2];
            }

        } catch (Exception e) {
            log.error("并行处理询价单主表数据失败", e);
            throw e;
        }

        log.info("询价单主表数据同步完成，总计处理：{} 条，新增 {} 条，更新 {} 条",
                totalProcessed, totalInsertCount, totalUpdateCount);
        return totalProcessed;
    }

    /**
     * 同步询价单明细表数据
     * @param allPages 所有分页数据
     * @return 处理的记录数
     */
    private int syncInquiryOrderDetailTable(List<List<List<Object>>> allPages) {
        int totalProcessed = 0;
        int totalInsertCount = 0;
        int totalUpdateCount = 0;

        try {
            // 使用CompletableFuture并行处理每个页面的数据
            List<CompletableFuture<Integer[]>> futures = allPages.stream()
                    .map(pageData -> CompletableFuture.supplyAsync(() -> {
                        int localProcessed = 0;
                        int localInsertCount = 0;
                        int localUpdateCount = 0;

                        // 按fid分组收集数据
                        Map<String, List<InquiryOrderEntry>> groupedEntries = new HashMap<>();

                        for (List<Object> rowData : pageData) {
                            try {
                                // 转换为询价单明细实体
                                InquiryOrderEntry entry = convertToInquiryOrderEntry(rowData);

                                if (entry.getFid() == null || entry.getFbillno() == null) {
                                    log.warn("询价单明细数据不完整，跳过处理: FID={}, BillNo={}",
                                            entry.getFid(), entry.getFbillno());
                                    continue;
                                }

                                // 按fid分组，确保删除和插入操作针对相同的数据
                                groupedEntries.computeIfAbsent(entry.getFid(), k -> new ArrayList<>()).add(entry);
                                localProcessed++;
                            } catch (Exception e) {
                                log.error("处理询价单明细数据失败: {}", rowData, e);
                                // 不抛出异常，继续处理其他数据
                            }
                        }

                        // 对每个fid组进行删除和插入操作
                        for (Map.Entry<String, List<InquiryOrderEntry>> entryGroup : groupedEntries.entrySet()) {
                            String fid = entryGroup.getKey();
                            List<InquiryOrderEntry> entriesForFid = entryGroup.getValue();

                            // 删除该fid对应的所有现有记录
                            inquiryOrderEntryMapper.deleteByParentId(fid);
                            localUpdateCount++; // 增加更新计数（这里用更新计数表示删除操作）

                            // 插入该fid的所有新记录
                            for (InquiryOrderEntry entry : entriesForFid) {
                                inquiryOrderEntryMapper.save(entry);
                                localInsertCount++; // 增加插入计数
                            }
                        }

                        return new Integer[]{localProcessed, localInsertCount, localUpdateCount};
                    }, executorService))
                    .collect(Collectors.toList());

            // 等待所有异步任务完成并收集结果
            for (CompletableFuture<Integer[]> future : futures) {
                Integer[] result = future.join();
                totalProcessed += result[0];
                totalInsertCount += result[1];
                totalUpdateCount += result[2];
            }

        } catch (Exception e) {
            log.error("并行处理询价单明细数据失败", e);
            // 不抛出异常，让已成功处理的数据提交
        }

        log.info("询价单明细数据同步完成，总计处理：{} 条，新增 {} 条，更新 {} 条",
                totalProcessed, totalInsertCount, totalUpdateCount);
        return totalProcessed;
    }

    /**
     * 将金蝶数据转换为InquiryOrder实体
     */
    private InquiryOrder convertToInquiryOrder(List<Object> rowData) {
        InquiryOrder order = new InquiryOrder();

        int index = 0;
        // FID
        order.setFid(K3DataUtils.getString(rowData, index++));
        // FBillNo
        order.setFbillno(K3DataUtils.getString(rowData, index++));
        // FDocumentStatus
        order.setFdocumentstatus(K3DataUtils.getString(rowData, index++));
        // FCreatorId
        order.setFcreatorid(K3DataUtils.getString(rowData, index++));
        // FCreateDate
        order.setFcreatedate(K3DataUtils.getLocalDateTime(rowData, index++));
        // Fshr
        order.setFshr(K3DataUtils.getString(rowData, index++));
        // FDATE_sh
        order.setFdateSh(K3DataUtils.getLocalDateTime(rowData, index++));
        // FDate
        order.setFdate(DateUtils.parseLocalDate(rowData.get(index++)));
        // F_ckgj
        order.setFCkgj(K3DataUtils.getString(rowData, index++));
        // F_khly
        order.setFKhly(K3DataUtils.getString(rowData, index++));
        // F_gdhl
        order.setFGdhl(K3DataUtils.getBigDecimal(rowData, index++));
        // F_bjbb
        order.setFBjbb(K3DataUtils.getString(rowData, index++));
        // F_mll
        order.setFMll(K3DataUtils.getBigDecimal(rowData, index++));
        // F_khbm
        order.setFKhbm(K3DataUtils.getString(rowData, index++));
        // F_cty_BaseProperty
        order.setFCtyBaseproperty(K3DataUtils.getString(rowData, index++));
        // F_cty_BaseProperty1
        order.setFCtyBaseproperty1(K3DataUtils.getString(rowData, index++));
        // F_xsy1
        order.setFXsy1(K3DataUtils.getString(rowData, index++));
        // F_ywy
        order.setFYwy(K3DataUtils.getString(rowData, index++));
        // Fkhxq
        order.setFkhxq(K3DataUtils.getString(rowData, index++));
        // F_qyd
        order.setFQyd(K3DataUtils.getString(rowData, index++));
        // F_fkfs
        order.setFFkfs(K3DataUtils.getString(rowData, index++));
        // F_cty_BaseProperty4
        order.setFCtyBaseproperty4(K3DataUtils.getString(rowData, index++));
        // F_btjshj
        order.setFBtjshj(K3DataUtils.getBigDecimal(rowData, index++));
        // F_jshjbwb
        order.setFJshjbwb(K3DataUtils.getBigDecimal(rowData, index++));
        // F_btcbhj
        order.setFBtcbhj(K3DataUtils.getBigDecimal(rowData, index++));
        // F_Email
        order.setFEmail(K3DataUtils.getString(rowData, index++));
        // F_mob
        order.setFMob(K3DataUtils.getString(rowData, index++));
        // F_Tel
        order.setFTel(K3DataUtils.getString(rowData, index++));
        // F_Fax
        order.setFFax(K3DataUtils.getString(rowData, index++));
        // Fmjlr
        order.setFmjlr(K3DataUtils.getBigDecimal(rowData, index++));
        // F_cgbjrq
        order.setFCgbjrq(DateUtils.parseLocalDate(rowData.get(index++)));
        // F_bzf
        order.setFBzf(K3DataUtils.getBigDecimal(rowData, index++));
        // F_hdf
        order.setFHdf(K3DataUtils.getBigDecimal(rowData, index++));
        // F_lyf
        order.setFLyf(K3DataUtils.getBigDecimal(rowData, index++));
        // F_qtfy
        order.setFQtfy(K3DataUtils.getBigDecimal(rowData, index++));
        // F_fyxj
        order.setFFyxj(K3DataUtils.getBigDecimal(rowData, index++));
        // F_hqbjfk
        order.setFHqbjfk(K3DataUtils.getString(rowData, index++));

        return order;
    }

    /**
     * 将金蝶数据转换为InquiryOrderEntry实体
     */
    private InquiryOrderEntry convertToInquiryOrderEntry(List<Object> rowData) {
        InquiryOrderEntry entry = new InquiryOrderEntry();

        int index = 0;
        // FID
        entry.setFid(K3DataUtils.getString(rowData, index++));
        // FBillNo
        entry.setFbillno(K3DataUtils.getString(rowData, index++));
        // F_cplb
        entry.setFCplb(K3DataUtils.getString(rowData, index++));
        // F_jqfx
        entry.setFJqfx(K3DataUtils.getString(rowData, index++));
        // F_wldm
        entry.setFWldm(K3DataUtils.getString(rowData, index++));
        // F_hsdj
        entry.setFHsdj(K3DataUtils.getBigDecimal(rowData, index++));
        // F_ywpm
        entry.setFYwpm(K3DataUtils.getString(rowData, index++));
        // F_cty_BaseProperty2
        entry.setFCtyBaseproperty2(K3DataUtils.getString(rowData, index++));
        // F_Cbhj
        entry.setFCbhj(K3DataUtils.getBigDecimal(rowData, index++));
        // F_cty_BaseProperty3
        entry.setFCtyBaseproperty3(K3DataUtils.getString(rowData, index++));
        // F_xsddgls
        entry.setFXsddgls(K3DataUtils.getBigDecimal(rowData, index++));
        // F_xsddxtzt
        entry.setFXsddxtzt(K3DataUtils.getString(rowData, index++));
        // F_cty_baseproperty5
        entry.setFCtyBaseproperty5(K3DataUtils.getString(rowData, index++));
        // F_khbzyq
        entry.setFKhbzyq(K3DataUtils.getString(rowData, index++));
        // F_xdl
        entry.setFXdl(K3DataUtils.getBigDecimal(rowData, index++));
        // F_hscgj
        entry.setFHscgj(K3DataUtils.getBigDecimal(rowData, index++));
        // F_cgxpms
        entry.setFCgxpms(K3DataUtils.getString(rowData, index++));
        // F_cgbzfs
        entry.setFCgbzfs(K3DataUtils.getString(rowData, index++));
        // F_qdl
        entry.setFQdl(K3DataUtils.getBigDecimal(rowData, index++));
        // F_jshj
        entry.setFJshj(K3DataUtils.getBigDecimal(rowData, index++));
        // F_Cptp1
        entry.setFCptp1(K3DataUtils.getString(rowData, index++));
        // F_TP
        entry.setFTp(K3DataUtils.getString(rowData, index++));
        // F_Cgtp1
        entry.setFCgtp1(K3DataUtils.getString(rowData, index++));
        // F_Cgtp2
        entry.setFCgtp2(K3DataUtils.getString(rowData, index++));
        // F_jgsxrq
        entry.setFJgsxrq(DateUtils.parseLocalDate(rowData.get(index++)));
        // F_yjjq
        entry.setFYjjq(DateUtils.parseLocalDate(rowData.get(index++)));
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
        // F_glsl
        entry.setFGlsl(K3DataUtils.getBigDecimal(rowData, index++));
        // F_gctp
        entry.setFGctp(K3DataUtils.getString(rowData, index++));
        // Fgcywms
        entry.setFgcywms(K3DataUtils.getString(rowData, index++));
        // Fgccshys
        entry.setFgccshys(K3DataUtils.getString(rowData, index++));
        // Fbzcc
        entry.setFbzcc(K3DataUtils.getString(rowData, index++));
        // Fkhhh
        entry.setFkhhh(K3DataUtils.getString(rowData, index++));
        // Fzjxdhsj
        entry.setFzjxdhsj(K3DataUtils.getBigDecimal(rowData, index++));
        // Fzjxdsl
        entry.setFzjxdsl(K3DataUtils.getBigDecimal(rowData, index++));
        // Fmrcghsj
        entry.setFmrcghsj(K3DataUtils.getBigDecimal(rowData, index++));
        // Fxsj
        entry.setFxsj(K3DataUtils.getBigDecimal(rowData, index++));
        // Fgys
        entry.setFgys(K3DataUtils.getString(rowData, index++));
        // Ftpr
        entry.setFtpr(K3DataUtils.getString(rowData, index++));
        // F_tp3
        entry.setFTp3(K3DataUtils.getString(rowData, index++));
        // Fbjr
        entry.setFbjr(K3DataUtils.getString(rowData, index++));
        // Fcpys
        entry.setFcpys(K3DataUtils.getString(rowData, index++));
        // F_YWMS
        entry.setFYwms(K3DataUtils.getString(rowData, index++));

        return entry;
    }

    @Override
    public TableDataInfo<InquiryOrder> selectPageInquiryOrder(InquiryOrder inquiryOrder, PageQuery pageQuery) {
        Page<InquiryOrder> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        List<InquiryOrder> list = inquiryOrderMapper.selectList(inquiryOrder);
        page.setRecords(list);
        page.setTotal(list.size());
        return TableDataInfo.build(page);
    }

    @Override
    public InquiryOrder selectById(String id) {
        InquiryOrder order = inquiryOrderMapper.selectById(id);
        if (order != null) {
            List<InquiryOrderEntry> entries = inquiryOrderEntryMapper.selectByProductCode(order.getFbillno());
            if (entries != null && !entries.isEmpty()){
            order.setEntries(entries);
            }
        }
        return order;
    }

    @Override
    public InquiryOrder selectByBillNo(String billNo) {
        return inquiryOrderMapper.selectByBillNo(billNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addInquiryOrder(InquiryOrder inquiryOrder) {
        try {
            // 检查单据编号是否已存在
            InquiryOrder existing = inquiryOrderMapper.selectByBillNo(inquiryOrder.getFbillno());
            if (existing != null) {
                return Result.error("单据编号已存在: " + inquiryOrder.getFbillno());
            }
            Long userId = LoginHelper.getUserId();
            SysUserVo user  = userMapper.selectVoById(userId);
            // 设置创建时间
            inquiryOrder.setFcreatedate(LocalDateTime.now());

            // 插入主表数据
            int result = inquiryOrderMapper.insert(inquiryOrder);

            if (result > 0) {
                log.info("新增询价单成功，单据编号: {}", inquiryOrder.getFbillno());

                // 如果有详情数据，一并插入
                if (inquiryOrder.getEntries() != null && !inquiryOrder.getEntries().isEmpty()) {
                    for (InquiryOrderEntry entry : inquiryOrder.getEntries()) {
                        entry.setFid(inquiryOrder.getFid()); // 关联主表ID
                        entry.setFbillno(inquiryOrder.getFbillno()); // 设置单据编号
                        inquiryOrderEntryMapper.insert(entry); // 插入详情表
                    }
                    log.info("新增询价单详情 {} 条", inquiryOrder.getEntries().size());
                }

                //飞书通知
                Map<String, String> fields = new HashMap<>();
                fields.put("询价单编号", inquiryOrder.getFbillno());
                fields.put("客户编码", inquiryOrder.getFKhbm());
                fields.put("提交人", user.getNickName());
                fields.put("状态", "推送成功");

                batchGetEmployeeConfig.sendCommonPushCard(
                    "询价单推送",
                    fields,
                    "http://113.46.194.126/k3cloud",
                    "打开金蝶系统"
                );
                return Result.success("新增询价单成功", inquiryOrder);
            } else {
                log.error("新增询价单失败，单据编号: {}", inquiryOrder.getFbillno());
                return Result.error("新增询价单失败");
            }
        } catch (Exception e) {
            log.error("新增询价单异常，单据编号: {}", inquiryOrder.getFbillno(), e);
            return Result.error("新增询价单异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateInquiryOrder(InquiryOrder inquiryOrder) {
        try {
            if (inquiryOrder == null || inquiryOrder.getFid() == null) {
                return Result.error("询价单信息不完整");
            }

            // 查询原数据
            InquiryOrder existing = inquiryOrderMapper.selectById(inquiryOrder.getFid());
            if (existing == null) {
                return Result.error("询价单不存在");
            }

            // 更新主表
            int result = inquiryOrderMapper.updateById(inquiryOrder);

            if (result > 0) {
                log.info("更新询价单成功，单据编号: {}", inquiryOrder.getFbillno());

                // 更新详情数据（先删除后新增）
                if (inquiryOrder.getEntries() != null) {
                    inquiryOrderEntryMapper.deleteByParentId(inquiryOrder.getFid());
                    for (InquiryOrderEntry entry : inquiryOrder.getEntries()) {
                        entry.setFid(inquiryOrder.getFid());
                        entry.setFbillno(inquiryOrder.getFbillno());
                        entry.setId(UUID.randomUUID().toString());
                        inquiryOrderEntryMapper.insert(entry);
                    }
                    log.info("更新询价单详情 {} 条", inquiryOrder.getEntries().size());
                }

                return Result.success("更新询价单成功", inquiryOrder);
            } else {
                return Result.error("更新询价单失败");
            }
        } catch (Exception e) {
            log.error("更新询价单异常", e);
            return Result.error("更新询价单异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteInquiryOrder(String fid) {
        try {
            if (fid == null) {
                return Result.error("询价单ID不能为空");
            }

            // 查询原数据
            InquiryOrder existing = inquiryOrderMapper.selectById(fid);
            if (existing == null) {
                return Result.error("询价单不存在");
            }

            // 先删除明细数据
            inquiryOrderEntryMapper.deleteByParentId(fid);

            // 再删除主表数据
            int result = inquiryOrderMapper.deleteById(fid);

            if (result > 0) {
                log.info("删除询价单成功，单据编号: {}", existing.getFbillno());
                return Result.success("删除询价单成功");
            } else {
                return Result.error("删除询价单失败");
            }
        } catch (Exception e) {
            log.error("删除询价单异常", e);
            return Result.error("删除询价单异常: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        if (executorService != null && !executorService.isShutdown()) {
            try {
                // 优雅关闭线程池
                executorService.shutdown();
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                    if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                        log.error("线程池未能正常关闭");
                    }
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
