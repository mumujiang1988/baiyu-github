package com.ruoyi.business.Component;

import com.ruoyi.business.entity.FinancialInformation;
import com.ruoyi.business.entity.Supplier;
import com.ruoyi.business.entity.SupplierContactBase;
import com.ruoyi.business.k3.config.AbstractK3FormProcessor;
import org.springframework.stereotype.Component;

import java.util.*;

import static cn.dev33.satoken.SaManager.log;

/**
 * 供应商表单处理器
 * 负责处理BD_Supplier表单的构建和提交
 * 继承AbstractK3FormProcessor，实现供应商特定的业务逻辑
 */
@Component
public class SupplierFormProcessor extends AbstractK3FormProcessor<Supplier> {

    /**
     * 返回供应商表单ID
     * @return 固定返回"BD_Supplier"
     */
    @Override
    protected String getFormId() {
        return "BD_Supplier";
    }

    /**
     * 获取文件字段名称列表
     * 供应商只有一个图片文件字段
     */
    @Override
    protected List<String> getFileFieldNames() {
        return Arrays.asList("FImageFileServer");
    }

    /**
     * 获取供应商单据编号
     * 用于文件上传时的BillNO字段
     */
    @Override
    protected String getDocumentNumber(Supplier formData) {
        return formData.getNumber() != null ? formData.getNumber() : "";
    }

