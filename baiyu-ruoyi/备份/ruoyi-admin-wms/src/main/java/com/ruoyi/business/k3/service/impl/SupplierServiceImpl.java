package com.ruoyi.business.k3.service.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.business.emen.SupplierEnums;
import com.ruoyi.business.entity.*;
import com.ruoyi.business.feishu.config.BatchGetEmployeeConfig;
import com.ruoyi.business.k3.service.SupplierService;
import com.ruoyi.business.k3.util.DateUtils;
import com.ruoyi.business.mapper.*;
import com.ruoyi.business.servicel.DictionaryLookupServicel;
import com.ruoyi.business.util.MinioUtil;
import com.ruoyi.business.util.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.ruoyi.business.util.ValidatorUtil;
import com.ruoyi.common.core.domain.bo.LoginUser;
import com.ruoyi.common.satoken.utils.LoginHelper;
import com.ruoyi.system.domain.bo.SysUserBo;
import com.ruoyi.system.domain.entity.SysUser;
import com.ruoyi.system.mapper.SysUserMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;




import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SupplierServiceImpl implements SupplierService {

    @Resource
    private SupplierMapper supplierMapper;
    @Autowired
    private SupplierContactMapper supplierContactMapper;
    @Autowired
    private BymaterialDictionaryMapper bymaterialDictionaryMapper;
    @Autowired
    private FinancialInformationMapper financialInformationMapper;
    @Autowired
    private SettlementMethodMapper settlementMethodMapper;
    @Resource
    private SupplierVisitRecordMapper supplierVisitRecordMapper;
    @Autowired
    private TaxRateMapper taxRateMapper;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private BatchGetEmployeeConfig batchGetEmployeeConfig;
    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    private ValidatorUtil validatorUtil;

    @Autowired
    private DictionaryTableMapper dictionaryLookupMapper;

    @Autowired
    private DictionaryLookupServicel dictionaryLookupServicel;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private SupplierEncodingMapper supplierEncodingMapper;

    @Autowired
    private SysDataAuditLogMapper auditLogMapper;

    @Autowired
    private CountryMapper countryMapper;


    @Override
    public boolean querySupplierList(List<List<Object>> querySupplierList) {

        List<Supplier> supplierList = new ArrayList<>();
        for (List<Object> rowData : querySupplierList) {
            Supplier supplier = new Supplier();

            // 使用toString()方法而不是强制类型转换
            supplier.setName(rowData.get(0) != null ? rowData.get(0).toString() : null);

            supplier.setNumber(rowData.get(1) != null ? rowData.get(1).toString() : null);
            supplier.setAbbreviation(rowData.get(2) != null ? rowData.get(2).toString() : null);
            supplier.setNation(rowData.get(3) != null ? rowData.get(3).toString() : null);
            supplier.setRegion(rowData.get(4) != null ? rowData.get(4).toString() : null);
            supplier.setAddress(rowData.get(5) != null ? rowData.get(5).toString() : null);
            supplier.setLegalPerson(rowData.get(6) != null ? rowData.get(6).toString() : null);
            supplier.setEstablishDate(DateUtils.parseIsoDate(rowData.get(7) != null ? rowData.get(7).toString() : null));
            supplier.setForeignShare(rowData.get(8) != null ? (rowData.get(8).toString()) : null);


            supplier.setManager(rowData.get(9) != null ? rowData.get(9).toString() : null);

            supplier.setSupplierCategory(rowData.get(10) != null ? rowData.get(10).toString() : null);
            supplier.setSupplyType(rowData.get(11) != null ? rowData.get(11).toString() : null);
            supplier.setMainProduct(rowData.get(12) != null ? rowData.get(12).toString() : null);
            supplier.setBusinessRegistration(rowData.get(13) != null ? rowData.get(13).toString() : null);
            supplier.setSocialCreditCode(rowData.get(14) != null ? rowData.get(14).toString() : null);
            supplier.setSource(rowData.get(15) != null ? rowData.get(15).toString() : null);
            supplier.setBusinessLicense(rowData.get(16) != null ? rowData.get(16).toString() : null);
            supplier.setInvoiceName(rowData.get(17) != null ? rowData.get(17).toString() : null);
            supplier.setContactInfo(rowData.get(18) != null ? rowData.get(18).toString() : null);
            supplier.setFollowUpFeedback(rowData.get(19) != null ? rowData.get(19).toString() : null);
            supplier.setSettlementCurrency(rowData.get(20) != null ? rowData.get(20).toString() : null);
            supplier.setSettlementMethod(rowData.get(21) != null ? rowData.get(21).toString() : null);
            supplier.setPaymentTerms(rowData.get(22) != null ? rowData.get(22).toString() : null);
            supplier.setInvoiceType(rowData.get(23) != null ? rowData.get(23).toString() : null);
            supplier.setTaxCategory(rowData.get(24) != null ? rowData.get(24).toString() : null);
            supplier.setSettlementParty(rowData.get(25) != null ? rowData.get(25).toString() : null);
            supplier.setPayee(rowData.get(26) != null ? rowData.get(26).toString() : null);
            supplier.setDefaultTaxRate(rowData.get(27) != null ? (rowData.get(27).toString()) : null);
            supplier.setCreatedBy(rowData.get(28) != null ? rowData.get(28).toString() : null);
            supplier.setCause(rowData.get(29) != null ? rowData.get(28).toString() : null);

            supplier.setCreatedAt(DateUtils.parseIsoDate(rowData.get(30) != null ? rowData.get(29).toString() : null));
            supplier.setSupplierid(rowData.get(31) != null ? rowData.get(31).toString() : null);
            supplier.setSupplierGroup(rowData.get(32) != null ? rowData.get(32).toString() : null);
            supplier.setUpdatedBy(rowData.get(33) != null ? rowData.get(33).toString() : null);
            supplier.setTurnover(rowData.get(34) != null ? rowData.get(34).toString() : null);
            supplier.setCustomization(rowData.get(35) != null ? rowData.get(35).toString() : null);
            supplier.setCustomization(rowData.get(36) != null ? rowData.get(36).toString() : null);
            supplier.setFactoryPeople(rowData.get(37) != null ? rowData.get(37).toString() : null);
            supplier.setFactoryCertification(rowData.get(38) != null ? rowData.get(38).toString() : null);
            supplier.setFactoryArea(rowData.get(39) != null ? rowData.get(39).toString() : null);
            supplier.setFactoryEquipment(rowData.get(40) != null ? rowData.get(40).toString() : null);
            supplier.setQualityControl(rowData.get(41) != null ? rowData.get(41).toString() : null);
            supplier.setFactoryPositioning(rowData.get(42) != null ? rowData.get(42).toString() : null);
            supplier.setBehave(rowData.get(43) != null ? rowData.get(43).toString() : null);
            supplier.setPriorityPayment(rowData.get(44) != null ? rowData.get(44).toString() : null);
            supplier.setAuditor(rowData.get(45) != null ? rowData.get(45).toString() : null);
            supplier.setAuditTime((rowData.get(46) != null ? rowData.get(46).toString() : null));
            supplier.setFdefaultContactId(rowData.get(47) != null ? rowData.get(47).toString() : null);
            supplierList.add(supplier);
        }

        // 批量遍历并插入/更新
        for (Supplier supplier : supplierList) {
            // 查询是否存在该供应商编码
            Supplier existing = supplierMapper.selectById(supplier.getNumber());
            if (existing != null) {
                // 设置ID以便更新
                supplier.setId(existing.getId());
                supplierMapper.updateSupplier(supplier);
            } else {
                supplierMapper.insertSupplier(supplier);
            }
        }

        return true;
    }

    @Override
    public boolean syncSupplierVisitRecordFromK3(List<List<Object>> visitRecordList) {
        try {


            List<SupplierVisitRecord> supplierList = new ArrayList<>();
            for (List<Object> rowData : visitRecordList) {
                SupplierVisitRecord supplierVisitRecord = new SupplierVisitRecord();

                supplierVisitRecord.setSupplierNumber(rowData.get(0) != null ? rowData.get(0).toString() : null);
                supplierVisitRecord.setVisitTime(rowData.get(1) != null ? rowData.get(1).toString() : null);
                supplierVisitRecord.setVisitor(rowData.get(2) != null ? rowData.get(2).toString() : null);
                supplierVisitRecord.setVisitContent(rowData.get(3) != null ? rowData.get(3).toString() : null);
                supplierList.add(supplierVisitRecord);
            }
            // 批量遍历并插入/更新
            for (SupplierVisitRecord supplierVisitRecord : supplierList) {
                // 查询是否存在该供应商编码
                List<SupplierVisitRecord> existing = supplierVisitRecordMapper.selectSupplierVisitRecordById(supplierVisitRecord.getSupplierNumber());
                if (existing != null) {
                    for (SupplierVisitRecord supplierVisitRecords : existing){
                        // 设置ID以便更新
                        supplierVisitRecord.setId(supplierVisitRecords.getId());
                        supplierVisitRecordMapper.updateSupplierVisitRecord(supplierVisitRecord);
                    }
                } else {
                    supplierVisitRecordMapper.insertSupplierVisitRecord(supplierVisitRecord);
                }
            }
            log.info("同步供应商访问记录{}条数据", supplierList.size());
        }catch (Exception e){
            log.error("同步供应商访问记录失败",e);
        }
        return true;
    }


    //新增供应商联系人信息
    @Override
    public Result save(Supplier supplier) throws Exception {
        //获取当前用户ID
        Long userId = LoginHelper.getUserId();
        //通过userId查询数据库获取当前用户
        SysUser user = sysUserMapper.selectById(userId);
        //将用户中的k3
        supplier.setCreatedBy(user.getK3Key());

        // 供应商付款信息
        if (supplier.getFinancialInformation()!= null) {
            supplier.getFinancialInformation().forEach(financialInfo -> {
                financialInfo.setSupplierNumber(supplier.getNumber());
                //通过负责人字段查询user表获取k3_id给供应商联系人表中的k3_id manager
                String staffId = supplier.getManager();
                SysUserBo users = sysUserMapper.selectUserByStaffId(staffId);
                financialInfo.setK3Id(Long.valueOf(users.getK3key()));
                // 确保财务信息对象的其他必要字段也被正确设置
            });
            Optional.ofNullable(supplier.getFinancialInformation())
                    .ifPresent(list -> list.forEach(financialInformationMapper::insert));
        }

        //供应商联系人信息
        if (supplier.getContactInformation()!= null) {
            // 获取供应商编号
            supplier.getContactInformation().forEach(financialInfo ->{
                financialInfo.setSupplierCode(supplier.getNumber());
                //通过负责人字段查询user表获取k3_id给供应商联系人表中的k3_id manager
                String staffId = supplier.getManager();
                SysUserBo users = sysUserMapper.selectUserByStaffId(staffId);
                financialInfo.setK3id(Long.valueOf(users.getK3key()));
            });
            Optional.ofNullable(supplier.getContactInformation())
                    .ifPresent(list -> list.forEach(supplierContactMapper::insert));
        }

        //回访信息
        if (supplier.getSupplierVisitRecord() != null){
            supplier.getSupplierVisitRecord().forEach(financialInfo ->{
                financialInfo.setCreatedBy(user.getStaffId());
                //获取编码
                financialInfo.setSupplierNumber(supplier.getNumber());
            });
            Optional.ofNullable(supplier.getSupplierVisitRecord())
                .ifPresent(list -> list.forEach(supplierVisitRecordMapper::insert));
        }

        //添加一条数据到供应商编码表
        SupplierEncoding supplierEncoding =  new SupplierEncoding();
        supplierEncoding.setNumber(supplier.getNumber());
        supplierEncoding.setSupplierGroup(supplier.getSupplierGroup());
        supplierEncoding.setCreatorTime(new Date());
        supplierEncodingMapper.insert(supplierEncoding);

        //字段校验
        Supplier supplier1= extracted(supplier);
        //获取当前登入人
        LoginUser loginUser = LoginHelper.getLoginUser();
        SysUserBo sysAccountUser=sysUserMapper.selectNickname(loginUser.getUserId());
        supplier1.setCreatedBy(sysAccountUser.getK3key());
        supplierMapper.insertSupplier(supplier1);
        //推送飞书消息提醒
        Map<String, String> fields = new HashMap<>();
        fields.put("供应商名称",supplier.getName() );
        fields.put("供应商编码", supplier.getNumber());
        fields.put("提交人", sysAccountUser.getNickName());
        fields.put("状态", "推送成功");

        batchGetEmployeeConfig.sendCommonPushCard(
            "供应商推送",
            fields,
            "http://113.46.194.126/k3cloud",
            "打开金蝶系统"
        );


        return Result.success("供应商新增成功");
    }

    @Override
    @Transactional
    public boolean updateById(Supplier supplier) {
        //获取当前用户id
        Long userId = LoginHelper.getUserId();
        SysUserBo sysAccountUser = sysUserMapper.selectNickname(userId);

        // 获取供应商编号
        String number = supplier.getNumber();
        //联系人信息
        if (supplier.getContactInformation() != null) {
            supplier.getContactInformation().forEach(financialInfo -> {
                financialInfo.setSupplierCode(number);
                financialInfo.setK3id(Long.valueOf(sysAccountUser.getK3key()));

            });

            List<SupplierContactBase> newContactList = supplier.getContactInformation();
            for (SupplierContactBase contactBase : newContactList) {
                if (contactBase.getId() != null) {
                    supplierContactMapper.updateSupplierContact(contactBase);

                } else {
                    supplierContactMapper.insertSupplierContact(contactBase);
                }
            }
        }
        //财务信息
        if (supplier.getFinancialInformation() != null) {
            supplier.getFinancialInformation().forEach(financialInfo -> {
                financialInfo.setSupplierNumber(number);
                financialInfo.setK3Id(Long.valueOf(sysAccountUser.getK3key()));
            });
            List<FinancialInformation> financialInformations = supplier.getFinancialInformation();
            for (FinancialInformation financialInformation : financialInformations) {
                if (financialInformation.getId() != null) {
                    financialInformationMapper.updateSupplierContact(financialInformation);
                } else {
                    financialInformationMapper.insertSupplierContact(financialInformation);
                }
            }
        }

        //回访信息
        if (supplier.getSupplierVisitRecord() != null){
            supplier.getSupplierVisitRecord().forEach(financialInfo -> {
                //获取当前用户
                financialInfo.setCreatedBy(sysAccountUser.getStaffId());
                //获取编码
                financialInfo.setSupplierNumber(number);
            });
            List<SupplierVisitRecord> supplierVisitRecord = supplier.getSupplierVisitRecord();
            for (SupplierVisitRecord supplierVisitRecords : supplierVisitRecord) {
                if (supplierVisitRecords.getId() != null) {
                    supplierVisitRecordMapper.updateSupplierVisitRecord(supplierVisitRecords);
                } else {
                    supplierVisitRecordMapper.insertSupplierVisitRecord(supplierVisitRecords);
                }
            }
        }

        Supplier supplier1= extracted(supplier);
        supplier1.setUpdatedBy(sysAccountUser.getK3key());

        return supplierMapper.updateSupplier(supplier1) > 0;
    }

    private Supplier extracted(Supplier supplier) {

        //负责人
        if(supplier.getManager() != null && !supplier.getManager().trim().isEmpty()){
            //查询
            if (validatorUtil.isAllChinese(supplier.getManager()) == false ){
                Employee employee = employeeMapper.selectByStaffNumber(supplier.getManager());
                supplier.setManager(employee.getFid().toString());
            }else {
                Employee employee = employeeMapper.selectByFName(supplier.getManager());
                supplier.setManager(employee.getFid().toString());
            }
        }

        //共应分组
        if (supplier.getSupplierGroup() != null && !supplier.getSupplierGroup().trim().isEmpty()){
            if (validatorUtil.isAllDigits(supplier.getSupplierGroup()) == false){
                //查询
                SupplierGroups supplierGroups = dictionaryLookupMapper.selectByGroupName(supplier.getSupplierGroup());
                supplier.setSupplierGroup(supplierGroups.getSupplierGroup().toString());
            }
        }

        if (supplier.getSupplierCategory() != null && !supplier.getSupplierCategory().isEmpty()) {
            // 直接用 valueOf 匹配枚举 供应商分类
            if (validatorUtil.isAllChinese(supplier.getSupplierCategory()) == false){
                BymaterialDictionary bymaterialDictionary  =  new BymaterialDictionary();
                bymaterialDictionary.setCode(supplier.getSupplierCategory());
                bymaterialDictionary.setCategory("supplier_classification");
                BymaterialDictionary dictionary = bymaterialDictionaryMapper.selectByCategoryAndNames(bymaterialDictionary);
                supplier.setSupplierCategory(dictionary.getKingdee());
            }
            else {
                BymaterialDictionary bymaterialDictionary  =  new BymaterialDictionary();
                bymaterialDictionary.setName(supplier.getSupplierCategory());
                bymaterialDictionary.setCategory("supplier_classification");
                BymaterialDictionary dictionary = bymaterialDictionaryMapper.selectByCategoryAndNames(bymaterialDictionary);
                supplier.setSupplierCategory(dictionary.getKingdee());
            }
        }

        //供应类别
        if(supplier.getSupplyType() != null && !supplier.getSupplyType().trim().isEmpty()){
            if (validatorUtil.containsUpperCase(supplier.getSupplyType()) == false){
                //查询
                BymaterialDictionary bymaterialDictionary  =  new BymaterialDictionary();
                bymaterialDictionary.setName(supplier.getSupplyType());
                bymaterialDictionary.setCategory("Supply_category");
                BymaterialDictionary dictionary = bymaterialDictionaryMapper.selectByCategoryAndNames(bymaterialDictionary);
                supplier.setSupplyType(dictionary.getKingdee());
            }
            else{
                BymaterialDictionary bymaterialDictionary  =  new BymaterialDictionary();
                bymaterialDictionary.setKingdee(supplier.getSupplyType());
                bymaterialDictionary.setCategory("Supply_category");
                BymaterialDictionary dictionary = bymaterialDictionaryMapper.selectByCategoryAndNames(bymaterialDictionary);
                supplier.setSupplyType(dictionary.getKingdee());
            }

        }


        if (supplier.getSource() != null && !supplier.getSource().isEmpty()) {
            // 直接用 valueOf 匹配枚举 供应商来源
            if (validatorUtil.isAllChinese(supplier.getSource())== false){
                BymaterialDictionary bymaterialDictionary  =  new BymaterialDictionary();
                bymaterialDictionary.setCode(supplier.getSource());
                bymaterialDictionary.setCategory("customer_source");
                BymaterialDictionary dictionary = bymaterialDictionaryMapper.selectByCategoryAndNames(bymaterialDictionary);
                supplier.setSource(dictionary.getKingdee());
            }
            else {
                BymaterialDictionary bymaterialDictionary  =  new BymaterialDictionary();
                bymaterialDictionary.setName(supplier.getSource());
                bymaterialDictionary.setCategory("customer_source");
                BymaterialDictionary dictionary = bymaterialDictionaryMapper.selectByCategoryAndNames(bymaterialDictionary);
                supplier.setSource(dictionary.getKingdee());
            }
        }

        if (supplier.getSettlementCurrency() != null && !supplier.getSettlementCurrency().isEmpty()) {
            // 直接用 valueOf 匹配枚举 结算币别
            if (validatorUtil.isAllChinese(supplier.getSettlementCurrency()) == false){
                BymaterialDictionary bymaterialDictionary  =  new BymaterialDictionary();
                bymaterialDictionary.setCode(supplier.getSettlementCurrency());
                bymaterialDictionary.setCategory("currency");
                BymaterialDictionary dictionary = bymaterialDictionaryMapper.selectByCategoryAndNames(bymaterialDictionary);
                supplier.setSettlementCurrency(dictionary.getKingdee());
            }
           else {
                BymaterialDictionary bymaterialDictionary  =  new BymaterialDictionary();
                bymaterialDictionary.setName(supplier.getSettlementCurrency());
                bymaterialDictionary.setCategory("currency");
                BymaterialDictionary dictionary = bymaterialDictionaryMapper.selectByCategoryAndNames(bymaterialDictionary);
                supplier.setSettlementCurrency(dictionary.getKingdee());
            }
        }

        if (supplier.getPaymentTerms() != null && !supplier.getPaymentTerms().isEmpty()) {
                // 直接用 valueOf 匹配枚举 付款条件
                if (validatorUtil.isAllDigits(supplier.getPaymentTerms()) == false) {
                    BymaterialDictionary bymaterialDictionary = new BymaterialDictionary();
                    bymaterialDictionary.setName(supplier.getPaymentTerms());
                    bymaterialDictionary.setCategory("payment_clause");
                    BymaterialDictionary dictionary = bymaterialDictionaryMapper.selectByCategoryAndNames(bymaterialDictionary);
                    supplier.setPaymentTerms(dictionary.getKingdee());
                }
        }

        if (supplier.getInvoiceType() != null && !supplier.getInvoiceType().isEmpty()) {
            // 直接用 valueOf 匹配枚举 发票类型
            if (validatorUtil.isAllDigits(supplier.getInvoiceType()) == false){
                BymaterialDictionary bymaterialDictionary  =  new BymaterialDictionary();
                bymaterialDictionary.setName(supplier.getInvoiceType());
                bymaterialDictionary.setCategory("Invoice_type");
                BymaterialDictionary dictionary = bymaterialDictionaryMapper.selectByCategoryAndNames(bymaterialDictionary);
                supplier.setInvoiceType(dictionary.getKingdee());
            }
        }

        if (supplier.getTaxCategory() != null && !supplier.getTaxCategory().isEmpty()) {
            // 直接用 valueOf 匹配枚举 税分类
            SupplierEnums.TaxpayerType type = SupplierEnums.TaxpayerType.fromCode(supplier.getTaxCategory());
            if (type == null){
                BymaterialDictionary bymaterialDictionary  =  new BymaterialDictionary();
                bymaterialDictionary.setName(supplier.getTaxCategory());
                BymaterialDictionary dictionary = bymaterialDictionaryMapper.selectByCategoryAndNames(bymaterialDictionary);
                supplier.setTaxCategory(dictionary.getKingdee());
            }
        }

        if (supplier.getDefaultTaxRate() != null && !supplier.getDefaultTaxRate().isEmpty()) {
            //默认税率（%）
            if (validatorUtil.containsUpperCase(supplier.getDefaultTaxRate()) == false){
                 SupplierEnums.DefaultTaxRate type = SupplierEnums.DefaultTaxRate.fromDisplayName(supplier.getDefaultTaxRate());
                 if (type != null) {
                     supplier.setDefaultTaxRate(type.getCode());
                 }
            }else {
                TaxRate taxRate = taxRateMapper.selectByCode(supplier.getDefaultTaxRate());
                if (taxRate != null){
                    SupplierEnums.DefaultTaxRate type = SupplierEnums.DefaultTaxRate.fromDisplayName(taxRate.getName());
                    if (type != null) {
                        supplier.setDefaultTaxRate(type.getCode());
                    }
                }
            }
        }

        if (supplier.getSettlementMethod() != null && !supplier.getSettlementMethod().isEmpty()) {
            // 结算方式
            if (validatorUtil.isAllDigits(supplier.getSettlementMethod()) == false){
                SettlementMethod method = new SettlementMethod();
                method.setName(supplier.getSettlementMethod());
                SettlementMethod tlementMethod = settlementMethodMapper.selectByTlementMethod(method);
                supplier.setSettlementMethod(tlementMethod.getId().toString());
            }
        }
        return supplier;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(String number) {
       Supplier supplier = supplierMapper.selectById( number);
       if (supplier.getBusinessLicense() != null && !supplier.getBusinessLicense().trim().isEmpty()) {
           minioUtil.deleteFile(supplier.getBusinessLicense());
       }

        List<SupplierVisitRecord> supplierVisitRecord = supplierVisitRecordMapper.selectSupplierVisitRecordById(supplier.getNumber());

        if (supplierVisitRecord.size() > 0){
            supplierVisitRecord.forEach(supplierVisitRecords ->
                //判断是否有附件
                {
                    if (supplierVisitRecords.getAttachment() != null) {
                        //删除回访信息附件
                        minioUtil.deleteFile(supplierVisitRecords.getAttachment());
                    }
                }
            );
            //删除回访信息
            supplierVisitRecord.forEach(supplierVisit ->
                supplierVisitRecordMapper.deleteSupplierVisitRecordBySupplierId(supplierVisit.getSupplierNumber())
            );
        }

        supplierContactMapper.deleteSupplierContact(supplier.getNumber());
        financialInformationMapper.deleteSupplierContact(supplier.getNumber());
        return supplierMapper.deleteById(supplier.getNumber()) > 0;
    }

    /**
     * 通用字典值转换方法
     * 根据 kingdee 编码和 category 分类将编码转换为名称
     *
     * @param fieldToKingdee 字段名 -> kingdee编码的映射
     * @param fieldToCategory 字段名 -> category分类的映射
     * @param fieldSetter 字段名 -> 设置值的函数式接口
     */
    private void convertDictionaryValues(Map<String, String> fieldToKingdee,
                                        Map<String, String> fieldToCategory,
                                        BiConsumer<String, String> fieldSetter) {
        // 去除空值并去重
        List<String> codes = fieldToKingdee.values().stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (codes.isEmpty()) {
            return;
        }

        // 查询字典数据
        List<BymaterialDictionary> dictionaryList = bymaterialDictionaryMapper.selectByKingdees(codes);

        // 构建 (kingdee + category) -> BymaterialDictionary 的映射
        Map<String, BymaterialDictionary> compositeKeyToDictMap = new HashMap<>();
        for (BymaterialDictionary dict : dictionaryList) {
            if (dict.getKingdee() != null && dict.getCategory() != null) {
                String key = dict.getKingdee() + "_" + dict.getCategory();
                compositeKeyToDictMap.putIfAbsent(key, dict);
            }
        }

        // 给字段赋值对应的名称
        fieldToKingdee.forEach((field, kingdee) -> {
            if (kingdee != null) {
                String category = fieldToCategory.get(field);
                if (category != null) {
                    String compositeKey = kingdee + "_" + category;
                    BymaterialDictionary dict = compositeKeyToDictMap.get(compositeKey);

                    if (dict != null && dict.getName() != null) {
                        fieldSetter.accept(field, dict.getName());
                    }
                }
            }
        });
    }

    @Override
    public Supplier getById(String ids) {
     //供应商信息表
     Supplier supplier=   supplierMapper.selectById(ids);
     //供应商联系人信息表
     supplier.setContactInformation(supplierContactMapper.selectByContractBse(supplier.getNumber()));
     //银行信息表
     supplier.setFinancialInformation(financialInformationMapper.selectByContractBse(supplier.getNumber()));
     //回访信息
     supplier.setSupplierVisitRecord(supplierVisitRecordMapper.selectSupplierVisitRecordBySupplierId(supplier.getNumber()));

        SettlementMethod settlementMethod=  settlementMethodMapper.selectByCode(supplier.getSettlementMethod());
        if (settlementMethod != null){
            supplier.setSettlementMethod(settlementMethod.getName());

        }
       TaxRate taxRate= taxRateMapper.selectByK3Code(supplier.getDefaultTaxRate());
        if (taxRate != null){
            supplier.setDefaultTaxRate(taxRate.getName());
        }

        SysUserBo sysAccountUser= sysUserMapper.selectLoginName(supplier.getCreatedBy());
        if (sysAccountUser != null && sysAccountUser.getUserName() != null){
            supplier.setCreatedBy(sysAccountUser.getUserName());
        }

        //供应商分组
        if (supplier.getSupplierGroup() != null && !supplier.getSupplierGroup().trim().isEmpty()){
            SupplierGroups supplierGroups = dictionaryLookupServicel.SupplierGroupsK3id(supplier.getSupplierGroup());
            if (supplierGroups != null){
                supplier.setSupplierGroup(supplierGroups.getGroupName());
            }
        }

        // 处理负责人
        if (supplier.getManager() != null && !supplier.getManager().isEmpty()) {
                SysUserBo managerUser = sysUserMapper.selectUserByStaffId(supplier.getManager());
                if (managerUser != null && managerUser.getUserName() != null) {
                    supplier.setManager(managerUser.getUserName());
                }
        }

        //处理结算方式
        if (supplier.getSettlementMethod() != null && !supplier.getSettlementMethod().trim().isEmpty()){
            //判断是不是数字
            SettlementMethod tlementMethod  = new SettlementMethod();
            if (validatorUtil.isAllDigits(supplier.getSettlementMethod()) != false){
                tlementMethod.setId(Long.valueOf(supplier.getSettlementMethod()));
            }else {
                tlementMethod.setName(supplier.getSettlementMethod());
            }
            SettlementMethod settlementMethods = settlementMethodMapper.selectByTlementMethod(tlementMethod);
            supplier.setSettlementMethod(settlementMethods.getName());
        }

        //供应类别
        if (supplier.getSupplyType() != null && !supplier.getSupplyType().trim().isEmpty() != false){
            if(validatorUtil.containsUpperCase(supplier.getSupplyType()) != false){
                //查询
                BymaterialDictionary dictionarys  = new BymaterialDictionary();
                dictionarys.setName(supplier.getSupplyType());
                BymaterialDictionary dictionary = bymaterialDictionaryMapper.selectByCategoryAndNames(dictionarys);
                supplier.setSupplyType(dictionary.getName());
            }
        }

        //判断营业执照是否为url地址
        if (validatorUtil.isCompleteUrl(supplier.getBusinessLicense()) == false){
            supplier.setBusinessLicense(null);
        }

        // 构建要查询的 kingdee 编码列表及其对应的分类
        Map<String, String> fieldToKingdee = new LinkedHashMap<>();
        fieldToKingdee.put("supplierCategory", supplier.getSupplierCategory());
        fieldToKingdee.put("supplyType", supplier.getSupplyType());
        fieldToKingdee.put("tax_category", supplier.getTaxCategory());
        fieldToKingdee.put("source", supplier.getSource());
        fieldToKingdee.put("settlementCurrency", supplier.getSettlementCurrency());
        fieldToKingdee.put("paymentTerms", supplier.getPaymentTerms());
        fieldToKingdee.put("invoiceType", supplier.getInvoiceType());

        // 定义每个字段对应的 category 分类（使用数据库实际的 category 值）
        Map<String, String> fieldToCategory = new LinkedHashMap<>();
        fieldToCategory.put("supplierCategory", "supplier_classification");  // 供应商分类
        fieldToCategory.put("supplyType", "Supply_category");  // 供应类别
        fieldToCategory.put("tax_category", "tariff_nomenclature");  // 税分类
        fieldToCategory.put("source", "customer_source");  // 供应商来源
        fieldToCategory.put("settlementCurrency", "currency");  // 结算币别
        fieldToCategory.put("paymentTerms", "payment_clause");  // 付款条件
        fieldToCategory.put("invoiceType", "Invoice_type");  // 发票类型

        // 调用公共方法进行字典值转换
        convertDictionaryValues(fieldToKingdee, fieldToCategory, (field, name) -> {
            switch (field) {
                case "supplierCategory":
                    supplier.setSupplierCategory(name);
                    break;
                case "supplyType":
                    supplier.setSupplyType(name);
                    break;
                case "tax_category":
                    supplier.setTaxCategory(name);
                    break;
                case "source":
                    supplier.setSource(name);
                    break;
                case "settlementCurrency":
                    supplier.setSettlementCurrency(name);
                    break;
                case "paymentTerms":
                    supplier.setPaymentTerms(name);
                    break;
                case "invoiceType":
                    supplier.setInvoiceType(name);
                    break;
            }
        });

        return supplier;
    }

    /**
     * 分页查询供应商列表
     * @param supplier 查询条件
     * @param page 页码
     * @param size 每页数量
     * @return 分页数据
     */
    @Override
    public Page<Supplier> listSuppliers(Supplier supplier, int page, int size) {
        try {
            int offset = (page - 1) * size;

            // 调用 Mapper 层的分页查询
            List<Supplier> records = supplierMapper.selectByCondition(offset, size, supplier);
            long total = supplierMapper.countByCondition(supplier);

            for (Supplier record : records) {
             //联系人信息
             List <SupplierContactBase> contactInformation =   supplierContactMapper .selectByContractBse(record.getNumber());
             record.setContactInformation (contactInformation);
             //供应商付款信息
             record.setFinancialInformation(financialInformationMapper.selectByContractBse(record.getNumber()));
             //回访信息
             record.setSupplierVisitRecord(supplierVisitRecordMapper.selectSupplierVisitRecordBySupplierId(record.getNumber()));
             //更新人
              String k3key = record.getUpdatedBy();
              SysUserBo user = sysUserMapper.selectUserByK3key(k3key);
              if (user != null){
                  record.setUpdatedBy(user.getNickName());
              }

            }

            Page<Supplier> result = Page.of(page, size, total);
            result.setRecords(records);
            return result;
        } catch (Exception e) {
            log.error("查询供应商列表失败", e);
            return Page.of(0, size);
        }
    }

    /**
     * 处理供应商列表，进行字典转换和关联查询
     */
    private List<Supplier> processSupplierList(List<Supplier> suppliers) {
        if (suppliers.isEmpty()) return suppliers;

        // 1. 收集所有 key
        //获取编码
        List<String> supplierNumbers = suppliers.stream().map(Supplier::getNumber)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        //获取供应商分类
        List<String> categoryKeys = suppliers.stream().map(Supplier::getSupplierCategory)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        //获取结算方
        List<String> settlementPartyKeys = suppliers.stream().map(Supplier::getSettlementParty)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        //获取结算币别
        List<String> settlementCurrencyKeys = suppliers.stream().map(Supplier::getSettlementCurrency)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        //获取结算方式
        List<String> settlementMethodKeys = suppliers.stream().map(Supplier::getSettlementMethod)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        //获取来源
        List<String> sources = suppliers.stream().map(Supplier::getSource)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        //获取付款条件
        List<String> paymentTermsKeys = suppliers.stream().map(Supplier::getPaymentTerms)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        //获取发票类型
        List<String> invoiceTypeKeys = suppliers.stream().map(Supplier::getInvoiceType)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        //获取税分类
        List<String> taxCategoryKeys = suppliers.stream().map(Supplier::getTaxCategory)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        //获取默认税率（%）
        List<String> taxRateKeys = suppliers.stream().map(Supplier::getDefaultTaxRate)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        //获取创建人
        List<String> collected = suppliers.stream().map(Supplier::getCreatedBy)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());

        // 2. 批量查询字典
        List<String> allKeys = new ArrayList<>();
        //供应商分类
        allKeys.addAll(categoryKeys);
        //获取结算方
        allKeys.addAll(settlementPartyKeys);
        //获取结算方式
        allKeys.addAll(settlementMethodKeys);
        //获取来源
        allKeys.addAll(sources);
        //获取结算币别
        allKeys.addAll(settlementCurrencyKeys);
        //获取付款条件
        allKeys.addAll(paymentTermsKeys);
        //获取发票类型
        allKeys.addAll(invoiceTypeKeys);
        //获取税分类
        allKeys.addAll(taxCategoryKeys);

        //查询字典
        List<Map<String,Object>> dictRows = allKeys.isEmpty()
                ? Collections.emptyList()
                : bymaterialDictionaryMapper.selectByKeys(allKeys);

        Map<String,String> materialDictMap = dictRows.isEmpty()
                ? Collections.emptyMap()
                : dictRows.stream()
                .filter(row -> row.get("kingdee") != null)
                .collect(Collectors.toMap(
                        row -> ((String) row.get("kingdee")).trim(),
                        row -> ((String) row.get("name")).trim(),
                        (a,b) -> a
                ));

        // 3. 批量查询税率
        Map<String, String> taxRateMap = taxRateKeys.isEmpty()
                ? Collections.emptyMap()
                : taxRateMapper.selectByK3Codes(taxRateKeys).stream()
                .filter(row -> row.get("k3_code") != null && row.get("name") != null)
                .collect(Collectors.toMap(
                        row -> row.get("k3_code").toString(),
                        row -> row.get("name").toString(),
                        (a, b) -> a
                ));

        // 4. 批量查询联系人
        Map<String, List<SupplierContactBase>> contactMap = supplierNumbers.isEmpty() ? Collections.emptyMap()
                : supplierContactMapper.selectByContractBseList(supplierNumbers)
                .stream().collect(Collectors.groupingBy(SupplierContactBase::getSupplierCode));

        // 5. 批量查询财务信息
        Map<String, List<FinancialInformation>> financialMap = supplierNumbers.isEmpty() ? Collections.emptyMap()
                : financialInformationMapper.selectBySupplierNumbers(supplierNumbers).stream()
                .filter(f -> f.getSupplierNumber() != null)
                .collect(Collectors.groupingBy(FinancialInformation::getSupplierNumber));
        //结算方式表
        List<String> codes = suppliers.stream()
                .map(Supplier::getSettlementMethod)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

            // 给供应商对象赋值结算方式名称或其他信息
        if (!codes.isEmpty()) {
            List<SettlementMethod> settlementMethods = settlementMethodMapper.selectBySupplierNumbers(codes);

            // 构建 Map 便于快速赋值
            Map<String, SettlementMethod> codeMap = settlementMethods.stream()
                    .collect(Collectors.toMap(
                            SettlementMethod::getCode,
                            sm -> sm,
                            (existing, replacement) -> existing  // 如果重复，保留第一个
                    ));

            // 给供应商对象赋值结算方式名称
            suppliers.forEach(s -> {
                if (s.getSettlementMethod() != null) {
                    SettlementMethod sm = codeMap.get(s.getSettlementMethod());
                    if (sm != null) {
                        s.setSettlementMethod(sm.getName()); // 只赋值结算方式名称
                    }
                }
            });
        }


        //6. 批量查询创建人信息
        List<Map<String, Object>> list = sysUserMapper.selectMapByLoginName(collected);
        // K3_KEY -> LoginName
        Map<String, String> k3Map = list.stream()
                .filter(m -> m.get("k3_key") != null
                && m.get("login_name") != null
                )
                .collect(Collectors.toMap(
                        m -> m.get("k3_key").toString(),
                        m -> m.get("login_name").toString(),
                        (a,b) -> a
                ));

        // Staff_ID -> LoginName
        Map<String, String> staffMap = list.stream()
                .filter(m -> m.get("staff_id") != null
                && m.get("login_name") != null
                )
                .collect(Collectors.toMap(
                        m -> m.get("staff_id").toString(),
                        m -> m.get("login_name").toString(),
                        (a,b) -> a
                ));


        //  循环赋值
        for (Supplier sup : suppliers) {
            sup.setSupplierCategory(materialDictMap.get(sup.getSupplierCategory()));
            sup.setSource(materialDictMap.get(sup.getSource()));
            sup.setSettlementParty(materialDictMap.get(sup.getSettlementParty()));

            sup.setPaymentTerms(materialDictMap.get(sup.getPaymentTerms()));
            sup.setSettlementCurrency(materialDictMap.get(sup.getSettlementCurrency()));
            sup.setInvoiceType(materialDictMap.get(sup.getInvoiceType()));
            sup.setTaxCategory(materialDictMap.get(sup.getTaxCategory()));
            sup.setMainProduct(staffMap.get(sup.getMainProduct()));
            String creatorName = k3Map.get(sup.getCreatedBy());
            sup.setCreatedBy(creatorName);
            sup.setDefaultTaxRate(taxRateMap.get(sup.getDefaultTaxRate()));
            sup.setContactInformation(contactMap.getOrDefault(sup.getNumber(), Collections.emptyList()));
            sup.setFinancialInformation(financialMap.getOrDefault(sup.getNumber(), Collections.emptyList()));
        }

        return suppliers;
    }

    /**
     * 查询所有供应商（不分页）
     */
    @Override
    public List<Supplier> list(Supplier supplier) {
        List<Supplier> suppliers = supplierMapper.selectAll(supplier);
        if (!suppliers.isEmpty()) {
            suppliers = processSupplierList(suppliers);
        }
        return suppliers;
    }

    @Override
    public Supplier getMaterialByNumberDirect(String number) {
        return supplierMapper.selectById( number);
    }

    @Override
    public Supplier selectBySupplierGroup(String supplierGroup) {
        return supplierMapper.selectBySupplierGroup(supplierGroup);
    }

    /**
     * 根据表名和主键ID查询审计日志（分页）
     ** @return 审计日志分页数据
     */
    @Override
    public Page<SysDataAuditLog> getAuditLogsByTableAndId(String tableName, String rowId, int pageNum, int pageSize) {
        Page<SysDataAuditLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysDataAuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDataAuditLog::getTableName, tableName)
            .eq(SysDataAuditLog::getRowId, rowId)
            .orderByDesc(SysDataAuditLog::getOperateTime);

        Page<SysDataAuditLog> resultPage = auditLogMapper.selectPage(page, wrapper);

        // 在查询时转换字典值
        resultPage.getRecords().forEach(auditLog -> {
            if (auditLog.getDiffJson() != null && !auditLog.getDiffJson().isEmpty()) {
                try {
                    String convertedJson = convertDiffJsonDictValues(auditLog.getDiffJson());
                    auditLog.setDiffJson(convertedJson);
                } catch (Exception e) {
                    log.error("转换审计日志字典值失败", e);
                }
            }
        });

        return resultPage;
    }

    /**
     * 转换 diff_json 中的字典值
     */
    private String convertDiffJsonDictValues(String diffJson) {
        try {

            JSONObject diff = new JSONObject(diffJson);
            JSONObject convertedDiff = new JSONObject();

            diff.forEach((field, changeObj) -> {
                if (changeObj instanceof JSONObject) {
                    JSONObject change = (JSONObject) changeObj;
                    Object oldVal = change.get("old");
                    Object newVal = change.get("new");

                    // 转换字典值
                    String convertedOld = convertDictValue(field, oldVal);
                    String convertedNew = convertDictValue(field, newVal);

                    JSONObject convertedChange = new JSONObject();
                    convertedChange.put("old", convertedOld);
                    convertedChange.put("new", convertedNew);
                    convertedDiff.put(field, convertedChange);
                } else {
                    convertedDiff.put(field, changeObj);
                }
            });


            return convertedDiff.toString();
        } catch (Exception e) {
            log.error("解析 diff_json 失败: {}", diffJson, e);
            return diffJson;
        }
    }

    /**
     * 转换字典值为中文名称
     */
    private String convertDictValue(String field, Object value) {
        if (value == null) {
            return null;
        }

        try {
            String valueStr = value.toString();
            if (valueStr.trim().isEmpty()) {
                return valueStr;
            }

            // 将字段名统一转换为小写进行比较
            String fieldLower = field.toLowerCase();

            // 根据字段名转换国家
            if ("nation".equals(fieldLower)) {
                // 国家 - 优先尝试ID查询
                Country nation = null;
                try {
                    // 先尝试作为数字ID查询
                    String id = valueStr;
                    nation = countryMapper.selectById(id);

                } catch (NumberFormatException e) {
                    // 不是数字，尝试通过kingdee值查询
                    nation = countryMapper.selectById(valueStr);
                }
                return (nation != null && nation.getNameZh() != null) ? nation.getNameZh() : valueStr;

            } else if ("manager".equals(fieldLower)) {
                // 负责人
                SysUserBo user = sysUserMapper.selectUserByStaffId(valueStr);
                return (user != null && user.getNickName() != null) ? user.getNickName() : valueStr;

            } else if ("supplier_category".equals(fieldLower) || "suppliercategory".equals(fieldLower)) {
                // 供应商分类
                BymaterialDictionary productDict = bymaterialDictionaryMapper.selectByKingdes(valueStr);
                return (productDict != null && productDict.getName() != null) ? productDict.getName() : valueStr;
            } else if ("supply_type".equals(fieldLower) || "supplytype".equals(fieldLower)) {
                // 供应类别
                BymaterialDictionary productDict = bymaterialDictionaryMapper.selectByKingdes(valueStr);
                return (productDict != null && productDict.getName() != null) ? productDict.getName() : valueStr;
            }else if ("supply_type".equals(fieldLower) || "supplytype".equals(fieldLower)) {
                // 供应类别
                BymaterialDictionary productDict = bymaterialDictionaryMapper.selectByKingdes(valueStr);
                return (productDict != null && productDict.getName() != null) ? productDict.getName() : valueStr;
            }else if ("source".equals(fieldLower) || "source".equals(fieldLower)){
                // 来源
                BymaterialDictionary productDict = bymaterialDictionaryMapper.selectByKingdes(valueStr);
                return (productDict != null && productDict.getName() != null) ? productDict.getName() : valueStr;
            } else if ("settlement_currency".equals(fieldLower) || "settlementcurrency".equals(fieldLower)){
                // 结算币别
                String category = "currency";
                BymaterialDictionary productDict = bymaterialDictionaryMapper.selectByKingd(valueStr,category);
                return (productDict != null && productDict.getName() != null) ? productDict.getName() : valueStr;
            } else if ("settlement_method".equals(fieldLower) || "settlementmethod".equals(fieldLower)){
                //结算方式
                SettlementMethod productDict = settlementMethodMapper.selectById(Long.valueOf(valueStr));
                return (productDict != null && productDict.getName() != null) ? productDict.getName() : valueStr;
            } else if ("payment_terms".equals(fieldLower) || "paymentterms".equals(fieldLower)) {
                //付款条件
                BymaterialDictionary productDict = bymaterialDictionaryMapper.selectByKingdes(valueStr);
                return (productDict != null && productDict.getName() != null) ? productDict.getName() : valueStr;
            } else if ("invoice_type".equals(fieldLower) || "invoicetype".equals(fieldLower)) {
                //发票类型
                String category = "Invoice_type";
                BymaterialDictionary productDict = bymaterialDictionaryMapper.selectByKingd(valueStr,category);
                return (productDict != null && productDict.getName() != null) ? productDict.getName() : valueStr;
            } else if ("tax_category".equals(fieldLower) || "taxcategory".equals(fieldLower)) {
                //税分类
                BymaterialDictionary productDict = bymaterialDictionaryMapper.selectByKingdes(valueStr);
                return (productDict != null && productDict.getName() != null) ? productDict.getName() : valueStr;
            }
            else if ("default_tax_rate".equals(fieldLower) || "defaulttaxrate".equals(fieldLower)) {
                //默认税率 taxrate
                TaxRate productDict = taxRateMapper.selectK3Cod(valueStr);
                return (productDict != null && productDict.getName() != null) ? productDict.getName() : valueStr;
            }

        } catch (Exception e) {
            log.error("转换字典值失败: 字段={}, 值={}", field, value, e);
        }

        return value.toString();
    }


}
