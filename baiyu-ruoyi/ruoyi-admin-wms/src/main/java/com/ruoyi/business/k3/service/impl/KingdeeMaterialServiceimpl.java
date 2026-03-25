package com.ruoyi.business.k3.service.impl;

import cn.hutool.json.JSONObject;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lark.oapi.service.vc.v1.model.Material;
import com.ruoyi.business.Component.K3FormProcessorFactory;
import com.ruoyi.business.dto.PriceListDTO;
import com.ruoyi.business.emen.ProductCategoryEnum;
import com.ruoyi.business.entity.*;
import com.ruoyi.business.feishu.config.BatchGetEmployeeConfig;
import com.ruoyi.business.k3.config.AbstractK3FormProcessor;
import com.ruoyi.business.k3.config.k3config;
import com.ruoyi.business.k3.service.KingdeeMaterialServicer;
import com.ruoyi.business.mapper.*;
import com.ruoyi.business.util.*;
import com.ruoyi.business.vo.Bymaterials;
import com.ruoyi.common.core.domain.bo.LoginUser;
import com.ruoyi.common.satoken.utils.LoginHelper;
import com.ruoyi.system.domain.vo.SysUserVo;
import com.ruoyi.system.mapper.SysUserMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@Slf4j
public class KingdeeMaterialServiceimpl implements KingdeeMaterialServicer {

    @Resource
    private MaterialMapper materialMapper;

    @Autowired
    private MinioUtil minioUtil; // 添加MinioUtil依赖
    @Autowired
    private QwenModelUtil qwenModelUtil;

    @Autowired
    private PriceListMapper priceListMapper;
    @Autowired
    private PriceListEntryMapper priceListEntryMapper;
    @Autowired
    private K3FormProcessorFactory k3FormProcessorFactory;
    @Autowired
    private SysDataAuditLogMapper auditLogMapper;
    @Autowired
    private k3config k3configks;
    @Autowired
    private PromptTemplateUtil promptTemplateUtil;
    @Autowired
    private BymaterialDictionaryMapper bymaterialDictionaryMapper;

    @Autowired
    private DictionaryTableMapper dictionaryTableMapper;
    @Autowired
    private BatchGetEmployeeConfig batchGetEmployeeConfig;
    @Autowired
    private SysUserMapper userMapper;


    @Override
    public void queryMaterialList(List<List<Object>> materialList,List<List<Object>> productCategories) {

        List<Bymaterial> materials = new ArrayList<>();
        for (List<Object> rowData : materialList) {
            Bymaterial material = new Bymaterial();
                // 确保数据索引不越界
                if (rowData.size() <= 21) { // 增加索引数量以包含新字段
                    // 使用toString()方法而不是强制类型转换
                    material.setName(rowData.get(0) != null ? rowData.get(0).toString() : null);
                    material.setNumber(rowData.get(1) != null ? rowData.get(1).toString() : null);
                    material.setSpecification(rowData.get(2) != null ? rowData.get(2).toString() : null);
                    material.setDescription1(rowData.get(3) != null ? rowData.get(3).toString() : null);
                    material.setErpClsId(rowData.get(4) != null ? rowData.get(4).toString() : null);
                    material.setMaterialgroup(rowData.get(5) != null ? rowData.get(5).toString() : null);
                    material.setFxlcp(rowData.get(6) != null ? rowData.get(6).toString() : null);
                    material.setVolume(rowData.get(7) != null ? rowData.get(7).toString() : null);
                    material.setInspectionReport(rowData.get(8) != null ? rowData.get(8).toString() : null);
                    material.setHsbm(rowData.get(9) != null ? rowData.get(9).toString() : null);
                    material.setProductCategory(rowData.get(10) != null ? rowData.get(10).toString() : null);
                    material.setCreator(rowData.get(11) != null ? rowData.get(11).toString() : null);
                    material.setCreator_time(rowData.get(12) != null ? LocalDateTime.parse(rowData.get(12).toString()) : null);
                    material.setK3Id(rowData.get(13) != null ? rowData.get(13).toString() : null);
                    material.setFormerNumber(rowData.get(14) != null ? rowData.get(14).toString() : null);
                    material.setFstate(rowData.get(15) != null ? rowData.get(15).toString() : null);
                    material.setFstateName(rowData.get(16) != null ? rowData.get(16).toString() : null);
                    material.setFstateTime(rowData.get(17) != null ? rowData.get(17).toString() : null);
                    material.setBcfwgys(rowData.get(18) != null ? rowData.get(18).toString() : null);

                    materials.add(material);
                }
        }
        // 2. 数据处理阶段 - 使用线程池并行处理物料插入/更新
        ThreadPoolExecutor processingExecutor = ThreadPoolUtil.createFixedThreadPool("MaterialProcessing");
        List<Future<?>> processingFutures = new ArrayList<>();
        int batchSize = 1000;
        for (int i = 0; i < materials.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, materials.size());
            List<Bymaterial> batch = materials.subList(i, endIndex);

            Future<?> future = processingExecutor.submit(() -> {
                processBatchMaterials(batch);
            });
            processingFutures.add(future);
        }
        // 等待所有处理任务完成
        for (Future<?> procFuture : processingFutures) {
            try {
                procFuture.get();
            } catch (Exception e) {
                log.error("处理物料数据时发生错误", e);
            }
        }

