package com.ruoyi.business.k3.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.dto.KdSalesStockoutVo;
import com.ruoyi.business.entity.KdSalesStockout;
import com.ruoyi.business.entity.KdSalesStockoutBo;
import com.ruoyi.business.k3.service.KdSalesStockoutService;
import com.ruoyi.business.mapper.KdSalesStockoutMapper;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 金蝶销售出库单主表 Service 业务层实现类
 *
 * @author aiflowy
 */
@RequiredArgsConstructor
@Service
public class KdSalesStockoutServiceImpl implements KdSalesStockoutService {

    private final KdSalesStockoutMapper kdSalesStockoutMapper;

    /**
     * 查询金蝶销售出库单主表
     *
     * @param fid 金蝶销售出库单主表主键
     * @return 金蝶销售出库单主表
     */
    @Override
    public KdSalesStockoutVo selectKdSalesStockoutByFid(String fid) {
        return kdSalesStockoutMapper.selectVoById(fid);
    }

    /**
     * 查询金蝶销售出库单主表列表（分页）
     *
     * @param bo 金蝶销售出库单主表
     * @param pageQuery 分页参数
     * @return 金蝶销售出库单主表集合
     */
    @Override
    public TableDataInfo<KdSalesStockoutVo> selectPageKdSalesStockoutList(KdSalesStockoutBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<KdSalesStockout> lqw = buildQueryWrapper(bo);
        Page<KdSalesStockoutVo> page = kdSalesStockoutMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    /**
     * 查询金蝶销售出库单主表列表
     *
     * @param bo 金蝶销售出库单主表
     * @return 金蝶销售出库单主表集合
     */
    @Override
    public List<KdSalesStockoutVo> selectKdSalesStockoutList(KdSalesStockoutBo bo) {
        LambdaQueryWrapper<KdSalesStockout> lqw = buildQueryWrapper(bo);
        return kdSalesStockoutMapper.selectVoList(lqw);
    }

    /**
     * 构建查询条件
     *
     * @param bo 业务对象
     * @return 查询条件包装器
     */
    private LambdaQueryWrapper<KdSalesStockout> buildQueryWrapper(KdSalesStockoutBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<KdSalesStockout> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getFbillNo()), KdSalesStockout::getFbillNo, bo.getFbillNo());
        lqw.eq(StringUtils.isNotBlank(bo.getFcustomerID()), KdSalesStockout::getFcustomerID, bo.getFcustomerID());
        lqw.eq(StringUtils.isNotBlank(bo.getFsaleOrgId()), KdSalesStockout::getFsaleOrgId, bo.getFsaleOrgId());
        lqw.eq(StringUtils.isNotBlank(bo.getFdocumentStatus()), KdSalesStockout::getFdocumentStatus, bo.getFdocumentStatus());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            KdSalesStockout::getFdate, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(KdSalesStockout::getCreateTime);
        return lqw;
    }

    /**
     * 新增金蝶销售出库单主表
     *
     * @param bo 金蝶销售出库单主表
     * @return 结果
     */
    @Override
    public int insertKdSalesStockout(KdSalesStockoutBo bo) {
        KdSalesStockout kdSalesStockout = new KdSalesStockout();
        // 属性复制
        org.springframework.beans.BeanUtils.copyProperties(bo, kdSalesStockout);
        return kdSalesStockoutMapper.insert(kdSalesStockout);
    }

    /**
     * 修改金蝶销售出库单主表
     *
     * @param bo 金蝶销售出库单主表
     * @return 结果
     */
    @Override
    public int updateKdSalesStockout(KdSalesStockoutBo bo) {
        KdSalesStockout kdSalesStockout = new KdSalesStockout();
        org.springframework.beans.BeanUtils.copyProperties(bo, kdSalesStockout);
        return kdSalesStockoutMapper.updateById(kdSalesStockout);
    }

    /**
     * 批量删除金蝶销售出库单主表
     *
     * @param fids 需要删除的金蝶销售出库单主表主键
     * @return 结果
     */
    @Override
    public int deleteKdSalesStockoutByFids(String[] fids) {
        return kdSalesStockoutMapper.deleteBatchIds(Arrays.asList(fids));
    }

    /**
     * 删除金蝶销售出库单主表信息
     *
     * @param fid 金蝶销售出库单主表主键
     * @return 结果
     */
    @Override
    public int deleteKdSalesStockoutByFid(String fid) {
        return kdSalesStockoutMapper.deleteById(fid);
    }
}
