package com.tihai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @Copyright : DuanInnovator
 * @Description :
 * @Author : DuanInnovator
 * @CreateTime : 2025/5/11
 * @Link : <a href="https://github.com/DuanInnovator/SuperTiKu">...</a>
 **/
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        // 使用 Apache HttpClient（需引入依赖）
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    }

}

