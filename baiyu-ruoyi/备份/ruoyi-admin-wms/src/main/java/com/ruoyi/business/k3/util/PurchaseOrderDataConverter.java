package com.ruoyi.business.k3.util;

import com.ruoyi.business.entity.PoOrderBillHead;
import com.ruoyi.business.entity.PoOrderBillHeadEntry;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;

/**
 * 采购订单数据转换工具类
 * 用于将金蝶接口返回的List<Object>数据转换为对应的实体对象
 */
@Slf4j
public class PurchaseOrderDataConverter {

    /**
     * 解析采购订单主表行数据
     * 将金蝶接口返回的List<Object>转换为PoOrderBillHead实体
     *
     * @param row 行数据
     * @return PoOrderBillHead实体
     */
    public static PoOrderBillHead parsePurchaseOrderFromRow(List<Object> row) {
        PoOrderBillHead order = new PoOrderBillHead();

        try {
            if (row == null || row.isEmpty()) {
                log.warn("采购订单主表行数据为空");
                return order;
            }

            int index = 0;

            // 0: FID - 忽略（K3内部ID）
            order.setFid(String.valueOf(K3DataUtils.getLong(row, index++)));

            // 1: FBillNo - 单据编号
            order.setFbillNo(K3DataUtils.getString(row, index++));

            // 2: FDate - 日期
            order.setFdate(convertToLocalDateToDate(DateUtils.getLocalDateTime(row, index++)));

            // 3: FBusinessType - 业务类型
            order.setFbusinessType(K3DataUtils.getString(row, index++));

            // 4: FBillTypeID - 单据类型ID
            order.setFbillTypeId(K3DataUtils.getString(row, index++));

            // 5: FSupplierId - 供应商ID
            order.setFsupplierId(K3DataUtils.getString(row, index++));

            // 6: FCreatorId - 创建人ID
            order.setFcreatorId(K3DataUtils.getString(row, index++));

            // 7: FCreateDate - 创建日期
            order.setFcreateDate(convertToLocalDateToDate(DateUtils.getLocalDateTime(row, index++)));
            order.setFmodifierId(K3DataUtils.getString(row, index++));

            order.setFmodifyDate(convertToLocalDateToDate(DateUtils.getLocalDateTime(row, index++)));
            order.setFapproverId(K3DataUtils.getString(row, index++));
            order.setFapproveDate(convertToLocalDateToDate(DateUtils.getLocalDateTime(row, index++)));

            // 9: FCloseStatus - 关闭状态
            order.setFcloseStatus(K3DataUtils.getString(row, index++));

            // 10: F_gyszh - 供应商账户
            order.setFGyszh(K3DataUtils.getString(row, index++));

            // 11: FACCTYPE - 会计类型
            order.setFacctype(K3DataUtils.getString(row, index++));

            // 12: F_gdy1 - 跟单员1
            order.setFGdy1(K3DataUtils.getString(row, index++));

            // 13: F_ora_Date - Oracle日期
            order.setFOraDate(K3DataUtils.getString(row, index++));

            // 14: FPayConditionId - 付款条件ID
            order.setFPayConditionId(K3DataUtils.getString(row, index++));

            // 15: F_XSY - 销售员
            order.setFXsy(K3DataUtils.getString(row, index++));

            // 16: F_KHJQNEW - 客户紧急度
            order.setFKhjqnew(K3DataUtils.getString(row, index++));

            // 17: FChangeReason - 变更原因
            order.setFchangeReason(K3DataUtils.getString(row, index++));

            // 18: F_gcbz - 工厂包装
            order.setFGcbz(K3DataUtils.getString(row, index++));

            // 19: F_cty_BaseProperty2 - 自定义属性2
            order.setFCtyBaseproperty2(K3DataUtils.getString(row, index++));

            // 20: F_ZLBZ - 质量标准
            order.setFZlbz(K3DataUtils.getString(row, index++));

            // 21: F_BZBZJYQ - 包装标准及要求
            order.setFBzbzjyq(K3DataUtils.getString(row, index++));

            // 22: Fjsfsjjq - 结算方式及期限
            order.setFjsfsjjq(K3DataUtils.getString(row, index++));

            // 23: Fhttk13 - 合同条款13
            order.setFhttk13(K3DataUtils.getString(row, index++));

            // 24: Fhttk510 - 合同条款510
            order.setFhttk510(K3DataUtils.getString(row, index++));

            // 25: FProviderContactId - 供应商联系人ID
            order.setFproviderContactId(K3DataUtils.getString(row, index++));

            // 26: FProviderJob - 供应商职务
            order.setFproviderJob(K3DataUtils.getString(row, index++));

            // 27: FProviderAddress - 供应商地址
            order.setFproviderAddress(K3DataUtils.getString(row, index++));

            // 28: FSettleId - 结算方ID
            order.setFsettleId(K3DataUtils.getString(row, index++));

            // 29: FChargeId - 付款方ID
            order.setFchargeId(K3DataUtils.getString(row, index++));

            // 30: FSettleCurrId - 结算币种ID
            order.setFSettleCurrId(K3DataUtils.getString(row, index++));

            // 31: FEntrySettleModeId - 入库结算方式ID
            order.setFEntrySettleModeId(K3DataUtils.getString(row, index++));

            // 32: FExchangeTypeId - 汇率类型ID
            order.setFExchangeTypeId(K3DataUtils.getString(row, index++));

            // 33: FExchangeRate - 汇率
            order.setFExchangeRate(K3DataUtils.getBigDecimal(row, index++));

            // 34: FPriceListId - 价格表ID
            order.setFPriceListId(K3DataUtils.getString(row, index++));

            // 35: FPriceTimePoint - 价格时间点
            order.setFPriceTimePoint(K3DataUtils.getString(row, index++));

            // 36: FDepositRatio - 保证金比例
            order.setFDepositRatio(K3DataUtils.getBigDecimal(row, index++));

            // 37: FBillTaxAmount_LC - 税额(本位币)
            order.setFBillTaxAmountLc(K3DataUtils.getBigDecimal(row, index++));

            // 38: FBillAmount_LC - 金额(本位币)
            order.setFBillAmountLc(K3DataUtils.getBigDecimal(row, index++));

            // 39: FBillAllAmount - 价税合计
            order.setFBillAllAmount(K3DataUtils.getBigDecimal(row, index++));

            // 40: FLocalCurrId - 本位币ID
            order.setFLocalCurrId(K3DataUtils.getString(row, index++));

            // 41: FBillTaxAmount - 税额
            order.setFBillTaxAmount(K3DataUtils.getBigDecimal(row, index++));

            // 42: FAllAmount - 总金额
            order.setFAllAmount(K3DataUtils.getBigDecimal(row, index++));

            // 43: FEntryAmount - 入库金额
            order.setFEntryAmount(K3DataUtils.getBigDecimal(row, index++));

            // 44: FIsIncludedTax - 是否含税
            order.setFIsIncludedTax(K3DataUtils.getString(row, index++));

            // 45: FISPRICEEXCLUDETAX - 价外税
            order.setFIspriceExcludetax(K3DataUtils.getString(row, index++));

            // 46: FDeposit - 保证金
            order.setFDeposit(K3DataUtils.getBigDecimal(row, index++));

            // 47: FAllDisCount - 整单折扣额
            order.setFAllDisCount(K3DataUtils.getBigDecimal(row, index++));

            // 48: FALLPAYAMOUNT - 应付总金额
            order.setFAllPayAmount(K3DataUtils.getBigDecimal(row, index++));

            // 49: FALLPAYAPPLYAMOUNT - 申请应付总金额
            order.setFAllPayApplyAmount(K3DataUtils.getBigDecimal(row, index++));

            // 50: FALLAPPYNOPAYAMOUNT - 申请未付总金额
            order.setFAllApplyNoPayAmount(K3DataUtils.getBigDecimal(row, index++));

            // 51: FPAYRELATAMOUNT - 付款关联金额
            order.setFPayRelatAmount(K3DataUtils.getBigDecimal(row, index++));

            // 52: FALLREFUNDAMOUNT - 退款总金额
            order.setFAllRefundAmount(K3DataUtils.getBigDecimal(row, index++));

            // 53: FALLNOPAYAMOUNT - 未付总金额
            order.setFAllNoPayAmount(K3DataUtils.getBigDecimal(row, index++));

            // 54: FBillAmount - 单据金额
            order.setFBillAmount(K3DataUtils.getBigDecimal(row, index++));

            // 55: FBillAllAmount_LC - 单据价税合计(本位币)
            order.setFBillAllAmount_LC(K3DataUtils.getBigDecimal(row, index++));

        } catch (Exception e) {
            log.error("解析采购订单主表数据失败", e);
            throw new RuntimeException("解析采购订单主表数据失败", e);
        }

        return order;
    }

