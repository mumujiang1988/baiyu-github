package com.ruoyi.business.util;

import org.springframework.stereotype.Component;

/**
 * 提示词工具类
 * 用于生成各种场景下的AI提示词
 */
@Component
public class PromptTemplateUtil {

    /**
     * 生成中文翻译成英文的提示词模板
     *
     * @param chineseText 待翻译的中文文本
     * @return 完整的提示词
     */
    public String generateChineseToEnglishTranslationPrompt(String chineseText) {
        return String.format("请将以下中文内容翻译成英文：\n%s\n\n要求：\n" +
            "1. 保持原意准确无误\n" +
            "2. 使用专业术语（如适用）\n" +
            "3. 只返回翻译结果，不要添加其他内容", chineseText);
    }

    /**
     * 生成物料描述翻译提示词
     *
     * @param materialName 物料名称
     * @param specification 规格型号
     * @return 完整的提示词
     */
    public String generateMaterialDescriptionTranslationPrompt(String materialName, String specification) {
        return String.format("物料名称：%s\n规格型号：%s\n\n" +
                "请根据以上物料信息编写一段英文使用说明，要求：\n" +
                "1. 包含物料的用途和功能描述\n" +
                "2. 如果是技术类产品，请简要说明主要技术参数\n" +
                "3. 使用工业/制造业专业术语\n" +
                "4. 保持语言简洁准确，长度在2-3句之间\n" +
                "5. 只返回英文使用说明内容，并适当添加其他修饰语言信息\n" +
                "6. 以\"This product\"或\"The item\"开头",
            materialName != null ? materialName : "未提供",
            specification != null ? specification : "未提供");
    }

    /**
     * 生成严格的输出格式提示词
     *
     * @param instruction 指令内容
     * @param outputRequirements 输出要求
     * @return 完整的提示词
     */
    public String generateStrictOutputPrompt(String instruction, String outputRequirements) {
        return String.format("%s\n\n输出要求：\n%s\n" +
                "重要：严格遵守以上输出要求，不要添加任何额外说明或内容",
            instruction, outputRequirements);
    }

    /**
     * 生成数据处理提示词
     *
     * @param dataType 数据类型
     * @param processingInstruction 处理指令
     * @return 完整的提示词
     */
    public String generateDataProcessingPrompt(String dataType, String processingInstruction) {
        return String.format("您将收到一组%s数据，请按以下要求处理：\n%s\n\n" +
                "处理完成后，按原始数据格式返回结果，不要添加其他内容。",
            dataType, processingInstruction);
    }

    /**
     * 生成批量翻译提示词
     *
     * @param itemsCount 项目数量
     * @return 完整的提示词
     */
    public String generateBatchTranslationPrompt(int itemsCount) {
        return String.format("请将以下%d条物料描述从中文翻译成英文：\n" +
            "要求：\n" +
            "1. 按行对应翻译\n" +
            "2. 保持专业术语准确性\n" +
            "3. 只返回翻译结果，不要添加序号或其他内容\n" +
            "4. 如遇到不确定的词汇，请保持原样", itemsCount);
    }

    /**
     * 生成通用翻译提示词
     *
     * @return 通用翻译提示词
     */
    public String generateGeneralTranslationPrompt() {
        return "请将提供的中文内容翻译成英文，要求：\n" +
            "1. 准确传达原意\n" +
            "2. 使用专业、正式的语言风格\n" +
            "3. 只返回翻译结果，不要添加解释或其他内容";
    }

    /**
     * 生成带示例的提示词
     *
     * @param exampleInput 示例输入
     * @param exampleOutput 示例输出
     * @param actualInput 实际输入
     * @return 完整的提示词
     */
    public String generateWithExamplePrompt(String exampleInput, String exampleOutput, String actualInput) {
        return String.format("请参考以下示例进行处理：\n\n" +
                "示例输入：\n%s\n\n" +
                "示例输出：\n%s\n\n" +
                "请按上述格式处理以下内容：\n%s\n\n" +
                "重要：严格按照示例的格式和风格输出，不要添加其他内容",
            exampleInput, exampleOutput, actualInput);
    }

    /**
     * 获取通用提示词
     *
     * @return 通用提示词
     */
    public String getGeneralPrompt() {
        return "你是一个专业、详尽的智能助手。请根据用户的问题提供准确、完整的回答。要求：\n" +
            "1. 回答内容要详细充实,避免过于简略\n" +
            "2. 如果问题涉及具体事实,请提供相关背景信息\n" +
            "3. 回答要有条理性,可以使用编号或项目符号\n" +
            "4. 如有不确定的信息,请明确说明\n" +
            "5. 回答长度应足够充分,确保信息完整\n" +
            "请根据以上要求回答以下问题：";
    }

    /**
     * 生成图片外观描述提示词
     * 用于大模型分析图片并生成详细的外观描述
     *
     * @return 图片外观描述提示词
     */
    public String generateImageAppearancePrompt() {
        return "请仔细观察这张图片,并提供详细的外观描述。要求：\n" +
            "1. 描述物体的整体形状、尺寸特征\n" +
            "2. 详细说明颜色、材质、表面特征\n" +
            "3. 描述关键的结构特征和细节\n" +
            "4. 如果是产品,请说明主要组成部分\n" +
            "5. 使用专业、准确的描述性语言\n" +
            "6. 描述应简洁明了,字数在400字以内\n" +
            "请只返回外观描述内容,不要添加其他说明。";
    }

    /**
     * 生成物料图片外观描述提示词
     * 专门用于工业/制造业物料的外观描述
     *
     * @return 物料图片外观描述提示词
     */
    public String generateMaterialImageAppearancePrompt() {
        return "请识别图片中的物料并生成专业的外观描述。要求：\n" +
            "1. 识别物料的类型和用途\n" +
            "2. 描述物料的颜色、材质、表面处理、数量\n" +
            "3. 说明物料的形状、结构特征\n" +
            "4. 如有明显的规格或尺寸特征,请说明\n" +
            "5. 使用工业/制造业专业术语\n" +
            "6. 描述要客观、准确,避免主观评价\n" +
            "7. 描述应简洁明了,字数在400字以内\n" +
            "请只返回外观描述内容,不要添加标题或其他说明。";
    }
}
