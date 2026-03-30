package com.ruoyi.business.util;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义线程池工具类
 */
public class ThreadPoolUtil {

    /**
     * 创建固定大小的线程池
     * @param corePoolSize 核心线程数
     * @param maximumPoolSize 最大线程数
     * @param keepAliveTime 空闲线程存活时间
     * @param unit 时间单位
     * @param queueSize 队列大小
     * @param threadNamePrefix 线程名称前缀
     * @return ThreadPoolExecutor实例
     */
    public static ThreadPoolExecutor createThreadPool(int corePoolSize,
                                                     int maximumPoolSize,
                                                     long keepAliveTime,
                                                     TimeUnit unit,
                                                     int queueSize,
                                                     String threadNamePrefix) {
        return new ThreadPoolExecutor(
            corePoolSize,
            maximumPoolSize,
            keepAliveTime,
            unit,
            new LinkedBlockingQueue<>(queueSize),
            new NamedThreadFactory(threadNamePrefix),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 创建固定大小线程池（默认配置）
     * @param threadNamePrefix 线程名称前缀
     * @return ThreadPoolExecutor实例
     */
    public static ThreadPoolExecutor createFixedThreadPool(String threadNamePrefix) {
        int processors = Runtime.getRuntime().availableProcessors();
        return createThreadPool(
            processors,
            processors,
            60L,
            TimeUnit.SECONDS,
            1000,
            threadNamePrefix
        );
    }

    /**
     * 自定义线程工厂，用于设置线程名称
     */
    static class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        NamedThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + "-thread-" + threadNumber.getAndIncrement());
            t.setDaemon(false);
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    /**
     * 安全关闭线程池
     * @param executor 线程池实例
     */
    public static void shutdown(ThreadPoolExecutor executor) {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                        System.err.println("线程池未能正常关闭");
                    }
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
