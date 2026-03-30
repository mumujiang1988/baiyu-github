package com.ruoyi.business.util;

import com.ruoyi.business.entity.BymaterialDictionary;
import com.ruoyi.business.entity.Customer;
import com.ruoyi.business.entity.SettlementMethod;
import com.ruoyi.business.mapper.BymaterialDictionaryMapper;
import com.ruoyi.business.mapper.SettlementMethodMapper;

/**
 * 字典转换工具类
 */
public class DictConvertUtil {

    /**
     * 批量处理客户对象的字典字段转换
     * @param customer 客户对象
     * @param bymaterialDictionaryMapper 物料字典数据访问对象
     * @param settlementMethodMapper 结算方式数据访问对象
     */
    public static void convertCustomerDictFields(Customer customer,
                                                 BymaterialDictionaryMapper bymaterialDictionaryMapper,
                                                 SettlementMethodMapper settlementMethodMapper) {
        // 客户分组
        String fgroupId = getBymaterialDictByCategoryId(customer.getFgroupId(), bymaterialDictionaryMapper);
        if (fgroupId != null) {
            customer.setFgroupId(fgroupId);
        }

        // 结算币别
        String ftradingCurrId = BymaterialDictByCategoryId(customer.getFtradingCurrId(), bymaterialDictionaryMapper);
        if (ftradingCurrId != null) {
            customer.setFtradingCurrId(ftradingCurrId);
        }

        // 收款币别
        String freceiveCurrId = getBymaterialDictByCategoryId(customer.getFreceiveCurrId(), bymaterialDictionaryMapper);
        if (freceiveCurrId != null) {
            customer.setFreceiveCurrId(freceiveCurrId);
        }

        // 收款条件
        String frecConditionId = getBymaterialDictByCategoryId(customer.getFrecConditionId(), bymaterialDictionaryMapper);
        if (frecConditionId != null) {
            customer.setFrecConditionId(frecConditionId);
        }

        // 销售组
        String fsalGroupId = getBymaterialDictByCategoryId(customer.getFsalGroupId(), bymaterialDictionaryMapper);
        if (fsalGroupId != null) {
            customer.setFsalGroupId(fsalGroupId);
        }

        // 结算方式
        Long fsettleTypeId = getSettlementMethodByCategoryId(customer.getFsettleTypeId(), settlementMethodMapper);
        if (fsettleTypeId != null) {
            customer.setFsettleTypeId(fsettleTypeId);
        }

        // 税分类
        Long ftaxType = getSettlementMethodByCategoryId(customer.getFtaxType(), settlementMethodMapper);
        if (ftaxType != null) {
            customer.setFtaxType(ftaxType);
        }
    }

    /**
     * 根据分类ID获取物料字典的金蝶编码
     * @param categoryId 分类ID
     * @param mapper 物料字典数据访问对象
     * @return 金蝶编码
     */
    public static String getBymaterialDictByCategoryId(String categoryId, BymaterialDictionaryMapper mapper) {
        if (categoryId != null) {
            BymaterialDictionary dict = mapper.selectByKingdes(categoryId);
            if (dict != null && dict.getKingdee() != null && !dict.getKingdee().trim().isEmpty()) {
                try {
                    return dict.getKingdee();
                } catch (NumberFormatException e) {
                    // 如果转换失败，返回null
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 根据分类ID获取物料字典的金蝶编码
     * @param categoryId 分类ID
     * @param mapper 物料字典数据访问对象
     * @return 金蝶编码
     */
    public static String BymaterialDictByCategoryId(String categoryId, BymaterialDictionaryMapper mapper) {
        if (categoryId != null) {
            String category = "currency";
            BymaterialDictionary dict = mapper.selectByKingde(categoryId,category);
            if (dict != null && dict.getKingdee() != null && !dict.getKingdee().trim().isEmpty()) {
                try {
                    return dict.getKingdee();
                } catch (NumberFormatException e) {
                    // 如果转换失败，返回null
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 根据分类ID获取结算方式的编码
     * @param categoryId 分类ID
     * @param mapper 结算方式数据访问对象
     * @return 编码
     */
    public static Long getSettlementMethodByCategoryId(Long categoryId, SettlementMethodMapper mapper) {
        if (categoryId != null) {
            SettlementMethod settlementMethod = mapper.selectById(categoryId);
            if (settlementMethod != null && settlementMethod.getCode() != null && !settlementMethod.getCode().trim().isEmpty()) {
                try {
                    return Long.valueOf(settlementMethod.getCode());
                } catch (NumberFormatException e) {
                    // 如果转换失败，返回null
                    return null;
                }
            }
        }
        return null;
    }
}
