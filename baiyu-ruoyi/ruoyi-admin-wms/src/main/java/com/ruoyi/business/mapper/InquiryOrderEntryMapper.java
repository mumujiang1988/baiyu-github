package com.ruoyi.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.entity.InquiryOrderEntry;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 询价单详情表 Mapper接口
 */
public interface InquiryOrderEntryMapper extends BaseMapper<InquiryOrderEntry> {

    /**
     * 根据主表ID查询详情列表
     */
    List<InquiryOrderEntry> selectByParentId(@Param("fid") String fid);

    /**
     * 根据询价单ID查询详情列表
     */
    InquiryOrderEntry selectByInquiryId(@Param("fbillno") String fbillno, @Param("productCode") String productCode);

    /**
     * 插入询价单明细
     */
    int save(InquiryOrderEntry entry);

    /**
     * 根据产品代码查询
     */
    List<InquiryOrderEntry> selectByProductCode(@Param("fbillno") String fbillno);

    /**
     * 批量插入详情数据
     */
    int batchInsert(@Param("entries") List<InquiryOrderEntry> entries);

    /**
     * 根据主表ID删除详情数据
     */
    int deleteByParentId(@Param("fid") String fid);
}
