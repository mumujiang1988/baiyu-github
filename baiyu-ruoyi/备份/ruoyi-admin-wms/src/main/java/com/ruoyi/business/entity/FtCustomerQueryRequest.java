package com.ruoyi.business.entity;

import lombok.Data;

import java.util.List;

/**
 * 富通系统客户查询请求实体
 */
@Data
public class FtCustomerQueryRequest {

    /**
     * 页数，默认值: 1
     */
    private Integer num = 1;

    /**
     * 每页的条数，默认值: 20，取值范围: 1-100
     */
    private Integer size = 20;

    /**
     * accessToken
     */
    private String accessToken;

    /**
     * 创建起始时间，年月日时分秒
     */
    private String createDateStart;

    /**
     * 创建结束时间，年月日时分秒
     */
    private String createDateEnd;

    /**
     * 修改起始时间，年月日时分秒
     */
    private String updateDateStart;

    /**
     * 修改结束时间，年月日时分秒
     */
    private String updateDateEnd;

    /**
     * 业务员更换起始时间，年月日时分秒
     */
    private String transDateStart;

    /**
     * 业务员更换结束时间，年月日时分秒
     */
    private String transDateEnd;

    /**
     * 共享状态修改起始时间，年月日时分秒
     */
    private String shareUpdateDateStart;

    /**
     * 共享状态修改结束时间，年月日时分秒
     */
    private String shareUpdateDateEnd;

    /**
     * 业务员名称
     */
    private String operatorName;

    /**
     * 客户类型名称集合
     */
    private List<String> typeList;

    /**
     * 客户来源名称集合
     */
    private List<String> sourceList;

    /**
     * 客户编码
     */
    private List<String> codeList;

    /**
     * 客户名称
     */
    private List<String> nameList;

    /**
     * 邮箱地址
     */
    private List<String> emailList;

    /**
     * 客户简称
     */
    private List<String> shortNameList;

    /**
     * 是否公海 0:公海; 1:私海
     */
    private Integer belong;

    /**
     * 排序字段 createTime:创建时间; updateTime:修改时间; operator:业务员; code:编码; name:名称
     */
    private String sortField;

    /**
     * 排序方式 asc:正序; desc:倒序
     */
    private String sortMode;

    /**
     * 第三方数据id
     */
    private String extId;

    /**
     * 云平台数据id
     */
    private String id;

    /**
     * 是否关联了第三方数据id 0:否; 1:是
     */
    private Integer isExt;

    /**
     * 审批状态 0:草稿 3:审批完成
     */
    private Integer status;

    /**
     * 审批通过起始时间，年月日时分秒
     */
    private String approvalTimeStart;

    /**
     * 审批通过结束时间，年月日时分秒
     */
    private String approvalTimeEnd;

    /**
     * 是否包含已彻底删除的信息 0:不包含删除信息(默认); 1:包含已删除信息 [-1:仅查询已删除到回收箱信息; -2:仅查询已彻底删除信息;]
     */
    private Integer includeDeleteFlag = 0;

    /**
     * 自定义字段查询参数
     */
    private List<CustomizeFieldQuery> customizeFieldList;

    /**
     * 系统字段查询参数
     */
    private List<BaseFieldQuery> baseFieldList;

    /**
     * 自定义字段查询参数
     */
    @Data
    public static class CustomizeFieldQuery {
        private String modular;
        private String filedCode;
        private Integer dataType;
        private String value;
        private Integer queryType;
    }

    /**
     * 系统字段查询参数
     */
    @Data
    public static class BaseFieldQuery {
        private String modular;
        private String filedCode;
        private Integer dataType;
        private String value;
        private Integer queryType;
    }
}
