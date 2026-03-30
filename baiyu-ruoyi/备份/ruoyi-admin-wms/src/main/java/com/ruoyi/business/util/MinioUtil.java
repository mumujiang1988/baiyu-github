package com.ruoyi.business.util;

import cn.hutool.http.Method;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class MinioUtil {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;

    @Value("${minio.bucketName}")
    private String bucketName;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        try {
            minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
            createBucket();
            setBucketPublic();
        } catch (Exception e) {
            log.error("MinIO客户端初始化失败", e);
        }
    }

    /**
     * 创建bucket
     */
    private void createBucket() {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            log.error("创建bucket失败", e);
        }
    }
    /**
     * 设置存储桶为公开读取权限
     */
    public void setBucketPublic() {
        try {
            // 定义公开读取策略
            String policy = "{\n" +
                "  \"Version\": \"2012-10-17\",\n" +
                "  \"Statement\": [\n" +
                "    {\n" +
                "      \"Effect\": \"Allow\",\n" +
                "      \"Principal\": {\"AWS\": [\"*\"]},\n" +
                "      \"Action\": [\"s3:GetObject\"],\n" +
                "      \"Resource\": [\"arn:aws:s3:::" + bucketName + "/*\"]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

            minioClient.setBucketPolicy(
                SetBucketPolicyArgs.builder()
                    .bucket(bucketName)
                    .config(policy)
                    .build()
            );

            log.info("存储桶 {} 已设置为公开读取权限", bucketName);
        } catch (Exception e) {
            log.error("设置存储桶公开访问权限失败", e);
        }
    }

    /**
     * 上传文件
     * @param file 文件
     * @param objectName 对象名称（文件路径+文件名）
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String objectName) {
        try {
            // 添加空值检查
            if (file == null) {
                throw new RuntimeException("文件不能为空");
            }

            // 如果未指定对象名称，则生成唯一名称
            if (objectName == null || objectName.isEmpty()) {
                objectName = generateUniqueFileName(file.getOriginalFilename());
            }

            // 上传文件
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );

            // 返回文件访问URL
            return "http://118.178.144.159:9000/" + bucketName + "/" + objectName;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }

    }
    @Async
    public  CompletableFuture<String> uploadFileFromBytes(byte[] fileBytes, String fileName) throws Exception {
        try {
            String url = uploadFileFromStream(new ByteArrayInputStream(fileBytes), fileName, fileBytes.length);
            return CompletableFuture.completedFuture(url);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
    /**
     * 根据图片字节数组判断图片格式
     */
    public static String getImageExtension(byte[] imageBytes){
        if (imageBytes == null || imageBytes.length < 4) {
            return ".jpg";
        }

        // 检查文件头
        if (imageBytes[0] == (byte) 0xFF && imageBytes[1] == (byte) 0xD8) {
            return ".jpg";
        } else if (imageBytes[0] == (byte) 0x89 && imageBytes[1] == (byte) 0x50 &&
            imageBytes[2] == (byte) 0x4E && imageBytes[3] == (byte) 0x47) {
            return ".png";
        } else if (imageBytes[0] == (byte) 0x47 && imageBytes[1] == (byte) 0x49 &&
            imageBytes[2] == (byte) 0x46) {
            return ".gif";
        }
        return ".jpg"; // 默认
    }
    public String uploadFileFromStream(InputStream inputStream, String fileName, long fileSize) throws Exception {
        try {
            // 确定文件内容类型
            String contentType = "application/octet-stream"; // 默认类型
            if (fileName != null) {
                if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
                    contentType = "inspection_reports/jpeg";
                } else if (fileName.toLowerCase().endsWith(".png")) {
                    contentType = "inspection_reports/png";
                } else if (fileName.toLowerCase().endsWith(".gif")) {
                    contentType = "image/gif";
                } else if (fileName.toLowerCase().endsWith(".pdf")) {
                    contentType = "application/pdf";
                }
            }

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(inputStream, fileSize, -1)
                    .contentType(contentType)
                    .build()
            );

            // 返回文件访问URL
            return "http://118.178.144.159:9000/" + bucketName + "/" + fileName;
        } catch (Exception e) {
            log.error("通过输入流上传文件失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }


    /**
     * 上传文件（自动生成文件名）
     * @param file 文件
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file) {
        // 添加空值检查
        if (file == null) {
            throw new RuntimeException("文件不能为空");
        }
        return uploadFile(file, null);
    }

    /**
     * 获取文件访问URL
     * @param objectName 对象名称
     * @return 文件访问URL
     */
    public String getFileUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(io.minio.http.Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(7 * 24 * 60 * 60) // 7天有效期
                    .build()
            );
        } catch (Exception e) {
            log.error("获取文件URL失败", e);
            throw new RuntimeException("获取文件URL失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     * @param objectName 对象名称
     * @return 文件流
     */
    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
        } catch (Exception e) {
            log.error("文件下载失败", e);
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     * @param objectName 对象名称
     */
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
        } catch (Exception e) {
            log.error("文件删除失败", e);
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除文件
     * @param objectNames 对象名称列表
     */
    public void deleteFiles(List<String> objectNames) {
        try {
            List<DeleteObject> deleteObjects = new ArrayList<>();
            for (String objectName : objectNames) {
                deleteObjects.add(new DeleteObject(objectName));
            }

            minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                    .bucket(bucketName)
                    .objects(deleteObjects)
                    .build()
            );
        } catch (Exception e) {
            log.error("批量删除文件失败", e);
            throw new RuntimeException("批量删除文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件信息
     * @param objectName 对象名称
     * @return 文件信息
     */
    public StatObjectResponse getFileInfo(String objectName) {
        try {
            return minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
        } catch (Exception e) {
            log.error("获取文件信息失败", e);
            throw new RuntimeException("获取文件信息失败: " + e.getMessage());
        }
    }



    /**
     * 生成唯一的文件名
     * @param originalFilename 原始文件名
     * @return 唯一文件名
     */
    private String generateUniqueFileName(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            return UUID.randomUUID().toString();
        }

        String extension = "";
        int lastIndex = originalFilename.lastIndexOf(".");
        if (lastIndex > 0) {
            extension = originalFilename.substring(lastIndex);
        }

        return UUID.randomUUID().toString().replace("-", "") + extension;
    }

    /**
     * 检查文件是否存在
     * @param objectName 对象名称
     * @return 是否存在
     */
    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void deleteFileByUrl(String fileUrl) {
        try {
            String objectName = extractObjectNameFromUrl(fileUrl);
            if (objectName != null && !objectName.isEmpty()) {
                deleteFile(objectName);
            }
        } catch (Exception e) {
            log.error("根据URL删除文件失败", e);
            throw new RuntimeException("删除文件失败: " + e.getMessage());
        }
    }

    /**
     * 从文件URL中提取对象名称
     * @param fileUrl 文件URL
     * @return 对象名称
     */
    public String extractObjectNameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }

        try {
            String bucketPattern = "/" + bucketName + "/";
            int bucketIndex = fileUrl.indexOf(bucketPattern);

            if (bucketIndex > 0) {
                String objectPath = fileUrl.substring(bucketIndex + bucketPattern.length());
                // 移除查询参数（如果存在）
                int queryIndex = objectPath.indexOf("?");
                if (queryIndex > 0) {
                    objectPath = objectPath.substring(0, queryIndex);
                }
                return objectPath;
            }
            return null;
        } catch (Exception e) {
            log.warn("提取对象名称失败: " + fileUrl, e);
            return null;
        }
    }
}
