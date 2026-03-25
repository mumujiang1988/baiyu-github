package com.ruoyi.business.k3.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.ruoyi.business.entity.Customer;
import com.ruoyi.business.entity.PriceList;
import com.ruoyi.business.k3.config.k3config;
import com.ruoyi.business.k3.domain.bo.PriceListBo;
import com.ruoyi.business.k3.service.PriceListService;
import com.ruoyi.business.util.MinioUtil;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import com.ruoyi.common.web.core.BaseController;
import jakarta.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

/**
 * 采购价目表管理控制器
 */
@RestController
@RequestMapping("/k3/pricelist")
public class PriceListController extends BaseController {

    @Resource
    private PriceListService priceListService;
    @Resource
    private k3config k3configks;

    @Autowired
    private MinioUtil minioUtil;

    /**
     * 同步金蝶采购价目表信息
     */
    @PostMapping("/login")
    @Transactional(rollbackFor = Exception.class)  // 添加类级别的事务支持
    public Result login() {
        //获取采购价目主表数据
        List<List<Object>> PriceLarsList =  k3configks.PriceLarsList() ;
        priceListService.syncPriceList(PriceLarsList);
        //获取采购价目明细表
        List<List<Object>> PriceParticularsList =  k3configks.PriceParticularsList() ;
        priceListService.syncPriceListEntry(PriceParticularsList);
        return Result.success();
    }

    /**
     * 添加价目表（包含明细）
     * @param priceList 价目表信息（包含entries明细列表）
     * @return 操作结果
     */
    @SaCheckPermission("k3:pricelist:save")
    @PostMapping(value = "/save",produces = "application/json;charset=UTF-8")
    @Transactional(rollbackFor = Exception.class)
    public Result save(
        @RequestPart("priceList") PriceList priceList,
        @RequestPart(value = "ftp1File", required = false) MultipartFile ftp1File) {
        try {
            //1.如果有工厂图片，先上传到MinIO（本地备份）
            if (ftp1File != null && !ftp1File.isEmpty()) {
                String businessLicenseUrl = minioUtil.uploadFile(ftp1File);
                priceList.getEntries().forEach(en ->{
                    en.setFTP1(businessLicenseUrl);
                });
            }
            return priceListService.save(priceList);
        } catch (Exception e) {
            return Result.error("添加价目表失败: " + e.getMessage());
        }
    }

    /**
     * 更新价目表信息（包含明细）
     * @param priceList 价目表信息
     * @return 操作结果
     */
    @SaCheckPermission("k3:pricelist:update")
    @PostMapping(value = "/update",produces = "application/json;charset=UTF-8")
    @Transactional(rollbackFor = Exception.class)
    public Result update(
        @RequestPart("priceList") PriceList priceList,
        @RequestPart(value = "ftp1File", required = false) MultipartFile ftp1File) {
        try {
            //1.如果有工厂图片，先上传到MinIO（本地备份）
            if (ftp1File != null && !ftp1File.isEmpty()){
                String businessLicenseUrl = minioUtil.uploadFile(ftp1File);
                priceList.getEntries().forEach(en ->{
                    en.setFTP1(businessLicenseUrl);
                });
            }
            return priceListService.update(priceList);
        } catch (Exception e) {
            return Result.error("更新价目表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID删除价目表（级联删除明细）
     *
     * @param id 价目表ID
     * @return 操作结果
     */
    @SaCheckPermission("k3:pricelist:delete")
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Long id) {
        try {
            boolean result = priceListService.deleteById(id);
            if (result) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error("删除价目表失败: " + e.getMessage());
        }
    }

    /**
     * 根据编码删除价目表
     *
     * @param FNumber 价目表编码
     * @return 操作结果
     */
    @DeleteMapping("/delete/number/{FNumber}")
    @SaCheckPermission("/api/v1/pricelist/delete/number/{FNumber}")
    public Result deleteByNumber(@PathVariable String FNumber) {
        try {
            boolean result = priceListService.deleteByNumber(FNumber);
            if (result) {
                return Result.success("删除成功");
            } else {
                return Result.error("价目表不存在或删除失败");
            }
        } catch (Exception e) {
            return Result.error("删除价目表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询价目表（包含明细）
     *
     * @param id 价目表ID
     * @return 价目表信息
     */
    @GetMapping("/get/{id}")
    @SaCheckPermission("/api/v1/pricelist/get/{id}")
    public Result getById(@PathVariable Long id) {
        try {
            PriceList priceList = priceListService.getById(id);
            if (priceList != null) {
                return Result.success(priceList);
            } else {
                return Result.error("未找到该价目表");
            }
        } catch (Exception e) {
            return Result.error("查询价目表失败: " + e.getMessage());
        }
    }

    /**
     * 根据编码查询价目表（包含明细）
     *
     * @param id 价目表编码
     * @return 价目表信息
     */
    @SaCheckPermission("k3:pricelist:query")
    @GetMapping("/query")
    public Result getByNumber(@RequestParam("id") String id) {
        try {
            PriceList priceList = priceListService.getByNumber(id);
            if (priceList != null) {
                return Result.success(priceList);
            } else {
                return Result.error("未找到该价目表");
            }
        } catch (Exception e) {
            return Result.error("查询价目表失败: " + e.getMessage());
        }
    }

    /**
     * 查询价目表列表（支持条件查询）
     *
     * @param priceList 查询条件
     * @param pageQuery 分页参数
     * @return 价目表列表
     */
    @SaCheckPermission("k3:pricelist:list")
    @GetMapping("/list")
    public TableDataInfo<PriceList> list(PriceListBo priceList, PageQuery pageQuery) {
        return priceListService.list(priceList, pageQuery);
    }

    /**
     * 根据供应商ID查询价目表
     *
     * @param supplierId 供应商ID
     * @return 价目表列表
     */
    @GetMapping("/list/supplier/{supplierId}")
    public Result listBySupplierId(@PathVariable Long supplierId) {
        try {
            List<PriceList> priceLists = priceListService.listBySupplierId(supplierId);
            return Result.success(priceLists);
        } catch (Exception e) {
            return Result.error("查询价目表失败: " + e.getMessage());
        }
    }

}
