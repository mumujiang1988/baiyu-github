package com.ruoyi.business.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.util.StringUtils;
import com.ruoyi.business.k3.service.MaterialFileService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static cn.dev33.satoken.SaManager.log;

@Component
public class ExceptionUtil {

    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    private MaterialFileService materialFileService;

    // 常量定义
    private static final String IMAGE_FOLDER_INSPECTION_REPORTS = "material/inspection_reports/";
    private static final String IMAGE_FOLDER_IMAGES = "material/images/";
    private static final String DEFAULT_IMAGE_EXTENSION = ".jpg";
    private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    /**
     * 根据图片字节数组判断图片格式
     */
    private String getImageExtension(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length < 4) {
            return DEFAULT_IMAGE_EXTENSION;
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
        return DEFAULT_IMAGE_EXTENSION;
    }

    /**
     * 从数据行提取物料编码
     */
    private String extractMaterialCodeFromRow(Map<String, Object> rowData) {
        String[] possibleKeys = {"materialCode", "编码(必填)", "编码"};

        for (String key : possibleKeys) {
            Object value = rowData.get(key);
            if (value instanceof String && !((String) value).trim().isEmpty()) {
                return ((String) value).trim();
            }
        }
        return null;
    }

    /**
     * 上传物料图片到MinIO
     */
    public String uploadMaterialImage(byte[] imageData, String materialCode, String imageFileName) {
        if (imageData == null || imageData.length == 0) {
            throw new IllegalArgumentException("图片数据不能为空");
        }

        try {
            String fileExtension = getImageExtension(imageData);
            String datePath = LocalDate.now().toString().replace("-", "/");
            String safeMaterialCode = materialCode.replace("/", "_");

            String fileName = String.format("%s%s/%s%s",
                IMAGE_FOLDER_INSPECTION_REPORTS,
                datePath,
                safeMaterialCode,
                fileExtension);

            CompletableFuture<String> uploadFuture = minioUtil.uploadFileFromBytes(imageData, fileName);
            return uploadFuture.join();
        } catch (Exception e) {
            log.error("上传物料图片失败，物料编码: {}", materialCode, e);
            throw new RuntimeException("图片上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 处理外部图片文件
     */
    private Map<String, String> processExternalImages(MultipartFile[] imageFiles) {
        Map<String, String> imageCodeMapping = new ConcurrentHashMap<>();

        if (imageFiles == null || imageFiles.length == 0) {
            return imageCodeMapping;
        }

        Arrays.stream(imageFiles).parallel().forEach(imageFile -> {
            try {
                if (isValidImageFile(imageFile)) {
                    String originalFilename = imageFile.getOriginalFilename();
                    String materialCode = extractMaterialCodeFromFilename(originalFilename);

                    if (materialCode != null && !materialCode.isEmpty()) {
                        String extension = getFileExtension(originalFilename);
                        String fileName = buildImageFileName(materialCode, extension);
                        String imageUrl = minioUtil.uploadFile(imageFile, fileName);

                        imageCodeMapping.put(materialCode, imageUrl);
                        log.debug("外部图片上传成功: {} -> {}", materialCode, fileName);
                    }
                }
            } catch (Exception e) {
                log.error("处理外部图片失败: {}", imageFile.getOriginalFilename(), e);
            }
        });

        return imageCodeMapping;
    }

    /**
     * 检查是否为有效的图片文件
     */
    private boolean isValidImageFile(MultipartFile file) {
        return file != null && !file.isEmpty() &&
            file.getOriginalFilename() != null &&
            !file.getOriginalFilename().isEmpty();
    }

    /**
     * 从文件名提取物料编码
     */
    private String extractMaterialCodeFromFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }

        String code = filename;
        int dotIndex = code.lastIndexOf('.');
        if (dotIndex > 0) {
            code = code.substring(0, dotIndex);
        }

        return code.trim();
    }

    /**
     * 构建图片文件名
     */
    private String buildImageFileName(String materialCode, String extension) {
        String datePath = LocalDate.now().toString().replace("-", "/");
        String safeCode = materialCode.replaceAll("[^a-zA-Z0-9._-]", "_");
        String timestamp = String.valueOf(System.currentTimeMillis() % 1000000);

        return String.format("%s%s/%s_%s%s",
            IMAGE_FOLDER_IMAGES,
            datePath,
            safeCode,
            timestamp,
            extension);
    }

    /**
     * 获取文件扩展名对应的MIME类型
     */
    public String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return DEFAULT_MIME_TYPE;
        }

