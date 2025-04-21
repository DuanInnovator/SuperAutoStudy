package com.tihai.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Copyright : DuanInnovator
 * @Description : 线程池配置
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/2
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "custom.thread-pool")
public class ThreadPoolProperties {
    private int coreSize;
    private int maxSize;
    private int keepAlive;
    private int queueCapacity;
    private String threadNamePrefix;
    private boolean allowCoreThreadTimeout;
}