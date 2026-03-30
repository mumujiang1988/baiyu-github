package com.ruoyi.business.k3.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.ruoyi.business.entity.DictionaryTable;
import com.ruoyi.business.k3.service.DictionaryTableService;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.core.domain.R;
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
public class DictionarytableController {

    @Autowired
    private DictionaryTableService dictionaryTableService;

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
            return Result.error("查询异常: " + e.getMessage());

        }
    }
}
