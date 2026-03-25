package com.ruoyi.business.mapper;

import com.ruoyi.business.entity.ReceiveNotice;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReceiveNoticeMapper {

    /** 根据主键ID查询 */
    ReceiveNotice selectById(@Param("fid") Long fid);

    /** 根据单据编号查询 */
    ReceiveNotice selectByBillNo(@Param("billNo") String billNo);

    int insert(ReceiveNotice receiveNotice);

    int updateById(ReceiveNotice receiveNotice);

    int deleteById(@Param("fid") Long fid);

    /** 查询所有收料通知单 */
    List<ReceiveNotice> selectList();

    /** 分页查询收料通知单 */
    List<ReceiveNotice> selectListByPage(@Param("offset") int offset, @Param("pageSize") int pageSize);

    /** 条件分页查询收料通知单 */
    List<ReceiveNotice> selectListByConditionPage(@Param("pageNum") int pageNum,
                                                 @Param("pageSize") int pageSize,
                                                 @Param("receiveNotice") ReceiveNotice receiveNotice);

    /** 批量插入收料通知单 */
    int batchInsert(@Param("list") List<ReceiveNotice> list);

    /** 批量更新收料通知单 */
    int batchUpdateById(@Param("list") List<ReceiveNotice> list);

}
