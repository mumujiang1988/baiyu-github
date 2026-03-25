package com.ruoyi.business.util;

/**
 * 导入结果类
 */

public class ImportResult {
    private String number;
    private String message;
    private boolean success;
    private int rowIndex;
    private String materialCode;
    private String imageUrl;

    public ImportResult(String number, String message, boolean success) {
        this.number = number;
        this.message = message;
        this.success = success;
    }

    // Getters and setters
    public String  getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public int getRowIndex() { return rowIndex; }
    public void setRowIndex(int rowIndex) { this.rowIndex = rowIndex; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
