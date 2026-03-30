package com.ruoyi.business.emen;

import lombok.Getter;

import java.util.Arrays;

/**
 * 产品分类枚举（金蝶物料分类字典）
 */
@Getter
public enum ProductCategoryEnum {

    本科("1", "本科"),
    专科("2", "专科"),
    高中("3", "高中"),
    中专("4", "中专"),
    初中("5", "初中"),
    小学("6", "小学"),
    博士("7", "博士"),
    硕士("8", "硕士"),
    无("9", "无"),
    男("0", "男"),
    女("1", "女");


    /**
     * 金蝶匹配码（bilhead）
     */
    private final String code;

    /**
     * 字典名称（dict_name）
     */
    private final String dictName;

    ProductCategoryEnum(String code, String dictName) {
        this.code = code;
        this.dictName = dictName;
    }

    /**
     * 根据金蝶匹配码查询枚举
     *
     * @param code 金蝶匹配码
     * @return 对应的枚举，未找到返回null
     */
    public static ProductCategoryEnum fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        return Arrays.stream(ProductCategoryEnum.values())
                .filter(e -> e.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据字典名称查询枚举
     *
     * @param dictName 字典名称
     * @return 对应的枚举，未找到返回null
     */
    public static ProductCategoryEnum fromDictName(String dictName) {
        if (dictName == null || dictName.isEmpty()) {
            return null;
        }
        return Arrays.stream(ProductCategoryEnum.values())
                .filter(e -> e.dictName.equals(dictName))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据枚举名称查询
     *
     * @param name 枚举名称
     * @return 对应的枚举，未找到返回null
     */
    public static ProductCategoryEnum fromName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        return Arrays.stream(ProductCategoryEnum.values())
                .filter(e -> e.name().equals(name))
                .findFirst()
                .orElse(null);
    }
}
