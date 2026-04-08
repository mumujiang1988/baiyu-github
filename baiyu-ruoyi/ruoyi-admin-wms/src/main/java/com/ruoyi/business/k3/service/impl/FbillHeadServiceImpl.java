package com.ruoyi.business.k3.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.entity.PriceList;
import com.ruoyi.business.entity.PriceListEntry;
import com.ruoyi.business.k3.domain.bo.FbillHeadBo;
import com.ruoyi.business.k3.domain.entity.FFpurPatentry;
import com.ruoyi.business.k3.domain.entity.FbillHead;
import com.ruoyi.business.k3.service.FbillHeadService;
import com.ruoyi.business.mapper.FFpurPatentryMapper;
import com.ruoyi.business.mapper.FbillHeadMapper;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j
public class FbillHeadServiceImpl implements FbillHeadService {

    @Resource
    private FbillHeadMapper fbillHeadMapper;

    @Resource
    private FFpurPatentryMapper patentryMapper;

    /**
     * 同步金蝶采购调价主表信息
     * */

    @Override
    public void syncBillHeadList(List<List<Object>> billHeadList) {

        billHeadList.forEach(en ->{
            FbillHead billHead = new FbillHead();
            //id
            billHead.setFId(Long.valueOf(en.get(0) != null ? en.get(0).toString() : null));
            //编码
            billHead.setFBillNo(en.get(1) != null ? en.get(1).toString() : null);
            //单据状态
            billHead.setFDocumentStatus(en.get(2) != null ? en.get(2).toString() : null);
            //将日期String类型转成LocalDateTime
            if(en.get(3).toString() != null && !en.get(3).toString().trim().isEmpty()){
                String fDate = en.get(3).toString();
                LocalDateTime dateTime = LocalDateTime.parse(fDate);
                billHead.setFDate(dateTime);
            }
            //采购组织
            billHead.setFPurchaseOrgId(en.get(4) != null ? en.get(4).toString() : null);
            //名称 (必填项)
            billHead.setFName(en.get(5) != null ? en.get(5).toString() : null);
            //描述
            billHead.setFDescription(en.get(6) != null ? en.get(6).toString() : null);
            //调价原因
            billHead.setFPaReason(en.get(7) != null ? en.get(7).toString() : null);
            //使用组织
            billHead.setFUseOrgId(en.get(8) != null ? en.get(8).toString() : null);
            // 禁用fForbidStatus
            billHead.setFForbidStatus(en.get(9) != null ? en.get(9).toString() : null);
            //创建人
            billHead.setCreateBy(en.get(10) != null ? en.get(10).toString() : null);
            //将创建日期String类型转成LocalDateTime
            if(en.get(11).toString() != null && !en.get(11).toString().trim().isEmpty()){
                String createdate = en.get(11).toString();
                LocalDateTime dateTime = LocalDateTime.parse(createdate);
                billHead.setCreateTime(dateTime);
            }
            //最后修改人
            billHead.setUpdateBy(en.get(12) != null ? en.get(12).toString() : null);

            //将最后修改日期String类型转成LocalDateTime
            Object fapproveDateObj = en.get(15);
            if(fapproveDateObj != null){
                String modifydate = fapproveDateObj.toString();
                LocalDateTime dateTime = LocalDateTime.parse(modifydate);
                billHead.setUpdateTime(dateTime);
            }

            //审核人
            billHead.setFApproverId(en.get(14) != null ? en.get(14).toString() : null);
            //将审核日期String类型转成LocalDateTime
            Object fapprovedate = en.get(15);
            if(fapprovedate != null){
                String fapprovedates = en.get(15).toString();
                LocalDateTime dateTime = LocalDateTime.parse(fapprovedates);
                billHead.setFApproveDate(dateTime);
            }

            //禁用人
            billHead.setFforbiderId(en.get(16) != null ? en.get(16).toString() : null);
            //将禁用日期String类型转成LocalDateTime
            Object fforbidDate = en.get(17);
            if(fforbidDate != null){
                String fforbidDates = en.get(17).toString();
                LocalDateTime dateTime = LocalDateTime.parse(fforbidDates);
                billHead.setFforbidDate(dateTime);
            }
            //生效状态
            billHead.setFeffectivestatus(en.get(18) != null ? en.get(18).toString() : null);
            //生效人
            billHead.setFeffectiveuserid(en.get(19) != null ? en.get(19).toString() : null);

            //插入采购调价组表
            if(billHead != null){
                FbillHead billhead = fbillHeadMapper.selectById(billHead.getFId());
                if (billhead != null){
                    fbillHeadMapper.updateById(billHead);
                }else {
                    fbillHeadMapper.insert(billHead);
                }

            }
        });
    }

