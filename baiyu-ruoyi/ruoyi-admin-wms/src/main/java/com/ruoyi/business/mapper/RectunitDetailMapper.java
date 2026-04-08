package com.ruoyi.business.mapper;

import com.ruoyi.business.k3.domain.entity.RectunitDetail;
import com.ruoyi.business.k3.domain.vo.RectunitDetailVo;
import com.ruoyi.common.mybatis.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RectunitDetailMapper extends BaseMapperPlus<RectunitDetail,RectunitDetailVo> {

    List<RectunitDetail> selectByEntryId(@Param("fentryId") Long fentryId);

}
