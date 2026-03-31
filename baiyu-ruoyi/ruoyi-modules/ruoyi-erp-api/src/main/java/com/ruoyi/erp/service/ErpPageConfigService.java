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
 * ERP Page Config Service Business Layer Interface
 * 
 * @author JMH
 * @date 2026-03-22
 */
public interface ErpPageConfigService {

    /**
     * Query config by ID
     *
     * @param configId Config ID
     * @return Config info
     */
    ErpPageConfigVo selectById(Long configId);

    /**
     * Query config list
     *
     * @param bo Config params
     * @return Config collection
     */
    List<ErpPageConfigVo> selectList(ErpPageConfigBo bo);

    /**
     * Page query config list
     *
     * @param bo Config params
     * @param pageQuery Page params
     * @return Config page info
     */
    TableDataInfo<ErpPageConfigVo> selectPageList(ErpPageConfigBo bo, PageQuery pageQuery);

    /**
     * Insert config
     *
     * @param bo Config info
     * @return Result
     */
    int insertByBo(ErpPageConfigBo bo);

    /**
     * Update config
     *
     * @param bo Config info
     * @return Result
     */
    int updateByBo(ErpPageConfigBo bo);

    /**
     * Batch delete configs
     *
     * @param configIds Config ID array
     * @return Result
     */
    int deleteByIds(Long[] configIds);

    /**
     * Delete config
     *
     * @param configId Config ID
     * @return Result
     */
    int deleteById(Long configId);

    /**
     * Save config (with version management)
     *
     * @param bo Config info
     * @return Result
     */
    int saveWithVersion(ErpPageConfigBo bo);

    /**
     * Get page config (with cache)
     *
     * @param moduleCode Module code
     * @return JSON config content
     */
    String getPageConfig(String moduleCode);

    /**
     * Page query config history
     *
     * @param configId Config ID
     * @param pageQuery Page params
     * @return Config history page info
     */
    TableDataInfo<ErpPageConfigHistoryVo> selectHistoryPage(Long configId, PageQuery pageQuery);

    /**
     * Get version detail
     *
     * @param configId Config ID
     * @param version Version number
     * @return Version detail
     */
    ErpPageConfigHistoryVo getVersionDetail(Long configId, Integer version);

    /**
     * Rollback to specified version
     *
     * @param configId Config ID
     * @param targetVersion Target version number
     * @param reason Rollback reason
     */
    void rollbackToVersion(Long configId, Integer targetVersion, String reason);

    /**
     * Export config
     *
     * @param configId Config ID
     * @param response HTTP response object
     */
    void exportConfig(Long configId, HttpServletResponse response);

    /**
     * Import config
     *
     * @param file Uploaded file
     */
    void importConfig(MultipartFile file);

    /**
     * Copy config
     *
     * @param configId Config ID
     * @return Copied config info
     */
    ErpPageConfigVo copyConfig(Long configId);

    /**
     * Get config by module code
     *
     * @param moduleCode Module code
     * @return Config entity
     */
    ErpPageConfig getByModuleCode(String moduleCode);

    /**
     * Update config status
     *
     * @param configId Config ID
     * @param status New status (0-disabled, 1-enabled)
     */
    void updateConfigStatus(Long configId, String status);
}
