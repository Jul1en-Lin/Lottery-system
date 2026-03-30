package com.julien.lotterysystem.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置类
 * 用于异步处理中奖通知等任务
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {

    /**
     * 核心线程数：CPU核心数
     */
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    /**
     * 最大线程数
     */
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2;
    /**
     * 队列容量
     */
    private static final int QUEUE_CAPACITY = 20;
    /**
     * 线程空闲时间（秒）
     */
    private static final int KEEP_ALIVE_SECONDS = 60;
    /**
     * 线程名前缀
     */
    private static final String THREAD_NAME_PREFIX = "lottery-async-";

    /**
     * 异步任务执行线程池
     * 用于处理中奖通知等异步任务
     */
    @Bean("lotteryAsyncExecutor")
    public Executor lotteryAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(CORE_POOL_SIZE);
        // 最大线程数
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        // 队列容量
        executor.setQueueCapacity(QUEUE_CAPACITY);
        // 线程空闲时间
        executor.setKeepAliveSeconds(KEEP_ALIVE_SECONDS);
        // 线程名前缀
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        // 拒绝策略：由调用线程处理（CallerRunsPolicy）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间
        executor.setAwaitTerminationSeconds(60);
        // 初始化
        executor.initialize();
        log.info("线程池初始化完成，核心线程数：{}，最大线程数：{}", CORE_POOL_SIZE, MAX_POOL_SIZE);
        return executor;
    }
}
