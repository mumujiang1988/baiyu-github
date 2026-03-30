package com.ruoyi.business.k3.service.impl;

import com.ruoyi.business.dto.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.entity.*;
import com.ruoyi.business.k3.service.SalesPriceService;

import com.ruoyi.business.k3.util.K3DataUtils;
import com.ruoyi.business.mapper.*;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.satoken.utils.LoginHelper;
import com.ruoyi.system.domain.vo.SysUserVo;
import com.ruoyi.system.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SalesPriceServiceImpl implements SalesPriceService {

    @Autowired
    private SalesPriceMapper salesPriceMapper;

    @Autowired
    private SalesPriceItemMapper itemMapper;

    @Autowired
    private SalesPriceItemPackageMapper packageMapper;
    @Autowired
    private SaleChangeBillMapper saleChangeBillMapper;
    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SaleChangeDetailMapper saleChangeDetailMapper;

    @Autowired
    private MaterialMapper materialMapper;

    @Autowired
    private SupplierMapper supplierMapper;


    /**
     * 查询完整价目数据（包含价目明细）
     * 分步查询，避免笛卡尔积问题
     */
    @Override
    public SalesPrice getDetail(Long id) {
        // 1. 查询主表
        SalesPrice salesPrice = salesPriceMapper.selectPriceDetail(id);

        // 2. 查询价目明细列表
        List<SalesPriceItem> items = itemMapper.selectByPriceId(salesPrice.getFId());
        List<SalesPriceItemPackage> packageList = packageMapper.selectByPriceId(salesPrice.getFId());
        if (items != null && !items.isEmpty()){
            salesPrice.setItemList(items);

        }
        if (packageList != null && !packageList.isEmpty()){

            salesPrice.setItemPackageList(packageList);
        }

        return salesPrice;
    }

    /**
     * 保存销售价目（强事务保证）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(SalesPrice price) {

        // 1.保存主表
        salesPriceMapper.insertSalesPrice(price);

        // 2.保存价目明细
        for (SalesPriceItem item : price.getItemList()) {
            item.setPriceId(price.getId());
        }
        itemMapper.batchInsertItem(price.getItemList());
    }

    /**
     * 同步金蝶销售价目表数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean syncSalesPriceData(List<List<Object>> querySalesPriceList,
                                      List<List<Object>> querySalesPriceItemList,
                                      List<List<Object>> querySalesPriceItemPackageList) {
        try {
            // 1. 解析主表数据
            List<SalesPrice> salesPriceList = parseSalesPriceList(querySalesPriceList);
            log.info("解析销售价目表主表数据，共{}条", salesPriceList.size());

            // 2. 解析物料明细数据
            Map<Long, List<SalesPriceItem>> itemMap = parseSalesPriceItemMap(querySalesPriceItemList);
            log.info("解析销售价目表物料明细数据，共{}个K3主键", itemMap.size());

            // 3. 解析包材明细数据
            Map<Long, List<SalesPriceItemPackage>> packageMap = parseSalesPricePackageMap(querySalesPriceItemPackageList);
            log.info("解析销售价目表包材明组数据，共{}个K3主键", packageMap.size());

            // 4. 批量查询现有数据
            List<String> fNumbers = salesPriceList.stream()
                .map(SalesPrice::getFNumber)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            Map<String, SalesPrice> existingPriceMap = new HashMap<>();
            if (!fNumbers.isEmpty()) {
                List<SalesPrice> existingList = salesPriceMapper.selectByFNumbers(fNumbers);
                existingPriceMap = existingList.stream()
                    .collect(Collectors.toMap(SalesPrice::getFNumber, p -> p, (a, b) -> a));
            }
            log.info("查询到{}条现有主表数据", existingPriceMap.size());

            // 4. 同步主表数据
            Map<Long, Long> k3IdToPriceIdMap = new HashMap<>();

            for (SalesPrice salesPrice : salesPriceList) {
                String fNumber = salesPrice.getFNumber();
                Long k3Id = salesPrice.getFId();

                SalesPrice existing = existingPriceMap.get(fNumber);
                if (existing != null) {
                    salesPriceMapper.updateSalesPrice(salesPrice);
                    k3IdToPriceIdMap.put(k3Id, existing.getFId());
                } else {
                    salesPriceMapper.insertSalesPrice(salesPrice);
                    k3IdToPriceIdMap.put(k3Id, salesPrice.getFId());
                }
            }
            log.info("同步主表{}条数据", salesPriceList.size());

            // 5. 同步物料明细
            List<SalesPriceItem> itemsToInsert = new ArrayList<>();
            List<SalesPriceItem> itemsToUpdate = new ArrayList<>();

            for (Map.Entry<Long, List<SalesPriceItem>> entry : itemMap.entrySet()) {
                Long k3Id = entry.getKey();
                Long priceId = k3IdToPriceIdMap.get(k3Id);
                if (priceId == null) {
                    continue;
                }

                // 查询该价目表下的现有明细
                List<SalesPriceItem> existingItems = itemMapper.selectByPriceId(priceId);
                Map<String, SalesPriceItem> existingItemMap = existingItems.stream()
                    .collect(Collectors.toMap(SalesPriceItem::getFMaterialId, i -> i, (a, b) -> a));

                for (SalesPriceItem item : entry.getValue()) {
                    item.setPriceId(priceId);
                    SalesPriceItem existing = existingItemMap.get(item.getFMaterialId());

                    if (existing != null) {
                        item.setId(existing.getId());
                        itemsToUpdate.add(item);
                    } else {
                        itemsToInsert.add(item);
                    }
                }
            }

            // 批量新增
            if (!itemsToInsert.isEmpty()) {
                int batchSize = 2000;
                for (int i = 0; i < itemsToInsert.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, itemsToInsert.size());
                    itemMapper.batchInsertItem(itemsToInsert.subList(i, end));
                }
                log.info("批量新增物料明细{}条", itemsToInsert.size());
            }

            // 批量更新
            if (!itemsToUpdate.isEmpty()) {
                for (SalesPriceItem item : itemsToUpdate) {
                    itemMapper.updateItem(item);
                }
            }
            log.info("同步物料明细完成");

            // 6. 同步包材明细
            List<SalesPriceItemPackage> packagesToInsert = new ArrayList<>();
            List<SalesPriceItemPackage> packagesToUpdate = new ArrayList<>();

            for (Map.Entry<Long, List<SalesPriceItemPackage>> entry : packageMap.entrySet()) {
                Long k3Id = entry.getKey();
                Long priceId = k3IdToPriceIdMap.get(k3Id);
                if (priceId == null) {
                    continue;
                }

                // 查询该价目表下的现有包材明细
                List<SalesPriceItemPackage> existingPackages = packageMapper.selectByPriceId(priceId);
                Map<String, SalesPriceItemPackage> existingPackageMap = existingPackages.stream()
                    .filter(p -> p.getFBzbm() != null)
                    .collect(Collectors.toMap(SalesPriceItemPackage::getFBzbm, p -> p, (a, b) -> a));

                for (SalesPriceItemPackage pkg : entry.getValue()) {
                    pkg.setPriceId(priceId);
                    if (pkg.getFBzbm() != null) {
                        SalesPriceItemPackage existing = existingPackageMap.get(pkg.getFBzbm());

                        if (existing != null) {
                            pkg.setId(existing.getId());
                            packagesToUpdate.add(pkg);
                        } else {
                            packagesToInsert.add(pkg);
                        }
                    } else {
                        packagesToInsert.add(pkg);
                    }
                }
            }

            // 批量新增
            if (!packagesToInsert.isEmpty()) {
                int batchSize = 2000;
                for (int i = 0; i < packagesToInsert.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, packagesToInsert.size());
                    packageMapper.batchInsertPackage(packagesToInsert.subList(i, end));
                }
                log.info("批量新增包材明细{}条", packagesToInsert.size());
            }

            // 批量更新 - 改为逐条更新避免SQL语法错误
            if (!packagesToUpdate.isEmpty()) {
                for (SalesPriceItemPackage pkg : packagesToUpdate) {
                    packageMapper.updatePackage(pkg);
                }
                log.info("批量更新包材明细{}条", packagesToUpdate.size());
            }
            log.info("同步包材明细完成");

            log.info("销售价目表数据同步完成");
            return true;
        } catch (Exception e) {
            log.error("同步销售价目表数据失败", e);
            throw new RuntimeException("同步销售价目表数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析销售价目表主表数据
     */
    private List<SalesPrice> parseSalesPriceList(List<List<Object>> dataList) {
        List<SalesPrice> result = new ArrayList<>();
        for (List<Object> rowData : dataList) {
            if (rowData == null || rowData.isEmpty()) {
                continue;
            }

            SalesPrice salesPrice = new SalesPrice();
            salesPrice.setFName(getString(rowData, 0));
            salesPrice.setFNumber(getString(rowData, 1));
            salesPrice.setFCreatorId(getString(rowData, 2));
            salesPrice.setFDescription(getString(rowData, 3));
            salesPrice.setFCurrencyId(getString(rowData, 4));
            salesPrice.setFEffectiveDate(getLocalDate(rowData, 5));
            salesPrice.setFExpiryDate(getLocalDate(rowData, 6));
            salesPrice.setFPriceObject(getString(rowData, 7));
            salesPrice.setFLimitCustomer(getString(rowData, 8));
            salesPrice.setFLimitSalesMan(getString(rowData, 9));
            salesPrice.setFPriceType(getString(rowData, 10));
            salesPrice.setFXsy(getString(rowData, 11));
            salesPrice.setFKhjc(getString(rowData, 12));
            salesPrice.setFCreateDate(getLocalDateTime(rowData, 13));
            salesPrice.setFModifyDate(getLocalDateTime(rowData, 14));
            salesPrice.setFModifierId(getString(rowData, 15));
            salesPrice.setFId(getLong(rowData, 16)); // K3系统ID
            salesPrice.setFCreateDate(getLocalDateTime(rowData, 17)); // 创建日期

            result.add(salesPrice);
        }
        return result;
    }

    /**
     * 解析销售价目表物料明细数据（按K3主FID分组）
     */
    private Map<Long, List<SalesPriceItem>> parseSalesPriceItemMap(List<List<Object>> dataList) {
        Map<Long, List<SalesPriceItem>> result = new HashMap<>();
        for (List<Object> rowData : dataList) {
            if (rowData == null || rowData.isEmpty()) {
                continue;
            }

            Long k3Id = getLong(rowData, 0); // K3主表FID
            if (k3Id == null) {
                continue;
            }

            SalesPriceItem item = new SalesPriceItem();
            item.setFMaterialId(getString(rowData, 1));      // 物料编码
            item.setFKhhh(getString(rowData, 2));            // 客户货号
            item.setFPrice(getBigDecimal(rowData, 3));       // 价格
            item.setFBzyq(getString(rowData, 4));            // 包装要求
            item.setFBzmx(getString(rowData, 5));            // 售后调整
            item.setFYwpm(getString(rowData, 6));            // 英文品名
            item.setFzxzlxgrq(getLocalDate(rowData, 7));     // 装箱资料修改日期
            item.setFC(getBigDecimal(rowData, 8));          // 长
            item.setFK(getBigDecimal(rowData, 9));          // 宽
            item.setFG(getBigDecimal(rowData, 10));          // 高
            item.setFMz(getBigDecimal(rowData, 11));         // 毛重
            item.setFCptp2(getString(rowData, 12));          // 产品图片2
            item.setFCptp3(getString(rowData, 13));          // 产品图片3
            item.setFJgdztp(getString(rowData, 14));         // 激光打字图片
            item.setFWxtm(getString(rowData, 15));           // 外箱条码
            item.setFWxzm(getString(rowData, 16));           // 外箱正唛
            item.setFWxcm(getString(rowData, 17));           // 外箱侧唛
            item.setFNxzm(getString(rowData, 18));           // 内箱正唛
            item.setFNxcm(getString(rowData, 19));           // 内箱侧唛
            item.setFCtt(getString(rowData, 20));            // 彩贴图
            item.setFSmst(getString(rowData, 21));           // 说明书图
            item.setFCtaot(getString(rowData, 22));          // 彩套图
            item.setFCht(getString(rowData, 23));            // 彩盒图
            item.setFDkczz(getString(rowData, 24));          // 单款称重照
            item.setFZxczz(getString(rowData, 25));          // 整箱称重照
            item.setFtm(getString(rowData, 26));             // 条码
            item.setFBzt(getString(rowData, 27));            // 包装图
            item.setFGys(getString(rowData, 28));            // 供应商
            item.setFJz(getBigDecimal(rowData, 29));         // 净重
            item.setFbgrq(getLocalDate(rowData, 30));        // 变更日期
            item.setFYwbzfs(getString(rowData, 31));         // 英文包装方式

            result.computeIfAbsent(k3Id, k -> new ArrayList<>()).add(item);
        }
        return result;
    }

    /**
     * 解析销售价目表包材明细数据（按K3主FID分组）
     */
    private Map<Long, List<SalesPriceItemPackage>> parseSalesPricePackageMap(List<List<Object>> dataList) {
        Map<Long, List<SalesPriceItemPackage>> result = new HashMap<>();
        for (List<Object> rowData : dataList) {
            if (rowData == null || rowData.isEmpty()) {
                continue;
            }

            Long k3Id = getLong(rowData, 0); // K3主表FID
            if (k3Id == null) {
                continue;
            }

            SalesPriceItemPackage pkg = new SalesPriceItemPackage();
            pkg.setPriceId(getLong(rowData, 0));
            pkg.setFBzbm(getString(rowData, 1));
            pkg.setFBzmc(getString(rowData, 2));
            pkg.setFBzgg(getString(rowData, 3));
            pkg.setFDw(getString(rowData, 4));
            pkg.setFYl(getInteger(rowData, 5));
            pkg.setFBz(getString(rowData, 6));
            pkg.setFTp(getString(rowData, 7));
            pkg.setFBcfwgys(getString(rowData, 8));
            pkg.setFCpgys(getString(rowData, 9));
            pkg.setFCtyBaseProperty(getInteger(rowData, 10));

            result.computeIfAbsent(k3Id, k -> new ArrayList<>()).add(pkg);
        }
        return result;
    }

    // 工具方法
    private String getString(List<Object> rowData, int index) {
        return K3DataUtils.getString(rowData, index);
    }

    private Long getLong(List<Object> rowData, int index) {
        return K3DataUtils.getLong(rowData, index);
    }

    private Integer getInteger(List<Object> rowData, int index) {
        return K3DataUtils.getInteger(rowData, index);
    }

    private BigDecimal getBigDecimal(List<Object> rowData, int index) {
        return K3DataUtils.getBigDecimal(rowData, index);
    }

    private LocalDate getLocalDate(List<Object> rowData, int index) {
        return K3DataUtils.getLocalDate(rowData, index);
    }

    private LocalDateTime getLocalDateTime(List<Object> rowData, int index) {
        return K3DataUtils.getLocalDateTime(rowData, index);
    }
    /**
     * 同步金蝶销售价目表变更表数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean syncSaleChangeBillData(List<List<Object>> querySalesPriceAlterationList) {
        try {
            if (querySalesPriceAlterationList == null || querySalesPriceAlterationList.isEmpty()) {
                log.warn("销售价目表变更数据为空");
                return true;
            }

            // 1. 解析数据
            List<SaleChangeBillFlat> billHeaders = new ArrayList<>();
            List<SaleChangeDetail> billDetails = new ArrayList<>();

            // 按FID分组数据，每个FID可能有多行数据（代表多个明细）
            Map<Long, List<List<Object>>> groupedData = querySalesPriceAlterationList.stream()
                .collect(Collectors.groupingBy(row -> K3DataUtils.getLong(row, 0))); // FID在索引0

            for (Map.Entry<Long, List<List<Object>>> entry : groupedData.entrySet()) {
                List<List<Object>> rows = entry.getValue();

                // 创建表头记录（每个FID一个表头）
                SaleChangeBillFlat header = parseBillHeader(rows.get(0)); // 使用第一行的数据创建表头
                billHeaders.add(header);

                // 创建明细记录（每个FID可能有多行明细）
                for (List<Object> row : rows) {
                    SaleChangeDetail detail = parseBillDetail(row);
                    billDetails.add(detail);
                }
            }

            log.info("解析销售价目表变更数据，共{}个表头，{}个明细", billHeaders.size(), billDetails.size());

            if (billHeaders.isEmpty()) {
                return true;
            }

            // 2. 批量查询现有数据（根据FID）
            List<Long> fids = billHeaders.stream()
                .map(SaleChangeBillFlat::getFid)
                .distinct()
                .collect(Collectors.toList());

            Map<Long, SaleChangeBillFlat> existingMap = new HashMap<>();
            for (Long fid : fids) {
                SaleChangeBillFlat existing = saleChangeBillMapper.selectLatestByFid(fid);
                if (existing != null) {
                    existingMap.put(fid, existing);
                }
            }
            log.info("查询到{}条现有表头数据", existingMap.size());

            // 3. 分离新增和更新数据
            List<SaleChangeBillFlat> toInsertHeaders = new ArrayList<>();
            List<SaleChangeBillFlat> toUpdateHeaders = new ArrayList<>();

            for (SaleChangeBillFlat header : billHeaders) {
                if (existingMap.containsKey(header.getFid())) {
                    toUpdateHeaders.add(header);
                } else {
                    toInsertHeaders.add(header);
                }
            }

            // 4. 批量处理表头数据
            //  先删除已存在的表头对应的明细
            for (SaleChangeBillFlat header : toUpdateHeaders) {
                // 删除与源单编号相关的明细
                List<SaleChangeDetail> existingDetails = saleChangeDetailMapper.selectBySourceBillNo(header.getBillNo());
                for (SaleChangeDetail detail : existingDetails) {
                    saleChangeDetailMapper.deleteBySourceBillNo(detail.getSourceBillNo());
                }
            }

            //
            if (!toInsertHeaders.isEmpty()) {
                int batchSize = 2000;
                for (int i = 0; i < toInsertHeaders.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, toInsertHeaders.size());
                    saleChangeBillMapper.batchInsert(toInsertHeaders.subList(i, end));
                }
                log.info("新增{}条销售价目表变更表头数据", toInsertHeaders.size());
            }

            // 更新已有的表头
            if (!toUpdateHeaders.isEmpty()) {
                for (SaleChangeBillFlat header : toUpdateHeaders) {
                    saleChangeBillMapper.update(header);
                }
                log.info("更新{}条销售价目表变更表头数据", toUpdateHeaders.size());
            }

            //  处理明细数据
            if (!billDetails.isEmpty()) {
                int batchSize = 2000;
                for (int i = 0; i < billDetails.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, billDetails.size());
                    saleChangeDetailMapper.batchInsert(billDetails.subList(i, end));
                }
                log.info("插入{}条销售价目表变更明细数据", billDetails.size());
            }

            log.info("同步销售价目表变更数据完成");
            return true;

        } catch (Exception e) {
            log.error("同步销售价目表变更数据失败", e);
            throw new RuntimeException("同步销售价目表变更数据失败", e);
        }
    }

    /**
     * 解析表头数据
     */
    private SaleChangeBillFlat parseBillHeader(List<Object> row) {
        SaleChangeBillFlat header = new SaleChangeBillFlat();

        header.setFid(K3DataUtils.getLong(row, 0));                    // FID
        header.setCustomerShortName(K3DataUtils.getString(row, 1));    // Fkhjc
        header.setBillNo(K3DataUtils.getString(row, 2));             // FBillNo
        header.setBillDate(K3DataUtils.getLocalDate(row, 3));        // F_ora_Date
        header.setCreatorId(K3DataUtils.getString(row, 4));          // FCreatorId
        header.setModifierId(K3DataUtils.getString(row, 5));         // FModifierId
        header.setModifyDate(K3DataUtils.getLocalDateTime(row, 6));  // FModifyDate
        header.setAuditor(K3DataUtils.getString(row, 7));  // F_shr
        header.setAuditDate(K3DataUtils.getLocalDateTime(row, 8));  // F_shrq
        header.setCreateDate(K3DataUtils.getLocalDateTime(row, 29)); // FCreateDate
        header.setDocumentStatus(K3DataUtils.getString(row, 30)); // FDocumentStatus

        return header;
    }

    /**
     * 解析明细数据
     */
    private SaleChangeDetail parseBillDetail(List<Object> row) {
        SaleChangeDetail detail = new SaleChangeDetail();

        detail.setSourceBillNo(K3DataUtils.getString(row, 9));       // Fkhjc（客户简称）
        detail.setSourceBillType(K3DataUtils.getString(row, 10));    // F_ora_SourceBillType
        detail.setProductCode(K3DataUtils.getString(row, 11));       // Fcpbm
        detail.setProductName(K3DataUtils.getString(row, 12));       // 产品名称
        detail.setSpecModel(K3DataUtils.getString(row, 13));         // 规格型号
        detail.setOldSupplier(K3DataUtils.getString(row, 14));       // Fjgys (旧供应商)
        detail.setNewSupplier(K3DataUtils.getString(row, 15));       // Fxgys (新供应商)
        detail.setChangeReason(K3DataUtils.getString(row, 16));      // Fbgyy (变更原因)
        detail.setOldGrossWeight(K3DataUtils.getBigDecimal(row, 17)); // Fmz (旧毛重)
        detail.setOldNetWeight(K3DataUtils.getBigDecimal(row, 18));  // Fjz (旧净重)
        detail.setOldLength(K3DataUtils.getBigDecimal(row, 19));     // F_c (旧长)
        detail.setOldWidth(K3DataUtils.getBigDecimal(row, 20));      // F_k (旧宽)
        detail.setOldHeight(K3DataUtils.getBigDecimal(row, 21));     // F_g (旧高)
        detail.setOldBoxQty(K3DataUtils.getInteger(row, 22));        // Fzxs (旧装箱数)
        detail.setNewPackageReq(K3DataUtils.getString(row, 23));     // Fxbzyq (新包装要求)
        detail.setNewGrossWeight(K3DataUtils.getBigDecimal(row, 24)); // 对应新毛重
        detail.setNewNetWeight(K3DataUtils.getBigDecimal(row, 25));     // 对应新净重
        detail.setNewWidth(K3DataUtils.getBigDecimal(row, 26));     // 对应新宽
        detail.setNewHeight(K3DataUtils.getBigDecimal(row, 27));     // 对应新高
        detail.setNewBoxQty(K3DataUtils.getInteger(row, 28));       // 对应新装箱数

        return detail;
    }

    @Override
    public SaleChangeBillFlat selectById(Long id) {

        SaleChangeBillFlat saleChangeDetail =saleChangeBillMapper.selectById(id);
        if (saleChangeDetail != null) {
            saleChangeDetail.setSaleChangeDetails(saleChangeDetailMapper.selectBySourceBillNo(saleChangeDetail.getCustomerShortName()));
        }
        return saleChangeDetail;
    }
    /**
     * 分页查询价目列表
     */
    @Override
    public Page<SalesPrice> listSalesPrices(SalesPrice price, int page, int size) {
        try {
            int offset = (page - 1) * size;

            // 调用 Mapper 层的分页查询
            List<SalesPrice> records = salesPriceMapper.selectByCondition(offset, size, price);
            long total = salesPriceMapper.countByCondition(price);

            // 查询每个价目的明细和包材信息
            if (!records.isEmpty()) {
                for (SalesPrice record : records) {
                    // 查询物料明细
                    List<SalesPriceItem> items = itemMapper.selectByPriceId(record.getFId());
                    if (items != null && !items.isEmpty()) {
                        record.setItemList(items);
                    }

                    // 查询包材明细
                    List<SalesPriceItemPackage> packageList = packageMapper.selectByPriceId(record.getFId());
                    if (packageList != null && !packageList.isEmpty()) {
                        record.setItemPackageList(packageList);
                    }
                }
            }

            Page<SalesPrice> result = Page.of(page, size, total);
            result.setRecords(records);
            return result;
        } catch (Exception e) {
            log.error("查询价目列表失败", e);
            return Page.of(0, size);
        }
    }

    /**
     * 新增销售价目表变更表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addSaleChangeBill(SaleChangeBillFlat billFlat) {
        try {
            if (billFlat == null) {
                return Result.error("销售价目表变更表数据为空");
            }
            if (billFlat.getSaleChangeDetails()== null){
                return Result.error("销售价目表变更单详情数据数据为空");

            }
            Long userId = LoginHelper.getUserId();
            SysUserVo user  = sysUserMapper.selectVoById(userId);
            billFlat.setCreatorId(user.getK3Key());
            billFlat.setDocumentStatus("A");

            //  插入主表数据
            int headerResult = saleChangeBillMapper.insert(billFlat);
            if (headerResult <= 0) {
                log.error("新增销售价目表变更主表失败");
                return Result.error("新增销售价目表变更主表失败");
            }

            // 创建DTO用于发送审批消息
            SaleChangeBillFlatDto saleChangeBillFlatDto = new SaleChangeBillFlatDto();
            BeanUtils.copyProperties(billFlat, saleChangeBillFlatDto);
            saleChangeBillFlatDto.setCreatorId(user.getUserName());

            // 创建明细DTO集合
            List<SaleChangeDetailDto> detailDtoList = new ArrayList<>();

            // 校验明细表数据
            if (billFlat.getSaleChangeDetails() != null && !billFlat.getSaleChangeDetails().isEmpty()) {
                for (SaleChangeDetail detail : billFlat.getSaleChangeDetails()) {
                    // 为每一条明细创建DTO
                    SaleChangeDetailDto saleChangeDetailDto = new SaleChangeDetailDto();
                    BeanUtils.copyProperties(detail, saleChangeDetailDto);

                    // 校验产品编码（物料ID）
                    if (detail.getProductCode() != null) {
                        Bymaterial material = materialMapper.selectById(Long.valueOf(detail.getProductCode()));
                        if (material == null) {
                            log.error("产品编码不存在：{}", detail.getProductCode());
                            throw new RuntimeException("产品编码不存在：" + detail.getProductCode());
                        }
                        saleChangeDetailDto.setProductCode(material.getNumber());
                    }

                    // 校验旧供应商ID
                    if (detail.getOldSupplier() != null) {
                        Supplier oldSupplier = supplierMapper.supplierID(detail.getOldSupplier());
                        if (oldSupplier == null) {
                            log.error("旧供应商不存在：{}", detail.getOldSupplier());
                            throw new RuntimeException("旧供应商不存在：" + detail.getOldSupplier());
                        }
                        saleChangeDetailDto.setOldSupplier(oldSupplier.getName());
                    }

                    // 校验新供应商ID
                    if (detail.getNewSupplier() != null) {
                        Supplier newSupplier = supplierMapper.supplierID(detail.getNewSupplier());
                        if (newSupplier == null) {
                            log.error("新供应商不存在：{}", detail.getNewSupplier());
                            throw new RuntimeException("新供应商不存在：" + detail.getNewSupplier());
                        }
                        saleChangeDetailDto.setNewSupplier(newSupplier.getName());
                    }

                    // 将处理好的DTO添加到集合
                    detailDtoList.add(saleChangeDetailDto);
                }

                //  插入明细表数据
                saleChangeDetailMapper.batchInsert(billFlat.getSaleChangeDetails());
            }

            // 将明细DTO集合设置到主表DTO
            saleChangeBillFlatDto.setSaleChangeDetails(detailDtoList);

            log.info("新增销售价目表变更表成功");
            return Result.success("新增销售价目表变更表成功");
        } catch (Exception e) {
            log.error("新增销售价目表变更表失败", e);
            throw new RuntimeException("新增销售价目表变更表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 修改销售价目表变更表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSaleChangeBill(SaleChangeBillFlat billFlat) {
        try {
            if (billFlat == null || billFlat.getId() == null) {
                log.warn("销售价目表变更表数据为空或缺少ID");
                return false;
            }

            // 1. 查询现有记录
            SaleChangeBillFlat existing = saleChangeBillMapper.selectById(billFlat.getId());
            if (existing == null) {
                log.error("销售价目表变更表不存在，ID: {}", billFlat.getId());
                return false;
            }

            // 2. 更新表头数据
            int headerResult = saleChangeBillMapper.update(billFlat);
            if (headerResult <= 0) {
                log.error("更新销售价目表变更表头失败");
                return false;
            }
            log.info("更新销售价目表变更表头成功，单据编号：{}", billFlat.getBillNo());

            // 3. 处理明细数据：先删除再插入
            if (existing.getCustomerShortName() != null) {
                saleChangeDetailMapper.deleteBySourceBillNo(existing.getCustomerShortName());
                log.info("删除旧明细数据，客户简称：{}", existing.getCustomerShortName());
            }

            // 校验明细表数据
            if (billFlat.getSaleChangeDetails() != null && !billFlat.getSaleChangeDetails().isEmpty()) {
                for (SaleChangeDetail detail : billFlat.getSaleChangeDetails()) {
                    // 校验产品编码（物料ID）
                    if (detail.getProductCode() != null) {
                        Bymaterial material = materialMapper.selectById(Long.valueOf(detail.getProductCode()));
                        if (material == null) {
                            log.error("产品编码不存在：{}", detail.getProductCode());
                            throw new RuntimeException("产品编码不存在：" + detail.getProductCode());
                        }
                    }

                    // 校验旧供应商ID
                    if (detail.getOldSupplier() != null) {
                        Supplier oldSupplier = supplierMapper.supplierID(detail.getOldSupplier());
                        if (oldSupplier == null) {
                            log.error("旧供应商不存在：{}", detail.getOldSupplier());
                            throw new RuntimeException("旧供应商不存在：" + detail.getOldSupplier());
                        }
                    }

                    // 校验新供应商ID
                    if (detail.getNewSupplier() != null) {
                        Supplier newSupplier = supplierMapper.supplierID(detail.getNewSupplier());
                        if (newSupplier == null) {
                            log.error("新供应商不存在：{}", detail.getNewSupplier());
                            throw new RuntimeException("新供应商不存在：" + detail.getNewSupplier());
                        }
                    }
                }

                int detailResult = saleChangeDetailMapper.batchInsert(billFlat.getSaleChangeDetails());
                log.info("插入新明细数据{}条", detailResult);
            }

            log.info("修改销售价目表变更表成功");
            return true;
        } catch (Exception e) {
            log.error("修改销售价目表变更表失败", e);
            throw new RuntimeException("修改销售价目表变更表失败: " + e.getMessage(), e);
        }
    }
}
