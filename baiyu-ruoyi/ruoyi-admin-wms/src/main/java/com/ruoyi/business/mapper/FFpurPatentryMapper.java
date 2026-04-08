package com.ruoyi.business.mapper;
import com.ruoyi.business.k3.domain.entity.FFpurPatentry;
import com.ruoyi.business.k3.domain.vo.FFpurPatentryVo;
import com.ruoyi.common.mybatis.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FFpurPatentryMapper extends BaseMapperPlus<FFpurPatentry, FFpurPatentryVo> {

    /**
     * 根据条件分页查询采购调价明细列表
     *
     * @param fNumber 查询条件
     * @return 采购调价明细集合信息
     */
    List<FFpurPatentry> selectByCondition(@Param("fNumber") String fNumber);


}
