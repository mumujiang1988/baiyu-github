package com.ruoyi.business.emen;

import lombok.Getter;

import java.util.Arrays;

/**
 * 供应链业务相关枚举集合（统一管理字典表）
 */
public class SupplierEnums {

    /**
     * 供应商类型
     */
    @Getter
    public enum SupplierType {
        检测("67f35889145d35"),
        潜在供应商("638469ed8e365a"),
        合作供应商("638469fc8e365c"),
        备选供应商("652f683c70b45e"),
        暂停合作("6627473f3a89c5"),
        物流包装供应商("66c3f3ca67970c"),
        包材供应商("66c3f81867971a"),
        一六八八("6628b3c33abd2f"),
        上海五金展会("6628b3d23abd53"),
        客户介绍("6683b01ea96134");

        private final String code;
        SupplierType(String code) { this.code = code; }

        // 通用方法：根据 code 查枚举
        public static SupplierType fromCode(String code) {
            return Arrays.stream(SupplierType.values())
                    .filter(e -> e.code.equals(code))
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * 币种类型
     */
    @Getter
    public enum CurrencyType {
        人民币("PRE001"),
        香港元("PRE002"),
        欧元("PRE003"),
        日本日圆("PRE004"),
        新台币元("PRE005"),
        英镑("PRE006"),
        美元("PRE007"),
        卢布("PRE008");

        private final String code;
        CurrencyType(String code) { this.code = code; }

        public static CurrencyType fromCode(String code) {
            return Arrays.stream(CurrencyType.values())
                    .filter(e -> e.code.equals(code))
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * 付款方式
     */
    @Getter
    public enum PaymentTerm {

        FKTJ01_SYS("20000", "货到付款"),
        FKTJ02_SYS("20001", "30天后付款"),
        FKTJ03_SYS("20002", "月结30天"),
        FKTJ04_SYS("20003", "多到期日（按金额），货到付款2万，30天后付款5万，余款60天后付清"),
        MONTHLY45("181255", "月结45天"),
        ADVANCE_PAYMENT30("181256", "30%预付，尾款到货后45个工作日"),
        ADVANCE_PAYMENT50("181263", "50%预付款 尾款到货后30天"),
        BEFORE_DELIVERY("181838", "款到发货"),
        DAY_PAYMENT("181839", "20天付款"),
        BEFORE_SHIPMENT("185194", "预付30%，尾款发货前付清"),
        Days("234981", "月结60天"),
        ADVANCE("239538", "20%预付款，60%款到发货；20%发货后30天"),

        PAYMENT_DUE_IN_30DAYS("239843","30%预付，尾款到货后30天");

        private final String code;
        private final String displayName;

        PaymentTerm(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }
        /**
         * 根据前端传过来的显示名称获取枚举
         */
        public static PaymentTerm fromPaymentTermName(String displayName) {
            return Arrays.stream(PaymentTerm.values())
                    .filter(e -> e.displayName.equals(displayName))
                    .findFirst()
                    .orElse(null);
        }
        /**
         * 根据前端传过来的值显示名称获取枚举
         */
        public static PaymentTerm fromPaymentTerms(String PaymentTerms) {
            return Arrays.stream(PaymentTerm.values())
                .filter(e -> e.getCode().equals(PaymentTerms))
                .findFirst()
                .orElse(null);
        }
    }


    /**
     * 发票类型
     */
    @Getter
    public enum InvoiceType {
        增值税专用发票("1"),
        普通发票("2");

        private final String code;
        InvoiceType(String code) { this.code = code; }

        public static InvoiceType fromCode(String code) {
            return Arrays.stream(InvoiceType.values())
                    .filter(e -> e.code.equals(code))
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * 纳税人类型
     */
    @Getter
    public enum TaxpayerType {
        一般纳税人("9e855eb97bec43e7b50c3e0e0bf51210"),
        小规模纳税人("386b5ca3155c492fbfcb7e8bd7861295");

        private final String code;
        TaxpayerType(String code) { this.code = code; }

        public static TaxpayerType fromCode(String code) {
            return Arrays.stream(TaxpayerType.values())
                    .filter(e -> e.code.equals(code))
                    .findFirst()
                    .orElse(null);
        }
    }
    /**
     * 税率
     */
    @Getter
    public enum DefaultTaxRate {

        SL01_SYS("233", "17%增值税"),
        SL03_SYS("234", "13%增值税"),
        SL04_SYS("236", "零税率的增值税"),
        SL05_SYS("260", "11%增值税"),
        SL06_SYS("261", "6%增值税"),
        SL07_SYS("262", "3%增值税"),
        SL31_SYS("264", "税率为16%的增值税基本税率"),
        SL45_SYS("266", "税率为12%的增值税基本税率"),
        SL62_SYS("269", "10%增值税"),
        SL64_SYS("144098", "1%增值税"),
        SL65_SYS("268", "9%增值税");

        private final String code;
        private final String displayName;

        DefaultTaxRate(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }

        /**
         * 根据前端传过来的显示名称获取枚举
         */
        public static DefaultTaxRate fromDisplayName(String displayName) {
            return Arrays.stream(DefaultTaxRate.values())
                    .filter(e -> e.displayName.equals(displayName))
                    .findFirst()
                    .orElse(null);
        }
    }
    /**
     * 结算方式
     */
    @Getter
    public enum SettlementParty {
        现金("JSFS01_SYS"),
        现金支票("JSFS02_SYS"),
        转账支票("JSFS03_SYS"),
        电汇("JSFS04_SYS"),
        信汇("JSFS05_SYS"),
        商业承兑汇票("JSFS06_SYS"),
        银行承兑汇票("JSFS07_SYS"),
        信用证("JSFS08_SYS"),
        微信("JSFS31_SYS"),
        支付宝("JSFS32_SYS");

        private final String code;
        SettlementParty(String code) { this.code = code; }

        public static SettlementParty fromCode(String code) {
            return Arrays.stream(SettlementParty.values())
                    .filter(e -> e.code.equals(code))
                    .findFirst()
                    .orElse(null);
        }

        public static SettlementParty fromName(String name) {
            return Arrays.stream(SettlementParty.values())
                    .filter(e -> e.name().equals(name))
                    .findFirst()
                    .orElse(null);
        }

        // 推荐统一版本
        public static SettlementParty fromAny(String value) {
            if (value == null || value.isEmpty()) return null;
            return Arrays.stream(SettlementParty.values())
                    .filter(e -> e.name().equals(value.trim()) || e.code.equalsIgnoreCase(value.trim()))
                    .findFirst()
                    .orElse(null);
        }
    }


}
