package com.ruoyi.business.config;
import com.ruoyi.business.servicel.AIConversationHandler;
import com.ruoyi.business.servicel.impl.FileIdentificationHandler;
import com.ruoyi.business.servicel.impl.KnowledgeBaseHandler;
import com.ruoyi.business.servicel.impl.SimpleQnAHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


@Component
public class AIConversationHandlerFactory {

    private final ApplicationContext applicationContext;

    // 构造注入 ApplicationContext
    public AIConversationHandlerFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public AIConversationHandler getHandler(String businessType) {
        switch (businessType) {
            case "SIMPLE_QNA":
                return applicationContext.getBean(SimpleQnAHandler.class);
            case "KNOWLEDGE_BASE":
                return applicationContext.getBean(KnowledgeBaseHandler.class);
            case "FILE_IDENTIFICATION":
                return applicationContext.getBean(FileIdentificationHandler.class);
            default:
                throw new IllegalArgumentException("未知业务类型: " + businessType);
        }
    }
}


