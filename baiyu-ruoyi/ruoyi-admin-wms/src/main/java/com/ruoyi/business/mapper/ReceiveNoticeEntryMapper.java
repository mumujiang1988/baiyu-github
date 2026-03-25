package com.ruoyi.business.mapper;

import com.ruoyi.business.entity.ReceiveNoticeEntry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReceiveNoticeEntryMapper {

    /** 根据主键ID查询 */
    ReceiveNoticeEntry selectById(@Param("fbillno") String fbillno, @Param("fMaterialId") String fMaterialId);

    /** 根据单据编号查询明细 */
    List<ReceiveNoticeEntry> selectByBillNo(@Param("billNo") String billNo);

    int insert(ReceiveNoticeEntry receiveNoticeEntry);

    int updateById(ReceiveNoticeEntry receiveNoticeEntry);

    int deleteById(@Param("id") Long id);

    /** 删除单据的所有明细 */
    int deleteByBillNo(@Param("billNo") String billNo);

    /** 查询所有收料通知单明细 */
    List<ReceiveNoticeEntry> selectList();

    /** 根据条件查询收料通知单明细 */
    List<ReceiveNoticeEntry> selectListByCondition(@Param("entry") ReceiveNoticeEntry entry);

    /** 分页查询收料通知单明细 */
    List<ReceiveNoticeEntry> selectListByPage(@Param("offset") int offset, @Param("pageSize") int pageSize);

    /** 批量插入收料通知单明细 */
    int batchInsert(@Param("list") List<ReceiveNoticeEntry> list);

    /** 批量更新收料通知单明细 */
    int batchUpdateById(@Param("list") List<ReceiveNoticeEntry> list);
}
