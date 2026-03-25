package com.ruoyi.business.emen;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum PriceListDimensionEnum {

    MATERIAL("A", "按物料"),
    MATERIAL_CATEGORY("B", "按物料类别"),
    MATERIAL_GROUP("C", "按物料分组");

    private final String code;
    private final String label;

    PriceListDimensionEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 根据 code 获取枚举
     */
    public static PriceListDimensionEnum fromCode(String code) {
        for (PriceListDimensionEnum e : values()) {
            if (e.code.equalsIgnoreCase(code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("未知的价目表维度 code: " + code);
    }
    @Getter
    public enum CurrencyType {
        人民币(1L),
        香港元(2L),
        欧元(3L),
        日本日圆(4L),
        新台币元(5L),
        英镑(6L),
        美元(7L);

        private final Long code;
        CurrencyType(Long code) { this.code = code; }

        public static CurrencyType fromCode(Long code) {
            for (CurrencyType e : values()) {
                if (e.code.equals(code)) {
                    return e;
                }
            }
            throw new IllegalArgumentException("未知的货币类型 code: " + code);
        }

        public static CurrencyType fromCode(String codeStr) {
            try {
                return fromCode(Long.parseLong(codeStr));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("无效的货币类型 code: " + codeStr);
            }
        }
    }

}
