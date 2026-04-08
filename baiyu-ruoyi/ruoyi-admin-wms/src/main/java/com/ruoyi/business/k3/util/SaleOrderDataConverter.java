package com.ruoyi.business.k3.util;

import com.ruoyi.business.entity.SaleOrder;
import com.ruoyi.business.entity.SaleOrderCost;
import com.ruoyi.business.entity.SaleOrderEntry;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;

/**
 * 销售订单数据转换工具类
 * 用于将金蝶接口返回的List<Object>数据转换为对应的实体对象
 */
@Slf4j
public class SaleOrderDataConverter {

    /**
     * 解析销售订单主表行数据
     * 将金蝶接口返回的List<Object>转换为SaleOrder实体
     *
     * @param row 行数据
     * @return SaleOrder实体
     */
    public static SaleOrder parseSaleOrderFromRow(List<Object> row) {
        SaleOrder order = new SaleOrder();

        try {
            if (row == null || row.isEmpty()) {
                log.warn("销售订单主表行数据为空");
                return order;
            }

            int index = 0;

            order.setDocumentType(K3DataUtils.getString(row, index++));
            // FID
            order.setFid(K3DataUtils.getLong(row, index++));
            order.setFdocumentStatus(K3DataUtils.getString(row, index++));
            order.setOrderStatus(K3DataUtils.getString(row, index++));

            // FBillNo - 单据编号
            order.setFBillNo(K3DataUtils.getString(row, index++));
            // FDate - 销售合同日期
            order.setFDate(convertToLocalDateToDate(DateUtils.getLocalDate(row, index++)));

            // FCustId - 客户编码
            order.setFCustId(K3DataUtils.getString(row, index++));

            // F_ora_BaseProperty - 客户简称
            order.setFOraBaseProperty(K3DataUtils.getString(row, index++));

            // F_khhth - 客户合同号
            order.setFKhhth(K3DataUtils.getString(row, index++));

            // F_kglxr - 客户联系人
            order.setFKglxr(K3DataUtils.getString(row, index++));

            // F_cty_BaseProperty1 - 客户邮箱
            order.setFCtyBaseProperty1(K3DataUtils.getString(row, index++));

            // FSettleCurrId - 结算币别
            order.setFSettleCurrId(K3DataUtils.getString(row, index++));

            // F_tcbl - 提成比例%
            order.setFTcbl(K3DataUtils.getBigDecimal(row, index++));

            // F_KHSD - 客户首单（非样品）
            order.setFKhsd(K3DataUtils.getInteger(row, index++));

            // FIsIncludedTax - 是否含税
            order.setFIsIncludedTax(K3DataUtils.getInteger(row, index++));

            // F_sfbg - 是否报关
            order.setFSfbg(K3DataUtils.getInteger(row, index++));

            // FSalerId - 销售员
            order.setFSalerId(K3DataUtils.getString(row, index++));

            // F_lrl - 毛净利润率%
            order.setFLrl(K3DataUtils.getBigDecimal(row, index++));

            // F_jlrl - 净利润率%
            order.setFJlrl(K3DataUtils.getBigDecimal(row, index++));

            // F_gj - 抵运国家
            order.setFGj(K3DataUtils.getString(row, index++));
            order.setFstate(K3DataUtils.getString(row, index++));

            // F_myfs - 贸易方式
            order.setFMyfs(K3DataUtils.getString(row, index++));

            // F_zyxb - 占用信保
            order.setFZyxb(K3DataUtils.getInteger(row, index++));

            // F_yhzh - 银行账号
            order.setFYhzh(K3DataUtils.getString(row, index++));

            // F_cty_Date - 客户交期
            order.setFCtyDate(convertToLocalDateToDate(DateUtils.getLocalDate(row, index++)));

            // F_sygs - 所属公司
            order.setFSygs(K3DataUtils.getString(row, index++));

            // FRecConditionId - 收款条件
            order.setFRecConditionId(K3DataUtils.getString(row, index++));

            // Fbzfs - 包装方式
            order.setFbzfs(K3DataUtils.getString(row, index++));

            // FReceiveId - 收货方
            order.setFReceiveId(K3DataUtils.getString(row, index++));

            // FSettleId - 结算方
            order.setFSettleId(K3DataUtils.getString(row, index++));

            // FSettleAddress - 结算方地址
            order.setFSettleAddress(K3DataUtils.getString(row, index++));

            // FChargeId - 付款方
            order.setFChargeId(K3DataUtils.getString(row, index++));

            // F_shhl - 锁汇汇率
            order.setFShhl(K3DataUtils.getBigDecimal(row, index++));

            // F_shzt - 锁汇状态
            order.setFShzt(K3DataUtils.getInteger(row, index++));

            // F_shje - 锁汇金额
            order.setFShje(K3DataUtils.getBigDecimal(row, index++));

            // F_cty_Date1 - 解汇日期
            order.setFCtyDate1(convertToLocalDateToDate(DateUtils.getLocalDate(row, index++)));

            // FCreatorId - 创建人
            order.setFCreatorId(K3DataUtils.getString(row, index++));

            // FModifierId - 最后修改人
            order.setFModifierId(K3DataUtils.getString(row, index++));

            // FModifyDate - 最后修改日期
            order.setFModifyDate(convertToLocalDateToDate(DateUtils.getLocalDate(row, index++)));

            // FAllDisCount - 整单折扣额
            order.setFAllDisCount(K3DataUtils.getBigDecimal(row, index++));

            // F_ysbl1 - 预收比例%
            order.setFYsbl1(K3DataUtils.getBigDecimal(row, index++));

            // FBillTaxAmount - 税额
            order.setFBillTaxAmount(K3DataUtils.getBigDecimal(row, index++));

            // FLocalCurrId - 本位币
            order.setFLocalCurrId(K3DataUtils.getString(row, index++));

            // FExchangeTypeId - 汇率类型
            order.setFExchangeTypeId(K3DataUtils.getString(row, index++));

            // FExchangeRate - 汇率
            order.setFExchangeRate(K3DataUtils.getBigDecimal(row, index++));

            // FPLANNOTRECAMOUNT - 未收款金额
            order.setFPlannotRecAmount(K3DataUtils.getBigDecimal(row, index++));

            // FPLANALLRECAMOUNT - 累计收款金额
            order.setFPlanAllRecAmount(K3DataUtils.getBigDecimal(row, index++));

            // FPLANREFUNDAMOUNT - 累计退款金额
            order.setFPlanRefundAmount(K3DataUtils.getBigDecimal(row, index++));

            // 调试信息：输出FBillAmount的值
            if (row.size() > index) {
                Object billAmountValue = row.get(index);
                log.debug("FBillAmount原始值: {}, 类型: {}", billAmountValue, billAmountValue != null ? billAmountValue.getClass() : "null");
                order.setFBillAmount(K3DataUtils.getBigDecimal(row, index++));
            } else {
                log.warn("FBillAmount字段不存在，当前index: {}, row大小: {}", index, row.size());
                order.setFBillAmount(null);
                index++; // 仍然递增index以保持一致性
            }

        } catch (Exception e) {
            log.error("解析销售订单主表数据失败", e);
            throw new RuntimeException("解析销售订单主表数据失败", e);
        }

        return order;
    }

