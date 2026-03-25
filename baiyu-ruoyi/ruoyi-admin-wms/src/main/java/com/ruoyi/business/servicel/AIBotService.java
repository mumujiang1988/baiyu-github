package com.ruoyi.business.servicel;


import cn.hutool.json.JSONObject;
import com.ruoyi.business.entity.AIConversation;
import com.ruoyi.business.entity.AiLlmModer;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface AIBotService {


    List<AiLlmModer> selectAll();

    String insert(AIConversation aiConversation);

    /**
     * 图片识别并检索知识库
     * @param file 图片文件
     * @param imageUrl 图片URL
     * @return 识别结果和知识库检索结果
     */
    JSONObject recognizeImageAndSearch(MultipartFile file, String imageUrl);

    /**
     * 图片识别
     * @param imageUrl 图片URL
     * @return 图片识别结果
     */
    String recognizeImage(String imageUrl);

    /**
     * 知识库检索
     * @param query 检索问题
     * @param topN 返回结果数量
     * @return 知识库检索结果
     */
    JSONObject searchKnowledgeBase(String query, Integer topN);
}