    /**
     * 同步金蝶采购调价明细表信息
     * */
    @Override
    public void syncFpurPatentry(List<List<Object>> fpurpatentryList) {
        fpurpatentryList.forEach(en ->{
            FFpurPatentry patentry = new FFpurPatentry();

            //物料编码fMaterialId
            patentry.setFMaterialId(en.get(0) != null ? en.get(0).toString() : null);
            //物料名称 FmaterialName
            patentry.setFmaterialName(en.get(1) != null ? en.get(1).toString() : null);
            //规格型号
            patentry.setFuom01(en.get(2) != null ? en.get(2).toString() : null);
            //至
            patentry.setFToQty(en.get(3) != null ? en.get(3).toString() : null);

            //调前单价
            Object beforeprice = en.get(4);
            if(beforeprice != null){
                String beforeprices = beforeprice.toString();
                BigDecimal foreprice = new BigDecimal(beforeprices);
                patentry.setFBeforePrice(foreprice);
            }

            //调后单价
            Object afterprice = en.get(5);
            if(afterprice != null){
                String afterprices = afterprice.toString();
                BigDecimal foreprice = new BigDecimal(afterprices);
                patentry.setFAfterPrice(foreprice);
            }

            //调价幅度%
            Object adjustRange = en.get(6);
            if(adjustRange != null){
                String adjustRanges = adjustRange.toString();
                BigDecimal foreprice = new BigDecimal(adjustRanges);
                patentry.setFAdjustRange(foreprice);
            }

            //价格上限
            Object upprice = en.get(7);
            if(upprice != null){
                String upprices = upprice.toString();
                BigDecimal foreprice = new BigDecimal(upprices);
                patentry.setFUpPrice(foreprice);
            }

            //价格下限
            Object downprice = en.get(8);
            if(downprice != null){
                String downprices = downprice.toString();
                BigDecimal foreprice = new BigDecimal(downprices);
                patentry.setFDownPrice(foreprice);
            }

            //生效日期
            if(en.get(9).toString() != null && !en.get(9).toString().trim().isEmpty()){
                String fDate = en.get(9).toString();
                LocalDateTime dateTime = LocalDateTime.parse(fDate);
                patentry.setFEffectiveDate(dateTime);
            }

            //失效日期
            if(en.get(10).toString() != null && !en.get(10).toString().trim().isEmpty()){
                String fDate = en.get(10).toString();
                LocalDateTime dateTime = LocalDateTime.parse(fDate);
                patentry.setFExpiryDate(dateTime);
            }
            //价目表
            patentry.setFPriceListId(en.get(11) != null ? en.get(11).toString() : null);

            //供应商
            patentry.setFSupplierId(en.get(12) != null ? en.get(12).toString() : null);

            //币别
            patentry.setFCurrencyId(en.get(13) != null ? en.get(13).toString() : null);

            //辅助属性
            patentry.setFAuxpropId(en.get(14) != null ? en.get(14).toString() : null);

            // 物料类别
            patentry.setFmaterialtYpeId(en.get(15) != null ? en.get(15).toString() : null);

            //价目表下推
            patentry.setFIsPriceListPush(en.get(16) != null ? en.get(16).toString() : null);

            //计价单位
            patentry.setFUnitId(en.get(17) != null ? en.get(17).toString() : null);

            //调价唯一性标示
            patentry.setFPatidEntity(en.get(18) != null ? en.get(18).toString() : null);

            //调价对象
            patentry.setFPriceListObject(en.get(19) != null ? en.get(19).toString() : null);

            //调价类型
            patentry.setFAdjustType(en.get(20) != null ? en.get(20).toString() : null);

            // 调前税率
            patentry.setFBeforeTaxRate(en.get(21) != null ? en.get(21).toString() : null);

            //调后税率
            Object aftertaxrate = en.get(22);
            if(aftertaxrate != null){
                String aftertaxrates = aftertaxrate.toString();
                BigDecimal aftertaxrat= new BigDecimal(aftertaxrates);
                patentry.setFAfterTaxRate(aftertaxrat);
            }

            // 调前含税单价
            Object beforetaxprice = en.get(23);
            if(beforetaxprice != null){
                String beforetaxprices = beforetaxprice.toString();
                BigDecimal beforetaxpric= new BigDecimal(beforetaxprices);
                patentry.setFBeforeTaxPrice(beforetaxpric);
            }

            //调后含税单价
            Object aftertaxprice = en.get(24);
            if(aftertaxprice != null){
                String aftertaxprices = aftertaxprice.toString();
                BigDecimal aftertaxpric= new BigDecimal(aftertaxprices);
                patentry.setFAfterTaxPrice(aftertaxpric);
            }

            //含税
            patentry.setFIsIncludedTax(en.get(25) != null ? en.get(25).toString() : null);

            //需求组织
            patentry.setFProcessOrgId(en.get(26) != null ? en.get(26).toString() : null);

            //作业
            patentry.setFprocessId(en.get(27) != null ? en.get(27).toString() : null);

            //价目表分录内码
            patentry.setFSrcEntryId(en.get(28) != null ? en.get(28).toString() : null);

            //备注
            patentry.setFNote(en.get(29) != null ? en.get(29).toString() : null);

            //调前价格系数
            patentry.setFBeforePriceCoefficient(en.get(30) != null ? en.get(30).toString() : null);

            //调后价格系数
            patentry.setFAfterPriceCoefficient(en.get(31) != null ? en.get(31).toString() : null);

            //从
            patentry.setFfromqty(en.get(32) != null ? en.get(32).toString() : null);

            //自定义基础资料1
            patentry.setFDefBaseDataO(en.get(33) != null ? en.get(33).toString() : null);
            //自定义基础资料2
            patentry.setFDefBaseDataT(en.get(34) != null ? en.get(34).toString() : null);
            //自定义辅助资料1
            patentry.setFDefAssistantO(en.get(35) != null ? en.get(35).toString() : null);

            //自定义辅助资料2
            patentry.setFDefAssistantT(en.get(36) != null ? en.get(36).toString() : null);

            // 自定义文本1
            patentry.setFDefTextO(en.get(37) != null ? en.get(37).toString() : null);
            //自定义文本2
            patentry.setFDefTextT(en.get(38) != null ? en.get(38).toString() : null);

            //自定义价格1

            Object defaulttrice = en.get(39);
            if(defaulttrice != null){
                String aftertaxprices = defaulttrice.toString();
                BigDecimal aftertax= new BigDecimal(aftertaxprices);
                patentry.setFDefaultPriceO(aftertax);
            }

            //自定义价格2
            Object Pricet = en.get(40);
            if(Pricet != null){
                String Pricets = Pricet.toString();
                BigDecimal Pricetes = new BigDecimal(Pricets);
                patentry.setFDefaultPriceT(Pricetes);
            }

            //价外税
            patentry.setFIsPriceExcludeTax(en.get(41) != null ? en.get(41).toString() : null);

            //物料分组编码
            patentry.setFMaterialGroupId(en.get(42) != null ? en.get(42).toString() : null);

            //物料分组名称
            patentry.setFmaterialGroupName(en.get(43) != null ? en.get(43).toString() : null);

            //含税差价
            Object hscj = en.get(44);
            if(hscj != null){
                String hscjs = hscj.toString();
                BigDecimal hscjes = new BigDecimal(hscjs);
                patentry.setFHscj(hscjes);
            }
            //实体主键
            patentry.setFEntryId(en.get(46) != null ? en.get(46).toString() : null);

            patentry.setFNumber(en.get(47) != null ? en.get(47).toString() : null);


            patentryMapper.insert(patentry);

            /*if (patentry != null){
                patentryMapper.selectByCondition();
            }*/


        });

    }


