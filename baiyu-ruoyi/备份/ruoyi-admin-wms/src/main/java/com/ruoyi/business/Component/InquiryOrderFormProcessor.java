package com.ruoyi.business.Component;

import com.ruoyi.business.entity.InquiryOrder;
import com.ruoyi.business.entity.InquiryOrderEntry;
import com.ruoyi.business.k3.config.AbstractK3FormProcessor;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 询价单表单处理器
 * 负责处理询价单表单的构建和提交到金蝶
 */
@Component  // 关键：添加@Component注解，让Spring管理
public class InquiryOrderFormProcessor extends AbstractK3FormProcessor<InquiryOrder> {

    @Override
    protected String getFormId() {
        return "SAL_Inquiry"; // 询价单表单ID，根据实际情况调整
    }

    @Override
    protected List<String> getFileFieldNames() {
        // 询价单通常可能有附件字段，这里先预留，根据实际金蝶字段调整
        return new ArrayList<>();
    }

    @Override
    protected String getDocumentNumber(InquiryOrder formData) {
        return formData.getFbillno() != null ? formData.getFbillno() : "";
    }

    @Override
    protected Map<String, Object> buildModel(InquiryOrder inquiryOrder) {
        System.out.println("开始构建询价单模型数据，单据编号: " + inquiryOrder.getFbillno());

        Map<String, Object> model = new HashMap<>();

        if (inquiryOrder != null) {
            // ================= 基础字段 =================
            model.put("FID", 0); // 新增询价单固定为0
            model.put("FBillNo", inquiryOrder.getFbillno() != null ? inquiryOrder.getFbillno() : "");

            // 创建人信息
            Map<String, Object> creatorMap = new HashMap<>();
            creatorMap.put("FUserID", ""); // 默认空值
            model.put("FCreatorId", creatorMap);

            // 创建日期
            model.put("FCreateDate", "1900-01-01");


            // 日期
            model.put("FDate", inquiryOrder.getFdate() != null ? inquiryOrder.getFdate().toString() : "1900-01-01");

            // ================= 客户信息字段 =================
            Map<String, Object> customerMap = new HashMap<>();
            if (inquiryOrder.getFKhbm() != null) {
                customerMap.put("FNUMBER", inquiryOrder.getFKhbm()); // 注意：金蝶要求是FNUMBER而不是FNumber
            } else {
                customerMap.put("FNUMBER", "");
            }
            model.put("F_khbm", customerMap); // 注意：金蝶字段名是F_khbm而不是F_KHBM

            // ================= 业务字段 =================

            // 客户名称 - 必须存在，即使为空
            model.put("F_cty_BaseProperty", inquiryOrder.getFCtyBaseproperty() != null ? inquiryOrder.getFCtyBaseproperty() : "");
            // 客户简称 - 必须存在，即使为空
            model.put("F_cty_BaseProperty1", inquiryOrder.getFCtyBaseproperty1() != null ? inquiryOrder.getFCtyBaseproperty1() : "");
            // 客户需求 - 必须存在，即使为空
            model.put("Fkhxq", inquiryOrder.getFkhxq() != null ? inquiryOrder.getFkhxq() : "");
            // 出口国家 - 必须存在，即使为空
            Map<String, Object> exportCountryMap = new HashMap<>();
            exportCountryMap.put("FNUMBER", inquiryOrder.getFCkgj() != null ? inquiryOrder.getFCkgj() : "");
            model.put("F_ckgj", exportCountryMap);
            // 客户来源 - 必须存在，即使为空
            Map<String, Object> customerSourceMap = new HashMap<>();
            customerSourceMap.put("FNUMBER", inquiryOrder.getFKhly() != null ? inquiryOrder.getFKhly() : "");
            model.put("F_khly", customerSourceMap);
            // 固定汇率 - 必须存在，即使为空
            Map<String, Object> fixedRateMap = new HashMap<>();
            fixedRateMap.put("FNUMBER", ""); // 固定汇率应该是引用字段
            model.put("F_gdhl", fixedRateMap);
            // 币别 - 必须存在，即使为空
            Map<String, Object> currencyMap = new HashMap<>();
            currencyMap.put("FNUMBER", inquiryOrder.getFBjbb() != null ? inquiryOrder.getFBjbb() : "");
            model.put("F_bjbb", currencyMap);
            // 毛净利率% - 必须存在，即使为空
            model.put("F_mll", inquiryOrder.getFMll() != null ? inquiryOrder.getFMll().toString() : "0");
            // 销售员 - 必须存在，即使为空
            Map<String, Object> salesPersonMap = new HashMap<>();
            salesPersonMap.put("FSTAFFNUMBER", inquiryOrder.getFXsy1() != null ? inquiryOrder.getFXsy1() : "");
            model.put("F_xsy1", salesPersonMap);
            // 业务员 - 必须存在，即使为空
            model.put("F_ywy", inquiryOrder.getFYwy() != null ? inquiryOrder.getFYwy() : "");
            // Shipping to - 必须存在，即使为空
            Map<String, Object> shipToMap = new HashMap<>();
            shipToMap.put("FNumber", inquiryOrder.getFQyd() != null ? inquiryOrder.getFQyd() : "");
            model.put("F_qyd", shipToMap);
            // Payment terms - 必须存在，即使为空
            model.put("F_fkfs", inquiryOrder.getFFkfs() != null ? inquiryOrder.getFFkfs() : "");
            // 客户分组 - 必须存在，即使为空
            model.put("F_cty_BaseProperty4", inquiryOrder.getFCtyBaseproperty4() != null ? inquiryOrder.getFCtyBaseproperty4() : "");
            // 表头价税合计 - 必须存在，即使为空
            model.put("F_btjshj", inquiryOrder.getFBtjshj() != null ? inquiryOrder.getFBtjshj().toString() : "0");
            // 价税合计本位币 - 必须存在，即使为空
            model.put("F_jshjbwb", inquiryOrder.getFJshjbwb() != null ? inquiryOrder.getFJshjbwb().toString() : "0");
            // 表头成本合计 - 必须存在，即使为空
            model.put("F_btcbhj", inquiryOrder.getFBtcbhj() != null ? inquiryOrder.getFBtcbhj().toString() : "0");
            // 表头退税合计 - 必须存在，即使为空
            model.put("Fbttshj", "0");

            // ================= 联系信息 =================
            // Email - 必须存在，即使为空
            model.put("F_Email", inquiryOrder.getFEmail() != null ? inquiryOrder.getFEmail() : "");
            // 手机 - 必须存在，即使为空
            model.put("F_mob", inquiryOrder.getFMob() != null ? inquiryOrder.getFMob() : "");
            // 电话 - 必须存在，即使为空
            model.put("F_Tel", inquiryOrder.getFTel() != null ? inquiryOrder.getFTel() : "");
            // 传真 - 必须存在，即使为空
            model.put("F_Fax", inquiryOrder.getFFax() != null ? inquiryOrder.getFFax() : "");

            // ================= 财务信息 =================
            // 毛净利润 - 必须存在，即使为空
            model.put("Fmjlr", inquiryOrder.getFmjlr() != null ? inquiryOrder.getFmjlr().toString() : "0");
            // 采购报价日期 - 必须存在，即使为空
            if (inquiryOrder.getFCgbjrq() != null) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                model.put("F_cgbjrq", sdf.format(java.sql.Date.valueOf(inquiryOrder.getFCgbjrq())));
            } else {
                model.put("F_cgbjrq", "1900-01-01");
            }

            // ================= 费用信息 =================
            // 包装费 - 必须存在，即使为空
            model.put("F_bzf", inquiryOrder.getFBzf() != null ? inquiryOrder.getFBzf().toString() : "0");
            // 货代费 - 必须存在，即使为空
            model.put("F_hdf", inquiryOrder.getFHdf() != null ? inquiryOrder.getFHdf().toString() : "0");
            // 陆运费 - 必须存在，即使为空
            model.put("F_lyf", inquiryOrder.getFLyf() != null ? inquiryOrder.getFLyf().toString() : "0");
            // 其他费用 - 必须存在，即使为空
            model.put("F_qtfy", inquiryOrder.getFQtfy() != null ? inquiryOrder.getFQtfy().toString() : "0");
            // 费用小计 - 必须存在，即使为空
            model.put("F_fyxj", inquiryOrder.getFFyxj() != null ? inquiryOrder.getFFyxj().toString() : "0");

            // ================= 其他字段 =================
            // 后勤报价反馈 - 必须存在，即使为空
            model.put("F_hqbjfk", inquiryOrder.getFHqbjfk() != null ? inquiryOrder.getFHqbjfk() : "");

            // ================= 明细行数据 =================
            // 构建明细行数据
            List<Map<String, Object>> entryList = buildEntryList(inquiryOrder.getEntries());
            model.put("FEntity", entryList);
        }

