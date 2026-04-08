package com.ruoyi.business.servicel;

import com.ruoyi.business.entity.*;
import com.ruoyi.business.k3.domain.vo.SupplierVo;
import org.apache.commons.math3.geometry.partitioning.BSPTree;


import java.util.List;

public interface DictionaryLookupServicel {

    public List<DictionaryTable> getDictionaryLookup();

    /**
     * 供应商分组
     * */
    public List<SupplierGroups> getSupplierGroups();



    /**
     * 根据k3id查询供应商分组字典
     *
     * @param supplierGroup
     * @return 供应商分组字典信息
     */
    SupplierGroups SupplierGroupsK3id(String supplierGroup);

    List<BymaterialDictionary> categoryName(String categoryName, String category);

    /**
     *  通code获取来源名称
     * */
    BymaterialDictionary categoryCode(String code);


    List<SettlementMethod> settlementMethod();

     List<TaxRate> getByKingdee();

     /**
      * 供应商
      * */
     List<SupplierVo> listSuppliers();

     /**
      * 物料
      * */
    List<MaterialDictionary> selectmaterial();

}
