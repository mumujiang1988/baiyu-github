package com.ruoyi.business.k3.service;

import com.ruoyi.business.util.ImportResult;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;
import java.util.Map;

public interface MaterialFileService {

    /**
     * 上传图片相关文件到MinIO
     * @param file 文件
     * @param number 物料编码
     * @param fileType 文件类型
     * @return 文件URL
     */
    String  uploadMaterialFile (MultipartFile file, String number, String fileType) throws Exception;
    /**
     * 删除物料相关文件从MinIO
     * @param fileUrl 文件URL
     */
    void deleteMaterialFile(String fileUrl);

    ImportResult validateMaterialData(Map<String, Object> data, int index, String number);
    List<ImportResult> batchImportMaterials(List<Map<String, Object>> dataList);
}
