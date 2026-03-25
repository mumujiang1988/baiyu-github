package com.ruoyi.business.controller;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.ruoyi.business.entity.*;
import com.ruoyi.business.k3.domain.vo.SupplierVo;
import com.ruoyi.business.k3.service.SysAccountUserService;
import com.ruoyi.business.mapper.BymaterialDictionaryMapper;
import com.ruoyi.business.mapper.DictionaryTableMapper;
import com.ruoyi.business.servicel.CountryService;
import com.ruoyi.business.servicel.DictionaryLookupServicel;
import com.ruoyi.business.util.Result;
import com.ruoyi.system.domain.bo.SysUserBo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.*;

/**
 * 字典查询
 * */
@RestController
@RequestMapping("/api/v1/kingdee")
@Slf4j
public class DictionaryLookupcontroller {

    @Autowired
    private DictionaryLookupServicel dictionaryLookupServicel;
    @Autowired
    private SysAccountUserService sysAccountUserService;

    @Autowired
    private CountryService countryService;
    @Autowired
    private DictionaryTableMapper dictionaryTableMapper;
    @Autowired

    @PostMapping("/dictionaryLookup")
    public Result dictionaryLookup(){
      List<DictionaryTable> dictionaryLookup =  dictionaryLookupServicel.getDictionaryLookup();
        // 构建树
        Map<String, DictNode> map = new HashMap<>();
        dictionaryLookup.forEach(d -> {
            DictNode node = new DictNode();
            node.setId(d.getId().toString());
            node.setName(d.getDictName());
            node.setMaterialgroup(d.getBilhead());
            node.setParentCode(d.getParentCode());
            map.put(d.getDictCode(), node);
        });

        List<DictNode> roots = new ArrayList<>();
        for (DictNode node : map.values()) {
            if (node.getParentCode() == null || !map.containsKey(node.getParentCode())) {
                roots.add(node);
            } else {
                map.get(node.getParentCode()).getChildren().add(node);
            }
        }
        System.out.println("查询的数据为"+roots);
        return Result.success(roots);
    }

    /**
     * 供应商分组 supplier_groups
    * */
    @PostMapping("/suppliergroups")
    public Result supplierGroups() {
        try {
            List<SupplierGroups> supplierGroups = dictionaryLookupServicel.getSupplierGroups();

            if (CollectionUtils.isEmpty(supplierGroups)) {
                return Result.success(Collections.emptyList());
            }

            // 1. 创建所有节点映射
            Map<String, DictSupplierGroupsNode> nodeMap = new HashMap<>(supplierGroups.size());
            Map<Integer, String> idMapping = new HashMap<>(supplierGroups.size()); // 用于ID映射查找

            // 第一遍遍历：创建所有节点
            for (SupplierGroups group : supplierGroups) {
                DictSupplierGroupsNode node = new DictSupplierGroupsNode();
                node.setId(String.valueOf(group.getId()));
                node.setSupplierGroup(group.getSupplierGroup());
                node.setGroupName(group.getGroupName());
                node.setGroupPing(group.getGroupPing());
                node.setParentId(group.getParentId());

                String nodeId = node.getId();
                nodeMap.put(nodeId, node);

                // 存储原始ID到节点ID的映射（使用Long类型的ID）
                if (group.getId() != null) {
                    idMapping.put(group.getId().intValue(), nodeId);
                }
            }

            // 2. 构建树形结构
            List<DictSupplierGroupsNode> roots = new ArrayList<>();

            for (DictSupplierGroupsNode node : nodeMap.values()) {
                Integer parentId = node.getParentId();
                Integer groupPing = node.getGroupPing();

                // 优先使用parentId作为父节点关系，如果不存在则使用groupPing
                Integer effectiveParentId = (parentId != null && parentId > 0) ? parentId : groupPing;

                if (effectiveParentId == null || effectiveParentId <= 0) {
                    // 没有父节点，作为根节点
                    roots.add(node);
                    continue;
                }

                // 查找父节点
                String parentNodeId = idMapping.get(effectiveParentId);
                DictSupplierGroupsNode parentNode = nodeMap.get(parentNodeId);

                if (parentNode != null) {
                    // 避免循环引用
                    if (!isCircularReference(parentNode, node.getId())) {
                        parentNode.getChildren().add(node);
                    } else {
                        // 如果是循环引用，则作为根节点
                        roots.add(node);
                    }
                } else {
                    // 父节点不存在，作为根节点
                    roots.add(node);
                }
            }

            return Result.success(roots);
        } catch (Exception e) {
            log.error("构建供应商分组树失败", e);
            return Result.error("构建供应商分组树失败");
        }
    }

