package com.ruoyi.common.excel.core;

public class PictureDataInfo {

    private String fileName;
    private byte[] data;
    private int row;  // 图片在Excel中的行位置（0-based）
    private int col;  // 图片在Excel中的列位置（0-based）
    private int width; // 图片宽度
    private int height; // 图片高度

}
