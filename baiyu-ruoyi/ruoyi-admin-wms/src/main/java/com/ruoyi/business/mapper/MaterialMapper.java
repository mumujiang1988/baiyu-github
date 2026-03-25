package com.ruoyi.business.mapper;



import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.ruoyi.business.entity.Bymaterial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


import java.util.List;

@Mapper
public interface MaterialMapper extends BaseMapper<Bymaterial> {

    Bymaterial selectById(@Param("id") Long id);

    Bymaterial selectByNumber(@Param("number") String number);

    Bymaterial selectByFmaterialId(@Param("fmaterialId") String fmaterialId);

    List<Bymaterial> selectByCondition(@Param("offset") int offset, @Param("size") int size, @Param("isAsc") String isAsc,@Param("bymaterial") Bymaterial bymaterial);

    long countByCondition(@Param("bymaterial") Bymaterial bymaterial);
    List<Bymaterial> selectByConditionList();

    // 根据编码删除物料
    int deleteByNumber(@Param("id") Long id);
    int updateByNumber(Bymaterial material);
    //交付红线
    int updateBymaterial(Bymaterial material);

}
