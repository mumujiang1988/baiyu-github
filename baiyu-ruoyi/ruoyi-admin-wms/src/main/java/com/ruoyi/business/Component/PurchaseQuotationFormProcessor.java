package com.ruoyi.business.Component;

import com.ruoyi.business.entity.PurchaseQuotation;
import com.ruoyi.business.entity.PurchaseQuotationEntry;
import com.ruoyi.business.k3.config.AbstractK3FormProcessor;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 采购报价单表单处理器
 * 负责处理采购报价单表单的构建和提交到金蝶
 */
@Component  // 关键：添加@Component注解，让Spring管理
public class PurchaseQuotationFormProcessor extends AbstractK3FormProcessor<PurchaseQuotation> {

    @Override
    protected String getFormId() {
        return "PUR_Quotation"; // 采购报价单表单ID
    }

    @Override
    protected List<String> getFileFieldNames() {
        // 采购报价单通常可能有附件字段，这里先预留，根据实际金蝶字段调整
        return new ArrayList<>();
    }

    @Override
    protected String getDocumentNumber(PurchaseQuotation formData) {
        return formData.getFbillno() != null ? formData.getFbillno() : "";
    }

    @Override
    protected Map<String, Object> buildModel(PurchaseQuotation quotation) {
        System.out.println("开始构建采购报价单模型数据，单据编号: " + quotation.getFbillno());

        Map<String, Object> model = new HashMap<>();

        if (quotation != null) {
            // ================= 基础字段 =================
            model.put("FID", 0); // 新增采购报价单固定为0
            if (quotation.getFbillno() != null) {
                model.put("FBillNo", quotation.getFbillno());
            }

            // 创建人信息
            if (quotation.getFcreatorid() != null) {
                Map<String, Object> creatorMap = new HashMap<>();
                creatorMap.put("FUserID", quotation.getFcreatorid());
                model.put("FCreatorId", creatorMap);
            }

            // 创建日期
            if (quotation.getFcreatedate() != null) {
                model.put("FCreateDate", quotation.getFcreatedate().toString());
            }

            // 审核人信息
            if (quotation.getFshr() != null) {
                Map<String, Object> auditorMap = new HashMap<>();
                auditorMap.put("FUserID", quotation.getFshr());
                model.put("Fshr", auditorMap);
            }

            // 审核日期
            if (quotation.getFdateSh() != null) {
                model.put("FDATE_sh", quotation.getFdateSh().toString());
            }

            // 日期
            if (quotation.getFdate() != null) {
                model.put("FDate", quotation.getFdate().toString());
            }

            // ================= 业务字段 =================
            // 销售员
            if (quotation.getFxSy() != null) {
                Map<String, Object> salesPersonMap = new HashMap<>();
                salesPersonMap.put("FSTAFFNUMBER", quotation.getFxSy());
                model.put("F_xsy", salesPersonMap);
            }

            // 客户简称
            if (quotation.getFKhjc() != null) {
                model.put("F_KHJC", quotation.getFKhjc());
            }

            // 客户编码
            if (quotation.getFKh() != null) {
                Map<String, Object> customerMap = new HashMap<>();
                customerMap.put("FNUMBER", quotation.getFKh());
                model.put("F_kh", customerMap);
            }

            // 客户名称
            if (quotation.getFKhmc() != null) {
                model.put("F_KHMC", quotation.getFKhmc());
            }

            // 出口国家
            if (quotation.getFCkgj() != null) {
                Map<String, Object> exportCountryMap = new HashMap<>();
                exportCountryMap.put("FNUMBER", quotation.getFCkgj());
                model.put("F_Ckgj", exportCountryMap);
            }

            // 客户类型
            if (quotation.getFKhlx() != null) {
                Map<String, Object> customerTypeMap = new HashMap<>();
                customerTypeMap.put("FNUMBER", quotation.getFKhlx());
                model.put("F_khlx", customerTypeMap);
            }

            // ================= 联系信息 =================
            // Email
            if (quotation.getFEmail() != null) {
                model.put("F_Email", quotation.getFEmail());
            }
            // 手机
            if (quotation.getFMob() != null) {
                model.put("F_Mob", quotation.getFMob());
            }
            // 电话
            if (quotation.getFTel() != null) {
                model.put("F_Tel", quotation.getFTel());
            }
            // 传真
            if (quotation.getFFax() != null) {
                model.put("F_Fax", quotation.getFFax());
            }

            // ================= 业务信息 =================
            // Shipping To
            if (quotation.getFQyd() != null) {
                Map<String, Object> shipToMap = new HashMap<>();
                shipToMap.put("FNumber", quotation.getFQyd());
                model.put("F_qyd", shipToMap);
            }

            // Payment terms
            if (quotation.getFFktj() != null) {
                model.put("F_fktj", quotation.getFFktj());
            }

            // ================= 明细行数据 =================
            // 构建明细行数据
            List<Map<String, Object>> entryList = buildEntryList(quotation.getEntries());
            model.put("FEntity", entryList);
        }

        System.out.println("采购报价单模型构建完成: " + model);
        return model;
    }

