package com.ruoyi.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Map;

@Data
public class AiLlmModer implements Serializable {

    private static final long serialVersionUID = 1L;

    private BigInteger id;

    /**
     * 部门ID
     */
    @TableId(value = "部门ID")
    private BigInteger deptId;

    /**
     * 租户ID
     */
    private BigInteger tenantId;

    /**
     * 标题或名称
     */
    private String title;

    /**
     * 品牌
     */
    private String brand;

    /**
     * ICON
     */
    private String icon;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否支持对话
     */
    private Boolean supportChat;

    /**
     * 是否支持方法调用
     */
    private Boolean supportFunctionCalling;

    /**
     * 是否支持向量化
     */
    private Boolean supportEmbed;

    /**
     * 是否支持重排
     */
    private Boolean supportReranker;

    /**
     * 是否支持文字生成图片
     */
    private Boolean supportTextToImage;

    /**
     * 是否支持图片生成图片
     */
    private Boolean supportImageToImage;

    /**
     * 是否支持文字生成语音
     */
    private Boolean supportTextToAudio;

    /**
     * 是否支持语音生成语音
     */
    private Boolean supportAudioToAudio;

    /**
     * 是否支持文字生成视频
     */
    private Boolean supportTextToVideo;

    /**
     * 是否支持图片生成视频
     */
    private Boolean supportImageToVideo;

    /**
     * 大模型请求地址
     */
    private String llmEndpoint;

    /**
     * 大模型名称
     */
    private String llmModel;

    /**
     * 大模型 API KEY
     */
    private String llmApiKey;

    /**
     * 大模型其他属性配置
     */
    private String llmExtraConfig;

    /**
     * 其他配置内容
     */
    private Map<String, Object> options;

    @Override
    public String toString() {
        return "AiLlmBase{" + "id=" + id + ", deptId=" + deptId + ", tenantId=" + tenantId + ", title='" + title + '\''
            + ", brand='" + brand + '\'' + ", icon='" + icon + '\'' + ", description='" + description + '\''
            + ", supportChat=" + supportChat + ", supportFunctionCalling=" + supportFunctionCalling + ", supportEmbed="
            + supportEmbed + ", supportReranker=" + supportReranker + ", supportTextToImage=" + supportTextToImage
            + ", supportImageToImage=" + supportImageToImage + ", supportTextToAudio=" + supportTextToAudio
            + ", supportAudioToAudio=" + supportAudioToAudio + ", supportTextToVideo=" + supportTextToVideo
            + ", supportImageToVideo=" + supportImageToVideo + ", llmEndpoint='" + llmEndpoint + '\'' + ", llmModel='"
            + llmModel + '\'' + ", llmApiKey='" + llmApiKey + '\'' + ", llmExtraConfig='" + llmExtraConfig + '\''
            + ", options=" + options + '}';
    }



}
