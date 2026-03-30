package com.ruoyi.business.mapper;

import com.ruoyi.business.entity.AIConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AIConversationMapper {

    /**
     * 插入一条对话记录
     */
    int insert(AIConversation conversation);

    /**
     * 根据 sessionId 查询对话列表
     */
    List<AIConversation> selectBySessionId(@Param("sessionId") String sessionId);

    /**
     * 根据 userId 查询用户所有对话
     */
    List<AIConversation> selectByUserId(@Param("userId") String userId);

    /**
     * 根据 sessionId 更新对话记录
     */
    int updateBySessionId(AIConversation conversation);

    /**
     * 根据 sessionId 删除对话记录
     */
    int deleteBySessionId(@Param("sessionId") String sessionId);

}