    /**
     * 构建供应商模型数据
     * 将前端传入的Supplier对象转换为金蝶K3所需的格式
     */
    @Override
    protected Map<String, Object> buildModel(Supplier supplier) {
        log.info("开始构建供应商模型数据，供应商编号: {}", supplier.getNumber());

        Map<String, Object> model = new HashMap<>();

        // ================= 基础字段 =================
        model.put("FSupplierId", 0); // 新增供应商固定为0
        model.put("FNumber", supplier.getNumber() != null ? supplier.getNumber() : "");
        model.put("FName", supplier.getName() != null ? supplier.getName() : "");
        model.put("FShortName", supplier.getAbbreviation() != null ? supplier.getAbbreviation() : "");


        // 添加 F_fze 字段（与 FBaseInfo 同级）- 必须存在，即使为空
        Map<String, Object> fzeObj = new HashMap<>();
        if (supplier.getManager() != null && !supplier.getManager().isEmpty()) {
            fzeObj.put("FSTAFFNUMBER", supplier.getManager());
        } else {
            // 如果manager为空，使用空的FSTAFFNUMBER
            fzeObj.put("FSTAFFNUMBER", "");
        }
        model.put("F_fze", fzeObj);

        // ================= 业务字段 =================
        // 国外占比 - 必须存在，即使为空
        model.put("F_gwzb", supplier.getForeignShare() != null ? supplier.getForeignShare() : "");
        // 开票品名 - 必须存在，即使为空
        model.put("F_KPPM", supplier.getInvoiceName() != null ? supplier.getInvoiceName() : "");
        // 工厂问题 - 必须存在，即使为空
        model.put("Fgcwt", supplier.getContactInfo() != null ? supplier.getContactInfo() : "");
        // 新增原因 - 必须存在，即使为空
        model.put("Fxzyy1", supplier.getCause() != null ? supplier.getCause() : "");

        // ================= 字典字段（需要转换为金蝶编码格式）=================
        // 来源 - 必须存在，即使为空
        Map<String, Object> flyObj = new HashMap<>();
        if (supplier.getSource() != null && !supplier.getSource().isEmpty()) {
            flyObj.put("FNumber", supplier.getSource());
        } else {
            // 如果source为空，使用空的FNumber对象
            flyObj.put("FNumber", "");
        }
        model.put("Fly", flyObj);

        // 供应商分类（主表字段）- 必须存在，即使为空
        Map<String, Object> mainSupplierClassifyObj = new HashMap<>();
        if (supplier.getSupplierCategory() != null && !supplier.getSupplierCategory().isEmpty()) {
            mainSupplierClassifyObj.put("FNumber", supplier.getSupplierCategory());
        } else {
            // 如果supplierCategory为空，使用空的FNumber对象
            mainSupplierClassifyObj.put("FNumber", "");
        }
        model.put("FSupplierClassify", mainSupplierClassifyObj);

        // ================= 基础信息子表 =================
        Map<String, Object> baseInfo = new HashMap<>();
        baseInfo.put("FEntryId", 0);
        baseInfo.put("FRegisterCode", supplier.getBusinessRegistration() != null ? supplier.getBusinessRegistration() : "");  // 工商登记号
        baseInfo.put("FSOCIALCRECODE", supplier.getSocialCreditCode() != null ? supplier.getSocialCreditCode() : "");  // 统一社会信用代码
        baseInfo.put("FLegalPerson", supplier.getLegalPerson() != null ? supplier.getLegalPerson() : "");  // 法人代表
        // 添加通讯地址到基础信息子表
        baseInfo.put("FAddress", supplier.getAddress() != null ? supplier.getAddress() : "");
        // FFoundDate 应该是字符串格式，而不是 Date 对象
        if (supplier.getEstablishDate() != null) {
            // 将 Date 转换为字符串格式 "yyyy-MM-dd"
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            baseInfo.put("FFoundDate", sdf.format(supplier.getEstablishDate()));
        } else {
            baseInfo.put("FFoundDate", "");
        }

        // 添加负责人到基础信息子表（使用 FStaffId 字段）- 必须存在，即使为空
        Map<String, Object> staffObj = new HashMap<>();
        if (supplier.getManager() != null && !supplier.getManager().isEmpty()) {
            staffObj.put("FNumber", supplier.getManager());
        } else {
            // 如果manager为空，使用空的FNumber对象
            staffObj.put("FNumber", "");
        }
        baseInfo.put("FStaffId", staffObj);

        // 添加供应商分类到基础信息子表 - 必须存在，即使为空
        Map<String, Object> baseInfoSupplierClassifyObj = new HashMap<>();
        if (supplier.getSupplierCategory() != null && !supplier.getSupplierCategory().isEmpty()) {
            baseInfoSupplierClassifyObj.put("FNumber", supplier.getSupplierCategory());
        } else {
            // 如果supplierCategory为空，使用空的FNumber对象
            baseInfoSupplierClassifyObj.put("FNumber", "");
        }
        baseInfo.put("FSupplierClassify", baseInfoSupplierClassifyObj);

        // 添加主营产品到基础信息子表（使用 F_ora_Text2 字段）- 必须存在，即使为空
        baseInfo.put("F_ora_Text2", supplier.getMainProduct() != null ? supplier.getMainProduct() : "");

        // 添加供应类别到基础信息子表 - 必须存在，即使为空
        baseInfo.put("FSupplyClassify", supplier.getSupplyType() != null ? supplier.getSupplyType() : "");

        // 添加注册地址到基础信息子表 - 必须存在，即使为空
        baseInfo.put("FRegisterAddress", supplier.getRegion() != null ? supplier.getRegion() : "");

        // 添加省份信息到基础信息子表 - 必须存在，即使为空
        Map<String, Object> provincialObj = new HashMap<>();
        provincialObj.put("FNumber", "");
        baseInfo.put("FProvincial", provincialObj);

        model.put("FBaseInfo", baseInfo);

        // ================= 业务信息子表 =================
        Map<String, Object> businessInfo = new HashMap<>();
        businessInfo.put("FEntryId", 0);
        model.put("FBusinessInfo", businessInfo);

        // ================= 财务信息 =================
        Map<String, Object> financeInfo = new HashMap<>();
        financeInfo.put("FEntryId", 0);
        // 将 FInvoiceType 和 FTaxType 放在 FFinanceInfo 中
        // FInvoiceType 应该是直接的字符串值，而不是对象 - 必须存在，即使为空
        if (supplier.getInvoiceType() != null && !supplier.getInvoiceType().isEmpty()) {
            financeInfo.put("FInvoiceType", supplier.getInvoiceType());         // 发票类型
        } else {
            // 即使值为空，也要添加一个空字符串，以满足金蝶的要求
            financeInfo.put("FInvoiceType", "");
        }
        // FTaxType 应该使用 FNumber 对象格式 - 必须存在，即使为空
        putFNumber(financeInfo, "FTaxType", supplier.getTaxCategory() != null ? supplier.getTaxCategory() : "");         // 税分类
        // 其他财务字段 - 必须存在，即使为空
        putFNumber(financeInfo, "FPayCurrencyId", supplier.getSettlementCurrency() != null ? supplier.getSettlementCurrency() : "");      // 结算币别
        putFNumber(financeInfo, "FSettleTypeId", supplier.getSettlementMethod() != null ? supplier.getSettlementMethod() : "");         // 结算方式
        putFNumber(financeInfo, "FPayCondition", supplier.getPaymentTerms() != null ? supplier.getPaymentTerms() : "");             // 付款条件
        putFNumber(financeInfo, "FSettleId", supplier.getSettlementParty() != null ? supplier.getSettlementParty() : "");              // 结算方
        putFNumber(financeInfo, "FChargeId", supplier.getPayee() != null ? supplier.getPayee() : "");                        // 收款方
        putFNumber(financeInfo, "FTaxRateId", supplier.getDefaultTaxRate() != null ? supplier.getDefaultTaxRate() : "");              // 默认税率
        model.put("FFinanceInfo", financeInfo);

        // ================= 银行信息子表（数组） =================
        List<Map<String, Object>> bankInfoList = new ArrayList<>();
        // 添加一条空的银行信息记录
        if (supplier.getFinancialInformation() != null && !supplier.getFinancialInformation().isEmpty()) {
            for (FinancialInformation fi : supplier.getFinancialInformation()) {

                Map<String, Object> bankInfo = new HashMap<>();
                bankInfo.put("FBankId", 0);

                // 银行账号（必填）
                bankInfo.put("FBankCode", fi.getBankAccount() != null ? fi.getBankAccount() : "");

                // 户名（必填）
                bankInfo.put("FBankHolder", fi.getAccountName() != null ? fi.getAccountName() : "");

                // 收款银行（基础资料，必须对象）
                Map<String, Object> bankTypeRec = new HashMap<>();
                bankTypeRec.put("FNUMBER",
                    fi.getReceivingBank() != null ? fi.getReceivingBank() : "");
                bankInfo.put("FBankTypeRec", bankTypeRec);

                // 开户行
                bankInfo.put("FOpenBankName",
                    fi.getOpeningBank() != null ? fi.getOpeningBank() : "");

                // 开户地址
                bankInfo.put("FOpenAddressRec",
                    fi.getBankAddress() != null ? fi.getBankAddress() : "");

                bankInfoList.add(bankInfo);
            }
            model.put("FBankInfo", bankInfoList);

        }
        // ================= 地点信息子表（数组） =================
        List<Map<String, Object>> locationInfoList = new ArrayList<>();
        // 添加一条空的地点信息记录
        Map<String, Object> locationInfo = new HashMap<>();
        locationInfo.put("FLocationId", 0);
        locationInfo.put("FLocName", "");

        // FLocNewContact 需要使用对象格式 {"FNUMBER": ""} - 必须存在，即使为空
        Map<String, Object> locNewContact = new HashMap<>();
        locNewContact.put("FNUMBER", "");
        locationInfo.put("FLocNewContact", locNewContact);

        locationInfo.put("FLocAddress", "");
        locationInfo.put("FLocMobile", "");
        locationInfoList.add(locationInfo);
        model.put("FLocationInfo", locationInfoList);

        // ================= 联系人信息子表 =================
        List<Map<String, Object>> contactList = new ArrayList<>();

        if (supplier.getContactInformation() != null && !supplier.getContactInformation().isEmpty()) {
            for (SupplierContactBase c : supplier.getContactInformation()) {

                Map<String, Object> contact = new HashMap<>();
                contact.put("FContactId", 0);
                // 联系人姓名（必填）
                contact.put("FContact",
                    c.getContactName() != null ? c.getContactName() : "");
                // 手机（必填）
                contact.put("FMobile",
                    c.getMobile() != null ? c.getMobile() : "");
                // 可选字段（有就传）
                contact.put("FTel", c.getPhone() != null ? c.getPhone() : "");
                contact.put("FEMail", c.getEmail() != null ? c.getEmail() : "");
                contact.put("FPost", c.getPosition() != null ? c.getPosition() : "");
                contactList.add(contact);
            }
            model.put("FSupplierContact", contactList);

        }
        log.debug("供应商模型构建完成: {}", model);
        return model;
    }

