package com.ruoyi.business.k3.service.impl;

import com.ruoyi.business.k3.domain.entity.PaymentApplicationForm;
import com.ruoyi.business.k3.service.PaymentApplicationService;
import com.ruoyi.business.mapper.PaymentApplicationMapper;
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
public class PaymentApplicationServiceImpl implements PaymentApplicationService {

    @Resource
    private PaymentApplicationMapper applicationMapper;

    @Override
    public void syncPaymentApplicationList(List<List<Object>> PaymentApplicationList) {
        PaymentApplicationList.forEach(en ->{
            PaymentApplicationForm applicationForm = new PaymentApplicationForm();
            applicationForm.setId(Long.valueOf(en.get(0).toString())  != null ? Long.valueOf(en.get(0).toString()) : null);
            //单据编号
            applicationForm.setFBillNo(en.get(1).toString() != null ? en.get(1).toString() : null);
            applicationForm.setFDocumentStatus(en.get(2).toString() != null ? en.get(2).toString() : null);
            //创建者
            applicationForm.setCreateBy(en.get(3).toString() != null ? en.get(3).toString() : null);
            //审核人
            applicationForm.setFApproverId(en.get(4).toString() != null ? en.get(4).toString() : null);
            //创建时间
            if(en.get(5).toString() != null && !en.get(5).toString().trim().isEmpty()){
                String createTime = en.get(5).toString();
                LocalDateTime createtime = LocalDateTime.parse(createTime);
                applicationForm.setCreateTime(createtime);
            }
            //结算组织
            applicationForm.setFSettleorgId(Objects.toString(en.get(6), null));
            //审核日期
            Optional.ofNullable(en.get(7))
                .map(Object::toString)
                .filter(s -> !s.trim().isEmpty())
                .ifPresent(s -> applicationForm.setFApproveDate(LocalDateTime.parse(s)));
            //采购组织
            applicationForm.setFPurchaseorgId(en.get(8).toString() != null ? en.get(8).toString() : null);
            //应付金额
            Object fPayamountforH = en.get(9);
            if(fPayamountforH != null){
                String PayamountforH = fPayamountforH.toString();
                BigDecimal payamountforH = new BigDecimal(PayamountforH);
                applicationForm.setFPayamountforH(payamountforH);
            }
            //币别
            applicationForm.setFCurrencyId(en.get(10).toString() != null ? en.get(10).toString() : null);
            //申请日期
            if(en.get(11).toString() != null && !en.get(11).toString().trim().isEmpty()){
                String fDate = en.get(11).toString();
                LocalDateTime date = LocalDateTime.parse(fDate);
                applicationForm.setFDate(date);
            }
            //更新时间
            if(en.get(12).toString() != null && !en.get(12).toString().trim().isEmpty()){
                String updateTime = en.get(12).toString();
                LocalDateTime updatetime = LocalDateTime.parse(updateTime);
                applicationForm.setUpdateTime(updatetime);
            }
            //更新者
            applicationForm.setUpdateBy(en.get(13).toString() != null ? en.get(13).toString() : null);
            //单据类型
            applicationForm.setFBilltypeId(en.get(14).toString() != null ? en.get(14).toString() : null);
            //采购员
            applicationForm.setFPurchaserId(en.get(15).toString() != null ? en.get(15).toString() : null);
            //采购部门
            applicationForm.setFPurchasedeptId(en.get(16).toString() != null ? en.get(16).toString() : null);
            //申请付款金额
            Object fApplyamountforH = en.get(17);
            if(fApplyamountforH != null){
                String applyamountforH = fApplyamountforH.toString();
                BigDecimal applyamountforh = new BigDecimal(applyamountforH);
                applicationForm.setFApplyamountforH(applyamountforh);
            }
            //会计核算体系
            applicationForm.setFAccountSystem(en.get(18).toString() != null ? en.get(18).toString() : null);
            //作废人
            applicationForm.setFCancellerId(en.get(19).toString() != null ? en.get(18).toString() : null);
            //作废状态
            applicationForm.setFCancelStatus(en.get(20).toString() != null ? en.get(20).toString() : null);
            //作废日期
            Optional.ofNullable(en.get(21))
                .map(Object::toString)
                .filter(s -> !s.trim().isEmpty())
                .ifPresent(s -> applicationForm.setFCancelDate(LocalDateTime.parse(s)));

            //往来单位类型
            applicationForm.setFContactunitType(en.get(22).toString() != null ? en.get(22).toString() : null);
            //往来单位
            applicationForm.setFContactunit(en.get(23).toString() != null ? en.get(23).toString() : null);
            //收款单位类型
            applicationForm.setFRectunitType(en.get(24).toString() != null ? en.get(24).toString() : null);
            //收款单位
            applicationForm.setFRectunit(en.get(25).toString() != null ? en.get(25).toString() : null);
            //业务类型
            applicationForm.setFBusinessType(en.get(26).toString() != null ? en.get(26).toString() : null);
            //部门
            applicationForm.setFDepartment(en.get(27).toString() != null ? en.get(27).toString() : null);
            //付款组织
            applicationForm.setFPayorgId(en.get(28).toString() != null ? en.get(28).toString() : null);
            //销售组织
            applicationForm.setFSaleorgId(en.get(29).toString() != null ? en.get(29).toString() : null);
            //销售部门
            applicationForm.setFSaledeptId(en.get(30).toString() != null ? en.get(30).toString() : null);
            //销售员
            applicationForm.setFSaleerId(en.get(31).toString() != null ? en.get(31).toString() : null);
            //本位币
            applicationForm.setFMainbookId(en.get(32).toString() != null ? en.get(32).toString() : null);

            //汇率
            Object fExchangerate = en.get(33);
            if(fExchangerate != null){
                String exchangerate = fExchangerate.toString();
                BigDecimal exchangerat = new BigDecimal(exchangerate);
                applicationForm.setFExchangerate(exchangerat);
            }
            //汇率类型
            applicationForm.setFExchangeType(en.get(34).toString() != null ? en.get(34).toString() : null);
            //来源系统
            applicationForm.setFSourceSystem(en.get(35).toString() != null ? en.get(35).toString() : null);
            //是否信贷业务
            applicationForm.setFIscredit(en.get(36).toString() != null ? en.get(36).toString() : null);
            //银行账号
            applicationForm.setFBankactId(en.get(37).toString() != null ? en.get(36).toString() : null);
            //申请组织
            applicationForm.setFApplyorgId(en.get(38).toString() != null ? en.get(38).toString() : null);
            //扫描点
            applicationForm.setFScanPoint(en.get(39).toString() != null ? en.get(39).toString() : null);
            //关闭状态
            applicationForm.setFClosesTatus(en.get(40).toString() != null ? en.get(40).toString() : null);
            //关闭人
            applicationForm.setFCloserId(en.get(41).toString() != null ? en.get(41).toString() : null);
            //关闭日期
            Optional.ofNullable(en.get(42))
                .map(Object::toString)
                .filter(s -> !s.trim().isEmpty())
                .ifPresent(s -> applicationForm.setFCloseDate(LocalDateTime.parse(s)));
            //结算币别
            applicationForm.setFSettlecur(en.get(43).toString() != null ? en.get(43).toString() : null);
            //实际申请金额
            Object fRealapplyamountFor = en.get(44);
            if(fRealapplyamountFor != null){
                String RealapplyamountFor = fRealapplyamountFor.toString();
                BigDecimal realapplyamountFor = new BigDecimal(RealapplyamountFor);
                applicationForm.setFRealapplyamountFor(realapplyamountFor);
            }
            //结算汇率
            Object fSelttlerate = en.get(45);
            if(fSelttlerate != null){
                String Selttlerate = fSelttlerate.toString();
                BigDecimal selttlerate = new BigDecimal(Selttlerate);
                applicationForm.setFSelttlerate(selttlerate);
            }
            //申请付款金额本位币
            Object fApplyamount = en.get(46);
            if(fApplyamount != null){
                String Applyamount = fApplyamount.toString();
                BigDecimal applyamount = new BigDecimal(Applyamount);
                applicationForm.setFApplyamount(applyamount);
            }
            //采购组
            applicationForm.setFPurchasergroupId(en.get(47).toString() != null ? en.get(47).toString() : null);
            //销售组
            applicationForm.setFSalegroupId(en.get(48).toString() != null ? en.get(48).toString() : null);
            //是否其他预付
            applicationForm.setFIsBorrow(en.get(49).toString() != null ? en.get(49).toString() : null);
            //是否下推携带汇率到结算汇率
            applicationForm.setFIsCarryrate(en.get(50).toString() != null ? en.get(50).toString() : null);
            //多收款人
            applicationForm.setFMorereceive(en.get(51).toString() != null ? en.get(51).toString() : null);
            //预设基础资料字段1
            applicationForm.setFPresetbase1(en.get(52).toString() != null ? en.get(52).toString() : null);
            //预设基础资料字段2
            applicationForm.setFPresetbase2(en.get(52).toString() != null ? en.get(52).toString() : null);
            //预设辅助资料字段1
            applicationForm.setFPresetassistant1(en.get(53).toString() != null ? en.get(53).toString() : null);
            //预设辅助资料字段2
            applicationForm.setFPresetassistant2(en.get(54).toString() != null ? en.get(54).toString() : null);
            //预设文本字段1
            applicationForm.setFPresettext1(en.get(55).toString() != null ? en.get(55).toString() : null);
            //预设文本字段2
            applicationForm.setFPresettext2(en.get(55).toString() != null ? en.get(55).toString() : null);
            //实报实付
            applicationForm.setFRealPay(en.get(56).toString() != null ? en.get(56).toString() : null);
            //销售购销合同号
            applicationForm.setFXsgxht(en.get(57).toString() != null ? en.get(57).toString() : null);
            //出运单号
            applicationForm.setFCydh(en.get(58).toString() != null ? en.get(58).toString() : null);
            //表头备注
            applicationForm.setFBtbz(en.get(59).toString() != null ? en.get(59).toString() : null);
            //销售部门
            applicationForm.setFOraBase(en.get(60).toString() != null ? en.get(60).toString() : null);
            //销售员
            applicationForm.setFOraBase1(en.get(61).toString() != null ?en.get(61).toString() : null);
            //包材订单
            applicationForm.setFBcdd(en.get(62).toString() != null ? en.get(62).toString() : null);
            //付款公司
            applicationForm.setFFkgs(en.get(63).toString() != null ? en.get(63).toString() : null);
            //费用类
            applicationForm.setFFyl(en.get(64).toString() != null ? en.get(64).toString() : null);
            PaymentApplicationForm licationForm = applicationMapper.selectById(applicationForm.getId());
            if (licationForm != null){
                applicationMapper.updateById(applicationForm);
            }else{
                applicationMapper.insert(applicationForm);
            }
        });
    }
}
