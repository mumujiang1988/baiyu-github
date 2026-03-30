package com.ruoyi.business.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SaleOrderCost {

    /** 主键=销售订单FID */
    private Long fid;
    /**订单编号*/
    private String fbillno;
    /** 海运费（外币） */
    private BigDecimal fHyf;
    /** 价税合计 */
    private BigDecimal fBillAllAmount;
    /** 价税合计（本位币） */
    private BigDecimal fBillAllAmountLc;
    /** 保险费 */
    private BigDecimal fBxf;
    /** 国外银行费用 */
    private BigDecimal fGwyhfy;
    /** 其他外币费用 */
    private BigDecimal fQtwbfy;
    /** 明细成本合计 */
    private BigDecimal fMxcbhj;
    /** 明细退税合计 */
    private BigDecimal fMxtshj;
    /** 成本小计RMB */
    private BigDecimal fCbxj;
    /** 包装费 */
    private BigDecimal fBzf;
    /** 代理费 */
    private BigDecimal fDlf;
    /** 认证费 */
    private BigDecimal fRzf;
    /** 快递费成本 */
    private BigDecimal fKdf;
    /** 货贷费 */
    private BigDecimal fHdf;
    /** 陆运费 */
    private BigDecimal fLyf;
    /** 其他费用 */
    private BigDecimal fQtfy;
    /** 模具费 */
    private BigDecimal fMjf;
    /** 进仓费 */
    private BigDecimal fJcf;
    /** 费用小计 */
    private BigDecimal fFyxj;
    /** 外币盈亏 */
    private BigDecimal fWbyk;
    /** 净利润额 */
    private BigDecimal fJlre;
    /** 毛净利润率% */
    private BigDecimal fLrl;
    /** 净利润率% */
    private BigDecimal fJlrl;
}