    /**
     * 辅助方法：将普通字段转换为金蝶FNumber格式
     * 金蝶中很多字段需要以 { "FNumber": "값" } 的格式传递
     *
     * @param model 模델 객체
     * @param field 字段명
     * @param number 字段값
     */
    private void putFNumber(Map<String, Object> model, String field, String number) {
        // 总是添加字段，即使number为空
        Map<String, Object> inner = new HashMap<>();
        if (number != null && !number.isEmpty()) {
            inner.put("FNumber", number);
            log.debug("字段 {} 转换为FNumber格式: {}", field, number);
        } else {
            // 如果number为空，使用空的FNumber对象
            inner.put("FNumber", "");
            log.debug("字段 {} 的값为空，添加空的FNumber对象", field);
        }
        model.put(field, inner);
    }

    /**
     * 构建联系人明细列表
     * 将联系人对象转换为金蝶子表格式
     *
     * @param contacts 联系人列表
     * @return 金蝶格式的明细数组
     */
    private List<Map<String, Object>> buildContactEntries(List<SupplierContactBase> contacts) {
        List<Map<String, Object>> entries = new ArrayList<>();

        for (SupplierContactBase contact : contacts) {
            Map<String, Object> entry = new HashMap<>();

            // 基础联系人字段（根据金蝶实际字段名调整），并确保即使值为空也有默认值
            entry.put("FContact", contact.getContactName() != null ? contact.getContactName() : "");           // 联系人姓名
            entry.put("FPost", contact.getPosition() != null ? contact.getPosition() : "");                 // 职务
            entry.put("FTel", contact.getPhone() != null ? contact.getPhone() : "");                     // 电话
            entry.put("FMobile", contact.getMobile() != null ? contact.getMobile() : "");                 // 手机
            entry.put("FEMail", contact.getEmail() != null ? contact.getEmail() : "");                   // 邮箱
            entry.put("F_ora_BaseProperty", contact.getGender() != null ? contact.getGender() : "");      // 性别（如果金蝶有此字段）
            entry.put("FLocAddress", contact.getAddress() != null ? contact.getAddress() : "");            // 通讯地址

            // 如果有地点名称字段
            if (contact.getLocationName() != null && !contact.getLocationName().isEmpty()) {
                entry.put("FLocationName", contact.getLocationName());
            }

            // 如果有QQ字段
            if (contact.getQq() != null && !contact.getQq().isEmpty()) {
                entry.put("FQQ", contact.getQq());
            }

            entries.add(entry);
            log.debug("添加联系人明细: {}", contact.getContactName());
        }

        return entries;
    }

