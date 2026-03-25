package com.ruoyi.system.controller.erp;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.web.core.BaseController;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.system.domain.bo.ErpApprovalFlowBo;
import com.ruoyi.system.domain.vo.ErpApprovalFlowVo;
import com.ruoyi.system.service.ErpApprovalFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * ERP 审批流程配置 信息操作处理
 *
 * @author ERP Development Team
 * @date 2026-03-22
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/erp/approval-flow")
public class ErpApprovalFlowController extends BaseController {

    private final ErpApprovalFlowService approvalFlowService;

    /**
     * 查询审批流程列表
     */
    @SaCheckPermission("erp:approval:list")
    @GetMapping("/list")
    public TableDataInfo<ErpApprovalFlowVo> list(ErpApprovalFlowBo bo, PageQuery pageQuery) {
        Page<ErpApprovalFlowVo> page = approvalFlowService.selectPageList(bo, pageQuery);
        TableDataInfo<ErpApprovalFlowVo> info = new TableDataInfo<>();
        info.setRows(page.getRecords());
        info.setTotal(page.getTotal());
        return info;
    }

    /**
     * 获取审批流程详情
     *
     * @param flowId 流程 ID
     */
    @SaCheckPermission("erp:approval:query")
    @GetMapping("/{flowId}")
    public R<ErpApprovalFlowVo> getInfo(@PathVariable Long flowId) {
        return R.ok(approvalFlowService.selectById(flowId));
    }

    /**
     * 新增审批流程
     */
    @SaCheckPermission("erp:approval:add")
    @Log(title = "ERP 审批流程", businessType = BusinessType.INSERT)
    @PostMapping()
    public R<Void> add(@Validated @RequestBody ErpApprovalFlowBo bo) {
        return toAjax(approvalFlowService.insertByBo(bo));
    }

    /**
     * 修改审批流程
     */
    @SaCheckPermission("erp:approval:edit")
    @Log(title = "ERP 审批流程", businessType = BusinessType.UPDATE)
    @PutMapping()
    public R<Void> edit(@Validated @RequestBody ErpApprovalFlowBo bo) {
        return toAjax(approvalFlowService.updateByBo(bo));
    }

    /**
     * 删除审批流程
     *
     * @param flowIds 流程 ID 串
     */
    @SaCheckPermission("erp:approval:remove")
    @Log(title = "ERP 审批流程", businessType = BusinessType.DELETE)
    @DeleteMapping("/{flowIds}")
    public R<Void> remove(@PathVariable Long[] flowIds) {
        return toAjax(approvalFlowService.deleteByIds(flowIds));
    }
}
