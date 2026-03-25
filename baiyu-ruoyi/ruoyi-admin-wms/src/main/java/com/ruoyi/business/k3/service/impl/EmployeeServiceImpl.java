package com.ruoyi.business.k3.service.impl;

import com.ruoyi.business.entity.Employee;
import com.ruoyi.business.entity.FinancialInformation;
import com.ruoyi.business.entity.HrEmployeeFollowUp;
import com.ruoyi.business.k3.config.k3config;
import com.ruoyi.business.k3.service.EmployeeService;
import com.ruoyi.business.k3.util.DateUtils;
import com.ruoyi.business.mapper.EmployeeMapper;
import com.ruoyi.business.mapper.FinancialInformationMapper;
import com.ruoyi.business.mapper.HrEmployeeFollowUpMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 员工服务实现类
 */
@Slf4j
@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Resource
    private EmployeeMapper employeeMapper;

    @Resource
    private FinancialInformationMapper financialInformationMapper;

    @Resource
    private HrEmployeeFollowUpMapper hrEmployeeFollowUpMapper;

    @Resource
    private k3config k3Config;

    /**
     * 同步金蝶员工数据到本地数据库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncEmployeeData() {
        log.info("开始同步金蝶员工主表数据...");

        // 1）查询已存在的员工ID
        List<Long> existList = employeeMapper.selectAllFids();
        Set<Long> existFids = new HashSet<>(existList == null ? Collections.emptyList() : existList);
        log.info("已存在员工数量：{}", existFids.size());

        // 2）从金蝶查询员工列表
        List<List<Object>> employeeList = k3Config.queryCommonEmployeeList();
        if (employeeList == null || employeeList.isEmpty()) {
            log.info("没有获取到员工数据");
            return 0;
        }
        log.info("从金蝶获取到 {} 条员工数据", employeeList.size());

        List<Employee> inserts = new ArrayList<>();
        List<Employee> updates = new ArrayList<>();

        for (List<Object> rowData : employeeList) {
            if (rowData == null || rowData.isEmpty()) {
                continue;
            }

            Employee employee = convertToEmployee(rowData);
            if (employee == null || employee.getFid() == null) {
                continue;
            }

            if (existFids.contains(employee.getFid())) {
                updates.add(employee);
            } else {
                inserts.add(employee);
                existFids.add(employee.getFid());
            }
        }

        log.info("员工数据分析完成：新增 {} 条，更新 {} 条", inserts.size(), updates.size());

        // 批量写入
        batchWriteEmployee(inserts, updates);

        return inserts.size() + updates.size();
    }

    /**
     * 同步金蝶员工银行信息到本地数据库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncEmployeeBankData() {
        log.info("开始同步金蝶员工银行信息...");

        // 1）查询已存在的员工编号
        List<String> existList = financialInformationMapper.selectAllSupplierNumbers();
        Set<String> existNumbers = new HashSet<>(existList == null ? Collections.emptyList() : existList);
        log.info("已存在员工银行信息数量：{}", existNumbers.size());

        // 2）从金蝶查询员工银行列表
        List<List<Object>> bankList = k3Config.queryEmployeeBankList();
        if (bankList == null || bankList.isEmpty()) {
            log.info("没有获取到员工银行数据");
            return 0;
        }
        log.info("从金蝶获取到 {} 条员工银行数据", bankList.size());

        List<FinancialInformation> inserts = new ArrayList<>();
        List<FinancialInformation> updates = new ArrayList<>();

        for (List<Object> rowData : bankList) {
            if (rowData == null || rowData.isEmpty()) {
                continue;
            }

            FinancialInformation fi = convertToEmployeeBank(rowData);
            if (fi == null || fi.getSupplierNumber() == null) {
                continue;
            }

            // 检查是否有有效银行数据
            if (!hasValidBankInfo(fi)) {
                continue;
            }

            if (existNumbers.contains(fi.getSupplierNumber())) {
                updates.add(fi);
            } else {
                inserts.add(fi);
                existNumbers.add(fi.getSupplierNumber());
            }
        }

        log.info("员工银行数据分析完成：新增 {} 条，更新 {} 条", inserts.size(), updates.size());

        // 批量写入
        batchWriteEmployeeBank(inserts, updates);

        return inserts.size() + updates.size();
    }

    /**
     * 同步金蝶员工跟进信息到本地数据库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncEmployeeFollowUpData() {
        log.info("开始同步金蝶员工跟进信息...");

        // 1）查询已存在的员工编号
        List<String> existList = hrEmployeeFollowUpMapper.selectAllEmployeeNumbers();
        Set<String> existNumbers = new HashSet<>(existList == null ? Collections.emptyList() : existList);
        log.info("已存在员工跟进信息数量：{}", existNumbers.size());

        // 2）从金蝶查询员工跟进列表
        List<List<Object>> followUpList = k3Config.queryEmployeeTransferPositionList();
        if (followUpList == null || followUpList.isEmpty()) {
            log.info("没有获取到员工跟进数据");
            return 0;
        }
        log.info("从金蝶获取到 {} 条员工跟进数据", followUpList.size());

        List<HrEmployeeFollowUp> inserts = new ArrayList<>();
        List<HrEmployeeFollowUp> updates = new ArrayList<>();

        for (List<Object> rowData : followUpList) {
            if (rowData == null || rowData.isEmpty()) {
                continue;
            }

            HrEmployeeFollowUp followUp = convertToEmployeeFollowUp(rowData);
            if (followUp == null || followUp.getEmployeeNumber() == null) {
                continue;
            }
            // 检查是否有有效跟进数据
            if (!hasValidFollowUpInfo(followUp)) {
                continue;
            }

            if (existNumbers.contains(followUp.getEmployeeNumber())) {
                updates.add(followUp);
            } else {
                inserts.add(followUp);
                existNumbers.add(followUp.getEmployeeNumber());
            }
        }

        log.info("员工跟进数据分析完成：新增 {} 条，更新 {} 条", inserts.size(), updates.size());

        // 批量写入
        batchWriteEmployeeFollowUp(inserts, updates);

        return inserts.size() + updates.size();
    }

    /**
     * 同步所有员工相关数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncAllEmployeeData() {
        log.info("开始同步所有员工相关数据...");

        int employeeCount = syncEmployeeData();
        int bankCount = syncEmployeeBankData();
        int followUpCount = syncEmployeeFollowUpData();

        int total = employeeCount + bankCount + followUpCount;
        log.info("所有员工数据同步完成，总计处理：{} 条（员工：{}，银行：{}，跟进：{}）",
            total, employeeCount, bankCount, followUpCount);

        return total;
    }

    @Override
    public Employee getById(Long fid) {
        Assert.notNull(fid, "员工ID不能为空");
        return employeeMapper.selectById(fid);
    }

    // ==================== 批量写入方法 ====================

    private void batchWriteEmployee(List<Employee> inserts, List<Employee> updates) {
        final int batchSize = 500;

        if (!inserts.isEmpty()) {
            for (int i = 0; i < inserts.size(); i += batchSize) {
                int end = Math.min(i + batchSize, inserts.size());
                log.info("批量插入员工 {} - {}", i, end);
                employeeMapper.batchInsert(inserts.subList(i, end));
            }
        }

        if (!updates.isEmpty()) {
            for (int i = 0; i < updates.size(); i += batchSize) {
                int end = Math.min(i + batchSize, updates.size());
                log.info("批量更新员工 {} - {}", i, end);
                employeeMapper.batchUpdateByFid(updates.subList(i, end));
            }
        }
    }

    private void batchWriteEmployeeBank(List<FinancialInformation> inserts, List<FinancialInformation> updates) {
        final int batchSize = 500;

        if (!inserts.isEmpty()) {
            for (int i = 0; i < inserts.size(); i += batchSize) {
                int end = Math.min(i + batchSize, inserts.size());
                log.info("批量插入员工银行信息 {} - {}", i, end);
                financialInformationMapper.batchInsertEmployeeBank(inserts.subList(i, end));
            }
        }

        if (!updates.isEmpty()) {
            for (int i = 0; i < updates.size(); i += batchSize) {
                int end = Math.min(i + batchSize, updates.size());
                log.info("批量更新员工银行信息 {} - {}", i, end);
                financialInformationMapper.batchUpdateEmployeeBank(updates.subList(i, end));
            }
        }
    }

    private void batchWriteEmployeeFollowUp(List<HrEmployeeFollowUp> inserts, List<HrEmployeeFollowUp> updates) {
        final int batchSize = 500;

        if (!inserts.isEmpty()) {
            for (int i = 0; i < inserts.size(); i += batchSize) {
                int end = Math.min(i + batchSize, inserts.size());
                log.info("批量插入员工跟进信息 {} - {}", i, end);
                hrEmployeeFollowUpMapper.batchInsert(inserts.subList(i, end));
            }
        }

        if (!updates.isEmpty()) {
            for (int i = 0; i < updates.size(); i += batchSize) {
                int end = Math.min(i + batchSize, updates.size());
                log.info("批量更新员工跟进信息 {} - {}", i, end);
                hrEmployeeFollowUpMapper.batchUpdate(updates.subList(i, end));
            }
        }
    }

    // ==================== 转换方法 ====================

    /**
     * K3 行数据 → Employee 实体映射方法
     */
    private Employee convertToEmployee(List<Object> objectList) {
        Employee e = new Employee();
        if (objectList == null || objectList.size() < 51) {
            log.warn("员工数据字段不足，期望51个，实际{}个", objectList == null ? 0 : objectList.size());
            if (objectList == null || objectList.isEmpty()) return null;
        }

        int idx = 0;
        e.setFid(getLong(safeGet(objectList, idx++)));                    // 0: FID
        e.setFname(getString(safeGet(objectList, idx++)));                // 1: FName
        e.setFdescription(getString(safeGet(objectList, idx++)));         // 2: FDescription
        e.setFstaffNumber(getString(safeGet(objectList, idx++)));         // 3: FStaffNumber
        e.setFYwm(getString(safeGet(objectList, idx++)));                 // 4: F_ywm
        e.setFSfzh(getString(safeGet(objectList, idx++)));                // 5: F_SFZH
        e.setFCtyDate(getLocalDate(safeGet(objectList, idx++)));          // 6: F_cty_Date
        e.setFZzrq(getLocalDate(safeGet(objectList, idx++)));             // 7: F_zzrq
        e.setFNl(getInteger(safeGet(objectList, idx++)));                 // 8: F_nl
        e.setFmobile(getString(safeGet(objectList, idx++)));              // 9: FMobile
        e.setFaddress(getString(safeGet(objectList, idx++)));             // 10: FAddress
        e.setFOraText(getString(safeGet(objectList, idx++)));             // 11: F_ora_Text (毕业学校)
        e.setFOraDate1(getLocalDate(safeGet(objectList, idx++)));         // 12: F_ora_Date1 (毕业日期)
        e.setFOraCombo(getString(safeGet(objectList, idx++)));            // 13: F_ora_Combo (学历)
        e.setFOraText1(getString(safeGet(objectList, idx++)));            // 14: F_ora_Text1 (企业QQ)
        e.setFXb(getString(safeGet(objectList, idx++)));                  // 15: F_XB (性别)
        e.setFHkszd(getString(safeGet(objectList, idx++)));               // 16: F_hkszd (户口所在地)
        e.setFMz(getString(safeGet(objectList, idx++)));                  // 17: F_mz (民族)
        e.setFLzrq(getLocalDate(safeGet(objectList, idx++)));             // 18: F_lzrq (离职日期)
        e.setFSbrq(getLocalDate(safeGet(objectList, idx++)));             // 19: F_sbrq (社保日期)
        e.setFSbje(getBigDecimal(safeGet(objectList, idx++)));            // 20: F_sbje (社保金额)
        e.setFDkyl(getBigDecimal(safeGet(objectList, idx++)));            // 21: F_dkyl (代扣养老)
        e.setFDkyliao(getBigDecimal(safeGet(objectList, idx++)));         // 22: F_dkyliao (代扣医疗)
        e.setFDksy(getBigDecimal(safeGet(objectList, idx++)));            // 23: F_dksy (代扣失业)
        e.setFJsdj(getBigDecimal(safeGet(objectList, idx++)));            // 24: F_jsdj (计时单价)
        e.setFLdht(getString(safeGet(objectList, idx++)));                // 25: F_ldht (劳动合同)
        e.setFBxrq(getLocalDate(safeGet(objectList, idx++)));             // 26: F_bxrq (保险日期)
        e.setFGwgz(getBigDecimal(safeGet(objectList, idx++)));            // 27: F_gwgz (岗位补贴)
        e.setFJx(getBigDecimal(safeGet(objectList, idx++)));              // 28: F_jx (绩效津贴)
        e.setFMltcd(getBigDecimal(safeGet(objectList, idx++)));           // 29: F_mltcd (毛利提成点)
        e.setFYmtcd(getBigDecimal(safeGet(objectList, idx++)));           // 30: F_ymtcd (越南提成点)
        e.setFBmjt(getBigDecimal(safeGet(objectList, idx++)));            // 31: F_BMJT (保密津贴)
        e.setFPeuuImagefileserverQtr(getString(safeGet(objectList, idx++))); // 32: F_PEUU_ImageFileServer_qtr (新人入职登记表)
        e.setFPeuuDate83g(getLocalDate(safeGet(objectList, idx++)));      // 33: F_PEUU_Date_83g (劳动合同到期日)
        e.setFPeuuImagefileserverApv(getString(safeGet(objectList, idx++))); // 34: F_PEUU_ImageFileServer_apv (毕业证书)
        e.setFPeuuImagefileserverTzk(getString(safeGet(objectList, idx++))); // 35: F_PEUU_ImageFileServer_tzk (学位证书)
        e.setFPeuuImagefileserverCa9(getString(safeGet(objectList, idx++))); // 36: F_PEUU_ImageFileServer_ca9 (英语语言证书)
        e.setFPeuuImagefileserverUky(getString(safeGet(objectList, idx++))); // 37: F_PEUU_ImageFileServer_uky (保密协议)
        e.setFPeuuImagefileserverDvn(getString(safeGet(objectList, idx++))); // 38: F_PEUU_ImageFileServer_dvn (公司底线)
        e.setFtel(getString(safeGet(objectList, idx++)));                 // 39: FTel (企业电话)
        e.setFemail(getString(safeGet(objectList, idx++)));               // 40: FEmail (企业邮箱)
        e.setFBw(getString(safeGet(objectList, idx++)));                  // 41: F_bw (部门)
        e.setFJjlxr(getString(safeGet(objectList, idx++)));               // 42: F_JJLXR (紧急联系人)
        e.setFJjlxrdh(getString(safeGet(objectList, idx++)));             // 43: F_JJLXRDH (紧急联系人电话)
        e.setFJbgz(getBigDecimal(safeGet(objectList, idx++)));            // 44: F_jbgz (基本工资)
        e.setFJltcd(getBigDecimal(safeGet(objectList, idx++)));           // 45: F_jltcd (净利提成点)
        e.setFOraDate(getLocalDate(safeGet(objectList, idx++)));          // 46: F_ora_Date (出生日期)
        e.setFcreatorId(getLong(safeGet(objectList, idx++)));             // 47: FCreatorId (创建人)
        e.setFcreateDate(getLocalDateTime(safeGet(objectList, idx++)));   // 48: FCreateDate (创建日期)
        e.setFmodifierId(getLong(safeGet(objectList, idx++)));            // 49: FModifierId (修改人)
        e.setFmodifyDate(getLocalDateTime(safeGet(objectList, idx++)));    // 50: FModifyDate (修改日期)
        e.setFbaseProperty(getString(safeGet(objectList, idx++)));         // 51: FBaseProperty (岗位名称)

        return e;
    }

    /**
     * K3 行数据 → FinancialInformation 实体映射方法（员工银行信息）

     */
    private FinancialInformation convertToEmployeeBank(List<Object> objectList) {
        FinancialInformation fi = new FinancialInformation();
        if (objectList == null || objectList.size() < 7) {
            log.warn("员工银行数据字段不足，期望7个，实际{}个", objectList == null ? 0 : objectList.size());
            if (objectList == null || objectList.isEmpty()) return null;
        }

        int idx = 0;
        fi.setSupplierNumber(getString(safeGet(objectList, idx++)));      // 0: FStaffNumber (员工编号作为关联)
        fi.setAccountName(getString(safeGet(objectList, idx++)));         // 1: FName (员工姓名/账户名称)
        fi.setBankAccount(getString(safeGet(objectList, idx++)));         // 2: FBankCode (银行账号)
        fi.setOpeningBank(getString(safeGet(objectList, idx++)));         // 3: FOpenBankName (开户银行)
        fi.setReceivingBank(getString(safeGet(objectList, idx++)));       // 4: FBankTypeRec (收款银行)
        idx++; // 5: FBankHolder (持卡人，跳过)
        fi.setBankAddress(getString(safeGet(objectList, idx)));           // 6: FOpenBankName (开户行地址)

        return fi;
    }

    /**
     * K3 行数据 → HrEmployeeFollowUp 实体映射方法（员工跟进信息）
     */
    private HrEmployeeFollowUp convertToEmployeeFollowUp(List<Object> objectList) {
        HrEmployeeFollowUp fu = new HrEmployeeFollowUp();
        if (objectList == null || objectList.size() < 17) {
            log.warn("员工跟进数据字段不足，期望17个，实际{}个", objectList == null ? 0 : objectList.size());
            if (objectList == null || objectList.isEmpty()) return null;
        }

        int idx = 0;
        fu.setEmployeeNumber(getString(safeGet(objectList, idx++)));      // 0: FStaffNumber (员工编号)
        fu.setEmployeeName(getString(safeGet(objectList, idx++)));        // 1: FName (员工姓名)
        fu.setStatus(getString(safeGet(objectList, idx++)));              // 2: Fzzzt (在职状态)
        fu.setFollowDate(getLocalDate(safeGet(objectList, idx++)));       // 3: Fgtrq (沟通日期)
        fu.setFollowContent(getString(safeGet(objectList, idx++)));       // 4: Fgtsx (沟通事项)
        fu.setFollowResult(getString(safeGet(objectList, idx++)));        // 5: Fgsqk (改善情况)
        fu.setJobTransfer(getString(safeGet(objectList, idx++)));         // 6: Fgztg (工作调岗)
        fu.setSalaryAdjustTime(getLocalDate(safeGet(objectList, idx++))); // 7: Fxztzsj (薪资调整时间)
        fu.setSalaryAdjustReason(getString(safeGet(objectList, idx++)));  // 8: Fxztzyy (薪资调整原因)
        fu.setSalaryAdjustDocOld(getString(safeGet(objectList, idx++)));  // 9: Fgztzd (工资调整单旧)
        fu.setPositionAdjustDocOld(getString(safeGet(objectList, idx++))); // 10: Fgwtzd (岗位调整单旧)
        fu.setBusinessDataDocOld(getString(safeGet(objectList, idx++)));  // 11: Fywsjb (业务数据表旧)
        fu.setSalaryAdjustDoc(getString(safeGet(objectList, idx++)));     // 12: F_gztzd (工资调整单-新)
        fu.setPositionAdjustDoc(getString(safeGet(objectList, idx++)));   // 13: F_gwtzd (岗位调整单-新)
        fu.setBusinessDataDoc(getString(safeGet(objectList, idx++)));     // 14: F_ywsjb (业务数据表-新)
        fu.setRegularNoticeDoc(getString(safeGet(objectList, idx++)));    // 15: F_zztzh (转正通知函)
        fu.setCommunicationFollowDoc(getString(safeGet(objectList, idx))); // 16: F_gttzb (沟通跟进表)

        // 设置事件类型
        fu.setEventType("TRANSFER");
        fu.setCreateTime(LocalDateTime.now());

        return fu;
    }

    /**
     * 检查 FinancialInformation 是否有有效银行数据
     */
    private boolean hasValidBankInfo(FinancialInformation fi) {
        if (fi == null || fi.getSupplierNumber() == null) {
            return false;
        }
        return fi.getBankAccount() != null || fi.getAccountName() != null ||
               fi.getOpeningBank() != null || fi.getReceivingBank() != null;
    }

    /**
     * 检查 HrEmployeeFollowUp 是否有有效跟进数据
     */
    private boolean hasValidFollowUpInfo(HrEmployeeFollowUp fu) {
        if (fu == null || fu.getEmployeeNumber() == null) {
            return false;
        }
        return fu.getFollowContent() != null || fu.getFollowResult() != null ||
               fu.getStatus() != null || fu.getSalaryAdjustDoc() != null ||
               fu.getJobTransfer() != null;
    }

    // ==================== 工具方法 ====================

    private Object safeGet(List<Object> list, int index) {
        if (list == null || index >= list.size()) {
            return null;
        }
        return list.get(index);
    }

    private String getString(Object obj) {
        return obj == null ? null : obj.toString().trim();
    }

    private Long getLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Long) return (Long) obj;
        if (obj instanceof Integer) return ((Integer) obj).longValue();
        if (obj instanceof Number) return ((Number) obj).longValue();
        try {
            String s = obj.toString().trim();
            if (s.isEmpty()) return null;
            return Long.parseLong(s);
        } catch (Exception e) {
            return null;
        }
    }

    private Integer getInteger(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof Number) return ((Number) obj).intValue();
        try {
            String s = obj.toString().trim();
            if (s.isEmpty()) return null;
            return Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal getBigDecimal(Object obj) {
        if (obj == null) return null;
        try {
            String s = obj.toString().trim();
            if (s.isEmpty()) return null;
            return new BigDecimal(s);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime getLocalDateTime(Object obj) {
        return DateUtils.parseLocalDateTime(obj);
    }

    private LocalDate getLocalDate(Object obj) {
        return DateUtils.parseLocalDate(obj);
    }
}