    /**
     * 根据条件分页查询采购调价列表
     *
     * @param pageQuery 查询条件
     * @return 采购调价集合信息
     */
    @Override
    public TableDataInfo<FbillHead> list(FbillHeadBo billHead, PageQuery pageQuery) {
        // 执行分页查询
        Page<FbillHead> pageResult = fbillHeadMapper.selectByCondition(pageQuery.build(),this.buildQueryWrapper(billHead));
        pageResult.getRecords().forEach(en ->{
            String fNumber = en.getFBillNo();
            en.setPatentries(patentryMapper.selectByCondition(fNumber));
        });
        return TableDataInfo.build(pageResult);
    }

    /**
     * 根据ID查采购调价询表（包含明细）
     * @param id 采购调价表ID
     * @return 采购调价表信息
     */
    @Override
    public FbillHead getById(Long id) {
        // 查询主表
        FbillHead head = fbillHeadMapper.selectByIds(id);
        if (head.getFBillNo() != null && !head.getFBillNo().trim().isEmpty()) {
            // 查询明细
            List<FFpurPatentry> entries = patentryMapper.selectByCondition(head.getFBillNo());
            head.setPatentries(entries);
        }
        return head;
    }

    /**
     * 根据ID删除采购调价表（级联删除明细）
     * @param id 价目表ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        //1.查询采购调价表主表
        FbillHead head = fbillHeadMapper.selectById(id);
        if (head != null){
            List<FFpurPatentry> patentry = patentryMapper.selectByCondition(head.getFBillNo());
            if (patentry != null && !patentry.isEmpty()){
                patentry.forEach(ens -> {
                    //删除采购调价明细表
                    patentryMapper.deleteById(ens.getId());
                });
            }
        }
        // 2.删除采购调价主表
        return fbillHeadMapper.deleteById(id) > 0;

    }

    /**
     * @param billHead 查询条件对象
     * @return 采购调价列表
     */
    private Wrapper<FbillHead> buildQueryWrapper(FbillHeadBo billHead) {
        QueryWrapper<FbillHead> wrapper = Wrappers.query();
        //查询条件
        wrapper
            .eq(ObjectUtil.isNotNull(billHead.getId()),"f.id", billHead.getId())
            .like(StringUtils.isNotBlank(billHead.getFBillNo()), "f.f_bill_no", billHead.getFBillNo())
            .like(StringUtils.isNotBlank(billHead.getFName()), "f.f_name", billHead.getFName())
            .like(StringUtils.isNotBlank(billHead.getFSupplierId()), "s.name", billHead.getFSupplierId())
            .like(StringUtils.isNotBlank(billHead.getFmaterialName()), "fp.fmaterial_name", billHead.getFmaterialName())
            .like(StringUtils.isNotBlank(billHead.getFSupplierId()), "s.name", billHead.getFSupplierId())
            .like(StringUtils.isNotBlank(billHead.getFMaterialId()), "b.number", billHead.getFMaterialId())
            .eq(ObjectUtil.isNotNull(billHead.getFDocumentStatus()),"f.f_document_status", billHead.getFDocumentStatus())
            .orderByDesc("f.create_time");
        return wrapper;
    }




}
