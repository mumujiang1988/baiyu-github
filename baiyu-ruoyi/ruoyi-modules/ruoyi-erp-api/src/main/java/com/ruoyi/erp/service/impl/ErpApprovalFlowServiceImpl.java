package com.ruoyi.erp.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.MapstructUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.erp.domain.bo.ErpApprovalFlowBo;
import com.ruoyi.erp.domain.entity.ErpApprovalFlow;
import com.ruoyi.erp.domain.vo.ErpApprovalFlowVo;
import com.ruoyi.erp.mapper.ErpApprovalFlowMapper;
import com.ruoyi.erp.service.ErpApprovalFlowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * ERP 审批流程配置 Service 业务层实现
 * 
 * @author JMH
 * @date 2026-03-22
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ErpApprovalFlowServiceImpl implements ErpApprovalFlowService {

    private final ErpApprovalFlowMapper approvalFlowMapper;

    @Override
    public ErpApprovalFlowVo selectById(Long flowId) {
        return approvalFlowMapper.selectVoById(flowId);
    }

    @Override
    public List<ErpApprovalFlowVo> selectList(ErpApprovalFlowBo bo) {
        LambdaQueryWrapper<ErpApprovalFlow> lqw = buildQueryWrapper(bo);
        return approvalFlowMapper.selectVoList(lqw);
    }

    @Override
    public Page<ErpApprovalFlowVo> selectPageList(ErpApprovalFlowBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ErpApprovalFlow> lqw = buildQueryWrapper(bo);
        Page<ErpApprovalFlowVo> result = approvalFlowMapper.selectVoPage(pageQuery.build(), lqw);
        return result;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<ErpApprovalFlow> buildQueryWrapper(ErpApprovalFlowBo bo) {
        LambdaQueryWrapper<ErpApprovalFlow> lqw = Wrappers.lambdaQuery();
        
        lqw.eq(StringUtils.isNotBlank(bo.getModuleCode()), 
            ErpApprovalFlow::getModuleCode, bo.getModuleCode());
        lqw.like(StringUtils.isNotBlank(bo.getFlowName()), 
            ErpApprovalFlow::getFlowName, bo.getFlowName());
        lqw.eq(StringUtils.isNotBlank(bo.getIsActive()), 
            ErpApprovalFlow::getIsActive, bo.getIsActive());
        
        return lqw;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertByBo(ErpApprovalFlowBo bo) {
        // 检查模块编码是否唯一
        Long count = approvalFlowMapper.selectCount(new LambdaQueryWrapper<ErpApprovalFlow>()
            .eq(ErpApprovalFlow::getModuleCode, bo.getModuleCode())
            .eq(ErpApprovalFlow::getIsActive, "1"));
        
        if (count > 0) {
            throw new ServiceException("该模块的激活审批流程已存在");
        }
        
        ErpApprovalFlow flow = MapstructUtils.convert(bo, ErpApprovalFlow.class);
        flow.setCurrentVersion(1);
        return approvalFlowMapper.insert(flow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateByBo(ErpApprovalFlowBo bo) {
        ErpApprovalFlow flow = MapstructUtils.convert(bo, ErpApprovalFlow.class);
        
        // 版本号 +1
        Integer newVersion = bo.getCurrentVersion() + 1;
        flow.setCurrentVersion(newVersion);
        
        return approvalFlowMapper.updateById(flow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(Long[] flowIds) {
        if (ObjectUtil.isNotEmpty(flowIds)) {
            return approvalFlowMapper.deleteBatchIds(Arrays.asList(flowIds));
        }
        return 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long flowId) {
        return approvalFlowMapper.deleteById(flowId);
    }

    @Override
    public ErpApprovalFlowVo getApprovalFlow(String moduleCode) {
        return approvalFlowMapper.selectVoOne(new LambdaQueryWrapper<ErpApprovalFlow>()
            .eq(ErpApprovalFlow::getModuleCode, moduleCode)
            .eq(ErpApprovalFlow::getIsActive, "1")
            .orderByDesc(ErpApprovalFlow::getCurrentVersion)
            .last("LIMIT 1"));
    }
}
