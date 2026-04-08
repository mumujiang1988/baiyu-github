package com.ruoyi.business.k3.service.impl;

import com.ruoyi.business.k3.domain.entity.RectunitDetail;
import com.ruoyi.business.k3.service.RectunitDetailService;
import com.ruoyi.business.mapper.RectunitDetailMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class RectunitDetailServiceImpl implements RectunitDetailService {
    @Resource
    private RectunitDetailMapper detailMapper;
    /**
     * 收款单信明细息表
     * */
    @Override
    public void syncRectunitDetail(List<List<Object>> PaymentApplicationList) {
        PaymentApplicationList.forEach(en ->{
            RectunitDetail detail = new RectunitDetail();
            // 实体主键
            detail.setFEntryId(Long.valueOf(en.get(0).toString())  != null ? Long.valueOf(en.get(0).toString()) : null);
            //结算方式
            detail.setFSettletypeId(en.get(1).toString() != null ? en.get(1).toString() : null);
            //应付金额
            Object fPayamountFor = en.get(2);
            if(fPayamountFor != null){
                String PayamountFor = fPayamountFor.toString();
                BigDecimal payamountFor = new BigDecimal(PayamountFor);
                detail.setFPayamountFor(payamountFor);
            }
            //对方银行账号
            detail.setFEachbankaccount(en.get(3).toString() != null ? en.get(3).toString() : null);

            //申请付款金额
            Object fApplyamountFor = en.get(4);
            if(fApplyamountFor != null){
                String ApplyamountFor = fApplyamountFor.toString();
                BigDecimal applyamountFor = new BigDecimal(ApplyamountFor);
                detail.setFApplyamountFor(applyamountFor);
            }
            //付款用途
            detail.setFPaypurposeId(en.get(5).toString() != null ? en.get(5).toString() : null);
            //收款用途
            detail.setFArpurposeId(en.get(6).toString() != null ? en.get(6).toString() : null);
            //源单类型
            detail.setFSourceType(en.get(7).toString() != null ? en.get(7).toString() : null);
            detail.setFSrcbillNo(en.get(8).toString() != null ? en.get(8).toString() : null);
            //到期日
            Optional.ofNullable(en.get(9))
                .map(Object::toString)
                .filter(s -> !s.trim().isEmpty())
                .ifPresent(s -> detail.setFEnddate(LocalDateTime.parse(s)));
            //期望付款日期
            Optional.ofNullable(en.get(10))
                .map(Object::toString)
                .filter(s -> !s.trim().isEmpty())
                .ifPresent(s -> detail.setFExpectpayDate(LocalDateTime.parse(s)));
            //对方账户名称
            detail.setFEachccountName(Objects.toString(en.get(11), null));
            //对方开户行
            detail.setFEachbankName(Objects.toString(en.get(12), null));
            //备注（作废
            detail.setFComment(Objects.toString(en.get(13), null));
            //源单行内码
            detail.setFSrcrowId(en.get(14).toString() != null ? en.get(14).toString() : null);
            //付退款关联金额
            Object fRelatepayamount = en.get(15);
            if(fApplyamountFor != null){
                String Relatepayamount = fRelatepayamount.toString();
                BigDecimal relatepayamount = new BigDecimal(Relatepayamount);
                detail.setFRelatepayamount(relatepayamount);
            }
            //作废—收款退款关联金额
            Object fRelaterefundamount = en.get(16);
            if(fRelaterefundamount != null){
                String Relaterefundamount = fRelaterefundamount.toString();
                BigDecimal relaterefundamount = new BigDecimal(Relaterefundamount);
                detail.setFRelaterefundamount(relaterefundamount);
            }
            //费用项目
            detail.setFCostId(en.get(17).toString() != null ? en.get(17).toString() : null);
            //申请还本金额
            Object fApplypclamount = en.get(18);
            if(fApplypclamount != null){
                String Applypclamount = fApplypclamount.toString();
                BigDecimal applypclamount = new BigDecimal(Applypclamount);
                detail.setFApplypclamount(applypclamount);
            }
            //申请付利息金额
            Object fApplyinstamount = en.get(19);
            if(fApplyinstamount != null){
                String Applyinstamount = fApplyinstamount.toString();
                BigDecimal applyinstamount = new BigDecimal(Applyinstamount);
                detail.setFApplyinstamount(applyinstamount);
            }
            //关联付利息金额
            Object fWritepclamount = en.get(20);
            if(fWritepclamount != null){
                String Writepclamount = fWritepclamount.toString();
                BigDecimal writepclamount = new BigDecimal(Writepclamount);
                detail.setFWritepclamount(writepclamount);
            }
            //备注
            detail.setFDescription(en.get(21).toString() != null ? en.get(21).toString() : null);
            //物料编码
            detail.setFMaterialId(en.get(22).toString() != null ? en.get(22).toString() : null);
            //物料名称
            detail.setFMaterialName(Objects.toString(en.get(23), null));

            //订单号
            detail.setFPurchaseorderNo(en.get(24).toString() != null ? en.get(24).toString() : null);
            //订单行号
            detail.setFMaterialseq(en.get(25).toString() != null ? en.get(25).toString() : null);
            //订单明细内码
            detail.setFOrderentryId(en.get(26).toString() != null ? en.get(26).toString() : null);
            //含税单价
            Object fPrice = en.get(27);
            if(fPrice != null){
                String Price = fPrice.toString();
                BigDecimal price = new BigDecimal(Price);
                detail.setFPrice(price);
            }

            //数量
            Object fQty = en.get(28);
            if(fQty != null){
                String Qty = fQty.toString();
                BigDecimal qty = new BigDecimal(Qty);
                detail.setFQty(qty);
            }
            //计价单位
            detail.setFPriceunitId(en.get(29).toString() != null ? en.get(29).toString() : null);
            //付(退)款关联数量
            Object fRelatepayqty = en.get(30);
            if(fRelatepayqty != null){
                String Relatepayqty = fRelatepayqty.toString();
                BigDecimal relatepayqty = new BigDecimal(Relatepayqty);
                detail.setFRelatepayqty(relatepayqty);
            }
            //未付款金额
            Object fUnpaidamount = en.get(31);
            if(fUnpaidamount != null){
                String Unpaidamount = fUnpaidamount.toString();
                BigDecimal unpaidamount = new BigDecimal(Unpaidamount);
                detail.setFUnpaidamount(unpaidamount);
            }
            //SwiftCode
            detail.setFSwiftcode(en.get(32).toString() != null ? en.get(32).toString() : null);
            //税额
            Object fTaxamount = en.get(33);
            if(fTaxamount != null){
                String Taxamount = fTaxamount.toString();
                BigDecimal taxamount = new BigDecimal(Taxamount);
                detail.setFTaxamount(taxamount);
            }
            //银行网点
            detail.setFBankdetail(en.get(34).toString() != null ? en.get(34).toString() : null);
            //费用承担部门
            detail.setFExpensedeptId(en.get(35).toString() != null ? en.get(35).toString() : null);
            //应付比例（%）
            Object fYfbl = en.get(36);
            if(fYfbl != null){
                String Yfbl = fYfbl.toString();
                BigDecimal yfbl = new BigDecimal(Yfbl);
                detail.setFYfbl(yfbl);
            }
            //出运单号
            detail.setFCydh1(en.get(37).toString() != null ? en.get(37).toString() : null);
            //采购订单号
            detail.setFCgddh(en.get(38).toString() != null ? en.get(38).toString() : null);
            //费用承担部门1
            detail.setFFycdbm(en.get(39).toString() != null ? en.get(39).toString() : null);
            //比例%
            Object fCtyDecimal = en.get(40);
            if(fCtyDecimal != null){
                String CtyDecimal = fCtyDecimal.toString();
                BigDecimal ctyDecimal = new BigDecimal(CtyDecimal);
                detail.setFCtyDecimal(ctyDecimal);
            }
            //承担方式
            detail.setFCdfs(en.get(41).toString() != null ? en.get(41).toString() : null);
            //开票状态
            detail.setFKpzt(en.get(42).toString() != null ? en.get(42).toString() : null);
            //客户
            detail.setFkh(en.get(43).toString() != null ? en.get(43).toString() : null);
            //客户简称
            detail.setFCtyBaseproperty(Objects.toString(en.get(44), null));
            //付款日期
            Optional.ofNullable(en.get(10))
                .map(Object::toString)
                .filter(s -> !s.trim().isEmpty())
                .ifPresent(s -> detail.setFPeuuDate83g(LocalDateTime.parse(s)));
            List<RectunitDetail> details  = detailMapper.selectByEntryId(detail.getFEntryId());
            if (details != null && !details.isEmpty()){
                details.forEach(e ->{
                    detail.setId(e.getId());
                    detailMapper.updateById(detail);
                });
            }else {
                detailMapper.insert(detail);
            }

        });
    }
}
