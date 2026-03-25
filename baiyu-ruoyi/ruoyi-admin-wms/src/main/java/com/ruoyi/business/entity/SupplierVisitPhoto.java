package com.ruoyi.business.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SupplierVisitPhoto {

    private Long id;
    private Long supplierId;

    /** 人员合照 */
    private String staffPhoto;

    /** 样品间照片 */
    private String sampleRoomPhoto;

    /** 生产车间照片 */
    private String productionWorkshopPhoto;

    /** 包装车间照片 */
    private String packagingWorkshopPhoto;

    /** 检验区照片 */
    private String inspectionAreaPhoto;

    /** 厂区照片 */
    private String factoryAreaPhoto;

    /** 大门照片 */
    private String gatePhoto;

    /** 其他照片 */
    private String otherPhoto;

    private String remark;
    private String createdBy;
    private LocalDateTime createdAt;
}
