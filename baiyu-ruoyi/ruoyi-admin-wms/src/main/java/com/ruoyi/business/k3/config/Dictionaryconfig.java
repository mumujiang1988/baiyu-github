package com.ruoyi.business.k3.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kingdee.bos.webapi.sdk.K3CloudApi;
import com.ruoyi.business.Component.K3FormProcessorFactory;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class Dictionaryconfig {

    @Autowired
    private K3FormProcessorFactory k3FormProcessorFactory;


    private static final String BASE_URL = "http://113.46.194.126/K3Cloud/";
    private static final String UPLOAD_URL = BASE_URL + "Kingdee.BOS.WebApi.ServicesStub.DynamicFormService.AttachmentUpLoad.common.kdsvc";

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // 使用延迟初始化确保只有在需要时才创建K3CloudApi实例
    private static K3CloudApi k3CloudApiClient;

    protected static K3CloudApi getK3CloudApiClient() {
        if (k3CloudApiClient == null) {
            synchronized (k3config.class) {
                if (k3CloudApiClient == null) {
                    k3CloudApiClient = new K3CloudApi();
                }
            }
        }
        return k3CloudApiClient;
    }



    /**
     * 查询员工列表
     */

    public List<List<Object>> queryUserList(String FID) {
        try {
            // 构造查询 JSON，加入 FilterString
            String jsonData = "{" +
                "\"FormId\":\"BD_Empinfo\"," +
                "\"FieldKeys\":\"FID,FName,FNumber,FPersonID,FPost\"," +
                "\"FilterString\":\"FID='" + FID + "'\"," +   // 使用 FID 过滤
                "\"OrderString\":\"\"," +
                "\"TopRowCount\":0," +
                "\"StartRow\":0," +
                "\"Limit\":5000," +
                "\"SubSystemId\":\"\"" +
                "}";

            // 执行查询
            return (List<List<Object>>) getK3CloudApiClient().executeBillQuery(jsonData);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 出错时返回空列表而不是 null
        return new ArrayList<>();
    }

    /**
     * 获取销售价目表主表列表
     * @param startRow 起始行
     * @param limit 每页数量
     */

    public List<List<Object>> querySalesPriceList(int startRow, int limit) {
        try {
            String jsonData = "{" +
                "\"FormId\":\"BD_SAL_PriceList\"," +
                "\"FieldKeys\":\"FName,FNumber,FCreatorId,FDescription,FCurrencyId,FEffectiveDate,FExpiryDate,FPriceObject,FLimitCustomer,FLimitSalesMan,FPriceType,\n" +
                "F_xsy,F_khjc,FCreateDate,FModifyDate,FModifierId,FID\"," +
                "\"OrderString\":\"\"," +
                "\"TopRowCount\":0,\"StartRow\":" + startRow + "," +
                "\"Limit\":" + limit + ",\"SubSystemId\":\"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 获取销售价目表包材明细表
     * @param startRow 起始行
     * @param limit 每页数量
     */
    public List<List<Object>> querySalesPriceItemPackageList(int startRow, int limit) {
        try {
            String jsonData = "{" +
                "\"FormId\":\"BD_SAL_PriceList\"," +
                "\"FieldKeys\":\"FID,FNumber,F_BZBM,F_BZMC,F_BZGG,F_DW,F_YL,F_BZ,F_TP,F_BCFWGYS,F_CPGYS,F_cty_BaseProperty\"," +
                "\"OrderString\":\"\"," +
                "\"TopRowCount\":0,\"StartRow\":" + startRow + "," +
                "\"Limit\":" + limit + ",\"SubSystemId\":\"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 获取销售价目表物料明细表
     * @param startRow 起始行
     * @param limit 每页数量
     */
    public List<List<Object>> querySalesPriceItemList(int startRow, int limit) {
        try {
            String jsonData = "{" +
                "\"FormId\":\"BD_SAL_PriceList\"," +
                "\"FieldKeys\":\"FID,FNumber,FMaterialId,F_khhh,FPrice,F_bzyq,F_BZMX,F_ywpm,Fzxzlxgrq,F_c,F_k,F_g,F_mz,F_cptp2,F_cptp3,F_jgdztp,\n" +
                "F_wxtm,F_wxzm,F_wxcm,F_nxzm,F_nxcm,F_CTT,F_SMST,F_CTAOT,F_CHT,F_dkczz,F_zxczz,Ftm,F_bzt,F_gys,F_jz,Fbgrq,F_ywbzfs\"," +
                "\"OrderString\":\"\"," +
                "\"TopRowCount\":0,\"StartRow\":" + startRow + "," +
                "\"Limit\":" + limit + ",\"SubSystemId\":\"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 获取销售价目表变更表
     * @param startRow 起始行
     * @param limit 每页数量
     */
    public List<List<Object>> querySalesPriceAlterationList(int startRow, int limit) {
        try {
            String jsonData = "{" +
                "\"FormId\":\"ke61b3c7fd25147cf85ecbd686276fb28\"," +
                "\"FieldKeys\":\"FID,Fkhjc,FBillNo,F_ora_Date,FCreatorId,FModifierId,FModifyDate,F_shr,F_shrq,F_ora_SourceBillNo,\n" +
                "F_ora_SourceBillType,Fcpbm,F_cty_BaseProperty,F_cty_BaseProperty1,Fjgys,Fxgys,Fbgyy,\n" +
                "Fmz,Fjz,F_c,F_k,F_g,Fzxs,Fxbzyq,Fxmz,Fxc,Fxk,Fxg,Fxzxs,FCreateDate,FDocumentStatus\"," +
                "\"OrderString\":\"\"," +
                "\"TopRowCount\":0,\"StartRow\":" + startRow + "," +
                "\"Limit\":" + limit + ",\"SubSystemId\":\"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 获取销售订单表
     * @param startRow 起始行
     * @param limit 每页数量
     */
    public List<List<Object>> querySalesOrderProcessing(int startRow, int limit) {
        try {
            String jsonData = "{" +
                "\"FormId\":\"SAL_SaleOrder\"," +
                "\"FieldKeys\":\"FBillTypeID,FID,FDocumentStatus,FMrpCloseStatus,FBillNo,FDate,FCustId,F_ora_BaseProperty,F_khhth,F_kglxr,F_cty_BaseProperty1,FSettleCurrId,F_tcbl,F_KHSD,FIsIncludedTax,F_sfbg,FSalerId,F_lrl,\n" +
                "F_jlrl,F_gj,F_PEUU_BaseProperty_qtr,F_myfs,F_zyxb,F_yhzh,F_cty_Date,F_sygs,FRecConditionId,Fbzfs,FReceiveId,FSettleId,FSettleAddress,FChargeId,F_shhl,F_shzt,F_shje,F_cty_Date1,\n" +
                "FCreatorId,FModifierId,FModifyDate,FAllDisCount,F_ysbl1,FBillTaxAmount,FLocalCurrId,FExchangeTypeId,FExchangeRate,FPLANNOTRECAMOUNT,FPLANALLRECAMOUNT,FPLANREFUNDAMOUNT,FBillAmount\"," +
                "\"OrderString\":\"\"," +
                "\"TopRowCount\":0,\"StartRow\":" + startRow + "," +
                "\"Limit\":" + limit + ",\"SubSystemId\":\"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    /**获取销售订单明细表*/
    public List<List<Object>> querySalesOrderProcessingDetailList(int startRow, int limit) {
        try {
            String jsonData = "{" +
                "\"FormId\":\"SAL_SaleOrder\"," +
                "\"FieldKeys\":\"FBillNo,Fbcykc,F_sfxp,F_jqhx,FMaterialId,FMaterialName,FPlanUnitId,FQty,FMaterialPriceUnitQty,FMaterialPriceUnitID,FPrice,FTaxPrice,FEntryTaxRate,FEntryTaxAmount,FAllAmount," +
                "FBaseCanOutQty,FDeliQty,FStockBaseCanOutQty,F_ora_Text1,F_ora_Date3,F_FZR,F_ora_Text,F_ora_Date1,F_ora_Date2,FPurJoinQty,F_ora_Base1,\n" +
                "F_ycjjfa,F_ora_Decimal,F_ora_Date4,F_ora_Decimal1,F_ora_Integer2,F_ora_Date5,F_ora_Integer3,F_BZJDTWO,F_ora_Base,F_ora_Integer,F_ora_Date,F_mz,F_jz,F_mzz,F_zxs,F_xs,F_gdtp1,F_gdtp2,F_XDGJJDTWO," +
                "F_bzfs,F_tsyq,F_ljrksl,Fysbz,F_bgrq,F_ctt,F_cptp,F_gcbz,Fhhwrk,F_kpdj,F_bzxdzt,F_gxzt,F_Cht,F_jgt,F_smsfj,F_BCHQZT,F_tsl,F_bgywpm,F_bgdw,F_ygcb,F_hsbm,F_sbys,F_BCFYNEW,F_GLBCFYNEW," +
                "Fbzcc,Fbzgctg,Frkshrq,Fbjr,Ftpr,F_cty_BaseProperty4,Fkhywms,F_cty_BaseProperty6,F_ygcbdj,F_ygcbdj1,F_ygcb1,F_PEUU_Attachment_83g,Fcgddshrq,Fyjsj,Fslshrq,F_XLCP,F_cplb,F_ckrq," +
                "F_cty_BaseProperty8\"," +
                "\"OrderString\":\"\"," +
                "\"TopRowCount\":0,\"StartRow\":" + startRow + "," +
                "\"Limit\":" + limit + ",\"SubSystemId\":\"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    /**
     * 获取销售订单成本预估表
     * @param startRow 起始行
     * @param limit 每页数量
     */
    public List<List<Object>> querySalesOrderCostEstimation(int startRow, int limit) {
        try {
            String jsonData = "{" +
                "\"FormId\":\"SAL_SaleOrder\"," +
                "\"FieldKeys\":\"FID,FBillNo,F_hyf,FBillAllAmount,FBillAllAmount_LC,F_bxf,F_gwyhfy,F_qtwbfy,F_mxcbhj,F_mxtshj,F_cbxj,F_bzf,F_dlf,F_rzf,F_kdf,F_hdf,F_lyf,\n" +
                "F_qtfy,F_mjf,F_jcf,F_fyxj,F_lrl,F_wbyk,F_jlrl,F_jlre\"," +
                "\"OrderString\":\"\"," +
                "\"TopRowCount\":0,\"StartRow\":" + startRow + "," +
                "\"Limit\":" + limit + ",\"SubSystemId\":\"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 获取收料通知单表
     * @param startRow 起始行
     * @param limit 每页数量
     */
    public List<List<Object>> queryReceiveNoticeList(int startRow, int limit) {
        try {
            String jsonData = "{" +
                "\"FormId\":\"PUR_ReceiveBill\"," +
                "\"FieldKeys\":\"FID,FBillNo,FDocumentStatus,FBillTypeID,FDate,FReceiveDeptId,FPurOrgId,FReceiverId,F_jcf,FSupplierId,F_gcbz,F_shdz,F_DATE_jz,F_djtjcf,\n" +
                "FSupplyId,FProviderContactId,FSupplyAddress,FSettleId,FChargeId,FSettleModeId,FPayConditionId,FSettleCurrId,FAllDisCount,FCreatorId,FCreateDate,\n" +
                "FModifierId,FModifyDate,FApproveDate,FApproverId,F_cty_Date\"," +
                "\"OrderString\":\"\"," +
                "\"TopRowCount\":0,\"StartRow\":" + startRow + "," +
                "\"Limit\":" + limit + ",\"SubSystemId\":\"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    /**
     * 获取收料通知单明细表表
     * @param startRow 起始行
     * @param limit 每页数量
     */
    public List<List<Object>> queryReceiveNoticeDetailList(int startRow, int limit) {
        try {
            String jsonData = "{" +
                "\"FormId\":\"PUR_ReceiveBill\"," +
                "\"FieldKeys\":\"FID,FBillNo,F_bzbz,F_hxjq,F_cplb,FMaterialId,FMaterialName,FActReceiveQty,FPreDeliveryDate,FPriceUnitId,FPriceUnitQty,FStockStatusId,FLot,FRejectQty,\n" +
                "FRejectReason,FGiveAway,FTaxPrice,FStockUnitID,FStockQty,F_WBZC,F_WBZK,F_WBZG,F_WBZTJ,F_MZ,F_JZ,F_LXZ,F_xsdd,F_lxs,F_ztj,F_mzz,FIsReconciliationing,\n" +
                "FRejectsDiscountAmount,FJoinRejectsDiscountAmount,FReconciliationBillNo,FAllReconciliationBillNo,F_KHHH,F_khjc,F_kh1,F_XSY,F_gdy1,F_bzyq,\n" +
                "F_tsyq,F_CTt,F_jgdz,F_bcjsd,F_bcjsr,F_bcjsrdh,F_Ckj,F_jgtzyy,Fddshrq,Fxsddh,Fcgddh,Frkshrq,Fzlbzjjsyq,Fbzbzjyq,Fkhywms,F_PEUU_Attachment_apv\n\"," +
                "\"OrderString\":\"\"," +
                "\"TopRowCount\":0,\"StartRow\":" + startRow + "," +
                "\"Limit\":" + limit + ",\"SubSystemId\":\"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    /**
     * 获取询价单表
     * @param startRow 起始行
     * @param limit 每页数量；；；
     */
    public List<List<Object>> queryInquirylList(int startRow, int limit) {
        try {
            String jsonData = "{" +
                "\"FormId\":\"kcb04e306b4af450693b09c9106b672a6\"," +
                "\"FieldKeys\":\"FID,FBillNo,FDocumentStatus,FCreatorId,FCreateDate,Fshr,FDATE_sh,FDate,F_ckgj,F_khly,F_gdhl,F_bjbb,F_mll,\n" +
                "F_khbm,,F_cty_BaseProperty,F_cty_BaseProperty1,F_xsy1,F_ywy,Fkhxq,F_qyd,F_fkfs,F_cty_BaseProperty4,F_btjshj,\n" +
                "F_jshjbwb,F_btcbhj,F_Email,F_mob,F_Tel,F_Fax,Fmjlr,F_cgbjrq,F_bzf,F_hdf,F_lyf,F_qtfy,F_fyxj,F_hqbjfk\n\"," +
                "\"OrderString\":\"\"," +
                "\"TopRowCount\":0,\"StartRow\":" + startRow + "," +
                "\"Limit\":" + limit + ",\"SubSystemId\":\"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 获取询价单明细表
     * @param startRow 起始行
     * @param limit 每页数量
     */
    public List<List<Object>> queryInquiryEntryList(int startRow, int limit) {
        try {
            String jsonData = "{" +
                "\"FormId\":\"kcb04e306b4af450693b09c9106b672a6\"," +
                "\"FieldKeys\":\"FID,FBillNo,F_cplb,F_jqfx,F_wldm,F_hsdj,F_ywpm,F_cty_BaseProperty2,F_Cbhj,F_cty_BaseProperty3," +
                "F_xsddgls,F_xsddxtzt,F_cty_BaseProperty5,F_khbzyq,F_xdl,F_hscgj,F_cgxpms,F_cgbzfs,F_qdl,F_jshj,F_Cptp1,F_TP,F_Cgtp1," +
                "F_Cgtp2,F_jgsxrq,F_yjjq,F_zxs,F_mz,F_jz,F_C,F_k,F_g,F_glsl,F_gctp,Fgcywms,Fgccshys,Fbzcc,Fkhhh,Fzjxdhsj,Fzjxdsl," +
                "Fmrcghsj,Fxsj,Fgys,Ftpr,F_tp3,Fbjr,Fcpys,F_YWMS\n\"," +
                "\"OrderString\":\"\"," +
                "\"TopRowCount\":0,\"StartRow\":" + startRow + "," +
                "\"Limit\":" + limit + ",\"SubSystemId\":\"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 获取采购报价单表
     * @param startRow 起始行
     * @param limit 每页数量
     */
    public List<List<Object>> queryPurchaseQuotationList(int startRow, int limit) {
        try {
            String jsonData = "{\n" +
                "\"FormId\":\"k350c7af6cd7149238edfaa94b2946fd5\",\n" +
                "\"FieldKeys\":\"FID,FBillNo,FDocumentStatus,FCreatorId,FCreateDate,Fshr,FDATE_sh,FDate,F_xsy,F_KHJC,F_kh,F_KHMC,F_Ckgj,F_khlx,\n" +
                "F_Email,F_Mob,F_Tel,F_Fax,F_qyd,F_fktj\n\",\n" +
                "\"OrderString\":\"\",\n" +
                "\"TopRowCount\":0,\"StartRow\":" + startRow + ",\n" +
                "\"Limit\":" + limit + ",\"SubSystemId\":\"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 获取采购报价单明细表
     * @param startRow 起始行
     * @param limit 每页数量
     */
    public List<List<Object>> queryPurchaseQuotationEntryList(int startRow, int limit) {
        try {
            String jsonData = "{\n" +
                "\"FormId\":\"k350c7af6cd7149238edfaa94b2946fd5\",\n" +
                "\"FieldKeys\":\"FBillNo,FID,F_ora_UnitID,F_BZ,F_TP,F_gys,F_Cptp1,F_jgyxq,F_cgjg,F_zxqdl,F_xcpms,\n" +
                "F_stgys,F_xdl,F_cpdm1,F_ywbz,F_wltp,F_CPMS1,F_BZMX,F_cty_BaseProperty4,F_ywtp1,F_ywtp2,F_zxs,F_mz,F_jz,F_C,F_k,F_g,\n" +
                "F_Cghsj,F_cty_BaseProperty,Fgctp,Fyjjhrq,Fbzcc,Fcgshys,F_Fbjry,F_ywpm,F_ywms,F_cty_Text,Fkhhh,FQTY_kc,Fzxs,Ftj,Fmzz,\n" +
                "Fkhxq,Fcgxpms,Fxgyslxr,Fxgyslxfs\n\",\n" +
                "\"OrderString\":\"\",\n" +
                "\"TopRowCount\":0,\"StartRow\":" + startRow + ",\n" +
                "\"Limit\":" + limit + ",\"SubSystemId\":\"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 获取检验单完整数据（主表+明细表字段）
     */
    public List<List<Object>> queryInspectionBillFullData(int startRow, int limit) {
        try {
            String jsonData = "{\n" +
                "\"FormId\":\"QM_InspectBill\",\n" +
                "\"FieldKeys\":\"FID,FBillNo,FDocumentStatus,FApproverId,FApproveDate,FModifierId,FCreateDate,FCreatorId,FModifyDate,FCancelDate,FCanceler," +
                "FBillTypeID,FBusinessType,FDate,FMaterialId,FMaterialName,FMaterialModel,FQCSchemeId,FUnitID,FBaseUnitId,FInspectQty,FQualifiedQty," +
                "FUnqualifiedQty,FInspectResult,FQCStatus,FSupplierId,FStockId,FLot," +
                "Fzxs,Fmz,Fjz,Fchang,Fkuan,Fgao,Fzxshu,Ftj,FDiscountQty,Fmzh,Fbzyq,Ftsyq,Fcptp,Fxsddh,Fcgddh,Fkhjc,F_cty_Picture,F_cty_Picture1," +
                "F_PEUU_Attachment_ca9,Fnhc,Fnhk,Fnhg\",\n" +
                "\"OrderString\":\"\",\n" +
                "\"TopRowCount\":0,\"StartRow\":" + startRow + ",\n" +
                "\"Limit\":" + limit + ",\"SubSystemId\":\"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 获取采购入库单主表数据
     * @param startRow 起始行
     * @param limit 每页数量
     */
    public List<List<Object>> queryPurchaseInStockList(int startRow, int limit) {
        try {
            String jsonData = "{\n" +
                "\"FormId\": \"STK_InStock\",\n" +
                "\"FieldKeys\": \"FID,FBillNo,FDocumentStatus,FBillTypeID,FDate,FSupplierId,FSupplyId,FSettleId,FChargeId,FPurchaserId," +
                "FCreatorId,FCreateDate,FModifierId,FModifyDate,FApproverId,FApproveDate,FConfirmerId,FConfirmDate," +
                "FSupplyAddress,FStockerId,FSettleCurrId,FLocalCurrId,FExchangeTypeId,FExchangeRate,FIsIncludedTax," +
                "FPayConditionId,FSettleTypeId,FPriceTimePoint,FSettleOrgId,FBillAmount,FBillTaxAmount,FBillAllAmount\",\n" +
                "\"OrderString\": \"\",\n" +
                "\"TopRowCount\": 0,\n" +
                "\"StartRow\": " + startRow + ",\n" +
                "\"Limit\": " + limit + ",\n" +
                "\"SubSystemId\": \"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 获取采购入库单明细表数据
     * @param startRow 起始行
     * @param limit 每页数量
     */
    public List<List<Object>> queryPurchaseInStockEntryList(int startRow, int limit) {
        try {
            String jsonData = "{\n" +
                "\"FormId\": \"STK_InStock\",\n" +
                "\"FieldKeys\": \"FBillNo,F_cplb,F_jqhx,FMaterialId,FStockId,FProduceDate,FNote,FWWInType,FLot," +
                "FStockLocId,FReceiveLot,FSupplierLot,FGrossWeight,FNetWeight,FMaterialName,FMaterialType,FUOM," +
                "FContractlNo,FMustQty,FRealQty,FAuxUnitQty,FExpiryDate,FStockStatusId,FRejectsDiscountAmount," +
                "F_KHHH,Fbzfyzt,F_kh,F_XSY,F_gdy1,F_bzyq,F_tsyq,F_cty_BaseProperty,F_bcjsr,F_bcjsdh,F_ckj," +
                "F_jgtzyy,Fzdrq,Fddshrq,Frkzq,Fkcsl,F_C,Fsljzrq,FTaxPrice,FCostPrice,FEntryTaxRate,FEntryTaxAmount," +
                "FDiscountRate,FPriceCoefficient,FPriceUnitQty,FTaxNetPrice,FEntryCostAmount,FAllAmount,FTaxAmount_LC," +
                "FCostAmount_LC,FAllAmount_LC,FStockFlag,FBaseUnitPrice,FUnitID,FBaseUnitID,FBaseUnitQty,FAuxUnitID," +
                "FPriceUnitID,FPOOrderNo,FReceiveStockStatus,F_cty_Text1,F_PEUU_Attachment_tzk,Ftj,F_PEUU_BaseProperty_re5," +
                "F_ssxs,F_ora_Decimal,F_ora_MulCombo,F_ora_Picture,F_xsdd,F_JYZT,F_JYDSL,F_cpbm,F_ora_BaseProperty," +
                "F_yhbl,F_yhrq,FTaxRate,FTaxAmount,FCostPercent,FTaxCostAmount,FVAT,FIsReconciliationing," +
                "FReconciliationBillNo,F_CTt,F_jgdz,F_wxzm,F_wxcm,F_CT,F_ch,F_baozt,F_nxzm,F_nxcm,F_cty_Attachment\",\n" +
                "\"OrderString\": \"\",\n" +
                "\"TopRowCount\": 0,\n" +
                "\"StartRow\": " + startRow + ",\n" +
                "\"Limit\": " + limit + ",\n" +
                "\"SubSystemId\": \"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 获取仓库仓位数据
     * @param startRow 起始行
     * @param limit 每页数量
     */
    public List<List<Object>> queryWarehousePositionList(int startRow, int limit) {
        try {
            String jsonData = "{" +
                "\"FormId\":\"BD_STOCK\"," +
                "\"FieldKeys\":\"FStockId,FNumber,FName,FFlexEntryId,FFlexEntryName\",\n" +
                "\"OrderString\":\"\"," +
                "\"TopRowCount\":0,\"StartRow\":" + startRow + "," +
                "\"Limit\":" + limit + ",\"SubSystemId\":\"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 获取发货通知单主表数据
     * @param startRow 起始行
     * @param limit 每页数量
     */
    public List<List<Object>> queryDeliveryNoticeList(int startRow, int limit) {
        try {
            String jsonData = "{" +
                "\"FormId\":\"SAL_DELIVERYNOTICE\"," +
                "\"FieldKeys\":\"FID,FBillNo,FDocumentStatus,FDate,FCustomerID,FSalesManID,FCarrierID," +
                "FReceiverID,FSettleID,FPayerID,FCreatorId,FCreateDate,FModifyDate,FApproveDate,FCancelDate," +
                "FModifierId,FBillTypeID,FApproverID,FCancellerID,FCancelStatus,FOwnerIdHead,FRECEIPTCONDITIONID," +
                "FHeadDeliveryWay,FReceiveAddress,FHeadLocId,FLocalCurrID,FExchangeTypeID,FExchangeRate," +
                "FSettleTypeID,FSettleCurrID,FBillTaxAmount,FBillAmount,FBillAllAmount,FIsIncludedTax,FAllDisCount\"," +
                "\"OrderString\":\"\"," +
                "\"TopRowCount\":0,\"StartRow\":" + startRow + "," +
                "\"Limit\":" + limit + ",\"SubSystemId\":\"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 获取发货通知单明细表数据
     * @param startRow 起始行
     * @param limit 每页数量
     */
    public List<List<Object>> queryDeliveryNoticeEntryList(int startRow, int limit) {
        try {
            String jsonData = "{" +
                "\"FormId\":\"SAL_DELIVERYNOTICE\"," +
                "\"FieldKeys\":\"FID,FBillNo,FMaterialID,FMaterialName,FMateriaModel,FMateriaType," +
                "FUnitID,FQty,FStockID,FNoteEntry,FBaseUnitID,FBaseUnitQty,FDeliveryLoc,FDeliveryLAddress," +
                "FOrderNo,FOrderSeq,FCustMatID,FLot,FIsFree,FCustMatName,FRemainOutQty,FStockStatusId," +
                "FPriceUnitQty,FPrice,FTaxPrice,FTaxCombination,FEntryTaxRate,FPriceCoefficient,FSysPrice," +
                "FLimitDownPrice,FBefDisAmt,FBefDisAllAmt,FDiscountRate,F_ora_Decimal,F_ora_Decimal1," +
                "F_c,F_k,F_g,F_tj,F_mz,F_jz,F_mzz,F_xs,F_zxs,F_cgzj,F_zjzj,F_kpdj,F_kpzj," +
                "F_zwbgpm,F_ywbgpm,F_jzz,F_jcsj,F_khhh,F_sfbg,F_ddhsdj,F_tsl,F_bgdw,F_tcblNEW," +
                "F_BCFYNEW,F_GLBCFY,F_ffyq,Fddshrq,Fxsddh,Fxsddhh,Fbzgctg,Fypd,Fykc,F_cty_BaseProperty," +
                "Frkshrq,Fckshrq,F_BZFS\"," +
                "\"OrderString\":\"\"," +
                "\"TopRowCount\":0,\"StartRow\":" + startRow + "," +
                "\"Limit\":" + limit + ",\"SubSystemId\":\"\"}";

            return getK3CloudApiClient().executeBillQuery(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
