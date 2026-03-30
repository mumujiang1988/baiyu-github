package com.ruoyi.business.entity;

import com.ruoyi.business.util.ImportResult;
import lombok.Data;

import java.util.List;

@Data
    public  class ImportSummary {
        private int totalCount;
        private int successCount;
        private int failureCount;
        private List<ImportResult> results;
    }
