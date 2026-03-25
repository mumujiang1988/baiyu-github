package com.ruoyi.business.k3.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.entity.SaleChangeBillFlat;
import com.ruoyi.business.entity.SalesPrice;
import com.ruoyi.business.k3.config.Dictionaryconfig;
import com.ruoyi.business.k3.service.SalesPriceService;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**销售价目
 * */
@RestController
@RequestMapping("/k3/salesPrice")
public class SalesPriceController {

    @Autowired
    private SalesPriceService salesPriceService;



    @Autowired
    private Dictionaryconfig dictionaryconfig;

    /** 查询价目详情 */
    @GetMapping("/{id}")
    public SalesPrice getDetail(@PathVariable Long id) {
        return salesPriceService.getDetail(id);
    }

    /** 新增价目 */
    @PostMapping("/save")
    public String save(@RequestBody SalesPrice price) {
        salesPriceService.save(price);
        return "success";
    }

    /**
     * 同步金蝶销售价目表数据
     */
    @SaCheckPermission("k3:salesPrice:sync")
    @PostMapping("/sync")
    @Transactional(rollbackFor = Exception.class)
    public Result syncSalesPriceData() {
        int pageSize = 10000;

        // 1. 获取销售价目表主表数据
        List<List<Object>> allSalesPriceList = new ArrayList<>();
        int startRow = 0;
        while (true) {
            List<List<Object>> pageData = dictionaryconfig.querySalesPriceList(startRow, pageSize);
            if (pageData == null || pageData.isEmpty()) {
                break;
            }
            allSalesPriceList.addAll(pageData);
            if (pageData.size() < pageSize) {
                break;
            }
            startRow += pageSize;
        }

        // 2. 获取销售价目表物料明细表数据
        List<List<Object>> allSalesPriceItemList = new ArrayList<>();
        startRow = 0;
        while (true) {
            List<List<Object>> pageData = dictionaryconfig.querySalesPriceItemList(startRow, pageSize);
            if (pageData == null || pageData.isEmpty()) {
                break;
            }
            allSalesPriceItemList.addAll(pageData);
            if (pageData.size() < pageSize) {
                break;
            }
            startRow += pageSize;
        }

        // 3. 获取销售价目表包材明细表数据
        List<List<Object>> allSalesPriceItemPackageList = new ArrayList<>();
        startRow = 0;
        while (true) {
            List<List<Object>> pageData = dictionaryconfig.querySalesPriceItemPackageList(startRow, pageSize);
            if (pageData == null || pageData.isEmpty()) {
                break;
            }
            allSalesPriceItemPackageList.addAll(pageData);
            if (pageData.size() < pageSize) {
                break;
            }
            startRow += pageSize;
        }

        // 4. 调用服务层同步数据
        salesPriceService.syncSalesPriceData(allSalesPriceList, allSalesPriceItemList, allSalesPriceItemPackageList);

        return Result.create(true);
    }

    /**
     * 分页查询价目列表
     * @param price 查询条件
     * @param pageQuery 分页参数
     * @return 分页数据
     */
    @SaCheckPermission("k3:salesPrice:list")
    @GetMapping("/page")
    public Page<SalesPrice> list(SalesPrice price, PageQuery pageQuery) {
        int pageSize = pageQuery.getPageSize();
        int pageNumber = pageQuery.getPageNum();
        return salesPriceService.listSalesPrices(price, pageNumber, pageSize);
    }

    /**
     * 同步金蝶销售价目表变更表数据
     */
    @SaCheckPermission("k3:salesPriceAlteration:sync")
    @PostMapping("/syncAlteration")
    @Transactional(rollbackFor = Exception.class)
    public Result syncSaleChangeBillData() {
        int pageSize = 10000;

        // 1. 获取销售价目表变更表数据
        List<List<Object>> allAlterationList = new ArrayList<>();
        int startRow = 0;
        while (true) {
            List<List<Object>> pageData = dictionaryconfig.querySalesPriceAlterationList(startRow, pageSize);
            if (pageData == null || pageData.isEmpty()) {
                break;
            }
            allAlterationList.addAll(pageData);
            if (pageData.size() < pageSize) {
                break;
            }
            startRow += pageSize;
        }

        // 2. 调用服务层同步数据
        salesPriceService.syncSaleChangeBillData(allAlterationList);

        return Result.create(true);
    }

    /**
     * 根据ID查询销售价目表变更表详情
     */
    @SaCheckPermission("k3:salesPriceAlteration:query")
    @GetMapping("/alteration/{id}")
    public SaleChangeBillFlat getAlterationDetail(@PathVariable Long id) {
        return salesPriceService.selectById(id);
    }

    /**
     * 新增销售价目表变更表
     */
    @SaCheckPermission("k3:salesPriceAlteration:add")
    @PostMapping("/alteration/add")
    public Result addAlteration(@RequestBody SaleChangeBillFlat billFlat) {
        return salesPriceService.addSaleChangeBill(billFlat);

    }

    /**
     * 修改销售价目表变更表
     */
    @SaCheckPermission("k3:salesPriceAlteration:edit")
    @PutMapping("/alteration/edit")
    public Result updateAlteration(@RequestBody SaleChangeBillFlat billFlat) {
        boolean success = salesPriceService.updateSaleChangeBill(billFlat);
        return Result.create(success);
    }
}
