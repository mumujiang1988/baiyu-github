package com.ruoyi.business.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.ruoyi.business.entity.AiLlmModer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AiLlmModeMapper extends BaseMapper<AiLlmModer> {

    /**
     * 根据标题查询模型
     * @param title
     * @return
     */
    @Select("select * from tb_ai_llm where title = #{title}")
    AiLlmModer selectByTitle(@Param("title") String title);

    /**
     * 查询所有模型
     * @return
     */
    @Select("select * from tb_ai_llm where support_chat=1")
    List<AiLlmModer> selectAll();
}
