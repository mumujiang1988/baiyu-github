package com.ruoyi.business.mapper;

import com.ruoyi.business.k3.domain.entity.ReceivebillEntry;
import com.ruoyi.business.k3.domain.vo.ReceivebillEntryVo;
import com.ruoyi.common.mybatis.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReceivebillEntryMapper extends BaseMapperPlus<ReceivebillEntry, ReceivebillEntryVo> {
    List<ReceivebillEntry> selectByIds(@Param("fentryId") String fentryId);
}