    /**
     * 从行数据解析采购订单详情实体
     */
    public static PoOrderBillHeadEntry parsePurchaseOrderEntryFromRow(List<Object> row) {
        PoOrderBillHeadEntry entry = new PoOrderBillHeadEntry();

        try {
            if (row == null || row.isEmpty()) {
                log.warn("采购订单详情行数据为空");
                return entry;
            }

            int index = 0;

            // FBillNo - 单据编号 (现在对应实体类的fid字段)
            entry.setFid(K3DataUtils.getString(row, index++));
            entry.setFbillNo(K3DataUtils.getString(row, index++));

            // F_sfbg - 是否报关
            entry.setFSfbg(K3DataUtils.getInteger(row, index++));

            //  F_jqhx - 交期红线
            entry.setFJqhx(K3DataUtils.getString(row, index++));

            //  F_cplb - 产品类别
            entry.setFCplb(K3DataUtils.getString(row, index++));

            //F_bzbz - 包装补做
            entry.setFBzbz(K3DataUtils.getInteger(row, index++));

            //  F_khhh - 客户货号
            entry.setFKhhh(K3DataUtils.getString(row, index++));

            //  F_cpdm - 产品代码
            entry.setFCpdm(K3DataUtils.getString(row, index++));

            //  F_ora_BaseProperty - 产品名称
            entry.setFOraBaseproperty(K3DataUtils.getString(row, index++));

            // F_ora_BaseProperty1 - 产品规格
            entry.setFOraBaseproperty1(K3DataUtils.getString(row, index++));

            //  F_GYSWLBM - 供应商物料编码
            entry.setFGyswlbm(K3DataUtils.getString(row, index++));

            //  F_GYSWLMC - 供应商物料名称
            entry.setFGyswlmc(K3DataUtils.getString(row, index++));

            //  FQty - 采购数量
            entry.setFqty(K3DataUtils.getBigDecimal(row, index++));

            //  F_Cpsl - 产品数量
            entry.setFCpsl(K3DataUtils.getBigDecimal(row, index++));

            //  FUnitId - 采购单位
            entry.setFunitid(K3DataUtils.getString(row, index++));

            //  FPriceUnitId - 计价单位
            entry.setFpriceunitid(K3DataUtils.getString(row, index++));

            //  FDeliveryDate - 交货日期
            entry.setFdeliverydate(DateUtils.getLocalDate(row, index++));


            //  F_Ckj - 参考价
            entry.setFCkj(K3DataUtils.getBigDecimal(row, index++));

            //  F_cbj - 成本价
            entry.setFCbj(K3DataUtils.getBigDecimal(row, index++));

            //  FPrice - 单价
            entry.setFprice(K3DataUtils.getBigDecimal(row, index++));

            //  FTaxPrice - 含税单价
            entry.setFtaxprice(K3DataUtils.getBigDecimal(row, index++));

            //  F_jjhsdj - 加价含税单价
            entry.setFJjhsdj(K3DataUtils.getBigDecimal(row, index++));

            //  F_kphsdj - 开票含税单价
            entry.setFKphsdj(K3DataUtils.getBigDecimal(row, index++));

            //  FEntryDiscountRate - 折扣率%
            entry.setFentrydiscountrate(K3DataUtils.getBigDecimal(row, index++));

            //  FEntryTaxRate - 税率%
            entry.setFentrytaxrate(K3DataUtils.getBigDecimal(row, index++));

            //  FEntryTaxAmount - 税额
            entry.setFentrytaxamount(K3DataUtils.getBigDecimal(row, index++));

            //  FAllAmount - 价税合计
            entry.setFallamount(K3DataUtils.getBigDecimal(row, index++));

            //  FEntryAmount - 金额
            entry.setFentryamount(K3DataUtils.getBigDecimal(row, index++));

            //  FEntryNote - 备注
            entry.setFentrynote(K3DataUtils.getString(row, index++));

            //  FBillStatus - 验货状态
            entry.setFbillstatus(K3DataUtils.getString(row, index++));


            //  F_zxs - 装箱数
            entry.setFZxs(K3DataUtils.getInteger(row, index++));

            //  F_mzz - 毛总重
            entry.setFMzz(K3DataUtils.getBigDecimal(row, index++));

            //  F_xs - 总箱数
            entry.setFXs(K3DataUtils.getInteger(row, index++));

            //  F_bcfysl - 包材费用数量
            entry.setFBcfysl(K3DataUtils.getBigDecimal(row, index++));

            //  FBillStatus1 - 包材费用状态
            entry.setFbillstatus1(K3DataUtils.getString(row, index++));

            //  F_xtslddsl - 下推收料订单数量
            entry.setFXtslddsl(K3DataUtils.getBigDecimal(row, index++));

            //  FBillStatus3 - 是否完全下推收料单
            entry.setFbillstatus3(K3DataUtils.getString(row, index++));

            //  F_yhbl - 验货比例
            entry.setFYhbl(K3DataUtils.getBigDecimal(row, index++));

            //  F_bzfs - 包装要求
            entry.setFBzfs(K3DataUtils.getString(row, index++));

            //  F_smsfj - 说明书附件
            entry.setFSmsfj(K3DataUtils.getString(row, index++));

            //  F_cpgys - 产品供应商
            entry.setFCpgys(K3DataUtils.getString(row, index++));

            //  F_bcjsd - 包材接收地
            entry.setFBcjsd(K3DataUtils.getString(row, index++));

            //  F_bcjsr - 包材接收人
            entry.setFBcjsr(K3DataUtils.getString(row, index++));

            //  F_bcjsrdh - 包材接收人电话
            entry.setFBcjsrdh(K3DataUtils.getString(row, index++));

            //  F_bgdw - 报关单位
            entry.setFBgdw(K3DataUtils.getString(row, index++));

            //  F_sbys - 申报要素
            entry.setFSbys(K3DataUtils.getString(row, index++));

            //  F_CYS - 包材承运商
            entry.setFCys(K3DataUtils.getString(row, index++));

            //  F_KDDH - 快递单号
            entry.setFKddh(K3DataUtils.getString(row, index++));

            //  F_JCRQ - 寄出日期
            entry.setFJcrq(DateUtils.getLocalDate(row, index++));

            //  F_jgtzyy - 价格调整原因
            entry.setFJgtzyy(K3DataUtils.getString(row, index++));

            //  F_cpzlyq - 产品质量要求
            entry.setFCpzlyq(K3DataUtils.getString(row, index++));

            //  Fbzyq - 包装要求
            entry.setFbzyq(K3DataUtils.getString(row, index++));

            //  Ftsyq - 特殊要求
            entry.setFtsyq(K3DataUtils.getString(row, index++));

            //  Fcptp - 产品图片
            entry.setFcptp(K3DataUtils.getString(row, index++));

            //  Fbzgctg - 包装工厂提供
            entry.setFbzgctg(K3DataUtils.getInteger(row, index++));

            //  Fgchh - 工厂货号
            entry.setFgchh(K3DataUtils.getString(row, index++));

            //  Fxsddh - 销售订单号
            entry.setFxsddh(K3DataUtils.getString(row, index++));

            //  Fshrq - 审核日期
            entry.setFshrq(DateUtils.getLocalDate(row, index++));

            //  Fxddrkts - 下单到入库天数
            entry.setFxddrkts(K3DataUtils.getInteger(row, index++));

            //  Fbjr - 报价人
            entry.setFbjr(K3DataUtils.getString(row, index++));

            //  F_CPLB1 - 产品类别1
            entry.setFCplb1(K3DataUtils.getString(row, index++));

        } catch (Exception e) {
            log.error("解析采购订单详情数据失败", e);
            throw new RuntimeException("解析采购订单详情数据失败", e);
        }

        return entry;
    }

    /**
     * 将LocalDateTime转换为Date
     *
     * @param localDateTime LocalDateTime对象
     * @return Date对象，如果输入为null则返回null
     */
    private static Date convertToLocalDateToDate(java.time.LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return java.util.Date.from(localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant());
    }
}
