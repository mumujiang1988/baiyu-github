package com.ruoyi.business.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.k3.domain.entity.FbillHead;
import com.ruoyi.business.k3.domain.vo.FbillHeadVo;
import com.ruoyi.common.mybatis.annotation.DataColumn;
import com.ruoyi.common.mybatis.annotation.DataPermission;
import com.ruoyi.common.mybatis.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FbillHeadMapper extends BaseMapperPlus<FbillHead, FbillHeadVo> {

    /**
     * 根据条件分页查询采购调价列表
     *
     * @param queryWrapper 查询条件
     * @return 采购调价集合信息
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "d.dept_id"),
        @DataColumn(key = "userName", value = "su.user_id")
    })
    Page<FbillHead> selectByCondition(@Param("page") Page<FbillHead> page, @Param(Constants.WRAPPER) Wrapper<FbillHead> queryWrapper);

    FbillHead selectByIds(@Param("id") Long id);

}
