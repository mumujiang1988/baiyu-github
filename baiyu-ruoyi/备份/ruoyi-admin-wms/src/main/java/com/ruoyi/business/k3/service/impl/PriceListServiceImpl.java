package com.ruoyi.business.k3.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;


import com.ruoyi.business.Component.K3FormProcessorFactory;
import com.ruoyi.business.Component.PriceListFormProcessor;
import com.lark.oapi.service.vc.v1.model.Material;
import com.ruoyi.business.entity.Bymaterial;
import com.ruoyi.business.entity.PriceList;
import com.ruoyi.business.entity.PriceListEntry;

import com.ruoyi.business.feishu.config.BatchGetEmployeeConfig;
import com.ruoyi.business.k3.config.AbstractK3FormProcessor;
import com.ruoyi.business.k3.config.k3config;
import com.ruoyi.business.k3.domain.bo.PriceListBo;
import com.ruoyi.business.k3.service.PriceListService;
import com.ruoyi.business.mapper.MaterialMapper;
import com.ruoyi.business.mapper.PriceListEntryMapper;
import com.ruoyi.business.mapper.PriceListMapper;
import com.ruoyi.business.mapper.SupplierMapper;
import com.ruoyi.business.util.MinioUtil;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 价目表服务实现类
 */
@Service
@Slf4j
public class PriceListServiceImpl implements PriceListService {

    @Resource
    private PriceListMapper priceListMapper;
    @Resource
    private SupplierMapper supplierMapper;

    @Resource
    private PriceListEntryMapper priceListEntryMapper;
    @Resource
    private MaterialMapper materialMapper;
    @Resource
    private MinioUtil minioUtil;
    @Resource
    private BatchGetEmployeeConfig batchGetEmployeeConfig;
    @Resource
    private k3config k3configks;
    @Resource
    private K3FormProcessorFactory k3FormProcessorFactory;