    /**
     * 检查循环引用
     */
    private boolean isCircularReference(DictSupplierGroupsNode parent, String childId) {
        // 如果父节点的ID等于子节点ID，直接循环引用
        if (parent.getId().equals(childId)) {
            return true;
        }

        // 递归检查父节点的父节点
        Integer parentParentId = parent.getParentId();
        if (parentParentId != null && parentParentId > 0) {
            // 这里简化处理，实际应该继续向上检查
            // 由于数据结构简单，这里假设只检查一层
            return false;
        }

        return false;
    }

    /**
     * 国家
     * */
    @GetMapping("/getnAtion")
    public Result getnAtion() {
        List<Country> country = countryService.getnAtion();
        country.forEach(cy ->{
            //供应商-国家
            cy.setNation(cy.getNation());
            //客户-抵运国家
            cy.setFdygj(cy.getNation());
            //客户-国家
            cy.setFcountry(cy.getNation());
            cy.setNameZh(cy.getNameZh());
        });
        return Result.success(country);
    }


    /*物料属性*/
    @GetMapping("/MaterialDictionary")
    public Result getByKingdee(@RequestParam("categoryName") String categoryName) {
        String category = "erpClsId_property";
      List<BymaterialDictionary> bymaterialDictionaries=  dictionaryLookupServicel.categoryName(categoryName.toString(),category);
        ArrayList<BymaterialDictionary> array  = new ArrayList<>();
        for (BymaterialDictionary dictionary :bymaterialDictionaries){
            BymaterialDictionary materialDictionary = new BymaterialDictionary();
            materialDictionary.setErpClsId(String.valueOf(dictionary.getKingdee()));
            materialDictionary.setName(dictionary.getName());
            array.add(materialDictionary);
        }
        return Result.success(array);
    }

    /*新老产品*/
    @GetMapping("/xlproduct")
    public Result getxlproduct(@RequestParam("categoryName") String categoryName) {
        String category = "product_type";
        List<BymaterialDictionary> bymaterialDictionaries=  dictionaryLookupServicel.categoryName(categoryName.toString(),category);
        ArrayList<BymaterialDictionary> array  = new ArrayList<>();
        for (BymaterialDictionary dictionary :bymaterialDictionaries){
            BymaterialDictionary materialDictionary = new BymaterialDictionary();
            materialDictionary.setFxlcp(String.valueOf(dictionary.getKingdee()));
            materialDictionary.setName(dictionary.getName());
            array.add(materialDictionary);
        }
        return Result.success(array);
    }

    /**
     *  产品类别 Product Category
     * */
    @GetMapping("/ProductCategory")
    public Result getProductCategory(@RequestParam("categoryName") String categoryName) {
        String category = "product_category";
        List<BymaterialDictionary> bymaterialDictionaries=  dictionaryLookupServicel.categoryName(categoryName.toString(),category);
        ArrayList<BymaterialDictionary> array  = new ArrayList<>();
        for (BymaterialDictionary dictionary :bymaterialDictionaries){
            BymaterialDictionary materialDictionary = new BymaterialDictionary();
            materialDictionary.setProductCategory(String.valueOf(dictionary.getKingdee()));
            materialDictionary.setName(dictionary.getName());
            array.add(materialDictionary);
        }
        return Result.success(array);
    }

    /**
     *供应商分类
     * */
    @GetMapping("/supplierclassification")
    public Result supplierclassification() {
        String categoryName = "供应商分类";
        String category = "supplier_classification";
        List<BymaterialDictionary> bymaterialDictionaries=  dictionaryLookupServicel.categoryName(categoryName,category);
        ArrayList<BymaterialDictionary> array  = new ArrayList<>();
        for (BymaterialDictionary dictionary :bymaterialDictionaries){
            BymaterialDictionary materialDictionary = new BymaterialDictionary();
            materialDictionary.setProductCategory(dictionary.getCode());
            materialDictionary.setName(dictionary.getName());
            array.add(materialDictionary);
        }
        return Result.success(array);
    }