    /**
     * 解析销售订单明细表行数据
     * 将金蝶接口返回的List<Object>转换为SaleOrderEntry实体
     *
     * @param row 行数据
     * @return SaleOrderEntry实体
     */
    public static SaleOrderEntry parseSaleOrderEntryFromRow(List<Object> row) {
        SaleOrderEntry entry = new SaleOrderEntry();

        try {
            if (row == null || row.isEmpty()) {
                log.warn("销售订单明细表行数据为空");
                return entry;
            }

            int index = 0;

            // FID - 销售订单FID
            entry.setFbillNo(K3DataUtils.getString(row, index++));

            // Fbcykc - 包材有库存
            entry.setFBcykc(K3DataUtils.getString(row, index++));
            // F_sfxp - 产品类别
            entry.setFSfxp(K3DataUtils.getString(row, index++));

            // F_jqhx - 交期红线
            entry.setFJqhx(convertToLocalDateToDate(DateUtils.getLocalDate(row, index++)));

            // FPlanMaterialId - 物料编码
            entry.setFPlanMaterialId(K3DataUtils.getString(row, index++));

            // FPlanMaterialName - 物料名称
            entry.setFPlanMaterialName(K3DataUtils.getString(row, index++));

            // FPlanUnitId - 销售单位
            entry.setFPlanUnitId(K3DataUtils.getString(row, index++));

            // FQty - 销售数量
            entry.setFQty(K3DataUtils.getBigDecimal(row, index++));
            // FMaterialPriceUnitQty - 计价数量
            entry.setFMaterialPriceUnitQty(K3DataUtils.getBigDecimal(row, index++));

            // FMaterialPriceUnitID - 计价单位
            entry.setFMaterialPriceUnitID(K3DataUtils.getString(row, index++));

            // FPrice - 单价
            entry.setFPrice(K3DataUtils.getBigDecimal(row, index++));


            // FTaxPrice - 含税单价
            entry.setFTaxPrice(K3DataUtils.getBigDecimal(row, index++));


            // FEntryTaxRate - 税率%
            entry.setFEntryTaxRate(K3DataUtils.getBigDecimal(row, index++));


            // FEntryTaxAmount - 税额
            entry.setFEntryTaxAmount(K3DataUtils.getBigDecimal(row, index++));


            // FAllAmount - 价税合计
            entry.setFAllAmount(K3DataUtils.getBigDecimal(row, index++));


            // FBaseCanOutQty - 可出数量（销售基本）
            entry.setFBaseCanOutQty(K3DataUtils.getBigDecimal(row, index++));


            // FDeliQty - 累计发货通知数量
            entry.setFDeliQty(K3DataUtils.getBigDecimal(row, index++));


            // FStockBaseCanOutQty - 可出数量（库存基本）
            entry.setFStockBaseCanOutQty(K3DataUtils.getBigDecimal(row, index++));


            // F_ora_Text1 - 客户货号
            entry.setFOraText1(K3DataUtils.getString(row, index++));


            // F_ora_Date3 - 发货日期

            entry.setFOraDate3(convertToLocalDateToDate(DateUtils.getLocalDate(row, index++)));

            // F_FZR - 负责人
            entry.setFFzr(K3DataUtils.getString(row, index++));


            // F_ora_Text - 采购合同号

            entry.setFOraText(K3DataUtils.getString(row, index++));

            // F_ora_Date1 - 供应商实际完成订单日期
            entry.setFOraDate1(convertToLocalDateToDate(DateUtils.getLocalDate(row, index++)));

            // F_ora_Date2 - 实际收货日期
            entry.setFOraDate2(convertToLocalDateToDate(DateUtils.getLocalDate(row, index++)));


            // FPurJoinQty - 关联采购/生产数量

            entry.setFPurJoinQty(K3DataUtils.getBigDecimal(row, index++));


            // F_ora_Base1 - 延迟原因
            entry.setFOraBase1(K3DataUtils.getString(row, index++));


            // F_ycjjfa - 延迟解决方案
            entry.setFYcjjfa(K3DataUtils.getString(row, index++));


            // F_ora_Decimal - 采购价
            entry.setFOraDecimal(K3DataUtils.getBigDecimal(row, index++));


            // F_ora_Date4 - 业务员提供包装资料日期

            entry.setFOraDate4(convertToLocalDateToDate(DateUtils.getLocalDate(row, index++)));


            // F_ora_Decimal1 - 成本价
            entry.setFOraDecimal1(K3DataUtils.getBigDecimal(row, index++));


            // F_ora_Integer2 - 包装资料晚交天数
            entry.setFOraInteger2(K3DataUtils.getInteger(row, index++));


            // F_ora_Date5 - 包装寄出日期

            entry.setFOraDate5(convertToLocalDateToDate(DateUtils.getLocalDate(row, index++)));


            // F_ora_Integer3 - 包装寄出延迟天数
            entry.setFOraInteger3(K3DataUtils.getInteger(row, index++));


            // F_BZJDTWO - 包装进度

            entry.setFBzjdTwo(K3DataUtils.getString(row, index++));


            // F_ora_Base - 供应商
            entry.setFOraBase(K3DataUtils.getString(row, index++));


            // F_ora_Integer - 工厂交货期

            entry.setFOraInteger(K3DataUtils.getInteger(row, index++));


            // F_ora_Date - 采购合同日期
            entry.setFOraDate(convertToLocalDateToDate(DateUtils.getLocalDate(row, index++)));


            // F_mz - 毛重
            entry.setFmz(K3DataUtils.getBigDecimal(row, index++));


            // F_jz - 净重

            entry.setFjz(K3DataUtils.getBigDecimal(row, index++));

            // F_mzz - 毛总重

            entry.setFmzz(K3DataUtils.getBigDecimal(row, index++));

            // F_zxs - 装箱数
            entry.setFzxs(K3DataUtils.getInteger(row, index++));

            // F_xs - 箱数
            entry.setFxs(K3DataUtils.getInteger(row, index++));

            // F_gdtp1 - 跟单图片1
            entry.setFGdtp1(K3DataUtils.getString(row, index++));

            // F_gdtp2 - 跟单图片2
            entry.setFGdtp2(K3DataUtils.getString(row, index++));

            // F_XDGJJDTWO - 跟进情况
            entry.setFXdgjjdTwo(K3DataUtils.getString(row, index++));

            // F_bzfs - 包装要求
            entry.setFBzfs(K3DataUtils.getString(row, index++));

            // F_tsyq - 特殊要求
            entry.setFTsyq(K3DataUtils.getString(row, index++));

            // F_ljrksl - 累计入库数量
            entry.setFljrksl(K3DataUtils.getBigDecimal(row, index++));

            // Fysbz - 颜色标识
            entry.setFYsbz(K3DataUtils.getString(row, index++));

            // F_bgrq - 变更日期
            entry.setFBgrq(convertToLocalDateToDate(DateUtils.getLocalDate(row, index++)));

            // F_ctt - 彩贴图
            entry.setFCtt(K3DataUtils.getString(row, index++));

            // F_cptp - 产品图片
            entry.setFCptp(K3DataUtils.getString(row, index++));

            // F_gcbz - 工厂包装
            entry.setFGcbz(K3DataUtils.getString(row, index++));

            // Fhhwrk - 货好未入库
            entry.setFHhwrk(K3DataUtils.getBoolean(row.get(index++)));

            // F_kpdj - 开票单价
            entry.setFKpdj(K3DataUtils.getBigDecimal(row, index++));

            // F_bzxdzt - 包装下单状态
            entry.setFBzxdzt(K3DataUtils.getString(row, index++));

            // F_gxzt - 更新状态
            entry.setFGxzt(K3DataUtils.getString(row, index++));


            // F_Cht - 彩盒图
            entry.setFCht(K3DataUtils.getString(row, index++));

            // F_jgt - 禁告贴
            entry.setFjgt(K3DataUtils.getString(row, index++));

            // F_smsfj - 说明书附件
            entry.setFSmsfj(K3DataUtils.getString(row, index++));

            // F_BCHQZT - 包材获取状态
            entry.setFBchqzt(K3DataUtils.getString(row, index++));

            // F_tsl - 退税率%
            entry.setFTsl(K3DataUtils.getBigDecimal(row, index++));

            // F_bgywpm - 报关英文品名
            entry.setFBgywpm(K3DataUtils.getString(row, index++));

            // F_bgdw - 报关单位
            entry.setFBgdw(K3DataUtils.getString(row, index++));

            // FYgcb - 实际成本
            entry.setFYgcb(K3DataUtils.getBigDecimal(row, index++));

            // F_hsbm - HS编码
            entry.setFHsbm(K3DataUtils.getString(row, index++));

            // F_sbys - 申报要素
            entry.setFSbys(K3DataUtils.getString(row, index++));

            // F_BCFYNEW - 包材费用
            entry.setFBcfyNew(K3DataUtils.getBigDecimal(row, index++));

            // F_GLBCFYNEW - 关联包材费用
            entry.setFGlbcfyNew(K3DataUtils.getBigDecimal(row, index++));

            // Fbzcc - 包装尺寸
            entry.setFbzcc(K3DataUtils.getString(row, index++));

            // Fbzgctg - 包装工厂提供
            entry.setFbzgctg(K3DataUtils.getBoolean(row.get(index++)));

            // Frkshrq - 入库审核日期
            entry.setFrkshrq(convertToLocalDateToDate(DateUtils.getLocalDate(row, index++)));

            // Fbjr - 报价人
            entry.setFBjr(K3DataUtils.getString(row, index++));

            // Ftpr - 推品人
            entry.setFTpr(K3DataUtils.getString(row, index++));

            // F_cty_BaseProperty4 - 修改状态
            entry.setFCtyBaseProperty4(K3DataUtils.getString(row, index++));

            // Fkhywms - 客户英文描述
            entry.setFKhywms(K3DataUtils.getString(row, index++));

            // F_cty_BaseProperty6 - 延迟原因
            entry.setFCtyBaseProperty6(K3DataUtils.getString(row, index++));

            // F_ygcbdj - 实际成本单价
            entry.setFYgcbdj(K3DataUtils.getBigDecimal(row, index++));

            // F_ygcbdj1 - 预估成本单价
            entry.setFYgcbdj1(K3DataUtils.getBigDecimal(row, index++));

            // F_ygcb1 - 预估成本
            entry.setFYgcb1(K3DataUtils.getBigDecimal(row, index++));

            // F_PEUU_Attachment_83g - 验货报告
            entry.setFPeuuAttachment83g(K3DataUtils.getString(row, index++));

            // Fcgddshrq - 采购订单审核日期
            entry.setFCgddshrq(convertToLocalDateToDate(DateUtils.getLocalDate(row, index++)));

            // Fyjsj - 预警时间
            entry.setFYjsj(convertToLocalDateToDate(DateUtils.getLocalDate(row, index++)));

            // Fslshrq - 收料审核日期
            entry.setFSlshrq(convertToLocalDateToDate(DateUtils.getLocalDate(row, index++)));

            // F_XLCP - 新老产品
            entry.setFXlcp(K3DataUtils.getString(row, index++));

            // F_cplb - 产品大类
            entry.setFCplb(K3DataUtils.getString(row, index++));

            // F_ckrq - 出库日期
            entry.setFCkrq(convertToLocalDateToDate(DateUtils.getLocalDate(row, index++)));

            // F_cty_BaseProperty7 - 供应商评级
            entry.setFCtyBaseProperty7(K3DataUtils.getString(row, index++));

            // F_cty_BaseProperty8 - 工厂问题
            entry.setFCtyBaseProperty8(K3DataUtils.getString(row, index++));

        } catch (Exception e) {
            log.error("解析销售订单明细表数据失败", e);
            throw new RuntimeException("解析销售订单明细表数据失败", e);
        }

        return entry;
    }

