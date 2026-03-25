package com.ruoyi.business.k3.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.ruoyi.business.entity.Country;
import com.ruoyi.business.entity.DictionaryTable;
import com.ruoyi.business.k3.service.DictionaryTableService;
import com.ruoyi.business.servicel.CountryService;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字典表控制器
 */
@RestController
@RequestMapping("/k3/dictionary")
@Slf4j
public class DictionarytableController {

    @Autowired
    private DictionaryTableService dictionaryTableService;
    
    @Autowired
    private CountryService countryService;

    /**
     * 新增字典项
     *
     * @param dictionaryTable 字典项信息
     * @return 操作结果
     */
    @PostMapping("/add")
    public R add(@RequestBody DictionaryTable dictionaryTable) {
        try {
            int result = dictionaryTableService.insert(dictionaryTable);
            if (result>=1) {
                return R.ok("新增成功");
            } else {
                return R.fail("新增失败");
            }
        } catch (Exception e) {
            return R.fail("新增异常：" + e.getMessage());
        }
    }

    /**
     * 修改字典项
     *
     * @param dictionaryTable 字典项信息
     * @return 操作结果
     */
    @PutMapping("/update")
    public R update(@RequestBody DictionaryTable dictionaryTable) {
        try {
            boolean result = dictionaryTableService.update(dictionaryTable);
            if (result) {
                return R.ok("修改成功");
            } else {
                return R.fail("修改失败");
            }
        } catch (Exception e) {
            return R.fail("修改异常：" + e.getMessage());
        }
    }

    /**
     * 删除字典项
     *
     * @param id 字典项ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    public R delete(@PathVariable Long id) {
        try {
            boolean result = dictionaryTableService.deleteById(id);
            if (result) {
                return R.ok("删除成功");
            } else {
                return R.fail("删除失败");
            }
        } catch (Exception e) {
            return R.fail("删除异常：" + e.getMessage());
        }
    }

    /**
     * 批量删除字典项
     *
     * @param ids 字典项ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batchDelete")
    public R batchDelete(@RequestBody List<Long> ids) {
        try {
            int count = dictionaryTableService.batchDelete(ids);
            return R.ok("删除成功" + count + "条记录");
        } catch (Exception e) {
            return R.fail("删除异常：" + e.getMessage());
        }
    }

    /**
     * 查询字典项详情
     *
     * @param id 字典项ID
     * @return 字典项信息
     */
    @GetMapping("/get/{id}")
    public R get(@PathVariable Long id) {
        try {
            DictionaryTable dictionaryTable = dictionaryTableService.selectById(id);
            return R.ok(dictionaryTable);
        } catch (Exception e) {
            return R.fail("查询异常：" + e.getMessage());
        }
    }

    /**
     * 查询所有字典项（树形结构）
     *
     * @return 字典项树形结构
     */
    @GetMapping("/listTree")
    @SaCheckPermission("/api/v1/k3/dictionary/listTree")
    public Result listTree() {
        try {
            List<DictionaryTable> tree = dictionaryTableService.selectDictionaryTree();
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("数据", tree);
            responseData.put("消息", "查询成功");
            System.out.println("-----------------tree-----------------");
            System.out.println(tree);
            return Result.success(responseData);
        } catch (Exception e) {
            return Result.error("查询异常：" + e.getMessage());

        }
    }

    /**
     * 根据字典类型查询字典项列表
     * 用于销售订单等页面的字典数据查询
     *
     * @param dictType 字典类型 (如：currency, payment_clause, collection_terms, nation 等)
     * @return 字典项列表，格式：[{value, label}]
     */
    @GetMapping("/listByType/{dictType}")
    public Result listByType(@PathVariable String dictType) {
        try {
            // 调用 Service 层查询字典项 (从 bymaterial_dictionary 表或 country 表)
            List<DictionaryTable> dictList = dictionaryTableService.selectByDictType(dictType);
            
            // 转换为前端需要的格式
            List<Map<String, Object>> resultList = new java.util.ArrayList<>();
            for (DictionaryTable dict : dictList) {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("value", dict.getDictCode());      // kingdee 字段作为 value
                map.put("label", dict.getRemark());        // name 字段作为 label
                map.put("dictName", dict.getDictName());   // category 字段
                map.put("dictCode", dict.getDictCode());   // kingdee 字段
                resultList.add(map);
            }
            
            log.info("查询字典 [{}] 成功，共 {} 条记录", dictType, resultList.size());
            return Result.success(resultList);
        } catch (Exception e) {
            log.error("查询字典 [{}] 失败：{}", dictType, e.getMessage(), e);
            return Result.error("查询字典失败：" + e.getMessage());
        }
    }

    /**
     * 模糊搜索国家字典
     * @param keyword 搜索关键词（支持中文、英文）
     * @return 匹配的国家列表
     */
    @GetMapping("/nation/search")
    public Result searchNation(@RequestParam String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return Result.error("搜索关键词不能为空");
            }
            
            List<Country> countries = countryService.searchNation(keyword);
            
            // 转换为前端需要的格式
            List<Map<String, Object>> resultList = new java.util.ArrayList<>();
            for (Country country : countries) {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("value", country.getId());
                map.put("label", country.getNameZh() + " (" + country.getNameEn() + ")");
                map.put("nameZh", country.getNameZh());
                map.put("nameEn", country.getNameEn());
                resultList.add(map);
            }
            
            log.info("搜索国家 [{}] 成功，找到 {} 条记录", keyword, resultList.size());
            return Result.success(resultList);
        } catch (Exception e) {
            log.error("搜索国家失败：{}", e.getMessage(), e);
            return Result.error("搜索国家失败：" + e.getMessage());
        }
    }
}
