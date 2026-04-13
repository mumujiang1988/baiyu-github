package com.ruoyi.erp.exception;

import lombok.Getter;

/**
 * Computed Field Exception
 * 
 * @author JMH
 * @date 2026-03-24
 */
@Getter
public class ComputedFieldException extends ErpConfigException {
    
    /**
     * Field name
     */
    private final String fieldName;
    
    /**
     * Calculation formula
     */
    private final String formula;
    
    /**
     * Computed field exception
     * 
     * @param fieldName Field name
     * @param formula Calculation formula
     * @param message Error message
     */
    public ComputedFieldException(String fieldName, String formula, String message) {
        super("ERP", "COMPUTE_ERROR", message);
        this.fieldName = fieldName;
        this.formula = formula;
    }
    
    /**
     * Computed field exception
     * 
     * @param fieldName Field name
     * @param formula Calculation formula
     * @param message Error message
     * @param cause Original exception
     */
    public ComputedFieldException(String fieldName, String formula, String message, Throwable cause) {
        super("ERP", "COMPUTE_ERROR", message, cause);
        this.fieldName = fieldName;
        this.formula = formula;
    }
    
    /**
     * Computed field exception (with module code)
     * 
     * @param moduleCode Module code
     * @param fieldName Field name
     * @param formula Calculation formula
     * @param message Error message
     */
    public ComputedFieldException(String moduleCode, String fieldName, String formula, String message) {
        super(moduleCode, "COMPUTE_ERROR", message);
        this.fieldName = fieldName;
        this.formula = formula;
    }
    
    /**
     * Computed field exception (with module code and cause)
     * 
     * @param moduleCode Module code
     * @param fieldName Field name
     * @param formula Calculation formula
     * @param message Error message
     * @param cause Original exception
     */
    public ComputedFieldException(String moduleCode, String fieldName, String formula, String message, Throwable cause) {
        super(moduleCode, "COMPUTE_ERROR", message, cause);
        this.fieldName = fieldName;
        this.formula = formula;
    }
}
