package com.ruoyi.business.mapper;

import com.ruoyi.business.entity.FtCustomer;
import com.ruoyi.business.vo.FtCustomerVo;
import com.ruoyi.common.mybatis.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FtCustomerMapper extends BaseMapperPlus<FtCustomer, FtCustomerVo> {

    FtCustomer selectByIds(@Param("id") Long id);
}