    /**
     * 构建采购报价单明细行数据
     */
    private List<Map<String, Object>> buildEntryList(List<PurchaseQuotationEntry> entries) {
        List<Map<String, Object>> entryList = new ArrayList<>();

        if (entries != null && !entries.isEmpty()) {
            for (int i = 0; i < entries.size(); i++) {
                PurchaseQuotationEntry entry = entries.get(i);
                Map<String, Object> entryMap = new HashMap<>();

                // ================= 基础字段 =================
                // 行号 - 必须存在
                entryMap.put("FDetailID", i + 1);

                // 单据编号
                if (entry.getFbillno() != null) {
                    entryMap.put("FBillNo", entry.getFbillno());
                }

                // 计量单位
                if (entry.getFOraUnitId() != null) {
                    Map<String, Object> unitMap = new HashMap<>();
                    unitMap.put("FNumber", entry.getFOraUnitId());
                    entryMap.put("F_ora_UnitID", unitMap);
                }

                // 产品优势
                if (entry.getFBz() != null) {
                    entryMap.put("F_BZ", entry.getFBz());
                }

                // 采购图片1
                if (entry.getFTp() != null) {
                    entryMap.put("F_TP", entry.getFTp());
                }

                // 供应商
                if (entry.getFGys() != null) {
                    Map<String, Object> supplierMap = new HashMap<>();
                    supplierMap.put("FNUMBER", entry.getFGys());
                    entryMap.put("F_gys", supplierMap);
                }

                // 采购图片2
                if (entry.getFCptp1() != null) {
                    entryMap.put("F_Cptp1", entry.getFCptp1());
                }

                // 价格有效期
                if (entry.getFJgyxq() != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    entryMap.put("F_jgyxq", sdf.format(java.sql.Date.valueOf(entry.getFJgyxq())));
                }

                // 采购价
                if (entry.getFCgjg() != null) {
                    entryMap.put("F_cgjg", entry.getFCgjg().toString());
                }

                // 起订量
                if (entry.getFZxqdl() != null) {
                    entryMap.put("F_zxqdl", entry.getFZxqdl().toString());
                }

                // 采购包装方式
                if (entry.getFXcpms() != null) {
                    entryMap.put("F_xcpms", entry.getFXcpms());
                }

                // 新供应商
                if (entry.getFStgys() != null) {
                    entryMap.put("F_stgys", entry.getFStgys());
                }

                // 下单量
                if (entry.getFXdl() != null) {
                    entryMap.put("F_xdl", entry.getFXdl().toString());
                }

                // 产品代码
                if (entry.getFCpdm1() != null) {
                    entryMap.put("F_cpdm1", entry.getFCpdm1());
                }

                // 新品描述
                if (entry.getFYwbz() != null) {
                    entryMap.put("F_ywbz", entry.getFYwbz());
                }

                // 物料图片
                if (entry.getFWltp() != null) {
                    entryMap.put("F_wltp", entry.getFWltp());
                }

                // 产品名称
                if (entry.getFCpms1() != null) {
                    entryMap.put("F_CPMS1", entry.getFCpms1());
                }

                // 包装明细
                if (entry.getFBzmx() != null) {
                    entryMap.put("F_BZMX", entry.getFBzmx());
                }

                // 规格说明
                if (entry.getFCtyBaseproperty4() != null) {
                    entryMap.put("F_cty_BaseProperty4", entry.getFCtyBaseproperty4());
                }

                // 业务图片1
                if (entry.getFYwtp1() != null) {
                    entryMap.put("F_ywtp1", entry.getFYwtp1());
                }

                // 业务图片2
                if (entry.getFYwtp2() != null) {
                    entryMap.put("F_ywtp2", entry.getFYwtp2());
                }

                // ================= 物理属性字段 =================
                // 装箱数
                if (entry.getFZxs() != null) {
                    entryMap.put("F_zxs", entry.getFZxs().toString());
                }
                // 毛重
                if (entry.getFMz() != null) {
                    entryMap.put("F_mz", entry.getFMz().toString());
                }
                // 净重
                if (entry.getFJz() != null) {
                    entryMap.put("F_jz", entry.getFJz().toString());
                }
                // 长
                if (entry.getFC() != null) {
                    entryMap.put("F_C", entry.getFC().toString());
                }
                // 宽
                if (entry.getFK() != null) {
                    entryMap.put("F_k", entry.getFK().toString());
                }
                // 高
                if (entry.getFG() != null) {
                    entryMap.put("F_g", entry.getFG().toString());
                }

                // ================= 价格成本字段 =================
                // 成本价（含税）
                if (entry.getFCghsj() != null) {
                    entryMap.put("F_Cghsj", entry.getFCghsj().toString());
                }

                // ================= 产品信息 =================
                // 产品类型
                if (entry.getFCtyBaseproperty() != null) {
                    entryMap.put("F_cty_BaseProperty", entry.getFCtyBaseproperty());
                }
                // 工厂图片
                if (entry.getFgctp() != null) {
                    entryMap.put("Fgctp", entry.getFgctp());
                }
                // 预计交货日期
                if (entry.getFyjjhrq() != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    entryMap.put("Fyjjhrq", sdf.format(java.sql.Date.valueOf(entry.getFyjjhrq())));
                }

                // ================= 规格包装字段 =================
                // 包装尺寸
                if (entry.getFbzcc() != null) {
                    entryMap.put("Fbzcc", entry.getFbzcc());
                }
                // 常规塑盒颜色
                if (entry.getFcgshys() != null) {
                    entryMap.put("Fcgshys", entry.getFcgshys());
                }
                // 报价人员
                if (entry.getFFbjry() != null) {
                    entryMap.put("F_Fbjry", entry.getFFbjry());
                }
                // 英文品名
                if (entry.getFYwpm() != null) {
                    entryMap.put("F_ywpm", entry.getFYwpm());
                }
                // 英文描述
                if (entry.getFYwms() != null) {
                    entryMap.put("F_ywms", entry.getFYwms());
                }
                // 规格描述
                if (entry.getFCtyText() != null) {
                    entryMap.put("F_cty_Text", entry.getFCtyText());
                }
                // 客户货号
                if (entry.getFkhhh() != null) {
                    entryMap.put("Fkhhh", entry.getFkhhh());
                }

                // ================= 库存信息 =================
                // 库存数量
                if (entry.getFqtyKc() != null) {
                    entryMap.put("FQTY_kc", entry.getFqtyKc().toString());
                }
                // 总箱数
                if (entry.getFzxss() != null) {
                    entryMap.put("Fzxs", entry.getFzxss().toString());
                }
                // 体积
                if (entry.getFtj() != null) {
                    entryMap.put("Ftj", entry.getFtj().toString());
                }
                // 毛总重
                if (entry.getFmzz() != null) {
                    entryMap.put("Fmzz", entry.getFmzz().toString());
                }

                // ================= 其他字段 =================
                // 客户需求
                if (entry.getFkhxq() != null) {
                    entryMap.put("Fkhxq", entry.getFkhxq());
                }
                // 采购新品描述
                if (entry.getFcgxpms() != null) {
                    entryMap.put("Fcgxpms", entry.getFcgxpms());
                }
                // 新供应商联系人
                if (entry.getFxgyslxr() != null) {
                    entryMap.put("Fxgyslxr", entry.getFxgyslxr());
                }
                // 新供应商联系方式
                if (entry.getFxgyslxfs() != null) {
                    entryMap.put("Fxgyslxfs", entry.getFxgyslxfs());
                }

                entryList.add(entryMap);
            }
        } else {
            // 如果没有明细数据，添加一个空的明细行
            Map<String, Object> emptyEntry = new HashMap<>();
            emptyEntry.put("FDetailID", 0);

            entryList.add(emptyEntry);
        }

        return entryList;
    }
}
