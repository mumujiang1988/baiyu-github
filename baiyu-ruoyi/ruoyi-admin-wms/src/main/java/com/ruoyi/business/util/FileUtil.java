package com.ruoyi.business.util;

import com.ruoyi.business.entity.SupplierVisitRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class FileUtil {
    @Autowired
    private MinioUtil minioUtil;

    /**
     * 根据ID匹配处理回访记录附件
     * @param visitRecords 回访记录列表
     * @param visitFiles 上传的文件列表
     * @param visitFileIds 文件对应的记录ID列表
     */
    public void handleVisitRecordAttachmentsById(List<SupplierVisitRecord> visitRecords, List<MultipartFile> visitFiles, List<String> visitFileIds) {

        if (visitFileIds == null || visitFileIds.size() != visitFiles.size()) {
            throw new RuntimeException("附件ID与文件数量不匹配");
        }

        // 创建ID到文件的映射
        Map<String, MultipartFile> idToFileMap = new HashMap<>();
        for (int i = 0; i < visitFileIds.size(); i++) {
            idToFileMap.put(visitFileIds.get(i), visitFiles.get(i));
        }

        // 处理每个回访记录
        for (SupplierVisitRecord record : visitRecords) {
            String recordId = record.getId() != null ? record.getId().toString() : null;

            // 尝试匹配数据库ID或临时ID
            MultipartFile file = null;
            if (recordId != null && idToFileMap.containsKey(recordId)) {
                file = idToFileMap.get(recordId);
            } else if (record.getTempId() != null && idToFileMap.containsKey(record.getTempId())) {
                file = idToFileMap.get(record.getTempId());
            }

            // 如果有匹配的文件，处理上传
            if (file != null && !file.isEmpty()) {
                try {
                    // 上传文件到MinIO或文件系统
                    String fileUrl = minioUtil.uploadFile(file);
                    record.setAttachment(fileUrl);
                } catch (Exception e) {
                    throw new RuntimeException("上传回访附件失败: " + e.getMessage());
                }
            }
        }
    }
}