    @Override
    public void syncPriceList(List<List<Object>> PriceLarsList) {

        for (List<Object> list : PriceLarsList) {
            PriceList contactBase = new PriceList();

            contactBase.setPriceListId(Long.valueOf(list.get(0) != null ? list.get(0).toString() : null));
            contactBase.setFName(list.get(1) != null ? list.get(1).toString() : null);
            contactBase.setFNumber(list.get(2) != null ? list.get(2).toString() : null);
            contactBase.setFDescription(list.get(3) != null ? list.get(3).toString() : null);
            contactBase.setFCurrencyID(list.get(4) != null ? list.get(4).toString() : null);
            contactBase.setFSupplierID(list.get(5) != null ? list.get(5).toString() : null);
            contactBase.setFGYSLB(list.get(6) != null ? list.get(6).toString() : null);
            contactBase.setFPricer(list.get(7) != null ? list.get(7).toString() : null);
            contactBase.setFPriceObject(list.get(8) != null ? list.get(8).toString() : null);
            contactBase.setFPriceType(list.get(9) != null ? list.get(9).toString() : null);
            contactBase.setCreatedBy(list.get(10) != null ? list.get(10).toString() : null);

            PriceList result =   priceListMapper.selectByNumber(contactBase.getFNumber());
            if (result != null) {
                // 已存在 → 更新
                priceListMapper.updatePriceList(contactBase);
            } else {
                // 不存在 → 新增
               priceListMapper.insertPriceList(contactBase);
            }
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncPriceListEntry(List<List<Object>> priceParticularsList) {
        for (List<Object> objectList : priceParticularsList) {
            PriceListEntry priceParticulars = new PriceListEntry();
            priceParticulars.setPriceListNumber(objectList.get(0) != null ? objectList.get(0).toString() : null);
            priceParticulars.setFPrice(Double.valueOf(objectList.get(1) != null ? objectList.get(1).toString() : null));
            priceParticulars.setFTaxPrice(Double.valueOf(objectList.get(2) != null ? objectList.get(2).toString() : null));
            priceParticulars.setFMaterialId(objectList.get(3) != null ? objectList.get(3).toString() : null);
            priceParticulars.setFMaterialName(objectList.get(4) != null ? objectList.get(4).toString() : null);
            priceParticulars.setFGCHH(objectList.get(5) != null ? objectList.get(5).toString() : null);
            priceParticulars.setFctyBaseProperty(objectList.get(6) != null ? objectList.get(6).toString() : null);
            priceParticulars.setFGGSM(objectList.get(7) != null ? objectList.get(7).toString() : null);
            priceParticulars.setFNote(objectList.get(8) != null ? objectList.get(8).toString() : null);
            priceParticulars.setFTaxRate(Double.valueOf(objectList.get(9) != null ? objectList.get(9).toString() : null));
            priceParticulars.setF100t5(Double.valueOf(objectList.get(10) != null ? objectList.get(10).toString() : null));
            priceParticulars.setFUnitID(objectList.get(11) != null ? objectList.get(11).toString() : null);
            priceParticulars.setFPriceCoefficient(Double.valueOf(objectList.get(12) != null ? objectList.get(12).toString() : null));
            priceParticulars.setFDownPrice(Double.valueOf(objectList.get(13) != null ? objectList.get(13).toString() : null));
            priceParticulars.setFUpPrice(Double.valueOf(objectList.get(14) != null ? objectList.get(14).toString() : null));
            priceParticulars.setFEntryEffectiveDate(objectList.get(15) != null ? objectList.get(15).toString() : null);
            priceParticulars.setFEntryExpiryDate(objectList.get(16) != null ? objectList.get(16).toString() : null);
            priceParticulars.setFWBZC(Double.valueOf(objectList.get(17) != null ? objectList.get(17).toString() : null));
            priceParticulars.setFWBZG(Double.valueOf(objectList.get(18) != null ? objectList.get(18).toString() : null));
            priceParticulars.setFWBZTJ(Double.valueOf(objectList.get(19) != null ? objectList.get(19).toString() : null));
            priceParticulars.setFWBZSL(Double.valueOf(objectList.get(20) != null ? objectList.get(20).toString() : null));
            priceParticulars.setFWBZDW(objectList.get(21) != null ? objectList.get(21).toString() : null);
            priceParticulars.setFMZ(Double.valueOf(objectList.get(22) != null ? objectList.get(22).toString() : null));
            priceParticulars.setFJZ(Double.valueOf(objectList.get(23) != null ? objectList.get(21).toString() : null));
            priceParticulars.setFBCGG(objectList.get(24) != null ? objectList.get(22).toString() : null);
            priceParticulars.setFbzsm(objectList.get(25) != null ? objectList.get(25).toString() : null);
            priceParticulars.setFRECENTDATE(objectList.get(26) != null ? objectList.get(26).toString() : null);
            priceParticulars.setFcgshys(objectList.get(27) != null ? objectList.get(27).toString() : null);
            priceParticulars.setFdzshqdl(objectList.get(28) != null ? objectList.get(28).toString() : null);
            priceParticulars.setFZXSMS(objectList.get(29) != null ? objectList.get(29).toString() : null);
            priceParticulars.setFZXSMSTP(objectList.get(30) != null ? objectList.get(30).toString() : null);
            priceParticulars.setFkppm(objectList.get(31) != null ? objectList.get(31).toString() : null);
            priceParticulars.setFxjr(objectList.get(32) != null ? objectList.get(32).toString() : null);
            priceParticulars.setFsfyyywxj(parseIntSafe(objectList.get(33)).toString());
            priceParticulars.setFcpzlyq(objectList.get(34) != null ? objectList.get(34).toString() : null);
            priceParticulars.setFQDL(Integer.valueOf(objectList.get(35) != null ? objectList.get(35).toString() : null));

            PriceListEntry result =   priceListEntryMapper.selectMaterialId(priceParticulars.getFMaterialId());
            if (result != null) {
                // 已存在 → 更新
                priceListEntryMapper.updateEntry(priceParticulars);
            } else {
                // 不存在 → 新增
                priceListEntryMapper.insertEntry(priceParticulars);
            }
        }

    }
    private Integer parseIntSafe(Object obj) {
        if (obj == null) return 0;

        String s = String.valueOf(obj)
                .replaceAll("\\s+", "")   // 去掉各种空白，包括全角空格
                .trim();

        if (s.isEmpty()) return 0;

        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result save(PriceList priceList) {
        try {
            // 1. 校验必填字段
            if (priceList.getFName() == null || priceList.getFName().isEmpty()) {
                return Result.error("价目表名称不能为空");
            }
            if (priceList.getFCurrencyID() == null) {
                return Result.error("币别不能为空");
            }
            if (priceList.getFPriceObject() == null || priceList.getFPriceObject().isEmpty()) {
                return Result.error("价目表对象不能为空");
            }
            if (priceList.getFPriceType() == null || priceList.getFPriceType().isEmpty()) {
                return Result.error("价格类型不能为空");
            }

            // 2. 检查编码是否已存在
            if (priceList.getFNumber() != null && !priceList.getFNumber().isEmpty()) {
                PriceList existing = priceListMapper.selectByNumber(priceList.getFNumber());
                if (existing != null) {
                    return Result.error("编码 " + priceList.getFNumber() + " 已存在");
                }
            }

            int result = priceListMapper.insertPriceList(priceList);
            if (result <= 0) {
                return Result.error("价目表保存失败");
            }

            // 5. 插入明细（如果存在）
            if (priceList.getEntries() != null && !priceList.getEntries().isEmpty()) {
                List<PriceListEntry> entryList = new ArrayList<>();
                for (PriceListEntry entry : priceList.getEntries()) {

                    entry.setPriceListNumber(priceList.getFNumber()); // 使用插入后生成的编码

                    // 校验明细必填字段
                    if (entry.getFPrice() == null) {
                        return Result.error("单价不能为空");
                    }
                    if (entry.getFTaxPrice() == null) {
                        return Result.error("含税单价不能为空");
                    }
                    if (entry.getFUnitID() == null) {
                        return Result.error("计价单位不能为空");
                    }
                    if (entry.getFEntryEffectiveDate() == null) {
                        return Result.error("生效日期不能为空");
                    }
                    if (entry.getFEntryExpiryDate() == null) {
                        return Result.error("价格有效期不能为空");
                    }
                    if (entry.getFbzsm() == null || entry.getFbzsm().isEmpty()) {
                        return Result.error("包装说明不能为空");
                    }

                    entryList.add(entry);
                }

                // 批量插入明细
                priceListEntryMapper.batchInsert(entryList);

                // 6. 使用工厂模式将采购价目表数据推送到金蝶系统
                    log.info("开始推送采购价目表到金蝶系统，价目表编号：{}", priceList.getFNumber());

                    AbstractK3FormProcessor<Object> processor = k3FormProcessorFactory.getProcessor("PUR_PriceCategory");
                    processor.processForm(null, priceList);
                    Result pushResult = processor.processForm(null, priceList);
                    if (pushResult.isEmpty()||!pushResult.isSuccess()){
                        log.error("物料推送金蝶失败: " + pushResult);
                        return Result.error("物料推送金蝶失败: ");
                    }
                    if (pushResult.isSuccess()) {
                        log.info("采购价目表推送金蝶成功，价目表编号：{}", priceList.getFNumber());

                        // 推送成功后发送飞书通知
                        Map<String, String> fields = new HashMap<>();
                        fields.put("采购价目名称", priceList.getFName());
                        fields.put("采购价目编码", priceList.getFNumber());
                        fields.put("提交人", priceList.getCreatedBy());

                        batchGetEmployeeConfig.sendCommonPushCard(
                            "采购价目表推送",
                            fields,
                            "http://113.46.194.126/k3cloud",
                            "打开金蝶系统"
                        );
                    } else {
                        log.warn("采购价目表推送金蝶失败，价目表编号：{}，原因：{}",
                            priceList.getFNumber());
                    }
            }

            return Result.success("价目表保存成功", priceList);
        } catch (Exception e) {
            throw new RuntimeException("保存价目表失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result update(PriceList priceList) {
        try {
            // 1. 校验ID
            if (priceList.getId() == null) {
                return Result.error("价目表ID不能为空");
            }

            // 2. 检查是否存在
            PriceList existing = priceListMapper.selectById(priceList.getId());
            if (existing == null) {
                return Result.error("价目表不存在");
            }


            if (priceListMapper.updatePriceList(priceList) <= 0) return Result.error("价目表更新失败");


            // 只更新已存在的明细记录
            Optional.ofNullable(priceList.getEntries())
                    .ifPresent(entries -> updateExistingEntries(entries, priceList.getFNumber()));

            return Result.success("价目表更新成功");
        } catch (Exception e) {
            throw new RuntimeException("更新价目表失败: " + e.getMessage(), e);
        }
    }
    private void updateExistingEntries(List<PriceListEntry> entryDTOs, String priceListId) {
        // 获取现有明细ID集合
        List<Long> existingIds = priceListEntryMapper.selectByPriceListId(priceListId).stream()
                .map(PriceListEntry::getId)
                .collect(Collectors.toList());

        // 只更新已存在的记录
        entryDTOs.stream()
                .filter(dto -> dto.getId() != null && existingIds.contains(dto.getId()))
                .map(dto -> {

                    PriceListEntry entry = new PriceListEntry();
                    entry.setPriceListNumber(String.valueOf(priceListId));
                    return entry;
                })
                .forEach(priceListEntryMapper::updateEntry);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)

    public boolean deleteById(Long id) {
        //1.查询价采购价目表主表
        PriceList priceList = priceListMapper.selectById(id);
        if(priceList != null){
            //2.查询价采购价目详情表
            String priceListNumber = priceList.getFNumber();
            List<PriceListEntry> listEntries = priceListEntryMapper.selectByPriceListId(priceListNumber);
            if (!listEntries.isEmpty()){
                //3.删除采购价目详情中的工厂图片文件
                if (listEntries != null){
                    listEntries.forEach(en ->{
                        //删除工厂图片文件
                        if (en.getFTP1() != null && !en.getFTP1().trim().isEmpty()){
                            //删除客户logo
                            minioUtil.deleteFile(en.getFTP1());
                        }
                        //4.删除采购价目明细表
                        priceListEntryMapper.deleteById(en.getId());
                    });
                }
            }
        }
        // 5.再删除主表
        return priceListMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByNumber(String FNumber) {
        PriceList priceList = priceListMapper.selectByNumber(FNumber);
        if (priceList != null) {
            return deleteById(priceList.getId());
        }
        return false;
    }

    @Override
    public PriceList getById(Long id) {
        // 查询主表
        PriceList priceList = priceListMapper.selectById(id);
        if (priceList != null) {
            // 查询明细
            List<PriceListEntry> entries = priceListEntryMapper.selectByPriceListId(priceList.getFNumber());
            priceList.setEntries(entries);
        }
        return priceList;
    }

    @Override
    public PriceList getByNumber(String id) {
        // 查询主表
        PriceList priceList = priceListMapper.selectByNumber(id);
        if (priceList != null) {
            // 查询明细
            String priceListNumber = priceList.getFNumber();
            List<PriceListEntry> entries = priceListEntryMapper.selectByPriceListId(priceListNumber);
            if (!entries.isEmpty()){
                priceList.setEntries(entries);
            }
        }
        return priceList;
    }

    @Override
    public TableDataInfo<PriceList> list(PriceListBo priceList, PageQuery pageQuery) {
        // 执行分页查询
        Page<PriceList> pageResult = priceListMapper.selectPage(pageQuery.build(),this.buildQueryWrapper(priceList));
        pageResult.getRecords().forEach(en ->{
            en.setEntries(priceListEntryMapper.selectByPriceListId(en.getFNumber()));
        });
        return TableDataInfo.build(pageResult);
    }

     /**
     * @param priceList 查询条件对象
     * @return 客户列表
     */
    private Wrapper<PriceList> buildQueryWrapper(PriceListBo priceList) {
        QueryWrapper<PriceList> wrapper = Wrappers.query();
                //查询条件
                wrapper
                    .eq(ObjectUtil.isNotNull(priceList.getId()),"pl.id", priceList.getId())
                    .like(StringUtils.isNotBlank(priceList.getFNumber()), "pl.FNumber", priceList.getFNumber())
                    .like(StringUtils.isNotBlank(priceList.getFSupplierID()), "sp.name",priceList.getFSupplierID())
                    .like(StringUtils.isNotBlank(priceList.getFMaterialId()), "b.number",priceList.getFMaterialId())
                    .like(StringUtils.isNotBlank(priceList.getFMaterialName()), "ple.FMaterialName",priceList.getFMaterialName())
                    .orderByDesc("pl.created_at");
        return wrapper;
    }


    @Override
    public List<PriceList> listBySupplierId(Long supplierId) {
        return priceListMapper.selectBySupplierId(supplierId);
    }

    @Override
    public List<PriceList> listBySupplierCategory(Long supplierCategory) {
        return priceListMapper.selectBySupplierCategory(supplierCategory);
    }

    @Override
    public Result syncFromKingdee() {
        try {
            // 1. 从金蝶获取采购价目主表数据
            List<List<Object>> priceListData = k3configks.PriceLarsList();
            syncPriceList(priceListData);

            // 2. 从金蝶获取采购价目明细表数据
            List<List<Object>> priceListEntryData = k3configks.PriceParticularsList();
            syncPriceListEntry(priceListEntryData);

            return Result.success("从金蝶系统同步采购价目表数据成功");
        } catch (Exception e) {
            return Result.error("从金蝶系统同步采购价目表数据失败: " + e.getMessage());
        }
    }

}
