package com.ruoyi.business.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 客户同步分页结果
 */
@Data
@AllArgsConstructor
public class CustomerPageResult {
    private List<Customer> inserts;
    private List<Customer> updates;
    private List<FinancialInformation> financialInserts;
    private List<FinancialInformation> financialUpdates;
    private List<CustomerTransfer> transferInserts;
    private List<CustomerTransfer> transferUpdates;
}
