package com.ruoyi.business.k3.service;


import com.ruoyi.business.entity.PriceList;
import com.ruoyi.business.k3.domain.bo.PriceListBo;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;

import java.util.List;

/**
 * 价目表服务接口
 */
public interface PriceListService {

    /**
     * 同步价目信息表
     * */
    void syncPriceList( List<List<Object>> PriceLarsList);
    /**
     * 同步价目明细表
     * */
    void syncPriceListEntry(List<List<Object>> PriceParticularsList);
    /**
     * 添加价目表（包含明细）
     * @param priceListDTO 价目表信息（含entries明细列表）
     * @return 操作结果
     */
    Result save(PriceList priceListDTO);

    /**
     * 更新价目表（包含明细）
     * @param priceListDTO 价目表信息
     * @return 操作结果
     */
    Result update(PriceList priceListDTO);

    /**
     * 根据ID删除价目表（级联删除明细）
     * @param id 价目表ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);

    /**
     * 根据编码删除价目表
     * @param FNumber 价目表编码
     * @return 是否删除成功
     */
    boolean deleteByNumber(String FNumber);

    /**
     * 根据ID查询价目表（包含明细）
     * @param id 价目表ID
     * @return 价目表信息
     */
    PriceList getById(Long id);

    /**
     * 根据编码查询价目表（包含明细）
     * @param id 价目表编码
     * @return 价目表信息
     */
    PriceList getByNumber(String id);

    /**
     * 分页查询价目表列表（支持条件查询）
     * @param priceList 查询条件
     * @param pageQuery 分页参数
     * @return 价目表分页列表
     */
    TableDataInfo<PriceList> list(PriceListBo priceList, PageQuery pageQuery);

    /**
     * 根据供应商ID查询价目表
     * @param supplierId 供应商ID
     * @return 价目表列表
     */
    List<PriceList> listBySupplierId(Long supplierId);

    /**
     * 根据供应商类别查询价目表
     * @param supplierCategory 供应商类别
     * @return 价目表列表
     */
    List<PriceList> listBySupplierCategory(Long supplierCategory);

    /**
     * 从金蝶系统同步采购价目表数据
     * @return 同步结果
     */
    Result syncFromKingdee();
}
