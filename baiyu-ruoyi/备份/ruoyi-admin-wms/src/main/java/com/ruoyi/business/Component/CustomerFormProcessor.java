package com.ruoyi.business.Component;

import com.ruoyi.business.emen.SupplierEnums;
import com.ruoyi.business.entity.BymaterialDictionary;
import com.ruoyi.business.entity.Country;
import com.ruoyi.business.entity.Customer;
import com.ruoyi.business.entity.Employee;
import com.ruoyi.business.k3.config.AbstractK3FormProcessor;
import com.ruoyi.business.mapper.BymaterialDictionaryMapper;
import com.ruoyi.business.mapper.CountryMapper;
import com.ruoyi.business.mapper.EmployeeMapper;
import com.ruoyi.business.mapper.SettlementMethodMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;



/**
 * 客户表单处理器
 * 负责处理BD_Customer表单的构建和提交
 * 继承AbstractK3FormProcessor，实现客户特定的业务逻辑
 */
@Component
@Slf4j
public class CustomerFormProcessor extends AbstractK3FormProcessor<Customer> {

    @Autowired
    private BymaterialDictionaryMapper bymaterialDictionaryMapper;
    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private CountryMapper countryMapper;
    /**
     * 返回客户表单ID
     * @return 固定返回"BD_Customer"
     */
    @Override
    protected String getFormId() {
        return "BD_Customer";
    }

    /**
     * 获取文件字段名称列表
     * 客户没有图片文件字段
     */
    @Override
    protected List<String> getFileFieldNames() {
        return new ArrayList<>();
    }

    /**
     * 获取客户单据编号
     * 用于文件上传时的BillNO字段
     */
    @Override
    protected String getDocumentNumber(Customer formData) {
        return formData.getFnumber() != null ? formData.getFnumber() : "";
    }

