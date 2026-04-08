package com.ruoyi.business.k3.service.impl;

import com.ruoyi.business.k3.domain.entity.SalOutbound;
import com.ruoyi.business.k3.service.SalOutboundService;
import com.ruoyi.business.mapper.SalOutboundMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class SalOutboundServiceIpml implements SalOutboundService {
    @Autowired
    private SalOutboundMapper outboundMapper;

    //同步销售出库单主表数据
    @Override
    public void syncSalOutboundList(List<List<Object>> salOutboundList) {
        salOutboundList.forEach(en ->{
            SalOutbound outbound = new SalOutbound();
            outbound.setId(Long.valueOf(en.get(0).toString())  != null ? Long.valueOf(en.get(0).toString()) : null);
            //单据编号
            outbound.setFBillNo(en.get(1).toString() != null ? en.get(1).toString() : null);
            //单据状态
            outbound.setFDocumentStatus(en.get(2).toString() != null ? en.get(2).toString() : null);
            //销售组织 (必填项)
            outbound.setFSaleorgId(en.get(3).toString() != null ? en.get(3).toString() : null);
            //日期 (必填项)
            Optional.ofNullable(en.get(4))
                .map(Object::toString)
                .filter(s -> !s.trim().isEmpty())
                .ifPresent(s -> outbound.setFDate(LocalDateTime.parse(s)));
            //发货组织 (必填项)
            outbound.setFStockorgId(en.get(5).toString() != null ? en.get(5).toString() : null);
            //客户 (必填项)
            outbound.setFCustomerId(en.get(6).toString() != null ? en.get(6).toString() : null);
            //发货部门
            outbound.setFDeliverydeptId(en.get(7).toString() != null ? en.get(7).toString() : null);
            //销售部门
            outbound.setFSaledeptId(en.get(8).toString() != null ? en.get(8).toString() : null);
            //库存组
            outbound.setFStockergroupId(en.get(9).toString() != null ? en.get(9).toString() : null);
            //仓管员
            outbound.setFStockerId(en.get(10).toString() != null ? en.get(10).toString() : null);
            //销售组
            outbound.setFSalesgroupId(en.get(11).toString() != null ? en.get(11).toString() : null);
            //销售员
            outbound.setFSalesmanId(en.get(12).toString() != null ? en.get(12).toString() : null);
            //承运商
            outbound.setFCarrierId(en.get(13).toString() != null ? en.get(13).toString() : null);
            //运输单号
            outbound.setFCarriageNo(en.get(14).toString() != null ? en.get(14).toString() : null);
            //收货方
            outbound.setFReceiverId(en.get(15).toString() != null ? en.get(15).toString() : null);
            //结算方
            outbound.setFSettleId(en.get(16).toString() != null ? en.get(16).toString() : null);
            //付款方
            outbound.setFPayerId(en.get(17).toString() != null ? en.get(17).toString() : null);
            //创建时间
            Optional.ofNullable(en.get(18))
                .map(Object::toString)
                .filter(s -> !s.trim().isEmpty())
                .ifPresent(s -> outbound.setCreateTime(LocalDateTime.parse(s)));
            //更新者
            outbound.setUpdateBy(en.get(19).toString() != null ? en.get(19).toString() : null);
            //更新时间
            Optional.ofNullable(en.get(20))
                .map(Object::toString)
                .filter(s -> !s.trim().isEmpty())
                .ifPresent(s -> outbound.setUpdateTime(LocalDateTime.parse(s)));
            //创建者
            outbound.setCreateBy(en.get(21).toString() != null ? en.get(21).toString() : null);
            //审核人
            outbound.setFApproverId(en.get(22).toString() != null ? en.get(22).toString() : null);
            //审核日期
            Optional.ofNullable(en.get(23))
                .map(Object::toString)
                .filter(s -> !s.trim().isEmpty())
                .ifPresent(s -> outbound.setFApproveDate(LocalDateTime.parse(s)));
            //作废状态
            outbound.setFSancelStatus(en.get(24).toString() != null ? en.get(24).toString() : null);
            //作废人
            outbound.setFCancellerId(en.get(25).toString() != null ? en.get(25).toString() : null);
            //作废日期
            Optional.ofNullable(en.get(26))
                .map(Object::toString)
                .filter(s -> !s.trim().isEmpty())
                .ifPresent(s -> outbound.setFCancelDate(LocalDateTime.parse(s)));
            //单据类型
            outbound.setFBilltypeId(en.get(27).toString() != null ? en.get(27).toString() : null);
            //货主类型
            outbound.setFOwnerTypeidHead(en.get(28).toString() != null ? en.get(28).toString() : null);
            //货主
            outbound.setFOwneridHead(en.get(29).toString() != null ? en.get(29).toString() : null);
            //业务类型
            outbound.setFBussinessType(en.get(30).toString() != null ? en.get(30).toString() : null);
            //收货方地址
            outbound.setFReceiveAddress(en.get(31).toString() != null ? en.get(31).toString() : null);
            //交货地点
            outbound.setFHeadlocationId(en.get(32).toString() != null ? en.get(32).toString() : null);
            //信用检查结果
            outbound.setFCreditcheckResult(en.get(33).toString() != null ? en.get(33).toString() : null);
            //跨组织业务类型
            outbound.setFTransferbizType(en.get(34).toString() != null ? en.get(34).toString() : null);
            //对应组织
            outbound.setFCorrespondorgId(en.get(35).toString() != null ? en.get(35).toString() : null);
            //收货方联系人
            outbound.setFReceivercontactId(en.get(36).toString() != null ? en.get(36).toString() : null);
            //组织间结算跨法人标识
            outbound.setFIsinterlegalPerson(en.get(37).toString() != null ? en.get(37).toString() : null);
            //零售单日结生成
            outbound.setFGenfromposCmk(en.get(38).toString() != null ? en.get(38).toString() : null);
            //联系电话
            outbound.setFLinkPhone(en.get(39).toString() != null ? en.get(39).toString() : null);
            //收货人姓名
            outbound.setFLinkMan(en.get(40).toString() != null ? en.get(40).toString() : null);
            //销售门店
            outbound.setFBranchId(en.get(41).toString() != null ? en.get(41).toString() : null);
            //交货明细执行地址
            outbound.setFPlanrecAddress(en.get(42).toString() != null ? en.get(42).toString() : null);
            //整单服务或费用
            outbound.setFIstotalServiceOrcost(en.get(43).toString() != null ? en.get(43).toString() : null);
            //备注
            outbound.setFNote(en.get(44).toString() != null ? en.get(44).toString() : null);
            //拆单新单标识
            outbound.setFDisassemblyFlag(en.get(45).toString() != null ? en.get(45).toString() : null);
            //网店编码
            outbound.setFShopNumber(en.get(46).toString() != null ? en.get(46).toString() : null);
            //管易发货日期
            Optional.ofNullable(en.get(47))
                .map(Object::toString)
                .filter(s -> !s.trim().isEmpty())
                .ifPresent(s -> outbound.setFGyDate(LocalDateTime.parse(s)));
            //销售渠道
            outbound.setFSaleChannel(en.get(48).toString() != null ? en.get(48).toString() : null);
            //物流单号
            outbound.setFLogisticsnos(en.get(49).toString() != null ? en.get(49).toString() : null);
            //预设基础资料字段1
            outbound.setFPresetbase1(en.get(50).toString() != null ? en.get(50).toString() : null);
            //预设基础资料字段2
            outbound.setFPresetbase2(en.get(51).toString() != null ? en.get(51).toString() : null);
            //预设辅助资料字段1
            outbound.setFPresetassistant1(en.get(52).toString() != null ? en.get(52).toString() : null);
            //预设辅助资料字段2
            outbound.setFPresetassistant2(en.get(53).toString() != null ? en.get(53).toString() : null);
            //关联应收状态
            outbound.setFArStatus(en.get(54).toString() != null ? en.get(54).toString() : null);
            //出运单号
            outbound.setFCydh(en.get(55).toString() != null ? en.get(55).toString() : null);
            //客户简称
            outbound.setFOraBaseproperty(en.get(56).toString() != null ? en.get(56).toString() : null);
            //贸易术语
            outbound.setFMysy(en.get(57).toString() != null ? en.get(57).toString() : null);
            //港口名称
            outbound.setFGkmc(en.get(58).toString() != null ? en.get(58).toString() : null);
            //启运港
            outbound.setFQyg(en.get(59).toString() != null ? en.get(59).toString() : null);
            //客户全称
            outbound.setFKhqc(en.get(60).toString() != null ? en.get(60).toString() : null);
            //单据报关状态
            outbound.setFDjbgzt(en.get(61).toString() != null ? en.get(61).toString() : null);
            //清关资料
            outbound.setFOraAttachment(en.get(62).toString() != null ? en.get(62).toString() : null);
            //提单
            outbound.setFOraAttachment1(en.get(63).toString() != null ? en.get(63).toString() : null);
            //电放保函
            outbound.setFOraAttachment2(en.get(64).toString() != null ? en.get(64).toString() : null);
            //预录入单
            outbound.setFOraAttachment3(en.get(65).toString() != null ? en.get(65).toString() : null);
            //放行单
            outbound.setFOraAttachment4(en.get(66).toString() != null ? en.get(66).toString() : null);
            //客户地址
            outbound.setFKhdz(en.get(67).toString() != null ? en.get(67).toString() : null);
            //提成比例-作废
            Object fTcbl = en.get(68);
            if(fTcbl != null){
                String Tcbl = fTcbl.toString();
                BigDecimal tcbl = new BigDecimal(Tcbl);
                outbound.setFTcbl(tcbl);
            }
            //装箱单
            outbound.setFZxd(en.get(69).toString() != null ? en.get(69).toString() : null);
            //交货地址
            outbound.setFJhdz(en.get(70).toString() != null ? en.get(70).toString() : null);
            //核对状态
            outbound.setFHdzt(en.get(71).toString() != null ? en.get(71).toString() : null);
            //发货清单
            outbound.setFFhqd(en.get(72).toString() != null ? en.get(72).toString() : null);
            //退税税率
            Object fTssl = en.get(73);
            if(fTssl != null){
                String Tssl = fTssl.toString();
                BigDecimal tssl = new BigDecimal(Tssl);
                outbound.setFTssl(tssl);
            }
            //包材费用
            Object fBcfy = en.get(74);
            if(fBcfy != null){
                String Bcfy = fBcfy.toString();
                BigDecimal bcfy = new BigDecimal(Bcfy);
                outbound.setFBcfy(bcfy);
            }
            //包装费
            Object fCtyDecimal1 = en.get(75);
            if(fCtyDecimal1 != null){
                String CtyDecimal1 = fCtyDecimal1.toString();
                BigDecimal ctyDecimal1 = new BigDecimal(CtyDecimal1);
                outbound.setFCtyDecimal1(ctyDecimal1);
            }
            //售后订单
            outbound.setFShdd(en.get(76).toString() != null ? en.get(76).toString() : null);
            //锁汇汇率
            Object fShhl = en.get(77);
            if(fShhl != null){
                String Shhl = fShhl.toString();
                BigDecimal shhl = new BigDecimal(Shhl);
                outbound.setFShhl(shhl);
            }
            //锁汇汇率
            Object fShje = en.get(78);
            if(fShje != null){
                String Shje = fShje.toString();
                BigDecimal shje = new BigDecimal(Shje);
                outbound.setFShje(shje);
            }
            //解汇日期
            Optional.ofNullable(en.get(79))
                .map(Object::toString)
                .filter(s -> !s.trim().isEmpty())
                .ifPresent(s -> outbound.setFJhrq(LocalDateTime.parse(s)));
            //锁汇状态
            outbound.setFShzt(en.get(80).toString() != null ? en.get(80).toString() : null);
            //客户编码
            outbound.setFCtyBaseproperty(en.get(81).toString() != null ? en.get(81).toString() : null);
            //包装费用承担
            outbound.setFBzfycd(en.get(82).toString() != null ? en.get(82).toString() : null);
            //托盘费
            Object fTpf = en.get(83);
            if(fTpf != null){
                String Tpf = fTpf.toString();
                BigDecimal tpf = new BigDecimal(Tpf);
                outbound.setFTpf(tpf);
            }
            SalOutbound outbounds = outboundMapper.selectById(outbound.getId());
            if(outbounds != null){
                outboundMapper.updateById(outbound);
            }else {
                outboundMapper.insert(outbound);
            }
        });
    }
}