        System.out.println("询价单模型构建完成: " + model);
        return model;
    }

    /**
     * 构建询价单明细行数据
     */
    private List<Map<String, Object>> buildEntryList(List<InquiryOrderEntry> entries) {
        List<Map<String, Object>> entryList = new ArrayList<>();

        if (entries != null && !entries.isEmpty()) {
            for (int i = 0; i < entries.size(); i++) {
                InquiryOrderEntry entry = entries.get(i);
                Map<String, Object> entryMap = new HashMap<>();

                // ================= 基础字段 =================
                // 行号 - 必须存在
                entryMap.put("FEntryID", i + 1);

                // 产品代码 - 必须存在，即使为空
                Map<String, Object> productCodeMap = new HashMap<>();
                productCodeMap.put("FNUMBER", entry.getFWldm() != null ? entry.getFWldm() : "");
                entryMap.put("F_wldm", productCodeMap);

                // 产品代码(重复字段) - 必须存在，即使为空
                Map<String, Object> productCodeMap2 = new HashMap<>();
                productCodeMap2.put("FNUMBER", entry.getFWldm() != null ? entry.getFWldm() : "");
                entryMap.put("F_cpdm", productCodeMap2);

                // 产品描述 - 必须存在，即使为空
                entryMap.put("F_cpms", entry.getFCtyBaseproperty2() != null ? entry.getFCtyBaseproperty2() : "");
                // 英文品名 - 必须存在，即使为空
                entryMap.put("F_ywpm", entry.getFYwpm() != null ? entry.getFYwpm() : "");
                // 客户包装要求 - 必须存在，即使为空
                entryMap.put("F_khbzyq", entry.getFKhbzyq() != null ? entry.getFKhbzyq() : "");

                // ================= 数量和价格字段 =================
                // 下单量 - 必须存在，即使为空
                entryMap.put("F_xdl", entry.getFXdl() != null ? entry.getFXdl().toString() : "0");

                // 计价单位 - 必须存在，即使为空
                Map<String, Object> unitMap = new HashMap<>();
                unitMap.put("FNumber", ""); // 默认空值
                entryMap.put("F_ora_UnitID", unitMap);

                // 成本价（含税） - 必须存在，即使为空
                entryMap.put("F_hscgj", entry.getFHscgj() != null ? entry.getFHscgj().toString() : "0");
                // 采购价 - 必须存在，即使为空
                entryMap.put("F_cgj", "0");
                // 采购新品描述 - 必须存在，即使为空
                entryMap.put("F_cgxpms", entry.getFCgxpms() != null ? entry.getFCgxpms() : "");
                // 采购包装方式 - 必须存在，即使为空
                entryMap.put("F_cgbzfs", entry.getFCgbzfs() != null ? entry.getFCgbzfs() : "");
                // 含税单价 - 必须存在，即使为空
                entryMap.put("F_hsdj", entry.getFHsdj() != null ? entry.getFHsdj().toString() : "0");
                // 价税合计 - 必须存在，即使为空
                entryMap.put("F_jshj", entry.getFJshj() != null ? entry.getFJshj().toString() : "0");
                // 起订量 - 必须存在，即使为空
                entryMap.put("F_qdl", entry.getFQdl() != null ? entry.getFQdl().toString() : "0");

                // ================= 日期字段 =================
                // 价格失效日期 - 必须存在，即使为空
                if (entry.getFJgsxrq() != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    entryMap.put("F_jgsxrq", sdf.format(java.sql.Date.valueOf(entry.getFJgsxrq())));
                } else {
                    entryMap.put("F_jgsxrq", "1900-01-01");
                }
                // 预计交期 - 必须存在，即使为空
                if (entry.getFYjjq() != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    entryMap.put("F_yjjq", sdf.format(java.sql.Date.valueOf(entry.getFYjjq())));
                } else {
                    entryMap.put("F_yjjq", "1900-01-01");
                }

                // ================= 物理属性字段 =================
                // 装箱数 - 必须存在，即使为空
                entryMap.put("F_zxs", entry.getFZxs() != null ? entry.getFZxs().toString() : "0");
                // 毛重 - 必须存在，即使为空
                entryMap.put("F_mz", entry.getFMz() != null ? entry.getFMz().toString() : "0");
                // 净重 - 必须存在，即使为空
                entryMap.put("F_jz", entry.getFJz() != null ? entry.getFJz().toString() : "0");
                // 长 - 必须存在，即使为空
                entryMap.put("F_C", entry.getFC() != null ? entry.getFC().toString() : "0");
                // 宽 - 必须存在，即使为空
                entryMap.put("F_k", entry.getFK() != null ? entry.getFK().toString() : "0");
                // 高 - 必须存在，即使为空
                entryMap.put("F_g", entry.getFG() != null ? entry.getFG().toString() : "0");

                // ================= 来源单据字段 =================
                // 源单类型 - 必须存在，即使为空
                entryMap.put("F_cty_SourceBillType", "");
                // 源单编号 - 必须存在，即使为空
                entryMap.put("F_cty_SourceBillNo", "");
                // 关联数量 - 必须存在，即使为空
                entryMap.put("F_glsl", "0");
                // 成本合计 - 必须存在，即使为空
                entryMap.put("F_Cbhj", entry.getFCbhj() != null ? entry.getFCbhj().toString() : "0");
                // 销售订单关联数 - 必须存在，即使为空
                entryMap.put("F_xsddgls", "0");

                // ================= 工厂信息字段 =================
                // 工厂英文描述 - 必须存在，即使为空
                entryMap.put("Fgcywms", entry.getFgcywms() != null ? entry.getFgcywms() : "");
                // 工厂常塑盒颜色 - 必须存在，即使为空
                entryMap.put("Fgccshys", entry.getFgccshys() != null ? entry.getFgccshys() : "");
                // 包装尺寸 - 必须存在，即使为空
                entryMap.put("Fbzcc", entry.getFbzcc() != null ? entry.getFbzcc() : "");
                // 客户货号 - 必须存在，即使为空
                entryMap.put("Fkhhh", entry.getFkhhh() != null ? entry.getFkhhh() : "");

                // ================= 价格信息字段 =================
                // 最近下单含税价 - 必须存在，即使为空
                entryMap.put("Fzjxdhsj", entry.getFzjxdhsj() != null ? entry.getFzjxdhsj().toString() : "0");
                // 最近下单数量 - 必须存在，即使为空
                entryMap.put("Fzjxdsl", entry.getFzjxdsl() != null ? entry.getFzjxdsl().toString() : "0");
                // 默认采购含税价 - 必须存在，即使为空
                entryMap.put("Fmrcghsj", entry.getFmrcghsj() != null ? entry.getFmrcghsj().toString() : "0");
                // 销售价 - 必须存在，即使为空
                entryMap.put("Fxsj", entry.getFxsj() != null ? entry.getFxsj().toString() : "0");

                // ================= 供应商和人员字段 =================
                // 供应商 - 必须存在，即使为空
                Map<String, Object> supplierMap = new HashMap<>();
                supplierMap.put("FNUMBER", entry.getFgys() != null ? entry.getFgys() : "");
                entryMap.put("Fgys", supplierMap);

                // 推品人 - 必须存在，即使为空
                Map<String, Object> tprMap = new HashMap<>();
                tprMap.put("FUserID", ""); // 默认空值
                entryMap.put("Ftpr", tprMap);

                // 报价人 - 必须存在，即使为空
                Map<String, Object> bjrMap = new HashMap<>();
                bjrMap.put("FUserID", entry.getFbjr() != null ? entry.getFbjr() : "");
                entryMap.put("Fbjr", bjrMap);

                // ================= 产品优势字段 =================
                // 产品优势 - 必须存在，即使为空
                entryMap.put("Fcpys", entry.getFcpys() != null ? entry.getFcpys() : "");
                // 产品优势标签 - 必须存在，即使为空
                entryMap.put("Fcpys_Tag", "");

                entryList.add(entryMap);
            }
        } else {
            // 如果没有明细数据，添加一个空的明细行
            Map<String, Object> emptyEntry = new HashMap<>();
            emptyEntry.put("FEntryID", 0);

            // 添加所有必需的字段，设置为空值
            Map<String, Object> emptyProductCode = new HashMap<>();
            emptyProductCode.put("FNUMBER", "");
            emptyEntry.put("F_wldm", emptyProductCode);
            emptyEntry.put("F_cpdm", emptyProductCode);
            emptyEntry.put("F_cpms", "");
            emptyEntry.put("F_ywpm", "");
            emptyEntry.put("F_khbzyq", "");
            emptyEntry.put("F_xdl", "0");

            Map<String, Object> emptyUnit = new HashMap<>();
            emptyUnit.put("FNumber", "");
            emptyEntry.put("F_ora_UnitID", emptyUnit);

            emptyEntry.put("F_hscgj", "0");
            emptyEntry.put("F_cgj", "0");
            emptyEntry.put("F_cgxpms", "");
            emptyEntry.put("F_cgbzfs", "");
            emptyEntry.put("F_hsdj", "0");
            emptyEntry.put("F_jshj", "0");
            emptyEntry.put("F_qdl", "0");
            emptyEntry.put("F_jgsxrq", "1900-01-01");
            emptyEntry.put("F_yjjq", "1900-01-01");
            emptyEntry.put("F_zxs", "0");
            emptyEntry.put("F_mz", "0");
            emptyEntry.put("F_jz", "0");
            emptyEntry.put("F_C", "0");
            emptyEntry.put("F_k", "0");
            emptyEntry.put("F_g", "0");
            emptyEntry.put("F_cty_SourceBillType", "");
            emptyEntry.put("F_cty_SourceBillNo", "");
            emptyEntry.put("F_glsl", "0");
            emptyEntry.put("F_Cbhj", "0");
            emptyEntry.put("F_xsddgls", "0");
            emptyEntry.put("Fgcywms", "");
            emptyEntry.put("Fgccshys", "");
            emptyEntry.put("Fbzcc", "");
            emptyEntry.put("Fkhhh", "");
            emptyEntry.put("Fzjxdhsj", "0");
            emptyEntry.put("Fzjxdsl", "0");
            emptyEntry.put("Fmrcghsj", "0");
            emptyEntry.put("Fxsj", "0");

            Map<String, Object> emptySupplier = new HashMap<>();
            emptySupplier.put("FNUMBER", "");
            emptyEntry.put("Fgys", emptySupplier);

            Map<String, Object> emptyTpr = new HashMap<>();
            emptyTpr.put("FUserID", "");
            emptyEntry.put("Ftpr", emptyTpr);

            Map<String, Object> emptyBjr = new HashMap<>();
            emptyBjr.put("FUserID", "");
            emptyEntry.put("Fbjr", emptyBjr);

            emptyEntry.put("Fcpys", "");
            emptyEntry.put("Fcpys_Tag", "");

            entryList.add(emptyEntry);
        }

        return entryList;
    }
}
