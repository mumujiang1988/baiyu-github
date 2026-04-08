package com.ruoyi.business.k3.service;

import com.ruoyi.business.dto.SalesPriceDto;
import com.ruoyi.business.dto.SalesPriceItemDto;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.entity.SaleChangeBillFlat;
import com.ruoyi.business.entity.SalesPrice;
import com.ruoyi.business.k3.domain.bo.SalesPricesBo;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;

import java.util.List;

public interface SalesPriceService {

    /** 查询完整价目 */
    SalesPrice getDetail(Long id);

    /** 保存价目（主表 + 明细 + 包装） */
    void save(SalesPrice price);
    /**
     * 同步金蝶销售价目表变更表数据
     * @param querySalesPriceAlterationList 变更表数据
     * @return 是否同步成功
     */
    boolean syncSaleChangeBillData(List<List<Object>> querySalesPriceAlterationList);

    /**
     * 根据ID查询销售价目表变更表详情
     * @param id 主键ID
     * @return 销售价目表变更表详情
     */
    SaleChangeBillFlat selectById(Long id);
    /**
     * 同步金蝶销售价目表数据
     * @param querySalesPriceList 主表数据
     * @param querySalesPriceItemList 物料明细表数据
     * @param querySalesPriceItemPackageList 包材明细表数据
     * @return 是否同步成功
     */
    boolean syncSalesPriceData(List<List<Object>> querySalesPriceList,
                               List<List<Object>> querySalesPriceItemList,
                               List<List<Object>> querySalesPriceItemPackageList);

    /**
     * 分页查询价目列表
     * @param pageQuery 查询条件

     * @return 分页数据
     */
    TableDataInfo<SalesPrice> listSalesPrices(SalesPricesBo price, PageQuery pageQuery);

    /**
     * 新增销售价目表变更表
     * @param billFlat 销售价目表变更表（包含表头和明细）
     * @return 是否新增成功
     */
    Result addSaleChangeBill(SaleChangeBillFlat billFlat);

    /**
     * 修改销售价目表变更表
     * @param billFlat 销售价目表变更表（包含表头和明细）
     * @return 是否修改成功
     */
    boolean updateSaleChangeBill(SaleChangeBillFlat billFlat);
}
