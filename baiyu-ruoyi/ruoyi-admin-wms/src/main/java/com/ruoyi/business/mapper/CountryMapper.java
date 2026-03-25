package com.ruoyi.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.entity.Country;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CountryMapper extends BaseMapper<Country> {
    @Select(" SELECT id, name_zh, name_en FROM country WHERE status = 1 ORDER BY name_zh ASC ")
    List<Country> selectList();

    @Select("SELECT * FROM country WHERE id = #{id}")
    Country selectById(@Param("id") String id);

    @Select("SELECT * FROM country WHERE name_zh = #{nameZh}")
    Country selectFcountryList(String nameZh);
    
    /**
     * 模糊搜索国家（支持中文和英文）
     */
    @Select("<script>" +
            "SELECT id, name_zh, name_en FROM country " +
            "WHERE status = 1 " +
            "AND (name_zh LIKE CONCAT('%', #{keyword}, '%') OR name_en LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY name_zh ASC" +
            "</script>")
    List<Country> searchNation(@Param("keyword") String keyword);

}
