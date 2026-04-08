package com.ruoyi.business.mapper;
import com.ruoyi.business.k3.domain.entity.ReceivebillEntry;
import com.ruoyi.business.k3.domain.entity.SalOutboundDetails;
import com.ruoyi.business.k3.domain.vo.SalOutboundDetailsVo;
import com.ruoyi.common.mybatis.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SalOutboundDetailMapper extends BaseMapperPlus<SalOutboundDetails, SalOutboundDetailsVo> {

    List<SalOutboundDetails> selectByIds(@Param("fentryId") Long fentryId);

}
