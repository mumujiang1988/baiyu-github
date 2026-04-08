package com.ruoyi.business.mapper;

import com.ruoyi.business.entity.FtContact;
import com.ruoyi.business.vo.FtContactVo;
import com.ruoyi.common.mybatis.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FtContactMapper extends BaseMapperPlus<FtContact,FtContactVo> {
    List<FtContact> selectByIds(@Param("ftId") String ftId);
}
