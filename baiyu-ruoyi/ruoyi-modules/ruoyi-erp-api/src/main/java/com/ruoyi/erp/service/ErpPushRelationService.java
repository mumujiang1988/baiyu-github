package com.ruoyi.erp.service;

import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.erp.domain.bo.ErpPushRelationBo;
import com.ruoyi.erp.domain.vo.ErpPushRelationVo;

import java.util.List;

/**
 * ERP 下推关系配置 Service 业务层接口
 * 
 * @author JMH
 * @date 2026-03-22
 */
public interface ErpPushRelationService {

    /**
     * 根据 ID 查询下推关系
     *
     * @param relationId 关系 ID
     * @return 下推关系信息
     */
    ErpPushRelationVo selectById(Long relationId);

    /**
     * 查询下推关系列表
     *
     * @param bo 下推关系参数
     * @return 下推关系集合
     */
    List<ErpPushRelationVo> selectList(ErpPushRelationBo bo);

    /**
     * 分页查询下推关系列表
     *
     * @param bo 下推关系参数
     * @param pageQuery 分页参数
     * @return 下推关系分页信息
     */
    TableDataInfo<ErpPushRelationVo> selectPageList(ErpPushRelationBo bo, PageQuery pageQuery);

    /**
     * 新增下推关系
     *
     * @param bo 下推关系信息
     * @return 结果
     */
    int insertByBo(ErpPushRelationBo bo);

    /**
     * 修改下推关系
     *
     * @param bo 下推关系信息
     * @return 结果
     */
    int updateByBo(ErpPushRelationBo bo);

    /**
     * 批量删除下推关系
     *
     * @param relationIds 需要删除的关系 ID 数组
     * @return 结果
     */
    int deleteByIds(Long[] relationIds);

    /**
     * 删除下推关系
     *
     * @param relationId 关系 ID
     * @return 结果
     */
    int deleteById(Long relationId);

    /**
     * 获取下推关系配置
     *
     * @param sourceModule 源模块编码
     * @param targetModule 目标模块编码
     * @return 下推关系配置
     */
    ErpPushRelationVo getPushRelation(String sourceModule, String targetModule);
}