    /**
     * 解析销售订单成本表行数据
     * 将金蝶接口返回的List<Object>转换为SaleOrderCost实体
     *
     * @param row 行数据
     * @return SaleOrderCost实体
     */
    public static SaleOrderCost parseSaleOrderCostFromRow(List<Object> row) {
        SaleOrderCost cost = new SaleOrderCost();

        try {
            if (row == null || row.isEmpty()) {
                log.warn("销售订单成本表行数据为空");
                return cost;
            }

            int index = 0;

            // FID - 主键=销售订单FID
            cost.setFid(K3DataUtils.getLong(row, index++));
            cost.setFbillno(K3DataUtils.getString(row, index++));

            // F_hyf - 海运费（外币）
            cost.setFHyf(K3DataUtils.getBigDecimal(row, index++));

            // FBillAllAmount - 价税合计
            cost.setFBillAllAmount(K3DataUtils.getBigDecimal(row, index++));

            // FBillAllAmount_LC - 价税合计（本位币）
            cost.setFBillAllAmountLc(K3DataUtils.getBigDecimal(row, index++));

            // F_bxf - 保险费
            cost.setFBxf(K3DataUtils.getBigDecimal(row, index++));

            // F_gwyhfy - 国外银行费用
            cost.setFGwyhfy(K3DataUtils.getBigDecimal(row, index++));

            // F_qtwbfy - 其他外币费用
            cost.setFQtwbfy(K3DataUtils.getBigDecimal(row, index++));

            // F_mxcbhj - 明细成本合计
            cost.setFMxcbhj(K3DataUtils.getBigDecimal(row, index++));

            // F_mxtshj - 明细退税合计
            cost.setFMxtshj(K3DataUtils.getBigDecimal(row, index++));

            // F_cbxj - 成本小计RMB
            cost.setFCbxj(K3DataUtils.getBigDecimal(row, index++));

            // F_bzf - 包装费
            cost.setFBzf(K3DataUtils.getBigDecimal(row, index++));

            // F_dlf - 代理费
            cost.setFDlf(K3DataUtils.getBigDecimal(row, index++));

            // F_rzf - 认证费
            cost.setFRzf(K3DataUtils.getBigDecimal(row, index++));

            // F_kdf - 快递费成本
            cost.setFKdf(K3DataUtils.getBigDecimal(row, index++));

            // F_hdf - 货贷费
            cost.setFHdf(K3DataUtils.getBigDecimal(row, index++));

            // F_lyf - 陆运费
            cost.setFLyf(K3DataUtils.getBigDecimal(row, index++));

            // F_qtfy - 其他费用
            cost.setFQtfy(K3DataUtils.getBigDecimal(row, index++));

            // F_mjf - 模具费
            cost.setFMjf(K3DataUtils.getBigDecimal(row, index++));

            // F_jcf - 进仓费
            cost.setFJcf(K3DataUtils.getBigDecimal(row, index++));

            // F_fyxj - 费用小计
            cost.setFFyxj(K3DataUtils.getBigDecimal(row, index++));

            // F_lrl - 毛净利润率%
            cost.setFLrl(K3DataUtils.getBigDecimal(row, index++));

            // F_wbyk - 外币盈亏
            cost.setFWbyk(K3DataUtils.getBigDecimal(row, index++));

            // F_jlrl - 净利润率%
            cost.setFJlrl(K3DataUtils.getBigDecimal(row, index++));

            // F_jlre - 净利润额
            cost.setFJlre(K3DataUtils.getBigDecimal(row, index++));

        } catch (Exception e) {
            log.error("解析销售订单成本表数据失败", e);
            throw new RuntimeException("解析销售订单成本表数据失败", e);
        }

        return cost;
    }

    /**
     * 将LocalDate转换为Date
     *
     * @param localDate LocalDate对象
     * @return Date对象，如果输入为null则返回null
     */
    private static Date convertToLocalDateToDate(java.time.LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return java.util.Date.from(localDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
    }

}
