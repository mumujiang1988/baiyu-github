package com.ruoyi.erp.service;

import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.erp.domain.bo.ErpApprovalFlowBo;
import com.ruoyi.erp.domain.vo.ErpApprovalFlowVo;

import java.util.List;

/**
 * ERP Approval Flow Config Service Business Layer Interface
 * 
 * @author JMH
 * @date 2026-03-22
 */
public interface ErpApprovalFlowService {

    /**
     * Query approval flow by ID
     *
     * @param flowId Flow ID
     * @return Approval flow info
     */
    ErpApprovalFlowVo selectById(Long flowId);

    /**
     * Query approval flow list
     *
     * @param bo Approval flow params
     * @return Approval flow collection
     */
    List<ErpApprovalFlowVo> selectList(ErpApprovalFlowBo bo);

    /**
     * Page query approval flow list
     *
     * @param bo Approval flow params
     * @param pageQuery Page params
     * @return Approval flow page info
     */
    TableDataInfo<ErpApprovalFlowVo> selectPageList(ErpApprovalFlowBo bo, PageQuery pageQuery);

    /**
     * Insert approval flow
     *
     * @param bo Approval flow info
     * @return Result
     */
    int insertByBo(ErpApprovalFlowBo bo);

    /**
     * Update approval flow
     *
     * @param bo Approval flow info
     * @return Result
     */
    int updateByBo(ErpApprovalFlowBo bo);

    /**
     * Batch delete approval flows
     *
     * @param flowIds Flow ID array
     * @return Result
     */
    int deleteByIds(Long[] flowIds);

    /**
     * Delete approval flow
     *
     * @param flowId Flow ID
     * @return Result
     */
    int deleteById(Long flowId);

    /**
     * Get approval flow config
     *
     * @param moduleCode Module code
     * @return Approval flow config
     */
    ErpApprovalFlowVo getApprovalFlow(String moduleCode);
}
