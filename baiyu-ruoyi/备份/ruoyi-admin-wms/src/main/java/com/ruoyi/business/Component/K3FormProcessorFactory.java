package com.ruoyi.business.Component;

import com.ruoyi.business.k3.config.AbstractK3FormProcessor;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



import java.util.HashMap;
import java.util.Map;

import static cn.dev33.satoken.SaManager.log;

/**
 * 表单处理器工厂
 * 负责管理和提供各种表单类型的处理器
 * 使用工厂模式，便于扩展新的表单类型
 */
@Component
public class K3FormProcessorFactory {

    // 注入所有具体的处理器
    @Autowired
    private MaterialFormProcessor materialFormProcessor;

    @Autowired
    private SupplierFormProcessor supplierFormProcessor;

    @Autowired
    private PriceListFormProcessor priceListFormProcessor;

    @Autowired
    private InquiryOrderFormProcessor inquiryOrderFormProcessor;

    @Autowired
    private PurchaseQuotationFormProcessor purchaseQuotationFormProcessor;

    // 处理器映射表：表单ID -> 处理器实例
    private final Map<String, AbstractK3FormProcessor<?>> processorMap = new HashMap<>();

    /**
     * 初始化方法：注册所有处理器
     * 在Spring容器启动后自动执行
     */
    @PostConstruct
    public void init() {
        // 注册物料处理器
        processorMap.put("BD_MATERIAL", materialFormProcessor);
        log.info("注册物料表单处理器: BD_MATERIAL");

        // 注册供应商处理器
        processorMap.put("BD_Supplier", supplierFormProcessor);
        log.info("注册供应商表单处理器: BD_Supplier");

        // 注册采购价目表处理器
        processorMap.put("PUR_PriceCategory", priceListFormProcessor);
        log.info("注册采购价目表处理器: PUR_PriceCategory");

        // 注册询价单处理器
        processorMap.put("SAL_Inquiry", inquiryOrderFormProcessor);
        log.info("注册询价单处理器: SAL_Inquiry");

        // 注册采购报价单处理器
        processorMap.put("PUR_Quotation", purchaseQuotationFormProcessor);
        log.info("注册采购报价单处理器: PUR_Quotation");

        log.info("表单处理器工厂初始化完成，共注册 {} 个处理器", processorMap.size());
    }

    /**
     * 根据表单ID获取对应的处理器
     *
     * @param formId 表单ID，如 "BD_MATERIAL", "BD_Supplier"
     * @return 对应的处理器实例
     * @throws IllegalArgumentException 如果表单ID不支持
     */
    @SuppressWarnings("unchecked")
    public <T> AbstractK3FormProcessor<T> getProcessor(String formId) {
        AbstractK3FormProcessor<?> processor = processorMap.get(formId);
        if (processor == null) {
            String errorMsg = String.format("不支持的表单类型: %s，支持的类型: %s",
                formId, processorMap.keySet());
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        log.debug("获取到表单处理器: {}", formId);
        return (AbstractK3FormProcessor<T>) processor;
    }

}