        // 关闭处理线程池
        ThreadPoolUtil.shutdown(processingExecutor);
    }

    @Override
    public void queryMaterialList(List<List<Object>> productCategories) {
        for (List<Object> productCategory : productCategories) {
            Bymaterial bymaterial = new Bymaterial();
            bymaterial.setK3Id(productCategory.get(0) != null ? productCategory.get(0).toString() : null);
            bymaterial.setNumber(productCategory.get(1) != null ? productCategory.get(1).toString() : null);
           Bymaterial material = materialMapper.selectByNumber(bymaterial.getNumber());
           if (material != null){
               materialMapper.updateByNumber( bymaterial);
           }else {
               materialMapper.insert(bymaterial);
           }
        }
    }


    private void processBatchMaterials(List<Bymaterial> batch) {
        List<Bymaterial> toUpdate = new ArrayList<>();
        List<Bymaterial> toInsert = new ArrayList<>();

        // 分类需要更新和插入的数据
        for (Bymaterial material : batch) {
            Bymaterial existingMaterial = materialMapper.selectByNumber(material.getNumber());
            if (existingMaterial != null) {
                // 存在则更新 - 保留原有的非空字段，避免覆盖已有的former_number
                material.setModification_time(LocalDateTime.now());
                toUpdate.add(material);
            } else {
                // 不存在则插入
                material.setCreator_time(LocalDateTime.now());
                material.setModification_time(LocalDateTime.now());
                // 设置图片名称
                if (material.getNumber() != null) {
                    String imageName = material.getNumber().replace("/", "_");
                    material.setImageName(imageName);
                }
                toInsert.add(material);
            }
        }

        // 批量数据库操作 - 直接调用 Mapper 方法
        try {
            if (!toUpdate.isEmpty()) {
                log.info("批量更新物料数量: {}", toUpdate.size());
                for (Bymaterial material : toUpdate) {
                    materialMapper.updateByNumber(material);
                }
            }

            if (!toInsert.isEmpty()) {
                log.info("批量插入物料数量: {}", toInsert.size());
                for (Bymaterial material : toInsert) {
                    materialMapper.insert(material);
                }
            }
        } catch (Exception e) {
            log.error("批量处理物料数据失败", e);
        }
    }

    @Override
    public Result addMaterial(Bymaterial material) {
        String imageUrl = material.getImage(); // 保存图片URL用于可能的回滚操作
        //LoginUser loginUser = LoginHelper.getLoginUser();
        Long userId = LoginHelper.getUserId();
        SysUserVo user = userMapper.selectVoById(userId);
        material.setCreator(user.getK3Key());
        List<List<Object>> materialList = k3configks.queryMaterialList(0, 10, material.getNumber());

        try {
            for (List<Object> rowData : materialList) {
                material.setK3Id(rowData.get(13) != null ? rowData.get(13).toString() : null);
            }

            // 设置创建时间
            material.setCreator_time(LocalDateTime.now());
            material.setModification_time(LocalDateTime.now());

            material.setImageName(material.getNumber());
            String imageName = material.getNumber().replace("/", "_");
            material.setImageName(imageName);


            if (material.getMaterialgroup() != null && !material.getMaterialgroup().isEmpty()) {
                String bilhead = material.getMaterialgroup();
                DictionaryTable category = dictionaryTableMapper.selectByDictBilhead(bilhead);
                if (category != null) {
                    material.setMaterialgroup(category.getBilhead());
                }
            }

            if (material.getErpClsId() != null && !material.getErpClsId().isEmpty()) {
                // 物料属性
                String kingdee = material.getErpClsId();
                String category = "erpClsId_property";
                BymaterialDictionary bymaterialDictionary = bymaterialDictionaryMapper.selectCategoryIds(kingdee,category);
                if(bymaterialDictionary != null){
                    material.setErpClsId(bymaterialDictionary.getKingdee());
                }
            }
            if (material.getFxlcp() != null && !material.getFxlcp().isEmpty()) {
                // 新老产品
                String kingdee = material.getFxlcp();
                String category = "product_type";
                BymaterialDictionary bymaterialDictionary = bymaterialDictionaryMapper.selectCategoryIds(kingdee,category);
                if(bymaterialDictionary != null){
                    material.setFxlcp(bymaterialDictionary.getKingdee());
                }

            }
            if (material.getProductCategory() != null && !material.getProductCategory().isEmpty()) {
                // 产品类别
                String kingdee = material.getProductCategory();
                String category = "product_category";
                BymaterialDictionary bymaterialDictionary = bymaterialDictionaryMapper.selectCategoryIds(kingdee,category);
                if(bymaterialDictionary != null){
                    material.setProductCategory(bymaterialDictionary.getKingdee());
                }
            }

            // 处理旧物料编码字段
            if (material.getFormerNumber() == null) {
                material.setFormerNumber(""); // 如果为空，设置默认值
            }

            //启用状态
            material.setFstate("A");
            materialMapper.insert(material);
            // 处理物料对应供应商列表数据
            if (material.getPriceListDTOS() != null && !material.getPriceListDTOS().isEmpty()){
                processMaterialPriceList(material);
            }

            //飞书通知
            Map<String, String> fields = new HashMap<>();
            fields.put("物料名称名称", material.getName());
            fields.put("物料编码编码", material.getNumber());
            fields.put("提交人", user.getNickName());
            fields.put("状态", "推送成功");

            batchGetEmployeeConfig.sendCommonPushCard(
                "物料推送",
                fields,
                "http://113.46.194.126/k3cloud",
                "打开金蝶系统"
            );

            return Result.success("物料添加成功");
        } catch (Exception e) {
            log.error("添加物料失败", e);
            // 如果物料添加失败且有图片，删除已上传的图片
            if (imageUrl != null && !imageUrl.isEmpty()) {
                minioUtil.deleteFileByUrl(imageUrl);
                log.info("已删除因物料添加失败而上传的图片: {}", imageUrl);

            }
            return Result.success("物料添加失败: " + e.getMessage());
        }
    }
    /**
     * 处理物料价格列表数据
     * @param material 物料对象

     */
    private void processMaterialPriceList(Bymaterial material) {
        PriceList priceList = new PriceList();
        PriceListEntry priceListEntry = new PriceListEntry();
        for (PriceListDTO priceListDTO : material.getPriceListDTOS()) {
            priceList.setFName(priceListDTO.getFSupplierName());
            priceList.setFNumber(priceListDTO.getFNumber());
            priceList.setFCurrencyID(priceListDTO.getFCurrencyID());
            priceList.setFGYSLB(priceListDTO.getF_GYSLB());

            priceListMapper.insertPriceList(priceList);
            priceListEntry.setFPrice(priceListDTO.getFPrice());
            priceListEntry.setFMaterialId(material.getK3Id());
            priceListEntry.setFMaterialName(material.getName());
            priceListEntry.setSpecification(material.getSpecification());
            priceListEntry.setFTP1(priceListDTO.getF_TP1());
            priceListEntry.setFTaxPrice(priceListDTO.getFTaxPrice());
            priceListEntry.setFbzsm(priceListDTO.getF_bzsm());
            priceListEntry.setFDownPrice(priceListDTO.getFDownPrice());
            priceListEntry.setFUpPrice(priceListDTO.getFUpPrice());
            priceListEntry.setFBCGG(priceListDTO.getF_BCGG());
            priceListEntry.setFEntryEffectiveDate(priceListDTO.getFEntryEffectiveDate());
            priceListEntry.setFQDL(priceListDTO.getF_QDL());
            priceListEntry.setFdzshqdl(priceListDTO.getF_dzshqdl());
            priceListEntryMapper.insertEntry(priceListEntry);
        }
    }
    @Override
    public Result updateMaterial(Bymaterial material) {
        try {
           // LoginAccount loginAccount = SaTokenUtil.getLoginAccount();

           /*     if (material.getModification_time() == null || material.getModification_time().isEmpty()){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            material.setModification_time(LocalDateTime.now());

            }*/

            // 格式化日期字段
            material.setImageName(material.getNumber());
            //String imageName = material.getNumber().replace("/", "_");

            if (material.getMaterialgroup() != null && !material.getMaterialgroup().isEmpty()) {
                String bilhead = material.getMaterialgroup();
                DictionaryTable category = dictionaryTableMapper.selectByDictBilhead(bilhead);
                if (category != null) {
                    material.setMaterialgroup(category.getBilhead());
                }
            }

            if (material.getErpClsId() != null && !material.getErpClsId().isEmpty()) {
                // 物料属性
                String kingdee = material.getErpClsId();
                String category = "erpClsId_property";
                BymaterialDictionary bymaterialDictionary = bymaterialDictionaryMapper.selectCategoryIds(kingdee,category);
                if(bymaterialDictionary != null){
                    material.setErpClsId(bymaterialDictionary.getKingdee());
                }
            }
            if (material.getFxlcp() != null && !material.getFxlcp().isEmpty()) {
                // 新老产品
                String kingdee = material.getFxlcp();
                String category = "product_type";
                BymaterialDictionary bymaterialDictionary = bymaterialDictionaryMapper.selectCategoryIds(kingdee,category);
                if(bymaterialDictionary != null){
                    material.setFxlcp(bymaterialDictionary.getKingdee());
                }

            }
            if (material.getProductCategory() != null && !material.getProductCategory().isEmpty()) {
                // 产品类别
                String kingdee = material.getProductCategory();
                String category = "product_category";
                BymaterialDictionary bymaterialDictionary = bymaterialDictionaryMapper.selectCategoryIds(kingdee,category);
                if(bymaterialDictionary != null){
                    material.setProductCategory(bymaterialDictionary.getKingdee());
                }
            }

            // 处理旧物料编码字段
            if (material.getFormerNumber() == null) {
                material.setFormerNumber(""); // 如果为空，设置默认值
            }

          //  material.setModifier(loginAccount.getNickname());
            //material.setImageName(imageName);
            int result = materialMapper.updateByNumber(material);


            if (result > 0) {
                return Result.success("物料更新成功");
            } else {
                return Result.error("物料更新失败，未找到对应记录");
            }
        } catch (Exception e) {
            log.error("更新物料失败", e);
            return Result.error("物料更新失败: " + e.getMessage());
        }
    }


    @Override
    public Bymaterial getMaterialById(Long id) {
        try {
            Bymaterial material = materialMapper.selectById(id);
            // 新老产品
            if(material.getFxlcp() != null && !material.getFxlcp().trim().isEmpty()){
                String kingdee = material.getFxlcp();
                BymaterialDictionary materialDictionary = bymaterialDictionaryMapper.selectByKingdee(kingdee);
                material.setFxlcp(materialDictionary.getName());
            }

            // // 物料属性
            if(material.getErpClsId() != null && !material.getErpClsId().trim().isEmpty()){
                String kingdee = material.getErpClsId();
                String category = "erpClsId_property";
                BymaterialDictionary materialDictionary = bymaterialDictionaryMapper.selectCategoryIds(kingdee,category);
                material.setErpClsId(materialDictionary.getName());
            }

            //产品类别
            if(material.getProductCategory() != null && !material.getProductCategory().trim().isEmpty()){
                String kingdee = material.getProductCategory();
                BymaterialDictionary materialDictionary = bymaterialDictionaryMapper.selectByKingdes(kingdee);
                material.setProductCategory(materialDictionary.getName());
            }

            //物料分组
            if (material.getMaterialgroup() != null && !material.getMaterialgroup().trim().isEmpty()) {
                String bilhead = material.getMaterialgroup();
                DictionaryTable category = dictionaryTableMapper.selectByDictBilhead(bilhead);
                if (category != null) {
                    material.setMaterialgroup(category.getBilhead());
                }
            }
            if (material != null && material.getErpClsId() != null) {
                // 根据金蝶code查询枚举，获取dictName并设置
                ProductCategoryEnum category = ProductCategoryEnum.fromCode(material.getErpClsId());
                if (category != null) {
                    material.setErpClsId(category.getDictName());
                }
            }

            //查询物料对应供应商信息
            List<PriceListDTO> priceListDTOS = priceListMapper.selectByMaterialId(material.getK3Id());
            if (priceListDTOS != null && !priceListDTOS.isEmpty()){

            material.setPriceListDTOS(priceListDTOS);
            }
            return material; // 直接返回查询到的物料对象
        } catch (Exception e) {
            log.error("根据编码查询物料失败", e);
            return null; // 查询失败时返回null
        }
    }

    @Override
    public Page<Bymaterial> listMaterials(Bymaterial condition, int page, int size,String isAsc) {
        try {
            int offset = (page - 1) * size;
            /*查一遍字典 将查询字过一边字段找对应的数据*/
            if (condition.getFxlcp() != null){

                String kingdee = condition.getFxlcp();
                String category = "product_type";
                BymaterialDictionary bymaterialDictionary = bymaterialDictionaryMapper.selectCategoryIds(kingdee,category);
                BymaterialDictionary dictionary = bymaterialDictionary;
                if(dictionary != null){
                    condition.setFxlcp(dictionary.getKingdee());
                }
            }
            if (condition.getProductCategory() != null){
                String kingdee = condition.getProductCategory();
                String category = "product_category";
                BymaterialDictionary dictionarys = bymaterialDictionaryMapper.selectCategoryIds(kingdee,category);
                if (dictionarys != null){
                    condition.setProductCategory(dictionarys.getKingdee());
                }
            }
            if(condition.getErpClsId() != null){
                String kingdee = condition.getErpClsId();
                String category = "erpClsId_property";
                BymaterialDictionary dictionaryes = bymaterialDictionaryMapper.selectCategoryIds(kingdee,category);
                if (dictionaryes != null){
                    condition.setErpClsId(dictionaryes.getKingdee());
                }
            }

            List<Bymaterial> records = materialMapper.selectByCondition(offset, size,isAsc, condition);
            long total = materialMapper.countByCondition(condition);
            Page<Bymaterial> result = Page.of(page, size, total);
            result.setRecords(records);
            return result;
        } catch (Exception e) {
            log.error("查询物料列表失败", e);
            return Page.of(0, size);
        }
    }


    @Override
    public Result deleteMaterial(Long id) {
        try {
            // 根据编码删除物料
            int result = materialMapper.deleteByNumber(id);
            if (result > 0) {
                return Result.success("物料删除成功");
            } else {
                return Result.error("物料删除失败，未找到对应记录");
            }
        } catch (Exception e) {
            log.error("删除物料失败", e);
            return Result.error("物料删除失败: " + e.getMessage());
        }
    }

    /**
     * 查询审计日志（分页）
     */
    @Override
    public Page<SysDataAuditLog> getAuditLogsByTableAndId(String tableName, String rowId, int pageNum, int pageSize) {
        Page<SysDataAuditLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysDataAuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDataAuditLog::getTableName, tableName)
            .eq(SysDataAuditLog::getRowId, rowId)
            .orderByDesc(SysDataAuditLog::getOperateTime);

        Page<SysDataAuditLog> resultPage = auditLogMapper.selectPage(page, wrapper);

        // 在查询时转换字典值
        resultPage.getRecords().forEach(auditLog -> {
            if (auditLog.getDiffJson() != null && !auditLog.getDiffJson().isEmpty()) {
                try {
                    String convertedJson = convertDiffJsonDictValues(auditLog.getDiffJson());
                    auditLog.setDiffJson(convertedJson);
                } catch (Exception e) {
                    log.error("转换审计日志字典值失败", e);
                }
            }
        });

        return resultPage;
    }

    /**
     * 转换 diff_json 中的字典值
     */
    private String convertDiffJsonDictValues(String diffJson) {
        try {
            JSONObject diff = new JSONObject(diffJson);
            JSONObject convertedDiff = new JSONObject();

            // 获取交期红线对象
            JSONObject deliveryState = diff.getJSONObject("delivery_state");
            if (deliveryState != null){
                // 获取old
                String oldValue = deliveryState.getStr("old");
                if (oldValue != null){
                    if (oldValue.equals("1")){
                        deliveryState.put("old", "常规");
                    }else if (oldValue.equals("2")){
                        deliveryState.put("old", "低风险");
                    }else if (oldValue.equals("3")){
                        deliveryState.put("old", "中风险");
                    }else if (oldValue.equals("4")){
                        deliveryState.put("old", "高风险");
                    }
                }
                //new值
                String newValue = deliveryState.getStr("new");
                if (newValue != null){
                    if (newValue.equals("1")){
                        deliveryState.put("new", "常规");
                    }else if (newValue.equals("2")){
                        deliveryState.put("new", "低风险");
                    }else if (newValue.equals("3")){
                        deliveryState.put("new", "中风险");
                    }else if (newValue.equals("4")){
                        deliveryState.put("new", "高风险");
                    }
                }
            }
            // 获取物料状态对象
            JSONObject fState = diff.getJSONObject("f_state");
            if (fState != null){
                // 获取old
                String oldValue = fState.getStr("old");
                if(oldValue != null){
                    if (oldValue.equals("A")){
                        fState.put("old", "启用");
                    }else if (oldValue.equals("B")){
                        fState.put("old", "禁用");
                    }
                }
                // 获取new值
                String newValue = fState.getStr("new");
                if (newValue != null){
                    if (oldValue.equals("A")){
                        fState.put("new", "启用");
                    }else if (oldValue.equals("B")){
                        fState.put("new", "禁用");
                    }
                }
            }


            diff.forEach((field, changeObj) -> {
                if (changeObj instanceof JSONObject) {
                    JSONObject change = (JSONObject) changeObj;
                    Object oldVal = change.get("old");
                    Object newVal = change.get("new");

                    // 转换字典值
                    String convertedOld = convertDictValue(field, oldVal);
                    String convertedNew = convertDictValue(field, newVal);

                    JSONObject convertedChange = new JSONObject();
                    convertedChange.put("old", convertedOld);
                    convertedChange.put("new", convertedNew);
                    convertedDiff.put(field, convertedChange);
                } else {
                    convertedDiff.put(field, changeObj);
                }
            });

            return convertedDiff.toString();
        } catch (Exception e) {
            log.error("解析 diff_json 失败: {}", diffJson, e);
            return diffJson;
        }
    }

    /**
     * 转换字典值为中文名称
     */
    private String convertDictValue(String field, Object value) {
        if (value == null) {
            return null;
        }

        try {
            String valueStr = value.toString();
            if (valueStr.trim().isEmpty()) {
                return valueStr;
            }

            // 将字段名统一转换为小写进行比较
            String fieldLower = field.toLowerCase();

            // 根据字段名转换（不区分大小写）
            if ("erpclsid".equals(fieldLower)) {
                // 物料属性 - 优先尝试ID查询
                BymaterialDictionary erpDict = null;
                try {
                    // 先尝试作为数字ID查询
                    String id = valueStr;
                    erpDict = bymaterialDictionaryMapper.selectCategoryIds(id, "erpclsId_property");
                } catch (NumberFormatException e) {
                    // 不是数字，尝试通过kingdee值查询
                    erpDict = bymaterialDictionaryMapper.selectByKingds(valueStr);
                }
                return (erpDict != null && erpDict.getName() != null) ? erpDict.getName() : valueStr;

            } else if ("fxlcp".equals(fieldLower)) {
                // 新老产品
                BymaterialDictionary fxlcpDict = bymaterialDictionaryMapper.selectByKingdee(valueStr);
                return (fxlcpDict != null && fxlcpDict.getName() != null) ? fxlcpDict.getName() : valueStr;

            } else if ("product_category".equals(fieldLower) || "productcategory".equals(fieldLower)) {
                // 产品类别
                BymaterialDictionary productDict = bymaterialDictionaryMapper.selectByKingdes(valueStr);
                return (productDict != null && productDict.getName() != null) ? productDict.getName() : valueStr;

            } else if ("materialgroup".equals(fieldLower)) {
                // 物料分组
                DictionaryTable dictTable = dictionaryTableMapper.selectByDictBilhead(valueStr);
                return (dictTable != null && dictTable.getDictName() != null) ? dictTable.getDictName() : valueStr;
            }
        } catch (Exception e) {
            log.error("转换字典值失败: 字段={}, 值={}", field, value, e);
        }

        return value.toString();
    }

    @Override
    public Bymaterial getMaterialByNumberDirect(String number) {
        try {
            return materialMapper.selectByNumber(number);
        } catch (Exception e) {
            log.error("根据编码查询物料失败", e);
            return null;
        }
    }

    @Override
    public String fillEnglishDesc(Bymaterial materials) {
        String prompt =   promptTemplateUtil.generateMaterialDescriptionTranslationPrompt(materials.getName(), materials.getSpecification());

        try {
            String result =    qwenModelUtil.sendPrompt(prompt);
            return result;
        } catch (NoApiKeyException e) {
            log.error("调用Qwen模型失败：缺少API密钥", e);
        } catch (InputRequiredException e) {
            log.error("调用Qwen模型失败：输入内容不合法", e);
        } catch (Exception e) {
            log.error("调用Qwen模型时发生未知错误", e);
        }
        return null;


    }
    @Override
    public boolean isMaterialGroupExists(String materialGroup) {
        if (materialGroup == null || materialGroup.trim().isEmpty()) {
            return false; // 空值认为是有效的（可选字段）
        }
        try {
            // 查询物料分组字典项是否存在
            Pattern pattern = Pattern.compile("\\(([^)]+)\\)");
            Matcher matcher = pattern.matcher(materialGroup);
            String name = null;
            if (matcher.find()){
                DictionaryTable dictionaryTable = new DictionaryTable();
                dictionaryTable.setDictName(matcher.group(1));
                name = dictionaryTable.getDictName();
            }else {
                name = materialGroup;
            }
            int count = dictionaryTableMapper.selectByDictName(name);
            return count > 0;
        } catch (Exception e) {
            log.error("校验物料属性失败: " + materialGroup, e);
            return false;
        }
    }

    @Override
    public boolean isErpClsIdExists(String erpClsId) {
        if (erpClsId == null || erpClsId.trim().isEmpty()) {
            return false; // 空值认为是有效的（可选字段）
        }
        try {
            // 查询物料属性字典项是否存在
            String name = erpClsId;
            int count = bymaterialDictionaryMapper.countByCategoryAndName("erpClsId_property", name);
            return count > 0;
        } catch (Exception e) {
            log.error("校验物料属性失败: " + erpClsId, e);
            return false;
        }
    }

    @Override
    public boolean isProductCategoryExists(String productCategory) {
        if (productCategory == null || productCategory.trim().isEmpty()) {
            return false; // 空值认为是有效的（可选字段）
        }
        try {
            // 查询产品类别字典项是否存在
            int count = bymaterialDictionaryMapper.countByCategoryAndName("product_category", productCategory);
            return count > 0;
        } catch (Exception e) {
            log.error("校验产品类别失败: " + productCategory, e);
            return false;
        }
    }

    @Override
    public boolean isNewProductFlagValid(String isNewProduct) {
      /*  if (isNewProduct == null || isNewProduct.trim().isEmpty()) {
            return true; // 空值认为是有效的（可选字段）
        }*/
/*
        // 定义有效的新老产品标识集合
        Set<String> validFlags = new HashSet<>();
        validFlags.add("新产品");
        validFlags.add("老产品");
        validFlags.add("NEW");
        validFlags.add("OLD");
        validFlags.add("新");
        validFlags.add("老");*/

       /* return validFlags.contains(isNewProduct);*/
        if (isNewProduct == null || isNewProduct.trim().isEmpty()) {
            return false; // 空值认为是有效的（可选字段）
        }
        try {
            // 查询产品类别字典项是否存在
            String name = isNewProduct;
            int count = bymaterialDictionaryMapper.countByCategoryAndName("product_type",name);
            return count > 0;
        } catch (Exception e) {
            log.error("校验新老产品失败: " + isNewProduct, e);
            return false;
        }


    }

    @Override
    public ImportResult processSingleDataRow(Map<String, Object> data, int index) {
        try {
            // 1. 基础字段非空校验
            String number = StringUtils.getStringValue(data.get("编码*"));

            Bymaterial existingMaterial = getMaterialByNumberDirect(number);
            Bymaterial material = buildMaterialFromData(data);

            // 4. 执行导入操作
            Result result;
            if (existingMaterial != null) {
                // 更新物料

                result = updateMaterial(material);

                if (!result.isSuccess()) {
                    log.error("更新物料失败：物料编码[" + number + "]，错误信息：" + result.failMessage());
                }
                return new ImportResult(number, "更新成功", result.isSuccess());
            } else {
                // 新增物料
                result = addMaterial(material);
                if (!result.isSuccess()) {
                    log.error("新增物料失败：物料编码[" + number + "]，错误信息：" + result.failMessage());
                }
                return new ImportResult(number, "新增成功", result.isSuccess());
            }
        } catch (Exception e) {
            String errorNumber = "未知编码";
            if (data != null && data.get("编码*") != null) {
                errorNumber = StringUtils.getStringValue(data.get("编码*"));
            }
            log.error("处理物料失败: " + errorNumber + "，行号: " + (index + 1), e);
            return new ImportResult(errorNumber, "处理失败: " + e.getMessage(), false);
        }
    }

    /**
     * 交付红线
     * */
    @Override
    public Result updateMaterials(Long[] ids, String state) {
        Integer success = null;
        for (Long id :ids){
            Bymaterial bymaterial = new Bymaterial();
            bymaterial.setId(id);
            bymaterial.setDeliveryState(state);
            success = materialMapper.updateBymaterial(bymaterial);
        }
        return Result.success(success);
    }


    /**
     * 根据Excel数据构建物料对象
     */
    private Bymaterial buildMaterialFromData(Map<String, Object> data) {
        Bymaterial material = new Bymaterial();

        material.setNumber((String) data.get("编码*"));
        material.setName((String) data.get("名称"));
        material.setSpecification((String) data.get("规格型号"));
        material.setImage((String) data.get("图片地址"));
        material.setDescription1((String) data.get("原英文描述"));
        material.setDescription2((String) data.get("英文描述"));
        material.setMaterialgroup((String) data.get("物料分组*"));
        material.setFormerNumber((String) data.get("旧物料编码")); // 新增旧物料编码字段
        material.setEnglishProductName((String) data.get("英文品名"));

        // 按点号分割，然后取前两部分
        String[] parts = material.getNumber().split("\\.");
        String dictCode = null;
        if (parts.length >= 2){
            dictCode = parts[0] + "." + parts[1];
        }else if(parts.length == 1){
            dictCode= parts[0];
        }else {
            dictCode = parts[0];
        }

        if (material.getMaterialgroup() != null && !material.getMaterialgroup().trim().isEmpty()){
            Pattern pattern = Pattern.compile("\\(([^)]+)\\)");
            Matcher matcher = pattern.matcher(material.getMaterialgroup());
            if (matcher.find()){
                DictionaryTable dictionaryTable = new DictionaryTable();
                dictionaryTable.setDictName(matcher.group(1));
                DictionaryTable dictionary = dictionaryTableMapper.selectByCondition(dictionaryTable);
                material.setMaterialgroup(dictionary.getBilhead());
            } else if (dictCode.equals("2.10")) {
                DictionaryTable dictionaryTable = new DictionaryTable();
                dictionaryTable.setDictCode(dictCode);
                DictionaryTable dictionary = dictionaryTableMapper.selectByCondition(dictionaryTable);
                material.setMaterialgroup(dictionary.getBilhead());
            } else if(material.getMaterialgroup().equals("产品")){
                DictionaryTable dictionaryTable = new DictionaryTable();
                dictionaryTable.setDictName(material.getMaterialgroup());
                dictionaryTable.setBilhead("150763");
                DictionaryTable dictionary = dictionaryTableMapper.selectByCondition(dictionaryTable);
                material.setMaterialgroup(dictionary.getBilhead());
            } else {
                DictionaryTable dictionaryTable = new DictionaryTable();
                dictionaryTable.setDictName(material.getMaterialgroup());
                DictionaryTable dictionary = dictionaryTableMapper.selectByCondition(dictionaryTable);
                material.setMaterialgroup(dictionary.getBilhead());
            }
        }

        material.setErpName((String) data.get("物料属性*"));
        if (material.getErpName() != null && !material.getErpName().trim().isEmpty()){
            BymaterialDictionary dictionary = bymaterialDictionaryMapper.selectByCategoryerpName(material.getErpName());
            if (dictionary != null){
                material.setErpClsId(String.valueOf(dictionary.getId()));
            }

        }

        material.setDeliveryState((String) data.get("交期红线"));
        if (material.getDeliveryState() != null && !material.getDeliveryState().trim().isEmpty()){
            if (material.getDeliveryState().equals("常规")){
                material.setDeliveryState("1");
            }
            if (material.getDeliveryState().equals("低风险")){
                material.setDeliveryState("2");
            }
            if (material.getDeliveryState().equals("中风险")){
                material.setDeliveryState("3");
            }
            if (material.getDeliveryState().equals("高风险")){
                material.setDeliveryState("4");
            }
        }

        material.setCreator((String) data.get("创建人"));
        material.setCreator_time((LocalDateTime) data.get("创建日期"));



        material.setHsbm((String) data.get("HS编码"));
      /*  material.setInspectionReport((String) data.get("验货报告"));*/

        material.setProductName((String) data.get("产品类别*"));
        if (material.getProductName() != null && !material.getProductName().trim().isEmpty()){
            BymaterialDictionary dictionary = bymaterialDictionaryMapper.selectByCategoryproductName(material.getProductName());
            if (dictionary != null){
                material.setProductCategory(String.valueOf(dictionary.getId()));
            }
        }

        material.setFxlcpName((String) data.get("新老产品"));
        if (material.getFxlcpName() != null && !material.getFxlcpName().trim().isEmpty()){
            BymaterialDictionary dictionary = bymaterialDictionaryMapper.selectByCategoryfxlcpName(material.getFxlcpName());
            if (dictionary != null){
                material.setFxlcp(String.valueOf(dictionary.getId()));
            }
        }
        return material;
    }


}