    /**
     * 构建财务信息明细列表
     * 将付款信息对象转换为金蝶子表格式
     *
     * @param financialInfoList 财务信息列表
     * @return 金蝶格式的明细数组
     */
    private List<Map<String, Object>> buildFinancialEntries(List<FinancialInformation> financialInfoList) {
        List<Map<String, Object>> entries = new ArrayList<>();

        for (FinancialInformation info : financialInfoList) {
            Map<String, Object> entry = new HashMap<>();

            // 财务信息字段（根据 querypaymentList 返回的字段调整）
            // 查询字段：FNumber,FBankCountry,FBankCode,FBankHolder,FBankTypeRec,FOpenAddressRec,FOpenBankName

            // 映射到实际字段，并确保即使值为空也有默认값
            entry.put("FBankCode", info.getBankAccount() != null ? info.getBankAccount() : "");         // 银行账号
            entry.put("FBankHolder", info.getAccountName() != null ? info.getAccountName() : "");       // 账户名称/开户名
            entry.put("FOpenAddressRec", info.getBankAddress() != null ? info.getBankAddress() : "");   // 开户行地址
            entry.put("FOpenBankName", info.getOpeningBank() != null ? info.getOpeningBank() : "");     // 开户银行名称

            // FBankCountry 需要使用对象格式 {"FNumber": ""}
            Map<String, Object> bankCountry = new HashMap<>();
            if (info.getNation() != null && !info.getNation().isEmpty()) {
                bankCountry.put("FNumber", info.getNation());
            } else {
                // 即使值为空，也要添加一个空的FNumber对象，以满足金蝶的要求
                bankCountry.put("FNumber", "");
            }
            entry.put("FBankCountry", bankCountry);

            // FBankTypeRec 需要使用对象格式 {"FNUMBER": ""}
            Map<String, Object> bankTypeRec = new HashMap<>();
            if (info.getReceivingBank() != null && !info.getReceivingBank().isEmpty()) {
                bankTypeRec.put("FNUMBER", info.getReceivingBank());
            } else {
                // 即使值为空，也要添加一个空的FNUMBER对象，以满足金蝶的要求
                bankTypeRec.put("FNUMBER", "");
            }
            entry.put("FBankTypeRec", bankTypeRec);

            // 添加 FBankDetail 字段（如果需要）
            // Map<String, Object> bankDetail = new HashMap<>();
            // bankDetail.put("FNUMBER", "");
            // entry.put("FBankDetail", bankDetail);

            entries.add(entry);
            log.debug("添加财务信息明细: 账户名={}, 账号={}",
                info.getAccountName(), info.getBankAccount());
        }

        return entries;
    }
}