    /**
     * 构建客户模型数据
     * 将前端传入的Customer对象转换为金蝶K3所需的格式
     */
    @Override
    public Map<String, Object> buildModel(Customer customer) {
        log.info("开始构建客户模型数据，客户编号: " + customer.getFnumber());
        log.info("客户名称字段值: '{}'", customer.getFname());
        log.info("客户名称是否为null: {}", customer.getFname() == null);
        log.info("客户名称是否为空字符串: {}", customer.getFname() != null && customer.getFname().isEmpty());

        Map<String, Object> model = new LinkedHashMap<>();

        // =================基础必填字段（按金蝶要求顺序）=================
        model.put("FCUSTID", 0); // 新增客户固定为 0
        
        // FCreateOrgId - 创建组织 ID
        Map<String, Object> createOrgObj = new LinkedHashMap<>();
        if (customer.getFcreateOrgId() != null) {
            createOrgObj.put("FNumber", customer.getFcreateOrgId().toString());
        } else {
            createOrgObj.put("FNumber", "100"); // 默认值
        }
        model.put("FCreateOrgId", createOrgObj);
        
        // FNumber - 客户编号
        if (customer.getFnumber() != null && !customer.getFnumber().isEmpty()) {
            model.put("FNumber", customer.getFnumber());
        }
        
        // FUseOrgId - 使用组织 ID
        Map<String, Object> useOrgObj = new LinkedHashMap<>();
        if (customer.getFcreateOrgId() != null) {
            useOrgObj.put("FNumber", customer.getFcreateOrgId().toString());
        } else {
            useOrgObj.put("FNumber", "100"); // 默认值
        }
        model.put("FUseOrgId", useOrgObj);
        
        // FName - 客户名称
        if (customer.getFname() != null && !customer.getFname().isEmpty()) {
            model.put("FName", customer.getFname());
        }
        
        // FShortName - 客户简称
        if (customer.getFshortName() != null && !customer.getFshortName().isEmpty()) {
            model.put("FShortName", customer.getFshortName());
        }

        // ================= 基础资料字段（按金蝶顺序）=================
        // FCOUNTRY - 国家
        if (customer.getFcountry() != null) {
            Country country = countryMapper.selectById(customer.getFcountry());
            if (country != null && country.getNameEn() != null) {
                Map<String, Object> countryObj = new LinkedHashMap<>();
                countryObj.put("FNumber", country.getNameEn());
                model.put("FCOUNTRY", countryObj);
            }
        }
        
        // FSELLER - 销售员
        if (customer.getFseller() != null) {
            Map<String, Object> sellerObj = new LinkedHashMap<>();
            Employee employee = employeeMapper.selectBySalesmanId(customer.getFseller());
            if (employee != null && employee.getFstaffNumber() != null) {
                sellerObj.put("FNumber", employee.getFstaffNumber());
                model.put("FSELLER", sellerObj);
            }
        }
        
        // F_khly - 客户来源
        if (customer.getFKhly() != null) {
            Map<String, Object> fkhlyobject = new LinkedHashMap<>();
            BymaterialDictionary bymaterialDictionary = bymaterialDictionaryMapper.selectByKingde(customer.getFKhly(), "customer_source");
            if (bymaterialDictionary != null && bymaterialDictionary.getKingdee() != null) {
                fkhlyobject.put("FNUMBER", bymaterialDictionary.getCode());
                model.put("F_khly", fkhlyobject);
            } else if (customer.getFKhly() != null && !customer.getFKhly().isEmpty()) {
                fkhlyobject.put("FNUMBER", customer.getFKhly());
                model.put("F_khly", fkhlyobject);
            }
        }
        
        // FTRADINGCURRID - 结算币别
        if (customer.getFtradingCurrId() != null) {
            Map<String, Object> tradingCurrencyObj = new LinkedHashMap<>();
            BymaterialDictionary bymaterialDictionary = bymaterialDictionaryMapper.selectByKingde(customer.getFtradingCurrId(), "currency");
            if (bymaterialDictionary != null && bymaterialDictionary.getName() != null) {
                SupplierEnums.CurrencyType currencyType = SupplierEnums.CurrencyType.valueOf(bymaterialDictionary.getName());
                tradingCurrencyObj.put("FNumber", currencyType.getCode());
                model.put("FTRADINGCURRID", tradingCurrencyObj);
            } else if (customer.getFtradingCurrId() != null && !customer.getFtradingCurrId().isEmpty()) {
                tradingCurrencyObj.put("FNumber", customer.getFtradingCurrId());
                model.put("FTRADINGCURRID", tradingCurrencyObj);
            }
        }
        
        // Fbzfs - 包装方式
        if (customer.getFBzfs() != null && !customer.getFBzfs().isEmpty()) {
            model.put("Fbzfs", customer.getFBzfs());
        }
        
        // Fsbsq - 是否申报
        if (customer.getFsbsq() != null) {
            model.put("Fsbsq", customer.getFsbsq());
        }
        
        // FGroup - 客户分组
        if (customer.getFgroupId() != null) {
            Map<String, Object> groupObj = new LinkedHashMap<>();
            BymaterialDictionary bymaterialDictionary = bymaterialDictionaryMapper.selectByKingdes(customer.getFgroupId());
            if (bymaterialDictionary != null && bymaterialDictionary.getKingdee() != null) {
                groupObj.put("FNumber", bymaterialDictionary.getCode());
                model.put("FGroup", groupObj);
            } else if (customer.getFgroupId() != null && !customer.getFgroupId().isEmpty()) {
                groupObj.put("FNumber", customer.getFgroupId());
                model.put("FGroup", groupObj);
            }
        }
        
        // FRECCONDITIONID - 收款条件
        if (customer.getFrecConditionId() != null) {
            Map<String, Object> receiveConditionObj = new LinkedHashMap<>();
            BymaterialDictionary bymaterialDictionary = bymaterialDictionaryMapper.selectByKingde(customer.getFrecConditionId(), "collection_terms");
            if (bymaterialDictionary != null && bymaterialDictionary.getCode() != null) {
                receiveConditionObj.put("FNumber", bymaterialDictionary.getCode());
                model.put("FRECCONDITIONID", receiveConditionObj);
            }
        }

        // ================= 其他业务字段 =================
        // FMATERIALID - 物料 ID（预留字段，如需要则添加）
        // model.put("FMATERIALID", customer.getFmaterialid());
        
        // FAddress - 地址
        if (customer.getFaddress() != null && !customer.getFaddress().isEmpty()) {
            model.put("FAddress", customer.getFaddress());
        }
        
        // F_fhyq - 货运要求
        if (customer.getFFhyq() != null && !customer.getFFhyq().isEmpty()) {
            model.put("F_fhyq", customer.getFFhyq());
        }
        
        // FWebsite - 网站
        if (customer.getFwebsite() != null && !customer.getFwebsite().isEmpty()) {
            model.put("FWebsite", customer.getFwebsite());
        }
        
        // Fpjskzq - 平均收款账期
        if (customer.getFpjskzq() != null) {
            model.put("Fpjskzq", customer.getFpjskzq());
        }
        
        // FTel - 电话
        if (customer.getFtel() != null && !customer.getFtel().isEmpty()) {
            model.put("FTel", customer.getFtel());
        }
        
        // F_mjll - 毛利率
        if (customer.getFMjll() != null) {
            model.put("F_mjll", customer.getFMjll());
        }
        
        // Fsfsd - 是否发送
        if (customer.getFsfsd() != null) {
            model.put("Fsfsd", customer.getFsfsd());
        }
        
        // FLegalPerson - 法人代表
        if (customer.getFlegalPerson() != null && !customer.getFlegalPerson().isEmpty()) {
            model.put("FLegalPerson", customer.getFlegalPerson());
        }
        
        // F_khqc - 客户全称
        if (customer.getFKhqc() != null && !customer.getFKhqc().isEmpty()) {
            model.put("F_khqc", customer.getFKhqc());
        }
        
        // FDescription - 描述
        if (customer.getFdescription() != null && !customer.getFdescription().isEmpty()) {
            model.put("FDescription", customer.getFdescription());
        }
        
        // 日期字段
        if (customer.getFcreateDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            model.put("FCreateDate", sdf.format(java.sql.Timestamp.valueOf(customer.getFcreateDate())));
        }
        if (customer.getFfoundDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            model.put("FFoundDate", sdf.format(java.sql.Date.valueOf(customer.getFfoundDate())));
        }
        if (customer.getFKhzrrq() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            model.put("F_khzrrq", sdf.format(java.sql.Date.valueOf(customer.getFKhzrrq())));
        }
        
        // FRegisterAddress - 注册地址
        if (customer.getFregisterAddress() != null && !customer.getFregisterAddress().isEmpty()) {
            model.put("FRegisterAddress", customer.getFregisterAddress());
        }
        
        // FinvoiceType - 发票类型
        if (customer.getFinvoiceType() != null && !customer.getFinvoiceType().isEmpty()) {
            model.put("FInvoiceType", customer.getFinvoiceType());
        }
        
        // F_bzyq - 备注要求
        if (customer.getFBzyq() != null && !customer.getFBzyq().isEmpty()) {
            model.put("F_bzyq", customer.getFBzyq());
        }
        
        // F_zlbzhjsyq - 质量标准和验收要求
        if (customer.getFZlbzhjsyq() != null && !customer.getFZlbzhjsyq().isEmpty()) {
            model.put("F_zlbzhjsyq", customer.getFZlbzhjsyq());
        }
        
        // F_sfysqs - 是否已授权
        if (customer.getFSfysqs() != null) {
            model.put("F_sfysqs", customer.getFSfysqs());
        }
        
        // F_cty_LargeText - 大文本
        if (customer.getFCtyLargeText() != null && !customer.getFCtyLargeText().isEmpty()) {
            model.put("F_cty_LargeText", customer.getFCtyLargeText());
        }
        
        // F_KHLOGO - 客户 Logo
        if (customer.getFKhlogo() != null && !customer.getFKhlogo().isEmpty()) {
            model.put("F_KHLOGO", customer.getFKhlogo());
        }
        
        // FCPAdminCode - 行政编码
        if (customer.getFcpAdminCode() != null && !customer.getFcpAdminCode().isEmpty()) {
            model.put("FCPAdminCode", customer.getFcpAdminCode());
        }
        
        // 社交媒体字段（按金蝶顺序）
        if (customer.getFYoutube() != null && !customer.getFYoutube().isEmpty()) {
            model.put("F_Youtube", customer.getFYoutube());
        }
        if (customer.getFLinkedin() != null && !customer.getFLinkedin().isEmpty()) {
            model.put("F_linkedin", customer.getFLinkedin());
        }
        if (customer.getFFacebook() != null && !customer.getFFacebook().isEmpty()) {
            model.put("F_facebook", customer.getFFacebook());
        }
        if (customer.getFTwitter() != null && !customer.getFTwitter().isEmpty()) {
            model.put("F_twitter", customer.getFTwitter());
        }
        if (customer.getFInstagram() != null && !customer.getFInstagram().isEmpty()) {
            model.put("F_instagram", customer.getFInstagram());
        }
        if (customer.getFVk() != null && !customer.getFVk().isEmpty()) {
            model.put("F_vk", customer.getFVk());
        }
        if (customer.getFFacebookmess() != null && !customer.getFFacebookmess().isEmpty()) {
            model.put("F_facebookmess", customer.getFFacebookmess());
        }
        if (customer.getFSkype() != null && !customer.getFSkype().isEmpty()) {
            model.put("F_skype", customer.getFSkype());
        }
        if (customer.getFWhatsapp() != null && !customer.getFWhatsapp().isEmpty()) {
            model.put("F_whatsapp", customer.getFWhatsapp());
        }
        if (customer.getFwechat() != null && !customer.getFwechat().isEmpty()) {
            model.put("FWeChat", customer.getFwechat());
        }
        if (customer.getFQq() != null && !customer.getFQq().isEmpty()) {
            model.put("F_qq", customer.getFQq());
        }
        if (customer.getFtn() != null && !customer.getFtn().isEmpty()) {
            model.put("Ftn", customer.getFtn());
        }
        if (customer.getFYolo() != null && !customer.getFYolo().isEmpty()) {
            model.put("F_Yolo", customer.getFYolo());
        }
        if (customer.getFHangouts() != null && !customer.getFHangouts().isEmpty()) {
            model.put("F_Hangouts", customer.getFHangouts());
        }
        if (customer.getFViber() != null && !customer.getFViber().isEmpty()) {
            model.put("F_Viber", customer.getFViber());
        }

        log.info("客户模型构建完成: " + model);
        return model;
    }
}
