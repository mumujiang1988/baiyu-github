package com.ruoyi.erp.service;
import java.util.List;
import com.ruoyi.erp.domain.vo.ErpApprovalHistoryVo;
import com.ruoyi.erp.domain.bo.ErpApprovalHistoryBo;

/**
 * ERP Approval History Service Interface
 * 
 * @author JMH
 * @date 2026-03-30
 */
public interface ErpApprovalHistoryService {

    /**
     * Query approval history by module code and bill ID
     * 
     * @param moduleCode Module code
     * @param billId Bill ID
     * @return Approval history list
     */
    List<ErpApprovalHistoryVo> selectByModuleAndBillId(String moduleCode, Long billId);
    
    /**
     * Save approval history
     * 
     * @param bo Approval history business object
     * @return Affected rows
     */
    int save(ErpApprovalHistoryBo bo);
}
