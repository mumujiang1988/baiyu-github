package com.ruoyi.business.k3.service.impl;

import com.ruoyi.business.k3.domain.entity.ReceivebillEntry;
import com.ruoyi.business.k3.service.ReceivebillEntryService;
import com.ruoyi.business.mapper.ReceivebillEntryMapper;
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
public class ReceivebillEntryServiceImpl implements ReceivebillEntryService {
    @Resource
    private ReceivebillEntryMapper receivebillEntryMapper;

    @Override
    public void ReceivebillEntryList(List<List<Object>> receivebillentrylist) {
        receivebillentrylist.forEach(en ->{
            ReceivebillEntry receivebillentry = new ReceivebillEntry();
            receivebillentry.setFentryId(en.get(0).toString()  != null ? en.get(0).toString() : null);
            //结算方式
            receivebillentry.setFsettleTypeId(en.get(1).toString() != null ? en.get(1).toString() : null);
            //折后金额
            Object setterecamountfor = en.get(2);
            if(setterecamountfor != null){
                String setterecamountfors = setterecamountfor.toString();
                BigDecimal setterecamountfores = new BigDecimal(setterecamountfors);
                receivebillentry.setFsetterecamountfor(setterecamountfores);
            }
            //现金折扣
            Object settledistamountfor = en.get(3);
            if(setterecamountfor != null){
                String tledistamountfor = settledistamountfor.toString();
                BigDecimal tledistamountfors = new BigDecimal(tledistamountfor);
                receivebillentry.setFsettledistamountfor(tledistamountfors);
            }
            //表体-应收金额
            Object rectotalamountfor = en.get(4);
            if(rectotalamountfor != null){
                String rectotalamount = rectotalamountfor.toString();
                BigDecimal rectotalamounts = new BigDecimal(rectotalamount);
                receivebillentry.setFrectotalamountfor(rectotalamounts);
            }
            //折后金额本位币
            Object settlerecamount = en.get(5);
            if(settlerecamount != null){
                String tlerecamount = settlerecamount.toString();
                BigDecimal tlerecamounts = new BigDecimal(tlerecamount);
                receivebillentry.setFsettlerecamount(tlerecamounts);
            }
            //现金折扣本位币
            Object settledistamount = en.get(6);
            if(settledistamount != null){
                String tledistamount = settledistamount.toString();
                BigDecimal tledistamounts = new BigDecimal(tledistamount);
                receivebillentry.setFsettledistamount(tledistamounts);
            }
            //表体-应收金额本位币
            Object rectotalamount = en.get(7);
            if(rectotalamount != null){
                String rectotalamounts = rectotalamount.toString();
                BigDecimal rectotalamountes = new BigDecimal(rectotalamounts);
                receivebillentry.setFrectotalamount(rectotalamountes);
            }
            //表体明细-核销状态
            receivebillentry.setFwrittenoffstatus(en.get(8).toString() != null ? en.get(8).toString() : null);
            //表体明细-已核销金额
            Object fwrittenoffmountforD = en.get(9);
            if(fwrittenoffmountforD != null){
                String writtenoffmountforD = fwrittenoffmountforD.toString();
                BigDecimal writtenoffmountfor = new BigDecimal(writtenoffmountforD);
                receivebillentry.setFwrittenoffmountforD(writtenoffmountfor);
            }
            //备注
            receivebillentry.setFcomment(en.get(10).toString() != null ? en.get(10).toString() : null);
            //对方银行账号
            receivebillentry.setFoppositebankaccount(en.get(11).toString() != null ? en.get(11).toString() : null);
            //对方账户名称
            receivebillentry.setFoppositeccountName(en.get(12).toString() != null ? en.get(12).toString() : null);
            //预收销售订单
            receivebillentry.setFreceivitem(en.get(13).toString() != null ? en.get(13).toString() : null);
            //手续费
            Object fhandlingchargefor = en.get(14);
            if(fhandlingchargefor != null){
                String handlingchargefor = fhandlingchargefor.toString();
                BigDecimal handlingchargefors = new BigDecimal(handlingchargefor);
                receivebillentry.setFhandlingchargefor(handlingchargefors);
            }
            //手续费本位币
            Object fhandlingcharge = en.get(15);
            if(fhandlingcharge != null){
                String handlingcharge = fhandlingcharge.toString();
                BigDecimal handlingcharges = new BigDecimal(handlingcharge);
                receivebillentry.setFhandlingcharge(handlingcharges);
            }
            //手续费本位币
            Object frealrecamountforD = en.get(16);
            if(frealrecamountforD != null){
                String realrecamountforD = frealrecamountforD.toString();
                BigDecimal realrecamountfor = new BigDecimal(realrecamountforD);
                receivebillentry.setFrealrecamountforD(realrecamountfor);
            }

            //表体-实收金额本位币
            Object frealrecamountd = en.get(17);
            if(frealrecamountd != null){
                String realrecamountd = frealrecamountd.toString();
                BigDecimal realrecamountds = new BigDecimal(realrecamountd);
                receivebillentry.setFrealrecamountd(realrecamountds);
            }
            //关联总金额
            Object fasstotalamountfor = en.get(18);
            if(fasstotalamountfor != null){
                String asstotalamountfor = fasstotalamountfor.toString();
                BigDecimal asstotalamountfors = new BigDecimal(asstotalamountfor);
                receivebillentry.setFasstotalamountfor(asstotalamountfors);
            }
            //预收项目类型
            receivebillentry.setFreceivitemType(en.get(19).toString() != null ? en.get(19).toString() : null);
            //预收销售订单号内码
            receivebillentry.setFsaleOrderId(en.get(20).toString() != null ? en.get(20).toString() : null);
            //我方银行账号
            receivebillentry.setFaccountId(en.get(21).toString() != null ? en.get(21).toString() : null);
            //我方账户名称
            receivebillentry.setFrecaccountName(en.get(22).toString() != null ? en.get(22).toString() : null);
            //我方开户行
            receivebillentry.setFrecbankId(en.get(23).toString() != null ? en.get(23).toString() : null);
            //长短款
            Object foverunderamountfor = en.get(24);
            if(foverunderamountfor != null){
                String overunderamountfor = foverunderamountfor.toString();
                BigDecimal overunderamountfors = new BigDecimal(overunderamountfor);
                receivebillentry.setFoverunderamountfor(overunderamountfors);
            }
            //对方开户行
            receivebillentry.setFoppositebankName(en.get(25).toString() != null ? en.get(25).toString() : null);
            //结算号
            receivebillentry.setFsettleno(en.get(26).toString() != null ? en.get(26).toString() : null);
            //勾对
            receivebillentry.setFblend(en.get(27).toString() != null ? en.get(27).toString() : null);
            //收款用途
            receivebillentry.setFpurposeId(en.get(28).toString() != null ? en.get(28).toString() : null);
            //长短款本位币
            Object foverunderamount = en.get(29);
            if(foverunderamount != null){
                String overunderamount = foverunderamount.toString();
                BigDecimal overunderamounts = new BigDecimal(overunderamount);
                receivebillentry.setFoverunderamount(overunderamounts);
            }
            //内部账号
            receivebillentry.setFinneraccountId(en.get(30).toString() != null ? en.get(30).toString() : null);
            //内部账户名称
            receivebillentry.setFinneraccountName(Objects.toString(en.get(31), null));
            //退款关联金额
            Object frefundamount = en.get(32);
            if(frefundamount != null){
                String refundamount = frefundamount.toString();
                BigDecimal refundamounts = new BigDecimal(refundamount);
                receivebillentry.setFoverunderamount(refundamounts);
            }
            //现金账号
            receivebillentry.setFcashAccount(en.get(33).toString() != null ? en.get(33).toString() : null);
            //收款金额
            Object frecamountfore = en.get(34);
            if(frecamountfore != null){
                String recamountfore = frecamountfore.toString();
                BigDecimal recamountfores = new BigDecimal(recamountfore);
                receivebillentry.setFrecamountfore(recamountfores);
            }
            //收款金额本位币
            Object frecamounte = en.get(35);
            if(frecamounte != null){
                String recamounte = frecamounte.toString();
                BigDecimal recamountes = new BigDecimal(recamounte);
                receivebillentry.setFrecamounte(recamountes);
            }
            //登账日期
            Optional.ofNullable(en.get(36))
                .map(Object::toString)
                .filter(s -> !s.trim().isEmpty())
                .ifPresent(s -> receivebillentry.setFPostDate(LocalDateTime.parse(s)));
            //是否登账
            receivebillentry.setFisPost(en.get(37).toString() != null ? en.get(37).toString() : null);
            //物料编码
            receivebillentry.setFmaterialId(en.get(38).toString() != null ? en.get(38).toString() : null);
            //物料名称
            receivebillentry.setFmaterialName(Objects.toString(en.get(39), null));
            //销售订单号
            receivebillentry.setFsaleorderNo(en.get(40).toString() != null ? en.get(40).toString() : null);
            //订单行号
            receivebillentry.setFmaterialseq(en.get(41).toString() != null ? en.get(41).toString() : null);
            //销售订单明细内码
            receivebillentry.setForderentryId(en.get(42).toString() != null ? en.get(42).toString() : null);
            //保证金转货款金额
            Object ftopaymentamountfor = en.get(43);
            if(ftopaymentamountfor != null){
                String topaymentamountfor = ftopaymentamountfor.toString();
                BigDecimal topaymentamountfors = new BigDecimal(topaymentamountfor);
                receivebillentry.setFtopaymentamountfor(topaymentamountfors);
            }
            //已核销金额本位币
            Object fwrittenoffamount = en.get(44);
            if(fwrittenoffamount != null){
                String writtenoffamount = fwrittenoffamount.toString();
                BigDecimal writtenoffamounts = new BigDecimal(writtenoffamount);
                receivebillentry.setFwrittenoffamount(writtenoffamounts);
            }
            //未核销金额
            Object fnotverificateamount = en.get(45);
            if(fnotverificateamount != null){
                String notverificateamount = fnotverificateamount.toString();
                BigDecimal notverificateamounts = new BigDecimal(notverificateamount);
                receivebillentry.setFnotverificateamount(notverificateamounts);
            }
            //计价单位
            receivebillentry.setFpriceunitId(en.get(46).toString() != null ? en.get(46).toString() : null);
            //含税单价
            Object fprice = en.get(47);
            if(fprice != null){
                String price = fprice.toString();
                BigDecimal prices = new BigDecimal(price);
                receivebillentry.setFprice(prices);
            }
            //数量
            Object fqty = en.get(48);
            if(fqty != null){
                String qty = fqty.toString();
                BigDecimal qtys = new BigDecimal(qty);
                receivebillentry.setFqty(qtys);
            }
            //费用项目
            receivebillentry.setFcostid(en.get(49).toString() != null ? en.get(49).toString() : null);
            //费用承担部门
            receivebillentry.setFcostdepartmentid(en.get(50).toString() != null ? en.get(50).toString() : null);
            //费用项目名称
            receivebillentry.setFcostName(Objects.toString(en.get(51), null));
            //关联行ID
            receivebillentry.setFlinkRowId(Objects.toString(en.get(52), null));
            //销售订单（基础资料）
            receivebillentry.setFsaleorderBase(en.get(53).toString() != null ? en.get(53).toString() : null);
            //销售订单（基础资料）
            Object frelateRefundAmount = en.get(54);
            if(frelateRefundAmount != null){
                String relateRefundAmount = frelateRefundAmount.toString();
                BigDecimal relateRefundAmounts = new BigDecimal(relateRefundAmount);
                receivebillentry.setFrelateRefundAmount(relateRefundAmounts);
            }
            //预估税率(%)
            Object fentrytaxrate = en.get(55);
            if(fentrytaxrate != null){
                String entrytaxrate = fentrytaxrate.toString();
                BigDecimal entrytaxrates = new BigDecimal(entrytaxrate);
                receivebillentry.setFentrytaxrate(entrytaxrates);
            }
            //税额
            Object ftaxamountfor = en.get(56);
            if(ftaxamountfor != null){
                String taxamountfor = ftaxamountfor.toString();
                BigDecimal taxamountfors = new BigDecimal(taxamountfor);
                receivebillentry.setFtaxamountfor(taxamountfors);
            }
            //税额本位币
            Object ftaxamount = en.get(57);
            if(ftaxamount != null){
                String taxamount = ftaxamount.toString();
                BigDecimal taxamounts = new BigDecimal(taxamount);
                receivebillentry.setFtaxamount(taxamounts);
            }
            //应收金额不含税
            Object frecnotaxamountfor = en.get(58);
            if(frecnotaxamountfor != null){
                String recnotaxamountfor = frecnotaxamountfor.toString();
                BigDecimal recnotaxamountfors = new BigDecimal(recnotaxamountfor);
                receivebillentry.setFrecnotaxamountfor(recnotaxamountfors);
            }
            //应收金额不含税本位币
            Object frecnotaxamount = en.get(59);
            if(frecnotaxamount != null){
                String recnotaxamount = frecnotaxamount.toString();
                BigDecimal recnotaxamounts = new BigDecimal(recnotaxamount);
                receivebillentry.setFrecnotaxamount(recnotaxamounts);
            }
            //已核销税额
            Object fwrittenoffaxamountfor = en.get(60);
            if(fwrittenoffaxamountfor != null){
                String writtenoffaxamountfor = fwrittenoffaxamountfor.toString();
                BigDecimal writtenoffaxamountfors = new BigDecimal(writtenoffaxamountfor);
                receivebillentry.setFwrittenoffaxamountfor(writtenoffaxamountfors);
            }
            // //已核销税额
            Object Fwrittenofftaxamount = en.get(61);
            if(Fwrittenofftaxamount != null){
                String writtenofftaxamount = Fwrittenofftaxamount.toString();
                BigDecimal writtenofftaxamounts = new BigDecimal(writtenofftaxamount);
                receivebillentry.setFwrittenofftaxamount(writtenofftaxamounts);
            }
            //出运单号
            receivebillentry.setFcydhDjt(en.get(62).toString() != null ? en.get(53).toString() : null);
            //费用项目
            receivebillentry.setFdxfyx(en.get(63).toString() != null ? en.get(53).toString() : null);
            //费用承担部门
            receivebillentry.setFfycdbm(en.get(64).toString() != null ? en.get(54).toString() : null);
            //客户
            receivebillentry.setFkh(en.get(65).toString() != null ? en.get(55).toString() : null);

            List<ReceivebillEntry> entry = receivebillEntryMapper.selectByIds(receivebillentry.getFentryId());
            if (entry != null && !entry.isEmpty()){
                entry.forEach(e ->{
                    receivebillentry.setId(e.getId());
                    receivebillEntryMapper.updateById(receivebillentry);
                });
            }else {
                receivebillEntryMapper.insert(receivebillentry);
            }

        });
    }
}