    /**
     * 供应商来源
     * */
    @GetMapping("/suppliersource")
    public Result suppliersource() {
        String categoryName = "供应商来源";
        String category = "customer_source";
        List<BymaterialDictionary> bymaterialDictionaries=  dictionaryLookupServicel.categoryName(categoryName,category);
        ArrayList<BymaterialDictionary> array  = new ArrayList<>();
        for (BymaterialDictionary dictionary :bymaterialDictionaries){
            BymaterialDictionary materialDictionary = new BymaterialDictionary();
            materialDictionary.setProductCategory(dictionary.getCode());
            materialDictionary.setName(dictionary.getName());
            array.add(materialDictionary);
        }
        return Result.success(array);
    }

    /**
     * 币别
     * */
    @GetMapping("/currency")
    public Result currency() {
        String categoryName = "币别";
        String category = "currency";
        List<BymaterialDictionary> bymaterialDictionaries=  dictionaryLookupServicel.categoryName(categoryName,category);
        ArrayList<BymaterialDictionary> array  = new ArrayList<>();
        for (BymaterialDictionary dictionary :bymaterialDictionaries){
            BymaterialDictionary materialDictionary = new BymaterialDictionary();
            materialDictionary.setProductCategory(dictionary.getCode());
            materialDictionary.setName(dictionary.getName());
            materialDictionary.setFtradingCurrId(dictionary.getKingdee());
            materialDictionary.setFCurrencyID(dictionary.getKingdee());
            array.add(materialDictionary);
        }
        return Result.success(array);
    }

    /**付款条件
     * */
    @GetMapping("/paymentterms")
    public Result paymentterms() {
        String categoryName = "付款条件";
        String category = "payment_clause";
        List<BymaterialDictionary> bymaterialDictionaries=  dictionaryLookupServicel.categoryName(categoryName,category);
        ArrayList<BymaterialDictionary> array  = new ArrayList<>();
        for (BymaterialDictionary dictionary :bymaterialDictionaries){
            BymaterialDictionary materialDictionary = new BymaterialDictionary();
            materialDictionary.setProductCategory(dictionary.getKingdee());
            materialDictionary.setName(dictionary.getName());
            array.add(materialDictionary);
        }
        return Result.success(array);
    }

    /**
     * 收款条件
     * */
    @GetMapping("/payment")
    public Result payment() {
        String categoryName = "收款条件";
        String category = "collection_terms";
        List<BymaterialDictionary> bymaterialDictionaries=  dictionaryLookupServicel.categoryName(categoryName,category);
        bymaterialDictionaries.forEach(dictionaries -> {
            dictionaries.setFrecConditionId(dictionaries.getKingdee());
        });
        return Result.success(bymaterialDictionaries);
    }

    /**
     * 客户分组
     * */
    @GetMapping("/groupId")
    public Result customer() {
        String categoryName = "客户分组";
        String category = "Customer_grouping";
        List<BymaterialDictionary> bymaterialDictionaries=  dictionaryLookupServicel.categoryName(categoryName,category);
        bymaterialDictionaries.forEach(dictionaries -> {
            dictionaries.setFgroupId(dictionaries.getKingdee());
        });
        return Result.success(bymaterialDictionaries);
    }

    /**
    * 发票类型
    * */
    @GetMapping("/invoicetype")
    public Result invoicetype() {
        String categoryName = "发票类型";
        String category = "Invoice_type";
        List<BymaterialDictionary> bymaterialDictionaries=  dictionaryLookupServicel.categoryName(categoryName,category);
        ArrayList<BymaterialDictionary> array  = new ArrayList<>();
        for (BymaterialDictionary dictionary :bymaterialDictionaries){
            BymaterialDictionary materialDictionary = new BymaterialDictionary();
            materialDictionary.setProductCategory(String.valueOf(dictionary.getKingdee()));
            materialDictionary.setName(dictionary.getName());
            array.add(materialDictionary);
        }
        return Result.success(array);
    }

