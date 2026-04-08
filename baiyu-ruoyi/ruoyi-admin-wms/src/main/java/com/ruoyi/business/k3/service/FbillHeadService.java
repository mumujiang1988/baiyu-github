package com.ruoyi.business.k3.service;

import com.ruoyi.business.k3.domain.bo.FbillHeadBo;
import com.ruoyi.business.k3.domain.entity.FbillHead;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;

import java.util.List;

public interface FbillHeadService {



    /**
     * 同步金蝶采购调价主表信息
     * */
    void syncBillHeadList( List<List<Object>> billHeadList);


    /**
     * 同步金蝶采购调价明细表信息
     * */
    void syncFpurPatentry( List<List<Object>> fpurpatentryList);


    /**
     * 分页查询采购调价表列表（支持条件查询）
     * @param billHead 查询条件
     * @param pageQuery 分页参数
     * @return 采购调价表分页列表
     */
    TableDataInfo<FbillHead> list(FbillHeadBo billHead, PageQuery pageQuery);


    /**
     * 根据ID查采购调价询表（包含明细）
     * @param id 采购调价表ID
     * @return 采购调价表信息
     */
    FbillHead getById(Long id);


    /**
     * 根据ID删除采购调价表（级联删除明细）
     * @param id 价目表ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);

}
