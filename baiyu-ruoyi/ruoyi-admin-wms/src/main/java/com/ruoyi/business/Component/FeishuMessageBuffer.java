package com.ruoyi.business.Component;

import com.ruoyi.business.feishu.service.FeishuDirectSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

@Component
public class FeishuMessageBuffer {

    private final Map<String, BlockingQueue<String>> buffer = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler =
        Executors.newSingleThreadScheduledExecutor();

    @Autowired
    private FeishuDirectSender directSender;

    public FeishuMessageBuffer() {

        scheduler.scheduleAtFixedRate(() -> {
            buffer.forEach((chatId, queue) -> {
                List<String> chunks = new ArrayList<>();
                queue.drainTo(chunks);
                if (chunks.isEmpty()) return;

                String merged = String.join("", chunks);

                try {
                    directSender.sendTextDirect(chatId, merged);
                } catch (Exception ignored) {}
            });
        }, 200, 250, TimeUnit.MILLISECONDS);
    }

    public void add(String chatId, String delta) {
        buffer.computeIfAbsent(chatId, k -> new LinkedBlockingQueue<>()).add(delta);
    }
}