    /**
     * 税分类
     * */
    @GetMapping("/taxclassification")
    public Result taxclassification() {
        String categoryName = "税分类";
        String category = "tariff_nomenclature";
        List<BymaterialDictionary> bymaterialDictionaries=  dictionaryLookupServicel.categoryName(categoryName,category);
        ArrayList<BymaterialDictionary> array  = new ArrayList<>();
        for (BymaterialDictionary dictionary :bymaterialDictionaries){
            BymaterialDictionary materialDictionary = new BymaterialDictionary();
            materialDictionary.setProductCategory(String.valueOf(dictionary.getKingdee()));
            materialDictionary.setName(dictionary.getName());
            array.add(materialDictionary);
        }
        return Result.success(array);
    }

    /**供应类别
     * */
    @GetMapping("/supplycategory")
    public Result supplycategory() {
        String categoryName = "供应类别";
        String category = "Supply_category";
        List<BymaterialDictionary> bymaterialDictionaries=  dictionaryLookupServicel.categoryName(categoryName,category);
        ArrayList<BymaterialDictionary> array  = new ArrayList<>();
        for (BymaterialDictionary dictionary :bymaterialDictionaries){
            BymaterialDictionary materialDictionary = new BymaterialDictionary();
            materialDictionary.setProductCategory(String.valueOf(dictionary.getKingdee()));
            materialDictionary.setName(dictionary.getName());
            array.add(materialDictionary);
        }
        return Result.success(array);
    }


    /**
     *结算方式 */
    @GetMapping("/settlementMethod/all")
    public List<SettlementMethod> getByCode() {
      List<SettlementMethod> bymaterialDictionaries=  dictionaryLookupServicel.settlementMethod();
        return bymaterialDictionaries;
    }

    /**
     *客户结算方式
     * */
    @GetMapping("/lementMethod/all")
    public Result getByCodes() {
        List<SettlementMethod> bymaterialDictionaries=  dictionaryLookupServicel.settlementMethod();
        bymaterialDictionaries.forEach(dictionaries ->{
            dictionaries.setFsettleTypeId(dictionaries.getId());
        });
        return Result.success(bymaterialDictionaries);
    }


    /**
     * 税率
     * */
    @GetMapping("/taxRate/all")
    public List<TaxRate>  getByK3Codes() {
        List<TaxRate>  bymaterialDictionaries=  dictionaryLookupServicel.getByKingdee();
        return bymaterialDictionaries;
    }

    /**
     * 查询负责人
     * */
    @GetMapping("/user/all")
    public List<SysUserBo> getUser() {
        List<SysUserBo> users =  sysAccountUserService.getSysAccountUser();
        return users;
    }

    /**
     * 销售员
     * */
    @GetMapping("/user/sales")
    public Result getSales() {
        List<SysUserBo> users =  sysAccountUserService.getSysAccountSales();
        return Result.success(users);
    }


    /**
     * 查询创建人
     * */
    @GetMapping("/user/k3key")
    public List<SysUserBo> getUserk3key() {
        List<SysUserBo> users =  sysAccountUserService.getSysAccountUser();
        return users;
    }

    /**
     * 新增物料分组
     * */
    @PostMapping("/materialgroup/add")
    public Result addMaterialGroup(@RequestBody DictionaryTable dictionaryTable) {

            // 设置默认值
            if (dictionaryTable != null) {
                // 检查是否已存在相同名称的物料分组
                int count = dictionaryTableMapper.selectByDictName(dictionaryTable.getDictName());
                if (count > 0) {
                    return Result.error("物料分组名称已存在");
                }

                // 新增物料分组
                int result = dictionaryTableMapper.insert(dictionaryTable);

                if (result > 0) {
                    return Result.success("新增物料分组成功");
                } else {
                    return Result.error("新增物料分组失败");
                }
            } else {
                return Result.error("参数不能为空");
            }

    }

