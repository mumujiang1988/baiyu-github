   package com.ruoyi.business.config;

import com.ruoyi.business.entity.SaleOrder;
import com.ruoyi.business.entity.SaleOrderCost;
import com.ruoyi.business.entity.SaleOrderEntry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * 销售订单成本计算器
 * 用于计算销售订单成本相关字段的值
 */
public class SaleOrderCostCalculator {

    /**
     * 计算销售订单成本表中的计算字段
     * @param saleOrder 主订单信息
     * @param saleOrderCost 成本信息（将被更新）
     * @param saleOrderEntries 订单明细列表
     */
    public static void calculateCostFields(SaleOrder saleOrder, SaleOrderCost saleOrderCost, List<SaleOrderEntry> saleOrderEntries) {
        if (saleOrderCost == null) {
            return;
        }

        // 1. 价税合计 = 明细信息表中每个价税合计总和
        BigDecimal fBillAllAmount = calculateBillAllAmount(saleOrderEntries);
        saleOrderCost.setFBillAllAmount(fBillAllAmount);

        // 2. 价税合计本位币 = 价税合计 * 报价汇率
        BigDecimal exchangeRate = saleOrder != null ? saleOrder.getFExchangeRate() : BigDecimal.ZERO;
        BigDecimal fBillAllAmountLc = calculateBillAllAmountLc(fBillAllAmount, exchangeRate);
        saleOrderCost.setFBillAllAmountLc(fBillAllAmountLc);

        // 3. 明细成本合计 = 明细信息表中实际成本 * 计价数量 总和
        BigDecimal fMxcbhj = calculateMxcbhj(saleOrderEntries);
        saleOrderCost.setFMxcbhj(fMxcbhj);

        // 4. 明细退税合计 = 明细成本合计 / 1.13 * 0.13
        BigDecimal fMxtshj = calculateMxtshj(fMxcbhj);
        saleOrderCost.setFMxtshj(fMxtshj);

        // 5. 费用小计 = 认证费 + 代理费 + 包装费 + 快递费成本 + 货代费 + 陆运费 + 其他费用 + 模具费 + 进仓费
        BigDecimal fFyxj = calculateFyxj(saleOrderCost);
        saleOrderCost.setFFyxj(fFyxj);
    }

    /**
     * 计算价税合计 = 明细信息表中每个价税合计总和
     * @param saleOrderEntries 订单明细列表
     * @return 价税合计
     */
    private static BigDecimal calculateBillAllAmount(List<SaleOrderEntry> saleOrderEntries) {
        if (saleOrderEntries == null || saleOrderEntries.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return saleOrderEntries.stream()
                .map(SaleOrderEntry::getFAllAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 计算价税合计本位币 = 价税合计 * 报价汇率
     * @param billAllAmount 价税合计
     * @param exchangeRate 汇率
     * @return 价税合计本位币
     */
    private static BigDecimal calculateBillAllAmountLc(BigDecimal billAllAmount, BigDecimal exchangeRate) {
        if (billAllAmount == null) {
            billAllAmount = BigDecimal.ZERO;
        }
        if (exchangeRate == null) {
            exchangeRate = BigDecimal.ZERO;
        }

        return billAllAmount.multiply(exchangeRate).setScale(6, RoundingMode.HALF_UP);
    }

    /**
     * 计算明细成本合计 = 明细信息表中实际成本 * 计价数量 总和
     * @param saleOrderEntries 订单明细列表
     * @return 明细成本合计
     */
    private static BigDecimal calculateMxcbhj(List<SaleOrderEntry> saleOrderEntries) {
        if (saleOrderEntries == null || saleOrderEntries.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return saleOrderEntries.stream()
                .map(entry -> {
                    BigDecimal actualCost = entry.getFYgcb(); // 实际成本
                    BigDecimal priceQuantity = entry.getFMaterialPriceUnitQty(); // 计价数量

                    if (actualCost != null && priceQuantity != null) {
                        return actualCost.multiply(priceQuantity);
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 计算明细退税合计 = 明细成本合计 / 1.13 * 0.13
     * @param mxcbhj 明细成本合计
     * @return 明细退税合计
     */
    private static BigDecimal calculateMxtshj(BigDecimal mxcbhj) {
        if (mxcbhj == null) {
            mxcbhj = BigDecimal.ZERO;
        }

        // 计算公式: 明细成本合计 / 1.13 * 0.13
        BigDecimal divisor = new BigDecimal("1.13");
        BigDecimal multiplier = new BigDecimal("0.13");

        return mxcbhj.divide(divisor, 6, RoundingMode.HALF_UP)
                   .multiply(multiplier)
                   .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算费用小计 = 认证费 + 代理费 + 包装费 + 快递费成本 + 货代费 + 陆运费 + 其他费用 + 模具费 + 进仓费
     * @param saleOrderCost 成本信息
     * @return 费用小计
     */
    private static BigDecimal calculateFyxj(SaleOrderCost saleOrderCost) {
        if (saleOrderCost == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal fRzf = Optional.ofNullable(saleOrderCost.getFRzf()).orElse(BigDecimal.ZERO);      // 认证费
        BigDecimal fDlf = Optional.ofNullable(saleOrderCost.getFDlf()).orElse(BigDecimal.ZERO);      // 代理费
        BigDecimal fBzf = Optional.ofNullable(saleOrderCost.getFBzf()).orElse(BigDecimal.ZERO);      // 包装费
        BigDecimal fKdf = Optional.ofNullable(saleOrderCost.getFKdf()).orElse(BigDecimal.ZERO);      // 快递费成本
        BigDecimal fHdf = Optional.ofNullable(saleOrderCost.getFHdf()).orElse(BigDecimal.ZERO);      // 货代费
        BigDecimal fLyf = Optional.ofNullable(saleOrderCost.getFLyf()).orElse(BigDecimal.ZERO);      // 陆运费
        BigDecimal fQtfy = Optional.ofNullable(saleOrderCost.getFQtfy()).orElse(BigDecimal.ZERO);    // 其他费用
        BigDecimal fMjf = Optional.ofNullable(saleOrderCost.getFMjf()).orElse(BigDecimal.ZERO);      // 模具费
        BigDecimal fJcf = Optional.ofNullable(saleOrderCost.getFJcf()).orElse(BigDecimal.ZERO);      // 进仓费

        return fRzf.add(fDlf)
                  .add(fBzf)
                  .add(fKdf)
                  .add(fHdf)
                  .add(fLyf)
                  .add(fQtfy)
                  .add(fMjf)
                  .add(fJcf)
                  .setScale(4, RoundingMode.HALF_UP);
    }
}
