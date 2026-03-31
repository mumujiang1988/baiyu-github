package com.ruoyi.erp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service; 
import java.util.List;
import java.util.Map; 
import com.ruoyi.common.core.utils.MapstructUtils;
import com.ruoyi.erp.domain.vo.ErpApprovalHistoryVo;
import com.ruoyi.erp.domain.bo.ErpApprovalHistoryBo;
import com.ruoyi.erp.service.ErpApprovalHistoryService; 

/**
 * ERP Approval History Service Business Layer Implementation
 * @author JMH
 * @date 2026-03-30
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ErpApprovalHistoryServiceImpl implements ErpApprovalHistoryService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<ErpApprovalHistoryVo> selectByModuleAndBillId(String moduleCode, Long billId) {
        String sql = """
            SELECT 
                history_id, module_code, bill_id, flow_id, current_step,
                approver_id, approval_action, approval_opinion, approval_time
            FROM erp_approval_history
            WHERE module_code = ? AND bill_id = ?
            ORDER BY approval_time DESC
        """;
        
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, moduleCode, billId);
        return rows.stream().map(this::mapToVo).toList();
    }
    
    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public int save(ErpApprovalHistoryBo bo) {
        String sql = """
            INSERT INTO erp_approval_history (
                module_code, bill_id, flow_id, current_step,
                approval_action, approval_opinion, approver_id,
                approval_time
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        return jdbcTemplate.update(sql,
            bo.getModuleCode(),
            bo.getBillId(),
            bo.getFlowId(),
            bo.getCurrentStep(),
            bo.getApprovalAction(),
            bo.getApprovalOpinion(),
            bo.getApproverId(),
            bo.getApprovalTime()
        );
    }
    
    /**
     * Map to VO
     */
    private ErpApprovalHistoryVo mapToVo(Map<String, Object> row) {
        // Use RuoYi MapstructUtils auto conversion
        return MapstructUtils.convert(row, ErpApprovalHistoryVo.class);
    }
}
