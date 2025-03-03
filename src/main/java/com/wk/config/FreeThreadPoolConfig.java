package com.wk.config;

import com.wk.factory.CustomThreadFactory;
import com.wk.factory.PriorityRejectPolicy;
import com.wk.properties.ThreadPoolProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Copyright : DuanInnovator
 * @Description : 自定义线程池
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/1
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Configuration
public class FreeThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor customThreadPool(ThreadPoolProperties config) {
        // 创建优先级阻塞队列
        BlockingQueue<Runnable> queue = new PriorityBlockingQueue<>(config.getQueueCapacity());

        // 构建线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                config.getCoreSize(),
                config.getMaxSize(),
                config.getKeepAlive(),
                TimeUnit.SECONDS,
                queue,
                new CustomThreadFactory(config.getThreadNamePrefix()),
                new PriorityRejectPolicy()
        );

        // 允许核心线程超时
        executor.allowCoreThreadTimeOut(config.isAllowCoreThreadTimeout());

        return executor;
    }

    @Bean
    public TaskExecutor taskExecutor(ThreadPoolExecutor customThreadPool) {
        return new ConcurrentTaskExecutor(customThreadPool);
    }
}
