package com.ruoyi.business.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI对话对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AIConversation {
    private String userId;           // 用户ID
    private String sessionId;   // 对话ID
    private String modelName;        // 大模型名称
    private String businessType; //业务标识,业务类型（普通问答=SIMPLE_QNA / 知识库=KNOWLEDGE_BASE / 图片识别=FILE_IDENTIFICATION）
    private String knowledgeBaseName;     // 知识库名称
    private String userQuestion;          // 用户问题
    private MultipartFile file;          // 图片
    private String aiResponse;            // 大模型回答内容


    @Override
    public String toString() {
        return "AIConversation{" +
                "userId='" + userId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", modelName='" + modelName + '\'' +
                ", businessType=" + businessType +
                ", knowledgeBaseName='" + knowledgeBaseName + '\'' +
                ", userQuestion='" + userQuestion + '\'' +
                ", aiResponse='" + aiResponse + '\'' +
                '}';
    }
}
