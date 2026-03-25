package com.ruoyi.business.util;


import java.util.List;
import java.util.Map;

public  class ExcelParseResult {
    private final List<Map<String, Object>> dataList;
    private final Map<String, byte[]> imageMap;
    private final Map<Integer, List<String>> rowImageMap; // 行索引 -> 图片名列表
    private Map<String, Integer> imageRowMap; // 图片名 -> 行索引

    // 修改构造函数，添加 rowImageMap 参数
    public ExcelParseResult(List<Map<String, Object>> dataList,
                            Map<String, byte[]> imageMap,
                            Map<Integer, List<String>> rowImageMap) {
        this.dataList = dataList;
        this.imageMap = imageMap;
        this.rowImageMap = rowImageMap;
    }

    public List<Map<String, Object>> getDataList() {
        return dataList;
    }

    public Map<String, byte[]> getImageMap() {
        return imageMap;
    }

    public Map<String, Integer> getImageRowMap() {
        return imageRowMap;
    }

    public void setImageRowMap(Map<String, Integer> imageRowMap) {
        this.imageRowMap = imageRowMap;
    }

    public Map<Integer, List<String>> getRowImageMap() {
        return rowImageMap;
    }

}
