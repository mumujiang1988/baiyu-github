package com.ruoyi.business.Component;



import com.ruoyi.business.entity.Bymaterial;
import com.ruoyi.business.entity.BymaterialDictionary;
import com.ruoyi.business.entity.DictionaryTable;
import com.ruoyi.business.k3.config.AbstractK3FormProcessor;
import com.ruoyi.business.mapper.BymaterialDictionaryMapper;
import com.ruoyi.business.mapper.DictionaryTableMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.dev33.satoken.SaManager.log;

/**
 * 物料表单处理器
 * 负责处理BD_MATERIAL表单的构建和提交
 */
@Component  // 关键：添加@Component注解，让Spring管理
public class MaterialFormProcessor extends AbstractK3FormProcessor<Bymaterial> {

    @Autowired
    private DictionaryTableMapper dictionaryTableMapper;

    @Autowired
    private BymaterialDictionaryMapper bymaterialDictionaryMapper;

    @Override
    protected String getFormId() {
        return "BD_MATERIAL";
    }

    @Override
    protected List<String> getFileFieldNames() {
        return Arrays.asList("FImageFileServer", "Fyhbg");
    }

    @Override
    protected String getDocumentNumber(Bymaterial formData) {
        return formData.getNumber() != null ? formData.getNumber() : "";
    }

    @Override
    protected Map<String, Object> buildModel(Bymaterial bymaterial) {
        log.info("开始构建物料模型数据，物料编号: {}", bymaterial.getNumber());

        Map<String, Object> model = new HashMap<>();

        if (bymaterial != null) {
            // 基础字段
            model.put("FNumber", bymaterial.getNumber() != null ? bymaterial.getNumber() : "");
            model.put("FName", bymaterial.getName() != null ? bymaterial.getName() : "");
            model.put("FSpecification", bymaterial.getSpecification() != null ? bymaterial.getSpecification() : "");
            model.put("FDescription1", bymaterial.getDescription1() != null ? bymaterial.getDescription1() : "");

            // 物料组字段
            Map<String, Object> materialGroupMap = buildMaterialGroup(bymaterial.getMaterialgroup());
            model.put("FMaterialGroup", materialGroupMap);

            // 新产品字段
            Map<String, Object> newProductMap = buildNewProductCategory(bymaterial.getFxlcp());
            model.put("F_XLCP", newProductMap);

            // 体积字段
            /*model.put("FVOLUME", bymaterial.getVolume() != null ? bymaterial.getVolume() : "");*/

            // 海关编码字段
            model.put("F_HSBM1", bymaterial.getHsbm() != null ? bymaterial.getHsbm() : "");

            // 产品类别字段
            Map<String, Object> productCategoryMap = buildProductCategory(bymaterial.getProductCategory());
            model.put("F_cplb", productCategoryMap);
        }

        return model;
    }

    private Map<String, Object> buildMaterialGroup(String materialGroupName) {
        Map<String, Object> materialGroupMap = new HashMap<>();

        if (materialGroupName != null && !materialGroupName.isEmpty()) {
            try {
                DictionaryTable dictionaryTable = new DictionaryTable();
                dictionaryTable.setBilhead(materialGroupName);
                DictionaryTable result = dictionaryTableMapper.selectByCondition(dictionaryTable);

                if (result != null && result.getDictCode() != null) {
                    materialGroupMap.put("FNumber", result.getDictCode());
                } else {
                    materialGroupMap.put("FNumber", "");
                }
            } catch (Exception e) {
                materialGroupMap.put("FNumber", "");
            }
        } else {
            materialGroupMap.put("FNumber", "");
        }

        return materialGroupMap;
    }

    private Map<String, Object> buildNewProductCategory(String newProductCode) {
        Map<String, Object> newProductMap = new HashMap<>();

        if (newProductCode != null) {
            try {
                BymaterialDictionary dictionary = bymaterialDictionaryMapper.selectByCategoryAndCode("product_type", newProductCode);
                if (dictionary != null && dictionary.getKingdee() != null) {
                    newProductMap.put("FNumber", dictionary.getKingdee());
                } else {
                    newProductMap.put("FNumber", "");
                }
            } catch (Exception e) {
                newProductMap.put("FNumber", "");
            }
        } else {
            newProductMap.put("FNumber", "");
        }

        return newProductMap;
    }

    private Map<String, Object> buildProductCategory(String productCategory) {
        Map<String, Object> productCategoryMap = new HashMap<>();

        if (productCategory != null) {
            try {
                BymaterialDictionary dictionary = bymaterialDictionaryMapper.selectByCategoryAndName(productCategory);
                if (dictionary != null && dictionary.getCode() != null) {
                    productCategoryMap.put("FNumber", dictionary.getCode());
                } else {
                    productCategoryMap.put("FNumber", "");
                }
            } catch (Exception e) {
                productCategoryMap.put("FNumber", "");
            }
        } else {
            productCategoryMap.put("FNumber", "");
        }

        return productCategoryMap;
    }
}
