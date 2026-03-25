package com.ruoyi.erp.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.MapstructUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.erp.domain.bo.ErpPushRelationBo;
import com.ruoyi.erp.domain.entity.ErpPushRelation;
import com.ruoyi.erp.domain.vo.ErpPushRelationVo;
import com.ruoyi.erp.mapper.ErpPushRelationMapper;
import com.ruoyi.erp.service.ErpPushRelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * ERP 下推关系配置 Service 业务层实现
 * 
 * @author ERP Development Team
 * @date 2026-03-22
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ErpPushRelationServiceImpl implements ErpPushRelationService {

    private final ErpPushRelationMapper pushRelationMapper;

    @Override
    public ErpPushRelationVo selectById(Long relationId) {
        return pushRelationMapper.selectVoById(relationId);
    }

    @Override
    public List<ErpPushRelationVo> selectList(ErpPushRelationBo bo) {
        LambdaQueryWrapper<ErpPushRelation> lqw = buildQueryWrapper(bo);
        return pushRelationMapper.selectVoList(lqw);
    }

    @Override
    public Page<ErpPushRelationVo> selectPageList(ErpPushRelationBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ErpPushRelation> lqw = buildQueryWrapper(bo);
        Page<ErpPushRelationVo> result = pushRelationMapper.selectVoPage(pageQuery.build(), lqw);
        return result;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<ErpPushRelation> buildQueryWrapper(ErpPushRelationBo bo) {
        LambdaQueryWrapper<ErpPushRelation> lqw = Wrappers.lambdaQuery();
        
        lqw.eq(StringUtils.isNotBlank(bo.getSourceModule()), 
            ErpPushRelation::getSourceModule, bo.getSourceModule());
        lqw.eq(StringUtils.isNotBlank(bo.getTargetModule()), 
            ErpPushRelation::getTargetModule, bo.getTargetModule());
        lqw.like(StringUtils.isNotBlank(bo.getRelationName()), 
            ErpPushRelation::getRelationName, bo.getRelationName());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), 
            ErpPushRelation::getStatus, bo.getStatus());
        
        return lqw;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertByBo(ErpPushRelationBo bo) {
        // 检查源模块 + 目标模块是否唯一
        Long count = pushRelationMapper.selectCount(new LambdaQueryWrapper<ErpPushRelation>()
            .eq(ErpPushRelation::getSourceModule, bo.getSourceModule())
            .eq(ErpPushRelation::getTargetModule, bo.getTargetModule()));
        
        if (count > 0) {
            throw new ServiceException("该源模块到目标模块的下推关系已存在");
        }
        
        ErpPushRelation relation = MapstructUtils.convert(bo, ErpPushRelation.class);
        relation.setVersion(1);
        return pushRelationMapper.insert(relation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateByBo(ErpPushRelationBo bo) {
        ErpPushRelation relation = MapstructUtils.convert(bo, ErpPushRelation.class);
        return pushRelationMapper.updateById(relation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(Long[] relationIds) {
        if (ObjectUtil.isNotEmpty(relationIds)) {
            return pushRelationMapper.deleteBatchIds(Arrays.asList(relationIds));
        }
        return 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long relationId) {
        return pushRelationMapper.deleteById(relationId);
    }

    @Override
    public ErpPushRelationVo getPushRelation(String sourceModule, String targetModule) {
        return pushRelationMapper.selectVoOne(new LambdaQueryWrapper<ErpPushRelation>()
            .eq(ErpPushRelation::getSourceModule, sourceModule)
            .eq(ErpPushRelation::getTargetModule, targetModule)
            .eq(ErpPushRelation::getStatus, "1"));
    }
}
