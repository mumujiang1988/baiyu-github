package com.ruoyi.erp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.erp.domain.vo.ErpApprovalHistoryVo;
import com.ruoyi.erp.domain.bo.ErpApprovalHistoryBo;
import com.ruoyi.erp.service.ErpApprovalHistoryService;

/**
 * ERP 审批历史记录 Service 业务层实现
 * 
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
     * Map 转 VO
     */
    private ErpApprovalHistoryVo mapToVo(Map<String, Object> row) {
        ErpApprovalHistoryVo vo = new ErpApprovalHistoryVo();
        vo.setHistoryId(getLong(row.get("history_id")));
        vo.setModuleCode(getString(row.get("module_code")));
        vo.setBillId(getLong(row.get("bill_id")));
        vo.setFlowId(getLong(row.get("flow_id")));
        vo.setCurrentStep(getInteger(row.get("current_step")));
        vo.setApproverId(getString(row.get("approver_id")));
        vo.setApprovalAction(getString(row.get("approval_action")));
        vo.setApprovalOpinion(getString(row.get("approval_opinion")));
        vo.setApprovalTime((LocalDateTime) row.get("approval_time"));
        return vo;
    }
    
    /**
     * 安全获取 Long 值
     */
    private Long getLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime) {
            return null;
        }
        return ((Number) value).longValue();
    }
    
    /**
     * 安全获取 Integer 值
     */
    private Integer getInteger(Object value) {
        return value != null ? ((Number) value).intValue() : null;
    }
    
    /**
     * 安全获取 String 值
     */
    private String getString(Object value) {
        return value != null ? value.toString() : null;
    }
}
