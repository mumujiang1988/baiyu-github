package com.ruoyi.erp.service;

import java.util.List;
import com.ruoyi.erp.domain.vo.ErpApprovalHistoryVo;
import com.ruoyi.erp.domain.bo.ErpApprovalHistoryBo;

/**
 * ERP 审批历史记录 Service 接口
 * 
 * @author JMH
 * @date 2026-03-30
 */
public interface ErpApprovalHistoryService {

    /**
     * 根据模块编码和单据 ID 查询审批历史
     * 
     * @param moduleCode 模块编码
     * @param billId 单据 ID
     * @return 审批历史列表
     */
    List<ErpApprovalHistoryVo> selectByModuleAndBillId(String moduleCode, Long billId);
    
    /**
     * 保存审批历史记录
     * 
     * @param bo 审批历史业务对象
     * @return 影响行数
     */
    int save(ErpApprovalHistoryBo bo);
}
