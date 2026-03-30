package com.ruoyi.business.k3.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.Component.CustomerFormProcessor;
import com.ruoyi.business.entity.*;
import com.ruoyi.business.feishu.config.BatchGetEmployeeConfig;
import com.ruoyi.business.k3.config.k3config;
import com.ruoyi.business.k3.domain.bo.CustomerBo;
import com.ruoyi.business.k3.domain.vo.CustomerVo;
import com.ruoyi.business.k3.service.CustomerService;
import com.ruoyi.business.k3.util.DateUtils;
import com.ruoyi.business.mapper.*;

import com.ruoyi.business.util.DictConvertUtil;
import com.ruoyi.business.util.MinioUtil;
import com.ruoyi.business.util.Result;
import com.ruoyi.business.util.ValidatorUtil;
import com.ruoyi.common.core.constant.UserConstants;
import com.ruoyi.common.core.domain.bo.LoginUser;
import com.ruoyi.common.core.utils.StreamUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import com.ruoyi.common.mybatis.helper.DataBaseHelper;
import com.ruoyi.common.satoken.utils.LoginHelper;
import com.ruoyi.system.domain.bo.SysPostBo;
import com.ruoyi.system.domain.bo.SysRoleBo;
import com.ruoyi.system.domain.bo.SysUserBo;
import com.ruoyi.system.domain.entity.SysDept;
import com.ruoyi.system.domain.entity.SysPost;
import com.ruoyi.system.domain.entity.SysRole;
import com.ruoyi.system.domain.entity.SysUser;
import com.ruoyi.system.domain.vo.SysUserVo;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 客户服务实现类
 */
