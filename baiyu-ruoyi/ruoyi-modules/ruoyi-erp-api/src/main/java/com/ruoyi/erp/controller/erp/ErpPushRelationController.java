package com.ruoyi.erp.controller.erp;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.web.core.BaseController;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.erp.domain.bo.ErpPushRelationBo;
import com.ruoyi.erp.domain.vo.ErpPushRelationVo;
import com.ruoyi.erp.service.ErpPushRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * ERP 下推关系配置 信息操作处理
 *
 * @author JMH
 * @date 2026-03-22
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/erp/push-relation")
public class ErpPushRelationController extends BaseController {

    private final ErpPushRelationService pushRelationService;

    /**
     * 查询下推关系列表
     */
    @SaCheckPermission("erp:push:list")
    @GetMapping("/list")
    public TableDataInfo<ErpPushRelationVo> list(ErpPushRelationBo bo, PageQuery pageQuery) {
        // Service 已返回 TableDataInfo，直接返回
        return pushRelationService.selectPageList(bo, pageQuery);
    }

    /**
     * 获取下推关系详情
     *
     * @param relationId 关系 ID
     */
    @SaCheckPermission("erp:push:query")
    @GetMapping("/{relationId}")
    public R<ErpPushRelationVo> getInfo(@PathVariable Long relationId) {
        return R.ok(pushRelationService.selectById(relationId));
    }

    /**
     * 新增下推关系
     */
    @SaCheckPermission("erp:push:add")
    @Log(title = "ERP 下推关系", businessType = BusinessType.INSERT)
    @PostMapping()
    public R<Void> add(@Validated @RequestBody ErpPushRelationBo bo) {
        return toAjax(pushRelationService.insertByBo(bo));
    }

    /**
     * 修改下推关系
     */
    @SaCheckPermission("erp:push:edit")
    @Log(title = "ERP 下推关系", businessType = BusinessType.UPDATE)
    @PutMapping()
    public R<Void> edit(@Validated @RequestBody ErpPushRelationBo bo) {
        return toAjax(pushRelationService.updateByBo(bo));
    }

    /**
     * 删除下推关系
     *
     * @param relationIds 关系 ID 串
     */
    @SaCheckPermission("erp:push:remove")
    @Log(title = "ERP 下推关系", businessType = BusinessType.DELETE)
    @DeleteMapping("/{relationIds}")
    public R<Void> remove(@PathVariable Long[] relationIds) {
        return toAjax(pushRelationService.deleteByIds(relationIds));
    }
}
