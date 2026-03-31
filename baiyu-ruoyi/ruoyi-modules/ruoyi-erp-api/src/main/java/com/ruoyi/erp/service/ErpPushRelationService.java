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
     */
    ErpPushRelationVo selectById(Long relationId);

    /**
     * Query push relation list
     */
    List<ErpPushRelationVo> selectList(ErpPushRelationBo bo);

    /**
     * Page query push relation list
     */
    TableDataInfo<ErpPushRelationVo> selectPageList(ErpPushRelationBo bo, PageQuery pageQuery);

    /**
     * Insert push relation
     */
    int insertByBo(ErpPushRelationBo bo);

    /**
     * Update push relation
     */
    int updateByBo(ErpPushRelationBo bo);

    /**
     * Batch delete push relations
     */
    int deleteByIds(Long[] relationIds);

    /**
     * Delete push relation
     */
    int deleteById(Long relationId);

    /**
     * Get push relation config
     */
    ErpPushRelationVo getPushRelation(String sourceModule, String targetModule);
}
