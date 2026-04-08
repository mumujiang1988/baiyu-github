package com.ruoyi.business.k3.service.impl;

import com.ruoyi.business.k3.domain.entity.SalOutboundDetails;
import com.ruoyi.business.k3.service.SalOutboundDetailService;
import com.ruoyi.business.mapper.SalOutboundDetailMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class SalOutboundDetailServiceIpml implements SalOutboundDetailService {
    @Autowired
    private SalOutboundDetailMapper detailMapper;

    @Override
    public void syncSalOutboundDetails(List<List<Object>> detailsList) {
        detailsList.forEach(en ->{
            SalOutboundDetails details = new SalOutboundDetails();
            details.setFEntryId(Long.valueOf(en.get(0).toString())  != null ? Long.valueOf(en.get(0).toString()) : null);
            //客户物料编码
            details.setFCustmatId(en.get(1).toString() != null ? en.get(1).toString() : null);
            //客户物料名称
            details.setFCustmatName(Objects.toString(en.get(2), null));
            //物料名称
            details.setFMaterialName(en.get(3).toString() != null ? en.get(3).toString() : null);
            //物料编码 (必填项)
            details.setFMateriaModel(en.get(4).toString() != null ? en.get(4).toString() : null);
            //物料类别
            details.setFMateriaType(en.get(5).toString() != null ? en.get(5).toString() : null);
            //库存单位
            details.setFUnitId(en.get(6).toString() != null ? en.get(6).toString() : null);
            //应发数量
            Object fMustQty = en.get(7);
            if(fMustQty != null){
                String MustQty = fMustQty.toString();
                BigDecimal mustQty = new BigDecimal(MustQty);
                details.setFMustQty(mustQty);
            }
            //实发数量
            Object fRealQty = en.get(8);
            if(fRealQty != null){
                String RealQty = fRealQty.toString();
                BigDecimal realQty = new BigDecimal(RealQty);
                details.setFRealQty(realQty);
            }
            //仓库
            details.setFStockId(en.get(9).toString() != null ? en.get(9).toString() : null);
            //库存状态
            details.setFStocksStatusId(en.get(10).toString() != null ? en.get(10).toString() : null);
            //货主类型 (必填项)
            details.setFOwnerTypeId(en.get(11).toString() != null ? en.get(11).toString() : null);
            //货主
            details.setFOwnerId(en.get(12).toString() != null ? en.get(12).toString() : null);
            //保管者类型
            details.setFKeeperTypeId(en.get(13).toString() != null ? en.get(13).toString() : null);
            //保管者
            details.setFKeeperId(en.get(14).toString() != null ? en.get(14).toString() : null);
            //备注
            details.setFEntrynote(en.get(15).toString() != null ? en.get(15).toString() : null);
            //BOM版本
            details.setFBomId(en.get(16).toString() != null ? en.get(16).toString() : null);
            //库存基本数量
            Object fBaseUnitQty = en.get(17);
            if(fBaseUnitQty != null){
                String BaseUnitQty = fBaseUnitQty.toString();
                BigDecimal baseUnitQty = new BigDecimal(BaseUnitQty);
                details.setFBaseUnitQty(baseUnitQty);
            }
            //库存辅单位
            details.setFAuxunitId(en.get(18).toString() != null ? en.get(18).toString() : null);
            //库存辅单位数量
            Object fAuxunitQty = en.get(19);
            if(fAuxunitQty != null){
                String AuxunitQty = fAuxunitQty.toString();
                BigDecimal auxunitQty = new BigDecimal(AuxunitQty);
                details.setFAuxunitQty(auxunitQty);
            }
            //成本价（本位币）
            Object fCostPrice = en.get(20);
            if(fCostPrice != null){
                String CostPrice = fCostPrice.toString();
                BigDecimal costPrice = new BigDecimal(CostPrice);
                details.setFCostPrice(costPrice);
            }
            //总成本
            Object fEntrycostAmount = en.get(21);
            if(fEntrycostAmount != null){
                String EntrycostAmount = fEntrycostAmount.toString();
                BigDecimal entrycostAmount = new BigDecimal(EntrycostAmount);
                details.setFEntrycostAmount(entrycostAmount);
            }
            //总成本(本位币)
            Object fCostAmountLc = en.get(22);
            if(fCostAmountLc != null){
                String CostAmountLc = fCostAmountLc.toString();
                BigDecimal costAmountLc = new BigDecimal(CostAmountLc);
                details.setFCostAmountLc(costAmountLc);
            }
            //关联退货数量
            Object fReturnQty = en.get(23);
            if(fReturnQty != null){
                String ReturnQty = fReturnQty.toString();
                BigDecimal returnQty = new BigDecimal(ReturnQty);
                details.setFReturnQty(returnQty);
            }
            //累计退货通知数量
            Object fSumretNoticeQty = en.get(24);
            if(fSumretNoticeQty != null){
                String SumretNoticeQty = fSumretNoticeQty.toString();
                BigDecimal sumretNoticeQty = new BigDecimal(SumretNoticeQty);
                details.setFSumretNoticeQty(sumretNoticeQty);
            }
            //累计退货数量
            Object fSumretStockQty = en.get(25);
            if(fSumretStockQty != null){
                String SumretStockQty = fSumretStockQty.toString();
                BigDecimal sumretStockQty = new BigDecimal(SumretStockQty);
                details.setFSumretStockQty(sumretStockQty);
            }
            //累计开票数量(作废)
            Object fInvoicedQty = en.get(26);
            if(fInvoicedQty != null){
                String InvoicedQty = fInvoicedQty.toString();
                BigDecimal invoicedQty = new BigDecimal(InvoicedQty);
                details.setFInvoicedQty(invoicedQty);
            }
            //累计应收数量（销售）
            Object fSuminvoicedQty = en.get(27);
            if(fSuminvoicedQty != null){
                String SuminvoicedQty = fSuminvoicedQty.toString();
                BigDecimal suminvoicedQty = new BigDecimal(SuminvoicedQty);
                details.setFSuminvoicedQty(suminvoicedQty);
            }
            //累计开票金额(作废)
            Object fSuminvoicedAmt = en.get(28);
            if(fSuminvoicedAmt != null){
                String SuminvoicedAmt = fSuminvoicedAmt.toString();
                BigDecimal suminvoicedAmt = new BigDecimal(SuminvoicedAmt);
                details.setFSuminvoicedAmt(suminvoicedAmt);
            }
            //累计收款金额
            Object fSumreceivedAmt = en.get(29);
            if(fSumreceivedAmt != null){
                String SumreceivedAmt = fSumreceivedAmt.toString();
                BigDecimal sumreceivedAmt = new BigDecimal(SumreceivedAmt);
                details.setFSumreceivedAmt(sumreceivedAmt);
            }
            //关联退货数量(基本单位)
            Object fBasereturnQty = en.get(30);
            if(fBasereturnQty != null){
                String BasereturnQty = fBasereturnQty.toString();
                BigDecimal basereturnQty = new BigDecimal(BasereturnQty);
                details.setFBasereturnQty(basereturnQty);
            }
            //关联开票数量(基本单位)(作废)
            Object fBaseinvoicedQty = en.get(31);
            if(fBaseinvoicedQty != null){
                String BaseinvoicedQty = fBaseinvoicedQty.toString();
                BigDecimal baseinvoicedQty = new BigDecimal(BaseinvoicedQty);
                details.setFBaseinvoicedQty(baseinvoicedQty);
            }
            //库存更新标识
            details.setFStockFlag(en.get(32).toString() != null ? en.get(32).toString() : null);
            //销售订单单号
            details.setFSoorDerno(en.get(33).toString() != null ? en.get(33).toString() : null);
            //辅助属性
            details.setFAuxpropId(en.get(34).toString() != null ? en.get(34).toString() : null);
            //累计退货通知数量(销售基本)
            Object fBaseSumretNoticeQty = en.get(35);
            if(fBaseSumretNoticeQty != null){
                String BaseSumretNoticeQty = fBaseSumretNoticeQty.toString();
                BigDecimal baseSumretNoticeQty = new BigDecimal(BaseSumretNoticeQty);
                details.setFBaseSumretNoticeQty(baseSumretNoticeQty);
            }
            //源单类型
            details.setFSrcType(en.get(36).toString() != null ? en.get(36).toString() : null);
            //累计退货数量（库存基本）
            Object fStockBaseSumretStockQty = en.get(37);
            if(fStockBaseSumretStockQty != null){
                String StockBaseSumretStockQty = fStockBaseSumretStockQty.toString();
                BigDecimal stockBaseSumretStockQty = new BigDecimal(StockBaseSumretStockQty);
                details.setFBaseSumretStockQty(stockBaseSumretStockQty);
            }
            //仓位
            details.setFStockLocId(en.get(38).toString() != null ? en.get(38).toString() : null);
            //生产日期
            Optional.ofNullable(en.get(39))
                .map(Object::toString)
                .filter(s -> !s.trim().isEmpty())
                .ifPresent(s -> details.setFProduceDate(LocalDateTime.parse(s)));
            //有效期至
            Optional.ofNullable(en.get(40))
                .map(Object::toString)
                .filter(s -> !s.trim().isEmpty())
                .ifPresent(s -> details.setFExpiryDate(LocalDateTime.parse(s)));
            //勾稽数量（作废）
            details.setFJoinedQty(en.get(41).toString() != null ? en.get(41).toString() : null);
            //未勾稽数量（作废）
            details.setFUnjoinQty(en.get(42).toString() != null ? en.get(42).toString() : null);
            //勾稽金额（作废）
            Object fJoinedAmount = en.get(43);
            if(fJoinedAmount != null){
                String JoinedAmount = fJoinedAmount.toString();
                BigDecimal joinedAmount = new BigDecimal(JoinedAmount);
                details.setFJoinedAmount(joinedAmount);
            }
            //未勾稽金额（作废）
            Object fUnjoinAmount = en.get(44);
            if(fUnjoinAmount != null){
                String UnjoinAmount = fUnjoinAmount.toString();
                BigDecimal unjoinAmount = new BigDecimal(UnjoinAmount);
                details.setFUnjoinAmount(unjoinAmount);
            }
            //完全勾稽（作废）
            details.setFUllyJoined(en.get(45).toString() != null ? en.get(45).toString() : null);
            //行勾稽状态（作废）
            details.setFJoinStatus(en.get(46).toString() != null ? en.get(46).toString() : null);
            //批号
            details.setFLot(en.get(47).toString() != null ? en.get(47).toString() : null);
            //保质期单位
            details.setFExpiryPeriodUnit(en.get(48).toString() != null ? en.get(48).toString() : null);
            //保质期
            details.setFExpiryPeriod(Integer.valueOf(en.get(49).toString()) != null ? Integer.valueOf(en.get(49).toString()) : null);
            //是否赠品
            details.setFIsFree(en.get(50).toString() != null ? en.get(50).toString() : null);
            //累计开票数量(计价基本)(作废)
            Object fBaseSuminvoicedQty = en.get(51);
            if(fBaseSuminvoicedQty != null){
                String BaseSuminvoicedQty = fBaseSuminvoicedQty.toString();
                BigDecimal baseSuminvoicedQty = new BigDecimal(BaseSuminvoicedQty);
                details.setFBaseSuminvoicedQty(baseSuminvoicedQty);
            }
            //基本单位应发数量
            Object fBaseMustQty = en.get(52);
            if(fBaseMustQty != null){
                String BaseMustQty = fBaseMustQty.toString();
                BigDecimal baseMustQty = new BigDecimal(BaseMustQty);
                details.setFBaseMustQty(baseMustQty);
            }
            //基本单位
            details.setFBaseUnitId(en.get(53).toString() != null ? en.get(53).toString() : null);
            //到货确认
            details.setFArrivaIsTatus(en.get(54).toString() != null ? en.get(54).toString() : null);
            //到货确认人
            details.setFArrivalConfirmor(en.get(55).toString() != null ? en.get(55).toString() : null);
            //检验日期
            Optional.ofNullable(en.get(56))
                .map(Object::toString)
                .filter(s -> !s.trim().isEmpty())
                .ifPresent(s -> details.setFValiDate(LocalDateTime.parse(s)));
            //检验确认
            details.setFValidateStatus(en.get(57).toString() != null ? en.get(57).toString() : null);
            //检验确认人
            details.setFValidateConfirmor(en.get(58).toString() != null ? en.get(58).toString() : null);
            //计价单位
            details.setFPriceUnitId(en.get(59).toString() != null ? en.get(59).toString() : null);
            //计价数量
            Object fPriceUnitQty = en.get(60);
            if(fPriceUnitQty != null){
                String PriceUnitQty = fPriceUnitQty.toString();
                BigDecimal priceUnitQty = new BigDecimal(PriceUnitQty);
                details.setFPriceUnitQty(priceUnitQty);
            }
            //单价
            Object fPrice = en.get(61);
            if(fPrice != null){
                String Price = fPrice.toString();
                BigDecimal price = new BigDecimal(Price);
                details.setFPrice(price);
            }
            //含税单价
            Object fTaxPrice = en.get(62);
            if(fTaxPrice != null){
                String TaxPrice = fTaxPrice.toString();
                BigDecimal price = new BigDecimal(TaxPrice);
                details.setFTaxPrice(price);
            }
            //税组合
            details.setFTaxCombination(en.get(63).toString() != null ? en.get(63).toString() : null);
            //税率%
          /*  Object fEntryTaxRate = en.get(64);
            if(fEntryTaxRate != null){
                String EntryTaxRate = fEntryTaxRate.toString();
                BigDecimal entryTaxRate = new BigDecimal(EntryTaxRate);
                details.setFEntryTaxRate(entryTaxRate);
            }*/

            //查询数据是否存在
            List<SalOutboundDetails> detail = detailMapper.selectByIds(details.getFEntryId());
            if (detail != null && !detail.isEmpty()){
                detail.forEach(e -> {
                    details.setId(e.getId());
                    detailMapper.updateById(details);
                });
            }else {
                detailMapper.insert(details);
            }
        });
    }
}