@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {

    @Resource
    private CustomerMapper customerMapper;

    @Resource
    private FinancialInformationMapper financialInformationMapper;

    @Resource
    private CustomerTransferMapper customerTransferMapper;

    @Resource
    private k3config k3Config;

    @Resource
    private SupplierContactMapper supplierContactMapper;

    @Resource
    private BymaterialDictionaryMapper bymaterialDictionaryMapper;

    @Resource
    private SettlementMethodMapper settlementMethodMapper;


    @Autowired
    private CustomerFormProcessor customerFormProcessor;

    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private ValidatorUtil validatorUtil;

    @Resource
    private EmployeeMapper employeeMapper;

    @Autowired
    CountryMapper countryMapper;

    @Autowired
    SysDeptMapper deptMapper;

    @Autowired
    private BatchGetEmployeeConfig batchGetEmployeeConfig;

    /**
     * 同步金蝶客户主表数据到本地数据库
     */
    @Override
    public int syncCustomerMultiThread() {
        log.info("开始同步金蝶客户主数据");

        // 1. 查询K3客户主表数据
        List<List<Object>> allData = k3Config.queryCustomerList();
        if (allData == null || allData.isEmpty()) {
            log.info("没有查询到客户主表数据");
            return 0;
        }

        log.info("查询到客户主表总数：{} 条", allData.size());

        // 2. 遍历处理
        int totalInserted = 0;
        int totalUpdated = 0;

        for (List<Object> rowData : allData) {
            // 转换为客户实体
            Customer customer = convertToCustomer(rowData);
            try {
                // 查询数据库是否存在
                Customer existing = customerMapper.selectByFcustid(customer.getFcustid());
                if (existing != null) {
                    // 存在则更新
                    customerMapper.updateByFcustid(customer);
                    totalUpdated++;
                } else {
                    // 不存在则新增
                    customerMapper.insert(customer);
                    totalInserted++;
                }
            } catch (Exception e) {
                log.error("处理客户主表失败：{}", customer.getFcustid(), e);
                break; // 跳出循环
            }
        }

        log.info("客户主数据同步完成，新增 {} 条，更新 {} 条", totalInserted, totalUpdated);
        return allData.size();
    }

    @Override
    public int syncCustomerBankInfo() {
        log.info("开始同步金蝶客户银行信息");

        // 1. 查询K3银行信息数据
        List<List<Object>> allData = k3Config.queryCustomerBankList();
        if (allData == null || allData.isEmpty()) {
            log.info("没有查询到客户银行信息数据");
            return 0;
        }

        log.info("查询到客户银行信息总数：{} 条", allData.size());

        // 2. 遍历处理
        int totalInserted = 0;
        int totalUpdated = 0;

        for (List<Object> rowData : allData) {
            if (rowData == null || rowData.size() < 7) {
                continue;
            }

            // 转换为银行信息实体
            FinancialInformation financialInfo = convertToFinancialInformation(rowData);
            try {
                // 查询数据库是否存在
                FinancialInformation existing = financialInformationMapper.selectBySupplierNumber(financialInfo.getSupplierNumber());
                if (existing != null) {
                    // 存在则更新
                    financialInfo.setId(existing.getId());
                    financialInformationMapper.updateSupplierContact(financialInfo);
                    totalUpdated++;
                } else {
                    // 不存在则新增
                    financialInformationMapper.insertSupplierContact(financialInfo);
                    totalInserted++;
                }
            } catch (Exception e) {
                log.error("处理银行信息失败：{}", financialInfo.getSupplierNumber(), e);
            }
        }

        log.info("客户银行信息同步完成，新增 {} 条，更新 {} 条", totalInserted, totalUpdated);
        return allData.size();
    }

    @Override
    public int syncCustomerTransferInfo() {
        log.info("开始同步金蝶客户转让人信息");

        // 1. 查询K3客户转让信息数据
        List<List<Object>> allData = k3Config.queryCustomerTransferList();
        if (allData == null || allData.isEmpty()) {
            log.info("没有查询到客户转让信息数据");
            return 0;
        }

        log.info("查询到客户转让信息总数：{} 条", allData.size());

        // 2. 遍历处理
        int totalInserted = 0;
        int totalUpdated = 0;

        for (List<Object> rowData : allData) {
            if (rowData == null || rowData.size() < 5) {
                continue;
            }

            // 转换为客户转让信息实体
            CustomerTransfer transfer = convertToCustomerTransfer(rowData);
            try {
                // 查询数据库是否存在
                CustomerTransfer existing = customerTransferMapper.selectByCustomerCode(transfer.getCustomerCode());

                if (existing != null) {
                    // 存在则更新
                    customerTransferMapper.update(transfer);
                    totalUpdated++;
                } else {
                    // 不存在则新增
                    customerTransferMapper.insert(transfer);
                    totalInserted++;
                }
            } catch (Exception e) {
                log.error("处理客户转让信息失败：{}", transfer.getCustomerCode(), e);
            }
        }

        log.info("客户转让信息同步完成，新增 {} 条，更新 {} 条", totalInserted, totalUpdated);
        return allData.size();
    }

    /**
     * K3 行数据 → Customer 实体映射方法
     */
    public Customer convertToCustomer(List<Object> objectList) {
        Customer c = new Customer();

        // 按照 K3 查询字段顺序严格对应
        int idx = 0;
        c.setFcustid(getLong(objectList.get(idx++)));              // 0: FCUSTID
        c.setFnumber(getString(objectList.get(idx++)));            // 1: FNumber
        c.setFname(getString(objectList.get(idx++)));              // 2: FName
        c.setFdocumentStatus(getString(objectList.get(idx++)));    // 3: FDocumentStatus
        c.setFshortName(getString(objectList.get(idx++)));         // 4: FShortName
        c.setFKhqc(getString(objectList.get(idx++)));              // 5: F_khqc
        c.setFdescription(getString(objectList.get(idx++)));       // 6: FDescription
        c.setFcreateOrgId(getLong(objectList.get(idx++)));         // 7: FCreateOrgId
        c.setFcreatorId(getString(objectList.get(idx++)));           // 8: FCreatorId
        c.setFmodifierId(getString(objectList.get(idx++)));          // 9: FModifierId
        c.setFseller(getString(objectList.get(idx++)));              // 10: FSeller
        c.setFKfxsy1(getString(objectList.get(idx++)));              // 11: F_kfxsy1
        c.setFsalDeptId(getLong(objectList.get(idx++)));           // 12: FSalDeptId
        c.setFsalGroupId(getString(objectList.get(idx++)));          // 13: FSalGroupId
        c.setFcustTypeId(getString(objectList.get(idx++)));          // 14: F_cust_type_id
        c.setFcreateDate(getLocalDateTime(objectList.get(idx++))); // 15: FCreateDate
        c.setFmodifyDate(getLocalDateTime(objectList.get(idx++))); // 16: FModifyDate
        c.setFfoundDate(getLocalDate(objectList.get(idx++)));      // 17: FFoundDate
        c.setFKhzrrq(getLocalDate(objectList.get(idx++)));         // 18: F_khzrrq
        c.setFZmmttp1(getString(objectList.get(idx++)));         // 19: F_ZMMTTP1
        c.setFZmmttp2(getString(objectList.get(idx++)));         // 19: F_ZMMTTP1
        c.setFZmmttpMs(getString(objectList.get(idx++)));         // 19: F_ZMMTTP1
        c.setFcmmttp1(getString(objectList.get(idx++)));         // 20: F_CMMTTP1
        c.setFcmmttp2(getString(objectList.get(idx++)));         // 20: F_CMMTTP1
        c.setFCmmttpMs(getString(objectList.get(idx++)));         // 20: F_CMMTTP1
        c.setFcountry(getString(objectList.get(idx++)));           // 21: FCountry
        c.setFprovincial(getString(objectList.get(idx++)));        // 22: FProvincial
        c.setFaddress(getString(objectList.get(idx++)));           // 23: FAddress
        c.setFregisterAddress(getString(objectList.get(idx++)));   // 24: FRegisterAddress
        c.setFtel(getString(objectList.get(idx++)));               // 25: FTel
        c.setFwebsite(getString(objectList.get(idx++)));           // 26: FWebsite
        c.setFtradingCurrId(getString(objectList.get(idx++)));       //27: FTradingCurrId
        c.setFreceiveCurrId(getString(objectList.get(idx++)));       // 28: FReceiveCurrId
        c.setFsettleTypeId(getLong(objectList.get(idx++)));        // 29: FSettleTypeId
        c.setFrecConditionId(getString(objectList.get(idx++)));      // 30: FRecConditionId
        c.setFpriceListId(getLong(objectList.get(idx++)));         // 31: FPriceListId
        c.setFtaxType(getLong(objectList.get(idx++)));             // 32: FTaxType
        c.setFtaxRate(getBigDecimal(objectList.get(idx++)));       // 33: FTaxRate
        c.setFgroupId(getString(objectList.get(idx++)));             // 34: FGroup
        c.setFKhly(getString(objectList.get(idx++)));              // 35: F_khly
        c.setFLy(getString(objectList.get(idx++)));                // 36: F_ly
        c.setFSylx(getString(objectList.get(idx++)));              // 37: F_sylx
        c.setFKhgm(getString(objectList.get(idx++)));              // 38: F_khgm
        c.setFKhzy(getString(objectList.get(idx++)));              // 39: F_khzy
        c.setFisGroup(getInteger(objectList.get(idx++)));          // 40: FIsGroup
        c.setFisDefPayer(getInteger(objectList.get(idx++)));       // 41: FIsDefPayer
        c.setFlegalPerson(getString(objectList.get(idx++)));       // 42: FLegalPerson
        c.setFinvoiceType(getString(objectList.get(idx++)));       // 44: FInvoiceType
        c.setFsupplierId(getLong(objectList.get(idx++)));          // 45: FSupplierId
        c.setFBzyq(getString(objectList.get(idx++)));              // 46: F_bzyq
        c.setFBzfs(getString(objectList.get(idx++)));              // 47: Fbzfs
        c.setFFhyq(getString(objectList.get(idx++)));              // 48: F_fhyq
        c.setFZlbzhjsyq(getString(objectList.get(idx++)));         // 49: F_zlbzhjsyq
        c.setFSfysqs(getInteger(objectList.get(idx++)));           // 50: F_sfysqs
        Integer fsbsqVal = getInteger(objectList.get(idx++));
        c.setFsbsq(fsbsqVal != null ? String.valueOf(fsbsqVal) : null);   // 51: Fsbsq
        Integer fsfsdVal = getInteger(objectList.get(idx++));
        c.setFsfsd(fsfsdVal != null ? String.valueOf(fsfsdVal) : null);   // 52: Fsfsd
        c.setFsfts(getInteger(objectList.get(idx++)));             // 53: Fsfts
        c.setFCtyDecimal(getBigDecimal(objectList.get(idx++)));    // 54: F_cty_Decimal
        c.setFTcfpfa(getString(objectList.get(idx++)));            // 55: F_tcfpfa
        c.setFpjskzq(getInteger(objectList.get(idx++)));           // 56: Fpjskzq
        c.setFMjll(getBigDecimal(objectList.get(idx++)));          // 57: F_mjll
        c.setFdygj(getString(objectList.get(idx++)));              // 58: F_DYGJ
        c.setFapperoberDate(getLocalDate(objectList.get(idx++)));  // 59: FAPPROVEDATE
        c.setFapproverId(getString(objectList.get(idx++)));         // 60: FAPPROVERID
        c.setFCtyLargeText(getString(objectList.get(idx++)));      // 60: F_cty_LargeText
        c.setFKhlogo(getString(objectList.get(idx++)));            // 61: F_KHLOGO
        c.setFcpAdminCode(getString(objectList.get(idx++)));         // 62: FCPAdminCode
        c.setFYoutube(getString(objectList.get(idx++)));             // 63: F_Youtube
        c.setFLinkedin(getString(objectList.get(idx++)));            // 64: F_linkedin
        c.setFFacebook(getString(objectList.get(idx++)));            // 65: F_facebook
        c.setFTwitter(getString(objectList.get(idx++)));             // 66: F_twitter
        c.setFInstagram(getString(objectList.get(idx++)));           // 67: F_instagram
        c.setFVk(getString(objectList.get(idx++)));                  // 68: F_vk
        c.setFFacebookmess(getString(objectList.get(idx++)));        // 69: F_facebookmess
        c.setFSkype(getString(objectList.get(idx++)));               // 70: F_skype
        c.setFWhatsapp(getString(objectList.get(idx++)));            // 71: F_whatsapp
        c.setFwechat(getString(objectList.get(idx++)));              // 72: FWeChat
        c.setFQq(getString(objectList.get(idx++)));                  // 73: F_qq
        c.setFtn(getString(objectList.get(idx++)));                  // 74: Ftn
        c.setFYolo(getString(objectList.get(idx++)));                // 75: F_Yolo
        c.setFHangouts(getString(objectList.get(idx++)));            // 76: F_Hangouts
        c.setFViber(getString(objectList.get(idx++)));                 // 77: F_Viber

        return c;
    }


    /**
     * K3 行数据 → CustomerTransfer 实体映射方法
     */
    public CustomerTransfer convertToCustomerTransfer(List<Object> objectList) {
        CustomerTransfer ct = new CustomerTransfer();
        int idx = 0;
        ct.setFZrr(getString(objectList.get(idx++)));        // 79: F_zrr
        ct.setFJsr(getString(objectList.get(idx++)));        // 80: F_jsr
        ct.setFZrrq(getLocalDate(objectList.get(idx++)));    // 81: F_zrrq
        ct.setFTcbl(getBigDecimal(objectList.get(idx++)));   // 82: F_tcbl
        ct.setCustomerCode(getString(objectList.get(idx)));  // 83: FNumber
        return ct;
    }

    /**
     * 检查 CustomerTransfer 是否有有效数据
     */
    public boolean hasValidTransferInfo(CustomerTransfer ct) {
        return ct != null && ct.getCustomerCode() != null &&
               (ct.getFZrr() != null || ct.getFJsr() != null);
    }

    @Override
    public Customer getById(Long id) {
        Customer customer = customerMapper.selectById(id);
        if (customer != null){
            //供应商付款信息
            if (customer.getFnumber() != null && !customer.getFnumber().trim().isEmpty()){
                FinancialInformation financialInformation = financialInformationMapper.selectBySupplierNumber(customer.getFnumber());
                if (financialInformation != null){
                    customer.setInformationList(financialInformation);
                }
            }
            //客户转让
            if (customer.getFnumber() != null && !customer.getFnumber().trim().isEmpty()){
                List<CustomerTransfer> transfer = customerTransferMapper.selectByCustomerCodeList(customer.getFnumber());
                if (transfer != null && !transfer.isEmpty()){
                    customer.setCustomerTransfer(transfer);
                }
            }
            //供应商联系人
            if (customer.getFnumber() != null && !customer.getFnumber().trim().isEmpty()){
                List<SupplierContactBase> contactBases  = supplierContactMapper.selectByContractBse(customer.getFnumber());
                if (contactBases != null && !contactBases.isEmpty()){
                    customer.setSupplierContactList(contactBases);
                }
            }
        }
        return customer;
    }

    /**
     * 根据ID删除客户
     *
     * @param id 客户ID
     * @return 是否删除成功
     */
    @Override
    public Boolean removeById(String id) {
        Customer customer = customerMapper.selectById(Long.valueOf(id));
        if (customer.getFnumber() != null){
            if(customer.getFKhlogo() != null && !customer.getFKhlogo().trim().isEmpty()){
                //删除客户logo
                minioUtil.deleteFile(customer.getFKhlogo());
            }
            if (customer.getFZmmttp1() != null && !customer.getFZmmttp1().trim().isEmpty()){
                //删除正唛外箱
                minioUtil.deleteFile(customer.getFZmmttp1());
            }
            if (customer.getFZmmttp2() != null && !customer.getFZmmttp2().trim().isEmpty()){
                //删除正唛内箱
                minioUtil.deleteFile(customer.getFZmmttp2());
            }
            if (customer.getFcmmttp1() != null && !customer.getFcmmttp1().trim().isEmpty()){
                //删除侧唛外箱
                minioUtil.deleteFile(customer.getFcmmttp1());
            }
            if (customer.getFcmmttp2() != null && !customer.getFcmmttp2().trim().isEmpty()){
                //删除侧唛内箱
                minioUtil.deleteFile(customer.getFcmmttp2());
            }

            //供应商付款信息
            String supplierNumber = customer.getFnumber();
            FinancialInformation financialInformation = financialInformationMapper.selectBySupplierNumber(supplierNumber);
            if (financialInformation != null){
                financialInformationMapper.deleteSupplierContact(supplierNumber);
            }
            //客户转让
            String customerCode = customer.getFnumber();
            List<CustomerTransfer> customerTransfer = customerTransferMapper.selectByCustomerCodeList(customerCode);
            if (customerTransfer != null && !customerTransfer.isEmpty()){
                customerTransfer.forEach(tr ->{
                    customerTransferMapper.deleteCustomerTransfer(tr.getCustomerCode());
                });
            }

            //客户联系人
            String customerNumber = customer.getFnumber();
            List<SupplierContactBase> supplier = supplierContactMapper.selectByinformationList(customerNumber);
            if(supplier != null && !supplier.isEmpty()){
                supplier.forEach(su ->{
                    supplierContactMapper.deleteByCustomerNumber(su.getCustomerNumber());
                });
            }
        }
        return customerMapper.removeById(id);
    }


    @Override
    public Result create(Customer customer){
        Assert.notNull(customer, "客户对象不能为空");
        Assert.notNull(customer.getFname(), "客户名称不能为空");
        Assert.notNull(customer.getFshortName(), "客户简称不能为空");

        // 检查客户编码是否已存在
        if (customer.getFnumber() != null && !customer.getFnumber().isEmpty()) {
            Customer existingCustomer = customerMapper.selectByFnumber(customer.getFnumber());
            if (existingCustomer != null) {
                return Result.error("客户编码已存在: " + customer.getFnumber());
            }
        }

        // 在字典转换前处理freceiveCurrId字段，确保不会传入空字符串
        if (customer.getFreceiveCurrId() != null && customer.getFreceiveCurrId().trim().isEmpty()) {
            customer.setFreceiveCurrId(null);
        }

        // 批量处理字典字段转换
        DictConvertUtil.convertCustomerDictFields(customer, bymaterialDictionaryMapper, settlementMethodMapper);

        //客户来源
        if (customer.getFKhly() != null) {
            String kingdee = customer.getFKhly();
            BymaterialDictionary  materialDictionary = bymaterialDictionaryMapper.selectByCategoryAndName(kingdee);
            if (materialDictionary != null) {
                customer.setFKhly(materialDictionary.getKingdee());
                customer.setFLy(materialDictionary.getName());
            }
        }

        //供应商付款信息
        if (customer.getInformationList() != null){
            customer.getInformationList().setSupplierNumber(customer.getFnumber());
            financialInformationMapper.insert(customer.getInformationList());
        }

        //客户转让
        List<CustomerTransfer> transfer = customer.getCustomerTransfer();
        if (transfer != null && !transfer.isEmpty()){
            transfer.forEach(fer ->{
                fer.setCustomerCode(customer.getFnumber());
                customerTransferMapper.insert(fer);
            });
        }

        //客户联系人
        List<SupplierContactBase> contactBases = customer.getSupplierContactList();
        if (contactBases != null && !contactBases.isEmpty()){
            contactBases.forEach(bases ->{
                bases.setCustomerNumber(customer.getFnumber());
                supplierContactMapper.insert(bases);
            });

        }

        //销售员
        if (customer.getFseller() != null){
            Employee employee = employeeMapper.selectBySalesmanId(customer.getFseller());
            customer.setFsalDeptId(Long.valueOf(employee.getFBw()));
        }

        //正唛描述
        customer.setFZmmttpMs(customer.getZmmtwx()+","+customer.getZmmtnx());
        // 侧唛描述
        customer.setFCmmttpMs(customer.getFcmmtwx()+","+customer.getFcmmtnx());

        //获取当前登入人
        LoginUser loginUser = LoginHelper.getLoginUser();
        SysUserBo sysAccountUser = sysUserMapper.selectNickname(loginUser.getUserId());
        //k3员工id
        customer.setFcreatorId(sysAccountUser.getStaffId());
        //创建时间
        customer.setFcreateDate(LocalDateTime.now());
        int result = customerMapper.insert(customer);
        try {
            //推送飞书消息提醒
            Map<String, String> fields = new HashMap<>();
            fields.put("客户名称",customer.getFname());
            fields.put("供应商编码", customer.getFnumber());
            fields.put("提交人", sysAccountUser.getNickName());
            fields.put("状态", "推送成功");
            batchGetEmployeeConfig.sendCommonPushCard(
                    "客户推送",
                fields,
                "http://113.46.194.126/k3cloud",
                "打开金蝶系统"
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        return Result.create(result > 0);
    }

    @Override
    public boolean update(Customer customer) {
        //供应商付款信息
        if (customer.getInformationList() != null){
            //国家
            if (customer.getInformationList().getNation() != null && !customer.getInformationList().getNation().trim().isEmpty()){
                if (validatorUtil.isAllChinese(customer.getInformationList().getNation()) == true){
                    String nameZh = customer.getInformationList().getNation();
                    Country country = countryMapper.selectFcountryList(nameZh);
                    customer.getInformationList().setNation(country.getId());
                }else {
                    Country country = countryMapper.selectById(customer.getInformationList().getNation());
                    customer.getInformationList().setNation(country.getId());
                }
            }
            //更新供应商付款信息
            financialInformationMapper.updateById(customer.getInformationList());
        }else {
            log.error("客户付款信息为空");
        }

        //客户转让
        List<CustomerTransfer> transfer = customer.getCustomerTransfer();
        if (transfer != null && !transfer.isEmpty()){
            transfer.forEach(fer ->{
                //customerCode
                if (fer.getCustomerCode() != null){
                    customerTransferMapper.update(fer);
                }else {
                    fer.setCustomerCode(customer.getFnumber());
                    customerTransferMapper.insert(fer);
                }
            });
        }else {
            log.error("客户转让为空");
        }

        //客户联系人
        List<SupplierContactBase> contactBases = customer.getSupplierContactList();
        if (contactBases != null && !contactBases.isEmpty()){
            contactBases.forEach(bases ->{
                if (bases.getCustomerNumber() != null && !bases.getCustomerNumber().trim().isEmpty()){
                    supplierContactMapper.updateById(bases);
                }
                else {
                    bases.setCustomerNumber(customer.getFnumber());
                    supplierContactMapper.insert(bases);
                }
            });
        }else {
            log.error("客户联系人");
        }

        //国家
        if(customer.getFcountry() != null && !customer.getFcountry().trim().isEmpty()){
            if (validatorUtil.isAllChinese(customer.getFcountry()) == true){
                String nameZh = customer.getFcountry();
                Country country = countryMapper.selectFcountryList(nameZh);
                customer.setFcountry(country.getId());
            }else {
                Country country = countryMapper.selectById(customer.getFcountry());
                customer.setFcountry(country.getId());
            }
        }

        //抵运国家
        if (customer.getFdygj() != null && !customer.getFdygj().trim().isEmpty()){
            if (validatorUtil.isAllChinese(customer.getFdygj()) == true){
                String nameZh = customer.getFdygj();
                Country country = countryMapper.selectFcountryList(nameZh);
                customer.setFdygj(country.getId());
            }else {
                Country country = countryMapper.selectById(customer.getFdygj());
                customer.setFdygj(country.getId());
            }
        }


        //客户来源
        if (validatorUtil.isAllChinese(customer.getFKhly()) == true) {
            String productName = customer.getFKhly();
            BymaterialDictionary  materialDictionary = bymaterialDictionaryMapper.selectByCategoryproductName(productName);
            if (materialDictionary != null) {
                customer.setFKhly(materialDictionary.getKingdee());
                customer.setFLy(materialDictionary.getName());
            }
        }else {
            String productName = customer.getFKhly();
            BymaterialDictionary  materialDictionary = bymaterialDictionaryMapper.selectByCategoryproductName(productName);
            if (materialDictionary != null) {
                customer.setFKhly(materialDictionary.getKingdee());
                customer.setFLy(materialDictionary.getName());
            }
        }

        //客户类别
        if (validatorUtil.isAllChinese(customer.getFcustTypeId()) == true){
            String productName =customer.getFgroupId();
            BymaterialDictionary  materialDictionary = bymaterialDictionaryMapper.selectByCategoryproductName(productName);
            if (materialDictionary != null) {
                customer.setFcustTypeId(materialDictionary.getKingdee());
            }
        }else {
            String productName = customer.getFcustTypeId();
            BymaterialDictionary materialDictionary = bymaterialDictionaryMapper.selectByCategoryproductName(productName);
            if (materialDictionary != null) {
                customer.setFcustTypeId(materialDictionary.getKingdee());
            }
        }

        //客户分组
        if (validatorUtil.isAllDigits(customer.getFgroupId()) == true){
            String kingdee = customer.getFgroupId();
            BymaterialDictionary  materialDictionary = bymaterialDictionaryMapper.selectByCategoryAndName(kingdee);
            if (materialDictionary != null) {
                customer.setFgroupId(materialDictionary.getKingdee());
            }
        }else {
            String productName = customer.getFgroupId();
            BymaterialDictionary materialDictionary = bymaterialDictionaryMapper.selectByCategoryproductName(productName);
            if (materialDictionary != null) {
                customer.setFgroupId(materialDictionary.getKingdee());
            }
        }

        //包装方式
        if (validatorUtil.isAllDigits(customer.getFBzfs()) == true){
            String kingdee = customer.getFgroupId();
            String category = "manner_packing";
            BymaterialDictionary  materialDictionary = bymaterialDictionaryMapper.selectByKingde(kingdee,category);
            if (materialDictionary != null) {
                customer.setFBzfs(materialDictionary.getKingdee());
            }
        }else {
            String productName = customer.getFBzfs();
            BymaterialDictionary materialDictionary = bymaterialDictionaryMapper.selectByCategoryproductName(productName);
            if (materialDictionary != null) {
                customer.setFBzfs(materialDictionary.getKingdee());
            }
        }


        //结算币别 ftradingCurrId
        if (validatorUtil.isAllDigits(customer.getFtradingCurrId()) == true){
            String kingdee = customer.getFtradingCurrId();
            String category = "currency";
            BymaterialDictionary  materialDictionary = bymaterialDictionaryMapper.selectByKingde(kingdee,category);
            if (materialDictionary != null) {
                customer.setFtradingCurrId(materialDictionary.getKingdee());
            }
        }else {
            String productName = customer.getFtradingCurrId();
            BymaterialDictionary materialDictionary = bymaterialDictionaryMapper.selectByCategoryproductName(productName);
            if (materialDictionary != null) {
                customer.setFtradingCurrId(materialDictionary.getKingdee());
            }
        }

        //收款条件
        if (validatorUtil.isAllDigits(customer.getFrecConditionId()) == true){
            String kingdee = customer.getFrecConditionId();
            String category = "collection_terms";
            BymaterialDictionary  materialDictionary = bymaterialDictionaryMapper.selectByKingde(kingdee,category);
            if (materialDictionary != null) {
                customer.setFrecConditionId(materialDictionary.getKingdee());
            }
        }else {
            String productName = customer.getFrecConditionId();
            BymaterialDictionary materialDictionary = bymaterialDictionaryMapper.selectByCategoryproductName(productName);
            if (materialDictionary != null) {
                customer.setFrecConditionId(materialDictionary.getKingdee());
            }
        }

        //销售员
        if (validatorUtil.isAllDigits(customer.getFseller()) == true){
            Employee employee = employeeMapper.selectBySalesmanId(customer.getFseller());
            customer.setFsalDeptId(Long.valueOf(employee.getFBw()));
            customer.setFseller(employee.getSalesmanId());
        }else {
            Employee employee = employeeMapper.selectByFName(customer.getFseller());
            customer.setFsalDeptId(Long.valueOf(employee.getFBw()));
            customer.setFseller(employee.getSalesmanId());
        }

        //正唛描述
        customer.setFZmmttpMs(customer.getZmmtwx()+","+customer.getZmmtnx());
        // 侧唛描述
        customer.setFCmmttpMs(customer.getFcmmtwx()+","+customer.getFcmmtnx());

        //获取当前登入人
        LoginUser loginUser = LoginHelper.getLoginUser();
        SysUserBo sysAccountUser = sysUserMapper.selectNickname(loginUser.getUserId());
        //k3员工id
        customer.setFmodifierId(sysAccountUser.getStaffId());
        return customerMapper.updateById(customer) > 0;
    }

    /**
     * 获取客户列表（支持条件和分页）
     * @param pageQuery 查询条件对象
     * @return 客户列表
     */
    @Override
    public TableDataInfo<Customer> getList(CustomerBo customer, PageQuery pageQuery) {
        // 1. 查询原始 CustomerVo 分页数据（实际类型为 CustomerVo）
        Page<Customer> records = customerMapper.selectList(pageQuery.build(),this.buildQueryWrapper(customer));

        if (records.getRecords() != null && !records.getRecords().isEmpty()){
            records.getRecords().forEach(re ->{
                //供应商付款信息
                String supplierNumber = re.getFnumber();
                if (supplierNumber != null && !supplierNumber.trim().isEmpty()){
                    re.setInformationList(financialInformationMapper.selectBySupplierNumber(supplierNumber));
                }

                //客户转让
                String customerCode = re.getFnumber();
                if (customerCode != null && !customerCode.trim().isEmpty()){
                    re.setCustomerTransfer(customerTransferMapper.selectByCustomerCodeList(customerCode));
                }

                //客户联系人
                String customerNumber = re.getFnumber();
                if (customerNumber != null && !customerNumber.trim().isEmpty()){
                    re.setSupplierContactList (supplierContactMapper.selectByinformationList(customerNumber));
                }
            });
        }

        return TableDataInfo.build(records);
    }

    /**
     * 根据条件分页查询用户列表
     *
     * @param customer 用户信息
     * @return 用户信息集合信息
     */
    public List<CustomerVo> selectUserList(CustomerBo customer) {
        return customerMapper.selectCustomerList(this.buildQueryWrapper(customer));
    }

    /**
     * @param customer 查询条件对象
     * @return 客户列表
     */
    private Wrapper<Customer> buildQueryWrapper(CustomerBo customer) {
        QueryWrapper<Customer> wrapper = Wrappers.query();
        wrapper
            .eq(ObjectUtil.isNotNull(customer.getId()),"bcr.id", customer.getId())
            .like(StringUtils.isNotBlank(customer.getFnumber()), "bcr.fnumber", customer.getFnumber())
            .like(StringUtils.isNotBlank(customer.getFname()), "bcr.fname", customer.getFname())
            .like(StringUtils.isNotBlank(customer.getFshortName()), "bcr.fshort_name", customer.getFshortName())
            .eq(ObjectUtil.isNotNull(customer.getFseller()),"bcr.fseller", customer.getFseller())
            .eq(ObjectUtil.isNotNull(customer.getFkhly()),"bcr.f_khly", customer.getFkhly())
            .eq(ObjectUtil.isNotNull(customer.getFcustTypeId()),"bcr.fcustTypeId", customer.getFcustTypeId())
            .orderByDesc("bcr.fcreate_date");
        return wrapper;
    }

    /**
     * 同步金蝶客户联系人信息
     */

    public Result pushCustomerListToK3(List<Customer> customers) {
        if (customers == null || customers.isEmpty()) {
            return Result.error("客户列表为空");
        }

        List<String> successList = new ArrayList<>();
        List<String> errorList = new ArrayList<>();

        for (Customer customer : customers) {
            try {
                // 使用表单处理器直接处理推送
                com.ruoyi.business.util.Result result = customerFormProcessor.processForm(null, customer);

                if (result.isSuccess()) {
                    successList.add(customer.getFnumber() != null ? customer.getFnumber() : "未知编号");
                } else {
                    errorList.add(customer.getFnumber() != null ? customer.getFnumber() : "未知编号");
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorList.add(customer.getFnumber() != null ? customer.getFnumber() : "未知编号");
            }
        }

        StringBuilder resultMsg = new StringBuilder();
        resultMsg.append("批量推送完成，成功: ").append(successList.size()).append("个，失败: ").append(errorList.size()).append("个");
        if (!successList.isEmpty()) {
            resultMsg.append("，成功列表: ").append(String.join(",", successList));
        }
        if (!errorList.isEmpty()) {
            resultMsg.append("，失败列表: ").append(String.join(",", errorList));
        }

        if (errorList.isEmpty()) {
            return Result.success(resultMsg.toString());
        } else {
            return Result.error(resultMsg.toString());
        }
    }

    @Override
    public int syncCustomerContactList() {
        List<List<Object>> queryCustomerContactList = k3Config.queryCommonContactList();
        if (queryCustomerContactList == null || queryCustomerContactList.isEmpty()) {
            log.info("没有获取到客户联系人数据");
            return 0;
        }
        log.info("开始同步客户联系人信息，共 {} 条", queryCustomerContactList.size());

        int total = 0;
        int insertCount = 0;
        int updateCount = 0;

        for (List<Object> rowData : queryCustomerContactList) {
            if (rowData == null || rowData.size() < 14) {
                continue;
            }

            SupplierContactBase contactBase = new SupplierContactBase();

            // 映射K3字段到实体

            contactBase.setK3id(getLong(rowData.get(0)));                // FCONTACTID -> 金蝶主键ID
            contactBase.setCustomerNumber(getString(rowData.get(1)));    // FNumber -> 客户编码
            contactBase.setContactName(getString(rowData.get(2)));       // FName -> 联系人
            contactBase.setCreator(getString(rowData.get(3)));           // FCreatorId -> 创建人
            contactBase.setCreationDate(getLocalDateTime(rowData.get(4))); // FCreateDate -> 创建日期
            contactBase.setRemark(getString(rowData.get(5)));          // FDescription -> 描述
            contactBase.setGender(getString(rowData.get(6)));            // Fex -> 性别
            contactBase.setPosition(getString(rowData.get(7)));          // FPost -> 职务
            contactBase.setType(getString(rowData.get(8)));              // FCompanyType -> 类型
            contactBase.setMobile(getString(rowData.get(9)));            // FMobile -> 手机
            contactBase.setEmail(getString(rowData.get(10)));             // FEmail -> 电子邮箱
            contactBase.setLocationName(getString(rowData.get(11)));     // FBizLocation -> 地点名称
            contactBase.setAddress(getString(rowData.get(12)));          // FBizAddress -> 通讯地址
            contactBase.setQq(getString(rowData.get(13)));               // F_ora_Text -> QQ
            contactBase.setVx(getString(rowData.get(14)));               // F_wx -> 微信

            // 检查是否已存在该联系人（根据客户编码和联系人姓名）
            SupplierContactBase existing = supplierContactMapper.selectByCustomerNumberAndContactName(
                contactBase.getCustomerNumber(), contactBase.getContactName());

            int result;
            if (existing != null) {
                // 已存在 → 更新

                result = supplierContactMapper.updateSupplierContact(contactBase);
                if (result > 0) updateCount++;
            } else {
                // 不存在 → 新增
                result = supplierContactMapper.insertSupplierContact(contactBase);
                if (result > 0) insertCount++;
            }

            if (result > 0) {
                total += result;
            }
        }

        log.info("客户联系人同步完成，新增 {} 条，更新 {} 条，共 {} 条", insertCount, updateCount, total);
        return total;
    }

    // ==================== 工具方法 ====================

    /**
     * 转换K3数据为银行信息实体
     */
    private FinancialInformation convertToFinancialInformation(List<Object> objectList) {
        FinancialInformation financialInfo = new FinancialInformation();

        // 银行信息字段索引：0-6
        int idx = 0;
        financialInfo.setSupplierNumber(getString(objectList.get(idx++)));   // 0: FNumber (客户编码)
        financialInfo.setNation(getString(objectList.get(idx++)));           // 1: FCOUNTRY1
        financialInfo.setBankAccount(getString(objectList.get(idx++)));      // 2: FBANKCODE
        financialInfo.setAccountName(getString(objectList.get(idx++)));      // 3: FACCOUNTNAME
        financialInfo.setReceivingBank(getString(objectList.get(idx++)));    // 4: FBankTypeRec
        financialInfo.setBankAddress(getString(objectList.get(idx++)));      // 5: FOpenAddressRec
        financialInfo.setOpeningBank(getString(objectList.get(idx)));        // 6: FOPENBANKNAME

        return financialInfo;
    }

    /**
     * 校验银行信息是否有效
     */
    private boolean hasValidBankInfo(FinancialInformation financialInfo) {
        if (financialInfo == null || financialInfo.getSupplierNumber() == null) {
            return false;
        }
        // 至少有一个银行相关字段不为空
        return financialInfo.getBankAccount() != null ||
               financialInfo.getAccountName() != null ||
               financialInfo.getOpeningBank() != null ||
               financialInfo.getReceivingBank() != null;
    }

    private String getString(Object obj) {
        return obj == null ? null : obj.toString();
    }

    private Long getLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Long) return (Long) obj;
        if (obj instanceof Integer) return ((Integer) obj).longValue();
        if (obj instanceof Number) return ((Number) obj).longValue();
        try {
            return Long.parseLong(obj.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer getInteger(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof Number) return ((Number) obj).intValue();
        try {
            return Integer.parseInt(obj.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal getBigDecimal(Object obj) {
        if (obj == null) return null;
        try {
            return new BigDecimal(obj.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime getLocalDateTime(Object obj) {
        return DateUtils.parseLocalDateTime(obj);
    }

    private LocalDate getLocalDate(Object obj) {
        return DateUtils.parseLocalDate(obj);
    }



}
