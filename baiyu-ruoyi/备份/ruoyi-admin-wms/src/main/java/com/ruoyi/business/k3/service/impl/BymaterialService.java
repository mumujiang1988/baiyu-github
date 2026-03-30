package com.ruoyi.business.k3.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.business.bo.BymaterialBo;
import com.ruoyi.business.entity.Bymaterial;
import com.ruoyi.business.mapper.MaterialMapper;
import com.ruoyi.business.mapper.MaterialsMapper;
import com.ruoyi.business.vo.BymaterialVo;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.system.domain.entity.SysPost;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BymaterialService {
    @Resource
    private MaterialsMapper materialsMapper;
    /**
     * 查询岗位信息集合
     *
     * @param bymaterialBo 岗位信息
     * @return 岗位信息集合
     */
    public List<BymaterialVo> selectBymaterialList(BymaterialBo bymaterialBo) {
        return materialsMapper.selectVoList(buildQueryWrapper(bymaterialBo));
    }

    /**
     * 根据查询条件构建查询包装器
     *
     * @param bo 查询条件对象
     * @return 构建好的查询包装器
     */
    private LambdaQueryWrapper<Bymaterial> buildQueryWrapper(BymaterialBo bo) {
        LambdaQueryWrapper<Bymaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper./*like(StringUtils.isNotBlank(bo.getErpClsId()), Bymaterial::getErpClsId, bo.getErpClsId())
            .like(StringUtils.isNotBlank(bo.getMaterialgroup()), Bymaterial::getMaterialgroup, bo.getMaterialgroup())
            .eq(StringUtils.isNotBlank(bo.getProductCategory()), Bymaterial::getProductCategory, bo.getProductCategory())*/
            eq(bo.getId() != null, Bymaterial::getId, bo.getId())
            .orderByAsc(Bymaterial::getCreator_time);
        return wrapper;
    }


}
