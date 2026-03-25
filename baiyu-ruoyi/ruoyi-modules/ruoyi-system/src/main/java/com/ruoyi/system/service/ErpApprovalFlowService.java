package com.ruoyi.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.system.domain.bo.ErpApprovalFlowBo;
import com.ruoyi.system.domain.vo.ErpApprovalFlowVo;

import java.util.List;

/**
 * ERP 审批流程配置 Service 业务层接口
 * 
 * @author ERP Development Team
 * @date 2026-03-22
 */
public interface ErpApprovalFlowService {

    /**
     * 根据 ID 查询审批流程
     *
     * @param flowId 流程 ID
     * @return 审批流程信息
     */
    ErpApprovalFlowVo selectById(Long flowId);

    /**
     * 查询审批流程列表
     *
     * @param bo 审批流程参数
     * @return 审批流程集合
     */
    List<ErpApprovalFlowVo> selectList(ErpApprovalFlowBo bo);

    /**
     * 分页查询审批流程列表
     *
     * @param bo 审批流程参数
     * @param pageQuery 分页参数
     * @return 审批流程分页信息
     */
    Page<ErpApprovalFlowVo> selectPageList(ErpApprovalFlowBo bo, PageQuery pageQuery);

    /**
     * 新增审批流程
     *
     * @param bo 审批流程信息
     * @return 结果
     */
    int insertByBo(ErpApprovalFlowBo bo);

    /**
     * 修改审批流程
     *
     * @param bo 审批流程信息
     * @return 结果
     */
    int updateByBo(ErpApprovalFlowBo bo);

    /**
     * 批量删除审批流程
     *
     * @param flowIds 需要删除的流程 ID 数组
     * @return 结果
     */
    int deleteByIds(Long[] flowIds);

    /**
     * 删除审批流程
     *
     * @param flowId 流程 ID
     * @return 结果
     */
    int deleteById(Long flowId);

    /**
     * 获取审批流程配置
     *
     * @param moduleCode 模块编码
     * @return 审批流程配置
     */
    ErpApprovalFlowVo getApprovalFlow(String moduleCode);
}
