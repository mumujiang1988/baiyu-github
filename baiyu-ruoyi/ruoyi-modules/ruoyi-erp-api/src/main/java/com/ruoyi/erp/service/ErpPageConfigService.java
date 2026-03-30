package com.ruoyi.erp.service;

import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.erp.domain.bo.ErpPageConfigBo;
import com.ruoyi.erp.domain.entity.ErpPageConfig;
import com.ruoyi.erp.domain.vo.ErpPageConfigVo;
import com.ruoyi.erp.domain.vo.ErpPageConfigHistoryVo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * ERP 公共配置 Service 业务层接口
 * 
 * @author JMH
 * @date 2026-03-22
 */
public interface ErpPageConfigService {

    /**
     * 根据 ID 查询配置
     *
     * @param configId 配置 ID
     * @return 配置信息
     */
    ErpPageConfigVo selectById(Long configId);

    /**
     * 查询配置列表
     *
     * @param bo 配置参数
     * @return 配置集合
     */
    List<ErpPageConfigVo> selectList(ErpPageConfigBo bo);

    /**
     * 分页查询配置列表
     *
     * @param bo 配置参数
     * @param pageQuery 分页参数
     * @return 配置分页信息
     */
    TableDataInfo<ErpPageConfigVo> selectPageList(ErpPageConfigBo bo, PageQuery pageQuery);

    /**
     * 新增配置
     *
     * @param bo 配置信息
     * @return 结果
     */
    int insertByBo(ErpPageConfigBo bo);

    /**
     * 修改配置
     *
     * @param bo 配置信息
     * @return 结果
     */
    int updateByBo(ErpPageConfigBo bo);

    /**
     * 批量删除配置
     *
     * @param configIds 需要删除的配置 ID 数组
     * @return 结果
     */
    int deleteByIds(Long[] configIds);

    /**
     * 删除配置
     *
     * @param configId 配置 ID
     * @return 结果
     */
    int deleteById(Long configId);

    /**
     * 保存配置 (包含版本管理)
     *
     * @param bo 配置信息
     * @return 结果
     */
    int saveWithVersion(ErpPageConfigBo bo);

    /**
     * 获取页面配置 (带缓存)
     *
     * @param moduleCode 模块编码
     * @return JSON 配置内容
     */
    String getPageConfig(String moduleCode);

    /**
     * 分页查询配置历史
     *
     * @param configId 配置 ID
     * @param pageQuery 分页参数
     * @return 配置历史分页信息
     */
    TableDataInfo<ErpPageConfigHistoryVo> selectHistoryPage(Long configId, PageQuery pageQuery);

    /**
     * 获取版本详情
     *
     * @param configId 配置 ID
     * @param version 版本号
     * @return 版本详情
     */
    ErpPageConfigHistoryVo getVersionDetail(Long configId, Integer version);

    /**
     * 回滚到指定版本
     *
     * @param configId 配置 ID
     * @param targetVersion 目标版本号
     * @param reason 回滚原因
     */
    void rollbackToVersion(Long configId, Integer targetVersion, String reason);

    /**
     * 导出配置
     *
     * @param configId 配置 ID
     * @param response HTTP 响应对象
     */
    void exportConfig(Long configId, HttpServletResponse response);

    /**
     * 导入配置
     *
     * @param file 上传的文件
     */
    void importConfig(MultipartFile file);

    /**
     * 复制配置
     *
     * @param configId 配置 ID
     * @return 复制后的配置信息
     */
    ErpPageConfigVo copyConfig(Long configId);

    /**
     * 根据模块编码获取配置
     *
     * @param moduleCode 模块编码
     * @return 配置实体
     */
    ErpPageConfig getByModuleCode(String moduleCode);

    /**
     * 更新配置状态
     *
     * @param configId 配置 ID
     * @param status 新状态（0-停用，1-启用）
     */
    void updateConfigStatus(Long configId, String status);
}
