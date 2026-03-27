package com.ruoyi.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.entity.Country;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CountryMapper extends BaseMapper<Country> {
    @Select(" SELECT id as nation,name_zh FROM country ")
    List<Country> selectList();

    @Select("SELECT * FROM country WHERE id = #{id}")
    Country selectById(@Param("id") String id);

    @Select("SELECT * FROM country WHERE name_zh = #{nameZh}")
    Country selectFcountryList(String nameZh);

}
