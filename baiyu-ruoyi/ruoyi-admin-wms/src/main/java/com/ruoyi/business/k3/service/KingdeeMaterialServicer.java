package com.ruoyi.business.k3.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.entity.Bymaterial;
import com.ruoyi.business.entity.SysDataAuditLog;
import com.ruoyi.business.util.ImportResult;
import com.ruoyi.business.util.Result;

import java.util.List;
import java.util.Map;


public interface KingdeeMaterialServicer {

    void queryMaterialList(List<List<Object>> materialList,List<List<Object>> productCategories);

    /**
     * 查询物料列表
     */
    void queryMaterialList(List<List<Object>> productCategories);
    /**
     * 新增物料
     */
  Result addMaterial(Bymaterial material);
    Result updateMaterial(Bymaterial material);
    /**
     * 根据id查询物料
     */
    Bymaterial getMaterialById( Long id);
    /**
     * 根据表名和主键ID查询审计日志（分页）
     ** @return 审计日志分页数据
     */
    Page<SysDataAuditLog> getAuditLogsByTableAndId(String tableName, String rowId, int pageNum, int pageSize);
    /**
     * 分页查询物料列表
     */
    Page<Bymaterial> listMaterials(Bymaterial condition, int page, int size,String isAsc);

    // 用于内部调用的直接查询方法
    Bymaterial getMaterialByNumberDirect(String number);


    String fillEnglishDesc(Bymaterial materials);

    Result deleteMaterial(Long id);

    /**
     * 校验物料分组是否存在
     * @param materialGroup 物料分组名称
     * @return true: 存在, false: 不存在
     */
    boolean isMaterialGroupExists(String materialGroup);
    /**
     * 校验物料属性是否存在
     * @param erpClsId 物料属性ID
     * @return true: 存在, false: 不存在
     */
    boolean isErpClsIdExists(String erpClsId);

    /**
     * 校验产品类别是否存在
     * @param productCategory 产品类别名称
     * @return true: 存在, false: 不存在
     */
    boolean isProductCategoryExists(String productCategory);

    /**
     * 校验新老产品标识是否有效
     * @param isNewProduct 新老产品标识
     * @return true: 有效, false: 无效
     */
    boolean isNewProductFlagValid(String isNewProduct);

    ImportResult processSingleDataRow(Map<String, Object> data, int index) ;

    /**
     *
     * 交付红线
     * */
    Result updateMaterials(Long[] ids,String state);


}
