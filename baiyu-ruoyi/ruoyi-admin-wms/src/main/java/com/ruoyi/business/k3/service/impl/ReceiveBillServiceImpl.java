package com.ruoyi.business.k3.service.impl;

import com.ruoyi.business.k3.domain.entity.ReceiveBill;
import com.ruoyi.business.k3.service.ReceiveBillService;
import com.ruoyi.business.mapper.ReceiveBillMapper;
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
public class ReceiveBillServiceImpl implements ReceiveBillService {

    @Resource
    private ReceiveBillMapper receivebillmapper;

    /**
     * 收款单信息表
     * */
    @Override
    public void syncReceiveBillList(List<List<Object>> receivebillList) {
        receivebillList.forEach(en ->{
            ReceiveBill receivebill = new ReceiveBill();
            //实体主键
            receivebill.setId(Long.valueOf(en.get(0).toString())  != null ? Long.valueOf(en.get(0).toString()) : null);
            //单据编号
            receivebill.setFbillNo(en.get(1).toString() != null ? en.get(1).toString() : null);
            //单据状态
            receivebill.setFdocumentStatus(en.get(2).toString() != null ? en.get(2).toString() : null);
            //创建人
            receivebill.setCreateBy(en.get(3).toString() != null ? en.get(3).toString() : null);
            //审核人
            receivebill.setFapproverId(en.get(4).toString() != null ? en.get(4).toString() : null);
            //创建日期String类型转成LocalDateTime
            if(en.get(5).toString() != null && !en.get(5).toString().trim().isEmpty()){
                String createTime = en.get(5).toString();
                LocalDateTime dateTime = LocalDateTime.parse(createTime);
                receivebill.setCreateTime(dateTime);
            }
            //结算组织
            receivebill.setFsettleorgId(en.get(6).toString() != null ? en.get(6).toString() : null);
            //审核日期
            if(en.get(7).toString() != null && !en.get(7).toString().trim().isEmpty()){
                String faproveDate = en.get(7).toString();
                LocalDateTime faprovedate = LocalDateTime.parse(faproveDate);
                receivebill.setFaproveDate(faprovedate);
            }
            //销售组织
            receivebill.setFsaleorgId(en.get(8).toString() != null ? en.get(8).toString() : null);
            //汇率类型
            receivebill.setFexchangeType(en.get(9).toString() != null ? en.get(9).toString() : null);
            //表头-应收金额
            Object freceiveamountforH = en.get(10);
            if(freceiveamountforH != null){
                String freceiveamountfor = freceiveamountforH.toString();
                BigDecimal freceiveamountfors = new BigDecimal(freceiveamountfor);
                receivebill.setFreceiveamountforH(freceiveamountfors);
            }
            //本位币
            receivebill.setFmainbookcurId(en.get(11).toString() != null ? en.get(11).toString() : null);
            //表头-应收金额本位币
            Object freceiveamountH = en.get(12);
            if(freceiveamountH != null){
                String freceiveamounth = freceiveamountH.toString();
                BigDecimal freceiveamount = new BigDecimal(freceiveamounth);
                receivebill.setFreceiveamountH(freceiveamount);
            }
            //币别
            receivebill.setFcurrencyId(en.get(13).toString() != null ? en.get(13).toString() : null);
           //业务日期
            if(en.get(14).toString() != null && !en.get(14).toString().trim().isEmpty()){
                String fdate = en.get(14).toString();
                LocalDateTime fdates = LocalDateTime.parse(fdate);
                receivebill.setFdate(fdates);
            }
            //更新时间
            if(en.get(15).toString() != null && !en.get(15).toString().trim().isEmpty()){
                String fdate = en.get(15).toString();
                LocalDateTime fdates = LocalDateTime.parse(fdate);
                receivebill.setUpdateTime(fdates);
            }
            //更新者
            receivebill.setUpdateBy(en.get(16).toString() != null ? en.get(16).toString() : null);
            //汇率
            Object fexchangerate = en.get(17);
            if(fexchangerate != null){
                String fexchangerates = fexchangerate.toString();
                BigDecimal fexchangeratess = new BigDecimal(fexchangerates);
                receivebill.setFexchangerate(fexchangeratess);
            }
            //核销状态
            receivebill.setFwrittenoffStatus(en.get(18).toString() != null ? en.get(18).toString() : null);
            //销售员
            receivebill.setFsaleerId(en.get(19).toString() != null ? en.get(19).toString() : null);
            //销售组
            receivebill.setFsaleGroupId(en.get(20).toString() != null ? en.get(20).toString() : null);
            //销售部门
            receivebill.setFsaleDeptId(en.get(21).toString() != null ? en.get(21).toString() : null);
            //单据类型
            receivebill.setFbillTypeId(en.get(22).toString() != null ? en.get(22).toString() : null);
            //表头-实收金额
            Object frealrecamountfor = en.get(23);
            if(frealrecamountfor != null){
                String frealrecamountfore = frealrecamountfor.toString();
                BigDecimal frealrecamountfores = new BigDecimal(frealrecamountfore);
                receivebill.setFrealrecamountfor(frealrecamountfores);
            }
            //表头-实收金额本位币
            Object frealrecamountH = en.get(24);
            if(frealrecamountH != null){
                String frealrecamounth = frealrecamountH.toString();
                BigDecimal frealrecamount = new BigDecimal(frealrecamounth);
                receivebill.setFrealrecamountH(frealrecamount);
            }
            //会计核算体系
            receivebill.setFaccountsystem(en.get(25).toString() != null ? en.get(25).toString() : null);
            //作废日期
            Optional.ofNullable(en.get(26))
                .map(Object::toString)
                .filter(s -> !s.trim().isEmpty())
                .ifPresent(s -> receivebill.setFcancelDate(LocalDateTime.parse(s)));

            //作废状态
            receivebill.setFcancelStatus(en.get(27).toString() != null ? en.get(27).toString() : null);
            //作废人
            receivebill.setFcancellerId(en.get(28).toString() != null ? en.get(28).toString() : null);
            //往来单位类型
            receivebill.setFcontactunitType(en.get(29).toString() != null ? en.get(29).toString() : null);
            //往来单位
            receivebill.setFcontactunit(en.get(30).toString() != null ? en.get(30).toString() : null);
            //付款单位类型
            receivebill.setFcontactunitType(en.get(31).toString() != null ? en.get(31).toString() : null);
            //往来单位
            receivebill.setFcontactunit(en.get(32).toString() != null ? en.get(32).toString() : null);
            //付款单位类型
            receivebill.setFbusinessType(en.get(33).toString() != null ? en.get(33).toString() : null);
            //信用检查结果
            receivebill.setFcreditCheckResult(en.get(34).toString() != null ? en.get(34).toString() : null);
            //是否期初单据
            receivebill.setFisinit(en.get(35).toString() != null ? en.get(35).toString() : null);
            //部门
            receivebill.setFdepartment(en.get(36).toString() != null ? en.get(36).toString() : null);
            //收款组织
            receivebill.setFpayorgId(en.get(37).toString() != null ? en.get(37).toString() : null);
            //是否相同组织
            receivebill.setFisSameOrg(en.get(38).toString() != null ? en.get(38).toString() : null);
            //来源系统
            receivebill.setFsourceSystem(en.get(39).toString() != null ? en.get(39).toString() : null);
            //现销
            receivebill.setFcashsale(en.get(40).toString() != null ? en.get(40).toString() : null);
            //结算币别
            receivebill.setFsettcur(en.get(41).toString() != null ? en.get(41).toString() : null);
            //结算汇率
            Object fsettlerate = en.get(42);
            if(fsettlerate != null){
                String fsettlerates = fsettlerate.toString();
                BigDecimal settlerate = new BigDecimal(fsettlerates);
                receivebill.setFsettlerate(settlerate);
            }
            //收款金额
            Object frecamountfor = en.get(43);
            if(frecamountfor != null){
                String recamountfor = frecamountfor.toString();
                BigDecimal recamountfors = new BigDecimal(recamountfor);
                receivebill.setFrecamountfor(recamountfors);
            }
            //表头-收款金额本位币
            Object frecamount = en.get(44);
            if(frecamount != null){
                String recamount = frecamount.toString();
                BigDecimal recamounts = new BigDecimal(recamount);
                receivebill.setFrecamount(recamounts);
            }
            //B2C业务
            receivebill.setFIsb2c(en.get(45).toString() != null ? en.get(45).toString() : null);
            //流水号/对账码
            receivebill.setFwbsettleno(en.get(46).toString() != null ? en.get(46).toString() : null);
            //是否转销
            receivebill.setFisWriteOff(en.get(47).toString() != null ? en.get(47).toString() : null);
            //核销方式
            receivebill.setFmatchMethodId(en.get(48).toString() != null ? en.get(48).toString() : null);
            //扫描点
            receivebill.setFscanPoint(en.get(49).toString() != null ? en.get(49).toString() : null);
            //金蝶支付流水号
            receivebill.setFkdpayOn(en.get(50).toString() != null ? en.get(50).toString() : null);
            //备注
            receivebill.setFremark(en.get(51).toString() != null ? en.get(51).toString() : null);
            //第三方单据编号
            receivebill.setFthirdbillno(en.get(52).toString() != null ? en.get(52).toString() : null);
            //结算本位币
            receivebill.setFsettlemainbookId(en.get(53).toString() != null ? en.get(53).toString() : null);
            // 结算汇率类型
            receivebill.setFsettleexchangType(en.get(54).toString() != null ? en.get(54).toString() : null);
            //转出往来单位
            receivebill.setFoutcontactId(en.get(55).toString() != null ? en.get(55).toString() : null);
            //转出往来单位类型
            receivebill.setFoutcontactType(en.get(56).toString() != null ? en.get(56).toString() : null);
            //管易财务流水内码
            receivebill.setFgyaccountwaterId(en.get(57).toString() != null ? en.get(57).toString() : null);
            //是否下推携带汇率到结算汇率
            receivebill.setFiscarryrate(en.get(58).toString() != null ? en.get(58).toString() : null);
            //预设基础资料字段1
            receivebill.setFpresetbase1(en.get(59).toString() != null ? en.get(59).toString() : null);
            //预设基础资料字段2
            receivebill.setFpresetbase2(en.get(60).toString() != null ? en.get(58).toString() : null);
            //预设辅助资料字段1
            receivebill.setFpresetssistant1(en.get(61).toString() != null ? en.get(61).toString() : null);
            //预设文本字段2
            receivebill.setFpresetssistant2(en.get(62).toString() != null ? en.get(62).toString() : null);
            //预设文本字段1
            receivebill.setFpresettext1(en.get(63).toString() != null ? en.get(63).toString() : null);
            //预设文本字段2
            receivebill.setFpresettext1(en.get(64).toString() != null ? en.get(64).toString() : null);
            //来源销售订单下推
            receivebill.setFisfromsalorder(en.get(65).toString() != null ? en.get(65).toString() : null);
            //是否同一核算组织
            receivebill.setFvirIsAameAcctOrg(en.get(66).toString() != null ? en.get(66).toString() : null);
            //来源票据号
            receivebill.setFsourceBillNumber(en.get(67).toString() != null ? en.get(67).toString() : null);
            //出运单号
            receivebill.setFcydh(en.get(68).toString() != null ? en.get(68).toString() : null);
            //银行水单单号
            receivebill.setFyhsddh(en.get(69).toString() != null ? en.get(69).toString() : null);
            //原始汇率
            Object fyshl = en.get(70);
            if(fyshl != null){
                String yshl = fyshl.toString();
                BigDecimal yshls = new BigDecimal(yshl);
                receivebill.setFrecamount(yshls);
            }
            //客户代码
            receivebill.setFkhdm(en.get(71).toString() != null ? en.get(71).toString() : null);
            //客户简称
            receivebill.setFctyBaseProperty(Objects.toString(en.get(72), null));
            //水单
            receivebill.setFsd(en.get(73).toString() != null ? en.get(73).toString() : null);
            ReceiveBill bill = receivebillmapper.selectById(receivebill.getId());
            if (bill != null){
                receivebillmapper.updateById(receivebill);
            }else {
                receivebillmapper.insert(receivebill);
            }

        });
    }


}