        String lowerCaseName = fileName.toLowerCase();

        // MIME类型映射
        Map<String, String> mimeTypes = new HashMap<>();
        mimeTypes.put(".png", "image/png");
        mimeTypes.put(".jpg", "image/jpeg");
        mimeTypes.put(".jpeg", "image/jpeg");
        mimeTypes.put(".gif", "image/gif");
        mimeTypes.put(".bmp", "image/bmp");
        mimeTypes.put(".webp", "image/webp");
        mimeTypes.put(".tiff", "image/tiff");
        mimeTypes.put(".tif", "image/tiff");
        mimeTypes.put(".ico", "image/x-icon");
        mimeTypes.put(".svg", "image/svg+xml");

        for (Map.Entry<String, String> entry : mimeTypes.entrySet()) {
            if (lowerCaseName.endsWith(entry.getKey())) {
                return entry.getValue();
            }
        }

        return DEFAULT_MIME_TYPE;
    }

    /**
     * 获取字段值
     */
    public String getFieldValue(Map<String, Object> rowData, String fieldName, String requiredFieldName) {
        Object value = rowData.get(requiredFieldName);
        if (value == null) {
            value = rowData.get(fieldName);
        }
        return value != null ? value.toString() : null;
    }

    /**
     * 按文件名匹配并上传图片
     */
    public String findAndUploadImageByFileName(String fileName, String materialNumber,
                                               List<MultipartFile> imageFiles, int rowIndex) {
        if (StringUtils.isBlank(fileName) || imageFiles == null || imageFiles.isEmpty()) {
            return null;
        }

        String fileNameWithoutExt = fileName.replaceAll("\\.[^.]+$", "");

        for (MultipartFile file : imageFiles) {
            if (!isValidImageFile(file)) {
                continue;
            }

            String originalFileName = file.getOriginalFilename();
            String fileBaseName = originalFileName.replaceAll("\\.[^.]+$", "");

            // 1. 精确匹配
            if (fileBaseName.equals(fileNameWithoutExt)) {
                return uploadImageFile(file, materialNumber, rowIndex);
            }

            // 2. 清理特殊字符后匹配
            String cleanFileBaseName = fileBaseName.replaceAll("[_\\-\\s]", "");
            String cleanFileName = fileNameWithoutExt.replaceAll("[_\\-\\s]", "");
            if (cleanFileBaseName.equals(cleanFileName)) {
                return uploadImageFile(file, materialNumber, rowIndex);
            }

            // 3. 包含匹配
            if (fileBaseName.contains(fileNameWithoutExt) || fileNameWithoutExt.contains(fileBaseName)) {
                return uploadImageFile(file, materialNumber, rowIndex);
            }
        }

        return null;
    }

    /**
     * 获取物料编号
     */
    public String getMaterialNumberFromRow(Map<String, Object> rowData) {
        // 支持带*和不帧*的键名
        String[] possibleKeys = {"编码*", "编码", "materialCode"};

        for (String key : possibleKeys) {
            Object value = rowData.get(key);
            if (value != null) {
                String strValue = value.toString().trim();
                if (!strValue.isEmpty()) {
                    return strValue;
                }
            }
        }
        return null;
    }

    /**
     * 改进的图片查找和上传方法
     */
    public String findAndUploadImageForMaterial(String materialNumber, int rowIndex, List<MultipartFile> imageFiles) {
        if (StringUtils.isBlank(materialNumber) || imageFiles == null || imageFiles.isEmpty()) {
            return null;
        }

        log.debug("为物料 {} 查找图片，行号: {}", materialNumber, rowIndex + 1);

        // 多种匹配策略
        String imageUrl = findAndUploadByExactMatch(materialNumber, imageFiles, rowIndex);
        if (imageUrl != null) return imageUrl;

        imageUrl = findAndUploadByRowIndex(rowIndex, materialNumber, imageFiles);
        if (imageUrl != null) return imageUrl;

        imageUrl = findAndUploadBySimplifiedMatch(materialNumber, imageFiles, rowIndex);
        if (imageUrl != null) return imageUrl;

        imageUrl = findAndUploadByCoreNumber(materialNumber, imageFiles, rowIndex);
        if (imageUrl != null) return imageUrl;

        // 如果是带后缀的编号（如BY-AA1001-1），尝试匹配主编号
        if (materialNumber.contains("-")) {
            String[] parts = materialNumber.split("-");
            if (parts.length > 2) {
                String mainNumber = parts[0] + "-" + parts[1];
                imageUrl = findAndUploadByExactMatch(mainNumber, imageFiles, rowIndex);
                if (imageUrl != null) return imageUrl;
            }
        }

        log.warn("行{}: 未找到物料编号 {} 对应的图片（已尝试多种匹配策略）", rowIndex + 1, materialNumber);
        return null;
    }

    /**
     * 按精确匹配查找并上传图片
     */
    private String findAndUploadByExactMatch(String materialNumber, List<MultipartFile> imageFiles, int rowIndex) {
        for (MultipartFile file : imageFiles) {
            if (!isValidImageFile(file)) {
                continue;
            }

            String originalFileName = file.getOriginalFilename();
            String fileBaseName = originalFileName.replaceAll("\\.[^.]+$", "");

            if (fileBaseName.equals(materialNumber) || fileBaseName.equalsIgnoreCase(materialNumber)) {
                return uploadImageFile(file, materialNumber, rowIndex);
            }
        }
        return null;
    }

    /**
     * 按行索引查找并上传图片
     */
    private String findAndUploadByRowIndex(int rowIndex, String materialNumber, List<MultipartFile> imageFiles) {
        if (rowIndex >= 0 && rowIndex < imageFiles.size()) {
            MultipartFile file = imageFiles.get(rowIndex);
            if (isValidImageFile(file)) {
                log.debug("通过行索引匹配图片: 行{} -> {}", rowIndex + 1, file.getOriginalFilename());
                return uploadImageFile(file, materialNumber, rowIndex);
            }
        }
        return null;
    }

    /**
     * 按简化后的编号匹配
     */
    private String findAndUploadBySimplifiedMatch(String materialNumber, List<MultipartFile> imageFiles, int rowIndex) {
        String simplifiedNumber = materialNumber.replaceAll("[^a-zA-Z0-9]", "");

        for (MultipartFile file : imageFiles) {
            if (!isValidImageFile(file)) {
                continue;
            }

            String originalFileName = file.getOriginalFilename();
            String fileBaseName = originalFileName.replaceAll("\\.[^.]+$", "");
            String simplifiedFileName = fileBaseName.replaceAll("[^a-zA-Z0-9]", "");

            if (simplifiedFileName.equals(simplifiedNumber)) {
                log.debug("通过简化匹配找到图片: {} -> {}", materialNumber, originalFileName);
                return uploadImageFile(file, materialNumber, rowIndex);
            }
        }
        return null;
    }

    /**
     * 按编号核心部分匹配
     */
    private String findAndUploadByCoreNumber(String materialNumber, List<MultipartFile> imageFiles, int rowIndex) {
        String[] parts = materialNumber.split("[_-]");

        for (String part : parts) {
            if (part.length() > 2) { // 避免太短的片段
                for (MultipartFile file : imageFiles) {
                    if (!isValidImageFile(file)) {
                        continue;
                    }

                    String originalFileName = file.getOriginalFilename();
                    String fileBaseName = originalFileName.replaceAll("\\.[^.]+$", "");

                    if (fileBaseName.equals(part) || fileBaseName.contains(part)) {
                        log.debug("通过核心部分匹配找到图片: {} -> {}", materialNumber, originalFileName);
                        return uploadImageFile(file, materialNumber, rowIndex);
                    }
                }
            }
        }
        return null;
    }

    /**
     * 上传图片文件
     */
    private String uploadImageFile(MultipartFile imageFile, String materialNumber, int rowIndex) {
        try {
            if (!isValidImageFile(imageFile)) {
                log.warn("行{}: 图片文件为空或无效", rowIndex + 1);
                return null;
            }

            String imageUrl = materialFileService.uploadMaterialFile(
                imageFile,
                materialNumber,
                "images"
            );

            log.info("行{}图片上传成功: {} -> {}", rowIndex + 1, materialNumber, imageUrl);
            return imageUrl;

        } catch (Exception e) {
            log.error("上传图片失败，物料: {}, 行号: {}", materialNumber, rowIndex + 1, e);
            return null;
        }
    }

    /**
     * 从路径中提取文件名（不含扩展名）
     */
    public String extractFileName(String path) {
        if (StringUtils.isBlank(path)) {
            return "";
        }

        String fileNameWithExt;
        if (path.contains("/")) {
            fileNameWithExt = path.substring(path.lastIndexOf("/") + 1);
        } else if (path.contains("\\")) {
            fileNameWithExt = path.substring(path.lastIndexOf("\\") + 1);
        } else {
            fileNameWithExt = path;
        }

        int dotIndex = fileNameWithExt.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileNameWithExt.substring(0, dotIndex);
        }

        return fileNameWithExt;
    }

    /**
     * 解析Excel文件（支持内嵌图片）
     */
    public ExcelParseResult parseExcelFileWithImages(MultipartFile file) throws IOException {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, byte[]> allImageMap = new HashMap<>();
        Map<Integer, List<String>> rowImageMap = new HashMap<>(); // 修改为行->图片名列表

        File tempFile = null;
        try {
            // 创建临时文件
            tempFile = File.createTempFile("excel_", ".xlsx");
            file.transferTo(tempFile);

            // 1. 提取所有内嵌图片
            allImageMap = extractEmbeddedImages(tempFile);
            log.info("提取到 {} 张内嵌图片", allImageMap.size());

            // 2. 解析Excel并建立行与图片的映射关系
            rowImageMap = matchImagesToRowsV2(tempFile, allImageMap);

            // 3. 解析数据行
            result = parseExcelDataV2(tempFile, rowImageMap, allImageMap);

            log.info("Excel解析完成，数据行数: {}, 图片总数: {}, 图片-行映射数: {}",
                result.size(), allImageMap.size(), rowImageMap.size());

        } catch (Exception e) {
            log.error("解析Excel文件时发生错误", e);
            throw new IOException("解析Excel文件失败: " + e.getMessage(), e);
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                try {
                    tempFile.delete();
                } catch (Exception e) {
                    log.warn("删除临时文件失败: {}", tempFile.getAbsolutePath());
                }
            }
        }

        return new ExcelParseResult(result, allImageMap, rowImageMap);
    }

    /**
     * 改进的图片与行匹配算法 - 修复版
     */
    private Map<Integer, List<String>> matchImagesToRowsV2(File excelFile, Map<String, byte[]> allImageMap) throws IOException {
        Map<Integer, List<String>> rowImageMap = new HashMap<>();

        if (allImageMap.isEmpty()) {
            return rowImageMap;
        }

        try (FileInputStream fis = new FileInputStream(excelFile);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Drawing<?> drawing = sheet.getDrawingPatriarch();

            if (drawing == null) {
                log.warn("Excel中没有找到图片位置信息，将使用顺序匹配");
                return matchImagesByOrderV2(allImageMap, sheet.getLastRowNum());
            }

            // 按行号分组收集图片
            Map<Integer, List<String>> tempRowImageMap = new TreeMap<>();
            List<XSSFPicture> pictures = new ArrayList<>();

            // 收集所有图片
            for (Object obj : drawing) {
                if (obj instanceof XSSFPicture) {
                    pictures.add((XSSFPicture) obj);
                }
            }

            log.info("找到 {} 个图片对象", pictures.size());

            // 首先创建图片数据到图片名的映射
            Map<String, String> pictureDataToNameMap = new HashMap<>();
            for (Map.Entry<String, byte[]> entry : allImageMap.entrySet()) {
                String imageName = entry.getKey();
                byte[] imageData = entry.getValue();
                String dataKey = generateImageKey(imageData);
                pictureDataToNameMap.put(dataKey, imageName);
            }

            // 处理每个图片，根据位置匹配到行
            for (XSSFPicture picture : pictures) {
                try {
                    PictureData pictureData = picture.getPictureData();
                    byte[] imageData = pictureData.getData();

                    // 查找对应的图片名
                    String dataKey = generateImageKey(imageData);
                    String imageName = pictureDataToNameMap.get(dataKey);

                    if (imageName == null) {
                        // 如果通过key找不到，尝试直接匹配
                        imageName = findImageNameByData(allImageMap, imageData);
                    }

                    if (imageName != null) {
                        // 获取图片在Excel中的位置
                        ClientAnchor anchor = picture.getClientAnchor();
                        if (anchor == null) {
                            anchor = picture.getPreferredSize();
                        }

                        if (anchor != null) {
                            int row1 = anchor.getRow1();
                            int row2 = anchor.getRow2();
                            int col1 = anchor.getCol1();
                            int col2 = anchor.getCol2();

                            // 确定图片所在的行
                            int pictureRow = row1;
                            if (row1 == 0 && row2 > 0) {
                                pictureRow = row2;
                            }

                            // 转换为数据行索引（假设标题行占用1行）
                            int dataRowIndex = Math.max(0, pictureRow - 1);

                            // 检查这个行是否有数据
                            Row targetRow = sheet.getRow(pictureRow);
                            if (targetRow == null) {
                                // 尝试查找最近的有数据的行
                                for (int i = pictureRow + 1; i <= sheet.getLastRowNum(); i++) {
                                    Row nextRow = sheet.getRow(i);
                                    if (nextRow != null) {
                                        dataRowIndex = Math.max(0, i - 1);
                                        break;
                                    }
                                }
                            }

                            // 根据列位置进一步确认（如果图片在编码列附近）
                            String materialCodeAtRow = getMaterialCodeAtRow(sheet, pictureRow);
                            if (materialCodeAtRow != null) {
                                // 可以根据物料编码进一步验证
                                log.debug("图片 {} 附近找到物料编码: {}", imageName, materialCodeAtRow);
                            }

                            tempRowImageMap.computeIfAbsent(dataRowIndex, k -> new ArrayList<>())
                                .add(imageName);

                            log.debug("图片 '{}' 匹配到行 {} (原始行: {}, 列: {})",
                                imageName, dataRowIndex + 1, pictureRow, col1);
                        }
                    }
                } catch (Exception e) {
                    log.error("处理单个图片时出错", e);
                }
            }

            // 验证匹配结果
            validateAndFixMatching(tempRowImageMap, allImageMap, sheet);

            rowImageMap = tempRowImageMap;

        } catch (Exception e) {
            log.error("匹配图片到行时出错", e);
            return matchImagesByOrderV2(allImageMap, 100);
        }

        return rowImageMap;
    }

    /**
     * 验证并修复匹配结果
     */
    private void validateAndFixMatching(Map<Integer, List<String>> rowImageMap,
                                        Map<String, byte[]> allImageMap,
                                        Sheet sheet) {

        // 统计已匹配的图片
        Set<String> matchedImages = new HashSet<>();
        rowImageMap.values().forEach(matchedImages::addAll);

        // 找出未匹配的图片
        List<String> unmatchedImages = new ArrayList<>(allImageMap.keySet());
        unmatchedImages.removeAll(matchedImages);

        if (!unmatchedImages.isEmpty()) {
            log.warn("有 {} 张图片未匹配到行，将尝试智能分配", unmatchedImages.size());

            // 按行顺序分配未匹配的图片
            int lastRowNum = sheet.getLastRowNum();
            int dataRowCount = Math.max(0, lastRowNum - 1); // 减去标题行

            if (dataRowCount > 0 && unmatchedImages.size() == dataRowCount) {
                // 如果未匹配图片数量等于数据行数，按顺序分配
                unmatchedImages.sort(Comparator.naturalOrder());

                for (int i = 0; i < unmatchedImages.size() && i < dataRowCount; i++) {
                    int targetRow = i;
                    rowImageMap.computeIfAbsent(targetRow, k -> new ArrayList<>())
                        .add(unmatchedImages.get(i));
                    log.debug("智能分配: 图片 '{}' -> 行 {}", unmatchedImages.get(i), targetRow + 1);
                }
            } else {
                // 其他情况，尝试根据文件名中的编码匹配
                for (String imageName : unmatchedImages) {
                    // 从图片名中提取可能的物料编码
                    String possibleCode = extractPossibleCodeFromImageName(imageName);
                    if (possibleCode != null) {
                        // 查找包含该编码的行
                        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                            Row row = sheet.getRow(i);
                            if (row != null) {
                                Cell codeCell = row.getCell(0);
                                if (codeCell != null) {
                                    String cellValue = codeCell.getStringCellValue();
                                    if (cellValue != null && cellValue.contains(possibleCode)) {
                                        int targetRow = Math.max(0, i - 1);
                                        rowImageMap.computeIfAbsent(targetRow, k -> new ArrayList<>())
                                            .add(imageName);
                                        log.debug("通过编码匹配: 图片 '{}' -> 行 {} (编码: {})",
                                            imageName, targetRow + 1, possibleCode);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 从图片名中提取可能的物料编码
     */
    private String extractPossibleCodeFromImageName(String imageName) {
        // 移除扩展名
        String nameWithoutExt = imageName.replaceAll("\\.[^.]+$", "");

        // 尝试提取类似 "BY-AA1027A" 的格式
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[A-Z]{2}-[A-Z]{2}\\d+[A-Z]?");
        java.util.regex.Matcher matcher = pattern.matcher(nameWithoutExt);

        if (matcher.find()) {
            return matcher.group();
        }

        return null;
    }

    /**
     * 获取指定行的物料编码
     */
    private String getMaterialCodeAtRow(Sheet sheet, int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row != null) {
            // 假设物料编码在第一列（索引0）
            Cell cell = row.getCell(0);
            if (cell != null) {
                return cell.getStringCellValue();
            }
        }
        return null;
    }




    /**
     * 为图片数据生成唯一key
     */
    private String generateImageKey(byte[] imageData) {
        if (imageData == null || imageData.length < 100) {
            return "small_image";
        }
        // 使用前100个字节和总长度生成key
        int hash = Arrays.hashCode(Arrays.copyOf(imageData, Math.min(100, imageData.length)));
        return "img_" + hash + "_" + imageData.length;
    }

    /**
     * 根据图片数据查找图片名
     */
    private String findImageNameByData(Map<String, byte[]> imageMap, byte[] targetData) {
        for (Map.Entry<String, byte[]> entry : imageMap.entrySet()) {
            if (Arrays.equals(entry.getValue(), targetData)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 按顺序匹配图片到行 - 改进版
     */
    private Map<Integer, List<String>> matchImagesByOrderV2(Map<String, byte[]> allImageMap, int lastRowNum) {
        Map<Integer, List<String>> rowImageMap = new HashMap<>();

        List<String> imageNames = new ArrayList<>(allImageMap.keySet());

        // 对图片名进行排序，确保一致性
        imageNames.sort(Comparator.naturalOrder());

        for (int i = 0; i < imageNames.size() && i <= lastRowNum; i++) {
            rowImageMap.put(i, Collections.singletonList(imageNames.get(i)));
            log.debug("顺序匹配: 图片 '{}' -> 行 {}", imageNames.get(i), i + 1);
        }

        return rowImageMap;
    }

    /**
     * 改进的Excel数据解析方法
     */
    private List<Map<String, Object>> parseExcelDataV2(File file,
                                                       Map<Integer, List<String>> rowImageMap,
                                                       Map<String, byte[]> imageMap) {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            List<Map<Integer, String>> rawDataList = EasyExcel.read(file)
                .sheet()
                .headRowNumber(0)
                .doReadSync();

            if (rawDataList != null && !rawDataList.isEmpty()) {
                Map<Integer, String> headerRow = rawDataList.get(0);
                List<String> headers = new ArrayList<>();

                for (int i = 0; i < headerRow.size(); i++) {
                    String header = headerRow.get(i);
                    headers.add((header == null || header.trim().isEmpty()) ?
                        "column_" + (i + 1) : header.trim());
                }

                // 逐条处理数据行
                for (int i = 1; i < rawDataList.size(); i++) {
                    Map<Integer, String> rowData = rawDataList.get(i);
                    Map<String, Object> convertedRow = new LinkedHashMap<>();

                    // 转换行数据
                    for (int j = 0; j < headers.size(); j++) {
                        convertedRow.put(headers.get(j), rowData.get(j));
                    }

                    // 添加图片信息到行数据
                    int dataRowIndex = i - 1;
                    if (rowImageMap.containsKey(dataRowIndex)) {
                        List<String> imageNames = rowImageMap.get(dataRowIndex);
                        List<byte[]> rowImages = new ArrayList<>();

                        for (String imageName : imageNames) {
                            byte[] imageData = imageMap.get(imageName);
                            if (imageData != null) {
                                rowImages.add(imageData);
                            }
                        }

                        if (!rowImages.isEmpty()) {
                            convertedRow.put("_rowImages", rowImages);
                            convertedRow.put("_imageCount", rowImages.size());
                            convertedRow.put("_imageNames", imageNames);

                            log.debug("行 {} 关联了 {} 张图片: {}",
                                dataRowIndex + 1, rowImages.size(), imageNames);
                        }
                    }

                    // 仅添加非空行
                    if (!isEmptyRow(convertedRow)) {
                        result.add(convertedRow);
                    }
                }

                log.info("Excel解析完成，共 {} 行有效数据，其中 {} 行有图片",
                    result.size(), rowImageMap.size());
            }
        } catch (Exception e) {
            log.error("Excel数据解析失败", e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
        return result;
    }

    /**
     * 匹配图片到对应的数据行
     */
    private Map<Integer, List<byte[]>> matchImagesToRows(File excelFile, Map<String, byte[]> allImageMap) throws IOException {
        Map<Integer, List<byte[]>> rowImageMap = new HashMap<>();

        if (allImageMap.isEmpty()) {
            return rowImageMap;
        }

        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Drawing<?> drawing = sheet.getDrawingPatriarch();

            if (drawing == null) {
                log.warn("Excel中没有找到图片位置信息，将使用顺序匹配");
                return matchImagesByOrder(allImageMap, sheet.getLastRowNum());
            }

            // 遍历所有形状，获取图片位置
            for (Shape shape : drawing) {
                if (shape instanceof XSSFPicture) {
                    XSSFPicture picture = (XSSFPicture) shape;
                    XSSFClientAnchor anchor = picture.getPreferredSize();
                    int rowIndex = anchor.getRow1();

                    PictureData pictureData = picture.getPictureData();
                    byte[] imageData = pictureData.getData();
                    String imageKey = findImageKeyByData(allImageMap, imageData);

                    if (imageKey != null && rowIndex >= 0) {
                        int dataRowIndex = Math.max(0, rowIndex - 1);
                        rowImageMap.computeIfAbsent(dataRowIndex, k -> new ArrayList<>())
                            .add(imageData);
                        log.debug("图片 {} 匹配到行 {}", imageKey, dataRowIndex + 1);
                    }
                }
            }

            // 如果通过位置匹配不到，使用顺序匹配作为后备方案
            if (rowImageMap.isEmpty()) {
                log.info("未通过位置匹配到图片，使用顺序匹配");
                return matchImagesByOrder(allImageMap, sheet.getLastRowNum());
            }

        } catch (Exception e) {
            log.error("匹配图片到行时出错", e);
            return matchImagesByOrder(allImageMap, 100);
        }

        return rowImageMap;
    }

    /**
     * 根据图片数据查找对应的key
     */
    private String findImageKeyByData(Map<String, byte[]> imageMap, byte[] targetData) {
        for (Map.Entry<String, byte[]> entry : imageMap.entrySet()) {
            if (Arrays.equals(entry.getValue(), targetData)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 按顺序匹配图片到行（后备方案）
     */
    private Map<Integer, List<byte[]>> matchImagesByOrder(Map<String, byte[]> allImageMap, int lastRowNum) {
        Map<Integer, List<byte[]>> rowImageMap = new HashMap<>();

        List<byte[]> imageList = new ArrayList<>(allImageMap.values());

        for (int i = 0; i < imageList.size() && i <= lastRowNum; i++) {
            rowImageMap.put(i, Collections.singletonList(imageList.get(i)));
            log.debug("顺序匹配: 图片{} -> 行{}", i + 1, i + 1);
        }

        return rowImageMap;
    }

    /**
     * 提取Excel内嵌图片
     */
    public Map<String, byte[]> extractEmbeddedImages(File excelFile) throws IOException {
        Map<String, byte[]> imageMap = new HashMap<>();

        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(excelFile))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.getName().startsWith("xl/media/")) {
                    String imageName = entry.getName().substring("xl/media/".length());
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    byte[] data = new byte[1024];
                    int read;

                    while ((read = zip.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, read);
                    }

                    imageMap.put(imageName, buffer.toByteArray());
                }
                zip.closeEntry();
            }
        } catch (IOException e) {
            log.error("解析Excel内嵌图片失败", e);
            throw new IOException("解析Excel内嵌图片失败: " + e.getMessage(), e);
        }
        return imageMap;
    }

    /**
     * 解析Excel数据（支持图片映射）
     */
    private List<Map<String, Object>> parseExcelData(File file, Map<Integer, List<byte[]>> rowImageMap) {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            List<Map<Integer, String>> rawDataList = EasyExcel.read(file)
                .sheet()
                .headRowNumber(0)
                .doReadSync();

            if (rawDataList != null && !rawDataList.isEmpty()) {
                Map<Integer, String> headerRow = rawDataList.get(0);
                List<String> headers = new ArrayList<>();

                for (int i = 0; i < headerRow.size(); i++) {
                    String header = headerRow.get(i);
                    headers.add((header == null || header.trim().isEmpty()) ?
                        "column_" + (i + 1) : header.trim());
                }

                // 逐条处理数据行
                for (int i = 1; i < rawDataList.size(); i++) {
                    Map<Integer, String> rowData = rawDataList.get(i);
                    Map<String, Object> convertedRow = new LinkedHashMap<>();

                    // 转换行数据
                    for (int j = 0; j < headers.size(); j++) {
                        convertedRow.put(headers.get(j), rowData.get(j));
                    }

                    // 添加图片信息到行数据
                    int dataRowIndex = i - 1;
                    if (rowImageMap.containsKey(dataRowIndex)) {
                        List<byte[]> rowImages = rowImageMap.get(dataRowIndex);
                        convertedRow.put("_rowImages", rowImages);
                        convertedRow.put("_imageCount", rowImages.size());
                    }

                    // 仅添加非空行
                    if (!isEmptyRow(convertedRow)) {
                        result.add(convertedRow);
                    }
                }

                log.info("Excel解析完成，共 {} 行有效数据，其中 {} 行有图片",
                    result.size(), rowImageMap.size());
            }
        } catch (Exception e) {
            log.error("Excel数据解析失败", e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
        return result;
    }

    /**
     * 判断行是否为空
     */
    private boolean isEmptyRow(Map<String, Object> row) {
        if (row == null || row.isEmpty()) {
            return true;
        }

        for (Object value : row.values()) {
            if (value instanceof String) {
                if (!((String) value).trim().isEmpty()) {
                    return false;
                }
            } else if (value != null) {
                return false;
            }
        }

        return true;
    }
}
