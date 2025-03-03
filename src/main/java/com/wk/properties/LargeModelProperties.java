package com.wk.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Copyright : DuanInnovator
 * @Description : 大模型配置
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/3
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@ConfigurationProperties(prefix = "large.model")
@Configuration
@Data
public class LargeModelProperties {

    /**
     * QWen Api Key
     * TODO 将引入OpenAI等
     */
    private String QWenApiKey;
}

