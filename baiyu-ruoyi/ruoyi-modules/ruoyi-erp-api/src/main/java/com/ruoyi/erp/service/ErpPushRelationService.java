package com.ruoyi.erp.service;

import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.erp.domain.bo.ErpPushRelationBo;
import com.ruoyi.erp.domain.vo.ErpPushRelationVo;

import java.util.List;

/**
 * ERP Push Relation Config Service Business Layer Interface
 * 
 * @author JMH
 * @date 2026-03-22
 */
public interface ErpPushRelationService {

    /**
     * Query push relation by ID
     *
     * @param relationId Relation ID
     * @return Push relation info
     */
    ErpPushRelationVo selectById(Long relationId);

    /**
     * Query push relation list
     *
     * @param bo Push relation params
     * @return Push relation collection
     */
    List<ErpPushRelationVo> selectList(ErpPushRelationBo bo);

    /**
     * Page query push relation list
     *
     * @param bo Push relation params
     * @param pageQuery Page params
     * @return Push relation page info
     */
    TableDataInfo<ErpPushRelationVo> selectPageList(ErpPushRelationBo bo, PageQuery pageQuery);

    /**
     * Insert push relation
     *
     * @param bo Push relation info
     * @return Result
     */
    int insertByBo(ErpPushRelationBo bo);

    /**
     * Update push relation
     *
     * @param bo Push relation info
     * @return Result
     */
    int updateByBo(ErpPushRelationBo bo);

    /**
     * Batch delete push relations
     *
     * @param relationIds Relation ID array
     * @return Result
     */
    int deleteByIds(Long[] relationIds);

    /**
     * Delete push relation
     *
     * @param relationId Relation ID
     * @return Result
     */
    int deleteById(Long relationId);

    /**
     * Get push relation config
     *
     * @param sourceModule Source module code
     * @param targetModule Target module code
     * @return Push relation config
     */
    ErpPushRelationVo getPushRelation(String sourceModule, String targetModule);
}