    /**
     * 修改物料分组
     * */
    @PutMapping("/materialgroup/update")
    public Result updateMaterialGroup(@RequestBody DictionaryTable dictionaryTable) {
            if (dictionaryTable != null && dictionaryTable.getId() != null) {

                // 检查修改后的名称是否与其他已存在的物料分组冲突
                DictionaryTable existingTable = dictionaryTableMapper.selectById(dictionaryTable.getId());
                if (existingTable == null) {
                    return Result.error("要修改的物料分组不存在");
                }

                if (!dictionaryTable.getDictName().equals(existingTable.getDictName())) {
                    // 只有当名称发生改变时才检查冲突
                    DictionaryTable condition = new DictionaryTable();
                    condition.setDictName(dictionaryTable.getDictName());
                    DictionaryTable conflictTable = dictionaryTableMapper.selectByCondition(condition);
                    if (conflictTable != null && !conflictTable.getId().equals(dictionaryTable.getId())) {
                        return Result.error("物料分组名称已存在");
                    }
                }

                // 修改物料分组
                int result = dictionaryTableMapper.update(dictionaryTable);

                if (result > 0) {
                    return Result.success("修改物料分组成功");
                } else {
                    return Result.error("修改物料分组失败");
                }
            } else {
                return Result.error("参数不能为空");
            }

    }

    /**
     * 客户来源
     * */
    @GetMapping("/customer/all")
    public Result getByCustomer() {
        String categoryName = "客户来源";
        String category = "customer_source";
        List<BymaterialDictionary> bymaterialDictionaries=  dictionaryLookupServicel.categoryName(categoryName,category);
        bymaterialDictionaries.forEach(dictionary ->{
             dictionary.setFKhly(dictionary.getKingdee());
        });
        return Result.success(bymaterialDictionaries);
    }

    /**
    * 包装方式
    * */
    @GetMapping("/customer/packaging")
    public Result getByPackaging() {
        String categoryName = "包装方式";
        String category = "manner_packing";
        List<BymaterialDictionary> bymaterialDictionaries=  dictionaryLookupServicel.categoryName(categoryName,category);
        bymaterialDictionaries.forEach(dictionary ->{
            dictionary.setFBzfs(dictionary.getKingdee());
        });
        return Result.success(bymaterialDictionaries);
    }

    /**
     * 客户类别
     * */
    @GetMapping("/client")
    public Result getByclient() {
        String categoryName = "客户类别";
        String category = "customer_category";
        List<BymaterialDictionary> bymaterialDictionaries=  dictionaryLookupServicel.categoryName(categoryName,category);
        bymaterialDictionaries.forEach(dictionary ->{
            dictionary.setFcustTypeId(dictionary.getKingdee());
        });
        return Result.success(bymaterialDictionaries);
    }

    /**
    * 供应商
    * */
    @GetMapping("/supplier")
    public Result getsupplier() {
        List<SupplierVo> supplier=  dictionaryLookupServicel.listSuppliers();
        return Result.success(supplier);
    }

    /**
     * 价格类型
    * */
    @GetMapping("/pricetype")
    public Result getByPriceType() {
        String categoryName = "价格类型";
        String category = "price_type";
        List<BymaterialDictionary> bymaterialDictionaries=  dictionaryLookupServicel.categoryName(categoryName,category);
        bymaterialDictionaries.forEach(dictionary ->{
            dictionary.setFPriceType(dictionary.getKingdee());
        });
        return Result.success(bymaterialDictionaries);
    }

    /**
     * 物料
     * */
    @GetMapping("/material")
    public Result getmaterial() {
        List<BymaterialDictionary> materials = dictionaryLookupServicel.selectmaterial();
        return Result.success(materials);
    }

    /**
     * 计价单位
     * */
    @GetMapping("/pricingunit")
    public Result getByPricingUnit() {
        String categoryName = "基本单位";
        String category = "unit";
        List<BymaterialDictionary> bymaterialDictionaries=  dictionaryLookupServicel.categoryName(categoryName,category);
        bymaterialDictionaries.forEach(dictionary ->{
            dictionary.setFUnitID(dictionary.getCode());
            dictionary.setFWBZDW(dictionary.getCode());
        });
        return Result.success(bymaterialDictionaries);
    }



}
